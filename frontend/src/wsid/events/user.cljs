(ns wsid.events.user
  (:require
   [clojure.edn :as edn]
   [clojure.spec.alpha :as s]
   [re-frame.cofx :refer [inject-cofx]]
   [re-frame.core :as re-frame]
   [superstructor.re-frame.fetch-fx]
   [wsid.config :as config]
   [wsid.db :as db]
   [wsid.events.local-storage :as local-storage :refer [ls-keys]]))

;; TODO: take logic out of the events

(defn validate-user [^str stored]
  (let [parsed (edn/read-string stored)
        is-valid (s/valid? ::db/user parsed)]
    (if is-valid
      parsed
      (do
        (println "Failed to load user from local store:")
        (s/explain ::db/user parsed)))))

(re-frame/reg-event-fx
 :app/load-user-from-storage
 [(inject-cofx :local-storage/load (:user ls-keys))]
 (fn [{db :db local-storage :local-storage/load} _]
   (if-let [stored-user (validate-user local-storage)]
     {:db (assoc db :user stored-user)}
     {:db db})))

(re-frame/reg-event-db
 :authenticate
 (fn [db _]
   (let [active-user {:email ""
                      :password ""}]
     (-> db
         (assoc-in [:transient :user-active] active-user)
         (assoc-in [:transient :user-active-validation :is-valid] false)))))

(re-frame/reg-event-db
 :user-active-update
 (fn [db [_ property value]]
   (let [updated-user (assoc (get-in db [:transient :user-active]) (keyword property) value)
         user-valid? (s/valid? ::db/user-credentials updated-user)]
     (-> db
         (assoc-in [:transient :user-active] updated-user)
         (assoc-in [:transient :user-active-validation :is-valid] user-valid?)))))

(re-frame/reg-event-db
 :user-active-cancel
 (fn [db _]
   (-> db
       (assoc-in [:transient :user-active] nil)
       (assoc-in [:transient :user-active-validation :is-valid] nil))))

(re-frame/reg-event-fx
 :submit-login
 (fn [{:keys [db]} _]
   (let [user-active (get-in db [:transient :user-active])]
     {:db (assoc-in db [:transient :user-active] nil)
      :fetch {:method :post
              :url (str (get-in config/config [:api :base-url]) "/user/login")
              :headers {"Content-Type" "application/json"
                        "Accept" "application/json"}
              :body (js/JSON.stringify #js {"email" (:email user-active)
                                           "password" (:password user-active)})
              :mode :cors
              :credentials :omit
              :response-content-types {"application/json" :json}
              :on-success [:login-success]
              :on-failure [:login-failure]}})))

(re-frame/reg-event-fx
 :login-success
 (fn [{:keys [db]} [_ response]]
   (let [user-data (get response :body)
         updated-db (-> db
                        (assoc-in [:user :email] (:email user-data))
                        (assoc-in [:user :jwt-token] (:jwt-token user-data)))]
     {:db updated-db
      :local-storage/save {:key (:user ls-keys) 
                           :val (:user updated-db)}})))

(re-frame/reg-event-db
 :login-failure
 (fn [db [_ response]]
   (println "Login failed:" response)
   db))

(re-frame/reg-event-fx
 :logout
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:user :email] nil)
            (assoc-in [:user :jwt-token] nil))
    :local-storage/save {:key (:user ls-keys) 
                         :val nil}}))
