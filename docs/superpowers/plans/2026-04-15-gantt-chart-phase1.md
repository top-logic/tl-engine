# Gantt Chart — Phase 1 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** A static Gantt chart renders in `com.top_logic.demo` using a new `GanttLayout` type in the Flow library, with the complete msgbuf model that Phases 2 and 3 will later activate without rework.

**Architecture:** Add a new `GanttLayout` proto type to `com.top_logic.react.flow.common/data.proto`, implement `GanttLayoutOperations` for layout + SVG rendering (two-phase `computeIntrinsicSize`/`distributeSize`, then `draw`). Extend `FlowFactory` in `com.top_logic.react.flow.server` with TL-Script functions for building Gantt models. Introduce an `AxisProvider` SPI registered via TopLogic `PolymorphicConfiguration`. Wire a demo Gantt into `com.top_logic.demo` using the `days-since-epoch` axis provider.

**Tech Stack:** msgbuf code generation (`.proto`), Java 17, TopLogic `ReactControl` / `TLScriptFunctions` / `PolymorphicConfiguration`, SVG (`SvgWriter`), GWT-compiled client rendering.

**Spec:** `docs/superpowers/specs/2026-04-15-gantt-chart-design.md`

---

## File Structure

**New files (common module, msgbuf + operations):**
- `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto` — **modified** (add Gantt types)
- `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java` — **new**

**New files (server module, factory + registry):**
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java` — **modified** (add `gantt*` functions)
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisProvider.java` — **new**
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisProviderService.java` — **new**
- `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/providers/DaysSinceEpochAxisProvider.java` — **new**
- `com.top_logic.react.flow.server/src/main/webapp/WEB-INF/conf/tl-react-flow-server.conf.config.xml` — **modified** (register service)

**New files (tests):**
- `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java` — **new**

**New files (demo):**
- `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/components/gantt/ganttDemo.layout.xml` — **new**
- Navigation entry in the demo menu structure — **modified** (location TBD by demo navigation convention; see Task 21)

---

## Data Model Reference

All proto types listed in Tasks 1–7 live in `data.proto`. Refer back to these when implementing operations and factory functions.

Complete reference from the spec:

```
GanttLayout extends Layout          # inherits "contents: repeated Box"
  rootRows     : List<Row>
  items        : List<Item>         # Span | Milestone
  edges        : List<Edge>
  decorations  : List<Decoration>   # Line | Range
  axis         : AxisProvider
  rowHeight    : double             # default 32.0
  indentWidth  : double             # default 16.0
  axisHeight   : double             # default 24.0

Row                                  # Tree; flat rendering Phase 1
  id, label, children

Item (abstract)
  id, rowId, box, canMoveTime, canMoveRow,
  canBeEdgeSource, canBeEdgeTarget
Span extends Item: start, end, canResizeStart, canResizeEnd
Milestone extends Item: at

Edge
  id, sourceItemId, sourceEndpoint, targetItemId, targetEndpoint, enforce
  Endpoint enum { START, END }
  Enforce enum { STRICT, WARN, NONE }

Decoration (abstract)
  id, color, label, canMove, relevantFor (List<String>, null=all)
Line extends Decoration: at
Range extends Decoration: from, to, canResize

AxisProvider
  providerId, rangeMin, rangeMax,
  currentZoom, snapGranularity,
  currentTicks : List<Tick>
Tick
  position, label, emphasis
```

---

### Task 1: Add `Row` and `Tick` proto messages

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto` (append at end)

- [ ] **Step 1: Add messages to `data.proto`**

Open `data.proto`, go to end of file (line ~900). Append:

```proto
/** A row in a {@link GanttLayout}. Rows form a forest (tree of trees). */
message GanttRow {
	/** Opaque identifier, unique within a {@link GanttLayout}. */
	string id;

	/** Human-readable label shown at the row header. */
	string label;

	/** Child rows forming a hierarchy. Phase 1 renders the tree flat with indentation. */
	repeated GanttRow children;
}

/** A tick on the Gantt time axis. */
message GanttTick {
	/** Position in layout units (pixels at zoom 100%). */
	double position;

	/** Label drawn at the tick. */
	string label;

	/** Emphasis in [0..1] — renderer interpolates stroke width and label prominence. */
	double emphasis;
}
```

- [ ] **Step 2: Build the common module so msgbuf regenerates**

Run:
```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: `BUILD SUCCESS`, and the generated classes `GanttRow_Impl.java`, `GanttTick_Impl.java` appear under the module's generated-sources.

- [ ] **Step 3: Verify the generated classes exist**

Run:
```bash
find com.top_logic.react.flow.common -name "GanttRow*.java" -o -name "GanttTick*.java" 2>/dev/null
```

Expected: Paths containing `GanttRow_Impl.java`, `GanttTick_Impl.java`, `GanttRow.java`, `GanttTick.java`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto
git commit -m "Ticket #29108: Add GanttRow and GanttTick proto messages."
```

---

### Task 2: Add endpoint/enforce enums and `GanttEdge` proto message

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto` (append)

- [ ] **Step 1: Add enums and message**

Append:

```proto
/** Which end of an item a Gantt edge attaches to. */
enum GanttEndpoint {
	START;
	END;
}

/** Semantic of a Gantt edge. */
enum GanttEnforce {
	/** Operation is blocked when the constraint would be violated. */
	STRICT;
	/** Operation is allowed; violation is marked visually (red edge). */
	WARN;
	/** Purely decorative relation; no time constraint. */
	NONE;
}

/** A dependency edge between two Gantt items. */
message GanttEdge {
	string id;

	/** ID of the source item. */
	string sourceItemId;

	GanttEndpoint sourceEndpoint;

	/** ID of the target item. */
	string targetItemId;

	GanttEndpoint targetEndpoint;

	GanttEnforce enforce;
}
```

- [ ] **Step 2: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: `BUILD SUCCESS`, `GanttEdge_Impl.java`, `GanttEndpoint.java`, `GanttEnforce.java` generated.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto
git commit -m "Ticket #29108: Add GanttEdge with endpoint and enforce enums."
```

---

### Task 3: Add `GanttItem` hierarchy (Span, Milestone)

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Add messages**

Append:

```proto
/** Abstract base for entries on a Gantt time axis. */
abstract message GanttItem {
	string id;

	/** Reference to the {@link GanttRow#id} this item lives in. */
	string rowId;

	/** Rendering content; positioned by the layout, drawn via its own operations. */
	Box box;

	bool canMoveTime = true;
	bool canMoveRow = true;
	bool canBeEdgeSource = true;
	bool canBeEdgeTarget = true;
}

/** A task with start and end on the time axis. */
message GanttSpan extends GanttItem {
	double start;
	double end;
	bool canResizeStart = true;
	bool canResizeEnd = true;
}

/** A milestone (point in time). */
message GanttMilestone extends GanttItem {
	double at;
}
```

