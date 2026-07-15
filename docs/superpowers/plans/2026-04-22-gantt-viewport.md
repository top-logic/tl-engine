# GanttLayout Viewport Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable the GanttLayout to manage its own viewport with frozen header/sidebar panes and independent scroll/zoom, so large Gantt charts are usable without relying on diagram-level panning.

**Architecture:** The GanttLayout renders its content into five clipped SVG groups (corner, header, sidebar, decorations, content), each with scroll-offset transforms. The client intercepts wheel/pointer events within the GanttLayout area and updates scroll/zoom state directly, bypassing the diagram-level pan/zoom. Zooming changes `pixelsPerUnit` (horizontal scale only), triggering a client-side re-layout.

**Tech Stack:** Java (msgbuf proto, GanttLayoutOperations), GWT (FlowDiagramClientControl, SVGBuilder), TL-Script (FlowFactory)

---

## File Structure

| File | Role |
|------|------|
| `com.top_logic.react.flow.common/.../data/data.proto` | Add `scrollX`, `scrollY`, `frozenRows`, transient region fields to `GanttLayout` |
| `com.top_logic.react.flow.common/.../data/GanttLayout.java` | Regenerated interface |
| `com.top_logic.react.flow.common/.../data/impl/GanttLayout_Impl.java` | Regenerated impl |
| `com.top_logic.react.flow.common/.../operations/layout/GanttLayoutOperations.java` | Restructured layout + draw with viewport groups |
| `com.top_logic.react.flow.client/.../control/FlowDiagramClientControl.java` | Intercept wheel/drag events for GanttLayout viewport |
| `com.top_logic.react.flow.server/.../script/FlowFactory.java` | Add `frozenRows` param to `gantt()` |
| `com.top_logic.demo/.../views/demo/gantt-demo.view.xml` | Enable viewport in demo |

---

### Task 1: Proto Model — Add viewport fields to GanttLayout

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto:1270-1322`
- Regenerate: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/GanttLayout.java`
- Regenerate: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/impl/GanttLayout_Impl.java`

- [ ] **Step 1: Add fields to GanttLayout in data.proto**

Add these fields inside the `message GanttLayout extends Layout` block, after the existing `transient double columnWidth;` field:

```proto
/**
 * Horizontal scroll offset, in position units (not pixels).
 *
 * <p>
 * The pixel offset applied to the content and header groups is {@code scrollX * currentZoom}.
 * Using position units ensures that the viewport stays centered on the same time point
 * when zoom changes.
 * </p>
 */
double scrollX;

/**
 * Vertical scroll offset, in pixels.
 *
 * <p>
 * Applied to the content and sidebar groups. Not affected by zoom changes (row heights
 * do not change when zooming).
 * </p>
 */
double scrollY;

/**
 * Number of leading top-level rows in {@link #rootRows} that form the frozen header.
 *
 * <p>
 * If {@code frozenRows = 3}, the first three root rows (e.g. Year, Month, Markers) stay
 * visible at the top and do not scroll vertically. Their labels form the frozen corner.
 * {@code 0} means no frozen header.
 * </p>
 */
int frozenRows;

/**
 * Height of the frozen header area, in pixels. Computed during layout from the
 * accumulated heights of the first {@link #frozenRows} root rows and their descendants.
 */
transient double headerHeight;

/**
 * Total height of the scrollable data rows, in pixels. Computed during layout.
 * Used for scroll bounds: {@code maxScrollY = max(0, dataHeight - viewportContentHeight)}.
 */
transient double dataHeight;
```

- [ ] **Step 2: Regenerate msgbuf classes**

Run from project root:

```bash
mvn -B de.haumacher.msgbuf:msgbuf-generator-maven-plugin:1.1.11:generate -pl com.top_logic.react.flow.common 2>&1 | tee com.top_logic.react.flow.common/target/mvn-generate.log
```

- [ ] **Step 3: Build the module**

```bash
mvn -B install -pl com.top_logic.react.flow.common 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto \
       com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/GanttLayout.java \
       com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/impl/GanttLayout_Impl.java
git commit -m "Ticket #29108: Add viewport fields (scrollX, scrollY, frozenRows) to GanttLayout proto."
```

---

### Task 2: Layout Computation — Compute header and data region dimensions

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java:76-267`

**Context:** The existing `computeIntrinsicSize` already computes per-row heights and cumulative Y positions in arrays `rowYStart` and `rowTotalHeight`. We need to additionally compute `headerHeight` (sum of frozen row heights) and `dataHeight` (sum of data row heights), and store them as transient fields on the GanttLayout.

- [ ] **Step 1: Add helper to count flattened rows in frozen root rows**

At the end of `GanttLayoutOperations` (before the closing `}`), add:

