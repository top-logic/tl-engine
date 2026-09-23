import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A page layout of columns whose widths follow a weight, and that stack below a breakpoint.
 *
 * State:
 * - weights: number[]  (the width share of each column, in the order of the children; default: 1)
 * - breakpoint: string  (CSS length the columns stack below, default: "48rem")
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - children: ChildDescriptor[]  (one child per column)
 *
 * The container is a wrapping flex row, and the flex basis of a column is the width the breakpoint
 * leaves over the current container width, scaled up steeply. While the container is at least as
 * wide as the breakpoint that term is negative, hence clamped to zero, and the columns share the
 * row by their grow factor. Once the container is narrower the term turns positive and, scaled,
 * exceeds the row, so every column wraps onto a line of its own and shrinks to the full width.
 */
const TLColumns: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const weights = (state.weights as number[]) ?? [];
  const breakpoint = (state.breakpoint as string) ?? '48rem';
  const gap = (state.gap as string) ?? 'default';
  const children = (state.children as unknown[]) ?? [];

  return (
    <div id={controlId} className={`tlColumns tlColumns--gap-${gap}`}>
      {children.map((child, i) => (
        <div
          key={i}
          className="tlColumns__column"
          style={{ flex: `${weights[i] ?? 1} 1 calc((${breakpoint} - 100%) * 999)` }}
        >
          <TLChild control={child} />
        </div>
      ))}
    </div>
  );
};

export default TLColumns;