- [ ] **Step 2: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: `BUILD SUCCESS`, generated `GanttItem.java`, `GanttSpan_Impl.java`, `GanttMilestone_Impl.java`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto
git commit -m "Ticket #29108: Add GanttItem hierarchy (Span, Milestone)."
```

---

### Task 4: Add `GanttDecoration` hierarchy (Line, Range)

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Add messages**

Append:

```proto
/** Abstract base for Gantt chart decorations (lines, ranges). */
abstract message GanttDecoration {
	string id;

	/** CSS-style color. */
	string color;

	/** Label shown near the decoration. */
	string label;

	bool canMove;

	/**
	 * Optional restriction: which rows this decoration applies to.
	 * Empty list means: all rows.
	 */
	repeated string relevantFor;
}

/** Vertical line at a single position on the time axis. */
message GanttLineDecoration extends GanttDecoration {
	double at;
}

/** Coloured range between two positions. */
message GanttRangeDecoration extends GanttDecoration {
	double from;
	double to;
	bool canResize;
}
```

- [ ] **Step 2: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: `BUILD SUCCESS`, generated `GanttDecoration.java`, `GanttLineDecoration_Impl.java`, `GanttRangeDecoration_Impl.java`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto
git commit -m "Ticket #29108: Add GanttDecoration hierarchy (Line, Range)."
```

---

### Task 5: Add `GanttAxis` proto message

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Add message**

Append:

```proto
/** Time axis configuration for a {@link GanttLayout}. */
message GanttAxis {
	/** Name of the server-side axis provider that computes ticks and snap data. */
	string providerId;

	/** Minimum representable position (zoom 100%). */
	double rangeMin;

	/** Maximum representable position (zoom 100%). */
	double rangeMax;

	/** Current zoom (pixels per position unit). Written by the client on zoom changes. */
	double currentZoom = 1.0;

	/** Granularity for client-side drag snap, in position units. */
	double snapGranularity = 1.0;

	/** Ticks computed for the current zoom. */
	repeated GanttTick currentTicks;
}
```

- [ ] **Step 2: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto
git commit -m "Ticket #29108: Add GanttAxis proto message."
```

---

### Task 6: Add `GanttLayout` top-level proto message

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Add message**

Append:

```proto
/**
 * A layout placing {@link GanttItem} boxes along a time axis, grouped into rows.
 *
 * Inherits {@code contents} from {@link Layout} — the layout populates it with the
 * {@link GanttItem#box} elements so that standard layout/render dispatch reaches them.
 */
@Operations("com.top_logic.react.flow.operations.layout.GanttLayoutOperations")
message GanttLayout extends Layout {
	/** Root rows of the forest. */
	repeated GanttRow rootRows;

	/** All items in the chart (referenced by row via {@link GanttItem#rowId}). */
	repeated GanttItem items;

	/** Dependency edges between items. */
	repeated GanttEdge edges;

	/** Decorations overlaid on the chart (lines, ranges). */
	repeated GanttDecoration decorations;

	/** Time axis configuration. */
	GanttAxis axis;

	/** Height allocated per row, in pixels at zoom 100%. */
	double rowHeight = 32.0;

	/** Horizontal indentation per row hierarchy level, in pixels. */
	double indentWidth = 16.0;

	/** Height of the axis header, in pixels. */
	double axisHeight = 24.0;

	/** Width of the row label column, in pixels. */
	double rowLabelWidth = 200.0;
}
```

- [ ] **Step 2: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: `BUILD SUCCESS`, `GanttLayout_Impl.java` generated. **The build will fail compiling if `GanttLayoutOperations` doesn't exist yet**; in that case, proceed to Task 7 first, then return here.

Alternative ordering: skip Step 2 until Task 7 is complete.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto
git commit -m "Ticket #29108: Add GanttLayout top-level proto message."
```

---

### Task 7: Create `GanttLayoutOperations` interface skeleton

**Files:**
- Create: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Create file with skeleton**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.operations.BoxOperations;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Layout and rendering operations for {@link GanttLayout}.
 *
 * <p>
 * Places {@link com.top_logic.react.flow.data.GanttItem} boxes along the X axis according to
 * their {@code start}/{@code end} (or {@code at}) positions, and along the Y axis according
 * to the item's row. Renders axis ticks from {@code axis.currentTicks}, draws row-lane
 * backgrounds, edges (orthogonal), and decorations.
 * </p>
 */
public interface GanttLayoutOperations extends BoxOperations {

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		// Implemented in Task 9.
		GanttLayout self = (GanttLayout) this;
		self.setX(offsetX);
		self.setY(offsetY);
		self.setWidth(self.getRowLabelWidth() + (self.getAxis().getRangeMax() - self.getAxis().getRangeMin()) * self.getAxis().getCurrentZoom());
		self.setHeight(self.getAxisHeight() + countRows(self) * self.getRowHeight());
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		// Implemented in Task 10.
		computeIntrinsicSize(context, offsetX, offsetY);
	}

	@Override
	default void draw(SvgWriter out) {
		// Implemented in Tasks 11–15.
	}

	/** Total number of rows counting the full tree. */
	static int countRows(GanttLayout layout) {
		int total = 0;
		for (com.top_logic.react.flow.data.GanttRow root : layout.getRootRows()) {
			total += 1 + countDescendants(root);
		}
		return total;
	}

	private static int countDescendants(com.top_logic.react.flow.data.GanttRow row) {
		int total = 0;
		for (com.top_logic.react.flow.data.GanttRow child : row.getChildren()) {
			total += 1 + countDescendants(child);
		}
		return total;
	}
}
```

- [ ] **Step 2: Build the common module**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: `BUILD SUCCESS`. The generated `GanttLayout_Impl` implements `GanttLayoutOperations` via the `@Operations` binding.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java
git commit -m "Ticket #29108: Create GanttLayoutOperations skeleton."
```

---

### Task 8: Write the first layout test (failing)

**Files:**
- Create: `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java`

- [ ] **Step 1: Create test class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.layout;

import java.util.Arrays;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.Border;
import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Diagram;
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.data.Padding;
import com.top_logic.react.flow.data.Text;
import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;

/** Tests for {@link com.top_logic.react.flow.operations.layout.GanttLayoutOperations}. */
public class TestGanttLayout extends TestCase {

	public void testLayoutHeightScalesWithRows() {
		GanttLayout layout = GanttLayout.create()
			.setRowHeight(32.0)
			.setAxisHeight(24.0)
			.setRowLabelWidth(200.0)
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(
				row("r1", "Row 1"),
				row("r2", "Row 2"),
				row("r3", "Row 3")));

		Diagram d = Diagram.create().setRoot(layout);

		RenderContext context = new AWTContext(12f);
		d.layout(context);

