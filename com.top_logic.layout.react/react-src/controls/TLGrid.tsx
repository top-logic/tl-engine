import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A CSS Grid container with responsive column support.
 *
 * State:
 * - columns: number  (fixed column count, default: null)
 * - minColumnWidth: string  (e.g. "16rem", triggers auto-fit)
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - children: ChildDescriptor[]
 */
const TLGrid: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const columns = state.columns as number | null;
  const minColumnWidth = state.minColumnWidth as string | null;
  const gap = (state.gap as string) ?? 'default';
  const children = (state.children as unknown[]) ?? [];

  const style: React.CSSProperties = {};
  if (minColumnWidth) {
    // Cap the column minimum at the container width so a minimum wider than a narrow
    // (mobile) viewport collapses to full-width columns instead of overflowing.
    style.gridTemplateColumns = `repeat(auto-fit, minmax(min(${minColumnWidth}, 100%), 1fr))`;
  } else if (columns) {
    style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
  }

  return (
    <div id={controlId} className={`tlGrid tlGrid--gap-${gap}`} style={style}>
      {children.map((child, i) => (
        <TLChild key={i} control={child} />
      ))}
    </div>
  );
};

export default TLGrid;
