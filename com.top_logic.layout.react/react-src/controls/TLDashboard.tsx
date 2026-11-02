import { React, useTLState, useTLCommand, useI18N, TLChild, FillBarrier, rootClassName, stateAttr, usePart, ICON_CLOSE, isInteractiveTarget, useListReorder, reorderState, useTokenPx, tooltipProps } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useMemo, useRef, useState } = React;

const I18N_KEYS = {
  'js.dashboard.hideTile': 'Hide tile',
  'js.dashboard.tile': 'Tile {0}',
};

/** The space between the tiles, for the grid and its packing alike, while the design system is not loaded. */
const GAP_FALLBACK_PX = 16;

/** The command sending the new order of the shown tiles after the user moved one. */
const REORDER_COMMAND = 'reorder';

/** The argument of {@link REORDER_COMMAND}: the ids of the shown tiles in their new order. */
const ORDER_ARG = 'order';

/** The command running the action of a tile. */
const ACTIVATE_COMMAND = 'activate';

/** The command removing a tile from the dashboard; it can be added again by the dashboard's menu. */
const HIDE_COMMAND = 'hide';

/** The argument of {@link ACTIVATE_COMMAND} and {@link HIDE_COMMAND} naming the tile. */
const TILE_ID_ARG = 'tileId';

/**
 * What activating a tile offers: the name the tile is announced under, whether the activation is
 * currently refused and the text explaining it.
 */
interface TileAction {
  label: string;
  disabled: boolean;
  tooltip?: string | null;
}

interface TileDescriptor {
  id: string;
  width: 'quarter' | 'third' | 'half' | 'two-thirds' | 'full';
  rowSpan: number;
  control: unknown;
  /** What the tile runs when activated, absent for a tile that only displays its content. */
  action?: TileAction | null;
  /** Whether the tile is withheld from the dashboard; it then carries no control. */
  hidden?: boolean;
}

interface Placement {
  id: string;
  colStart: number;
  colEnd: number;
  rowStart: number;
  rowEnd: number;
}

const WIDTH_FRACTION: Record<TileDescriptor['width'], number> = {
  'quarter': 0.25,
  'third': 1 / 3,
  'half': 0.5,
  'two-thirds': 2 / 3,
  'full': 1.0,
};

const COL_BREAKPOINTS = [1, 2, 3, 4];

/** The pixel value of a length the server states, such as the minimum column width `16rem`. */
function parseLengthPx(value: string, fontPx: number): number {
  const match = /^([\d.]+)(rem|em|px)?$/.exec(value.trim());
  if (!match) return 16 * fontPx;
  const num = parseFloat(match[1]);
  const unit = match[2] || 'px';
  if (unit === 'rem' || unit === 'em') return num * fontPx;
  return num;
}

function calcMaxColumns(containerWidth: number, minColPx: number): number {
  const raw = Math.max(1, Math.floor(containerWidth / minColPx));
  // Snap to nice numbers so items don't flicker between column counts.
  let snapped = 1;
  for (const b of COL_BREAKPOINTS) {
    if (raw >= b) snapped = b;
  }
  return snapped;
}

function itemSpan(width: TileDescriptor['width'], cols: number): number {
  const frac = WIDTH_FRACTION[width] ?? 1.0;
  return Math.max(1, Math.round(frac * cols));
}

