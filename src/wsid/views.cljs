(ns wsid.views
  (:require
   [re-frame.core :as re-frame]
   [wsid.subs :as subs]
   ))

(declare v-factor-card v-factors-panel)

(defn v-main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      "Factors " @name]
     (v-factors-panel)
     ]))

(defn v-factor-card [factor]
  [:div.factor-card__wrapper {:key (:id factor)}
   [:div.factor-card
    [:div.factor-card__title (:title factor)]
    [:div.factor-card__range]
    [:div.factor-card__edit-button]]])

(defn v-factors-panel []
  (let [factors (re-frame/subscribe [::subs/factors-sorted])]
    [:div.factors-panel__wrapper
     [:div.factors-panel
      [:h2.factors-panel__heading]
      [:ul.factors-panel__list
       (map v-factor-card @factors)]]]))
