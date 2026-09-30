# React Tree Table Support Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add tree table support to ReactTableControl — detect TreeTableModel, enrich row state with depth/expansion, add expand/collapse command, render tree indentation and chevrons on the client.

**Architecture:** ReactTableControl detects `TreeTableModel` at construction and accesses its `TreeUIModel` for expansion state and leaf detection. Tree nodes expose depth via a new cached `_depth` field on `AbstractTLTreeNode`. The client renders tree indentation on the first column via inline `paddingLeft` and a chevron toggle for expandable nodes.

**Tech Stack:** Java 17 (server), TypeScript/React (client via tl-react-bridge), Maven, Vite

---

### Task 1: Add `getDepth()` to `TLTreeNode` and `AbstractTLTreeNode`

**Files:**
- Modify: `com.top_logic/src/main/java/com/top_logic/layout/tree/model/TLTreeNode.java`
- Modify: `com.top_logic/src/main/java/com/top_logic/layout/tree/model/AbstractTLTreeNode.java`

**Step 1: Add `getDepth()` to the `TLTreeNode` interface**

In `TLTreeNode.java`, add after the `getBusinessObject()` method:

```java
/**
 * The depth of this node in the tree (0 for root).
 */
public int getDepth();
```

**Step 2: Add cached `_depth` field and implementation to `AbstractTLTreeNode`**

In `AbstractTLTreeNode.java`, add a field and modify the constructor:

```java
private int _depth;
```

In the constructor, after `_businessObject = businessObject;`, add:

```java
_depth = (parent == null) ? 0 : parent.getDepth() + 1;
```

Add the getter method:

```java
@Override
public int getDepth() {
    return _depth;
}
```

**Step 3: Build to verify**

Run: `mvn install -DskipTests=true -pl com.top_logic`
Expected: BUILD SUCCESS

**Step 4: Commit**

```bash
git add com.top_logic/src/main/java/com/top_logic/layout/tree/model/TLTreeNode.java \
       com.top_logic/src/main/java/com/top_logic/layout/tree/model/AbstractTLTreeNode.java
git commit -m "Ticket #29109: Add cached getDepth() to TLTreeNode and AbstractTLTreeNode."
```

---

### Task 2: Add tree state enrichment and ExpandCommand to ReactTableControl

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add imports**

Add to the import section:

```java
import com.top_logic.layout.tree.model.TLTreeNode;
import com.top_logic.layout.tree.model.TreeTableModel;
import com.top_logic.layout.tree.model.TreeUIModel;
```

**Step 2: Add tree model field and detection**

Add field after `_modelListener`:

```java
/** The tree UI model, or {@code null} if the model is not a tree table. */
private final TreeUIModel<?> _treeModel;
```

In the constructor, after `_tableModel.addTableModelListener(_modelListener);`, add:

```java
_treeModel = (_tableModel instanceof TreeTableModel)
    ? ((TreeTableModel) _tableModel).getTreeModel()
    : null;
```

Add a state key constant with the other state keys:

```java
private static final String TREE_MODE = "treeMode";
```

In the constructor, after `putState(FROZEN_COLUMN_COUNT, ...)`:

```java
putState(TREE_MODE, Boolean.valueOf(_treeModel != null));
```

**Step 3: Register ExpandCommand**

Add `new ExpandCommand()` to the COMMANDS map:

```java
private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
    new ScrollCommand(),
    new SortCommand(),
    new SelectCommand(),
    new SelectAllCommand(),
    new ColumnResizeCommand(),
    new ColumnReorderCommand(),
    new ExpandCommand());
```

**Step 4: Enrich row state in `updateViewport`**

In the `updateViewport` method, inside the loop that builds `rowStates`, after `rowState.put("selected", ...)`:

```java
if (table._treeModel != null && rowObject instanceof TLTreeNode) {
    TLTreeNode<?> node = (TLTreeNode<?>) rowObject;
    rowState.put("treeDepth", Integer.valueOf(node.getDepth()));
    boolean leaf = table._treeModel.isLeaf(castNode(rowObject));
    rowState.put("expandable", Boolean.valueOf(!leaf));
    if (!leaf) {
        rowState.put("expanded", Boolean.valueOf(table._treeModel.isExpanded(castNode(rowObject))));
    }
}
```

Note: `updateViewport` is a private method, so access `_treeModel` directly via `this` (no `table.` prefix needed — the tree enrichment code goes inside the same method body where `rowState` is built). The actual reference is just `_treeModel`.

Add a helper method (private, near `getDisplayedRows()`):

```java
/**
 * Casts a row object to a node type compatible with the tree UI model.
 */
@SuppressWarnings("unchecked")
private static <N> N castNode(Object node) {
    return (N) node;
}
```

**Step 5: Add ExpandCommand inner class**

Add after `ColumnReorderCommand`:

