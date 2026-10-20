---
description: Read before declaring @ToolMethod(types = ...) on a tool - the pre-flight check DeclaredTypeAccess (refused when the principal may read none of the declared types, dropped from the schemas by ToolPermissionFilter), Unusable.Cause.TYPE_UNREADABLE, and the rule to declare only types whose Read grant goes to a role held on the security root.
order: 31
---

# Declared types

A tool names the qualified model types it works on, and the names are checked against the acting principal's READ rights before the tool runs:

```java
@ToolMethod(description = "...", mode = ToolMode.READ,
            group = ToolGroups.DATA_READ, types = { "myapp:Customer", "myapp:Contract" })
```

## The rule

A tool that declares types is refused when the principal may read **none** of them, and is left out of the tool schemas the provider request carries for the same reason. Not "one of them is unreadable": a tool naming `Customer` and `Contract` can still do the customer half of its job for a user who may read customers, and refusing it would take away work that user is allowed to do. Gate and schema filter are the same predicate on purpose - a rule that refused on one unreadable type while the filter dropped only fully unreadable tools would advertise a tool that every call refuses.

`DeclaredTypeAccess` decides, and `ToolPermissionFilter` asks it - the one seam both the schema filter (`AgentLoop.callProviderOnce`) and the invocation gate (`AgentLoop.executeWithSecurityCheck`, and `McpToolAdapter` on the server side) go through. The refusal is an ordinary tool error: flagged `is_error`, excluded from the turn's rollback decision like every other denial, and logged at `info` rather than `warn`, because an account without a read right is a configured state and not an operator's problem.

`AgentCapabilities` asks the same check, over a `DeclaredTypeAccess` built from the very `SecurityContext.modelSecurity()` the filter uses - one rule, asked where it is needed. That keeps the surfaces of the [capability notices](doc:ai/security/capability-notices) honest: `listTools` does not advertise a declared tool the schema filter drops, and a skill whose only tool is type-withheld does not count as usable. A refusal for a declared type names no capability, because the capability a type-withheld tool costs is one the account **holds** - naming it would send an administrator looking for a grant that is already there. A tool, skill or agent that nothing but its declared types withheld therefore answers `Unusable.Cause.TYPE_UNREADABLE`, whose `aboutCapabilities()` is false, and `SubagentSpawner`, `DelegationTool` and `listTools` relay the sentence naming the type. One capability-denied tool in the set makes the cause `NO_REACHABLE_TOOL` again - a capability grant then really does reach something - and the message names the types as well. `TestCapabilityDeclaredTypes` pins it on the `TypeReadAccess` seam; `SecDemoDeclaredTypesTest` pins what the real model rights answer.

## What it buys

1. **A pre-flight refusal with a reason.** "you have no read access to `myapp:Customer`" instead of an empty result the model reports to the user as "there are no customers". The sentence also says that a *model access* right and not a tool permission is the cause, so nobody looks in `<tool-access>` for an entry that is already correct.
2. **Schema filtering.** The tool is not offered, so the model does not plan around it.
3. **Reviewability.** A reviewer reads off which tools claim which types.

## Which types may be declared

The check reads roles **on the security root**, through `ModelSecurity.accessibleTypes(READ)` - the computation the model digest and the read filter use. It is an L1 check and has L1's scope:

> **Declare a type only where its READ grant goes to a role held on the security root.** A type whose read right is derived *per object* must **not** be declared. `tl.ai.screenshots:Screenshot` grants Read to `AiBinaryOwner`, which a `<rule type="reference">` derives from the screenshot's own owner: nobody holds that role on the root, so declaring the type would refuse the owner their own screenshot. Where you cannot tell, declare nothing.

That is why **no shipped tool declares a type**. The AI modules' own model types are either owner-derived (`Screenshot`, `CookiePool`) or carry no `<security-config>` at all (`tl.ai.execution:UserMemory`, `AgentSession`, `tl.ai.core:Agent`), and model access is deny-by-default - so a declaration on any of them would refuse the tool to every non-administrator. Declaring is a statement an *application* makes about its own tier grants. The test bed's `secdemo-projects.listProjects` / `listProjectsUndeclared` pair (`SecDemoDeclaredTypesTest`) differs in the declaration and in nothing else.

**Tools that cannot declare anything.** Instance browsing (`listInstances`, `getInstance`), TL-Script execution and the search tools take their type from an *argument*, so there is nothing fixed to name. They declare nothing and are never refused on this ground; what governs them is the per-object checks that apply to every tool - `ModelSecurity.canRead` on each object, `TidResolver` on each identifier, the [result filtering](doc:ai/security/result-filtering) and the [pre-commit guard](doc:ai/security/pre-commit-guard). A TL-Script tool and a remote MCP tool declare nothing by construction.

**Not a substitute for a check.** A tool can declare a type it does not touch, or touch one it did not declare, so L2 and L4 apply in full whatever the declaration says. A declaration makes a refusal *legible*; it never makes a check unnecessary, which is also why a name that resolves to no type of the application model is skipped with a warning rather than treated as a denial.
