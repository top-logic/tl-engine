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

**Or write it in the application:** *Administration -> Berechtigungen -> Agent-Sicherheit ->
Richtlinie* is the editor for exactly this policy - see [Writing the policy: the Richtlinie
tab](#writing-the-policy-the-richtlinie-tab). It stores the same XML, and it refuses to save anything
that would stop the next start.

**Without this block your agent can read, and change nothing.** This check is always applied - there
is no setting that turns it off - and it denies by default. What an application that configures nothing
gets is the **default baseline**: `model.read` and `data.read` for everyone, plus the implicit
`interaction` and `introspection` grants. Enough for a usable, read-only assistant; not enough to
change, script or send anything anywhere. See [The default baseline](#the-default-baseline) below for what is in it and how to
replace it.

**The same holds one layer down, and it is the mistake to expect.** The engine's
`SecurityConfigurationService` is active in *every* TopLogic 8 application whether you configure it or
not, and it instantiates with empty rights - which is deny-all for role-based accounts. So an
application must ship a `<security-config>` as well, or its agent can read no business data at all.
[role-model-worked-example.md](role-model-worked-example.md) builds both tables from scratch and walks
four personas through them.

**Administrators bypass all of it**, so testing with an admin account proves nothing. Log in as a
role-based account.

You do not have to grant `interaction` or `introspection`. Asking the user a question, loading a skill
and delegating to a sub-agent are granted to every role implicitly, because an agent that cannot ask a
question is broken; so is `listTools`, an agent saying which tools and skills it carries, because an
agent that cannot answer that answers it by guessing. Use `<baseline>` only if you want *further* groups
granted to everyone - see below.

---

## What you are granting

A **tool group** is the unit you grant. Every tool declares which group it is in, so you administer
eleven groups rather than eighty tools.

| Group | What it lets the agent do |
|---|---|
| `model.read` | Inspect the application model, layouts and configuration schema. Read-only. |
| `data.read` | Read business data: browse instances, search, semantic search, and answer a question with one read-only query (`query`, `present`). |
| `data.write` | Change business data. Always needs the user's approval as well. In the framework itself: the object tools (`updateObject`, `createObject`, `deleteObject`), `editText`, and **making a change** with `changeData` - see the note under `script`. |
| `agent.meta` | Inspect which agents, skills and tools exist. Read-only. |
| `interaction` | Ask the user a question, load a skill, delegate to a sub-agent. |
| `introspection` | `listTools`: which tools and skills *this agent* carries, and which of them the acting account may use. Not what the application has elsewhere - that is `agent.meta`. |
| `workspace` | The user's own tasks and published pages. |
| `egress` | Send data out of the application: web fetch/search, issue trackers, remote MCP servers. |
| `script` | **Privileged.** Change data with one TL-Script (`changeData`). Alone it reaches only the dry run, which saves nothing: **making** the change requires `data.write` on top. |
| `model.structure` | **Privileged.** Change the model, its annotations and layouts. |
| `agent.authoring` | **Privileged.** Create and rewire agents, skills and tools. |

A **privileged** group is one whose holder can escalate their own rights. Nothing about granting it is
technically special - nothing at all is reached implicitly here - but it is the grant worth arguing
about before you write it.

`egress` is not privileged but deserves the same care: `webFetch` is a read-only tool and the best way
to get data *out* of the application.

> **One rule depends on an argument, not on a tool.** `changeData` is in `script`, and every call of it
> that is not a dry run persists whatever the script changes. Such a call therefore requires `data.write`
> **in addition** to `script`; a dry run (`dryRun=true`) saves nothing and needs `script` alone. The
> decision is the one a `data.write` tool would get - `<deny>` first, then the baseline, then your role
> grants, capped by the `<background-allowlist>` and bypassed by administrators - and the refusal is a
> normal tool error naming `data.write`, which the agent can relay; it is given before the user is asked
> to approve anything. Granting `script` alone therefore lets an account try a change and never make
> one, and a role that needs to *change* data with TL-Script needs both groups. Reading with the query
> language is not in `script` at all: `changeData` refuses a script that only reads, and `query` and
> `present` are in `data.read`.

### The same table, in the raw editor

You do not have to keep this page open while writing a policy. The recommended surface is the
**Richtlinie** tab below; this section describes the raw one, **Administration → Development →
Service Editor → AiToolAccessService**, which renders the stored format directly. There the names a
policy refers to are **selection lists**, not text fields, and hovering a group shows the sentence
from the table above:

| Field | In the editor |
|---|---|
| `<grant group="...">`, `<deny group="...">` | a list of the known groups, each with a tooltip saying what granting it lets an agent do |
| `<tool-groups><group name="...">` | the same list |
| `<role name="...">` | the roles of the application |
| `<grant tools="...">`, `<tool-groups><group tools="...">` | the registered tools, several selectable at once, each with the tool's own description as its tooltip |

The group list holds every group the framework ships, every group a registered tool declares, and every
group the policy in effect already defines - so an application's own group is selectable as soon as one
of its tools declares it. A group that is in none of the three is the case the boot warning below calls
out, and it is nearly always a typo.

The role and tool lists work the same way: what the application has, plus whatever the policy already
names. That second half is what lets a policy mention a role an aspect creates only when it is switched
on, or a tool of an MCP server that is offline right now, without an open-and-save of the editor
dropping the entry.

**The stored format is the technical name throughout.** What is written to the file is the group name,
the role name and the tool names; a policy written by hand parses exactly as one written in the editor,
and the labels exist only in the editor.

### Agent-Sicherheit: the view that explains this policy, and the one that writes it

The raw editor shows the policy as *names*. Three questions it cannot answer - "what am I granting
with `data.read`?", "who may use what?" and "what can this account's agent actually call?" - have
their own place: **Administration → Berechtigungen → Agent-Sicherheit**, with one tab each:

| Tab | Answers | Reads |
|---|---|---|
| Tool-Katalog | which tools exist, what each does, which group it is in | the **saved** policy |
| Richtlinie | who may use what - and this is the tab that **writes** | the **saved** policy |
| Effektive Rechte | what this account's agent may call, and which rule decided | the **running** service |

That split is deliberate and visible: after an apply, the saved policy and the running one differ
until the service is restarted, and the Richtlinie tab says so in a banner with the restart action
next to it.

**Tool-Katalog** is one row per registered tool:

| Column | Answers |
|---|---|
| Tool | the qualified name, i.e. exactly what a `<grant tools="...">` writes and what a denial prints |
| Beschreibung | what the tool does, in one sentence written for an administrator, in your language. A tool nobody could describe - one of a connected MCP server, one authored in the running application - shows the text the *model* is given instead, marked `(Beschreibung für das Modell, EN)` in that row |
| Gruppe | the capability group, with its display name and its technical name; `KEINE GRUPPE` for a tool no grant can reach |
| Hinweis zur Gruppe | privileged / sends data outward / granted implicitly |
| Herkunft der Gruppe | whether the tool declared the group itself or a `<tool-groups>` entry assigned it - which is what explains why removing one entry de-classifies one tool and not the one next to it |
| Erreichbar für | the roles the policy configures that reach the tool, `alle (Grundberechtigung)`, or `niemand` |

Unclassified tools are sorted to the top, because that is the state an administrator has to act on. The
"Erreichbar für" column deliberately never lists administrators - they reach every tool, and saying so
in every row would be noise. Search, the group filter and "only unclassified" are the table's own column
filters: the filter of the *Gruppe* column offers every group plus `KEINE GRUPPE`.

### Writing the policy: the Richtlinie tab

The **Richtlinie** tab is the recommended place to write this policy. It stores exactly the XML this
page describes: a policy written by hand parses unchanged, and opening the tab and applying it
changes nothing.

It has four sub-tabs - **Agenten**, **Modelle**, **MCP-Server** and **Fähigkeiten** - and each is one
matrix: a row per role, a column per name, a cell for what that role says about that name (#404). The
rows are every role of the security structure plus every role the policy names; the columns are
every name the application has plus every name the policy mentions. A role or a name the policy
mentions and the application does not have is shown in red, because the next start would refuse it.
The four sub-tabs edit one draft: *Übernehmen*, *Verwerfen*, *Rückgängig* and *Wiederholen* act on all
four, whichever sub-tab they are clicked on.

| You decide | The tab shows | It stores |
|---|---|---|
| whether a run section names a role | the row state **nicht genannt / eingeschränkt** | no role entry, or `<role name="...">` |
| what a named role may use | a cell: empty, ✓, or ✕ | nothing, a grant, or a deny naming it |
| what a role says about a capability | a cell of the *Fähigkeiten* matrix: empty, ✓, or ✕ | no entry, `<grant group="...">`, or `<deny group="...">` |
| what a run section admits from a role it does not name | the dropdown in the header of the row-state column | `default="..."` of the section |
| single tools rather than a group | the **Einzeltools** column, which opens a dialog | `<grant tools="...">` / `<deny tools="...">` |
| what everybody gets | the first row of *Fähigkeiten*: **Standard / Nichts / Eigene Liste**, and its cells | no `<baseline>`, `<baseline/>`, or `<baseline>` with entries |
| what an unattended run may reach | the second row: **Nicht festgelegt / Alles / Eigene Liste**, and its cells | no element, `all="true"`, or entries |
| which tools are in which group | **Gruppen-Zuordnungen**, below the *Fähigkeiten* matrix | `<tool-groups>` |

**A row of a run section that is not named is locked.** Its cells show, greyed and hatched, what the
section default gives the role (✓ for *Alle*, *A* for *Zugewiesen* / *Agent*, empty for *Keine*), and
a click does nothing: a first click would otherwise restrict the role to that one name without saying
so. Setting the row to *eingeschränkt* writes an empty role entry, which means "may use none", and
makes its cells clickable. **Emptying the last cell keeps the row restricted**, so nothing ever widens
by accident; only the row state returns a role to the section default.

**A click cycles between empty and ✓.** ✕ is a prohibition that beats every permission of every other
role the account holds, so it is not one accidental click away: it is set and lifted through the
cell's context menu or through **Auswahl bearbeiten …**, and a ✕ cell does not react to a click. A
policy written by hand that names the same thing in both lists reads as ✕, because that is what it
enforces, and is left as it is until somebody changes that cell.

The *Fähigkeiten* matrix has no locked rows: an entry that grants and forbids nothing changes nothing,
so the first mark gives a role its entry. Its row state *kein Eintrag* removes the role's entry with
everything in it. `interaction` and `introspection` are not columns: they cannot be withheld by
omission, so there is nothing to decide.

**Auswahl bearbeiten …** grants, forbids or clears chosen columns for every ticked role at once. It
says before anything happens how many cells change, and a role the section does not name is either
restricted first or skipped - whichever the dialog says.

**Effektiv anzeigen** puts on every cell what a run of that role *on its own* would do, computed from
the draft: ⊘ where the run refuses what the cell or the default grants (an infrastructure agent, an
agent whose own model or server the role may not use), and *A* where a model or server is reachable
only because an agent the role may run uses it. The cell's tooltip gives the reason, and for an *A*
the agents that use it. A role across all four dimensions at once, accounts that hold several roles,
and the running service are what the **Effektive Rechte** tab answers.

The role search and the row filter (*nur eingeschränkte*, *nur nicht genannte*) narrow the rows; a
page shows 25 of them. The *Rückgängig* history lasts for the editing session and is forgotten by an
apply, a discard or a restart.

Above the matrix, the tab lists what this policy does that its author probably did not mean - one
sentence per combination, computed from the whole policy: `script` without `data.write` (and with
it), `data.read` together with `egress`, `agent.authoring` at all, a role with data capabilities and
no model access rights, a limit for unattended runs that carries `script`, and an entry for a role
the framework only uses internally. These never block anything.

**Übernehmen checks before it saves.** The toolbar's *Übernehmen* refuses everything that would stop
the next application start, with the sentence the start would log: a role that does not exist, a name
the application does not have, an entry that names neither a group nor a tool, and a missing limit for
unattended runs. The rule is that nothing you can save
here may take down the next start. *Verwerfen*, next to it, throws the unsaved changes away.

**Applying is not yet enforcing.** A successful apply writes
`WEB-INF/autoconf/com.top_logic.tl.ai.core.security.AiToolAccessService.config.xml` and reloads the
application configuration - the same file, with the same `config:override="true"`, that the raw
service editor writes. The running service keeps applying the policy of its last start until it is
restarted, so the tab shows a banner saying so and a **"Dienst jetzt neu starten"** button next to
it. After the restart the banner is gone and the Effektive-Rechte tab agrees with the other two.

> ⚠️ **What you save overwrites the whole policy of the configuration files.** The written file
> replaces - not extends - the `<tool-access>`, `<baseline>`, `<background-allowlist>` and
> `<tool-groups>` of every other configuration file, an aspect's included. From then on, editing
> those files has no effect until you delete the written one. The confirmation of the apply says
> this; it is the price of in-application editing and it is how the engine's service editor has
> always behaved.

**Gruppen-Zuordnungen** is the one place `<tool-groups>` is edited. One row per capability group,
with the tools in it and a *Entfernen* action; *Zuordnung hinzufügen* starts another one. A group
that already has a row is not offered to a second one, because one group has one entry. Removing the
last tool of a row removes the row: an entry naming no tool assigns nothing and would be refused.
After an apply, the catalog's *Herkunft der Gruppe* reports the change.

The basic permissions and the limit for unattended runs both accept **single tools** next to whole
capability groups, because the stored format uses the same `<grant>` entry there as in a role.

**Effektive Rechte** answers the "why". Pick an account - or, hypothetically, a role the policy
configures - and a run mode, and you get one row per capability group with the verdict, the number of
tools behind it, and **the rule that decided together with the rule it overrode**:

```
Gewährt    Daten ändern (data.write)      1    Berechtigung der Rolle projectPartner
Verweigert Daten ändern (data.write)      1    Durch die Hintergrund-Zulassungsliste begrenzt.
                                               Die Rolle projectPartner gewährt es, aber ein
                                               unbeaufsichtigter Lauf darf es nicht.
```

That second sentence is the point of the view: the same row is denied in an unattended run, and the
element to change is the `<background-allowlist>` and not the role. The same distinction is made for a
`<deny>` (it names the grant it beat) and for an administrator account (it names what the bypass covered
up, so that "everything allowed" is not misread as "my policy works"). Expanding a row lists the tools of
the group with their own verdicts, so a per-tool grant or deny is visible where it applies. Tools in no
group sit at the end, on their own, because no capability grant can reach them.

Under the table: the sentence that this view is the *tool* layer only, what an unattended run is, and -
for the roles being shown - the two cross-layer warnings that otherwise only ever appear in the boot log
(a role with data capabilities and no model access rights; a role granted tools that no role rule can
produce on the security root).

Everything shown is computed by the same code the enforcement path runs, so the view cannot claim an
access a real run would be refused. What it does not show is which *data* an allowed call then sees -
see [What this does not decide](#what-this-does-not-decide).

---

## The rules, in one place

- **Nothing is granted unless you grant it**, with two exceptions: `interaction` and `introspection` are
  always granted, and a policy that writes no `<baseline>` at all gets the read-only default one - see
  below.
- **Several roles add up.** A user holding two roles gets the union of their grants.
- **A `<deny>` always wins**, over any grant, including one from the user's other roles and including
  the baseline. That is how you carve one capability out of an otherwise broad grant without
  re-listing everything.
- **A tool in no group is denied to everyone** unless you name it in a `<grant tools="...">`. This is
  what happens to a remote MCP tool, or to a tool whose author forgot to classify it.
- **Nothing is reached by a pattern.** Groups and tools are named one at a time, so a tool that appears
  after you wrote the policy is denied until you name it. Fail-closed, on purpose.
- **`script` does not include writing.** Making a change with `changeData` costs `data.write` as
  well - see the note above. A `<grant tools="...">` naming the tool does not substitute for it: the
  capability is granted with `<grant group="data.write"/>` and nothing else.
- **Roles are matched on the security root, and nowhere else.** A tool call has no object yet when this
  check runs, so "which roles does this account hold" is asked of the application's security root. A
  role that your application's role rules only ever produce **on business objects** therefore matches no
  entry here, however correctly it is spelled: it governs model access and not tool access. A role has
  to be assigned twice to do both jobs - once on the root (a `<singleton-rule>` naming a group), once on
  the objects it should scope. The boot log warns about a role granted tools here that no rule can
  produce on the root, and [role-model-worked-example.md](role-model-worked-example.md) builds both
  assignments in full.
- **Administrators bypass all of it**, exactly as they bypass the model access rights.
- **Unattended runs are additionally capped** by `<background-allowlist>`, which you must declare
  explicitly as soon as you list any role - see below.
- **The MCP endpoint (`/mcp/*`) is off by default, and this file governs it once it is on.** It ships
  with `MCPServerService$Module` set to `false`, so a default deployment answers `/mcp/*` with 503, and
  even with the module on it serves nothing until the application configures an authentication for it.
  A request that *does* authenticate acts as the account its credential names, and its tool calls are
  authorized by this policy exactly like a call from the chat UI - with one difference that matters
  here: **an MCP caller is an unattended run**, because there is no human on the request to answer an
  approval prompt. So the `<background-allowlist>` below is its ceiling too, and a capability a role
  holds but the allowlist withholds is refused over MCP while it works in the chat UI. The endpoint's
  **resource** surface (`toplogic://instance/...` and friends) is not governed by this file - what it
  discloses is decided by the caller's **model access rights** - see
  [mcp-endpoint.md](mcp-endpoint.md) and issue #261.
- **Every agent run needs a logged-in account.** The chat UI and a TL-Script call both act as the
  logged-in account. A run that cannot name one is refused where it starts, not at its first tool call.
  An unattended entry point has to be given a **configured service account** - a real account holding
  real roles - because there is no mode that lets it run without one.

---

## Which agents, models and MCP servers a role may use

The sections above decide what a run may **do**. Three more decide what a run may **be** - which agent
answers, which model it runs on, which MCP servers it reaches. They sit in the same policy and are
written in the same idiom, and until you name a role in one of them nothing changes for anybody.

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

### What each section decides when you have written nothing

| Section | Without any entry | What that means for the chat |
|---------|-------------------|------------------------------|
| `agent-access` | Everybody may run every agent that is not marked `infrastructure` | The `+` menu offers every agent, as it always has |
| `model-access` | A request runs on the model its agent is assigned | The model menu offers that model and nothing else |
| `mcp-access` | A request reaches the servers its agent declares | The MCP list offers those servers and nothing else |

The last two are a tightening: before this, any user could pick any configured model and attach any
configured MCP server. Write `<model-access default="all"/>` to hand the model choice back to
everybody, and `<mcp-access default="all"/>` for the servers.

### What naming a role does

As soon as **one of a user's roles** is named in a section, that user may use exactly what their named
roles permit there - the section's default no longer applies to them, and it still applies to everybody
else. That is what lets you restrict one role in a running application without touching anyone.

- A role entry with **no** `<grants>` lets its holders use none of them.
- A `<deny>` wins over everything, including over what the agent itself uses. Denying a model an
  agent is assigned, or a server it declares, therefore takes that **agent** away from the role: a run
  of it is refused whatever the user picks afterwards, and the chat drops it from the picker rather
  than offering a turn that comes back refused. Deny what a role's own agents use only when you mean
  that.
- What the agent itself uses - the model it is assigned, a server it declares - is always permitted
  unless a `<deny>` names it. A request that echoes the agent's own model back is not choosing a model,
  and every chat client does exactly that on every turn.
- **A `<grant>` is a floor, not a ceiling.** It adds to what the agents of that role already use; it
  cannot take anything away. A role granted only the small model may still run on the large one when
  that is what its agent is assigned, and the only ways to stop that are a `<deny>` naming the large
  model - which takes that agent away from the role, see above - or a different assignment for the
  agent. If you are restricting rather than widening, write the `<deny>`.
- `none` refuses the same requests as `assigned` and `agent` do. What it changes is what a client is
  *offered*: under `none` the menu is empty, so the choice is not shown at all.
- Administrators bypass all three.

### Recipe: one role, one agent, one model, no MCP server

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

A holder of `ticketReader` is offered `TL_ModelExplorer` and nothing else, and may pick the Haiku model
as well as the one its agent is assigned. `internal-docs` is out of reach: an agent that does not
declare it cannot attach it, and an agent that *does* declare it is not offered to this role at all,
because every run of it would reach the denied server. Everybody else keeps every agent.

### Which agents a restricted role needs

Only the agents a user is meant to **talk to**. Two agents the chat runs for the user rather than as
a choice need no grant and cannot be granted meaningfully:

- **The chat entry point.** A client that names no agent runs the entry point if the account may run
  it, and otherwise the first agent that account may run. A restricted role therefore gets its own
  agent, not a refusal naming one it was never offered.
- **The prompt improver.** It rewrites a draft and never answers as a persona, so `<agent-access>`
  does not decide it. The chat hides it from the picker, and an allowlist that had to name it would
  be an allowlist naming something nobody can discover.

Only `<agent-access>` is waived for those two. `<model-access>` and `<mcp-access>` still decide what
they run on, so a `<deny>` on the model the improver is assigned switches the improver off for that
role - the chat then stops offering the "improve prompt" button rather than offering a refusal. If you
want a role to keep it, do not deny the model it runs on.

Everything else an account may run has to be granted.

### Infrastructure agents: not runnable as a persona, whatever this policy says

The prompt improver is the shipped example of an agent the application runs **for** a user instead of
letting them talk to it. An agent definition says so itself:

```xml
<agent name="TL_PromptImprover" infrastructure="true">
    ...
</agent>
```

and an agent record in the knowledge base carries the same flag as its **Infrastructure agent**
checkbox.

The mark is a statement about the agent, not about a role, and `<agent-access>` therefore cannot
lift it:

- no menu offers it - not `/chat/capabilities`, not the delegate catalogue, not `list_agents`, and
  not the options of an agent record's `delegations`;
- a request that names it anyway is answered **403** with *"The agent … cannot be talked to: the
  application runs it for you."* - `/chat/send`, `/chat/regenerate`, a delegation, a
  `spawn_subagent`, a TL-Script `aiAgentChat`, a scheduled run;
- this holds under `<agent-access default="all">`, under a role that explicitly `<grant>`s the agent
  by name, and in an application that wrote no policy at all. There is nothing to grant.

What still works is the product's own call path. The improve-prompt button runs the very same agent,
and what makes that run legal is not which class started it but what the run says about itself: it
is built with `SecurityContext.withoutAgentChoice()`, the one seam that states in code that the
**product**, and not the user, named this agent. An entry point of your own that runs an
infrastructure agent on a user's behalf says the same thing the same way; one that does not say it is
refused like any other caller.

Mark an agent `infrastructure` when it does a piece of work on a user's behalf and its answer is
consumed by the application rather than read as a reply. Do not mark an agent you merely want to hide
from most roles - that is what `<agent-access>` is for, and an `infrastructure` agent is hidden from
everybody including administrators.

### What happens when a request asks for something else

A request that names an agent, a model or an MCP server the account may not use is answered **403**
with the sentence the user reads in the notice strip above the composer, in their language. The run
is refused before the provider is called, so a refusal costs nothing.

The menu is filtered to match: `/chat/capabilities` lists the agents and MCP servers a run of that
account accepts. The model list is the one thing a single payload cannot make exact - it is fetched
once and the agent can change without another round trip - so it lists the models the account may
choose with *some* agent it may run, and marks each with whether the account's roles grant it. A
client narrows that to the agent the user has selected; the shipped chat does.

### Names are checked at boot

An agent, a model or an MCP server this application does not have **refuses the start**, with the name
in the log - unlike an unknown capability group, which is only a warning. These three are closed sets
the application can enumerate, so a name outside one of them is a typo, and a misspelled `<deny>`
refuses nothing while reading like a prohibition.

### Delegation

An agent the account may not run is not reachable by asking another agent for it either: it is dropped
from the delegate catalogue and from `list_agents`, and a `spawn_subagent` naming it is refused. The
same holds for an agent this account may run but whose own model or own server a role denies - it is
dropped from the catalogue and refused when named, because a child run is the acting user's run, and
for an agent marked `infrastructure`, which is nobody's delegate. If you want an orchestrator to
delegate to a specialist, grant the specialist to the same role.

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

### Grant single tools rather than a whole group

```xml
<grant tools="task-tools.createTask, task-tools.listTasks"/>
```

Use the `{factory}.{tool}` name from the application configuration - the same name an agent definition
lists. One entry may name any number of tools; in the editor it is a selection list of the registered
ones. There is **no pattern syntax**: a name matches that tool and nothing that merely starts like it.

An entry says either a group or tools, never both. One that says both, or neither, is a **boot error** -
it would otherwise look like it grants something and not.

### Grant a capability to every account

```xml
<baseline>
    <grant group="model.read"/>
</baseline>
```

`<baseline>` is added to every account's grants, whatever roles it holds. `interaction` and
`introspection` are in it implicitly and need no entry; writing either anyway is legal and changes
nothing. A `<deny>` on a role still wins over the baseline, the two implicit grants included - if you
deny them, you have said so out loud, which is a different thing from never having mentioned them.

### The default baseline

**If you write no `<baseline>` at all, every account gets `model.read` and `data.read`.** That is the
shipped default, and it exists so that an application which has configured nothing has a usable agent
instead of one that can only ask questions. It is read-only on purpose: `data.write`,
`model.structure`, `agent.authoring`, `agent.meta` and `workspace` all change something; `egress` sends
data out of the application; and `script` reaches every type and can persist a write through its own
argument. None of them is in it.

The element has **three** states, and they are all different:

| What you write | What every account gets |
|---|---|
| nothing at all | `model.read` + `data.read`, the read-only default |
| `<baseline/>` | nothing beyond `interaction` and `introspection`. The explicit "no capability for everyone" |
| `<baseline>` with entries | exactly what you list. The default is **replaced**, not added to |

The last row is the one to be sure of: writing `<baseline><grant group="workspace"/></baseline>` gives
everyone `workspace` and **not** the two read capabilities. If you want the default plus something,
list all three.

> **What the default rests on, and where that stops.** A default `data.read` is safe because the second
> layer decides per object what is actually visible - the account's model access rights - and since
> **issue #289** the data tools honour it. `listInstances` lists only the instances the account may read
> and counts only those; `getInstance` omits an attribute the account may not read and answers for an
> object it may not read at all exactly as it answers for one that does not exist; and a TL-Script
> result carries no identifier and no label of an object the account may not read **that is still an
> object when the result is rendered** - see the note below for the two values that are not. An
> application that has written no `<security-config>` has denied everything, so its agent reads nothing.
>
> ⚠️ **Neither a count nor a label is covered.** Per-object read filtering covers **objects and
> attribute values**. It cannot cover a value that is no longer an object by the time the filter runs,
> and TL-Script produces two such values:
>
> - ``all(`myapp:Contract`).size()`` is a **number**, so per-object filtering cannot narrow it. Accepted
>   rather than fixed (**issue #291**).
> - ``toString($contract)`` and ``"" + $contract`` are **strings**: the coercion resolves the object's
>   label through the engine's `MetaLabelProvider`, which is deliberately not access-checked. **This is
>   intended engine behaviour and will not change** (**issue #296**) - object labels are considered
>   non-secret by design, exactly as the user interface shows a referenced object by its label and
>   secures only navigation into it. It puts an obligation on you as the modeller: **do not encode
>   confidential information in the names of objects.** If a contract's name has to be secret, the
>   secret belongs in an attribute with its own `<part>` grant, not in the label.
>
> Two controls, both the caller's: filter *inside* the script with ``filterSecurity(all(...))``, the
> engine's own function for applying the current user's read permissions to a value; or withhold the
> `script` capability from an account that must learn neither - it is not in the default baseline, for
> this reason among others.

### Classify tools that cannot declare their own group

Remote MCP tools and TL-Script tools authored in the running application have no annotation to carry
a group, so they arrive ungrouped and denied. Put them in a group here - and an entry here also
*overrides* a group a tool declared, which is how you move a tool into `egress` that its author did
not classify as such:

```xml
<tool-groups>
    <group name="egress"
        tools="mcp__jira__createIssue, mcp__jira__addComment"
    />
</tool-groups>
```

A name matches **any** of a tool's names - the bare name the model calls, the `{factory}.{tool}` name
written in configuration, and the provider-safe `factory__tool` form - so whichever of the three you
have at hand will do. A TL-Script tool is referenced as `{factory}.{name}`:

```xml
<tool-groups>
    <group name="data.read"
        tools="reporting.listOpenTickets, reporting.countOverdue"
    />
</tool-groups>
```

**This is the mechanism for TL-Script tools.** There is deliberately no way for a tool to declare its
own group in its configuration - see the note below.

Every tool is named, one at a time, and there is deliberately no way to say "everything from this
factory" or "everything from this server". A tool that appears later - one a `createScriptTool` call
adds, one a connected MCP server grows - is therefore denied until you name it. That is the point: a
tool's group says what the tool *does* while the check asks what the *user* holds, so whoever picks the
group picks which capability it costs to call the tool. `createScriptTool` is reachable with
`agent.authoring`, which is not the same as holding `data.write` - and a pattern that swallowed a whole
namespace would hand that choice to whoever can author into it.

Note what an ungrouped tool costs beyond being uncallable: it is also
invisible to the agents whose skills reference it. A skill none of whose tools the acting user may
use is not offered at all, and the drop is logged at INFO with the skill name and the reason.
Classifying the tool is what brings the skill back.

### Cap what an unattended run can do

A background run - a scheduler job, `aiAgentChatAsync`, a detached sub-agent, or a request from an
external MCP client - has no human to answer an approval prompt, so every tool call self-approves.
Approval is the control that makes a write tool safe, so a run that has lost it should not reach one:

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

**A detached subagent is a background run.** `spawn_subagent` is fire-and-forget: it hands the calling
agent a handle, the chat turn ends, and the subagent works on with nobody watching it - so this list
caps it as well. Expect that: a subagent may not be able to do what the same agent does when you talk
to it directly in the chat. The drop is logged at INFO with the subagent's id and the capabilities it
lost, and the subagent's own prompt says which ones and that the reason is the allowlist rather than
the account's roles. If you want subagents to write, put the capability on this list. A *synchronous*
delegation is unaffected - it finishes inside the turn you are watching.

**`script` on this list does not admit writes.** Making a change with `changeData` needs
`data.write`, and the ceiling is applied to that capability exactly as it is to a tool - so a list
carrying `script` without `data.write` lets an unattended run *try* a change in a dry run and refuses
to let it persist, administrators included. Allowlisting `script` used to be the whole of it, which made every
allowlist that carried it silently carry every write TL-Script can express.

**You have to declare it.** As soon as your `<tool-access>` names a role, a policy with no
`<background-allowlist>` element is a **configuration error** and the application refuses to start -
and the Richtlinie tab refuses to save it in the first place:

```
The agent tool access policy names roles but declares no <background-allowlist>. Unattended
agent runs approve every tool call themselves, so the allowlist is what bounds them and it has
to be stated. Write <background-allowlist> with the groups such runs may use, or
<background-allowlist/> to forbid them every tool, or <background-allowlist all="true"/> to
accept unattended runs bounded only by the acting user's grants.
```

A policy with no `<tool-access>` entries at all boots without one: there is nothing you could have
forgotten, because every run of such an application already reaches only `interaction` and
`introspection`.

The allowlist is the *only* thing standing in for the approval prompt an unattended run cannot show,
so its absent state must not be the permissive one - otherwise writing a careful `<tool-access>` matrix
and never learning this element exists would hand unattended runs the user's full grant set,
auto-approved. Silence is therefore not read as a decision in either direction. The three things you
can say are:

| Declaration | Meaning |
|---|---|
| `<background-allowlist>` with `<grant>` entries | Unattended runs reach the intersection of the user's grants and these capabilities. The normal choice. |
| `<background-allowlist/>` | Unattended runs reach **nothing** beyond the always-granted `interaction` and `introspection`. The strictest choice, and correct if you run no schedulers, use no `aiAgentChatAsync` and serve no MCP endpoint. |
| `<background-allowlist all="true"/>` | Unattended runs are bounded only by the acting user's grants. The explicit opt-out - it gives up the compensating control, so it has to be said out loud. |

---

## Answering "why can this account not use this tool?"

Every denial is logged at `WARN` under `com.top_logic.tl.ai.core.security.ToolAuthorization`, naming
the tool, its group, the account, the roles that account holds, and the rule that decided:

```
WARN com.top_logic.tl.ai.core.security.ToolAuthorization - Tool access denied:
tool='secdemo-editing.addTicket' group=data.write user=ben
roles=[SecurityStructure.user, ticketReader]
reason=Permission denied: tool 'framework-wiring.listFrameworkAgents' requires the
'agent.meta' capability, which none of your roles grants.
```

The agent gets the same sentence, so it can tell the user what is missing instead of failing opaquely.
Neither the log nor the message ever contains data that was withheld - tool names, group names,
operations and role names only.

A refusal that was decided for a **capability** rather than for a tool - today only a change made with
`changeData` - names the capability and the operation instead of a tool:

```
WARN com.top_logic.tl.ai.core.security.ToolAuthorization - Tool access denied:
capability='data.write' user=ben roles=[SecurityStructure.user, ticketReader]
reason=Permission denied: changing data with changeData requires the
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
INFO ... AiToolAccessService - Agent tool access: roles=2 baseline=0(+interaction+introspection) backgroundAllowlist=3
```

`baseline=0(+interaction+introspection)` means "a `<baseline>` element is there and it lists no entry,
plus the implicit grants every policy carries". An application that wrote no element at all reads
`baseline=default[model.read, data.read]` instead - the two are different states and the log says
which one you have.

`roles=0` means the service read no grant table. If you wrote one and see this, the `<tool-access>` did
not reach the service - and until it does, every account reaches only the baseline.

**Or ask before it happens:** *Administration → Berechtigungen → Agent-Sicherheit → Effektive Rechte*
answers the same question for any account, in either run mode, without waiting for a denial - see
[Agent-Sicherheit](#agent-sicherheit-the-view-that-explains-this-policy-and-the-one-that-writes-it).

Two things to know when reading the roles in a denial:

- Roles are read **on the security root**, not on any business object: "may this account use the model
  editor at all" is a global question. Which *objects* it may then touch is the separate model-access
  layer.
- **In an application built on `tl-bpe-app`** - the archetype, and what the shipped demo uses - every
  account holds `SecurityStructure.user` there in addition to its own roles: that module ships the
  role, the `user` group and the `<singleton-rule target="SecurityStructure#ROOT">` binding the two,
  so it is a role name you can write a `<tool-access>` entry against and granting it grants every
  account. **An application on `tl-core`/`tl-element` alone does not have it** - those ship only
  `navigation` and `selection` - and a policy row naming a role the application does not have refuses
  to boot (`AiToolAccessService.refusals()`). Check your own archetype before writing a row against it.

---

## What this does not decide

Tool access is one of two independent authorizations. It answers "may this account use this tool"; it
says nothing about *which data* the tool may then touch - that is the engine's `<security-config>`
(model access rights). A role can hold the TL-Script tool and still be denied every object it asks for,
and it can have broad data rights and no tool to reach them with. Configure both.

**Every per-type and per-object guarantee rests on `<security-config>`, not on this file.** This check
is structurally incapable of answering "which object": it runs before there is one. So a role granted
`data.read` here and named in no `Read` grant there has read tools that see nothing - and because a
refused read comes back *empty* rather than as an error, an agent acting for that account may report
"there are no projects" instead of "you may not see them". The boot log warns about exactly that
combination. What it cannot warn about is a filtered **count**: an aggregate computed inside a script
is not read-filtered (**issue #291**), and the control for it is withholding `script`.

Approval is a third, separate thing: a tool classified as changing shared data asks the user to confirm
regardless of which role they hold. There is no way to pre-approve a role, on purpose.

**An agent definition decides nothing about who may use it.** It briefly could name a security mode of
its own; that setting is gone. What an agent definition decides is which tools it *carries*, which
model it is assigned and which MCP servers it declares - and the checks above then decide which of
those the acting account may reach. Who may run the agent at all is `<agent-access>`, which is keyed by
role and lives here, because an agent definition is editable by whoever may author agents.

---

## Trying it out

The demo carries a working example behind one switch: uncomment `secdemo.config.xml` in
`tl-ai-demo/src/main/webapp/WEB-INF/conf/metaConf.txt`. That brings six roles, six accounts, a
`<tool-access>` granting each of them a different set of capabilities, and three TL-Script tool
factories - the tool of two of them named in `<tool-groups>`, the third deliberately not.

**[testbed.md](../../tl-ai-demo/doc/secdemo/testbed.md) is where to start.** It maps every behaviour of this layer to a persona, a
thing to type and an expected result.
