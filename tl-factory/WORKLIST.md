# Worklist of the control application (tl-factory)

Known gaps of the control application whose fix lies outside this module. Each entry names the
ticket that closes it and what changes here once that ticket is merged. An entry is removed when
its gap is closed.

## Gaps

| Gap | Effect in tl-factory | Closed by | Then in tl-factory |
|-----|----------------------|-----------|--------------------|
| React forms ignore the create visibility of an attribute (`AttributeFieldControl` of `com.top_logic.layout.view`). | The attributes of `tl.factory:Project` that are read-only but editable on creation cannot be entered in "Anwendung anlegen": `repository` (an existing repository to adopt), `tenant` (the tenant an operator creates a project in) and `gitConnection` (the git connection; until then a new project gets the default, the first built-in connection). | #30014 | Nothing to change: merge master into the branch and check the three fields in the create dialog. |
| A classifier (value of an enumeration) cannot be discovered by a user without administrator rights, so such a user cannot set an attribute of an enumeration type (`Update.checkNewTargets` of `com.top_logic.model.search`, `SecurityConfigurationService.getRoles`). | A Verwalter can neither change the role of a member ("Rolle ändern", `factory/change-member-role.view.xml`) nor create a member with another role than the default (`factory/create-member.view.xml`); the operator can. | #30016 | Preset the current role of the member in "Rolle ändern" (left out because presetting it fails the same way), then check both dialogs as Verwalter. |
| A creation in a container is not checked while the container channel is empty (`ModelAccessRule.creationInContainer` of `com.top_logic.layout.view`). | "Anwendung anlegen" (`create-transient` of `tl.factory:Project` with `container="tenant"` in `factory/projects.view.xml`) is offered to a user who is neither operator nor member of a tenant; the creation fails only on saving. | #30015 | Nothing to change: the command is hidden for such a user. Check it in the browser with an account of no tenant. |
