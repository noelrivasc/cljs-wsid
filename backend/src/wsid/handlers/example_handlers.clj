(ns wsid.handlers.example-handlers
  (:require
   [wsid.util :refer [response]]))

(defn error-example-handler [_]
  (response 500 {:message "This is an error message."
                 :extra "The body is not limited to message."}))
