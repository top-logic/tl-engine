# Gantt Axis Redesign — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remove the built-in axis/tick rendering from GanttLayout and replace it with the standard row/item mechanism: axis levels become normal rows, axis ticks become point items, axis bars become span items. Rename `GanttMilestone` → `GanttPoint`. Add `strokeWidth`/`dashes` to `GanttLineDecoration` for grid lines.

**Architecture:** The `GanttTick` type and the `drawAxis` renderer are deleted. `GanttAxis` shrinks to pure coordinate config (range, zoom, snap). Axis visuals are built by the application in TL-Script (or via the AxisProvider convenience helper) using regular `ganttRow` / `ganttSpan` / `ganttPoint` calls — the same rendering pipeline as data rows. `GanttLineDecoration` gains `strokeWidth` + `dashes` so vertical grid lines can be styled. Point items (`GanttPoint`, formerly `GanttMilestone`) are sized 0×0 initially and grow to their box's intrinsic size.

**Tech Stack:** msgbuf proto, Java 17, TopLogic TLScriptFunctions, SVG/SvgWriter.

**Spec:** `docs/superpowers/specs/2026-04-15-gantt-chart-design.md` (axis section superseded by this plan)

---

## File Structure

**Proto changes** (all in `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`):
- Remove `GanttTick` message entirely
- Rename `GanttMilestone` → `GanttPoint`
- Simplify `GanttAxis` (remove `currentTicks`)
- Add `strokeWidth` + `dashes` to `GanttLineDecoration`

**Operations** (`com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`):
- Delete `drawAxis`, tick sizing passes (1c, 2c), tick imports
- Change Point sizing to 0×0 initial, accept intrinsic

**Server** (`com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java`):
- Rename `ganttMilestone` → `ganttPoint`
- Simplify `ganttAxis` (no tick generation)
- Remove tick-label-to-contents logic from `gantt()`

**AxisProvider** (`com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/`):
- Change `AxisProvider` interface: `ticksFor` → `buildAxis` returning `AxisContent(rows, items)`
- `DaysSinceEpochAxisProvider` generates rows + items

**Tests** (`com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/`):
- Update `TestGanttLayout` — remove tick references, rename milestone → point
- Update `TestDaysSinceEpochAxisProvider` — adapt to new interface

**Demo** (`com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml`):
- Rewrite with manual axis rows in TL-Script

---

### Task 1: Rename `GanttMilestone` → `GanttPoint` in proto

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`

- [ ] **Step 1: Rename in proto**

Find the `GanttMilestone` message (around line 1082) and rename:

```proto
/** A point item positioned at a single time on the axis. */
message GanttPoint extends GanttItem {
	/** Position of the point on the axis; see {@link GanttAxis} for position semantics. */
	double at;
}
```

Also update any `{@link GanttMilestone...}` references elsewhere in the proto to `GanttPoint`.

- [ ] **Step 2: Build**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: BUILD SUCCESS. Generated files now have `GanttPoint.java`, `GanttPoint_Impl.java` (old `GanttMilestone*` files may linger — delete them manually if they do: `rm com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/GanttMilestone.java com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/impl/GanttMilestone_Impl.java` then rebuild).

- [ ] **Step 3: Update all Java references**

Rename `GanttMilestone` → `GanttPoint` in:
- `GanttLayoutOperations.java` (import + `instanceof GanttMilestone` → `instanceof GanttPoint`)
- `FlowFactory.java` (import + method name `ganttMilestone` → `ganttPoint`, return type, `.create()` call)
- `TestGanttLayout.java` (import + any usage)
- `TestDaysSinceEpochAxisProvider.java` (if referenced)

Search comprehensively:
```bash
grep -rn "GanttMilestone\|ganttMilestone" --include="*.java" com.top_logic.react.flow.common com.top_logic.react.flow.server 2>/dev/null
```

Fix every hit. Then rebuild both modules:
```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.server -DskipTests=true
```

- [ ] **Step 4: Update demo view**

In `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml`, rename `reactFlowGanttMilestone` → `reactFlowGanttPoint`.

- [ ] **Step 5: Run tests**

```bash
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout,TestDaysSinceEpochAxisProvider
```

Expected: all PASS.

- [ ] **Step 6: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Rename GanttMilestone to GanttPoint.

Removes the project-management semantics from the point item type.
GanttPoint is the geometric counterpart to GanttSpan — a positioned
box at a single time rather than a time range. No behavioral change.

User prompt: "Finden wir noch einen besseren Namen für ganttMilestone()"
EOF
)"
```

