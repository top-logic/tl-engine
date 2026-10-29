# Worklist of the control application (tl-factory)

Known gaps of the control application whose fix lies outside this module. Each entry names the
ticket that closes it and what changes here once that ticket is merged. An entry is removed when
its gap is closed.

## Gaps

| Gap | Effect in tl-factory | Closed by | Then in tl-factory |
|-----|----------------------|-----------|--------------------|
| A classifier (value of an enumeration) cannot be discovered by a user without administrator rights, so such a user cannot set an attribute of an enumeration type (`Update.checkNewTargets` of `com.top_logic.model.search`, `SecurityConfigurationService.getRoles`). | A Verwalter can neither change the role of a member ("Rolle ändern", `factory/change-member-role.view.xml`) nor create a member with another role than the default (`factory/create-member.view.xml`); the operator can. | #30016 | Preset the current role of the member in "Rolle ändern" (left out because presetting it fails the same way), then check both dialogs as Verwalter. |
| Deciding whether a command or unit is hidden fails with a `NullPointerException` for a type whose access parent does not know its parent types statically (`ModelAccessPolicy.isGrantable` of `com.top_logic.layout.view`; `ContainerRelation` answers `null`). | Every member of a tenant: the first page after the login ends with "Internal error" (a reload passes it), and selecting a project updates its detail only partly; the agent tab does not open. `Workspace` and `AgentConversation` have their container as access parent. The operator (`root`) is not affected. | #30024 | Nothing to change: merge master once #30024 is merged, then check login, project selection and the agent tab as Verwalter and Leser. |
