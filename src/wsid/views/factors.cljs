(ns wsid.views.factors
  (:require 
   [wsid.views.icons :as i]
   [re-frame.core :as re-frame]
   [wsid.subs :as subs]
   [wsid.util.theming
    :refer [apply-current-theme]
    :rename {apply-current-theme t}]))

(declare v-factor-card v-factors-panel v-factor-form v-factor-interpretation v-factor-range)

(defn v-factor-card [factor]
  [:div.factor-card {:key (:id factor)}
   [:div.factor-card__inner
    [:div.factor-card__title (:title factor)]
    [:div.factor-card__range
     (v-factor-range (:min factor) (:max factor) 120 15)]
    [:button.factor-card__edit-button
     {:type "button"
      :value "add"
      :on-click #(re-frame.core/dispatch [:factor-edit factor])}
     [:span.icon
      (i/get-icon i/edit)]]]])

(defn v-factor-range [minimum maximum width height]
  (let [negative-width (* (/ minimum 10) (/ width 2) -1)
        positive-width (* (/ maximum 10) (/ width 2))
        centerline-position (/ width 2)]
    [:div.factor-range
     {:style {:width (str width "px")
              :height (str height "px")}
      :class ["bg-cyan-100" "border-cyan-500" "border-1" "box-content"]}
     [:div.factor-range__center-line
      {:style {:position "absolute"
               :left (str centerline-position "px")
               :height (str height "px")}
       :class ["border-l-1" "border-cyan-500"]}]
     [:div.factor-range__negative-bar
      {:style {:position "absolute"
               :left (str (+ (* -1 negative-width) centerline-position) "px")
               :height (str height "px")
               :width (str negative-width "px") }
       :class ["bg-red-300"]}]
     [:div.factor-range__positive-bar
      {:style {:position "absolute"
               :left (str (+ 1 centerline-position) "px")
               :height (str height "px")
               :width (str positive-width "px") }
      :class ["bg-blue-300"]}]]))

(defn v-factors-panel []
  (let [factors (re-frame/subscribe [::subs/factors-sorted])]
    [:div.factors-panel__wrapper
     [:div.factors-panel
      [:div.factors-panel__heading__wrapper
       [:h2.factors-panel__heading
        {:class ["text-xl" "text-yellow-400"]}
        "Factors"]
       [:div.factors-panel__heading__add
        [:button.factors-panel__heading__add__button
         {:type "button"
          :value "add"
          :on-click #(re-frame.core/dispatch [:factor-create])}
         [:span.icon
          (i/get-icon i/square-plus)]]]]
      [:ul.factors-panel__list
       (map #(t (v-factor-card %)) @factors)]]]))

(defn v-factor-form []
  (let [factor-edit-defaults (re-frame/subscribe [::subs/factor-edit-defaults])
        factor-active (re-frame/subscribe [::subs/factor-active])
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
     (v-factor-range (:min @factor-active) (:max @factor-active) 120 15)
     [:div.factor-form__actions
      (if (:id @factor-edit-defaults)
        [:input {:type "button"
                 :value "delete"
                 :class ["factor-form__actions__button" "factor-form__actions__button--delete"]
                 :on-click #(re-frame.core/dispatch [:factor-active-delete])}]
        nil)
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