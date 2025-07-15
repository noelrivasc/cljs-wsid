(ns wsid.util
  "Provides utilities to parse http input and produce output.
   Includes functions for content negotiation."
  (:require
   [io.pedestal.http.content-negotiation :as content-negotiation]
   [clojure.string :as string]
   [clojure.edn :as edn]
   [cheshire.core :as json]))

; UTILITIES -------------------
(defn ok [body]
  {:status 200 :body body})

(defn response [code body]
  {:status code :body body})

; UTILITY INTERCEPTORS---------
(def parse-body-interceptor
  "Decode the request body and add it to the context as :request-body"
  {:name :auth
   :enter (fn [context]
            (let [content-type-header (string/lower-case (get-in context [:request :headers :content-type] "application/json"))
                  body (-> context :request :body slurp)
                  parsed-body (case content-type-header
                                "application/json" (json/parse-string body true)
                                "application/edn" (edn/read-string body))
                  request-with-body (assoc (:request context) :request-body parsed-body)]
              (assoc context :request request-with-body)))})

(def supported-types ["application/edn"
                      "application/json"])

(def content-negotiation-interceptor
  (content-negotiation/negotiate-content supported-types))

(def coerce-body-interceptor
  {:name ::coerce-body
   :leave
   (fn [context]
     (let [accepted (get-in context [:request :accept :field] "application/json")
           response (:response context)
           body (:body response)
           coerced-body (case accepted
                          "application/edn" (pr-str body)
                          "application/json" (json/generate-string body))
           updated-response (assoc response
                                   :headers {"Content-Type" accepted}
                                   :body coerced-body)]
       (assoc context :response updated-response)))})
