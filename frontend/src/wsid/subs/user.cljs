(ns wsid.subs.user
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :user-active
 (fn [db]
   (get-in db [:transient :user-active])))

(re-frame/reg-sub
 :user-active-is-set
 (fn [db]
   (not (nil? (get-in db [:transient :user-active])))))

(re-frame/reg-sub
 :user-active-is-valid
 (fn [db]
   (get-in db [:transient :user-active-validation :is-valid])))