# GraphLayout (Sugiyama) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a `GraphLayout` type to the React flow diagram that arranges arbitrary directed graphs using the Sugiyama hierarchical layout algorithm, enabling UML model editor development.

**Architecture:** The existing `tl-graph-layouter` module is refactored to be GWT-compatible by moving UML-specific code (node sizing from TLType, AWT text metrics, JSON export) to `com.top_logic.graph.diagramjs.server`. The stripped-down layouter provides `Sugiyama.layout(direction, graph)` which takes a `LayoutGraph` with pre-sized nodes and produces node positions + edge waypoints. A new `GraphLayout` msgbuf type in `react.flow.common` bridges the flow widget system with the Sugiyama algorithm: it computes intrinsic sizes of child widgets, builds a `LayoutGraph`, invokes Sugiyama, and maps results back to widget coordinates.

**Tech Stack:** Java 17, GWT, msgbuf, Maven, Sugiyama algorithm (Eiglsperger, BrandesKoepf, OrthogonalEdgeRouter)

---

## File Structure

### Part 1: Make tl-graph-layouter GWT-compatible

**Moved to `com.top_logic.graph.diagramjs.server`:**
- `DefaultNodeSizer.java` — UML-specific node sizing from TLType
- `DiagramTextRenderingUtil.java` — AWT font metrics
- `TechnicalNamesLabelProvider.java` — LabelProvider for TLNamedPart
- `JSONDefaultLayoutGraphExporter.java` — JSON export with TLModel
- `I18NConstants.java` (5 files) — I18N base class from com.top_logic.layout
- TLModel-specific methods extracted from `LayoutGraphUtil.java`

**Modified in `tl-graph-layouter`:**
- `LayoutContext.java` — strip to `LayoutDirection` only
- `Sugiyama.java` — accept `NodeSizer` parameter
- `LayoutGraphUtil.java` — remove TLModel methods, keep pure graph utilities
- `DefaultNodePortCoordinateAssigner.java` — decouple from label width calculations
- `QuadraticEquationSolver.java` — remove Logger dependency
- `pom.xml` — remove tl-core, com.top_logic.layout, com.top_logic.model dependencies

**Created in `tl-graph-layouter`:**
- `TLGraphLayouter.gwt.xml` — GWT module definition

### Part 2: GraphLayout in react.flow

**Modified in `com.top_logic.react.flow.common`:**
- `data.proto` — add `GraphLayout`, `GraphEdge`, `GraphWaypoint` types
- `pom.xml` — add dependency on `tl-graph-layouter`
- `TLReactFlow.gwt.xml` — inherit `TLGraphLayouter` GWT module

**Created in `com.top_logic.react.flow.common`:**
- `GraphLayoutOperations.java` — Sugiyama integration
- `GraphEdgeOperations.java` — orthogonal edge SVG rendering
- `GraphWaypointOperations.java` — waypoint data holder (if needed)

**Modified in `com.top_logic.react.flow.server`:**
- `FlowFactory.java` — add `graphLayout()` and `graphEdge()` TL-Script functions

**Modified in `com.top_logic.demo`:**
- `flow-diagram-demo.view.xml` — add graph layout demo (or separate view)

---

## Task 1: Move UML-specific code out of tl-graph-layouter

This is the largest task — moving 6+ files and splitting `LayoutGraphUtil`. Each sub-step must compile independently.

**Files:**
- Move: `DefaultNodeSizer.java` → `com.top_logic.graph.diagramjs.server`
- Move: `DiagramTextRenderingUtil.java` → `com.top_logic.graph.diagramjs.server`
- Move: `TechnicalNamesLabelProvider.java` → `com.top_logic.graph.diagramjs.server`
- Move: `JSONDefaultLayoutGraphExporter.java` → `com.top_logic.graph.diagramjs.server`
- Modify: `LayoutGraphUtil.java` — extract TLModel methods
- Modify: `LayoutContext.java` — strip down
- Modify: `Sugiyama.java` — make NodeSizer a parameter
- Modify: `DefaultNodePortCoordinateAssigner.java` — decouple from label widths
- Modify: `QuadraticEquationSolver.java` — remove Logger
- Move: I18NConstants files → `com.top_logic.graph.diagramjs.server`
- Modify: `com.top_logic.graph.layouter/pom.xml` — remove heavy deps
- Modify: `com.top_logic.graph.diagramjs.server/pom.xml` — add tl-graph-layouter dep if not present
- Create: `TLGraphLayouter.gwt.xml`

