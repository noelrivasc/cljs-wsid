(ns wsid.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [wsid.events.main :as events]
   [wsid.views.main :as views]
   [wsid.config :as config]
   [wsid.util.theming :refer [set-theme]]
   [wsid.views.themes.slate :refer [theme]]))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

; This sets the theme that the whole application
; will use (the Tailwind classes that will be applied
; to every HTML element) when wsid.util.theming/apply-theme
; is called.
(set-theme theme)

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/v-main] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
