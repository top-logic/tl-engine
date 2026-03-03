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

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * and multi-selection with checkbox column.
 *
 * State:
 * - columns: ColumnState[]
 * - totalRowCount: number
 * - viewportStart: number
 * - rows: RowState[]
 * - rowHeight: number
 * - selectionMode: 'single' | 'multi'
 * - selectionForced: boolean
 * - selectedCount: number
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

  const totalHeight = totalRowCount * rowHeight;

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

  const handleSort = React.useCallback((columnName: string, currentDirection?: string) => {
    let newDirection: string;
    if (!currentDirection || currentDirection === 'desc') {
      newDirection = 'asc';
    } else {
      newDirection = 'desc';
    }
    sendCommand('sort', { column: columnName, direction: newDirection });
  }, [sendCommand]);

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

  const tableWidth = columns.reduce((sum, col) => sum + col.width, 0)
    + (isMulti ? checkboxWidth : 0);

  // Header checkbox state.
  const allSelected = selectedCount === totalRowCount && totalRowCount > 0;
  const someSelected = selectedCount > 0 && selectedCount < totalRowCount;

  // Ref for indeterminate state (cannot set via JSX attribute).
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
          {columns.map((col) => (
            <div
              key={col.name}
              className={'tlTableView__headerCell' + (col.sortable ? ' tlTableView__headerCell--sortable' : '')}
              style={{ width: col.width, minWidth: col.width }}
              onClick={col.sortable ? () => handleSort(col.name, col.sortDirection) : undefined}
            >
              <span className="tlTableView__headerLabel">{col.label}</span>
              {col.sortDirection && (
                <span className="tlTableView__sortIndicator">
                  {col.sortDirection === 'asc' ? '\u25B2' : '\u25BC'}
                </span>
              )}
            </div>
          ))}
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
              {columns.map((col) => (
                <div
                  key={col.name}
                  className="tlTableView__cell"
                  style={{ width: col.width, minWidth: col.width }}
                >
                  <TLChild control={row.cells[col.name]} />
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default TLTableView;
