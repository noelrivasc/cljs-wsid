(ns wsid.events.local-storage
  (:require
   [re-frame.core :as re-frame]))

(def ls-keys {:decision "wsid"
              :user "wsid-user"})

(re-frame/reg-cofx
 :local-storage/load
 (fn [coeffects key]
   (assoc coeffects
          :local-storage/load
          (.getItem js/localStorage key))))

(re-frame/reg-fx
 :local-storage/save
 (fn [{:keys [key val]}]
   (.setItem js/localStorage key val)))

(re-frame/reg-event-fx
 :save-to-local-storage
 (fn [{:keys [db]} _]
   {:local-storage/save {:key (:decision ls-keys) :val (:decision db)}}))
