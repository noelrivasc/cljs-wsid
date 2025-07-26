(ns wsid.util.response
  "Provides utilities to parse http input and produce output.
   Includes functions for content negotiation.")

(defn ok [body]
  {:status 200 :body body})

(defn response [code body]
  {:status code :body body})
