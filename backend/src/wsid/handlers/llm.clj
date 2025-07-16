(ns wsid.handlers.llm
  (:require
   [wsid.util :refer [ok response]]
   [clojure.string :as string]
   [cheshire.core :as json]
   [wsid.config :refer [config]]
   [clj-http.client :as http]))

(def providers
  {:deepinfra
   {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"}})

(def models
  {"mistralai/Mistral-Small-3.2-24B-Instruct-2506"
   {:process-fn (fn mistal [r] r)}}) ; TODO - meaningful process of the responses

(defn llm-prompt-parse-request
  "Parses the request and either builds a map with valid prepared data
  to run a query to the LLM service, or returns an error"
  [request]
  (try

    (let [request-body (:request-body request)
          prompt (:prompt request-body)
          provider-keyword (keyword (:provider request-body))
          provider (provider-keyword providers)
          model-name (:model request-body)]

      (assert (map? provider)
              "Error: provider not found.")
      (assert (not-empty prompt)
              "Error: prompt must not be empty.")
      (assert (get models model-name)
              (str "Error: settings for model " model-name " not found."))

      {:prompt prompt
       :model-name model-name
       :model (get models model-name)
       :endpoint-url (:endpoint-url provider)
       :provider provider})
    (catch Error e
      (throw (Exception. (.getMessage e))))))

#_(def large-prompt (slurp "llm-experiments/prompts/wsid-job-edn-output"))

(defn get-message-content
  "Get the message content from the JSON response from the DeepInfra API"
  [r] (-> r :choices first :message :content))

(defn remove-think [r] (string/replace r #"(?s)<think>.*</think>" ""))

(defn llm-prompt-handler [request]
  (try
    (let [parsed-input (llm-prompt-parse-request request)
          llm-request-body {:model (:model-name parsed-input)
                            :messages [{:role "user" :content (:prompt parsed-input)}]}
          http-response (http/post (:endpoint-url parsed-input)
                                   {:body (json/generate-string llm-request-body)
                                    :headers {"Content-Type" "application/json"
                                              "Authentication" (str "Bearer "
                                                                    (:llm-token-deepinfra config))}
                                    :throw-exceptions false ; Don't throw on HTTP errors
                                    :as :json})
          response-process-fn (:process-fn (:model parsed-input))] ; Parse JSON response automatically

      (if (<= 200 (:status http-response) 299)
        ;; Success case - return just the body
        ;; TODO - process response
        (ok {:response (-> (:body http-response)
                           get-message-content
                           response-process-fn)})

        ;; HTTP error case - return error info
        (response 500 {:message (get-in http-response [:body :message] "HTTP request failed")
                       :body (:body http-response)})))

    (catch Exception e
      ;; Exception case - most likely a request validation error
      (response 500 {:message (.getMessage e)
                     :type :exception}))))
