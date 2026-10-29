---
description: Read before verifying agent security by hand or adding a case to the security test bed of tl-ai-demo (secdemo.config.xml, the pm model, SecDemoSetup) - enabling and resetting the fixture, the personas anna, ben, cara and dora, the four harness scripts (probe, fire, collect, reset) for the script console, the scenario tables per layer (tool access T1-T10, read filtering D1-D11, capability notices C1-C8, background allowlist A1-A5, pre-commit guard, object tools, closed doors P1-P2) and the SecDemo*Test classes asserting each.
order: 6
---

# The security test bed

`tl-ai-demo` carries a small projects-and-tickets application in which agent security is fully configured: the [worked example](doc:ai/security/role-model-worked-example), built. Find the behaviour in the [scenario tables](#the-scenarios), log in as the persona of that row, do the one thing the row says and compare with the last column. Every scenario is about the agent layers - which tools an account may invoke, what a permitted tool discloses, what the agent is told about what it cannot do, what an unattended run may do, what the pre-commit guard catches. The engine's model security is the substrate; the worked example explains it persona by persona.

## The fixture

| Element | Value |
|---|---|
| model | module `pm`: `pm:Project {name, partners, tickets}`, `pm:Ticket {title, body}` (`WEB-INF/model/pm.model.xml`) |
| seed data | two projects (Alpha, Beta), three tickets |
| roles | `projectPartner`, `ticketReader` - both appear in both grant tables |
| rules | four declarative role rules and one security-parent rule; nothing assigned from Java |
| accounts | `anna`, `ben`, `cara`, `dora`, password `Sec-Demo-1` |
| tools | the TL-Script tools `secdemo-reporting.countTickets`, `secdemo-reporting.countVisibleTickets`, `secdemo-editing.addTicket`, `secdemo-unclassified.countTicketsUnclassified`; the Java tools `secdemo-projects.listProjects` and `listProjectsUndeclared` ([declared types](doc:ai/security/declared-types)) |
| visible UI | the chat tab and *Administration → Development → Script console*, the same for every persona |
| baseline | an explicit, empty `<baseline/>`: without the element the shipped read-only default (`model.read`, `data.read`) would apply and make the L1 refusals unobservable |
| `use-default-security-parent` | not configured, so the engine default `false` applies |

Everything is in `tl-ai-demo/src/main/webapp/WEB-INF/conf/secdemo.config.xml`; `SecDemoSetup` (startup hook `secdemo`) seeds accounts, projects and view access and checks after boot that the rules produced the roles the matrix expects. The console scripts are in `tl-ai-demo/src/test/secdemo/harness/`, the tests in `tl-ai-demo/src/test/java/com/top_logic/ai/demo/SecDemo*Test.java`.

### Which layer you are looking at

A confusing observation is almost always the wrong layer. The first three must allow before anything happens; the fourth is asked again at the end of the turn.

| Layer | Configured by | Decides | Symptom when it denies |
|---|---|---|---|
| component access | `<component-access>` of `SecDemoSetup` | whether a tab opens | "No tabs to choose from." |
| tool access (L1) | `<tool-access>` | whether this account may invoke this tool | `Permission denied: tool '...' requires the '...' capability` |
| model access (L2) | `<security-config>` plus the role rules | whether this account may read or write this object | an empty result, `totalCount` 0, `Object with TID '...' not found` for an object that exists, an absent attribute, `CREATE_PERMISSION_DENIED` |
| pre-commit guard (L4) | the same `<security-config>`, read at the end of the turn | whether the turn's changes may be persisted | the changes are gone; `The data changes of this turn were rolled back` |

L1 cannot see objects, so Anna and Dora are indistinguishable at L1 and opposite at L2 - one partners on a project, the other on nothing. Role resolution walks the touched object and its security ancestors, never its children: with `use-default-security-parent="true"` a role on the root would be held on everything (`SecDemoRoleRuleTest.testTheTierDoesNotBecomeAScope` fails then), and a role held on the root grants no model access at all, so "a ticket reader may read all tickets" needs a rule putting the role on every ticket.

**Component access** is a mechanism of its own: model rights say nothing about which views an account may open, and the demo grants no view to anybody (it has no `WEB-INF/conf/security.xml`). The aspect grants the `Read` command group on `tl-chat/chat/tabLayout.layout.xml#TabLayout`, `tl-chat/chat/agent.layout.xml#agent`, `admin/index.layout.xml#adminView`, `admin/studio/index.layout.xml#studioView` and `com.top_logic.model.search/admin/scriptConsole.layout.xml#view` - every nested `securityLayout` needs its own entry. The format is that of `security.xml`; the profile name `*` stands for every role a `<persona>` is expected to hold. `SecDemoSetup` applies it additively, after the engine's `access-configuration` hook, because the engine's import of `security.xml` resets the access of every view; unknown roles, views or command groups fail the boot.

**The console grant deliberately lets a non-administrator role execute arbitrary TL-Script**, to show that the tool policy and the model rights hold even then. Only `Read` is granted, so *execute and save changes* (the `Write` command group) stays disabled for the personas. Do not copy the grant into a real application: a console user can call `aiCallTool(...)`, gated by `<tool-access>`, and a role holding `script` expresses any data operation the model rights allow.

## Enabling and resetting

1. Uncomment `secdemo.config.xml` in `tl-ai-demo/src/main/webapp/WEB-INF/conf/metaConf.txt` and comment out `research.config.xml`, whose background allowlist is a second one.
2. Build and start the demo (the `tl-app` skill, or `mvn -B` in `tl-ai-demo`); the log ends with `***** up and running *****`.
3. Check two lines of `tl-ai-demo/tmp/app-data/logs/top-logic.tl-ai.log`:

   ```
   SecDemoSetup - Security demo: checked the roles of 4 personas against the aspect's role rules.
   AiToolAccessService - Agent tool access: roles=2 baseline=0(+interaction+introspection) backgroundAllowlist=3
   ```

   A warning above the first line names a persona and a role a rule failed to produce; every scenario of that persona is then wrong in a way that looks like a policy problem. On the first boot of a fresh database the line comes at the end of the startup, after the startup transaction committed. In the second line, `baseline=default[model.read, data.read]` means the `<baseline/>` did not reach the service and `roles=0` that the aspect did not reach the application ([diagnosis](doc:ai/security/tool-access-recipes)).
4. Between scenarios run `reset.tlscript` as `root` with **execute and save changes**: it deletes the tickets a scenario created and restores missing seed tickets. A second run must report `none` on both lines.

To turn it off, comment the line out again **and delete `tl-ai-demo/tmp/h2.mv.db`**: withdrawing the `pm` model makes the model upgrade delete the module, which fails while instances exist. To keep the database, delete the instances as `root` first (``all(`pm:Ticket`).concat(all(`pm:Project`)).foreach(x -> $x.delete())``, tickets first) and restart after disabling.

## The personas

| Login | Roles, and where | Tool capabilities | Here to demonstrate |
|---|---|---|---|
| `anna` | `projectPartner` on the root (tier) and on Alpha (scope) | `model.read`, `data.read`, `data.write`, `script` | tier and scope as two assignments: write tools everywhere, one project to use them on |
| `ben` | `ticketReader` on the root and on every ticket | `model.read`, `data.read`, `script` | the rows L1 decides: a ticket creation refused for want of `data.write` |
| `cara` | both roles - `projectPartner` on the root and on Beta, `ticketReader` on the root and every ticket | the union | roles add up, and reading is not writing |
| `dora` | `projectPartner` on the root only | as `anna` | the failure mode: write tools and nothing to write; reads come back empty rather than denied |
| `root` | administrator | bypasses every layer | the baseline, nothing else |

**Running anything as `root` proves nothing**: administrators bypass the tool layer and the model rights, so every deny scenario passes vacuously; `probe.tlscript` and `fire.tlscript` warn when they detect it. `model.structure`, `agent.meta`, `agent.authoring`, `workspace` and `egress` are granted to no role - deny by default, no `<deny>` needed. `data.write` is the only discriminating tool capability; everything else that differs between two personas differs at L2. Shapes four personas cannot show - a partially denied skill, a persona holding no capability, a delegate dropped as wholly unreachable, a spawn refused before the child exists - are asserted in `tl-ai-core` against hand-built policies.

To add a persona: a `<persona>` entry naming its groups and expected roles, its login in a `<project partners="...">` if it should scope something, a row in the table above and one in `SecDemoToolAccessTest`. The seeded password is `initial-password` of `SecDemoSetup` (it must satisfy the password policy and is applied only when the account is created).

## The harness scripts

Paste a script into *Administration → Development → Script console* and press the plain **execute** button. Each returns one multi-line string, so the output of two personas can be diffed.

| Script (`tl-ai-demo/src/test/secdemo/harness/`) | What it is for | Needs a key |
|---|---|---|
| `probe.tlscript` | the tool layer: one `ALLOWED` / `DENIED` line per capability for whoever is logged in | no |
| `fire.tlscript` | background agent runs: one unattended run per case naming the logged-in account | yes |
| `collect.tlscript` | reads back the runs `fire.tlscript` put into the session - only those | yes |
| `reset.tlscript` | puts the seed data back; as `root`, **execute and save changes** | no |

Fire and collect are two executions in the same session ([asynchronous runs](doc:ai/script-and-scheduled-runs#asynchronous-runs)). `fire.tlscript` uses `aiAgentChatAsync`, which runs as the account that executed the script, marked as a background run: every tool call is auto-approved, and the `<background-allowlist>` bounds the run. `SecDemoHarnessScriptsTest` checks that every script still compiles against the model and the registered functions.

**No persona can commit from the console**: plain **execute** evaluates inside `KnowledgeBase.withoutModifications(...)`, and *execute and save changes* is disabled for them. A write scenario therefore ends in one of three errors, and which one is the observation:

| The layer that answered | The message |
|---|---|
| L1, tool access | `Permission denied: tool '...' requires the 'data.write' capability, which none of your roles grants.` |
| L2, model access | `Object with TID '...' not found` - the object exists; this account may not read it |
| the console's read-only mode | `Illegal modification access. Currently no changes are allowed.` - L1 and L2 both allowed |

To watch a write land, use `root` with *execute and save changes* (T4), or the chat.

## The scenarios

*probe* / *console* need no key; *chat* (the chat tab) and *fire+collect* need one - the demo reads it from `tl_anthropic_api_key` (`x-api-key header is required` in a run's errors means it is missing). Judge a chat row by whether the tool layer emitted the message of its guarantee column and whether a denied tool was never invoked, never by the model's wording.

### Tool access - the invocation gate

T2 to T6 are one call, `aiCallTool("secdemo-editing.addTicket", {"project": "<a project's TID>", "title": "x"})`, by different personas with different TIDs.

| # | Behaviour | Persona | Do this | You should see |
|---|---|---|---|---|
| T1 | the whole matrix | any | run `probe.tlscript`, again as a second persona, diff | one line per capability; both roles hold every read capability, so what differs between Anna and Dora is what the allowed calls return |
| T2 | the row L1 decides | `ben` | the call, Alpha's TID | `Permission denied: ... requires the 'data.write' capability ...`; the same with Beta's TID, because L1 runs before the object is resolved |
| T3 | a persona holding the capability | `anna` | the call, Alpha's TID | `Illegal modification access` - L1 and L2 allowed, the console stopped her |
| T4 | the write, committed | `root` | the call with *execute and save changes* | the ticket exists; run `reset.tlscript` afterwards |
| T5 | a partner is not a partner of everything | `anna`, then `dora` | the call, Beta's TID | `Object with TID '...' not found` - missing, not forbidden; for Dora both projects |
| T6 | roles add up | `cara` | the call, Beta's TID | `Illegal modification access`; with Alpha's TID `not found` |
| T7 | a group granted to nobody | any | `aiCallTool("model-modification.createClass", null)` | `requires the 'model.structure' capability`; likewise `task.createTask` (`workspace`), `web-fetch.webFetch` (`egress`), `agent-authoring.createAgent` (`agent.authoring`) |
| T8 | an unclassified tool and its classified twin | any | `aiCallTool("secdemo-unclassified.countTicketsUnclassified", null)`, then `aiCallTool("secdemo-reporting.countTickets", null)` | `belongs to no tool group and has no explicit grant, so it is denied to everyone ...`, then `3`; the difference is one `<tool-groups>` entry, and classifying is not granting (T2) |
| T9 | a name nothing registers | any | `aiCallTool("model-instances.listEverything", null)` | `Tool '...' not found. Available tools: ...` - the whole registry, since `aiCallTool` has no agent tool list (in the chat see C3) |
| T10 | changing data with a script costs `data.write` on top of `script` | `ben`, then `anna` | ``aiCallTool("script-exec.changeData", {"script": "new(`pm:Ticket`)..set(`pm:Ticket#title`, \"x\")"})`` | Ben: `changing data with changeData requires the 'data.write' capability ...`; Anna is stopped by the console's read-only mode; with `"dryRun": true` both get the dry run's report |

A TL-Script tool carries no annotation, so it is ungrouped and denied to everyone until a `<tool-groups>` entry names it (`SecDemoScriptToolClassificationTest`). These are console rows because no framework agent carries the fixture's script tools (C4).

### Read filtering - what a permitted tool discloses

Every conversion of a model object into something the agent sees goes through one filter taking the run's rights ([result filtering](doc:ai/security/result-filtering)): an unreadable object is absent, an unreadable attribute is absent from the object. Every row is a pair, because an empty result alone is also what a broken tool returns.

| # | Behaviour | Persona | Do this (console) | You should see |
|---|---|---|---|---|
| D1 | a listing and its count are filtered per object | `anna`, then `ben` | `aiCallTool("model-instances.listInstances", {"moduleName": "pm", "typeName": "Project"})` | Anna `"totalCount":1`; Ben `"totalCount":0`, `"instances":[]` |
| D2 | an unreadable object is reported missing | `anna`, then `cara` | `aiCallTool("model-instances.getInstance", {"tids": ["<a Beta ticket's TID>"]})` | Anna: `Object with TID '...' not found`, as for a TID naming nothing; Cara: the ticket |
| D3 | an unreadable attribute is omitted | `ben`, then `anna` | the same with an Alpha ticket's TID | Ben: `title` and no `body` key; Anna: `body` too - the `<part name="pm:Ticket#body">` grant |
| D4 | a query result carries no hidden identity | `dora`, then `anna` | ``aiCallTool("model-query.query", {"expression": "all(`pm:Project`)"})`` | Dora `(empty list)`; Anna `Alpha [type:pm:Project] [id:...]` |
| D5 | a single hidden object is `null` | `dora` | the same with ``all(`pm:Project`).firstElement()`` | `null` |
| D6 | an aggregate is not filtered | `dora` | `secdemo-reporting.countTickets`, then `countVisibleTickets` | `3`, then `0` - a count is a number before any filter sees it; the second tool filters inside the script |
| D7 | a string coercion returns a label | `dora` | ``... "all(`pm:Project`).map(p -> toString($p))"`` | `Alpha`, `Beta` - labels are not access-checked (`MetaLabelProvider`) |
| D8 | the control | `dora`, then `anna` | ``... "filterSecurity(all(`pm:Project`)).map(p -> toString($p))"`` | Dora `(empty list)`; Anna `Alpha` |
| D9 | the model digest names only readable types | `ben`, then `anna` | `aiCallTool("model-browser.outlineModel", {"includeFramework": false})` | Ben: `class Ticket` without `class Project`; Anna: both |
| D10 | a module without a readable type is absent | any | the same call | no `## demo.events` - its model has no `<security-config>` grant |
| D11 | structure is not instances | `dora` | the same call, then the D1 listing | `class Project` in the digest, `"totalCount":0` in the listing |

D6 to D8 are the boundary of the guarantee: per-object filtering covers objects and attribute values, not a **count** or a **label**, both plain values before any filter runs. Learning how many tickets exist or what a project is called is expected; learning a ticket's `body` would be a finding. Object labels are not secret by design, so a modeller must not encode confidential information in object names. The controls are the caller's: `filterSecurity` inside the script, or withholding `script`. The digest is bounded by `accessibleTypes(READ)` at the security root: an emitted type is written out in full, attribute-level grants are not reflected (Ben is told about `pm:Ticket#body`; D3 decides what he reads), and Cara, who holds `projectPartner` on no root, is not told about `pm:Project` although she may read an instance - the error is towards disclosing less. The vector index drops a hit whose source object the persona may not read, with its text; the fixture ships no index, so `SecDemoVectorIndexTest` asserts it with chunks shaped as the indexer writes them.

### What the agent is told - skills, delegates, notices

This is user experience, not a control: why the agent can say "you lack `workspace`" instead of "I can't do that". A row fails when the tool layer asserts something untrue, never because the model paraphrased. All are chat rows except C7.

| # | Behaviour | Persona | Do this | The tool layer must | The reply should |
|---|---|---|---|---|---|
| C1 | a fully denied skill is dropped and explained | any | "Load the TL_taskManagement skill and create a task." | answer that the skill is carried by this agent but withheld from the current user, name `workspace`, and end with "These capabilities explain only this skill. ..." (`AgentCapabilities.NO_GUESSING`) | relay that only an administrator granting `workspace` changes it |
| C2 | the prompt names the missing capabilities | any | "What can you not do for me, and what capability is missing?" | put `### Capability Limits` into the system prompt with exactly the capabilities the roles do not reach | name `workspace`, `agent.meta`, `agent.authoring`, `model.structure`, `egress` - and, as `ben`, `data.write` |
| C3 | a hallucinated tool name is no permission problem | any | "Call the tool listEverythingEverywhere." | answer `listTools` with this agent's catalog and the note that an absent name is no permission problem; a call anyway gets `Unknown tool: '...'` with only the tools this persona may call; name no capability | say the tool is not in this agent's toolset |
| C4 | a tool that exists but no agent carries | `anna` | "Use secdemo-editing.addTicket to add a ticket." | the same; a call anyway is answered that the tool exists in the application but is not carried by this agent (`ToolCatalog.absenceReason`); name no capability | say an administrator would have to add it |
| C5 | a persona really denied it still hears why | `ben` | the same prompt | `Permission denied: ... requires the 'data.write' capability` | name `data.write`; with C4: one tool, two personas, two true answers |
| C6 | a misspelled skill id discloses nothing | any | "Load the TL_agenticAuthoringg skill." | `Skill '...' is not available for this agent. Available: [...]`, no capability | offer a real skill id |
| C7 | a delegate says what it cannot do | any | console: `aiCallTool("subagent.list_agents", null)` | list every agent not wholly out of reach, each with what it cannot use | `TL_ScriptWriter` with `"cannotUse":["workspace"]`, `TL_ModelExplorer` with none |
| C8 | an agent says what it carries | any | "Which tools do you have, and which of them can I not use?" | answer `listTools` - implicitly granted (`introspection`), so it reaches every persona although the baseline is empty - with every skill marked `[loaded]` / `[loadable]` / `[withheld]` and the capability behind each withheld one | name the five capabilities behind what it cannot |

`listTools` answers only this agent's catalog; whether the application has a tool of some name is a separate question, answered by `TL_agenticAuthoring`, which these personas cannot load. `SecDemoCapabilityNoticeTest`, `SecDemoSkillAbsenceTest`, `SecDemoListToolsTest` and `SecDemoSchemaFilterTest` assert the guarantees without a model, including that a denied tool's schema never reaches the provider request ([capability notices](doc:ai/security/capability-notices)).

### Unattended runs - the background allowlist

A run with nobody to answer an approval prompt auto-approves every call, so it is capped by `<background-allowlist>` (`model.read`, `data.read`, `script` here). `data.write` is deliberately off the list; `script` is on it, because the harness needs it. Run `fire.tlscript`, then `collect.tlscript`; `fire` selects the cases of the logged-in account and prints its expectation beside each result. The rows are model-dependent; `SecDemoBackgroundAccessTest` is their deterministic form.

| # | Behaviour | Persona | Case | You should see |
|---|---|---|---|---|
| A1 | a capability the role holds is out of reach unattended | `anna` | 3.2 | refused naming `data.write` and the allowlist; `collect` reports `case 3.2 must not have persisted 'Anna was here' : OK` |
| A2 | an allowlisted `script` admits no writes | `anna` | 3.2 beside 3.1 and 3.6 | the commit refused while the read-only scripts of the same run succeed |
| A3 | it binds administrators too | `root` | 3.2 (as `root`, `fire` fires every case) | still refused: the missing approval prompt is the reason, not the principal's authority |
| A4 | what the allowlist admits works | `ben` | 3.3, 3.4, 3.5 | the listings, with the model rights applied |
| A5 | an enforcing policy must declare an allowlist | - | delete `<background-allowlist>` from `secdemo.config.xml` and restart | the application refuses to start ([capping unattended runs](doc:ai/security/tool-access-recipes)) |

### The pre-commit guard and the object tools

L4 reads the turn's changes from the engine's transaction and asks the same `<security-config>` whether the acting account may persist them - the one layer that catches a tool that never checked ([pre-commit guard](doc:ai/security/pre-commit-guard)). The fixture's own tools check, and a demo must not ship an unchecked write tool, so there is no console row: `SecDemoPreCommitGuardTest` registers one unchecked tool in a real agent run and asserts as `anna` that a permitted write commits, that a creation inside Beta and an update of a Beta ticket are rolled back with a notice naming the operation and the type but never the value, that `Delete` is a right of its own, and that a turn mixing a permitted with a forbidden change loses both; as `dora` the same write is refused. Changing the `partners` of Alpha is rolled back although Anna may write Alpha, because a `<part>` grant withholds that attribute - the guard reads every attribute kept outside the holder's row, references and translated texts included.

`updateObject`, `createObject` and `deleteObject` ask the model rights themselves, per object and attribute, before they write ([object tools](doc:ai/security/object-tools)); `SecDemoObjectEditTest`:

| Persona | Call | Expected |
|---|---|---|
| `anna` | change the title of an Alpha ticket / a Beta ticket | changed, the revision's author is Anna / refused, nothing written |
| `anna` | set `partners` of Alpha | refused, naming `'partners'`: no role may write who partners on a project |
| `anna` | create a ticket in Alpha / in Beta | created / refused |
| `anna` | delete an Alpha ticket | refused: `pm:Ticket` has no Delete grant |
| `ben` | any of the three | not offered (no `data.write`), refused by the model rights when handed the tool anyway |
| `anna`, unattended | any of the three | not offered: the background allowlist holds no `data.write` |

### Two closed doors

| # | Behaviour | Where | You should see |
|---|---|---|---|
| P1 | `/mcp/*` is not served | `curl -i http://localhost:8080/tl-ai-demo/mcp` | HTTP 503 naming both steps: enable the module `MCPServerService$Module` and configure an authentication ([the MCP endpoint](doc:ai/security/mcp-endpoint)); `SecDemoMcpEndpointTest` asserts the authenticated surface against these personas |
| P2 | a run without an account does not start | test | `SecDemoBackgroundAccessTest.testARunWithoutAPrincipalCannotBeBuilt` |

## The tests

The scenario tables are for exploring; the tests keep the feature honest, against a real application boot with real roles, rules, accounts, the shipped aspect file and the running tool registry:

```bash
mvn -B test -DskipTests=false -pl tl-ai-demo -Dtest='com.top_logic.ai.demo.SecDemo*' -Dsurefire.failIfNoSpecifiedTests=false
```

| Class | Scenarios | Class | Scenarios |
|---|---|---|---|
| `SecDemoToolAccessTest` | T1-T10 | `SecDemoSchemaFilterTest` | C3-C5 |
| `SecDemoScriptToolClassificationTest` | T8 | `SecDemoDelegateFilterTest` | C7 |
| `SecDemoReadFilterTest` | D1-D8 | `SecDemoBackgroundAccessTest` | A1-A5, P2 |
| `SecDemoModelDigestTest` | D9-D11 | `SecDemoPreCommitGuardTest` | the pre-commit guard |
| `SecDemoCapabilityNoticeTest` | C1, C2 | `SecDemoObjectEditTest` | the object tools |
| `SecDemoSkillAbsenceTest` | C1, C4-C6 | `SecDemoRoleRuleTest` | the boot check |
| `SecDemoListToolsTest` | C3, C4, C8 | `SecDemoMcpEndpointTest` | P1 |
| `SecDemoVectorIndexTest` | the vector index | `SecDemoModelSecurityTest` | the worked example, persona by persona |

`SecDemoSpawnFilterTest` (a spawn is decided per child context), `SecDemoDeclaredTypesTest` (a tool declaring `pm:Project` refuses a persona without read access before it runs), `SecDemoInfrastructureAgentTest` (an agent the application runs for a user, such as the prompt improver, cannot be run as a chat persona, whatever `<agent-access>` says), `SecDemoHarnessScriptsTest` (the harness compiles) and the tests of the [policy editor](doc:ai/security/tool-access-editor) (`SecDemoEffectiveAccessTest`, `SecDemoPolicyEditorTest`, `SecDemoApplyPolicyGuardTest`, `SecDemoToolDescriptionTest`, `SecDemoGermanTerminologyTest`) run in the same suite because they need the booted fixture. The unit-level counterparts in `tl-ai-core` use hand-built policies: `TestRoleToolAccess`, `TestToolAuthorization`, `TestAiToolAccessService`, `TestAgentCapabilities`, `TestDelegateFilter`, `TestSpawnFilter`, `TestDetachedSubagentCeiling`, `TestSkillTool`, `TestSkillAbsenceReason`, `TestTopLogicSecurityContext`, `TestPreCommitGuard`, `TestReadFilter`.

## Pitfalls

- **An empty result is the model layer, not a broken tool**, which is why almost every row is a pair. An application whose agent's tools all work and all return nothing has forgotten a scope rule.
- **`belongs to no tool group` is not `requires the '...' capability`**: the first is fixed by a `<tool-groups>` entry, the second by granting a role.
- **`STRUCTURAL` tools are also blocked by the execution mode**: in a deployed application the model editor and the agent-authoring tools stay out of the request whatever the policy says, and the denial names production mode (`SecDemoSchemaFilterTest`).
- **Every account of the demo also holds `SecurityStructure.user`** on the security root (from `tl-bpe-app`, which the demo builds on), so a denial log line shows more roles than the matrix names, and a grant against that role reaches every account.
- **Approval interrupts only in the chat**: a tool that changes anything asks there, and the call times out; `aiCallTool` and `aiAgentChat` in the console run with automatic approval.
- **`data.write` is what changing data costs**: the object tools, `editText` and `changeData` (classified `script`, asking for `data.write` on top, because the invocation gate decides per tool and does not see `dryRun`). Without it, `changeData` can only dry-run.
