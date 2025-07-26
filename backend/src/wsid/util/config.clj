(ns wsid.util.config
  (:require
   [clojure.edn :as edn]))

(def ^:dynamic *debug-enabled* false)

(defn load-edn-config [filename]
  (try
    (edn/read-string (slurp filename))
    (catch java.io.FileNotFoundException _
      {})))

(def file-config (load-edn-config "config.local.edn"))

(def config {:default-timezone (or (:default-timezone file-config) "America/Mexico_City")
             :time-format (or (:time-format file-config) "yyyy-MM-dd HH:mm:ss z")
             :db-spec {:dbtype "postgresql"
                       :host (or (System/getenv "DB_HOST") (:db-host file-config) "localhost")
                       :port (or (System/getenv "DB_PORT") (:db-port file-config) 5432)
                       :dbname (or (System/getenv "DB_NAME") (:db-name file-config) "wsid")
                       :user (or (System/getenv "DB_USER") (:db-user file-config) "wsid_user")
                       :password (or (System/getenv "DB_PASSWORD") (:db-password file-config) "wsid_password")}
             :jwt-token-expiration-hours (or (System/getenv "JWT_EXPIRATION_HOURS") (:jwt-expiration-hours file-config) 24)
             :jwt-secret (or (System/getenv "JWT_SECRET") (:jwt-secret file-config) "default-dev-secret")
             :api-token-deepinfra (or (System/getenv "API_TOKEN_DEEPINFRA") (:api-token-deepinfra file-config) "default-dev-secret")
             :debug-mode (or (System/getenv "DEBUG") (:debug-mode file-config) *debug-enabled*)})
