(ns wsid.db
  (:require
   [clojure.spec.alpha :as s]))

; -- UTILITIES ----------------------
(def nil-or-map (s/or :empty nil? :map map?))
(def nil-or-number (s/or :empty nil? :number number?))
(def id (s/or :empty (s/and string? #(= 0 (count %)))
              :uuid (s/and string? #(= 36 (count %)))))

; -- FACTORS ------------------------
(s/def :factor/id id)
(s/def :factor/weight (s/and int? #(<= 0 % 10)))
(s/def :factor/title (s/and string? #(<= 1 (count %) 50)))
(s/def :factor/description (s/and string? #(<= (count %) 120)))

; {
;  :id ""
;  :title ""
;  :description ""
;  :weight 0
;  }
(s/def ::factor (s/keys :req-un [:factor/title :factor/weight]
                        :opt-un [:factor/id :factor/description]))
(s/def :factors/all (s/coll-of ::factor :kind vector?))

; -- SCENARIOS ----------------------
(s/def :scenario/id id)
(s/def :scenario/title (s/and string? #(<= 1 (count %) 50)))
(s/def :scenario/description (s/and string? #(<= (count %) 120)))
(s/def ::scenario (s/keys :req-un [:scenario/title]
                          :opt-un [:scenario/id :scenario/description]))

; -- TRANSIENT ----------------------
(s/def :transient/factor-edit-defaults nil-or-map)
(s/def :transient/factor-active nil-or-map)
(s/def :transient/factor-active-validation map?)
(s/def :transient/scenario-edit-defaults nil-or-map)
(s/def :transient/scenario-active nil-or-map)
(s/def :transient/scenario-active-validation map?)

; -- APP DB -------------------------
(s/def :app-db/transient
  (s/keys :req-un [:transient/factor-edit-defaults
                   :transient/factor-active
                   :transient/factor-active-validation
                   :transient/scenario-edit-defaults
                   :transient/scenario-active
                   :transient/scenario-active-validation]))
(s/def :app-db/factors (s/keys :req-un [:factors/all]))
(s/def :app-db/scenarios (s/coll-of ::scenario :kind vector?))

; A map of scenarios with nested maps of factor values
(s/def :app-db/scenario-factor-values (s/map-of string? (s/map-of string? nil-or-number)))
(s/def ::app-db (s/keys :req-un [:app-db/transient
                                 :app-db/factors
                                 :app-db/scenarios
                                 :app-db/scenario-factor-values]))

(def default-db
  {; Information that is used for procedures but that is
   ; not the resulting data that is the goal of the program
   :transient {; Just default values of factor form
               ; These do not change as form is edited
               :factor-edit-defaults nil

               ; The model that changes as input is made
               ; Goal of duplication: avoid re-renders or
               ; having local state in the component
               :factor-active nil
               :factor-active-validation {:is-valid nil}

               :scenario-edit-defaults nil
               :scenario-active nil
               :scenario-active-validation {:is-valid nil}}
   :factors {:all []}
   :scenarios []
   :scenario-factor-values {}})