```java
/**
 * Counts the total number of rows (including descendants) in the first
 * {@code frozenCount} root rows.
 */
private static int countFrozenFlatRows(List<GanttRow> rootRows, int frozenCount) {
    int count = 0;
    for (int i = 0; i < Math.min(frozenCount, rootRows.size()); i++) {
        count += countRowAndDescendants(rootRows.get(i));
    }
    return count;
}

private static int countRowAndDescendants(GanttRow row) {
    int count = 1;
    for (GanttRow child : row.getChildren()) {
        count += countRowAndDescendants(child);
    }
    return count;
}
```

- [ ] **Step 2: Compute and store headerHeight/dataHeight at end of computeIntrinsicSize**

In `computeIntrinsicSize`, after the line `self.setHeight(totalHeight);` (line 266), add:

```java
// Compute frozen header vs. scrollable data region heights.
int frozenRows = self.getFrozenRows();
if (frozenRows > 0 && frozenRows <= self.getRootRows().size()) {
    int frozenFlatCount = countFrozenFlatRows(self.getRootRows(), frozenRows);
    double headerH = 0;
    for (int i = 0; i < frozenFlatCount && i < totalRows; i++) {
        headerH += rowTotalHeight[i];
    }
    self.setHeaderHeight(headerH);
    self.setDataHeight(totalHeight - headerH);
} else {
    self.setHeaderHeight(0);
    self.setDataHeight(totalHeight);
}
```

- [ ] **Step 3: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: BUILD SUCCESS. No behavioral change yet — `headerHeight`/`dataHeight` are computed but not used.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java
git commit -m "Ticket #29108: Compute headerHeight and dataHeight during Gantt layout for viewport region splitting."
```

---

### Task 3: Rendering — Restructure draw() into clipped viewport groups

**Files:**
- Modify: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java:308-317` (draw method) and related private draw methods

**Context:** The current `draw()` renders everything in a flat SVG structure:
1. `drawRowLanes` — background rects for all rows
2. `drawDecorations` — vertical lines, range bands
3. Standard `out.write(content)` loop — item boxes + decoration labels
4. `drawEdges` — dependency arrows

We restructure this into five clipped groups with scroll transforms. The groups are painted in this order (back to front):

1. **Decorations group** — grid lines, range bands (scrolls X only, spans header+data height)
2. **Content group** — data row lanes, data item boxes, edges (scrolls X+Y)
3. **Sidebar group** — data row labels with opaque background (scrolls Y only)
4. **Header group** — frozen row lanes, frozen items with opaque background (scrolls X only)
5. **Corner group** — frozen row labels with opaque background (no scroll)

- [ ] **Step 1: Add isFrozenRow helper and split item categorization**

Add these helper methods to `GanttLayoutOperations`:

```java
/**
 * Returns the set of row IDs that belong to frozen (header) rows.
 */
private static java.util.Set<String> buildFrozenRowIds(GanttLayout self) {
    java.util.Set<String> frozen = new java.util.HashSet<>();
    int frozenCount = self.getFrozenRows();
    List<GanttRow> roots = self.getRootRows();
    for (int i = 0; i < Math.min(frozenCount, roots.size()); i++) {
        collectRowIds(roots.get(i), frozen);
    }
    return frozen;
}

private static void collectRowIds(GanttRow row, java.util.Set<String> ids) {
    ids.add(row.getId());
    for (GanttRow child : row.getChildren()) {
        collectRowIds(child, ids);
    }
}
```

- [ ] **Step 2: Replace the draw() method**

Replace the entire `draw()` default method with the new viewport-aware implementation:

