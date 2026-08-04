# Configuring which roles may use which agent tools

**Audience:** whoever administers the security roles of a TopLogic application.
**Companion:** [agent-security-architecture.md](agent-security-architecture.md) section 4.1 is the full
specification; this page is the part you need to get started.

---

## The five-line version

```xml
<config service-class="com.top_logic.tl.ai.core.security.AiToolAccessService">
    <instance>
        <tool-access>
            <role name="YourRole">
                <grants>
                    <grant group="model.read"/>
                    <grant group="data.read"/>
                </grants>
            </role>
        </tool-access>
    </instance>
</config>
```

Put that in any application configuration file (an aspect, or your
`WEB-INF/conf/<app>.config.xml`). `YourRole` must be a role that exists - a name that does not is a
**boot error**, deliberately, so that a typo cannot silently hand out nothing.

**Without this block, nothing changes.** Every tool an agent carries stays callable by anyone who can
use that agent, which is how the framework behaved before this layer existed. Writing a non-empty
`<tool-access>` is the whole opt-in.

You do not have to grant `interaction`: asking the user a question, loading a skill and delegating to a
sub-agent are granted to every role implicitly, because an agent that cannot ask a question is broken.
Use `<baseline>` only if you want *further* groups granted to everyone - see below.

---

## What you are granting

A **tool group** is the unit you grant. Every tool declares which group it is in, so you administer ten
groups rather than eighty tools.

| Group | What it lets the agent do |
|---|---|
| `model.read` | Inspect the application model, layouts and configuration schema. Read-only. |
| `data.read` | Read business data: browse instances, search, semantic search. |
| `data.write` | Change business data. Always needs the user's approval as well. In the framework itself this is what **committing** through `executeTLScript` costs - see the note under `script`. |
| `agent.meta` | Inspect which agents, skills and tools exist. Read-only. |
| `interaction` | Ask the user a question, load a skill, delegate to a sub-agent. |
| `workspace` | The user's own tasks and published pages. |
| `egress` | Send data out of the application: web fetch/search, issue trackers, remote MCP servers. |
| `script` | **Privileged.** Execute TL-Script. Read-only by itself: **persisting** what a script changes (`executeTLScript` with `withCommit=true`) requires `data.write` on top. |
| `model.structure` | **Privileged.** Change the model, its annotations and layouts. |
| `agent.authoring` | **Privileged.** Create and rewire agents, skills and tools. |

A **privileged** group is one whose holder can escalate their own rights, so it is never reached by a
wildcard and you have to name it explicitly. Granting one is a deliberate act.

`egress` is not privileged but deserves the same care: `webFetch` is a read-only tool and the best way
to get data *out* of the application.

> **One rule depends on an argument, not on a tool.** `executeTLScript` is in `script`, and calling it
> with `withCommit=true` persists whatever the script changes. That call therefore requires `data.write`
> **in addition** to `script`; the same call with `withCommit` absent or `false`, and
> `validateTLScript`, need `script` alone. The decision is the one a `data.write` tool would get -
> `<deny>` first, then the baseline, then your role grants, capped by the `<background-allowlist>` and
> bypassed by administrators - and the refusal is a normal tool error naming `data.write`, which the
> agent can relay. Granting `script` alone is therefore a read-only grant, and a role that needs to
> *change* data with TL-Script needs both groups.

### The same table, in the editor

You do not have to keep this page open while writing a policy. In **Administration → Development →
Service Editor → AiToolAccessService**, the names a policy refers to are **selection lists**, not text
fields, and hovering a group shows the sentence from the table above:

| Field | In the editor |
|---|---|
| `<grant group="...">`, `<deny group="...">` | a list of the known groups, each with a tooltip saying what granting it lets an agent do |
| `<tool-groups><group name="...">` | the same list |
| `<role name="...">` | the roles of the application |
| `<grant tool="...">`, `<tool name="...">` | **free text**, deliberately: `mcp__jira__*` is the form most entries take and no list of registered names contains it |

The group list holds every group the framework ships, every group a registered tool declares, and every
group the policy in effect already defines - so an application's own group is selectable as soon as one
of its tools declares it. A group that is in none of the three is the case the boot warning below calls
out, and it is nearly always a typo.

The role list works the same way: the roles the application has, plus whatever the policy already names.
That second half is what lets a policy mention a role an aspect creates only when it is switched on,
without an open-and-save of the editor dropping the entry.

**Nothing about the stored format changes.** What is written to the file is the technical group name and
the role name; a policy written by hand parses exactly as before, and the labels exist only in the
editor.

---

## The rules, in one place

- **Nothing is granted unless you grant it**, with one exception: `interaction` is always granted, so a
  role with no entry can still ask a question and nothing else.
- **Several roles add up.** A user holding two roles gets the union of their grants.
- **A `<deny>` always wins**, over any grant, including one from the user's other roles and including
  the baseline. That is how you carve one capability out of an otherwise broad grant without
  re-listing everything.
