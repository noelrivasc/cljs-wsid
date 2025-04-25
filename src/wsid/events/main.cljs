(ns wsid.events.main
  (:require
   [re-frame.core :as re-frame]
   [wsid.db :as db]
   [day8.re-frame.tracing :refer-macros [fn-traced]]

    #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.factors :as factors]
    #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.scenarios :as scenarios]
   ))

(def evt> re-frame.core/dispatch)

(declare _)

(re-frame/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))