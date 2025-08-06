(ns wsid.events.decisions
  (:require
   [clojure.edn :as edn]
   [wsid.db :as db]
   [clojure.spec.alpha :as s]
   [re-frame.core :as re-frame]))

(defn validate-decision [^str stored]
  (let [parsed (edn/read-string stored)
        is-valid (s/valid? ::db/decision parsed)]
    (if is-valid
      parsed
      (do
        (println "Failed to load decision from local store:")
        (s/explain ::db/decision parsed)))))

(re-frame/reg-event-db
 :decision-metadata-update
 (fn [db [_ values]]
   (-> db
       (assoc-in [:decision :title] (:title values))
       (assoc-in [:decision :description] (:description values)))))
