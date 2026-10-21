import { React, useTLState, useTLCommand, TLChild, useI18N, KeyboardScopeProvider, useKeyboardBinding, writeDragPayload, runningDrag, onDragEnd, afterDragImage, readDragPayload, dragKindAccepted, dragModes, flatZoneSplit, treeZoneSplit, dropZoneAt, startPointerDrag, useFill, rootClassName, stateAttr, tokenPx, useTokenPx, useListReorder, reorderState, usePopover, ThemeIcon, tooltipProps, TOOLTIP_WHEN_CLIPPED, createPortal, isEditorTarget, useFocusAfterRemove, usePart, ICON_SORT_ASC, ICON_SORT_DESC, ICON_FILTER, ICON_GROUP, ICON_COLUMNS, ICON_SAVE, ICON_CONFIRM, ICON_CANCEL, separatorMove, separatorStep, movedSize } from 'tl-react-bridge';
import type { TLCellProps, TLDropZone, TLDropMarker, TLRunningDrag } from 'tl-react-bridge';
import { isInteractiveTarget, isOperableTarget } from './interactive';
import { NO_DRAG_IMAGE } from './drop-hint';
import { DropHint } from './DropHint';
import type { HintAnchor } from './DropHint';
import type { DropVerdict } from './drop-hint';
import { Menu, MenuItem } from './menu/Menu';
import { SearchField } from './SearchField';
import { chipFocusTargets } from './chip/chipFocus';

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
  onExpand: (expanded: boolean) => void;
  onGrid: () => boolean;
  inEditor: () => boolean;
}> = ({ isMulti, cursorIndex, onMove, onToggle, onSelectAll, onActivate, onExpand, onGrid, inEditor }) => {
  // An editor in a cell - a text input, a dropdown, a checkbox, an editable text - keeps every key
  // it answers itself: the grid declines them, so the arrows move the caret or the option and Space
  // and Enter reach the editor.
  const move = (direction: string, extend: boolean, cursorOnly: boolean) => {
    if (inEditor()) {
      return false;
    }
    onMove(direction, extend, cursorOnly);
    return true;
  };
  // Right opens the cursor row of a tree or group, left closes it - as in the tree. Where there is
  // nothing to open or close, the arrows scroll the body sideways (the focus is on the grid, not on
  // the scrolling body). Only while the grid itself has the focus: a dropdown, a checkbox, a chip or
  // a button in the table keeps its arrows.
  useKeyboardBinding('ArrowRight', () => { if (!onGrid()) { return false; } onExpand(true); return true; });
  useKeyboardBinding('ArrowLeft', () => { if (!onGrid()) { return false; } onExpand(false); return true; });
  useKeyboardBinding('ArrowUp', () => move('up', false, false));
  useKeyboardBinding('ArrowDown', () => move('down', false, false));
  useKeyboardBinding('Home', () => move('home', false, false));
  useKeyboardBinding('End', () => move('end', false, false));
  useKeyboardBinding('PageUp', () => move('pageUp', false, false));
  useKeyboardBinding('PageDown', () => move('pageDown', false, false));
  // Shift extends the range (multi only); in single selection it behaves like a plain move.
  useKeyboardBinding('Shift+ArrowUp', () => move('up', isMulti, false));
  useKeyboardBinding('Shift+ArrowDown', () => move('down', isMulti, false));
  useKeyboardBinding('Shift+Home', () => move('home', isMulti, false));
  useKeyboardBinding('Shift+End', () => move('end', isMulti, false));
  useKeyboardBinding('Shift+PageUp', () => move('pageUp', isMulti, false));
  useKeyboardBinding('Shift+PageDown', () => move('pageDown', isMulti, false));
  // Ctrl moves the focus cursor without changing the selection (multi only).
  useKeyboardBinding('Ctrl+ArrowUp', () => move('up', false, isMulti));
  useKeyboardBinding('Ctrl+ArrowDown', () => move('down', false, isMulti));
  // Space toggles the cursor row - on the grid only: on a button or a checkbox in the table it is
  // that control's. Ctrl+A selects all (multi only).
  useKeyboardBinding('Space', () => { if (cursorIndex < 0 || !onGrid()) { return false; } onToggle(); return true; });
  useKeyboardBinding('Ctrl+A', () => { if (!isMulti || inEditor()) { return false; } onSelectAll(); return true; });
  // Enter opens the cursor row; it declines when there is nothing to open, so the gesture falls
  // through to an enclosing scope (a dialog's default button).
  useKeyboardBinding('Enter', () => !inEditor() && onActivate());
  return null;
};

const I18N_KEYS = {
  'js.table.selectAll': 'Select all rows',
  'js.table.selectRow': 'Select this row',
  'js.table.empty': 'No entries',
  'js.treeView.expand': 'Expand',
  'js.treeView.collapse': 'Collapse',
  'js.table.freezeUpTo': 'Freeze up to here',
  'js.table.unfreezeAll': 'Unfreeze all',
  'js.table.groupBy': 'Group by this column',
  'js.table.ungroup': 'Remove grouping',
  'js.table.fitColumn': 'Fit width to content',
  'js.table.grouped': 'The rows are grouped by this column',
  'js.table.freezeSplitter': 'Drag to choose the columns that stay in place while scrolling',
  'js.table.filter': 'Filter',
  'js.table.filterActive': 'Filter (active)',
  'js.table.columnWidth': 'Change the width of {0}',
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

/**
 * How long a mounted table waits for its body to be able to scroll back to the window it showed
 * last. The containers fill within a few renders; a body that still cannot scroll that far then has
 * a shorter scroll range than before.
 */
const RESTORE_TIMEOUT_MS = 1000;

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
  /**
   * What the column's label says about itself over and above its text, offered on the heading.
   * Absent for a label that describes itself.
   */
  tooltip?: string;
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
  cells: Record<string, unknown>;
  /**
   * The tooltip of the cells that say more than they display, by column name. A cell named here
   * offers this text; the others offer their own text while it does not fit.
   */
  tooltips?: Record<string, string>;
  /** The CSS class the row's element carries in addition to its own classes; absent for none. */
  cssClass?: string;
  /**
   * The CSS class of the cells that are marked, by column name. A cell named here carries it in
   * addition to its column's class; the others carry the column's class only.
   */
  cellClasses?: Record<string, string>;
  treeDepth?: number;
  expandable?: boolean;
  expanded?: boolean;
  /** Present exactly on a group header row: how many rows the group holds. */
  groupCount?: number;
  /** Whether the row may be dragged; present while the table's rows are draggable at all. */
  draggable?: boolean;
}

/** The selection of a table that has no row selected, shared so that it keeps its identity. */
const NO_ROWS: number[] = [];

/** Command asking the server whether a drop at the hovered target would be accepted. */
const CMD_DROP_PROBE = 'dropProbe';

/** Command applying a drop (DropSupport.CMD_DROP). */
const CMD_DROP = 'drop';

/** State word (drag.css) of each marker drawn at a row. */
const ROW_MARKER_STATE: Record<Exclude<TLDropMarker, 'control'>, string> = {
  before: 'drop-before',
  after: 'drop-after',
  into: 'drop-onto',
};

/** Where a running drag hovers the table: a row and the zone within it, or beside the rows. */
interface DropState {
  /** The hovered row, `null` beside the rows. */
  row: string | null;
  zone: TLDropZone;
  /** Identifier of the probe asking about this target, `null` for a drag not started here. */
  probe: string | null;
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
    root.querySelectorAll<HTMLElement>('.tl-table__header-cell, .tl-table__cell'))
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
      copy.querySelectorAll('.tl-table__resize').forEach((handle) => handle.remove());
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
 * The attribute marking the chip of a named filter that can be deleted, by which the focus finds the
 * neighbour of a deleted one.
 */
const FILTER_CHIP_ATTR = 'data-tl-filter-chip';

/**
 * Opens the column selection. Rendered either over the right edge of the header, where it needs a
 * strip of the header kept clear of the columns, or inside the heading of the rightmost pinned
 * column — that heading carries no label, so the button takes no room from the columns there. The
 * strip (tl-table__columns) places the button, the part `Button` draws it.
 */
