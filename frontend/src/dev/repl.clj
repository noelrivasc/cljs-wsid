(ns dev.repl)

(defn init []
  (shadow.cljs.devtools.api/nrepl-select :app))
