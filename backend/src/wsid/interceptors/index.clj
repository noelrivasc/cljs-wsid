(ns wsid.interceptors.index
  (:require
   [wsid.interceptors.auth :refer [auth-interceptor]]
   [wsid.interceptors.coerce-body :refer [coerce-body-interceptor]]
   [wsid.interceptors.content-negotiation :refer [content-negotiation-interceptor]]
   [wsid.interceptors.db :refer [db-interceptor]]
   [wsid.interceptors.parse-body :refer [parse-body-interceptor]]))

(def auth auth-interceptor)
(def db db-interceptor)
(def parse-body parse-body-interceptor)
(def content-negotiation content-negotiation-interceptor)
(def coerce-body coerce-body-interceptor)