**Approach:** This is a refactoring task with many interdependencies. The safest order:

1. First create the target package structure in `diagramjs.server`
2. Move files one by one, updating imports in both modules
3. Strip `LayoutContext` and `Sugiyama` last (after all dependents are moved)
4. Verify both modules compile

Due to the high coupling, this task should be executed by an experienced developer who reads all affected code before making changes. The detailed steps below are a guide, not a rigid sequence.

- [ ] **Step 1: Understand the dependency graph**

Read all 12 GWT-incompatible files listed in the analysis. Map which methods in `LayoutGraphUtil` are used by:
- (a) The Sugiyama algorithm chain (must stay)
- (b) UML-specific sizing/export (must move)

The key split in `LayoutGraphUtil`:
- **Stays:** `getNodesStream()`, `getEdgesStream()`, `getOutgoingEdgesStream()`, `getIncomingEdgesStream()`, `getTopNodePorts()`, `getBottomNodePorts()`, port/edge stream helpers — pure graph operations
- **Moves:** `getNodeAttributesHeight/Width()`, `getLabelWidth/Height()`, `getCardinality()`, `getLabel()`, `getNodeGridWidth()`, `getEdgeSourceLabelWidth()`, `getEdgeTargetLabelWidth()`, all `*PortLabelWidth()` methods, `*PortsGridWidth()` methods

- [ ] **Step 2: Move DiagramTextRenderingUtil**

Move `com.top_logic.graph.layouter/src/main/java/com/top_logic/graph/layouter/text/util/DiagramTextRenderingUtil.java`
to `com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/text/util/DiagramTextRenderingUtil.java`

Keep the same package name so existing references from `LayoutGraphUtil` (which will also move) don't break.

```bash
mkdir -p com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/text/util/
git mv com.top_logic.graph.layouter/src/main/java/com/top_logic/graph/layouter/text/util/DiagramTextRenderingUtil.java \
       com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/text/util/
```

- [ ] **Step 3: Move DefaultNodeSizer**

Move to `com.top_logic.graph.diagramjs.server` keeping the same package:

```bash
mkdir -p com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/algorithm/node/size/
git mv com.top_logic.graph.layouter/src/main/java/com/top_logic/graph/layouter/algorithm/node/size/DefaultNodeSizer.java \
       com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/algorithm/node/size/
```

- [ ] **Step 4: Move TechnicalNamesLabelProvider**

```bash
git mv com.top_logic.graph.layouter/src/main/java/com/top_logic/graph/layouter/TechnicalNamesLabelProvider.java \
       com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/
```

- [ ] **Step 5: Move JSONDefaultLayoutGraphExporter**

```bash
mkdir -p com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/export/
git mv com.top_logic.graph.layouter/src/main/java/com/top_logic/graph/layouter/export/JSONDefaultLayoutGraphExporter.java \
       com.top_logic.graph.diagramjs.server/src/main/java/com/top_logic/graph/layouter/export/
```

- [ ] **Step 6: Extract TLModel methods from LayoutGraphUtil**

Create a new class `DiagramJSLayoutGraphUtil` in `com.top_logic.graph.diagramjs.server` containing all TLModel-specific methods from `LayoutGraphUtil`:
- `getNodeAttributesHeight(LayoutContext, TLType)`
- `getNodeAttributesWidth(LayoutContext, TLType)`
- `getNodeAttributesStream(TLType, Collection)`
- `getLabelWidth(LayoutContext, TLType)` / `getLabelHeight(LayoutContext, TLType)`
- `getLabelWidth(LayoutContext, TLStructuredTypePart)`
- `getLabel(LabelProvider, TLTypePart)` / `getLabel(LabelProvider, TLType)`
- `getCardinality(TLStructuredTypePart)`
- `getNodeGridWidth(LayoutContext, TLType, int, int)`
- All `*PortLabelWidth()` methods
- All `*PortsGridWidth()` methods
- `getEdgeSourceLabelWidth()` / `getEdgeTargetLabelWidth()`

Remove these methods from the original `LayoutGraphUtil`.

Update all callers in `diagramjs.server` to use `DiagramJSLayoutGraphUtil` instead.

