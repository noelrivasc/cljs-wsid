# What Should I Do?

A proof-of-concept decision-support tool built as a learning experiment in Clojure and ClojureScript. You define factors, weight them, describe scenarios, and the app helps you reason through the trade-offs — optionally with LLM assistance. The real goal is exploring idiomatic Clojure patterns: composable pure functions, Pedestal interceptor pipelines, and re-frame's event/subscription architecture.

The LLM integration (`backend/src/wsid/handlers/llm.clj`) is a good place to start reading. It demonstrates a runtime-configurable pipeline where providers, prompt templates, and output-processing functions are all data — swappable maps and function references rather than hard-wired code paths. From there, `core.clj` shows how routes and interceptors are composed into request-handling chains.

On the frontend, `src/wsid/db.cljs` defines the entire app-state shape with `clojure.spec`, and those specs are used throughout `events/` to validate entities on every mutation. The `events/` and `subs/` directories show the same composability idea applied to UI state via re-frame's event/subscription pattern.

## Development

### Running

**Backend** (see backend/README.md)

**Run the frontend** (see frontend/README.md for options)

```
cd frontend
npm install
npm run watch
```

**Run the proxy server**

```bash
./start-proxy
# This just runs nginx with the proxy.conf configuration
# nginx -c $(pwd)/proxy.conf -g "daemon off;"
```

The proxy just helps deal with CORS issues on localhost. The app can run fine without it in other environments.

## Deployment

The backend compiles to an uberjar (`clojure -X:uberjar`) and runs on AWS Lambda with the Java 21 runtime, exposed via a Lambda Function URL. The frontend builds to static assets (`npm run release`) and is synced to an S3 bucket served through CloudFront. Both pipelines are automated through GitHub Actions — see `.github/workflows/` for the definitions and `backend/AWS.md` for the Lambda setup instructions. There is no infrastructure-as-code; AWS resources were configured manually.
