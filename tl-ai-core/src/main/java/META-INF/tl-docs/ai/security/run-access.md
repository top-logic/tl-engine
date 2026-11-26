---
description: Read before restricting which agents, models and MCP servers a role may use - <agent-access default="all|none">, <model-access default="assigned|all|none">, <mcp-access default="agent|all|none"> in the AiToolAccessService policy, the five decision rules (RoleAccessPolicy, RunAuthorization), infrastructure agents (infrastructure="true", SecurityContext.withoutAgentChoice), the chat entry point, the chat's capabilities and 403 refusals, delegation, the model of a TL-Script aiChat call (the default model in the place of the model of the agent), the boot check of names (AgentNameCheck), and stored agents and skills under model rights (Read on tl.ai.core:Agent and tl.ai.core:Skill, AgentDefinitionQuery with a reader, the global names).
order: 75
---

# Run access: agents, models and MCP servers

The [tool access policy](doc:ai/security/tool-access-configuration) decides what a run may **do**. Three more sections of the same policy decide what a run may **be** - which agent answers, which model it runs on, which MCP servers it reaches. They are written in the same idiom, and until a role is named in one of them nothing changes for anybody.

```xml
<agent-access default="all">            <!-- all | none -->
    <role name="SalesUser">
        <grants><grant agents="sales-assistant, planner"/></grants>
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

An agent definition does **not** decide who may run it: it is editable by whoever may author agents, so anything it declared about its own security would be inert or an escalation path. An allowlist keyed by role is - and, for an agent stored in the knowledge base, also the model rights on its record ([stored agents and skills](#stored-agents-and-skills-under-model-rights)).

## What each section decides when nothing is written

| Section | Default | Without any entry | Why |
|---|---|---|---|
| `agent-access` | `all` | everybody may run every agent that is not marked `infrastructure`; the chat's `+` menu offers all of them | an agent is a persona, and an application that configured nothing offers all of them |
| `model-access` | `assigned` | a request runs on the model its agent is assigned; the model menu offers that model and nothing else | a model choice is a cost and a capability decision, handed out by an administrator |
| `mcp-access` | `agent` | a request reaches the servers its agent declares; the MCP list offers those and nothing else | attaching a server is egress the administrator did not plan for that turn |

`<model-access default="all"/>` hands the model choice to everybody, `<mcp-access default="all"/>` the servers.

## The rules

Asked about one name a request asks for, against the roles the account holds on the security root:

| # | Rule | Outcome |
|---|---|---|
| 1 | Any held role names it in `<denies>` | Denied, whatever else says |
| 2 | Any held role names it in `<grants>` | Permitted |
| 3 | It is what the agent itself uses - its assigned model, a server it declares | Permitted |
| 4 | Any held role is named in this section | Denied |
| 5 | Otherwise | The section's `default` decides |

Rule 3 keeps the rule set usable: every chat client sends the model it shows with its `send`, which is the agent's own until the user picks another, and a rule that called that a *choice* would refuse every request of a restricted role. Its consequence: **a `<grant>` is a floor, never a ceiling.** It adds to what the role's agents already use and cannot take anything away - a role granted only the small model still runs on the large one when that is what its agent is assigned. Bounding a role downwards is a `<deny>` or a different agent assignment, never a narrower grant.

Rule 4 is the allowlist reading of a role entry, and it is why `default="all"` and a role entry do not contradict each other: naming a role restricts its holders and leaves everybody else where they were, so one role is restricted in a running application without touching anyone. A role entry with no `<grants>` says "this role may use none of them".

A request asks about very little - the chat sends a model and, when the user attached one, a server. So the run asks the two remaining questions on its own behalf before it starts - is the model my agent is assigned denied to this account, is any server my agent declares denied to it - and refuses if either is. Without them rule 1 would answer only when a client happened to name the denied thing. The consequence for whoever writes the policy: **denying what an agent itself uses takes that agent away from the role**, and the chat's capabilities drop it for the same reason - a menu that offered it would offer a turn that comes back refused. Deny what a role's own agents use only when that is meant.

`none` refuses the same requests as `assigned` and `agent` do, because of rule 3; what it changes is what a client is *offered* - under `none` the menu is empty, so the choice is not shown at all. Administrators bypass all three sections, as they bypass the tool layer and the engine's model rights. There is no background ceiling here: which agent a run is and what it runs on is settled before it starts.

## Agents the chat runs for the user

Only the agents a user is meant to **talk to** need a grant. Two agents the chat runs *for* the user are outside `<agent-access>`, because it decides which persona **answers**, and neither of them answers:

- **The chat entry point.** A request that names no agent runs the configured entry point when the account may run it, and otherwise the first agent it may run - `AgentFactory.defaultAgentFor`, which the chat's capabilities report and `ChatProcessor` runs, so the menu and the run cannot name different agents. A restricted role therefore gets its own agent, not a refusal naming one it was never offered. An account that may run none at all is refused with a sentence of its own.
- **The prompt improver**, and every agent a client hides from its picker. Its run is built with `SecurityContext.withoutAgentChoice()`, which drops the agent dimension and nothing else - the model, the MCP servers and every tool it calls stay decided. A `<deny>` on the model the improver is assigned therefore switches it off for that role, and the chat stops offering the "improve prompt" button rather than offering a refusal.

### Infrastructure agents

An agent the application runs **for** a user instead of letting them talk to it is marked as such in its definition, or with the *Infrastructure agent* checkbox of its record in the knowledge base:

```xml
<agent name="TL_PromptImprover" infrastructure="true">
    ...
