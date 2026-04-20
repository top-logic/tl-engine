# Gantt Phase 2.1 — DragConstraints + Item-Move/Resize — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add generic drag infrastructure to the Flow library (DragController interface, pointer-event-based drag mechanism with clone visualization) and implement Gantt-specific item move/resize as the first consumer.

**Architecture:** Three layers: (1) proto types `DragEdge`/`DropArea` in common, (2) `DragController` Java interface in common, (3) GWT pointer-event drag mechanism in client that finds DragControllers by walking the Box parent tree, clones the dragged box for WYSIWYG feedback, and calls `commitDrag` on drop. `GanttLayoutOperations` implements `DragController` with time-snap and row-snap constraints.

**Tech Stack:** msgbuf proto, Java 17, GWT (elemental2 / Vectomatic SVG DOM), TopLogic ReactBridge.

**Spec:** `docs/superpowers/specs/2026-04-20-gantt-phase2-drag-design.md`

---

## File Structure

**Proto (common module):**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto` — add `DragEdge`, `DropArea`

**Interface (common module):**
- Create: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/drag/DragController.java`
- Create: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/drag/DragState.java` (mutable state holder for active drag session)

**GWT client:**
- Modify: `com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java` — add pointer event listeners, drag orchestration
- Create: `com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/DragHandler.java` — drag lifecycle logic (extracted for clarity)

**Gantt implementation (common module):**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java` — implement DragController

**Tests (server module):**
- Modify: `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java` — add constraint tests

**Demo:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml` — add drag-testable items

---

### Task 1: Add `DragEdge` enum and `DropArea` message to proto

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Add types to proto**

Append at end of `data.proto`:

```proto
/** Edge of a box for resize operations. */
enum DragEdge {
	/** Top edge. */
	N;
	/** Right edge. */
	E;
	/** Bottom edge. */
	S;
	/** Left edge. */
	W;
}

/** A rectangular region where a box may be dropped during drag. */
message DropArea {
	/** X coordinate of the top-left corner (layout units). */
	double x;

	/** Y coordinate of the top-left corner (layout units). */
	double y;

	/** Width of the area (layout units). */
	double width;

	/** Height of the area (layout units). */
	double height;
}
```

- [ ] **Step 2: Build**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: BUILD SUCCESS. Generated `DragEdge.java`, `DropArea.java`, `DropArea_Impl.java`.

- [ ] **Step 3: Verify generated files**

```bash
find com.top_logic.react.flow.common -name "DragEdge*.java" -o -name "DropArea*.java" 2>/dev/null
```

- [ ] **Step 4: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Add DragEdge enum and DropArea message to proto.

Proto types for the generic drag infrastructure. DragEdge identifies
which edge of a box is being resized (N/E/S/W). DropArea is a rectangle
describing where a box may be dropped. Both can be transmitted via
model-sync so servers can pre-compute drop areas for layouts that need
application-specific validation.

User prompt: "leg los"
EOF
)"
```

---

### Task 2: Create `DragController` interface