- **A tool in no group is denied to everyone** unless you name it in a `<grant tool="...">`. This is
  what happens to a remote MCP tool, or to a tool whose author forgot to classify it.
- **`script` does not include writing.** Committing through `executeTLScript` costs `data.write` as
  well - see the note above. A `<grant tool="...">` naming the tool does not substitute for it: the
  capability is granted with `<grant group="data.write"/>` and nothing else.
- **Administrators bypass all of it**, exactly as they bypass the model access rights.
- **Unattended runs are additionally capped** by `<background-allowlist>`, which you must declare
  explicitly once enforcement is on - see below.
- **The MCP endpoint (`/mcp/*`) cannot execute tools while enforcement is on.** It authenticates
  nobody, so a call there has no user to authorize; it is refused rather than run as the system.
  Tracked as issue #261.

---

## Recipes

### Give one role read-only access, another role the write tools

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

### Take one capability away from a role that otherwise has a lot

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

A `<deny>` is worth writing even where deny-by-default already covers it: it survives someone later
granting the group to another role the same person holds.

### Grant a single tool rather than its whole group

```xml
<grant tool="task-tools.createTask"/>
```

Use the `{factory}.{tool}` name from the application configuration - the same name an agent definition
lists. A trailing `*` matches a prefix (`<grant tool="mcp__jira__*"/>`), but a wildcard never reaches a
tool in a privileged group.

### Grant a capability to every account

```xml
<baseline>
    <grant group="model.read"/>
</baseline>
```

`<baseline>` is added to every account's grants, whatever roles it holds. `interaction` is in it
implicitly and needs no entry; writing it anyway is legal and changes nothing. A `<deny>` on a role
still wins over the baseline, the implicit `interaction` included.

### Classify tools that cannot declare their own group

Remote MCP tools and TL-Script tools authored in the running application have no annotation to carry
a group, so they arrive ungrouped and denied. Put them in a group here - and an entry here also
*overrides* a group a tool declared, which is how you move a tool into `egress` that its author did
not classify as such:

```xml
<tool-groups>
    <group name="egress">
        <tools>
            <tool name="mcp__jira__*"/>
        </tools>
    </group>
</tool-groups>
```

A trailing `*` matches a prefix, and it matches **any** of a tool's names - the bare name the model
calls, the `{factory}.{tool}` name written in configuration, and the provider-safe `factory__tool`
form. Since a TL-Script tool is referenced as `{factory}.{name}`, a factory wildcard classifies the
whole factory in one line:

```xml
<tool-groups>
    <group name="data.read">
        <tools>
            <tool name="reporting.*"/>
        </tools>
    </group>
</tool-groups>
```

**This is the mechanism for TL-Script tools.** There is deliberately no way for a tool to declare its
own group in its configuration - see the warning below.

Note what an ungrouped tool costs under enforced access, beyond being uncallable: it is also
invisible to the agents whose skills reference it. A skill none of whose tools the acting user may
use is not offered at all, and the drop is logged at INFO with the skill name and the reason.
Classifying the factory is what brings the skill back.

> **A factory wildcard delegates classification authority for that namespace.** A tool's group is
> supposed to say what the tool *does*, while the check asks what the *user* holds - so whoever
> decides a tool's group decides which capability it costs to call. `createScriptTool` lets its
> caller pick the factory name freely, and holding `agent.authoring` is not the same as holding
> `data.write`. Map `reporting.*` to `data.read`, and anyone who can author into `reporting` can put
> a tool that *writes* under a capability their user only holds for *reading*.
>
> Therefore: map only factories whose contents you control, keep the factories agents author into
> (`custom` by default) out of `<tool-groups>`, and never point a factory wildcard at a privileged
> group (`script`, `model.structure`, `agent.authoring`). Naming individual tools rather than a
> wildcard removes the delegation entirely, at the cost of one line per tool.

### Cap what an unattended run can do

A background run - a scheduler job, or `aiAgentChatAsync` - has no human to answer an approval prompt,
so every tool call self-approves. Approval is the control that makes a write tool safe, so a run that
has lost it should not reach one:

```xml
<background-allowlist>
    <grant group="model.read"/>
    <grant group="data.read"/>
    <grant group="agent.meta"/>
</background-allowlist>
```

This is a **ceiling**, not a grant: a background run gets the intersection of the user's role grants and
this list. It applies to administrators too, because the missing approval prompt is the reason for it,
not the user's authority.

**`script` on this list does not admit writes.** Committing through `executeTLScript` needs
`data.write`, and the ceiling is applied to that capability exactly as it is to a tool - so a list
carrying `script` without `data.write` lets an unattended run *read* with TL-Script and refuses to let
it persist, administrators included. Allowlisting `script` used to be the whole of it, which made every
allowlist that carried it silently carry every write TL-Script can express.

**You have to declare it.** Once your `<tool-access>` turns enforcement on, a policy with no
`<background-allowlist>` element is a **configuration error** and the application refuses to start:

```
Agent tool access is enforced (ENFORCED) but the policy declares no <background-allowlist>.
Unattended agent runs approve every tool call themselves, so the allowlist is what bounds them
and it has to be stated. Write <background-allowlist> with the groups such runs may use, or
<background-allowlist/> to forbid them every tool, or <background-allowlist all="true"/> to
accept unattended runs bounded only by the acting user's grants.
```

The allowlist is the *only* thing standing in for the approval prompt an unattended run cannot show,
so its absent state must not be the permissive one - otherwise writing a careful `<tool-access>` matrix
and never learning this element exists would hand unattended runs the user's full grant set,
auto-approved. Silence is therefore not read as a decision in either direction. The three things you
can say are:

| Declaration | Meaning |
|---|---|
| `<background-allowlist>` with `<grant>` entries | Unattended runs reach the intersection of the user's grants and these capabilities. The normal choice. |
| `<background-allowlist/>` | Unattended runs reach **nothing** beyond the always-granted `interaction`. The strictest choice, and correct if you run no schedulers and use no `aiAgentChatAsync`. |
| `<background-allowlist all="true"/>` | Unattended runs are bounded only by the acting user's grants. The explicit opt-out - it gives up the compensating control, so it has to be said out loud. |

---

## Answering "why can this account not use this tool?"

Every denial is logged at `WARN` under `com.top_logic.tl.ai.core.security.ToolAuthorization`, naming
the tool, its group, the account, the roles that account holds, and the rule that decided:

```
WARN com.top_logic.tl.ai.core.security.ToolAuthorization - Tool access denied:
tool='framework-wiring.listFrameworkAgents' group=agent.meta user=secreader1
roles=[SecurityStructure.user, secReader]
reason=Permission denied: tool 'framework-wiring.listFrameworkAgents' requires the
'agent.meta' capability, which none of your roles grants.
```

The agent gets the same sentence, so it can tell the user what is missing instead of failing opaquely.
Neither the log nor the message ever contains data that was withheld - tool names, group names,
operations and role names only.

A refusal that was decided for a **capability** rather than for a tool - today only the commit path of
`executeTLScript` - names the capability and the operation instead of a tool:

```
WARN com.top_logic.tl.ai.core.security.ToolAuthorization - Tool access denied:
capability='data.write' user=secreader1 roles=[SecurityStructure.user, secReader]
reason=Permission denied: executing TL-Script with withCommit=true requires the
'data.write' capability, which none of your roles grants.
```

An agent whose whole skill was withheld says so too. Under an enforcing policy, the generated
`### Available Skills` section of every agent's system prompt is followed by the capabilities the
acting account does *not* hold, among those the agent's own tools and its skills' tools need - so the
agent can name the missing capability rather than reporting a vague inability. Skills none of whose
tools the account may use are left out of that catalog and out of `loadSkill`, and each omission is
logged:

```
INFO ... AgentFactory - Agent 'TL_SingleAgent': skill 'TL_taskManagement' is not offered
to the current user - none of its 3 known tools is permitted; they require [workspace]
```

That prompt text is **user experience, not a control**: it helps the agent explain itself and is
never what stops a tool from running. The gate is the Java check on the call path, unchanged.

At boot the service states what it read:

```
INFO ... AiToolAccessService - Agent tool access: mode=ENFORCED roles=2 baseline=0(+interaction) backgroundAllowlist=3
```

`baseline=0(+interaction)` means "no `<baseline>` entries were configured, plus the implicit
`interaction` grant every policy carries".

`mode=LEGACY ... (not enforced)` means no policy is in effect. If you wrote one and see this, the
`<tool-access>` is empty or did not reach the service.

Two things to know when reading the roles in a denial:

- Roles are read **on the security root**, not on any business object: "may this account use the model
  editor at all" is a global question. Which *objects* it may then touch is the separate model-access
  layer.
- Every account holds `SecurityStructure.user` there in addition to its own roles. It is a role name
  you can write a `<tool-access>` entry against, and granting it grants every account.

---

## What this does not decide

Tool access is one of two independent authorizations. It answers "may this account use this tool"; it
says nothing about *which data* the tool may then touch - that is the engine's `<security-config>`
(model access rights). A role can hold the TL-Script tool and still be denied every object it asks for,
and it can have broad data rights and no tool to reach them with. Configure both.

Approval is a third, separate thing: a tool classified as changing shared data asks the user to confirm
regardless of which role they hold. There is no way to pre-approve a role, on purpose.

---

## Trying it out

The demo carries a working example behind one switch: uncomment `secdemo.config.xml` in
`tl-ai-demo/src/main/webapp/WEB-INF/conf/metaConf.txt`. That brings six roles, six accounts, a
`<tool-access>` granting each of them a different set of capabilities, and three TL-Script tool
factories - two classified by a factory wildcard, one deliberately not.

**[testbed.md](testbed.md) is where to start.** It maps every behaviour of this layer to a persona, a
thing to type and an expected result.