---

### Task 2: Add `strokeWidth` + `dashes` to `GanttLineDecoration`

**Files:**
- Modify: `data.proto`
- Modify: `GanttLayoutOperations.java` (`drawDecorations`)
- Modify: `FlowFactory.java` (`ganttLineDeco`)

- [ ] **Step 1: Add fields to proto**

In `GanttLineDecoration` (around line 1127), add after the `at` field:

```proto
	/** Stroke width of the line in pixels. */
	double strokeWidth = 1.0;

	/** Dash pattern (alternating dash/gap lengths in pixels). Empty = solid. */
	repeated double dashes;
```

- [ ] **Step 2: Update `drawDecorations` in `GanttLayoutOperations.java`**

Find the line-decoration rendering code in `drawDecorations`. Currently it draws a line with hardcoded stroke width `DECORATION_LINE_STROKE_WIDTH`. Replace with:

```java
double sw = line.getStrokeWidth() > 0 ? line.getStrokeWidth() : 1.0;
```

For dashes: check if `line.getDashes()` is non-empty, and if so call `out.setStrokeDasharray(...)` before drawing the path, similar to how edges handle dashes. **Inspect the existing edge-dashing code in `drawEdges` to find the exact `SvgWriter` API calls.**

- [ ] **Step 3: Update `FlowFactory.ganttLineDeco`**

Add optional parameters `Double strokeWidth` and `List<Double> dashes`:

```java
public static GanttLineDecoration ganttLineDeco(
        @Mandatory Object model,
        double at,
        String color,
        Box label,
        List<Object> relevantFor,
        Boolean canMove,
        Double strokeWidth,
        List<Double> dashes) {
    GanttLineDecoration deco = GanttLineDecoration.create()
        ...
    if (strokeWidth != null) deco.setStrokeWidth(strokeWidth);
    if (dashes != null) deco.setDashes(dashes);
    return deco;
}
```

- [ ] **Step 4: Build + test**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.server -DskipTests=true
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout,TestDaysSinceEpochAxisProvider
```

Expected: all PASS.

- [ ] **Step 5: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Add strokeWidth and dashes to GanttLineDecoration.

Line decorations (used for vertical grid lines) now support custom
stroke width and dash patterns. Defaults to 1px solid. The drawDecorations
renderer reads these from the proto instead of using a hardcoded constant.

User prompt: "Decorations mit Strichstaerke/Dashing/..."
EOF
)"
```

---

### Task 3: Remove `GanttTick` and simplify `GanttAxis`

**Files:**
- Modify: `data.proto`

- [ ] **Step 1: Remove `GanttTick` message**

Delete the entire `GanttTick` message block (around lines 932-941).

- [ ] **Step 2: Remove `currentTicks` from `GanttAxis`**

In `GanttAxis`, delete:
```proto
	/** Axis ticks computed for the current zoom, produced by the {@link AxisProvider}. */
	repeated GanttTick currentTicks;
```

- [ ] **Step 3: Update `GanttAxis` docblock**

The "Positions vs. TimeValues" docblock references `{@link GanttTick#getPosition}`. Remove that reference (and any other `GanttTick` references in the proto).

- [ ] **Step 4: Build**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: BUILD FAILURE — downstream Java files still reference `GanttTick`. That's OK; Task 4 fixes them.

Delete the orphaned generated files if they linger:
```bash
rm -f com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/GanttTick.java \
      com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/impl/GanttTick_Impl.java
```

