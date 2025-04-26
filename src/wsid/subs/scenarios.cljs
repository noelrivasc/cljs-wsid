(ns wsid.subs.scenarios
  (:require
   [re-frame.core :as re-frame
    :refer [subscribe]]))


(re-frame/reg-sub
 :scenario-edit-defaults
 (fn [db]
   (get-in db [:transient :scenario-edit-defaults])))

(re-frame/reg-sub
 :scenario-active-is-set
 (fn [db]
   (nil? (get-in db [:transient :scenario-active]))))

(re-frame/reg-sub
 :scenario-active-is-valid
 (fn [db]
   (get-in db [:transient :scenario-active-validation :is-valid])))

(re-frame/reg-sub
 :scenario-ids
 (fn [db]
   (map :id (get-in db [:scenarios]))))

(re-frame/reg-sub
 :scenario
 (fn [db [_ scenario-id]]
   (first (filter
           #(= scenario-id (:id %))
           (get db :scenarios)))))

(re-frame/reg-sub
 :scenario-score
 (fn [_ [_ scenario-id]] (subscribe [:scenario scenario-id]))
 (fn [scenario]
   (apply + (vals (:factors scenario)))))

(re-frame/reg-sub
 :scenario-factors
 (fn [[_ scenario-id]]
   [(subscribe [:scenario scenario-id])
    (subscribe [:factors-sorted])])

 (fn [[scenario factors]]
   (map #(conj % {:scenario-value (get-in scenario [:factors (:id %)])}) factors)))