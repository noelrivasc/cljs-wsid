(ns wsid.interceptors.content-negotiation 
  (:require
   [io.pedestal.http.content-negotiation :as content-negotiation]))


(def supported-types ["application/edn"
                      "application/json"])

(def content-negotiation-interceptor
  (content-negotiation/negotiate-content supported-types))