- [ ] **Step 7: Move I18NConstants files**

Move all 5 I18NConstants files (and their generated messages.properties) to `com.top_logic.graph.diagramjs.server`:

```bash
# model/util/I18NConstants.java
# export/I18NConstants.java
# algorithm/crossing/I18NConstants.java
# algorithm/acycle/I18NConstants.java
# math/polynom/I18NConstants.java
```

For the `algorithm/crossing` and `algorithm/acycle` ones: check if they define ResKey constants used by the algorithm code itself. If so, the algorithm code must be changed to not use I18N (or use a simpler mechanism).

- [ ] **Step 8: Strip LayoutContext**

Replace `LayoutContext` with a simple class containing only `LayoutDirection`:

```java
package com.top_logic.graph.layouter;

public class LayoutContext {
    private final LayoutDirection _direction;

    public LayoutContext(LayoutDirection direction) {
        _direction = direction;
    }

    public LayoutDirection getDirection() {
        return _direction;
    }
}
```

Create a subclass `DiagramJSLayoutContext extends LayoutContext` in `com.top_logic.graph.diagramjs.server` that carries the additional fields:

```java
package com.top_logic.graph.diagramjs.server.layout;

public class DiagramJSLayoutContext extends LayoutContext {
    private final LabelProvider _labelProvider;
    private final Collection<Object> _hiddenElements;
    private final Collection<TLType> _hiddenGeneralizations;
    // constructor, getters...
}
```

Update `DiagramJSGraphBuilder` and `GraphModelUtil` in `diagramjs.server` to use `DiagramJSLayoutContext`.

- [ ] **Step 9: Make Sugiyama.layout() accept NodeSizer**

Change `Sugiyama.layout()` to accept a `NodeSizer` parameter:

```java
public void layout(LayoutContext context, LayoutGraph graph, NodeSizer sizer) {
    // ... existing algorithm steps ...
    sizer.size(graph);  // instead of: new DefaultNodeSizer(context).size(graph)
    // ... rest of algorithm ...
}
```

Update callers in `diagramjs.server` to pass `new DefaultNodeSizer(diagramJSContext)`.

- [ ] **Step 10: Decouple DefaultNodePortCoordinateAssigner**

The port coordinate assigner calls `LayoutGraphUtil.*PortLabelWidth()` methods which are now in `DiagramJSLayoutGraphUtil`. Two options:

**Option A (recommended):** Make port coordinate assignment pluggable via an interface. For GraphLayout (no port labels), use a simple implementation that spaces ports evenly. For UML (with port labels), use the existing label-width-based implementation.

**Option B:** Move `DefaultNodePortCoordinateAssigner` to `diagramjs.server` too, and create a `SimpleNodePortCoordinateAssigner` in the layouter that doesn't need label widths.

Choose based on what compiles cleanly.

- [ ] **Step 11: Fix QuadraticEquationSolver**

Remove the `Logger.error()` call or replace with a RuntimeException:

```java
// Before:
Logger.error("Polynomial degree exceeds quadratic.", QuadraticEquationSolver.class);

// After:
throw new IllegalArgumentException("Polynomial degree exceeds quadratic.");
```

- [ ] **Step 12: Clean up pom.xml**

Remove from `com.top_logic.graph.layouter/pom.xml`:
```xml
<dependency>
    <groupId>com.top-logic</groupId>
    <artifactId>tl-core</artifactId>
</dependency>
<!-- Also remove com.top_logic.layout, com.top_logic.model if explicitly listed -->
<!-- Also remove ext-com-google-fonts (AWT font files) -->
```

Ensure `com.top_logic.graph.diagramjs.server/pom.xml` has `tl-graph-layouter` as dependency (it likely already does).

- [ ] **Step 13: Create GWT module**

Create `com.top_logic.graph.layouter/src/main/java/com/top_logic/graph/layouter/TLGraphLayouter.gwt.xml`:

```xml
<module>
    <inherits name="com.google.gwt.user.User" />
    <inherits name="com.top_logic.graph.common.TLGraphCommon" />

    <source path="." />
    <source path="algorithm" />
    <source path="model" />
    <source path="math" />
</module>
```

Exclude any remaining non-GWT packages (text, export) via `<exclude>` if they still exist.

- [ ] **Step 14: Verify build**

