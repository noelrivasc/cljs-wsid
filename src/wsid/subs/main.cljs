(ns wsid.subs.main
  (:require
   [re-frame.core :as re-frame]
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.subs.factors :as factors] 
   #_{:clj-kondo/ignore [:unused-namespace]}
   [wsid.subs.scenarios :as scenarios]
   ))

(def <sub (comp deref re-frame.core/subscribe))