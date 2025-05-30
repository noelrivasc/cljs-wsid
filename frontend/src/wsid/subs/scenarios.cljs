(ns wsid.subs.scenarios
  (:require
   [re-frame.core :as re-frame
    :refer [subscribe]]))


(re-frame/reg-sub
 :scenario-edit-defaults
 (fn [db]
   (get-in db [:transient :scenario-edit-defaults])))

(re-frame/reg-sub
 :scenario-active
 (fn [db]
   (get-in db [:transient :scenario-active])))

(re-frame/reg-sub
 :scenario-active-is-set
 :<- [:scenario-active]
 (fn [s _]
   (not (nil? s))))

(re-frame/reg-sub
 :scenario-active-is-valid
 (fn [db]
   (get-in db [:transient :scenario-active-validation :is-valid])))

(re-frame/reg-sub
 :scenarios
 (fn [db]
   (:scenarios db)))

(re-frame/reg-sub
 :scenario-ids-sorted
 :<- [:scenarios]
 (fn [s _]
   (map :id (sort-by :title s))))

(re-frame/reg-sub
 :scenario
 (fn [db [_ scenario-id]]
   (let [scenario (first
                   (filter
                    #(= scenario-id (:id %))
                    (get db :scenarios)))]
     scenario)
   ))

(re-frame/reg-sub
 :scenario-score
 (fn [[_ scenario-id]]
   [(subscribe [:scenario-factor-values scenario-id])
    (subscribe [:factors])])

 (fn [[values factors]]
   ; sum the weighted factor values for the scenario
   (apply + (map #(* (get values (:id %)) (:weight %)) factors))))

(re-frame/reg-sub
 :scenario-factor-values
 (fn [db [_ scenario-id]]
   (get-in db [:scenario-factor-values scenario-id])))

(re-frame/reg-sub
 :scenario-factors
 (fn [[_ scenario-id]]
   [(subscribe [:scenario-factor-values scenario-id])
    (subscribe [:factors])])

 (fn [[values factors]]
   ;; Enrich the global factors with the values for the given scenario
   (for [f factors]
     (conj f {:scenario-value (get values (:id f))}))))