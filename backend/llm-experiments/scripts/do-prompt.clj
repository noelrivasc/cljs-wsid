#!/usr/bin/env bb

; This script helps with:
; - preparing prompts
; - calling LLM models
; - saving the results to file for further analysis

(require '[babashka.http-client :as http]
         '[cheshire.core :as json]
         '[babashka.cli :as cli]
         '[clojure.string :as string]
         '[clojure.java.io :as io])
(import
 '[java.time ZoneId ZonedDateTime]
 '[java.time.format DateTimeFormatter])

; See https://book.babashka.org/#_parsing_command_line_arguments
(def opts-schema {:provider {:default "deepinfra"}
                  :prompt-options {:default {}}
                  :output-base-dir {:default "output"}})
(def opts-required [:prompt-path :provider :model])
(def opts (cli/parse-opts *command-line-args* {:spec opts-schema
                                               :require opts-required}))

(prn opts)

(def prompt
  (try
    (slurp (:prompt-path opts))
    (catch Exception _
      (println "Exception: unable to read prompt from " (:prompt-path opts))
      (System/exit 1))))

(def providers
  {:deepinfra {:endpoint-url  "https://api.deepinfra.com/v1/openai/chat/completions"}})

(def provider
  (let [p ((keyword (:provider opts)) providers)]
    (if p p (do
              (println "Error: unrecognized provider: " (:provider opts))
              (System/exit 1)))))

(def token
  (try
    (.trim (slurp (str ".token-" (:provider opts))))
    (catch Exception _
      (println "Exception: unable to read prompt from " (:prompt-path opts))
      (System/exit 1))))

(def response
  (http/post (:endpoint-url provider)
             {:headers {"Content-Type" "application/json"
                        "Authorization" (str "Bearer " token)}
              :body (json/generate-string
                     {:model (:model opts)
                      :messages [{:role "user"
                                  :content prompt}]})}))

(defn formatted-time-in-timezone [timezone-str format-pattern]
  (-> (ZoneId/of timezone-str)
      (ZonedDateTime/now)
      (.format (DateTimeFormatter/ofPattern format-pattern))))

(def prompt-filename (last (string/split (:prompt-path opts) #"/")))
(def timestamp (formatted-time-in-timezone "America/Mexico_City" "yyyy-mm-dd"))
(def output-dir
  (-> (:output-base-dir opts)
      (str "/" timestamp "--" prompt-filename "--")
      (str (string/replace (:model opts) #"/" "-"))))

(prn "Output directory: " output-dir)

(io/make-parents (str output-dir "/prompt")) ; create the output directory
(spit (str output-dir "/prompt") prompt)
(spit (str output-dir "/response") (:body response))

