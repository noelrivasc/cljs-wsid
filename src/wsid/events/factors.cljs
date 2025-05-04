(ns wsid.events.factors
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   ; [day8.re-frame.tracing :refer-macros [fn-traced]] ; TODO reimplement the fn-traced and figure out what it does and how to fix the macro errors in the editor
   
   [clojure.spec.alpha :as s]))

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
   (let [updated-factor (assoc (get-in db [:transient :factor-active]) (keyword property) value)
         factor-valid? (s/valid? ::db/factor updated-factor)]
     (-> db
         (assoc-in [:transient :factor-active] updated-factor)
         (assoc-in [:transient :factor-active-validation :is-valid] factor-valid?)))))


(re-frame/reg-event-db
 :factor-active-save
 (fn [db _]
   ; If the factor does not have an id, create it
   ; (factor validation is handled by a separate method)
   ; Save the active factor to the factors vector
   ; clear the active-factor
   (let [active-factor (get-in db [:transient :factor-active])
         is-new (= "" (:id active-factor))
         factor-id (if is-new
                       (.toString (random-uuid))
                       (:id active-factor))
         factor-prepared (assoc active-factor :id factor-id)
         factors (conj
                  (vec (filter #(not (= (:id factor-prepared) (:id %)))
                               (get-in db [:factors :all])))
                  factor-prepared)
         new-db (-> db
                    (assoc-in [:factors :all] factors)
                    (assoc-in [:transient :factor-edit-defaults] nil)
                    (assoc-in [:transient :factor-active] nil))]
     (if is-new
       (re-frame.core/dispatch [:scenario-factor-values-initialize-factor (:id factor-prepared)])
       (re-frame.core/dispatch [:scenario-factor-values-clip-factor (:id factor-prepared)]))
     
     new-db)
   ))

(re-frame/reg-event-db
 :factor-active-delete
 (fn [db _]
   (let [factor-active (get-in db [:transient :factor-active])
         factors (vec (filter #(not (= (:id %) (:id factor-active))) (get-in db [:factors :all])))
         new-db (-> db
                    (assoc-in [:factors :all] factors)
                    (assoc-in [:transient :factor-edit-defaults] nil)
                    (assoc-in [:transient :factor-active] nil))]
     (re-frame.core/dispatch [:scenario-factor-values-prune (:id factor-active)])
     new-db)))

(re-frame/reg-event-db
 :factor-active-cancel
 (fn [db _]
   (-> db
       (assoc-in [:transient :factor-edit-defaults] nil)
       (assoc-in [:transient :factor-active] nil))))