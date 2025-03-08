(ns wsid.views
  (:require
   [re-frame.core :as re-frame]
   [wsid.subs :as subs]
   ))

(declare v-factor-card v-factors-panel v-factor-form)

(defn v-main-panel []
  (let [name (re-frame/subscribe [::subs/name])
        current-factor (re-frame/subscribe [::subs/current-factor])]
    [:div
     [:h1
      "Factors" @name]
     (v-factors-panel)
     (when-not (nil? current-factor) (v-factor-form))
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
      [:div.factors-panel__heading__wrapper 
       [:h2.factors-panel__heading]
       [:div.factors-panel__heading__edit-button
        [:input.factors-panel__heading__edit-button__button 
         {:type "button" :value "add" :on-click #(re-frame.core/dispatch [:factors-create])}]]]
      [:ul.factors-panel__list
       (map v-factor-card @factors)]]]))

(defn v-factor-form []
  (let [current-factor (re-frame/subscribe [::subs/current-factor])]
    [:div "aqui va el formulario"]))