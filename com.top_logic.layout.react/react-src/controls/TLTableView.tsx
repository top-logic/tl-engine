import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

interface ColumnState {
  name: string;
  label: string;
  width: number;
  sortable: boolean;
  sortDirection?: 'asc' | 'desc';
}

interface RowState {
  id: string;
  index: number;
  selected: boolean;
  cells: Record<string, unknown>;
}

const MIN_COL_WIDTH = 50;

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * multi-selection with checkbox column, and column resize.
 */
const TLTableView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  const rowHeight = (state.rowHeight as number) ?? 36;
  const selectionMode = (state.selectionMode as string) ?? 'single';
  const selectedCount = (state.selectedCount as number) ?? 0;

  const isMulti = selectionMode === 'multi';
  const checkboxWidth = 40;

  const scrollContainerRef = React.useRef<HTMLDivElement>(null);
  const scrollTimeoutRef = React.useRef<number | null>(null);

  // -- Resize state --
  const [columnWidthOverrides, setColumnWidthOverrides] = React.useState<Record<string, number>>({});
  const resizeRef = React.useRef<{ column: string; startX: number; startWidth: number } | null>(null);
  const justResizedRef = React.useRef(false);

  const getColWidth = (col: ColumnState): number => {
    return columnWidthOverrides[col.name] ?? col.width;
  };

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
        setColumnWidthOverrides((prev) => {
          const next = { ...prev };
          delete next[info.column];
          return next;
        });
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
  const handleSort = React.useCallback((columnName: string, currentDirection?: string) => {
    if (justResizedRef.current) return;
    let newDirection: string;
    if (!currentDirection || currentDirection === 'desc') {
      newDirection = 'asc';
    } else {
      newDirection = 'desc';
    }
    sendCommand('sort', { column: columnName, direction: newDirection });
  }, [sendCommand]);

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
    <div className="tlTableView">
      {/* Header */}
      <div className="tlTableView__header" style={{ width: tableWidth }}>
        <div className="tlTableView__headerRow">
          {isMulti && (
            <div className="tlTableView__headerCell tlTableView__checkboxCell"
              style={{ width: checkboxWidth, minWidth: checkboxWidth }}>
              <input
                type="checkbox"
                ref={headerCheckboxRef}
                className="tlTableView__checkbox"
                checked={allSelected}
                onChange={handleSelectAll}
              />
            </div>
          )}
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
        </div>
      </div>

      {/* Scrollable body */}
      <div
        ref={scrollContainerRef}
        className="tlTableView__body"
        onScroll={handleScroll}
      >
        {/* Spacer for virtual scrolling */}
        <div style={{ height: totalHeight, position: 'relative' }}>
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
                width: tableWidth,
              }}
              onClick={(e) => handleRowClick(row.index, e)}
            >
              {isMulti && (
                <div className="tlTableView__cell tlTableView__checkboxCell"
                  style={{ width: checkboxWidth, minWidth: checkboxWidth }}
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
              {columns.map((col) => {
                const w = getColWidth(col);
                return (
                  <div
                    key={col.name}
                    className="tlTableView__cell"
                    style={{ width: w, minWidth: w }}
                  >
                    <TLChild control={row.cells[col.name]} />
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TLTableView;
