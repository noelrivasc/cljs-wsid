(ns wsid.events
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 :factor-create
 (fn [db _]    
   (let 
    [active-factor-path [:transient :factor-active]
     active-factor 
     (if (nil? (get-in db active-factor-path))
      {
       :id ""
       :title ""
       :description ""
       :min -10
       :max 10
       :weight 0
       }
      (get-in db [:transient :factor-active]))]
    (-> db
        (assoc-in [:transient :factor-edit-defaults] active-factor)
        (assoc-in [:transient :factor-active] active-factor)))))

(re-frame/reg-event-db
 :factor-active-update
 (fn [db [_ property value]]
   (assoc-in db [:transient :factor-active (keyword property)] value)))