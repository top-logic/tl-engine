import { React, useTLState, useTLCommand, TLChild, useI18N, KeyboardScopeProvider, useKeyboardBinding, useStandaloneKeyboardScope } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Registers the table's keyboard row-navigation bindings into the enclosing (focus-gated) scope.
 * Rendered inside the table's {@link KeyboardScopeProvider} so the bindings only fire while the
 * table has focus. Navigation is resolved on the server (see {@code moveSelection}); the client
 * only sends the direction plus the Shift (extend) / Ctrl (move-cursor-only) modifiers.
 */
const TableKeyBindings: React.FC<{
  isMulti: boolean;
  cursorIndex: number;
  onMove: (direction: string, extend: boolean, move: boolean) => void;
  onToggle: () => void;
  onSelectAll: () => void;
}> = ({ isMulti, cursorIndex, onMove, onToggle, onSelectAll }) => {
  useKeyboardBinding('ArrowUp', () => { onMove('up', false, false); return true; });
  useKeyboardBinding('ArrowDown', () => { onMove('down', false, false); return true; });
  useKeyboardBinding('Home', () => { onMove('home', false, false); return true; });
  useKeyboardBinding('End', () => { onMove('end', false, false); return true; });
  useKeyboardBinding('PageUp', () => { onMove('pageUp', false, false); return true; });
  useKeyboardBinding('PageDown', () => { onMove('pageDown', false, false); return true; });
  // Shift extends the range (multi only); in single selection it behaves like a plain move.
  useKeyboardBinding('Shift+ArrowUp', () => { onMove('up', isMulti, false); return true; });
  useKeyboardBinding('Shift+ArrowDown', () => { onMove('down', isMulti, false); return true; });
  useKeyboardBinding('Shift+Home', () => { onMove('home', isMulti, false); return true; });
  useKeyboardBinding('Shift+End', () => { onMove('end', isMulti, false); return true; });
  useKeyboardBinding('Shift+PageUp', () => { onMove('pageUp', isMulti, false); return true; });
  useKeyboardBinding('Shift+PageDown', () => { onMove('pageDown', isMulti, false); return true; });
  // Ctrl moves the focus cursor without changing the selection (multi only).
  useKeyboardBinding('Ctrl+ArrowUp', () => { onMove('up', false, isMulti); return true; });
  useKeyboardBinding('Ctrl+ArrowDown', () => { onMove('down', false, isMulti); return true; });
  // Space toggles the cursor row; Ctrl+A selects all (multi only).
  useKeyboardBinding('Space', () => { if (cursorIndex < 0) { return false; } onToggle(); return true; });
  useKeyboardBinding('Ctrl+A', () => { if (!isMulti) { return false; } onSelectAll(); return true; });
  return null;
};

const I18N_KEYS = {
  'js.table.freezeUpTo': 'Freeze up to here',
  'js.table.unfreezeAll': 'Unfreeze all',
  'js.table.filter': 'Filter',
};

interface ColumnState {
  name: string;
  label: string;
  width: number;
  sortable: boolean;
  sortDirection?: 'asc' | 'desc';
  sortPriority?: number;
  filterable?: boolean;
  filterActive?: boolean;
}

interface RowState {
  id: string;
  index: number;
  selected: boolean;
  cells: Record<string, unknown>;
  treeDepth?: number;
  expandable?: boolean;
  expanded?: boolean;
}

const MIN_COL_WIDTH = 50;

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * multi-selection with checkbox column, and column resize.
 */
/**
 * Whether the event originates from an interactive element inside a cell (input, button, link,
 * editor). Row-level gestures must leave such clicks alone: neither steal the element's focus for
 * the table's keyboard scope nor suppress its default mouse handling (e.g. double-click word
 * selection in a text input).
 */
function isInteractiveTarget(event: React.SyntheticEvent): boolean {
  const target = event.target as Element | null;
  return !!target?.closest?.('input, textarea, select, button, a, [contenteditable="true"]');
}

/**
 * Elements that accept text/edit focus inside an editable cell. Disabled/read-only controls are
 * excluded: a read-only row still renders its boolean columns as a disabled checkbox {@code
 * <input>}, which must not count as "this row is editable".
 */
