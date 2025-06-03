(ns wsid
  (:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestHandler]
   :methods [^:static [handleRequest [java.util.Map com.amazonaws.services.lambda.runtime.Context] java.util.Map]])
  (:require [io.pedestal.http :as http]
            [io.pedestal.http.route :as route]
            [ring.middleware.apigw :refer [wrap-apigw-lambda-proxy]])
  (:import
   [com.amazonaws.services.lambda.runtime Context]
   [java.time ZonedDateTime ZoneId]
   [java.time.format DateTimeFormatter]))

; objetivo de ahora, sólo poner un hello world para poder probar localmente y luego aventar a aws somehow

; CONFIG AND DEFAULTS ---------
(def defaults {
               :timezone "America/Mexico_City"
               :time-format "yyyy-MM-dd HH:mm:ss z"
               })

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

; LAMBDA HANDLER ---------------
; Convert Pedestal service to Ring handler and wrap for API Gateway
(defn -dummyHandler [evt context]
  {"statusCode" 200
   "headers" {"Content-Type" "application/json"}
   "body" (str "{\"message\": \"Hello from Lambda!\", \"event\": \""
               (.toString evt) "\"}")})

(defn -handleRequest
  [evt context]
  ((wrap-apigw-lambda-proxy
    (::http/service-fn (http/create-servlet service-map)))
   evt context))