**Files:**
- Create: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/drag/DragController.java`

- [ ] **Step 1: Create the interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.drag;

import java.util.List;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;

/**
 * Interface for layouts that support interactive drag-and-drop of their child boxes.
 *
 * <p>
 * A box is draggable if walking up its parent tree finds a parent that implements
 * {@link DragController} and returns {@code true} from {@link #canMove} or
 * {@link #canResize} for its direct child. The drag system calls the controller's
 * methods during the drag lifecycle.
 * </p>
 *
 * <p>
 * All methods run on the client (GWT-compiled). They must be synchronous and must
 * not initiate server communication. Layouts that need server-computed drop areas
 * should pre-populate them in the model before sending to the client.
 * </p>
 */
public interface DragController {

	/** Whether this box may be moved by dragging its body. */
	boolean canMove(Box box);

	/** Whether this box may be resized by dragging the given edge. */
	boolean canResize(Box box, DragEdge edge);

	/**
	 * Valid drop areas for this box during a drag. Called once at drag-start; the result
	 * is cached for the duration of the drag.
	 *
	 * <p>
	 * Implementations may compute areas from the model (e.g., Gantt row geometry) or
	 * read pre-computed areas from a model field (e.g., server-populated drop areas).
	 * </p>
	 */
	List<DropArea> getDropAreas(Box box);

	/**
	 * Constrain a proposed absolute position during a move drag. Called every frame.
	 *
	 * @param box
	 *        the box being dragged (original, not the clone)
	 * @param proposedX
	 *        proposed X from mouse offset
	 * @param proposedY
	 *        proposed Y from mouse offset
	 * @return corrected position; the drag clone is placed here
	 */
	double[] constrainMove(Box box, double proposedX, double proposedY);

	/**
	 * Constrain a proposed absolute edge position during a resize drag. Called every frame.
	 *
	 * @param box
	 *        the box being resized (original)
	 * @param edge
	 *        which edge is being dragged
	 * @param proposedEdgePos
	 *        proposed position of that edge
	 * @return corrected edge position
	 */
	double constrainResize(Box box, DragEdge edge, double proposedEdgePos);

	/**
	 * Commit the drag result to the model. Called on successful drop (mouseup).
	 *
	 * <p>
	 * The box still has its original position/size. The final values come as parameters.
	 * The controller compares with the originals to determine what changed (move vs.
	 * resize, which edge, row change) and writes the appropriate model mutations.
	 * </p>
	 *
	 * <p>
	 * After this method returns, the model is dirty and the change is transmitted to the
	 * server as a model patch via the standard update channel.
	 * </p>
	 */
	void commitDrag(Box box, double finalX, double finalY,
		double finalWidth, double finalHeight);

	/**
	 * Drag cancelled (ESC or pointer left the diagram). No model changes.
	 * Implementations should clean up any cached state (e.g., drop area maps).
	 */
	void cancelDrag(Box box);
}
```

**Note:** `constrainMove` returns `double[]` (2-element array `{x, y}`) instead of a `Point` object, because `Point` in the Flow library is a msgbuf message (heavyweight for a per-frame return value). If the implementer finds a lighter-weight Point type available in the common module, use that instead. **Check:**

```bash
grep -rn "class Point\|interface Point" --include="*.java" com.top_logic.react.flow.common/src/main/java 2>/dev/null | head -5
```

If `com.top_logic.react.flow.data.Point` is a msgbuf message, keep `double[]`. If there's a plain Java `Point` class, use it.

- [ ] **Step 2: Build**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/drag/
git commit -m "$(cat <<'EOF'
Ticket #29108: Create DragController interface.

Generic interface for layouts supporting interactive drag. A box is
draggable when its parent implements DragController and permits
move/resize. All methods run client-side (GWT); server interaction
happens through the model-sync channel after commitDrag.

User prompt: "leg los"
EOF
)"
```

---

### Task 3: Implement `DragController` on `GanttLayoutOperations`

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`
- Modify: `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java`

This task implements the Gantt-specific DragController logic. It can be fully tested without the GWT drag mechanism (pure Java, no DOM).

- [ ] **Step 1: Add failing test for `canMove`**

Append to `TestGanttLayout`:

```java
	public void testCanMoveTrueByDefault() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task 1");
		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1")))
			.setItems(Arrays.asList(span));
		layout.addContent(span.getBox());

		assertTrue("canMove default", ((DragController) layout).canMove(span.getBox()));
	}

	public void testCanMoveFalseWhenDisabled() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task 1");
		span.setCanMoveTime(false);
		span.setCanMoveRow(false);

		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1")))
			.setItems(Arrays.asList(span));
		layout.addContent(span.getBox());

		assertFalse("canMove disabled", ((DragController) layout).canMove(span.getBox()));
	}
```

Add import: `import com.top_logic.react.flow.operations.drag.DragController;`

