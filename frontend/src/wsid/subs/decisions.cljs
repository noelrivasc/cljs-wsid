(ns wsid.subs.decisions
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :decision-title
 (fn [db]
   (:title db)))

(re-frame/reg-sub
 :decision-description
 (fn [db]
   (:description db)))

