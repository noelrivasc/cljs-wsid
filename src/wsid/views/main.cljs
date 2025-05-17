(ns wsid.views.main
  (:require
   [wsid.subs.main :as subs :refer [<sub]] 
   [wsid.events.main :refer [evt>]]
   [wsid.util.theming
    :refer [apply-current-theme]
    :rename {apply-current-theme t}]
   [wsid.views.factors :refer [v-factors-panel v-factor-form]]
   [wsid.views.scenarios :refer [v-scenarios-panel v-scenario-form]]))

(defn v-modal-dialog [render? content-fn]
  [t [:dialog.modal-container (conj {}
                                    (if render? {:open true} nil))
      (if render? (content-fn) nil)]])

(defn v-main-panel []
  [t [:main.main-container
      [:div.title__wrapper
       [:span.title__acronym "w.s.i.d ?"]
       [:h1.title "What Should I Do?"]]

      [:div.work-area
       [:div.work-area__file-tools
        [:input {:type "button"
                 :value "Save"
                 :on-click #(evt> [:save-to-local-storage])}]]

    ; SECTIONS
       [:div.tool-container.tool-container--factors
        {:id "factors" :role "tabpanel" :aria-labelledby "factors-tab"}
        [v-factors-panel]]
       [:div.tool-container.tool-container--scenarios
        {:id "scenarios" :role "tabpanel" :aria-labelledby "scenarios-tab"}
        [v-scenarios-panel]]]]])

(defn v-main []
  (let [factor-active (<sub [:factor-active-is-set])
        scenario-active (<sub [:scenario-active-is-set])]
  [:div.wsid-app
   [v-main-panel]
   [v-modal-dialog (not factor-active) v-factor-form]
   [v-modal-dialog (not scenario-active) v-scenario-form]]))