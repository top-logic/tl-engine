---
description: Read before writing commands, drops or other action chains in a .view.xml - the named arguments of a chain (command inputs with as, the value names of triggers such as activate-as, submit-as, link-as), actions with a result and their follow-up actions in <with-result> (result, as), transactions with <with-transaction>, value= on single-value actions, scripts using the arguments as variables, <write-channel clear>, branching with <if> / <switch>, <notify>, toolbar groups (command cliques), <create-transient>, <persist-transient>, <delete-object>, and long-running jobs with <start-job> / <job-status>.
order: 50
---

# Commands and actions

## A command is a chain of actions over named arguments

`<generic-command>` (`GenericViewCommand`) runs the `<execute-script>`, `<store-form-state>`, `<enter-edit-mode>`, `<leave-edit-mode>` (which switch the edit mode of the form running the command, see [saving with an action chain](doc:view-layer/forms#saving-with-an-action-chain)), `<confirm>`, `<notify>`, `<verify-identity>`, `<with-transaction>`, `<open-dialog>`, `<write-channel>`, `<download>`, … actions written inside it as one chain (`ViewActionChain`). A drop, an upload, an activation and every other trigger of a chain runs its actions the same way.

**The arguments of a chain are named values** (`ActionArguments`), and **they flow unchanged through the chain**: every action of the chain sees the same arguments, whatever the actions before it produced. Which names exist is decided by what triggered the chain:

- **A command's `inputs`** — one argument per channel, named after the channel: `inputs="selection as ticket, editMode"` or `<inputs><input channel="selection" as="ticket"/><input channel="editMode"/></inputs>` (`NamedInputs`; `as` renames the argument). The first entry is the **main argument**, the value the command's rules deciding over a single value decide over; its script rules see all arguments (see [branching](#branching-if-and-switch)).
- **A trigger that supplies a value itself** names it; the value is the main argument, and the command's `inputs` are available beside it. Each trigger has a default name and a property renaming it:

  | trigger | name | renamed by |
  |---|---|---|
  | activation of a `<table>` / `<tree-table>` row, a row command column | `row` | `activate-as` on the table |
  | activation of a `<tree>` node | `node` | `activate-as` on the tree |
  | `<on-submit>` of a `<value-input>` | `value` | `submit-as` on the value input |
  | `<on-link>` of an `<html>` element | `link` | `link-as` on the element |
  | click into a chart (`ClickHandlerConfig`) | `element` | `as` on the click handler |
  | upload command (`UploadCommand`) | `file` | `as` on the command |
  | `<drop>` | `objects` plus its references `target` / `parent` / `before` | `as`, `target-as`, `parent-as`, `before-as` (see [drag and drop](doc:view-layer/tables#drag-and-drop-of-table-rows)) |

- **A command with neither inputs nor a trigger value** has the single argument `input`, whose value is `null` (`CommandArguments.DEFAULT_ARGUMENT`).

An action that has to wait — `<confirm>`, which opens a dialog — suspends the chain and resumes it from the dialog's answer, or aborts it on cancel. The dialog shows the message of a `<confirm>` with its line breaks, so a message is structured into lines with `\n` — the escape of a TL-Script string literal (`#('Delete milestone {0}?\nDue: {1}'@en)`), and equally a line break in a `ResKey` from Java; `<verify-identity>` (`VerifyIdentityAction`) is the same shape with the answer being the proof that the person at the keyboard still is the holder of the session's own account, given the way the session was established: a session established at an external identity provider re-authenticates there in a second browser window — the provider's answer returns to the authentication servlet, which completes the pending `IdentityVerifications` entry and resumes the chain in the window that asked (a failure of the resumed chain is shown in that window like any other command failure, through `CommandErrors`, and leaves the confirmed identity itself standing) — while every other session is asked for its password, which the account's `AuthenticationDevice` checks. It fails closed: a cancelled prompt, an account that neither a provider nor a device can confirm, or a context without a dialog all abort; an abort skips the remaining actions and runs the compensations the executed actions registered, newest first, as does a failure.

## Actions with a result: `<with-result>`

The result of an action does not reach the actions *following* it. An action with a useful result takes its **follow-up actions in its `<with-result>` element** (`WithResultActions`, `WithResult`); they run once the action has completed — including a commit the action does itself, so a follow-up action never sees an uncommitted object — with the arguments of the chain, extended by the result under the name `result`, or the name the `as` of `<with-result>` gives. The result is their default value. An inner name shadows an equal outer one. Once the follow-up actions are done, the chain goes on with the siblings of the action, which see the arguments without the result.

```xml
<generic-command inputs="model" placement="BUTTON_BAR">
  <store-form-state/>
  <persist-transient container="container" reference="milestones">
    <with-result>
      <write-channel name="selection"/>
      <close-dialog/>
    </with-result>
  </persist-transient>
</generic-command>
```

- **Actions with a result**: `<execute-script>` and `<evaluate-script>` (the value of the script), `<create-transient>`, `<persist-transient>`, `<read-form-object>`, `<store-form-state>`, `<link-element>`, `<remove-element>`, `<start-job>`, `<issue-access-token>`, `<create-account>`, `<set-password>`, `<with-transaction>` (the result of its last inner action, see [transactions](#transactions-with-transaction)) and the load actions of the administration views. Chains of producers nest: `<store-form-state><with-result><persist-transient><with-result><write-channel name="selection"/></with-result></persist-transient></with-result></store-form-state>`.
- An action with a result accepts follow-up actions only inside its `<with-result>`; an action written directly into it is a configuration error.
- **Actions without a result**: `<open-dialog>` (the chain goes on once the dialog is open), `<write-channel>`, `<close-dialog>`, `<close-popover>`, `<delete-object>`, and the containers `<if>` and `<switch>`. The actions inside a container run with the arguments the container sees; a sibling following the container does not see what happened inside, so an action working with a result produced inside a branch is nested there too.

## Transactions: `<with-transaction>`

`<with-transaction>` (`WithTransactionAction`) runs the actions written inside it in one transaction with the arguments of the chain, commits it with its `<commitMessage>` (filled with the label of the default argument) and results in the result of its last inner action. When an inner action throws, the transaction is rolled back and the command fails.

The actions in the transaction only read and modify the model. An action that changes the state of the view — `<write-channel>`, `<open-dialog>`, `<close-dialog>`, `<close-popover>`, `<notify>`, `<show-object>`, `<show-view>`, `<download>`, `<scroll-to-anchor>`, `<history-back>`, the wizard and tile-stack navigation, … (`ViewAction#changesViewState()`) — would keep its effect when the commit fails, e.g. a channel holding an object that was never created. Configured inside the transaction, it is reported as a configuration error when the view is loaded. It belongs to the `<with-result>` of the transaction, whose actions run only after a successful commit, with the arguments of the chain and the result of the transaction under the name `as` gives (`result` without one):

```xml
<generic-command inputs="model" placement="BUTTON_BAR">
  <store-form-state/>
  <with-transaction>
    <commitMessage><en>Created ticket {0}.</en><de>Ticket {0} angelegt.</de></commitMessage>
    <persist-transient>
      <with-result as="created">
        <execute-script function="$created.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/>
      </with-result>
    </persist-transient>
    <with-result>
      <write-channel name="selection"/>
    </with-result>
  </with-transaction>
  <close-dialog/>
</generic-command>
```

- The follow-up actions of an action *inside* the transaction run inside it as well, before the commit, so they are model operations too — like the `<execute-script>` setting the status above.
- The actions of the `<with-result>` of the transaction run outside the transaction, as its follow-up actions: they may suspend (a `<confirm>`, an `<open-dialog>`) and abort the chain, the committed changes stay. Their scripts see the result as a variable, and the script editor offers it.
- Inner actions must complete synchronously; a `<confirm>` or another suspending action inside a transaction fails, place it before the `<with-transaction>`.
- A `<with-transaction>` nested in the actions of another transaction commits with the outer one; its `<with-result>` then still runs inside the outer transaction and is restricted the same way.

## Actions processing one value: `value`

`<write-channel>`, `<open-dialog>` (the value it binds into the dialog), `<delete-object>`, `<persist-transient>`, `<show-object>`, `<show-view>`, `<download>`, `<scroll-to-anchor>`, `<wizard-goto>` and the other actions working on a single value (`WithValueArgument`) process the **default argument**: the innermost result of an enclosing action, or the main argument of the trigger outside of follow-up actions. `value="<name>"` selects another argument by name:

```xml
<generic-command inputs="selection as ticket, project">
  <execute-script function="$project.get(`demo.tickets:Project#lead`)">
    <with-result>
      <write-channel name="lead"/>                      <!-- the result -->
      <write-channel name="lastTicket" value="ticket"/> <!-- an argument by name -->
    </with-result>
  </execute-script>
</generic-command>
```

**`<write-channel name="…" clear="true"/>`** empties a channel instead of writing a value; it selects no argument, so `clear` and `value` exclude each other.

## Scripts use the arguments as variables

Every script of a chain — the `function` of `<execute-script>` and `<start-job>`, the `test` of `<if>`, the `value` of `<switch>` and the `match` / `test` of its cases, the `expr` of `<confirm>` and `<notify>`, the `file-name` of `<download>`, the `target` of `<scroll-to-anchor>`, `<evaluate-script>`, and the `refuse-if` of a `<drop>` — **uses the arguments defined at its position as variables, without declaring them** (`ActionScript`): `$ticket`, `$result`, `$objects`. A script has no lambda header; one that declares parameters anyway is a function and computes nothing.

- The names are known when the view is loaded: every trigger and every action with a result declares the names it defines, and the actions are compiled against the names of their position (`ActionScope`). A script using a name that is not defined there is a **configuration error** reported at load time; the script editor offers exactly the defined names for completion.
- **An action's own `inputs`** (`<execute-script>`, `<if>`, `<confirm>`, `<notify>`, `<download>`, `<start-job>`) add one variable per channel, named after the channel or its `as`, behind the arguments of the chain: ``<execute-script function="$context.add(`m:Container#items`, $ticket)" inputs="context"/>``.
- The `<bind expr>` and `label-expr` of `<show-view>` / `<show-views>` are scripts of the chain as well; a `<bind>` without `expr` receives the value the action processes.
- **Executability rules see the arguments of their command** in the same way: `<visible-if expr="$ticket != null"/>` and `<disabled-if expr="…"/>` use the command's inputs (named after their channels or their `as`) and the value its trigger supplies (the activated row, the uploaded file) as variables. The button state is computed from the same arguments the command runs with; before a trigger supplied its value, that argument is `null`. A rule guarding a single value of an element sees it under the name the element gives it: the dragged row of a table as `row` (`row-as`), the node of a tree as `node` (`node-as`), the card of a board as `element` (`element-as`), the target of a drop as `target` (`target-as`), the object of a `<form>` in its `edit-executability` as the form's `input` channel name, and the rules of a `<drag>` / `<drop>` as a whole the value of their `input` (with `as`).
- The scripts of channels and elements outside a chain see named variables as well: the values of their inputs and the values the element supplies (see [named inputs](doc:view-layer/channels#named-inputs-variables-instead-of-parameters)); [which names a script sees](doc:view-layer/channels#which-names-a-script-sees) lists them for every script of the view layer, the scripts of a chain included.

```xml
<value-input value="jump">
  <on-submit>
    <execute-script function="all(`demo.tickets:Ticket`).filter(t -> $t.get(`demo.tickets:Ticket#name`) == $value).firstElement()">
      <with-result>
        <if test="$result != null">
          <then>
            <write-channel name="ticket"/>
          </then>
          <else>
            <notify kind="warning" stop="true" expr="#('No ticket {0}.'@en, 'Kein Ticket {0}.'@de).fill($value)"/>
          </else>
        </if>
      </with-result>
    </execute-script>
  </on-submit>
</value-input>
```

`<notify>` (`NotifyAction`) tells the user something from inside the chain. Its `expr` computes the message over the arguments, exactly like every other script of an action; a result of `null` or an empty text is nothing to say — so the message itself decides whether the user hears anything. `kind="info|warning|error"` (`info` by default) says how serious the notice is, `display="snackbar|dialog"` (`snackbar` by default) where it is read: a snackbar passes by beside the user's work and is shown synchronously, while a dialog is a single-OK message that suspends the chain until it is acknowledged, exactly as a `<confirm>` does (a chain running headless has no dialog to open and falls back to the snackbar). Either way the notice changes the state of the view, so after a model change it is placed in the `<with-result>` of the transaction, where it reports only what was committed. `stop="true"` ends the chain after the notice: the compensations of the actions before it run, the remaining actions are skipped, and nothing is logged or reported beyond the message — the notice *is* the outcome. That is what separates it from a failure raised by the TL-Script `throw(#('…'@en, '…'@de))` inside an `<execute-script>`, which travels the error path: it is logged and reported through `CommandErrors` like any other failure of the command. A message shown as a dialog keeps its line breaks, as the message of a `<confirm>` does.

## Branching: `<if>` and `<switch>`

Two actions branch the chain by a script over its arguments. `<if>` decides between two chains; `<switch>` computes a switch value with its `value` script (the default argument when no `value` is configured) and gives it to the `<case>`s, each of which either names the value it stands for with `match` or decides with a `test` condition. The scripts of the cases see the switch value as the variable `value`, or the name the switch's `as` gives:

```xml
<generic-command inputs="ticket">
  <switch value="$ticket.get(`demo.tickets:Ticket#status`)" as="status">
    <case match="`demo.tickets:TicketStatus#closed`">
      <with-transaction>
        <execute-script function="$ticket.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#open`)"/>
      </with-transaction>
    </case>
    <case test="$status == null">
      <confirm expr="#('The ticket has no status.'@en)"/>
    </case>
    <default>
      <with-transaction>
        <execute-script function="$ticket.set(`demo.tickets:Ticket#status`, `demo.tickets:TicketStatus#closed`)"/>
      </with-transaction>
    </default>
  </switch>
</generic-command>
```

- The chosen branch runs as a *nested* chain (`ViewActionChain.nest`) with the same arguments as the branch action. A branch that is not configured — a missing `<else>`, a `<switch>` without a matching case and without a `<default>` — does nothing, so a branch never breaks the chain. An action that needs a result produced inside a branch is nested into that branch.
- An abort inside a branch aborts the whole command, and a compensation registered inside a branch takes its place in the enclosing chain's unwind: it runs whenever the command is later aborted or fails, before the compensations of the actions preceding the branch. A `<confirm>` therefore works inside a branch exactly as beside it, including the suspension: the branch may resume long after the command returned.
- `<if>` reads its condition in the fuzzy sense of TL-Script, so an object stands for a true condition and nothing (`null`, an empty list, an empty text) for a false one. A `<case match="…">` holds an expression that matches when the switch value equals its value under the TL-Script comparison (`==`), so a classifier is written as `` `module:Enumeration#literal` ``, a text as `'text'` and a number as the number it is. A `<case test="…">` holds a condition read in the same fuzzy sense as an `<if>` condition; a case configures exactly one of the two. Without a `value` script the switch value is the default argument, so `<switch><case test="$value == null">` decides on the main argument of the command.
- A command whose chain applies the entered form values is disabled while the form has errors — a branch reports that for the actions of *all* its branches, taken or not, because the button's state cannot depend on the decision.
- **`<executability>` guards the command with rules over its arguments**: `<visible-if expr="…"/>` hides the command while its predicate over the arguments (see above) does not return `true`; `<disabled-if expr="…"/>` keeps it visible but disabled and takes the reason from its script — no value or `false` means executable, `true` disables it with a generic reason, a resource key or a text disables it with that reason, which the button shows as its tooltip. A rule that inspects objects beyond the main argument needs those types in the command's `observed-types`, otherwise their changes do not re-evaluate it; a change of any of the command's input channels re-evaluates it. A rule deciding over a single value (`<null-input-disabled/>`, `<model-access/>`, …) decides over the main argument: the trigger's value, otherwise the first input.

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

## Icon, label or both: the display mode

Whether a button shows the command's icon, its label or both is the command's `display="icon-only|icon-label|label-only"` (on any view command, `ViewCommand.Config#getDisplay`). An icon-only button shows its label as tooltip. `icon-only` on a command without an `image` is refused with a warning, and the button shows its label; a command with an image that requests nothing shows icon and label. A command scope sets the default for every button it renders whose command requests no mode: `command-display` on `<panel>`, `<window>` and `<full-page>` (`CommandScopeElement`) applies to the configured commands and to those contributed by child elements (e.g. a form's edit and save buttons), and `command-display` on `<accordion>` (default `icon-only`) to the buttons of its section headers; the command's own `display` always wins.

A `<button>` (`ButtonElement`) shows its one command the same way, so its mode is set on its `<action>`:

```xml
<button appearance="ghost" size="small">
	<action class="com.top_logic.layout.view.command.GenericViewCommand"
		display="icon-only"
		image="css:bi bi-x"
	>
		<label><en>Close</en><de>Schließen</de></label>
	</action>
</button>
```

## Creating and deleting objects: `<create-transient>`, `<persist-transient>`, `<delete-object>`

Three actions perform the model operations of a create dialog and a delete button. Each enforces the model access right of its operation like the TL-Script function it corresponds to, and brings the matching executability rule itself (`ViewAction#getIntrinsicRule()`), so the command offering it is hidden or disabled before the operation would fail — no `<executability>` configuration for the right is needed. The rule decides on the *command's* main argument, not on the value the action processes (see `ModelAccessRule` and `ModelAccessPolicy` for hide vs. disable).

- **`<create-transient type="…" [container="ch" reference="attr"]/>`** (`CreateTransientAction`) results in a transient object of the type — the draft the dialog edits, as `new(type, transient: true)` creates it; its follow-up actions (e.g. the `<open-dialog>` binding it into the dialog) see it as `result`. Its rule is the right to create an object of the type: without `container` against the security root (refused → hidden), with `container` in the context of the channel's object and, with `reference`, together with Write on that reference (refused → disabled). `container`/`reference` serve the check only; the dialog gets the container through the `<open-dialog>` bindings.
- **`<persist-transient [type="…"] [container="ch" reference="attr"]/>`** (`PersistTransientAction`) makes the transient object it processes (its [default argument or `value`](#actions-processing-one-value-value)) persistent the way `$draft.copy(transient: false)` does (values and composition parts; a refusal reports a refused *creation*), in the context of the container if one is given, and with `reference` adds the created object to that reference of the container, the way `$container.add(reference, $created)` does including its Write check. It runs in a transaction of its own and results in the persistent object. Its rule is the same creation check; the created type is `type` if given, else the reference's type, else the type of the command's main argument — so the dialog's Create button names the draft as its input: `inputs="model"`.
- **`<delete-object/>`** (`DeleteObjectAction`) deletes the object (or the objects of a collection) it processes the way `delete()` does, compositions included, in a transaction of its own; a following `<write-channel name="…" clear="true"/>` empties the channel that showed it. Its rule is Delete on the command's main argument (refused → disabled with "You may not delete this object.", hidden when no role may ever delete the type), combined with `<delete-veto-disabled/>` (`DeleteVetoDisabled`): an input whose `TLObject#tDeleteVeto()` refuses the deletion disables the command with the veto as reason. A command deleting by script configures `<delete-veto-disabled/>` explicitly.

A transaction nested in a `<with-transaction>` commits with it, so the actions compose with further script steps in one transaction. A command whose effect is a free script uses the general rule instead: `<model-access operation="…"/>` in `<executability>`; `<model-access operation="Create"/>` without a `type` checks the creation of an object of the type of the command's main argument.

A `<model-access>` check on a concrete object (explicit, or the rule an action brings) also asks the global `CommandApprovalService` (without component and command ID) once the access rights allow the operation: the approval checks configured for the object's type — e.g. that the anonymous account is neither edited nor deleted — disable the command with the approval's reason, as in the classic UI.

The opener and the dialog of a creation in a container:

```xml
<!-- opener, e.g. in the list toolbar -->
<generic-command image="css:bi bi-plus-lg" placement="TOOLBAR">
  <create-transient type="tl.demo.projectManagement:Milestone" container="selectedScope" reference="milestones">
    <with-result>
      <open-dialog bind-input-to="model" dialog-view="demo/create-milestone.view.xml">
        <bind channel="container" to="selectedScope"/>
        <bind channel="selection" to="selectedMilestone"/>
      </open-dialog>
    </with-result>
  </create-transient>
</generic-command>

<!-- dialog: the form edits the draft in "model" -->
<generic-command image="css:bi bi-check-lg" inputs="model" placement="BUTTON_BAR">
  <store-form-state>
    <with-result>
      <persist-transient container="container" reference="milestones">
        <with-result>
          <write-channel name="selection"/>
          <close-dialog/>
        </with-result>
      </persist-transient>
    </with-result>
  </store-form-state>
</generic-command>
```

A top-level creation omits `container` and `reference` on both actions. Deleting the selected object:

```xml
<generic-command image="css:bi bi-trash" inputs="project">
  <executability>
    <null-input-disabled/>
  </executability>
  <delete-object/>
  <write-channel name="project" clear="true"/>
</generic-command>
```

## Long-running jobs: `<start-job>` and `<job-status>`

Work that takes longer than a request may take does not belong in the request. `<start-job>` (`StartJobAction`) is the action that hands it to a worker thread, publishes what it reports on a channel, and **suspends the command** until the work has ended — the same suspension a `<confirm>` uses, so the chain simply continues afterwards, and the follow-up actions in the `<with-result>` of the `<start-job>` see the job's result as `result`:

```xml
<action class="com.top_logic.layout.view.command.GenericViewCommand" inputs="progress">
  <executability>
    <disabled-if expr="if(jobIsRunning($progress), #('A job is already running.'@en), null)"/>
  </executability>
  <start-job job="progress" cancelable="true" update-interval="200">
    <phases>
      <phase name="read"><label><en>Reading</en><de>Lesen</de></label></phase>
      <phase name="check"><label><en>Checking</en><de>Prüfen</de></label></phase>
    </phases>
    <function><![CDATA[{
	$job.jobPhase('read');
	$job.jobMessage(#('Reading the records.'@en));
	count(1, 6).foreach(i -> { sleep(400); $job.jobProgress($i, 5); });
	$job.jobPhase('check');
	$job.jobIndeterminate();
	sleep(1500);
	#('5 records processed.'@en);
}]]></function>
    <with-result>
      <write-channel name="report"/>
    </with-result>
  </start-job>
</action>
```

- **The channel carries immutable snapshots.** The `progress` channel holds a `JobState` from the moment the job starts, and a *new* one on every report, so nothing a display would have to observe ever changes. `update-interval` is the shortest time in milliseconds between two published snapshots: a job counting thousands of items is followed at that pace instead of flooding the browser, the last report of a burst is never lost, and the snapshot that ends the job is always delivered. The body runs in the sub-session of the starting request and publishes under the window's interaction through a `ChannelPublisher` (the mechanism behind a [`<source-channel>`](doc:view-layer/channels#channels-fed-by-a-server-side-source-source-channel) as well), so the channel write, the controls updating from it and the updates reaching the browser are serialized against the requests of the same session exactly like a command is.
- **The work runs outside any transaction, on a thread that serves no request.** Persisting what the job produced is the business of the follow-up actions in the `<with-result>` of the `<start-job>`: the command continues where it left off, and they see the job's result as `result` (or the name its `as` gives), so a `<with-transaction><execute-script function="… $result …"/></with-transaction>` or a `<write-channel>` in that `<with-result>` is where the result lands. A job that fails or is cancelled **aborts** the command instead — the remaining actions are skipped, the compensations of the ones before it run, and the failure stays visible in the last state of the job rather than in a snackbar.
- **The body is a TL-Script `function=` or a Java `<body class="…"/>`**, exactly one of the two. The function sees the **monitor of the job as the variable `job`**, beside the arguments of the chain and the values of the action's own `inputs` as variables named after them (`ScriptJobBody`, `StartJobAction.jobScope`); the monitor hides an argument of the same name. It reports with `$job.jobPhase('name')` (entering a step marks the steps passed over as done; naming a step that was never declared ends the job with an error), `$job.jobPhases([…])` or `$job.jobPhases({name: label})` for a job that learns its steps only while running, `$job.jobProgress(done, total)`, `$job.jobIndeterminate()` and `$job.jobMessage(text)`. What the function returns is the result of the job.
- **Reading a snapshot** is `jobIsRunning($s)`, `jobIsFinished($s)`, `jobStatus($s)` (the texts `running`, `completed`, `failed`, `cancelled`, so a `<switch><case match="'completed'">` decides on it), `jobResult($s)` and `jobError($s)`. Each of them answers over no job at all as well, which is what the channel holds before the first start — so a start button guards itself with `inputs="progress"` plus `<disabled-if expr="jobIsRunning($progress)"/>` and needs no case of its own for the time before the first run.
- **Cancellation is cooperative.** `cancelable="true"` offers the reader a cancel button; pressing it marks the job and interrupts the worker. `sleep()` keeps the interrupt it was woken by, so a sleeping job wakes at once and ends at the next point it *reports* from — which is what makes a loop of `sleep` + `jobProgress` stop within one step. Every report a Java body makes on its `JobMonitor` checks the same way, and `JobMonitor.checkCancelled()` is that check on its own for a stretch of work that reports nothing. Only declare it for work that may be given up half-done: a cancelled job has done part of what it was started for.
- **`<job-status input="progress"/>`** (`JobStatusElement` → `ReactJobStatusControl` / `TLJobStatus`) is the display, bound to the channel alone and holding no state of its own. It shows the status, the declared steps as done / active / pending, the bar (determinate or indeterminate), the message, the elapsed time — counted in the browser, so it ticks without a server round trip and freezes when the job ends — and at the end the result or the error. A channel holding anything that is not a job state displays nothing. Every text is resolved for the reader on the server: the phases and the message by their `ResKey`, the result through `MetaLabelProvider`, so a body returning an i18n literal `#('…'@en, '…'@de)` is displayed in the reader's language.
- **CSS hooks**: the BEM block `tlJobStatus` with the status modifier `tlJobStatus--running|completed|failed|cancelled` and the elements `__header`, `__state`, `__elapsed`, `__cancel`, `__phases`, `__phase` (`--done`, `--active`, `--pending`), `__bar`, `__message`, `__error`, `__result` (`tlReactControls.css`). An application restyles the display through these classes; the bar inside it is the design system's `tl-progress` (`progress.css`), addressed through its own classes `tl-progress__track|__fill|__label`, never through `tlJobStatus`.
- **Demo**: `com.top_logic.demo.react/…/views/demo/long-job-demo.view.xml` — a three-phase job with a determinate loop, an indeterminate phase and a result written to a second channel, a failing job, and a standalone indeterminate `<progress>`, plus a chunked import creating 500 tickets in one pass and closing every second of them in a next one, and the chunked removal of what it created.

### Committing in chunks: `ChunkedScriptJobBody`

A job that *creates persistent objects* runs outside any transaction and TL-Script opens none, so the body needs a transactional frame. `<body class="com.top_logic.layout.view.job.ChunkedScriptJobBody" chunk-size="200">` is that frame written in configuration; `ChunkedJobBody` is the same frame for a body written in Java, with the hooks `hasInit()/init`, `elements`, `stepCount()/step` and `hasFinish()/finish`:

```xml
<start-job job="importState" cancelable="true">
  <body class="com.top_logic.layout.view.job.ChunkedScriptJobBody" chunk-size="200">
    <init-label><en>Reading the file</en><de>Datei einlesen</de></init-label>
    <init><![CDATA[{ s = new(`my:Import`, transient: true); $s.set(`my:Import#rows`, $file.parse()); $s; }]]></init>
    <elements><![CDATA[$state.get(`my:Import#rows`)]]></elements>
    <steps>
      <step>
        <label><en>Creating the records</en><de>Datensätze anlegen</de></label>
        <expr><![CDATA[$chunk.foreach(r -> $state.get(`my:Import#target`).create($r))]]></expr>
      </step>
      <step>
        <expr><![CDATA[$chunk.foreach(r -> $r.resolveReferences())]]></expr>
      </step>
    </steps>
    <finish><![CDATA[$state.get(`my:Import#created`)]]></finish>
  </body>
</start-job>
```

- **Every script sees the monitor of the job as `job`**, exactly like the `function=` body, and its further values as variables, without declaring them: `init` the values the job was started with, named after them (`$file` for an upload command's file); `elements` the `state`; a `<step>` the `chunk` and the `state`; `finish` the `state`. So every one of them reports with `$job.jobMessage(…)`, `$job.jobProgress(…)` and friends.
- **The state ties the scripts together.** What `init` returns is what `elements`, every pass and `finish` receive; without an `<init>` the state is the *first value the job was started with* (the main argument of the chain, or the first of the action's `inputs` where the chain has none). For a state that has to change while the job runs, make it a transient object — `new(\`my:Import\`, transient: true)` — whose attributes the passes set; several objects the passes need are a map literal `{'target': $t, 'index': $byKey}`.
- **`elements` is evaluated once, read-only and outside any transaction**, against the state. A collection is the list of work items, any other value is the single item it stands for, nothing at all is no items. The whole list is held for the run, so what it selects has to fit in memory — the chunking bounds the *transactions*, not the list.
- **Every `<step>` is a full pass over that list**, applied to successive chunks of `chunk-size` items (200 by default), each chunk in a transaction of its own. A pass begins once the pass before it has committed every chunk, which is what makes a *second* pass the place for work that needs all the items of the first one — resolving cross references between them, for instance.
- **A chunk that fails is retried item by item**, each item in a transaction of its own; only the items that genuinely cannot be processed are skipped, each of them logged and reported as a message naming the item and the failure, and counted. A step script therefore has to be **repeatable for an item it already saw** in the failed chunk. A failure in `init` or in `finish` is *not* caught: it ends the job with its own message, and its transaction is given up with it.
- **Cancellation takes effect between two committed chunks**, and at once at a report from *inside* a chunk: the chunk in progress is rolled back, the chunks that committed stay, and the job ends as cancelled rather than counting the item as one that could not be processed.
- **The phases are the steps of the job**: `init` where there is one, `step-1` … `step-n`, `finish` where there is one — announced by the body itself, so `<start-job>` needs no `<phases>` for it. `<init-label>`, a `<step>`'s `<label>` and `<finish-label>` name them for the reader; unnamed, a pass is shown as its number. The progress within a pass counts its chunks.
- **The result of the job is what `finish` returns**; a body without a `<finish>` ends with the text saying how many items it processed and how many it skipped — which the job reports as its last message either way.
