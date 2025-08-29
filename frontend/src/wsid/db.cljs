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
(s/def :factor/description (s/and string? #(<= (count %) 200)))

; {
;  :id ""
;  :title ""
;  :description ""
;  :weight 0
;  }
(s/def ::factor (s/keys :req-un [:factor/title :factor/weight]
                        :opt-un [:factor/id :factor/description]))

; -- SCENARIOS ----------------------
(s/def :scenario/id id)
(s/def :scenario/title (s/and string? #(<= 1 (count %) 50)))
(s/def :scenario/description (s/and string? #(<= (count %) 400)))
(s/def ::scenario (s/keys :req-un [:scenario/title]
                          :opt-un [:scenario/id :scenario/description]))

; -- USER ---------------------------
(s/def :user-credentials/email (s/and string? not-empty))
(s/def :user-credentials/password (s/and string? not-empty))
(s/def ::user-credentials (s/keys :req-un [:user-credentials/email :user-credentials/password]))

(s/def :user/jwt-token (s/nilable string?))
(s/def :user/email (s/nilable string?))
(s/def ::user (s/keys :req-un [:user/email :user/jwt-token]))

; -- TRANSIENT ----------------------
(s/def :transient/db-is-dirty (s/or :empty nil :bool boolean?))
(s/def :transient/factor-edit-defaults nil-or-map)
(s/def :transient/factor-active nil-or-map)
(s/def :transient/factor-active-validation map?)
(s/def :transient/scenario-edit-defaults nil-or-map)
(s/def :transient/scenario-active nil-or-map)
(s/def :transient/scenario-active-validation map?)
(s/def :transient/user-active nil-or-map)
(s/def :transient/llm-request-pending (s/or :empty nil :bool boolean?))

; -- DECISION -----------------------
(s/def :decision/title (s/and string? #(<= 1 (count %) 50)))
(s/def :decision/description (s/and string? #(<= (count %) 5000)))
(s/def :decision/factors (s/coll-of ::factor :kind vector?))
(s/def :decision/scenarios (s/coll-of ::scenario :kind vector?))

; A map of scenarios with nested maps of factor values
(s/def :decision/scenario-factor-values (s/map-of string? ; outer map is keyed by scenario-id
                                                (s/map-of string? ; inner map is keyed by factor-id
                                                          nil-or-number)))

(s/def ::decision (s/keys :req-un [:decision/title
                                   :decision/description
                                   :decision/factors
                                   :decision/scenarios
                                   :decision/scenario-factor-values]))

; -- APP DB -------------------------
(s/def :app-db/transient
  (s/keys :req-un [:transient/db-is-dirty
                   :transient/factor-edit-defaults
                   :transient/factor-active
                   :transient/factor-active-validation
                   :transient/scenario-edit-defaults
                   :transient/scenario-active
                   :transient/scenario-active-validation
                   :transient/user-active
                   :transient/llm-request-pending]))

(s/def :app-db/user (s/nilable ::user))
(s/def :app-db/decision (s/nilable ::decision))

(s/def ::app-db (s/keys :req-un [:app-db/transient
                                 :app-db/user
                                 :app-db/decision]))

(def default-db
  {; Information that is used to control UI but that is
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
               :scenario-active-validation {:is-valid nil}
               :user-active nil
               :llm-request-pending false}

   :user nil
   :decision {:title ""
              :description ""
              :factors []
              :scenarios []
              :scenario-factor-values {}}})
