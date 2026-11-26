import {
  React, useTLState, TLChild, useFillHost, FillProvider, rootClassName, useKeyedTransition, scrollingAncestor,
} from 'tl-react-bridge';
import type { TLCellProps, ChildDescriptor } from 'tl-react-bridge';

const { useRef, useLayoutEffect } = React;

/**
 * One frame of the drill-down stack.
 *
 * A covered frame keeps its control tree and its layout box, so returning to it shows it as the
 * user left it - the selected tab, the selected table row, the values being edited and the scroll
 * offsets included. Its own wrapper carries the hiding, because the frame content renders its root
 * element itself and would overwrite a style set on it from the outside. The wrapper also clips
 * what the covered content shows beyond the stack (tile-stack.css): a covered frame lends the box
 * that scrolls the stack nothing to scroll.
 */
const TileFrame: React.FC<{
  frame: ChildDescriptor;
  covered: boolean;
  wrapperRef: (element: HTMLDivElement | null) => void;
}> = ({ frame, covered, wrapperRef }) => {
  const [fillClass, fillHost] = useFillHost();

  const className = ['tl-tile-stack__frame', fillClass].filter(Boolean).join(' ');

  return (
    <FillProvider host={fillHost}>
      <div className={className} data-tl-state={covered ? 'covered' : undefined} ref={wrapperRef}>
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
 * Moving through the stack is a transition the package stylesheet owns: the root says which way the stack
 * moved - `data-tl-state~="backward"` for a frame popped, nothing for one pushed - and
 * {@link useKeyedTransition} marks the frame arriving `entering` and holds an inert copy of the frame left behind,
 * marked `exiting`, over the stack while it leaves, both in the frame's `data-tl-state`; a frame that is covered
 * carries `covered` there.
 *
 * A frame that does not fill is scrolled by a box outside the stack, which all frames share. The
 * stack keeps the offset of that box per frame: a frame pushed starts at its top, a frame returned
 * to shows the part it showed when it was covered.
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

  // The offsets of the box scrolling the stack, by frame. The offset of the frame being covered is
  // taken while the page still shows it: once it is covered, it no longer extends that box, which
  // then may not scroll as far any more.
  const scrollOffsets = useRef(new Map<string, number>()).current;
  const changed = useRef(false);
  if (activeIndex !== displayedIndex.current) {
    const left = frames[displayedIndex.current];
    const scroller = rootRef.current && scrollingAncestor(rootRef.current);
    if (left) {
      scrollOffsets.set(left.controlId, scroller ? scroller.scrollTop : 0);
    }
    direction.current = activeIndex > displayedIndex.current ? 'forward' : 'backward';
    displayedIndex.current = activeIndex;
    changed.current = true;
  }

  const activeKey = frames[activeIndex]?.controlId;
  useLayoutEffect(() => {
    if (!changed.current) {
      return;
    }
    changed.current = false;
    for (const key of scrollOffsets.keys()) {
      if (!frames.some((frame) => frame.controlId === key)) {
        scrollOffsets.delete(key);
      }
    }
    const scroller = rootRef.current && scrollingAncestor(rootRef.current);
    if (scroller && activeKey) {
      scroller.scrollTop = scrollOffsets.get(activeKey) ?? 0;
    }
  }, [activeKey]);

  useKeyedTransition({
    keys: frames.map((frame) => frame.controlId),
    node: (key) => frameNodes.get(key) ?? null,
    container: () => rootRef.current,
    state: true,
  });

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} ref={rootRef} className={rootClassName(state, 'tl-tile-stack', fillClass)}
        data-tl-state={direction.current === 'backward' ? 'backward' : undefined}>
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