- [ ] **Step 5: Commit proto change only** (downstream fixes in next tasks)

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/
git commit -m "$(cat <<'EOF'
Ticket #29108: Remove GanttTick and currentTicks from GanttAxis.

Axis ticks are replaced by regular GanttPoint/GanttSpan items in
axis rows. GanttAxis retains only coordinate configuration (range,
zoom, snap, providerId). Downstream Java references will be fixed
in subsequent commits.

User prompt: "Achse raus, GanttTick raus"
EOF
)"
```

---

### Task 4: Remove tick/axis rendering from `GanttLayoutOperations`

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`

- [ ] **Step 1: Remove tick-related code**

Delete or update:
1. Remove `import com.top_logic.react.flow.data.GanttTick;`
2. **Pass 1c** (tick label intrinsic sizing loop) — delete entirely.
3. **Pass 2c** (tick label positioning loop) — delete entirely.
4. **`drawAxis`** method — delete entirely.
5. In `draw()`, remove the `drawAxis(self, out);` call.
6. Remove any axis-background-related color constants that are now unused (e.g. `AXIS_BACKGROUND`, `AXIS_BORDER` if they exist as constants).

- [ ] **Step 2: Change Point sizing**

Find the `instanceof GanttPoint` (formerly `GanttMilestone`) block in `computeIntrinsicSize`. Currently it sizes the box as a square of `rowContentHeight`. Change to:

```java
} else if (item instanceof GanttPoint pt) {
    double cx = offsetX + columnWidth + (pt.getAt() - rangeMin) * zoom;
    // Initial proposal: 0x0 at the anchor point; accept whatever intrinsic size the box reports.
    box.computeIntrinsicSize(context, cx, y);
    double w = box.getWidth();
    double h = box.getHeight();
    // Anchor at the point position, centered vertically in the row.
    box.setX(cx - w / 2.0);
    box.setY(y + (contentHeight - h) / 2.0);
}
```

Where `contentHeight` is the row's content height (= `rowFinalHeight - 2 * rowPadding`). Make sure the variable is available at that scope (it should be from the per-row height calculation in pass 2).

In the per-item **intrinsic height aggregation** (pass 1): for Points, the intrinsic height starts at 0 and is whatever the box reports — this is already the case if `computeIntrinsicSize(context, 0, 0)` is called and `box.getHeight()` is read. No special handling needed.

**Important:** in `distributeSize` (pass 2), Points should NOT have their width/height overridden to the row's full dimensions. They keep their intrinsic size. Only Spans get width forced. Update accordingly:

```java
} else if (item instanceof GanttPoint pt) {
    double cx = offsetX + columnWidth + (pt.getAt() - rangeMin) * zoom;
    double w = box.getWidth();
    double h = Math.max(box.getHeight(), finalContentHeight);
    box.distributeSize(context, cx - w / 2.0, y, w, h);
}
```

Wait — the user said "initial 0,0 als Vorschlag aber jede Größe akzeptieren". So the box starts with 0×0, computes its intrinsic size, and that's what it gets. The point box should participate in row-height growth (a tall point box makes the row taller), but its width is NOT forced.

For `distributeSize` call: width stays intrinsic, height gets the row's final content height (so all items in the row grow together):

```java
} else if (item instanceof GanttPoint pt) {
    double cx = offsetX + columnWidth + (pt.getAt() - rangeMin) * zoom;
    double w = box.getWidth();
    // Give it the row's full content height so it can grow with siblings.
    box.distributeSize(context, cx - w / 2.0, y, w, finalContentHeight);
}
```

- [ ] **Step 3: Remove `axisHeight` usage from layout geometry**

The chart's total height no longer includes a separate axis header. Axis rows are just rows and contribute to height through the normal row mechanism. In `computeIntrinsicSize`, remove the `axisHeight` addend from the total height calculation:

```java
// Old: self.setHeight(axisHeight + totalRowHeight);
// New:
self.setHeight(totalRowHeight);
```

Similarly, the Y offset for the first row was `offsetY + axisHeight`. Now it's just `offsetY`.

