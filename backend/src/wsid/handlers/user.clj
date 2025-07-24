(ns wsid.handlers.user
  (:require
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs]
   [wsid.config :refer [config]]
   [wsid.util.request-handling :refer [ok response]])
  (:import
   [java.time Instant]
   [java.time.temporal ChronoUnit]))

(defn get-user-by-email
  "Gets an user from the database. On error, throws lets the
   database exception out to be handled by the caller."
  [email db-connection]
  (jdbc/execute-one! db-connection
                     ["SELECT id, email, password_hash FROM users WHERE email = ?" email]
                     {:builder-fn rs/as-unqualified-lower-maps}))

(defn validate-password
  "Checks the given password against the password hash saved for the user."
  [user password]
  (let [result (hashers/verify password (:password_hash user))]
    result))

(defn create-user-token
  "Create a JWT token for a user"
  [user]
  (let [now (Instant/now)
        exp (.plus now (:jwt-token-expiration-hours config) ChronoUnit/HOURS)
        claims {:id (:id user)
                :email (:email user)
                :iat (.getEpochSecond now)
                :exp (.getEpochSecond exp)}]
    (jwt/sign claims (:jwt-secret config))))

(def login-handler
  "Handle login requests"
  {:name :login-handler
   :enter (fn [context]
            (let [{:keys [email password]} (get-in context [:request :request-body])
                  db-connection (:db-connection context)
                  user (get-user-by-email email db-connection)
                  validation-result (validate-password user password)
                  token (if (:valid validation-result)
                          (create-user-token user)
                          nil)
                  r (if token
                             (ok token)
                             (response 401 "Invalid credentials"))]

              (assoc context :response r)))})