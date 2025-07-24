# AWS Setup Instructions

This document provides step-by-step instructions for setting up AWS infrastructure to deploy the Clojure Pedestal backend to AWS Lambda.

## Prerequisites

- AWS account with appropriate permissions
- AWS CLI configured (optional, for GitHub Actions)
- Built uberjar: `clojure -X:uberjar` (creates `target/wsid-lambda.jar`)

## 1. Create IAM Role for Lambda

**IAM Console** → Roles → Create role:
- **Trusted entity**: AWS service → Lambda
- **Permissions policies**: 
  - `AWSLambdaBasicExecutionRole` (for CloudWatch logs)
- **Role name**: `wsid-backend-execution-role`
- **Note the Role ARN** for Lambda configuration

## 2. Create Lambda Function

**Lambda Console** → Create function:
- **Function name**: `wsid-backend`
- **Runtime**: Java 21
- **Execution role**: Use existing role → `wsid-backend-execution-role`
- **Handler**: `wsid::lambda-handler`
- **Upload**: `target/wsid-lambda.jar`
- **Memory**: 512MB
- **Timeout**: 30 seconds

## 3. Configure Function URL (Recommended)

**Lambda function** → Configuration → Function URL → Create:
- **Auth type**: NONE
- **CORS**: 
  - Allow origins: `*`
  - Allow methods: `*`
  - Allow headers: `*`
- **Copy the Function URL** (needed for GitHub secrets)

## 4. Alternative: Create API Gateway (Optional)

If you prefer API Gateway over Function URL:

**API Gateway Console** → Create API → REST API:
- **API name**: `wsid-api`
- **Resource**: Create resource with path `{proxy+}`
- **Method**: ANY
- **Integration type**: Lambda Function
- **Lambda function**: `wsid-backend`
- **Use Lambda Proxy integration**: ✓
- **Deploy API** → New stage → `prod`
- **Note the Invoke URL**

## 5. Configure GitHub Repository Secrets

**GitHub Repository** → Settings → Secrets and variables → Actions:

Required secrets:
- `AWS_ACCESS_KEY_ID`: Your AWS access key
- `AWS_SECRET_ACCESS_KEY`: Your AWS secret access key
- `AWS_REGION`: Your preferred AWS region (e.g., `us-east-1`)

## 6. Test Deployment

### Build jar file and test it
```bash
# Build the uberjar
clojure -X:uberjar

# Test locally (optional)
java -jar target/wsid-lambda.jar
```

### Remote Testing
- **Function URL**: `{FUNCTION_URL}/ping?timezone=America/Mexico_City`
- **API Gateway**: `{INVOKE_URL}/ping?timezone=America/Mexico_City`

## 7. Deploy via GitHub Actions

Once secrets are configured:
1. Push to `main` branch
2. GitHub Actions will automatically build and deploy
3. Check Actions tab for deployment status

## Architecture Notes

- **Function URL**: Simpler, direct HTTP access to Lambda 
- **API Gateway**: More features (caching, throttling, custom domains)
- **Handler**: `wsid::lambda-handler` in `src/wsid.clj:70-72`
- **Ring Adapter**: `ring-apigw-lambda-proxy` handles request/response conversion

## Troubleshooting

- **Cold starts**: First request may be slow (~2-3 seconds)
- **Logs**: Check CloudWatch Logs for debugging
- **Handler errors**: Verify handler name matches `wsid::lambda-handler`
- **Timeout**: Increase if needed (max 15 minutes for Lambda)