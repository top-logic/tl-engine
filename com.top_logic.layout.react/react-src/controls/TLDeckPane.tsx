import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A deck pane that shows one child at a time, driven by a server-side active index.
 *
 * State:
 * - activeIndex: number
 * - activeChild: ChildDescriptor | null
 * - childCount: number
 */
const TLDeckPane: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  return (
    <div id={controlId} className="tlDeckPane" style={{ width: '100%', height: '100%' }}>
      {state.activeChild && <TLChild control={state.activeChild} />}
    </div>
  );
};

export default TLDeckPane;
