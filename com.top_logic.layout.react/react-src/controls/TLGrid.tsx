import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { renderItems } from './items';

/**
 * A CSS Grid container with responsive column support.
 *
 * State:
 * - columns: number  (fixed column count, default: null)
 * - minColumnWidth: string  (e.g. "16rem", triggers auto-fit)
 * - maxColumns: number  (upper bound for the auto-fit column count, default: null - unbounded)
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - itemClass: string  (CSS class of a wrapper around each child, default: null - no wrapper)
 * - children: ChildDescriptor[]
 */
const TLGrid: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const columns = state.columns as number | null;
  const minColumnWidth = state.minColumnWidth as string | null;
  const maxColumns = state.maxColumns as number | null;
  const gap = (state.gap as string) ?? 'default';
  const itemClass = (state.itemClass as string) ?? null;
  const children = (state.children as unknown[]) ?? [];

  const style: React.CSSProperties = {};
  if (minColumnWidth) {
    // Cap the column minimum at the container width so a minimum wider than a narrow
    // (mobile) viewport collapses to full-width columns instead of overflowing.
    const cappedMin = `min(${minColumnWidth}, 100%)`;
    if (maxColumns) {
      // Raise the column minimum to the width a row of maxColumns columns leaves for one of them
      // (the gaps between them taken off the container width), so that auto-fit never places more
      // than maxColumns columns while it still drops columns as the container narrows.
      const maxColumnsMin = `calc((100% - ${maxColumns - 1} * var(--tlGrid-gap)) / ${maxColumns})`;
      style.gridTemplateColumns = `repeat(auto-fit, minmax(max(${cappedMin}, ${maxColumnsMin}), 1fr))`;
    } else {
      style.gridTemplateColumns = `repeat(auto-fit, minmax(${cappedMin}, 1fr))`;
    }
  } else if (columns) {
    style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
  }

  return (
    <div id={controlId} className={rootClassName(state, `tlGrid tlGrid--gap-${gap}`)} style={style}>
      {renderItems(children, itemClass)}
    </div>
  );
};

export default TLGrid;
