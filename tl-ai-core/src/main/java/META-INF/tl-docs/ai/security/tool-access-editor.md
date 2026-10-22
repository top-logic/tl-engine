---
description: Read before editing the agent tool access policy in the running application - the service editor of AiToolAccessService (selection lists for groups, roles and tools) and the Agent security view of the administration with its tabs Tool catalog, Policy (the matrices Agents / Models / MCP servers / Capabilities, row states, ✓ / ✕ cells, Single tools, Group assignments, Apply writes WEB-INF/autoconf and needs a service restart) and Effective permissions.
order: 82
---

# The policy editor

The [tool access policy](doc:ai/security/tool-access-configuration) is configuration, and it can be written in the running application in two places: the raw service editor, and the *Agent security* view of the administration, which is the recommended one. Both store exactly the XML the configuration articles describe: a policy written by hand parses unchanged, and opening an editor and applying changes nothing.

## The service editor

*Administration → Development → Service Editor → AiToolAccessService* renders the stored format directly. The names a policy refers to are **selection lists**, not text fields:

| Field | In the editor |
|---|---|
| `<grant group="...">`, `<deny group="...">` | a list of the known groups, each with a tooltip saying what granting it lets an agent do |
| `<tool-groups><group name="...">` | the same list |
| `<role name="...">` | the roles of the application |
| `<grant tools="...">`, `<tool-groups><group tools="...">` | the registered tools, several selectable at once, each with the tool's own description as its tooltip |

The group list holds every group the framework ships, every group a registered tool declares, and every group the policy in effect already defines - so an application's own group is selectable as soon as one of its tools declares it. A group that is in none of the three is the case the boot warning calls out, and it is nearly always a typo. The role and tool lists work the same way: what the application has, plus whatever the policy already names - so a policy can mention a role an aspect creates only when it is switched on, or a tool of an MCP server that is offline right now, without an open-and-save dropping the entry. What is written to the file is the technical name throughout; the labels exist only in the editor.

## The agent security view

Three questions the raw editor cannot answer - "what am I granting with `data.read`?", "who may use what?" and "what can this account's agent actually call?" - have their own place: the *Agent security* view in the access-rights section of the administration, with one tab each:

| Tab | Answers | Reads |
|---|---|---|
| Tool catalog | which tools exist, what each does, which group it is in | the **saved** policy |
| Policy | who may use what - the tab that **writes** | the **saved** policy |
| Effective permissions | what this account's agent may call, and which rule decided | the **running** service |

After an apply, the saved policy and the running one differ until the service is restarted, and the Policy tab says so in a banner with the restart action next to it.

### Tool catalog

One row per registered tool:

| Column | Answers |
|---|---|
| Tool | the qualified name - exactly what a `<grant tools="...">` writes and what a denial prints |
| Description | what the tool does, in one sentence written for an administrator, in the user's language. A tool nobody could describe - one of a connected MCP server, one authored in the running application - shows the text the *model* is given instead, marked *(Description for the model, EN)* |
| Group | the capability group, with its display name and its technical name; *NO GROUP* for a tool no grant can reach |
| Group note | privileged / sends data outward / granted implicitly |
| Group source | whether the tool declared the group itself or a `<tool-groups>` entry assigned it - which explains why removing one entry de-classifies one tool and not the one next to it |
| Reachable for | the roles the policy configures that reach the tool, *Everyone (baseline)*, or *Nobody* |

Unclassified tools are sorted to the top, because that is the state an administrator has to act on. *Reachable for* never lists administrators - they reach every tool. Search, the group filter and "only unclassified" are the table's own column filters.

### Policy

The Policy tab has four sub-tabs - **Agents**, **Models**, **MCP servers** and **Capabilities** - and each is one matrix: a row per role, a column per name, a cell for what that role says about that name. The rows are every role of the security structure plus every role the policy names; the columns are every name the application has plus every name the policy mentions. A role or a name the policy mentions and the application does not have is shown in red, because the next start would refuse it. The four sub-tabs edit one draft: *Apply*, *Discard*, *Undo* and *Redo* act on all four, whichever sub-tab they are clicked on.

| You decide | The tab shows | It stores |
|---|---|---|
| whether a run section names a role | the row state *Not named* / *Restricted* | no role entry, or `<role name="...">` |
| what a named role may use | a cell: empty, ✓ or ✕ | nothing, a grant, or a deny naming it |
| what a role says about a capability | a cell of the *Capabilities* matrix: empty, ✓ or ✕ | no entry, `<grant group="...">` or `<deny group="...">` |
| what a run section admits from a role it does not name | the dropdown in the header of the row-state column | `default="..."` of the section |
| single tools rather than a group | the *Single tools* column, which opens a dialog | `<grant tools="...">` / `<deny tools="...">` |
| what everybody gets | the first row of *Capabilities*: *Standard* / *Nothing* / *Own list*, and its cells | no `<baseline>`, `<baseline/>`, or `<baseline>` with entries |
| what an unattended run may reach | the second row: *Not stated* / *Everything the roles allow* / *Own list*, and its cells | no element, `all="true"`, or entries |
| which tools are in which group | *Group assignments*, below the *Capabilities* matrix | `<tool-groups>` |

