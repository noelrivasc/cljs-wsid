(ns auth
  (:require
   [buddy.hashers :as hashers]
   [buddy.sign.jwt :as jwt]
   [cheshire.core :as json]
   [next.jdbc :as jdbc]
   [next.jdbc.result-set :as rs])
  (:import
   [java.time Instant]
   [java.time.temporal ChronoUnit]))

;; JWT secret - in production this should come from environment variables
(def jwt-secret (or (System/getenv "JWT_SECRET") "default-dev-secret"))

;; Database connection - in production this should come from environment variables
(def db-spec
  {:dbtype "postgresql"
   :host (or (System/getenv "DB_HOST") "localhost")
   :port (or (System/getenv "DB_PORT") 5432)
   :dbname (or (System/getenv "DB_NAME") "wsid")
   :user (or (System/getenv "DB_USER") "wsid_user")
   :password (or (System/getenv "DB_PASSWORD") "wsid_password")})

;; JWT token expiration (24 hours)
(def token-expiration-hours 24)

(defn create-jwt-token
  "Create a JWT token for a user"
  [user-id email]
  (let [now (Instant/now)
        exp (.plus now token-expiration-hours ChronoUnit/HOURS)
        claims {:user-id user-id
                :email email
                :iat (.getEpochSecond now)
                :exp (.getEpochSecond exp)}]
    (jwt/sign claims jwt-secret)))

(defn verify-jwt-token
  "Verify and decode a JWT token"
  [token]
  (try
    (jwt/unsign token jwt-secret)
    (catch Exception _
      nil)))

(defn find-user-by-email
  "Find a user by email address"
  [email]
  (try
    (with-open [conn (jdbc/get-connection db-spec)]
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

(defn login-handler
  "Handle login requests"
  [request]
  (try
    (let [body (-> request :body slurp)
          {:keys [email password]} (json/parse-string body true)]
      (if (and email password)
        (if-let [user (authenticate-user email password)]
          (let [token (create-jwt-token (:id user) (:email user))]
            {:status 200
             :headers {"Content-Type" "application/json"}
             :body (json/generate-string {:token token
                                          :user {:id (:id user)
                                                 :email (:email user)}})})
          {:status 401
           :headers {"Content-Type" "application/json"}
           :body (json/generate-string {:error "Invalid credentials"})})
        {:status 400
         :headers {"Content-Type" "application/json"}
         :body (json/generate-string {:error "Email and password are required"})}))
    (catch Exception e
      (println "Login error:" (.getMessage e))
      {:status 500
       :headers {"Content-Type" "application/json"}
       :body (json/generate-string {:error "Internal server error"})})))

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