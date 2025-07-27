(ns wsid.interceptors.coerce-body 
  (:require
   [cheshire.core :as json]))

(def coerce-body-interceptor
  {:name ::coerce-body
   :leave
   (fn [context]
     (let [accepted (get-in context [:request :accept :field] "application/json")
           response (:response context)
           body (:body response)
           coerced-body (case accepted
                          "application/edn" (pr-str body)
                          "application/json" (json/generate-string body))
           updated-response (assoc response
                                   :headers {"Content-Type" accepted}
                                   :body coerced-body)]
       (assoc context :response updated-response)))})
