# End-to-end security test: model access rights and tool access

> **Not the entry point.** Start at **[testbed.md](testbed.md)** - it maps every behaviour of the
> feature to a persona, a thing to type and an expected result, and it is where the persona matrix and
> the automated coverage are described. **This** page is the depth behind one part of it: the engine's
> model access rights, and the TL-Script harness that drives agent runs against them.

**Scope:** this walkthrough exercises the two enforcement layers that are live today.

- **Model rights** (sections 2 to 5): the engine's `ModelAccessRights` on `8.0.0-alpha7`, driven by a
  `<security-config>` and the roles of the acting account - *what a role may do with the data.*
- **Tool rights** (section 6): tl-ai's `<tool-access>` policy - *which agent tools a role may invoke.*

The two are independent, and the other layers of the RFC are still staged; see
[What this does and does not test](#7-what-this-does-and-does-not-test) at the end.

> **Two independent security layers.** TopLogic separates *what a role may do with the data* from
> *which views a role may open*. This test is about the first layer, but you cannot reach the agent
> without the second: a role with model access rights and no component access logs in to an
> application with **no tab at all**. The scaffold therefore wires both; see
> [1.1 Component access](#11-component-access-why-the-accounts-see-a-tab-at-all).

Related: [agent-security-architecture.md](agent-security-architecture.md) (the RFC), issues #220 and
#234.

---

## 1. What the scaffold sets up

A dedicated example model plus a minimal role system, all behind one opt-in switch.

| Element | Value |
|---|---|
| Module | `secdemo` |
| Types | `secdemo:Note {title, body}`, `secdemo:Secret {label, value}` |
| Roles | `secReader`, `secEditor`, and four more - see [testbed.md](testbed.md#3-the-persona-matrix) |
| Groups | `secReaders`, `secEditors`, and four more |
| Accounts | `secreader1` (in `secReaders`), `seceditor1` (in `secEditors`), and four more |
| Password | `Sec-Demo-1` (all accounts; configurable, see below) |
| Seed data | 3 `Note`s, 2 `Secret`s |
| Visible UI | the **Albert** tab (the agent chat) and the **Script console**, for both roles |
| Tool policy | `<tool-access>` granting the roles different tool capabilities, see [section 6](#6-the-tool-layer-which-roles-may-use-which-tools) |

The model policy (`WEB-INF/conf/secdemo.config.xml`):

| Type | Read | Create | Write |
|---|---|---|---|
| `secdemo:Note` | `secReader`, `secEditor`, `secWriter`, `secViewer` | `secEditor`, `secWriter` | `secEditor`, `secWriter` |
| `secdemo:Secret` | *(no grant)* | *(no grant)* | *(no grant)* |

`secdemo:Secret` has no grant at all - that is what makes it inaccessible. The engine's model
security is **deny-by-default**: an operation on a type without a matching grant is denied for every
role-based user. Administrators bypass all of it, which is the intended baseline.

Files involved:

```
tl-ai-demo/src/main/webapp/WEB-INF/model/secdemo.model.xml            the example model
tl-ai-demo/src/main/webapp/WEB-INF/conf/secdemo.config.xml            the opt-in aspect (model + roles + policy + view access)
tl-ai-demo/src/main/java/.../secdemo/SecDemoSetupService.java         seeds accounts, memberships, role assignments, view access, data
tl-ai-demo/src/main/webapp/WEB-INF/conf/metaConf.txt                  the on/off switch
```

### 1.1 Component access: why the accounts see a tab at all

The `<security-config>` above governs **model access**: which role may `Read` / `Create` / `Write` a
model *type*. It says nothing about the UI. Whether an account may see and open a view is a second,
completely independent mechanism:

| | Layer 1: model access | Layer 2: component access |
|---|---|---|
| Configured by | `<security-config>` on `SecurityConfigurationService` | command groups per role on a `securityLayout` component |
| Enforced by | `ModelAccessRights` | `BoundChecker.allow(...)` via `PersBoundComp` |
| Decides | what the account may do with an instance | whether the tab appears and opens |

Deny-by-default applies to layer 2 as well: a role with no command group on a view cannot open it.
Since the shipped demo grants nothing to anybody (there is no `WEB-INF/conf/security.xml`), the two
demo accounts would log in to an application showing *"No tabs to choose from."* - and none of the
cases in section 3 could be executed. Administrators are unaffected, because they bypass both layers.

The aspect therefore grants the `Read` command group on the security components that make up the
agent chat and the TL-Script console, to **both** demo roles:

| View | What it is |
|---|---|
| `tl-chat/chat/tabLayout.layout.xml#TabLayout` | the **Albert** tab |
| `tl-chat/chat/agent.layout.xml#agent` | the chat inside it, a security component of its own |
| `admin/index.layout.xml#adminView` | the **Administration** tab |
| `admin/studio/index.layout.xml#studioView` | the **Development** sub-tab inside it |
| `com.top_logic.model.search/admin/scriptConsole.layout.xml#view` | the **Script console** inside that |

Only a `securityLayout` (`CompoundSecurityLayout`) can hold access rights, and a nested one does
*not* inherit from its parent - which is why every level on the way to a view needs its own entry.
The sibling views the demo accounts do *not* get (model editor, service editor, layout editor,
accounts, ...) are security components of their own with no grant, so they stay closed.

The console is needed for the TL-Script harness in
[4. Running the cases as background agent runs](#4-running-the-cases-as-background-agent-runs-from-tl-script).
See there for what that grant means and why it is deliberate.

The configuration lives in the aspect, on `SecDemoSetupService`, in exactly the format the engine
uses for `WEB-INF/conf/security.xml`:

```xml
<component-access>
    <profile name="*">
        <view name="tl-chat/chat/tabLayout.layout.xml#TabLayout">
            <commandGroup name="Read"/>
        </view>
        ...
    </profile>
</component-access>
```

The profile name `*` is expanded by `SecDemoSetupService` to the role of every configured
`<persona>`. A profile named after a single role still works, for a persona that deliberately needs
different views.

**Why not `WEB-INF/conf/security.xml` directly?** That path is fixed and read on every boot, and the
engine's importer *resets* the access of every view in the application before applying the file. An
opt-in aspect must not do either. `SecDemoSetupService` applies the same configuration
**additively** instead: nothing outside the list above is touched, so switching the aspect off leaves
the demo exactly as shipped. Unknown roles, views or command groups fail the boot rather than
silently producing an empty application.

Note that every persona role gets the *same* view access, which is what the `*` profile says out
loud. That is deliberate: what distinguishes two personas is their model rights and their tool grants,
and keeping the UI identical is what makes the allow/deny pairs in section 3 comparable - a difference
you observe is then attributable to the policy and to nothing else.

---

## 2. Setup

### 2.1 Enable the test configuration

Edit `tl-ai-demo/src/main/webapp/WEB-INF/conf/metaConf.txt` and **uncomment** the last line:

```
db-default.xml
tl-ai-demo.conf.xml

# Agent security end-to-end test scaffold (secdemo model, roles, accounts, seed data).
# Uncomment the following line to enable it, comment it out again to disable it.
# See doc/security/e2e-security-test.md.
secdemo.config.xml          <-- remove the leading '#'
```

That single line is the whole manual setup. It brings the model, the roles and groups, the
`<security-config>`, the `<tool-access>`, the accounts, the seed data **and** the component access the
two accounts need to reach the agent chat (see
[1.1](#11-component-access-why-the-accounts-see-a-tab-at-all)). There is nothing to click in an
administration UI.

**To disable again:** put the `#` back **and start from a fresh database** (delete
`tl-ai-demo/tmp/h2.mv.db`, or drop the schema of whatever database you use).

> ⚠️ Removing the declaration makes the engine's automatic model upgrade try to *delete* the
> `secdemo` module on the next boot. That fails while `Note` / `Secret` instances still exist, and
> the application does not start:
>
> ```
> DynamicModelService - Deleting part with name 'secdemo': secdemo
> ... KnowledgeBaseException: Database operation failed.
> ... DeletedObjectAccess: Object DBKnowledgeObject[wrapper: secdemo:Note] locally deleted.
> ```
>
> This is standard engine behaviour for withdrawing a model module, not a defect of the scaffold.
> If you want to keep the database, log in as `root` **before** disabling and delete all `Note` and
> `Secret` instances (e.g. TL-Script `all(\`secdemo:Note\`).concat(all(\`secdemo:Secret\`)).foreach(x
> -> $x.delete())`), then disable and restart. The roles, groups and accounts are harmless and may
> stay.
>
> Treating the scaffold as "on for a test database, off for a clean one" is the least troublesome
> workflow.

### 2.2 Build and run

From the repository root, on this branch (engine `8.0.0-alpha7`):

```bash
mvn -B -o -pl tl-ai-demo -am install -DskipTests   # or your usual build
cd tl-ai-demo && mvn -B                            # default goal starts the app
```

The log ends with `***** up and running *****` and prints
`Server started: http://localhost:<port>/tl-ai-demo/`.

A fresh database is the cleanest starting point. On an existing database the engine performs an
incremental model upgrade and logs `Adding module 'secdemo'`.

### 2.3 Check the boot log

With the aspect enabled you should see, in order:

```
DynamicModelService - Adding module 'secdemo'.
InitialRolesManager - Created system wide role ... name: secReader
... one line per persona role ...
InitialGroupManager - Created system wide group ... name: secReaders
... one line per persona group ...
BasicRuntimeModule  - Service 'com.top_logic.ai.demo.secdemo.SecDemoSetupService' successfully started.
```

The tool policy reports itself in the same boot (in the `tl-ai` log file, see below):

```
AiToolAccessService - Agent tool access: mode=ENFORCED roles=6 baseline=0(+interaction) backgroundAllowlist=4
```

`SecDemoSetupService` logs its own steps under the logger `com.top_logic.ai`, which the demo routes to
a **separate** file - look in `tl-ai-demo/tmp/app-data/logs/top-logic.tl-ai.log`, not in the main log:

```
SecDemoSetupService - Granted 'Read' on view 'tl-chat/chat/tabLayout.layout.xml#TabLayout' to role 'secReader'.
SecDemoSetupService - Granted 'Read' on view 'tl-chat/chat/agent.layout.xml#agent' to role 'secReader'.
SecDemoSetupService - Granted 'Read' on view 'admin/index.layout.xml#adminView' to role 'secReader'.
SecDemoSetupService - Granted 'Read' on view 'admin/studio/index.layout.xml#studioView' to role 'secReader'.
SecDemoSetupService - Granted 'Read' on view 'com.top_logic.model.search/admin/scriptConsole.layout.xml#view' to role 'secReader'.
... the same five lines for each of the other five persona roles ...
SecDemoSetupService - Created security demo persona 'secreader1' (role 'secReader'): read-only plus TL-Script - reads notes, cannot create one, sees no secret
... one line per persona, each naming what it is in the fixture to demonstrate ...
SecDemoSetupService - Created 3 example instances of 'secdemo:Note'.
SecDemoSetupService - Created 2 example instances of 'secdemo:Secret'.
```

Everything here is idempotent: on a restart against the same database these lines simply do not
reappear.

If a role name in the `<security-config>` does not exist, the engine reports it as a **configuration
error at boot** (`The role ... does not exist`) rather than silently widening or narrowing access.
Likewise, an unknown role, view or command group in `<component-access>` aborts the boot with a
message naming it - an empty application is never the result of a typo.

### 2.4 Logins

The two this walkthrough uses; the full matrix is in
[testbed.md](testbed.md#3-the-persona-matrix).

| Login | Password | Role | Sees |
|---|---|---|---|
| `secreader1` | `Sec-Demo-1` | `secReader` | **Albert** and *Administration → Development → Script console* |
| `seceditor1` | `Sec-Demo-1` | `secEditor` | the same |
| `root` | your usual dev password | administrator (bypasses everything) | every tab |

To change the seeded password, add it to the aspect:

```xml
<config service-class="com.top_logic.ai.demo.secdemo.SecDemoSetupService">
    <instance class="com.top_logic.ai.demo.secdemo.SecDemoSetupService"
        initial-password="My-Own-Pwd-1"
    >
        <!-- ... keep the <component-access> block from the shipped aspect ... -->
    </instance>
</config>
```

It must satisfy the configured password policy (by default: at least 8 characters from at least two
character classes). The password is only applied when the account is created; to change it later,
use *Administration → Accounts*.

> If a demo account logs in and the content area says *"No tabs to choose from."*, the
> `<component-access>` block is missing or its view names no longer match the layouts. That is layer 2,
> not the model policy; see [1.1](#11-component-access-why-the-accounts-see-a-tab-at-all).

---

## 3. The test cases

Open the chat UI (`/tl-ai-demo/`, tab **Albert**) after logging in as the account named in each case.
For a demo account **Albert** is the entry tab and is selected right after login.

To run these cases from TL-Script instead of typing them into the chat, see
[4. Running the cases as background agent runs](#4-running-the-cases-as-background-agent-runs-from-tl-script).

> **Important - steer the agent to TL-Script.** Only the **TL-Script tool** runs with the engine's
> security checks enabled. The read-only model tools (`listInstances`, the instance browser) query
> the KnowledgeBase directly and are **not** filtered yet - that is exactly the result-filtering
> layer that is still staged (#234). Each prompt below therefore asks for TL-Script explicitly. If
> the agent answers from a different tool, say *"do that with the TL-Script tool"* and repeat.

> **The cases that write need the chat, not the background harness.** Committing through TL-Script
> requires the `data.write` capability on top of `script`, and `data.write` is deliberately off the
> demo's `<background-allowlist>`. The runs of [section 4](#4-running-the-cases-as-background-agent-runs-from-tl-script)
> are unattended, so every commit is refused there before the model rights are consulted - as intended,
> see [testbed.md](testbed.md#44-unattended-runs---the-background-allowlist) scenario **A5**. Read cases
> behave identically on both surfaces.

### 3.1 `secReader` - read is allowed

| | |
|---|---|
| **Login** | `secreader1` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`secdemo:Note\`).get(\`secdemo:Note#title\`)` |
| **Expect** | ✅ **ALLOWED** - the three seeded note titles (`Release checklist`, `Team offsite`, `Open questions`). |
| **Why** | `secdemo:Note` grants `Read` to `secReader`. |

### 3.2 `secReader` - create is denied

| | |
|---|---|
| **Login** | `secreader1` |
| **Prompt** | `Use the TL-Script tool with commit to evaluate: new(\`secdemo:Note\`).set(\`secdemo:Note#title\`, "Reader was here")` |
| **Expect** | ❌ **DENIED** - the tool result is an error, not an object: *"executing TL-Script with withCommit=true requires the 'data.write' capability, which none of your roles grants."* |
| **Why** | The **tool** layer refuses first now: `secReader` holds `script`, which reaches `executeTLScript` read-only, and committing costs `data.write` on top. The model layer would refuse too - `secdemo:Note` grants `Create` only to `secEditor` and `secWriter` - but this account no longer gets that far. To see the engine's `CREATE_PERMISSION_DENIED__TYPE` through this tool, use case 3.6, whose persona may commit and still may not create a `Secret`. |
| **Also check** | Re-run case 3.1 - still exactly three notes, nothing was persisted. |

### 3.3 `secReader` - secrets are not readable

| | |
|---|---|
| **Login** | `secreader1` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`secdemo:Secret\`).get(\`secdemo:Secret#value\`)` |
| **Expect** | ❌ **DENIED / EMPTY** - an empty list. No secret values appear. |
| **Why** | `secdemo:Secret` has no grant, so every attribute read on it is denied and yields the empty value. |
| **Note** | `all(\`secdemo:Secret\`)` on its own still returns the *objects* (without readable content) - object-list filtering is opt-in in TL-Script. To see the filtered list, use `filterSecurity(all(\`secdemo:Secret\`))`, which returns an empty list for this account. |

### 3.4 `secEditor` - create is allowed and persists

| | |
|---|---|
| **Login** | `seceditor1` |
| **Prompt** | `Use the TL-Script tool with commit to evaluate: new(\`secdemo:Note\`).set(\`secdemo:Note#title\`, "Editor note")` |
| **Expect** | ✅ **ALLOWED** - a new object is returned. **In the chat only**: `secEditor` holds `data.write`, which the commit requires, but the background harness of section 4 is capped by the allowlist and refuses it. |
| **Verify** | Ask again: `Use the TL-Script tool to evaluate: all(\`secdemo:Note\`).get(\`secdemo:Note#title\`)` → four titles now, including `Editor note`. |

### 3.5 `secEditor` - read is allowed

| | |
|---|---|
| **Login** | `seceditor1` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`secdemo:Note\`).get(\`secdemo:Note#body\`)` |
| **Expect** | ✅ **ALLOWED** - the note bodies. |

### 3.6 `secEditor` - secrets stay out of reach

| | |
|---|---|
| **Login** | `seceditor1` |
| **Prompt** | `Use the TL-Script tool with commit to evaluate: new(\`secdemo:Secret\`).set(\`secdemo:Secret#label\`, "mine now")` |
| **Expect** | ❌ **DENIED** - `CREATE_PERMISSION_DENIED` in the chat, where the commit itself is permitted. Reading works no better: `all(\`secdemo:Secret\`).get(\`secdemo:Secret#value\`)` is empty. |
| **Why** | No role has any grant on `secdemo:Secret`. This is the case that shows the model layer through the tool: the persona holds `data.write`, so the tool layer lets the commit through, and the engine denies the create anyway. Unattended (section 4) the tool layer refuses earlier and the model layer is not reached. |

### 3.7 `root` - the administrator baseline

| | |
|---|---|
| **Login** | `root` |
| **Prompts** | All of the above. |
| **Expect** | ✅ **ALLOWED** throughout, including creating and reading `secdemo:Secret`. |
| **Why** | Administrators bypass `ModelAccessRights` - the engine's own behaviour, unchanged by this scaffold. |
| **Note** | `root` also sees every other tab, because the component check is bypassed too. A demo account seeing two tabs where `root` sees all of them is the correct contrast, not a defect. |

### 3.8 Clean-up between runs

Cases 3.2 and 3.4 change data. To reset, run
[`harness/reset.tlscript`](harness/reset.tlscript) as `root`, or start with a fresh database
(`tl-ai-demo/tmp`). Run the reset script twice to verify it: the second run must report `none` on all
four of its lines. See [testbed.md](testbed.md#23-reset-between-scenarios).

---

## 4. Running the cases as background agent runs from TL-Script

Section 3 is a walkthrough for a human typing into a chat. The same cases can be driven from the
**Script console** instead, which makes the whole matrix repeatable: one script starts a background
agent run per case, a second one collects status, result and expectation side by side.

The scripts live next to this document:

| File | Purpose | Needs an LLM key |
|---|---|---|
| [`harness/probe.tlscript`](harness/probe.tlscript) | prints the **tool**-layer decision for the logged-in persona, one line per capability | no |
| [`harness/fire.tlscript`](harness/fire.tlscript) | starts one background agent run per case and remembers the handles | yes |
| [`harness/collect.tlscript`](harness/collect.tlscript) | reads status, result and expectation back, plus the post-conditions | no |
| [`harness/reset.tlscript`](harness/reset.tlscript) | puts the seeded data back the way it was (run as `root`) | no |

`fire.tlscript` also carries four tool-layer cases (`T1` to `T4`) that ask a persona for something it
lacks the capability for, and are judged on whether the agent **names** the missing capability. The
deterministic version of those assertions is the `SecDemo*` JUnit suite; see
[testbed.md](testbed.md#6-the-deterministic-backbone).

### 4.1 Why the agent runs as the calling user

The harness calls `aiAgentChatAsync(agentName, prompt)`, a TL-Script binding of
`com.top_logic.tl.ai.core.scripting.AIScriptFunctions#agentChatAsync`. It is the *only* one of the
two agent entry points that is a valid surface for this test:

| | `aiAgentChatAsync` | `aiAgentChat` |
|---|---|---|
| Security context | captures the caller's `TLSubSessionContext` and re-enters it on the background thread (`ThreadContextManager.inContext(...)`), with a `TopLogicSecurityContext` | `ToolContext.forCurrentUser()`, i.e. a `TopLogicSecurityContext` for the account bound to the calling thread |
| Runs as | **the account that executed the script**, marked as a background run | the account that executed the script, as an attended run |
| Blocks the console | no, returns a handle immediately | yes, until the agent is finished |

Because the async variant re-enters the caller's sub-session, `TLContext.currentUser()` inside the
agent's tools is the account that pressed the button, and `ModelAccessRights` evaluates that
account's roles. The synchronous `aiAgentChat` would prove nothing here, for the same reason `root`
proves nothing (below). `agentChatAsync` also refuses to start at all from a system or background
context - it needs a sub-session with a person, which is exactly the property this test relies on.

Two consequences to be aware of:

- A background run has no human to answer approval prompts, so **every tool call is auto-approved**
  (`ApprovalCallback.AUTO_APPROVE`). That is intended here - the point is what the *engine* denies,
  not what a human refuses.
- The run commits in its own transaction on the background thread, independent of the console
  request.

### 4.2 ⚠️ Running the harness as `root` proves nothing

Administrators bypass `ModelAccessRights` entirely. Executed as `root`, **every** case succeeds,
including the ones whose whole purpose is to be denied - the deny cases pass vacuously and the test
asserts nothing. `fire.tlscript` therefore prints a warning line when it detects that it is running
as `root`.

Running as `root` is only useful as the deliberate administrator baseline of case 3.7: the same
prompts, expected to be allowed throughout. For everything else, log in as `secreader1` or
`seceditor1`.

### 4.3 The Script console grant

Every persona can reach the console because the aspect grants the `Read` command group on the three
nested views listed in [1.1](#11-component-access-why-the-accounts-see-a-tab-at-all) to every persona
role.
Without it they cannot run TL-Script at all: the shipped demo has no `WEB-INF/conf/security.xml`, so
no role holds a command group on any view, and the console is invisible.

Only `Read` is granted. That covers opening the console and its **execute** button; the
**execute and save changes** button is bound to the `Write` command group and stays disabled for the
demo accounts (its tooltip says *"(Keine Berechtigung)"*). The agent runs the harness starts are not
affected - they write in their own transaction on the background thread.

> **This deliberately gives a non-administrator role the ability to execute arbitrary TL-Script.**
> That is the point: the test asserts that model access rights hold even for a user who can write
> any expression they like. It is confined to an opt-in demo aspect that is off in the shipped
> configuration. Do not copy the grant into a real application without thinking about it - note in
> particular that a Script console user can also call `aiCallTool(...)`, which executes a registered
> agent tool directly. That call is now gated by the `<tool-access>` policy the aspect configures (see
> [6. The tool layer](#6-the-tool-layer-which-roles-may-use-which-tools)), so a console user reaches
> only the tools their role is granted - but a role holding `script` can still express any *data*
> operation TL-Script allows, bounded only by the model rights.

### 4.4 Running it

1. Log in as `secreader1` (cases 3.1 - 3.3) or `seceditor1` (cases 3.4 - 3.6).
2. Open *Administration → Development → Script console*.
3. Paste `harness/fire.tlscript` and press **execute** (the ▶ button). It prints how many runs it
   started and their handles. The default agent is `TL_SingleAgent`, the demo's chat entry point;
   change `agentName` at the top of the script to use another one.
4. Wait until the runs are finished. Without an LLM key that is immediate; with one it takes as long
   as the agent needs.
5. Paste `harness/collect.tlscript` and press **execute**. Re-run it as often as you like.

**Fire and collect must be two separate executions.** A request pins its knowledge-base revision,
and the background run's session and execution rows are committed out-of-band at a newer revision.
Inside the request that started it, the run is therefore invisible: `aiAgentRunStatus(handle)`
returns `null`, not `RUNNING`, no matter how long the script sleeps. A single self-contained
fire-wait-collect script consequently cannot work in the Script console; the two-step form is not a
convenience, it is the only shape that works.

The handles travel from one script to the other through the user session (`storeInSession` /
`loadFromSession`), so both must run in the **same browser session**. `fire.tlscript` also prints
the handles, so a lost session can be recovered by hand with
`aiAgentRunStatus("ID(...)")` / `aiAgentRunResult("ID(...)")`.

### 4.5 Reading the output

`collect.tlscript` prints four lines per case:

```
== 3.1  [secreader1]  status: COMPLETE
   expected : ALLOWED - the three seeded note titles (Release checklist, Team offsite, Open questions).
   result   : ...the agent's final answer...
   errors   : -
```

| Field | Meaning |
|---|---|
| `status` | `COMPLETE`, `ERROR`, `CANCELLED`, `REJECTED`, `RUNNING`, or `<unknown handle>` if the session state was lost |
| `expected` | the expectation from section 3, verbatim |
| `result` | the agent's final answer, or `(none ...)` while the run is not `COMPLETE` |
| `errors` | every `errorMessage` recorded in the run transcript |

A judgement is only possible on `COMPLETE`. `ERROR` with
`errors : x-api-key header is required` (or a comparable authentication message) means **no LLM API
key is configured** - a setup problem, not a security finding. The demo reads its key from the
environment variable `tl_anthropic_api_key`.

The report ends with the post-conditions, evaluated *directly* in the console rather than taken from
the agent's narration, and therefore with the engine's security checks on:

```
-- post conditions, evaluated directly as the logged-in account --
   secdemo:Note titles readable here : Release checklist, Team offsite, Open questions
   case 3.2 must not have persisted 'Reader was here' : OK
   case 3.4 note 'Editor note' present : no - expected: an unattended run may not commit, ...
   secdemo:Secret values readable here : no - expected for secReader / secEditor
```

The last line is the sharpest single check in the harness: it must say `no` for `secreader1` and
`seceditor1` and `YES` for `root`.

The `case 3.4` line says `no` for every account, `root` included: these runs are unattended and the
`<background-allowlist>` does not admit `data.write`, so no commit reaches the database from here. A
`yes` means a chat run persisted the note earlier - `reset.tlscript` clears it.

> The console's result table shows one row per line and its **Result** column is narrow by default.
> Drag the right border of the column header (or maximize the result panel with the ⛶ button) to
> read the rows in full.

### 4.6 Re-running

Neither write case leaves anything behind here: 3.2 must fail, and 3.4 is refused as well because these
runs are unattended and cannot commit. Only the chat persists `Editor note`, and it then accumulates over
runs. `harness/reset.tlscript`, executed as `root` with the **execute and save changes** button, deletes
every `Note` and `Secret` that is not one of the seeded ones and restores any seeded one a scenario
removed. A fresh database (`tl-ai-demo/tmp`) is the other way.

Everything else in the harness is idempotent: the runs are new each time, and the handles in the
session are overwritten by the next `fire.tlscript`.

---

## 5. Automated counterpart

The same allow/deny matrix runs as a JUnit test that evaluates the identical TL-Script expressions
the way the agent's TL-Script tool does (compiled with security checks on):

```bash
mvn -B -o -pl tl-ai-demo -am test -DskipTests=false \
    -Dsurefire.failIfNoSpecifiedTests=false -Dtest=SecDemoModelSecurityTest
```

`tl-ai-demo/src/test/java/com/top_logic/ai/demo/SecDemoModelSecurityTest.java` covers: reader reads
notes, reader cannot create a note, reader sees no secret values, `filterSecurity` drops the
secrets, editor creates a note, editor cannot create a secret. The test application enables the
**same** aspect file (`src/test/webapp/WEB-INF/conf/metaConf.txt`), so the test and the shipped
configuration cannot drift apart.

The test does not assert anything about the UI, but it does boot `SecDemoSetupService` with the same
`<component-access>` block, and an unknown view name fails that boot. A view rename in `tl-chat` or
`tl-model-search` will therefore break this test rather than quietly returning the demo to an empty
application or an unreachable Script console.

Use this to confirm the enforcement is intact before spending time in the UI; use the manual cases
above to confirm the *agent* actually routes through the enforced code path.

---

## 6. The tool layer: which roles may use which tools

Sections 3 to 5 are about the **model** rights - what a role may do with the data. This section is about
the **tool** rights, the other, independent authorization: whether a role may invoke a given agent tool
at all. A role can hold the TL-Script tool and be denied every object it asks for, and it can have broad
data rights and no tool to reach them with.

The aspect configures a `<tool-access>` for six persona roles, described in full in
[testbed.md](testbed.md#3-the-persona-matrix). The pair this section is about:

| | `secReader` | `secEditor` |
|---|---|---|
| `model.read`, `data.read` | granted | granted |
| `script` (privileged) | granted | granted |
| `agent.meta` | **not granted** | granted |
| `data.write` | **not granted** | granted |
| `model.structure`, `agent.authoring`, `egress`, `workspace` | not granted | not granted |

Both keep `script` deliberately: the cases in sections 3 and 4 ask the agent to use the TL-Script tool in
order to reach the model rights, and denying it here would stop them before the layer they are about.
The `agent.meta` difference is what the one-tool comparison below rests on, and nothing else in this demo
depends on it.

`data.write` is the second difference and it is what the write cases of section 3 rest on: `script`
reaches `executeTLScript` read-only, and `withCommit=true` requires `data.write` as well - so `secEditor`
may persist what a script changes and `secReader` may not, before any model right is consulted.

### 6.1 Driving it from the Script console

No LLM key is needed: `aiCallTool` dispatches a tool directly and the permission check happens before
any model call. Log in, open *Administration → Development → Script Konsole*, and run one line.

| Login | Expression | Expect |
|---|---|---|
| `secreader1` | `aiCallTool("framework-wiring.listFrameworkAgents", null)` | ❌ **DENIED** - `Permission denied: tool 'framework-wiring.listFrameworkAgents' requires the 'agent.meta' capability, which none of your roles grants.` |
| `seceditor1` | the same expression | ✅ **ALLOWED** - a JSON list of the configured agents |
| either | `aiCallTool("model-modification.createClass", null)` | ❌ **DENIED** - `requires the 'model.structure' capability`. Deny-by-default: no role is granted that privileged group, and no `<deny>` was needed |
| either | `aiCallTool("mcp__anything__doThing", null)` | ❌ **DENIED** - `belongs to no tool group and has no explicit grant`. A tool nobody classified is denied to everyone |
| `secreader1` | ``aiCallTool("script-exec.executeTLScript", {"script": "new(`secdemo:Note`)", "withCommit": true})`` | ❌ **DENIED** - `executing TL-Script with withCommit=true requires the 'data.write' capability, which none of your roles grants.` The same call with `"withCommit": false` succeeds |
| `seceditor1` | the same expression | ✅ **ALLOWED** - a note is created. It holds `data.write`; clean up with `reset.tlscript` |
| `root` | any of the above | ✅ **ALLOWED** - administrators bypass the tool layer exactly as they bypass the model rights |

The reader/editor pair on the *same* expression in the *same* application is the point: the only thing
that differs is which role the account holds.

### 6.2 Reading the boot log and the audit log

At boot, in `tmp/app-data/logs/top-logic.tl-ai.log`:

```
AiToolAccessService - Agent tool access: mode=ENFORCED roles=6 baseline=0(+interaction) backgroundAllowlist=4
```

With the aspect switched off it reads `roles=0`, and then every account reaches only the implicit
`interaction` grant - enforcement is unconditional, so an absent grant table denies rather than
permits.

Every denial is logged at `WARN`, naming the tool, its group, the account, the roles it holds and the
rule that decided:

```
WARN ToolAuthorization - Tool access denied: tool='framework-wiring.listFrameworkAgents'
group=agent.meta user=secreader1 roles=[SecurityStructure.user, secReader]
reason=Permission denied: tool 'framework-wiring.listFrameworkAgents' requires the 'agent.meta'
capability, which none of your roles grants.
```

Note `SecurityStructure.user`: every account holds that role on the security root in addition to its
own. It is another name a `<tool-access>` entry can be written against, and granting it grants every
account.

### 6.3 The background ceiling

`aiAgentChatAsync` - what the harness of section 4 uses - has no human to answer an approval prompt, so
every tool call self-approves. Such a run is additionally capped by `<background-allowlist>`, and a
denial from that cap says so rather than blaming the role:

> `Permission denied: tool 'model-modification.createClass' is not on the background allowlist. This run has no user who could approve it, so only the allowlisted capabilities are reachable.`

The demo allowlist includes `script` **only** so the harness of section 4 keeps working. A real
deployment would leave a group containing a write tool off an unattended run's allowlist; that is the
entire point of the mechanism.

### 6.4 Automated counterpart

```bash
mvn -B -o -pl tl-ai-demo -am test -DskipTests=false \
    -Dsurefire.failIfNoSpecifiedTests=false -Dtest=SecDemoToolAccessTest
```

`tl-ai-demo/src/test/java/com/top_logic/ai/demo/SecDemoToolAccessTest.java` runs the matrix above
against a real application boot: real roles, real accounts, the shipped policy file and the running tool
registry. It is the tool-layer counterpart of `SecDemoModelSecurityTest` and, like it, uses the very same
aspect file as the shipped configuration, so the two cannot drift apart.

Administrator-facing documentation for writing such a policy in your own application:
[tool-access-configuration.md](tool-access-configuration.md).

---

## 7. What this does and does not test

**It tests (live today, engine `8.0.0-alpha7`):**

- The engine's `ModelAccessRights` / `SecurityConfigurationService` - deny-by-default access to model
  types and attributes, keyed on the roles of `TLContext.currentUser()`.
- That the agent acts **as the logged-in user**: the chat sub-session is propagated to the agent
  loop and to the tool worker threads, so `TLContext.currentUser()` is the chat user and not a
  system context.
- That TL-Script executed by the agent's TL-Script tool is compiled **with** security checks
  (`QueryExecutor.compile(...)` without `disableSecurity()`), so `new(...)`, attribute reads,
  `Update`, `Delete` and reference navigation are gated.
- Administrator bypass, i.e. the positive-side demonstration requested in #234.

- **tl-ai's role→tool matrix** (`RoleToolAccess`): the aspect configures a `<tool-access>`, so the
  tool-level gate is enforced for every persona. See
  [6. The tool layer](#6-the-tool-layer-which-roles-may-use-which-tools) below.
- **The pre-commit guard** (`PreCommitGuard`): the end-of-turn verification of everything the turn
  wrote, which is what covers a tool that ran *no* check of its own. Not exercised by the walkthrough
  here - a TL-Script tool always checks, so there is nothing for the guard to catch on this surface -
  but covered deterministically by `SecDemoPreCommitGuardTest` and described in
  [testbed.md](testbed.md#48-the-pre-commit-guard---what-catches-an-uncooperative-tool).

**It does not test (staged, not wired on this branch):**

- **Result pre-filtering in tl-ai tools**: the read-only model tools (`listInstances`, instance
  browser, model search) bypass the engine check because they query the KnowledgeBase directly.
  Only TL-Script is enforced. This is the single most important gap to keep in mind while testing -
  a "the agent showed me the secret" observation through those tools is a *known* gap, not a new
  finding. Note the tool layer is no substitute: it decides *whether* a role may call `listInstances`,
  not *which instances* the call returns.
- **Row-level / instance-level policies**: the scaffold grants on *types*. Per-instance rules
  (`RoleRule`s on a security structure) are a separate engine mechanism and are not exercised here.
- **Component security itself.** The `<component-access>` block is a *precondition* for this test, not
  its subject. It is set up so the accounts can reach the agent and the Script console; nobody
  asserts here that a view a role has no grant on stays closed (that the demo accounts see only
  those two places is a side observation, not a test case).
- **The agent loop calling the tool gate.** Section 6 drives the gate through `aiCallTool`, which
  needs no model provider. That the *agent loop* refuses a denied tool mid-run, and narrates the
  denial, needs an LLM API key and is not covered here. The gate is the same object either way
  (`SecurityContext.hasToolPermission`), reached from `AgentLoop.executeWithSecurityCheck`.
- **Per-agent `required-groups`.** Section 4.2 of the RFC; not built, so no agent definition narrows
  itself. The per-agent security mode that briefly existed alongside it was removed by issue #288 - an
  agent definition decides nothing about enforcement.

---

## 8. Gotchas worth knowing

- **`use-default-security-parent`.** Plain model objects only inherit roles from the security root
  when `BoundHelper` is configured with `use-default-security-parent="true"`. The default is
  `false`; without it, a role granted on the security root never reaches a `secdemo:Note` and every
  instance-level check denies. The aspect sets it.
- **Roles must exist before the policy is read.** `SecurityConfigurationService` resolves role names
  at startup and reports unknown roles as boot errors. The aspect creates them via
  `InitialRolesManager`, which the service declares as a dependency, so the order is guaranteed.
- **Model rights are not view rights.** The two layers are entirely independent, and the second one is
  the easier to forget: an account with a perfect `<security-config>` and no command group on any view
  logs in to *"No tabs to choose from."* See
  [1.1](#11-component-access-why-the-accounts-see-a-tab-at-all).
- **Only a `securityLayout` holds access rights, and nesting does not inherit.** Command groups are
  stored per `CompoundSecurityLayout` (`PersBoundComp`); ordinary components share the rights of their
  nearest such ancestor, but a nested `securityLayout` starts from zero. The chat needs grants on both
  `tl-chat/chat/tabLayout.layout.xml#TabLayout` and `tl-chat/chat/agent.layout.xml#agent`, and the
  Script console needs three, one per nesting level. To find the name of a view, look for the
  `securityLayout` in the layout file: the name is `<layout resource path>#<component name>`. The
  authoritative list is the `ID` column of the `PERS_BOUND_COMP` table in the application's database
  - every security component of the running application has a row there.
- **`WEB-INF/conf/security.xml` is all-or-nothing.** `AccessConfigurationSetupService` reads that one
  fixed path and *resets* the access of every view before applying it, so a partial file silently
  revokes everything else. That is why the scaffold applies the same format additively from
  `SecDemoSetupService` instead of shipping a `security.xml`.
- **The `<access-rights>` model annotation is inert on alpha7.** `TLAccessRights` exists as a model
  annotation but nothing consumes it in this engine version. The working configuration path is
  `<security-config>` on `SecurityConfigurationService`.
- **Not-yet-committed objects are exempt.** `ModelAccessRights.isAllowed` returns `true` for objects
  created in the current transaction, because role assignments are computed at commit time. The
  `CREATE` check against a committed context is what gates creation.
- **A system context bypasses everything.** Code running via
  `ThreadContextManager.inSystemInteraction(...)` *without* an ambient user interaction acts as the
  system and is fully allowed. That is why the MCP server endpoint (`/mcp/*`, unauthenticated) is not
  a valid test surface for per-user enforcement - use the chat UI or the harness of section 4.
- **The Script console enforces the model rights too.** `ExpertSearchExpressionEditor` compiles with
  `QueryExecutor.compileOptional(...)` and never calls `disableSecurity()`, so an expression typed
  into the console is gated exactly like one executed by the agent's TL-Script tool. That is what
  makes the post-conditions in `collect.tlscript` meaningful - and it is also why reading the name
  of a `tl.ai.core:Agent` returns nothing for a demo account: that type has no grant either.
- **The demo's agents are not knowledge-base objects.** `TL_SingleAgent` and its siblings come from
  the application configuration, so ``all(`tl.ai.core:Agent`)`` is empty even for `root` in the
  shipped demo. `AgentDefinitionQuery` merges both sources; to see the configured ones from
  TL-Script use `aiCallTool("framework-wiring.listFrameworkAgents", null)`.
- **A request pins its revision.** Data committed on another thread after the request started is
  invisible for the rest of that request. This is why `fire.tlscript` and `collect.tlscript` must be
  two executions (see [4.4](#44-running-it)), and why reading an attribute of an object that the
  same script deleted earlier fails - `reset.tlscript` collects its report data before deleting.
