## PENDING
### State

### Views
- Add edit icon or button to factors panel
- Remove name from title

### Work
- On press new button, set state of currently edited factor to empty map (no id yet)
- On press save button, dispatch save factor event
- Write save factor event handler (returns new db, no need for fx here)
- On press the delete button, dispatch remove factor event
- Write remove factor event handler (return  new db) 
- On press cancel button, dispatch unset currently edited - not an empty map but nil
- Condition the rendering of the form to the thing being edited not nil