- [ ] **Step 2: Run — expect FAIL** (GanttLayoutOperations doesn't implement DragController yet)

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout#testCanMoveTrueByDefault
```

Expected: compile error or ClassCastException.

- [ ] **Step 3: Implement DragController on GanttLayoutOperations**

Make `GanttLayoutOperations` extend `DragController`:

```java
public interface GanttLayoutOperations extends BoxOperations, DragController {
```

Add all required method implementations as `default` methods. **Read the current file first** to understand the existing structure, then add the methods after the existing rendering code.

**`canMove`:**
```java
	@Override
	default boolean canMove(Box box) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) return false;
		return item.getCanMoveTime() || item.getCanMoveRow();
	}
```

**`canResize`:**
```java
	@Override
	default boolean canResize(Box box, DragEdge edge) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) return false;
		if (!(item instanceof GanttSpan span)) return false;
		return switch (edge) {
			case W -> span.getCanResizeStart();
			case E -> span.getCanResizeEnd();
			default -> false;
		};
	}
```

**`getDropAreas`:**
```java
	@Override
	default List<DropArea> getDropAreas(Box box) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) return java.util.Collections.emptyList();

		List<DropArea> areas = new java.util.ArrayList<>();
		double columnWidth = self.getColumnWidth();
		double chartWidth = self.getWidth() - columnWidth;
		// Walk the row geometry to get Y ranges per row.
		// Use the same buildRowGeometry helper that draw uses.
		Map<String, RowGeometry> geometry = buildRowGeometry(self);
		for (RowGeometry rg : geometry.values()) {
			if (!item.getCanMoveRow() && !rg.id().equals(item.getRowId())) {
				continue;
			}
			double areaX = item.getCanMoveTime() ? columnWidth : box.getX();
			double areaW = item.getCanMoveTime() ? chartWidth : box.getWidth();
			areas.add(DropArea.create()
				.setX(areaX).setY(rg.y())
				.setWidth(areaW).setHeight(rg.height()));
		}
		return areas;
	}
```

**Note:** `buildRowGeometry` and `RowGeometry` must already exist from Phase 1 (the variable-row-height refactor). Inspect the file to find the exact names/signatures. If they're private to the draw methods, extract them into a shared helper. Adapt the code above to match what actually exists.

**`constrainMove`:**
```java
	@Override
	default double[] constrainMove(Box box, double proposedX, double proposedY) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		GanttAxis axis = self.getAxis();

		double x = proposedX;
		double y = proposedY;

		if (item != null && !item.getCanMoveTime()) {
			x = box.getX();
		} else {
			// Snap X to grid.
			double snap = axis.getSnapGranularity() * axis.getCurrentZoom();
			if (snap > 0) {
				double offset = self.getColumnWidth() + axis.getRangeMin() * axis.getCurrentZoom();
				x = Math.round((x - offset) / snap) * snap + offset;
			}
			// Clamp to chart area.
			double minX = self.getColumnWidth();
			double maxX = self.getWidth() - box.getWidth();
			x = Math.max(minX, Math.min(maxX, x));
		}

		if (item != null && !item.getCanMoveRow()) {
			y = box.getY();
		} else {
			// Snap Y to nearest valid row.
			List<DropArea> areas = getDropAreas(box);
			double bestY = y;
			double bestDist = Double.MAX_VALUE;
			double padding = self.getRowPadding();
			for (DropArea area : areas) {
				double rowY = area.getY() + padding;
				double dist = Math.abs(y - rowY);
				if (dist < bestDist) {
					bestDist = dist;
					bestY = rowY;
				}
			}
			y = bestY;
		}

		return new double[] { x, y };
	}
```

**`constrainResize`:**
```java
	@Override
	default double constrainResize(Box box, DragEdge edge, double proposedEdgePos) {
		GanttLayout self = (GanttLayout) this;
		GanttAxis axis = self.getAxis();

		// Snap to grid.
		double snap = axis.getSnapGranularity() * axis.getCurrentZoom();
		double pos = proposedEdgePos;
		if (snap > 0) {
			double offset = self.getColumnWidth() + axis.getRangeMin() * axis.getCurrentZoom();
			pos = Math.round((pos - offset) / snap) * snap + offset;
		}

		// Prevent crossing: minimum 1 snap unit width.
		double minWidth = Math.max(snap, 1.0);
		if (edge == DragEdge.W) {
			pos = Math.min(pos, box.getX() + box.getWidth() - minWidth);
		} else if (edge == DragEdge.E) {
			pos = Math.max(pos, box.getX() + minWidth);
		}

		return pos;
	}
