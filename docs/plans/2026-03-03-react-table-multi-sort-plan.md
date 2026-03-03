# React Table Multi-Column Sort - Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add multi-column sorting to the React table, where plain click replaces the sort and Shift+click adds/toggles a column in the sort chain, with priority badges in column headers.

**Architecture:** The server maintains an ordered `List<SortKey>` instead of single `_sortColumn`/`_sortAscending` fields. The `SortCommand` receives a `mode` parameter ("replace" or "add"). Each `ColumnDef` gets a `sortPriority` (1-based) sent to the client. The client passes `event.shiftKey` as the mode and renders priority badges when multiple columns are sorted.

**Tech Stack:** Java 17, React via tl-react-bridge, CSS, Maven

---

## File Overview

### Modified Files

**Java (com.top_logic.layout.react):**
- `src/main/java/com/top_logic/layout/react/control/table/ColumnDef.java` — Add `_sortPriority` field and serialization
- `src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — Replace single sort fields with `SortKey` list, update `SortCommand`

**TypeScript (com.top_logic.layout.react):**
- `react-src/controls/TLTableView.tsx` — Pass `mode` in sort command, render priority badges

**CSS (com.top_logic.layout.react):**
- `src/main/webapp/style/tlReactControls.css` — Add sort priority badge styles

---

## Task 1: Add sortPriority to ColumnDef

**Purpose:** Extend `ColumnDef` to carry a sort priority number (1-based position in the multi-sort chain, 0 if unsorted) and include it in the serialized state map.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ColumnDef.java`

**Step 1: Add the field**

After `_sortDirection` (line 29), add:

```java
private int _sortPriority;
```

**Step 2: Add the setter**

After the `setSortDirection` method (lines 73-76), add:

```java
/**
 * Sets the sort priority (1-based position in the multi-sort chain, or 0 for unsorted).
 */
public ColumnDef setSortPriority(int priority) {
	_sortPriority = priority;
	return this;
}
```

**Step 3: Include sortPriority in toStateMap()**

In `toStateMap()`, after the `sortDirection` block (lines 87-89), add:

```java
if (_sortPriority > 0) {
	map.put("sortPriority", Integer.valueOf(_sortPriority));
}
```

**Step 4: Commit**

```
Ticket #29109: Add sortPriority to ColumnDef for multi-column sort.
```

---

## Task 2: Replace Single Sort with SortKey List in ReactTableControl

**Purpose:** Replace `_sortColumn` and `_sortAscending` with an ordered `List<SortKey>`. Update `SortCommand` to handle "replace" and "add" modes. Chain comparators in sort key order.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add SortKey inner class**

After the `_rowCellCache` field (line 111), before the constructor, add:

```java
/**
 * A single entry in the multi-column sort chain.
 */
static class SortKey {

	final String _columnName;

	boolean _ascending;

	SortKey(String columnName, boolean ascending) {
		_columnName = columnName;
		_ascending = ascending;
	}
}
```

**Step 2: Replace _sortColumn and _sortAscending with _sortKeys**

Remove these two fields (lines 89-91):
```java
private String _sortColumn;

private boolean _sortAscending = true;
```

Replace with:
```java
private final List<SortKey> _sortKeys = new ArrayList<>();
```

**Step 3: Add helper method to update ColumnDef sort metadata from _sortKeys**

After `getCellValue` method (line 202), add:

```java
/**
 * Updates the sort direction and priority on all column definitions from the current sort
 * key list.
 */
private void applySortKeysToColumnDefs() {
	// Build a lookup from column name to sort key index.
	Map<String, Integer> sortIndex = new LinkedHashMap<>();
	for (int i = 0; i < _sortKeys.size(); i++) {
		sortIndex.put(_sortKeys.get(i)._columnName, Integer.valueOf(i));
	}

	for (ColumnDef col : _columnDefs) {
		Integer idx = sortIndex.get(col.getName());
		if (idx != null) {
			SortKey key = _sortKeys.get(idx.intValue());
			col.setSortDirection(key._ascending ? "asc" : "desc");
			col.setSortPriority(idx.intValue() + 1);
		} else {
			col.setSortDirection(null);
			col.setSortPriority(0);
		}
	}
}
```

**Step 4: Add helper method to build chained comparator**

After `applySortKeysToColumnDefs`, add:

```java
/**
 * Creates a chained comparator from the current sort key list.
 *
 * @return A comparator, or {@code null} if no sort keys are set.
 */
private Comparator<Object> createChainedComparator() {
	Comparator<Object> result = null;
	for (SortKey key : _sortKeys) {
		Comparator<Object> comp = createSortComparator(key._columnName, key._ascending);
		if (comp != null) {
			result = result == null ? comp : result.thenComparing(comp);
		}
	}
	return result;
}
```

**Step 5: Rewrite SortCommand.execute()**

Replace the entire body of `SortCommand.execute()` (lines 335-363) with:

