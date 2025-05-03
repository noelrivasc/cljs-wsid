(ns wsid.views.scenarios
  (:require 
    [wsid.events.main :refer [evt>]]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.views.icons :as i]
   [wsid.util.theming
    :refer [apply-current-theme]
    :rename {apply-current-theme t}]
   [reagent.core :as r]))

(defn v-scenario-factor-item [scenario-id scenario-factor value-update-callback]
  (let [datalist-id (str "datalist-" scenario-id "-" (:id scenario-factor))]
    [:div.scenario-factor-item {:key (:id scenario-factor)}
     [:div.scenario-factor-item__title (:title scenario-factor)]
     [:div.scenario-factor-item__value
      [:datalist.scenario-factor-item__datalist {:id datalist-id}
       (for [x (range (:min scenario-factor) (+ 1 (:max scenario-factor)))]
         [:option {:value x :label x :key x}])]
      [:input.scenario-factor-item__slider
       {:type "range"
        :default-value (:scenario-value scenario-factor)
        :min (:min scenario-factor)
        :max (:max scenario-factor)
        :on-change (fn [el] (value-update-callback (:id scenario-factor) (-> el .-target .-value js/parseInt)))}]]]))

(defn v-scenario-factors [scenario-id]
  (let [scenario-factors (<sub [:scenario-factors scenario-id])

        ; TESTING A DEBOUNCING STRATEGY
        ; (alternative: use :on-pointer-down and :on-pointer-up?)

        ; Default values; changes from global state
        scenario-factor-values (<sub [:scenario-factor-values scenario-id])

        ; Keeps values locally until they are committed
        live-values (r/atom scenario-factor-values)

        ; Each time live-values change, the debounce timeout is reset.
        ; The commit only goes out as an evt when the timeout expires.
        commit-values #(evt> [:scenario-factor-values-update scenario-id @live-values])
        debounce-timeout-id (r/atom nil)
        debounce-time-ms 250
        set-debounce-timeout (fn []
                               (js/clearTimeout @debounce-timeout-id)
                               (reset! debounce-timeout-id (js/setTimeout commit-values debounce-time-ms)))
        update-value (fn [factor-id value]
                       (swap! live-values assoc factor-id value)
                       (set-debounce-timeout))]
    [:details.scenario-card__factors
     [:summary.scenario-card__factors__summary "Decision Factors"]
     [:div.scenario-card__factors__instructions
      "Move the slider to adjust the points this scenario gets for each decision factor."]
     [:ul.scenario-card__factors-list
      (for [f scenario-factors]
        ^{:key (str "factor-" scenario-id "-" f)}
        [t [v-scenario-factor-item scenario-id f update-value]])
      ]]))

(defn v-scenario-card [scenario-id]
  (let [scenario (<sub [:scenario scenario-id])
        scenario-score (<sub [:scenario-score scenario-id])]
    [:div.scenario-card {:key scenario-id}
     [:div.scenario-card__inner
      [:div.scenario-card__title (:title scenario)]
      [:div.scenario-card__description (:description scenario)]
      [:div.scenario-card__score scenario-score]
      [v-scenario-factors scenario-id]]]))

(defn v-scenarios-panel []
  (let [scenario-ids (<sub [:scenario-ids])]
    [:div.scenarios-panel__wrapper
     [:div.scenarios-panel
      [:div.scenarios-panel__heading__wrapper
       [:h2.scenarios-panel__heading "Scenarios"]
       [:div.scenarios-panel__heading__add
        [:button.scenarios-panel__heading__add__button
         {:type "button"
          :value "add"
          :on-click #(evt> [:scenario-create-stub])}
         [:span.icon
          (i/get-icon i/square-plus)]]]]
      [:ul.scenarios-panel__list
       (for [id scenario-ids]
         ^{:key id}
         [t [v-scenario-card id]])]]]))

(defn v-scenario-form []
  (let [scenario-edit-defaults (<sub [:scenario-edit-defaults])
        scenario-valid? (<sub [:scenario-active-is-valid])
        update-scenario (fn [el] (let [type (-> el .-target .-type)
                                     property (-> el .-target .-name)
                                     value (-> el .-target .-value)]
                                 (evt> [:scenario-active-update property
                                        (if (= type "number") (parse-long value) value)])))]
    [:details {:open true}
     [:summary "Edit Scenario"]
     [:form.scenario-active-edit
      [:label {:for "scenario-title"} "Title"
       [:input.scenario-active-edit__input
        {:defaultValue (:title scenario-edit-defaults)
         :id "scenario-title"
         :name "title"
         :on-change update-scenario}]]
      [:label {:for "scenario-description"} "Description"
       [:textarea.scenario-active-edit__textarea
        {:defaultValue (:description scenario-edit-defaults)
         :id "scenario-description"
         :name "description"
         :on-change update-scenario}]]

      [:div.scenario-form__actions
       (if (:id scenario-edit-defaults)
         [:input.scenario-form__actions__button.scenario-form__actions__button--delete
          {:type "button"
           :value "delete"
           :on-click #(evt> [:scenario-active-delete])}]
         nil)
       [:input.scenario-form__actions__button.scenario-form__actions__button--cancel
        {:type "button"
         :value "cancel"
         :on-click #(evt> [:scenario-active-cancel])}]

       [:input.scenario-form__actions__button.scenario-form__actions__button--save
        (conj {:type "button"
               :value "save"
               :on-click #(evt> [:scenario-active-save])}
              (when-not scenario-valid? {:disabled true}))]]]]))