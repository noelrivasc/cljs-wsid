Run using jetty (I think I'd use this in a container, but has no real use now either in dev or AWS Lambda)

clojure -M -m wsid

Compile uberjar

clojure -X:uberjar

Run compiled jar

java -jar target/wsid-lambda.jar
