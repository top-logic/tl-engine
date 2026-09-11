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
  onActivate: () => boolean;
}> = ({ isMulti, cursorIndex, onMove, onToggle, onSelectAll, onActivate }) => {
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
  // Enter opens the cursor row; it declines when there is nothing to open, so the gesture falls
  // through to an enclosing scope (a dialog's default button).
  useKeyboardBinding('Enter', () => onActivate());
  return null;
};

const I18N_KEYS = {
  'js.table.freezeUpTo': 'Freeze up to here',
  'js.table.unfreezeAll': 'Unfreeze all',
  'js.table.groupBy': 'Group by this column',
  'js.table.ungroup': 'Remove grouping',
  'js.table.fitColumn': 'Fit width to content',
  'js.table.grouped': 'The rows are grouped by this column',
  'js.table.freezeSplitter': 'Drag to choose the columns that stay in place while scrolling',
  'js.table.filter': 'Filter',
  'js.table.columns': 'Columns',
  'js.table.search': 'Search',
  'js.table.searchHint': 'Search the displayed columns',
  'js.table.clearFilter': 'Show all rows again',
  'js.table.saveFilter': 'Save this filter',
  'js.table.filterName': 'Filter name',
  'js.table.deleteFilter': 'Delete this filter',
  'js.table.cancelSave': 'Do not save',
};

/**
 * Debounce for sending a typed search term: long enough to coalesce a burst of keystrokes into one
 * round-trip, short enough that the rows follow the typing. Enter sends the term at once.
 */
const SEARCH_DEBOUNCE_MS = 300;

interface ColumnState {
  name: string;
  label: string;
  width: number;
  sortable: boolean;
  sortDirection?: 'asc' | 'desc';
  sortPriority?: number;
  filterable?: boolean;
  filterActive?: boolean;
  groupable?: boolean;
  /**
   * Whether the column keeps its place at the end of the table: rendered behind all others, fixed
   * to the right edge while the table scrolls, and beyond the user's arrangement - it can neither
   * be moved, hidden, frozen nor resized.
   */
  pinnedEnd?: boolean;
  /**
   * CSS class put on every cell of the column, its heading included: how the column presents its
   * cells, e.g. a column holding a button instead of text.
   */
  cssClass?: string;
}

/** One of the filter criteria the table offers under a name, displayed as a chip in the filter bar. */
interface NamedFilterState {
  id: string;
  label: string;
  deletable: boolean;
}

interface RowState {
  id: string;
  index: number;
  selected: boolean;
  cells: Record<string, unknown>;
  treeDepth?: number;
  expandable?: boolean;
  expanded?: boolean;
  /** Present exactly on a group header row: how many rows the group holds. */
  groupCount?: number;
}

const MIN_COL_WIDTH = 50;

/**
 * The width the column needs for the content it shows right now: its heading and the cells of the
 * rows currently rendered, whichever is widest.
 *
 * The cells on screen are clipped to the column width and their text wraps inside it, so neither
 * their layout width nor their scroll width tells how much room the content wants. Each cell is
 * therefore measured as a copy sized to its content, in a container that is part of the table and
 * hence inherits its fonts. The copies keep the cells' classes and inline styles, so the padding
 * and the border they are measured with are the ones on screen, and a single layout pass covers
 * the whole column.
 *
 * @param root The table's root element.
 * @param columnName Name of the column to measure.
 * @returns The width in whole pixels, or 0 when the column renders nothing.
 */
const measureColumnContentWidth = (root: HTMLElement, columnName: string): number => {
  const cells = Array.from(
    root.querySelectorAll<HTMLElement>('.tlTableView__headerCell, .tlTableView__cell'))
    .filter((cell) => cell.dataset.col === columnName);
  if (cells.length === 0) {
    return 0;
  }

  const box = document.createElement('div');
  box.style.cssText =
    'position:absolute;top:0;left:0;height:0;overflow:hidden;visibility:hidden;pointer-events:none';
  root.appendChild(box);
  try {
    const copies = cells.map((cell) => {
      const copy = cell.cloneNode(true) as HTMLElement;
      // The handle sits at the cell border and is no content; the ids would be duplicates while
      // the copy is in the document.
      copy.querySelectorAll('.tlTableView__resizeHandle').forEach((handle) => handle.remove());
      copy.querySelectorAll('[id]').forEach((element) => element.removeAttribute('id'));
      copy.style.position = 'static';
      copy.style.flex = 'none';
      copy.style.width = 'max-content';
      copy.style.minWidth = '0';
      copy.style.maxWidth = 'none';
      box.appendChild(copy);
      return copy;
    });
    // No copy is the last child of the box: the last cell of a row goes without its right border,
    // and a copy measured as one would come out that border short.
    box.appendChild(document.createElement('div'));
    return Math.ceil(copies.reduce((widest, copy) => Math.max(widest, copy.getBoundingClientRect().width), 0));
  } finally {
    box.remove();
  }
};

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * multi-selection with checkbox column, and column resize.
 */
