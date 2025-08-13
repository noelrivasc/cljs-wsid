(ns wsid.config)

(def debug?
  ^boolean goog.DEBUG)

(goog-define api-base-url "http://localhost:3000")
(def api-base-url "https://vowdnadrthaorh6in7eowoxcxu0ultza.lambda-url.us-west-2.on.aws")

(def config
  {:api {:base-url api-base-url}})
