# End-to-end security test: model access rights and tool access

> **Not the entry point.** Start at **[testbed.md](testbed.md)** - it maps every behaviour of the
> feature to a persona, a thing to type and an expected result, and it is where the persona matrix and
> the automated coverage are described. **This** page is the depth behind one part of it: the engine's
> model access rights, and the TL-Script harness that drives agent runs against them.

**Scope:** this walkthrough exercises the two enforcement layers that are live today.

- **Model rights** (sections 2 to 5): the engine's `ModelAccessRights` on `8.0.0-alpha7`, driven by a
  `<security-config>` **and the role rules that decide which roles the acting account holds on which
  object** - *what a role may do with the data, per object.*
- **Tool rights** (section 6): tl-ai's `<tool-access>` policy - *which agent tools a role may invoke.*

The two are independent, and what remains staged is listed under
[What this does and does not test](#7-what-this-does-and-does-not-test) at the end.

**The one idea to carry into every section below:** a role is a relationship `(group, object, role)`, and
the two layers ask about the same role at *different objects* - the tool layer at the security root, the
model layer at the object being touched. Almost every refusal in this document is the second one, because
the first is structurally incapable of answering "which object".

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
| Module | `pm` |
| Types | `pm:Project {name, partners, tickets}`, `pm:Ticket {title, body}` |
| Roles | `projectPartner`, `ticketReader` - **two**, both named in both grant tables |
| Groups | `projectPartners`, `ticketReaders` |
| Accounts | `anna`, `ben`, `cara`, `dora` - see [testbed.md](testbed.md#3-the-personas) |
| Password | `Sec-Demo-1` (all accounts; configurable, see below) |
| Seed data | 2 `Project`s (Alpha with 2 tickets, Beta with 1) |
| Visible UI | the **Albert** tab (the agent chat) and the **Script console**, for both roles |
| Tool policy | `<tool-access>` granting the two roles different tool capabilities, see [section 6](#6-the-tool-layer-which-roles-may-use-which-tools) |

The model policy (`WEB-INF/conf/secdemo.config.xml`):

| Element | Read | Create | Write | Delete |
|---|---|---|---|---|
| `pm:Project` | `projectPartner` | `projectPartner` | `projectPartner` | *(no grant)* |
| `pm:Ticket` | `projectPartner`, `ticketReader` | `projectPartner` | `projectPartner` | *(no grant)* |
| `pm:Ticket#body` | `projectPartner` | - | - | - |

Three properties of that table carry most of this document:

- **Deny-by-default is real.** Neither type has a `Delete` grant, so nobody deletes anything by role,
  and no denial was written to achieve it.
- **A `<part>` grant narrows.** `pm:Ticket#body` is consulted only after the class-level `Read` check
  has passed, so a ticket reader gets the ticket, gets its `title`, and `body` is simply absent.
- **The table alone decides nothing per object.** "Whether this account holds `projectPartner`" is
  answered on the object being touched, which is what the aspect's **role rules** produce - see
  [testbed.md](testbed.md#12-the-hinge). A role held on the security root satisfies
  the *tool* layer and grants no data access at all.

Administrators bypass all of it, which is the intended baseline.

Files involved:

```
tl-ai-demo/src/main/webapp/WEB-INF/model/pm.model.xml                 the business model
tl-ai-demo/src/main/webapp/WEB-INF/conf/secdemo.config.xml            the opt-in aspect (model + roles + rules + policies + view access)
tl-ai-demo/src/main/java/.../secdemo/SecDemoSetupService.java         seeds accounts, memberships, projects, view access - and VERIFIES the roles
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

The profile name `*` is expanded by `SecDemoSetupService` to every role a configured `<persona>` is
expected to hold. A profile named after a single role still works, for a persona that deliberately needs
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

# Agent security test bed (pm model, declarative role rules, accounts, tool policy, seed data).
# Uncomment the following line to enable it, comment it out again to disable it.
# See doc/security/e2e-security-test.md.
secdemo.config.xml          <-- remove the leading '#'
```

That single line is the whole manual setup. It brings the model, the roles and groups, the role rules, the
`<security-config>`, the `<tool-access>`, the accounts, the seed data **and** the component access the
two accounts need to reach the agent chat (see
[1.1](#11-component-access-why-the-accounts-see-a-tab-at-all)). There is nothing to click in an
administration UI.

**To disable again:** put the `#` back **and start from a fresh database** (delete
`tl-ai-demo/tmp/h2.mv.db`, or drop the schema of whatever database you use).

> ⚠️ Removing the declaration makes the engine's automatic model upgrade try to *delete* the
> `pm` module on the next boot. That fails while `Project` / `Ticket` instances still exist, and
> the application does not start:
>
> ```
> DynamicModelService - Deleting part with name 'pm': pm
> ... KnowledgeBaseException: Database operation failed.
> ... DeletedObjectAccess: Object DBKnowledgeObject[wrapper: pm:Ticket] locally deleted.
> ```
>
> This is standard engine behaviour for withdrawing a model module, not a defect of the scaffold.
> If you want to keep the database, log in as `root` **before** disabling and delete all instances
> (TL-Script ``all(\`pm:Ticket\`).concat(all(\`pm:Project\`)).foreach(x -> $x.delete())`` - the tickets
> first, because they are a composition of the projects), then disable and restart. The roles, groups
> and accounts are harmless and may stay.
>
> **The same applies in the other direction, and it will catch you first.** A database that ran the
> *previous* version of this fixture holds `secdemo:Note` and `secdemo:Secret` instances of a module
> nothing declares any more, so enabling this one against it does not boot either. Start from a fresh
> database.
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
DynamicModelService - Adding module 'pm'.
InitialRolesManager - Created system wide role ... name: projectPartner
InitialRolesManager - Created system wide role ... name: ticketReader
InitialGroupManager - Created system wide group ... name: projectPartners
InitialGroupManager - Created system wide group ... name: ticketReaders
BasicRuntimeModule  - Service 'com.top_logic.ai.demo.secdemo.SecDemoSetupService' successfully started.
```

The tool policy reports itself in the same boot (in the `tl-ai` log file, see below):

```
AiToolAccessService - Agent tool access: roles=2 baseline=0(+interaction) backgroundAllowlist=3
```

`SecDemoSetupService` logs its own steps under the logger `com.top_logic.ai`, which the demo routes to
a **separate** file - look in `tl-ai-demo/tmp/app-data/logs/top-logic.tl-ai.log`, not in the main log:

```
SecDemoSetupService - Granted 'Read' on view 'tl-chat/chat/tabLayout.layout.xml#TabLayout' to role 'projectPartner'.
... the same five views for each of the two persona roles ...
SecDemoSetupService - Created security demo persona 'anna' (expects role(s) [projectPartner] from the tier rules): tier and scope: write tools globally, and only project Alpha to write them on
... one line per persona, each naming what it is in the fixture to demonstrate ...
SecDemoSetupService - Created project 'Alpha' with partners [anna] and 2 ticket(s).
SecDemoSetupService - Created project 'Beta' with partners [cara] and 1 ticket(s).
SecDemoSetupService - Created 2 example projects with 3 tickets.
SecDemoSetupService - Security demo: checked the roles of 4 personas against the aspect's role rules.
```

**The last line is the one to read.** `SecDemoSetupService` assigns no roles - the aspect's
`<role-rules>` do - so it checks afterwards whether they arrived, and warns per persona and per role
when they did not. A warning there means every case below will be wrong for that persona in a way that
looks like a policy problem.

The "Created" lines are idempotent: on a restart against the same database they simply do not reappear,
while the role check runs every time.

If a role name in the `<security-config>` does not exist, the engine reports it as a **configuration
error at boot** (`The role ... does not exist`) rather than silently widening or narrowing access.
Likewise, an unknown role, view or command group in `<component-access>` aborts the boot with a
message naming it - an empty application is never the result of a typo.

### 2.4 Logins

The two this walkthrough uses; the full matrix is in
[testbed.md](testbed.md#3-the-personas).

| Login | Password | Roles, and where | Sees |
|---|---|---|---|
| `anna` | `Sec-Demo-1` | `projectPartner` on the root **and on project Alpha** | **Albert** and *Administration → Development → Script console* |
| `ben` | `Sec-Demo-1` | `ticketReader` on the root **and on every ticket** | the same |
| `cara` | `Sec-Demo-1` | both roles; `projectPartner` scoped to **Beta** | the same |
| `dora` | `Sec-Demo-1` | `projectPartner` on the root **only** | the same |
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

> **Steer the agent to TL-Script for the *model rights*, and to the data tools for the *read filter*.**
> Both surfaces are enforced now: TL-Script has always run with the engine's checks, and since issue
> #289 every conversion of a model object into a tool result goes through one read filter that takes the
> run's rights. What differs is what each shows. TL-Script shows the engine's answer directly - an empty
> list, a refused create. The data tools show what an agent is *told* - a `totalCount` that follows the
> filter, an absent attribute, a "not found" for an object that exists. The cases below use TL-Script,
> and [testbed.md](testbed.md#53-read-filtering---what-a-permitted-tool-then-discloses-issue-289) section
> 5.3 is the same layer through the tools. If the agent answers from a tool you did not ask for, say
> *"do that with the TL-Script tool"* and repeat.

> **The cases that write need the chat, not the background harness.** Committing through TL-Script
> requires the `data.write` capability on top of `script`, and `data.write` is deliberately off the
> demo's `<background-allowlist>`. The runs of [section 4](#4-running-the-cases-as-background-agent-runs-from-tl-script)
> are unattended, so every commit is refused there before the model rights are consulted - as intended,
> see [testbed.md](testbed.md#55-unattended-runs---the-background-allowlist) scenario **A2**. Read cases
> behave identically on both surfaces.

### 3.1 `anna` - the scope, per object

| | |
|---|---|
| **Login** | `anna` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`pm:Project\`).get(\`pm:Project#name\`)` |
| **Expect** | ✅ **ALLOWED**, and exactly one name: `Alpha`. |
| **Why** | `pm:Project` grants `Read` to `projectPartner`, and Anna holds that role **on Alpha** - because Alpha lists her in `partners` and a `<rule type="reference">` derives the role from that attribute. She does not hold it on Beta, so Beta is not in the result at all. |
| **The point** | Two instances of one type, one capability, two different answers. This is the case the whole fixture exists for, and the one its predecessor could not produce. |

### 3.2 `anna` - the tier is not what bounds her

| | |
|---|---|
| **Login** | `anna` |
| **Prompt** | Run [`harness/probe.tlscript`](harness/probe.tlscript) first, then case 3.1 again. |
| **Expect** | `model.read`, `data.read` and `script` `ALLOWED` in the probe - it has no write entry, every probe is read-only - and still only `Alpha` from the script. |
| **Why** | Anna holds `data.write` **on the security root**, so the tool layer lets every write tool through for her. What stops her at Beta is that she holds `projectPartner` on Alpha and not on Beta. A design in which the tool layer were the control could not express this: at the moment it runs there is no object yet. |

### 3.3 `anna` - the security parent carries the scope downward

| | |
|---|---|
| **Login** | `anna` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`pm:Ticket\`).get(\`pm:Ticket#title\`)` |
| **Expect** | ✅ the **two Alpha tickets**, and not Beta's. |
| **Why** | No role rule mentions tickets for a partner. A `<security-parents>` rule makes a ticket's project its security parent, and role resolution walks an object *and its security ancestors* - so her scope on Alpha reaches Alpha's tickets and nothing else. |

### 3.4 `ben` - a type-scope rule is a different thing from a tier

| | |
|---|---|
| **Login** | `ben` |
| **Prompt** | the same as 3.3 |
| **Expect** | ✅ **all three** ticket titles. |
| **Why** | `ticketReader` is on the security root *and* on every ticket, from a `<rule meta-element="pm:Ticket">` whose `<path>` is only a `<group-with-name>`. The root assignment is what the tool layer sees; the per-ticket assignment is what the model rights see. Delete the second and Ben holds every read tool and gets nothing from any of them - see [testbed.md](testbed.md#12-the-hinge). |

### 3.5 `ben` - per-attribute Read

| | |
|---|---|
| **Login** | `ben` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`pm:Ticket\`).get(\`pm:Ticket#body\`)` |
| **Expect** | ❌ **an empty list** - while 3.4 returned all three titles. |
| **Why** | The `<part name="pm:Ticket#body">` grant names `projectPartner` only. An attribute-level grant is consulted *after* the class-level check has passed and narrows it, so the ticket is readable and this one attribute of it is not. |
| **Pair with** | The same prompt as `anna` returns Alpha's bodies. Without that half, an empty list would prove nothing - a broken expression returns empty too. |

### 3.6 `ben` - a reader reads no project

| | |
|---|---|
| **Login** | `ben` |
| **Prompt** | `Use the TL-Script tool to evaluate: all(\`pm:Project\`).get(\`pm:Project#name\`)` |
| **Expect** | ❌ **an empty list**. Not an error. |
| **Why** | `pm:Project` grants `Read` to `projectPartner` only. Note the *shape* of the refusal: the engine's read semantics return nothing rather than raising, which is why an agent acting for such an account may report "there are no projects". |

### 3.7 `ben` - the one refusal the tool layer owns

| | |
|---|---|
| **Login** | `ben` |
| **Prompt** | `Use the TL-Script tool with commit to evaluate: new(\`pm:Ticket\`).set(\`pm:Ticket#title\`, "Reader was here")` |
| **Expect** | ❌ **DENIED**, loudly: *"executing TL-Script with withCommit=true requires the 'data.write' capability, which none of your roles grants."* |
| **Why** | `ticketReader` holds `script`, which reaches `executeTLScript` read-only; committing costs `data.write` on top. The model layer would have refused too - `pm:Ticket` grants `Create` to `projectPartner` - but this account does not get that far. The contrast with every other case here is the *volume*: an L1 refusal names what is missing, an L2 refusal is silent. |
| **Also check** | Re-run 3.4 - still three tickets, nothing was persisted. |

### 3.8 `anna` - the same commit, allowed

| | |
|---|---|
| **Login** | `anna` |
| **Prompt** | the same as 3.7 |
| **Expect** | ✅ **ALLOWED** in the chat - a new object. `projectPartner` holds `data.write` at L1 and `Create` on `pm:Ticket` at L2, and a top-level creation is checked in the security root's context, where she holds the role. |
| **Note** | **In the chat only.** The background harness of section 4 is capped by the `<background-allowlist>`, which withholds `data.write`, and refuses the same request. |

### 3.9 `cara` - roles add up, and reading is not writing

| | |
|---|---|
| **Login** | `cara` |
| **Prompts** | 3.4 (read every ticket), then `Use the TL-Script tool to evaluate: all(\`pm:Ticket\`).firstElement()..set(\`pm:Ticket#title\`, "mine now")` against an **Alpha** ticket |
| **Expect** | ✅ all three titles, ❌ the edit refused. |
| **Why** | She holds both roles, so she reads every ticket as a reader. `Write` on `pm:Ticket` is granted to `projectPartner`, which she holds **on Beta** - so an Alpha ticket is out of reach. A reader is not a writer, and a partner of Beta is not a partner of Alpha. |

### 3.10 `dora` - the failure mode

| | |
|---|---|
| **Login** | `dora` |
| **Prompts** | run `probe.tlscript`, then 3.1 |
| **Expect** | the same probe decisions as `anna` - the read capabilities `ALLOWED`, the ones no persona role holds `DENIED` - differing only in what the allowed calls return, `totalCount 0` where hers says 1. Then an **empty** result. |
| **Why** | She holds `projectPartner` on the security root and partners on no project. So she has every read and write tool, and there is no object any of them may touch. **Nothing in the answer tells her that projects exist.** |
| **The point** | This is the drift the tier/scope split makes possible - two assignments that can disagree - and it argues for the boot-time warning of issue #289, not for a different architecture. See [testbed.md](testbed.md#3-the-personas). |

### 3.11 Nobody deletes anything

| | |
|---|---|
| **Login** | any persona, `anna` included |
| **Prompt** | `Use the TL-Script tool with commit to evaluate: all(\`pm:Ticket\`).firstElement().delete()` |
| **Expect** | ❌ **DENIED**, even for a ticket of the persona's own project. |
| **Why** | Neither type has a `Delete` grant, so the role set for that operation is empty and a check against an empty role set is false. A `Write` grant is not a licence to delete, and no `<deny>` element was needed. |

### 3.12 `root` - the administrator baseline

| | |
|---|---|
| **Login** | `root` |
| **Prompts** | All of the above. |
| **Expect** | ✅ **ALLOWED** throughout, every project and every attribute. |
| **Why** | Administrators bypass `ModelAccessRights` - the engine's own behaviour, unchanged by this scaffold. Which is why **running these cases as `root` proves nothing.** |
| **Note** | `root` also sees every other tab, because the component check is bypassed too. A demo account seeing two tabs where `root` sees all of them is the correct contrast, not a defect. |

### 3.13 Clean-up between runs

Cases 3.7 and 3.8 change data. To reset, run [`harness/reset.tlscript`](harness/reset.tlscript) as
`root`, or start with a fresh database (`tl-ai-demo/tmp`). Run the reset script twice to verify it: the
second run must report `none` on both of its lines. See
[testbed.md](testbed.md#23-reset-between-scenarios).

---

## 4. Running the cases as background agent runs from TL-Script

Section 3 is a walkthrough for a human typing into a chat. The same cases can be driven from the
**Script console** instead, which makes the whole matrix repeatable: one script starts a background
agent run per case, a second one collects status, result and expectation side by side.

The scripts live next to this document:

| File | Purpose | Needs an LLM key |
|---|---|---|
| [`harness/probe.tlscript`](harness/probe.tlscript) | prints the **tool**-layer decision for the logged-in persona as one string, one line per probed capability (`interaction` is not probed - it would ask the tester a question) | no |
| [`harness/fire.tlscript`](harness/fire.tlscript) | starts one background agent run per case and remembers the handles | yes |
| [`harness/collect.tlscript`](harness/collect.tlscript) | reads status, result and expectation back, plus the post-conditions | no |
| [`harness/reset.tlscript`](harness/reset.tlscript) | puts the seeded data back the way it was (run as `root`) | no |

`fire.tlscript` also carries four tool-layer cases (`T1` to `T4`) that ask a persona for something it
lacks the capability for, and are judged on whether the agent **names** the missing capability. The
deterministic version of those assertions is the `SecDemo*` JUnit suite; see
[testbed.md](testbed.md#7-the-deterministic-backbone).

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

Running as `root` is only useful as the deliberate administrator baseline of case 3.12: the same
prompts, expected to be allowed throughout. For everything else, log in as one of the four personas.

### 4.3 The Script console grant

Every persona can reach the console because the aspect grants the `Read` command group on the three
nested views listed in [1.1](#11-component-access-why-the-accounts-see-a-tab-at-all) to every persona
role.
Without it they cannot run TL-Script at all: the shipped demo has no `WEB-INF/conf/security.xml`, so
no role holds a command group on any view, and the console is invisible.

Only `Read` is granted. That covers opening the console and its **execute** button; the
**execute and save changes** button is bound to the `Write` command group and stays disabled for the
demo accounts (its tooltip says *"(Keine Berechtigung)"*). **So no expression a persona runs in the
console can persist anything**, and neither can a tool it reaches through `aiCallTool`: the plain
**execute** button evaluates inside `KnowledgeBase.withoutModifications(...)`, so a write that the
policy and the model rights both allowed still ends in the engine's `Illegal modification access` - see
[6.1](#61-driving-it-from-the-script-console). The agent runs the harness starts are not affected -
they write in their own transaction on the background thread.

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

1. Log in as the persona whose cases you want to see - `fire.tlscript` selects the cases of whoever is
   logged in, so `anna`, `ben`, `cara` and `dora` each run their own subset.
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
== 3.1  [anna]  status: COMPLETE
   expected : ALLOWED, and EXACTLY ONE name: Alpha. Anna partners on Alpha and not on Beta, ...
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
   pm:Project names readable here : Alpha
   pm:Ticket titles readable here : Alpha: cut the release branch, Alpha: fix the login timeout
   pm:Ticket bodies readable here : 2 of them - expected for a project partner, ...
   case 3.2 must not have persisted 'Anna was here' : OK
   the accepted boundary (issue #291): unfiltered count 3 vs filtered count 2 - ...
```

The first two lines are the sharpest single check in the harness, and they are a **pair**: as `anna`
the project line says `Alpha` and the ticket line names Alpha's two tickets; as `dora` both are
`(none)`; as `ben` the projects are `(none)` and all three tickets are there. Any of those on its own
proves nothing.

The last line is the boundary of the read-filtering guarantee (**issue #291**): a count computed inside
a script is a number by the time any filter sees it, so `unfiltered count 3` is what `anna` gets even
though she may read two of the three tickets. That is accepted and documented, not a finding - see
[testbed.md](testbed.md#53-read-filtering---what-a-permitted-tool-then-discloses-issue-289) scenario
**D6**.

The `case 3.2` line says `OK` for every account, `root` included: these runs are unattended and the
`<background-allowlist>` does not admit `data.write`, so no commit reaches the database from here. A
`yes` means a chat run persisted the note earlier - `reset.tlscript` clears it.

> Every harness script returns its report as a **single multi-line string**, so the console shows one
> value rather than a row per line - which is what makes the whole report copyable in one piece and two
> personas diffable. The **Result** column is narrow by default: drag the right border of the column
> header, or maximize the result panel with the ⛶ button, to read the report in full.

### 4.6 Re-running

No write case leaves anything behind here: 3.2 is refused because these runs are unattended and cannot
commit. Only the chat persists a ticket, and it then accumulates over runs.
`harness/reset.tlscript`, executed as `root` with the **execute and save changes** button, deletes every
ticket that is not one of the seeded ones and restores any seeded one that went missing. A fresh database
(`tl-ai-demo/tmp`) is the other way.

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

`tl-ai-demo/src/test/java/com/top_logic/ai/demo/SecDemoModelSecurityTest.java` covers one test per row
of section 3, and two further classes stand next to it:

| Class | What it adds |
|---|---|
| `SecDemoRoleRuleTest` | that the declarative rules produced the roles **at all** - the substrate every other assertion stands on. If this and the walkthrough both fail, start here |
| `SecDemoReadFilterTest` | the same rights seen through the data tools rather than through TL-Script: a filtered listing, a filtered `totalCount`, an omitted attribute, a "not found" for an object that exists |

The test application enables the **same** aspect file
(`src/test/webapp/WEB-INF/conf/metaConf.txt`), so the tests and the shipped configuration cannot drift
apart.

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

The aspect configures a `<tool-access>` for the **two** roles of the fixture, described in full in
[testbed.md](testbed.md#3-the-personas):

| | `ticketReader` | `projectPartner` |
|---|---|---|
| `model.read`, `data.read` | granted | granted |
| `script` (privileged) | granted | granted |
| `data.write` | **not granted** | granted |
| `agent.meta`, `agent.authoring`, `model.structure`, `egress`, `workspace` | not granted | not granted |

Both keep `script` deliberately: the cases in sections 3 and 4 ask the agent to use the TL-Script tool in
order to reach the model rights, and denying it here would stop them before the layer they are about.

`data.write` is the **only** difference, and it is the only refusal in this whole document that the tool
layer owns: `script` reaches `executeTLScript` read-only, and `withCommit=true` requires `data.write` as
well, so `projectPartner` may persist what a script changes and `ticketReader` may not, before any model
right is consulted.

That the two roles differ in so little is the point rather than a thinness of the fixture. **Anna and
Dora do not differ here at all** - same role on the security root, same grants, same answer for every
tool - and they differ in everything at the model layer, because one of them partners on a project. If
you want to see what this layer *can* decide, that is the row to run in both directions.

### 6.1 Driving it from the Script console

No LLM key is needed: `aiCallTool` dispatches a tool directly and the permission check happens before
any model call. Log in, open *Administration → Development → Script Konsole*, and run one line with the
plain **execute** button.

> ⚠️ **The plain execute button is read-only, so no persona commits anything here.** It evaluates inside
> `KnowledgeBase.withoutModifications(...)`, and the personas hold only `Read` on the console, so
> **execute and save changes** is disabled for them. The rows below therefore end in a *tool-layer*
> refusal, or in the console's own `Illegal modification access` once the tool layer and the model rights
> have both allowed the call - which is the ALLOWED signal here. See
> [testbed.md](testbed.md#4-the-four-harness-scripts) for the three messages and which layer produces
> each, and its scenarios **T2** - **T6** for the same rows with the reasoning.

| Login | Expression | Expect |
|---|---|---|
| `ben` | `aiCallTool("secdemo-editing.addTicket", {"project": "<Alpha's TID>", "title": "x"})` | ❌ **DENIED at L1** - `Permission denied: tool 'secdemo-editing.addTicket' requires the 'data.write' capability, which none of your roles grants.` |
| `anna` | the same expression | **ALLOWED by both L1 and L2**, then `Error executing tool: Illegal modification access. Currently no changes are allowed.` - the console, not the policy. With **Beta's** TID she gets `Object with TID '...' not found` instead, because L2 does not let her see that project |
| either | `aiCallTool("model-modification.createClass", null)` | ❌ **DENIED** - `requires the 'model.structure' capability`. Deny-by-default: no role is granted that privileged group, and no `<deny>` was needed |
| either | `aiCallTool("secdemo-unclassified.countTicketsUnclassified", null)` | ❌ **DENIED** - `belongs to no tool group and has no explicit grant`. A tool nobody classified is denied to everyone. A name nothing *registers* is a different answer: `Error: Tool '...' not found` |
| `ben` | ``aiCallTool("script-exec.executeTLScript", {"script": "new(`pm:Ticket`)..set(`pm:Ticket#title`, \"x\")", "withCommit": true})`` | ❌ **DENIED at L1** - `executing TL-Script with withCommit=true requires the 'data.write' capability, which none of your roles grants.` The same call with `"withCommit": false` succeeds |
| `anna` | the same expression | **ALLOWED at L1** - and then `This context '...ImmutableDBContext@...' can not be committed`, the console again |
| `root` | the two read-only rows | ✅ **ALLOWED** - administrators bypass the tool layer exactly as they bypass the model rights. The two write rows reach `Illegal modification access` for him too, until he presses **execute and save changes** - which is the only way a write lands from this surface. Clean up with `reset.tlscript` |

The reader/editor pair on the *same* expression in the *same* application is the point: the only thing
that differs is which role the account holds, and Ben's refusal names a capability where Anna's names
none.

### 6.2 Reading the boot log and the audit log

At boot, in `tmp/app-data/logs/top-logic.tl-ai.log`:

```
AiToolAccessService - Agent tool access: roles=2 baseline=0(+interaction) backgroundAllowlist=3
```

With the aspect switched off it reads `roles=0 baseline=default[model.read, data.read]`, and then every
account holds the shipped read-only default and nothing else - enforcement is unconditional, so an
absent grant table grants no write, script or egress capability to anyone.

Every denial is logged at `WARN`, naming the tool, its group, the account, the roles it holds and the
rule that decided:

```
WARN ToolAuthorization - Tool access denied: tool='secdemo-editing.addTicket'
group=data.write user=ben roles=[SecurityStructure.user, ticketReader]
reason=Permission denied: tool 'secdemo-editing.addTicket' requires the 'data.write'
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
  [testbed.md](testbed.md#56-the-pre-commit-guard-l4).

- **Result read-filtering in the data tools** (issue **#289**): `listInstances` and `getInstance` now
  consult the same model rights TL-Script does, through the one `ReadFilter` every
  `TLObject`-to-result conversion goes through. An instance the account may not read is absent from a
  listing and absent from its `totalCount`; an attribute it may not read is absent from a description;
  an object it may not read answers exactly as one that does not exist. `SecDemoReadFilterTest` is the
  deterministic form, and it asserts each row as a *pair* - the reader sees the data, the author sees
  none of it - because an empty result on its own would also be what a broken tool returns.

**It does not test (staged, not wired on this branch):**

- **Filtered aggregates and labels.** Per-object read filtering covers **objects and attribute
  values**; a value that TL-Script has already reduced to plain data is outside it, and there are two
  such values. ``all(`pm:Ticket`).size()`` counts unfiltered, because a count is a number by the time
  any filter sees it (issue **#291**). ``all(`pm:Project`).map(p -> toString($p))`` returns the project
  **labels** to a persona who may read neither project, because the coercion resolves the label through
  `MetaLabelProvider`, which is deliberately not access-checked (issue **#296**) - intended engine
  behaviour that will not change, since object labels are non-secret by design and the modeller must
  keep confidential information out of object names. Both are accepted rather than fixed, and both are
  asserted in `SecDemoReadFilterTest` so they are not rediscovered as findings. An observation that a
  persona learned *how many* tickets exist, or what a project is *called*, is therefore expected
  behaviour; an observation that it learned a ticket's `body` is a finding. Two controls, both the
  caller's: ``filterSecurity(all(...))`` inside the script, or withholding `script`.
- **The MCP resource surface.** `tl-ai-mcp-server`'s `InstanceResource` and `TypeInstancesResource`
  serialize instances the way the two tools used to. They are closed today only because `/mcp/*`
  authenticates nobody (issue **#261**) and are not a valid surface for per-user enforcement either
  way.
- **Row-level policies beyond a derived role**: the fixture derives a role from one reference
  attribute. A rule navigating several steps, an `inheritance`-typed rule, and a rule keyed on a
  classifier are all separate shapes and are not exercised here.
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
  `false`, **and this fixture leaves it there** - which is the whole reason it can demonstrate
  per-object scoping. The consequence to internalise is the one that reads as a defect and is not: a role
  granted on the security root reaches **no** business object, so it satisfies the *tool* layer and
  grants no data access whatever. Every per-object and per-type statement therefore needs a role rule
  that produces the role on the objects themselves. See
  [testbed.md](testbed.md#12-the-hinge) and
  [role-model-worked-example.md](role-model-worked-example.md) section 3.4.1.
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
