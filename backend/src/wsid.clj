(ns wsid
  #_(:gen-class
   :implements [com.amazonaws.services.lambda.runtime.RequestHandler]
   :methods [^:static [handleRequest [java.util.Map com.amazonaws.services.lambda.runtime.Context] java.util.Map]])
  (:require
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   [clojure.java.io :as io]
   [cheshire.core :refer [parse-stream generate-stream]]
   [lambda-url :refer [wrap-lambda-url-proxy]]
   [uswitch.lambada.core :refer [deflambdafn]])
  (:import
   [java.time ZoneId ZonedDateTime]
   [java.time.format DateTimeFormatter]))

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

(defn -dummyRequestHandler [evt context]
  {"statusCode" 200
   "headers" {"Content-Type" "application/json"}
   "body" (str "{\"message\": \"Hello from Lambda!\", \"event\": \""
               (.toString evt) "\"}")})

;; Create a Ring handler from Pedestal service map
;; Use http/create-server instead of create-servlet to get proper Ring handler
(def handler 
  (wrap-lambda-url-proxy
   (::http/service-fn (http/create-server service-map))))

; LAMBDA HANDLER ---------------
; Convert Pedestal service to Ring handler and wrap url lambda

#_{:clj-kondo/ignore [:unresolved-symbol]}
(deflambdafn wsid.handle [is os ctx]
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