```java
@Override
default void draw(SvgWriter out) {
    GanttLayout self = (GanttLayout) this;

    double x0 = self.getX();
    double y0 = self.getY();
    double totalW = self.getWidth();
    double totalH = self.getHeight();
    double colW = self.getColumnWidth();
    double headerH = self.getHeaderHeight();
    double scrollX = self.getScrollX();
    double scrollY = self.getScrollY();
    double zoom = self.getAxis().getCurrentZoom();
    double scrollXPx = scrollX * zoom;

    // Determine frozen row IDs.
    java.util.Set<String> frozenRowIds = buildFrozenRowIds(self);
    boolean hasViewport = self.getFrozenRows() > 0;

    if (!hasViewport) {
        // No frozen rows — draw everything flat (original behavior).
        drawRowLanes(self, out, null, true, true);
        drawDecorations(self, out);
        for (Box content : self.getContents()) {
            out.write(content);
        }
        drawEdges(self, out);
        return;
    }

    // --- Define clip regions ---
    String clipCorner = self.getClientId() + "-clip-corner";
    String clipHeader = self.getClientId() + "-clip-header";
    String clipSidebar = self.getClientId() + "-clip-sidebar";
    String clipContent = self.getClientId() + "-clip-content";
    String clipDecorations = self.getClientId() + "-clip-deco";

    // Emit clipPath definitions (SVG <defs> are scoped to the nearest SVG root,
    // but clipPaths can be defined inline — they just need unique IDs).
    emitClipPath(out, clipCorner, x0, y0, colW, headerH);
    emitClipPath(out, clipHeader, x0 + colW, y0, totalW - colW, headerH);
    emitClipPath(out, clipSidebar, x0, y0 + headerH, colW, totalH - headerH);
    emitClipPath(out, clipContent, x0 + colW, y0 + headerH, totalW - colW, totalH - headerH);
    emitClipPath(out, clipDecorations, x0 + colW, y0, totalW - colW, totalH);

    // NOTE: Each scrolling group uses TWO nested <g> elements:
    //   outer <g>: holds clip-path attribute (no transform)
    //   inner <g>: holds translate transform + has an ID for fast client-side updates
    // This is required because SVG clip-path coordinates are evaluated in the
    // referencing element's coordinate system. If clip-path and transform were on the
    // same <g>, the clip region would shift with the translate — defeating the purpose.

    String ganttId = self.getClientId();

    // --- 1. Decorations group (scroll X only, spans full height) ---
    out.beginGroup();
    out.writeAttribute("clip-path", "url(#" + clipDecorations + ")");
    out.writeCssClass("tl-gantt-vp-decorations");
    out.beginGroup();
    out.writeId(ganttId + "-scroll-deco");
    out.translate(-scrollXPx, 0);
    drawDecorations(self, out);
    out.endGroup();
    out.endGroup();

    // --- 2. Content group (scroll X+Y) ---
    out.beginGroup();
    out.writeAttribute("clip-path", "url(#" + clipContent + ")");
    out.writeCssClass("tl-gantt-vp-content");
    out.beginGroup();
    out.writeId(ganttId + "-scroll-content");
    out.translate(-scrollXPx, -scrollY);
    drawRowLanes(self, out, frozenRowIds, false, true);
    drawItemBoxes(self, out, frozenRowIds, false);
    drawEdges(self, out);
    out.endGroup();
    out.endGroup();

    // --- 3. Sidebar group (scroll Y only) ---
    out.beginGroup();
    out.writeAttribute("clip-path", "url(#" + clipSidebar + ")");
    out.writeCssClass("tl-gantt-vp-sidebar");
    // Opaque background to cover scrolling content (outside the scroll transform).
    out.beginRect(x0, y0 + headerH, colW, totalH - headerH);
    out.setFill("#ffffff");
    out.setStroke("none");
    out.endRect();
    out.beginGroup();
    out.writeId(ganttId + "-scroll-sidebar");
    out.translate(0, -scrollY);
    drawRowLabels(self, out, frozenRowIds, false);
    out.endGroup();
    out.endGroup();

    // --- 4. Header group (scroll X only) ---
    out.beginGroup();
    out.writeAttribute("clip-path", "url(#" + clipHeader + ")");
    out.writeCssClass("tl-gantt-vp-header");
    // Opaque background (outside the scroll transform).
    out.beginRect(x0 + colW, y0, totalW - colW, headerH);
    out.setFill("#ffffff");
    out.setStroke("none");
    out.endRect();
    out.beginGroup();
    out.writeId(ganttId + "-scroll-header");
    out.translate(-scrollXPx, 0);
    drawRowLanes(self, out, frozenRowIds, true, false);
    drawItemBoxes(self, out, frozenRowIds, true);
    out.endGroup();
    out.endGroup();

    // --- 5. Corner group (no scroll, no inner transform group needed) ---
    out.beginGroup();
    out.writeAttribute("clip-path", "url(#" + clipCorner + ")");
    out.writeCssClass("tl-gantt-vp-corner");
    // Opaque background.
    out.beginRect(x0, y0, colW, headerH);
    out.setFill("#ffffff");
    out.setStroke("none");
    out.endRect();
    drawRowLanes(self, out, frozenRowIds, true, false);
    drawRowLabels(self, out, frozenRowIds, true);
    out.endGroup();
}
```

- [ ] **Step 3: Add emitClipPath helper**

```java
private static void emitClipPath(SvgWriter out, String id, double x, double y, double w, double h) {
    out.beginClipPath();
    out.writeAttribute("id", id);
    out.beginRect(x, y, w, h);
    out.endRect();
    out.endClipPath();
}
```

- [ ] **Step 4: Refactor drawRowLanes to accept a filter**

Replace the existing `drawRowLanes` method signature. The new version takes a `frozenRowIds` set and booleans to select which rows to draw:

