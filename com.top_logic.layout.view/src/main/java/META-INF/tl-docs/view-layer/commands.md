---
description: Read before writing commands in a .view.xml - action chains and branching, toolbar groups (command cliques), <create-transient>, <persist-transient>, <delete-object>, and long-running jobs with <start-job> / <job-status>.
order: 50
---

# Commands and actions

## A command is a chain of actions, and the chain can branch

`<generic-command>` (`GenericViewCommand`) runs the `<execute-script>`, `<store-form-state>`, `<confirm>`, `<notify>`, `<verify-identity>`, `<with-transaction>`, `<open-dialog>`, `<write-channel>`, … actions written inside it as one chain (`ViewActionChain`): each action's result is the next action's input, the first action gets the command's input. An action that has to wait — `<confirm>`, which opens a dialog — suspends the chain and resumes it from the dialog's answer, or aborts it on cancel; `<verify-identity>` (`VerifyIdentityAction`) is the same shape with the answer being the proof that the person at the keyboard still is the holder of the session's own account, given the way the session was established: a session established at an external identity provider re-authenticates there in a second browser window — the provider's answer returns to the authentication servlet, which completes the pending `IdentityVerifications` entry and resumes the chain in the window that asked (a failure of the resumed chain is shown in that window like any other command failure, through `CommandErrors`, and leaves the confirmed identity itself standing) — while every other session is asked for its password, which the account's `AuthenticationDevice` checks. It fails closed: a cancelled prompt, an account that neither a provider nor a device can confirm, or a context without a dialog all abort; an abort skips the remaining actions and runs the compensations the executed actions registered, newest first, as does a failure. A script over the chain's value takes further arguments from channels: `<inputs><input channel="context"/></inputs>` puts those channel values in front of the chain's value (`ActionScript`, shared by every action that takes a script).

`<notify>` (`NotifyAction`) tells the user something from inside the chain. Its `expr` computes the message over the chain's value, with channel values in front of it through `<inputs>`, exactly like every other script of an action; a result of `null` or an empty text is nothing to say, and the chain passes its value on untouched — so the message itself decides whether the user hears anything. `kind="info|warning|error"` (`info` by default) says how serious the notice is, `display="snackbar|dialog"` (`snackbar` by default) where it is read: a snackbar passes by beside the user's work and is shown synchronously, so it may sit inside a `<with-transaction>`, while a dialog is a single-OK message that suspends the chain until it is acknowledged, exactly as a `<confirm>` does, and therefore may not (a chain running headless has no dialog to open and falls back to the snackbar). `stop="true"` ends the chain after the notice: the compensations of the actions before it run, the remaining actions are skipped, and nothing is logged or reported beyond the message — the notice *is* the outcome. That is what separates it from a failure raised by the TL-Script `throw(#('…'@en, '…'@de))` inside an `<execute-script>`, which travels the error path: it is logged and reported through `CommandErrors` like any other failure of the command.

```xml
<execute-script function="name -> all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#name`) == $name).firstElement()"/>
<if test="t -> $t != null">
  <then>
    <write-channel name="ticket"/>
  </then>
  <else>
    <notify kind="warning" stop="true" expr="term -> x -> #('No ticket {0}.'@en, 'Kein Ticket {0}.'@de).fill($term)">
      <inputs>
        <input channel="jump"/>
      </inputs>
    </notify>
  </else>
</if>
```

Two actions branch the chain by a TL-Script function over its current value. `<if>` decides between two chains; `<switch>` computes a switch value with its `value` function (the chain's own value when no `value` is configured) and gives it to the `<case>`s, each of which either names the value it stands for with `match` or decides with a `test` predicate:

```xml
<if test="ticket -> $ticket != null">
  <then>
    <write-channel name="ticket"/>
  </then>
  <else>
    <create-transient type="demo.tickets:Ticket"/>
    <open-dialog bind-input-to="model" dialog-view="tickets-create.view.xml"/>
  </else>
</if>

<switch value="t -> $t.get(`demo.tickets:Ticket#status`)">
  <case match="`demo.tickets:TicketStatus#closed`">
    <with-transaction>
      <execute-script function="t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#open`)"/>
    </with-transaction>
  </case>
  <case test="s -> $s == null">
    <confirm expr="x -> #('The ticket has no status.'@en)"/>
  </case>
  <default>
    <with-transaction>
      <execute-script function="t -> $t.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/>
    </with-transaction>
  </default>
