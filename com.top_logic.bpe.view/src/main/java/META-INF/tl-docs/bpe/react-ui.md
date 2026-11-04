---
description: Read before showing or editing the BPMN diagram of a workflow definition (tl.bpe.bpml:Collaboration) in a .view.xml - the <bpmn-editor> element of tl-bpe-view (BpmnEditorElement, BpmnEditorControl), its input, selection and diagram channels, editable, saving with bpmlUpdateFromDiagram, unsaved changes -, before working on the Workflows section of the administration (admin/workflows/workflows.view.xml, its diagram edit mode and properties form), and before writing or embedding the step view of a manual task (tl.bpe.bpml:ManualTask#view, the channels token and process, viewParse).
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
  BPML file* (a `<download>` of `bpmlExport`) and *Delete* (`<delete-object>` after a confirmation)
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
For a task without a step view, it first stores an initial one: the two channels and a form on
`process` with a field for each attribute the model type of the participant declares itself. The
initial view is built as XML text and turned into the configuration with the TL-Script function
`viewParse(xml)` of `tl-layout-view`, which reads a text like a `.view.xml` file into a
`ViewElement.Config`, the value of a `tl.view:ViewDefinition` attribute. Storing the step view does
not change the diagram, so the editor keeps a change of the diagram that is not saved yet.
