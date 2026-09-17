import { React, useTLState, TLChild, useFillHost, FillProvider } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Insets a single content control from the surrounding container border.
 *
 * State:
 * - child: ChildDescriptor
 *
 * Takes part in the fill contract as a container: an inset around a filling content fills its own
 * container, so the inset content spans the available height minus the padding.
 */
const TLInset: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [fillClass, fillHost] = useFillHost();

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={fillClass ? 'tlInset ' + fillClass : 'tlInset'}>
        {state.child && <TLChild control={state.child} />}
      </div>
    </FillProvider>
  );
};

export default TLInset;
