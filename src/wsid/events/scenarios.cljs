(ns wsid.events.scenarios
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor

   [clojure.spec.alpha :as s]))

(re-frame/reg-event-db
 :scenario-create
 (fn [db _]
   (let
    [active-scenario-path [:transient :scenario-active]
     factor-ids (map :id (get-in db [:factors :all]))
     active-scenario
     (if (nil? (get-in db active-scenario-path))
       {:id ""
        :title ""
        :description ""
        :factors (zipmap factor-ids (repeat 0))}
       (get-in db active-scenario-path))]
     (-> db
         (assoc-in [:transient :scenario-edit-defaults] active-scenario)
         (assoc-in [:transient :scenario-active] active-scenario)))))

(re-frame/reg-event-db
 :scenario-active-update
 (fn [db [_ property value]]
   (let [updated-scenario (assoc (get-in db [:transient :scenario-active]) (keyword property) value)
         scenario-valid? (s/valid? ::db/scenario updated-scenario)]
     (-> db
         (assoc-in [:transient :scenario-active] updated-scenario)
         (assoc-in [:transient :scenario-active-validation :is-valid] scenario-valid?)))))

(re-frame/reg-event-db
 :scenario-active-save
 (fn [db _]
   ; If the scenario does not have an id, create it
   ; (scenario validation is handled by a separate method)
   ; Save the active scenario to the scenarios vector
   ; clear the active-scenario
   (let [active-scenario (get-in db [:transient :scenario-active])
         scenario-prepared (assoc active-scenario :id (if
                                                       (= "" (:id active-scenario))
                                                        (.toString (random-uuid))
                                                        (:id active-scenario)))
         other-scenarios 
                    (vec (filter #(not= (:id scenario-prepared) (:id %))
                                 (get-in db [:scenarios])))
         scenarios (conj other-scenarios
                    scenario-prepared)]
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
         (assoc-in [:transient :scenario-active] nil)))))

(re-frame/reg-event-db
 :scenario-active-cancel
 (fn [db _]
   (-> db
       (assoc-in [:transient :scenario-edit-defaults] nil)
       (assoc-in [:transient :scenario-active] nil))))

(re-frame/reg-event-db
 :scenario-factor-update
 (fn [db [_ scenario-id factor-id value]]
   (let [scenarios (:scenarios db)]
     (assoc db :scenarios
            (map (fn [scenario]
                   (if (= scenario-id (:id scenario))
                     (assoc-in scenario [:factors factor-id] value)
                     scenario))
                 scenarios)))))