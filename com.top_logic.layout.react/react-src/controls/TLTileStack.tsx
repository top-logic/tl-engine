import {
  React, useTLState, TLChild, useFillHost, FillProvider, rootClassName, useKeyedTransition,
} from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';

const { useRef } = React;

/**
 * One frame of the drill-down stack.
 *
 * A covered frame keeps its control tree and its layout box, so returning to it shows it as the
 * user left it - the selected tab, the selected table row, the values being edited and the scroll
 * offsets included. Its own wrapper carries the hiding, because the frame content renders its root
 * element itself and would overwrite a style set on it from the outside.
 */
const TileFrame: React.FC<{
  frame: ChildDescriptor;
  covered: boolean;
  wrapperRef: (element: HTMLDivElement | null) => void;
}> = ({ frame, covered, wrapperRef }) => {
  const [fillClass, fillHost] = useFillHost();

  const className = [
    'tlTileStack__frame',
    covered ? 'tlTileStack__frame--covered' : '',
    fillClass,
  ].filter(Boolean).join(' ');

  return (
    <FillProvider host={fillHost}>
      <div className={className} ref={wrapperRef}>
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
 * Moving through the stack is a transition an application stylesheet owns: the root says which way
 * the stack moved - `tlTileStack--forward` for a frame pushed, `tlTileStack--backward` for one
 * popped - and {@link useKeyedTransition} marks the frame arriving `tlTileStack__frame--entering`
 * and holds an inert copy of the frame left behind, marked `tlTileStack__frame--exiting`, over the
 * stack while it leaves. The engine only places the copy; the movement is the stylesheet's.
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

  const rootRef = useRef<HTMLDivElement | null>(null);
  const frameNodes = useRef(new Map<string, HTMLDivElement>()).current;

  // Which way the stack moved, from the position it displays: deeper into the stack is forward,
  // back towards its start is backward. It is decided where the change is noticed, so that the
  // modifier is on the root by the time the frame arriving is marked.
  const displayedIndex = useRef(activeIndex);
  const direction = useRef('forward');
  if (activeIndex !== displayedIndex.current) {
    direction.current = activeIndex > displayedIndex.current ? 'forward' : 'backward';
    displayedIndex.current = activeIndex;
  }

  useKeyedTransition({
    keys: frames.map((frame) => frame.controlId),
    node: (key) => frameNodes.get(key) ?? null,
    container: () => rootRef.current,
    enterClass: 'tlTileStack__frame--entering',
    exitClass: 'tlTileStack__frame--exiting',
  });

  const rootClass = ['tlTileStack', 'tlTileStack--' + direction.current, fillClass]
    .filter(Boolean).join(' ');

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} ref={rootRef} className={rootClassName(state, rootClass)}>
        {frames.map((frame, i) => (
          <TileFrame
            key={frame.controlId}
            frame={frame}
            covered={i !== activeIndex}
            wrapperRef={(element) => {
              if (element) {
                frameNodes.set(frame.controlId, element);
              } else {
                frameNodes.delete(frame.controlId);
              }
            }}
          />
        ))}
      </div>
    </FillProvider>
  );
};

export default TLTileStack;
