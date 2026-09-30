# React Flow Construction Plan Demo — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the legacy construction plan flowchart demo to a React View using the new `<flow-diagram>` element with input channels and TL-Script.

**Architecture:** A single view XML replaces the legacy multi-file layout. A `<table>` lists Products, its selection feeds into a `<flow-diagram>` via a channel, and the diagram's selection feeds a detail panel. The `createChart` TL-Script is ported 1:1 from the legacy `constructionPlanChart.layout.xml`, replacing `flow*` function names with `reactFlow*`.

**Tech Stack:** View XML, TL-Script, existing `test.flowchart` model data

---

## File Structure

### Modified
- `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/flow-diagram-demo.view.xml` — complete rewrite to construction plan demo

### Reference (read-only)
- `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/components/flowchart1/products/constructionPlan/constructionPlanChart.layout.xml` — legacy TL-Script to port
- `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/channels-demo.view.xml` — pattern for table + channel wiring

---

## Task 1: Write the Construction Plan View

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/flow-diagram-demo.view.xml`

- [ ] **Step 1: Write the view XML**

Replace the entire file content with:

```xml
<?xml version="1.0" encoding="utf-8" ?>
<view xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<channels>
		<channel name="selectedProduct" />
		<channel name="selectedNode" />
	</channels>
	<split-panel orientation="horizontal" resizable="true">
		<panes>
			<pane size="25" unit="%">
				<panel>
					<title>
						<en>Products</en>
						<de>Produkte</de>
					</title>
					<table
						selection="selectedProduct"
						types="test.flowchart:Product"
					>
						<rows>all(`test.flowchart:Product`)</rows>
					</table>
				</panel>
			</pane>
			<pane size="75" unit="%">
				<split-panel orientation="vertical" resizable="true">
					<panes>
						<pane size="70" unit="%">
							<panel>
								<title>
									<en>Construction Plan</en>
									<de>Bauplan</de>
								</title>
								<flow-diagram selection="selectedNode">
									<inputs>
										<input channel="selectedProduct" />
									</inputs>
									<createChart><![CDATA[product -> {
parts = $product == null ? list() : $product.get(`test.flowchart:Product#buildInstructions`);

nodes = $parts.map(p -> reactFlowSelection(
    userObject: $p,
    content: reactFlowBorder(stroke: "#eccf33", thickness: 2, cssClass: "tlSelectionMarker",
    content: reactFlowFill(fill: "#ffee95", cssClass: "tlSelectionMarker",
    content: reactFlowPadding(all: 3,
    content: reactFlowVertical(
        contents: [
            $p.get(`test.flowchart:FlowNode#marker`).with(marker ->
                $marker.isEmpty()
                    ? reactFlowEmpty()
                    : reactFlowPadding(bottom: 8, content: reactFlowAlign(
                        hAlign: "middle",
                        content:
                            reactFlowBorder(
                                stroke: "red",
                                dashes: [5, 5],
                                content: reactFlowPadding(
                                    all: 4,
                                    content: reactFlowText($marker)))))
            ),

            reactFlowAlign(
                hAlign: "middle",
                content: reactFlowPadding(
                    all: 4,
                    content: reactFlowText(
                        fontWeight: "bold",
                        text: $p.get(`test.flowchart:FlowNode#name`))
            )),

            reactFlowAlign(
                hAlign: "middle", content: reactFlowGrid(
                gapY: -1,
                contents: $p.get(`test.flowchart:FlowNode#locations`).map(l -> [
                    $l.get(`test.flowchart:Location#count`).with(count ->
                        $count == null
                        ? reactFlowEmpty()
                        : reactFlowBorder(
                            right: false,
                            content: reactFlowPadding(all: 3, content: reactFlowText($count)))
                    ),
                    reactFlowBorder(
                        content: reactFlowFill(
                            fill: $l.get(`test.flowchart:Location#part`)
                                .get(`test.flowchart:Part#color`),
                            content:
                                reactFlowPadding(all: 3, content: reactFlowText($l.get(`test.flowchart:Location#part`)))
                        )
                    ),
                    {
                        usages = $l.get(`test.flowchart:Location#usages`);
                        $usages.isEmpty()
                            ? reactFlowEmpty()
                            : reactFlowAlign(
                                content: reactFlowBorder(
                                    left: false,
                                    content: reactFlowPadding(all: 3, content: reactFlowText($usages))))
                    }
                ])
            ))
        ]))))
    )
);

nodeByPart = $nodes.indexBy(n -> $n["userObject"]);

$parts.isEmpty()
    ? reactFlowChart()
    : reactFlowChart(
        root: reactFlowPadding(all: 16,
            content: reactFlowTree(
                nodes: $nodes,
                connections: $parts
                    .filter(p -> !$p.get(`test.flowchart:FlowNode#inputs`).isEmpty())
                    .map(p -> $p
                        .get(`test.flowchart:FlowNode#inputs`)
                        .map(c -> reactFlowConnection(
                            parent: $nodeByPart[$p],
                            child: $nodeByPart[$c])))
                    .flatten()
            )
        )
    )
}]]></createChart>
								</flow-diagram>
							</panel>
						</pane>
						<pane size="30" unit="%">
							<panel>
								<title>
									<en>Details</en>
									<de>Details</de>
								</title>
								<form
									types="test.flowchart:FlowNode"
									model="selectedNode"
								/>
							</panel>
						</pane>
					</panes>
				</split-panel>
			</pane>
		</panes>
	</split-panel>
