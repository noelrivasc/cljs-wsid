(ns wsid.interceptors.parse-body 
  (:require
   [cheshire.core :as json]
   [clojure.edn :as edn]
   [clojure.string :as string]))

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