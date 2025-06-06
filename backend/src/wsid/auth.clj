(ns wsid.auth
  (:require
   [wsid.config :refer [config]]
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs])
  (:import
   [java.time Instant]
   [java.time.temporal ChronoUnit]))

(defn create-jwt-token
  "Create a JWT token for a user"
  [user-id email]
  (let [now (Instant/now)
        exp (.plus now (:jwt-token-expiration-hours config) ChronoUnit/HOURS)
        claims {:user-id user-id
                :email email
                :iat (.getEpochSecond now)
                :exp (.getEpochSecond exp)}]
    (jwt/sign claims (:jwt-secret config))))

(defn verify-jwt-token
  "Verify and decode a JWT token"
  [token]
  (try
    (jwt/unsign token (:jwt-secret config))
    (catch Exception _
      nil)))

; TODO - move opening and closing of db connection to its own interceptor
(defn find-user-by-email
  "Find a user by email address"
  [email]
  (try
    (with-open [conn (jdbc/get-connection (:db-spec config))]
      (jdbc/execute-one! conn
                         ["SELECT id, email, password_hash FROM users WHERE email = ?" email]
                         {:builder-fn rs/as-unqualified-lower-maps}))
    (catch Exception e
      (println "Database error:" (.getMessage e))
      nil)))

(defn authenticate-user
  "Authenticate a user with email and password"
  [email password]
  (let [u (find-user-by-email email)]
    (println "user found: " u))
  (when-let [user (find-user-by-email email)]
    (when (hashers/verify password (:password_hash user))
      (dissoc user :password_hash))))


(defn extract-token-from-header
  "Extract JWT token from Authorization header"
  [request]
  (when-let [auth-header (get-in request [:headers "authorization"])]
    (when (.startsWith auth-header "Bearer ")
      (.substring auth-header 7))))

(defn authenticate-request
  "Middleware to authenticate requests using JWT"
  [request]
  (if-let [token (extract-token-from-header request)]
    (if-let [claims (verify-jwt-token token)]
      (assoc request :user claims)
      nil)
    nil))

(def auth-interceptor
  "Pedestal interceptor for JWT authentication"
  {:name :auth
   :enter (fn [context]
            (let [request (:request context)]
              (if-let [authenticated-request (authenticate-request request)]
                (assoc context :request authenticated-request)
                (assoc context :response {:status 401
                                          :headers {"Content-Type" "application/json"}
                                          :body "{\"error\": \"Unauthorized\"}"}))))})