		// 3 rows * 32 px + 24 axis + 200 label column is irrelevant for height
		assertEquals("height", 24.0 + 3 * 32.0, layout.getHeight());
	}

	private static GanttRow row(String id, String label) {
		return GanttRow.create().setId(id).setLabel(label);
	}

	private static GanttAxis axis(double min, double max) {
		return GanttAxis.create()
			.setProviderId("test")
			.setRangeMin(min)
			.setRangeMax(max)
			.setCurrentZoom(1.0)
			.setCurrentTicks(Arrays.asList(
				tick(0, "0", 1.0),
				tick(50, "50", 0.5),
				tick(100, "100", 1.0)));
	}

	private static GanttTick tick(double pos, String label, double emphasis) {
		return GanttTick.create().setPosition(pos).setLabel(label).setEmphasis(emphasis);
	}

	private static Box cell(String label) {
		return Border.create().setContent(Padding.create().setAll(2.0).setContent(Text.create().setValue(label)));
	}

	@SuppressWarnings("unused")
	private static GanttSpan span(String id, String rowId, double start, double end, String label) {
		return GanttSpan.create()
			.setId(id).setRowId(rowId)
			.setStart(start).setEnd(end)
			.setBox(cell(label));
	}
}
```

- [ ] **Step 2: Run the test — expect PASS** (the skeleton already sets width/height in `computeIntrinsicSize`)

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: `testLayoutHeightScalesWithRows` PASSES (because Task 7's skeleton happens to compute this). This confirms test infrastructure works.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java
git commit -m "Ticket #29108: Add TestGanttLayout with height-per-row check."
```

---

### Task 9: Implement `computeIntrinsicSize` — rows and items positioning

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Extend `TestGanttLayout` with a span-position assertion (failing)**

Add this test method to `TestGanttLayout`:

```java
	public void testSpanPositionedAtTimeAndRow() {
		GanttSpan span = span("s1", "r2", 10.0, 30.0, "Task 1");

		GanttLayout layout = GanttLayout.create()
			.setRowHeight(32.0)
			.setAxisHeight(24.0)
			.setRowLabelWidth(200.0)
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(
				row("r1", "Row 1"),
				row("r2", "Row 2")))
			.setItems(Arrays.asList(span));
		layout.addContent(span.getBox());

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		// r2 is the second row (index 1). Expected y = axisHeight + 1 * rowHeight = 56.
		// Expected x = rowLabelWidth + 10 * zoom = 210.
		assertEquals("span.x", 210.0, span.getBox().getX(), 0.5);
		assertEquals("span.y", 56.0, span.getBox().getY(), 0.5);
		// Width = (end - start) * zoom = 20. Height = rowHeight - 4 (small inset).
		assertEquals("span.width", 20.0, span.getBox().getWidth(), 0.5);
	}
```

- [ ] **Step 2: Run — expect FAIL**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout#testSpanPositionedAtTimeAndRow
```

Expected: FAIL (box stays at x=y=0 because `computeIntrinsicSize` doesn't position items yet).

- [ ] **Step 3: Implement row-index table + item positioning**

Replace the body of `GanttLayoutOperations.computeIntrinsicSize` and add a helper:

```java
	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY) {
		GanttLayout self = (GanttLayout) this;
		GanttAxis axis = self.getAxis();

		// Index rows in depth-first order and remember depth.
		java.util.Map<String, Integer> rowIndex = new java.util.HashMap<>();
		java.util.Map<String, Integer> rowDepth = new java.util.HashMap<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRows(root, 0, rowIndex, rowDepth, counter);
		}

		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double labelWidth = self.getRowLabelWidth();
		double rowHeight = self.getRowHeight();
		double axisHeight = self.getAxisHeight();

		for (GanttItem item : self.getItems()) {
			Integer idx = rowIndex.get(item.getRowId());
			if (idx == null) {
				continue;
			}
			double y = offsetY + axisHeight + idx * rowHeight + 2;
			Box box = item.getBox();
			if (item instanceof GanttSpan span) {
				double x = offsetX + labelWidth + (span.getStart() - rangeMin) * zoom;
				double w = (span.getEnd() - span.getStart()) * zoom;
				box.setX(x);
				box.setY(y);
				box.setWidth(w);
				box.setHeight(rowHeight - 4);
				box.computeIntrinsicSize(context, x, y);
				box.setX(x);
				box.setY(y);
				box.setWidth(w);
				box.setHeight(rowHeight - 4);
			} else if (item instanceof GanttMilestone ms) {
				double cx = offsetX + labelWidth + (ms.getAt() - rangeMin) * zoom;
				double r = (rowHeight - 4) / 2.0;
				box.computeIntrinsicSize(context, cx - r, y);
				box.setX(cx - r);
				box.setY(y);
				box.setWidth(2 * r);
				box.setHeight(2 * r);
			}
		}

		self.setX(offsetX);
		self.setY(offsetY);
		self.setWidth(labelWidth + (axis.getRangeMax() - rangeMin) * zoom);
		self.setHeight(axisHeight + counter[0] * rowHeight);
	}

	private static void indexRows(GanttRow row, int depth,
			java.util.Map<String, Integer> idx, java.util.Map<String, Integer> depths,
			int[] counter) {
		idx.put(row.getId(), counter[0]);
		depths.put(row.getId(), depth);
		counter[0]++;
		for (GanttRow child : row.getChildren()) {
			indexRows(child, depth + 1, idx, depths, counter);
		}
	}
```

Add imports at top of file:
```java
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttMilestone;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
```

Remove the old `countRows` / `countDescendants` helpers — replaced by the counter in `indexRows`.

- [ ] **Step 4: Rebuild common, then run test**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true \
  && mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: both tests PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java \
        com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java
git commit -m "Ticket #29108: Position Gantt items by time (x) and row index (y)."
```

---

### Task 10: `distributeSize` — let the layout span the available width

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Write failing test**

Add to `TestGanttLayout`:

```java
	public void testDistributeSizeUsesGivenWidth() {
		GanttLayout layout = GanttLayout.create()
			.setRowHeight(32.0)
			.setAxisHeight(24.0)
			.setRowLabelWidth(200.0)
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1")));

		Diagram d = Diagram.create().setRoot(layout);
		d.setViewBoxWidth(800);
		d.setViewBoxHeight(100);
		d.layout(new AWTContext(12f));

		// distributeSize should not shrink below intrinsic width.
		// (Intrinsic = 200 + 100*1 = 300; demo ViewBox gives 800.)
		assertEquals("width", 800.0, layout.getWidth(), 0.5);
	}
```

- [ ] **Step 2: Run — expect FAIL** (layout still at 300 because distributeSize currently falls through to computeIntrinsicSize)

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout#testDistributeSizeUsesGivenWidth
```

Expected: FAIL.

- [ ] **Step 3: Implement**

Replace `distributeSize` body:

```java
	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		GanttLayout self = (GanttLayout) this;
		computeIntrinsicSize(context, offsetX, offsetY);
		if (width > self.getWidth()) {
			self.setWidth(width);
		}
		if (height > self.getHeight()) {
			self.setHeight(height);
		}
	}
