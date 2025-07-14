#!/usr/bin/env bb

(require
 '[cheshire.core :as json]
 '[clojure.edn :as edn]
 '[utils.core :as u])

(def command (first *command-line-args*))
(def file-path (second *command-line-args*))

(when-not (or file-path command)
  (println "Usage: ./parse-output.clj <text-file-path> <command>")
  (System/exit 1))

(def llm-output (slurp file-path))

(def commands {:get-clean-response
               #(-> % u/get-message-content u/remove-think .trim)

               :get-parsed-edn
               #(-> % u/get-message-content u/remove-think .trim edn/read-string)})

(defn run [command output]
  (let [c ((keyword command) commands)]
    (if c
      (c output)
      (println "Command " command "does not exist."))))

(println (run command llm-output))