```

**`commitDrag`:**
```java
	@Override
	default void commitDrag(Box box, double finalX, double finalY,
			double finalWidth, double finalHeight) {
		GanttLayout self = (GanttLayout) this;
		GanttItem item = findItemByBox(self, box);
		if (item == null) return;

		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double columnWidth = self.getColumnWidth();
		double rangeMin = axis.getRangeMin();

		if (item instanceof GanttSpan span) {
			double deltaX = finalX - box.getX();
			double deltaW = finalWidth - box.getWidth();

			if (deltaW == 0) {
				// Pure move: shift both start and end.
				double newStart = (finalX - columnWidth) / zoom + rangeMin;
				double duration = span.getEnd() - span.getStart();
				span.setStart(newStart);
				span.setEnd(newStart + duration);
			} else if (deltaX != 0 && deltaW != 0) {
				// Resize start (WEST edge moved).
				double newStart = (finalX - columnWidth) / zoom + rangeMin;
				span.setStart(newStart);
			} else {
				// Resize end (EAST edge moved).
				double newEnd = (finalX + finalWidth - columnWidth) / zoom + rangeMin;
				span.setEnd(newEnd);
			}
		} else if (item instanceof GanttPoint point) {
			double newAt = (finalX + finalWidth / 2.0 - columnWidth) / zoom + rangeMin;
			point.setAt(newAt);
		}

		// Row change: determine target row from Y position.
		double deltaY = finalY - box.getY();
		if (deltaY != 0) {
			Map<String, RowGeometry> geometry = buildRowGeometry(self);
			for (RowGeometry rg : geometry.values()) {
				if (finalY >= rg.y() && finalY < rg.y() + rg.height()) {
					item.setRowId(rg.id());
					break;
				}
			}
		}
	}
```

**`cancelDrag`:**
```java
	@Override
	default void cancelDrag(Box box) {
		// No persistent state to clean up in the current implementation.
	}
```

**Helper `findItemByBox`:**
```java
	private static GanttItem findItemByBox(GanttLayout layout, Box box) {
		for (GanttItem item : layout.getItems()) {
			if (item.getBox() == box) {
				return item;
			}
		}
		return null;
	}
```

Add imports:
```java
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.DropArea;
import com.top_logic.react.flow.operations.drag.DragController;
```

- [ ] **Step 4: Build and run tests**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: all tests PASS.

- [ ] **Step 5: Add more constraint tests**

Append to `TestGanttLayout`:

```java
	public void testConstrainMoveSnapsToGrid() {
		GanttLayout layout = buildSimpleLayout();
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		GanttSpan span = (GanttSpan) layout.getItems().get(0);
		DragController dc = (DragController) layout;

		double[] result = dc.constrainMove(span.getBox(), 205.7, span.getBox().getY());
		// snapGranularity = 1.0, zoom = 1.0 → snap to integer positions.
		assertEquals("snapped X", Math.round(205.7), result[0], 0.5);
	}

	public void testGetDropAreasReturnsAllRowsWhenMovable() {
		GanttLayout layout = buildSimpleLayout();
		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		GanttSpan span = (GanttSpan) layout.getItems().get(0);
		DragController dc = (DragController) layout;

		List<DropArea> areas = dc.getDropAreas(span.getBox());
		assertEquals("one area per row", 2, areas.size());
	}

	public void testGetDropAreasRestrictedWhenCanMoveRowFalse() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		span.setCanMoveRow(false);

		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1"), row("r2", "Row 2")))
			.setItems(Arrays.asList(span));
		layout.addContent(span.getBox());

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		DragController dc = (DragController) layout;
		List<DropArea> areas = dc.getDropAreas(span.getBox());
		assertEquals("only current row", 1, areas.size());
	}

	private GanttLayout buildSimpleLayout() {
		GanttSpan span = span("s1", "r1", 10.0, 30.0, "Task");
		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1"), row("r2", "Row 2")))
			.setItems(Arrays.asList(span));
		layout.addContent(span.getBox());
		return layout;
	}