```

- [ ] **Step 4: Rebuild and run**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true \
  && mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: all tests PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java \
        com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java
git commit -m "Ticket #29108: Let GanttLayout fill available width and height."
```

---

### Task 11: `draw` — axis ticks from `currentTicks`

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Implement `draw` — axis header rendering**

Replace the empty `draw` with:

```java
	@Override
	default void draw(SvgWriter out) {
		GanttLayout self = (GanttLayout) this;
		drawAxis(self, out);
		drawRowLanes(self, out);
		drawDecorations(self, out);
		for (Box content : self.getContents()) {
			out.write(content);
		}
		drawEdges(self, out);
	}

	private static void drawAxis(GanttLayout self, SvgWriter out) {
		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double x0 = self.getX() + self.getRowLabelWidth();
		double y0 = self.getY();
		double h = self.getAxisHeight();

		out.beginGroup();
		out.writeAttribute("class", "tl-gantt-axis");
		out.rect(x0, y0, self.getWidth() - self.getRowLabelWidth(), h, "#f8f8f8", "#c0c0c0", 1.0);
		for (GanttTick tick : axis.getCurrentTicks()) {
			double tx = x0 + (tick.getPosition() - rangeMin) * zoom;
			double strokeWidth = 0.5 + 1.5 * Math.max(0.0, Math.min(1.0, tick.getEmphasis()));
			out.line(tx, y0, tx, y0 + h, "#707070", strokeWidth);
			out.text(tx + 2, y0 + h - 4, tick.getLabel(), "10px", "#303030");
		}
		out.endGroup();
	}

	private static void drawRowLanes(GanttLayout self, SvgWriter out) {
		// Implemented in Task 12.
	}

	private static void drawDecorations(GanttLayout self, SvgWriter out) {
		// Implemented in Task 14.
	}

	private static void drawEdges(GanttLayout self, SvgWriter out) {
		// Implemented in Task 13.
	}
```

**Note:** The `SvgWriter` helper methods `rect`, `line`, `text`, `beginGroup`, `endGroup`, `writeAttribute` reflect the existing API. If the exact method signatures differ, adjust by looking at how other `*LayoutOperations` produce SVG (e.g., `TreeLayoutOperations`). This task may require inspecting `SvgWriter.java` first; do so before writing code.

Before writing, run:

```bash
head -200 com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/svg/SvgWriter.java
```

and adapt the calls to the real API.

- [ ] **Step 2: Rebuild common**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Add test that calls draw and asserts SVG contains expected markers**

In `TestGanttLayout`, add a helper and test:

```java
	public void testAxisRendersAllTicks() {
		GanttLayout layout = GanttLayout.create()
			.setAxis(axis(0, 100))
			.setRootRows(Arrays.asList(row("r1", "Row 1")));

		Diagram d = Diagram.create().setRoot(layout);
		d.layout(new AWTContext(12f));

		String svg = renderToSvg(d);
		assertTrue("tick 0 label", svg.contains(">0<"));
		assertTrue("tick 50 label", svg.contains(">50<"));
		assertTrue("tick 100 label", svg.contains(">100<"));
	}

	private static String renderToSvg(Diagram d) {
		java.io.StringWriter out = new java.io.StringWriter();
		try (com.top_logic.react.flow.svg.SvgWriter w = new com.top_logic.react.flow.svg.SvgWriter(out)) {
			w.beginDiagram(d);
			w.write(d.getRoot());
			w.endDiagram();
		}
		return out.toString();
	}
```

**Note on `renderToSvg`:** The exact `SvgWriter` construction may differ — inspect `TestGraphLayout.java` for the pattern used in the codebase and adapt if necessary. If the existing tests don't write SVG, skip this test method and instead assert layout-level side-effects (e.g., that `axis.getCurrentTicks().size()` matches, or inspect an internal state field).

- [ ] **Step 4: Run tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: tests PASS.

- [ ] **Step 5: Commit**

```bash
git add -u
git commit -m "Ticket #29108: Render Gantt time axis from currentTicks."
```

---

### Task 12: `draw` — row lanes with indentation and labels

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Implement `drawRowLanes`**

Replace the empty `drawRowLanes`:

```java
	private static void drawRowLanes(GanttLayout self, SvgWriter out) {
		java.util.Map<String, Integer> rowIndex = new java.util.LinkedHashMap<>();
		java.util.Map<String, Integer> rowDepth = new java.util.HashMap<>();
		java.util.Map<String, String> rowLabel = new java.util.HashMap<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			indexRowsWithLabels(root, 0, rowIndex, rowDepth, rowLabel, counter);
		}

		double x0 = self.getX();
		double yTop = self.getY() + self.getAxisHeight();
		double labelWidth = self.getRowLabelWidth();
		double totalWidth = self.getWidth();
		double rowHeight = self.getRowHeight();
		double indent = self.getIndentWidth();

		out.beginGroup();
		out.writeAttribute("class", "tl-gantt-lanes");
		int idx = 0;
		for (java.util.Map.Entry<String, Integer> e : rowIndex.entrySet()) {
			double y = yTop + idx * rowHeight;
			String fill = (idx % 2 == 0) ? "#ffffff" : "#f5f5f5";
			out.rect(x0, y, totalWidth, rowHeight, fill, "#e0e0e0", 0.5);
			int depth = rowDepth.getOrDefault(e.getKey(), 0);
			double labelX = x0 + 4 + depth * indent;
			out.text(labelX, y + rowHeight / 2.0 + 4, rowLabel.get(e.getKey()), "12px", "#202020");
			out.line(x0 + labelWidth, y, x0 + labelWidth, y + rowHeight, "#a0a0a0", 1.0);
			idx++;
		}
		out.endGroup();
	}

	private static void indexRowsWithLabels(GanttRow row, int depth,
			java.util.Map<String, Integer> idx, java.util.Map<String, Integer> depths,
			java.util.Map<String, String> labels, int[] counter) {
		idx.put(row.getId(), counter[0]);
		depths.put(row.getId(), depth);
		labels.put(row.getId(), row.getLabel());
		counter[0]++;
		for (GanttRow child : row.getChildren()) {
			indexRowsWithLabels(child, depth + 1, idx, depths, labels, counter);
		}
	}
```

Add field to `GanttLayout` proto if `indentWidth` is missing (it was added in Task 6, confirm).

- [ ] **Step 2: Rebuild and run tests**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true \
  && mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: all existing tests still PASS.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java