</view>
```

**Key design decisions:**

1. **Product selection via table**: A `<table>` with `rows="all(test.flowchart:Product)"` lists all Products. Its `selection` channel feeds the flow diagram.

2. **createChart as CDATA child element**: The TL-Script is too complex for an XML attribute. Using `<createChart><![CDATA[...]]></createChart>` as a child element keeps it readable. This works because TypedConfiguration reads child elements as property values.

3. **Null-safety**: The script starts with `$product == null ? list() : ...` to handle the case when no Product is selected. Similarly, `$parts.isEmpty() ? reactFlowChart() : ...` creates an empty diagram when there are no build instructions.

4. **1:1 port of legacy script**: Every `flow*()` call is replaced with `reactFlow*()` (prefix concatenated, no separator). The logic is identical to `constructionPlanChart.layout.xml`.

5. **Selection wiring**: `<flow-diagram selection="selectedNode">` writes the clicked node's `userObject` (a `FlowNode`) to the `selectedNode` channel. The detail form reads this channel.

6. **Detail form**: A `<form>` element bound to the `selectedNode` channel shows FlowNode fields.

**Important note about `<createChart>` as child element:** The `FlowDiagramElement.Config.getCreateChart()` property has `@Name(CREATE_CHART)` where `CREATE_CHART = "createChart"`. TypedConfiguration allows properties to be set either as XML attributes or as child elements. Since `Expr` has a format annotation, both work. If child elements don't work for `Expr` properties, fall back to using the attribute with `&gt;` for `>` and `&amp;` for `&` in the TL-Script.

- [ ] **Step 2: Test in browser**

The view XML files are loaded at runtime. However, `FlowDiagramElement` compiles the expression in its constructor (shared across sessions), so a fresh start may be needed if the element was previously cached with an error.

1. If the app is already running, navigate to Flow-Diagramm in the sidebar
2. If there's an error, restart the app: `.claude/scripts/tl-app.sh restart com.top_logic.demo`
3. Login: root / root1234
4. Click "Flow-Diagramm" in sidebar
5. Verify: Product list appears on the left
6. Click a Product → Construction plan diagram appears
7. Click a FlowNode in the diagram → Details panel shows the node's fields

Check the server log for errors:
```bash
grep -i "error\|exception" com.top_logic.demo/tmp/tl-app.log | grep -v "SOCKET_APPENDER\|TCP:localhost" | tail -10
```

- [ ] **Step 3: Fix issues if any**

Common issues:
- **`Unknown method 'reactFlowXxx'`**: Check the exact function name in `FlowFactory.java` — all methods use `@ScriptPrefix("reactFlow")` so the TL-Script name is `reactFlow` + method name (concatenated, no separator)
- **`child element not allowed`**: If `<createChart>` as child element doesn't work for `Expr`, move the script to the `createChart="..."` attribute and escape XML entities
- **`null` on first load**: Add null-guard for empty product selection

- [ ] **Step 4: Commit**

```
Ticket #29108: Port construction plan flowchart demo to React View.

Replace simple test diagram with full construction plan demo using
test.flowchart model data, product selection table, and detail form.
```

---

## Summary

| Task | Description | Complexity |
|------|-------------|------------|
| 1 | Write construction plan view XML with table + flow-diagram + detail form | Medium — TL-Script port + view wiring |
