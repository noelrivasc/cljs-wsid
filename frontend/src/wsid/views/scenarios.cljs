(ns wsid.views.scenarios
  (:require
   [wsid.events.main :refer [evt>]]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.views.icons :as i]
   [reagent.core :as r]))

(defn v-scenario-factor-item [scenario-id scenario-factor value-update-callback]
  (let [datalist-id (str "datalist-" scenario-id "-" (:id scenario-factor))]
    [:div.scenario-factor-item {:key (:id scenario-factor)}
     [:div.scenario-factor-item__header
      [:div.scenario-factor-item__title (:title scenario-factor)]
      [:div.scenario-factor-item__value (:scenario-value scenario-factor)]]
     [:div.scenario-factor-item__slider
      [:datalist.scenario-factor-item__datalist {:id datalist-id}
       (for [x (range -10 11)]
         [:option {:value x :label x :key x}])]
      [:input.scenario-factor-item__slider
       {:type "range"
        :default-value (:scenario-value scenario-factor)
        :min -10
        :max 10
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
        commit-values #(evt> ^:ls-compare [:scenario-factor-values-update scenario-id @live-values])
        debounce-timeout-id (r/atom nil)
        debounce-time-ms 250
        set-debounce-timeout (fn []
                               (js/clearTimeout @debounce-timeout-id)
                               (reset! debounce-timeout-id (js/setTimeout commit-values debounce-time-ms)))
        update-value (fn [factor-id value]
                       (swap! live-values assoc factor-id value)
                       (set-debounce-timeout))]
    [:details.scenario-factors
     [:summary.scenario-factors__summary "Decision Factors"]
     [:div.scenario-factors__instructions
      "Move the slider to adjust the points this scenario gets for each decision factor."]
     [:ul.scenario-factors__list
      (for [f scenario-factors]
        ^{:key (str "factor-" scenario-id "-" f)}
        [v-scenario-factor-item
         scenario-id
         (assoc f :scenario-value (get scenario-factor-values (:id f)))
         update-value])]]))

(defn v-scenario-card [scenario-id]
  (let [scenario (<sub [:scenario scenario-id])
        scenario-score (<sub [:scenario-score scenario-id])]
    [:div.card
     [:div.card__start
      [:div.card__title (:title scenario)]
      [:div.card__description (:description scenario)]
      [:div.card__score
       [:div.score-label "Score"]
       [:div.score-value scenario-score]]
      [v-scenario-factors scenario-id]]
     [:div.card__end
      [:button.card__edit-button
       {:type "button"
        :value "edit"
        :on-click #(evt> [:scenario-edit scenario])}
       [:span.icon
        (i/get-icon i/edit)]]]]))

(defn v-scenarios-panel []
  (let [scenario-ids (<sub [:scenario-ids-sorted])]
    [:div.panel__wrapper
     [:div.panel
      [:div.panel__header
       [:h2.panel__title "Scenarios"]
       [:button.panel__add-button
        {:type "button"
         :value "add"
         :on-click #(evt> [:scenario-create-stub])}
        [:span.icon
         (i/get-icon i/square-plus)]]]
      [:ul.panel__list
       (for [id scenario-ids]
         ^{:key id}
         [v-scenario-card id])]]]))

(defn v-scenario-form []
  (let [scenario-edit-defaults (<sub [:scenario-edit-defaults])
        scenario-valid? (<sub [:scenario-active-is-valid])
        update-scenario (fn [el] (let [type (-> el .-target .-type)
                                       property (-> el .-target .-name)
                                       value (-> el .-target .-value)]
                                   (evt> [:scenario-active-update property
                                          (if (= type "number") (parse-long value) value)])))]
    [:form.form
     [:div.form__field
      [:label.form__label {:for "scenario-title"} "Title"]
      [:input.form__input
       {:defaultValue (:title scenario-edit-defaults)
        :id "scenario-title"
        :name "title"
        :on-change update-scenario}]]

     [:div.form__field
      [:label.form__label {:for "scenario-description"} "Description"]
      [:textarea.form__input.form__input--textarea
       {:defaultValue (:description scenario-edit-defaults)
        :id "scenario-description"
        :name "description"
        :on-change update-scenario}]]

     [:div.form__actions
      (if (:id scenario-edit-defaults)
        [:input.button.button--danger
         {:type "button"
          :value "delete"
          :on-click #(evt> ^:ls-compare [:scenario-active-delete])}]
        nil)
      [:input.button
       {:type "button"
        :value "cancel"
        :on-click #(evt> [:scenario-active-cancel])}]

      [:input.button.button--primary
       (conj {:type "button"
              :value "save"
              :on-click #(evt> ^:ls-compare [:scenario-active-save])}
             (when-not scenario-valid? {:disabled true}))]]]))
