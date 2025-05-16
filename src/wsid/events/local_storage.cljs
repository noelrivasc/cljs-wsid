(ns wsid.events.local-storage
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-cofx
 :local-storage/load
 (fn [coeffects]
   (assoc coeffects
          :local-storage/load
          (.getItem js/localStorage "wsid"))))

(re-frame/reg-fx
 :local-storage/save
 (fn [{:keys [key val]}]
   (.setItem js/localStorage key val)))

(re-frame/reg-event-fx
 :save-to-local-storage
 (fn [{:keys [db]} _]
   {:local-storage/save {:key "wsid" :val (dissoc db :transient)}}))
