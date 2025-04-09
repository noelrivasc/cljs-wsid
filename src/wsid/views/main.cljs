(ns wsid.views.main
  (:require
   [wsid.subs :as subs :refer [<sub]] 
   [wsid.util.theming
    :refer [apply-current-theme]
    :rename {apply-current-theme t}]
   [wsid.views.factors :refer [v-factors-panel v-factor-form]]
   [wsid.views.scenarios :refer [v-scenarios-panel]]))

(declare v-modal-dialog)

(defn v-modal-dialog [render? content-fn]
  [:dialog.modal-container (conj {}
                                 (if render? {:open true} nil)
                                 {:class ["absolute" "w-screen" "h-screen" "bg-red-300" "left-0" "top-0"]})
   (if render? (content-fn) nil)])

(defn v-main-panel []
  (let [factor-active (<sub [:factor-active-is-set])]
    [:main.main-container
      [:div.title__wrapper
       [:span.title__acronym "w.s.i.d ?"]
       [:h1.title 
        {:class ["text-cyan-700" "font-bold" "italic" "text-6xl"]}
        "What Should I Do?"]]
      [:div.decision-container
       [t (v-factors-panel) :v-factors-panel]
       [v-scenarios-panel]]
      [v-modal-dialog (not factor-active) v-factor-form]]))

(defn v-main []
  [:div.wsid-app
   [t (v-main-panel)]])