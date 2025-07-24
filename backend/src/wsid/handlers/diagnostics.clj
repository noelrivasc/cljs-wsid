(ns wsid.handlers.diagnostics
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http-client]
   [io.pedestal.http :as http]
   [io.pedestal.log :as log]
   [wsid.logging :as logging :refer [debug-timing] :rename {debug-timing dt}]) 
  (:import
   [java.net InetAddress NetworkInterface]))

(def db-connection
  "Attempt to connect to the DB and produce logs useful to diagnose the connection."
  {:name :db-connection
   :enter (fn [context]
            (let [msg (if (:db-connection context)
                        "DB connection found in context at :db-connection"
                        "DB connection not found in context.")]
              (io.pedestal.log/info :msg msg))
            
            (http/respond-with context 200 "db-connection"))})

(defn get-network-interfaces 
  "Get all network interfaces and their details"
  []
  (try
    (let [interfaces (enumeration-seq (NetworkInterface/getNetworkInterfaces))]
      (map (fn [iface]
             {:name (.getName iface)
              :display-name (.getDisplayName iface)
              :up? (.isUp iface)
              :loopback? (.isLoopback iface)
              :point-to-point? (.isPointToPoint iface)
              :virtual? (.isVirtual iface)
              :mtu (.getMTU iface)
              :addresses (map #(.getHostAddress %) (enumeration-seq (.getInetAddresses iface)))})
           interfaces))
    (catch Exception e
      [{:error (.getMessage e)}])))

(defn test-dns-resolution
  "Test DNS resolution for a hostname"
  [hostname]
  (try
    (let [start-time (System/nanoTime)
          address (InetAddress/getByName hostname)
          end-time (System/nanoTime)
          duration-ms (/ (- end-time start-time) 1000000.0)]
      {:hostname hostname
       :resolved-ip (.getHostAddress address)
       :resolution-time-ms duration-ms
       :success true})
    (catch Exception e
      {:hostname hostname
       :error (.getMessage e)
       :success false})))

(defn test-http-connectivity
  "Test HTTP connectivity to a URL with detailed timing"
  [url timeout-ms]
  (try
    (let [start-time (System/nanoTime)]
      (dt {:request-start-time (java.time.Instant/now)} "Starting HTTP test" :data {:url url :timeout timeout-ms})
      (let [response (http-client/get url {:timeout timeout-ms
                                          :connection-timeout timeout-ms
                                          :throw-exceptions false
                                          :as :text})
            end-time (System/nanoTime)
            duration-ms (/ (- end-time start-time) 1000000.0)]
        (dt {:request-start-time (java.time.Instant/now)} "HTTP test completed" :data {:status (:status response) :duration duration-ms})
        {:url url
         :status (:status response)
         :success (<= 200 (:status response) 399)
         :response-time-ms duration-ms
         :headers (:headers response)
         :body-length (count (:body response ""))}))
    (catch Exception e
      (dt {:request-start-time (java.time.Instant/now)} "HTTP test failed" :data {:url url :error (.getMessage e)})
      {:url url
       :error (.getMessage e)
       :success false})))

(defn get-environment-info
  "Get relevant environment information for AWS Lambda"
  []
  {:java-version (System/getProperty "java.version")
   :java-vendor (System/getProperty "java.vendor")
   :os-name (System/getProperty "os.name")
   :os-arch (System/getProperty "os.arch")
   :aws-lambda-function-name (System/getenv "AWS_LAMBDA_FUNCTION_NAME")
   :aws-lambda-function-version (System/getenv "AWS_LAMBDA_FUNCTION_VERSION")
   :aws-lambda-log-group-name (System/getenv "AWS_LAMBDA_LOG_GROUP_NAME")
   :aws-lambda-log-stream-name (System/getenv "AWS_LAMBDA_LOG_STREAM_NAME")
   :aws-region (System/getenv "AWS_REGION")
   :aws-default-region (System/getenv "AWS_DEFAULT_REGION")
   :vpc-config-subnet-ids (System/getenv "_LAMBDA_VPC_CONFIG_SUBNET_IDS")
   :vpc-config-security-group-ids (System/getenv "_LAMBDA_VPC_CONFIG_SECURITY_GROUP_IDS")})

(def aws-network-diagnostics
  "Comprehensive AWS Lambda network diagnostics handler"
  {:name :aws-network-diagnostics
   :enter (fn [context]
            (dt context "AWS network diagnostics starting")
            
            (let [diagnostics-data
                  {:timestamp (java.time.Instant/now)
                   :environment (get-environment-info)
                   :network-interfaces (get-network-interfaces)
                   :dns-tests [(test-dns-resolution "api.deepinfra.com")
                               (test-dns-resolution "google.com")
                               (test-dns-resolution "aws.amazon.com")]
                   :connectivity-tests [(test-http-connectivity "https://httpbin.org/get" 1000)]}]
              
              (dt context "Network diagnostics completed" :data {:test-count (+ (count (:dns-tests diagnostics-data))
                                                                                (count (:connectivity-tests diagnostics-data)))})
              
              (log/info :msg "AWS Network Diagnostics Results" :data diagnostics-data)
              
              (http/respond-with context 200 {:content-type "application/json"
                                             :body (json/generate-string diagnostics-data {:pretty true})})))})