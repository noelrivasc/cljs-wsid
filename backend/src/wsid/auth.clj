(ns wsid.auth
  (:require
   [wsid.config :refer [config]]
   [buddy.sign.jwt :as jwt]))


(defn extract-token-from-header
  "Extract JWT token from Authorization header"
  [request]
  (when-let [auth-header (get-in request [:headers "authorization"])]
    (when (.startsWith auth-header "Bearer ")
      (.substring auth-header 7))))

(defn verify-jwt-token
  "Verify and decode a JWT token"
  [token]
  (try
    (jwt/unsign token (:jwt-secret config))
    (catch Exception _
      nil)))

(def auth-interceptor
  "Pedestal interceptor for JWT authentication"
  {:name :auth
   :enter (fn [context]
            (let [request (:request context)
                  token (extract-token-from-header request)
                  claims (verify-jwt-token token)]
              (if claims
                (assoc context :request (assoc request :user claims))
                (assoc context :response {:status 401
                                          :headers {"Content-Type" "application/json"} ; TODO - content type negotiation, move encoding to exit path
                                          :body "{\"error\": \"Unauthorized\"}"}))))})