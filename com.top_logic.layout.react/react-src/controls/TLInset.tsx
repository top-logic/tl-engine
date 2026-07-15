import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Insets a single content control from the surrounding container border.
 *
 * State:
 * - child: ChildDescriptor
 */
const TLInset: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  return (
    <div id={controlId} className="tlInset">
      {state.child && <TLChild control={state.child} />}
    </div>
  );
};

export default TLInset;
