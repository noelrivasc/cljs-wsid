## Epics
- Design UX for full behavior
- Finish core behavior (scenarios; evaluation)
- Style the app
- Implement Spec fully (db, key functions in events)

## TODO
- Add active factor validation (disable save until factor is valid; show errors)
- Debounce form input or create local state to prevent fast re-renders on input

## Style the thing
- Add max dimensions to main panel, center.
- Add a title.
- Add a title for a decision (Bulsamia).
- Add a description field for decision.
- Make layout (2-col; responsive mobile 1-col)
- Make factors container, title, float add icon
- Make factors list, each w/ name, tooltip, edit button, value bar.
- Make scenarios panel, grey, place holder w/ title.
- Separate markup structure from markup style; make a function to merge style data by using BEM classes; components are a potentially hard to maintain eye sore

## Topics I'd like to cover

### Not yet
HIGH! (otherwise one has to read all the code to know what a _factor_ is)) use spec to validate state
use fx handlers
use external data for coeffects -> load decision, parse JSON
use effect handler, custom
use HTTP effect handler
use effect handler, cookie or local storage.

### Covered:
(low) use figwheel >> using shadow cljs for building and code reloading
(low) use calva?
Set up state
Use extractors & transformers
render something to screen
dispatch events
use event handlers 
(very low) integrate graphic library (three, pixi, something else). Figure out how continuous drawing (interpolation) can interact with the state in both directions
