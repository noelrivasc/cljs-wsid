(ns wsid.events.user
  (:require
   [clojure.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [superstructor.re-frame.fetch-fx]
   [wsid.config :as config]
   [wsid.db :as db]))

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

(re-frame/reg-event-fx
 :llm-fetch
 (fn [{:keys [db]} _]
   (let [description (get-in db [:decision :description])
         jwt-token (get-in db [:user :jwt-token])]
     {:fetch {:method :post
              :url (str (get-in config/config [:api :base-url]) "/llm/wsid-1--mistral")
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
     ;; - move this out of user
     ;; - validate the decision before populating (perhaps use existing code)
     ;; - implement retries (timeout, model busy, wrong output)
     ;; - show backend problems and retries to the user
     ;; - show some spinner
     ;; - use SSE?
     (-> db
         (assoc-in [:decision :factors] (:factors decision-data))
         (assoc-in [:decision :scenarios] (:scenarios decision-data))
         (assoc-in [:decision :scenario-factor-values] (string-keys (:scenario-factor-values decision-data))))
     (do
       (println "There was an error getting the decision data.")
       db))))