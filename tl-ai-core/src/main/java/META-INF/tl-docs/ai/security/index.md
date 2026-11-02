---
description: Read before configuring or changing the security of AI agent runs - the principle (a run acts as exactly one authenticated account), the threat model (assets, trust boundaries, threats T1-T13, objectives, non-goals), the enforcement layers L0-L6 (identity, tool RBAC, model access rights, result filtering, pre-commit guard, output guard, audit), and what an application must configure (<security-config>, <tool-access>, <background-allowlist>).
order: 70
---

# Security of agent runs

## The principle

An agent always acts **as** the logged-in user. It holds exactly that user's rights: it can see nothing the user may not see and change nothing the user may not change - like a clerk with the same permissions who works faster because they know the shortcuts. Changes to the model and to data are transactional: they are carried out completely or rolled back completely, so a failed operation leaves no inconsistent data behind.

### Permissions on two levels

In a classic TopLogic application, permissions are bound to the user interface: a user may see a view, execute a command, edit a field of a form. That makes sense where the user interface is the only way into the application. An agent does not work through the user interface - it reaches the model and the business data directly. Binding it to the interface permissions would make it work its way through views and commands, and most of its advantage would be lost.

So permissions are looked at on two levels:

- **The data level**: who may read and write which data, independent of the user interface - types, objects and attributes. "Role X may read customers but not change them", "role Y may create orders". This is the engine's `<security-config>` (model access rights, see [the access configuration article](doc:access-configuration)).
- **The interface level**: which views and commands are visible. It is a *projection* of the data level onto the user interface: it decides *how* a user exercises their rights, not *which* rights they have.

For the user interface the interface level is enough, because it reflects the data permissions implicitly: a user who does not see the delete button cannot delete. An agent needs the data level explicitly. It does not ask "may I click the button?", but "may this user change objects of type Customer?" - which is more direct, faster, and just as safe. With a clean data level both sides gain: the agent works directly and is checked on every access; the user interface can derive its rules from the data permissions instead of maintaining its own; and there is one source of truth for permissions, whether an access comes through the user interface, an agent or an interface.

### Two authorizations per tool call

Every tool call needs two independent authorizations:

- the user's roles grant the **tool** - role-based, configured in `<tool-access>` of `AiToolAccessService` ([tool authorization](doc:ai/security/tool-authorization));
- every model object the tool touches passes the **model check** - the engine's `ModelAccessRights` from `<security-config>` ([data authorization](doc:ai/security/data-authorization)).

Neither substitutes for the other: `changeData` is one tool but can touch any type, and `listInstances` is a read tool but can enumerate a payroll table. **Both deny by default.** A tool with no grant is not offered and not executable; a model type with no grant is not readable. The one deliberate exception is the read-only default baseline at the tool layer: an application that writes no `<baseline>` grants every account `model.read` and `data.read`, so that configuring nothing yields a usable read-only agent rather than an inert one - which still reads nothing until the application writes a `<security-config>`.

There is no security mode, flag or agent setting that answers "who does this run act as" differently: a run that cannot name an authenticated account does not start ([identity](doc:ai/security/identity)).