const EDITABLE_SELECTOR =
  'input:not([disabled]):not([readonly]), textarea:not([disabled]):not([readonly]), '
  + 'select:not([disabled]), [contenteditable="true"]';

/** Every keyboard-focusable element inside a row cell, including enabled action buttons/links. */
const FOCUSABLE_SELECTOR = EDITABLE_SELECTOR + ', button:not([disabled]), a[href]';

/** The cell {@code <div>}s (carrying data-row/data-col) of the given row, in column order. */
function rowCells(body: HTMLElement, rowId: string): HTMLElement[] {
  return Array.from(body.querySelectorAll<HTMLElement>('[data-row][data-col]'))
    .filter((c) => c.dataset.row === rowId);
}

/**
 * The editable input to focus within a row: the given column's input when present, otherwise the
 * first (or, with {@code last}, the last) editable cell in column order. Returns null when the row
 * has no editable cell (e.g. a not-yet-selected row in a single-row-editing table).
 */
function editableInRow(
  body: HTMLElement, rowId: string, opts: { col?: string; last?: boolean } = {}
): HTMLElement | null {
  const cells = rowCells(body, rowId);
  if (opts.col) {
    const target = cells.find((c) => c.dataset.col === opts.col);
    const inCol = target?.querySelector<HTMLElement>(EDITABLE_SELECTOR);
    if (inCol) return inCol;
  }
  const ordered = opts.last ? [...cells].reverse() : cells;
  for (const c of ordered) {
    const found = c.querySelector<HTMLElement>(EDITABLE_SELECTOR);
    if (found) return found;
  }
  return null;
}

