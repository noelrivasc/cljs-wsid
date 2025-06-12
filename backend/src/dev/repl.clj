(ns repl
  (:require
   [io.pedestal.http :as http]
   [wsid.core]))


;; For interactive development
(defonce server (atom nil))

; SERVER MANAGEMENT -----------

(defn start-dev []
  (reset! server
          (http/start (http/create-server
                       (assoc wsid.core/service-map
                              ::http/join? false))))
  ::start-dev-server)

(defn stop-dev []
  (http/stop @server)
  ::stop-dev-server)

(defn restart []
  (stop-dev)
  (start-dev)
  ::restart-dev-server)