const ColumnsButton: React.FC<{
  label: string;
  inCell?: boolean;
  onClick: (event: React.MouseEvent) => void;
}> = ({ label, inCell, onClick }) => {
  const Button = usePart('Button');
  return (
    <div className="tl-table__columns">
      <Button
        label={label}
        image={ICON_COLUMNS}
        display="icon-only"
        appearance="ghost"
        small={inCell}
        // In a heading, the gestures of the heading itself (sorting, dragging) are none of the
        // button's business.
        onMouseDown={(e) => e.stopPropagation()}
        onClick={onClick}
      />
    </div>
  );
};

/**
 * React table component with virtual scrolling, server-driven cell controls,
 * multi-selection with checkbox column, and column resize.
 *
 * The table takes part in the fill contract (see {@link useFill}) and always fills: its body only
 * renders the rows its viewport shows, so the table needs a height bounded by its container rather
 * than one following its rows. Its containers fill in turn, up to the next bounded region.
 */
/** A highlight class of the application among the given classes (#29815). */
const STATUS_CLASS = /(?:^|\s)tl-status-(error|warning|success|info)(?=\s|$)/;

/**
 * The status role the highlight class among the given classes stands for: `tl-status-error`,
 * `-warning`, `-success` or `-info`, which the design system draws as `data-tl-role` on the row or the
 * cell. Undefined without such a class.
 */
function statusRole(cssClass: string | undefined): string | undefined {
  return cssClass ? STATUS_CLASS.exec(cssClass)?.[1] : undefined;
}

