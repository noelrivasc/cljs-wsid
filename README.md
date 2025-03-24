# What Should I Do?

A re-frame app to help you make tricky decisions.

The default readme of the re-frame app is found at DEFAULT-README.md.

Started with https://github.com/day8/re-frame-template at commit 3d4358f14bc2e85b22ba6bafaddfb9561b204d15;

## Development

### Running

```
npm install
npm run watch
```

This will run the shadow-cls build tool, start a development web server with hot code reload
at http://localhost:8280/ and start Tailwind watch.

### Tailwind integration

- Tailwind CLI v4.x is installed as a NPM dependency
- A `tailwind-watch` command is provided in package.json. This command watches the *.cljs files for changes and compiles Tailwind CSS to a location included in index.html
