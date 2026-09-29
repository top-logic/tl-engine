import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Wraps a single child control in a scroll anchor.
 *
 * The wrapper carries the model object's anchor id as a `data-tl-anchor` attribute; a
 * `TLScrollLink` pointing at the same object scrolls the browser to this element.
 *
 * State:
 * - child: the wrapped child control descriptor
 * - anchor: string | null (the `data-tl-anchor` value)
 */
const TLAnchor: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const anchor = (state.anchor as string | null) ?? undefined;
  return (
    <div id={controlId} className="tlAnchor" data-tl-anchor={anchor}>
      {state.child && <TLChild control={state.child} />}
    </div>
  );
};

export default TLAnchor;