```bash
# tl-graph-layouter must compile without server dependencies
mvn -B install -DskipTests=true -pl com.top_logic.graph.layouter 2>&1 | tail -20

# diagramjs.server must still compile with moved code
mvn -B install -DskipTests=true -pl com.top_logic.graph.diagramjs.server 2>&1 | tail -20

# The demo app must still work (legacy UML editor uses diagramjs.server)
mvn -B install -DskipTests=true -pl com.top_logic.demo 2>&1 | tail -20
```

- [ ] **Step 15: Commit**

```
Ticket #29108: Make tl-graph-layouter GWT-compatible.

Move UML-specific code (DefaultNodeSizer, DiagramTextRenderingUtil,
TechnicalNamesLabelProvider, JSONDefaultLayoutGraphExporter, TLModel
utility methods) to com.top_logic.graph.diagramjs.server. Strip
LayoutContext to LayoutDirection only. Make NodeSizer pluggable in
Sugiyama.layout(). Add TLGraphLayouter.gwt.xml.
```

---

## Task 2: Add GraphLayout and GraphEdge to data.proto

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Add new message types to data.proto**

Add after the `TreeLayout` definition:

```protobuf
/**
 * Layout for arbitrary directed graphs using hierarchical (Sugiyama) layout algorithm.
 *
 * Nodes are positioned in layers. Edges are routed orthogonally between layers.
 * Node sizes must be computed before layout (via widget intrinsic size computation).
 * The layout algorithm assigns x/y coordinates to nodes and waypoints to edges.
 *
 * @Operations com.top_logic.react.flow.operations.layout.GraphLayoutOperations
 */
message GraphLayout extends FloatingLayout {
    /** Edges connecting nodes in this graph. */
    repeated GraphEdge edges;

    /** Gap between layers (perpendicular to layout direction). */
    double layerGap;

    /** Gap between nodes within the same layer. */
    double nodeGap;
}

/**
 * Edge in a {@link GraphLayout} connecting two nodes with orthogonal waypoints.
 *
 * @Operations com.top_logic.react.flow.operations.GraphEdgeOperations
 */
message GraphEdge extends Widget {
    /** Source node. */
    Box source;

    /** Target node. */
    Box target;

    /** Waypoints computed by the layout algorithm. */
    repeated GraphWaypoint waypoints;

    /** Stroke color. */
    string strokeStyle = "black";

    /** Line thickness. */
    double thickness = 1.0;

    /** Dash pattern. */
    repeated double dashes;

    /** Decorations on the edge (arrow heads, diamonds, labels). */
    repeated EdgeDecoration decorations;
}

/**
 * A point on an edge path.
 */
message GraphWaypoint {
    double x;
    double y;
}
```

- [ ] **Step 2: Run msgbuf generator and verify**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.react.flow.common 2>&1 | tail -20
```

Expected: BUILD SUCCESS with generated `GraphLayout.java`, `GraphEdge.java`, `GraphWaypoint.java` in `data/` and `data/impl/`.

- [ ] **Step 3: Commit**

```
Ticket #29108: Add GraphLayout, GraphEdge, GraphWaypoint to data.proto.
```

---

## Task 3: Add tl-graph-layouter dependency to react.flow.common

**Files:**
- Modify: `com.top_logic.react.flow.common/pom.xml`
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/common/TLReactFlow.gwt.xml`

- [ ] **Step 1: Add Maven dependency**

Add to `com.top_logic.react.flow.common/pom.xml`:

```xml
<dependency>
    <groupId>com.top-logic</groupId>
    <artifactId>tl-graph-layouter</artifactId>
</dependency>
```

- [ ] **Step 2: Add GWT module inheritance**

Add to `TLReactFlow.gwt.xml`:

```xml
<inherits name="com.top_logic.graph.layouter.TLGraphLayouter" />
```

- [ ] **Step 3: Verify build**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.react.flow.common 2>&1 | tail -20
```

- [ ] **Step 4: Commit**

```
Ticket #29108: Add tl-graph-layouter dependency to react.flow.common.
```

---

## Task 4: Implement GraphLayoutOperations

**Files:**
- Create: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GraphLayoutOperations.java`

- [ ] **Step 1: Implement GraphLayoutOperations**

