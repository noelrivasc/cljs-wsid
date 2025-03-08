(ns wsid.events
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

#_(re-frame/reg-event-db
 ::factors-create
 (fn-traced [db _] 
   (let 
    [current-factor 
     (if (nil? (get-in db [:factors :current]))
      {
       :id ""
       :title ""
       :description ""
       :min -10
       :max 10
       :weight 0
       }
      (get-in db [:factors :current]))]
     (assoc-in db [:factors :current] @current-factor)
     )))