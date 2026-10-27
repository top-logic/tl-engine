---
description: Read when writing a concrete agent tool access policy or diagnosing a denial - recipes for <tool-access> (read-only and write roles, a <deny>, single tools), <baseline> and the default baseline, <tool-groups> for MCP and TL-Script tools, the <background-allowlist> for unattended runs and detached subagents, and reading the ToolAuthorization WARN lines and the boot line of AiToolAccessService.
order: 81
---

# Tool access recipes

## Give one role read-only access, another the write tools

```xml
<tool-access>
    <role name="Clerk">
        <grants>
            <grant group="model.read"/>
            <grant group="data.read"/>
            <grant group="agent.meta"/>
        </grants>
    </role>
    <role name="Manager">
        <grants>
            <grant group="model.read"/>
            <grant group="data.read"/>
            <grant group="data.write"/>
            <grant group="workspace"/>
        </grants>
    </role>
</tool-access>
```

## Take one capability away from a role that otherwise has a lot

```xml
<role name="Contractor">
    <grants>
        <grant group="model.read"/>
        <grant group="data.read"/>
    </grants>
    <denies>
        <deny group="egress"/>
    </denies>
</role>
```

A `<deny>` is worth writing even where deny-by-default already covers it: it survives somebody later granting the group to another role the same person holds.

## Grant single tools rather than a whole group

```xml
<grant tools="task-tools.createTask, task-tools.listTasks"/>
```

Use the `{factory}.{tool}` name from the application configuration - the same name an agent definition lists. One entry may name any number of tools; in the editor it is a selection list of the registered ones. There is **no pattern syntax**: a name matches that tool and nothing that merely starts like it. An entry says either a group or tools, never both; one that says both, or neither, is a **boot error** - it would otherwise look like it grants something and not.

## Grant a capability to every account

```xml
<baseline>
    <grant group="model.read"/>
</baseline>
```

`<baseline>` is added to every account's grants, whatever roles it holds. `interaction` and `introspection` are in it implicitly and need no entry; writing either anyway is legal and changes nothing. A `<deny>` on a role still wins over the baseline, the two implicit grants included.

### The default baseline

**If no `<baseline>` is written at all, every account gets `model.read` and `data.read`.** That default exists so that an application which has configured nothing has a usable agent instead of one that can only ask questions. It is read-only on purpose: `data.write`, `model.structure`, `agent.authoring`, `agent.meta` and `workspace` all change something; `egress` sends data out of the application; and `script` reaches every type and can persist a write through its own argument. None of them is in it.

The element has **three** states:

| What you write | What every account gets |
|---|---|
| nothing at all | `model.read` + `data.read`, the read-only default |
| `<baseline/>` | nothing beyond `interaction` and `introspection` - the explicit "no capability for everyone" |
| `<baseline>` with entries | exactly what is listed. The default is **replaced**, not added to |

Writing `<baseline><grant group="workspace"/></baseline>` gives everyone `workspace` and **not** the two read capabilities; to keep the default plus something, list all three.

