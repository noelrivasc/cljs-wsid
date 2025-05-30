## Local Development

Run using jetty (I think I'd use this in a container, but has no real use now either in dev or AWS Lambda)

clojure -M -m wsid

## AWS Lambda Deployment

Compile uberjar

clojure -X:uberjar

Run compiled jar

java -jar target/wsid-lambda.jar

### Lambda Components

- **Handler**: `wsid/lambda-handler` - entry point for AWS Lambda
- **Adapter**: `ring-apigw-lambda-proxy` - converts API Gateway events to Ring requests
- **Build**: AOT compilation enabled for faster cold starts
- **Integration**: API Gateway proxy routes all requests to single Lambda function
