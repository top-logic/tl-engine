# Dashboard Layout Control Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a responsive dashboard layout that packs tiles into a CSS Grid, supports per-tile `width` fractions and `row-span`, fills row-span gaps by vertically extending neighbors, provides drag-to-reorder in edit mode with a directional drop indicator, and persists user reordering via `PersonalConfiguration`.

**Architecture:**
- A new React control `ReactDashboardControl` in `com.top_logic.layout.react` holding arbitrary child `ReactControl` tiles. Each tile ships with stable id, width enum and row-span. The client-side layout engine (ported from the HTML mockup in `docs/superpowers/specs/2026-04-10-dashboard-layout-mockup.html`) computes CSS Grid placement. A `@ReactCommand("reorder")` persists the new order in `PersonalConfiguration` keyed by a stable dashboard id.
- A new view element pair `<dashboard id="..." min-col-width="...">` / `<tile id="..." width="..." row-span="...">` in `com.top_logic.layout.view.element` wraps that control, applies the persisted personal order at render time, and embeds arbitrary child `UIElement` instances as tile contents.

**Tech Stack:** Java 17 / TopLogic config framework, React via `tl-react-bridge`, Vite (triggered by `frontend-maven-plugin`), CSS Grid, `PersonalConfiguration` API, Playwright for manual verification.

---

## File Structure

### Create
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDashboardControl.java` — Java React control: state (min-col-width, children descriptors with id/width/row-span); `@ReactCommand("reorder")` handler.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/TileWidth.java` — enum `QUARTER`, `THIRD`, `HALF`, `TWO_THIRDS`, `FULL` with fractions and external names.
- `com.top_logic.layout.react/react-src/controls/TLDashboard.tsx` — React component: layout engine (pack + row-span gap fill), DnD, drop indicator, edit overlay.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java` — `@TagName("dashboard")` UI element, applies personal order, instantiates `ReactDashboardControl`.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TileElement.java` — `@TagName("tile")` UI element, one child `UIElement`, carries `id`/`width`/`row-span`.
- `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/dashboard-layout-demo.view.xml` — demo dashboard with mixed-size tiles.

### Modify
- `com.top_logic.layout.react/react-src/controls-entry.ts` — register `TLDashboard`.
- `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` — append dashboard + tile + drop-indicator + edit-overlay styles.
- `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml` — add a `<nav-item id="dashboard-layout-demo">` referencing the new view.

### Reference (read-only)
- `docs/superpowers/specs/2026-04-10-dashboard-layout-mockup.html` — canonical layout engine + drag behavior.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactStackControl.java` — pattern for container React control.
- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactSplitPanelControl.java` — pattern for `@ReactCommand` with arguments map.
- `com.top_logic.layout.react/react-src/controls/TLStack.tsx`, `TLSplitPanel.tsx` — TS patterns (`useTLState`, `useTLCommand`, `TLChild`).
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/StackElement.java` — UI element pattern.
- `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/ContainerElement.java` — children plumbing.
- `com.top_logic/src/main/java/com/top_logic/knowledge/wrap/person/PersonalConfiguration.java` — `getJSONValue`/`setJSONValue`.

---

## Task 1: TileWidth enum

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/TileWidth.java`

- [ ] **Step 1: Write the enum**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Relative width of a dashboard tile as a fraction of the available column count.
 *
 * <p>
 * The actual column span is {@code round(fraction * columns)}, capped to the
 * remaining free columns in the current row. Fractions are targets, not hard
 * constraints — the layout engine stretches the last tile per row to guarantee
 * gap-free rows.
 * </p>
 */
public enum TileWidth implements ExternallyNamed {

	/** Roughly 25% of available columns. */
	QUARTER("quarter", 0.25),

	/** Roughly 33% of available columns. */
	THIRD("third", 1.0 / 3.0),

	/** Roughly 50% of available columns. */
	HALF("half", 0.5),

	/** Roughly 67% of available columns. */
	TWO_THIRDS("two-thirds", 2.0 / 3.0),

	/** Full row. */
	FULL("full", 1.0);

	private final String _externalName;

	private final double _fraction;

	TileWidth(String externalName, double fraction) {
		_externalName = externalName;
		_fraction = fraction;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}

	/**
	 * The fraction of available columns (0, 1].
	 */
	public double getFraction() {
		return _fraction;
	}
}
```

- [ ] **Step 2: Verify module compiles**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tee com.top_logic.layout.react/target/mvn-build.log | tail -20`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/TileWidth.java
git commit -m "Ticket #29108: Add TileWidth enum for dashboard tile fractions."
```

---

## Task 2: ReactDashboardControl — skeleton + state

**Files:**
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDashboardControl.java`