</switch>
```

- The chosen branch runs as a *nested* chain (`ViewActionChain.nest`): it starts with the chain's current value, and the result of its last action becomes the value the enclosing chain continues with. A branch that is not configured — a missing `<else>`, a `<switch>` without a matching case and without a `<default>` — passes the value through unchanged, so a branch never breaks the chain.
- An abort inside a branch aborts the whole command, and a compensation registered inside a branch takes its place in the enclosing chain's unwind: it runs whenever the command is later aborted or fails, before the compensations of the actions preceding the branch. A `<confirm>` therefore works inside a branch exactly as beside it, including the suspension: the branch may resume long after the command returned.
- `<if>` reads its condition in the fuzzy sense of TL-Script, so an object stands for a true condition and nothing (`null`, an empty list, an empty text) for a false one. A `<case match="…">` holds a TL-Script expression without parameters and matches when the switch value equals its value under the TL-Script comparison (`==`), so a classifier is written as `` `module:Enumeration#literal` ``, a text as `'text'` and a number as the number it is. A `<case test="…">` holds a predicate that is called with the switch value and read in the same fuzzy sense as an `<if>` condition; a case configures exactly one of the two. Without a `value` function the switch value is the chain's own value, so `<switch><case test="t -> $t == null">` decides on what the chain carries.
- A command whose chain applies the entered form values is disabled while the form has errors — a branch reports that for the actions of *all* its branches, taken or not, because the button's state cannot depend on the decision.
- **`<executability>` guards the command with rules over its input**: `<visible-if expr="…"/>` hides the command while its predicate does not return `true`; `<disabled-if expr="…"/>` keeps it visible but disabled and takes the reason from its function — no value or `false` means executable, `true` disables it with a generic reason, a resource key or a text disables it with that reason, which the button shows as its tooltip. A rule that inspects objects beyond the input object needs those types in the command's `observed-types`, otherwise their changes do not re-evaluate it.

## Toolbar groups: command cliques