```java
@Override
protected HandlerResult execute(DisplayContext context, Control control,
		Map<String, Object> arguments) {
	ReactTableControl table = (ReactTableControl) control;
	String column = (String) arguments.get("column");
	String direction = (String) arguments.get("direction");
	boolean ascending = !"desc".equals(direction);
	String mode = (String) arguments.get("mode");

	if ("add".equals(mode)) {
		// Shift+click: add to chain or toggle existing.
		boolean found = false;
		for (SortKey key : table._sortKeys) {
			if (key._columnName.equals(column)) {
				key._ascending = ascending;
				found = true;
				break;
			}
		}
		if (!found) {
			table._sortKeys.add(new SortKey(column, ascending));
		}
	} else {
		// Plain click: replace entire sort with single column.
		table._sortKeys.clear();
		table._sortKeys.add(new SortKey(column, ascending));
	}

	// Update column defs with sort metadata.
	table.applySortKeysToColumnDefs();

	// Sort the displayed rows.
	Comparator<Object> comparator = table.createChainedComparator();
	if (comparator != null) {
		table._displayedRows.sort(comparator);
	}

	// Rebuild: reset viewport to top, send new columns + rows.
	table.buildFullState();
	return HandlerResult.DEFAULT_RESULT;
}
```

**Step 6: Commit**

```
Ticket #29109: Replace single sort with multi-column SortKey list in ReactTableControl.
```

---

## Task 3: Update TLTableView Sort Handler and Render Priority Badges

**Purpose:** Pass `mode` ("replace"/"add") in the sort command based on Shift key, and render sort priority badges when multiple columns are sorted.

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLTableView.tsx`

**Step 1: Add sortPriority to ColumnState interface**

Update the `ColumnState` interface (lines 4-10) to add:

```typescript
interface ColumnState {
  name: string;
  label: string;
  width: number;
  sortable: boolean;
  sortDirection?: 'asc' | 'desc';
  sortPriority?: number;
}
```

**Step 2: Update handleSort to accept the mouse event and pass mode**

Replace the current `handleSort` (lines 129-138):

```typescript
const handleSort = React.useCallback((columnName: string, currentDirection: string | undefined, event: React.MouseEvent) => {
  if (justResizedRef.current) return;
  let newDirection: string;
  if (!currentDirection || currentDirection === 'desc') {
    newDirection = 'asc';
  } else {
    newDirection = 'desc';
  }
  const mode = event.shiftKey ? 'add' : 'replace';
  sendCommand('sort', { column: columnName, direction: newDirection, mode });
}, [sendCommand]);
```

**Step 3: Update the onClick handler on header cells to pass the event**

In the header column map (line 303), replace:

```typescript
onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
```

With:

```typescript
onClick={col.sortable ? (e) => handleSort(col.name, col.sortDirection, e) : undefined}
```

**Step 4: Compute whether multiple columns are sorted**

After `const frozenColumnCount = ...` (line 35), add:

```typescript
const sortedColumnCount = React.useMemo(
  () => columns.filter((c) => c.sortPriority && c.sortPriority > 0).length,
  [columns]
);
```

**Step 5: Render priority badge in header cells**

Replace the sort indicator block (lines 310-313):

```typescript
{col.sortDirection && (
  <span className="tlTableView__sortIndicator">
    {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
  </span>
)}
```

With:

```typescript
{col.sortDirection && (
  <span className="tlTableView__sortIndicator">
    {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
    {sortedColumnCount > 1 && col.sortPriority != null && col.sortPriority > 0 && (
      <span className="tlTableView__sortPriority">{col.sortPriority}</span>
    )}
  </span>
)}
```

**Step 6: Commit**

```
Ticket #29109: Add multi-column sort with Shift+click and priority badges to TLTableView.
```

---

## Task 4: Add Sort Priority Badge CSS

**Purpose:** Style the priority badge number next to the sort arrow.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Add sort priority badge styles**

After the `.tlTableView__sortIndicator` rule (around line 1894), add:

```css
.tlTableView__sortPriority {
	display: inline-flex;
	align-items: center;
	justify-content: center;
	min-width: 0.875rem;
	height: 0.875rem;
	margin-left: 2px;
	padding: 0 2px;
	border-radius: 0.4375rem;
	background: var(--text-secondary, #525252);
	color: var(--text-on-color, #fff);
	font-size: 0.5625rem;
	font-weight: 600;
	line-height: 1;
	vertical-align: middle;
}
```

**Step 2: Commit**

```
Ticket #29109: Add CSS styles for multi-sort priority badges.
```

---

## Task 5: Build and Verify

**Purpose:** Build both modules and verify everything compiles.

**Step 1: Build com.top_logic.layout.react**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Expected: BUILD SUCCESS (this also runs the Vite build via frontend-maven-plugin)

**Step 2: Build com.top_logic.demo (incremental, NO clean)**

```bash
touch com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java
mvn compile -DskipTests=true -pl com.top_logic.demo
```

**IMPORTANT:** Never use `mvn clean` on `com.top_logic.demo`.

Expected: BUILD SUCCESS

**Step 3: Verify the JS bundle contains the changes**

```bash
grep -c "sortPriority" com.top_logic.layout.react/src/main/webapp/script/tl-react-controls.js
```

Expected: at least 1 match

**Step 4: Commit fixes if needed**

```
Ticket #29109: Fix build issues for multi-column sort.
```

---

## Summary

| # | Task | Files | Change |
|---|------|-------|--------|
| 1 | ColumnDef sortPriority | ColumnDef.java | Add field, setter, serialization |
| 2 | SortKey list + SortCommand | ReactTableControl.java | Replace single sort with ordered list, chain comparators |
| 3 | Client sort + badges | TLTableView.tsx | Pass mode, render priority badges |
| 4 | CSS badge styles | tlReactControls.css | Priority badge styling |
| 5 | Build & verify | — | Compile check |
