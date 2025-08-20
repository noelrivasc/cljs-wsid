(ns wsid.views.main
  (:require
   [wsid.subs.main :as subs :refer [<sub]] 
   [wsid.events.main :refer [evt>]]
   [wsid.views.factors :refer [v-factors-panel v-factor-form]]
   [wsid.views.scenarios :refer [v-scenarios-panel v-scenario-form]]
   [wsid.views.decisions :refer [v-decision-panel]]
   [wsid.views.user :refer [v-user-form]]
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

(defn v-toolbar []
  (let [db-is-dirty (<sub [:db-is-dirty])
        icon-class (if db-is-dirty [:icon :opacity-100] [:icon :opacity-20])
        user-token (<sub [:user-token])]
    [:div.toolbar
     [:div.toolbar__save-to-browser
      [:button.button
       {:on-click #(evt> ^:ls-compare [:save-to-local-storage])}
       [:span.text "Save to Browser"]
       [:span.button__icon {:class icon-class}
        (i/get-icon i/edit :svg.icon--sm)]]]
     [:div.toolbar__authentication
      (if user-token
        [:button.button
         {:on-click #(evt> [:logout])}
         [:span.text "Log out"]]
        [:button.button
         {:on-click #(evt> [:authenticate])}
         [:span.text "Log in"]])]]))

(defn v-main-panel []
  [:main.main-container
   [:div.title__wrapper
    [:span.title__acronym "w.s.i.d"]
    [:h1.title "What Should I Do?"]]

   [:div.work-area
    [v-toolbar]

    [:div.tool-container.tool-container--decision
     [v-decision-panel]]

    [:div.tool-container.tool-container--ai-help
     [:input.button {:type "button"
                     :value "Give me some ideas ✨🤖"
                     :on-click #(evt> [:llm-fetch])}]]

      ; SECTIONS
    [:div.tool-container.tool-container--scenarios
     [v-scenarios-panel]]
    [:div.tool-container.tool-container--factors
     [v-factors-panel]]]])
    
(defn v-main []
  (let [f-active (<sub [:factor-active-is-set])
        s-active (<sub [:scenario-active-is-set])
        u-active (<sub [:user-active-is-set])]
   [:div.wsid-app
    [v-main-panel]
    [v-modal-dialog f-active v-factor-form "Edit Factor"]
    [v-modal-dialog s-active v-scenario-form "Edit Scenario"]
    [v-modal-dialog u-active v-user-form "Log In"]]))
