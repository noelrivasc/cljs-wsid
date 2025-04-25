(ns wsid.views.scenarios
  (:require 
    [wsid.events.main :refer [evt>]]
   [wsid.views.icons :as i]))

(defn v-scenarios-panel []
  [:div.scenarios-panel__wrapper
   [:div.scenarios-panel
    [:div.scenarios-panel__heading__wrapper
     [:h2.scenarios-panel__heading "Scenarios"]
     [:div.scenarios-panel__heading__add
        [:button.scenarios-panel__heading__add__button
         {:type "button"
          :value "add"
          :on-click #(evt> [:scenario-create])}
         [:span.icon
          (i/get-icon i/square-plus)]]]
     ]]
   ])