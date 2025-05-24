(ns wsid.views.factors
  (:require 
   [wsid.views.icons :as i]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.events.main :refer [evt>]]))

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
  [:div.card.card--factor
   {:on-click #(evt> [:factor-edit factor])}
   [:div.card__start
    [:div.card__title (:title factor)]
    [:div.card__description (:description factor)]]
   [:div.card__end
    [:button.card__edit-button
     {:type "button"
      :value "edit"}
     [:button.icon
      (i/get-icon i/edit)]]]])


(defn v-factors-panel []
  (let [factors (<sub [:factors])] ; OPTIMIZE: subscribe to list of factor ids rather than factors
    [:div.panel__wrapper
     [:div.panel
      [:div.panel__header
       [:h2.panel__title "Factors"]
       [:button.panel__add-button
        {:type "button"
         :value "add"
         :on-click #(evt> [:factor-create])}
        [:span.icon
         (i/get-icon i/square-plus)]]]
      [:ul.panel__list
       (for [f factors]
         ^{:key (:id f)}
         [v-factor-card f])]]]))

(defn v-factor-form []
  (let [factor-edit-defaults (<sub [:factor-edit-defaults])
        factor-valid? (<sub [:factor-active-is-valid])
        update-factor (fn [el] (let [type (-> el .-target .-type)
                                     property (-> el .-target .-name)
                                     value (-> el .-target .-value)]
                                 (evt> [:factor-active-update property
                                        (if (= type "number") (parse-long value) value)])))]
    [:div.factor-form
     [:form.form.form--factor-active-edit
      [:div.form__field
       [:label.form__label {:for "factor-title"} "Title"]
       [:input.form__input
        {:defaultValue (:title factor-edit-defaults)
         :id "factor-title"
         :name "title"
         :on-change update-factor}]]

      [:div.form__field
       [:label.form__label {:for "factor-description"} "Description"]
       [:textarea.form__input.form__input--textarea
        {:defaultValue (:description factor-edit-defaults)
         :id "factor-description"
         :name "description"
         :on-change update-factor}]]

      [:div.form__field
       [:label.form__label {:for "factor-weight"} "Importance"]
       [:input.form__input.form__input--number
        {:defaultValue (:weight factor-edit-defaults)
         :type "range"
         :min 1
         :max 10
         :step 1
         :id "factor-weight"
         :name "weight"
         :on-change update-factor}]]

      [:div.form__actions
       (if (:id factor-edit-defaults)
         [:input.button.button--danger
          {:type "button"
           :value "delete"
           :on-click #(evt> [:factor-active-delete])}]
         nil)
       [:input.button
        {:type "button"
         :value "cancel"
         :on-click #(evt> [:factor-active-cancel])}]

       [:input.button.button--primary
        (conj {:type "button"
               :value "save"
               :on-click #(evt> [:factor-active-save])}
              (when-not factor-valid? {:disabled true}))]]]]))