- [ ] **Step 1: Write the Java control**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a responsive dashboard grid of
 * {@link Tile tiles} with drag-to-reorder support.
 *
 * <p>
 * The client-side layout engine packs tiles left-to-right, stretches the last
 * tile in each row to remove horizontal gaps and extends neighbors vertically
 * to fill gaps left by row-spanning tiles.
 * </p>
 */
public class ReactDashboardControl extends ReactControl {

	private static final String REACT_MODULE = "TLDashboard";

	private static final String MIN_COL_WIDTH = "minColWidth";

	private static final String CHILDREN = "children";

	private static final String ORDER_ARG = "order";

	/**
	 * A single tile of the dashboard.
	 */
	public static final class Tile {

		private final String _id;

		private final TileWidth _width;

		private final int _rowSpan;

		private final ReactControl _control;

		/**
		 * Creates a new {@link Tile}.
		 *
		 * @param id
		 *        Stable tile id, used as persistence key for reordering.
		 * @param width
		 *        The tile's relative width.
		 * @param rowSpan
		 *        Number of rows the tile occupies (>= 1).
		 * @param control
		 *        The control rendered inside the tile.
		 */
		public Tile(String id, TileWidth width, int rowSpan, ReactControl control) {
			_id = id;
			_width = width;
			_rowSpan = Math.max(1, rowSpan);
			_control = control;
		}

		/** The stable id of this tile. */
		public String getId() {
			return _id;
		}

		/** The inner control. */
		public ReactControl getControl() {
			return _control;
		}
	}

	private final List<Tile> _tiles;

	private final Consumer<List<String>> _onReorder;

	/**
	 * Creates a new {@link ReactDashboardControl}.
	 *
	 * @param minColWidth
	 *        CSS length used to decide column count (e.g. {@code "16rem"}).
	 * @param tiles
	 *        Tiles in the order they should appear initially.
	 * @param onReorder
	 *        Callback invoked with the new tile-id order after the user
	 *        reordered tiles. May be {@code null}.
	 */
	public ReactDashboardControl(ReactContext context, String minColWidth, List<Tile> tiles,
			Consumer<List<String>> onReorder) {
		super(context, null, REACT_MODULE);
		_tiles = new ArrayList<>(tiles);
		_onReorder = onReorder;
		putState(MIN_COL_WIDTH, minColWidth);
		putState(CHILDREN, buildDescriptors());
	}

	private List<Map<String, Object>> buildDescriptors() {
		List<Map<String, Object>> list = new ArrayList<>(_tiles.size());
		for (Tile t : _tiles) {
			Map<String, Object> d = new LinkedHashMap<>();
			d.put("id", t._id);
			d.put("width", t._width.getExternalName());
			d.put("rowSpan", Integer.valueOf(t._rowSpan));
			d.put("control", t._control);
			list.add(d);
		}
		return list;
	}

	@Override
	protected void cleanupChildren() {
		for (Tile t : _tiles) {
			t._control.cleanupTree();
		}
	}

	/**
	 * Handles the {@code reorder} command sent by the React client after a
	 * drag-and-drop reorder.
	 */
	@SuppressWarnings("unchecked")
	@ReactCommand("reorder")
	@FrameworkInternal
	void handleReorder(Map<String, Object> arguments) {
		Object raw = arguments.get(ORDER_ARG);
		if (!(raw instanceof List)) {
			return;
		}
		List<String> newOrder = (List<String>) raw;
		reorderTiles(newOrder);
		putState(CHILDREN, buildDescriptors());
		if (_onReorder != null) {
			_onReorder.accept(Collections.unmodifiableList(new ArrayList<>(newOrder)));
		}
	}

	private void reorderTiles(List<String> newOrder) {
		Map<String, Tile> byId = new LinkedHashMap<>();
		for (Tile t : _tiles) {
			byId.put(t._id, t);
		}
		List<Tile> reordered = new ArrayList<>(_tiles.size());
		for (String id : newOrder) {
			Tile t = byId.remove(id);
			if (t != null) {
				reordered.add(t);
			}
		}
		// Append any tiles that weren't mentioned in the new order.
		reordered.addAll(byId.values());
		_tiles.clear();
		_tiles.addAll(reordered);
	}
}
```

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.react compile 2>&1 | tee com.top_logic.layout.react/target/mvn-build.log | tail -20`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactDashboardControl.java
git commit -m "Ticket #29108: Add ReactDashboardControl with reorder command."
```

---

## Task 3: TLDashboard.tsx — React component with layout engine and drag-drop

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLDashboard.tsx`