const TLTableView: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const rootRef = React.useRef<HTMLDivElement>(null);

  // Tooltip resolver: look upwards from the hovered target for a cell carrying
  // data-row / data-col, and turn that into an opaque key for ReactTableControl.
  React.useEffect(() => {
    const node = rootRef.current;
    if (!node) return;
    const handler = (e: Event) => {
      const detail = (e as CustomEvent).detail as {
        target: Element;
        resolved: { key: string } | { inline: unknown } | null;
      };
      let el: Element | null = detail.target;
      while (el && el !== node) {
        const rowId = (el as HTMLElement).dataset.row;
        const colName = (el as HTMLElement).dataset.col;
        if (rowId != null && colName != null) {
          detail.resolved = { key: rowId + '|' + colName };
          return;
        }
        el = el.parentElement;
      }
    };
    node.addEventListener('tl-tooltip-resolve', handler as EventListener);
    return () => node.removeEventListener('tl-tooltip-resolve', handler as EventListener);
  }, []);

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  const rowHeight = (state.rowHeight as number) ?? 36;
  const selectionMode = (state.selectionMode as string) ?? 'single';
  const selectedCount = (state.selectedCount as number) ?? 0;
  const cursorIndex = (state.cursorIndex as number) ?? -1;
  const frozenColumnCount = (state.frozenColumnCount as number) ?? 0;
  const treeMode = (state.treeMode as boolean) ?? false;

  const sortedColumnCount = React.useMemo(
    () => columns.filter((c) => c.sortPriority && c.sortPriority > 0).length,
    [columns]
  );

  const isMulti = selectionMode === 'multi';
  const checkboxWidth = 40;
  const treeIndentWidth = 20;

  const headerRef = React.useRef<HTMLDivElement>(null);
  const scrollContainerRef = React.useRef<HTMLDivElement>(null);
  const scrollTimeoutRef = React.useRef<number | null>(null);

  // A cell whose editable input should receive the caret once the server has re-rendered its row
  // as editable. Set on a selecting cell-click (edit the clicked cell) and on Tab-wrap to a
  // neighbouring row; consumed by the focus effect below. Addressed by row index (stable across
  // virtual scrolling) rather than the transient row id.
  const pendingFocusRef = React.useRef<{ index: number; col?: string; last?: boolean } | null>(null);

  // -- Resize state --
  const [columnWidthOverrides, setColumnWidthOverrides] = React.useState<Record<string, number>>({});
  const resizeRef = React.useRef<{ column: string; startX: number; startWidth: number } | null>(null);
  const justResizedRef = React.useRef(false);

  // -- Drag reorder state --
  const dragColumnRef = React.useRef<string | null>(null);
  const [dragOver, setDragOver] = React.useState<{ column: string; side: 'left' | 'right' } | null>(null);

  // -- Column context menu state --
  const [contextMenu, setContextMenu] = React.useState<{
    x: number; y: number; colIdx: number;
  } | null>(null);


  // Clear overrides when server pushes updated columns (resize confirmed).
  React.useEffect(() => {
    if (!resizeRef.current) {
      setColumnWidthOverrides({});
    }
  }, [columns]);

  const getColWidth = React.useCallback((col: ColumnState): number => {
    return columnWidthOverrides[col.name] ?? col.width;
  }, [columnWidthOverrides]);

  const frozenOffsets = React.useMemo(() => {
    const offsets: number[] = [];
    let left = isMulti && frozenColumnCount > 0 ? checkboxWidth : 0;
    for (let i = 0; i < frozenColumnCount && i < columns.length; i++) {
      offsets.push(left);
      left += getColWidth(columns[i]);
    }
    return offsets;
  }, [columns, frozenColumnCount, isMulti, checkboxWidth, getColWidth]);

  const totalHeight = totalRowCount * rowHeight;

  // -- Resize handlers --
  const resizeAutoScrollRef = React.useRef<number | null>(null);

  const handleResizeStart = React.useCallback((columnName: string, colWidth: number, event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    resizeRef.current = { column: columnName, startX: event.clientX, startWidth: colWidth };

    // Track latest mouse position and cumulative auto-scroll offset.
    let lastClientX = event.clientX;
    let autoScrollOffset = 0;

    const updateWidth = () => {
      const info = resizeRef.current;
      if (!info) return;
      const newWidth = Math.max(MIN_COL_WIDTH, info.startWidth + (lastClientX - info.startX) + autoScrollOffset);
      setColumnWidthOverrides((prev) => ({ ...prev, [info.column]: newWidth }));
    };

    const autoScroll = () => {
      const body = scrollContainerRef.current;
      const header = headerRef.current;
      if (!body || !resizeRef.current) return;
      const rect = body.getBoundingClientRect();
      const threshold = 40;
      const speed = 8;
      const prevScrollLeft = body.scrollLeft;
      if (lastClientX > rect.right - threshold) {
        body.scrollLeft += speed;
      } else if (lastClientX < rect.left + threshold) {
        body.scrollLeft = Math.max(0, body.scrollLeft - speed);
      }
      const actualDelta = body.scrollLeft - prevScrollLeft;
      if (actualDelta !== 0) {
        if (header) header.scrollLeft = body.scrollLeft;
        // Widen/narrow the column by the scroll amount so the resize
        // continues even when the mouse is stuck at the screen edge.
        autoScrollOffset += actualDelta;
        updateWidth();
      }
      resizeAutoScrollRef.current = requestAnimationFrame(autoScroll);
    };
    resizeAutoScrollRef.current = requestAnimationFrame(autoScroll);

    const onMouseMove = (e: MouseEvent) => {
      lastClientX = e.clientX;
      updateWidth();
    };

    const onMouseUp = (e: MouseEvent) => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
      if (resizeAutoScrollRef.current !== null) {
        cancelAnimationFrame(resizeAutoScrollRef.current);
        resizeAutoScrollRef.current = null;
      }
      const info = resizeRef.current;
      if (info) {
        const finalWidth = Math.max(MIN_COL_WIDTH, info.startWidth + (e.clientX - info.startX) + autoScrollOffset);
        sendCommand('columnResize', { column: info.column, width: finalWidth });
        resizeRef.current = null;
        justResizedRef.current = true;
        requestAnimationFrame(() => { justResizedRef.current = false; });
      }
    };

    document.addEventListener('mousemove', onMouseMove);
    document.addEventListener('mouseup', onMouseUp);
  }, [sendCommand]);

  // -- Scroll handler --
  const handleScroll = React.useCallback(() => {
    // Sync header horizontal scroll immediately.
    if (headerRef.current && scrollContainerRef.current) {
      headerRef.current.scrollLeft = scrollContainerRef.current.scrollLeft;
    }
    // Debounced vertical scroll command.
    if (scrollTimeoutRef.current !== null) {
      clearTimeout(scrollTimeoutRef.current);
    }
    scrollTimeoutRef.current = window.setTimeout(() => {
      const container = scrollContainerRef.current;
      if (!container) return;
      const scrollTop = container.scrollTop;
      const visibleCount = Math.ceil(container.clientHeight / rowHeight);
      const start = Math.floor(scrollTop / rowHeight);
      sendCommand('scroll', { start, count: visibleCount });
    }, 80);
  }, [sendCommand, rowHeight]);

  // -- Sort handler --
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

  const handleDrop = React.useCallback((event: React.DragEvent) => {
    event.preventDefault();
    event.stopPropagation();
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

  // -- Selection handlers --
  const handleRowClick = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    // A click that concluded a text-selection drag inside the row copies text,
    // it does not change the row selection.
    const selection = window.getSelection();
    if (selection && !selection.isCollapsed && event.currentTarget.contains(selection.anchorNode)) {
      return;
    }
    // Give the body keyboard focus so the table's keyboard scope becomes active - except when
    // the click landed in an interactive cell element (e.g. a text input of an editable cell),
    // which must keep the focus to stay editable.
    if (!isInteractiveTarget(event)) {
      scrollContainerRef.current?.focus({ preventScroll: true });
      // A plain selecting click enters the clicked cell for editing: once the row re-renders
      // editable, the focus effect moves the caret into that column's input.
      if (!event.ctrlKey && !event.metaKey && !event.shiftKey) {
        const col = (event.target as Element)?.closest?.('[data-col]')?.getAttribute('data-col');
        pendingFocusRef.current = { index: rowIndex, col: col ?? undefined };
      }
    }
    sendCommand('select', {
      rowIndex,
      ctrlKey: event.ctrlKey || event.metaKey,
      shiftKey: event.shiftKey,
    });
  }, [sendCommand]);

  // -- Keyboard navigation (server-resolved; see moveSelection) --
  const handleMove = React.useCallback((direction: string, extend: boolean, move: boolean) => {
    sendCommand('moveSelection', { direction, extend, move });
  }, [sendCommand]);

  const handleToggleCursor = React.useCallback(() => {
    if (cursorIndex < 0) {
      return;
    }
    sendCommand('select', { rowIndex: cursorIndex, ctrlKey: isMulti, shiftKey: false });
  }, [sendCommand, cursorIndex, isMulti]);

  const handleSelectAllRows = React.useCallback(() => {
    sendCommand('selectAll', { selected: true });
  }, [sendCommand]);

  // Predicate for the focus-gated table scope: active only while focus is within this table.
  const isTableFocused = React.useCallback(
    () => !!rootRef.current && rootRef.current.contains(document.activeElement),
    []
  );

  // Keep the keyboard cursor row visible as it moves through the virtualized body.
  React.useEffect(() => {
    if (cursorIndex < 0) {
      return;
    }
    const el = scrollContainerRef.current;
    if (!el) {
      return;
    }
    const rowTop = cursorIndex * rowHeight;
    const rowBottom = rowTop + rowHeight;
    if (rowTop < el.scrollTop) {
      el.scrollTop = rowTop;
    } else if (rowBottom > el.scrollTop + el.clientHeight) {
      el.scrollTop = rowBottom - el.clientHeight;
    }
  }, [cursorIndex, rowHeight]);

  // Once a row has re-rendered as editable (selected via cell-click or reached via Tab-wrap), move
  // the caret into the intended cell. Runs on every server state update; a no-op until the target
  // row is both rendered and editable, so it naturally waits out the select round-trip.
  React.useEffect(() => {
    const pending = pendingFocusRef.current;
    const body = scrollContainerRef.current;
    if (!pending || !body) {
      return;
    }
    const row = rows.find((r) => r.index === pending.index);
    if (!row) {
      return;
    }
    const input = editableInRow(body, row.id, { col: pending.col, last: pending.last });
    if (!input) {
      return;
    }
    pendingFocusRef.current = null;
    input.focus({ preventScroll: false });
    if (input instanceof HTMLInputElement) {
      input.select();
    }
  }, [rows]);

  // Tab at a row boundary wraps to the neighbouring row: forward from the last editable cell to the
  // start of the next row, backward from the first to the end of the previous row. In a single-row-
  // editing table the neighbour must first be selected to become editable; when it is already
  // editable (all rows editable) native Tab handles the move and this steps aside.
  const handleBodyKeyDown = React.useCallback((e: React.KeyboardEvent) => {
    if (e.key !== 'Tab') {
      return;
    }
    const body = scrollContainerRef.current;
    const active = document.activeElement as HTMLElement | null;
    if (!body || !active || !body.contains(active)) {
      return;
    }
    const cell = active.closest<HTMLElement>('[data-row][data-col]');
    if (!cell) {
      return;
    }
    const rowId = cell.dataset.row!;
    const row = rows.find((r) => r.id === rowId);
    if (!row) {
      return;
    }
    const focusables = rowCells(body, rowId)
      .flatMap((c) => Array.from(c.querySelectorAll<HTMLElement>(FOCUSABLE_SELECTOR)));
    const pos = focusables.indexOf(active);
    if (pos < 0) {
      return;
    }
    const forward = !e.shiftKey;
    const atRowEnd = forward ? pos === focusables.length - 1 : pos === 0;
    if (!atRowEnd) {
      // Still room to move within the row: let native Tab handle it.
      return;
    }
    const targetIndex = forward ? row.index + 1 : row.index - 1;
    if (targetIndex < 0 || targetIndex >= totalRowCount) {
      return;
    }
    const neighbour = rows.find((r) => r.index === targetIndex);
    if (neighbour && editableInRow(body, neighbour.id)) {
      // Neighbour already editable (all-rows-editable table): native Tab flows into it.
      return;
    }
    e.preventDefault();
    pendingFocusRef.current = { index: targetIndex, last: !forward };
    sendCommand('select', { rowIndex: targetIndex, ctrlKey: false, shiftKey: false });
  }, [rows, totalRowCount, sendCommand]);

  const handleCheckboxClick = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    event.stopPropagation();
    sendCommand('select', { rowIndex, ctrlKey: true, shiftKey: false });
  }, [sendCommand]);

  const handleSelectAll = React.useCallback(() => {
    const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
    sendCommand('selectAll', { selected: !allSelected });
  }, [sendCommand, selectedCount, totalRowCount]);

  // -- Expand handler --
  const handleExpand = React.useCallback((rowIndex: number, expanded: boolean, event: React.MouseEvent) => {
    event.stopPropagation();
    sendCommand('expand', { rowIndex, expanded });
  }, [sendCommand]);

  // -- Column context menu handlers --
  const handleColumnContextMenu = React.useCallback((colIdx: number, event: React.MouseEvent) => {
    event.preventDefault();
    setContextMenu({ x: event.clientX, y: event.clientY, colIdx });
  }, []);

  const handleFreezeUpTo = React.useCallback(() => {
    if (!contextMenu) return;
    sendCommand('setFrozenColumnCount', { count: contextMenu.colIdx + 1 });
    setContextMenu(null);
  }, [contextMenu, sendCommand]);

  const handleUnfreezeAll = React.useCallback(() => {
    sendCommand('setFrozenColumnCount', { count: 0 });
    setContextMenu(null);
  }, [sendCommand]);

  // Close context menu on outside click; Escape is handled by the shared keyboard dispatcher.
  React.useEffect(() => {
    if (!contextMenu) return;
    const handleMouseDown = () => setContextMenu(null);
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [contextMenu]);
  useStandaloneKeyboardScope(!!contextMenu, { ESCAPE: () => setContextMenu(null) });

  // -- Filter handler: open the server-side filter dialog for a column. --
  const handleOpenFilter = React.useCallback((columnName: string, event: React.MouseEvent) => {
    event.stopPropagation();
    event.preventDefault();
    sendCommand('openFilter', { column: columnName });
  }, [sendCommand]);

  // -- Computed values --
  const tableWidth = columns.reduce((sum, col) => sum + getColWidth(col), 0)
    + (isMulti ? checkboxWidth : 0);

  const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
  const someSelected = selectedCount > 0 && selectedCount < totalRowCount;

  const headerCheckboxRef = React.useCallback((el: HTMLInputElement | null) => {
    if (el) {
      el.indeterminate = someSelected;
    }
  }, [someSelected]);

  return (
    <KeyboardScopeProvider active={isTableFocused}>
    <TableKeyBindings
      isMulti={isMulti}
      cursorIndex={cursorIndex}
      onMove={handleMove}
      onToggle={handleToggleCursor}
      onSelectAll={handleSelectAllRows}
    />
    <div ref={rootRef} id={controlId} className="tlTableView" data-tooltip="dynamic"
      onDragOver={(e) => {
        if (!dragColumnRef.current) return;
        e.preventDefault();
        // Auto-scroll horizontally during column drag.
        const body = scrollContainerRef.current;
        const header = headerRef.current;
        if (!body) return;
        const rect = body.getBoundingClientRect();
        const threshold = 40;
        const speed = 8;
        if (e.clientX < rect.left + threshold) {
          body.scrollLeft = Math.max(0, body.scrollLeft - speed);
        } else if (e.clientX > rect.right - threshold) {
          body.scrollLeft += speed;
        }
        if (header) header.scrollLeft = body.scrollLeft;
      }}
      onDrop={handleDrop}
    >
      {/* Header */}
      <div className="tlTableView__header" ref={headerRef}>
        <div className="tlTableView__headerRow" style={{ width: tableWidth }}>
          {isMulti && (
            <div className={'tlTableView__headerCell tlTableView__checkboxCell'
                + (frozenColumnCount > 0 ? ' tlTableView__headerCell--frozen' : '')}
              style={{
                width: checkboxWidth, minWidth: checkboxWidth,
                ...(frozenColumnCount > 0 ? { position: 'sticky' as const, left: 0, zIndex: 2 } : {}),
              }}
              onDragOver={(e) => {
                if (!dragColumnRef.current) return;
                e.preventDefault();
                e.dataTransfer.dropEffect = 'move';
                if (columns.length > 0 && columns[0].name !== dragColumnRef.current) {
                  setDragOver({ column: columns[0].name, side: 'left' });
                }
              }}
            >
              <input
                type="checkbox"
                ref={headerCheckboxRef}
                className="tlTableView__checkbox"
                checked={allSelected}
                onChange={handleSelectAll}
              />
            </div>
          )}
          {columns.map((col, colIdx) => {
            const w = getColWidth(col);
            const isLast = colIdx === columns.length - 1;
            let cellClass = 'tlTableView__headerCell';
            if (col.sortable) cellClass += ' tlTableView__headerCell--sortable';
            if (dragOver && dragOver.column === col.name) {
              cellClass += ' tlTableView__headerCell--dragOver-' + dragOver.side;
            }
            const isFrozen = colIdx < frozenColumnCount;
            const isFrozenLast = colIdx === frozenColumnCount - 1;
            if (isFrozen) cellClass += ' tlTableView__headerCell--frozen';
            if (isFrozenLast) cellClass += ' tlTableView__headerCell--frozenLast';
            return (
              <div
                key={col.name}
                className={cellClass}
                style={{
                  width: w, minWidth: w,
                  position: isFrozen ? 'sticky' as const : 'relative' as const,
                  ...(isFrozen ? { left: frozenOffsets[colIdx], zIndex: 2 } : {}),
                }}
                draggable={true}
                onClick={col.sortable ? (e) => handleSort(col.name, col.sortDirection, e) : undefined}
                onContextMenu={(e) => handleColumnContextMenu(colIdx, e)}
                onDragStart={(e) => handleDragStart(col.name, e)}
                onDragOver={(e) => handleDragOver(col.name, e)}
                onDrop={handleDrop}
                onDragEnd={handleDragEnd}
              >
                <span className="tlTableView__headerLabel">{col.label}</span>
                {col.filterable && (
                  <button
                    type="button"
                    className={'tlTableView__filterButton'
                      + (col.filterActive ? ' tlTableView__filterButton--active' : '')}
                    title={i18n['js.table.filter']}
                    style={{
                      border: 'none', background: 'transparent', cursor: 'pointer', padding: '0 4px',
                      color: col.filterActive ? '#1565c0' : 'inherit',
                    }}
                    onMouseDown={(e) => e.stopPropagation()}
                    onClick={(e) => handleOpenFilter(col.name, e)}
                  >
                    <i className={col.filterActive ? 'bi bi-funnel-fill' : 'bi bi-funnel'} />
                  </button>
                )}
                {col.sortDirection && (
                  <span className="tlTableView__sortIndicator">
                    {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
                    {sortedColumnCount > 1 && col.sortPriority != null && col.sortPriority > 0 && (
                      <span className="tlTableView__sortPriority">{col.sortPriority}</span>
                    )}
                  </span>
                )}
                <div
                  className="tlTableView__resizeHandle"
                  onMouseDown={(e) => handleResizeStart(col.name, w, e)}
                />
              </div>
            );
          })}
          {/* Drop zone for reordering past the last column */}
          <div
            style={{ flex: '0 0 0', minHeight: '100%' }}
            onDragOver={(e) => {
              if (!dragColumnRef.current) return;
              if (columns.length > 0) {
                const lastCol = columns[columns.length - 1];
                if (lastCol.name !== dragColumnRef.current) {
                  e.preventDefault();
                  e.dataTransfer.dropEffect = 'move';
                  setDragOver({ column: lastCol.name, side: 'right' });
                }
              }
            }}
            onDrop={handleDrop}
          />
        </div>
      </div>

      {/* Scrollable body (focusable so keyboard row navigation can target it) */}
      <div
        ref={scrollContainerRef}
        className="tlTableView__body"
        onScroll={handleScroll}
        onKeyDown={handleBodyKeyDown}
        tabIndex={0}
      >
        {/* Spacer for virtual scrolling */}
        <div style={{ height: totalHeight, position: 'relative', width: tableWidth }}>
          {rows.map((row) => (
            <div
              key={row.id}
              className={
                'tlTableView__row' +
                (row.selected ? ' tlTableView__row--selected' : '') +
                (row.index === cursorIndex ? ' tlTableView__row--cursor' : '')
              }
              style={{
                position: 'absolute',
                top: row.index * rowHeight,
                height: rowHeight,
                width: tableWidth,
                ...(row.index === cursorIndex
                  ? { outline: '2px solid var(--color-primary, #1a73e8)', outlineOffset: '-2px' }
                  : {}),
              }}
              onMouseDown={(e) => {
                // Suppress the text selection the browser would start as a side
                // effect of row-selection gestures (shift/ctrl range or toggle,
                // double-click); plain click-and-drag still selects cell text, and
                // interactive cell elements keep their own mouse handling (e.g.
                // double-click word selection in a text input).
                if ((e.shiftKey || e.ctrlKey || e.metaKey || e.detail > 1) && !isInteractiveTarget(e)) {
                  e.preventDefault();
                }
              }}
              onClick={(e) => handleRowClick(row.index, e)}
            >
              {isMulti && (
                <div className={'tlTableView__cell tlTableView__checkboxCell'
                    + (frozenColumnCount > 0 ? ' tlTableView__cell--frozen' : '')}
                  style={{
                    width: checkboxWidth, minWidth: checkboxWidth,
                    ...(frozenColumnCount > 0 ? { position: 'sticky' as const, left: 0, zIndex: 2 } : {}),
                  }}
                  onClick={(e) => e.stopPropagation()}>
                  <input
                    type="checkbox"
                    className="tlTableView__checkbox"
                    checked={row.selected}
                    onChange={() => {/* handled by onClick */}}
                    onClick={(e) => handleCheckboxClick(row.index, e)}
                    tabIndex={-1}
                  />
                </div>
              )}
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
                    data-row={row.id}
                    data-col={col.name}
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
            </div>
          ))}
        </div>
      </div>

      {/* Column context menu */}
      {contextMenu && (
        <div
          className="tlMenu"
          role="menu"
          style={{ position: 'fixed', top: contextMenu.y, left: contextMenu.x, zIndex: 10000 }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          {contextMenu.colIdx + 1 !== frozenColumnCount && (
            <button type="button" className="tlMenu__item" role="menuitem" onClick={handleFreezeUpTo}>
              <span className="tlMenu__label">{i18n['js.table.freezeUpTo']}</span>
            </button>
          )}
          {frozenColumnCount > 0 && (
            <button type="button" className="tlMenu__item" role="menuitem" onClick={handleUnfreezeAll}>
              <span className="tlMenu__label">{i18n['js.table.unfreezeAll']}</span>
            </button>
          )}
        </div>
      )}
    </div>
    </KeyboardScopeProvider>
  );
};

export default TLTableView;
