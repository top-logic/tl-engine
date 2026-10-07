---
description: Read before putting a <flow-diagram> into a .view.xml or adding a diagram element type - the createChart script and its inputs, the reactFlow* functions, the selection channel shared with tables and forms (reactFlowSelection, selectable reactFlowGraphEdge), and the drawing rule for widgets (one top-level SVG element carrying the widget's id).
order: 75
---

# Flow diagrams

`<flow-diagram>` (`FlowDiagramElement`, module `com.top_logic.react.flow.server`) displays a diagram that a TL-Script function builds from the values of the view's channels. The diagram is drawn as SVG in the browser.

## The element

- **`inputs`** names the channels whose values the script receives as positional arguments, in the notation every `inputs` property of the view layer reads (`inputs="a, b"` or `<inputs><input channel="a"/></inputs>`, see [channels](doc:view-layer/channels#declaring-channels-channels)). A diagram that depends on no channel declares an empty `<inputs/>`.
- **`createChart`** is the script, usually written as element content in a CDATA section. It is a function of the inputs, `product -> …` for one input, and returns a diagram created with `reactFlowChart(root: …)`. A box returned instead of a diagram becomes the root of a diagram; any other result is an empty diagram. Always configure it: the default expression does not name a function of this module.
- **`selection`** names a channel the diagram shares its selection through, in both directions (see [selection](#selection) below).

The diagram is built when the element is displayed and built anew whenever one of the input channels takes a new value; the script runs again as a whole. A mutation that changes what the diagram shows without changing an input - an action editing the displayed module - therefore rebuilds the diagram only through an input of its own: the model editor (`com.top_logic.dev.tools`) declares a `reload` channel as a second input and writes `now()` into it after each change.

The building blocks of the script are the TL-Script functions of `FlowFactory`, all with the prefix `reactFlow` (`@ScriptPrefix`): boxes (`reactFlowText`, `reactFlowImage`, `reactFlowEmpty`), decorations (`reactFlowBorder`, `reactFlowFill`, `reactFlowPadding`, `reactFlowAlign`, `reactFlowClipbox`), layouts (`reactFlowVertical`, `reactFlowHorizontal`, `reactFlowStack`, `reactFlowGrid`, `reactFlowCompass`, `reactFlowFloating`), trees (`reactFlowTree`, `reactFlowConnection`), graphs (`reactFlowGraphLayout`, `reactFlowGraphEdge`), Gantt charts (`reactFlowGantt`, `reactFlowGanttRow`, `reactFlowGanttSpan`, …), interaction (`reactFlowSelection`, `reactFlowClickTarget`, `reactFlowTooltip`, `reactFlowContextMenu`, `reactFlowDropRegion`) and the export `reactFlowToSvg`.

The construction plan of `com.top_logic.demo.react` (`WEB-INF/views/demo/flow-diagram-demo.view.xml`) draws the build steps of the selected product as a tree, and shares the selected step with a table and a form (shortened):

```xml
<flow-diagram selection="selectedNode">
	<inputs>
		<input channel="selectedProduct"/>
	</inputs>
	<createChart><![CDATA[product -> {
parts = $product == null ? [] : $product.get(`test.flowchart:Product#buildInstructions`);

nodes = $parts.map(p -> reactFlowSelection(
    userObject: $p,
    content: reactFlowBorder(
        content: reactFlowPadding(all: 4,
            content: reactFlowText($p.get(`test.flowchart:FlowNode#name`))))));

nodeByPart = $nodes.indexBy(n -> $n["userObject"]);

$parts.isEmpty()
    ? reactFlowChart()
    : reactFlowChart(
        root: reactFlowPadding(all: 16,
            content: reactFlowTree(
                nodes: $nodes,
                connections: $parts
                    .map(p -> $p.get(`test.flowchart:FlowNode#inputs`)
                        .map(c -> reactFlowConnection(
                            parent: $nodeByPart[$p],
                            child: $nodeByPart[$c])))
                    .flatten())))
}]]></createChart>
</flow-diagram>
```

## Selection

A diagram element is selectable when the script makes it so: `reactFlowSelection(content: …, userObject: $obj)` wraps a box into a selectable node, and `reactFlowGraphEdge(…, selectable: true, userObject: $obj)` makes an edge of a graph layout selectable. The **user object** is what connects the diagram to the rest of the view: the `selection` channel holds user objects, not diagram elements (`DiagramSelectionBinding`).

- A click selects the element alone. In a diagram whose `multiSelect` property is set, a click with shift or ctrl held adds it to the selection, and a ctrl-click on a selected element removes it.
- The channel then holds the user object of the selected element, the set of user objects for several, and no value for none.
- A value written to the channel by another element - a row selected in a table over the same objects - marks every element carrying that user object. A value no element of the diagram carries is shown as no selection and is left on the channel.
- An element without a user object takes no part in the shared selection.

Rebuilding the diagram drops its selection, so a form with unsaved input bound to the selection channel is asked about its changes before an input of the diagram is written (see [unsaved changes](doc:view-layer/forms#unsaved-changes-are-asked-before-a-channel-write-transitively)).

The stylesheet of the module (`/style/tl-react-flow.css`) marks the selection by CSS classes: a selectable element carries `tlCanSelect`, a selected one `tlSelected`. Inside a selected node, an element of the class `tlSelectionStroke` gets the focus color as stroke, one of `tlSelectionFill` the selection background, and one of `tlSelectionMarker` is shown only while the node is selected; the visible line of a selected edge is drawn in the focus color. A script highlights its nodes by giving these classes to the border and fill inside `reactFlowSelection`, as the model editor does with `reactFlowBorder(cssClass: "tlSelectionStroke", content: reactFlowFill(fill: "white", cssClass: "tlSelectionFill", …))`.

## Drawing rule for diagram elements

A change of the diagram - a selection, a changed CSS class - reaches the browser as a patch of the changed widgets, and the client redraws only those (`FlowDiagramClientControl.applyScopeChanges()`). The client finds the SVG element of a changed widget by the id it gave the element when it was first drawn, removes that element's children, keeps aside every descendant that carries an id of its own, and runs the widget's `draw()` again into the found element: anonymous content is drawn anew, and a sub-widget written with `out.write(child)` is put back from the kept elements instead of being drawn again.

This works only if every widget whose `draw()` (in its `*Operations` class in `com.top_logic.react.flow.common`) attaches its model emits **exactly one top-level SVG element that carries its id**, with everything else it draws as children of that element:

```java
@Override
default void draw(SvgWriter out) {
	out.beginGroup();
	out.attachModel(self());

	out.beginPath();
	// ... the widget's own, anonymous drawing
	out.endPath();

	drawContent(out); // sub-widgets through out.write(...)

	out.endGroup();
}
```

- `attachModel(self())` directly follows the `beginGroup()`: it links the element just begun to the widget and gives it the widget's id.
- Paths, texts and groups a widget draws for itself stay inside that group and need no id; they are rebuilt with the widget.
- Contained widgets are drawn with `out.write(child)`, never by calling `child.draw(out)`: `write` is where the update keeps the elements of an unchanged sub-widget.
- A widget emitting siblings next to its identified element - a background path beside the group holding its content, a clip path beside the clipped group - loses those siblings on its next redraw or redraws them a second time. Such a widget wraps all of them in its one group, as `ClipBoxOperations` wraps both its content group and its clip path.

A widget that attaches no model - a text, an image, a layout that only places its contents - draws anonymous elements into the element of the nearest enclosing widget that has an id, and is redrawn with it.
