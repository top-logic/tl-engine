import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Renders the top frame of a tile-stack drill-down navigation.
 *
 * State:
 * - frame: ChildDescriptor | null - the currently active frame (initial view or top of the stack)
 */
const TLTileStack: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  return (
    <div id={controlId} className="tlTileStack" style={{ width: '100%', height: '100%' }}>
      {state.frame && <TLChild control={state.frame} />}
    </div>
  );
};

export default TLTileStack;