```

Add import: `import com.top_logic.react.flow.data.DropArea;`

- [ ] **Step 6: Run all tests**

```bash
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: all PASS.

- [ ] **Step 7: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: GanttLayoutOperations implements DragController.

canMove/canResize delegate to per-item flags. getDropAreas builds
rectangles from the row geometry. constrainMove snaps X to the
axis grid and Y to the nearest valid row. constrainResize prevents
start/end crossing. commitDrag writes position and rowId back to
the item model. All logic is purely client-side (no server calls).

Tests verify canMove flags, drop area computation, and X-snap.

User prompt: "leg los"
EOF
)"
```

---

### Task 4: GWT pointer event infrastructure

**Files:**
- Create: `com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/DragHandler.java`
- Modify: `com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java`

This is the core GWT task — implementing the drag mechanism on the client. It requires working with the Vectomatic SVG DOM and elemental2 pointer events.

- [ ] **Step 1: Read the existing code thoroughly**

Before writing any code, read these files completely and understand the patterns:

```bash
cat com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java
cat com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/SVGBuilder.java
cat com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/dom/DOMUtil.java
```

Note:
- How `_svg` (OMSVGSVGElement) is created and managed
- How event listeners are registered (lines 440–445)
- How `SubIdGenerator._nextId` assigns client IDs to model objects
- How `svgBuilder()` returns a builder that links model objects to SVG elements
- How `applyScopeChanges` incrementally re-renders dirty widgets

- [ ] **Step 2: Create `DragHandler.java`**

This class encapsulates the drag lifecycle, keeping FlowDiagramClientControl clean. It needs access to the `Diagram`, the SVG root, and the render context.

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.client.control;

import java.util.List;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.DragEdge;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.DropArea;
import com.top_logic.react.flow.data.Widget;
import com.top_logic.react.flow.operations.drag.DragController;
import com.top_logic.react.flow.svg.RenderContext;

/**
 * Manages the lifecycle of a single drag operation within a flow diagram.
 *
 * <p>
 * Created by {@link FlowDiagramClientControl} on pointer-down; destroyed on
 * pointer-up or ESC.
 * </p>
 */
public class DragHandler {

	private static final double EDGE_ZONE_PX = 4.0;

	private final Diagram _diagram;
	private final RenderContext _renderContext;

	// Drag session state (set during startDrag, cleared in endDrag/cancelDrag).
	private DragController _controller;
	private Box _dragTarget;
	private Box _clone;
	private DragEdge _resizeEdge;
	private boolean _isResize;
	private double _offsetX;
	private double _offsetY;
	private double _origX;
	private double _origY;
	private double _origW;
	private double _origH;
	private List<DropArea> _dropAreas;
	private boolean _active;

	public DragHandler(Diagram diagram, RenderContext renderContext) {
		_diagram = diagram;
		_renderContext = renderContext;
	}

	public boolean isActive() {
		return _active;
	}

	/**
	 * Attempt to start a drag on the given box at the given pointer position.
	 *
	 * @return {@code true} if drag started (a DragController was found), {@code false} otherwise.
	 */
	public boolean startDrag(Box hitBox, double pointerX, double pointerY) {
		// Detect resize edge.
		DragEdge edge = detectEdge(hitBox, pointerX, pointerY);
		boolean resize = (edge != null);

		// Walk up parent tree to find DragController.
		Box candidate = hitBox;
		while (candidate != null) {
			Widget parent = candidate.getParent();
			if (parent instanceof DragController dc) {
				Box directChild = candidate;
				if (resize && dc.canResize(directChild, edge)) {
					_controller = dc;
					_dragTarget = directChild;
					_isResize = true;
					_resizeEdge = edge;
					break;
				} else if (!resize && dc.canMove(directChild)) {
					_controller = dc;
					_dragTarget = directChild;
					_isResize = false;
					_resizeEdge = null;
					break;
				}
			}
			candidate = (parent instanceof Box b) ? b : null;
		}

		if (_controller == null) return false;

		_origX = _dragTarget.getX();
		_origY = _dragTarget.getY();
		_origW = _dragTarget.getWidth();
		_origH = _dragTarget.getHeight();
		_offsetX = pointerX - _origX;
		_offsetY = pointerY - _origY;
		_dropAreas = _controller.getDropAreas(_dragTarget);
		_active = true;

		// Clone will be created by the caller (needs SVG access).
		return true;
	}

	/** Update clone position/size based on current pointer position. */
	public void updateDrag(double pointerX, double pointerY) {
		if (!_active) return;

		if (_isResize) {
			double proposedEdgePos;
			if (_resizeEdge == DragEdge.W) {
				proposedEdgePos = pointerX;
			} else if (_resizeEdge == DragEdge.E) {
				proposedEdgePos = pointerX;
			} else if (_resizeEdge == DragEdge.N) {
				proposedEdgePos = pointerY;
			} else {
				proposedEdgePos = pointerY;
			}
			double corrected = _controller.constrainResize(_dragTarget, _resizeEdge, proposedEdgePos);

			if (_clone != null) {
				if (_resizeEdge == DragEdge.W) {
					double newW = (_origX + _origW) - corrected;
					_clone.setX(corrected);
					_clone.setWidth(newW);
				} else if (_resizeEdge == DragEdge.E) {
					double newW = corrected - _origX;
					_clone.setX(_origX);
					_clone.setWidth(newW);
				} else if (_resizeEdge == DragEdge.N) {
					double newH = (_origY + _origH) - corrected;
					_clone.setY(corrected);
					_clone.setHeight(newH);
				} else { // S
					double newH = corrected - _origY;
					_clone.setY(_origY);
					_clone.setHeight(newH);
				}
			}
		} else {
			double proposedX = pointerX - _offsetX;
			double proposedY = pointerY - _offsetY;
			double[] constrained = _controller.constrainMove(_dragTarget, proposedX, proposedY);
			if (_clone != null) {
				_clone.setX(constrained[0]);
				_clone.setY(constrained[1]);
			}
		}

		// Re-layout clone for WYSIWYG.
		if (_clone != null) {
			_clone.computeIntrinsicSize(_renderContext, _clone.getX(), _clone.getY());
			_clone.distributeSize(_renderContext, _clone.getX(), _clone.getY(),
				_clone.getWidth(), _clone.getHeight());
		}
	}

	/** Complete the drag — write model mutation. */
	public void commitDrag() {
		if (!_active) return;

		if (_clone != null) {
			_controller.commitDrag(_dragTarget,
				_clone.getX(), _clone.getY(),
				_clone.getWidth(), _clone.getHeight());
		}

		cleanup();
	}

	/** Cancel the drag — no model changes. */
	public void cancelDrag() {
		if (!_active) return;
		_controller.cancelDrag(_dragTarget);
		cleanup();
	}

	public Box getDragTarget() {
		return _dragTarget;
	}

	public void setClone(Box clone) {
		_clone = clone;
	}

	public Box getClone() {
		return _clone;
	}

	public boolean isInsideDropArea(double x, double y) {
		if (_dropAreas == null) return false;
		for (DropArea area : _dropAreas) {
			if (x >= area.getX() && x <= area.getX() + area.getWidth()
				&& y >= area.getY() && y <= area.getY() + area.getHeight()) {
				return true;
			}
		}
		return false;
	}

	private void cleanup() {
		_controller = null;
		_dragTarget = null;
		_clone = null;
		_dropAreas = null;
		_active = false;
	}

	private DragEdge detectEdge(Box box, double px, double py) {
		double relX = px - box.getX();
		double relY = py - box.getY();
		double w = box.getWidth();
		double h = box.getHeight();

		if (relX < EDGE_ZONE_PX && relX >= 0) return DragEdge.W;
		if (relX > w - EDGE_ZONE_PX && relX <= w) return DragEdge.E;
		if (relY < EDGE_ZONE_PX && relY >= 0) return DragEdge.N;
		if (relY > h - EDGE_ZONE_PX && relY <= h) return DragEdge.S;
		return null;
	}
}
```