**A row of a run section that is not named is locked.** Its cells show, greyed and hatched, what the section default gives the role (✓ for *all*, *A* for *assigned* / *agent*, empty for *none*), and a click does nothing - a first click would otherwise restrict the role to that one name without saying so. Setting the row to *Restricted* writes an empty role entry, which means "may use none", and makes its cells clickable. **Emptying the last cell keeps the row restricted**, so nothing ever widens by accident; only the row state returns a role to the section default.

**A click cycles between empty and ✓.** ✕ is a prohibition that beats every permission of every other role the account holds, so it is not one accidental click away: it is set and lifted through the cell's context menu or through *Edit selection …*, and a ✕ cell does not react to a click. A policy written by hand that names the same thing in both lists reads as ✕, because that is what it enforces.

The *Capabilities* matrix has no locked rows: an entry that grants and forbids nothing changes nothing, so the first mark gives a role its entry. Its row state *No entry* removes the role's entry with everything in it. `interaction` and `introspection` are not columns: they cannot be withheld by omission.

*Edit selection …* grants, forbids or clears chosen columns for every ticked role at once. It says before anything happens how many cells change, and a role the section does not name is either restricted first or skipped - whichever the dialog says.

*Show effective* puts on every cell what a run of that role *on its own* would do, computed from the draft: ⊘ where the run refuses what the cell or the default grants (an infrastructure agent, an agent whose own model or server the role may not use), and *A* where a model or server is reachable only because an agent the role may run uses it. The cell's tooltip gives the reason, and for an *A* the agents that use it. A role across all four dimensions at once, accounts that hold several roles, and the running service are what the Effective permissions tab answers.

The role search and the row filter (only restricted, only not named) narrow the rows; a page shows 25 of them. The undo history lasts for the editing session and is forgotten by an apply, a discard or a restart.

Above the matrix, the tab lists what this policy does that its author probably did not mean - one sentence per combination, computed from the whole policy: `script` without `data.write` (and with it), `data.read` together with `egress`, `agent.authoring` at all, a role with data capabilities and no model access rights, a limit for unattended runs that carries `script`, and an entry for a role the framework only uses internally. These never block anything.

**Apply checks before it saves.** It refuses everything that would stop the next application start, with the sentence the start would log: a role that does not exist, a name the application does not have, an entry that names neither a group nor a tool, and a missing limit for unattended runs. Nothing that can be saved here can take down the next start. *Discard* throws the unsaved changes away.

**Applying is not yet enforcing.** A successful apply writes `WEB-INF/autoconf/com.top_logic.tl.ai.core.security.AiToolAccessService.config.xml` and reloads the application configuration - the same file, with the same `config:override="true"`, that the raw service editor writes. The running service applies the policy of its last start until it is restarted, so the tab shows a banner saying so and a *Restart the service now* button next to it.

> ⚠️ **What is saved overwrites the whole policy of the configuration files.** The written file replaces - not extends - the `<tool-access>`, `<baseline>`, `<background-allowlist>` and `<tool-groups>` of every other configuration file, an aspect's included. From then on, editing those files has no effect until the written one is deleted. The confirmation of the apply says so; it is how the engine's service editor behaves.

**Group assignments** is the one place `<tool-groups>` is edited: one row per capability group, with the tools in it and a *Remove* action; *Add an assignment* starts another one. A group that already has a row is not offered to a second one. Removing the last tool of a row removes the row, because an entry naming no tool assigns nothing and would be refused. After an apply, the catalog's *Group source* reports the change. The basic permissions and the limit for unattended runs accept **single tools** next to whole capability groups, because the stored format uses the same `<grant>` entry there as in a role.

### Effective permissions

The tab answers the "why". Pick an account - or, hypothetically, a role the policy configures - and a run mode, and get one row per capability group with the verdict, the number of tools behind it, and **the rule that decided together with the rule it overrode**:

```
Granted   Change data (data.write)      1    Grant of the role projectPartner
Denied    Change data (data.write)      1    Capped by the background allowlist.
                                             The role projectPartner grants it, but an
                                             unattended run may not.
```

The second line is the point of the view: the same row is denied in an unattended run, and the element to change is the `<background-allowlist>`, not the role. The same distinction is made for a `<deny>` (it names the grant it beat) and for an administrator account (it names what the bypass covered up, so that "everything allowed" is not misread as "my policy works"). Expanding a row lists the tools of the group with their own verdicts, so a per-tool grant or deny is visible where it applies. Tools in no group sit at the end, because no capability grant can reach them.

Under the table: the sentence that this view is the *tool* layer only, what an unattended run is, and - for the roles shown - the two cross-layer warnings that otherwise only appear in the boot log (a role with data capabilities and no model access rights; a role granted tools that no role rule can produce on the security root). Everything shown is computed by the same code the enforcement path runs, so the view cannot claim an access a real run would be refused. What it does not show is which *data* an allowed call then sees.