git commit -m "Ticket #29108: Render Gantt row lanes with hierarchy indentation."
```

---

### Task 13: `draw` — static orthogonal edges between items

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Implement `drawEdges`**

```java
	private static void drawEdges(GanttLayout self, SvgWriter out) {
		java.util.Map<String, GanttItem> byId = new java.util.HashMap<>();
		for (GanttItem it : self.getItems()) {
			byId.put(it.getId(), it);
		}
		out.beginGroup();
		out.writeAttribute("class", "tl-gantt-edges");
		for (GanttEdge edge : self.getEdges()) {
			GanttItem src = byId.get(edge.getSourceItemId());
			GanttItem tgt = byId.get(edge.getTargetItemId());
			if (src == null || tgt == null) {
				continue;
			}
			double sx = endpointX(src, edge.getSourceEndpoint());
			double sy = centerY(src);
			double tx = endpointX(tgt, edge.getTargetEndpoint());
			double ty = centerY(tgt);

			// Simple orthogonal routing: out horizontally, vertical to target row, horizontal to target.
			double midY = (sy + ty) / 2.0;
			String stroke = edge.getEnforce() == GanttEnforce.WARN ? "#d01030" : "#606060";
			double strokeWidth = edge.getEnforce() == GanttEnforce.NONE ? 0.8 : 1.2;
			String[] dash = edge.getEnforce() == GanttEnforce.NONE ? new String[] { "4", "3" } : null;

			out.polyline(new double[] { sx, sx + 6, sx + 6, tx - 6, tx - 6, tx },
			             new double[] { sy, sy,     midY,  midY,   ty,    ty },
			             stroke, strokeWidth, dash);
		}
		out.endGroup();
	}

	private static double endpointX(GanttItem it, GanttEndpoint endpoint) {
		Box b = it.getBox();
		return endpoint == GanttEndpoint.START ? b.getX() : b.getX() + b.getWidth();
	}

	private static double centerY(GanttItem it) {
		Box b = it.getBox();
		return b.getY() + b.getHeight() / 2.0;
	}
```

Imports to add:
```java
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
```

**Note:** The exact `SvgWriter.polyline` signature may differ. If it takes a single `double[]` of alternating x/y or a `PolygonalChain` object, adapt. Confirm by reading `SvgWriter.java` and, if needed, `PolygonalChainOperations.java`.

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java
git commit -m "Ticket #29108: Render Gantt edges with orthogonal routing."
```

---

### Task 14: `draw` — line and range decorations

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Implement `drawDecorations`**

```java
	private static void drawDecorations(GanttLayout self, SvgWriter out) {
		GanttAxis axis = self.getAxis();
		double zoom = axis.getCurrentZoom();
		double rangeMin = axis.getRangeMin();
		double x0 = self.getX() + self.getRowLabelWidth();
		double yTop = self.getY() + self.getAxisHeight();
		double rowHeight = self.getRowHeight();

		java.util.Map<String, Integer> rowIndex = new java.util.LinkedHashMap<>();
		int[] counter = new int[] { 0 };
		for (GanttRow root : self.getRootRows()) {
			collectIndex(root, rowIndex, counter);
		}

		out.beginGroup();
		out.writeAttribute("class", "tl-gantt-decorations");
		for (GanttDecoration deco : self.getDecorations()) {
			double[] yRange = computeYRange(deco, rowIndex, yTop, rowHeight, counter[0]);
			if (deco instanceof GanttLineDecoration line) {
				double x = x0 + (line.getAt() - rangeMin) * zoom;
				out.line(x, yRange[0], x, yRange[1], deco.getColor(), 1.5);
				if (deco.getLabel() != null && !deco.getLabel().isEmpty()) {
					out.text(x + 2, yRange[0] + 10, deco.getLabel(), "10px", deco.getColor());
				}
			} else if (deco instanceof GanttRangeDecoration range) {
				double xFrom = x0 + (range.getFrom() - rangeMin) * zoom;
				double xTo = x0 + (range.getTo() - rangeMin) * zoom;
				out.rect(xFrom, yRange[0], xTo - xFrom, yRange[1] - yRange[0], deco.getColor(), null, 0.0);
				if (deco.getLabel() != null && !deco.getLabel().isEmpty()) {
					out.text(xFrom + 2, yRange[0] + 10, deco.getLabel(), "10px", "#303030");
				}
			}
		}
		out.endGroup();
	}

	private static double[] computeYRange(GanttDecoration deco,
			java.util.Map<String, Integer> rowIndex, double yTop, double rowHeight, int totalRows) {
		if (deco.getRelevantFor() == null || deco.getRelevantFor().isEmpty()) {
			return new double[] { yTop, yTop + totalRows * rowHeight };
		}
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		for (String rowId : deco.getRelevantFor()) {
			Integer idx = rowIndex.get(rowId);
			if (idx == null) continue;
			if (idx < min) min = idx;
			if (idx > max) max = idx;
		}
		if (min == Integer.MAX_VALUE) {
			return new double[] { yTop, yTop + totalRows * rowHeight };
		}
		return new double[] { yTop + min * rowHeight, yTop + (max + 1) * rowHeight };
	}

	private static void collectIndex(GanttRow row, java.util.Map<String, Integer> idx, int[] counter) {
		idx.put(row.getId(), counter[0]);
		counter[0]++;
		for (GanttRow child : row.getChildren()) {
			collectIndex(child, idx, counter);
		}
	}
```

Imports:
```java
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttRangeDecoration;
```

- [ ] **Step 2: Rebuild and run tests**

```bash
mvn -B install -pl com.top_logic.react.flow.common -DskipTests=true \
  && mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout
```

Expected: tests PASS.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java
git commit -m "Ticket #29108: Render Gantt line and range decorations."
```

---

### Task 15: `AxisProvider` SPI and registry service

**Files:**
- Create: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisProvider.java`
- Create: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisProviderService.java`

- [ ] **Step 1: Create `AxisProvider` interface**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

import java.util.List;

import com.top_logic.react.flow.data.GanttTick;

/**
 * Server-side axis implementation for a {@link com.top_logic.react.flow.data.GanttLayout}.
 *
 * <p>
 * A provider encapsulates the mapping between application-specific {@code TimeValue}s and
 * layout positions and computes axis ticks for a given visible range and zoom level.
 * Applications provide their own implementations via the {@link AxisProviderService} registry
 * and reference them from {@link com.top_logic.react.flow.data.GanttAxis#getProviderId()}.
 * </p>
 */
public interface AxisProvider {

	/** Identifier used in {@code GanttAxis.providerId}. */
	String getId();

	/**
	 * Compute axis ticks covering the given position range at the given zoom level.
	 *
	 * @param rangeMin
	 *        minimum position (layout units at zoom 100%)
	 * @param rangeMax
	 *        maximum position
	 * @param pixelsPerUnit
	 *        current zoom factor (1.0 = 100%)
	 */
	List<GanttTick> ticksFor(double rangeMin, double rangeMax, double pixelsPerUnit);

	/**
	 * Snap granularity (layout units) for the given zoom level. Client uses this during drag
	 * to round positions to the nearest multiple.
	 */
	default double snapGranularity(double pixelsPerUnit) {
		return 1.0;
	}
}
```

