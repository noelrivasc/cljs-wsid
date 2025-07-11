# WSID frontend

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