- [ ] **Step 1: Write the component**

```tsx
import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useMemo, useRef, useState } = React;

interface TileDescriptor {
  id: string;
  width: 'quarter' | 'third' | 'half' | 'two-thirds' | 'full';
  rowSpan: number;
  control: unknown;
}

interface Placement {
  id: string;
  colStart: number;
  colEnd: number;
  rowStart: number;
  rowEnd: number;
}

const WIDTH_FRACTION: Record<TileDescriptor['width'], number> = {
  'quarter': 0.25,
  'third': 1 / 3,
  'half': 0.5,
  'two-thirds': 2 / 3,
  'full': 1.0,
};

const COL_BREAKPOINTS = [1, 2, 3, 4];

function parseLengthPx(value: string, fontPx: number): number {
  const match = /^([\d.]+)(rem|em|px)?$/.exec(value.trim());
  if (!match) return 16 * fontPx;
  const num = parseFloat(match[1]);
  const unit = match[2] || 'px';
  if (unit === 'rem' || unit === 'em') return num * fontPx;
  return num;
}

function calcMaxColumns(containerWidth: number, minColPx: number): number {
  const raw = Math.max(1, Math.floor(containerWidth / minColPx));
  // Snap to nice numbers so items don't flicker between column counts.
  let snapped = 1;
  for (const b of COL_BREAKPOINTS) {
    if (raw >= b) snapped = b;
  }
  return snapped;
}

function itemSpan(width: TileDescriptor['width'], cols: number): number {
  const frac = WIDTH_FRACTION[width] ?? 1.0;
  return Math.max(1, Math.round(frac * cols));
}

function computeLayout(tiles: TileDescriptor[], cols: number): Placement[] {
  const safeCols = Math.max(1, cols);
  const occupied: Record<number, Record<number, boolean>> = {};
  const isOccupied = (r: number, c: number) => !!(occupied[r] && occupied[r][c]);
  const mark = (r: number, c: number) => {
    if (!occupied[r]) occupied[r] = {};
    occupied[r][c] = true;
  };

  const placements: Placement[] = [];
  let curRow = 0;
  let curCol = 0;

  const stretchLastInRow = (row: number) => {
    let last: Placement | null = null;
    for (const p of placements) if (p.rowStart === row) last = p;
    if (!last) return;
    let newEnd = last.colEnd;
    while (newEnd < safeCols && !isOccupied(row, newEnd)) newEnd++;
    if (newEnd !== last.colEnd) {
      for (let r = last.rowStart; r < last.rowEnd; r++) {
        for (let c = last.colEnd; c < newEnd; c++) mark(r, c);
      }
      last.colEnd = newEnd;
    }
  };

  for (const t of tiles) {
    const rowSpan = safeCols <= 1 ? 1 : Math.max(1, t.rowSpan || 1);
    let span = Math.min(itemSpan(t.width, safeCols), safeCols);

    while (isOccupied(curRow, curCol)) {
      curCol++;
      if (curCol >= safeCols) { curCol = 0; curRow++; }
    }

    let freeInRow = 0;
    for (let c = curCol; c < safeCols; c++) {
      if (!isOccupied(curRow, c)) freeInRow++;
      else break;
    }

    if (span > freeInRow) {
      stretchLastInRow(curRow);
      curCol = 0;
      curRow++;
      while (isOccupied(curRow, curCol)) {
        curCol++;
        if (curCol >= safeCols) { curCol = 0; curRow++; }
      }
      freeInRow = 0;
      for (let c = curCol; c < safeCols; c++) {
        if (!isOccupied(curRow, c)) freeInRow++;
        else break;
      }
      span = Math.min(span, freeInRow);
    }

    const colStart = curCol;
    const colEnd = curCol + span;
    const rowStart = curRow;
    const rowEnd = curRow + rowSpan;
    placements.push({ id: t.id, colStart, colEnd, rowStart, rowEnd });
    for (let r = rowStart; r < rowEnd; r++) {
      for (let c = colStart; c < colEnd; c++) mark(r, c);
    }
    curCol = colEnd;
    if (curCol >= safeCols) { curCol = 0; curRow++; }
  }

  stretchLastInRow(curRow);

  // Fill row-span gaps: extend neighbors downward.
  let maxRow = 0;
  for (const p of placements) if (p.rowEnd > maxRow) maxRow = p.rowEnd;
  for (let r = 1; r < maxRow; r++) {
    for (let c = 0; c < safeCols; c++) {
      if (isOccupied(r, c)) continue;
      const above = placements.find(p => p.rowEnd === r && p.colStart <= c && c < p.colEnd);
      if (!above) continue;
      above.rowEnd = r + 1;
      for (let cc = above.colStart; cc < above.colEnd; cc++) mark(r, cc);
    }
  }

  return placements;
}

const TLDashboard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const minColWidth = (state.minColWidth as string) ?? '16rem';
  const tiles = ((state.children as TileDescriptor[]) ?? []).filter(t => t && t.id);

  const containerRef = useRef<HTMLDivElement>(null);
  const [cols, setCols] = useState<number>(1);
  const [editMode, setEditMode] = useState<boolean>(false);

  // Recompute column count on resize.
  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    const fontPx = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
    const minPx = parseLengthPx(minColWidth, fontPx);
    const update = () => setCols(calcMaxColumns(el.clientWidth, minPx));
    update();
    const ro = new ResizeObserver(update);
    ro.observe(el);
    return () => ro.disconnect();
  }, [minColWidth]);

  const placements = useMemo(() => computeLayout(tiles, cols), [tiles, cols]);
  const placementById = useMemo(() => {
    const m: Record<string, Placement> = {};
    for (const p of placements) m[p.id] = p;
    return m;
  }, [placements]);

  // --- Drag and drop (edit mode only) ---
  const [draggedId, setDraggedId] = useState<string | null>(null);
  const [dropTarget, setDropTarget] = useState<{ id: string; before: boolean } | null>(null);

  const onDragStart = useCallback((e: React.DragEvent<HTMLDivElement>, id: string) => {
    if (!editMode) { e.preventDefault(); return; }
    setDraggedId(id);
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', id);
  }, [editMode]);

  const onDragOver = useCallback((e: React.DragEvent<HTMLDivElement>, id: string) => {
    if (!editMode || !draggedId || draggedId === id) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect();
    const before = e.clientX < rect.left + rect.width / 2;
    setDropTarget(prev => (prev && prev.id === id && prev.before === before) ? prev : { id, before });
  }, [editMode, draggedId]);

  const onDragLeave = useCallback(() => {
    // Cleared on drop/dragend; leaving a single tile shouldn't clear.
  }, []);

  const commitReorder = useCallback((draggedTileId: string, targetId: string, before: boolean) => {
    const ids = tiles.map(t => t.id);
    const from = ids.indexOf(draggedTileId);
    if (from < 0) return;
    ids.splice(from, 1);
    const toBase = ids.indexOf(targetId);
    if (toBase < 0) { ids.splice(from, 0, draggedTileId); return; }
    const insertAt = before ? toBase : toBase + 1;
    ids.splice(insertAt, 0, draggedTileId);
    sendCommand('reorder', { order: ids });
  }, [tiles, sendCommand]);

  const onDrop = useCallback((e: React.DragEvent<HTMLDivElement>, id: string) => {
    if (!editMode || !draggedId || draggedId === id) return;
    e.preventDefault();
    const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect();
    const before = e.clientX < rect.left + rect.width / 2;
    commitReorder(draggedId, id, before);
    setDraggedId(null);
    setDropTarget(null);
  }, [editMode, draggedId, commitReorder]);

  const onDragEnd = useCallback(() => {
    setDraggedId(null);
    setDropTarget(null);
  }, []);

  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gap: '1rem',
  };

  return (
    <div
      id={controlId}
      ref={containerRef}
      className={'tlDashboard' + (editMode ? ' tlDashboard--edit' : '')}
    >
      <div className="tlDashboard__toolbar">
        <button
          type="button"
          className={'tlDashboard__editBtn' + (editMode ? ' tlDashboard__editBtn--active' : '')}
          onClick={() => setEditMode(v => !v)}
        >
          {editMode ? 'Done' : 'Edit Layout'}
        </button>
      </div>
      <div className="tlDashboard__grid" style={gridStyle}>
        {tiles.map(tile => {
          const p = placementById[tile.id];
          if (!p) return null;
          const style: React.CSSProperties = {
            gridColumn: `${p.colStart + 1} / ${p.colEnd + 1}`,
            gridRow: `${p.rowStart + 1} / ${p.rowEnd + 1}`,
          };
          const classes = ['tlDashboard__tile'];
          if (draggedId === tile.id) classes.push('tlDashboard__tile--dragging');
          if (dropTarget && dropTarget.id === tile.id) {
            classes.push(dropTarget.before ? 'tlDashboard__tile--dropBefore' : 'tlDashboard__tile--dropAfter');
          }
          return (
            <div
              key={tile.id}
              className={classes.join(' ')}
              style={style}
              draggable={editMode}
              onDragStart={e => onDragStart(e, tile.id)}
              onDragOver={e => onDragOver(e, tile.id)}
              onDragLeave={onDragLeave}
              onDrop={e => onDrop(e, tile.id)}
              onDragEnd={onDragEnd}
            >
              <TLChild control={tile.control} />
              {editMode && <div className="tlDashboard__overlay" />}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default TLDashboard;
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLDashboard.tsx
git commit -m "Ticket #29108: Add TLDashboard React component with layout engine."
```