**Where read scoping happens**, stated here rather than discovered later: the output filter covers **objects and attribute values**, not what a script has already reduced to plain data before the filter runs - a **count**, or a **label** reached through string coercion. Those are bounded one step earlier, by the secured evaluation of the script: `all(...)`, references and the inputs deliver only the objects the acting account may *discover* (the engine's Discover right), so a count or a label discloses nothing about any other object. A label is as visible as its object, which puts the obligation on the modeller to keep confidential information out of the names of objects that accounts may discover without reading them. [Data authorization](doc:ai/security/data-authorization#what-labels-are) has the mechanism and the control (`filterSecurity`).

## Threat model

### Assets

| # | Asset | Why it matters |
|---|---|---|
| A1 | Business data in the `KnowledgeBase` | The primary confidentiality and integrity asset |
| A2 | Application model and configuration | Structural change is a privilege-escalation primitive: a new type, a changed annotation or a rewired agent changes everyone's security posture |
| A3 | Agent, skill and tool definitions | An agent's system prompt, tool set and delegation edges are security policy in disguise |
| A4 | Conversation transcripts | Persisted per `AgentSession`; contain whatever the agent read |
| A5 | Provider credentials and outbound network access | `webFetch`, MCP stdio servers, the Gitea integration |
| A6 | The LLM context window | Everything placed in it is disclosed to the model provider and, through the transcript, to whoever can read the session |

### Actors and trust boundaries

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

The decisive property: **everything that enters the context window is untrusted input, including tool results.** The model is not a security boundary and is never relied on to enforce policy. All gates are deterministic Java checks on the call path, never prompt instructions.

### Threats

| # | Threat | Addressed by |
|---|---|---|
| T1 | **Agent over-privilege.** The agent invokes any tool in its catalog regardless of who is chatting - a read-only clerk drives the model editor. | L1 |
| T2 | **Data over-read.** Data tools return objects the user may not see, and the answer, the transcript and the event stream carry them. Objects and attribute values are filtered per object and per attribute; counts and coerced labels are bounded by the Discover right of the secured evaluation. | L2, L3 |
| T3 | **Prompt-injection-driven tool abuse.** A malicious value in a business object, an MCP tool description or a fetched page instructs the agent to call a write tool or exfiltrate data. | L1, L2, L4; approval for write modes; marking of untrusted content - not mitigated by prompting alone |
| T4 | **Exfiltration through an outbound tool.** The agent is induced to put secret data into a `webFetch` URL, an issue body or a call to an external MCP server. | L2 on the read side (it never gets the data), the `egress` group, the browsing policy and URL provenance |
| T5 | **Unattended over-privilege.** A background run auto-approves every approval-gated tool. | Identity, and the `<background-allowlist>` ceiling |
| T6 | **Unrestricted contexts.** A permissive `SecurityContext` built on a production path. | L0: the permissive constructs are test seams, and no production path builds one |
| T7 | **The MCP bypass.** An unauthenticated `/mcp/*` executing tools and reading resources in a system interaction. | Off by default; every request authenticated to an account, authorized by `<tool-access>`, bounded by the background allowlist, resources read through the `ReadFilter` ([MCP endpoint](doc:ai/security/mcp-endpoint)) |
| T8 | **Escalation through self-modification.** An agent that may author agents, skills and script tools grants itself any tool. | The privileged `agent.authoring` group; delegation cannot widen; tools cannot choose their own group |
| T9 | **Escalation through a self-selected security level.** | There is no level to select, and no production path builds a permissive context |
| T10 | **Leakage through the vector index.** Search returns chunks of indexed objects the user may not read. | L3: query-time filtering per hit; the embedding of unfiltered text is accepted and stated |
| T11 | **Schema disclosure.** The model digest enumerates every module, type and attribute. | L3: the digest emits only the types the principal may read |
| T12 | **A write without authority reaching the commit.** A tool that forgot its check writes through the shared `TransactionScope`. | L4, the pre-commit guard |
| T13 | **Disclosure across sessions.** A transcript keeps what the agent read; a later reader of the session sees data they may no longer be entitled to. | Out of scope, see the non-goals |

### Objectives

- **O1 - Act as the user.** Every run has exactly one authenticated `Person`, resolved once and immutable for the run. No `Person`, no run.
- **O2 - Least privilege on tools.** A user may invoke a tool only if a role they hold is granted that tool. Unknown tools default to denied.
- **O3 - Least privilege on data.** Every model access performed on the user's behalf is checked with `ModelAccessRights` against that `Person`.
- **O4 - Deny by default.** Absence of configuration denies, independently at the tool layer and at the model layer.
- **O5 - Defense in depth.** Filtering the tool list is a UX affordance, never the enforcement point; every layer re-checks, and a tool that skips its own check is still caught before the commit.
- **O6 - Separation of duties.** Who may *use* an agent, who may *define* agents and tools, and who may *write the security policy* are three different privileges.
- **O7 - Auditability.** Every denial and every rolled-back write is logged with run, turn, user, tool and reason.
- **O8 - Explainability.** A denial produces a reason the model can act on ("you lack capability X") and an operator can trace, without disclosing the content that was withheld.

### Non-goals

- **No defense against a malicious model provider.** Data that legitimately reaches the context window is disclosed to the provider by construction.
- **No "scan the answer for secrets".** By the time text is emitted it has been streamed to the browser. Post-hoc redaction of free text protects nothing; the layers pre-filter instead. The one deterministic output check kept is about markup and references ([output guard](doc:ai/security/output-guard-and-audit)).
- **No prompt-injection immunity.** The blast radius is reduced - an injected instruction can only do what the user could do - rather than injection detected.
- **No retroactive re-authorization of transcripts.** The mitigation is that less gets into them.
- **Not the model-security policy itself.** *Which* roles may read *which* types is the application's `<security-config>`; these layers consume it.
- **No credential mechanism of their own for `/mcp/*`.** The endpoint reuses the engine's `Authenticator` framework, so which credential a deployment accepts is that deployment's configuration.

## The layers

```
                       ┌─────────────────────────────────────────────┐
   run starts  ───────▶│ L0  Identity                                │  once per run
                       │     Person required, or the run is refused  │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   build request ─────▶│ L1a Tool RBAC - schema filter (UX)          │  every turn
                       │     ToolPermissionFilter                    │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   tool call  ────────▶│ L1b Tool RBAC - invocation gate (SAFETY)    │  every call
                       │     AgentLoop.executeWithSecurityCheck      │
                       │     + ExecutionMode gate + approval         │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   inside the tool ───▶│ L2  Model access rights                     │  every object
                       │     ctx.modelSecurity().canRead/Write/Create│
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   before serialize ──▶│ L3  Result pre-filter                       │  every result
                       │     ReadFilter + digest scope + index filter│
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   end of turn  ──────▶│ L4  Pre-commit action guard                 │  every commit
                       │     PreCommitGuard @ commitTransaction      │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
   emit answer  ──────▶│ L5  Output guard: HTML allow-list,          │
                       │     cited links checked                     │
                       └───────────────────┬─────────────────────────┘
                                           │
                       ┌───────────────────▼─────────────────────────┐
                       │ L6  Audit log (denials, rollbacks)          │
                       └─────────────────────────────────────────────┘
```

The layers are **independent**: L1 does not assume L2 ran, L4 does not assume L2 ran. That is the point - L4 exists precisely because some tool will forget L2. All types live in `com.top_logic.tl.ai.core.security` unless named otherwise.

| Layer | Article |
|---|---|
| L0 identity | [Identity](doc:ai/security/identity) |
| L1 tool authorization | [Tool authorization](doc:ai/security/tool-authorization), [capability notices](doc:ai/security/capability-notices), [declared types](doc:ai/security/declared-types) |
| L2 data authorization | [Data authorization](doc:ai/security/data-authorization), [object tools](doc:ai/security/object-tools) |
| L3 result filtering | [Result filtering](doc:ai/security/result-filtering) |
| L4 pre-commit guard | [Pre-commit guard](doc:ai/security/pre-commit-guard) |
| L5 output guard, L6 audit | [Output guard and audit](doc:ai/security/output-guard-and-audit) |

## What an application has to configure

| Application | Behaviour |
|---|---|
| No `<security-config>`, no `<tool-access>` | The agent may call the read tools (the default baseline), and model access is deny-all, so it can ask and is told nothing. The state to fix - see the [worked example](doc:ai/security/role-model-worked-example) |
| `<security-config>` present, no `<tool-access>` | The smallest working setup: read tools from the default baseline, data rights per object from the engine |
| Both present | The intended state. A `<tool-access>` that names a role also needs a `<background-allowlist>`, or the application does not boot |
| Uses `/mcp/*` from an external client | Two explicit switches: the module `MCPServerService$Module` (shipped `false`) and an authentication on `MCPServerService` - see [the MCP endpoint](doc:ai/security/mcp-endpoint) |
| Uses `aiAgentChat` from TL-Script | The run acts as the calling account; a thread with no bound account is refused |
| Runs agents unattended (`AgentTask`, `aiAgentChatAsync`) | The run acts as a configured account and is bounded by the `<background-allowlist>` |

Administrators and engine super-users bypass the tool layer and the model rights, and so does any code in a system context - **testing with an admin account proves nothing**. [The security test bed](doc:ai/security/test-bed) of `tl-ai-demo` has personas and a scenario per behaviour of each layer.

The administrator's guides: [tool access configuration](doc:ai/security/tool-access-configuration), [run access](doc:ai/security/run-access) (agents, models, MCP servers), [browsing governance](doc:ai/security/browsing-governance), [the MCP endpoint](doc:ai/security/mcp-endpoint), and the [administration reference](doc:ai/security/administration) for the precise semantics of the policy, approval decided from the call, and the shipped read policy for screenshots and cookie pools.