/**
 * Elements that handle a click themselves: the native form controls, and the controls that carry
 * their role through ARIA instead of an element name — a dropdown, for one, is a `div` with
 * `role="combobox"`, so leaving those out made a click on it look like a click on plain cell text.
 */
const INTERACTIVE_SELECTOR =
  'input, textarea, select, button, a, [contenteditable="true"], '
  + '[role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], '
  + '[role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], '
  + '[role="slider"], [role="menu"], [role="menuitem"]';

/**
 * Whether the event originates from an interactive element inside a cell (input, button, link,
 * editor, dropdown). Row-level gestures must leave such clicks alone: neither steal the element's
 * focus for the table's keyboard scope, nor suppress its default mouse handling (e.g. double-click
 * word selection in a text input), nor read them as a row selection.
 */
function isInteractiveTarget(event: React.SyntheticEvent): boolean {
  const target = event.target as Element | null;
  return !!target?.closest?.(INTERACTIVE_SELECTOR);
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
  if (opts.col) {
    // Asked for one specific column: a cell whose control takes no caret (a dropdown, a checkbox)
    // must not send the focus to some other column - that would move the focus, and the horizontal
    // scroll position with it, away from the cell the user addressed.
    return null;
  }
  const ordered = opts.last ? [...cells].reverse() : cells;
  for (const c of ordered) {
    const found = c.querySelector<HTMLElement>(EDITABLE_SELECTOR);
    if (found) return found;
  }
  return null;
}

/**
 * Opens the column selection. Rendered either over the right edge of the header, where it needs a
 * strip of the header kept clear of the columns, or inside the heading of the rightmost pinned
 * column — that heading carries no label, so the button takes no room from the columns there.
 */
