import { React, useTLState, TLChild, useFillHost, FillProvider } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A flexbox container that arranges children with consistent spacing.
 *
 * State:
 * - direction: "column" | "row"  (default: "column")
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - align: "start" | "center" | "end" | "stretch"  (default: "stretch")
 * - wrap: boolean  (default: false)
 * - growFirst: boolean  (default: false) — first child fills the main axis
 * - cssClass: string - optional additional CSS class appended to the layout classes
 * - children: ChildDescriptor[]
 *
 * Takes part in the fill contract as a container: a stack hosting a filling child fills its own
 * container, so that the child's height resolves against a definite one.
 */
const TLStack: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  const direction = (state.direction as string) ?? 'column';
  const gap = (state.gap as string) ?? 'default';
  const align = (state.align as string) ?? 'stretch';
  const wrap = state.wrap === true;
  const growFirst = state.growFirst === true;
  const children = (state.children as unknown[]) ?? [];

  const [fillClass, fillHost] = useFillHost();

  const className = [
    'tlStack',
    `tlStack--${direction}`,
    `tlStack--gap-${gap}`,
    `tlStack--align-${align}`,
    wrap ? 'tlStack--wrap' : '',
    growFirst ? 'tlStack--grow-first' : '',
    fillClass,
    (state.cssClass as string) ?? '',
  ].filter(Boolean).join(' ');

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={className}>
        {children.map((child, i) => (
          <TLChild key={i} control={child} />
        ))}
      </div>
    </FillProvider>
  );
};

export default TLStack;
