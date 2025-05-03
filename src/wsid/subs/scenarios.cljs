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
   (let [scenario (first
                   (filter
                    #(= scenario-id (:id %))
                    (get db :scenarios)))]
     scenario)
   ))

(re-frame/reg-sub
 :scenario-score
 (fn [db [_ scenario-id]]
   (apply + (vals (get-in db [:scenario-factor-values scenario-id])))))

(re-frame/reg-sub
 :scenario-factor-values
 (fn [db [_ scenario-id]]
   (get-in db [:scenario-factor-values scenario-id])))

(re-frame/reg-sub
 :scenario-factors
 (fn [[_ scenario-id]]
   [(subscribe [:scenario-factor-values scenario-id])
    (subscribe [:factors-sorted])])

 (fn [[values factors]]
   ;; Enrich the global factors with the values for the given scenario
   (for [f factors]
     (conj f {:scenario-value (get values (:id f))}))))