## PENDING
### State

### Views

### Works
- On press save button, dispatch save factor event (transfer factor-active to list of factors; produce UUID if necessary)
- Write save factor event handler (returns new db, no need for fx here)
- On press the delete button, dispatch remove factor event
- Write remove factor event handler (return  new db) 
- On press cancel button, dispatch unset currently edited - not an empty map but nil
- Condition the rendering of the form to the thing being edited not nil
- Add active factor validation (disable save until factor is valid; show errors)