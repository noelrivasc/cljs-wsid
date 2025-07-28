(ns wsid.events.user
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-event-db
 :jwt-token-update
 (fn [db [_ token]]
   (assoc-in db [:transient :user :jwt-token] token)))