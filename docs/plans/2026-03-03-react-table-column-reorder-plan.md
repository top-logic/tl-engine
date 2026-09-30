# React Table Column Reorder Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add drag-and-drop column reorder on header cells with drop indicator and server persistence.

**Architecture:** HTML5 Drag and Drop API on header cells. Client shows a drop indicator (blue border) during dragover. On drop, client sends `columnReorder` command with column name and target index. Server reorders `_columnDefs`, rebuilds viewport (cell order changes), and pushes updated state.

**Tech Stack:** Java 17, React via tl-react-bridge, Maven, Vite

---

## File Overview

### Modified Files

- `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java` — Add `ColumnReorderCommand`
- `com.top_logic.layout.react/react-src/controls/TLTableView.tsx` — Add drag-and-drop handlers
- `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` — Add drag indicator styles

---

## Task 1: Server-Side ColumnReorderCommand

**Purpose:** Add a command that reorders a column in `_columnDefs` and rebuilds the viewport.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/table/ReactTableControl.java`

**Step 1: Add `ColumnReorderCommand` to the command map**

Change:
```java
	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand(),
		new SelectAllCommand(),
		new ColumnResizeCommand());
```

To:
```java
	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new ScrollCommand(),
		new SortCommand(),
		new SelectCommand(),
		new SelectAllCommand(),
		new ColumnResizeCommand(),
		new ColumnReorderCommand());
```

**Step 2: Add the `ColumnReorderCommand` inner class**

Add after `ColumnResizeCommand`, before the closing brace of `ReactTableControl`:

```java
	/**
	 * Handles column reorder from the client.
	 */
	static class ColumnReorderCommand extends ControlCommand {

		ColumnReorderCommand() {
			super("columnReorder");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.legacy("react.table.columnReorder");
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control,
				Map<String, Object> arguments) {
			ReactTableControl table = (ReactTableControl) control;
			String column = (String) arguments.get("column");
			int targetIndex = ((Number) arguments.get("targetIndex")).intValue();

			// Find and remove the column.
			ColumnDef moved = null;
			for (int i = 0; i < table._columnDefs.size(); i++) {
				if (table._columnDefs.get(i).getName().equals(column)) {
					moved = table._columnDefs.remove(i);
					break;
				}
			}

			if (moved == null) {
				return HandlerResult.DEFAULT_RESULT;
			}

			// Clamp target index and insert.
			int insertAt = Math.max(0, Math.min(targetIndex, table._columnDefs.size()));
			table._columnDefs.add(insertAt, moved);

			// Rebuild columns and viewport (cell order in rows changes).
			table.buildFullState();
			return HandlerResult.DEFAULT_RESULT;
		}
	}
```

**Step 3: Commit**

```
Ticket #29109: Add ColumnReorderCommand for server-side column reorder.
```

---

## Task 2: Client-Side Drag-and-Drop Logic

**Purpose:** Add HTML5 drag-and-drop handlers to header cells for reordering columns.

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLTableView.tsx`

**Step 1: Add drag state refs and state**

After the existing resize state block (after `const justResizedRef = ...`), add:

```tsx
  // -- Drag reorder state --
  const dragColumnRef = React.useRef<string | null>(null);
  const [dragOver, setDragOver] = React.useState<{ column: string; side: 'left' | 'right' } | null>(null);
```

**Step 2: Add drag event handlers**

After the existing `handleSort` callback, add these handlers:

```tsx
  // -- Drag reorder handlers --
  const handleDragStart = React.useCallback((columnName: string, event: React.DragEvent) => {
    dragColumnRef.current = columnName;
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData('text/plain', columnName);
  }, []);

  const handleDragOver = React.useCallback((columnName: string, event: React.DragEvent) => {
    if (!dragColumnRef.current || dragColumnRef.current === columnName) {
      setDragOver(null);
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const side = (event.clientX < rect.left + rect.width / 2) ? 'left' : 'right';
    setDragOver({ column: columnName, side });
  }, []);

  const handleDragLeave = React.useCallback(() => {
    setDragOver(null);
  }, []);

  const handleDrop = React.useCallback((event: React.DragEvent) => {
    event.preventDefault();
    const draggedName = dragColumnRef.current;
    if (!draggedName || !dragOver) {
      dragColumnRef.current = null;
      setDragOver(null);
      return;
    }

    // Compute target index based on drop side.
    let targetIndex = columns.findIndex((c) => c.name === dragOver.column);
    if (targetIndex < 0) {
      dragColumnRef.current = null;
      setDragOver(null);
      return;
    }
    const draggedIndex = columns.findIndex((c) => c.name === draggedName);
    if (dragOver.side === 'right') {
      targetIndex++;
    }
    // Adjust for removal: if dragged is before target, removal shifts indices down.
    if (draggedIndex < targetIndex) {
      targetIndex--;
    }

    sendCommand('columnReorder', { column: draggedName, targetIndex });
    dragColumnRef.current = null;
    setDragOver(null);
  }, [columns, dragOver, sendCommand]);

  const handleDragEnd = React.useCallback(() => {
    dragColumnRef.current = null;
    setDragOver(null);
  }, []);
```