- [ ] **Step 3: Wire DragHandler into FlowDiagramClientControl**

This step requires modifying `FlowDiagramClientControl` to register pointer event listeners and orchestrate the drag via `DragHandler`. The integration involves:

1. **Add fields:**
```java
private DragHandler _dragHandler;
```

2. **Register pointer events** in the same place where other events are registered (around lines 440–445). Add listeners for `"pointerdown"`, `"pointermove"`, `"pointerup"`, and `"keydown"` (for ESC).

**Before implementing**, inspect these aspects of the existing code:
- How `_control` (the HTML container div) registers event listeners
- How to convert pointer event client coordinates to SVG diagram coordinates (the viewBox transform) — look at how `panSVG` handles coordinate conversion
- How to find which Box was hit from a DOM event (need `event.target` → walk up to find an element with a client ID → look up in model)
- How to serialize/deserialize a Box for cloning (JSON round-trip via the `_scope`)

The implementer should:

a) Add a `pointerdown` listener that:
   - Converts pointer coordinates to SVG/diagram space
   - Walks up from `event.target` in the DOM to find a model-linked element (one with a clientId)
   - Looks up the Box in the model via `_scope` or by traversing the diagram tree
   - Calls `_dragHandler.startDrag(box, diagramX, diagramY)`
   - If started: creates a clone (JSON serialize/deserialize the drag target box), assigns it to the handler, dims the original SVG element (`DOMUtil.addClassName(el, "tl-drag-source")`), renders the clone into a new SVG `<g>` group
   - Calls `event.preventDefault()` to suppress the default pan-by-drag behavior

