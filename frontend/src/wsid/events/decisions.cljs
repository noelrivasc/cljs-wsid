(ns wsid.events.decisions
  (:require
   [clojure.edn :as edn]
   [wsid.db :as db]
   [clojure.spec.alpha :as s]
   [re-frame.core :as re-frame]
   [re-frame.cofx :refer [inject-cofx]]
   [wsid.events.local-storage :as local-storage :refer [ls-keys]]))

(defn validate-decision [^str stored]
  (let [parsed (edn/read-string stored)
        is-valid (s/valid? ::db/decision parsed)]
    (if is-valid
      parsed
      (do
        (println "Failed to load decision from local store:")
        (s/explain ::db/decision parsed)))))

(defn parse-query-string []
  (let [search (.-search js/window.location)]
    (when (seq search)
      (let [params (js/URLSearchParams. search)]
        (.get params "decision")))))

(re-frame/reg-fx
 :decision/parse-from-query-string
 (fn [_]
   (when-let [edn-str (parse-query-string)]
     (println edn-str)
     (try
       (let [parsed (edn/read-string edn-str)
             is-valid (s/valid? ::db/decision parsed)]
         (if is-valid
           (re-frame/dispatch [:decision/load-from-query-string-success parsed])
           (do
             (println "Failed to validate decision from query string:")
             (s/explain ::db/decision parsed))))
       (catch js/Error e
         (println "Failed to parse EDN from query string:" (.-message e)))))))

(re-frame/reg-event-fx
 :app/load-decision-from-storage
 [(inject-cofx :local-storage/load (:decision ls-keys))]
 (fn [{db :db local-storage :local-storage/load} _]
   (let [stored-decision (validate-decision local-storage)]
     (if stored-decision
       {:db (-> db
                (assoc-in [:decision :title] (:title stored-decision))
                (assoc-in [:decision :description] (:description stored-decision))
                (assoc-in [:decision :factors] (:factors stored-decision))
                (assoc-in [:decision :scenarios] (:scenarios stored-decision))
                (assoc-in [:decision :scenario-factor-values] (:scenario-factor-values stored-decision)))}
       {:db db}))))

(re-frame/reg-event-fx
 :decision/load-from-query-string-success
 (fn [{db :db} [_ decision]]
   {:db (-> db
            (assoc-in [:decision :title] (:title decision))
            (assoc-in [:decision :description] (:description decision))
            (assoc-in [:decision :factors] (:factors decision))
            (assoc-in [:decision :scenarios] (:scenarios decision))
            (assoc-in [:decision :scenario-factor-values] (:scenario-factor-values decision)))}))

(re-frame/reg-event-fx
 :decision/load-from-query-string
 (fn [_ _]
   {:decision/parse-from-query-string nil}))

(re-frame/reg-event-db
 :decision-metadata-update
 (fn [db [_ values]]
   (-> db
       (assoc-in [:decision :title] (:title values))
       (assoc-in [:decision :description] (:description values)))))
