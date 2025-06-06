(ns wsid.handlers.user
  (:require
   [cheshire.core :as json]
   [wsid.auth :as auth]))

(defn login-handler
  "Handle login requests"
  [request]
  (try
    (let [body (-> request :body slurp)
          {:keys [email password]} (json/parse-string body true)]
      (if (and email password)
        (if-let [user (auth/authenticate-user email password)]
          (let [token (auth/create-jwt-token (:id user) (:email user))]
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