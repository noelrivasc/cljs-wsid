(ns wsid.logging
  (:require
   [io.pedestal.log :as log]
   [wsid.config :refer [config]])
  (:import
   [java.time Instant]
   [java.time.temporal ChronoUnit]))

(when (:debug-mode config)
  (System/setProperty "org.slf4j.simpleLogger.log.wsid" "DEBUG"))

(def logging-interceptor
  "Interceptor that captures request start time for performance logging"
  {:name :start-time-interceptor
   :enter (fn [context]
            (assoc context :request-start-time (Instant/now)))})

(defn elapsed-ms
  "Get elapsed milliseconds from request start time in context"
  [context]
  (if-let [start-time (:request-start-time context)]
    (.between ChronoUnit/MILLIS start-time (Instant/now))
    "missing logging interceptor"))

(defn debug-timing
  "Log debug timing information if debug mode is enabled"
  [context message & {:keys [data]}]
  (when (:debug-mode config)
    (let [elapsed (elapsed-ms context)
          log-data (merge {:msg message
                          :elapsed-ms elapsed}
                         data)]
      (log/debug :timing log-data))))