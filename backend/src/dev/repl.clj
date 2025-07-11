(ns dev.repl
  (:require
   [io.pedestal.http :as http]
   [wsid.core]))


;; For interactive development
(defonce server (atom nil))

; SERVER MANAGEMENT -----------

(defn start-dev []
  (require 'wsid.core :reload)
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

; Find all the namespaces that start with wsid
(defn wsid-ns [] (filter #(re-find #"^wsid" (str %)) (all-ns)))
; Reload all the wsid namespaces
(defn reboot [] (apply require (conj (map #(symbol (str %)) (wsid-ns)) :reload)))

; Reboot with debugging enabled
; Debugging will stay enabled in config
#_(binding [wsid.config/*debug-enabled* true] (reboot))
