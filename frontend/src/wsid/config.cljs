(ns wsid.config)

(def debug?
  ^boolean goog.DEBUG)

(goog-define api-base-url "http://localhost:3000")

(def config
  {:api {:base-url api-base-url}})