```java
package com.top_logic.react.flow.operations.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.graph.layouter.LayoutContext;
import com.top_logic.graph.layouter.LayoutDirection;
import com.top_logic.graph.layouter.Sugiyama;
import com.top_logic.graph.layouter.algorithm.node.size.NodeSizer;
import com.top_logic.graph.layouter.model.LayoutGraph;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutEdge;
import com.top_logic.graph.layouter.model.LayoutGraph.LayoutNode;
import com.top_logic.graph.layouter.model.Waypoint;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphLayout;
import com.top_logic.react.flow.data.GraphWaypoint;
import com.top_logic.react.flow.svg.RenderContext;

/**
 * Operations for {@link GraphLayout}.
 *
 * <p>
 * Bridges the flow widget system with the Sugiyama layout algorithm. In
 * {@link #computeIntrinsicSize}, child widget sizes are computed first, then a
 * {@link LayoutGraph} is built and laid out by {@link Sugiyama}. The resulting
 * node positions and edge waypoints are mapped back to the widget model.
 * </p>
 */
public interface GraphLayoutOperations extends FloatingLayoutOperations {

    @Override
    GraphLayout self();

    @Override
    default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
        List<? extends Box> nodes = self().getNodes();
        List<? extends GraphEdge> edges = self().getEdges();

        // Phase 1: Compute intrinsic sizes of all child nodes.
        for (Box node : nodes) {
            node.computeIntrinsicSize(context, 0, 0);
        }

        if (nodes.isEmpty()) {
            self().setWidth(0);
            self().setHeight(0);
            return;
        }

        // Phase 2: Build LayoutGraph from widgets.
        LayoutGraph graph = new LayoutGraph();
        Map<Box, LayoutNode> nodeMap = new HashMap<>();

        for (Box node : nodes) {
            LayoutNode layoutNode = graph.add(new LayoutNode());
            layoutNode.setWidth(node.getWidth());
            layoutNode.setHeight(node.getHeight());
            layoutNode.setUserObject(node);
            nodeMap.put(node, layoutNode);
        }

        for (GraphEdge edge : edges) {
            LayoutNode source = nodeMap.get(edge.getSource());
            LayoutNode target = nodeMap.get(edge.getTarget());
            if (source != null && target != null) {
                LayoutEdge layoutEdge = graph.connect(source, target);
                layoutEdge.setBusinessObject(edge);
            }
        }

        // Phase 3: Run Sugiyama layout.
        // NodeSizer is a no-op — sizes are already set from Phase 1.
        NodeSizer preSized = new NodeSizer(n -> n.getWidth(), n -> n.getHeight());
        LayoutContext layoutContext = new LayoutContext(LayoutDirection.VERTICAL_FROM_SOURCE);
        Sugiyama.INSTANCE.layout(layoutContext, graph, preSized);

        // Phase 4: Map results back to widgets.
        double maxX = 0, maxY = 0;
        for (Box node : nodes) {
            LayoutNode layoutNode = nodeMap.get(node);
            node.setX(layoutNode.getX());
            node.setY(layoutNode.getY());
            maxX = Math.max(maxX, layoutNode.getX() + layoutNode.getWidth());
            maxY = Math.max(maxY, layoutNode.getY() + layoutNode.getHeight());
        }

        // Copy edge waypoints.
        for (GraphEdge edge : edges) {
            LayoutNode source = nodeMap.get(edge.getSource());
            LayoutNode target = nodeMap.get(edge.getTarget());
            if (source != null && target != null) {
                LayoutEdge layoutEdge = findEdge(source, target);
                if (layoutEdge != null && layoutEdge.getWaypoints().isPresent()) {
                    edge.getWaypoints().clear();
                    for (Waypoint wp : layoutEdge.getWaypoints().get()) {
                        edge.addWaypoint(GraphWaypoint.create().setX(wp.getX()).setY(wp.getY()));
                        maxX = Math.max(maxX, wp.getX());
                        maxY = Math.max(maxY, wp.getY());
                    }
                }
            }
        }

        self().setWidth(maxX);
        self().setHeight(maxY);
    }

    private static LayoutEdge findEdge(LayoutNode source, LayoutNode target) {
        for (LayoutEdge edge : source.outgoing()) {
            if (edge.target() == target) {
                return edge;
            }
        }
        return null;
    }

    @Override
    default void draw(com.top_logic.react.flow.svg.SvgWriter out) {
        // Draw nodes (inherited from FloatingLayoutOperations).
        FloatingLayoutOperations.super.draw(out);

        // Draw edges.
        for (GraphEdge edge : self().getEdges()) {
            edge.draw(out);
        }
    }
}
```

