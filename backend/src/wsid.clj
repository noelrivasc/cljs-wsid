(ns wsid
  (:require
   [io.pedestal.http :as http]
   [lambda :as lambda]
   [io.pedestal.http.route :as route]
   [auth :as auth])
  (:import
   [java.time ZoneId ZonedDateTime]
   [java.time.format DateTimeFormatter]
   [com.amazonaws.services.lambda.runtime Context]))

; CONFIG AND DEFAULTS ---------
(def defaults {:timezone "America/Mexico_City"
               :time-format "yyyy-MM-dd HH:mm:ss z"})

; UTILITIES -------------------
(defn ok [body]
  {:status 200 :body body})

(defn formatted-time-in-timezone [timezone-str format-pattern]
  (-> (ZoneId/of timezone-str)
      (ZonedDateTime/now)
      (.format (DateTimeFormatter/ofPattern format-pattern))))

; HANDLERS --------------------
(defn ping [request]
  (let [timezone (get-in request [:query-params :timezone] (:timezone defaults))
        format (get-in request [:query-params :format] (:time-format defaults))
        time (formatted-time-in-timezone timezone format)
        user (:user request)
        body (str "It's " time " for user " (:email user))]
    (ok body)))

; ROUTES ----------------------
(def routes
  (route/expand-routes
   #{["/ping" :get [auth/auth-interceptor ping] :route-name :ping]
     ["/login" :post auth/login-handler :route-name :login]}))

; CONFIGURATION ---------------
(def service-map
  {::http/routes routes
   ::http/type :jetty
   ::http/port 8890})

; SERVER MANAGEMENT -----------
(defn start []
  (http/start (http/create-server service-map)))

;; For interactive development
(defonce server (atom nil))

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc service-map
                              ::http/join? false)))))

(defn stop-dev []
  (http/stop @server))

(defn restart []
  (stop-dev)
  (start-dev))

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
