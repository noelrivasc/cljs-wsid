(ns wsid.subs.main
  (:require
   [re-frame.core :as re-frame]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.subs.factors :as factors] 
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.subs.scenarios :as scenarios]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.subs.decisions :as decisions]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.subs.user :as user]))

(def <sub (comp deref re-frame.core/subscribe))

(re-frame/reg-sub
 :app/db
 (fn [db]
   db))

(re-frame/reg-sub
 :db-is-dirty
 (fn [db]
   (get-in db [:transient :db-is-dirty])))

(re-frame/reg-sub
 :llm-request-pending
 (fn [db]
   (get-in db [:transient :llm-request-pending])))