**Important notes:**
- The exact `LayoutGraph` API (`graph.add()`, `graph.connect()`, `source.outgoing()`) must be verified against the actual class. Adjust constructor calls and method names as needed.
- The `Sugiyama.layout()` signature depends on Task 1's changes (added `NodeSizer` parameter).
- `LayoutDirection.VERTICAL_FROM_SOURCE` is the default; this can later be made configurable via a `GraphLayout` property.

- [ ] **Step 2: Verify build**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.react.flow.common 2>&1 | tail -20
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Implement GraphLayoutOperations with Sugiyama integration.
```

---

## Task 5: Implement GraphEdgeOperations

**Files:**
- Create: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/GraphEdgeOperations.java`

- [ ] **Step 1: Implement edge rendering**

```java
package com.top_logic.react.flow.operations;

import java.util.List;

import com.top_logic.react.flow.data.GraphEdge;
import com.top_logic.react.flow.data.GraphWaypoint;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for {@link GraphEdge}.
 *
 * <p>
 * Renders edges as orthogonal SVG polylines using waypoints computed by the layout algorithm.
 * </p>
 */
public interface GraphEdgeOperations extends WidgetOperations {

    @Override
    GraphEdge self();

    @Override
    default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
        // Edge size is determined by waypoints, computed during GraphLayout.
        // Nothing to do here.
    }

    @Override
    default void distributeSize(RenderContext context, double offsetX, double offsetY,
            double width, double height) {
        // No-op — edges don't participate in size distribution.
    }

    @Override
    default void draw(SvgWriter out) {
        List<? extends GraphWaypoint> waypoints = self().getWaypoints();
        if (waypoints.size() < 2) {
            return;
        }

        StringBuilder pathData = new StringBuilder();
        GraphWaypoint first = waypoints.get(0);
        pathData.append("M ").append(first.getX()).append(' ').append(first.getY());
        for (int i = 1; i < waypoints.size(); i++) {
            GraphWaypoint wp = waypoints.get(i);
            pathData.append(" L ").append(wp.getX()).append(' ').append(wp.getY());
        }

        out.beginGroup(self());
        out.writePath(pathData.toString(),
            self().getStrokeStyle(),
            self().getThickness(),
            self().getDashes());

        // Draw edge decorations (arrows, diamonds) if any.
        for (var decoration : self().getDecorations()) {
            decoration.draw(out);
        }

        out.endGroup();
    }
}
```

**Note:** The exact `SvgWriter` API for path rendering (`writePath()`) must be verified against the existing implementation. The `TreeConnectionOperations` in the same codebase is a good reference for how edges are rendered.

- [ ] **Step 2: Verify build**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.react.flow.common 2>&1 | tail -20
```

- [ ] **Step 3: Commit**

```
Ticket #29108: Implement GraphEdgeOperations for orthogonal edge rendering.
```

---

## Task 6: Add TL-Script functions in FlowFactory

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java`

- [ ] **Step 1: Add graphLayout() function**

Add to `FlowFactory.java`:

```java
/**
 * Factory for {@link GraphLayout}s.
 *
 * <p>
 * Arranges nodes in a directed graph using hierarchical (layered) layout.
 * </p>
 *
 * @param nodes
 *        The nodes to arrange.
 * @param edges
 *        The edges connecting nodes.
 * @param layerGap
 *        Gap between layers.
 * @param nodeGap
 *        Gap between nodes in the same layer.
 * @param cssClass
 *        The CSS class for the layout container.
 * @param userObject
 *        User object of the layout.
 * @return The new graph layout.
 */
@SideEffectFree
@Label("Create graph layout")
public static Box graphLayout(
    @Mandatory List<? extends Box> nodes,
    @Mandatory List<? extends GraphEdge> edges,
    @DoubleDefault(60) double layerGap,
    @DoubleDefault(30) double nodeGap,
    String cssClass,
    Object userObject
) {
    return GraphLayout.create()
        .setNodes(nodes.stream().filter(Objects::nonNull).toList())
        .setEdges(edges.stream().filter(Objects::nonNull).toList())
        .setLayerGap(layerGap)
        .setNodeGap(nodeGap)
        .setCssClass(cssClass)
        .setUserObject(userObject);
}
```

