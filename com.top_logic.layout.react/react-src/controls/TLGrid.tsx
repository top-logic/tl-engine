import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useItems } from './items';

/**
 * The gap of each step, the same as the rules of tl-grid in the design system read (layout.css); the column template
 * computes with it. Kept equal to the stylesheet by a test of the package (engine-classes.test.mjs).
 */
const GAP_TOKEN: Record<string, string> = {
  compact: 'var(--tl-space-sm)',
  default: 'var(--tl-space-md)',
  loose: 'var(--tl-space-lg)',
};

/**
 * A CSS Grid container with responsive column support.
 *
 * State:
 * - columns: number  (fixed column count, default: null)
 * - minColumnWidth: string  (e.g. "16rem", triggers auto-fit)
 * - maxColumns: number  (upper bound for the auto-fit column count, default: null - unbounded)
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - maxWidth: string  (CSS length the grid is bounded to, centered in its container; default:
 *   null - the width the container offers)
 * - itemClass: string  (CSS class of a wrapper around each child, default: null - no wrapper)
 * - children: ChildDescriptor[]
 * - selectable, itemKeys, selected, multiSelect: the selection among the items (see items.tsx)
 */
const TLGrid: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const columns = state.columns as number | null;
  const minColumnWidth = state.minColumnWidth as string | null;
  const maxColumns = state.maxColumns as number | null;
  const gap = (state.gap as string) ?? 'default';
  const maxWidth = (state.maxWidth as string) ?? null;
  const items = useItems(state);

  const style: React.CSSProperties = {};
  if (minColumnWidth) {
    // Cap the column minimum at the container width so a minimum wider than a narrow
    // (mobile) viewport collapses to full-width columns instead of overflowing.
    const cappedMin = `min(${minColumnWidth}, 100%)`;
    if (maxColumns) {
      // Raise the column minimum to the width a row of maxColumns columns leaves for one of them
      // (the gaps between them taken off the container width), so that auto-fit never places more
      // than maxColumns columns while it still drops columns as the container narrows.
      const maxColumnsMin = `calc((100% - ${maxColumns - 1} * ${GAP_TOKEN[gap] ?? GAP_TOKEN.default}) / ${maxColumns})`;
      style.gridTemplateColumns = `repeat(auto-fit, minmax(max(${cappedMin}, ${maxColumnsMin}), 1fr))`;
    } else {
      style.gridTemplateColumns = `repeat(auto-fit, minmax(${cappedMin}, 1fr))`;
    }
  } else if (columns) {
    style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
  }
  if (maxWidth) {
    // Only the bound itself is a length; that it is centered is the rule the class carries.
    style.maxWidth = maxWidth;
  }

  const className = rootClassName(state, 'tl-grid', `tl-grid--gap-${gap}`, maxWidth && 'tl-bounded');

  return (
    <div id={controlId} {...items.containerProps} className={className} style={style}>
      {items.content}
    </div>
  );
};

export default TLGrid;
