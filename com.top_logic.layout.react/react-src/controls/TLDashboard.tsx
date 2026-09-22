import { React, useTLState, useTLCommand, TLChild, FillBarrier } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';
import { isInteractiveTarget } from './interactive';

const { useCallback, useEffect, useMemo, useRef, useState } = React;

/**
 * What activating a tile offers: the name the tile is announced under, the icon marking it as an
 * entry point, whether the activation is currently refused and the text explaining it.
 */
interface TileAction {
  label: string;
  image?: string | null;
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
  const tiles = ((state.children as TileDescriptor[]) ?? []).filter(t => t && t.id);

  const containerRef = useRef<HTMLDivElement>(null);
  const [cols, setCols] = useState<number>(1);
  const editMode = state.editMode === true;

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

  // --- Drag and drop (edit mode only) ---
  const [draggedId, setDraggedId] = useState<string | null>(null);
  const [dropTarget, setDropTarget] = useState<{ id: string; before: boolean } | null>(null);

  const onDragStart = useCallback((e: React.DragEvent<HTMLDivElement>, id: string) => {
    if (!editMode) { e.preventDefault(); return; }
    setDraggedId(id);
    e.dataTransfer.effectAllowed = 'move';
    e.dataTransfer.setData('text/plain', id);
  }, [editMode]);

  const onDragOver = useCallback((e: React.DragEvent<HTMLDivElement>, id: string) => {
    if (!editMode || !draggedId || draggedId === id) return;
    e.preventDefault();
    e.dataTransfer.dropEffect = 'move';
    const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect();
    const before = e.clientX < rect.left + rect.width / 2;
    setDropTarget(prev => (prev && prev.id === id && prev.before === before) ? prev : { id, before });
  }, [editMode, draggedId]);

  const onDragLeave = useCallback(() => {
    // Cleared on drop/dragend; leaving a single tile shouldn't clear.
  }, []);

  const commitReorder = useCallback((draggedTileId: string, targetId: string, before: boolean) => {
    const ids = tiles.map(t => t.id);
    const from = ids.indexOf(draggedTileId);
    if (from < 0) return;
    ids.splice(from, 1);
    const toBase = ids.indexOf(targetId);
    if (toBase < 0) { ids.splice(from, 0, draggedTileId); return; }
    const insertAt = before ? toBase : toBase + 1;
    ids.splice(insertAt, 0, draggedTileId);
    sendCommand('reorder', { order: ids });
  }, [tiles, sendCommand]);

  const onDrop = useCallback((e: React.DragEvent<HTMLDivElement>, id: string) => {
    if (!editMode || !draggedId || draggedId === id) return;
    e.preventDefault();
    const rect = (e.currentTarget as HTMLDivElement).getBoundingClientRect();
    const before = e.clientX < rect.left + rect.width / 2;
    commitReorder(draggedId, id, before);
    setDraggedId(null);
    setDropTarget(null);
  }, [editMode, draggedId, commitReorder]);

  const onDragEnd = useCallback(() => {
    setDraggedId(null);
    setDropTarget(null);
  }, []);

  // --- Activation ---

  const activateTile = useCallback((tileId: string) => {
    sendCommand('activate', { tileId });
  }, [sendCommand]);

  /**
   * A press anywhere on the tile activates it - except while the dashboard is rearranged, while
   * the action is refused, and where the press landed on a descendant that answers it itself.
   * The tile's own action button is such a descendant, and its click handler has already sent the
   * activation, so the tile is activated exactly once however it was pressed.
   */
  const onTileClick = useCallback((e: React.MouseEvent<HTMLDivElement>, tile: TileDescriptor) => {
    const action = tile.action;
    if (!action || editMode || action.disabled || isInteractiveTarget(e)) return;
    activateTile(tile.id);
  }, [editMode, activateTile]);

  const gridStyle: React.CSSProperties = {
    display: 'grid',
    gridTemplateColumns: `repeat(${cols}, 1fr)`,
    gridAutoRows: rowHeight,
    gap: '1rem',
  };

  return (
    <div
      id={controlId}
      ref={containerRef}
      className={'tlDashboard' + (editMode ? ' tlDashboard--edit' : '')}
    >
      <div className="tlDashboard__grid" style={gridStyle}>
        {tiles.map(tile => {
          const p = placementById[tile.id];
          if (!p) return null;
          const style: React.CSSProperties = {
            gridColumn: `${p.colStart + 1} / ${p.colEnd + 1}`,
            gridRow: `${p.rowStart + 1} / ${p.rowEnd + 1}`,
          };
          const action = tile.action;
          const classes = ['tlDashboard__tile'];
          if (action) classes.push('tlDashboard__tile--action');
          if (action && action.disabled) classes.push('tlDashboard__tile--disabled');
          if (draggedId === tile.id) classes.push('tlDashboard__tile--dragging');
          if (dropTarget && dropTarget.id === tile.id) {
            classes.push(dropTarget.before ? 'tlDashboard__tile--dropBefore' : 'tlDashboard__tile--dropAfter');
          }
          return (
            <div
              key={tile.id}
              className={classes.join(' ')}
              style={style}
              draggable={editMode}
              onClick={action ? e => onTileClick(e, tile) : undefined}
              data-tooltip={action && action.tooltip ? `text:${action.tooltip}` : undefined}
              onDragStart={e => onDragStart(e, tile.id)}
              onDragOver={e => onDragOver(e, tile.id)}
              onDragLeave={onDragLeave}
              onDrop={e => onDrop(e, tile.id)}
              onDragEnd={onDragEnd}
            >
              {action && (
                // The tile's accessible name and its keyboard operation: a button of its own, out
                // of sight but in the tab order, activated by Enter and Space as any button is.
                // While the tiles are rearranged it steps aside, so tabbing does not reach an
                // action the dashboard currently does not offer.
                <button
                  type="button"
                  className="tlDashboard__tileAction"
                  disabled={action.disabled}
                  tabIndex={editMode ? -1 : 0}
                  onClick={() => activateTile(tile.id)}
                >
                  {action.label}
                </button>
              )}
              <FillBarrier>
                <TLChild control={tile.control} />
              </FillBarrier>
              {action && action.image && (
                <span className="tlDashboard__tileMarker" aria-hidden="true">
                  <ThemeIcon encoded={action.image} />
                </span>
              )}
              {editMode && <div className="tlDashboard__overlay" />}
            </div>
          );
        })}
      </div>
    </div>
  );
};

export default TLDashboard;
