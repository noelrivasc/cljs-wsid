(ns wsid.events.scenarios
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   [wsid.events.factors]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor

   [clojure.spec.alpha :as s]))

(re-frame/reg-event-db
 :scenario-create-stub
 (fn [db _]
   (let
    [active-scenario-path [:transient :scenario-active]
     active-scenario
     (if (nil? (get-in db active-scenario-path))
       {:id ""
        :title ""
        :description ""}
       (get-in db active-scenario-path))]
     (-> db
         (assoc-in [:transient :scenario-edit-defaults] active-scenario)
         (assoc-in [:transient :scenario-active] active-scenario)))))

(re-frame/reg-event-db
 :scenario-edit
 (fn [db [_ scenario]]
   (-> db
       (assoc-in [:transient :scenario-active] scenario)
       (assoc-in [:transient :scenario-edit-defaults] scenario))))

(re-frame/reg-event-db
 :scenario-active-update
 (fn [db [_ property value]]
   (let [updated-scenario (assoc (get-in db [:transient :scenario-active]) (keyword property) value)
         scenario-valid? (s/valid? ::db/scenario updated-scenario)]
     (-> db
         (assoc-in [:transient :scenario-active] updated-scenario)
         (assoc-in [:transient :scenario-active-validation :is-valid] scenario-valid?)))))

(re-frame/reg-event-db
 :scenario-factor-values-initialize-scenario
 (fn 
   ; Initialize the factor values for the given scenario to nil.
   [db [_ scenario-id]]
   (assoc-in db 
             [:scenario-factor-values scenario-id]
             (zipmap (map :id (get-in db [:factors :all])) (repeat nil)))))

(defn clip-value
  "Clips the given value to prevent it from overflowing minimum and maximum,
   inclusive. Preserves nil values."
  [v minimum maximum]
  (if (nil? v) v
      (min maximum
           (max minimum v))))

(re-frame/reg-event-db
 :scenario-factor-values-initialize-factor
 (fn [db [_ factor-id]]
   (assoc-in db [:scenario-factor-values]
             (update-vals (get-in db [:scenario-factor-values])
                          (fn [s]
                            (assoc s factor-id nil))))))

(re-frame/reg-event-db
 :scenario-factor-values-clip-factor
 (fn [db [_ factor-id]]
   (assoc-in db [:scenario-factor-values]
             (update-vals (get-in db [:scenario-factor-values])
                          (fn [s]
                            (let [factor (wsid.events.factors/get-factor-by-id (get-in db [:factors :all]) factor-id)
                                  new-value (clip-value
                                             (get s factor-id)
                                             (:min factor)
                                             (:max factor))]
                              (assoc s factor-id new-value)))))))
(re-frame/reg-event-db
 :scenario-factor-values-prune
 (fn [db [_ factor-id]]
   (assoc-in db [:scenario-factor-values]
             (update-vals (get-in db [:scenario-factor-values])
                          (fn [s]
                            (dissoc s factor-id))))))

(re-frame/reg-event-db
 :scenario-active-save
 (fn [db _]
   ; If the scenario does not have an id, create it
   ; (scenario validation is handled by a separate method)
   ; Save the active scenario to the scenarios vector
   ; clear the active-scenario
   (let [active-scenario (get-in db [:transient :scenario-active])
         is-new (= "" (:id active-scenario))
         scenario-id (if is-new
                       (.toString (random-uuid))
                       (:id active-scenario))
         scenario-prepared (assoc active-scenario :id scenario-id)
         other-scenarios
         (vec (filter #(not= (:id scenario-prepared) (:id %))
                      (get-in db [:scenarios])))
         scenarios (conj other-scenarios
                         scenario-prepared)]
     (when is-new
       (re-frame.core/dispatch [:scenario-factor-values-initialize-scenario scenario-id]))
     (-> db
         (assoc-in [:scenarios] scenarios)
         (assoc-in [:transient :scenario-edit-defaults] nil)
         (assoc-in [:transient :scenario-active] nil)))))

(re-frame/reg-event-db
 :scenario-active-delete
 (fn [db _]
   (let [scenario-active (get-in db [:transient :scenario-active])
         scenarios (vec (filter #(not (= (:id %) (:id scenario-active))) (get-in db [:scenarios])))]
     (-> db
         (assoc-in [:scenarios] scenarios)
         (assoc-in [:transient :scenario-edit-defaults] nil)
         (assoc-in [:transient :scenario-active] nil)
         (assoc :scenario-factor-values (dissoc (:scenario-factor-values db) (:id scenario-active)))))))

(re-frame/reg-event-db
 :scenario-active-cancel
 (fn [db _]
   (-> db
       (assoc-in [:transient :scenario-edit-defaults] nil)
       (assoc-in [:transient :scenario-active] nil))))

(re-frame/reg-event-db
 :scenario-factor-values-update
 (fn [db [_ scenario-id values]]
   (assoc-in db [:scenario-factor-values scenario-id] values)))

(re-frame/reg-event-db
 :scenario-factor-values-clip
 (fn [db [_ factor-id]]
   (let [factors (get-in db [:factors :all])
         clip #()
         factor-values (get-in db [:scenario-factor-values])]
     (assoc db :scenario-factor-values
            ()))))

