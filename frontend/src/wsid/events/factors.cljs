(ns wsid.events.factors
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   [wsid.events.util :refer [evt>]]
   [wsid.domain.factors :as factors]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor

   [clojure.spec.alpha :as s]))

(defn factor-create
  "If the :factor-active is empty, creates and inserts a new factor
   to both the :factor-active and the :factor-edit-defaults.
   If the :factor-active is set, just returns the existing db.

   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - factor: a map that conforms to the ::db/factor schema
   
   Output:
   - A new ::db/app-db"

  [db]
  (let [active-factor (get-in db [:transient :factor-active])]
    (if (nil? active-factor)
      (let [new-factor (factors/create-new-factor)]
        (-> db
            (assoc-in [:transient :factor-active] new-factor)
            (assoc-in [:transient :factor-edit-defaults] new-factor)))
      db)))

(re-frame/reg-event-db
 :factor-create
 (fn [db _]
   (factor-create db)))

(defn factor-edit
  "Loads the given factor in both [:transient :factor-active]
   and [:transient :factor-edit-defaults].

   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - factor: a map that conforms to the ::db/factor schema
   
   Output:
   - A new ::db/app-db"
  [db factor]
  (-> db
      (assoc-in [:transient :factor-active] factor)
      (assoc-in [:transient :factor-edit-defaults] factor)))

(re-frame/reg-event-db
 :factor-edit
 (fn [db [_ factor]]
   (factor-edit db factor)))

(defn factor-active-update
  "Updates a property in the active factor.
   
   Arguments:
   - db: a map that conforms to ::db/app-db schema
   - property: string or keyword, the property to update
   - value: the new value for the property

   Output:
   - A new ::db/app-db"
  [db property value]
  (let [updated-factor (factors/factor-update-property
                        (get-in db [:transient :factor-active])
                        property
                        value)
        factor-valid? (s/valid? ::db/factor updated-factor)]
    (-> db
        (assoc-in [:transient :factor-active] updated-factor)
        (assoc-in [:transient :factor-active-validation :is-valid] factor-valid?))))

(re-frame/reg-event-db
 :factor-active-update
 (fn [db [_ property value]]
   (factor-active-update db property value)))

(defn factor-active-wipe
  "Unsets [:transient :factor-active] and [:transient :factor-edit-defaults]"
  [db]
  (-> db
      (assoc-in [:transient :factor-edit-defaults] nil)
      (assoc-in [:transient :factor-active] nil)))

(defn factor-active-save
  "Saves the active factor to the decision factors vector.
   - If the factor is new, creates a UUID for it
   
   Arguments:
   - db
   
   Output: [db factor-is-new factor-id]
   - db: new ::db/app-db which includes the updated factor
   - factor-is-new: boolean, indicates newly inserted factor (vs edited)
   - factor-id: string, the id of the factor"
  [db]
  (let [active-factor (get-in db [:transient :factor-active])
        [factor-prepared factor-is-new] (factors/factor-create-id active-factor)
        factors (conj
                 (vec (filter #(not (= (:id factor-prepared) (:id %)))
                              (get-in db [:decision :factors])))
                 factor-prepared)
        new-db (assoc-in db [:decision :factors] factors)]
    [new-db factor-is-new (:id factor-prepared)]))

(re-frame/reg-event-db
 :factor-active-save
 (fn [db _]
   ; Save the active factor to the factors vector
   ; Clear the active-factor
   ; Trigger the initialization of factor values in existing scenarios
   (let [[new-db factor-is-new factor-id] (factor-active-save db)]
     (when factor-is-new
       (evt> [:scenario-factor-values-initialize-factor factor-id]))
     (factor-active-wipe new-db))))

(defn factor-active-delete
  "Removes the :factor-active from the decision factors vector.
   
   Arguments:
   - db: a ::db/app-db
   
   Output: [db deleted-factor-id]
   - db: ::db/app-db
   - deleted-factor-id: string"
  [db]
  (let [factor-active (get-in db [:transient :factor-active])
        factors (vec (filter #(not (= (:id %) (:id factor-active))) (get-in db [:decision :factors])))
        new-db (-> db
                   (assoc-in [:decision :factors] factors)
                   (assoc-in [:transient :factor-edit-defaults] nil)
                   (assoc-in [:transient :factor-active] nil))]
    
    [new-db (:id factor-active)]))

(re-frame/reg-event-db
 :factor-active-delete
 (fn [db _]
   (let [[new-db deleted-factor-id] (factor-active-delete db)]
     (evt> [:scenario-factor-values-prune deleted-factor-id])
     new-db)))

(re-frame/reg-event-db
 :factor-active-cancel
 (fn [db _]
   (factor-active-wipe db)))