- [ ] **Step 2: Create `AxisProviderService`**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * TopLogic module hosting the registered {@link AxisProvider}s.
 */
@ServiceDependencies({})
public class AxisProviderService extends ManagedClass {

	/** Module configuration for {@link AxisProviderService}. */
	public interface Config extends ServiceConfiguration<AxisProviderService> {
		@Name("providers")
		List<PolymorphicConfiguration<AxisProvider>> getProviders();
	}

	private final Map<String, AxisProvider> _providers = new HashMap<>();

	/** Bound by module framework. */
	public AxisProviderService(InstantiationContext context, Config config) {
		super(context, config);
		for (PolymorphicConfiguration<AxisProvider> entry : config.getProviders()) {
			AxisProvider provider = context.getInstance(entry);
			if (provider != null) {
				_providers.put(provider.getId(), provider);
			}
		}
	}

	/** Look up a provider by id, or {@code null} if unknown. */
	public AxisProvider lookup(String id) {
		return _providers.get(id);
	}

	/** Module registration. */
	public static final class Module extends TypedRuntimeModule<AxisProviderService> {
		/** Singleton. */
		public static final Module INSTANCE = new Module();

		@Override
		public Class<AxisProviderService> getImplementation() {
			return AxisProviderService.class;
		}
	}
}
```

**Note:** If `ServiceConfiguration` import is missing, add `import com.top_logic.basic.module.ServiceConfiguration;` — or match the pattern used by other services in the flow server module (inspect `com.top_logic.react.flow.server` for examples, e.g., any existing `*Service.java`).

- [ ] **Step 3: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.server -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/
git commit -m "Ticket #29108: Add AxisProvider SPI and registry service."
```

---

### Task 16: Implement a "days since epoch" `AxisProvider`

**Files:**
- Create: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/providers/DaysSinceEpochAxisProvider.java`

- [ ] **Step 1: Create file**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis.providers;

import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.server.axis.AxisProvider;

/**
 * {@link AxisProvider} that interprets positions as days since 1970-01-01 and emits month and
 * year ticks. Used by the Gantt demo; applications will usually register their own provider.
 */
public class DaysSinceEpochAxisProvider implements AxisProvider {

	/** Configured id (defaults to {@code "days-since-epoch"}). */
	private final String _id;

	/** Bound by the module framework. */
	public DaysSinceEpochAxisProvider() {
		this("days-since-epoch");
	}

	/** For tests. */
	public DaysSinceEpochAxisProvider(String id) {
		_id = id;
	}

	@Override
	public String getId() {
		return _id;
	}

	@Override
	public List<GanttTick> ticksFor(double rangeMin, double rangeMax, double pixelsPerUnit) {
		List<GanttTick> ticks = new ArrayList<>();
		LocalDate min = LocalDate.ofEpochDay((long) Math.floor(rangeMin));
		LocalDate max = LocalDate.ofEpochDay((long) Math.ceil(rangeMax));

		LocalDate firstOfMonth = min.withDayOfMonth(1);
		if (firstOfMonth.isBefore(min)) {
			firstOfMonth = firstOfMonth.plusMonths(1);
		}
		for (LocalDate d = firstOfMonth; !d.isAfter(max); d = d.plusMonths(1)) {
			double emphasis = (d.getMonth() == Month.JANUARY) ? 1.0 : 0.35;
			String label = (d.getMonth() == Month.JANUARY)
				? String.valueOf(d.getYear())
				: d.getMonth().name().substring(0, 3);
			ticks.add(GanttTick.create()
				.setPosition(d.toEpochDay())
				.setLabel(label)
				.setEmphasis(emphasis));
		}
		return ticks;
	}

	@Override
	public double snapGranularity(double pixelsPerUnit) {
		// 1 day always; could refine by zoom.
		return 1.0;
	}
}
```

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.server -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Add unit test**

Create `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/axis/TestDaysSinceEpochAxisProvider.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.axis;

import java.time.LocalDate;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.server.axis.providers.DaysSinceEpochAxisProvider;

public class TestDaysSinceEpochAxisProvider extends TestCase {

	public void testMonthTicksInJanuaryCarryYearLabel() {
		double from = LocalDate.of(2026, 1, 1).toEpochDay();
		double to = LocalDate.of(2026, 12, 31).toEpochDay();
		List<GanttTick> ticks = new DaysSinceEpochAxisProvider().ticksFor(from, to, 1.0);

		assertEquals("one tick per month in the year", 12, ticks.size());
		assertEquals("year label in January", "2026", ticks.get(0).getLabel());
		assertEquals("month abbreviation", "FEB", ticks.get(1).getLabel());
	}
}
```

- [ ] **Step 4: Run the new test**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestDaysSinceEpochAxisProvider
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/providers/ \
        com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/axis/
git commit -m "Ticket #29108: Add DaysSinceEpochAxisProvider and unit test."
```

---

### Task 17: Register `AxisProviderService` in module config

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/webapp/WEB-INF/conf/tl-react-flow-server.conf.config.xml`

- [ ] **Step 1: Inspect existing file format**

```bash
cat com.top_logic.react.flow.server/src/main/webapp/WEB-INF/conf/tl-react-flow-server.conf.config.xml
```

If the file is short and a template-style services config, append an entry; if it's a pure import list, register the service via a new section following the existing services pattern (the codebase convention is project-wide; match the nearest similar service module's config).

- [ ] **Step 2: Add the module registration**

Example (adapt to actual file structure):

```xml
<config service="com.top_logic.react.flow.server.axis.AxisProviderService$Module">
    <instance class="com.top_logic.react.flow.server.axis.AxisProviderService">
        <providers>
            <provider class="com.top_logic.react.flow.server.axis.providers.DaysSinceEpochAxisProvider"/>
        </providers>
    </instance>
</config>
```

- [ ] **Step 3: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.server -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.react.flow.server/src/main/webapp/WEB-INF/conf/tl-react-flow-server.conf.config.xml
git commit -m "Ticket #29108: Register AxisProviderService and default provider."
```

---

### Task 18: Add Gantt factory functions to `FlowFactory`

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java`

- [ ] **Step 1: Add factory methods**

Append within the `FlowFactory` class (after existing factory methods; match the style of existing methods — `@SideEffectFree`, `@Label`, `@Mandatory` on required parameters):