**Step 3: Update header cell rendering**

Replace the header cell `columns.map(...)` block. The current code:

```tsx
          {columns.map((col) => {
            const w = getColWidth(col);
            return (
              <div
                key={col.name}
                className={'tlTableView__headerCell' + (col.sortable ? ' tlTableView__headerCell--sortable' : '')}
                style={{ width: w, minWidth: w, position: 'relative' }}
                onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
              >
                <span className="tlTableView__headerLabel">{col.label}</span>
                {col.sortDirection && (
                  <span className="tlTableView__sortIndicator">
                    {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
                  </span>
                )}
                <div
                  className="tlTableView__resizeHandle"
                  onMouseDown={(e) => handleResizeStart(col.name, w, e)}
                />
              </div>
            );
          })}
```

Replace with:

```tsx
          {columns.map((col) => {
            const w = getColWidth(col);
            let cellClass = 'tlTableView__headerCell';
            if (col.sortable) cellClass += ' tlTableView__headerCell--sortable';
            if (dragOver && dragOver.column === col.name) {
              cellClass += ' tlTableView__headerCell--dragOver-' + dragOver.side;
            }
            return (
              <div
                key={col.name}
                className={cellClass}
                style={{ width: w, minWidth: w, position: 'relative' }}
                draggable={true}
                onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
                onDragStart={(e) => handleDragStart(col.name, e)}
                onDragOver={(e) => handleDragOver(col.name, e)}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                onDragEnd={handleDragEnd}
              >
                <span className="tlTableView__headerLabel">{col.label}</span>
                {col.sortDirection && (
                  <span className="tlTableView__sortIndicator">
                    {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
                  </span>
                )}
                <div
                  className="tlTableView__resizeHandle"
                  onMouseDown={(e) => handleResizeStart(col.name, w, e)}
                />
              </div>
            );
          })}
```

**Step 4: Commit**

```
Ticket #29109: Add drag-and-drop column reorder to TLTableView.
```

---

## Task 3: CSS for Drag Indicators

**Purpose:** Style the drop indicator and dragging state.

**Files:**
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

**Step 1: Append drag styles after the resize handle block**

After the `.tlTableView__resizeHandle:hover, .tlTableView__resizeHandle:active` block (at the end of the TLTableView section), add:

```css

.tlTableView__headerCell[draggable] {
	cursor: grab;
}

.tlTableView__headerCell[draggable]:active {
	cursor: grabbing;
}

.tlTableView__headerCell--dragOver-left {
	border-left: 2px solid var(--interactive, #0f62fe);
}

.tlTableView__headerCell--dragOver-right {
	border-right: 2px solid var(--interactive, #0f62fe);
}
```

**Step 2: Commit**

```
Ticket #29109: Add column drag-and-drop indicator CSS styles.
```

---

## Task 4: Build and Verify

**Step 1: Build com.top_logic.layout.react**

```bash
cd com.top_logic.layout.react
mvn install -DskipTests=true
```

Expected: BUILD SUCCESS (includes Vite build via frontend-maven-plugin)

**Step 2: Commit build artifacts**

```
Ticket #29109: Rebuild React controls bundle.
```

---

## Task 5: Manual Testing

Test in the demo app (React > Table tab):

1. **Drag indicator** — drag a header cell over another, blue line appears on left/right side
2. **Drop reorders** — dropping moves column to correct position
3. **Checkbox column unaffected** — checkbox header is not draggable (it's not in the columns array)
4. **Sort still works** — clicking a header (without dragging) sorts
5. **Resize still works** — dragging the resize handle resizes, doesn't start a column drag
6. **Selection unaffected** — reorder doesn't change selection state
