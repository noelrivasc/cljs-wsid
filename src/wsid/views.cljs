(ns wsid.views
  (:require
   [re-frame.core :as re-frame]
   [wsid.subs :as subs]
   [wsid.icons :as i]))

(declare v-factor-card v-factors-panel v-scenarios-panel v-factor-form v-factor-interpretation v-modal-dialog)

(defn v-modal-dialog [render? content-fn]
  [:dialog.modal-container (conj {}
                                 (if render? {:open true} nil)
                                 {:class ["absolute" "w-screen" "h-screen" "bg-red-300" "left-0" "top-0"]})
   (if render? (content-fn) nil)])

(defn v-main-panel []
  (let [factor-active (re-frame/subscribe [::subs/factor-active])]
    [:div.wsid-app
     [:main
      [:div.title__wrapper
       [:h1.title 
        {:class ["text-cyan-700" "font-bold" "italic" "text-6xl"]}
        "What Should I Do?"]]
      [:div.decision-container
       (v-factors-panel)
       (v-scenarios-panel)]
      (v-modal-dialog (not (nil? @factor-active)) v-factor-form)]]))

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
       [:h2.factors-panel__heading
        {:class ["text-xl" "text-yellow-400"]}
        "Factors"]
       [:div.factors-panel__heading__add-button
        [:button.factors-panel__heading__add-button__button
         {:type "button"
          :value "add"
          :on-click #(re-frame.core/dispatch [:factor-create])}
         [:span.icon
          (i/get-icon i/square-plus ["fill-red-800" "size-4"])]]]]
      [:ul.factors-panel__list
       (map v-factor-card @factors)]]]))

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
               :class ["factor-form__actions__button" "factor-form__actions__button--cancel"]
               :on-click #(re-frame.core/dispatch [:factor-active-cancel])}]
      [:input {:type "button"
               :value "save"
               :class ["factor-form__actions__button" "factor-form__actions__button--save"]
               :on-click #(re-frame.core/dispatch [:factor-active-save])}]]]]
    ))

(defn v-factor-interpretation []
  (let [range-interpretation (re-frame/subscribe [::subs/factor-active-range-interpretation])]
    [:div.factor-active-edit__range-interpretation @range-interpretation]))

(defn v-scenarios-panel []
  [:div.scenarios-panel "Wait a bit."])