```java
	@SideEffectFree
	@Label("Create Gantt row")
	public static GanttRow ganttRow(
			@Mandatory String id,
			@Mandatory String label,
			List<GanttRow> children) {
		GanttRow row = GanttRow.create().setId(id).setLabel(label);
		if (children != null) {
			row.setChildren(children);
		}
		return row;
	}

	@SideEffectFree
	@Label("Create Gantt span item")
	public static GanttSpan ganttSpan(
			@Mandatory String id,
			@Mandatory String rowId,
			@Mandatory Box box,
			double start,
			double end) {
		return GanttSpan.create()
			.setId(id).setRowId(rowId).setBox(box)
			.setStart(start).setEnd(end);
	}

	@SideEffectFree
	@Label("Create Gantt milestone item")
	public static GanttMilestone ganttMilestone(
			@Mandatory String id,
			@Mandatory String rowId,
			@Mandatory Box box,
			double at) {
		return GanttMilestone.create()
			.setId(id).setRowId(rowId).setBox(box)
			.setAt(at);
	}

	@SideEffectFree
	@Label("Create Gantt edge")
	public static GanttEdge ganttEdge(
			@Mandatory String id,
			@Mandatory String sourceItemId,
			@Mandatory GanttEndpoint sourceEndpoint,
			@Mandatory String targetItemId,
			@Mandatory GanttEndpoint targetEndpoint,
			GanttEnforce enforce) {
		return GanttEdge.create()
			.setId(id)
			.setSourceItemId(sourceItemId).setSourceEndpoint(sourceEndpoint)
			.setTargetItemId(targetItemId).setTargetEndpoint(targetEndpoint)
			.setEnforce(enforce != null ? enforce : GanttEnforce.STRICT);
	}

	@SideEffectFree
	@Label("Create Gantt line decoration")
	public static GanttLineDecoration ganttLineDeco(
			@Mandatory String id,
			double at,
			String color,
			String label,
			List<String> relevantFor) {
		GanttLineDecoration deco = GanttLineDecoration.create()
			.setId(id).setAt(at)
			.setColor(color != null ? color : "#c02020")
			.setLabel(label != null ? label : "");
		if (relevantFor != null) {
			deco.setRelevantFor(relevantFor);
		}
		return deco;
	}

	@SideEffectFree
	@Label("Create Gantt range decoration")
	public static GanttRangeDecoration ganttRangeDeco(
			@Mandatory String id,
			double from,
			double to,
			String color,
			String label,
			List<String> relevantFor) {
		GanttRangeDecoration deco = GanttRangeDecoration.create()
			.setId(id).setFrom(from).setTo(to)
			.setColor(color != null ? color : "rgba(255, 220, 120, 0.35)")
			.setLabel(label != null ? label : "");
		if (relevantFor != null) {
			deco.setRelevantFor(relevantFor);
		}
		return deco;
	}

	@SideEffectFree
	@Label("Create Gantt axis")
	public static GanttAxis ganttAxis(
			@Mandatory String providerId,
			double rangeMin,
			double rangeMax) {
		AxisProvider provider = AxisProviderService.Module.INSTANCE.getImplementationInstance().lookup(providerId);
		List<GanttTick> ticks = provider != null
			? provider.ticksFor(rangeMin, rangeMax, 1.0)
			: java.util.Collections.emptyList();
		double snap = provider != null ? provider.snapGranularity(1.0) : 1.0;
		return GanttAxis.create()
			.setProviderId(providerId)
			.setRangeMin(rangeMin).setRangeMax(rangeMax)
			.setCurrentZoom(1.0).setSnapGranularity(snap)
			.setCurrentTicks(ticks);
	}

	@SideEffectFree
	@Label("Create Gantt layout")
	public static GanttLayout gantt(
			@Mandatory List<GanttRow> rootRows,
			@Mandatory List<GanttItem> items,
			List<GanttEdge> edges,
			List<GanttDecoration> decorations,
			@Mandatory GanttAxis axis) {
		GanttLayout layout = GanttLayout.create()
			.setRootRows(rootRows)
			.setItems(items)
			.setAxis(axis);
		if (edges != null) {
			layout.setEdges(edges);
		}
		if (decorations != null) {
			layout.setDecorations(decorations);
		}
		// contents are the item boxes so that standard layout/render dispatch reaches them.
		List<Box> contents = new java.util.ArrayList<>(items.size());
		for (GanttItem it : items) {
			contents.add(it.getBox());
		}
		layout.setContents(contents);
		return layout;
	}
```

Add imports at top of `FlowFactory.java`:
```java
import com.top_logic.react.flow.data.GanttAxis;
import com.top_logic.react.flow.data.GanttDecoration;
import com.top_logic.react.flow.data.GanttEdge;
import com.top_logic.react.flow.data.GanttEndpoint;
import com.top_logic.react.flow.data.GanttEnforce;
import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttLayout;
import com.top_logic.react.flow.data.GanttLineDecoration;
import com.top_logic.react.flow.data.GanttMilestone;
import com.top_logic.react.flow.data.GanttRangeDecoration;
import com.top_logic.react.flow.data.GanttRow;
import com.top_logic.react.flow.data.GanttSpan;
import com.top_logic.react.flow.data.GanttTick;
import com.top_logic.react.flow.server.axis.AxisProvider;
import com.top_logic.react.flow.server.axis.AxisProviderService;
```

**Note:** `@Mandatory` here refers to `com.top_logic.basic.config.annotation.Mandatory` per the existing `FlowFactory` pattern — confirm by checking existing imports in the file.

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.server -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java
git commit -m "Ticket #29108: Add Gantt factory functions to FlowFactory."
```

---

### Task 19: Create the Gantt demo layout file

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/layouts/com.top_logic.demo/technical/components/gantt/ganttDemo.layout.xml`

- [ ] **Step 1: Inspect an existing demo flow chart layout for structure**

```bash
find com.top_logic.demo -name "flowchart*.layout.xml" -o -name "*flow*.layout.xml" 2>/dev/null | head -5
```

Then read the first result to see the expected structure.

- [ ] **Step 2: Create the demo layout**

```xml
<?xml version="1.0" encoding="utf-8" ?>
<template xmlns:config="http://www.top-logic.com/ns/config/6.0">
	<config:template-call template="com.top_logic.react.flow/flowChart.template.xml">
		<config:parameter name="chart"><![CDATA[
			reactFlow.gantt(
				rootRows: [
					reactFlow.ganttRow(id: "r-design", label: "Design"),
					reactFlow.ganttRow(id: "r-impl",   label: "Implementation"),
					reactFlow.ganttRow(id: "r-test",   label: "Testing")
				],
				items: [
					reactFlow.ganttSpan(id: "s1", rowId: "r-design", start: 20454, end: 20461,
						box: reactFlow.border(content: reactFlow.padding(all: 4,
							content: reactFlow.text(value: "Spec"))) ),
					reactFlow.ganttSpan(id: "s2", rowId: "r-impl",   start: 20461, end: 20478,
						box: reactFlow.border(content: reactFlow.padding(all: 4,
							content: reactFlow.text(value: "Build")) )),
					reactFlow.ganttMilestone(id: "m1", rowId: "r-test", at: 20478,
						box: reactFlow.border(content: reactFlow.padding(all: 2,
							content: reactFlow.text(value: "Beta"))) ),
					reactFlow.ganttSpan(id: "s3", rowId: "r-test",   start: 20478, end: 20490,
						box: reactFlow.border(content: reactFlow.padding(all: 4,
							content: reactFlow.text(value: "QA")) ))
				],
				edges: [
					reactFlow.ganttEdge(id: "e1",
						sourceItemId: "s1", sourceEndpoint: `reactflow.data:GanttEndpoint.END`,
						targetItemId: "s2", targetEndpoint: `reactflow.data:GanttEndpoint.START`,
						enforce: `reactflow.data:GanttEnforce.STRICT`),
					reactFlow.ganttEdge(id: "e2",
						sourceItemId: "s2", sourceEndpoint: `reactflow.data:GanttEndpoint.END`,
						targetItemId: "m1", targetEndpoint: `reactflow.data:GanttEndpoint.START`,
						enforce: `reactflow.data:GanttEnforce.WARN`)
				],
				decorations: [
					reactFlow.ganttRangeDeco(id: "freeze", from: 20485, to: 20488,
						color: "rgba(255, 80, 80, 0.2)", label: "Freeze")
				],
				axis: reactFlow.ganttAxis(
					providerId: "days-since-epoch",
					rangeMin: 20450,
					rangeMax: 20495)
			)
		]]></config:parameter>
	</config:template-call>
</template>
```

