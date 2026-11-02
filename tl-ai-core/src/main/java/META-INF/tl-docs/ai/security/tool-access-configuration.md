---
description: Read before granting roles agent tools - the administrator's guide to the <tool-access> policy of AiToolAccessService (the five-line version, what each tool group lets an agent do, the rules: deny wins, roles matched on the security root, ungrouped tools denied, the default baseline, the <background-allowlist>, MCP callers as unattended runs) and what it does not decide (<security-config>, approval).
order: 80
---

# Tool access configuration

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
        <background-allowlist>
            <grant group="model.read"/>
            <grant group="data.read"/>
        </background-allowlist>
    </instance>
</config>
```

Put it in any application configuration file (an aspect, or `WEB-INF/conf/<app>.config.xml`). `YourRole` must be a role that exists - a name that does not is a **boot error**, deliberately, so that a typo cannot silently hand out nothing. A policy that names a role also declares what unattended runs may reach (`<background-allowlist>`), or it does not boot either - see [capping unattended runs](doc:ai/security/tool-access-recipes#cap-what-an-unattended-run-can-do).

Or write it in the application: the *Policy* tab of the *Agent security* view in the administration is the editor for exactly this policy, stores the same XML, and refuses to save anything that would stop the next start - see [the policy editor](doc:ai/security/tool-access-editor).

**Without this block the agent can read, and change nothing.** The check is always applied - no setting turns it off - and it denies by default. An application that configures nothing gets the **default baseline**: `model.read` and `data.read` for everyone, plus the implicit `interaction` and `introspection` grants - a usable, read-only assistant that cannot change, script or send anything anywhere ([the default baseline](doc:ai/security/tool-access-recipes#the-default-baseline)).

**The same holds one layer down, and it is the mistake to expect.** The engine's `SecurityConfigurationService` is active in every TopLogic 8 application and starts with empty rights - deny-all for role-based accounts. So an application must ship a `<security-config>` as well, or its agent reads no business data at all. The [worked example](doc:ai/security/role-model-worked-example) builds both tables from scratch and walks four personas through them.

**Administrators bypass all of it**, so testing with an admin account proves nothing. Log in as a role-based account.

`interaction` and `introspection` need no grant: asking the user a question, loading a skill and delegating to a subagent are granted to every role implicitly, because an agent that cannot ask a question is broken; so is `listTools`, the agent saying which tools and skills it carries, because an agent that cannot answer that answers it by guessing. Use `<baseline>` only for *further* groups granted to everyone.

## What you are granting

A **tool group** is the unit you grant. Every tool declares which group it is in, so you administer eleven groups rather than eighty tools.

| Group | What it lets the agent do |
|---|---|
| `model.read` | Inspect the application model, layouts and configuration schemas. Read-only. |
| `data.read` | Read business data: browse instances, search, semantic search, and answer a question with one read-only query (`query`, `present`). |
| `data.write` | Change business data; always needs the user's approval as well. The object tools (`updateObject`, `createObject`, `deleteObject`), `editText`, and **making a change** with `changeData` - see the note under `script`. |
| `agent.meta` | Inspect which agents, skills and tools exist. Read-only. |
| `interaction` | Ask the user a question, load a skill, delegate to a subagent. |
| `introspection` | `listTools`: which tools and skills *this agent* carries, and which of them the acting account may use. Not what the application has elsewhere - that is `agent.meta`. |
| `workspace` | The user's own tasks and published pages. |
| `egress` | Send data out of the application: web fetch and search, the browser, issue trackers, remote MCP servers. |
| `script` | **Privileged.** Change data with one TL-Script (`changeData`). Alone it reaches only the dry run, which saves nothing: **making** the change requires `data.write` on top. |
| `model.structure` | **Privileged.** Change the model, its annotations and layouts. |
| `agent.authoring` | **Privileged.** Create and rewire agents, skills and tools. |

A **privileged** group is one whose holder can escalate their own rights. Nothing about granting it is technically special - nothing at all is reached implicitly - but it is the grant worth arguing about before writing it. `egress` is not privileged but deserves the same care: `webFetch` is a read-only tool and the best way to get data *out* of the application. *Where* an agent with `egress` may go is the [browsing policy](doc:ai/security/browsing-governance).

> **One rule depends on an argument, not on a tool.** `changeData` is in `script`, and every call of it that is not a dry run persists whatever the script changes. Such a call therefore requires `data.write` **in addition** to `script`; a dry run (`dryRun=true`) saves nothing and needs `script` alone. The decision is the one a `data.write` tool gets - `<deny>` first, then the baseline, then the role grants, capped by the `<background-allowlist>` and bypassed by administrators - and the refusal is a normal tool error naming `data.write`, given before the user is asked to approve anything. Granting `script` alone lets an account try a change and never make one. Reading with the query language is not in `script` at all: `changeData` refuses a script that only reads, and `query` and `present` are in `data.read`.

## The rules, in one place

- **Nothing is granted unless you grant it**, with two exceptions: `interaction` and `introspection` are always granted, and a policy that writes no `<baseline>` at all gets the read-only default baseline.
- **Several roles add up.** A user holding two roles gets the union of their grants.
- **A `<deny>` always wins**, over any grant, including one from the user's other roles and including the baseline. That is how one capability is carved out of an otherwise broad grant without re-listing everything.
- **A tool in no group is denied to everyone** unless a `<grant tools="...">` names it. This is what happens to a remote MCP tool, a TL-Script tool, or a tool whose author forgot to classify it - see [classifying tools](doc:ai/security/tool-access-recipes#classify-tools-that-cannot-declare-their-own-group).
- **Nothing is reached by a pattern.** Groups and tools are named one at a time, so a tool that appears after the policy was written is denied until it is named. Fail-closed, on purpose.
- **`script` does not include writing.** Making a change with `changeData` costs `data.write` as well; a `<grant tools="...">` naming the tool does not substitute for it.
- **Roles are matched on the security root, and nowhere else.** A tool call has no object yet when this check runs, so "which roles does this account hold" is asked of the application's security root. A role that the application's role rules only ever produce **on business objects** matches no entry here, however correctly it is spelled: it governs model access and not tool access. A role has to be assigned twice to do both jobs - once on the root (a `<singleton-rule>` naming a group), once on the objects it should scope. The boot log warns about a role granted tools that no rule can produce on the root.
- **Administrators bypass all of it**, exactly as they bypass the model access rights.
- **Unattended runs are additionally capped** by `<background-allowlist>`, which must be declared explicitly as soon as any role is listed.
- **An MCP caller is an unattended run.** The MCP endpoint (`/mcp/*`) is off by default and serves nothing until the application configures an authentication. A request that authenticates acts as the account its credential names, and its tool calls are authorized by this policy exactly like a call from the chat - but there is no human on the request to answer an approval prompt, so the `<background-allowlist>` is its ceiling too, and a capability a role holds but the allowlist withholds is refused over MCP while it works in the chat. The endpoint's **resource** surface is not governed by this policy; what it discloses is decided by the caller's model access rights - see [the MCP endpoint](doc:ai/security/mcp-endpoint).
- **Every agent run needs a logged-in account.** The chat and a TL-Script call both act as the logged-in account; a run that cannot name one is refused where it starts. An unattended entry point acts as a configured account - a real account holding real roles.

## What this does not decide

Tool access is one of two independent authorizations. It answers "may this account use this tool"; it says nothing about *which data* the tool may then touch - that is the engine's `<security-config>`. A role can hold the TL-Script tool and still be denied every object it asks for, and it can have broad data rights and no tool to reach them with. Configure both.

**Every per-type and per-object guarantee rests on `<security-config>`, not on this policy.** This check is structurally incapable of answering "which object": it runs before there is one. So a role granted `data.read` here and named in no `Read` grant there has read tools that see nothing - and because a refused read comes back *empty* rather than as an error, an agent acting for that account may report "there are no projects" instead of "you may not see them". The boot log warns about exactly that combination. What it cannot warn about is a **count** computed inside a script, which is not read-filtered.

**Approval** is a third, separate thing: a tool classified as changing shared data asks the user to confirm regardless of which role they hold. No role is pre-approved.

**An agent definition decides nothing about who may use it.** It decides which tools it *carries*, which model it is assigned and which MCP servers it declares; the checks above decide which of those the acting account may reach, and who may run the agent at all is [run access](doc:ai/security/run-access), keyed by role.

## Trying it out

The demo carries a working example behind one switch: uncomment `secdemo.config.xml` in `tl-ai-demo/src/main/webapp/WEB-INF/conf/metaConf.txt`. That brings two roles, four persona accounts holding them in different combinations, a `<tool-access>` granting the two roles different sets of capabilities, and three TL-Script tool factories - the tools of two of them named in `<tool-groups>`, the third deliberately not. [The test bed](doc:ai/security/test-bed) maps every behaviour of this layer to a persona, a thing to type and an expected result.

Further: [recipes and diagnosis](doc:ai/security/tool-access-recipes), [the policy editor](doc:ai/security/tool-access-editor), the precise [administration reference](doc:ai/security/administration#the-tool-access-policy).
