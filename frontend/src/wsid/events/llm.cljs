(ns wsid.events.llm
  (:require
   [re-frame.core :as re-frame]
   [superstructor.re-frame.fetch-fx]
   [wsid.config :as config]
   [wsid.db :as db]
   [wsid.events.decisions :as decisions]
   [clojure.spec.alpha :as s]))

(defn- string-keys
  "Converts the keys in the map to strings, recursively."
  [m]
  (into {} (map (fn [[k v]]
                  [(str (symbol k))
                   (if (map? v)
                     (string-keys v)
                     v)])
                m)))

(defn conform-llm-decision
  "Prepares the decision data from an LLM response to conform to ::db/decision"
  [decision]
  ;; The conversion of the keys in scenario-factor-values to keys is needed
  ;; because by default, the keys are converted to keywords. Keywords are fine
  ;; for the other maps.
  (let [prepared-decision (assoc decision :scenario-factor-values (string-keys (:scenario-factor-values decision)))
        decision-valid? (s/valid? ::db/decision prepared-decision)]
    (if decision-valid?
      prepared-decision
      (when config/debug?
        (println "The received decision data is not valid.")
        (println (s/explain ::db/decision prepared-decision))))))

(defn process-llm-decision-fetch-success
  "Processes the response of the call to the LLM service.
   - validates the decision response
   - loads the decision to state if valid
   
   Arguments:
   - db: map conforming to ::db/app-db
   - response: the JSON response, parsed
   
   Output: ::db/app-db with the decision loaded if validation succeeded"
  [db response]
  (let [decision-raw (get-in response [:body :llm-message])
        decision (conform-llm-decision decision-raw)]

    (-> db
        (decisions/load-decision decision)
        (assoc-in [:transient :llm-request-pending] false))))

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

(re-frame/reg-event-db
 :llm-fetch-success
 (fn [db [_ response]]
   (process-llm-decision-fetch-success db response)))

(re-frame/reg-event-db
 :llm-fetch-failure
 (fn [db [_ response]]
   (when config/debug?
     (println "LLM fetch failed.")
     (println response))
   (assoc-in db [:transient :llm-request-pending] false)))