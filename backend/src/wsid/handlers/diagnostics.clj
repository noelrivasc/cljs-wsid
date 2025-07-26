(ns wsid.handlers.diagnostics
  (:require
   [clj-http.client :as http-client]
   [io.pedestal.http :as http]
   [io.pedestal.log :as log]))

(def db-connection
  "Attempt to connect to the DB and produce logs useful to diagnose the connection."
  {:name :db-connection
   :enter (fn [context]
            (let [msg (if (:db-connection context)
                        "DB connection found in context at :db-connection"
                        "DB connection not found in context.")]
              (io.pedestal.log/info :msg msg))
            
            (http/respond-with context 200 "db-connection"))})

(def http-outbound-connectivity
  "Comprehensive AWS Lambda network diagnostics handler"
  {:name :aws-network-diagnostics
   :enter (fn [context]
            (let [http-response (http-client/get "https://httpbin.org/get")]
              (if (<= 200 (:status http-response) 299)
                ;; Success case
                (http/respond-with context 200 {:response "HTTP call succeeded."})
                ;; HTTP error case
                (http/respond-with context 500 {:response "HTTP call failed."}))))})