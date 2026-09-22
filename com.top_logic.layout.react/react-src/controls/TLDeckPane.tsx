import { React, useTLState, TLChild, useFillHost, FillProvider } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A deck pane that shows one child at a time, driven by a server-side active index.
 *
 * State:
 * - activeIndex: number
 * - activeChild: ChildDescriptor | null
 * - childCount: number
 *
 * Takes part in the fill contract as a container: a deck pane showing a filling child fills its
 * own container and offers that height to the child, while a deck pane showing content of its own
 * size takes exactly that size - so a deck standing inline, in an app bar or beside a breadcrumb,
 * is as wide and as high as the child it shows.
 */
const TLDeckPane: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [fillClass, fillHost] = useFillHost();

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={fillClass ? 'tlDeckPane ' + fillClass : 'tlDeckPane'}>
        {state.activeChild && <TLChild control={state.activeChild} />}
      </div>
    </FillProvider>
  );
};

export default TLDeckPane;
