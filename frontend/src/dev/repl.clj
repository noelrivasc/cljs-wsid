(ns dev.repl
  (:require [shadow.cljs.devtools.api :as shadow-api]))

(defn init []
  (shadow-api/nrepl-select :app))
