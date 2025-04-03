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
(s/def ::factor-list (s/coll-of ::factor :kind vector?))

; -- SCENARIOS ----------------------
          
(def default-db
  {
   ; Information that is used for procedures but that is
   ; not the resulting data that is the goal of the program
   :transient {
               :factor-edit-defaults nil ; SPEC :factor
               :factor-active nil ; SPEC :factor
               }
   :factors {
             :all []
             }
   })