# Worklist of the control application (tl-factory)

Known gaps of the control application whose fix lies outside this module. Each entry names the
ticket that closes it and what changes here once that ticket is merged. An entry is removed when
its gap is closed.

## Gaps

| Gap | Effect in tl-factory | Closed by | Then in tl-factory |
|-----|----------------------|-----------|--------------------|
| A creation in a container is not checked while the container channel is empty (`ModelAccessRule.creationInContainer` of `com.top_logic.layout.view`). | "Anwendung anlegen" (`create-transient` of `tl.factory:Project` with `container="tenant"` in `factory/projects.view.xml`) is offered to a user who is neither operator nor member of a tenant; the creation fails only on saving. | #30015 | Nothing to change: the command is hidden for such a user. Check it in the browser with an account of no tenant. |
