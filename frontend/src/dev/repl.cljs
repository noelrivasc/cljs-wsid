(ns dev.repl
  (:require
    [wsid.subs.main :as subs]
    [wsid.events.main :as events]))

(defn evt> [e] (events/evt> e))
(defn <sub [e] (subs/<sub e))
