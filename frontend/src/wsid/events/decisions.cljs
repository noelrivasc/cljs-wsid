(ns wsid.events.decisions
  (:require
   [clojure.edn :as edn]
   [wsid.db :as db]
   [wsid.config :as config]
   [clojure.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [re-frame.cofx :refer [inject-cofx]]
   [wsid.events.local-storage :as local-storage :refer [ls-keys]]))

(defn validate-decision [^str stored]
  (let [parsed (edn/read-string stored)
        is-valid (s/valid? ::db/decision parsed)]
    (if is-valid
      parsed
      (when config/debug?
        (when (empty? parsed) (println "The decision in localstorage is empty."))
        (println "The decision in localstorage does not conform to the spec ::db/decision")
        (s/explain ::db/decision parsed)))))

(defn load-decision
  "Validates and loads a decision into the provided db.
   
   Arguments:
   - db: ::db/app-db
   - decision: ::db/decision"
  [db decision]
  (let [validated-decision (validate-decision decision)]
    (if validated-decision
      (-> db
          (assoc-in [:decision :title] (:title validated-decision))
          (assoc-in [:decision :description] (:description validated-decision))
          (assoc-in [:decision :factors] (:factors validated-decision))
          (assoc-in [:decision :scenarios] (:scenarios validated-decision))
          (assoc-in [:decision :scenario-factor-values] (:scenario-factor-values validated-decision)))
      (do 
        (when config/debug?
          (println "Attempted to load an invalid decision.")
          (println (s/explain ::db/decision decision)))
        db))))

(re-frame/reg-event-db
 :app/load-decision
 (fn [db [_ decision]]
   (load-decision db decision)))

(re-frame/reg-event-fx
 :app/load-decision-from-storage
 [(inject-cofx :local-storage/load (:decision ls-keys))]
 (fn [{db :db local-storage :local-storage/load} _]
   {:db (load-decision db local-storage)}))

(re-frame/reg-event-db
 :decision-metadata-update
 (fn [db [_ values]]
   (-> db
       (assoc-in [:decision :title] (:title values))
       (assoc-in [:decision :description] (:description values)))))
