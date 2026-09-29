import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A maximize root that provides the positioning context for maximized panels.
 *
 * When a descendant TLPanel has expansionState === 'MAXIMIZED', it renders with
 * position: absolute; inset: 0 inside this container, overlaying the normal content.
 *
 * State:
 * - child: ChildDescriptor (the normal content tree)
 * - maximized: boolean
 */
const TLMaximizeRoot: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();

  return (
    <div
      id={controlId}
      className={`tlMaximizeRoot${state.maximized === true ? ' tlMaximizeRoot--maximized' : ''}`}
      style={{ position: 'relative', width: '100%', height: '100%', overflow: 'hidden' }}
    >
      <TLChild control={state.child} />
    </div>
  );
};

export default TLMaximizeRoot;
