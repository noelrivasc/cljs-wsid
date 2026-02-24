# WSID frontend

A re-frame app to help you make tricky decisions.

The default readme of the re-frame app is found at DEFAULT-README.md.

Started with https://github.com/day8/re-frame-template at commit 3d4358f14bc2e85b22ba6bafaddfb9561b204d15;

## REPL

### Running build and REPL

```bash
# Run tailwind & cljs watch + nREPL
# See package.json
npm run watch
```

Running the REPL by itself is possible but does not seem to make much sense, as it leads to a REPL without a JS runtime.

### Connect to running nREPL

```bash
clojure -M:nrebel --port `cat .shadow-cljs/nrepl.port
```

and then to enter the CLJS REPL:

```clojure
(require 'dev.repl :reload) ; reload optional
(dev.repl/init)
```

OR

```clojure
(shadow.cljs.devtools.api/nrepl-select :app)
```

Note that this relies on an alias defined in deps.edn

## Testing

### Running Tests

```bash
# Compile and run browser tests
npm run test:browser

# Or run tests in watch mode (includes browser-test build)
npm run shadow-watch
```

Tests are located in `src/wsid/test/` and should have the `-test` suffix in their namespace names.

Browser tests run at http://localhost:8290/index.html and provide an interactive test runner UI.