A command names its clique (`clique="…"` on any view command, default `create`); the commands of one clique form one group of the toolbar (`ToolbarBuilder`). Which cliques exist, in which order their groups are displayed, and whether a group is shown inline or folded into a menu is application configuration of the `CommandCliqueService` — the standard cliques are configured in `tl-layout-view.conf.config.xml`: `create`, `edit`, `delete`, `commit`, `navigate` inline, then `view`, `export` and `more` as menus. A menu clique carries a `label` (a `ResKey`, resolved in the user's language when the toolbar is built) and optionally an `icon` (a `ThemeImage`); a menu with an icon shows the label as the trigger's accessible name, one without shows it as the trigger's text.

An application adds or relabels a clique in its own configuration; entries are keyed by `name`, so an entry with an existing name overrides that clique, and a new name is appended (or placed with `config:position`/`config:reference`):

```xml
<config service-class="com.top_logic.layout.view.command.CommandCliqueService">
	<instance>
		<cliques>
			<clique name="more" display="menu" icon="css:bi bi-three-dots">
				<label><en>Actions</en><de>Aktionen</de></label>
			</clique>
			<clique name="report" display="menu"
				config:position="before" config:reference="more"
			>
				<label><en>Reports</en><de>Berichte</de></label>
			</clique>
		</cliques>
	</instance>
</config>
```

A clique that is not configured is not an error: its commands form an inline group without label after the groups of all configured cliques.

## Creating and deleting objects: `<create-transient>`, `<persist-transient>`, `<delete-object>`

Three actions perform the model operations of a create dialog and a delete button. Each enforces the model access right of its operation like the TL-Script function it corresponds to, and brings the matching executability rule itself (`ViewAction#getIntrinsicRule()`), so the command offering it is hidden or disabled before the operation would fail — no `<executability>` configuration for the right is needed. The rule decides on the *command's* input, not on the value the chain hands to the action (see `ModelAccessRule` and `ModelAccessPolicy` for hide vs. disable).

- **`<create-transient type="…" [container="ch" reference="attr"]/>`** (`CreateTransientAction`) results in a transient object of the type — the draft the dialog edits, as `new(type, transient: true)` creates it; its input is ignored. Its rule is the right to create an object of the type: without `container` against the security root (refused → hidden), with `container` in the context of the channel's object and, with `reference`, together with Write on that reference (refused → disabled). `container`/`reference` serve the check only; the dialog gets the container through the `<open-dialog>` bindings.
- **`<persist-transient [type="…"] [container="ch" reference="attr"]/>`** (`PersistTransientAction`) makes the transient object it receives persistent the way `$draft.copy(transient: false)` does (values and composition parts; a refusal reports a refused *creation*), in the context of the container if one is given, and with `reference` adds the created object to that reference of the container, the way `$container.add(reference, $created)` does including its Write check. It runs in a transaction of its own and results in the persistent object. Its rule is the same creation check; the created type is `type` if given, else the reference's type, else the type of the command input — so the dialog's Create button binds its input to the draft: `input="model"`.
- **`<delete-object/>`** (`DeleteObjectAction`) deletes the object (or the objects of a collection) it receives the way `delete()` does, compositions included, in a transaction of its own, and results in `null`. Its rule is Delete on the command input (refused → disabled with "You may not delete this object.", hidden when no role may ever delete the type), combined with `<delete-veto-disabled/>` (`DeleteVetoDisabled`): an input whose `TLObject#tDeleteVeto()` refuses the deletion disables the command with the veto as reason. A command deleting by script configures `<delete-veto-disabled/>` explicitly.

A transaction nested in a `<with-transaction>` commits with it, so the actions compose with further script steps in one transaction. A command whose effect is a free script uses the general rule instead: `<model-access operation="…"/>` in `<executability>`; `<model-access operation="Create"/>` without a `type` checks the creation of an object of the command input's type.

A `<model-access>` check on a concrete object (explicit, or the rule an action brings) also asks the global `CommandApprovalService` (without component and command ID) once the access rights allow the operation: the approval checks configured for the object's type — e.g. that the anonymous account is neither edited nor deleted — disable the command with the approval's reason, as in the classic UI.

The opener and the dialog of a creation in a container:

```xml
<!-- opener, e.g. in the list toolbar -->
<generic-command image="css:bi bi-plus-lg" placement="TOOLBAR">
  <create-transient type="tl.demo.projectManagement:Milestone" container="selectedScope" reference="milestones"/>
  <open-dialog bind-input-to="model" dialog-view="demo/create-milestone.view.xml">
    <bind channel="container" to="selectedScope"/>
    <bind channel="selection" to="selectedMilestone"/>
  </open-dialog>
</generic-command>

<!-- dialog: the form edits the draft in "model" -->
<generic-command image="css:bi bi-check-lg" input="model" placement="BUTTON_BAR">
  <store-form-state/>
  <persist-transient container="container" reference="milestones"/>
  <write-channel name="selection"/>
  <close-dialog/>
</generic-command>
```

A top-level creation omits `container` and `reference` on both actions. Deleting the selected object:

```xml
<generic-command image="css:bi bi-trash" input="project">
  <executability>
    <null-input-disabled/>
  </executability>
  <delete-object/>
  <write-channel name="project"/>
</generic-command>
```

## Long-running jobs: `<start-job>` and `<job-status>`

Work that takes longer than a request may take does not belong in the request. `<start-job>` (`StartJobAction`) is the action that hands it to a worker thread, publishes what it reports on a channel, and **suspends the command** until the work has ended — the same suspension a `<confirm>` uses, so the chain simply continues afterwards:

```xml
<action class="com.top_logic.layout.view.command.GenericViewCommand" input="job">
  <executability>
    <disabled-if expr="s -> if(jobIsRunning($s), #('A job is already running.'@en), null)"/>
  </executability>
  <start-job job="job" cancelable="true" update-interval="200">
    <phases>
      <phase name="read"><label><en>Reading</en><de>Lesen</de></label></phase>
      <phase name="check"><label><en>Checking</en><de>Prüfen</de></label></phase>
    </phases>
    <function><![CDATA[job -> x -> {
	$job.jobPhase('read');
	$job.jobMessage(#('Reading the records.'@en));
	count(1, 6).foreach(i -> { sleep(400); $job.jobProgress($i, 5); });
	$job.jobPhase('check');
	$job.jobIndeterminate();
	sleep(1500);
	#('5 records processed.'@en);
}]]></function>
  </start-job>
  <write-channel name="report"/>
</action>
```

- **The channel carries immutable snapshots.** The `job` channel holds a `JobState` from the moment the job starts, and a *new* one on every report, so nothing a display would have to observe ever changes. `update-interval` is the shortest time in milliseconds between two published snapshots: a job counting thousands of items is followed at that pace instead of flooding the browser, the last report of a burst is never lost, and the snapshot that ends the job is always delivered. The body runs in the sub-session of the starting request and publishes under the window's interaction, so the channel write, the controls updating from it and the updates reaching the browser are serialized against the requests of the same session exactly like a command is.
- **The work runs outside any transaction, on a thread that serves no request.** Persisting what the job produced is the business of the actions *after* it: the command continues where it left off with the job's result as its value, so a `<with-transaction><execute-script .../></with-transaction>` or a `<write-channel>` behind the `<start-job>` is where the result lands. A job that fails or is cancelled **aborts** the command instead — the remaining actions are skipped, the compensations of the ones before it run, and the failure stays visible in the last state of the job rather than in a snackbar.
- **The body is a TL-Script `function=` or a Java `<body class="…"/>`**, exactly one of the two. The function is called with the **monitor of the job as its first argument**, followed by the `inputs` channel values in declaration order and the command's own value last. It reports with `$job.jobPhase('name')` (entering a step marks the steps passed over as done; naming a step that was never declared ends the job with an error), `$job.jobPhases([…])` or `$job.jobPhases({name: label})` for a job that learns its steps only while running, `$job.jobProgress(done, total)`, `$job.jobIndeterminate()` and `$job.jobMessage(text)`. What the function returns is the result of the job.
- **Reading a snapshot** is `jobIsRunning($s)`, `jobIsFinished($s)`, `jobStatus($s)` (the texts `running`, `completed`, `failed`, `cancelled`, so a `<switch><case match="'completed'">` decides on it), `jobResult($s)` and `jobError($s)`. Each of them answers over no job at all as well, which is what the channel holds before the first start — so a start button guards itself with `input="job"` plus `<disabled-if expr="s -> jobIsRunning($s)"/>` and needs no case of its own for the time before the first run.
- **Cancellation is cooperative.** `cancelable="true"` offers the reader a cancel button; pressing it marks the job and interrupts the worker. `sleep()` keeps the interrupt it was woken by, so a sleeping job wakes at once and ends at the next point it *reports* from — which is what makes a loop of `sleep` + `jobProgress` stop within one step. Every report a Java body makes on its `JobMonitor` checks the same way, and `JobMonitor.checkCancelled()` is that check on its own for a stretch of work that reports nothing. Only declare it for work that may be given up half-done: a cancelled job has done part of what it was started for.
- **`<job-status input="job"/>`** (`JobStatusElement` → `ReactJobStatusControl` / `TLJobStatus`) is the display, bound to the channel alone and holding no state of its own. It shows the status, the declared steps as done / active / pending, the bar (determinate or indeterminate), the message, the elapsed time — counted in the browser, so it ticks without a server round trip and freezes when the job ends — and at the end the result or the error. A channel holding anything that is not a job state displays nothing. Every text is resolved for the reader on the server: the phases and the message by their `ResKey`, the result through `MetaLabelProvider`, so a body returning an i18n literal `#('…'@en, '…'@de)` is displayed in the reader's language.
- **CSS hooks**: the BEM block `tlJobStatus` with the status modifier `tlJobStatus--running|completed|failed|cancelled` and the elements `__header`, `__state`, `__elapsed`, `__cancel`, `__phases`, `__phase` (`--done`, `--active`, `--pending`), `__bar`, `__message`, `__error`, `__result` (`tlReactControls.css`). An application restyles the display through these classes; the bar inside it is the design system's `tl-progress` (`progress.css`), addressed through its own classes `tl-progress__track|__fill|__label`, never through `tlJobStatus`.
- **Demo**: `com.top_logic.demo.react/…/views/demo/long-job-demo.view.xml` — a three-phase job with a determinate loop, an indeterminate phase and a result written to a second channel, a failing job, and a standalone indeterminate `<progress>`, plus a chunked import creating 500 tickets in one pass and closing every second of them in a next one, and the chunked removal of what it created.

### Committing in chunks: `ChunkedScriptJobBody`

A job that *creates persistent objects* runs outside any transaction and TL-Script opens none, so the body needs a transactional frame. `<body class="com.top_logic.layout.view.job.ChunkedScriptJobBody" chunk-size="200">` is that frame written in configuration; `ChunkedJobBody` is the same frame for a body written in Java, with the hooks `hasInit()/init`, `elements`, `stepCount()/step` and `hasFinish()/finish`:

```xml
<start-job job="importState" cancelable="true">
  <body class="com.top_logic.layout.view.job.ChunkedScriptJobBody" chunk-size="200">
    <init-label><en>Reading the file</en><de>Datei einlesen</de></init-label>
    <init><![CDATA[job -> file -> { s = new(`my:Import`, transient: true); $s.set(`my:Import#rows`, $file.parse()); $s; }]]></init>
    <elements><![CDATA[job -> state -> $state.get(`my:Import#rows`)]]></elements>
    <steps>
      <step>
        <label><en>Creating the records</en><de>Datensätze anlegen</de></label>
        <expr><![CDATA[job -> chunk -> state -> $chunk.foreach(r -> $state.get(`my:Import#target`).create($r))]]></expr>
      </step>
      <step>
        <expr><![CDATA[job -> chunk -> state -> $chunk.foreach(r -> $r.resolveReferences())]]></expr>
      </step>
    </steps>
    <finish><![CDATA[job -> state -> $state.get(`my:Import#created`)]]></finish>
  </body>
</start-job>
```

- **Every script is called with the monitor of the job first**, exactly like the `function=` body: `init` as `job -> a -> b -> …` (the values the job was started with), `elements` as `job -> state -> …`, a `<step>` as `job -> chunk -> state -> …` and `finish` as `job -> state -> …`. So every one of them reports with `$job.jobMessage(…)`, `$job.jobProgress(…)` and friends.
- **The state ties the scripts together.** What `init` returns is what `elements`, every pass and `finish` receive; without an `<init>` the state is the *first value the job was started with* (the first `inputs` channel, or the command's value where there is none). For a state that has to change while the job runs, make it a transient object — `new(\`my:Import\`, transient: true)` — whose attributes the passes set; several objects the passes need are a map literal `{'target': $t, 'index': $byKey}`.
- **`elements` is evaluated once, read-only and outside any transaction**, against the state. A collection is the list of work items, any other value is the single item it stands for, nothing at all is no items. The whole list is held for the run, so what it selects has to fit in memory — the chunking bounds the *transactions*, not the list.
- **Every `<step>` is a full pass over that list**, applied to successive chunks of `chunk-size` items (200 by default), each chunk in a transaction of its own. A pass begins once the pass before it has committed every chunk, which is what makes a *second* pass the place for work that needs all the items of the first one — resolving cross references between them, for instance.
- **A chunk that fails is retried item by item**, each item in a transaction of its own; only the items that genuinely cannot be processed are skipped, each of them logged and reported as a message naming the item and the failure, and counted. A step script therefore has to be **repeatable for an item it already saw** in the failed chunk. A failure in `init` or in `finish` is *not* caught: it ends the job with its own message, and its transaction is given up with it.
- **Cancellation takes effect between two committed chunks**, and at once at a report from *inside* a chunk: the chunk in progress is rolled back, the chunks that committed stay, and the job ends as cancelled rather than counting the item as one that could not be processed.
- **The phases are the steps of the job**: `init` where there is one, `step-1` … `step-n`, `finish` where there is one — announced by the body itself, so `<start-job>` needs no `<phases>` for it. `<init-label>`, a `<step>`'s `<label>` and `<finish-label>` name them for the reader; unnamed, a pass is shown as its number. The progress within a pass counts its chunks.
- **The result of the job is what `finish` returns**; a body without a `<finish>` ends with the text saying how many items it processed and how many it skipped — which the job reports as its last message either way.