b) Add a `pointermove` listener that:
   - If `_dragHandler.isActive()`: converts coordinates, calls `updateDrag`, re-renders the clone SVG, updates cursor (`"no-drop"` if outside drop areas, `"grabbing"` otherwise)

c) Add a `pointerup` listener that:
   - If active: calls `commitDrag()`, removes clone SVG group, removes `tl-drag-source` class from original, triggers `onChange()` to send the model patch

d) Add a `keydown` listener for ESC that:
   - If active: calls `cancelDrag()`, removes clone SVG, restores original

**CSS class for dimming:**
Add to the diagram's CSS (or inline on the SVG element):
```css
.tl-drag-source { opacity: 0.3; }
```

**This task will require significant adaptation to the actual code structure.** The plan provides the conceptual flow; the implementer must adapt to the real GWT event APIs, coordinate transforms, and SVG DOM methods found in the codebase.

**Key references in the existing code:**
- Coordinate conversion: look at lines 345–381 (pan logic) for how `event.clientX/Y` maps to SVG coordinates
- SVG element lookup: line 526 (`_svg.getElementById(id)`)
- Client ID assignment: lines 867–875
- Model change notification: lines 680–703 (`onChange()` debounce)
- CSS class manipulation: `DOMUtil.addClassName/removeClassName`

- [ ] **Step 4: Build the GWT client module**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.client -DskipTests=true
```

Expected: BUILD SUCCESS.

- [ ] **Step 5: Commit**

```bash
git add -u
git add com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/DragHandler.java
git commit -m "$(cat <<'EOF'
Ticket #29108: Add pointer-event drag mechanism to Flow client.

DragHandler encapsulates a single drag session: edge detection,
DragController lookup by parent-tree walk, clone management,
constraint application, and commit/cancel. FlowDiagramClientControl
wires it into the SVG via pointerdown/pointermove/pointerup listeners.
Clone boxes are serialized/deserialized for deep copy and re-laid-out
each frame for WYSIWYG feedback. Original is dimmed with CSS class
tl-drag-source.

