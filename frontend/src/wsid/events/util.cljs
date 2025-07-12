(ns wsid.events.util
  (:require
   [re-frame.core :as re-frame]))
 
(def evt> 
  (fn [e]
    ; Dispatch the normal event first
    (re-frame/dispatch e)

    ; Then, if the localstorage compare metadata is set,
    ; trigger the event once the effects of the first one
    ; have settled.
    (let [m (meta e)
          ls-compare (:ls-compare m)]
      (when ls-compare
        (re-frame/dispatch [:app/compare-db-localstorage])))))
  

