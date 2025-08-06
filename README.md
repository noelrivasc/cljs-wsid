# What Should I Do?

A re-frame + Pedestal app to help you make tricky decisions.

## Development

### Running

**Run the backend** (see backend/README.md for options)

```bash
cd backend
clj -M -m wsid.core
```

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
