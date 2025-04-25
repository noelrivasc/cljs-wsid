(ns wsid.events.scenarios
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor
   
   [clojure.spec.alpha :as s]))

(re-frame/reg-event-db
 :scenario-create
 (fn [db _]    
   (let 
    [active-scenario-path [:transient :scenario-active]
     factor-ids (map :id (get-in db [:factors :all]))
     active-scenario 
     (if (nil? (get-in db active-scenario-path))
      {
       :id ""
       :title ""
       :description ""
       :factors (zipmap factor-ids (repeat nil)) 
       }
      (get-in db active-scenario-path))]
    (-> db
        (assoc-in [:transient :scenario-edit-defaults] active-scenario)
        (assoc-in [:transient :scenario-active] active-scenario)))))