(ns wsid.specs.llm
  (:require [clojure.spec.alpha :as s]))

;; LLM API request body specs
(s/def :llm.message/role #{"user" "assistant" "system"})
(s/def :llm.message/content (s/and string? (complement empty?)))
(s/def :llm.message/message (s/keys :req-un [:llm.message/role :llm.message/content]))
(s/def :llm.api/messages (s/coll-of :llm.message/message :min-count 1))
(s/def :llm.api/model (s/and string? (complement empty?)))
(s/def :llm.api/request-body (s/keys :req-un [:llm.api/model :llm.api/messages]))

;; LLM model configuration specs
(s/def :llm.process/process-fn fn?)

;; LLM provider configuration specs  
(s/def :llm.provider/endpoint-url (s/and string? (complement empty?)))
(s/def :llm.provider/extract-response-fn fn?)
(s/def :llm.provider/config
  (s/keys :req-un [:llm.provider/endpoint-url :llm.provider/extract-response-fn]))

;; LLM user input specs
(s/def :llm.input/prompt (s/and string? (complement empty?)))
(s/def :llm.input/prompt-template (s/and string? (complement empty?)))
(s/def :llm.input/prompt-parameters (s/nilable (s/map-of keyword? string?)))
(s/def :llm.input/model-name (s/and string? (complement empty?)))
(s/def :llm.input/provider-name (s/and string? (complement empty?)))
(s/def :llm.input/provider-config :llm.provider/config)

;; Either prompt or prompt-template must be present (mutually exclusive)
(s/def :llm.params/prompt-or-template
  #(let [has-prompt (and (contains? % :prompt) (some? (:prompt %)))
         has-template (and (contains? % :prompt-template) (some? (:prompt-template %)))]
     (and (or has-prompt has-template)
          (not (and has-prompt has-template))
          (if has-prompt (s/valid? :llm.input/prompt (:prompt %)) true)
          (if has-template (s/valid? :llm.input/prompt-template (:prompt-template %)) true))))

;; Complete request params spec
(s/def :llm.params/request-params
  (s/and :llm.params/prompt-or-template
         (s/keys :req-un [:llm.input/model-name
                          :llm.input/provider-name
                          :llm.input/provider-config
                          :llm.process/process-fn]
                 :opt-un [:llm.input/prompt-parameters])))