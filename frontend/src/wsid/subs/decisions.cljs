(ns wsid.subs.decisions
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :decision-title
 (fn [db]
   (get-in db [:decision :title])))

(re-frame/reg-sub
 :decision-description
 (fn [db]
   (get-in db [:decision :description])))

