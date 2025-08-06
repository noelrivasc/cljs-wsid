(ns wsid.events.user
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   [clojure.spec.alpha :as s]
   [superstructor.re-frame.fetch-fx]))

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
              ; TODO: get URL from config and differentiate between environments
              :url "http://localhost:3000/api/user/login"
              :headers {"Content-Type" "application/json"
                        "Accept" "application/json"}
              :body (js/JSON.stringify #js {"email" (:email user-active)
                                           "password" (:password user-active)})
              :response-content-types {"application/json" :json}
              :on-success [:login-success]
              :on-failure [:login-failure]}})))

(re-frame/reg-event-db
 :login-success
 (fn [db [_ response]]
   (let [user-data (get response :body)]
     (-> db
         (assoc-in [:user :email] (:email user-data))
         (assoc-in [:user :jwt-token] (:jwt-token user-data))))))

(re-frame/reg-event-db
 :login-failure
 (fn [db [_ response]]
   (println "Login failed:" response)
   db))

(re-frame/reg-event-db
 :jwt-token-update
 (fn [db [_ token]]
   (assoc-in db [:transient :user :jwt-token] token)))

(re-frame/reg-event-fx
 :llm-fetch
 (fn [{:keys [db]} _]
   (let [description (:description db)
         jwt-token (get-in db [:transient :user :jwt-token])]
     {:fetch {:method :post
              ; TODO: get URL from config and differentiate between environments
              :url "http://localhost:3000/api/llm/wsid-1--mistral"
              :headers {"Authorization" (str "Bearer " jwt-token)
                        "Content-Type" "application/json"
                        "Accept" "application/json"}
              :body (js/JSON.stringify #js {"user-situation" description})
              :response-content-types {"application/json" :json}
              :on-success [:llm-fetch-success]
              :on-failure [:llm-fetch-failure]}})))

(defn- string-keys
  "Converts the keys in the map to strings, recursively."
  [m]
  (into {} (map (fn [[k v]]
                  (println k)
                  [(str (symbol k))
                   (if (map? v)
                     (string-keys v)
                     v)])
                m)))

(re-frame/reg-event-db
 :llm-fetch-success
 (fn [db [_ response]]
   (println (get-in response [:body :llm-message]))
   (if-let [decision-data (get-in response [:body :llm-message])]
     ;; TODOS
     ;; - fix the parsing of scenario-factor-values (convert keyword to string recursively or disable key munging)
     ;; - move this out of user
     ;; - validate the decision before populating (perhaps use existing code)
     ;; - implement retries (timeout, model busy, wrong output)
     ;; - show backend problems and retries to the user
     ;; - show some spinner
     ;; - consider making this async, the backend streaming?
     (-> db
         (assoc :factors (:factors decision-data))
         (assoc :scenarios (:scenarios decision-data))
         (assoc :scenario-factor-values (string-keys (:scenario-factor-values decision-data))))
     (do
       (println "There was an error getting the decision data.")
       db))))