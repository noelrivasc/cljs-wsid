(ns wsid.db
  (:require
   [wsid.config :refer [config]]
   [io.pedestal.log]
   [next.jdbc :as jdbc]))

(def db-interceptor
  "Pedestal interceptor to open and close db connections that
   can be reused for the duration of the request."
  {:name :db
   :enter (fn [context]
            (try
              (when (:db-connection context)
                (io.pedestal.log/error :msg "Error initializing database connection: db-connection already present in context.")
                (throw (Exception. "Database error")))
              
              (when (:debug-mode config)
                (io.pedestal.log/info :msg "Attempting to open connection with the configuration:"
                                      :db-spec (dissoc (:db-spec config) :password)))

              (let [connection (jdbc/get-connection (:db-spec config))]
                (assoc context :db-connection connection))

              (catch Exception e
                (io.pedestal.log/error :msg "Error opening a db connection." :error (.getMessage e))
                (assoc context :response {:status 500
                                          :body "Database error"}))))
   :exit (fn [context]
           (when (not (:db-connection context))
             (println "db-connection not found on db-interceptor exit. Closing will not be attempted."))
           (.close (:db-connection context)))})