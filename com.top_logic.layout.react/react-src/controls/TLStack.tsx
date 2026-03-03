import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A flexbox container that arranges children with consistent spacing.
 *
 * State:
 * - direction: "column" | "row"  (default: "column")
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - align: "start" | "center" | "end" | "stretch"  (default: "stretch")
 * - wrap: boolean  (default: false)
 * - children: ChildDescriptor[]
 */
const TLStack: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const direction = (state.direction as string) ?? 'column';
  const gap = (state.gap as string) ?? 'default';
  const align = (state.align as string) ?? 'stretch';
  const wrap = state.wrap === true;
  const children = (state.children as unknown[]) ?? [];

  const className = [
    'tlStack',
    `tlStack--${direction}`,
    `tlStack--gap-${gap}`,
    `tlStack--align-${align}`,
    wrap ? 'tlStack--wrap' : '',
  ].filter(Boolean).join(' ');

  return (
    <div className={className}>
      {children.map((child, i) => (
        <TLChild key={i} control={child} />
      ))}
    </div>
  );
};

export default TLStack;
