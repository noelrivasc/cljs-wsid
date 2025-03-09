(ns wsid.views
  (:require
   [re-frame.core :as re-frame]
   [wsid.subs :as subs]))

(declare v-factor-card v-factors-panel v-factor-form)

(defn v-main-panel []
  (let [current-factor (re-frame/subscribe [::subs/current-factor])]
    [:div
     [:h1
      "Factors"]
     (v-factors-panel)
     (println "*" @current-factor "*")
     (if (nil? @current-factor) nil (v-factor-form))
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
         {:type "button" 
          :value "add"
          :on-click #(re-frame.core/dispatch [:factor-create])
          }]]]
      [:ul.factors-panel__list
       (map v-factor-card @factors)]]]
    ))

(defn v-factor-form []
  (let [current-factor (re-frame/subscribe [::subs/current-factor])
        update-factor (fn [el] (let [property (-> el .-target .-name)
                                     value (-> el .-target .-value)]
                                 (re-frame.core/dispatch [:factor-active-update property value])))
        ] 
    [:form.factor-active-edit  
     [:label {:for "factor-title"} "Title"
      [:input {:defaultValue (:title @current-factor)
               :id "factor-title"
               :name "factor-title"
               :class ["factor-active-edit__input"]
               :on-change update-factor}]]
     [:label {:for "factor-description"} "Description"
      [:textarea {:defaultValue (:description @current-factor)
                  :id "factor-description" 
                  :class ["factor-active-edit__textarea"]
                  :name "factor-description"
                  :on-change update-factor}]]
     [:label {:for "factor-min"} "Minimum value"
      [:input {:defaultValue (:title @current-factor)
               :type "number"
               :min -10
               :max 0
               :id "factor-min"
               :name "factor-min" 
               :class ["factor-active-edit__input" "factor-active-edit__input--number"]
               :on-change update-factor}]]
     [:label {:for "factor-max"} "Maximum value"
      [:input {:defaultValue (:title @current-factor)
               :type "number"
               :min 0
               :max 10
               :id "factor-max"
               :name "factor-max"
               :class ["factor-active-edit__input" "factor-active-edit__input--number"]
               :on-change update-factor}]]
     [:div "Interpretation: soon."]
     [:div.factor-form__actions
      (if (:id current-factor) [:input.factor-form__actions__button.factor-form__actions__button--delete {:type "button" :value "delete"}] nil)
      [:input.factor-form__actions__button.factor-form__actions__button--cancel {:type "button" :value "cancel"}]
      [:input.factor-form__actions__button.factor-form__actions__button--save {:type "button" :value "save"}]]]))