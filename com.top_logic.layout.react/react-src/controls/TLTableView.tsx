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
 * React table component with virtual scrolling and server-driven cell controls.
 *
 * State:
 * - columns: ColumnState[]
 * - totalRowCount: number
 * - viewportStart: number
 * - rows: RowState[]
 * - rowHeight: number
 * - selectionMode: 'single' | 'multi' | 'range'
 */
const TLTableView: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  const rowHeight = (state.rowHeight as number) ?? 36;

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

  const handleRowClick = React.useCallback((rowIndex: number) => {
    sendCommand('select', { rowIndex });
  }, [sendCommand]);

  const tableWidth = columns.reduce((sum, col) => sum + col.width, 0);

  return (
    <div className="tlTableView">
      {/* Header */}
      <div className="tlTableView__header" style={{ width: tableWidth }}>
        <div className="tlTableView__headerRow">
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
              onClick={() => handleRowClick(row.index)}
            >
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
