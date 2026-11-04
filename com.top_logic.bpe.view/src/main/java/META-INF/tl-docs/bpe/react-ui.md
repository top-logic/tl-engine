---
description: Read before showing or editing the BPMN diagram of a workflow definition (tl.bpe.bpml:Collaboration) in a .view.xml - the <bpmn-editor> element of tl-bpe-view (BpmnEditorElement, BpmnEditorControl), its input, selection and diagram channels, editable, saving with bpmlUpdateFromDiagram, unsaved changes -, before working on the Workflows section of the administration (admin/workflows/workflows.view.xml, its diagram edit mode and properties form), before writing or embedding the step view of a manual task (tl.bpe.bpml:ManualTask#view, the channels token and process, viewParse, bpeDefaultStepView), and before working on or embedding the task screens - the Tasks item of the application shell, bpe/tasks.view.xml (My tasks, Start process), bpe/task.view.xml (the task screen and its Finish command) and bpe/finish-task.view.xml, the creation form of a process in the start dialog (bpeModelType, bpeStart with a draft) and additional data of an option with a form type in the finish dialog (bpeFormType, bpeFinish with additional).
order: 30
---

# The React UI of the process engine

`tl-bpe-view` (`com.top_logic.bpe.view`) is the user interface of the process engine for the
`.view.xml` layer. It builds on the headless `tl-bpe` (the TL-Script functions of
[workflow definitions](doc:bpe/workflow-definitions) and [task execution](doc:bpe/task-execution))
and on `tl-layout-view`; it needs neither the classic modeler nor the classic execution UI.

## `<bpmn-editor>`: the diagram of a workflow

The element draws the BPMN diagram of a workflow definition with [bpmn-js](https://bpmn.io/):

```xml
<bpmn-editor
	diagram="changedDiagram"
	editable="$workflow.get(`tl.bpe.bpml:Collaboration#state`) == `tl.bpe.bpml:ApprovalState#Development`"
	input="selectedWorkflow as workflow"
	selection="selectedElement"
/>
```

| Attribute | Meaning |
|---|---|
| `input` | channel holding the `tl.bpe.bpml:Collaboration` whose diagram is shown (mandatory); any other value shows an empty canvas |
| `selection` | optional, two-way: the model element the user clicks (task, gateway, event, lane, sequence flow, ...; the empty canvas stands for the collaboration) is written there; a model element of the shown workflow written there by another element is shown selected |
| `diagram` | optional, output: `null` while the diagram shows the stored model, the BPMN XML of the whole diagram after the user changed it |
| `editable` | TL-Script, default `false`; sees the input value as a variable named after the channel (or by its `as` name). `true` shows the bpmn-js modeler with palette and context pad, otherwise a viewer that can be moved and zoomed only |

The element always fills the height its container offers (see the
[fill contract](doc:view-layer/basics#filling-the-available-height-the-fill-contract)). The
stylesheets of bpmn-js are scoped to the canvas of the element, so they do not reach the rest of the
page; the colors of the canvas, the palette and the context pad come from the theme.

### Following the model

The element draws the diagram from the model whenever the input channel gets another workflow, and
whenever the shown collaboration or one of its parts is changed in the knowledge base in a way the
diagram shows. A change the diagram does not show - an application specific extension of an element
such as its form or its [step view](#the-step-view-of-a-manual-task) - keeps a change of the diagram
that is not saved yet. A selected element that is not part of the shown workflow any more is
replaced with `null` in the selection channel. `editable` is evaluated anew on each of these
occasions, so a workflow that is released becomes read-only.

### Saving

The element does not store a change. A view saves the diagram with an action chain:

```xml
<generic-command inputs="changedDiagram, selectedWorkflow as workflow">
	<label><en>Save diagram</en><de>Diagramm speichern</de></label>
	<executability>
		<null-input-disabled/>
	</executability>
	<with-transaction>
		<execute-script function="bpmlUpdateFromDiagram($workflow, $changedDiagram)"/>
	</with-transaction>
</generic-command>
```

`<null-input-disabled/>` decides over the first input, so the command is enabled while there is a
change to save.

`bpmlUpdateFromDiagram` makes the collaboration structurally equal to the diagram, keeping the
identity of its elements and their application specific extensions: an element of the diagram is the
same object as before as long as it keeps its BPMN ID and its type; an element whose type is changed
in the diagram (a task replaced by a manual task) is a new object, which takes over the extensions of
the old one. The update changes the collaboration in the knowledge base; the element then draws the
stored model and resets the `diagram` channel to `null`. Writing `null` to the `diagram` channel from
anywhere else discards the changes; a save chain clears it after the save as well, which also covers
a diagram that was changed and changed back.

The client sends a change debounced, but always before an action command, so the save sees the
diagram as the user left it.

### Unsaved changes

While the `diagram` channel holds a change, the element takes part in the
[unsaved changes protocol](doc:view-layer/forms#unsaved-changes-are-asked-before-a-channel-write-transitively)
like a form: it blocks the write of its `input` channel and reports itself to the enclosing
dirty-tracked scope. The user is asked before another workflow is shown; the question offers to
discard the change, not to save it, since saving is the business of the view's action chain.

## The Workflows section of the administration

`tl-bpe-view` adds the section *Workflows* to the administration of the React UI with an overlay of
`WEB-INF/views/admin/admin.view.xml` (see [overlays](doc:view-layer/overlays)). Its view
`admin/workflows/workflows.view.xml` has three panes. A *BPML file* is the BPMN XML of a workflow with the application specific extensions, the format `bpmlExport` writes (`<name>.bpml`) and `bpmlImport` reads; a plain BPMN file is read as well.

- **The list** of all workflow definitions with their state. Its toolbar creates a workflow from the
  default diagram (`new-workflow.view.xml` asks for the name, `bpmlCreate`), from a BPML file (an
  `<upload-command>` calling `bpmlImport`), and as a copy of the selected one
  (`copy-workflow.view.xml`, `bpmlCopy`); each selects the new workflow. *Release* and *Revoke* are
  shown while `bpmlCanRelease` / `bpmlCanRevoke` allow them. *Update from BPML file*
  (`update-workflow.view.xml`, `bpmlUpdate`, optionally taking the extensions from the file), *Export
  BPML file* (a `<download>` of `bpmlExport`) and *Delete* (`<delete-object>` after a confirmation; disabled by `bpmlCanDelete` for a workflow of which processes were started, which is revoked instead)
  work on the selected workflow. The changing commands declare `Write` on the workflow, the creating
  ones `Create` of a collaboration. Release, revoke and update are disabled while the diagram has a
  change that is not saved.
- **The diagram** of the selected workflow, edited in a mode of its own: *Edit diagram* (for a workflow
  in development) writes `true` to the channel `diagramEditMode`, upon which a `<switch>` shows the
  editable `<bpmn-editor>` instead of the viewer. *Save diagram* stores the change with
  `bpmlUpdateFromDiagram`, *Cancel* discards it by clearing the `diagram` channel; both end the mode.
  `diagramEditMode` is initialized from the selected workflow, so selecting another workflow ends the
  mode too - after the unsaved-changes question, while the diagram has a change.
- **The properties** of the element selected in the diagram, a `<form>` with the attributes of all
  BPML element types; a field of an attribute the selected element does not have is not displayed
  (see [forms](doc:view-layer/forms)). An application that adds attributes to the BPML types adds
  their fields with an overlay of the view. The save of the form applies a pending change of the
  diagram first, in the same transaction, then stores the form:

  ```xml
  <save-action>
  	<with-transaction>
  		<if inputs="diagram" test="$diagram != null">
  			<then>
  				<execute-script function="bpmlUpdateFromDiagram($workflow, $diagram)"
  					inputs="selectedWorkflow as workflow, diagram"/>
  			</then>
  		</if>
  		<store-form-state/>
  	</with-transaction>
  	<leave-edit-mode/>
  </save-action>
  ```

  Saving the form alone would change the workflow in a way the diagram may show (a renamed task),
  upon which the editor draws the stored model and drops the change of the diagram. Since the diagram
  update keeps the identity of the elements, the form stores its values into the element that was
  updated. An element the pending diagram deletes or changes the type of fails the save; the
  transaction is rolled back, and both changes are still there to be corrected.

## The step view of a manual task

`tl-bpe-view` adds the attribute `view` (*Step view*, datatype `tl.view:ViewDefinition`) to
`tl.bpe.bpml:ManualTask` (`WEB-INF/model/tl-bpe-view/extension.tl.bpe.bpml.model.xml`). It is the view
the user working on the task sees. It is a BPML extension: an update from the diagram keeps it, and
an exported workflow carries it.

**The contract.** The task screen embeds the step view with a `<dynamic-view>` (see
[a view stored on a model object](doc:view-layer/channels#a-view-stored-on-a-model-object-dynamic-view))
and binds two channels the step view declares:

| Channel | Value |
|---|---|
| `token` | the `tl.bpe.execution:Token` being worked on; its node is the manual task |
| `process` | the `tl.bpe.execution:ProcessExecution` of the token (``$token.get(`tl.bpe.execution:Token#processExecution`)``), an instance of the model type of the participant the task belongs to (``tl.bpe.bpml:Participant#modelType``, e.g. `demo.process:LeaveRequest`) |

```xml
<view>
	<channels>
		<channel name="token"/>
		<channel name="process"/>
	</channels>
	<form input="process" with-inset="true">
		<field attribute="from"/>
		<field attribute="to"/>
	</form>
</view>
```

**Designing it.** The command *Design step view* of the properties pane, shown for a manual task,
opens the View Designer on the attribute (`<open-view-designer attribute="tl.bpe.bpml:ManualTask#view"/>`).
For a task without a step view, it first stores an initial one, `bpeDefaultStepView(task)`
(`StepViewFunctions`): the two channels and a form on `process` with a field for each attribute the
model type of the participant declares itself - the view the [task screen](#working-on-tasks) shows
for a task without a step view. The function builds the view as XML text and turns it into the
configuration with `viewParse(xml)` of `tl-layout-view`, which reads a text like a `.view.xml` file
into a `ViewElement.Config`, the value of a `tl.view:ViewDefinition` attribute. Storing the step view does
not change the diagram, so the editor keeps a change of the diagram that is not saved yet.

## Working on tasks

`tl-bpe-view` adds the item *Tasks* (`tasks`) to the section `application` of the application shell
with an overlay of `WEB-INF/views/app.view.xml` (see [the application shell](doc:view-layer/app-shell)),
so every React application containing the module offers its users their tasks. An application
places the item among its own ones with `<nav-item id="tasks" config:position="…"/>` in its own
overlay of the shell, or takes it out with `config:operation="remove"`.

The item shows `bpe/tasks.view.xml`, built on the [task execution](doc:bpe/task-execution) functions:

- **My tasks**, a table of `bpeTasks()` - the task (the token's name), the workflow, the process (its label), the number of the
  process and the time the task is waiting since. The table observes `tl.bpe.execution:Token`, so a task appears and
  disappears while the page is displayed, whoever started or finished it. The selected task is the
  channel `selectedTask`, carried in the address bar as `task/<id>`; another view leads to a task with
  `<show-view view="bpe/tasks.view.xml"><bind channel="selectedTask" expr="$token"/></show-view>`
  (see [navigation](doc:view-layer/navigation#entry-points)).
- **Start process** opens `bpe/start-process.view.xml`, listing `bpeStartEvents()` with their
  workflow, and below the list the creation form of the process chosen, see
  [starting a process](#starting-a-process).
- **The task screen** `bpe/task.view.xml` of the selected task. It declares the channel `token` and
  can be embedded wherever an application shows a token:

  ```xml
  <view-ref view="bpe/task.view.xml">
  	<bind channel="token" to="selectedTask"/>
  </view-ref>
  ```

  It embeds the step view of the task with a `<dynamic-view>` and binds the channels of the
  [contract](#the-step-view-of-a-manual-task), `process` being derived from the token. A task without a
  step view - a manual task none was designed for, or a task of another kind - shows
  `bpeDefaultStepView(task)`, the TL-Script function of `tl-bpe-view` that also gives *Design step
  view* its initial view: the two channels and a form on `process` with a field for each attribute
  the model type of the participant declares itself. The dynamic view observes
  `tl.bpe.bpml:ManualTask`, so a step view applied in the View Designer is shown at once.

### Starting a process

The creation form of the start dialog edits a transient object of the model type of the process
(`bpeModelType(event)`, the `modelType` of the participant), so the constraints of the type - its
mandatory attributes above all - are checked before the process exists:

```xml
<channels>
	<channel name="event"/>
	<derived-channel name="draft"
		expr="$event == null ? null : new(bpeModelType($event), transient: true)"
		inputs="event"
	/>
</channels>
...
<form initial-edit-mode="true" input="draft" mode-switch="false">
	<dynamic-view inputs="event" view="$event == null ? null : viewFields(bpeModelType($event))"/>
	<commands>
		<generic-command inputs="event, draft" placement="BUTTON_BAR">
			<store-form-state/>
			<with-transaction>
				<execute-script function="bpeStart($event, $draft)"/>
				...
			</with-transaction>
		</generic-command>
	</commands>
</form>
```

The fields are those the default step view shows: a field for each attribute the model type
declares itself (`viewFields(type)` of `tl-layout-view`, a view with a `<fields>` grid that a
`<dynamic-view>` embeds into the enclosing form, see
[fields of a type known at runtime](doc:view-layer/forms#fields-of-a-type-known-at-runtime)). A model type without attributes of its own shows an empty form.
*Start* stores the form into the draft - an error such as a missing mandatory value stops here and
is shown at its field -, and `bpeStart($event, $draft)` makes the draft persistent as the process
execution and runs it to its first task, in one transaction. The dialog selects the first task
(`bpeStartTask`) when the user works on it right away: the start event does not require a review,
or the user is an actor of the task. Otherwise the task waits in the task lists of its actors.

### Finishing a task

*Finish* in the toolbar of the task screen completes the task with one of its options
(`bpeOptions`):

- The command declares `check-dirty="SELF"`: input of the step view that is not saved yet is asked
  about before anything else - saved or discarded, or the command is cancelled. The options and
  their conditions are thus evaluated on the stored process, and finishing never loses an input.
- With a single option without additional data, an error of the option (`bpeErrors`) is shown in a
  `<notify kind="error" stop="true">` that ends the command, a warning (`bpeWarnings`) is asked in a
  `<confirm>`, and `bpeFinish` completes the task in a transaction.
- With several options - the outgoing flows of a gateway the user decides at - or a single option
  asking for additional data, the dialog `bpe/finish-task.view.xml` lists them with their labels
  (`bpeOptionLabel`) and the errors blocking them, with the default option (`bpeDefaultOption`)
  preselected; *Finish* there is disabled for a blocked option, confirms the warnings and calls
  `bpeFinish` with the chosen option.
- An option with a form type (`bpeFormType(token, option)`, the `formType` of its sequence flow, see
  [options](doc:bpe/task-execution#options)) asks for additional data: below the options, the
  dialog shows a creation form of a transient object of that type (the channel `additional`, with
  the fields of `viewFields`). *Finish* stores the form into the object - an error such as a
  missing mandatory value stops the command at its field - and passes it to
  `bpeFinish($token, $option, $additional)`. The engine hands the object to the operations walking
  the option's path as their fourth argument (`process -> finished -> next -> additional -> ...`),
  as the classic user interface does; it stores the object nowhere itself, so an operation keeps
  what the process needs of it, e.g. a persistent copy with
  ``$process.set(`my:Request#rejection`, $additional.copy(transient: false))``.

The result of `bpeFinish` - the next task of the same process the user is an actor for, or nothing -
is written to `token`. The user goes on with the next step of the process, or the screen empties and
the finished task leaves *My tasks*.

`com.top_logic.demo.react` ships the workflow *Urlaubsantrag* (`WEB-INF/workflow/LeaveRequest.bpmn.xml`,
imported at startup by `InitialProcessSetup`), released by its `state` extension, so a process of it
is started from *Tasks* right away: the start dialog asks for the `demo.process:LeaveRequest` with
its mandatory name, first and last day (labeled *name: first – last day*), its first task *Antrag
prüfen* edits it and is finished at a gateway offering *genehmigen* and *ablehnen*. *ablehnen* has
the form type `demo.process:Rejection` (`<tl:formType value="demo.process:Rejection"/>`): the
approver enters the mandatory reason and optionally an alternative period, and the operation of
the flow stores a persistent copy as the `rejection` of the request. The rejection leads to the
task *Ablehnung ansehen* of the applicant, whose step view (`<tl:view>` in the BPMN file) shows the
request with its rejection.

`InitialProcessSetup` stores a hash of each workflow file it imported and imports a changed file
again at the next startup, updating the workflow of the same name. Each file is imported in a
transaction of its own: a file that fails is logged and rolled back without a trace - the workflow
keeps its previous version, the hash stays - and is tried again at the next startup.