Search for ALL usages of `self.getAxisHeight()` or `axisHeight` in the file and remove/adjust them. This includes `drawRowLanes`, `drawDecorations`, and anywhere row Y positions are computed.

Note: the `axisHeight` field stays in the proto for now (to avoid a breaking change while other callers might reference it), but it's effectively unused by the layout. Or, if you're confident nothing else uses it, remove it from the proto too. **Check first:**
```bash
grep -rn "axisHeight\|getAxisHeight\|setAxisHeight" --include="*.java" --include="*.xml" com.top_logic.react.flow.common com.top_logic.react.flow.server com.top_logic.demo 2>/dev/null
```

If only `GanttLayoutOperations` and tests use it, remove from proto too.

- [ ] **Step 4: Build**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.common -DskipTests=true
```

Expected: BUILD SUCCESS. (Server module may still fail due to `FlowFactory` / test references — fixed in Tasks 5+6.)

- [ ] **Step 5: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Remove drawAxis and tick passes from GanttLayoutOperations.

Axis rendering is no longer a layout responsibility — axis rows are
regular rows and render through the standard item/row pipeline.

GanttPoint (formerly Milestone) sizing changes from a forced square
to 0x0 initial with intrinsic-size acceptance. Points are centered
on their time position, vertically centered in the row, and participate
in row-height growth but do not have their width forced.

The axisHeight addend is removed from the total height calculation;
Y offsets no longer include a separate axis header gap.

User prompt: "Achsen-Levels sind Gantt-Zeilen"
EOF
)"
```

---

### Task 5: Restructure `AxisProvider` + update `DaysSinceEpochAxisProvider`

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisProvider.java`
- Create: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisContent.java`
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/providers/DaysSinceEpochAxisProvider.java`

- [ ] **Step 1: Create `AxisContent` record**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.server.axis;

import java.util.List;

import com.top_logic.react.flow.data.GanttItem;
import com.top_logic.react.flow.data.GanttRow;

/**
 * Result of an {@link AxisProvider#buildAxis} call: rows and items that represent the axis
 * visually. The application adds these to its {@code rootRows} and {@code items} lists.
 */
public record AxisContent(List<GanttRow> rows, List<GanttItem> items) {
}
```

- [ ] **Step 2: Change `AxisProvider` interface**

Replace `ticksFor` with `buildAxis`:

```java
public interface AxisProvider {

	String getId();

	/**
	 * Build the axis rows and items for the given visible range and zoom.
	 *
	 * <p>
	 * The returned rows become part of the chart's row forest; the items become
	 * regular Gantt items rendered through the standard pipeline. The provider
	 * decides how many levels to produce and what each level looks like (year bars,
	 * month ticks, week markers, etc.) based on the zoom level.
	 * </p>
	 */
	AxisContent buildAxis(double rangeMin, double rangeMax, double pixelsPerUnit);

	default double snapGranularity(double pixelsPerUnit) {
		return 1.0;
	}
}
```

Remove the `GanttTick` import.

- [ ] **Step 3: Rewrite `DaysSinceEpochAxisProvider`**

The provider now generates rows + items instead of ticks. Example implementation:

```java
@Override
public AxisContent buildAxis(double rangeMin, double rangeMax, double pixelsPerUnit) {
    List<GanttRow> rows = new ArrayList<>();
    List<GanttItem> items = new ArrayList<>();

    // Year level
    GanttRow yearRow = GanttRow.create()
        .setUserObject("axis:years")
        .setLabel(Text.create().setValue("Year"));
    rows.add(yearRow);

    int startYear = LocalDate.ofEpochDay((long) Math.floor(rangeMin)).getYear();
    int endYear = LocalDate.ofEpochDay((long) Math.ceil(rangeMax)).getYear();
    for (int y = startYear; y <= endYear; y++) {
        double from = LocalDate.of(y, 1, 1).toEpochDay();
        double to = LocalDate.of(y, 12, 31).toEpochDay();
        GanttSpan span = GanttSpan.create()
            .setUserObject("year:" + y)
            .setRowModel("axis:years")
            .setBox(Text.create().setValue(String.valueOf(y)))
            .setStart(Math.max(from, rangeMin))
            .setEnd(Math.min(to, rangeMax))
            .setCanMoveTime(false).setCanMoveRow(false)
            .setCanResizeStart(false).setCanResizeEnd(false)
            .setCanBeEdgeSource(false).setCanBeEdgeTarget(false);
        items.add(span);
    }

    // Month level
    GanttRow monthRow = GanttRow.create()
        .setUserObject("axis:months")
        .setLabel(Text.create().setValue("Month"));
    rows.add(monthRow);

    LocalDate firstMonth = LocalDate.ofEpochDay((long) Math.floor(rangeMin)).withDayOfMonth(1);
    if (firstMonth.toEpochDay() < rangeMin) firstMonth = firstMonth.plusMonths(1);
    LocalDate lastDate = LocalDate.ofEpochDay((long) Math.ceil(rangeMax));
    for (LocalDate d = firstMonth; !d.isAfter(lastDate); d = d.plusMonths(1)) {
        GanttPoint tick = GanttPoint.create()
            .setUserObject("month:" + d)
            .setRowModel("axis:months")
            .setBox(Text.create().setValue(d.getMonth().name().substring(0, 3)))
            .setAt(d.toEpochDay())
            .setCanMoveTime(false).setCanMoveRow(false)
            .setCanBeEdgeSource(false).setCanBeEdgeTarget(false);
        items.add(tick);
    }

    return new AxisContent(rows, items);
}
```

**Imports to add:** `GanttSpan`, `GanttPoint`, `GanttRow`, `GanttItem`, `Text`, `AxisContent`.
**Remove:** `GanttTick` import.

- [ ] **Step 4: Build**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.server -DskipTests=true
```

Expected: BUILD SUCCESS (or failure if FlowFactory still references old `ticksFor` — fixed in Task 6).

- [ ] **Step 5: Commit**

```bash
git add -u
git add com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/axis/AxisContent.java
git commit -m "$(cat <<'EOF'
Ticket #29108: AxisProvider returns rows + items instead of ticks.

ticksFor is replaced by buildAxis returning AxisContent(rows, items).
DaysSinceEpochAxisProvider now generates a year row (spans per year)
and a month row (point items per month start). The application adds
these rows and items to its chart; no special axis-rendering code is
involved.

User prompt: "Achsen-Levels als Gantt-Zeilen"
EOF
)"
```

---

### Task 6: Update `FlowFactory` and `gantt()` aggregator

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java`

- [ ] **Step 1: Remove GanttTick references**

Remove `import com.top_logic.react.flow.data.GanttTick;` and any usage. The `ganttAxis` factory no longer calls `provider.ticksFor(...)` or sets `currentTicks`. Change `ganttAxis`:

```java
public static GanttAxis ganttAxis(
        @Mandatory String providerId,
        double rangeMin,
        double rangeMax,
        Double zoom) {
    double pixelsPerUnit = zoom != null ? zoom : 1.0;
    AxisProviderService svc = AxisProviderService.Module.INSTANCE.getImplementationInstance();
    AxisProvider provider = (svc != null) ? svc.lookup(providerId) : null;
    double snap = provider != null ? provider.snapGranularity(pixelsPerUnit) : 1.0;
    return GanttAxis.create()
        .setProviderId(providerId)
        .setRangeMin(rangeMin).setRangeMax(rangeMax)
        .setCurrentZoom(pixelsPerUnit).setSnapGranularity(snap);
}
```

- [ ] **Step 2: Add `ganttAxisContent` helper**

New factory function that calls the provider and returns the generated rows + items. The application merges them into its own lists:

```java
@SideEffectFree
@Label("Build axis rows and items from a registered axis provider")
public static AxisContent ganttAxisContent(
        @Mandatory String providerId,
        double rangeMin,
        double rangeMax,
        Double zoom) {
    double pixelsPerUnit = zoom != null ? zoom : 1.0;
    AxisProviderService svc = AxisProviderService.Module.INSTANCE.getImplementationInstance();
    AxisProvider provider = (svc != null) ? svc.lookup(providerId) : null;
    if (provider == null) {
        return new AxisContent(java.util.Collections.emptyList(), java.util.Collections.emptyList());
    }
    return provider.buildAxis(rangeMin, rangeMax, pixelsPerUnit);
}
```

