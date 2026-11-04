---
description: Read before showing or editing the BPMN diagram of a workflow definition (tl.bpe.bpml:Collaboration) in a .view.xml - the <bpmn-editor> element of tl-bpe-view (BpmnEditorElement, BpmnEditorControl), its input, selection and diagram channels, editable, saving with bpmlUpdateFromDiagram, unsaved changes, and the Workflows section of the administration.
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
whenever the shown collaboration or one of its parts is changed in the knowledge base. A selected
element that is not part of the shown workflow any more is replaced with `null` in the selection
channel. `editable` is evaluated anew on each of these occasions, so a workflow that is released
becomes read-only.

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
identity of its elements and their application specific extensions. The update changes the
collaboration in the knowledge base; the element then draws the stored model and resets the
`diagram` channel to `null`. Writing `null` to the `diagram` channel from anywhere else discards the
changes.

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
`WEB-INF/views/admin/admin.view.xml` (see [overlays](doc:view-layer/overlays)): the table of all
workflow definitions and the diagram of the selected one.
