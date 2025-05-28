(ns wsid.subs.factors
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :factors
 (fn [db]
   (:factors db)))

(re-frame/reg-sub
 :factors-sorted
 :<- [:factors]
 (fn [f _]
   (sort-by :title f)))

(re-frame/reg-sub
 :factor-edit-defaults
 (fn [db]
   (get-in db [:transient :factor-edit-defaults])))

(re-frame/reg-sub
 :factor-active
 (fn [db]
   (get-in db [:transient :factor-active])))

(re-frame/reg-sub
 :factor-active-is-set
 (fn [db]
   (nil? (get-in db [:transient :factor-active]))))

; TODO use in scenarios or remove.
; This will end up in scenarios. Keeping as reference.
#_(re-frame/reg-sub
 :factor-active-range-interpretation
 (fn [_ _]
   (re-frame/subscribe [:factor-active]))
 (fn [factor-active]
   (let [min (get factor-active :min)
         max (get factor-active :max)
         effect-direction (case [(< min 0) (> max 0)]
                            [true true] :both-ways
                            [true false] :negative-only
                            [false true] :positive-only
                            [false false] :zero)
         min-weight (cond
                      (#{0 nil} min) "are nonexistent"
                      (#{-1 -2} min) "are tiny, really"
                      (#{-3 -4 -5} min) "can be small but noticeable"
                      (#{-6 -7} min) "can be considerable at worst"
                      (#{-8 -9} min) "can weigh heavily on you"
                      (= min -10) "could be terrible"
                      :else "unknown min-weight")
         max-weight (cond
                      (#{0 nil} max) "are nonexistent"
                      (#{1 2} max) "are tiny, really"
                      (#{3 4 5} max) "can be small but nice"
                      (#{6 7} max) "are not too bad"
                      (#{8 9} max) "could be great"
                      (= max 10) "could be just fabulous"
                      :else "unknown max-weight")
         interpretation (case effect-direction
                          :both-ways (str "This factor can have either negative or positive effects. Negative effects "
                                          min-weight
                                          " and positive effects "
                                          max-weight
                                          ".")
                          :negative-only (str "This factor can only be negative and the effects "
                                              min-weight
                                              ".")
                          :positive-only (str "This factor can only be positive and the effects "
                                              max-weight
                                              ".")
                          :zero "")]
     interpretation)
   ))

(re-frame/reg-sub
 :factor-active-is-valid
 (fn [db]
   (get-in db [:transient :factor-active-validation :is-valid])))
