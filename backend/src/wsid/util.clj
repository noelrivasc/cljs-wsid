(ns wsid.util
  "Provides utilities to parse http input and produce output.
   Includes functions for content negotiation."
  (:require
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
            (let [body (-> context :request :body slurp)
                  parsed-body (json/parse-string body true) ; TODO - move decoding to content negotiation
                  request-with-body (assoc (:request context) :request-body parsed-body)]
              (assoc context :request request-with-body)))})