</agent>
```

The mark is a statement about the agent, not about a role, and `<agent-access>` cannot lift it:

- no menu offers it - not the chat's capabilities, not the delegate catalogue, not `list_agents`, and not the options of an agent record's `delegations`;
- a request that names it anyway is refused with *"The agent … cannot be talked to: the application runs it for you."* - the chat's `send` and `regenerate`, a delegation, a `spawn_subagent`, a TL-Script `aiAgentChat`, a scheduled run;
- this holds under `<agent-access default="all">`, under a role that explicitly grants the agent by name, and in an application that wrote no policy at all.

The product's own call path still works: what makes the improve-prompt run legal is not which class started it but what the run says about itself - it is built with `SecurityContext.withoutAgentChoice()`, the one seam that states in code that the **product**, and not the user, named this agent. An entry point of an application that runs an infrastructure agent on a user's behalf says the same thing the same way. Mark an agent `infrastructure` when it does a piece of work on a user's behalf and its answer is consumed by the application rather than read as a reply; to hide an agent from most roles, use `<agent-access>` - an `infrastructure` agent is hidden from everybody, administrators included.

## `aiChat`: a call without an agent

`aiChat(messages, model, ...)` runs one exchange with a model and has no agent. So the default model of the installation takes the place of the model of the agent: the `default-model` of `AgentRuntimeService`, or the first model of the first available provider when none is configured. The five rules apply with that model in rule 3:

- a call that names no model, or names the default model, runs, unless a role denies the default model (rule 1);
- a call that names another model needs a `<grant>` of a held role (rule 2), or `default="all"` for an account whose roles the section does not name;
- while no default model can be resolved - no `default-model` and no available provider - the call fails with the error of the providers, which names the missing secrets.

The call acts as the account that executed the script. A refused call stops with a `TopLogicException` before any request is sent, and its message is the reason of the policy in the language of the user. Under the shipped default it is *"The model … is not available to you. A call without an agent runs on the default model of the application."* Administrators are not restricted. The failover chain of a permitted model is configuration of the administrator and is not checked against `<model-access>`, as for an agent run.

`TestAIScriptChatModelAccess` and `SecDemoScriptModelAccessTest` assert: the default model needs no grant, with or without its name; another model is refused without a grant and runs with one; a deny of the default model refuses a call without a model name; an administrator is not restricted. Scenario M1 of the [test bed](doc:ai/security/test-bed) shows it in the script console.

## Where it is enforced

`AgentFactory.runAgent(..., RunOverrides)` is the choke point - the chat, `AgentRunner`, the scheduler and the TL-Script functions that run an agent all pass through it - and it authorizes before applying anything of the request, so no provider round trip is spent on a run that may not happen. `aiChat` runs no agent and asks the model section itself, the same way. `RoleAccessPolicy` decides all three sections, and `RunAuthorization` carries the decision on the security context. A refusal is a `ToolSecurityException` carrying a `ResKey`, and for `aiChat` a `TopLogicException` with that reason; the chat's `send` and `regenerate` commands pre-check the same three questions and answer `forbidden` with that text, which the user reads in the notice strip above the composer, in their language.

The chat's capabilities (the state of a `<chat-conversation>`) list the agents and MCP servers a run of that account accepts. The model list is the one thing a single payload cannot make exact - it is written per render, and the agent can change without another round trip - so it lists the models the account may choose with *some* agent it may run, and marks each with whether the account's roles grant it; the shipped chat narrows that to the selected agent.

**Delegation is bounded by the acting principal too**, not only by the parent agent's `delegations` list: `buildAgentLoop` and `buildSubagentLoop` refuse an agent the account may not run, and `AgentCapabilities` drops such an agent from the delegate catalogue and from `list_agents`, so the model does not ask for it. The same holds for an agent whose own model or own server a role denies, and for an `infrastructure` agent, which is nobody's delegate. To let an orchestrator delegate to a specialist, grant the specialist to the same role.

**Names are checked at boot.** An agent, a model or an MCP server the application does not have refuses the start, with the name in the log - unlike an unknown capability group, which is only a warning: these are closed sets the application can enumerate, so a name outside one of them is a typo, and a misspelled `<deny>` refuses nothing while reading like a prohibition. Agents can be configured or records in the knowledge base that the initial data import creates (for example `WEB-INF/data/agents.objects.xml`, which the agent editor's "Apply to development environment" writes), so the agent names are checked after that import, by the service `AgentNameCheck`.

## Recipe: one role, one agent, one model, no MCP server

```xml
<agent-access default="all">
    <role name="ticketReader">
        <grants><grant agents="TL_ModelExplorer"/></grants>
    </role>
