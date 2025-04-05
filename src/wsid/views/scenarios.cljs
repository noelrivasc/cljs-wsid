(ns wsid.views.scenarios
  (:require 
   [re-frame.core :as re-frame]
   [wsid.views.icons :as i]))

(defn v-scenarios-panel []
  [:div.scenarios-panel__wrapper
   [:div.scenarios-panel
    [:div.scenarios-panel__heading__wrapper
     [:h2.scenarios-panel__heading 
      {:class ["text-xl" "text-yellow-400"]}
      "Scenarios"]
     [:div.scenarios-panel__heading__add
        [:button.scenarios-panel__heading__add__button
         {:type "button"
          :value "add"
          :on-click #(re-frame.core/dispatch [:scenario-create])}
         [:span.icon
          (i/get-icon i/square-plus)]]]
     ]]
   ])