const TLTableView: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const fillClass = useFill(true);
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');
  const DisclosureToggle = usePart('DisclosureToggle');
  const Checkbox = usePart('Checkbox');
  const TextInput = usePart('TextInput');
  const Chip = usePart('Chip');
  const rootRef = React.useRef<HTMLDivElement>(null);
  // The grid (role=grid) holds the keyboard focus and points at the cursor row with
  // aria-activedescendant; the body inside it only scrolls.
  const gridRef = React.useRef<HTMLDivElement>(null);
  const frameRef = React.useRef<HTMLDivElement>(null);
  // Whether the keyboard focus is on the grid itself, not on a control within it (Left, Right, Space).
  const isGridFocused = React.useCallback(() => document.activeElement === gridRef.current, []);
  // Whether the keyboard focus is in an editor within the grid - a cell's input, dropdown, checkbox
  // or editable text, the heading's checkbox: the grid leaves it its keys.
  const isEditorFocused = React.useCallback(() => {
    const active = document.activeElement;
    return !!active && active !== gridRef.current && !!gridRef.current?.contains(active)
      && isEditorTarget(active);
  }, []);

  const columns = (state.columns as ColumnState[]) ?? [];
  const totalRowCount = (state.totalRowCount as number) ?? 0;
  const rows = (state.rows as RowState[]) ?? [];
  /** The first row of the window the body showed last, unlike the start of the rows sent. */
  const scrollStart = (state.scrollStart as number) ?? 0;
  // The client measures what the design system says (3b §3): a row is size-row high, so the table
  // follows the density (36 px normal, 32 px compact). The server knows no height. The grid is
  // rendered from the first layout on, so the token is read where the rows inherit it.
  const rowHeight = useTokenPx(gridRef, '--tl-size-row', 36);
  const selectionMode = (state.selectionMode as string) ?? 'single';
  /**
   * What the select-all checkbox shows, decided by the server from how many of the rows the
   * select-all acts on - the rows the filter lets pass, collapsed group members included - are
   * selected: `none`, `some` or `all`.
   */
  const selectAllState = (state.selectAllState as string) ?? 'none';
  const cursorIndex = (state.cursorIndex as number) ?? -1;
  /**
   * The indexes of the selected rows among the rows sent. The selection is a state of its own, not a
   * flag of each row, so a change of it reaches the client without the rows.
   */
  const selectedRows = (state.selectedRows as number[] | undefined) ?? NO_ROWS;
  const selectedIndexes = React.useMemo(() => new Set(selectedRows), [selectedRows]);
  const isSelected = (row: RowState) => selectedIndexes.has(row.index);
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
  const dragEnabled = (state.dragEnabled as boolean) ?? false;
  const dragKind = (state.dragKind as string | null) ?? undefined;
  const dropAcceptsAny = (state.dropAcceptsAny as boolean) ?? false;
  const dropAccepts = (state.dropAccepts as string[]) ?? [];
  const dropModes = (state.dropModes as string[]) ?? [];
  const dropModesByKind = (state.dropModesByKind as Record<string, string[]>) ?? {};
  // Rows forming a tree are split as tree nodes: an insertion has a middle part going into the row.
  const dropTreeZones = (state.dropTreeZones as boolean) ?? false;
  const dropVerdicts = (state.dropVerdicts as Record<string, DropVerdict>) ?? {};
  /** How many rows the table has (of how many, while filtered); empty while it shows no count. */
  const rowCount = (state.rowCount as string) ?? '';
  /** How many rows are selected; empty while nothing is or the table selects a single row only. */
  const rowCountSelected = (state.rowCountSelected as string) ?? '';

  const sortedColumnCount = React.useMemo(
    () => columns.filter((c) => c.sortPriority && c.sortPriority > 0).length,
    [columns]
  );

  const isMulti = selectionMode === 'multi';
  // The selection column is a square as high as the heading (size-bar); a tree level indents by the
  // width of its toggle (size-control-sm), so a child stands under its parent's label; the column
  // button is a control-sized square.
  const checkboxWidth = useTokenPx(gridRef, '--tl-size-bar', 40);
  const treeIndentWidth = useTokenPx(gridRef, '--tl-size-control-sm', 24);
  const columnsButtonWidth = useTokenPx(gridRef, '--tl-size-control', 32);
  const treegrid = treeMode || grouping !== '';
  const rowId = (index: number) => `${controlId}-row-${index}`;

  const headerRef = React.useRef<HTMLDivElement>(null);
  const scrollContainerRef = React.useRef<HTMLDivElement>(null);
  const scrollTimeoutRef = React.useRef<number | null>(null);

  // Whether the pointer pressed on the body and has not been released yet.
  const bodyPressRef = React.useRef(false);

  // Hands a focus that landed on the body on to the grid. A focus a press put there moves on only
  // once the press is over (released, or ended as a drag): Chromium starts no drag from a press
  // during which the focus moved away from where the press put it, so no row could be dragged.
  const bodyFocusToGrid = React.useCallback(() => {
    if (document.activeElement === scrollContainerRef.current) {
      gridRef.current?.focus({ preventScroll: true });
    }
  }, []);

  const handleBodyPress = React.useCallback(() => {
    bodyPressRef.current = true;
    const pressOver = () => {
      window.removeEventListener('pointerup', pressOver, true);
      window.removeEventListener('dragend', pressOver, true);
      bodyPressRef.current = false;
      bodyFocusToGrid();
    };
    window.addEventListener('pointerup', pressOver, true);
    window.addEventListener('dragend', pressOver, true);
  }, [bodyFocusToGrid]);

  const handleBodyFocus = React.useCallback(() => {
    if (!bodyPressRef.current) {
      bodyFocusToGrid();
    }
  }, [bodyFocusToGrid]);

  // A cell whose editable input should receive the caret once the server has re-rendered its row
  // as editable. Set on a selecting cell-click (edit the clicked cell) and on Tab-wrap to a
  // neighbouring row; consumed by the focus effect below. Addressed by row index (stable across
  // virtual scrolling) rather than the transient row id.
  const pendingFocusRef = React.useRef<{ index: number; col?: string; last?: boolean } | null>(null);

  // -- Resize state --
  const [columnWidthOverrides, setColumnWidthOverrides] = React.useState<Record<string, number>>({});
  const resizeRef = React.useRef<{ column: string; startX: number; startWidth: number } | null>(null);
  const justResizedRef = React.useRef(false);

  // -- Column reordering: the common gesture (useListReorder, halves along the row of headings) --
  const columnReorder = useListReorder({
    axis: 'horizontal',
    count: columns.length,
    onMove: (from, to) => sendCommand('columnReorder', { column: columns[from].name, targetIndex: to }),
  });
  const columnDragging = columnReorder.dragIndex !== null;

  // -- Row drop state: where an accepted drag currently hovers, or null while none does. A null row
  //    means the drag hovers the table itself rather than one of its rows. --
  const [dropState, setDropState] = React.useState<DropState | null>(null);

  // -- Drop probes sent for the running drag, by probe identifier: each target is asked once per
  //    drag. Reset when a probe of another drag is sent. --
  const probesRef = React.useRef<{ drag: string; sent: Set<string> } | null>(null);

  // The verdict on the hovered target: undefined while no verdict has arrived, which counts as
  // accepted until the server says otherwise.
  const dropVerdict: DropVerdict | undefined = dropState?.probe ? dropVerdicts[dropState.probe] : undefined;
  const dropRefused = dropVerdict !== undefined && !dropVerdict.accepted;

  // -- The rows a drag started here carries, faded by drag.css (data-tl-state~="dragging") while it
  //    runs: the grabbed row, or the whole selection when the grabbed row is part of it. --
  const [draggedRow, setDraggedRow] = React.useState<{ id: string; selection: boolean } | null>(null);
  // The grabbed row may scroll out of the rendered window and take its dragend handler with it.
  React.useEffect(() => (draggedRow ? onDragEnd(() => setDraggedRow(null)) : undefined), [draggedRow]);

  // The marker of the drop accepted at the hovered place. Until the verdict on a newly hovered place
  // arrives, the marker of the place hovered before stays, so the marker does not flicker.
  const lastAcceptedRef = React.useRef<DropVerdict | null>(null);
  if (dropState === null || dropRefused) {
    lastAcceptedRef.current = null;
  } else if (dropVerdict !== undefined) {
    lastAcceptedRef.current = dropVerdict;
  }
  const dropMarker = lastAcceptedRef.current?.marker;
  const dropMarkerKey = lastAcceptedRef.current?.markerKey;

  // A drag hovering this table may end without any event reaching it: a refused drop is not
  // dispatched here, and the source's dragend reaches the source's control only.
  const dragHovers = dropState !== null;
  React.useEffect(() => {
    if (!dragHovers) {
      return undefined;
    }
    return onDragEnd(() => setDropState(null));
  }, [dragHovers]);

  // -- Where the hint on a refused target stands - at the pointer, beside the drag image - and the
  //    hint's way to follow it: dragover fires continuously, and only the hint re-renders for each
  //    move, not the table. --
  const hintAnchorRef = React.useRef<HintAnchor>({ x: 0, y: 0, height: 0 });
  const hintMoveRef = React.useRef<((at: HintAnchor) => void) | null>(null);

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

  /** Re-measures whether columns run underneath the column button, see {@link columnsCovered}. */
  const measureCoveredRef = React.useRef<() => void>(() => {});

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

  const handleResizeStart = React.useCallback((columnName: string, colWidth: number, event: React.PointerEvent) => {
    // The default of the press is prevented, which keeps the heading - a drag source for column
    // reordering - from starting that drag on the resize handle, and the text selection with it.
    event.preventDefault();
    event.stopPropagation();
    // The rendered width, not the configured one: the last column grows into the space the others
    // leave over, and starting from its configured width would snap it back the moment the drag
    // begins. The handle sits in the heading whose width is wanted.
    const heading = (event.currentTarget as HTMLElement).parentElement;
    const startWidth = heading ? Math.round(heading.getBoundingClientRect().width) : colWidth;
    resizeRef.current = { column: columnName, startX: event.clientX, startWidth };

    // Track latest pointer position and cumulative auto-scroll offset.
    let lastClientX = event.clientX;
    let autoScrollOffset = 0;

    // Whole pixels: the server takes an integer width, and pointer coordinates as well as scroll
    // positions are fractional under browser zoom and fractional display scaling. Rounding here as
    // well as at the end of the drag shows exactly the width the drag reports.
    const widthAt = (clientX: number, info: { startX: number; startWidth: number }) =>
      Math.round(Math.max(MIN_COL_WIDTH, info.startWidth + (clientX - info.startX) + autoScrollOffset));

    const updateWidth = () => {
      const info = resizeRef.current;
      if (!info) return;
      setColumnWidthOverrides((prev) => ({ ...prev, [info.column]: widthAt(lastClientX, info) }));
    };

    const stopAutoScroll = () => {
      if (resizeAutoScrollRef.current !== null) {
        cancelAnimationFrame(resizeAutoScrollRef.current);
        resizeAutoScrollRef.current = null;
      }
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
        // continues even when the pointer is stuck at the screen edge.
        autoScrollOffset += actualDelta;
        updateWidth();
      }
      resizeAutoScrollRef.current = requestAnimationFrame(autoScroll);
    };
    resizeAutoScrollRef.current = requestAnimationFrame(autoScroll);

    startPointerDrag(event, {
      cursor: 'col-resize',

      onMove: (e) => {
        lastClientX = e.clientX;
        updateWidth();
      },

      onEnd: (e, dragged) => {
        stopAutoScroll();
        const info = resizeRef.current;
        resizeRef.current = null;
        if (info && dragged) {
          const finalWidth = widthAt(e.clientX, info);
          sendCommand('columnResize', { column: info.column, width: finalWidth });
          justResizedRef.current = true;
          requestAnimationFrame(() => { justResizedRef.current = false; });
        }
        // A press that stayed where it was reports no width: it is a click on the handle, and the
        // double click it may belong to fits the column to its content (see onDoubleClick).
      },

      onCancel: () => {
        stopAutoScroll();
        const info = resizeRef.current;
        if (info) {
          // Nothing is reported, so the column shows the width it had when the drag began again.
          setColumnWidthOverrides((prev) => ({ ...prev, [info.column]: info.startWidth }));
          resizeRef.current = null;
        }
      },
    });
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

  // The width handle takes the keyboard like any separator (bridge/separator.ts): the arrows widen and narrow the
  // column by one step, Home narrows it to the minimum. A column has no maximum, so End fits it to its content - what a
  // double click on the handle does - and reports it at once. The arrows reach the server when the key is released.
  const keyResizeRef = React.useRef<{ column: string; width: number } | null>(null);

  const handleResizeKeyDown = React.useCallback((columnName: string, colWidth: number, event: React.KeyboardEvent<HTMLElement>) => {
    const handle = event.currentTarget;
    const move = separatorMove(event.key, 'vertical', handle);
    if (!move) {
      return;
    }
    // The table's own keyboard (cursor, selection) does not see the key.
    event.preventDefault();
    event.stopPropagation();
    if (move.to === 'max') {
      keyResizeRef.current = null;
      fitColumnToContent(columnName);
      return;
    }
    const from = keyResizeRef.current?.column === columnName ? keyResizeRef.current.width : colWidth;
    const width = movedSize(from, move, MIN_COL_WIDTH, Number.MAX_SAFE_INTEGER, separatorStep(handle));
    keyResizeRef.current = { column: columnName, width };
    setColumnWidthOverrides((prev) => ({ ...prev, [columnName]: width }));
  }, [fitColumnToContent]);

  const handleResizeKeyUp = React.useCallback(() => {
    const info = keyResizeRef.current;
    keyResizeRef.current = null;
    if (info) {
      sendCommand('columnResize', { column: info.column, width: info.width });
    }
  }, [sendCommand]);

  // -- The rows the body shows right now, by index: what the server has not sent of them yet is drawn
  //    as a skeleton row, and the grid is aria-busy until it has. Updated when the range changes, not
  //    on every scrolled pixel. --
  const [windowRange, setWindowRange] = React.useState<{ first: number; last: number }>({ first: 0, last: -1 });
  const measureWindow = React.useCallback(() => {
    const body = scrollContainerRef.current;
    if (!body || rowHeight <= 0) return;
    const first = Math.floor(body.scrollTop / rowHeight);
    const last = Math.min(totalRowCount - 1, Math.ceil((body.scrollTop + body.clientHeight) / rowHeight) - 1);
    setWindowRange((p) => (p.first === first && p.last === last ? p : { first, last }));
  }, [rowHeight, totalRowCount]);
  React.useLayoutEffect(measureWindow, [measureWindow]);

  // The header scrolls by itself only when the browser brings a focused header control into view (Tab to
  // a sort or filter button beyond the edge); the body follows, and its scroll event aligns both again.
  const handleHeaderScroll = React.useCallback(() => {
    const header = headerRef.current;
    const body = scrollContainerRef.current;
    if (header && body && body.scrollLeft !== header.scrollLeft) {
      body.scrollLeft = header.scrollLeft;
    }
  }, []);

  // -- Scroll handler --
  const handleScroll = React.useCallback(() => {
    // Sync header horizontal scroll immediately.
    if (headerRef.current && scrollContainerRef.current) {
      headerRef.current.scrollLeft = scrollContainerRef.current.scrollLeft;
    }
    measureWindow();
    measureCoveredRef.current();
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
  }, [sendCommand, rowHeight, measureWindow]);

  // A density switch changes the row height: the rows the server sends for the viewport change with
  // it. Not for the first measurement - useTokenPx starts from its fallback (36) and reaches the
  // token's value (32 in compact density) one render later; a scroll command then would only repeat
  // the window the server already sent. A height the hook has not caught up with yet is skipped, the
  // first one that matches the token is recorded without a command.
  const reportedRowHeightRef = React.useRef<number | null>(null);
  React.useEffect(() => {
    if (rowHeight !== tokenPx(gridRef.current, '--tl-size-row', 36)) {
      return;
    }
    const previous = reportedRowHeightRef.current;
    reportedRowHeightRef.current = rowHeight;
    if (previous === null || previous === rowHeight) {
      return;
    }
    if (scrollContainerRef.current) {
      handleScroll();
    }
  }, [rowHeight]);

  // -- Sort handler: click or Enter on the label sorts by the column; with Shift it adds a level to the
  //    sort order. --
  const handleSort = React.useCallback((columnName: string, currentDirection: string | undefined, event: { shiftKey: boolean }) => {
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

  // -- Row drag-and-drop handlers --

  /**
   * Starts a row drag. The payload names the row by its id and says whether the row was selected -
   * the server then drags the whole selection, which may reach beyond the rendered row window.
   */
  const handleRowDragStart = React.useCallback((row: RowState, selected: boolean, event: React.DragEvent) => {
    if (isInteractiveTarget(event)) {
      // A drag begun on a cell's input belongs to that input (selecting its text), not to the row.
      event.preventDefault();
      return;
    }
    writeDragPayload(event, {
      source: controlId,
      keys: [row.id],
      selection: selected,
      kind: dragKind,
    });
    const dragged = { id: row.id, selection: selected };
    // A drag cancelled before the image was taken has already ended: no row stays marked dragging.
    afterDragImage(() => { if (runningDrag()) setDraggedRow(dragged); });
  }, [controlId, dragKind]);

  /**
   * Which row an event points at and the zone within it, split as the drop modes announced for the
   * dragged kind need, or no row where the pointer is beside the rows or the rows have no zones.
   */
  const dropTargetAt = React.useCallback(
    (event: React.DragEvent): { row: string | null; zone: TLDropZone } => {
      const modes = dragModes(event.dataTransfer, dropModes, dropModesByKind);
      const zoneSplit = dropTreeZones ? treeZoneSplit(modes) : flatZoneSplit(modes);
      if (zoneSplit !== 'none' && event.target instanceof Element) {
        const rowElement = event.target.closest('.tl-table__row') as HTMLElement | null;
        const key = rowElement?.dataset.dropRow;
        if (rowElement && key) {
          return { row: key, zone: dropZoneAt(event.clientY, rowElement, zoneSplit) };
        }
      }
      return { row: null, zone: 'none' };
    }, [dropModes, dropModesByKind, dropTreeZones]);

  const handleRootDragOver = React.useCallback((event: React.DragEvent) => {
    if (columnDragging) {
      event.preventDefault();
      // Auto-scroll horizontally during column drag.
      const body = scrollContainerRef.current;
      const header = headerRef.current;
      if (!body) return;
      const rect = body.getBoundingClientRect();
      const threshold = 40;
      const speed = 8;
      if (event.clientX < rect.left + threshold) {
        body.scrollLeft = Math.max(0, body.scrollLeft - speed);
      } else if (event.clientX > rect.right - threshold) {
        body.scrollLeft += speed;
      }
      if (header) header.scrollLeft = body.scrollLeft;
      return;
    }
    // Coarse acceptance from the payload's kind alone: during a drag the payload itself is
    // unreadable, and the kind is what the table declares its accepted drags against. Whether this
    // particular drop is possible is the server's answer, given once it arrives.
    if (!dragKindAccepted(event.dataTransfer, dropAcceptsAny, dropAccepts)) {
      return;
    }
    const drag = runningDrag();
    // The browser draws the drag image around the pointer; the hint stands beside it.
    const image = drag?.image ?? NO_DRAG_IMAGE;
    hintAnchorRef.current = { x: event.clientX, y: event.clientY + image.top, height: image.bottom - image.top };
    hintMoveRef.current?.(hintAnchorRef.current);
    const target = dropTargetAt(event);
    let probe: string | null = null;
    if (drag) {
      // The payload is unreadable here, but a drag started in this document is known: ask the
      // server once per drag and target whether a drop there would be accepted.
      probe = drag.id + '|' + (target.row ?? '') + '|' + target.zone;
      let probes = probesRef.current;
      if (!probes || probes.drag !== drag.id) {
        probes = { drag: drag.id, sent: new Set() };
        probesRef.current = probes;
      }
      if (!probes.sent.has(probe)) {
        probes.sent.add(probe);
        const args: Record<string, unknown> = {
          source: drag.payload.source,
          keys: drag.payload.keys.join(','),
          selection: drag.payload.selection,
          zone: target.zone,
          drag: drag.id,
          probe,
        };
        if (target.row) {
          args.targetKey = target.row;
        }
        void sendCommand(CMD_DROP_PROBE, args);
      }
    }
    setDropState((previous) =>
      previous && previous.row === target.row && previous.zone === target.zone
          && previous.probe === probe
        ? previous
        : { ...target, probe });
    const verdict = probe ? dropVerdicts[probe] : undefined;
    if (verdict && !verdict.accepted) {
      // Refused: leaving the default in place makes the target refuse the drop.
      event.dataTransfer.dropEffect = 'none';
      return;
    }
    event.preventDefault();
    event.dataTransfer.dropEffect = 'move';
  }, [columnDragging, dropAcceptsAny, dropAccepts, dropTargetAt, dropVerdicts, sendCommand]);

  const handleRootDragLeave = React.useCallback((event: React.DragEvent) => {
    // Moving among the table's own descendants fires a leave on each one left behind; only leaving
    // the table itself ends the highlight.
    if (!event.currentTarget.contains(event.relatedTarget as Node | null)) {
      setDropState(null);
    }
  }, []);

  const handleRootDrop = React.useCallback((event: React.DragEvent) => {
    if (!dragKindAccepted(event.dataTransfer, dropAcceptsAny, dropAccepts)) {
      // Not a row drag: a column heading released anywhere in the table lands where its mark shows
      // (useListReorder).
      if (columnDragging) columnReorder.containerProps.onDrop?.(event);
      return;
    }
    event.preventDefault();
    event.stopPropagation();
    const payload = readDragPayload(event.dataTransfer);
    const target = dropTargetAt(event);
    setDropState(null);
    if (payload) {
      const args: Record<string, unknown> = {
        source: payload.source,
        // Comma-separated: the command argument is a formatted string list, and a row key holds
        // no comma.
        keys: payload.keys.join(','),
        selection: payload.selection,
        zone: target.zone,
      };
      if (target.row) {
        // Named only for a drop on a row; a drop beside the rows names none.
        args.targetKey = target.row;
      }
      sendCommand(CMD_DROP, args);
    }
  }, [columnDragging, columnReorder.containerProps, dropAcceptsAny, dropAccepts, dropTargetAt, sendCommand]);

  // -- Selection handlers --
  const handleRowClick = React.useCallback((rowIndex: number, event: React.MouseEvent) => {
    // A click that concluded a text-selection drag inside the row copies text,
    // it does not change the row selection.
    const selection = window.getSelection();
    if (selection && !selection.isCollapsed && event.currentTarget.contains(selection.anchorNode)) {
      return;
    }
    // Give the grid keyboard focus so the table's keyboard scope becomes active - except when
    // the click landed in an interactive cell element (e.g. a text input of an editable cell),
    // which must keep the focus to stay editable.
    if (!isInteractiveTarget(event)) {
      gridRef.current?.focus({ preventScroll: true });
      // A plain selecting click enters the clicked cell for editing: once the row re-renders
      // editable, the focus effect moves the caret into that column's input.
      if (!event.ctrlKey && !event.metaKey && !event.shiftKey) {
        const col = (event.target as Element)?.closest?.('[data-col]')?.getAttribute('data-col');
        pendingFocusRef.current = { index: rowIndex, col: col ?? undefined };
      }
    }
    // Operating a control inside a cell - a button, a checkbox, an input - is not a selection
    // gesture: the click belongs to the control. Selecting the row anyway would not only change
    // the selection the user did not ask to change; the server's re-render of the row would also
    // overwrite the value the control is sending at the same moment, and the edit would be lost.
    // A control that cannot be operated - the read-only checkbox of a row not yet editable - does
    // nothing with the click, which then selects the row as a click on its text would.
    if (isOperableTarget(event) && !event.ctrlKey && !event.metaKey && !event.shiftKey) {
      return;
    }
    sendCommand('select', {
      rowIndex,
      ctrlKey: event.ctrlKey || event.metaKey,
      shiftKey: event.shiftKey,
    });
  }, [sendCommand]);

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
    // the focus out of the table on purpose; taking it back would close what was just opened. The
    // clicked row has given the focus to the grid, which holds the body.
    const active = document.activeElement;
    if (active && active !== document.body && !gridRef.current?.contains(active)) {
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
  }, [rows, selectedRows]);

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

  const handleCheckboxClick = React.useCallback((rowIndex: number) => {
    // The box is no stop of its own (tabindex -1): the keyboard stays with the grid, whose cursor
    // follows the selection.
    gridRef.current?.focus({ preventScroll: true });
    sendCommand('select', { rowIndex, ctrlKey: true, shiftKey: false });
  }, [sendCommand]);

  const handleSelectAll = React.useCallback(() => {
    sendCommand('selectAll', { selected: selectAllState !== 'all' });
  }, [sendCommand, selectAllState]);

  // -- Expand handler --
  const handleExpand = React.useCallback((rowIndex: number, expanded: boolean, event: React.MouseEvent) => {
    event.stopPropagation();
    sendCommand('expand', { rowIndex, expanded });
  }, [sendCommand]);

  // Right and left at the cursor: open or close its tree or group row; where there is nothing to open
  // or close, scroll the body sideways by a control's width.
  const handleExpandCursor = React.useCallback((expanded: boolean) => {
    const row = rows.find((r) => r.index === cursorIndex);
    if (row?.expandable && row.expanded !== expanded) {
      sendCommand('expand', { rowIndex: row.index, expanded });
      return;
    }
    const body = scrollContainerRef.current;
    if (body) {
      body.scrollLeft += expanded ? columnsButtonWidth : -columnsButtonWidth;
    }
  }, [rows, cursorIndex, sendCommand, columnsButtonWidth]);

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
  const handleFrozenSplitStart = React.useCallback((event: React.PointerEvent) => {
    event.preventDefault();
    event.stopPropagation();
    const area = frameRef.current;
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
    startPointerDrag(event, {
      cursor: 'col-resize',

      onMove: (e) => {
        const x = e.clientX - area.getBoundingClientRect().left;
        target = options.reduce(
          (best, option) => (Math.abs(option.x - x) < Math.abs(best.x - x) ? option : best), options[0]);
        setFrozenPreview(target);
      },

      onEnd: () => {
        setFrozenPreview(null);
        if (target.count !== frozenColumnCount) {
          sendCommand('setFrozenColumnCount', { count: target.count });
        }
      },

      onCancel: () => {
        // The boundary the preview shows is not reported: the frozen area stays as it is.
        setFrozenPreview(null);
      },
    });
  }, [columns, frozenWidth, frozenColumnCount, sendCommand]);

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

  // The name being typed for a filter to save; null while the save affordance is a plain button.
  const [saveName, setSaveName] = React.useState<string | null>(null);
  const searchInputRef = React.useRef<HTMLInputElement>(null);
  const filterBarRef = React.useRef<HTMLDivElement>(null);

  // A deleted filter's chip vanishes with its focused remove button once the server has dropped it:
  // the focus goes to the neighbour's remove button (same place, else the one before), without any
  // to the search, else to the grid - the same hand-over as the select's chips.
  const noteFilterRemoval = useFocusAfterRemove(
    namedFilters,
    () => chipFocusTargets(filterBarRef.current, FILTER_CHIP_ATTR),
    () => searchInputRef.current ?? gridRef.current,
  );

  const handleDeleteNamedFilter = React.useCallback((id: string, event: React.SyntheticEvent<HTMLElement>) => {
    event.stopPropagation();
    const removable = namedFilters.filter((named) => named.deletable);
    noteFilterRemoval(removable.findIndex((named) => named.id === id), event.currentTarget);
    sendCommand('deleteNamedFilter', { id });
  }, [namedFilters, noteFilterRemoval, sendCommand]);
  const saveButtonRef = React.useRef<HTMLButtonElement>(null);
  // Whether closing the save form took the focus with it: the form's input and buttons vanish.
  const saveFocusReturnRef = React.useRef(false);

  // Closes the save form and hands the focus back to the button that opened it (WCAG 2.4.3).
  const closeSaveForm = React.useCallback(() => {
    saveFocusReturnRef.current = true;
    setSaveName(null);
  }, []);

  // Once the form is gone, the focus goes to the save button, or to the grid where the table offers
  // no saving any more; it must not fall to the document body.
  React.useLayoutEffect(() => {
    if (saveName !== null || !saveFocusReturnRef.current) {
      return;
    }
    saveFocusReturnRef.current = false;
    (saveButtonRef.current ?? gridRef.current)?.focus();
  }, [saveName, filterSaving]);

  const handleSaveSubmit = React.useCallback(() => {
    const name = (saveName ?? '').trim();
    if (!name) {
      return;
    }
    sendCommand('saveNamedFilter', { filterName: name });
    closeSaveForm();
  }, [saveName, sendCommand, closeSaveForm]);

  const handleSaveKeyDown = React.useCallback((event: React.KeyboardEvent<HTMLInputElement>) => {
    if (event.key === 'Enter') {
      event.preventDefault();
      handleSaveSubmit();
    } else if (event.key === 'Escape') {
      // Stops the enclosing dialog/window from reading the Escape as "close me".
      event.preventDefault();
      closeSaveForm();
    }
  }, [handleSaveSubmit, closeSaveForm]);

  // -- Computed values --
  const tableWidth = columns.reduce((sum, col) => sum + getColWidth(col), 0)
    + (isMulti ? checkboxWidth : 0);

  // A table ending in a pinned column has a heading without a label there, so the column button
  // goes into that heading: it then needs no room of its own, and the pinned column reaches the
  // right edge of the table.
  const cogInHeaderCell = columnSelect && columns.length > 0 && !!columns[columns.length - 1].pinnedEnd;

  // Without such a heading, both the header row and the body end this much behind the last column,
  // keeping the column button clear of it: otherwise the button covers the last column's funnel as
  // soon as the columns fill the available width, and that filter cannot be opened at all. The
  // button is a control-sized square (size-control); the reserve applies to the body as well so that
  // scrolling to the right end frees the funnel there, too.
  // Kept as padding rather than width: the cells live in the content box, so the reserve widens the
  // scroll range without offering the last cell space to grow into and without a sticky cell -
  // confined to the content box - ever reaching underneath the button. The design system sizes every
  // box as border-box, so a minimum width given for a row includes its padding: it must add the
  // reserve to the width of the columns, or a table wider than its viewport loses the reserve.
  const buttonReserve = columnSelect && !cogInHeaderCell ? columnsButtonWidth : 0;

  // Whether columns run on underneath the column button: the table is wider than its header and
  // not scrolled to its right end. The header then fades out in front of the button, so the column
  // cut off there reads as scrolled out of view rather than as a heading the button covers by
  // mistake.
  const [columnsCovered, setColumnsCovered] = React.useState(false);
  const measureCovered = React.useCallback(() => {
    const header = headerRef.current;
    if (!header || buttonReserve === 0) {
      setColumnsCovered(false);
      return;
    }
    const columnsEnd = tableWidth - header.scrollLeft;
    const covered = columnsEnd > header.clientWidth - buttonReserve + 0.5;
    setColumnsCovered((previous) => (previous === covered ? previous : covered));
  }, [tableWidth, buttonReserve]);
  measureCoveredRef.current = measureCovered;
  // Measured again whenever what it depends on changes - a column resize changes the table width
  // with every pointer move - while the observer of the header's size stays one for the table's
  // lifetime, reaching the current measurement through the ref.
  React.useLayoutEffect(() => {
    measureCovered();
  }, [measureCovered]);
  React.useLayoutEffect(() => {
    const header = headerRef.current;
    if (!header) {
      return;
    }
    const observer = new ResizeObserver(() => measureCoveredRef.current());
    observer.observe(header);
    return () => observer.disconnect();
  }, []);

  const allSelected = selectAllState === 'all';
  const someSelected = selectAllState === 'some';

  // The cursor row as the grid's active descendant: only while it is rendered - the body renders
  // exactly the rows the server sent - and within the visible part of the body; otherwise the grid
  // keeps the ring itself (base.css). A reference to a row that is not there, or scrolled out of
  // sight, would hide the focus (WCAG 2.4.7).
  const cursorRendered = cursorIndex >= 0
    && cursorIndex >= windowRange.first && cursorIndex <= windowRange.last
    && rows.some((r) => r.index === cursorIndex);

  // The rows of the visible window the server has not sent yet: skeleton rows of row height.
  const sentIndexes = new Set(rows.map((r) => r.index));
  const missing: number[] = [];
  for (let i = windowRange.first; i <= windowRange.last; i++) {
    if (!sentIndexes.has(i)) missing.push(i);
  }

  // The server keeps the window of the body that showed the table last, also when the page is
  // reloaded or the table mounted again: the body then starts at the top, while the server sends the
  // rows it scrolled to before. The body scrolls back to the first row of that window once the row
  // height is the token's (see the density switch above) and the body can scroll that far: the
  // containers fill only a render after the table reports filling (fill.ts), so on its first layout
  // the body is as high as its rows and cannot scroll at all. A body that does not get there within
  // RESTORE_TIMEOUT_MS - the scroll range has shrunk - scrolls as far as it can. Should the rows sent
  // still not cover what the body shows, the body reports its window, which no scroll event does when
  // the position stays.
  const scrollRestoredRef = React.useRef(false);
  const restoreTimedOutRef = React.useRef(false);
  const restoreScrollRef = React.useRef<() => void>(() => {});
  restoreScrollRef.current = () => {
    const body = scrollContainerRef.current;
    if (scrollRestoredRef.current) {
      // The body changed its height: the window measured before is stale.
      measureWindow();
      return;
    }
    if (!body || rowHeight !== tokenPx(gridRef.current, '--tl-size-row', 36)) {
      return;
    }
    const target = Math.min(scrollStart, Math.max(0, totalRowCount - 1)) * rowHeight;
    const range = body.scrollHeight - body.clientHeight;
    if (target > range && !restoreTimedOutRef.current) {
      return;
    }
    scrollRestoredRef.current = true;
    if (target > 0) {
      body.scrollTop = Math.min(target, Math.max(0, range));
    }
    measureWindow();
    const first = Math.floor(body.scrollTop / rowHeight);
    const last = Math.min(totalRowCount - 1, Math.ceil((body.scrollTop + body.clientHeight) / rowHeight) - 1);
    for (let i = first; i <= last; i++) {
      if (!sentIndexes.has(i)) {
        handleScroll();
        break;
      }
    }
  };
  React.useLayoutEffect(() => {
    if (!scrollRestoredRef.current) {
      restoreScrollRef.current();
    }
  });
  React.useEffect(() => {
    const body = scrollContainerRef.current;
    if (!body) {
      return;
    }
    // The body gets its height from the containers filling, without a render of the table.
    const observer = new ResizeObserver(() => restoreScrollRef.current());
    observer.observe(body);
    const timeout = window.setTimeout(() => {
      restoreTimedOutRef.current = true;
      restoreScrollRef.current();
    }, RESTORE_TIMEOUT_MS);
    return () => {
      observer.disconnect();
      clearTimeout(timeout);
    };
  }, []);

  // Where a cell sits relative to the scrolling area: frozen at the start, pinned at the end. A state
  // of its location (data-tl-state), not of the user's handling of it.
  const placeOf = (colIdx: number) => {
    const col = columns[colIdx];
    const isFrozen = colIdx < frozenColumnCount;
    const isPinned = !!col.pinnedEnd;
    return {
      isFrozen,
      isPinned,
      words: [
        isFrozen && 'frozen',
        colIdx === frozenColumnCount - 1 && 'frozen-last',
        isPinned && 'pinned',
        isPinned && colIdx === lastUnpinnedIdx + 1 && 'pinned-first',
      ],
    };
  };

  // The width a cell takes: the last column the user arranges takes the space left over, in the
  // heading exactly as in the rows; the configured width is the flex basis, so cell and heading are
  // the same width whatever either of them holds.
  const widthStyle = (colIdx: number, w: number, isFrozen: boolean): React.CSSProperties =>
    colIdx === lastUnpinnedIdx && !isFrozen ? { flex: `1 0 ${w}px`, minWidth: w } : { width: w, minWidth: w };

  // The sort order as the heading announces it. WAI-ARIA knows no levels: aria-sort names the first
  // level only, and only there (APG sortable table); every other heading leaves it out.
  const ariaSort = (col: ColumnState): 'ascending' | 'descending' | undefined => {
    if (!col.sortable || !col.sortDirection || (col.sortPriority ?? 1) > 1) return undefined;
    return col.sortDirection === 'asc' ? 'ascending' : 'descending';
  };

  // The marker the server's verdict names, at the row it names; a refusal at the hovered row.
  const dropWord = (row: RowState) => {
    if (!dropState) return undefined;
    if (dropRefused) return dropState.row === row.id ? 'drop-refused' : undefined;
    return dropMarker && dropMarker !== 'control' && dropMarkerKey === row.id ? ROW_MARKER_STATE[dropMarker] : undefined;
  };
  const draggedWord = (row: RowState) =>
    draggedRow && (row.id === draggedRow.id || (draggedRow.selection && isSelected(row))) ? 'dragging' : undefined;

  return (
    <KeyboardScopeProvider active={isTableFocused}>
    <TableKeyBindings
      isMulti={isMulti}
      cursorIndex={cursorIndex}
      onMove={handleMove}
      onToggle={handleToggleCursor}
      onSelectAll={handleSelectAllRows}
      onActivate={handleActivateCursor}
      onExpand={handleExpandCursor}
      onGrid={isGridFocused}
      inEditor={isEditorFocused}
    />
    <div ref={rootRef} id={controlId}
      className={rootClassName(state, 'tl-table', fillClass)}
      data-tl-state={dropState ? (dropRefused
          ? (dropState.row === null ? 'drop-refused' : undefined)
          : (dropMarker === 'control' ? 'drop-onto' : undefined)) : undefined}
      onDragOver={handleRootDragOver}
      onDragLeave={handleRootDragLeave}
      onDrop={handleRootDrop}
    >
      {/* Why the target under the running drag refuses it: a tl-popover in the document body, so that
          neither the table's scrolling nor its clipping can hide it. */}
      {dropRefused && dropVerdict?.reason && (
        <DropHint reason={dropVerdict.reason} start={hintAnchorRef.current} moveRef={hintMoveRef} />
      )}
      {/* Filter bar above the headings: the named criteria as removable chips, the cross-column
          search, and saving the current criteria under a name. Outside both scrollers, so it
          neither scrolls with the columns nor takes part in the header/body width alignment. */}
      {filterBar && (
        <div ref={filterBarRef} className="tl-table__filter-bar">
          {namedFilters.length > 0 && (
            <span className="tl-chip-group">
              {namedFilters.map((named) => {
                const isActive = named.id === activeNamedFilter;
                // Only a filter of the user's own can be deleted; a declared one is part of the
                // table and offers no remove affordance.
                return (
                  <Chip
                    key={named.id}
                    label={named.label}
                    pressed={isActive}
                    tooltip={isActive ? i18n['js.table.clearFilter'] : named.label}
                    onClick={() => handleNamedFilter(named.id)}
                    onRemove={named.deletable ? (e) => handleDeleteNamedFilter(named.id, e) : undefined}
                    removeLabel={i18n['js.table.deleteFilter']}
                    {...(named.deletable ? { [FILTER_CHIP_ATTR]: true } : {})}
                  />
                );
              })}
            </span>
          )}
          <SearchField
            className="tl-table__search"
            tooltip={i18n['js.table.searchHint']}
            label={i18n['js.table.searchHint']}
            placeholder={i18n['js.table.search']}
            inputRef={searchInputRef}
            value={searchDraft}
            onChange={handleSearchChange}
            onClear={() => {
              setSearchDraft('');
              sendSearch('');
            }}
            onKeyDown={handleSearchKeyDown}
          />
          {filterSaving && (saveName === null ? (
            <Button
              ref={saveButtonRef}
              label={i18n['js.table.saveFilter']}
              image={ICON_SAVE}
              display="icon-only"
              appearance="ghost"
              onClick={() => setSaveName('')}
            />
          ) : (
            <span className="tl-table__save">
              <TextInput
                autoFocus
                placeholder={i18n['js.table.filterName']}
                aria-label={i18n['js.table.filterName']}
                value={saveName}
                onChange={setSaveName}
                onKeyDown={handleSaveKeyDown}
              />
              <Button
                label={i18n['js.table.saveFilter']}
                image={ICON_CONFIRM}
                display="icon-only"
                appearance="ghost"
                disabled={!saveName.trim()}
                onClick={handleSaveSubmit}
              />
              <Button
                label={i18n['js.table.cancelSave']}
                image={ICON_CANCEL}
                display="icon-only"
                appearance="ghost"
                onClick={closeSaveForm}
              />
            </span>
          ))}
        </div>
      )}

      {/* The frame holds the grid and what lies over it without being a cell: the column selection
          above the body's vertical scrollbar, the grip of the frozen area and its preview. */}
      <div className="tl-table__frame" ref={frameRef}>
      <div
        ref={gridRef}
        className="tl-table__grid"
        role={treegrid ? 'treegrid' : 'grid'}
        // Heading plus rows; an empty table counts its status row (aria-rowindex 2).
        aria-rowcount={Math.max(totalRowCount, 1) + 1}
        aria-colcount={columns.length + (isMulti ? 1 : 0)}
        aria-multiselectable={isMulti || undefined}
        aria-busy={missing.length > 0 || undefined}
        aria-activedescendant={cursorRendered ? rowId(cursorIndex) : undefined}
        tabIndex={0}
      >
      <div className="tl-table__header" ref={headerRef} role="rowgroup" onScroll={handleHeaderScroll}
        data-tl-state={stateAttr(columnsCovered && 'covered')}>
        {/* Fills the header even when the columns are narrower: a cell sticking to the right edge
            cannot leave its row, so a row ending with the last column would hold the pinned cells
            back from that edge. The reserve is padding, which a sticky cell never enters. */}
        <div className="tl-table__header-row tl-type-label-strong" role="row" aria-rowindex={1}
          data-tl-axis="horizontal" {...columnReorder.containerProps}
          style={{ minWidth: tableWidth + buttonReserve + scrollbarWidth, paddingRight: buttonReserve + scrollbarWidth }}>
          {isMulti && (
            <div role="columnheader"
              className="tl-table__header-cell tl-table__select"
              data-tl-state={stateAttr(frozenColumnCount > 0 && 'frozen')}
              style={{ width: checkboxWidth, minWidth: checkboxWidth, ...(frozenColumnCount > 0 ? { left: 0 } : {}) }}
            >
              <Checkbox
                small
                aria-label={i18n['js.table.selectAll']}
                checked={allSelected}
                indeterminate={someSelected}
                onChange={handleSelectAll}
              />
            </div>
          )}
          {columns.map((col, colIdx) => {
            const w = getColWidth(col);
            const { isFrozen, isPinned, words } = placeOf(colIdx);
            const sortIcon = col.sortDirection === 'asc' ? ICON_SORT_ASC : ICON_SORT_DESC;
            // A label cut off by its ellipsis offers its full text by itself (tooltip host, master
            // #29752); only a tooltip the column declares is set here.
            const label = (
              <span className="tl-table__label" {...tooltipProps(col.tooltip)}>{col.label}</span>
            );
            const filterLabel = col.filterActive ? i18n['js.table.filterActive'] : i18n['js.table.filter'];
            return (
              <div
                key={col.name}
                role="columnheader"
                aria-sort={ariaSort(col)}
                className={'tl-table__header-cell' + (col.cssClass ? ' ' + col.cssClass : '')}
                data-col={col.name}
                data-col-idx={colIdx}
                data-tl-state={stateAttr(...words, col.filterActive && 'filtered',
                  reorderState(columnReorder.itemState(colIdx)))}
                style={{
                  ...widthStyle(colIdx, w, isFrozen),
                  ...(isFrozen ? { left: frozenOffsets[colIdx] } : {}),
                  // The header ends with the reserve the body's scrollbar and, where it is not in a
                  // heading, the column button take; its cells stick that much further from the right
                  // edge than the body's - which puts a heading above its column at every scroll position.
                  ...(isPinned ? { right: pinnedOffsets[colIdx] + buttonReserve + scrollbarWidth } : {}),
                }}
                {...(isPinned ? {} : columnReorder.itemProps(colIdx))}
                onContextMenu={(e) => handleColumnContextMenu(colIdx, e)}
              >
                {col.sortable ? (
                  <button
                    type="button"
                    className="tl-table__sort"
                    onClick={(e) => handleSort(col.name, col.sortDirection, e)}
                    onKeyDown={(e) => {
                      // Enter sorts here, with Shift as a further level: a keyboard click carries no Shift
                      // in every browser.
                      if (e.key === 'Enter') {
                        e.preventDefault();
                        handleSort(col.name, col.sortDirection, e);
                      }
                    }}
                  >
                    {label}
                    {col.sortDirection && <ThemeIcon encoded={sortIcon} className="tl-table__mark tl-icon-sm" />}
                    {col.sortDirection && sortedColumnCount > 1 && col.sortPriority != null && col.sortPriority > 0 && (
                      <span className="tl-badge tl-type-label-strong tl-nums" aria-hidden="true">{col.sortPriority}</span>
                    )}
                  </button>
                ) : label}
                {col.name === grouping && (
                  <span className="tl-table__mark" {...tooltipProps(i18n['js.table.grouped'])}>
                    <ThemeIcon encoded={ICON_GROUP} className="tl-icon-sm" />
                  </span>
                )}
                {col.filterable && (
                  // One glyph; an active filter is the column's state (data-tl-state~="filtered",
                  // icon-primary) and the button's name says so as well.
                  <Button
                    label={filterLabel}
                    image={ICON_FILTER}
                    display="icon-only"
                    appearance="ghost"
                    small
                    className="tl-table__filter"
                    onMouseDown={(e) => e.stopPropagation()}
                    onClick={(e) => handleOpenFilter(col.name, e)}
                  />
                )}
                {cogInHeaderCell && colIdx === columns.length - 1 && (
                  <ColumnsButton label={i18n['js.table.columns']} inCell onClick={handleOpenColumnSelect} />
                )}
                {!isPinned && (
                  <div
                    className="tl-table__resize"
                    role="separator"
                    aria-orientation="vertical"
                    aria-label={i18n['js.table.columnWidth'].replace('{0}', col.label)}
                    tabIndex={0}
                    aria-valuenow={w}
                    aria-valuemin={MIN_COL_WIDTH}
                    aria-valuemax={Math.max(w, scrollContainerRef.current?.clientWidth ?? w)}
                    onKeyDown={(e) => handleResizeKeyDown(col.name, w, e)}
                    onKeyUp={handleResizeKeyUp}
                    onPointerDown={(e) => handleResizeStart(col.name, w, e)}
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
        </div>
      </div>
      {/* Scrollable body; the focus stays on the grid. tabIndex -1: Chromium makes a scrolling
          element without one a tab stop of its own. */}
      <div
        ref={scrollContainerRef}
        className="tl-table__body tl-type-body"
        role="rowgroup"
        tabIndex={-1}
        // A press on the body or a part of it that takes no focus - the empty area below the rows,
        // the selection cell beside its box - focuses the body (it carries a tabindex); the focus
        // belongs to the grid, which holds the keyboard and shows the ring.
        onPointerDown={handleBodyPress}
        onFocus={(e) => {
          if (e.target === e.currentTarget) {
            handleBodyFocus();
          }
        }}
        onScroll={handleScroll}
        onKeyDown={handleBodyKeyDown}
      >
        {totalRowCount === 0 && (
          <div role="row" aria-rowindex={2} aria-level={treegrid ? 1 : undefined}>
            <div role="gridcell" className="tl-status">{i18n['js.table.empty']}</div>
          </div>
        )}
        {/* Spacer for virtual scrolling. Fills the body when the columns are narrower than it, so
            the rows reach the right edge and a cell pinned there lands on it; the reserve is
            padding, so it widens the scroll range without taking any cell along. */}
        <div className="tl-table__canvas" style={{ height: totalHeight, minWidth: tableWidth + buttonReserve, paddingRight: buttonReserve }}>
          {missing.map((index) => (
            <div key={'skeleton-' + index} className="tl-table__placeholder" aria-hidden="true"
              style={{ position: 'absolute', top: index * rowHeight, height: rowHeight, left: 0, right: 0 }}>
              <span className="tl-skeleton" />
            </div>
          ))}
          {rows.map((row) => (
            <div
              key={row.id}
              id={rowId(row.index)}
              role="row"
              aria-rowindex={row.index + 2}
              aria-selected={isSelected(row)}
              aria-level={treegrid ? (row.treeDepth ?? 0) + 1 : undefined}
              aria-expanded={treegrid && row.expandable ? !!row.expanded : undefined}
              data-drop-row={row.id}
              draggable={dragEnabled && row.draggable !== false}
              className={'tl-table__row' + (row.cssClass ? ' ' + row.cssClass : '')}
              // The selection wins over a highlight: a selected row carries no role.
              data-tl-role={isSelected(row) ? undefined : statusRole(row.cssClass)}
              data-tl-state={stateAttr(row.index === cursorIndex && 'cursor', draggedWord(row), dropWord(row))}
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
              onDragStart={dragEnabled && row.draggable !== false ? (e) => handleRowDragStart(row, isSelected(row), e) : undefined}
              onDragEnd={dragEnabled && row.draggable !== false ? () => { setDropState(null); setDraggedRow(null); } : undefined}
              onDoubleClick={(e) => handleRowActivate(row.index, e)}
            >
              {isMulti && (
                <div role="gridcell"
                  className="tl-table__cell tl-table__select"
                  data-tl-state={stateAttr(frozenColumnCount > 0 && 'frozen')}
                  style={{ width: checkboxWidth, minWidth: checkboxWidth, ...(frozenColumnCount > 0 ? { left: 0 } : {}) }}
                  onClick={(e) => e.stopPropagation()}>
                  {row.groupCount == null && (
                    // Space at the cursor selects; the box is no stop of its own in the tab order.
                    <Checkbox
                      small
                      aria-label={i18n['js.table.selectRow']}
                      checked={isSelected(row)}
                      onChange={() => handleCheckboxClick(row.index)}
                      tabIndex={-1}
                    />
                  )}
                </div>
              )}
              {columns.map((col, colIdx) => {
                const w = getColWidth(col);
                const { isFrozen, isPinned, words } = placeOf(colIdx);
                const rowCellClass = row.cellClasses?.[col.name];
                const isTreeColumn = treeMode && colIdx === 0;
                const treeDepth = row.treeDepth ?? 0;
                // What the cell says: the tooltip of its content where it has one, its own text while
                // the column is too narrow to read it otherwise. In the tree column the declaration
                // sits on the value, so the toggle and the group size stay out of it.
                const cellTooltip = row.tooltips?.[col.name];
                const cellTooltipProps = cellTooltip ? tooltipProps(cellTooltip) : TOOLTIP_WHEN_CLIPPED;
                const toggleLabel = row.expanded ? i18n['js.treeView.collapse'] : i18n['js.treeView.expand'];
                return (
                  <div
                    key={col.name}
                    role="gridcell"
                    className={'tl-table__cell' + (row.groupCount != null ? ' tl-type-body-strong' : '') + (col.cssClass ? ' ' + col.cssClass : '')
                      + (rowCellClass ? ' ' + rowCellClass : '')}
                    data-row={row.id}
                    data-col={col.name}
                    data-tl-state={stateAttr(...words)}
                    data-tl-role={isSelected(row) ? undefined : statusRole(rowCellClass) ?? statusRole(col.cssClass)}
                    {...(isTreeColumn ? {} : cellTooltipProps)}
                    style={{
                      ...widthStyle(colIdx, w, isFrozen),
                      ...(isFrozen ? { left: frozenOffsets[colIdx] } : {}),
                      ...(isPinned ? { right: pinnedOffsets[colIdx] + buttonReserve } : {}),
                    }}
                  >
                    {isTreeColumn ? (
                      <div className="tl-table__tree" style={{ paddingInlineStart: treeDepth * treeIndentWidth }}>
                        {row.expandable ? (
                          // The toggle opens its own row, so it points at no other element. A press
                          // keeps the focus where it is - on the grid, whose ring stays at the cursor.
                          <DisclosureToggle
                            expanded={!!row.expanded}
                            label={toggleLabel}
                            tabIndex={-1}
                            onMouseDown={(e) => e.preventDefault()}
                            onClick={(e) => handleExpand(row.index, !row.expanded, e)}
                          />
                        ) : (
                          <span className="tl-table__spacer" />
                        )}
                        {/* A row that predates the current columns has no control for a newly shown
                            column yet - leave that cell empty rather than tearing down the table. */}
                        <span className="tl-table__value" {...cellTooltipProps}>
                          {!!row.cells[col.name] && <TLChild control={row.cells[col.name]} />}
                        </span>
                        {row.groupCount != null && (
                          <span className="tl-table__count tl-type-label">({row.groupCount})</span>
                        )}
                      </div>
                    ) : (
                      !!row.cells[col.name] && <TLChild control={row.cells[col.name]} />
                    )}
                  </div>
                );
              })}
            </div>
          ))}
        </div>
      </div>
      </div>

        {/* Grip on the boundary of the frozen columns, confined to the heading: a grip running down
            the body would swallow the clicks on the cells behind it. */}
        <div
          className="tl-table__freeze"
          role="separator"
          aria-orientation="vertical"
          aria-label={i18n['js.table.freezeSplitter']}
          style={{ left: frozenWidth }}
          {...tooltipProps(i18n['js.table.freezeSplitter'])}
          onPointerDown={handleFrozenSplitStart}
        />
        {columnSelect && !cogInHeaderCell && (
          <ColumnsButton label={i18n['js.table.columns']} onClick={handleOpenColumnSelect} />
        )}
        {/* Where the frozen area would end if the grip were dropped now, over heading and rows. */}
        {frozenPreview && (
          <div className="tl-table__freeze-preview" style={{ left: frozenPreview.x }} />
        )}
      </div>

      {/* The row count below the rows, outside the scroller, so it stays in view wherever the rows
          are scrolled to. A live region, so a screen reader hears the count a filter leaves. */}
      {rowCount !== '' && (
        <div className="tl-table__footer tl-type-label" role="status" aria-live="polite">
          <span className="tl-table__row-count">{rowCount}</span>
          {rowCountSelected !== '' && (
            <span className="tl-table__selection-count">{rowCountSelected}</span>
          )}
        </div>
      )}

      {/* Column context menu */}
      <Menu open={!!contextMenu} anchor={contextMenu ? { x: contextMenu.x, y: contextMenu.y } : null} onClose={() => setContextMenu(null)}>
        {contextMenu && contextMenu.colIdx + 1 !== frozenColumnCount && !columns[contextMenu.colIdx]?.pinnedEnd && (
          <MenuItem label={i18n['js.table.freezeUpTo']} onSelect={handleFreezeUpTo} />)}
        {contextMenu && frozenColumnCount > 0 && <MenuItem label={i18n['js.table.unfreezeAll']} onSelect={handleUnfreezeAll} />}
        {contextMenu && !columns[contextMenu.colIdx]?.pinnedEnd && (
          <MenuItem label={i18n['js.table.fitColumn']} onSelect={() => { fitColumnToContent(columns[contextMenu.colIdx].name); setContextMenu(null); }} />)}
        {contextMenu && columns[contextMenu.colIdx]?.groupable && columns[contextMenu.colIdx].name !== grouping && (
          <MenuItem label={i18n['js.table.groupBy']} onSelect={() => handleGroupBy(columns[contextMenu.colIdx].name)} />)}
        {contextMenu && grouping !== '' && <MenuItem label={i18n['js.table.ungroup']} onSelect={handleUngroup} />}
      </Menu>
    </div>
    </KeyboardScopeProvider>
  );
};

export default TLTableView;
