import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const I18N_KEYS = {
  'js.table.freezeUpTo': 'Freeze up to here',
  'js.table.unfreezeAll': 'Unfreeze all',
};

interface ColumnState {
  name: string;
  label: string;
  width: number;
  sortable: boolean;
  sortDirection?: 'asc' | 'desc';
  sortPriority?: number;
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
const TLTableView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  const rowHeight = (state.rowHeight as number) ?? 36;
  const selectionMode = (state.selectionMode as string) ?? 'single';
  const selectedCount = (state.selectedCount as number) ?? 0;
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
  const handleResizeStart = React.useCallback((columnName: string, colWidth: number, event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    resizeRef.current = { column: columnName, startX: event.clientX, startWidth: colWidth };

    const onMouseMove = (e: MouseEvent) => {
      const info = resizeRef.current;
      if (!info) return;
      const newWidth = Math.max(MIN_COL_WIDTH, info.startWidth + (e.clientX - info.startX));
      setColumnWidthOverrides((prev) => ({ ...prev, [info.column]: newWidth }));
    };

    const onMouseUp = (e: MouseEvent) => {
      document.removeEventListener('mousemove', onMouseMove);
      document.removeEventListener('mouseup', onMouseUp);
      const info = resizeRef.current;
      if (info) {
        const finalWidth = Math.max(MIN_COL_WIDTH, info.startWidth + (e.clientX - info.startX));
        sendCommand('columnResize', { column: info.column, width: finalWidth });
        // Keep the override in place — it will be naturally superseded
        // when the server pushes the updated column width via SSE.
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
    if (event.shiftKey) {
      event.preventDefault();
    }
    sendCommand('select', {
      rowIndex,
      ctrlKey: event.ctrlKey || event.metaKey,
      shiftKey: event.shiftKey,
    });
  }, [sendCommand]);

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

  // Close context menu on outside click or Escape.
  React.useEffect(() => {
    if (!contextMenu) return;
    const handleMouseDown = () => setContextMenu(null);
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setContextMenu(null);
    };
    document.addEventListener('mousedown', handleMouseDown);
    document.addEventListener('keydown', handleKeyDown);
    return () => {
      document.removeEventListener('mousedown', handleMouseDown);
      document.removeEventListener('keydown', handleKeyDown);
    };
  }, [contextMenu]);

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
    <div className="tlTableView"
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
        <div className="tlTableView__headerRow" style={{ minWidth: tableWidth }}>
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
                  ...(isLast && !isFrozen
                    ? { flex: '1 0 auto', minWidth: w }
                    : { width: w, minWidth: w }),
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

      {/* Scrollable body */}
      <div
        ref={scrollContainerRef}
        className="tlTableView__body"
        onScroll={handleScroll}
      >
        {/* Spacer for virtual scrolling */}
        <div style={{ height: totalHeight, position: 'relative', minWidth: tableWidth }}>
          {rows.map((row) => (
            <div
              key={row.id}
              className={
                'tlTableView__row' +
                (row.selected ? ' tlTableView__row--selected' : '')
              }
              style={{
                position: 'absolute',
                top: row.index * rowHeight,
                height: rowHeight,
                minWidth: tableWidth,
                width: '100%',
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
  );
};

export default TLTableView;