Add import for `AxisContent`.

- [ ] **Step 3: Remove tick-label-to-contents logic from `gantt()`**

In the `gantt()` aggregator, find the block that adds tick label boxes to `contents`:

```java
for (GanttTick tick : axis.getCurrentTicks()) {
    if (tick.getLabel() != null) contents.add(tick.getLabel());
}
```

Delete it. Tick labels no longer exist; axis items' boxes are added to contents through the normal item loop (since axis items are regular `GanttItem`s in the `items` list).

- [ ] **Step 4: Build + test**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.react.flow.server -DskipTests=true
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout,TestDaysSinceEpochAxisProvider
```

Expected: `TestGanttLayout` passes (if tick references were removed in tests — if not, fix them here). `TestDaysSinceEpochAxisProvider` likely fails (tests still call `ticksFor`) — fixed in Task 7.

- [ ] **Step 5: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Simplify ganttAxis and add ganttAxisContent helper.

ganttAxis no longer calls the provider for ticks — it purely sets
coordinate config. The new ganttAxisContent(providerId, rangeMin,
rangeMax, zoom?) calls the provider's buildAxis and returns the
generated rows + items for the application to merge into its chart.

The gantt() aggregator no longer adds tick labels to contents (ticks
don't exist anymore; axis items use the standard item-to-contents path).

User prompt: "Achse raus"
EOF
)"
```

---

### Task 7: Fix tests

**Files:**
- Modify: `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/layout/TestGanttLayout.java`
- Modify: `com.top_logic.react.flow.server/src/test/java/test/com/top_logic/react/flow/server/axis/TestDaysSinceEpochAxisProvider.java`

- [ ] **Step 1: Fix `TestGanttLayout`**

1. Remove `import com.top_logic.react.flow.data.GanttTick;`
2. Remove the `tick(...)` helper method.
3. Simplify the `axis(...)` helper — no longer creates ticks:
   ```java
   private static GanttAxis axis(double min, double max) {
       return GanttAxis.create()
           .setProviderId("test")
           .setRangeMin(min).setRangeMax(max)
           .setCurrentZoom(1.0);
   }
   ```
4. In `addRowsWithLabels(...)` (or wherever contents are assembled): remove the tick-label-to-contents block.
5. Delete `testAxisRenderingProducesTickOutput` (the concept of axis ticks no longer exists; axis is built by the application as normal rows/items).
6. Remove `setAxisHeight(...)` calls from tests that use it (the field may be removed from proto; if it still exists, just remove test calls to it).

- [ ] **Step 2: Fix `TestDaysSinceEpochAxisProvider`**

The provider no longer has `ticksFor`. Replace with `buildAxis` tests:

```java
public void testBuildAxisProducesYearAndMonthRows() {
    double from = LocalDate.of(2026, 1, 1).toEpochDay();
    double to = LocalDate.of(2026, 12, 31).toEpochDay();
    AxisContent content = new DaysSinceEpochAxisProvider().buildAxis(from, to, 1.0);

    assertEquals("two axis rows (year + month)", 2, content.rows().size());
    assertEquals("year row label", "Year",
        ((Text) content.rows().get(0).getLabel()).getValue());

    // Items: 1 year span + 12 month points = 13
    assertTrue("at least 13 items", content.items().size() >= 13);
}

public void testGetIdReturnsDefault() {
    assertEquals("days-since-epoch", new DaysSinceEpochAxisProvider().getId());
}
```

Remove old `tickLabelValue` helper and tick-based assertions. Add imports for `AxisContent`, `Text`, `LocalDate`.

- [ ] **Step 3: Run all tests**

```bash
mvn -B test -Dtl.javadoc.skipTranslate=true -DskipTests=false -pl com.top_logic.react.flow.server -Dtest=TestGanttLayout,TestDaysSinceEpochAxisProvider
```

