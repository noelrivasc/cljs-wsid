(ns wsid.events.scenarios
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   [wsid.events.factors]
   [wsid.domain.scenarios :as scenarios]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor

   [wsid.events.util :refer [evt>]]
   [clojure.spec.alpha :as s]))

(defn scenario-create-stub
  "If the :scenario-active is empty, creates and inserts a new scenario
   to both the :scenario-active and the :scenario-edit-defaults.
   If the :scenario-active is set, just returns the existing db.

   Arguments:
   - db: a map that conforms to ::db/app-db schema

   Output:
   - A new ::db/app-db"
  [db]
  (let [active-scenario (get-in db [:transient :scenario-active])]
    (if (nil? active-scenario)
      (let [new-scenario (scenarios/create-new-scenario)]
        (-> db
            (assoc-in [:transient :scenario-active] new-scenario)
            (assoc-in [:transient :scenario-edit-defaults] new-scenario)))
      db)))

(defn scenario-edit
  "Loads the given scenario in both [:transient :scenario-active]
   and [:transient :scenario-edit-defaults].

   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - scenario: a map that conforms to the ::db/scenario schema
   
   Output:
   - A new ::db/app-db"
  [db scenario]
  (-> db
      (assoc-in [:transient :scenario-active] scenario)
      (assoc-in [:transient :scenario-edit-defaults] scenario)))

(defn scenario-active-update
  "Updates a property in the active scenario.
   
   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - property: string or keyword, the property to update
   - value: the new value for the property

   Output:
   - A new ::db/app-db"
  [db property value]
  (let [updated-scenario (scenarios/scenario-update-property
                          (get-in db [:transient :scenario-active])
                          property
                          value)
        scenario-valid? (s/valid? ::db/scenario updated-scenario)]
    (-> db
        (assoc-in [:transient :scenario-active] updated-scenario)
        (assoc-in [:transient :scenario-active-validation :is-valid] scenario-valid?))))

(defn scenario-factor-values-initialize-scenario
  "Initialize the factor values for the given scenario to nil.
   
   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - scenario-id: string, the id of the scenario
   
   Output:
   - A new ::db/app-db"
  [db scenario-id]
  (let [factors (get-in db [:decision :factors])
        factor-values (scenarios/initialize-scenario-factor-values factors)]
    (assoc-in db [:decision :scenario-factor-values scenario-id] factor-values)))

(defn scenario-factor-values-initialize-factor
  "Adds a new factor with nil value to all existing scenario factor values.
   
   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - factor-id: string, the id of the factor to add
   
   Output:
   - A new ::db/app-db"
  [db factor-id]
  (let [scenario-factor-values (get-in db [:decision :scenario-factor-values])
        updated-values (scenarios/initialize-factor-in-scenario-values scenario-factor-values factor-id)]
    (assoc-in db [:decision :scenario-factor-values] updated-values)))

(defn scenario-factor-values-prune-factor
  "Removes a factor from all scenario factor values.
   
   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - factor-id: string, the id of the factor to remove
   
   Output:
   - A new ::db/app-db"
  [db factor-id]
  (let [scenario-factor-values (get-in db [:decision :scenario-factor-values])
        updated-values (scenarios/prune-factor-from-scenario-values scenario-factor-values factor-id)]
    (assoc-in db [:decision :scenario-factor-values] updated-values)))

(defn scenario-factor-values-prune-scenario
  "Removes a scenario from the scenario factor values.
   
   Arguments:
   - db: a map that conforms to :db/app-db schema
   - scenario-id: string, the id of the scenario to remove
   
   Output:
   - A new ::db/app-db"
  [db scenario-id]
  (assoc-in db [:decision :scenario-factor-values]
            (dissoc (get-in db [:decision :scenario-factor-values]) scenario-id)))

(defn scenario-active-wipe
  "Unsets [:transient :scenario-active] and [:transient :scenario-edit-defaults]"
  [db]
  (-> db
      (assoc-in [:transient :scenario-edit-defaults] nil)
      (assoc-in [:transient :scenario-active] nil)))

