---
description: Read before putting a <flow-diagram> into a .view.xml or adding a diagram element type - the createChart script and its inputs, the reactFlow* functions, the size of a diagram and its view (layout to the offered space, Gantt viewport, reactFlowChart fixedView), the selection channel shared with tables and forms (reactFlowSelection, selectable reactFlowGraphEdge, selection-mode for multiple selection, the ctrl+drag selection rectangle), the context menu of a diagram through <context-menu input="..."> on the selection channel, dropping table rows or tree nodes onto a diagram (<drop target="node|diagram"> with x / y / x-as / y-as, reactFlowDropRegion, FlowDiagramControl.setDropTarget, Widget.dropTarget, nodeAt), and the drawing rule for widgets (one top-level SVG element carrying the widget's id).
order: 75
---

# Flow diagrams

`<flow-diagram>` (`FlowDiagramElement`, module `com.top_logic.react.flow.server`) displays a diagram that a TL-Script function builds from the values of the view's channels. The diagram is drawn as SVG in the browser.

## The element

- **`inputs`** names the channels whose values the script sees as variables, named after the channel or by the name given with `as`, in the notation every `inputs` property of the view layer reads (`inputs="selectedProduct as product, reload"` or `<inputs><input channel="selectedProduct" as="product"/></inputs>`, see [channels](doc:view-layer/channels#declaring-channels-channels)). A diagram that depends on no channel declares an empty `<inputs/>`.
- **`createChart`** is the script, usually written as element content in a CDATA section. It uses the inputs as variables without declaring them (`$product`; a script starting with a parameter declaration such as `product -> …` is a configuration error), sees each handler of `handlers` as the variable named like the handler (`$onClick`), and returns a diagram created with `reactFlowChart(root: …)`. A box returned instead of a diagram becomes the root of a diagram; any other result is an empty diagram. Always configure it: the default expression does not name a function of this module.
- **`selection`** names a channel the diagram shares its selection through, in both directions (see [selection](#selection) below).
- **`selection-mode`** is `single` (the default) or `multi`, as for a table: whether the user may select one element at a time or any number of them. It applies to every diagram the script builds.

The diagram is built when the element is displayed and built anew whenever one of the input channels takes a new value; the script runs again as a whole. A mutation that changes what the diagram shows without changing an input - an action editing the displayed module - therefore rebuilds the diagram only through an input of its own: the model editor (`com.top_logic.dev.tools`) declares a `reload` channel as a second input and writes `now()` into it after each change.

The building blocks of the script are the TL-Script functions of `FlowFactory`, all with the prefix `reactFlow` (`@ScriptPrefix`): boxes (`reactFlowText`, `reactFlowImage`, `reactFlowEmpty`), decorations (`reactFlowBorder`, `reactFlowFill`, `reactFlowPadding`, `reactFlowAlign`, `reactFlowClipbox`, `reactFlowSticky`), layouts (`reactFlowVertical`, `reactFlowHorizontal`, `reactFlowStack`, `reactFlowGrid`, `reactFlowCompass`, `reactFlowFloating`), trees (`reactFlowTree`, `reactFlowConnection`), graphs (`reactFlowGraphLayout`, `reactFlowGraphEdge`), Gantt charts (`reactFlowGantt`, `reactFlowGanttRow`, `reactFlowGanttSpan`, …), interaction (`reactFlowSelection`, `reactFlowClickTarget`, `reactFlowTooltip`, `reactFlowDropRegion`) and the export `reactFlowToSvg`.

The construction plan of `com.top_logic.demo.react` (`WEB-INF/views/demo/flow-diagram-demo.view.xml`) draws the build steps of the selected product as a tree, and shares the selected steps with a table (shortened):

```xml
<flow-diagram selection="selectedNode" selection-mode="multi">
	<inputs>
		<input channel="selectedProduct" as="product"/>
	</inputs>
	<createChart><![CDATA[{
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

## Size and view

A diagram always fills the space its container offers (it takes part in the fill contract like a table, see [spacing](doc:view-layer/basics#spacing-model)); its height never comes from its drawing. The browser lays the diagram out within the size of that space at scale 1 and again whenever the space changes (`DiagramOperations.layout(context, availableWidth, availableHeight)`). Most elements take their natural size; an element with a viewport of its own - a Gantt chart (`reactFlowGantt`) - limits itself to the space left for it and scrolls its content internally: the wheel and dragging empty space scroll it, and the scrollbars it draws for each direction with a scroll range show where it stands (drag a thumb, or click its track to scroll by a page). Without an `initialZoom`, a Gantt chart fits its time axis into the offered width until the user zooms it with ctrl+wheel. An export (`reactFlowToSvg`) lays the diagram out without a size limit.

A label that should stay readable while a long span is scrolled partly out of view goes into `reactFlowSticky`: like `position: sticky` in CSS, the box takes the whole width of the span and keeps its content at the left edge of the visible time range, at most until the content ends at the end of the span. Borders and backgrounds around it stay in place, e.g. the year cell of a frozen calendar row `reactFlowGanttSpan(…, box: reactFlowBorder(reactFlowSticky(reactFlowText("2026"))))`. It works in the header and data rows of a Gantt chart; a `reactFlowLod` in its content chooses its variant by the width of the whole span. A text that may be longer than its span is given `reactFlowText(…, ellipsis: true)`: it is then cut to the width of the span and ends with "…", and the full text shows as tooltip, like `text-overflow: ellipsis` in CSS; this works for every text that gets less width than it needs, inside `reactFlowSticky` or not. To center a label vertically in a bar that is higher than the text, wrap it in `reactFlowAlign(…, hAlign: "strech", vAlign: "middle")`.

A Gantt dependency (`reactFlowGanttEdge`) is a plain line unless it names connector symbols: `sourceSymbol` and `targetSymbol` take the values of `reactFlowGraphEdge` (`arrow`, `closedArrow`, `filledArrow`, `diamond`, `filledDiamond`) and are drawn pointing into the source and target item, in the stroke color and width the edge currently shows (the violated style while its dependency is violated), solid also for a dashed edge.

A dependency is violated while its source endpoint lies after its target endpoint, whatever its `enforce` mode; the mode only decides whether dragging is limited. Between non-adjacent rows, the horizontal segment of an edge runs in the gap next to the source row, on the side of the target row, so it does not cross the items of the rows in between; edges passing through the same gap with overlapping horizontal ranges run on separate lanes.

Edges and decorations are colored by a color role of the user interface (`neutral`, `brand`, `error`, `warning`, `success`, `info`, `category-1` … `category-8`, the values of `ValueColor`, as for the colors of enumeration values): `strokeRole` and `violatedStrokeRole` of `reactFlowGanttEdge`, `colorRole` of `reactFlowGanttLineDeco` and `reactFlowGanttRangeDeco`. A role follows the theme and the dark mode; an exported SVG or PDF shows it in a fixed substitute color. Without any specification an edge is `neutral` and a violated edge `error`. A fixed color (`strokeColor`, `violatedStrokeColor`, `color`) takes precedence over the role. A decoration created without color and role keeps the default color of its function.

A diagram larger than its space is scaled down to fit, never above 100 %, and the user pans it with the wheel or by dragging and zooms it with ctrl+wheel (ctrl+0 resets the zoom). `reactFlowChart(fixedView: true, …)` switches this off: the diagram stays unscaled at its origin, while an element with its own viewport keeps scrolling and zooming. Ctrl+wheel over a diagram never zooms the browser page, whether it is fixed or not. Use it for a diagram that is laid out to its space anyway, e.g. one showing only a Gantt chart below a title, as the Gantt schedule of `com.top_logic.demo.react` does (`WEB-INF/views/demo/gantt-schedule-demo.view.xml`).

## Selection

A diagram element is selectable when the script makes it so: `reactFlowSelection(content: …, userObject: $obj)` wraps a box into a selectable node, and `reactFlowGraphEdge(…, selectable: true, userObject: $obj)` makes an edge of a graph layout selectable. The **user object** is what connects the diagram to the rest of the view: the `selection` channel holds user objects, not diagram elements (`DiagramSelectionBinding`).

- A click selects the element alone, and a ctrl-click on a selected element removes it. With `selection-mode="multi"`, a click with shift or ctrl held adds the element to the selection; a ctrl-click keeps the objects of the channel no element of the diagram carries, while a click without a modifier replaces the whole selection.
- With `selection-mode="multi"`, dragging with ctrl held draws a rectangle (CSS class `tlSelectionRectangle`) instead of panning the diagram; releasing the button adds every selectable element lying completely inside it to the selection, like a ctrl-click on each of them (`SelectionRectangle` in `com.top_logic.react.flow.client`, `DiagramOperations.addToSelection()`).
- The channel then holds the user object of the selected element, the set of user objects for several, and no value for none.
- A value written to the channel by another element - a row selected in a table over the same objects - marks every element carrying that user object. A value no element of the diagram carries is shown as no selection and is left on the channel.
- An element without a user object takes no part in the shared selection.

Rebuilding the diagram drops its selection, so a form with unsaved input bound to the selection channel is asked about its changes before an input of the diagram is written (see [unsaved changes](doc:view-layer/forms#unsaved-changes-are-asked-before-a-channel-write-transitively)).

The stylesheet of the module (`/style/tl-react-flow.css`) marks the selection by CSS classes: a selectable element carries `tlCanSelect`, a selected one `tlSelected`. Inside a selected node, an element of the class `tlSelectionStroke` gets the interactive border color as stroke, one of `tlSelectionFill` the selection background, and one of `tlSelectionMarker` is shown only while the node is selected; the visible line of a selected edge is drawn in that color. A script highlights its nodes by giving these classes to the border and fill inside `reactFlowSelection`, as the model editor does with `reactFlowBorder(cssClass: "tlSelectionStroke", content: reactFlowFill(fill: "white", cssClass: "tlSelectionFill", …))`.

## Context menu

A diagram has no menu of its own. Put it into a `<context-menu>` whose `input` is the diagram's `selection` channel, and its `CONTEXT_MENU` commands open on a right-click, as for a table:

```xml
<context-menu input="selection">
  <flow-diagram selection="selection" selection-mode="multi"> ... </flow-diagram>
  <commands>
    <generic-command inputs="selection" placement="CONTEXT_MENU"> ... </generic-command>
  </commands>
</context-menu>
```

Before the menu opens, the right-click adjusts the selection the menu acts on (`FlowDiagramControl.selectForContextMenu()`):

- On an element outside the selection, that element is selected alone - like a click without a modifier, so the selected objects the diagram does not display are given up as well.
- On a selected element, the selection stays as it is, also a selection of several elements, and the menu acts on all of them.
- On the empty area, the selection is cleared: commands that need an object are not executable, commands without one are.

A modifier key does not change this: a right-click never adds to the selection. A form with unsaved input bound to the selection channel is asked about its changes before the selection changes, as for a click. The client reports the right-click before the menu region asks for the menu, both through the command channel of the page, so the menu always opens on the adjusted selection.

## Drag and drop onto a diagram

A `<flow-diagram>` declares `<drop>` exactly as a [table](doc:view-layer/tables#drag-and-drop-of-table-rows) does and takes part in the same protocol: rows of a table, nodes of a tree or cards of a board are dropped onto it (`FlowDropConfig`, the shared `DropConfig`). `target` is `diagram` (default) or `node`:

| `target` | applies | arguments | renamed by |
|---|---|---|---|
| `diagram` | on the free area of the diagram | `objects`, `target` (always `null`), `x`, `y`: the point relative to the origin of the diagram | `as`, `target-as`, `x-as`, `y-as` |
| `node` | onto the node under the pointer | `objects`, `target`: the user object of the node, `x`, `y`: the point relative to its upper left corner | `as`, `target-as`, `x-as`, `y-as` |

- **The node** a drop is made onto is the innermost box under the pointer that is a drop region (`reactFlowDropRegion`) or has a user object; its user object is the `target`. A drop region without a user object stands for the diagram as a whole - a way to keep a part of a diagram from being dropped onto. Beside such boxes, the drop is made on the diagram.
- **The point** is in diagram units, independent of zoom and scrolling, so an action can create a node exactly where the drop was made, e.g. a `reactFlowPosition(x: ..., y: ...)` inside a `reactFlowFloating` root, whose origin is the diagram's origin. `refuse-if` sees the point as well.
- A `diagram` drop applies only on the free area where a `node` drop is declared for the dragged kind; without one, it applies anywhere on the diagram, the point then relative to the diagram's origin also over a node. A `node` drop refusing a drop does not hand it to the `diagram` drop: the place is one or the other.
- While a drag hovers the diagram, the node under the pointer is framed, or the diagram as a whole, and a refusal is shown at the pointer with its reason (`dropProbe`, asked once per drag, node and square of 10 diagram units).

```xml
<flow-diagram observed-types="test.flowchart:FlowNode">
  <createChart>reactFlowChart(root: reactFlowFloating(contents: ... reactFlowPosition(x: ..., y: ..., userObject: $s, content: ...)))</createChart>
  <drop accept="flowStep" as="steps" target="diagram">
    <with-transaction>
      <execute-script function="$steps.foreach(s -> { $s.set(`test.flowchart:FlowNode#x`, $x); $s.set(`test.flowchart:FlowNode#y`, $y); })"/>
    </with-transaction>
  </drop>
</flow-diagram>
```

**Underneath**: the user object of a widget never reaches the client, so the server tells it which widgets have one - the derived flag `Widget.dropTarget`, which `FlowDiagramControl` brings up to date before it serializes the diagram and before it sends a patch, so a user object set later reaches the client. The GWT control answers `nodeAt(clientX, clientY, findNode)` - the client-side ID of the node, the point through the inverse `getScreenCTM()` of the SVG minus the origin of the node, and the node's place on the screen - and knows no drag and drop otherwise; `TLFlowDiagram.tsx` handles `dragover` and `drop` with the helpers of `tl-react-bridge` (`dragKindAccepted`, `dragModes`, `putDropPoint`, `DropHint`) and sends `drop` / `dropProbe` with the node as `targetKey` and the point as `x` / `y`. `FlowDiagramControl.setDropTarget(…)` takes any `DropTarget`; a recorded drop names its node by the key of the node's user object.

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
