(ns wsid.views.decisions
  (:require 
   [wsid.views.icons :as i]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.events.main :refer [evt>]]))

  
(defn v-decision-form []
  (let [decision-default-title (<sub [:decision-title])
        decision-default-description (<sub [:decision-description])
        d (do (println decision-default-title))
        decision-edit (fn [title description]
                        (println title description))]
    [:div.form.form--decision
     [:div.form__field
      [:label.form__label {:for "decision-title"} "Title"]
      [:input.form__input
       {:defaultValue decision-default-title
        :id "decision-title"
        :name "title"}]]
     [:div.form__field
       [:label.form__label {:for "decision-description"} "Description"]
       [:textarea.form__input.form__input--textarea
        {:defaultValue decision-default-description
         :id "decision-description"
         :name "description"}]]]))

(defn v-decision-panel []
  [:div.panel__wrapper.panel__wrapper--decision
   [:div.panel
    [:div.panel__header
     [:h2.panel__title "About this decision"]
     [:button.panel__add-button
      {:type "button"
       :value "add"
       :on-click #(evt> [:factor-create])}
      [:span.icon
       (i/get-icon i/square-plus)]]]
    [:div.panel__list [v-decision-form]]]])
         