function computeLayout(tiles: TileDescriptor[], cols: number): Placement[] {
  const safeCols = Math.max(1, cols);
  const occupied: Record<number, Record<number, boolean>> = {};
  const isOccupied = (r: number, c: number) => !!(occupied[r] && occupied[r][c]);
  const mark = (r: number, c: number) => {
    if (!occupied[r]) occupied[r] = {};
    occupied[r][c] = true;
  };

  const placements: Placement[] = [];
  let curRow = 0;
  let curCol = 0;

  const stretchLastInRow = (row: number) => {
    let last: Placement | null = null;
    for (const p of placements) if (p.rowStart === row) last = p;
    if (!last) return;
    let newEnd = last.colEnd;
    while (newEnd < safeCols && !isOccupied(row, newEnd)) newEnd++;
    if (newEnd !== last.colEnd) {
      for (let r = last.rowStart; r < last.rowEnd; r++) {
        for (let c = last.colEnd; c < newEnd; c++) mark(r, c);
      }
      last.colEnd = newEnd;
    }
  };

  for (const t of tiles) {
    const rowSpan = Math.max(1, t.rowSpan || 1);
    let span = Math.min(itemSpan(t.width, safeCols), safeCols);

    while (isOccupied(curRow, curCol)) {
      curCol++;
      if (curCol >= safeCols) { curCol = 0; curRow++; }
    }

    let freeInRow = 0;
    for (let c = curCol; c < safeCols; c++) {
      if (!isOccupied(curRow, c)) freeInRow++;
      else break;
    }

    if (span > freeInRow) {
      stretchLastInRow(curRow);
      curCol = 0;
      curRow++;
      while (isOccupied(curRow, curCol)) {
        curCol++;
        if (curCol >= safeCols) { curCol = 0; curRow++; }
      }
      freeInRow = 0;
      for (let c = curCol; c < safeCols; c++) {
        if (!isOccupied(curRow, c)) freeInRow++;
        else break;
      }
      span = Math.min(span, freeInRow);
    }

    const colStart = curCol;
    const colEnd = curCol + span;
    const rowStart = curRow;
    const rowEnd = curRow + rowSpan;
    placements.push({ id: t.id, colStart, colEnd, rowStart, rowEnd });
    for (let r = rowStart; r < rowEnd; r++) {
      for (let c = colStart; c < colEnd; c++) mark(r, c);
    }
    curCol = colEnd;
    if (curCol >= safeCols) { curCol = 0; curRow++; }
  }

  stretchLastInRow(curRow);

  // Fill row-span gaps: extend neighbors downward.
  let maxRow = 0;
  for (const p of placements) if (p.rowEnd > maxRow) maxRow = p.rowEnd;
  for (let r = 1; r < maxRow; r++) {
    for (let c = 0; c < safeCols; c++) {
      if (isOccupied(r, c)) continue;
      const above = placements.find(p => p.rowEnd === r && p.colStart <= c && c < p.colEnd);
      if (!above) continue;
      above.rowEnd = r + 1;
      for (let cc = above.colStart; cc < above.colEnd; cc++) mark(r, cc);
    }
  }

  return placements;
}

