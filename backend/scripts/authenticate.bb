#!/usr/bin/env bb

(require '[babashka.http-client :as http]
         '[clojure.java.io :as io]
         '[cheshire.core :as json])

(def login-url "http://localhost:8890/login")
(def credentials {:email "test@example.com"
                  :password "000000"})
(def env-file "wsid-bruno/.env")

(try
  (let [response (http/post login-url
                           {:headers {"Accept" "application/edn"
                                     "Content-Type" "application/json"}
                            :body (json/generate-string credentials)
                            :throw false})]
    (if (= 200 (:status response))
      (let [jwt-token (:body response)]
        (when (.exists (io/file env-file))
          (io/delete-file env-file))
        (spit env-file (str "JWT_TOKEN=" jwt-token))
        (println "Authentication successful, JWT token saved to" env-file))
      (do
        (println "WARNING: Authentication failed with status:" (:status response))
        (System/exit 1))))
  (catch Exception e
    (println "WARNING: Request failed:" (.getMessage e))
    (System/exit 1)))