User prompt: "leg los"
EOF
)"
```

---

### Task 5: Update demo for drag testing

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml`

- [ ] **Step 1: Add drag-constrained demo items**

Read the current demo file:
```bash
cat com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml
```

Add two new items that demonstrate constraints:

1. A span with `canMoveRow: false` → can only slide on the time axis:
```
fixed_row = reactFlowGanttSpan(model: "task:fixed-row", rowModel: "phase:design",
    box: reactFlowBorder(reactFlowPadding(reactFlowText("Fixed Row"), all: 3)),
    start: 20470, end: 20478, canMoveRow: false);
```

2. A span with `canResize: false` → can move but not resize:
```
no_resize = reactFlowGanttSpan(model: "task:no-resize", rowModel: "phase:impl",
    box: reactFlowBorder(reactFlowPadding(reactFlowText("No Resize"), all: 3)),
    start: 20490, end: 20500, canResize: false);
```

Add them to the `items` list and update the `rootRows` call if needed.

- [ ] **Step 2: Build demo**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.demo -DskipTests=true
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Add drag-constrained items to Gantt demo.

Two new demo items: "Fixed Row" (canMoveRow: false, slides only on
time axis) and "No Resize" (canResize: false, moves but can't be
resized). Demonstrates the DragController constraint flags.

User prompt: "nimm auch die Ergaenzung des Gantt-Demos in tl-demo mit auf"
EOF
)"
```

---

### Task 6: Manual verification in browser

- [ ] **Step 1: Rebuild GWT client + server + demo**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.client -DskipTests=true
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.server -DskipTests=true
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.demo -DskipTests=true
```

- [ ] **Step 2: Start demo app** (use `tl-app` skill)

- [ ] **Step 3: Navigate to Gantt-Demo, test drag operations**

Test each operation:
- **Move a span (Spec)** — drag body horizontally, verify X snaps to grid; drag vertically into another row
- **Resize a span (Build)** — drag right edge, verify width changes; drag left edge
- **Move a point (Beta)** — drag horizontally, verify it moves
- **Fixed Row item** — verify it can only move horizontally, not to other rows
- **No Resize item** — verify no resize cursor appears at edges
- **ESC during drag** — verify box snaps back to original position
- **Drop outside valid area** — verify `no-drop` cursor, box snaps back

Take screenshot: `com.top_logic.demo/target/gantt-phase2-drag.png`.

- [ ] **Step 4: Document findings, fix issues**

- [ ] **Step 5: Stop demo app, commit fixes if any**

---

### Task 7: Final test run

- [ ] **Step 1: Run all Gantt tests**

```bash
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout,TestDaysSinceEpochAxisProvider
```

Expected: all PASS.

- [ ] **Step 2: Commit if any cleanup needed**

---

## Self-Review Notes

**Spec coverage:**
- DragController interface ✓ (Task 2)
- canMove/canResize with parent-tree walk ✓ (Tasks 2, 3, 4)
- getDropAreas from row geometry ✓ (Task 3)
- constrainMove with time-snap + row-snap ✓ (Task 3)
- constrainResize with edge clamping ✓ (Task 3)
- commitDrag with model mutation ✓ (Task 3)
- cancelDrag ✓ (Task 3)
- Clone-based WYSIWYG drag visualization ✓ (Task 4)
- DropArea/DragEdge as proto types ✓ (Task 1)
- Cursor feedback (no-drop / grabbing) ✓ (Task 4)
- Demo extension ✓ (Task 5)
- Manual verification ✓ (Task 6)

**Placeholder scan:** Task 4 (GWT integration) contains "adapt to real API" notes — these are flagged with specific code locations to inspect, not open TODOs. The implementer has concrete references to follow.

**Type consistency:** `DragController` method signatures match between Task 2 (interface), Task 3 (Gantt impl), and Task 4 (DragHandler calls). `constrainMove` returns `double[]` consistently. `DropArea` created via `DropArea.create()` (msgbuf factory).
