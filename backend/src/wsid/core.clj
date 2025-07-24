(ns wsid.core
  (:require
   [clojure.set]
   [io.pedestal.http :as http]
   [wsid.lambda :as lambda]
   [io.pedestal.http.route :as route]
   [wsid.auth :as auth]
   [wsid.util.request-handling :as util]
   [wsid.config :refer [config]]
   [wsid.handlers.ping :as ping]
   [wsid.handlers.diagnostics :as diagnostics]
   [wsid.handlers.user :as user]
   [wsid.handlers.llm :as llm]
   [wsid.handlers.example-handlers :as examples]
   [wsid.logging :as logging]
   [wsid.db :as db])
  (:import
   [com.amazonaws.services.lambda.runtime Context]))

; ROUTES ----------------------
(def app-routes
  #{["/ping" :get [util/coerce-body-interceptor
                   util/content-negotiation-interceptor
                   auth/auth-interceptor
                   ping/ping-handler] :route-name :ping]
    ["/test-exception" :post [examples/error-example-handler] :route-name :test-exception]
    ["/login" :post [logging/start-time-interceptor
                     util/parse-body-interceptor
                     util/coerce-body-interceptor
                     util/content-negotiation-interceptor
                     db/db-interceptor
                     user/login-handler] :route-name :login]
    ["/llm-prompt" :post [util/parse-body-interceptor
                          util/coerce-body-interceptor
                          util/content-negotiation-interceptor
                          auth/auth-interceptor
                          llm/llm-prompt-handler] :route-name :llm-prompt]})

(def diagnostic-routes
  #{["/diagnostics/db-connection" :get [db/db-interceptor diagnostics/db-connection]]})

(def routes
  (let [route-set (if (:debug-mode config)
                    (do (println "DEBUG MODE ENABLED")
                        (clojure.set/union app-routes diagnostic-routes))
                    app-routes)]
    (route/expand-routes route-set)))

; CONFIGURATION ---------------
(def service-map
  {::http/routes routes

   ; Turn off logging and tracing that I don't understand yet
   ::http/request-logger nil
   ::http/tracing nil

   ; The following only affect the server when started
   ; with http/start —not the lambda handler.
   ::http/type :jetty
   ::http/port 8890})

(defn start []
  (http/start (http/create-server service-map)))

(defn -main []
  (start))

; Code taken from example at
; https://github.com/pedestal/pedestal/tree/0.6.1/samples/aws-codestar-lambda
(def lambda-service (-> service-map
                        (merge {:env :lambda})
                        http/default-interceptors
                        lambda/direct-lambda-provider))

;; Note: Optionally, use the lambda.utils macros instead of the :gen-class setup here
(def lambda-service-fn (:io.pedestal.aws.lambda/lambda-handler lambda-service))

(gen-class
 ; when gen-class is included as part of the ns form, the name can be omitted, and it defaults to the ns
 :name "wsid"

 ; The handler method _must_ be static; otherwise it seems to be ignored, the class called with 3 args (stream input and output)
 :methods [^:static [handler [Object com.amazonaws.services.lambda.runtime.Context] Object]])

(defn -handler [^Object req ^Context ctx]
  (lambda-service-fn req ctx))
