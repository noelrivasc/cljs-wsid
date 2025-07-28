(ns wsid.handlers.llm
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http-client]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [io.pedestal.http :as http]
   [wsid.specs.llm]
   [wsid.util.config :refer [config]]
   [wsid.util.prompts :refer [get-prompt-template]]))

(def providers
  {:deepinfra
   {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"
    :extract-response-fn (fn [r] (-> r :choices first :message :content))
    :token (:llm-token-deepinfra config)}})

;; BASE process functions
;; These are meant to be blocks that are assembled to compose a processing pipeline
(defn- default-process-fn [r] r)
(defn- remove-think [r] (string/replace r #"(?s)<think>.*</think>" ""))
(defn- remove-md-code-delimiters [r] (second (re-find #"(?is)```(?:edn|clojure)(.*)```" r)))
#_(defn- validate-edn 
  "Parses an edn string to validate it, and re-encodes as string on success.
   Throws an error if r is not valid edn."
  [r] (when-let [parsed (read-string r)]
                          (pr-str parsed)))


;; COMPOSITE process functions
;; TODO - implement error handling
(defn- process--wsid-1--mistral
  "Removes the markdown code fences (ie ```clojure), and then parses the string as
   EDN, returning the output of parsing. Throws on parse error."
  [r] (-> r
                               remove-md-code-delimiters
                               read-string))

(def ^:private process-fns 
  {:default-process-fn default-process-fn
   :remove-think remove-think
   :remove-md-code-delimiters remove-md-code-delimiters
   
   :process--wsid-1--mistral process--wsid-1--mistral})

(def models ["mistralai/Mistral-Small-3.2-24B-Instruct-2506"])

(defn- substitute-template
  "Takes a template string with %%key%% placeholders and a map of parameters.
   Returns the template with all placeholders replaced by their corresponding values."
  [template params]
  (reduce (fn [text [key value]]
            (clojure.string/replace text (str "%%" (name key) "%%") (str value)))
          template
          params))

(defn- get-process-fn
  "Gets a process function by string name. If fn-name is nil, returns the default function.
   If fn-name is a string but the function is not found, throws an exception."
  [fn-name]
  (if (nil? fn-name)
    default-process-fn
    (if-let [process-fn ((keyword fn-name) process-fns)]
      process-fn
      (throw (Exception. "Error getting the process function.")))))


(defn- build-llm-request-body
  "Builds the LLM request body to call the LLM service.
  The input params are assumed to be valid."
  [request-params]
  (let [model-name (:model-name request-params)
        prompt (if (:prompt request-params)
                 (:prompt request-params)
                 (let [t (get-prompt-template (:prompt-template request-params))
                       p (:prompt-parameters request-params)]
                   (substitute-template t p)))
        llm-request-body {:model model-name
                          :messages [{:role "user" :content prompt}]}]
    
    ;; Validate final structure with spec
    (when-not (s/valid? :llm.api/request-body llm-request-body)
      (throw (Exception. (str "Invalid LLM request body: " 
                              (s/explain-str :llm.api/request-body llm-request-body)))))
    
    llm-request-body))

(defn- make-llm-http-request
  "Makes the HTTP request to the LLM API endpoint."
  [request-params request-body]
  (http-client/post (get-in request-params [:provider-config :endpoint-url])
             {:body (json/generate-string request-body)
              :headers {"Content-Type" "application/json"
                        "Authentication" (str "Bearer "
                                              (get-in request-params [:provider-config :token]))}
              :throw-exceptions false
              :as :json}))

(defn- process-llm-response
  "Processes the LLM response message using the model's processing function."
  [request-params http-response]
  (let [extract-response-fn (get-in request-params [:provider-config :extract-response-fn])
        process-fn (:process-fn request-params)]
    (if (<= 200 (:status http-response) 299)
      ;; Success case
      {:success true :response {:llm-message (-> (:body http-response)
                                    extract-response-fn
                                    process-fn)}}
      ;; HTTP error case
      {:success false :response {:message (get-in http-response [:body :message] "HTTP request failed")
                                 :body (:body http-response)}})))

(defn- do-prompt
  "Query LLM services and process the output message.
   Returns the string that results from processing the LLM output."
  [request-params]
  (let [request-body (build-llm-request-body request-params)
        http-response (make-llm-http-request request-params request-body)
        extracted-response (process-llm-response request-params http-response)]
    extracted-response))

(defn- build-request-params
  "Builds a map of request parameters. This map is for internal use during the
   request and has all the information needed to build a request to the LLM service,
   get parameter and model configurations."
  [{:keys [prompt prompt-template prompt-parameters provider model process-fn]}]
  (let [; Convert prompt parameter keys to keywords
        prompt-parameters (when-let [pp prompt-parameters]
                            (into {} (map (fn [[k v]] [(keyword k) v]) pp)))
        provider-keyword (keyword provider)
        provider-config (provider-keyword providers)
        model-name (some #{model} models)
        process-fn (get-process-fn process-fn)
        params {:prompt prompt
                :prompt-template prompt-template
                :prompt-parameters prompt-parameters
                :model-name model-name
                :process-fn process-fn
                :provider-name provider
                :provider-config provider-config}]

    ;; Validate the params using spec
    (if (s/valid? :llm.params/request-params params)
      params
      (throw (Exception. (str "Invalid request parameters: "
                              (s/explain-str :llm.params/request-params params)))))))

(def prompt
  "Handle LLM requests, allowing the client to configure the prompt or template,
   the template parameters and the process function applied to the output."
  {:name :llm-prompt
   :enter (fn [context]
            (let [request (:request context)
                  request-body (:request-body request)
                  request-params (build-request-params request-body)
                  extracted-response (do-prompt request-params)]
              (if (:success extracted-response)
                (http/respond-with context 200 (:response extracted-response))
                (http/respond-with context 500 (:response extracted-response)))))})

(def wsid-1--mistral
  "Run the wsid-1 prompt against the mistralai/Mistral-Small-3.2-24B-Instruct-2506 model
   and process its output to return structured data."
  {:name :llm-wsid-1--mistral
   :enter (fn [context]
            (let [user-situation (get-in context [:request :request-body :user-situation])
                  request-params (build-request-params
                                  {:prompt-template "wsid-1"
                                   :prompt-parameters {:user-situation user-situation}
                                   :provider "deepinfra"
                                   :model "mistralai/Mistral-Small-3.2-24B-Instruct-2506"
                                   :process-fn :process--wsid-1--mistral})
                  llm-response (do-prompt request-params)]
              (if (:success llm-response)
                (http/respond-with context 200 (:response llm-response))
                (http/respond-with context 500 (:response llm-response)))))})