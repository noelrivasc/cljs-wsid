(ns wsid.views.factors
  (:require 
   [wsid.views.icons :as i]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.events.main :refer [evt>]]
   [wsid.util.theming
    :refer [apply-current-theme]
    :rename {apply-current-theme t}]))

; TODO use for result visualization or remove
#_(defn v-factor-range [minimum maximum width height]
  (let [negative-width (* (/ minimum 10) (/ width 2) -1)
        positive-width (* (/ maximum 10) (/ width 2))
        centerline-position (/ width 2)]
    [:div.factor-range
     {:style {:width (str width "px")
              :height (str height "px")
              :box-sizing "content-box"}}
     [:div.factor-range__center-line
      {:style {:position "absolute"
               :left (str centerline-position "px")
               :height (str height "px")}}]
     [:div.factor-range__negative-bar
      {:style {:position "absolute"
               :left (str (+ (* -1 negative-width) centerline-position) "px")
               :height (str height "px")
               :width (str negative-width "px") }}]
     [:div.factor-range__positive-bar
      {:style {:position "absolute"
               :left (str (+ 1 centerline-position) "px")
               :height (str height "px")
               :width (str positive-width "px") }}]]))

(defn v-factor-card [factor]
  [t [:div.factor-card
      [:div.factor-card__inner
       [:div.factor-card__title (:title factor)]
       [:div.factor-card__description (:description factor)]
       [:button.factor-card__edit-button
        {:type "button"
         :value "edit"
         :on-click #(evt> [:factor-edit factor])}
        [:span.icon
         (i/get-icon i/edit)]]]]])


(defn v-factors-panel []
  (let [factors (<sub [:factors])] ; OPTIMIZE: subscribe to list of factor ids rather than factors
    [t [:div.factors-panel__wrapper
        [:div.factors-panel
         [:div.factors-panel__heading__wrapper
          [:h2.factors-panel__heading "Factors"]
          [:div.factors-panel__heading__add
           [:button.factors-panel__heading__add__button
            {:type "button"
             :value "add"
             :on-click #(evt> [:factor-create])}
            [:span.icon
             (i/get-icon i/square-plus)]]]]
         [:ul.factors-panel__list
          (for [f factors]
           ^{:key (:id f)}
           [v-factor-card f])]]]]))

(defn v-factor-form []
  (let [factor-edit-defaults (<sub [:factor-edit-defaults])
        factor-valid? (<sub [:factor-active-is-valid])
        update-factor (fn [el] (let [type (-> el .-target .-type)
                                     property (-> el .-target .-name)
                                     value (-> el .-target .-value)]
                                 (evt> [:factor-active-update property
                                        (if (= type "number") (parse-long value) value)])))]
    [:details {:open true}
     [:summary "Edit Factor"]
     [:form.factor-active-edit
      [:label {:for "factor-title"} "Title"
       [:input.factor-active-edit__input
        {:defaultValue (:title factor-edit-defaults)
         :id "factor-title"
         :name "title"
         :on-change update-factor}]]
      [:label {:for "factor-description"} "Description"
       [:textarea.factor-active-edit__textarea
        {:defaultValue (:description factor-edit-defaults)
         :id "factor-description"
         :name "description"
         :on-change update-factor}]]
      [:label {:for "factor-weight"} "Weight"
       [:input.factor-active-edit__input.factor-active-edit__input--number
        {:defaultValue (:weight factor-edit-defaults)
         :type "number"
         :min 1
         :max 10
         :id "factor-weight"
         :name "weight"
         :on-change update-factor}]]

      [:div.factor-form__actions
       (if (:id factor-edit-defaults)
         [:input.factor-form__actions__button.factor-form__actions__button--delete
          {:type "button"
           :value "delete"
           :on-click #(evt> [:factor-active-delete])}]
         nil)
       [:input.factor-form__actions__button.factor-form__actions__button--cancel
        {:type "button"
         :value "cancel"
         :on-click #(evt> [:factor-active-cancel])}]

       [:input.factor-form__actions__button.factor-form__actions__button--save
        (conj {:type "button"
               :value "save"
               :on-click #(evt> [:factor-active-save])}
              (when-not factor-valid? {:disabled true}))]]]]))
