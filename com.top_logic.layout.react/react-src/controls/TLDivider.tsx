import { React, useTLState, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps, DividerStateJson } from 'tl-react-bridge';

/**
 * Control drawing a line that separates the content before it from the content after it, drawn by
 * the part `Divider`: its direction, the text in the line and where that stands, and where the line
 * begins.
 *
 * The state is described by DividerStateJson.
 */
const TLDivider: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<DividerStateJson>>();
  const Divider = usePart('Divider');

  if (state.hidden === true) {
    return null;
  }

  return (
    <Divider
      id={controlId}
      orientation={state.orientation ?? undefined}
      label={state.label || undefined}
      labelPosition={state.labelPosition ?? undefined}
      inset={state.inset ?? undefined}
      className={rootClassName(state) || undefined}
    />
  );
};

export default TLDivider;