---

## Task 4: Register TLDashboard in controls bundle entry

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts`

- [ ] **Step 1: Add import and registration**

Add an import line in the import block (after the other control imports, e.g. after `import TLIconSelect from './controls/TLIconSelect';`):

```ts
import TLDashboard from './controls/TLDashboard';
```

Add a registration line at the end of the `register(...)` block:

```ts
register('TLDashboard', TLDashboard);
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls-entry.ts
git commit -m "Ticket #29108: Register TLDashboard control."
```

---

## Task 5: Dashboard CSS

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

- [ ] **Step 1: Append styles**

Append the following block at the end of `tlReactControls.css`:

```css
/* --- Dashboard --- */
.tlDashboard {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
  box-sizing: border-box;
}
.tlDashboard__toolbar {
  display: flex;
  justify-content: flex-end;
}
.tlDashboard__editBtn {
  font: inherit;
  padding: 0.35rem 0.75rem;
  border: 1px solid var(--color-border, #c0c4cc);
  background: var(--color-surface, #fff);
  border-radius: 4px;
  cursor: pointer;
}
.tlDashboard__editBtn--active {
  background: var(--layer-selected, #e6f0ff);
  border-color: var(--layer-selected-hover, #3380ff);
}
.tlDashboard__grid {
  width: 100%;
}
.tlDashboard__tile {
  position: relative;
  min-width: 0;
  min-height: 0;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  border: 1px solid var(--color-border, #dde1e7);
  border-radius: 6px;
  background: var(--color-surface, #fff);
}
.tlDashboard__tile > *:not(.tlDashboard__overlay) {
  flex: 1 1 auto;
  min-height: 0;
  min-width: 0;
}
.tlDashboard--edit .tlDashboard__tile { cursor: move; }
.tlDashboard__tile--dragging { opacity: 0.4; }
.tlDashboard__overlay {
  position: absolute;
  inset: 0;
  background: transparent;
  cursor: move;
  z-index: 5;
}
.tlDashboard__tile--dropBefore::before,
.tlDashboard__tile--dropAfter::after {
  content: "";
  position: absolute;
  top: -6px;
  bottom: -6px;
  width: 8px;
  background: var(--color-drop-zone, #3380ff);
  border-radius: 4px;
  box-shadow: 0 0 12px 2px var(--color-drop-zone, #3380ff),
              0 0 24px 4px var(--color-drop-zone, #3380ff);
  pointer-events: none;
  z-index: 10;
}
.tlDashboard__tile--dropBefore::before { left: -10px; }
.tlDashboard__tile--dropAfter::after { right: -10px; }
```

- [ ] **Step 2: Build tl-react-controls**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.layout.react 2>&1 | tee com.top_logic.layout.react/target/mvn-build.log | tail -20`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css com.top_logic.layout.react/src/main/webapp/script/tl-react-controls.js
git commit -m "Ticket #29108: Add dashboard tile styles and rebuild controls bundle."
```

Note: the `.js` artifact is checked in because `frontend-maven-plugin` writes it under `src/main/webapp`. If the repo gitignores it, leave it out.

---

## Task 6: TileElement — view element for a single tile

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TileElement.java`

- [ ] **Step 1: Write the class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.TileWidth;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * A single tile in a {@link DashboardElement dashboard}.
 *
 * <p>
 * Exactly one inner {@link UIElement} is rendered inside the tile. The tile
 * carries layout metadata: a stable {@link Config#getId() id} (used as
 * persistence key for reordering), a relative {@link Config#getWidth() width}
 * fraction and an optional {@link Config#getRowSpan() row span}.
 * </p>
 */
public class TileElement implements UIElement {

	/**
	 * Configuration for {@link TileElement}.
	 */
	@TagName("tile")
	public interface Config extends UIElement.Config {

		/** Config property name for {@link #getId()}. */
		String ID = "id";

		/** Config property name for {@link #getWidth()}. */
		String WIDTH = "width";

		/** Config property name for {@link #getRowSpan()}. */
		String ROW_SPAN = "row-span";

		/** Config property name for {@link #getContent()}. */
		String CONTENT = "content";

		@Override
		@ClassDefault(TileElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id identifying this tile. Used as the persistence key when
		 * users reorder tiles via drag-and-drop.
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * The tile's relative width as a fraction of available columns.
		 */
		@Name(WIDTH)
		@Mandatory
		TileWidth getWidth();

		/**
		 * The number of grid rows this tile spans. Defaults to 1.
		 */
		@Name(ROW_SPAN)
		@IntDefault(1)
		int getRowSpan();

		/**
		 * The {@link UIElement} rendered inside this tile.
		 */
		@Name(CONTENT)
		@DefaultContainer
		PolymorphicConfiguration<? extends UIElement> getContent();
	}

	private final String _id;

	private final TileWidth _width;

	private final int _rowSpan;

	private final UIElement _content;

	/**
	 * Creates a new {@link TileElement} from configuration.
	 */
	@CalledByReflection
	public TileElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_width = config.getWidth();
		_rowSpan = Math.max(1, config.getRowSpan());
		_content = context.getInstance(config.getContent());
	}

	/** The stable tile id. */
	public String getId() {
		return _id;
	}

	/** The tile's relative width. */
	public TileWidth getWidth() {
		return _width;
	}

	/** The tile's row span. */
	public int getRowSpan() {
		return _rowSpan;
	}

	/**
	 * Creates the inner control of this tile.
	 */
	public ReactControl createContentControl(ViewContext context) {
		IReactControl inner = _content.createControl(context);
		return (ReactControl) inner;
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		// Tiles are rendered by DashboardElement; direct rendering is unsupported.
		throw new UnsupportedOperationException("<tile> must be a child of <dashboard>.");
	}
}
```

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log | tail -20`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/TileElement.java
git commit -m "Ticket #29108: Add TileElement view element."
```

---

## Task 7: DashboardElement — view element with personal order persistence

**Files:**
- Create: `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java`

- [ ] **Step 1: Write the class**

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl;
import com.top_logic.layout.react.control.layout.ReactDashboardControl.Tile;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * A UI element that renders a responsive dashboard grid of {@link TileElement
 * tiles}.
 *
 * <p>
 * Tile order is personalized per user via {@link PersonalConfiguration}, keyed
 * by {@link Config#getId() this element's id}. Unknown ids in the persisted
 * order are ignored; tiles not mentioned are appended in their configured
 * order.
 * </p>
 */
public class DashboardElement implements UIElement {

	private static final String PC_KEY_PREFIX = "dashboard.tileOrder.";

	/**
	 * Configuration for {@link DashboardElement}.
	 */
	@TagName("dashboard")
	public interface Config extends UIElement.Config {

		/** Config property name for {@link #getId()}. */
		String ID = "id";

		/** Config property name for {@link #getMinColWidth()}. */
		String MIN_COL_WIDTH = "min-col-width";

		/** Config property name for {@link #getTiles()}. */
		String TILES = "tiles";

		@Override
		@ClassDefault(DashboardElement.class)
		Class<? extends UIElement> getImplementationClass();

		/**
		 * Stable id used as persistence key for personal tile ordering.
		 */
		@Name(ID)
		@Mandatory
		String getId();

		/**
		 * CSS length hint used by the client to decide the column count.
		 * Defaults to {@code 16rem}.
		 */
		@Name(MIN_COL_WIDTH)
		@StringDefault("16rem")
		String getMinColWidth();

		/**
		 * The tiles. Represented as polymorphic configurations so that the
		 * default container (child elements) can hold {@code <tile>} entries.
		 */
		@Name(TILES)
		@com.top_logic.basic.config.annotation.DefaultContainer
		List<PolymorphicConfiguration<? extends TileElement>> getTiles();
	}

	private final String _id;

	private final String _minColWidth;

	private final List<TileElement> _tiles;

	/**
	 * Creates a new {@link DashboardElement} from configuration.
	 */
	@CalledByReflection
	public DashboardElement(InstantiationContext context, Config config) {
		_id = config.getId();
		_minColWidth = config.getMinColWidth();
		_tiles = new ArrayList<>();
		for (PolymorphicConfiguration<? extends TileElement> tc : config.getTiles()) {
			TileElement tile = context.getInstance(tc);
			if (tile != null) {
				_tiles.add(tile);
			}
		}
	}

	@Override
	public IReactControl createControl(ViewContext context) {
		List<TileElement> ordered = applyPersonalOrder(_tiles);
		List<Tile> reactTiles = new ArrayList<>(ordered.size());
		for (TileElement t : ordered) {
			reactTiles.add(new Tile(t.getId(), t.getWidth(), t.getRowSpan(), t.createContentControl(context)));
		}
		return new ReactDashboardControl(context, _minColWidth, reactTiles, this::storePersonalOrder);
	}

	private List<TileElement> applyPersonalOrder(List<TileElement> tiles) {
		List<String> personal = readPersonalOrder();
		if (personal == null || personal.isEmpty()) {
			return tiles;
		}
		Map<String, TileElement> byId = new LinkedHashMap<>();
		for (TileElement t : tiles) {
			byId.put(t.getId(), t);
		}
		List<TileElement> result = new ArrayList<>(tiles.size());
		for (String id : personal) {
			TileElement t = byId.remove(id);
			if (t != null) {
				result.add(t);
			}
		}
		result.addAll(byId.values());
		return result;
	}

	@SuppressWarnings("unchecked")
	private List<String> readPersonalOrder() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(PC_KEY_PREFIX + _id);
		if (value instanceof List) {
			List<?> raw = (List<?>) value;
			List<String> result = new ArrayList<>(raw.size());
			for (Object o : raw) {
				if (o instanceof String) {
					result.add((String) o);
				}
			}
			return result;
		}
		return null;
	}

	private void storePersonalOrder(List<String> order) {
		try {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc == null) {
				return;
			}
			pc.setJSONValue(PC_KEY_PREFIX + _id, order);
			PersonalConfiguration.storePersonalConfiguration();
		} catch (RuntimeException ex) {
			Logger.warn("Failed to persist dashboard tile order for '" + _id + "'.", ex, DashboardElement.class);
		}
	}
}
```

- [ ] **Step 2: Verify compile**

Run: `mvn -B -pl com.top_logic.layout.view compile 2>&1 | tee com.top_logic.layout.view/target/mvn-build.log | tail -20`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/DashboardElement.java
git commit -m "Ticket #29108: Add DashboardElement with personal tile order persistence."
```

---

## Task 8: Build both modules end-to-end

- [ ] **Step 1: Full install**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.layout.react,com.top_logic.layout.view -am 2>&1 | tee /tmp/dashboard-full-build.log | tail -20`
Expected: `BUILD SUCCESS`.

---

## Task 9: Demo view

**Files:**
- Create: `com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/dashboard-layout-demo.view.xml`

- [ ] **Step 1: Write the view**

```xml
<?xml version="1.0" encoding="iso-8859-1" ?>

<view>
	<dashboard id="dashboard-layout-demo" min-col-width="16rem">
		<tile id="sales" width="half" row-span="2">
			<card variant="elevated">
				<title>
					<en>Sales</en>
					<de>Ums&#xE4;tze</de>
				</title>
				<demo-counter>
					<label>
						<en>Sales</en>
						<de>Ums&#xE4;tze</de>
					</label>
				</demo-counter>
			</card>
		</tile>
		<tile id="tasks" width="quarter">
			<card>
				<title>
					<en>Tasks</en>
					<de>Aufgaben</de>
				</title>
				<demo-counter>
					<label>
						<en>Tasks</en>
						<de>Aufgaben</de>
					</label>
				</demo-counter>
			</card>
		</tile>
		<tile id="issues" width="quarter">
			<card>
				<title>
					<en>Issues</en>
					<de>Probleme</de>
				</title>
				<demo-counter>
					<label>
						<en>Issues</en>
						<de>Probleme</de>
					</label>
				</demo-counter>
			</card>
		</tile>
		<tile id="reviews" width="half">
			<card>
				<title>
					<en>Reviews</en>
					<de>Reviews</de>
				</title>
				<demo-counter>
					<label>
						<en>Reviews</en>
						<de>Reviews</de>
					</label>
				</demo-counter>
			</card>
		</tile>
		<tile id="timeline" width="two-thirds">
			<card>
				<title>
					<en>Timeline</en>
					<de>Verlauf</de>
				</title>
				<demo-counter>
					<label>
						<en>Events</en>
						<de>Ereignisse</de>
					</label>
				</demo-counter>
			</card>
		</tile>
		<tile id="distribution" width="third">
			<card>
				<title>
					<en>Distribution</en>
					<de>Verteilung</de>
				</title>
				<demo-counter>
					<label>
						<en>Segments</en>
						<de>Segmente</de>
					</label>
				</demo-counter>
			</card>
		</tile>
		<tile id="activity" width="full">
			<card>
				<title>
					<en>Activity</en>
					<de>Aktivit&#xE4;t</de>
				</title>
				<demo-counter>
					<label>
						<en>Events</en>
						<de>Ereignisse</de>
					</label>
				</demo-counter>
			</card>
		</tile>
	</dashboard>
</view>
```

- [ ] **Step 2: Commit**

```bash
git add com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/dashboard-layout-demo.view.xml
git commit -m "Ticket #29108: Add dashboard-layout demo view."
```

---

## Task 10: Wire demo into navigation

**Files:**
- Modify: `com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml`

- [ ] **Step 1: Insert nav-item**

Immediately after the existing `<nav-item id="dashboard" ...>...</nav-item>` block (which ends around the line containing `<view-ref view="demo/dashboard.view.xml"/>` followed by `</nav-item>`), insert:

```xml
<nav-item id="dashboard-layout-demo"
	icon="bi bi-grid-1x2"
>
	<label>
		<en>Dashboard Layout</en>
		<de>Dashboard-Layout</de>
	</label>
	<view-ref view="demo/dashboard-layout-demo.view.xml"/>
</nav-item>
```

- [ ] **Step 2: Build demo app**

Run: `mvn -B install -DskipTests=true -pl com.top_logic.demo -am 2>&1 | tee /tmp/dashboard-demo-build.log | tail -20`
Expected: `BUILD SUCCESS`.

- [ ] **Step 3: Commit**

```bash
git add com.top_logic.demo/src/main/webapp/WEB-INF/views/app.view.xml
git commit -m "Ticket #29108: Add Dashboard Layout nav-item to demo app."
```

---

## Task 11: Manual verification with Playwright

- [ ] **Step 1: Start the demo app**

Use the `tl-app` skill: start `com.top_logic.demo`.

- [ ] **Step 2: Navigate and verify initial render**

Using `mcp__playwright__browser_navigate` to the demo app URL, log in as `root`/`root1234`, click the "Dashboard Layout" sidebar item. Take a screenshot into `com.top_logic.demo/target/dashboard-01-initial.png`.

Expected: the 7 tiles render in a responsive grid; no horizontal gaps; the row-span tile `sales` occupies two rows and has no gap to its right in the second row.

- [ ] **Step 3: Verify drag-and-drop**

Click "Edit Layout". Drag tile `activity` before tile `sales`:
- `mcp__playwright__browser_drag` from `activity` to `sales` (cursor on the left half).
- Screenshot into `com.top_logic.demo/target/dashboard-02-after-reorder.png`.

Expected: `activity` now renders first; drop indicator appeared on the left edge of `sales` during drag.

- [ ] **Step 4: Verify persistence across reload**

Reload the page (`mcp__playwright__browser_navigate` to the same URL). Click "Dashboard Layout" again.

Expected: the new order (`activity` first) survives the reload. Screenshot into `com.top_logic.demo/target/dashboard-03-after-reload.png`.

- [ ] **Step 5: Verify row-span gap fill**

Resize the browser window to a width that yields 4 columns, then 3 columns, then 2 columns (use `mcp__playwright__browser_resize`). At each size, take a screenshot into `com.top_logic.demo/target/dashboard-04-{cols}.png` and verify no gaps exist anywhere in the grid.

Expected: no visible gaps at any column count.

- [ ] **Step 6: Clean up temporary artifacts**

The PNG screenshots under `com.top_logic.demo/target/` are not committed (build output directory). Leave them there for reference; they're excluded by gitignore.

- [ ] **Step 7: Final commit (if anything changed)**

If the verification surfaced any fixes, commit them with a message like:

```bash
git commit -m "Ticket #29108: Fix <specific issue found during verification>."
```

If no changes were needed, skip this step.

---

## Notes on rigor

- **Messages generation:** `DashboardElement.Config` and `TileElement.Config` add new properties. Both interfaces carry `@TagName` so they get registered automatically, but `messages_en.properties` / `messages_de.properties` under `com.top_logic.layout.view/src/main/java/META-INF/` will be regenerated by `mvn install`. Run the full install (Task 8) twice if label lookups come back as missing. Add `@Label` annotations or JavaDoc `@en` comments on any property whose auto-generated label is inadequate; regenerate and commit the resulting `.properties` deltas.
- **Encoding:** All Java sources must be ISO-8859-1 (project default). Non-ASCII German strings in the demo view use numeric entities (`&#xE4;` etc.) as shown.
- **No direct `npx vite build`:** The React bundle is built by `frontend-maven-plugin` during `mvn install` on `com.top_logic.layout.react`. Never call Vite manually.
- **React import discipline:** `TLDashboard.tsx` imports only from `tl-react-bridge`, never from `react`. Violating this breaks hooks at runtime.
