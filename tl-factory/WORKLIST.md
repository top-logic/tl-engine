# Worklist of the control application (tl-factory)

Known gaps of the control application whose fix lies outside this module. Each entry names the
ticket that closes it and what changes here once that ticket is merged. An entry is removed when
its gap is closed.

## Gaps

| Gap | Effect in tl-factory | Closed by | Then in tl-factory |
|-----|----------------------|-----------|--------------------|
| React forms ignore the create visibility of an attribute (`AttributeFieldControl` of `com.top_logic.layout.view`). | The attributes of `tl.factory:Project` that are read-only but editable on creation cannot be entered in "Anwendung anlegen": `repository` (an existing repository to adopt) and `tenant` (the tenant an operator creates a project in). | #30014 | Nothing to change: merge master into the branch and check both fields in the create dialog. |
| A creation in a container is not checked while the container channel is empty (`ModelAccessRule.creationInContainer` of `com.top_logic.layout.view`). | "Anwendung anlegen" (`create-transient` of `tl.factory:Project` with `container="tenant"` in `factory/projects.view.xml`) is offered to a user who is neither operator nor member of a tenant; the creation fails only on saving. | #30015 | Nothing to change: the command is hidden for such a user. Check it in the browser with an account of no tenant. |
