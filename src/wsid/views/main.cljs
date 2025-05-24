(ns wsid.views.main
  (:require
   [wsid.subs.main :as subs :refer [<sub]] 
   [wsid.events.main :refer [evt>]]
   [wsid.views.factors :refer [v-factors-panel v-factor-form]]
   [wsid.views.scenarios :refer [v-scenarios-panel v-scenario-form]]))

(defn v-modal-dialog [render? content-fn title]
  (when render?
    [:div.modal
     [:div.modal__backdrop
      [:div.modal__dialog
       [:div.modal-container__header title]
       [:div.modal-container__main [content-fn]]]]]))

(defn v-main-panel []
  [:main.main-container
   [:div.title__wrapper
    [:span.title__acronym "w.s.i.d"]
    [:h1.title "What Should I Do?"]]

   [:div.work-area
    [:div.work-area__file-tools
     [:input.button {:type "button"
                     :value "Save"
                     :on-click #(evt> [:save-to-local-storage])}]]

 ; SECTIONS
    [:div.tool-container.tool-container--factors
     {:id "factors" :role "tabpanel" :aria-labelledby "factors-tab"}
     [v-factors-panel]]
    [:div.tool-container.tool-container--scenarios
     {:id "scenarios" :role "tabpanel" :aria-labelledby "scenarios-tab"}
     [v-scenarios-panel]]]])

(defn v-main []
  (let [factor-active (<sub [:factor-active-is-set])
        scenario-active (<sub [:scenario-active-is-set])]
  [:div.wsid-app
   [v-main-panel]
   [v-modal-dialog (not factor-active) v-factor-form "Edit Factor"]
   [v-modal-dialog (not scenario-active) v-scenario-form "Edit Scenario"]]))