Expected: all PASS.

- [ ] **Step 4: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Update tests for axis-as-rows redesign.

TestGanttLayout drops tick-related helpers and the axis-tick-rendering
test. TestDaysSinceEpochAxisProvider uses the new buildAxis interface
and verifies it produces year + month rows with the expected items.

User prompt: "Achse raus"
EOF
)"
```

---

### Task 8: Rewrite demo with manual axis rows in TL-Script

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml`

- [ ] **Step 1: Rewrite the TL-Script**

Replace the existing demo content with a version that builds axis rows manually, demonstrating the new pattern:

```xml
<createChart><![CDATA[{
// --- Axis rows ---
axisYears  = reactFlowGanttRow(model: "axis:years",  label: reactFlowText("Year"));
axisMonths = reactFlowGanttRow(model: "axis:months", label: reactFlowText("Month"));

// Year bars
y2026 = reactFlowGanttSpan(model: "y:2026", rowModel: "axis:years",
    box: reactFlowFill(reactFlowPadding(reactFlowText("2026"), all: 2), color: "#e8e8e8"),
    start: 20454, end: 20819, canMove: false, canResize: false, canBeEdge: false);

// Month ticks (using FloatingLayout for custom positioning)
feb = reactFlowGanttPoint(model: "m:feb", rowModel: "axis:months",
    box: reactFlowText("FEB"), at: 20485, canMove: false, canBeEdge: false);
mar = reactFlowGanttPoint(model: "m:mar", rowModel: "axis:months",
    box: reactFlowText("MAR"), at: 20513, canMove: false, canBeEdge: false);
apr = reactFlowGanttPoint(model: "m:apr", rowModel: "axis:months",
    box: reactFlowText("APR"), at: 20544, canMove: false, canBeEdge: false);
may = reactFlowGanttPoint(model: "m:may", rowModel: "axis:months",
    box: reactFlowText("MAY"), at: 20574, canMove: false, canBeEdge: false);

// --- Data rows ---
r_design = reactFlowGanttRow(model: "phase:design", label: reactFlowBorder(reactFlowPadding(reactFlowText("Design"), all: 4)));
r_impl   = reactFlowGanttRow(model: "phase:impl",   label: reactFlowBorder(reactFlowPadding(reactFlowText("Implementation"), all: 4)));
r_test   = reactFlowGanttRow(model: "phase:test",   label: reactFlowBorder(reactFlowPadding(reactFlowText("Testing"), all: 4)));

// Items
spec  = reactFlowGanttSpan(model: "task:spec",  rowModel: "phase:design", box: reactFlowBorder(reactFlowPadding(reactFlowText("Spec"),  all: 3)), start: 20454, end: 20461);
build = reactFlowGanttSpan(model: "task:build", rowModel: "phase:impl",   box: reactFlowBorder(reactFlowPadding(reactFlowText("Build"), all: 3)), start: 20461, end: 20478);
beta  = reactFlowGanttPoint(model: "milestone:beta", rowModel: "phase:test", box: reactFlowBorder(reactFlowPadding(reactFlowText("Beta"), all: 3)), at: 20478);
qa    = reactFlowGanttSpan(model: "task:qa",   rowModel: "phase:test",   box: reactFlowBorder(reactFlowPadding(reactFlowText("QA"),    all: 3)), start: 20478, end: 20490);

// Edges
e1 = reactFlowGanttEdge(model: "dep:spec->build", sourceModel: "task:spec",  sourceEndpoint: "END", targetModel: "task:build", targetEndpoint: "START", enforce: "STRICT");
e2 = reactFlowGanttEdge(model: "dep:build->beta", sourceModel: "task:build", sourceEndpoint: "END", targetModel: "milestone:beta", targetEndpoint: "START", enforce: "WARN");

// Grid lines as decorations
gridFeb = reactFlowGanttLineDeco(model: "grid:feb", at: 20485, color: "#d0d0d0", strokeWidth: 0.5);
gridMar = reactFlowGanttLineDeco(model: "grid:mar", at: 20513, color: "#d0d0d0", strokeWidth: 0.5);
gridApr = reactFlowGanttLineDeco(model: "grid:apr", at: 20544, color: "#d0d0d0", strokeWidth: 0.5);
gridMay = reactFlowGanttLineDeco(model: "grid:may", at: 20574, color: "#d0d0d0", strokeWidth: 0.5);

// Freeze decoration
freeze = reactFlowGanttRangeDeco(model: "freeze", from: 20485, to: 20488, color: "rgba(255,100,100,0.3)", label: reactFlowText("Freeze"));

// Assemble
axis = reactFlowGanttAxis(providerId: "days-since-epoch", rangeMin: 20450, rangeMax: 20600, zoom: 8);

reactFlowGantt(
    rootRows: list($axisYears, $axisMonths, $r_design, $r_impl, $r_test),
    items: list($y2026, $feb, $mar, $apr, $may, $spec, $build, $beta, $qa),
    edges: list($e1, $e2),
    decorations: list($gridFeb, $gridMar, $gridApr, $gridMay, $freeze),
    axis: $axis
)
}]]></createChart>
```

