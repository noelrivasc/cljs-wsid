(ns wsid.config)

; CONFIG AND DEFAULTS ---------
(def config {:default-timezone "America/Mexico_City"
             :time-format "yyyy-MM-dd HH:mm:ss z"

             :db-spec {:dbtype "postgresql"
                       :host (or (System/getenv "DB_HOST") "localhost")
                       :port (or (System/getenv "DB_PORT") 5432)
                       :dbname (or (System/getenv "DB_NAME") "wsid")
                       :user (or (System/getenv "DB_USER") "wsid_user")
                       :password (or (System/getenv "DB_PASSWORD") "wsid_password")}

             :jwt-token-expiration-hours (or (System/getenv "JWT_EXPIRATION_HOURS") 24)
             :jwt-secret (or (System/getenv "JWT_SECRET") "default-dev-secret")})