```java
/**
 * Handles expand/collapse requests from the client.
 */
static class ExpandCommand extends ControlCommand {

    ExpandCommand() {
        super("expand");
    }

    @Override
    public ResKey getI18NKey() {
        return ResKey.legacy("react.table.expand");
    }

    @Override
    protected HandlerResult execute(DisplayContext context, Control control,
            Map<String, Object> arguments) {
        ReactTableControl table = (ReactTableControl) control;
        if (table._treeModel == null) {
            return HandlerResult.DEFAULT_RESULT;
        }

        int rowIndex = ((Number) arguments.get("rowIndex")).intValue();
        boolean expanded = Boolean.TRUE.equals(arguments.get("expanded"));

        List<?> displayedRows = table.getDisplayedRows();
        if (rowIndex < 0 || rowIndex >= displayedRows.size()) {
            return HandlerResult.DEFAULT_RESULT;
        }

        Object rowObject = displayedRows.get(rowIndex);
        table._suppressModelEvents = true;
        try {
            table._treeModel.setExpanded(castNode(rowObject), expanded);
        } finally {
            table._suppressModelEvents = false;
        }

        table.buildFullState();
        return HandlerResult.DEFAULT_RESULT;
    }
}
```

**Step 6: Build to verify**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS

**Step 7: Commit**

```bash
git add com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java
git commit -m "Ticket #29109: Add tree table detection, row state enrichment, and ExpandCommand."
```

---

### Task 3: Add tree indentation and chevron toggle to TLTableView.tsx

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLTableView.tsx`

**Step 1: Extend RowState interface**

Add tree fields to the `RowState` interface:

```typescript
interface RowState {
  id: string;
  index: number;
  selected: boolean;
  cells: Record<string, unknown>;
  treeDepth?: number;
  expandable?: boolean;
  expanded?: boolean;
}
```

**Step 2: Read `treeMode` from state**

After the `frozenColumnCount` state read:

```typescript
const treeMode = (state.treeMode as boolean) ?? false;
```

**Step 3: Add expand handler**

After `handleSelectAll`:

```typescript
const handleExpand = React.useCallback((rowIndex: number, expanded: boolean, event: React.MouseEvent) => {
  event.stopPropagation();
  sendCommand('expand', { rowIndex, expanded });
}, [sendCommand]);
```

**Step 4: Add tree indent constant**

After `const checkboxWidth = 40;`:

```typescript
const treeIndentWidth = 20;
```

**Step 5: Modify first column cell rendering in the row body**

Replace the cell rendering block inside `columns.map((col, colIdx) => { ... })` in the row body. The first column (`colIdx === 0`) in tree mode gets tree indentation and a chevron. Change the cell content from just `<TLChild control={row.cells[col.name]} />` to conditionally wrap with tree indentation:

In the body rows' `columns.map(...)`, replace the inner `<div>` content:

```tsx
{columns.map((col, colIdx) => {
  const w = getColWidth(col);
  const isLast = colIdx === columns.length - 1;
  const isFrozen = colIdx < frozenColumnCount;
  const isFrozenLast = colIdx === frozenColumnCount - 1;
  let cellClass = 'tlTableView__cell';
  if (isFrozen) cellClass += ' tlTableView__cell--frozen';
  if (isFrozenLast) cellClass += ' tlTableView__cell--frozenLast';
  const isTreeColumn = treeMode && colIdx === 0;
  const treeDepth = row.treeDepth ?? 0;
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
      {isTreeColumn ? (
        <div className="tlTableView__treeCell" style={{ paddingLeft: treeDepth * treeIndentWidth }}>
          {row.expandable ? (
            <button
              className="tlTableView__treeToggle"
              onClick={(e) => handleExpand(row.index, !row.expanded, e)}
            >
              {row.expanded ? '\u25BE' : '\u25B8'}
            </button>
          ) : (
            <span className="tlTableView__treeToggleSpacer" />
          )}
          <TLChild control={row.cells[col.name]} />
        </div>
      ) : (
        <TLChild control={row.cells[col.name]} />
      )}
    </div>
  );
})}
```

**Step 6: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLTableView.tsx
git commit -m "Ticket #29109: Add tree indentation and expand/collapse chevron to TLTableView."
```

---

### Task 4: Add CSS for tree toggle and indentation

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Add tree CSS rules**

Append after the existing `.tlTableView__sortPriority` rules:

```css
/* Tree table */
.tlTableView__treeCell {
  display: flex;
  align-items: center;
  overflow: hidden;
  height: 100%;
}

.tlTableView__treeToggle {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  flex-shrink: 0;
  border: none;
  background: none;
  padding: 0;
  cursor: pointer;
  color: var(--text-secondary, #666);
  font-size: 12px;
  border-radius: 3px;
}

.tlTableView__treeToggle:hover {
  background-color: var(--layer-hover, rgba(0, 0, 0, 0.08));
}

.tlTableView__treeToggleSpacer {
  display: inline-block;
  width: 20px;
  flex-shrink: 0;
}
```

