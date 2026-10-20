---
description: Read before adding an entry point that starts agent runs, or when a run is refused for lack of an account - layer L0: TopLogicSecurityContext.forRun(Person, background), the principal of each entry point (chat, aiAgentChat, aiAgentChatAsync, delegation, /mcp/*, AgentTask), RunSecurity.contextFor, the test seams SecurityContext.UNRESTRICTED / ToolContext.EMPTY, and why every application needs a <security-config>.
order: 10
---

# Identity

**There is no security mode.** The enforcement layers apply to every agent run, unconditionally and unconfigurably. What a run may do follows from a single question - *who does it act as* - and a run that cannot answer it does not start. Whether enforcement is on is not a question; the principal is answered per entry point.

## Every run carries a `Person`, or refuses to start

`TopLogicSecurityContext.forRun(Person, boolean background)` is the single production constructor of a run's security context, and it throws when the principal is `null`. Every entry point reaches it through `RunSecurity.contextFor(RunPrincipal)`, which asks the installation's `<security-context>` factory ([approval decided from the call](doc:ai/security/administration#approval-decided-from-the-call)). Refusing where the context is built rather than at the first tool call is deliberate: a run without a principal cannot be authorized, and failing later wastes a provider round trip and produces a confusing half-run.

| Entry point | Principal |
|---|---|
| `ChatProcessor` (`/chat/*`) | the logged-in chat user; `ChatSessionFilter` has already rejected an unauthenticated caller |
| `AIScriptFunctions.aiAgentChat` | the account that executed the script (`ToolContext.forCurrentUser()`, which throws when the thread has none) |
| `AIScriptFunctions.aiAgentChatAsync` | the same account; the function refuses a sub-session without a `Person`, and the background run re-enters that sub-session |
| `aiCallTool` | the calling account; the function applies the tool permission check itself, since it dispatches a tool directly and never passes the agent loop's gate |
| Delegated child, detached subagent | the parent's principal, inherited unchanged |
| `/mcp/*`, `/a2a/*` | the account the request's credential names. The authentication filter rejects a request it cannot attribute to one - and rejects a credential mapped to *no* account, which the engine would otherwise read as "run this in system context". The run is classified **background**, because a remote caller has nobody to answer an approval prompt ([the MCP endpoint](doc:ai/security/mcp-endpoint)) |
| The scheduler task `AgentTask` | its configured `run-as` account - a real account holding real roles, administered like any other. It binds that account to its sub-session, so the run acts, and is recorded, as that account, bounded by the `<background-allowlist>` |

The resolved `Person` is captured on the `SecurityContext` at construction and is **not** re-read from `TLContext` per check: a concurrent tool batch runs on worker threads that re-establish the sub-session, and a design that re-reads thread state per check is one thread-local bug away from checking the wrong user.

There is no mode input at all - from the caller, the model or an agent definition. An agent definition declares no security mode, and `AgentDefinition` carries no field for one. A mode that an agent could ask for would make editing an agent definition a way to buy unlimited reach over all data; a total bypass was never a bounded one, because the background allowlist lives inside the tool authorization a bypass would switch off.

**Delegation and subagents.** A child run inherits its parent's `SecurityContext` unchanged, so it can never come out wider than its parent. A detached subagent additionally loses the approval prompt when the parent turn ends, so `ToolContext.forDetachedSubagent` narrows it by the `<background-allowlist>` - see [tool authorization](doc:ai/security/tool-authorization#background-and-delegated-runs). Being attended is a property separate from identity, which is why the `background` flag exists.

Genuine framework-internal bypasses - the security storage rebuild, migrations - use `ThreadContext.inSystemContext` in Java. That is where a bypass belongs, not in agent configuration.

## Two engine behaviours the identity rests on

- **`ThreadContext.isSystemContext()` fully bypasses model security.** `SecurityConfigurationService.isAllowedBypass` returns `TRUE` for a system context, and `BoundChecker.isAllowedBypass` returns `TRUE` for `ThreadContext.isAdmin()` and `FALSE` for a `null` user. So *how a run establishes its thread context decides whether any model security applies at all*. The object tools and `changeData` therefore refuse to run in a system context ([object tools](doc:ai/security/object-tools)), and so does a search of `tl-ai-index`.
- **`ToolExecutions.execute` wraps a tool call in `ThreadContextManager.inSystemInteraction(...)`.** That call reuses an existing interaction and creates a system interaction only when none is bound. So the chat path keeps the user's identity, while a path without a context would get a system interaction - which is why every production path has one. The MCP path deliberately does not go through it: its requests arrive with an authenticated account whose context the filter has opened, and it refuses rather than falling back to a system interaction. `McpCallerContext` is the explicit counterpart on that path: no bound account, no read.

## Test seams

Three permissive constructs exist, all of them test seams that no production path reaches:

| Construct | Why it exists |
|---|---|
| `SecurityContext.UNRESTRICTED` | a context with no principal, for unit tests of anything that merely needs *a* context |
| `ToolContext.EMPTY` | the same, wrapped with `AUTO_APPROVE` and a no-op transaction scope |
| `ModelSecurity.UNCHECKED`, `ToolAuthorization.UNCHECKED`, `PreCommitGuard.PERMISSIVE` | the engine's model rights need a booted `SecurityConfigurationService` and the tool policy a booted `AiToolAccessService`; a unit test about how a context *behaves* has neither |

`AiToolAccessService.authorizationFor` and `ModelAccessRightsSecurity` both throw when their service is down, rather than granting everything - both services are registered active by default, so absence is a deliberate deactivation and not a state to be permissive about.

## The consequence an administrator meets first

**Every application must ship a `<security-config>`, or its agent can read nothing.** `SecurityConfigurationService` is registered active by default in every TopLogic 8 application (`tl-core`'s web fragment switches its module on) and starts with empty rights, which is deny-all for role-based accounts. An application that writes no `<security-config>` has not opted out of model security; it has denied everything. TL-Script applies the same model access rights (`CreateObject` checks CREATE, attribute reads are filtered), so `changeData`, `query`, `present` and every script tool see creates and reads denied for non-administrators too. Engine `UpdateSecurityVisitor.disableSecurity(expr)` is the documented opt-out for internal queries.

The tool layer is deliberately kinder about the same silence, and only there. An absent `<tool-access>` matrix grants `RoleToolAccess.IMPLICIT_BASELINE` plus `RoleToolAccess.DEFAULT_BASELINE` - `model.read` and `data.read` - so that an application which has configured nothing has a usable, read-only agent instead of an inert one. Nothing in that default mutates (`data.write`, `model.structure`, `agent.authoring`, `agent.meta`, `workspace`), sends data outward (`egress`) or reaches every type (`script`). An application that writes a `<baseline>` replaces it; `<baseline/>` says "nothing for everyone".

> **What the default rests on.** A default `data.read` is safe because L1 grants the *ability to ask* and L2 answers per object: every instance the data tools disclose passes the read filter, and an application with no `<security-config>` has denied everything, so its agent reads nothing. What the default does not bound is a *count* computed inside TL-Script, which no read filter can see into.

Administrators and engine super-users bypass all of it, and so does any code in a system context, so **testing with an admin account proves nothing**. The [worked example](doc:ai/security/role-model-worked-example) is the configuration to follow: one role vocabulary, two grant tables, four personas walked through every operation.
