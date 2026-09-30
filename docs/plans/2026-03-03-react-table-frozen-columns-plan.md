# React Table Frozen Columns - Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add left-side frozen (sticky) columns to the React table so key columns remain visible during horizontal scrolling.

**Architecture:** The server sends `frozenColumnCount` as state. The client applies `position: sticky; left: <offset>` to frozen header and body cells via inline styles and CSS classes. The checkbox column is automatically frozen when `frozenColumnCount > 0`. A box-shadow on the last frozen column provides visual separation.

**Tech Stack:** Java 17, React via tl-react-bridge, CSS sticky positioning, Maven

---

## File Overview

### Modified Files

**Java (com.top_logic.layout.react):**
- `src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — Add `_frozenColumnCount` field, setter, and state key

**TypeScript (com.top_logic.layout.react):**
- `react-src/controls/TLTableView.tsx` — Compute sticky offsets, apply frozen styles

**CSS (com.top_logic.layout.react):**
- `src/main/webapp/style/tlReactControls.css` — Add frozen cell styles

**Java (com.top_logic.demo):**
- `src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java` — Set `frozenColumnCount = 2`

---

## Task 1: Server-Side — Add frozenColumnCount to ReactTableControl

**Purpose:** Add the `frozenColumnCount` field and pass it to the client as state.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add the state key constant**

After the existing `SELECTED_COUNT` constant (line 56), add:

```java
private static final String FROZEN_COLUMN_COUNT = "frozenColumnCount";
```

**Step 2: Add the field**

After the `_anchorAdded` field (line 101), add:

```java
/** Number of columns frozen on the left side. */
private int _frozenColumnCount;
```

**Step 3: Add the setter**

After the `setSelectionForced` method (around line 149), add:

```java
/**
 * Sets the number of columns frozen on the left side.
 *
 * <p>
 * The first {@code count} columns in the column list remain visible during horizontal
 * scrolling. In multi-select mode, the checkbox column is automatically frozen when
 * {@code count > 0}.
 * </p>
 *
 * @param count
 *        The number of frozen columns, or 0 for no frozen columns.
 */
public void setFrozenColumnCount(int count) {
	_frozenColumnCount = count;
	putState(FROZEN_COLUMN_COUNT, Integer.valueOf(count));
}
```

**Step 4: Send initial state in constructor**

In the constructor, after `putState(SELECTION_MODE, _selectionMode);` (line 129), add:

```java
putState(FROZEN_COLUMN_COUNT, Integer.valueOf(0));
```

**Step 5: Commit**

```
Ticket #29109: Add frozenColumnCount state to ReactTableControl.
```

---

## Task 2: Client-Side — Apply Sticky Positioning to Frozen Columns

**Purpose:** Read `frozenColumnCount` from state and apply `position: sticky` with computed `left` offsets to frozen header and body cells.

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLTableView.tsx`

**Step 1: Read frozenColumnCount from state**

After `const selectedCount = ...` (line 34), add:

```typescript
const frozenColumnCount = (state.frozenColumnCount as number) ?? 0;
```

**Step 2: Create a helper to compute frozen column left offsets**

After the `const isMulti = ...` line (line 36), add:

```typescript
// Compute cumulative left offsets for frozen columns.
// If multi-select, checkbox column is at left:0 and frozen columns start after it.
const frozenOffsets = React.useMemo(() => {
  const offsets: number[] = [];
  let left = isMulti && frozenColumnCount > 0 ? checkboxWidth : 0;
  for (let i = 0; i < frozenColumnCount && i < columns.length; i++) {
    offsets.push(left);
    left += getColWidth(columns[i]);
  }
  return offsets;
}, [columns, frozenColumnCount, isMulti, checkboxWidth, columnWidthOverrides]);
```

Note: `getColWidth` is defined later in the current code. Move the `getColWidth` function definition **above** this `useMemo` (currently at line 59). The function itself doesn't change:

```typescript
const getColWidth = React.useCallback((col: ColumnState): number => {
  return columnWidthOverrides[col.name] ?? col.width;
}, [columnWidthOverrides]);
```

Replace the existing plain function `getColWidth` (lines 59-61) with this `useCallback` version, placed before the `frozenOffsets` memo.

**Step 3: Apply sticky styles to the checkbox column in the header**

In the header's checkbox cell `<div>` (around line 243), add frozen styles when `frozenColumnCount > 0`:

Replace:
```typescript
<div className="tlTableView__headerCell tlTableView__checkboxCell"
  style={{ width: checkboxWidth, minWidth: checkboxWidth }}
```

With:
```typescript
<div className={'tlTableView__headerCell tlTableView__checkboxCell'
    + (frozenColumnCount > 0 ? ' tlTableView__headerCell--frozen' : '')}
  style={{
    width: checkboxWidth, minWidth: checkboxWidth,
    ...(frozenColumnCount > 0 ? { position: 'sticky' as const, left: 0, zIndex: 2 } : {}),
  }}
```

**Step 4: Apply sticky styles to frozen header cells**

In the header column map (around line 264), update the style computation. Currently the code computes `cellClass` and `style` per column. Add frozen logic:

After `if (dragOver && dragOver.column === col.name) {` block (around line 270), add:

```typescript
const isFrozen = colIdx < frozenColumnCount;
const isFrozenLast = colIdx === frozenColumnCount - 1;
if (isFrozen) cellClass += ' tlTableView__headerCell--frozen';
if (isFrozenLast) cellClass += ' tlTableView__headerCell--frozenLast';
```

Update the style on the header cell `<div>`. Replace the current style computation:

```typescript
style={isLast
  ? { flex: '1 0 auto', minWidth: w, position: 'relative' }
  : { width: w, minWidth: w, position: 'relative' }}
```

With:

```typescript
style={{
  ...(isLast && !isFrozen
    ? { flex: '1 0 auto', minWidth: w }
    : { width: w, minWidth: w }),
  position: isFrozen ? 'sticky' as const : 'relative' as const,
  ...(isFrozen ? { left: frozenOffsets[colIdx], zIndex: 2 } : {}),
}}
```

**Step 5: Apply sticky styles to the checkbox column in body rows**

In the body row checkbox cell (around line 343), update similarly:

Replace:
```typescript
<div className="tlTableView__cell tlTableView__checkboxCell"
  style={{ width: checkboxWidth, minWidth: checkboxWidth }}
```

With:
```typescript
<div className={'tlTableView__cell tlTableView__checkboxCell'
    + (frozenColumnCount > 0 ? ' tlTableView__cell--frozen' : '')}
  style={{
    width: checkboxWidth, minWidth: checkboxWidth,
    ...(frozenColumnCount > 0 ? { position: 'sticky' as const, left: 0, zIndex: 2 } : {}),
  }}
```

**Step 6: Apply sticky styles to frozen body cells**

In the body row column map (around line 356), update the cell rendering:

Replace:
```typescript
{columns.map((col, colIdx) => {
  const w = getColWidth(col);
  const isLast = colIdx === columns.length - 1;
  return (
    <div
      key={col.name}
      className="tlTableView__cell"
      style={isLast
        ? { flex: '1 0 auto', minWidth: w }
        : { width: w, minWidth: w }}
    >
```

With:
```typescript
{columns.map((col, colIdx) => {
  const w = getColWidth(col);
  const isLast = colIdx === columns.length - 1;
  const isFrozen = colIdx < frozenColumnCount;
  const isFrozenLast = colIdx === frozenColumnCount - 1;
  let cellClass = 'tlTableView__cell';
  if (isFrozen) cellClass += ' tlTableView__cell--frozen';
  if (isFrozenLast) cellClass += ' tlTableView__cell--frozenLast';
  return (
    <div
      key={col.name}
      className={cellClass}
      style={{
        ...(isLast && !isFrozen
          ? { flex: '1 0 auto', minWidth: w }
          : { width: w, minWidth: w }),
        ...(isFrozen ? { position: 'sticky' as const, left: frozenOffsets[colIdx], zIndex: 2 } : {}),
      }}
    >
```

**Step 7: Commit**

```
Ticket #29109: Apply CSS sticky positioning to frozen columns in TLTableView.
```

---

## Task 3: CSS — Add Frozen Column Styles

**Purpose:** Add CSS classes for frozen cells: opaque background and shadow separator.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Add frozen column CSS at the end of the TLTableView section**

After the `.tlTableView__headerCell--dragOver-right` rule (around line 1985), add:

```css
/* -- Frozen columns -- */

.tlTableView__headerCell--frozen,
.tlTableView__cell--frozen {
	background: var(--layer-01, #fff);
}

.tlTableView__row:hover .tlTableView__cell--frozen {
	background: var(--layer-hover, #e8e8e8);
}

.tlTableView__row--selected .tlTableView__cell--frozen {
	background: var(--layer-selected-01, #d0e2ff);
}

.tlTableView__row--selected:hover .tlTableView__cell--frozen {
	background: var(--layer-selected-hover-01, #b8d4ff);
}

.tlTableView__headerCell--frozenLast,
.tlTableView__cell--frozenLast {
	box-shadow: 2px 0 4px rgba(0, 0, 0, 0.1);
	clip-path: inset(0 -4px 0 0);
}
```

The `clip-path` prevents the shadow from bleeding above/below the cell — it only shows on the right edge.

**Step 2: Commit**

```
Ticket #29109: Add CSS styles for frozen table columns.
```

---

## Task 4: Demo — Enable Frozen Columns

**Purpose:** Update the demo to freeze the first 2 columns (ID and Name) so the feature can be visually verified.

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java`

**Step 1: Set frozenColumnCount**

In the `createDemoTable()` method, after `table.setSelectionMode("multi");` (line 109), add:

```java
table.setFrozenColumnCount(2);
```

**Step 2: Commit**

```
Ticket #29109: Enable frozen columns in React table demo.
```

---

## Task 5: Build and Verify

**Purpose:** Build both modules and verify the Vite bundle includes the changes.

**Step 1: Build the React bundle**

```bash
cd com.top_logic.layout.react && npx vite build --config vite.config.controls.ts
```

Check that `src/main/webapp/script/tl-react-controls.js` is updated.

**Step 2: Build com.top_logic.layout.react**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Expected: BUILD SUCCESS

**Step 3: Build com.top_logic.demo (incremental, NO clean)**

```bash
mvn compile -DskipTests=true -pl com.top_logic.demo
```

**IMPORTANT:** Never use `mvn clean` on `com.top_logic.demo`.

Expected: BUILD SUCCESS

**Step 4: Commit fixes if needed**

```
Ticket #29109: Fix build issues for frozen columns.
```

---

## Summary

| # | Task | Files | Change |
|---|------|-------|--------|
| 1 | Server-side state | ReactTableControl.java | Add frozenColumnCount field, setter, state |
| 2 | Client-side sticky | TLTableView.tsx | Compute offsets, apply sticky to frozen cells |
| 3 | CSS styles | tlReactControls.css | Opaque backgrounds, shadow separator |
| 4 | Demo update | DemoReactTableComponent.java | Set frozenColumnCount=2 |
| 5 | Build & verify | — | Compile check |