```java
/**
 * Draws row lane backgrounds and borders.
 *
 * @param frozenRowIds set of frozen row IDs (null = no viewport, draw all)
 * @param drawFrozen if true, draw lanes for rows IN frozenRowIds
 * @param drawData if true, draw lanes for rows NOT IN frozenRowIds
 */
private static void drawRowLanes(GanttLayout self, SvgWriter out,
        java.util.Set<String> frozenRowIds, boolean drawFrozen, boolean drawData) {
    double x0 = self.getX();
    double totalWidth = self.getWidth();
    double columnWidth = self.getColumnWidth();

    Map<String, RowGeometry> rowGeometry = buildRowGeometry(self, self.getY());

    out.beginGroup();
    out.writeCssClass("tl-gantt-lanes");

    int[] rowIndex = new int[] { 0 };
    for (GanttRow root : self.getRootRows()) {
        drawRowLane(root, x0, totalWidth, columnWidth,
            rowGeometry, out, rowIndex, frozenRowIds, drawFrozen, drawData);
    }

    out.endGroup();
}
```

Update `drawRowLane` to skip rows based on the filter:

```java
private static void drawRowLane(GanttRow row,
        double x0, double totalWidth, double columnWidth,
        Map<String, RowGeometry> rowGeometry, SvgWriter out, int[] rowIndex,
        java.util.Set<String> frozenRowIds, boolean drawFrozen, boolean drawData) {
    int idx = rowIndex[0]++;

    boolean isFrozen = frozenRowIds != null && frozenRowIds.contains(row.getId());
    boolean shouldDraw = (isFrozen && drawFrozen) || (!isFrozen && drawData)
        || frozenRowIds == null;

    if (shouldDraw) {
        RowGeometry geom = rowGeometry.get(row.getId());
        if (geom != null) {
            double rowY = geom.yStart();
            double rowHeight = geom.height();

            String bgColor = row.getBackgroundColor();
            String bdColor = row.getBorderColor();
            if (bgColor != null && !bgColor.isEmpty() || bdColor != null && !bdColor.isEmpty()) {
                out.beginRect(x0, rowY, totalWidth, rowHeight);
                out.setFill(bgColor != null && !bgColor.isEmpty() ? bgColor : "none");
                if (bdColor != null && !bdColor.isEmpty()) {
                    out.setStroke(bdColor);
                    out.setStrokeWidth(1.0);
                } else {
                    out.setStroke("none");
                }
                out.endRect();
            }

            if (bdColor != null && !bdColor.isEmpty()) {
                double sepX = x0 + columnWidth;
                out.beginPath();
                out.setStroke(bdColor);
                out.setStrokeWidth(1.0);
                out.setFill("none");
                out.beginData();
                out.moveToAbs(sepX, rowY);
                out.lineToAbs(sepX, rowY + rowHeight);
                out.endData();
                out.endPath();
            }
        }
    }

    for (GanttRow child : row.getChildren()) {
        drawRowLane(child, x0, totalWidth, columnWidth,
            rowGeometry, out, rowIndex, frozenRowIds, drawFrozen, drawData);
    }
}
```

- [ ] **Step 5: Add drawItemBoxes helper**

This replaces the `for (Box content : self.getContents()) { out.write(content); }` loop with a filtered version:

```java
/**
 * Draws item boxes for either frozen or data rows.
 *
 * @param frozenRowIds set of frozen row IDs
 * @param drawFrozen if true, draw items in frozen rows; if false, draw items in data rows
 */
private static void drawItemBoxes(GanttLayout self, SvgWriter out,
        java.util.Set<String> frozenRowIds, boolean drawFrozen) {
    for (GanttItem item : self.getItems()) {
        boolean isFrozen = frozenRowIds.contains(item.getRowId());
        if (isFrozen == drawFrozen) {
            Box box = item.getBox();
            if (box != null) {
                out.write(box);
            }
        }
    }
    // Draw decoration labels (they are in contents but not item boxes).
    // Decoration labels are always drawn in the content region (non-frozen).
    if (!drawFrozen) {
        for (GanttDecoration deco : self.getDecorations()) {
            Box label = deco.getLabel();
            if (label != null) {
                out.write(label);
            }
        }
    }
}
```

- [ ] **Step 6: Add drawRowLabels helper**

```java
/**
 * Draws row label boxes for either frozen or data rows.
 *
 * @param frozenRowIds set of frozen row IDs
 * @param drawFrozen if true, draw labels for frozen rows; if false, draw labels for data rows
 */
private static void drawRowLabels(GanttLayout self, SvgWriter out,
        java.util.Set<String> frozenRowIds, boolean drawFrozen) {
    for (GanttRow root : self.getRootRows()) {
        drawRowLabel(root, out, frozenRowIds, drawFrozen);
    }
}

private static void drawRowLabel(GanttRow row, SvgWriter out,
        java.util.Set<String> frozenRowIds, boolean drawFrozen) {
    boolean isFrozen = frozenRowIds.contains(row.getId());
    if (isFrozen == drawFrozen) {
        Box label = row.getLabel();
        if (label != null) {
            out.write(label);
        }
    }
    for (GanttRow child : row.getChildren()) {
        drawRowLabel(child, out, frozenRowIds, drawFrozen);
    }
}
```

- [ ] **Step 7: Build and verify**

