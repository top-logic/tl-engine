# React Table: TableModel Integration & Tree Table Support — Design

## Goal

Refactor ReactTableControl to use TopLogic's `TableModel` as its data source (replacing the static `List<Object>`), then add tree table support by detecting `TreeTableModel` and rendering tree metadata.

## Phasing

- **Phase A:** TableModel integration — dynamic data, delegated sort, change events
- **Phase B:** Tree table support — depth, expand/collapse, indentation in first column

## Phase A: TableModel Integration

### Architecture

`ReactTableControl` takes a `TableModel` + `ReactCellControlProvider` instead of `List<?>` + `List<ColumnDef>` + provider.

**Data flow:**

```
TableModel (owns rows, sort, filter)
  → getDisplayedRows() → flat row list
  → getValueAt(row, col) → cell values via Accessor chain
  → getColumnNames() + getColumnDescription() → ColumnDef population
  → TableModelListener → change events → viewport rebuild
```

### Constructor

```java
public ReactTableControl(TableModel model, ReactCellControlProvider cellProvider)
```

- `_allRows` / `_displayedRows` removed — read `model.getDisplayedRows()` on demand
- `ColumnDef` list built from `model.getColumnNames()` + `model.getColumnDescription()`
- Register `TableModelListener` for change events

### Cell Values

Replace the current `getCellValue()` (Map cast) with:

```java
Object value = model.getValueAt(rowObject, columnName);
```

Uses the `Accessor` chain from `ColumnConfiguration`, supporting any row object type.

### Sorting

`SortCommand` still maintains `_sortKeys` for UI state (direction arrows, priority badges). But instead of sorting `_displayedRows` directly:

1. Build a chained `Comparator` from each sort key's `ColumnConfiguration.getComparator()`
2. Call `model.setOrder(comparator)`
3. Model re-sorts internally, fires event
4. Listener detects change, updates ColumnDef sort metadata, rebuilds viewport

### Change Events

Register `TableModelListener`:

| Event | Action |
|-------|--------|
| INSERT / DELETE | Rebuild viewport (row count changed) |
| UPDATE | Refresh affected rows in viewport |
| INVALIDATE | Full rebuild |
| COLUMN_ORDER_UPDATE | Rebuild column state |

### ColumnDef

Stays as a serialization adapter. Populated from `ColumnConfiguration`:

| ColumnDef field | Source |
|----------------|--------|
| `name` | `getColumnName(index)` |
| `label` | Resolved from `ColumnConfiguration` header |
| `width` | `ColumnConfiguration` or default |
| `sortable` | `ColumnConfiguration.getComparator() != null` |

### What Stays the Same

- `ReactCellControlProvider` still creates cell controls (receives value from model)
- Selection stays in `ReactTableControl` (not part of `TableModel`)
- Column resize/reorder commands stay (view-level concerns)
- Virtual scrolling / viewport management unchanged
- Client-side `TLTableView.tsx` unchanged (same state protocol)

### Demo Update

`DemoReactTableComponent` creates an `ObjectTableModel` with `TableConfiguration` and column definitions instead of plain `List<Map>`.

---

## Phase B: Tree Table Support

### Tree Detection

`ReactTableControl` checks `model instanceof TreeTableModel`. If so, accesses the underlying `TreeUIModel` to enrich row state.

### Depth on Tree Nodes

Add cached `int _depth` field to `AbstractTLTreeNode`:

```java
public abstract class AbstractTLTreeNode<N extends AbstractTLTreeNode<N>> {
    private int _depth;

    public AbstractTLTreeNode(N parent, Object businessObject) {
        // ...
        _depth = (parent == null) ? 0 : parent.getDepth() + 1;
    }

    public int getDepth() {
        return _depth;
    }
}
```

Also expose `getDepth()` on the `TLTreeNode` interface.

Depth is computed once at construction — O(1) access, no parent chain walking.

### Row State Enrichment

When the model is a `TreeTableModel`, each row's state map gets:

| Field | Type | Source |
|-------|------|--------|
| `treeDepth` | int | `node.getDepth()` |
| `expanded` | Boolean | `null` = leaf, `true`/`false` = expandable |
| `expandable` | boolean | `!treeModel.isLeaf(node)` |

### Expand/Collapse Command

New `ExpandCommand`:
- Receives: row index + new expanded state (true/false)
- Calls `treeModel.setExpanded(node, expanded)` on the `TreeUIModel`
- Model updates flat row list synchronously (inserts/removes children)
- `TableModelListener` fires INSERT/DELETE events
- `ReactTableControl` rebuilds viewport

### Client Rendering (TLTableView.tsx)

- Read `treeDepth`, `expanded`, `expandable` from row state
- First column gets `paddingLeft: treeDepth * indentWidth` (e.g. 20px per level)
- Expandable nodes show chevron toggle (right = collapsed, down = expanded) before cell content
- Leaf nodes get same indent, no chevron
- Click chevron sends `expand` command

### CSS

- `.tlTableView__treeToggle` — chevron button styling
- Indentation via inline `paddingLeft` on first cell content

---

## Interaction with Existing Features

- **Sorting:** Works normally — sort keys reference columns, tree flattening happens after sort in the model
- **Multi-selection:** Works on flat row list — tree structure doesn't affect selection logic
- **Frozen columns:** Tree indent applies to the first visible column, works with frozen columns
- **Column resize/reorder:** Unaffected — tree indent is on cell content, not column structure
- **Virtual scrolling:** Unchanged — viewport reads from the flat displayed row list
