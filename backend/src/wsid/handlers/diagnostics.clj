(ns wsid.handlers.diagnostics
  (:require
   [io.pedestal.log]
   [io.pedestal.http :as http]))

(def db-connection
  "Attempt to connect to the DB and produce logs useful to diagnose the connection."
  {:name :db-connection
   :enter (fn [context]
            (let [msg (if (:db-connection context)
                        "DB connection found in context at :db-connection"
                        "DB connection not found in context.")]
              (io.pedestal.log/info :msg msg))
            
            (http/respond-with context 200 "db-connection"))})