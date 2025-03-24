(ns wsid.views
  (:require
   [re-frame.core :as re-frame]
   [wsid.subs :as subs]
   [wsid.style-helpers :as style]))

(declare v-factor-card v-factors-panel v-factor-form v-factor-interpretation)

(defn v-main-panel []
  (let [factor-active (re-frame/subscribe [::subs/factor-active])]
    [:div.wsid-app
     [:main
      [:div.title__wrapper
       [:h1.title  "What Should I Do?"]]
      (v-factors-panel)
      [:dialog.modal-container (conj {}
                                     (if (nil? @factor-active) nil {:open true}) ; TODO: switch to a modal-active container? or turn into a component
                                     {:class style/modal-container})
       (if (nil? @factor-active) nil (v-factor-form))]]]))

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
  (let [factor-edit-defaults (re-frame/subscribe [::subs/factor-edit-defaults])
        update-factor (fn [el] (let [type (-> el .-target .-type)
                                     property (-> el .-target .-name)
                                     value (-> el .-target .-value)]
                                 (re-frame.core/dispatch [:factor-active-update property
                                                          (if (= type "number") (parse-long value) value)])))] 
    [:details {:open true
               :class ["border-2 border-blue-600"]}
     [:summary "This is the summary"]
     [:form.factor-active-edit  
     [:label {:for "factor-title"} "Title"
      [:input {:defaultValue (:title @factor-edit-defaults)
               :id "factor-title"
               :name "title"
               :class ["factor-active-edit__input"]
               :on-change update-factor}]]
     [:label {:for "factor-description"} "Description"
      [:textarea {:defaultValue (:description @factor-edit-defaults)
                  :id "factor-description" 
                  :class ["factor-active-edit__textarea"]
                  :name "description"
                  :on-change update-factor}]]
     [:label {:for "factor-min"} "Minimum value"
      [:input {:defaultValue (:min @factor-edit-defaults)
               :type "number"
               :min -10
               :max 0
               :id "factor-min"
               :name "min" 
               :class ["factor-active-edit__input" "factor-active-edit__input--number"]
               :on-change update-factor}]]
     [:label {:for "factor-max"} "Maximum value"
      [:input {:defaultValue (:max @factor-edit-defaults)
               :type "number"
               :min 0
               :max 10
               :id "factor-max"
               :name "max"
               :class ["factor-active-edit__input" "factor-active-edit__input--number"]
               :on-change update-factor}]]
     (v-factor-interpretation)
     [:div.factor-form__actions
      (if (:id factor-edit-defaults) [:input {:type "button" 
                                              :value "delete"
                                              :class ["factor-form__actions__button" "factor-form__actions__button--delete"]}] nil)
      [:input {:type "button"
               :value "cancel"
               :class ["factor-form__actions__button" "factor-form__actions__button--cancel"]}]
      [:input {:type "button"
               :value "save"
               :class ["factor-form__actions__button" "factor-form__actions__button--save"]
               :on-click #(re-frame.core/dispatch [:factor-active-save])}]]]]
    ))

(defn v-factor-interpretation []
  (let [range-interpretation (re-frame/subscribe [::subs/factor-active-range-interpretation])]
    [:div.factor-active-edit__range-interpretation @range-interpretation]))