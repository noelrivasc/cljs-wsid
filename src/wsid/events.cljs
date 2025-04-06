(ns wsid.events
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor
   ))

(def evt> re-frame.core/dispatch)

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
 :factor-edit
 (fn [db [_ factor]]
   (-> db
       (assoc-in [:transient :factor-active] factor)
       (assoc-in [:transient :factor-edit-defaults] factor))))

(re-frame/reg-event-db
 :factor-active-update
 (fn [db [_ property value]]
   (assoc-in db [:transient :factor-active (keyword property)] value)))

(re-frame/reg-event-db
 :factor-active-save
 (fn [db _]
   ; If the factor does not have an id, create it
   ; (factor validation is handled by a separate method)
   ; Save the active factor to the factors vector
   ; clear the active-factor
   (let [active-factor (get-in db [:transient :factor-active])
         factor-prepared (assoc active-factor :id (if
                                          (= "" (:id active-factor))
                                           (.toString (random-uuid))
                                           (:id active-factor)))
         factors (conj
                   (vec (filter #(not (= (:id factor-prepared) (:id %)))
                            (get-in db [:factors :all])))
                  factor-prepared)]
     (-> db
         (assoc-in [:factors :all] factors)
         (assoc-in [:transient :factor-edit-defaults] nil)
         (assoc-in [:transient :factor-active] nil)))
   ))

(re-frame/reg-event-db
 :factor-active-delete
 (fn [db _]
   (let [factor-active (get-in db [:transient :factor-active])
         factors (vec (filter #(not (= (:id %) (:id factor-active))) (get-in db [:factors :all])))]
     (-> db
         (assoc-in [:factors :all] factors)
         (assoc-in [:transient :factor-edit-defaults] nil)
         (assoc-in [:transient :factor-active] nil)))))

(re-frame/reg-event-db
 :factor-active-cancel
 (fn [db _]
   (-> db
       (assoc-in [:transient :factor-edit-defaults] nil)
       (assoc-in [:transient :factor-active] nil))))