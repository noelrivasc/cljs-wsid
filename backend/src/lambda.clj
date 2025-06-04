(ns lambda
  (:require [clojure.string :as string]
            [io.pedestal.interceptor.chain :as chain])
  (:import [java.io ByteArrayInputStream]
           [com.amazonaws.services.lambda.runtime Context]))

(defn lambda-request-map
  "Given a parsed JSON event from API Gateway,
  return a Ring compatible request map.

  Optionally, you can decide to `process-headers?`, lower-casing them all to conform with the Ring spec
   defaults to `true`

  This assumes the apigw event has strings as keys
  -- no conversion has taken place on the JSON object other than the original parse.
  -- This ensures parse optimizations can be made without affecting downstream code."
  ([apigw-event]
   (lambda-request-map apigw-event true))
  ([apigw-event process-headers?]
   (let [path (get apigw-event "path" "/")
         headers (get apigw-event "headers" {})
         [http-version host] (string/split (get headers "Via" "") #" ")
         port (try (Integer/parseInt (get headers "X-Forwarded-Port" "")) (catch Throwable t 80))
         source-ip (get-in apigw-event ["requestContext" "identity" "sourceIp"] "")]
     {:server-port port
      :server-name (or host "")
      :remote-addr source-ip
      :uri path
      ;:query-string query-string
      :query-string-params (get apigw-event "queryStringParameters")
      :path-params (get apigw-event "pathParameters" {})
      :scheme (get headers "X-Forwarded-Proto" "http")
      :request-method (some-> (get apigw-event "httpMethod")
                              string/lower-case
                              keyword)
      :headers (if process-headers?
                 (persistent! (reduce (fn [hs [k v]]
                                        (assoc! hs (string/lower-case k) v))
                                      (transient {})
                                      headers))
                 headers)
      ;:ssl-client-cert ssl-client-cert
      :body (when-let [body (get apigw-event "body")]
              (ByteArrayInputStream. (.getBytes ^String body "UTF-8")))
      :path-info path
      :protocol (str "HTTP/" (or http-version "1.1"))
      :async-supported? false})))

(defn resolve-body-processor []
  ;;TODO:
  identity)

(defn lambda-response
  ([ring-response]
   (lambda-response ring-response identity))
  ([ring-response body-process-fn]
   (let [{:keys [status body headers]} ring-response
         processed-body (body-process-fn body)
                       ;(if (string? body)
                       ;  body
                       ;  (->> (ByteArrayOutputStream.)
                       ;       (servlet-utils/write-body-to-stream body)
                       ;       (.toString)))
         ]
     {"statusCode" (or status (if (string/blank? processed-body) 400 200))
      "body" processed-body
      "headers" headers})))

;; --- Proxy doesn't have to be InputStream/OutputStream!
;;     It will perform the JSON parse automatically ---
(defn direct-lambda-provider
  "Given a service map, return a service map with a provider function
  for an AWS API Gateway event, under `:io.pedestal.aws.lambda/apigw-handler`.

  This provider function takes the apigw-event map and the runtime.Context
  and returns an AWS API Gateway response map (containing -- :statusCode :body :headers)
  You may want to add a custom interceptor in your chain to handle Scheduled Events.

  This chain terminates if a Ring `:response` is found in the context
  or an API Gateway `:apigw-response` map is found.

  All additional conversion, coercion, writing, and extension should be handled by
  interceptors in the interceptor chain."
  [service-map]
  (let [interceptors (:io.pedestal.http/interceptors service-map [])
        default-context (get-in service-map [:io.pedestal.http/container-options :default-context] {})
        body-processor (get-in service-map
                               [:io.pedestal.http/container-options :body-processor]
                               (resolve-body-processor))]
    (assoc service-map
           :io.pedestal.aws.lambda/apigw-handler
           (fn [apigw-event ^Context context] ;[^InputStream input-stream ^OutputStream output-stream ^Context context]
             (let [;event (json/parse-stream
                   ;        (java.io.PushbackReader. (java.io.InputStreamReader. input-stream))
                   ;        nil
                   ;        nil)
                   request (lambda-request-map apigw-event)
                   initial-context (merge {;:aws.lambda/input-stream input-stream
                                           ;:aws.lambda/output-stream output-stream
                                           :aws.lambda/context context
                                           :aws.apigw/event apigw-event
                                           :request request
                                           ::chain/terminators [#(let [resp (:response %)]
                                                                   (and (map? resp)
                                                                        (integer? (:status resp))
                                                                        (map? (:headers resp))))
                                                                #(map? (:apigw-response %))]}
                                          default-context)
                   response-context (chain/execute initial-context interceptors)
                   response-map (or (:apigw-response response-context)
                                    ;; Use `or` to prevent evaluation
                                    (some-> (:response response-context)
                                            (lambda-response body-processor)))]
               response-map)))))