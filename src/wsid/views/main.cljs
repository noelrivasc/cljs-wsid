(ns wsid.views.main
  (:require
   [wsid.subs.main :as subs :refer [<sub]] 
   [wsid.util.theming
    :refer [apply-current-theme]
    :rename {apply-current-theme t}]
   [wsid.views.factors :refer [v-factors-panel v-factor-form]]
   [wsid.views.scenarios :refer [v-scenarios-panel v-scenario-form]]))

(defn v-modal-dialog [render? content-fn]
  [:dialog.modal-container (conj {}
                                 (if render? {:open true} nil))
   (if render? (content-fn) nil)])

(defn v-main-panel []
  [:main.main-container

   [:div.title__wrapper
    [:span.title__acronym "w.s.i.d ?"]
    [:h1.title "What Should I Do?"]]

   [:div.work-area
    ; TABS
    [:nav {:role "tablist" :aria-label "Tool sections"}
     [:button.tab
      {:role "tab" :aria-selected "true" :aria-controls "factors" :id "factors-tab"}
      [:div.tab__inner "Factors"]]
     [:button.tab
      {:role "tab" :aria-selected "false" :aria-controls "scenarios" :id "scenarios-tab"}
      [:div.tab__inner "Scenarios"]]]

    ; SECTIONS
    [:div.tool-container.tool-container--factors
     {:id "factors" :role "tabpanel" :aria-labelledby "factors-tab"}
     [t (v-factors-panel)]]
    [:div.tool-container.tool-container--scenarios
     {:id "scenarios" :role "tabpanel" :aria-labelledby "scenarios-tab"}
     [t (v-scenarios-panel)]]]])

(defn v-main []
  (let [factor-active (<sub [:factor-active-is-set])
        scenario-active (<sub [:scenario-active-is-set])]
  [:div.wsid-app
   [t (v-main-panel)]
   [t (v-modal-dialog (not factor-active) v-factor-form)]
   [t (v-modal-dialog (not scenario-active) v-scenario-form)]]))