const ColumnsButton: React.FC<{
  title: string;
  inCell?: boolean;
  onClick: (event: React.MouseEvent) => void;
}> = ({ title, inCell, onClick }) => (
  <button
    type="button"
    className={'tlTableView__columnsButton' + (inCell ? ' tlTableView__columnsButton--inCell' : '')}
    title={title}
    aria-label={title}
    // In a heading, the gestures of the heading itself (sorting, dragging) are none of the
    // button's business.
    onMouseDown={(e) => e.stopPropagation()}
    onClick={onClick}
  >
    <i className="bi bi-gear" />
  </button>
);

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
  /** The column the rows are grouped by, empty when they are not grouped. */
  const grouping = (state.grouping as string) ?? '';
  const columnSelect = (state.columnSelect as boolean) ?? false;
  const filterBar = (state.filterBar as boolean) ?? false;
  const namedFilters = (state.namedFilters as NamedFilterState[]) ?? [];
  const activeNamedFilter = (state.activeNamedFilter as string) ?? '';
  const serverSearch = (state.search as string) ?? '';
  const filterSaving = (state.filterSaving as boolean) ?? false;

  const sortedColumnCount = React.useMemo(
    () => columns.filter((c) => c.sortPriority && c.sortPriority > 0).length,
    [columns]
  );

  const isMulti = selectionMode === 'multi';
  const checkboxWidth = 40;
  const treeIndentWidth = 20;

  const headerRef = React.useRef<HTMLDivElement>(null);
  const headerAreaRef = React.useRef<HTMLDivElement>(null);
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

  // -- Frozen column splitter state: the boundary the running drag would drop the frozen area at. --
  const [frozenPreview, setFrozenPreview] = React.useState<{ x: number; count: number } | null>(null);

  // Width of the body's vertical scrollbar. The header has none, so its viewport is that much wider
  // than the body's - and its scroll range that much shorter. Scrolled to the right end, the header
  // would stop before the body does and the headings would sit beside the wrong columns; the header
  // therefore ends with a reserve of this width. Measured rather than assumed: it depends on the
  // platform, and it is zero for an overlay scrollbar or a table short enough not to scroll.
  const [scrollbarWidth, setScrollbarWidth] = React.useState(0);

  React.useEffect(() => {
    const body = scrollContainerRef.current;
    if (!body) {
      return;
    }
    const measure = () => {
      const width = body.offsetWidth - body.clientWidth;
      setScrollbarWidth((previous) => (previous === width ? previous : width));
    };
    measure();
    const observer = new ResizeObserver(measure);
    observer.observe(body);
    return () => observer.disconnect();
  }, []);


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

  // The index of the last column the user arranges - the one that grows into the space left over,
  // since the pinned columns keep their width. -1 while every column is pinned.
  const lastUnpinnedIdx = React.useMemo(
    () => columns.reduce((last, col, i) => (col.pinnedEnd ? last : i), -1),
    [columns]);

  // How far the right edge of a pinned cell stays from the right edge of the table: the widths of
  // the pinned columns behind it. The reserve the row ends with is added where the cells are
  // rendered - it differs between the header and the body.
  const pinnedOffsets = React.useMemo(() => {
    const offsets = columns.map(() => 0);
    let right = 0;
    for (let i = columns.length - 1; i >= 0; i--) {
      if (!columns[i].pinnedEnd) {
        continue;
      }
      offsets[i] = right;
      right += getColWidth(columns[i]);
    }
    return offsets;
  }, [columns, getColWidth]);

  // Where the frozen area ends, measured from the left edge of the table: the frozen cells stick to
  // that edge, so this is a fixed position independent of the horizontal scroll offset.
  const frozenWidth = React.useMemo(() => {
    if (frozenColumnCount <= 0) {
      return 0;
    }
    let width = isMulti ? checkboxWidth : 0;
    for (let i = 0; i < frozenColumnCount && i < columns.length; i++) {
      width += getColWidth(columns[i]);
    }
    return width;
  }, [columns, frozenColumnCount, isMulti, checkboxWidth, getColWidth]);

  const totalHeight = totalRowCount * rowHeight;

  // -- Resize handlers --
  const resizeAutoScrollRef = React.useRef<number | null>(null);

  const handleResizeStart = React.useCallback((columnName: string, colWidth: number, event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    if (event.detail > 1) {
      // The second click of a double click fits the column to its content. A drag started here
      // would end on the same mouse up and report the width the fit is about to replace.
      return;
    }
    // The rendered width, not the configured one: the last column grows into the space the others
    // leave over, and starting from its configured width would snap it back the moment the drag
    // begins. The handle sits in the heading whose width is wanted.
    const heading = (event.currentTarget as HTMLElement).parentElement;
    const startWidth = heading ? Math.round(heading.getBoundingClientRect().width) : colWidth;
    resizeRef.current = { column: columnName, startX: event.clientX, startWidth };

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

  // Give the column the width its content needs, from the header context menu and from a double
  // click on the resize handle. Applied like the end of a resize drag: the override shows the new
  // width at once, the command keeps it.
  const fitColumnToContent = React.useCallback((columnName: string) => {
    const root = rootRef.current;
    if (!root) {
      return;
    }
    const width = Math.max(MIN_COL_WIDTH, measureColumnContentWidth(root, columnName));
    setColumnWidthOverrides((prev) => ({ ...prev, [columnName]: width }));
    sendCommand('columnResize', { column: columnName, width });
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
    if (!dragColumnRef.current || dragColumnRef.current === columnName
        || columns.find((c) => c.name === columnName)?.pinnedEnd) {
      setDragOver(null);
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
    const rect = (event.currentTarget as HTMLElement).getBoundingClientRect();
    const side = (event.clientX < rect.left + rect.width / 2) ? 'left' : 'right';
    setDragOver({ column: columnName, side });
  }, [columns]);

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
    // Operating a control inside an already selected row is not a selection gesture. Sending one
    // anyway would have the server re-render the row, and that answer overwrites the value the
    // control is sending at the same moment - the edit would be lost.
    const row = rows.find((r) => r.index === rowIndex);
    if (isInteractiveTarget(event) && row?.selected
        && !event.ctrlKey && !event.metaKey && !event.shiftKey) {
      return;
    }
    sendCommand('select', {
      rowIndex,
      ctrlKey: event.ctrlKey || event.metaKey,
      shiftKey: event.shiftKey,
    });
  }, [sendCommand, rows]);

  // A double-click opens the row: the server selects it and runs what the view configured for an
  // activation. A double-click inside an interactive cell element belongs to that element
  // (selecting a word in a text input), so it opens nothing.
  const handleRowActivate = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    if (isInteractiveTarget(event)) {
      return;
    }
    // The click that preceded this double-click already toggled the group, and a group row has
    // nothing to open beyond that.
    if (rows.find((r) => r.index === rowIndex)?.groupCount != null) {
      return;
    }
    sendCommand('activate', { rowIndex });
  }, [sendCommand, rows]);

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

  // Enter opens the row carrying the keyboard cursor. Declined (false) when no row does, or when
  // the focus sits in a cell element that answers Enter itself (a text input, an action button).
  const handleActivateCursor = React.useCallback(() => {
    if (cursorIndex < 0) {
      return false;
    }
    const active = document.activeElement as Element | null;
    if (active?.closest?.(FOCUSABLE_SELECTOR)) {
      return false;
    }
    sendCommand('activate', { rowIndex: cursorIndex });
    return true;
  }, [sendCommand, cursorIndex]);

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
    // The row has to be editable first; until then this is a no-op and waits out the select
    // round-trip. Once it is, the request is answered - successfully or not.
    if (!editableInRow(body, row.id)) {
      return;
    }
    pendingFocusRef.current = null;
    // A click that opened a control of its own (a dropdown's option list, a date picker) has moved
    // the focus out of the table on purpose; taking it back would close what was just opened.
    const active = document.activeElement;
    if (active && active !== document.body && !body.contains(active)) {
      return;
    }
    const input = editableInRow(body, row.id, { col: pending.col, last: pending.last });
    if (!input) {
      return;
    }
    // The cell was just clicked, so it is on screen: scrolling to it can only move the viewport away
    // from where the user is looking.
    input.focus({ preventScroll: true });
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

  const handleGroupBy = React.useCallback((column: string) => {
    sendCommand('group', { column });
    setContextMenu(null);
  }, [sendCommand]);

  const handleUngroup = React.useCallback(() => {
    sendCommand('group', { column: '' });
    setContextMenu(null);
  }, [sendCommand]);

  // -- Frozen column splitter: drag the boundary of the frozen area onto another column border. --
  const handleFrozenSplitStart = React.useCallback((event: React.MouseEvent) => {
    event.preventDefault();
    event.stopPropagation();
    const area = headerAreaRef.current;
    const header = headerRef.current;
    if (!area || !header) {
      return;
    }

    // The boundary snaps to a column border that is on screen right now. Measuring the rendered
    // header cells covers the frozen columns (sticky, at their fixed offsets) and the scrolled ones
    // alike, and it keeps the frozen area from growing wider than the visible table: a border that
    // has scrolled out of view is no candidate.
    const areaWidth = area.clientWidth;
    const options: { x: number; count: number }[] = [{ x: 0, count: 0 }];
    header.querySelectorAll<HTMLElement>('[data-col-idx]').forEach((cell) => {
      const colIdx = Number(cell.dataset.colIdx);
      if (columns[colIdx]?.pinnedEnd) {
        // A pinned column is fixed to the other edge: the frozen area never reaches it.
        return;
      }
      const x = cell.getBoundingClientRect().right - area.getBoundingClientRect().left;
      if (x > 0 && x <= areaWidth) {
        options.push({ x, count: colIdx + 1 });
      }
    });

    let target = { x: frozenWidth, count: frozenColumnCount };
    const move = (e: MouseEvent) => {
      const x = e.clientX - area.getBoundingClientRect().left;
      target = options.reduce(
        (best, option) => (Math.abs(option.x - x) < Math.abs(best.x - x) ? option : best), options[0]);
      setFrozenPreview(target);
    };
    const up = () => {
      document.removeEventListener('mousemove', move);
      document.removeEventListener('mouseup', up);
      setFrozenPreview(null);
      if (target.count !== frozenColumnCount) {
        sendCommand('setFrozenColumnCount', { count: target.count });
      }
    };
    document.addEventListener('mousemove', move);
    document.addEventListener('mouseup', up);
  }, [columns, frozenWidth, frozenColumnCount, sendCommand]);

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

  // -- Column selection: open the server-side dialog choosing the displayed columns. --
  const handleOpenColumnSelect = React.useCallback((event: React.MouseEvent) => {
    event.stopPropagation();
    event.preventDefault();
    sendCommand('openColumnSelect', {});
  }, [sendCommand]);

  // -- Filter bar: named filters, the cross-column search, saving the current criteria. --

  // The typed term is held locally while a send is pending: the input stays responsive between
  // round-trips, and the echoed server state cannot move the caret while the user is still typing.
  const [searchDraft, setSearchDraft] = React.useState(serverSearch);
  const searchPendingRef = React.useRef(false);
  const searchTimeoutRef = React.useRef<number | null>(null);

  React.useEffect(() => {
    if (!searchPendingRef.current) {
      setSearchDraft(serverSearch);
    }
  }, [serverSearch]);

  React.useEffect(() => () => {
    if (searchTimeoutRef.current !== null) {
      clearTimeout(searchTimeoutRef.current);
    }
  }, []);

  const sendSearch = React.useCallback((term: string) => {
    if (searchTimeoutRef.current !== null) {
      clearTimeout(searchTimeoutRef.current);
      searchTimeoutRef.current = null;
    }
    searchPendingRef.current = false;
    sendCommand('search', { term });
  }, [sendCommand]);

  const handleSearchChange = React.useCallback((term: string) => {
    setSearchDraft(term);
    searchPendingRef.current = true;
    if (searchTimeoutRef.current !== null) {
      clearTimeout(searchTimeoutRef.current);
    }
    searchTimeoutRef.current = window.setTimeout(() => sendSearch(term), SEARCH_DEBOUNCE_MS);
  }, [sendSearch]);

  const handleSearchKeyDown = React.useCallback((event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      // Sends the term without waiting out the pause; the scope default must not also fire.
      event.preventDefault();
      sendSearch(event.currentTarget.value);
    }
  }, [sendSearch]);

  // Clicking a chip applies its criteria; clicking the active one withdraws them, so the chip
  // reads as a switch rather than as a command that can only be undone elsewhere.
  const handleNamedFilter = React.useCallback((id: string) => {
    if (id === activeNamedFilter) {
      sendCommand('clearFilter', {});
    } else {
      sendCommand('applyNamedFilter', { id });
    }
  }, [activeNamedFilter, sendCommand]);

  const handleDeleteNamedFilter = React.useCallback((id: string, event: React.MouseEvent) => {
    event.stopPropagation();
    sendCommand('deleteNamedFilter', { id });
  }, [sendCommand]);

  // The name being typed for a filter to save; null while the save affordance is a plain button.
  const [saveName, setSaveName] = React.useState<string | null>(null);

  const handleSaveSubmit = React.useCallback(() => {
    const name = (saveName ?? '').trim();
    if (!name) {
      return;
    }
    sendCommand('saveNamedFilter', { filterName: name });
    setSaveName(null);
  }, [saveName, sendCommand]);

  const handleSaveKeyDown = React.useCallback((event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      handleSaveSubmit();
    } else if (event.key === 'Escape') {
      // Stops the enclosing dialog/window from reading the Escape as "close me".
      event.preventDefault();
      setSaveName(null);
    }
  }, [handleSaveSubmit]);

  // -- Computed values --
  const tableWidth = columns.reduce((sum, col) => sum + getColWidth(col), 0)
    + (isMulti ? checkboxWidth : 0);

  // A table ending in a pinned column has a heading without a label there, so the column button
  // goes into that heading: it then needs no room of its own, and the pinned column reaches the
  // right edge of the table.
  const cogInHeaderCell = columnSelect && columns.length > 0 && !!columns[columns.length - 1].pinnedEnd;

  // Without such a heading, both the header row and the body end this much behind the last column,
  // keeping the column button clear of it: otherwise the button covers the last column's funnel as
  // soon as the columns fill the available width, and that filter cannot be opened at all. Matches
  // the button's CSS width (2rem), and applies to the body as well so that scrolling to the right
  // end frees the funnel there, too.
  // Kept as padding rather than width: the cells live in the content box, so the reserve widens the
  // scroll range without offering the last cell space to grow into and without a sticky cell -
  // confined to the content box - ever reaching underneath the button.
  const buttonReserve = columnSelect && !cogInHeaderCell ? 32 : 0;

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
      onActivate={handleActivateCursor}
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
      {/* Filter bar above the headings: the named criteria as chips, the cross-column search, and
          saving the current criteria under a name. Outside both scrollers, so it neither scrolls
          with the columns nor takes part in the header/body width alignment. */}
      {filterBar && (
        <div className="tlTableView__filterBar">
          {namedFilters.length > 0 && (
            <div className="tlTableView__filterChips">
              {namedFilters.map((named) => {
                const isActive = named.id === activeNamedFilter;
                return (
                  <span
                    key={named.id}
                    className={'tlTableView__chip' + (isActive ? ' tlTableView__chip--active' : '')}
                  >
                    <button
                      type="button"
                      className="tlTableView__chipLabel"
                      aria-pressed={isActive}
                      title={isActive ? i18n['js.table.clearFilter'] : named.label}
                      onClick={() => handleNamedFilter(named.id)}
                    >
                      {named.label}
                    </button>
                    {/* Only a filter of the user's own can be deleted; a declared one is part of
                        the table and offers no remove affordance. */}
                    {named.deletable && (
                      <button
                        type="button"
                        className="tlTableView__chipRemove"
                        title={i18n['js.table.deleteFilter']}
                        aria-label={i18n['js.table.deleteFilter']}
                        onClick={(e) => handleDeleteNamedFilter(named.id, e)}
                      >
                        &times;
                      </button>
                    )}
                  </span>
                );
              })}
            </div>
          )}
          <div className="tlTableView__search" title={i18n['js.table.searchHint']}>
            <i className="bi bi-search" aria-hidden="true" />
            <input
              type="search"
              className="tlTableView__searchInput"
              placeholder={i18n['js.table.search']}
              aria-label={i18n['js.table.searchHint']}
              value={searchDraft}
              onChange={(e) => handleSearchChange(e.target.value)}
              onKeyDown={handleSearchKeyDown}
            />
          </div>
          {filterSaving && (saveName === null ? (
            <button
              type="button"
              className="tlTableView__barButton"
              title={i18n['js.table.saveFilter']}
              aria-label={i18n['js.table.saveFilter']}
              onClick={() => setSaveName('')}
            >
              <i className="bi bi-bookmark-plus" />
            </button>
          ) : (
            <div className="tlTableView__saveForm">
              <input
                type="text"
                className="tlTableView__saveInput"
                autoFocus
                placeholder={i18n['js.table.filterName']}
                aria-label={i18n['js.table.filterName']}
                value={saveName}
                onChange={(e) => setSaveName(e.target.value)}
                onKeyDown={handleSaveKeyDown}
              />
              <button
                type="button"
                className="tlTableView__barButton"
                title={i18n['js.table.saveFilter']}
                aria-label={i18n['js.table.saveFilter']}
                disabled={!saveName.trim()}
                onClick={handleSaveSubmit}
              >
                <i className="bi bi-check-lg" />
              </button>
              <button
                type="button"
                className="tlTableView__barButton"
                title={i18n['js.table.cancelSave']}
                aria-label={i18n['js.table.cancelSave']}
                onClick={() => setSaveName(null)}
              >
                <i className="bi bi-x-lg" />
              </button>
            </div>
          ))}
        </div>
      )}

      {/* Header, plus the column selection sitting above the body's vertical scrollbar */}
      <div className="tlTableView__headerArea" ref={headerAreaRef}>
      <div className="tlTableView__header" ref={headerRef}>
        {/* Fills the header even when the columns are narrower: a cell sticking to the right edge
            cannot leave its row, so a row ending with the last column would hold the pinned cells
            back from that edge. The reserve is padding, which a sticky cell never enters. */}
        <div className="tlTableView__headerRow"
          style={{ minWidth: tableWidth, paddingRight: buttonReserve + scrollbarWidth }}>
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
            let cellClass = 'tlTableView__headerCell';
            if (col.sortable) cellClass += ' tlTableView__headerCell--sortable';
            if (dragOver && dragOver.column === col.name) {
              cellClass += ' tlTableView__headerCell--dragOver-' + dragOver.side;
            }
            const isFrozen = colIdx < frozenColumnCount;
            const isFrozenLast = colIdx === frozenColumnCount - 1;
            if (isFrozen) cellClass += ' tlTableView__headerCell--frozen';
            if (isFrozenLast) cellClass += ' tlTableView__headerCell--frozenLast';
            const isPinned = !!col.pinnedEnd;
            if (isPinned) cellClass += ' tlTableView__headerCell--pinnedEnd';
            if (isPinned && colIdx === lastUnpinnedIdx + 1) {
              cellClass += ' tlTableView__headerCell--pinnedEndFirst';
            }
            if (col.cssClass) cellClass += ' ' + col.cssClass;
            return (
              <div
                key={col.name}
                className={cellClass}
                data-col={col.name}
                data-col-idx={colIdx}
                style={{
                  // The last column the user arranges takes the space left over, in the heading
                  // exactly as in the rows - otherwise the two drift apart as soon as the columns
                  // no longer fill the table.
                  ...(colIdx === lastUnpinnedIdx && !isFrozen
                    ? { flex: '1 0 auto', minWidth: w }
                    : { width: w, minWidth: w }),
                  position: isFrozen || isPinned ? 'sticky' as const : 'relative' as const,
                  ...(isFrozen ? { left: frozenOffsets[colIdx], zIndex: 2 } : {}),
                  // The header ends with the reserve the body's scrollbar and, where it is not in a
                  // heading, the column button take; its cells therefore stick that much further
                  // from the right edge than the body's - which is what puts a heading above its
                  // column at every scroll position.
                  ...(isPinned
                    ? {
                      right: pinnedOffsets[colIdx] + buttonReserve + scrollbarWidth,
                      zIndex: 2,
                    }
                    : {}),
                }}
                draggable={!isPinned}
                onClick={col.sortable ? (e) => handleSort(col.name, col.sortDirection, e) : undefined}
                onContextMenu={(e) => handleColumnContextMenu(colIdx, e)}
                onDragStart={(e) => handleDragStart(col.name, e)}
                onDragOver={(e) => handleDragOver(col.name, e)}
                onDrop={handleDrop}
                onDragEnd={handleDragEnd}
              >
                <span className="tlTableView__headerLabel">{col.label}</span>
                {col.name === grouping && (
                  <i className="tlTableView__groupMark bi bi-collection"
                    title={i18n['js.table.grouped']} aria-hidden="true" />
                )}
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
                {cogInHeaderCell && colIdx === columns.length - 1 && (
                  <ColumnsButton title={i18n['js.table.columns']} inCell onClick={handleOpenColumnSelect} />
                )}
                {!isPinned && (
                  <div
                    className="tlTableView__resizeHandle"
                    onMouseDown={(e) => handleResizeStart(col.name, w, e)}
                    onClick={(e) => e.stopPropagation()}
                    onDoubleClick={(e) => {
                      e.stopPropagation();
                      fitColumnToContent(col.name);
                    }}
                  />
                )}
              </div>
            );
          })}
          {/* Drop zone for reordering past the last column */}
          <div
            style={{ flex: '0 0 0', minHeight: '100%' }}
            onDragOver={(e) => {
              if (!dragColumnRef.current) return;
              if (lastUnpinnedIdx >= 0) {
                const lastCol = columns[lastUnpinnedIdx];
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
        {/* Grip on the boundary of the frozen columns. Confined to the header: a grip running down
            the body would swallow the clicks on the cells behind it. */}
        <div
          className={'tlTableView__frozenSplitter'
            + (frozenPreview ? ' tlTableView__frozenSplitter--active' : '')}
          style={{ left: frozenWidth }}
          title={i18n['js.table.freezeSplitter']}
          onMouseDown={handleFrozenSplitStart}
        />
        {columnSelect && !cogInHeaderCell && (
          <ColumnsButton title={i18n['js.table.columns']} onClick={handleOpenColumnSelect} />
        )}
      </div>

      {/* Scrollable body (focusable so keyboard row navigation can target it) */}
      <div
        ref={scrollContainerRef}
        className="tlTableView__body"
        onScroll={handleScroll}
        onKeyDown={handleBodyKeyDown}
        tabIndex={0}
      >
        {/* Spacer for virtual scrolling. Fills the body when the columns are narrower than it, so
            the rows reach the right edge and a cell pinned there lands on it; the reserve is
            padding, so it widens the scroll range without taking any cell along. */}
        <div style={{
          height: totalHeight, position: 'relative', minWidth: tableWidth, paddingRight: buttonReserve,
        }}>
          {rows.map((row) => (
            <div
              key={row.id}
              className={
                'tlTableView__row' +
                (row.selected ? ' tlTableView__row--selected' : '') +
                (row.index === cursorIndex ? ' tlTableView__row--cursor' : '') +
                (row.groupCount != null ? ' tlTableView__row--group' : '')
              }
              style={{
                position: 'absolute',
                top: row.index * rowHeight,
                height: rowHeight,
                // Spans the spacer, hence the body, so a pinned cell reaches its right edge. The
                // cells stop in front of the reserve, exactly as the header's do.
                left: 0,
                right: 0,
                paddingRight: buttonReserve,
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
              onDoubleClick={(e) => handleRowActivate(row.index, e)}
            >
              {isMulti && (
                <div className={'tlTableView__cell tlTableView__checkboxCell'
                    + (frozenColumnCount > 0 ? ' tlTableView__cell--frozen' : '')}
                  style={{
                    width: checkboxWidth, minWidth: checkboxWidth,
                    ...(frozenColumnCount > 0 ? { position: 'sticky' as const, left: 0, zIndex: 2 } : {}),
                  }}
                  onClick={(e) => e.stopPropagation()}>
                  {row.groupCount == null && (
                    <input
                      type="checkbox"
                      className="tlTableView__checkbox"
                      checked={row.selected}
                      onChange={() => {/* handled by onClick */}}
                      onClick={(e) => handleCheckboxClick(row.index, e)}
                      tabIndex={-1}
                    />
                  )}
                </div>
              )}
              {columns.map((col, colIdx) => {
                const w = getColWidth(col);
                const isFrozen = colIdx < frozenColumnCount;
                const isFrozenLast = colIdx === frozenColumnCount - 1;
                let cellClass = 'tlTableView__cell';
                if (isFrozen) cellClass += ' tlTableView__cell--frozen';
                if (isFrozenLast) cellClass += ' tlTableView__cell--frozenLast';
                const isPinned = !!col.pinnedEnd;
                if (isPinned) cellClass += ' tlTableView__cell--pinnedEnd';
                if (isPinned && colIdx === lastUnpinnedIdx + 1) {
                  cellClass += ' tlTableView__cell--pinnedEndFirst';
                }
                if (col.cssClass) cellClass += ' ' + col.cssClass;
                const isTreeColumn = treeMode && colIdx === 0;
                const treeDepth = row.treeDepth ?? 0;
                return (
                  <div
                    key={col.name}
                    className={cellClass}
                    data-row={row.id}
                    data-col={col.name}
                    style={{
                      // The last column the user arranges takes the space left over; a pinned
                      // column keeps its width, so the space stays in front of it.
                      ...(colIdx === lastUnpinnedIdx && !isFrozen
                        ? { flex: '1 0 auto', minWidth: w }
                        : { width: w, minWidth: w }),
                      ...(isFrozen ? { position: 'sticky' as const, left: frozenOffsets[colIdx], zIndex: 2 } : {}),
                      ...(isPinned
                        ? {
                          position: 'sticky' as const,
                          right: pinnedOffsets[colIdx] + buttonReserve,
                          zIndex: 2,
                        }
                        : {}),
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
                        {/* A row that predates the current columns has no control for a newly shown
                            column yet \u2014 leave that cell empty rather than tearing down the table. */}
                        {row.cells[col.name] && <TLChild control={row.cells[col.name]} />}
                        {row.groupCount != null && (
                          <span className="tlTableView__groupCount">({row.groupCount})</span>
                        )}
                      </div>
                    ) : (
                      row.cells[col.name] && <TLChild control={row.cells[col.name]} />
                    )}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>

      {/* Where the frozen area would end if the splitter were dropped now. Drawn over the whole
          table, so the boundary can be judged against the rows, not only against the headings. */}
      {frozenPreview && (
        <div className="tlTableView__frozenPreview" style={{ left: frozenPreview.x }} />
      )}

      {/* Column context menu */}
      {contextMenu && (
        <div
          className="tlMenu"
          role="menu"
          style={{ position: 'fixed', top: contextMenu.y, left: contextMenu.x, zIndex: 10000 }}
          onMouseDown={(e) => e.stopPropagation()}
        >
          {contextMenu.colIdx + 1 !== frozenColumnCount
              && !columns[contextMenu.colIdx]?.pinnedEnd && (
            <button type="button" className="tlMenu__item" role="menuitem" onClick={handleFreezeUpTo}>
              <span className="tlMenu__label">{i18n['js.table.freezeUpTo']}</span>
            </button>
          )}
          {frozenColumnCount > 0 && (
            <button type="button" className="tlMenu__item" role="menuitem" onClick={handleUnfreezeAll}>
              <span className="tlMenu__label">{i18n['js.table.unfreezeAll']}</span>
            </button>
          )}
          {!columns[contextMenu.colIdx]?.pinnedEnd && (
            <button type="button" className="tlMenu__item" role="menuitem"
              onClick={() => {
                fitColumnToContent(columns[contextMenu.colIdx].name);
                setContextMenu(null);
              }}>
              <span className="tlMenu__label">{i18n['js.table.fitColumn']}</span>
            </button>
          )}
          {columns[contextMenu.colIdx]?.groupable
              && columns[contextMenu.colIdx].name !== grouping && (
            <button type="button" className="tlMenu__item" role="menuitem"
              onClick={() => handleGroupBy(columns[contextMenu.colIdx].name)}>
              <span className="tlMenu__label">{i18n['js.table.groupBy']}</span>
            </button>
          )}
          {grouping !== '' && (
            <button type="button" className="tlMenu__item" role="menuitem" onClick={handleUngroup}>
              <span className="tlMenu__label">{i18n['js.table.ungroup']}</span>
            </button>
          )}
        </div>
      )}
    </div>
    </KeyboardScopeProvider>
  );
};

export default TLTableView;
