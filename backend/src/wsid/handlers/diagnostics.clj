(ns wsid.handlers.diagnostics
  (:require
   [clj-http.client :as http-client]
   [wsid.util.request-handling :refer [ok response]]
   [io.pedestal.http :as http]
   [io.pedestal.log :as log]
   [wsid.logging :as logging :refer [debug-timing] :rename {debug-timing dt}]))

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
            (dt context "AWS network diagnostics starting")
            (let [http-response (http-client/get "https://httpbin.org/get")]
              (if (<= 200 (:status http-response) 299)
                ;; Success case
                (assoc context :response (ok {:response "HTTP call succeeded."}))
                ;; HTTP error case
                (assoc context :response (response 500 {:response "HTTP call failed."})))))})