- [ ] **Step 2: Add graphEdge() function**

```java
/**
 * Factory for {@link GraphEdge}s.
 *
 * @param source
 *        The source node.
 * @param target
 *        The target node.
 * @param strokeStyle
 *        Stroke color.
 * @param thickness
 *        Line thickness.
 * @param dashes
 *        Dash pattern.
 * @param decorations
 *        Edge decorations (arrows, diamonds).
 * @return The new graph edge.
 */
@SideEffectFree
@Label("Create graph edge")
public static GraphEdge graphEdge(
    @Mandatory Box source,
    @Mandatory Box target,
    @StringDefault("black") String strokeStyle,
    @DoubleDefault(1) double thickness,
    List<Double> dashes,
    List<? extends EdgeDecoration> decorations
) {
    GraphEdge edge = GraphEdge.create()
        .setSource(source)
        .setTarget(target)
        .setStrokeStyle(strokeStyle)
        .setThickness(thickness);
    if (dashes != null) {
        edge.setDashes(dashes);
    }
    if (decorations != null) {
        edge.setDecorations(decorations);
    }
    return edge;
}
```

- [ ] **Step 3: Verify build**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.react.flow.server 2>&1 | tail -20
```

- [ ] **Step 4: Commit**

```
Ticket #29108: Add reactFlowGraphLayout() and reactFlowGraphEdge() TL-Script functions.
```

---

## Task 7: Demo — Graph Layout in flow-diagram-demo.view.xml

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/flow-diagram-demo.view.xml`

- [ ] **Step 1: Replace reactFlowTree with reactFlowGraphLayout**

In the construction plan demo, replace `reactFlowTree()` with `reactFlowGraphLayout()` and `reactFlowConnection()` with `reactFlowGraphEdge()`:

Change this section in the createChart script:

```
reactFlowTree(
    nodes: $nodes,
    connections: $parts
        .filter(...)
        .map(... -> reactFlowConnection(...))
        .flatten()
)
```

To:

```
reactFlowGraphLayout(
    $nodes,
    $parts
        .filter(p -> !$p.get(`test.flowchart:FlowNode#inputs`).isEmpty())
        .map(p -> $p
            .get(`test.flowchart:FlowNode#inputs`)
            .map(c -> reactFlowGraphEdge(
                source: $nodeByPart[$p],
                target: $nodeByPart[$c])))
        .flatten()
)
```

Note: `reactFlowTree` used `parent`/`child` parameter names matching the tree metaphor. `reactFlowGraphEdge` uses `source`/`target`. The edge direction may need to be flipped depending on whether the Sugiyama algorithm expects source→target to flow top→bottom.

- [ ] **Step 2: Restart and test**

```bash
mvn -B install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server,com.top_logic.react.flow.client,com.top_logic.demo
.claude/scripts/tl-app.sh restart com.top_logic.demo
```

Verify in the browser:
1. Navigate to Flow-Diagramm
2. Select "Lego" product
3. The construction plan should render with nodes arranged in layers
4. Edges should be orthogonal lines between nodes

- [ ] **Step 3: Commit**

```
Ticket #29108: Use GraphLayout in construction plan demo view.
```

---

## Summary

| Task | Description | Complexity | Dependencies |
|------|-------------|------------|--------------|
| 1 | Make tl-graph-layouter GWT-compatible | High — refactoring with many files | None |
| 2 | Add GraphLayout/GraphEdge to data.proto | Low — proto additions + msgbuf generate | None |
| 3 | Add tl-graph-layouter dependency | Low — pom.xml + gwt.xml | Task 1 |
| 4 | Implement GraphLayoutOperations | Medium — Sugiyama bridge | Tasks 1, 2, 3 |
| 5 | Implement GraphEdgeOperations | Low — SVG path rendering | Task 2 |
| 6 | Add TL-Script functions | Low — FlowFactory additions | Tasks 2, 4, 5 |
| 7 | Demo view | Low — XML change | Task 6 |

**Critical path:** Task 1 → Task 3 → Task 4 → Task 6 → Task 7

Tasks 2 and 5 can run in parallel with Task 1.
