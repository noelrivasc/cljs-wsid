(ns lambda
  (:require [clojure.string :as string]
            [io.pedestal.interceptor.chain :as chain])
  (:import [java.io ByteArrayInputStream]
           [com.amazonaws.services.lambda.runtime Context]))

(comment
  "The functions in this file are almost a straight copy from those found
   in io.pedestal.http.aws.lambda.utils. The difference is that these can
   take either API Gateway events, or events from Lambdas with an URL.")

(defn lambda-request-map
  "Given a parsed JSON event from API Gateway or Lambda Function URL,
  return a Ring compatible request map.

  Supports both API Gateway events and Lambda Function URL events (payload version 2.0).
  
  Optionally, you can decide to `process-headers?`, lower-casing them all to conform with the Ring spec
   defaults to `true`

  This assumes the event has strings as keys
  -- no conversion has taken place on the JSON object other than the original parse.
  -- This ensures parse optimizations can be made without affecting downstream code."
  ([event]
   (lambda-request-map event true))
  ([event process-headers?]
   (let [is-lambda-url? (and (= "2.0" (get event "version"))
                             (contains? event "rawPath"))
         path (if is-lambda-url?
                (get event "rawPath" "/")
                (get event "path" "/"))
         headers (get event "headers" {})
         [http-version host] (string/split (get headers "Via" "") #" ")
         port (try (Integer/parseInt (get headers "X-Forwarded-Port" "")) (catch Throwable t 80))
         source-ip (if is-lambda-url?
                     (get-in event ["requestContext" "http" "sourceIp"] "")
                     (get-in event ["requestContext" "identity" "sourceIp"] ""))
         http-method (if is-lambda-url?
                       (get-in event ["requestContext" "http" "method"])
                       (get event "httpMethod"))
         query-string-params (when-let [params (get event "queryStringParameters")]
                               (into {} params))]
     {:server-port port
      :server-name (or host "")
      :remote-addr source-ip
      :uri path
      :query-string (when is-lambda-url? (get event "rawQueryString"))
      :query-string-params query-string-params
      :path-params (get event "pathParameters" {})
      :scheme (get headers "X-Forwarded-Proto" "http")
      :request-method (some-> http-method
                              string/lower-case
                              keyword)
      :headers (if process-headers?
                 (persistent! (reduce (fn [hs [k v]]
                                        (assoc! hs (string/lower-case k) v))
                                      (transient {})
                                      headers))
                 headers)
      :body (when-let [body (get event "body")]
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
  for an AWS API Gateway event, under `:io.pedestal.aws.lambda/lambda-handler`.

  This provider function takes the lambda-event map and the runtime.Context
  and returns an AWS API Gateway response map (containing -- :statusCode :body :headers)
  You may want to add a custom interceptor in your chain to handle Scheduled Events.

  This chain terminates if a Ring `:response` is found in the context
  or an API Gateway `:lambda-response` map is found.

  All additional conversion, coercion, writing, and extension should be handled by
  interceptors in the interceptor chain."
  [service-map]
  (let [interceptors (:io.pedestal.http/interceptors service-map [])
        default-context (get-in service-map [:io.pedestal.http/container-options :default-context] {})
        body-processor (get-in service-map
                               [:io.pedestal.http/container-options :body-processor]
                               (resolve-body-processor))]
    (assoc service-map
           :io.pedestal.aws.lambda/lambda-handler
           (fn [lambda-event ^Context context] ;[^InputStream input-stream ^OutputStream output-stream ^Context context]
             (let [;event (json/parse-stream
                   ;        (java.io.PushbackReader. (java.io.InputStreamReader. input-stream))
                   ;        nil
                   ;        nil)
                   request (lambda-request-map lambda-event)
                   initial-context (merge {;:aws.lambda/input-stream input-stream
                                           ;:aws.lambda/output-stream output-stream
                                           :aws.lambda/context context
                                           :aws.lambda/event lambda-event
                                           :request request
                                           ::chain/terminators [#(let [resp (:response %)]
                                                                   (and (map? resp)
                                                                        (integer? (:status resp))
                                                                        (map? (:headers resp))))
                                                                #(map? (:lambda-response %))]}
                                          default-context)
                   response-context (chain/execute initial-context interceptors)
                   response-map (or (:lambda-response response-context)
                                    ;; Use `or` to prevent evaluation
                                    (some-> (:response response-context)
                                            (lambda-response body-processor)))]
               response-map)))))