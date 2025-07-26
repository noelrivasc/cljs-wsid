(ns wsid.handlers.ping
  (:require
   [wsid.util.config :refer [config]]
   [wsid.util.request-handling :refer [ok]])
  (:import
   [java.time ZoneId ZonedDateTime]
   [java.time.format DateTimeFormatter]))

; UTILITIES -------------------
(defn- formatted-time-in-timezone [timezone-str format-pattern]
  (-> (ZoneId/of timezone-str)
      (ZonedDateTime/now)
      (.format (DateTimeFormatter/ofPattern format-pattern))))

; HANDLERS --------------------
(defn ping [request]
  (let [timezone (get-in request [:query-params :timezone] (:default-timezone config))
        format (get-in request [:query-params :format] (:time-format config))
        time (formatted-time-in-timezone timezone format)
        user (:user request)
        body {:time (str "It's " time " for user " (:email user))}]
    (ok body)))
