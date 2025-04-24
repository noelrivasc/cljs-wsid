(ns wsid.db
  (:require
   [clojure.spec.alpha :as s]))

; -- FACTORS ------------------------
(s/def :factor/id (s/or :empty (s/and string? #(= 0 (count %)))
                        :uuid (s/and string? #(= 36 (count %)))))
(s/def :factor/min (s/and int? #(<= -10 % 0)))
(s/def :factor/max (s/and int? #(<= 0 % 10)))
(s/def :factor/title (s/and string? #(<= 1 (count %) 50)))
(s/def :factor/description (s/and string? #(<= (count %) 120)))

; {
;  :id ""
;  :title ""
;  :description ""
;  :min -10
;  :max 10
;  :weight 0
;  }
(s/def ::factor (s/keys :req-un [:factor/title :factor/min :factor/max]
                        :opt-un [:factor/id :factor/description]))
(s/def :factors/all (s/coll-of ::factor :kind vector?))

; -- SCENARIOS ----------------------

; -- TRANSIENT ----------------------
(s/def :transient/factor-edit-defaults (s/or :empty nil?
                                             :map map?))
(s/def :transient/factor-active (s/or :empty nil?
                                      :map map?))
(s/def :transient/factor-active-validation map?)

; -- DEFAULT DB ---------------------
(s/def :default-db/transient
  (s/keys :req-un [:transient/factor-edit-defaults
                   :transient/factor-active
                   :transient/factor-active-validation]))
(s/def :default-db/factors (s/keys :req-un [:factors/all]))
(s/def ::default-db (s/keys :req-un [:default-db/transient :default-db/factors]))

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
               :factor-active-validation {:is-valid nil}}
   :factors {:all []}})