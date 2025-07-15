(ns wsid.handlers.llm)

(defn llm-handler [request]
  (let [prompt (get-in request [:parsed-body :prompt])]
    nil))