**Step 2: Commit**

```bash
git add com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css
git commit -m "Ticket #29109: Add CSS for tree toggle button and indentation."
```

---

### Task 5: Add tree table demo to DemoReactTableComponent

**Files:**
- Modify: `com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java`

**Step 1: Add tree table imports**

Add to imports:

```java
import com.top_logic.layout.tree.model.DefaultTreeTableModel;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableBuilder;
import com.top_logic.layout.tree.model.DefaultTreeTableModel.DefaultTreeTableNode;
```

**Step 2: Add a second table (tree table) to `writeBody`**

After the existing `_tableControl.write(...)` / closing `</div>`, add a second section for the tree table. Add a field:

```java
private ReactTableControl _treeTableControl;
```

In `writeBody`, after the existing table div close, add:

```java
if (_treeTableControl == null) {
    _treeTableControl = createDemoTreeTable();
}

out.beginTag(HTMLConstants.H2);
out.writeText("React Tree Table Demo");
out.endTag(HTMLConstants.H2);

out.beginTag(HTMLConstants.PARAGRAPH);
out.writeText("A tree table with expandable nodes. Click chevrons to expand/collapse.");
out.endTag(HTMLConstants.PARAGRAPH);

out.beginBeginTag(HTMLConstants.DIV);
out.writeAttribute(HTMLConstants.STYLE_ATTR, "height: 500px; border: 1px solid #ccc;");
out.endBeginTag();
_treeTableControl.write(displayContext, out);
out.endTag(HTMLConstants.DIV);
```

**Step 3: Add `createDemoTreeTable()` method**

```java
private ReactTableControl createDemoTreeTable() {
    List<String> columnNames = Arrays.asList("name", "type", "size");

    TableConfiguration tableConfig = TableConfiguration.table();
    tableConfig.getDefaultColumn().setAccessor(MapAccessor.INSTANCE);
    tableConfig.getDefaultColumn().setComparator(ComparableComparator.INSTANCE);

    declareColumn(tableConfig, "name", "Name", "300px");
    declareColumn(tableConfig, "type", "Type", "150px");
    declareColumn(tableConfig, "size", "Size", "100px");

    DefaultTreeTableModel model = new DefaultTreeTableModel(
        new DefaultTreeTableBuilder(), createRootData(), columnNames, tableConfig);

    // Build tree structure.
    DefaultTreeTableNode root = model.getRoot();
    String[] folders = { "Documents", "Pictures", "Source Code", "Music" };
    String[] types = { "Folder", "Folder", "Folder", "Folder" };

    for (int f = 0; f < folders.length; f++) {
        Map<String, Object> folderData = new LinkedHashMap<>();
        folderData.put("name", folders[f]);
        folderData.put("type", types[f]);
        folderData.put("size", "");
        DefaultTreeTableNode folder = root.createChild(folderData);

        for (int i = 0; i < 5; i++) {
            Map<String, Object> fileData = new LinkedHashMap<>();
            fileData.put("name", "File " + (i + 1) + " in " + folders[f]);
            fileData.put("type", "File");
            fileData.put("size", String.valueOf((i + 1) * 128) + " KB");
            DefaultTreeTableNode file = folder.createChild(fileData);

            // Add sub-items to first file of each folder.
            if (i == 0) {
                for (int j = 0; j < 3; j++) {
                    Map<String, Object> subData = new LinkedHashMap<>();
                    subData.put("name", "Version " + (j + 1));
                    subData.put("type", "Version");
                    subData.put("size", String.valueOf((j + 1) * 64) + " KB");
                    file.createChild(subData);
                }
            }
        }
    }

    LabelProvider labels = MetaLabelProvider.INSTANCE;
    ReactCellControlProvider cellProvider = (rowObject, columnName, cellValue) -> {
        return new ReactTextCellControl(labels.getLabel(cellValue));
    };

    ReactTableControl table = new ReactTableControl(model, cellProvider);
    table.setSelectionMode("multi");
    return table;
}

private static Map<String, Object> createRootData() {
    Map<String, Object> root = new LinkedHashMap<>();
    root.put("name", "Root");
    root.put("type", "Root");
    root.put("size", "");
    return root;
}
```

**Step 4: Commit**

```bash
git add com.top_logic.demo/src/main/java/com/top_logic/demo/react/DemoReactTableComponent.java
git commit -m "Ticket #29109: Add tree table demo to DemoReactTableComponent."
```

---

### Task 6: Build and verify

**Step 1: Build react module**

Run: `mvn install -DskipTests=true -pl com.top_logic.layout.react`
Expected: BUILD SUCCESS (Java + Vite build)

**Step 2: Build demo module**

Run: `mvn compile -DskipTests=true -pl com.top_logic.demo`
Expected: BUILD SUCCESS

**Step 3: Verify no compilation errors across both modules**

Both commands should complete with `BUILD SUCCESS`.
