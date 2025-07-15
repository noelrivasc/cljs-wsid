(ns wsid.handlers.llm
  (:require
   [wsid.util :refer [ok response]]
   [cheshire.core :as json]
   [clj-http.client :as http]))

(def providers
  {:deepinfra
   {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"
    :models-enabled ["deepseek-ai/DeepSeek-R1-0528"]}})

(defn llm-prompt-parse-request
  "Parses the request and either builds a map with valid prepared data
  to run a query to the LLM service, or returns an error"
  [request]
  (let [request-body (:request-body request)
        prompt (:prompt request-body)
        provider-keyword (keyword (:provider request-body))
        provider (provider-keyword providers)
        model (:model request-body)]

    (assert (map? provider)
            "Error: provider not found.")
    (assert (not-empty prompt)
            "Error: prompt must not be empty.")
    (assert (some #(= % model) (:models-enabled provider))
            (str "Error: model " model " not enabled for provider " provider-keyword "."))

    {:prompt prompt
     :model model
     :endpoint-url (:endpoint-url provider)
     :provider provider}))

(defn llm-prompt-handler [request]
  (try
    (let [parsed-input (llm-prompt-parse-request request)
          llm-request-body {:model (:model parsed-input)
                            :messages [{:role "user" :content (:prompt parsed-input)}]}
          response (http/post (:endpoint-url parsed-input)
                              {:body (json/generate-string llm-request-body)
                               :headers {"Content-Type" "application/json"}
                               :throw-exceptions false ; Don't throw on HTTP errors
                               :as :json})] ; Parse JSON response automatically

      (if (<= 200 (:status response) 299)
        ;; Success case - return just the body
        ;; TODO - process response
        (ok (:body response))

        ;; HTTP error case - return error info
        (response 500 {:message (get-in response [:body :message] "HTTP request failed")
                       :body (:body response)})))

    (catch Exception e
      ;; Exception case - network error, malformed JSON, etc.
      (response 500 {:message (.getMessage e)
                     :type :exception}))))