</agent-access>
<model-access default="assigned">
    <role name="ticketReader">
        <grants><grant models="claude-haiku-4-5"/></grants>
    </role>
</model-access>
<mcp-access default="agent">
    <role name="ticketReader">
        <denies><deny servers="internal-docs"/></denies>
    </role>
</mcp-access>
```

A holder of `ticketReader` is offered `TL_ModelExplorer` and nothing else, and may pick the Haiku model as well as the one its agent is assigned. `internal-docs` is out of reach: an agent that does not declare it cannot attach it, and an agent that *does* declare it is not offered to this role at all, because every run of it would reach the denied server. Everybody else keeps every agent.

## Stored agents and skills under model rights

Besides the configured agents, an application has stored agents and skills: objects of `tl.ai.core:Agent` and `tl.ai.core:Skill` (or of a specialization) that a person creates at runtime. They are data, so the model rights of the application apply to them in addition to `<agent-access>`. **Both must allow**: `<agent-access>` the name, and the account of the run `Read` on the record. Configured agents are not records and are decided by `<agent-access>` alone.

**Cannot read means unknown.** A stored agent or skill that an account may not read does not exist for that account. The engine applies the rule at each start - the chat, A2A, TL-Script, a scheduled task -, for the default agent, for a delegate and a subagent, for each skill (the skill catalog, `loadSkill`), and in each list: the agent menu of the chat, `list_agents`, the options of `delegations` and `skills` of an agent record, and the authoring tools. The answer is the answer for a name that nothing has: `No agent found`, "not found" in the chat. If a configured agent has the same name, it answers in place of the record.

The rights are those of the run, `SecurityContext.modelSecurity()`. Java code asks `AgentDefinitionQuery` with that reader: `findAgentByName(name, reader)`, `getAllAgents(reader)`, `findSkillByName(name, reader)`, `getAllSkills(reader)`; `AgentFactory.defaultAgentFor` and `runRefusal` take it too. A menu that has only a `Person` uses `new ModelAccessRightsSecurity(person)`, as the default security context does. A check of the configuration, which must see every definition, passes `ModelSecurity.UNCHECKED`, so that each lookup without a check shows at its call. The older methods without a reader are deprecated: they return every definition with no check, and the call does not show it.

**Names are global.** A name is unique in the whole application, also against records an account may not read: a person who creates an agent with the name of a record they do not see is told that the name is already in use. A stored agent cannot take the name of a configured agent, and a stored skill not the name of a configured skill; the case of the letters does not count. Only an administrator, and code in a system context such as the import of the initial data, may save such a name, because a record wins over the configured definition of its name. A name that starts or ends with a blank is refused for everybody. The lookup by name compares like the rule - the case is ignored and nothing else -, also on a database whose collation compares names more widely, for example without accents or trailing blanks.

**An allowlist and a new agent.** Under `<agent-access>` with a `<grants>` list, a role reaches only the names its list gives - an agent that a person creates at runtime has a name that no list names yet, so the holders of that role cannot run it until an administrator adds the name. Recommended: keep `<agent-access default="all">` and list only the forbidden agents in `<denies>`. Then a new stored agent works at once, and its model rights alone decide who uses it.

The access definition that the engine ships for the two types, and recipes for the agents of a team and for personal agents, are in the [administration reference](doc:ai/security/administration#the-shipped-access-definition-for-stored-agents-and-skills).
