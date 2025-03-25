## TODO
- Add edit button for events
- Handle edit button press for events
- On press the delete button, dispatch remove factor event
- Write remove factor event handler (return  new db) 
- Add active factor validation (disable save until factor is valid; show errors)

## Style the thing
- Add max dimensions to main panel, center.
- Add a title.
- Add a title for a decision (Bulsamia).
- Add a description field for decision.
- Make layout (2-col; responsive mobile 1-col)
- Make factors container, title, float add icon
- Make factors list, each w/ name, tooltip, edit button, value bar.
- Make scenarios panel, grey, place holder w/ title.

## Topics I'd like to cover

### Not yet
(low) use spec to validate state
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