```bash
mvn -B install -pl com.top_logic.react.flow.common 2>&1 | tee com.top_logic.react.flow.common/target/mvn-build.log
```

Expected: BUILD SUCCESS. With `frozenRows = 0` (default), the fallback path draws everything flat — no visual change.

- [ ] **Step 8: Commit**

```bash
git add com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java
git commit -m "Ticket #29108: Restructure GanttLayout draw into five clipped viewport groups with scroll transforms."
```

---

### Task 4: TL-Script API — Add frozenRows parameter

**Files:**
- Modify: `com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java`

- [ ] **Step 1: Add frozenRows parameter to gantt() factory method**

Find the `gantt()` method. Add a `frozenRows` parameter (type `Integer`, default `0`). In the method body, after the existing aggregator logic, add:

```java
if (frozenRows != null && frozenRows > 0) {
    layout.setFrozenRows(frozenRows);
}
```

The full parameter list of `gantt()` becomes:

```java
public static GanttLayout gantt(
        @Mandatory List<GanttRow> rootRows,
        @Mandatory List<GanttItem> items,
        List<GanttEdge> edges,
        List<GanttDecoration> decorations,
        @Mandatory GanttAxis axis,
        Integer frozenRows) {
```

- [ ] **Step 2: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.server 2>&1 | tee com.top_logic.react.flow.server/target/mvn-build.log
```

Expected: BUILD SUCCESS.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.react.flow.server/src/main/java/com/top_logic/react/flow/server/script/FlowFactory.java
git commit -m "Ticket #29108: Add frozenRows parameter to reactFlowGantt() TL-Script function."
```

---

### Task 5: Demo Update — Enable viewport with frozen rows

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml`

- [ ] **Step 1: Add more data rows for vertical scrolling**

In the demo view, after the existing row definitions (`r_design`, `r_impl`, `r_test`), add additional rows so the chart needs vertical scrolling:

```
r_deploy = reactFlowGanttRow(model: "phase:deploy", label: reactFlowPadding(reactFlowText("Deployment"),   all: 4), backgroundColor: "#f5f5f5", borderColor: "#e0e0e0");
r_docs   = reactFlowGanttRow(model: "phase:docs",   label: reactFlowPadding(reactFlowText("Documentation"), all: 4), backgroundColor: "#ffffff", borderColor: "#e0e0e0");
r_review = reactFlowGanttRow(model: "phase:review", label: reactFlowPadding(reactFlowText("Review"),        all: 4), backgroundColor: "#f5f5f5", borderColor: "#e0e0e0");
r_launch = reactFlowGanttRow(model: "phase:launch", label: reactFlowPadding(reactFlowText("Launch"),        all: 4), backgroundColor: "#ffffff", borderColor: "#e0e0e0");
r_maint  = reactFlowGanttRow(model: "phase:maint",  label: reactFlowPadding(reactFlowText("Maintenance"),   all: 4), backgroundColor: "#f5f5f5", borderColor: "#e0e0e0");
```

Add items for the new rows:

```
deploy_item = reactFlowGanttSpan(model: "task:deploy", rowModel: "phase:deploy",
    box: reactFlowFill(reactFlowBorder(reactFlowPadding(reactFlowText("Deploy"), all: 3)), fill: "#d1c4e9"), start: 20574, end: 20590);
docs_item = reactFlowGanttSpan(model: "task:docs", rowModel: "phase:docs",
    box: reactFlowFill(reactFlowBorder(reactFlowPadding(reactFlowText("Write Docs"), all: 3)), fill: "#b3e5fc"), start: 20544, end: 20580);
review_item = reactFlowGanttSpan(model: "task:review", rowModel: "phase:review",
    box: reactFlowFill(reactFlowBorder(reactFlowPadding(reactFlowText("Code Review"), all: 3)), fill: "#c8e6c9"), start: 20560, end: 20574);
launch_item = reactFlowGanttSpan(model: "task:launch", rowModel: "phase:launch",
    box: reactFlowFill(reactFlowBorder(reactFlowPadding(reactFlowText("Go Live"), all: 3)), fill: "#ffccbc"), start: 20590, end: 20600);
maint_item = reactFlowGanttSpan(model: "task:maint", rowModel: "phase:maint",
    box: reactFlowFill(reactFlowBorder(reactFlowPadding(reactFlowText("Support"), all: 3)), fill: "#f0f4c3"), start: 20590, end: 20620);
