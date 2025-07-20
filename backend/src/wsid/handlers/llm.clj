(ns wsid.handlers.llm
  (:require
   [wsid.util :refer [ok response]]
   [clojure.string :as string]
   [cheshire.core :as json]
   [wsid.config :refer [config]]
   [clj-http.client :as http]
   [clojure.spec.alpha :as s]
   [wsid.specs.llm]))

(def providers
  {:deepinfra
   {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"
    :extract-response-fn (fn [r] (-> r :choices first :message :content))
    :token (:llm-token-deepinfra config)}})

(def models
  {"mistralai/Mistral-Small-3.2-24B-Instruct-2506"
   {:process-fn (fn mistal [r] r)}}) ; TODO - meaningful process of the responses


#_(def large-prompt (slurp "llm-experiments/prompts/wsid-job-edn-output"))

#_(defn get-message-content
  "Get the message content from the JSON response from the DeepInfra API"
  [r] (-> r :choices first :message :content))

#_(defn remove-think [r] (string/replace r #"(?s)<think>.*</think>" ""))

(defn build-request-params
  "Builds a map of request parameters. This map is for internal use during the
   request and has all the information needed to build a request to the LLM service,
   get parameter and model configurations."
  [request]
  (let [request-body (:request-body request)
        prompt (:prompt request-body)
        prompt-template (:prompt-template request-body)
        prompt-parameters (:prompt-parameters request-body)
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
        prompt (or (:prompt params) (:prompt-template params))
        llm-request-body {:model model-name
                          :messages [{:role "user" :content prompt}]}]
    
    ;; Validate final structure with spec (defensive programming)
    (when-not (s/valid? :llm.api/request-body llm-request-body)
      (throw (Exception. (str "Invalid LLM request body: " 
                              (s/explain-str :llm.api/request-body llm-request-body)))))
    
    llm-request-body))

(defn make-llm-http-request
  "Makes the HTTP request to the LLM API endpoint."
  [request-params request-body]
  (http/post (get-in request-params [:provider-config :endpoint-url])
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
    (ok {:response (-> (:body http-response)
                       extract-response-fn
                       process-fn)})
    ;; HTTP error case
    (response 500 {:message (get-in http-response [:body :message] "HTTP request failed")
                   :body (:body http-response)}))))

(defn llm-prompt-handler [request]
  (try
    (let [request-params (build-request-params request)
          request-body (build-llm-request-body request-params)
          http-response (make-llm-http-request request-params request-body)]
      (process-llm-response request-params http-response))

    (catch Exception e
      ;; Exception case - most likely a request validation error
      (response 500 {:message (.getMessage e)
                     :type :exception}))))
