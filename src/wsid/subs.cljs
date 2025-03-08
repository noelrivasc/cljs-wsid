(ns wsid.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::factors-sorted
 (fn [db]
   (get-in db [:factors :all])))

(re-frame/reg-sub
 ::current-factor
 (fn [db]
   (get-in db [:factors :current])))