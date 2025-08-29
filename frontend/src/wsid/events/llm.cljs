(ns wsid.events.llm
  (:require
   [re-frame.core :as re-frame]
   [superstructor.re-frame.fetch-fx]
   [wsid.config :as config]))

(re-frame/reg-event-fx
 :llm-fetch
 (fn [{:keys [db]} _]
   (let [description (get-in db [:decision :description])
         title (get-in db [:decision :title])
         situation (str "Decision title: " title " Description of the situation: " description)
         jwt-token (get-in db [:user :jwt-token])]
     {:db (assoc-in db [:transient :llm-request-pending] true)
      :fetch {:method :post
              :url (str (get-in config/config [:api :base-url]) "/llm/wsid-1--mistral")
              :headers {"Authorization" (str "Bearer " jwt-token)
                        "Content-Type" "application/json"
                        "Accept" "application/json"}
              :body (js/JSON.stringify #js {"user-situation" situation})
              :mode :cors
              :credentials :omit
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
     (-> db
         (assoc-in [:decision :factors] (:factors decision-data))
         (assoc-in [:decision :scenarios] (:scenarios decision-data))
         (assoc-in [:decision :scenario-factor-values] (string-keys (:scenario-factor-values decision-data)))
         (assoc-in [:transient :llm-request-pending] false))
     (do
       (println "There was an error getting the decision data.")
       (assoc-in db [:transient :llm-request-pending] false)))))

(re-frame/reg-event-db
 :llm-fetch-failure
 (fn [db [_ response]]
   (println "LLM fetch failed:" response)
   (assoc-in db [:transient :llm-request-pending] false)))