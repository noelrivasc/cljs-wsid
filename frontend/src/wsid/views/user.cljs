(ns wsid.views.user
  (:require
   [wsid.subs.main :as subs :refer [<sub]]
   [wsid.events.main :refer [evt>]]))

(defn v-user-form []
  (let [user-edit-defaults (<sub [:user-active])
        user-valid? (<sub [:user-active-is-valid])
        update-user (fn [el] (let [property (-> el .-target .-name)
                                   value (-> el .-target .-value)]
                               (evt> [:user-active-update property value])))]
    [:div.user-form
     [:form.form.form--user-active-edit
      [:div.form__field
       [:label.form__label {:for "user-email"} "Email"]
       [:input.form__input
        {:defaultValue (:email user-edit-defaults)
         :type "email"
         :id "user-email"
         :name "email"
         :on-change update-user}]]

      [:div.form__field
       [:label.form__label {:for "user-password"} "Password"]
       [:input.form__input
        {:defaultValue (:password user-edit-defaults)
         :type "password"
         :id "user-password"
         :name "password"
         :on-change update-user}]]

      [:div.form__actions
       [:input.button
        {:type "button"
         :value "Cancel"
         :on-click #(evt> [:user-active-cancel])}]

       [:input.button.button--primary
        (conj {:type "button"
               :value "Log In"
               :on-click #(evt> [:submit-login])}
              (when-not user-valid? {:disabled true}))]]]]))