(ns wsid.util.prompts 
  (:require
   [clojure.java.io :as io]))

(defn get-prompt-template
  "Reads the contents of a template that matches the given keyword, if the
   template exists. Throws an error otherwise."
  [template-id]
  (slurp (io/resource (str "wsid/prompt-templates/" (name template-id)))))