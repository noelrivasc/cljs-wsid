(ns wsid
  (:gen-class
   :main true ;; for -main method in the Uberjar
   :methods [^:static [handler [Object com.amazonaws.services.lambda.runtime.Context] Object]]) ; for the AWS Lambda/APIGW hook
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.aws.lambda.utils :as lambda]
   [io.pedestal.http.route :as route])
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
        body (str "It's " time)]
    (ok body)))

; ROUTES ----------------------
(def routes
  (route/expand-routes
   #{["/ping" :get ping :route-name :ping]}))

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
; 
(def lambda-service (-> service-map
                        (merge {:env :lambda})
                        ; http/default-interceptors
                        lambda/direct-apigw-provider))

;; Note: Optionally, use the lambda.utils macros instead of the :gen-class setup here
(def lambda-service-fn (:io.pedestal.aws.lambda/apigw-handler lambda-service))

(defn -handler [^Object req ^Context ctx]
  (lambda-service-fn req ctx))

; Ill- adviced code below, a pattern suggested by Claude
; This is a Pedestal app, not a ring app.

#_(defn -dummyRequestHandler [evt context]
  {"statusCode" 200
   "headers" {"Content-Type" "application/json"}
   "body" (str "{\"message\": \"Hello from Lambda!\", \"event\": \""
               (.toString evt) "\"}")})

;; Create a Ring handler from Pedestal service map
;; Use http/create-server instead of create-servlet to get proper Ring handler
#_(def handler 
  (wrap-lambda-url-proxy
   (::http/service-fn (http/create-server service-map))))

; LAMBDA HANDLER ---------------
; Convert Pedestal service to Ring handler and wrap url lambda

#_(deflambdafn wsid.handle [is os ctx]
  (with-open [writer (io/writer os)]
    (let [request (parse-stream (io/reader is :encoding "UTF-8") true)]
      (generate-stream (handler request) writer))))

#_(defn -handleRequest [request context]
    (let [ring-request (apigw-request->ring-request request)
          response ((::http/service-fn (http/create-servlet service-map)) ring-request)]
      {:statusCode (:status response)
       :headers (:headers response)
       :body (:body response)}))

#_(defn -handleRequest
    [evt context]
    ((wrap-apigw-lambda-proxy
      (::http/service-fn (http/create-servlet service-map)))
     evt context))