(defn scenario-active-save
  "Saves the active scenario to the decision scenarios vector.
   - If the scenario is new, creates a UUID for it
   
   Arguments:
   - db
   
   Output: [db scenario-is-new scenario-id]
   - db: new ::db/app-db which includes the updated scenario
   - scenario-is-new: boolean, indicates newly inserted scenario (vs edited)
   - scenario-id: string, the id of the scenario"
  [db]
  (let [active-scenario (get-in db [:transient :scenario-active])
        [scenario-prepared scenario-is-new] (scenarios/scenario-create-id active-scenario)
        scenarios (conj
                   (vec (filter #(not (= (:id scenario-prepared) (:id %)))
                                (get-in db [:decision :scenarios])))
                   scenario-prepared)
        new-db (assoc-in db [:decision :scenarios] scenarios)]
    [new-db scenario-is-new (:id scenario-prepared)]))

(defn scenario-active-delete
  "Removes the :scenario-active from the decision scenarios vector.
   
   Arguments:
   - db: a ::db/app-db
   
   Output: [db deleted-scenario-id]
   - db: ::db/app-db
   - deleted-scenario-id: string"
  [db]
  (let [scenario-active (get-in db [:transient :scenario-active])
        scenarios (vec (filter #(not (= (:id %) (:id scenario-active))) (get-in db [:decision :scenarios])))
        new-db (-> db
                   (assoc-in [:decision :scenarios] scenarios)
                   (assoc-in [:transient :scenario-edit-defaults] nil)
                   (assoc-in [:transient :scenario-active] nil)
                   )]
    [new-db (:id scenario-active)]))

(defn scenario-factor-values-update
  "Updates the factor values for a specific scenario.
   
   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - scenario-id: string, the id of the scenario
   - values: map of factor-id -> numeric value
   
   Output:
   - A new ::db/app-db"
  [db scenario-id values]
  (assoc-in db [:decision :scenario-factor-values scenario-id] values))

;; Event definitions

;; Creates a new scenario if none is currently active
(re-frame/reg-event-db
 :scenario-create-stub
 (fn [db _]
   (scenario-create-stub db)))

;; Loads a scenario for editing in the active state
(re-frame/reg-event-db
 :scenario-edit
 (fn [db [_ scenario]]
   (scenario-edit db scenario)))

;; Updates a property of the currently active scenario
(re-frame/reg-event-db
 :scenario-active-update
 (fn [db [_ property value]]
   (scenario-active-update db property value)))

;; Initializes factor values for a new scenario with nil values
(re-frame/reg-event-db
 :scenario-factor-values-initialize-scenario
 (fn [db [_ scenario-id]]
   (scenario-factor-values-initialize-scenario db scenario-id)))

;; Adds a new factor to all existing scenarios with nil values
(re-frame/reg-event-db
 :scenario-factor-values-initialize-factor
 (fn [db [_ factor-id]]
   (scenario-factor-values-initialize-factor db factor-id)))

;; Removes a factor from all scenario factor values
(re-frame/reg-event-db
 :scenario-factor-values-prune-factor
 (fn [db [_ factor-id]]
   (scenario-factor-values-prune-factor db factor-id)))

;; Removes a scenario from the scenario factor values
(re-frame/reg-event-db
 :scenario-factor-values-prune-scenario
 (fn [db [_ scenario-id]]
   (scenario-factor-values-prune-scenario db scenario-id)))

;; Saves the active scenario and initializes factor values if new
(re-frame/reg-event-db
 :scenario-active-save
 (fn [db _]
   (let [[new-db scenario-is-new scenario-id] (scenario-active-save db)]
     (when scenario-is-new
       (evt> [:scenario-factor-values-initialize-scenario scenario-id]))
     (scenario-active-wipe new-db))))

;; Deletes the active scenario and removes its factor values
(re-frame/reg-event-db
 :scenario-active-delete
 (fn [db _]
   (let [[new-db deleted-scenario-id] (scenario-active-delete db)]
     (evt> [:scenario-factor-values-prune-scenario deleted-scenario-id])
     new-db)))

;; Cancels scenario editing and clears the active state
(re-frame/reg-event-db
 :scenario-active-cancel
 (fn [db _]
   (scenario-active-wipe db)))

;; Updates the factor values for a specific scenario
(re-frame/reg-event-db
 :scenario-factor-values-update
 (fn [db [_ scenario-id values]]
   (scenario-factor-values-update db scenario-id values)))
