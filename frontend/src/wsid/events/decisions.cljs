(ns wsid.events.decisions
  (:require
   [wsid.db :as db]
   [wsid.config :as config]
   [wsid.domain.decisions :as decisions]
   [clojure.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [re-frame.cofx :refer [inject-cofx]]
   [wsid.events.local-storage :as local-storage :refer [ls-keys]]))

(defn load-decision
  "Validates and loads a decision into the provided db.
   
   Arguments:
   - db: ::db/app-db
   - decision: ::db/decision
   
   Output:
   - a new ::app/app-db with the decision loaded"
  [db decision]
  {:pre [(s/valid? ::db/decision decision)]}
  (-> db
      (assoc-in [:decision :title] (:title decision))
      (assoc-in [:decision :description] (:description decision))
      (assoc-in [:decision :factors] (:factors decision))
      (assoc-in [:decision :scenarios] (:scenarios decision))
      (assoc-in [:decision :scenario-factor-values] (:scenario-factor-values decision))))

(defn load-decision-edn
  "Parses the decision-edn string and loads the decision
   components to the db if a valid decision is found."
  [db decision-edn]
  (let [validated-decision (decisions/validate-decision-edn decision-edn)]
    (if validated-decision
      (load-decision db validated-decision)
      (do
        (when config/debug?
          (println "Attempted to load an invalid decision edn.")
          (println decision-edn))
        db))))

(re-frame/reg-event-db
 :app/load-decision
 (fn [db [_ decision]]
   (load-decision db decision)))

(re-frame/reg-event-fx
 :app/load-decision-from-storage
 [(inject-cofx :local-storage/load (:decision ls-keys))]
 (fn [{db :db local-storage :local-storage/load} _]
   {:db (load-decision-edn db local-storage)}))

(re-frame/reg-event-db
 :decision-metadata-update
 (fn [db [_ values]]
   (-> db
       (assoc-in [:decision :title] (:title values))
       (assoc-in [:decision :description] (:description values)))))
