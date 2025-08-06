(ns wsid.views.main
  (:require
   [wsid.subs.main :as subs :refer [<sub]] 
   [wsid.events.main :refer [evt>]]
   [wsid.views.factors :refer [v-factors-panel v-factor-form]]
   [wsid.views.scenarios :refer [v-scenarios-panel v-scenario-form]]
   [wsid.views.decisions :refer [v-decision-panel]]
   [wsid.views.icons :as i]
   ["react-transition-group" :as rtg]))

(defn v-modal-dialog [render? content-fn title]
  [:> rtg/CSSTransition
   {:in render?
    :timeout 300
    :class-names "fade" ; fade-enter fade-enter-done fade-exit fade-exit-done
    :unmount-on-exit true}
   [:div.modal
    [:div.modal__backdrop
     [:div.modal__dialog
      [:div.modal-container__header title]
      [:div.modal-container__main [content-fn]]]]]])

(defn v-main-panel []
  (let [db-is-dirty (<sub [:db-is-dirty])]
    [:main.main-container
     [:div.title__wrapper
      [:span.title__acronym "w.s.i.d"]
      [:h1.title "What Should I Do?"]]

     [:div.work-area
      [:div.work-area__file-tools
       [:div.work-are__save-to-browser
        [:input.button
         {:type "button"
          :value "Save to Browser"
          :on-click #(evt> ^:ls-compare [:save-to-local-storage])}]
        (when db-is-dirty
          [:span.icon
           (i/get-icon i/edit)])]]

      [:div.tool-container.tool-container--decision
       [v-decision-panel]]

      [:div.tool-container.tool-container--ai-help
       [:input.form__input
        {:type "text"
         :placeholder "JWT Token"
         :on-change #(evt> ^:ls-compare [:jwt-token-update (-> % .-target .-value)])}]
       [:input.button {:type "button"
                       :value "AI Help"
                       :on-click #(evt> [:llm-fetch])}]]

      ; SECTIONS
      [:div.tool-container.tool-container--factors
       [v-factors-panel]]
      [:div.tool-container.tool-container--scenarios
       [v-scenarios-panel]]]]))
    
(defn v-main []
  (let [f-active (<sub [:factor-active-is-set])
        s-active (<sub [:scenario-active-is-set])]
   [:div.wsid-app
    [v-main-panel]
    [v-modal-dialog f-active v-factor-form "Edit Factor"]
    [v-modal-dialog s-active v-scenario-form "Edit Scenario"]]))
