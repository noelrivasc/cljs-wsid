(ns wsid.subs.scenarios
  (:require
   [re-frame.core :as re-frame]))


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