```

- [ ] **Step 2: Update the reactFlowGantt() call to include frozenRows and new rows/items**

Update the `reactFlowGantt(...)` call at the bottom of the createChart expression:

```
reactFlowGantt(
    rootRows:    list($r_year, $r_months, $r_markers, $r_design, $r_impl, $r_test, $r_deploy, $r_docs, $r_review, $r_launch, $r_maint),
    items:       list($year2026, $mFeb, $mMar, $mApr, $mMay, $freezeLabel, $spec, $build, $beta, $qa, $fixed_row, $no_resize, $deploy_item, $docs_item, $review_item, $launch_item, $maint_item),
    edges:       list($e1, $e2),
    decorations: list($gl_feb, $gl_mar, $gl_apr, $gl_may, $freeze),
    axis: $axis,
    frozenRows: 3
)
```

The `frozenRows: 3` freezes the first three root rows (Year, Months, Markers) as the header.

- [ ] **Step 3: Build demo**

```bash
mvn -B install -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server,com.top_logic.demo 2>&1 | tee com.top_logic.demo/target/mvn-build.log
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml
git commit -m "Ticket #29108: Enable viewport with frozenRows in Gantt demo, add more rows for scrolling."
```

---

### Task 6: Client — Scroll event handling

**Files:**
- Modify: `com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java`

**Context:** The wheel event is currently handled by `_zoomOrScrollSVG` which routes to `panSVG`/`zoomSVG` at the diagram level. We need to intercept wheel events that land inside a GanttLayout and route them to Gantt-specific scroll/zoom instead.

- [ ] **Step 1: Add helper to find GanttLayout from event target**

Add this method to `FlowDiagramClientControl`:

```java
/**
 * Walks the Box parent chain from the event target to find a {@link GanttLayout}.
 * Returns null if the event target is not inside a GanttLayout.
 */
private GanttLayout findGanttLayoutAt(elemental2.dom.Element target) {
    elemental2.dom.Element current = target;
    for (int i = 0; i < 50 && current != null; i++) {
        Object widget = getAttachedWidget(current);
        if (widget instanceof GanttLayout) {
            return (GanttLayout) widget;
        }
        current = current.parentElement;
    }
    return null;
}
```

- [ ] **Step 2: Add ganttScroll method**

```java
/**
 * Handles scroll within a GanttLayout viewport.
 * Updates scrollX/scrollY and applies transforms to the viewport SVG groups.
 */
private void ganttScroll(GanttLayout gantt, double deltaX, double deltaY) {
    double zoom = gantt.getAxis().getCurrentZoom();
    double rangeMin = gantt.getAxis().getRangeMin();
    double rangeMax = gantt.getAxis().getRangeMax();
    double colW = gantt.getColumnWidth();
    double headerH = gantt.getHeaderHeight();
    double dataH = gantt.getDataHeight();
    double totalW = gantt.getWidth();
    double totalH = gantt.getHeight();

    // Viewport content dimensions.
    double viewportContentW = totalW - colW;
    double viewportContentH = totalH - headerH;

    // Virtual content dimensions.
    double virtualContentW = (rangeMax - rangeMin) * zoom;

    // Max scroll bounds.
    double maxScrollX = Math.max(0, (virtualContentW - viewportContentW) / zoom);
    double maxScrollY = Math.max(0, dataH - viewportContentH);

    // Convert deltas to scroll units.
    // deltaX/deltaY are in screen pixels; convert to SVG units via the current diagram zoom.
    double factor = getFactor();
    double newScrollX = gantt.getScrollX() + deltaX * factor / zoom;
    double newScrollY = gantt.getScrollY() + deltaY * factor;

    // Clamp.
    newScrollX = Math.max(0, Math.min(maxScrollX, newScrollX));
    newScrollY = Math.max(0, Math.min(maxScrollY, newScrollY));

    gantt.setScrollX(newScrollX);
    gantt.setScrollY(newScrollY);

    // Apply transforms to SVG groups.
    applyGanttViewportTransforms(gantt);
}
```

- [ ] **Step 3: Add applyGanttViewportTransforms method**

This directly updates the SVG transform attributes on the inner scroll groups (identified by their IDs set in Task 3, Step 2) without re-rendering:

```java
/**
 * Updates the translate transforms on the GanttLayout's viewport scroll groups.
 * Each viewport region has an inner {@code <g>} with an explicit ID for fast updates.
 */
private void applyGanttViewportTransforms(GanttLayout gantt) {
    String ganttId = gantt.getClientId();
    if (ganttId == null) {
        return;
    }

    double scrollXPx = gantt.getScrollX() * gantt.getAxis().getCurrentZoom();
    double scrollY = gantt.getScrollY();

    setTranslateById(ganttId + "-scroll-deco", -scrollXPx, 0);
    setTranslateById(ganttId + "-scroll-content", -scrollXPx, -scrollY);
    setTranslateById(ganttId + "-scroll-sidebar", 0, -scrollY);
    setTranslateById(ganttId + "-scroll-header", -scrollXPx, 0);
    // Corner has no scroll transform.
}