**Note:** The exact TL-Script enum literal syntax (`` `reactflow.data:GanttEnforce.STRICT` ``) and the template reference (`com.top_logic.react.flow/flowChart.template.xml`) must be confirmed against an existing working demo. **Before committing, inspect** `flowchartChart.layout.xml` (mentioned in the exploration report) and adapt the syntax:

```bash
cat com.top_logic.demo/target/tl-demo-*-app/WEB-INF/layouts/com.top_logic.demo/technical/components/flowchart/flowchartChart.layout.xml 2>/dev/null | head -50
```

or search under `src/main/webapp`:

```bash
find com.top_logic.demo/src -name "flowchart*.layout.xml"
```

Copy the template reference and enum-literal style verbatim into this file.

- [ ] **Step 3: Add navigation entry**

Locate the demo navigation file (typically a `.layout.xml` that lists tiles/tabs under "technical/components"):

```bash
grep -rn "flowchart" com.top_logic.demo/src/main/webapp/WEB-INF/layouts --include="*.layout.xml" | head -5
```

Add a sibling entry referencing `gantt/ganttDemo.layout.xml`, copying the structure of the flowchart entry.

- [ ] **Step 4: Build the demo module**

```bash
mvn -B install -pl com.top_logic.demo -DskipTests=true
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 5: Commit**

```bash
git add com.top_logic.demo/
git commit -m "Ticket #29108: Add Gantt demo layout to com.top_logic.demo."
```

---

### Task 20: Manual verification in the demo app

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill to start the demo. (Invoke it via the Skill tool.)

- [ ] **Step 2: Navigate to the Gantt demo using Playwright**

Login with `root` / `root1234`, navigate to "Technical → Components → Gantt" (or the equivalent path where you added the navigation entry).

- [ ] **Step 3: Verify rendering**

The chart should show:
- A time axis header at the top with month ticks (FEB, MAR, APR, ...) and year labels at January boundaries.
- Three row lanes labelled "Design", "Implementation", "Testing", each with alternating light background.
- Four items: "Spec" in Design, "Build" in Implementation, "Beta" milestone in Testing, "QA" span in Testing.
- Two edges: one orthogonal line connecting Spec→Build (STRICT, dark grey), one connecting Build→Beta (WARN, red).
- A light-red vertical range decoration labelled "Freeze" near the right edge.

Take a screenshot and save to `com.top_logic.demo/target/gantt-demo.png` for reference.

- [ ] **Step 4: Document what works and what doesn't**

If anything is off (alignment, missing element, styling), create follow-up commits fixing it — these are real bugs, not scope creep.

- [ ] **Step 5: Stop the demo app**

Via `tl-app` skill.

- [ ] **Step 6: Commit any fixes from Step 4**

```bash
git add -A
git commit -m "Ticket #29108: Fix <specific issue> found during Gantt demo verification."
```

(If no fixes needed, skip this commit.)

---

### Task 21: Final check — run full test suite for both modules

- [ ] **Step 1: Run common-module tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.common 2>&1 | tee com.top_logic.react.flow.common/target/mvn-test.log
```

Expected: all PASS.

- [ ] **Step 2: Run server-module tests**

```bash
mvn -B test -DskipTests=false -pl com.top_logic.react.flow.server 2>&1 | tee com.top_logic.react.flow.server/target/mvn-test.log
```

Expected: all PASS, including `TestGanttLayout` and `TestDaysSinceEpochAxisProvider`.

- [ ] **Step 3: Check build of the demo module**

```bash
mvn -B install -pl com.top_logic.demo -DskipTests=true 2>&1 | tee com.top_logic.demo/target/mvn-build.log
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 4: Final commit (if any test files need cleanup)**

If everything is already clean, skip. Otherwise:

```bash
git add -A
git commit -m "Ticket #29108: Final cleanup for Gantt Phase 1."
```

---

## Out of Scope (Phase 2 / Phase 3)

- Interactive drag/resize — requires generic `DragConstraints` API and handle ports in the Flow library.
- `canDropTo` server command with drag-local cache.
- Hover highlight of incident edges.
- Row expand/collapse.
- Zoom-adaptive axis (client writes `currentZoom`; server recomputes `currentTicks`).
- `axisSnap`, `axisFormatLong`, `axisToPosition`, `axisFromPosition` in the `AxisProvider` interface (only `ticksFor` + `snapGranularity` are needed in Phase 1).
- Edge routing with collision avoidance.
- Decoration drag/resize interactivity.

The complete model is in place so Phases 2 and 3 activate behaviour rather than restructure data.

---

## Self-Review Notes (filled in after writing)

**Spec coverage:** Row tree ✓, Span/Milestone ✓, Edge with endpoints + enforce ✓, Decoration (Line/Range + relevantFor) ✓, AxisProvider with range/currentZoom/currentTicks/snapGranularity ✓, static rendering ✓, server-side axis ✓, demo ✓. Fields `canMoveTime/Row/ResizeStart/End/BeEdgeSource/Target/canMove/canResize` are present in the model but carry no interactive semantics in Phase 1 — correct per phase scope.

**Placeholders:** Three deferred-to-inspection points remain:
- Task 11: exact `SvgWriter` method names.
- Task 13: exact `polyline` signature.
- Task 19: TL-Script enum-literal syntax and the template reference.

These are flagged with explicit inspection commands (`head`, `find`, `cat`) and an instruction to adapt the code before committing. They are **not** TBDs: the engineer knows exactly which file to read and what to extract. Acceptable in this plan because the existing codebase is the authoritative reference.

**Type consistency:** Proto field names match between tasks (e.g., `rangeMin`/`rangeMax` used consistently). Java identifiers (`GanttRow`, `GanttLayout`, etc.) match proto message names.
