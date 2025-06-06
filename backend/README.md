# WSID Backend

A Clojure-based backend for WSID built with Pedestal and deployed on AWS Lambda.

## Local Development Setup

### Prerequisites
- Docker and Docker Compose
- Clojure CLI tools
- Java 11+

### Quick Start

1. **Start PostgreSQL database:**
   ```bash
   docker-compose up -d postgres
   ```

2. **Copy environment configuration:**
   ```bash
   cp .env.example .env
   # Edit .env if needed
   ```

3. **Download dependencies:**
   ```bash
   clj -P
   ```

4. **Start the development server:**
   ```bash
   clj -M -m wsid
   ```

The server will start on `http://localhost:8890`

### Test User
- Email: `test@example.com`
- Password: `000000`

### Database Management

- **Reset database:** `docker-compose down -v && docker-compose up -d postgres`
- **View logs:** `docker-compose logs postgres`
- **Connect to DB:** `docker-compose exec postgres psql -U wsid_user -d wsid`

### API Documentation
See `openapi.yaml` for complete API specification.

## AWS Lambda Deployment

Compile uberjar

clojure -X:uberjar

Run compiled jar

java -jar target/wsid-lambda.jar

Deploy jar to lambda function with handler `wsid::handler` (the name of the class, ::, then the name of the static handler method). The jar can be uploaded manually for testing purposes, but is normally built by github actions and deployed to the lambda.

### Lambda Components

- **Handler**: `wsid/handler` - entry point for AWS Lambda
- **Build**: AOT compilation enabled for faster cold starts
