import { React, useTLState, TLChild, useFillHost, FillProvider } from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';

/**
 * One frame of the drill-down stack.
 *
 * A covered frame keeps its control tree and its layout box, so returning to it shows it as the
 * user left it - the selected tab, the selected table row, the values being edited and the scroll
 * offsets included. Its own wrapper carries the hiding, because the frame content renders its root
 * element itself and would overwrite a style set on it from the outside.
 */
const TileFrame: React.FC<{ frame: ChildDescriptor; covered: boolean }> = ({ frame, covered }) => {
  const [fillClass, fillHost] = useFillHost();

  const className = [
    'tlTileStack__frame',
    covered ? 'tlTileStack__frame--covered' : '',
    fillClass,
  ].filter(Boolean).join(' ');

  return (
    <FillProvider host={fillHost}>
      <div className={className}>
        <TLChild control={frame} />
      </div>
    </FillProvider>
  );
};

/**
 * Renders the frames of a tile-stack drill-down navigation, the active one on top of the ones it
 * covers.
 *
 * State:
 * - frames: ChildDescriptor[] - one frame per stack position, the initial view first
 * - activeIndex: number - position of the frame to display
 *
 * Takes part in the fill contract as a container: a stack whose frame fills fills its own
 * container, so the frame content spans the available height instead of the stack collapsing to
 * the content height.
 */
const TLTileStack: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [fillClass, fillHost] = useFillHost();

  const frames = (state.frames as ChildDescriptor[]) ?? [];
  const activeIndex = (state.activeIndex as number) ?? 0;

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} className={fillClass ? 'tlTileStack ' + fillClass : 'tlTileStack'}>
        {frames.map((frame, i) => (
          <TileFrame key={frame.controlId} frame={frame} covered={i !== activeIndex} />
        ))}
      </div>
    </FillProvider>
  );
};

export default TLTileStack;
