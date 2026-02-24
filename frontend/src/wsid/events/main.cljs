(ns wsid.events.main
  (:require
   [re-frame.cofx :refer [inject-cofx]]
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.decisions :as decisions]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.factors :as factors]
   [wsid.events.local-storage :as local-storage :refer [ls-keys]]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.scenarios :as scenarios]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.user :as user]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.llm :as llm]
   [wsid.events.util :as util]))

(def evt> util/evt>)

(re-frame/reg-event-db
 :app/wipe-state
 (fn [_]
   db/default-db))

(re-frame/reg-event-fx
 :app/initialize-db
 (fn [_ _]
   {:db db/default-db
    :dispatch-n [[:app/load-decision-from-storage]
                 [:app/load-user-from-storage]]}))

(re-frame/reg-event-fx
 :app/compare-db-localstorage
 [(inject-cofx :local-storage/load (:decision ls-keys))]
 (fn [{db :db local-storage :local-storage/load} _]
   (let [stored-decision local-storage
         db-decision (:decision db)
         is-dirty (not (= db-decision stored-decision))]
     {:db (assoc-in db [:transient :db-is-dirty] is-dirty)})))
