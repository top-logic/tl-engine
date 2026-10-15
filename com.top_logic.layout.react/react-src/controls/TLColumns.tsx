import { React, useTLState, TLChild, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/** The gaps the design system has a modifier for (columns.css); anything else is the default. */
const GAPS = new Set(['compact', 'default', 'loose']);

/**
 * A page layout of columns whose widths follow a weight, and that stack below a breakpoint (design
 * system: tl-columns).
 *
 * State:
 * - weights: number[]  (the width share of each column, in the order of the children; default: 1)
 * - breakpoint: string  (CSS length the columns stack below, default: "48rem")
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - children: ChildDescriptor[]  (one child per column)
 *
 * The flex basis of a column is the width the breakpoint leaves over the current container width,
 * scaled up steeply: data from the state, set inline (columns.css explains the formula).
 */
const TLColumns: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const weights = (state.weights as number[]) ?? [];
  const breakpoint = (state.breakpoint as string) ?? '48rem';
  const gap = GAPS.has(state.gap as string) ? (state.gap as string) : 'default';
  const children = (state.children as unknown[]) ?? [];

  return (
    <div id={controlId} className={rootClassName(state, 'tl-columns', `tl-columns--gap-${gap}`)}>
      {children.map((child, i) => (
        <div
          key={i}
          className="tl-columns__column"
          style={{ flex: `${weights[i] ?? 1} 1 calc((${breakpoint} - 100%) * 999)` }}
        >
          <TLChild control={child} />
        </div>
      ))}
    </div>
  );
};

export default TLColumns;
