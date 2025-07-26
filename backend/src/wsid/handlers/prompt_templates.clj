(ns wsid.handlers.prompt-templates 
  (:require
   [clojure.java.io :as io]
   [io.pedestal.http :as http]
   [wsid.util.prompts :refer [get-prompt-template]]))

(defn- extract-template-variables
  "Extracts variable names from a template string that are delimited by %%var-name%%"
  [template-content]
  (->> template-content
       (re-seq #"%%([^%]+)%%")
       (map second)
       (into [])))

(defn- list-template-files
  "Lists all files in the wsid/prompt-templates/ resource directory"
  []
  (let [resource-path "wsid/prompt-templates/"
        resource-url (io/resource resource-path)]
    (when resource-url
      (let [template-dir (io/file (.getPath resource-url))]
        (when (.exists template-dir)
          (->> (.listFiles template-dir)
               (filter #(.isFile %))
               (map #(.getName %))
               (into [])))))))

(defn- build-templates-map
  "Builds a map of template names to their variables"
  []
  (let [template-files (list-template-files)]
    (reduce (fn [acc template-name]
              (try
                (let [template-content (get-prompt-template (keyword template-name))
                      variables (extract-template-variables template-content)]
                  (assoc acc template-name variables))
                (catch Exception e
                  (println (str "Error processing template " template-name ": " (.getMessage e)))
                  acc)))
            {}
            template-files)))

(def index
  "List the existing prompt templates with the options they take."
  {:name :llm-list-prompt-templates
   :enter (fn [context]
            (let [templates-map (build-templates-map)]
              (http/respond-with context 200 templates-map)))})