(ns wsid.handlers.llm
  (:require
   [cheshire.core :as json]
   [io.pedestal.http :as http]
   [clj-http.client :as http-client]
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [clojure.string :as string]
   [wsid.config :refer [config]]
   [wsid.logging :as logging :refer [debug-timing] :rename {debug-timing dt}]
   [wsid.specs.llm]))

(def providers
  {:deepinfra
   {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"
    :extract-response-fn (fn [r] (-> r :choices first :message :content))
    :token (:llm-token-deepinfra config)}})

(def models
  {"mistralai/Mistral-Small-3.2-24B-Instruct-2506"
   {:process-fn (fn mistal [r] r)}}) ; TODO - meaningful process of the responses

(defn get-prompt-template
  "Gets the contents of a template that matches the given keyword, if the
   template exists. Throws an error otherwise."
  [template-id]
  (slurp (io/resource (str "wsid/prompt-templates/" (name template-id)))))

(defn substitute-template
  "Takes a template string with %%key%% placeholders and a map of parameters.
   Returns the template with all placeholders replaced by their corresponding values."
  [template params]
  (reduce (fn [text [key value]]
            (clojure.string/replace text (str "%%" (name key) "%%") (str value)))
          template
          params))

#_(defn remove-think [r] (string/replace r #"(?s)<think>.*</think>" ""))

(defn build-request-params
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

(defn build-llm-request-body
  "Builds the LLM request body from validated params.
  Since params are already validated, this function trusts all input is valid."
  [params]
  (let [model-name (:model-name params)
        prompt (if (:prompt params)
                 (:prompt params)
                 (let [t (get-prompt-template (:prompt-template params))
                       p (:prompt-parameters params)]
                   (substitute-template t p)))
        llm-request-body {:model model-name
                          :messages [{:role "user" :content prompt}]}]
    
    ;; Validate final structure with spec
    (when-not (s/valid? :llm.api/request-body llm-request-body)
      (throw (Exception. (str "Invalid LLM request body: " 
                              (s/explain-str :llm.api/request-body llm-request-body)))))
    
    llm-request-body))

(defn make-llm-http-request
  "Makes the HTTP request to the LLM API endpoint."
  [request-params request-body]
  (http-client/post (get-in request-params [:provider-config :endpoint-url])
             {:body (json/generate-string request-body)
              :headers {"Content-Type" "application/json"
                        "Authentication" (str "Bearer "
                                              (get-in request-params [:provider-config :token]))}
              :throw-exceptions false
              :as :json}))

(defn process-llm-response
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

(def llm-prompt-handler
  "Handle LLM requests."
  {:name :llm-prompt-handler
   :enter (fn [context]
            (dt context "LLM handler starts")
            (let [request (:request context)
                  request-params (build-request-params request)
                  request-body (build-llm-request-body request-params)
                  http-response (make-llm-http-request request-params request-body)
                  extracted-response (process-llm-response request-params http-response)]

              (dt context "LLM handler response")

              (if (:success extracted-response)
                (http/respond-with context 200 (:response extracted-response))
                (http/respond-with context 500 (:response extracted-response)))))})

(defn extract-template-variables
  "Extracts variable names from a template string that are delimited by %%var-name%%"
  [template-content]
  (->> template-content
       (re-seq #"%%([^%]+)%%")
       (map second)
       (into [])))

(defn list-template-files
  "Lists all files in the wsid/prompt-templates/ resource directory"
  []
  (let [resource-path "wsid/prompt-templates/"
        resource-url (io/resource resource-path)]
    (when resource-url
      (let [template-dir (io/file (.getPath resource-url))]
        (when (.exists template-dir)
          (->> (.listFiles template-dir)
               (filter #(.isFile %))
               (map #(.getName %))
               (into [])))))))

(defn build-templates-map
  "Builds a map of template names to their variables"
  []
  (let [template-files (list-template-files)]
    (reduce (fn [acc template-name]
              (try
                (let [template-content (get-prompt-template (keyword template-name))
                      variables (extract-template-variables template-content)]
                  (assoc acc template-name variables))
                (catch Exception e
                  (println (str "Error processing template " template-name ": " (.getMessage e)))
                  acc)))
            {}
            template-files)))

(def llm-list-prompt-templates
  "List the existing prompt templates with the options they take."
  {:name :llm-list-prompt-templates
   :enter (fn [context]
            (dt context "LLM list promp templates starts")
            (let [templates-map (build-templates-map)]
              (dt context "LLM list prompt templates completed")
              (http/respond-with context 200 templates-map)))})