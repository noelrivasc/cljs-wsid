(ns wsid.util)

; UTILITIES -------------------
(defn ok [body]
  {:status 200 :body body})