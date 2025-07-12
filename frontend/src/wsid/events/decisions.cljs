(ns wsid.events.decisions
  (:require
    [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :decision-title-edit
 (fn [db [_ decision-title]]
   (assoc db :title decision-title)))

(re-frame/reg-event-db
 :decision-description-edit
 (fn [db [_ decision-description]]
   (assoc db :description decision-description)))