> **What the default rests on, and where that stops.** A default `data.read` is safe because the second layer - the account's model access rights - decides per object what is actually visible, and the data tools honour it: `listInstances` lists and counts only the instances the account may read; `getInstance` omits an attribute the account may not read and answers for an object it may not read at all exactly as for one that does not exist; and a TL-Script result carries no identifier and no label of an object the account may not read **that is still an object when the result is rendered**. An application that has written no `<security-config>` has denied everything, so its agent reads nothing.
>
> ⚠️ **A count and a label are bounded by the Discover right, not by the output filter.** ``all(`myapp:Contract`).size()`` is a **number**, so per-object filtering cannot narrow it; ``toString($contract)`` and ``"" + $contract`` are **strings** whose coercion resolves the object's label through the engine's `MetaLabelProvider`, which is not access-checked. What they disclose is bounded earlier: the script runs secured, so `all(...)` and every other object source deliver only the contracts the account may *discover* - and an account may discover an object it holds any right on, even without the Read right. **Do not encode confidential information in the names of objects**; a secret belongs in an attribute with its own `<part>` grant. Filtering *inside* the script with ``filterSecurity(all(...))`` narrows further to what the account may read. See [what labels are](doc:ai/security/data-authorization#what-labels-are).

## Classify tools that cannot declare their own group

Remote MCP tools and TL-Script tools authored in the running application have no annotation to carry a group, so they arrive ungrouped and denied. Put them in a group here - an entry here also *overrides* a group a tool declared, which is how a tool its author did not classify as `egress` is moved there:

```xml
<tool-groups>
    <group name="egress"
        tools="mcp__jira__createIssue, mcp__jira__addComment"
    />
    <group name="data.read"
        tools="reporting.listOpenTickets, reporting.countOverdue"
    />
</tool-groups>
```

A name matches **any** of a tool's names - the bare name the model calls, the `{factory}.{tool}` name written in configuration, and the provider-safe `factory__tool` form. A TL-Script tool is referenced as `{factory}.{name}`. There is deliberately no way for a tool to declare its own group in its configuration, and no way to say "everything from this factory" or "everything from this server": a tool that appears later - one a `createScriptTool` call adds, one a connected MCP server grows - is denied until it is named ([why](doc:ai/security/tool-authorization#why-a-tool-cannot-choose-its-own-group)).

An ungrouped tool is also invisible to the agents whose skills reference it: a skill none of whose tools the acting user may use is not offered at all, and the drop is logged at INFO with the skill name and the reason. Classifying the tool brings the skill back.

## Cap what an unattended run can do

A background run - a scheduler job, `aiAgentChatAsync`, a detached subagent, or a request from an external MCP or A2A client - has no human to answer an approval prompt, so every tool call self-approves. Approval is the control that makes a write tool safe, so a run that has lost it should not reach one:

```xml
<background-allowlist>
    <grant group="model.read"/>
    <grant group="data.read"/>
    <grant group="agent.meta"/>
</background-allowlist>
```

The element is a sibling of `<tool-access>` in the policy. It is a **ceiling**, not a grant: a background run gets the intersection of the user's role grants and this list. It applies to administrators too, because the missing approval prompt is the reason for it, not the user's authority.

**A detached subagent is a background run.** `spawn_subagent` hands the calling agent a handle, the chat turn ends, and the subagent works on with nobody watching it - so this list caps it as well. A subagent may therefore not be able to do what the same agent does when talked to directly in the chat. The drop is logged at INFO with the subagent's id and the capabilities it lost, and the subagent's own prompt says which ones and that the reason is the allowlist rather than the account's roles. For subagents that write, put the capability on this list. A *synchronous* delegation is unaffected - it finishes inside the turn being watched.

**`script` on this list does not admit writes.** Making a change with `changeData` needs `data.write`, and the ceiling applies to that capability exactly as to a tool - so a list carrying `script` without `data.write` lets an unattended run *try* a change in a dry run and refuses to let it persist, administrators included.

**The allowlist has to be declared.** As soon as `<tool-access>` names a role, a policy with no `<background-allowlist>` element is a configuration error and the application refuses to start - and the policy editor refuses to save it:

```
The agent tool access policy names roles but declares no <background-allowlist>. Unattended
agent runs approve every tool call themselves, so the allowlist is what bounds them and it has
to be stated. Write <background-allowlist> with the groups such runs may use, or
<background-allowlist/> to forbid them every tool, or <background-allowlist all="true"/> to
accept unattended runs bounded only by the acting user's grants.
```

A policy with no `<tool-access>` entries at all boots without one: every run of such an application reaches only the baseline. The allowlist is the *only* thing standing in for the approval prompt an unattended run cannot show, so its absent state must not be the permissive one - and silence is not read as a decision in either direction:

| Declaration | Meaning |
|---|---|
| `<background-allowlist>` with `<grant>` entries | Unattended runs reach the intersection of the user's grants and these capabilities. The normal choice. |
| `<background-allowlist/>` | Unattended runs reach **nothing** beyond the always-granted `interaction` and `introspection`. The strictest choice, and correct for an application that runs no schedulers, uses no `aiAgentChatAsync` and serves no MCP or A2A endpoint. |
| `<background-allowlist all="true"/>` | Unattended runs are bounded only by the acting user's grants. The explicit opt-out - it gives up the compensating control, so it has to be said out loud. |

## Why can this account not use this tool?

Every denial is logged at `WARN` under `com.top_logic.tl.ai.core.security.ToolAuthorization`, naming the tool, its group, the account, the roles that account holds, and the rule that decided:

```
WARN com.top_logic.tl.ai.core.security.ToolAuthorization - Tool access denied:
tool='secdemo-editing.addTicket' group=data.write user=ben
roles=[SecurityStructure.user, ticketReader]
reason=Permission denied: tool 'secdemo-editing.addTicket' requires the
'data.write' capability, which none of your roles grants.
```

The agent gets the same sentence, so it can tell the user what is missing instead of failing opaquely. Neither the log nor the message ever contains data that was withheld - tool names, group names, operations and role names only. A refusal decided for a **capability** rather than for a tool - a change made with `changeData` - names the capability and the operation instead of a tool:

```
WARN com.top_logic.tl.ai.core.security.ToolAuthorization - Tool access denied:
capability='data.write' user=ben roles=[SecurityStructure.user, ticketReader]
reason=Permission denied: changing data with changeData requires the
'data.write' capability, which none of your roles grants.
```

An agent whose whole skill was withheld says so too: the generated `### Available Skills` section of its system prompt is followed by the capabilities the acting account does *not* hold, among those the agent's own tools and its skills' tools need. Skills none of whose tools the account may use are left out of that catalog and out of `loadSkill`, and each omission is logged:

```
INFO ... AgentFactory - Agent 'TL_SingleAgent': skill 'TL_taskManagement' is not offered
to the current user - none of its 3 known tools is permitted; they require [workspace]
```

That prompt text is user experience, not a control ([capability notices](doc:ai/security/capability-notices)).

At boot the service states what it read:

```
INFO ... AiToolAccessService - Agent tool access: roles=2 baseline=0(+interaction+introspection) backgroundAllowlist=3
```

`baseline=0(+interaction+introspection)` means "a `<baseline>` element is there and lists no entry, plus the implicit grants every policy carries". An application that wrote no element at all reads `baseline=default[model.read, data.read]` instead. `roles=0` means the service read no grant table; if one was written and this is logged, the `<tool-access>` did not reach the service, and every account reaches only the baseline.

**Or ask before it happens:** the *Effective permissions* tab of the agent security view answers the same question for any account, in either run mode, without waiting for a denial ([the policy editor](doc:ai/security/tool-access-editor#effective-permissions)).

Two things to know when reading the roles in a denial:

- Roles are read **on the security root**, not on any business object: "may this account use the model editor at all" is a global question. Which *objects* it may then touch is the model-access layer.
- **In an application built on `tl-bpe-app`** - an application generated by the archetype with `useBPE=true` (the default for the classic UI), and what the demo uses - every account holds `SecurityStructure.user` on the root in addition to its own roles: that module ships the role, the `user` group and the `<singleton-rule target="SecurityStructure#ROOT">` binding the two, so a `<tool-access>` entry against it grants every account. **An application on `tl-core` / `tl-element` alone does not have it** - those ship only `navigation` and `selection` - and a policy row naming a role the application does not have refuses to boot (`AiToolAccessService.refusals()`).
