(ns wsid.views.decisions
  (:require 
   [wsid.views.icons :as i]
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.events.main :refer [evt>]]
   [reagent.core :as r]))

  
(defn v-decision-form []
  (let [decision-default-title (<sub [:decision-title])
        decision-default-description (<sub [:decision-description])
        default-values {:title decision-default-title :description decision-default-description}

        ; DEBOUNCING
        live-values (r/atom default-values)
        commit-values #(evt> ^:ls-compare [:decision-metadata-update @live-values])
        debounce-timeout-id (r/atom nil)
        debounce-time-ms 250
        set-debounce-timeout (fn []
                               (js/clearTimeout @debounce-timeout-id)
                               (reset! debounce-timeout-id (js/setTimeout commit-values debounce-time-ms)))
        update-value (fn [el]
                         (let [property (-> el .-target .-name)
                               value (-> el .-target .-value)]
                           (swap! live-values assoc (keyword property) value)
                           (set-debounce-timeout)))]
                        
    [:form.form.form--decision
     {:on-change update-value} 
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
       #_[:button.panel__add-button
          {:type "button"
           :value "add"
           :class ["flex" "items-center"]
           :on-click #(evt> [:factor-create])}
          "Dictate "
          [:span.icon
           (i/get-icon i/microphone)]]]
      [:div.panel__list [v-decision-form]]]])
   
         