**Notes on the demo:**
- Axis rows are just the first two entries in `rootRows`.
- Month ticks are `ganttPoint` with plain `Text` boxes.
- Grid lines at month boundaries as `GanttLineDecoration` with `strokeWidth: 0.5`.
- Range extended to 20600 and zoom adjusted to 8 for readability.
- Year bar uses `reactFlowFill` (check if this function exists; if not, use `reactFlowBorder` with background color).

**Before committing:** verify the exact TL-Script function names are correct. Run:
```bash
grep -n "public static.*reactFlow\|@Label" com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java | head -30
```
Adapt function names if the `reactFlow` prefix or any name is wrong.

- [ ] **Step 2: Build demo**

```bash
mvn -B install -Dtl.javadoc.skipTranslate=true -pl com.top_logic.demo -DskipTests=true
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add -u
git commit -m "$(cat <<'EOF'
Ticket #29108: Rewrite Gantt demo with manual axis rows.

The demo now builds axis rows (year + month) explicitly in TL-Script,
demonstrating the axis-as-rows pattern. Year bars are spans, month
labels are points, and vertical grid lines at month boundaries are
line decorations with strokeWidth 0.5. No AxisProvider is used for
rendering — the provider is only referenced for coordinate config.

User prompt: "Demo in TL-Script - damit man sieht, was das bedeutet."
EOF
)"
```

---

### Task 9: Manual verification in browser

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill.

- [ ] **Step 2: Navigate to Gantt Demo**

Login as root / root1234, click "Gantt-Demo" in sidebar.

- [ ] **Step 3: Verify**

The chart should show:
- Two axis rows at the top ("Year" with a 2026 bar, "Month" with FEB/MAR/APR/MAY point labels)
- Three data rows (Design, Implementation, Testing) with items
- Vertical grid lines at month boundaries (thin grey)
- Freeze decoration
- Edges

Take a screenshot: `com.top_logic.demo/target/gantt-axis-redesign.png`.

- [ ] **Step 4: Fix any issues, commit if needed**

- [ ] **Step 5: Stop the demo app**

---

## Self-Review Notes

**Spec coverage:**
- GanttTick removed ✓ (Task 3)
- GanttMilestone → GanttPoint ✓ (Task 1)
- drawAxis removed ✓ (Task 4)
- Point sizing 0×0 initial ✓ (Task 4)
- strokeWidth + dashes on LineDecoration ✓ (Task 2)
- AxisProvider returns rows+items ✓ (Task 5)
- Demo in TL-Script ✓ (Task 8)
- axisHeight field: removed or made unused ✓ (Task 4)

**Placeholder scan:** No TBD/TODO found.

**Type consistency:** `GanttPoint` used consistently (Task 1 renames, Tasks 4-8 use new name). `AxisContent` created in Task 5, used in Tasks 5-6. `buildAxis` replaces `ticksFor` everywhere.
