(ns wsid.events.main
  (:require
   [re-frame.core :as re-frame]
   [re-frame.cofx :refer [inject-cofx]]
   [wsid.db :as db]
   [clojure.edn :as edn]

   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.factors :as factors]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.scenarios :as scenarios]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.local-storage :as local-storage]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.decisions :as decisions]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.events.user :as user]
   [wsid.events.util :as util]

   [clojure.spec.alpha :as s]))

(def evt> util/evt>)

(defn validate-decision [^str stored]
  (let [parsed (edn/read-string stored)
        is-valid (s/valid? ::db/decision parsed)]
    (if is-valid
      parsed
      (do
        (println "Failed to load decision from local store:")
        (s/explain ::db/decision parsed)))))

(re-frame/reg-event-fx
 :app/initialize-db
 [(inject-cofx :local-storage/load)]
 (fn [{db :db local-storage :local-storage/load} _]
   (let [stored-decision (validate-decision local-storage)]
     (if stored-decision
       {:db (-> db
                (assoc :title (:title stored-decision))
                (assoc :description (:description stored-decision))
                (assoc :factors (:factors stored-decision))
                (assoc :scenarios (:scenarios stored-decision))
                (assoc :scenario-factor-values (:scenario-factor-values stored-decision)))}
       {:db db/default-db}))))

#_(re-frame/reg-event-db
    :app/load-decision
    (fn [db [_ decision]]
      (-> db
          (assoc :title (:title decision))
          (assoc :description (:description decision))
          (assoc :factors (:factors decision))
          (assoc :scenarios (:scenarios decision))
          (assoc :scenario-factor-values (:scenario-factor-values decision)))))

(re-frame/reg-event-fx
 :app/compare-db-localstorage
 [(inject-cofx :local-storage/load)]
 (fn [{db :db local-storage :local-storage/load} _]
   (let [stored-decision (validate-decision local-storage)
         db-decision (dissoc db :transient)
         is-dirty (not (= db-decision stored-decision))]
     (println "We did a thorough review and found the database to be "
              (if is-dirty "dirty" "not dirty"))
     {:db (assoc-in db [:transient :db-is-dirty] is-dirty)})))