private native void setTranslateById(String id, double tx, double ty) /*-{
    var el = $doc.getElementById(id);
    if (el) el.setAttribute('transform', 'translate(' + tx + ',' + ty + ')');
}-*/;
```

- [ ] **Step 4: Modify _zoomOrScrollSVG to route GanttLayout events**

In the `handleEvent` method of `_zoomOrScrollSVG`, add a check at the very beginning:

```java
@Override
public void handleEvent(Event evt) {
    WheelEvent event = (WheelEvent) evt;

    // Check if event is inside a GanttLayout — route to Gantt scroll.
    elemental2.dom.Element target = (elemental2.dom.Element) event.target;
    GanttLayout ganttLayout = findGanttLayoutAt(target);
    if (ganttLayout != null && ganttLayout.getFrozenRows() > 0) {
        event.preventDefault();
        event.stopPropagation();

        if (event.ctrlKey) {
            // Ctrl+wheel = Gantt zoom (Task 7).
            ganttZoom(ganttLayout, event);
        } else {
            // Normal/shift wheel = Gantt scroll.
            double scrollFactor = getWheelScrollFactor(evt);
            double deltaX;
            double deltaY;
            if (event.shiftKey) {
                deltaX = event.deltaY * scrollFactor;
                deltaY = event.deltaX * scrollFactor;
            } else {
                deltaX = event.deltaX * scrollFactor;
                deltaY = event.deltaY * scrollFactor;
            }
            ganttScroll(ganttLayout, deltaX, deltaY);
        }
        return;
    }

    // ... existing diagram-level pan/zoom code follows unchanged ...
```

- [ ] **Step 5: Add stub for ganttZoom (implemented in Task 7)**

```java
private void ganttZoom(GanttLayout gantt, WheelEvent event) {
    // TODO: Implement in Task 7.
}
```

- [ ] **Step 6: Build the client module**

```bash
mvn -B install -pl com.top_logic.react.flow.client 2>&1 | tee com.top_logic.react.flow.client/target/mvn-build.log
```

Expected: BUILD SUCCESS (GWT compilation takes several minutes).

- [ ] **Step 7: Commit**

```bash
git add com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java
git commit -m "Ticket #29108: Intercept wheel events inside GanttLayout for viewport scrolling."
```

---

### Task 7: Client — Gantt zoom handling

**Files:**
- Modify: `com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java`

**Context:** Ctrl+wheel inside a GanttLayout should change `pixelsPerUnit` on the GanttAxis, keeping the mouse position stable. This requires a full re-layout and re-draw (not just a transform update).

- [ ] **Step 1: Implement ganttZoom**

Replace the stub from Task 6:

```java
/**
 * Handles zoom (Ctrl+wheel) inside a GanttLayout.
 * Changes pixelsPerUnit and triggers re-layout.
 */
private void ganttZoom(GanttLayout gantt, WheelEvent event) {
    GanttAxis axis = gantt.getAxis();
    double currentZoom = axis.getCurrentZoom();

    // Compute new zoom level.
    double delta = event.deltaY == 0 ? event.deltaX : event.deltaY;
    double direction = JsMath.sign(delta);
    // Zoom in (delta < 0) = increase pixelsPerUnit, zoom out (delta > 0) = decrease.
    double factor = 1.0 - direction * 0.1;
    double newZoom = currentZoom * factor;

    // Clamp to reasonable range.
    double minZoom = 0.5;  // 0.5 px per position unit
    double maxZoom = 100.0; // 100 px per position unit
    newZoom = Math.max(minZoom, Math.min(maxZoom, newZoom));

    if (Math.abs(newZoom - currentZoom) < 0.001) {
        return;
    }

    // Adjust scrollX to keep the mouse position stable.
    // Mouse is at some position in the viewport; we want that position to map
    // to the same time value before and after zoom.
    double colW = gantt.getColumnWidth();
    double x0 = gantt.getX();
    double[] svgCoords = clientToSvg(event.clientX, event.clientY);
    double mouseXInChart = svgCoords[0] - x0 - colW;

    // Time value under the mouse before zoom:
    double scrollXPxBefore = gantt.getScrollX() * currentZoom;
    double timeUnderMouse = (mouseXInChart + scrollXPxBefore) / currentZoom + axis.getRangeMin();

    // After zoom, the same time value should be at the same screen position.
    // New scrollXPx = (timeUnderMouse - rangeMin) * newZoom - mouseXInChart
    double newScrollXPx = (timeUnderMouse - axis.getRangeMin()) * newZoom - mouseXInChart;
    double newScrollX = newScrollXPx / newZoom;

    // Clamp scrollX.
    double virtualContentW = (axis.getRangeMax() - axis.getRangeMin()) * newZoom;
    double viewportContentW = gantt.getWidth() - colW;
    double maxScrollX = Math.max(0, (virtualContentW - viewportContentW) / newZoom);
    newScrollX = Math.max(0, Math.min(maxScrollX, newScrollX));

    // Apply.
    axis.setCurrentZoom(newZoom);
    gantt.setScrollX(newScrollX);

    // Re-layout and re-draw.
    relayout();

    // Sync changes to server.
    onChange();
}
```

- [ ] **Step 2: Verify clientToSvg is accessible**

The `clientToSvg` method is currently on `DragHandler`. If it's not accessible from `FlowDiagramClientControl`, add a similar method or make `DragHandler.clientToSvg` accessible. Check by looking for `clientToSvg` in the FlowDiagramClientControl class. If not present, add:

```java
private double[] clientToSvg(double clientX, double clientY) {
    return _dragHandler.clientToSvg(clientX, clientY);
}
```

Or if `DragHandler.clientToSvg` is private, make it package-private or add a forwarding method.

- [ ] **Step 3: Build**

```bash
mvn -B install -pl com.top_logic.react.flow.client 2>&1 | tee com.top_logic.react.flow.client/target/mvn-build.log
```

Expected: BUILD SUCCESS.

- [ ] **Step 4: Commit**

```bash
git add com.top_logic.react.flow.client/src/main/java/com/top_logic/react/flow/client/control/FlowDiagramClientControl.java
git commit -m "Ticket #29108: Implement Gantt zoom (Ctrl+wheel) changing pixelsPerUnit with cursor-centered scaling."
```

---

### Task 8: Build All & Verify with Playwright

**Files:**
- Build: all three flow modules + demo

- [ ] **Step 1: Full build**

```bash
mvn -B install -pl com.top_logic.react.flow.common,com.top_logic.react.flow.server,com.top_logic.react.flow.client,com.top_logic.demo 2>&1 | tee com.top_logic.demo/target/mvn-full-build.log
```

Expected: BUILD SUCCESS for all modules.

- [ ] **Step 2: Start the demo app**

Use the `tl-app` skill:

```
tl-app start com.top_logic.demo
```

- [ ] **Step 3: Navigate to Gantt demo**

Use Playwright to:
1. Navigate to the demo app URL
2. Login as root/root1234
3. Navigate to the Gantt Demo view (under the demo section)

- [ ] **Step 4: Verify frozen panes**

Visually verify:
1. The top 3 rows (Year, Months, Markers) are visible as a frozen header
2. Row labels are visible on the left
3. The corner area shows the axis row labels

- [ ] **Step 5: Verify horizontal scroll**

Use Playwright to simulate Shift+wheel or regular wheel (depending on which produces horizontal scroll). Verify:
1. The timeline content scrolls horizontally
2. The header (axis spans) scrolls in sync
3. The sidebar (row labels) does NOT scroll horizontally
4. The corner does NOT scroll

- [ ] **Step 6: Verify vertical scroll**

Simulate vertical mouse wheel. Verify:
1. Data rows scroll vertically
2. The sidebar scrolls in sync
3. The header does NOT scroll vertically
4. The corner does NOT scroll

- [ ] **Step 7: Verify zoom**

Simulate Ctrl+wheel. Verify:
1. The timeline stretches/compresses horizontally
2. Row heights do NOT change
3. Text sizes do NOT change
4. The zoom is centered on the mouse position

- [ ] **Step 8: Take a screenshot**

Save a screenshot to `com.top_logic.demo/target/gantt-viewport.png` for reference.

---

## Architecture Notes

### Scroll Units

- **scrollX**: Position units (not pixels). Pixel offset = `scrollX * currentZoom`. This ensures the viewport stays centered on the same time point when zooming.
- **scrollY**: Pixels. Row heights don't change with zoom, so pixel offset is zoom-independent.

### Scroll Bounds

- `maxScrollX = max(0, (virtualContentWidth - viewportContentWidth) / zoom)` where `virtualContentWidth = (rangeMax - rangeMin) * zoom` and `viewportContentWidth = self.getWidth() - columnWidth`
- `maxScrollY = max(0, dataHeight - viewportContentHeight)` where `viewportContentHeight = self.getHeight() - headerHeight`

### SVG Group Z-Order (back to front)

1. Decorations (grid lines, range bands) — behind everything
2. Content (row lanes, items, edges) — main content
3. Sidebar (row labels + opaque background) — covers scrolling content
4. Header (axis content + opaque background) — covers scrolling content
5. Corner (axis labels + opaque background) — covers everything

### Performance

Scrolling only updates SVG `transform` attributes on existing groups — no re-layout, no re-draw. This should be 60fps smooth.

Zooming requires a full re-layout and re-draw because item positions change. This is acceptable since zoom changes are infrequent.

### Fallback

When `frozenRows = 0`, the draw method falls back to the original flat rendering (no clipPaths, no groups). This preserves backward compatibility for Gantt charts that don't need a viewport.

### Future: LOD Integration

The LOD-Box (`LOD` / `LODLevel`) will read the zoom value from a new `RenderContext.getZoom()` field. The GanttLayout will set this via `context.withZoom(axis.getCurrentZoom())` before computing child layouts. This is a separate plan item and not part of this viewport work.
