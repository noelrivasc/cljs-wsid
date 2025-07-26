(ns wsid.handlers.llm
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http-client]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [io.pedestal.http :as http]
   [wsid.util.config :refer [config]]
   [wsid.specs.llm]
   [wsid.util.prompts :refer [get-prompt-template]]))

(def providers
  {:deepinfra
   {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"
    :extract-response-fn (fn [r] (-> r :choices first :message :content))
    :token (:llm-token-deepinfra config)}})

(def models
  {"mistralai/Mistral-Small-3.2-24B-Instruct-2506"
   {:process-fn (fn mistal [r] r)}}) ; TODO - meaningful process of the responses

(defn- substitute-template
  "Takes a template string with %%key%% placeholders and a map of parameters.
   Returns the template with all placeholders replaced by their corresponding values."
  [template params]
  (reduce (fn [text [key value]]
            (clojure.string/replace text (str "%%" (name key) "%%") (str value)))
          template
          params))

#_(defn remove-think [r] (string/replace r #"(?s)<think>.*</think>" ""))

(defn- build-request-params
  "Builds a map of request parameters. This map is for internal use during the
   request and has all the information needed to build a request to the LLM service,
   get parameter and model configurations."
  [request]
  (let [request-body (:request-body request)
        prompt (:prompt request-body)
        prompt-template (:prompt-template request-body)
        ; Ensure keywords for prompt parameters; useful for JSON input
        prompt-parameters (when-let [pp (:prompt-parameters request-body)]
                            (into {} (map (fn [[k v]] [(keyword k) v]) pp)))
        provider-name (:provider request-body)
        provider-keyword (keyword provider-name)
        provider-config (provider-keyword providers)
        model-name (:model request-body)
        model-config (get models model-name)
        params {:prompt prompt
                :prompt-template prompt-template
                :prompt-parameters prompt-parameters
                :model-name model-name
                :model-config model-config
                :provider-name provider-name
                :provider-config provider-config}]
    
    ;; Validate the params using spec
    (if (s/valid? :llm.params/request-params params)
      params
      (throw (Exception. (str "Invalid request parameters: " 
                              (s/explain-str :llm.params/request-params params)))))))

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
        process-fn (get-in request-params [:model-config :process-fn])]
    (if (<= 200 (:status http-response) 299)
      ;; Success case
      {:success true :response (-> (:body http-response)
                                   extract-response-fn
                                   process-fn)}
      ;; HTTP error case
      {:success false :response {:message (get-in http-response [:body :message] "HTTP request failed")
                                 :body (:body http-response)}})))

(def prompt
  "Handle LLM requests."
  {:name :llm-prompt-handler
   :enter (fn [context]
            (let [request (:request context)
                  request-params (build-request-params request)
                  request-body (build-llm-request-body request-params)
                  http-response (make-llm-http-request request-params request-body)
                  extracted-response (process-llm-response request-params http-response)]

              (if (:success extracted-response)
                (http/respond-with context 200 (:response extracted-response))
                (http/respond-with context 500 (:response extracted-response)))))})
