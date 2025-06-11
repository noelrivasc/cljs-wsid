(ns wsid.db
  (:require
   [wsid.config :refer [config]]
   [next.jdbc :as jdbc]))

(def db-interceptor
  "Pedestal interceptor to open and close db connections that
   can be reused for the duration of the request."
  {:name :db
   :enter (fn [context]
            (try
              (when (:db-connection context)
                (println "Error initializing database connection: db-connection already present in context.")
                (throw (Exception. "Database error")))

              (let [connection (jdbc/get-connection (:db-spec config))]
                (assoc context :db-connection connection))

              (catch Exception e
                (println "Database error: " (.getMessage e))
                (assoc context :response {:status 500
                                          :body "Database error"}))))
   :exit (fn [context]
           (when (not (:db-connection context))
             (println "db-connection not found on db-interceptor exit. Closing will not be attempted."))
           (.close (:db-connection context)))})