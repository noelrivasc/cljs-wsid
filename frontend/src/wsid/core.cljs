(ns wsid.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [wsid.views.main :as views]
   [wsid.events.util :refer [evt>]]
   [wsid.config :as config]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/v-main] root-el)))

(defn init []
  (evt> [:app/initialize-db])
  (dev-setup)
  (mount-root))
