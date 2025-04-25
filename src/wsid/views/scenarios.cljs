(ns wsid.views.scenarios
  (:require 
    [wsid.events.main :refer [evt>]]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.views.icons :as i]))

(defn v-scenarios-panel []
  [:div.scenarios-panel__wrapper
   [:div.scenarios-panel
    [:div.scenarios-panel__heading__wrapper
     [:h2.scenarios-panel__heading "Scenarios"]
     [:div.scenarios-panel__heading__add
        [:button.scenarios-panel__heading__add__button
         {:type "button"
          :value "add"
          :on-click #(evt> [:scenario-create])}
         [:span.icon
          (i/get-icon i/square-plus)]]]
     ]]
   ])

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