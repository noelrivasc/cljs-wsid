(ns utils.core
  (:require
   [cheshire.core :as json]
   [clojure.string :as string]))

(defn get-message-content
  "Get the message content from the JSON response from the DeepInfra API"
  [r] (-> r (json/parse-string true) :choices first :message :content))

(defn remove-think [r] (string/replace r #"(?s)<think>.*</think>" ""))

