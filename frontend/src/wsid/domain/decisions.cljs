(ns wsid.domain.decisions
  (:require
   [wsid.db :as db]
   [wsid.config :as config]
   [clojure.spec.alpha :as s]
   [clojure.edn :as edn]))

(defn validate-decision-edn 
  "Validates that the string is valid EDN and that its
   contents conform to the ::db/decision spec."
  [^str stored]
  (let [parsed (edn/read-string stored)
        is-valid (s/valid? ::db/decision parsed)]
    (if is-valid
      parsed
      (when config/debug?
        (when (empty? parsed) (println "The decision is empty."))
        (println "The decision does not conform to the spec ::db/decision")
        (s/explain ::db/decision parsed)))))