const TLDashboard: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const minColWidth = (state.minColWidth as string) ?? '16rem';
  const rowHeight = (state.rowHeight as string) ?? '16rem';
  // A withheld tile is left out of the grid, which closes the gap.
  const tiles = ((state.children as TileDescriptor[]) ?? []).filter(t => t && t.id && !t.hidden);

  const containerRef = useRef<HTMLDivElement>(null);
  const [cols, setCols] = useState<number>(1);
  const editMode = state.editMode === true;

  // The space between the tiles is the design system's, read where the grid is drawn.
  const gap = useTokenPx(containerRef, '--tl-space-md', GAP_FALLBACK_PX);

  // Recompute column count on resize.
  useEffect(() => {
    const el = containerRef.current;
    if (!el) return;
    const fontPx = parseFloat(getComputedStyle(document.documentElement).fontSize) || 16;
    const minPx = parseLengthPx(minColWidth, fontPx);
    const update = () => setCols(calcMaxColumns(el.clientWidth, minPx));
    update();
    const ro = new ResizeObserver(update);
    ro.observe(el);
    return () => ro.disconnect();
  }, [minColWidth]);

  const placements = useMemo(() => computeLayout(tiles, cols), [tiles, cols]);
  const placementById = useMemo(() => {
    const m: Record<string, Placement> = {};
    for (const p of placements) m[p.id] = p;
    return m;
  }, [placements]);

  const i18n = useI18N(I18N_KEYS);
  const Button = usePart('Button');

  // --- Moving tiles (edit mode only): the common gesture, along the row of tiles ---

  /** Sends the order of the tiles after the tile at {@code from} moved to {@code to}. */
  const moveTile = useCallback((from: number, to: number) => {
    const ids = tiles.map(t => t.id);
    const [moved] = ids.splice(from, 1);
    ids.splice(to, 0, moved);
    sendCommand(REORDER_COMMAND, { [ORDER_ARG]: ids });
  }, [tiles, sendCommand]);

  const reorder = useListReorder({
    axis: 'horizontal',
    onMove: moveTile,
    enabled: editMode,
    handle: false,
    count: tiles.length,
    // A tile is announced under the name of its action, a tile without one by its position.
    labelOf: index => tiles[index]?.action?.label || i18n['js.dashboard.tile'].replace('{0}', String(index + 1)),
    keyOf: index => tiles[index]?.id ?? String(index),
  });

  // --- Activation ---

  const activateTile = useCallback((tileId: string) => {
    sendCommand(ACTIVATE_COMMAND, { [TILE_ID_ARG]: tileId });
  }, [sendCommand]);

  // --- Hiding ---

  const hideTile = useCallback((tileId: string) => {
    sendCommand(HIDE_COMMAND, { [TILE_ID_ARG]: tileId });
  }, [sendCommand]);

  /**
   * A press on the hide button must not begin moving the tile it sits in: the tile is draggable in
   * edit mode, and a drag would start from the button as from any other point of the tile.
   */
  const preventDrag = useCallback((e: React.DragEvent<HTMLDivElement>) => {
    e.preventDefault();
    e.stopPropagation();
  }, []);

  /**
   * A press anywhere on the tile activates it - except while the dashboard is rearranged, while
   * the action is refused, and where the press landed on a descendant that answers it itself.
   * The tile's own action button is such a descendant, and its click handler has already sent the
   * activation, so the tile is activated exactly once however it was pressed.
   */
  const onTileClick = useCallback((e: React.MouseEvent<HTMLDivElement>, tile: TileDescriptor) => {
    const action = tile.action;
    if (!action || editMode || action.disabled || isInteractiveTarget(e.target)) return;
    activateTile(tile.id);
  }, [editMode, activateTile]);

  // Columns, row height and gap are data of the packing; the design system has no form for them.
  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gridAutoRows: rowHeight,
    gap: `${gap}px`,
  };

  return (
    <div
      id={controlId}
      ref={containerRef}
      className={rootClassName(state, 'tl-dashboard', 'tl-inset')}
      data-tl-state={stateAttr(editMode && 'edit')}
    >
      <div className="tl-dashboard__grid" data-tl-axis="horizontal" style={gridStyle} {...reorder.containerProps}>
        {tiles.map((tile, index) => {
          const p = placementById[tile.id];
          if (!p) return null;
          const style: React.CSSProperties = {
            gridColumn: `${p.colStart + 1} / ${p.colEnd + 1}`,
            gridRow: `${p.rowStart + 1} / ${p.rowEnd + 1}`,
          };
          const action = tile.action;
          return (
            // While the dashboard is rearranged, the tile is what the keyboard moves: focusable, named and
            // described by the reorder gesture, Alt + arrow left and right move it one place.
            <div
              key={tile.id}
              className="tl-card tl-dashboard__tile tl-overlay tl-overlay--fill"
              style={style}
              data-tl-state={reorderState(reorder.itemState(index))}
              tabIndex={editMode ? 0 : undefined}
              // A group, so that the name the reorder gesture gives it is allowed (a generic div takes none).
              role={editMode ? 'group' : undefined}
              onClick={action ? e => onTileClick(e, tile) : undefined}
              {...tooltipProps(action?.tooltip)}
              {...reorder.itemProps(index)}
              {...reorder.handleProps(index)}
            >
              {action && (
                // The tile's accessible name and its keyboard operation: a button of its own, out
                // of sight but in the tab order, activated by Enter and Space as any button is.
                // While the tiles are rearranged it steps aside, so tabbing does not reach an
                // action the dashboard currently does not offer. It is a direct child of the tile,
                // which shows the focus ring for it.
                <button
                  type="button"
                  className="tl-dashboard__action tl-visually-hidden"
                  disabled={action.disabled}
                  tabIndex={editMode ? -1 : 0}
                  onClick={() => activateTile(tile.id)}
                >
                  {action.label}
                </button>
              )}
              <div className="tl-overlay__base">
                <FillBarrier>
                  <TLChild control={tile.control} />
                </FillBarrier>
              </div>
              {editMode && (
                // Takes the pointer from the content, so that a press moves the tile.
                <div className="tl-overlay__layer tl-overlay__layer--fill">
                  <div className="tl-dashboard__cover" />
                </div>
              )}
              {editMode && (
                // After the cover, so that it lies above it and is the one thing in a tile being
                // rearranged that answers the pointer instead of starting a move.
                <div className="tl-overlay__layer tl-overlay__layer--top-right">
                  <div className="tl-overlay__action" draggable onDragStart={preventDrag}>
                    <Button
                      label={i18n['js.dashboard.hideTile']}
                      image={ICON_CLOSE}
                      display="icon-only"
                      appearance="ghost"
                      small
                      onClick={() => hideTile(tile.id)}
                    />
                  </div>
                </div>
              )}
            </div>
          );
        })}
      </div>
      {reorder.announcer}
    </div>
  );
};

export default TLDashboard;
