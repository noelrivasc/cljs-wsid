(ns wsid.events.decisions
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :decision-metadata-update
 (fn [db [_ values]]
   (-> db
       (assoc :title (:title values))
       (assoc :description (:description values)))))
