import { React, usePopover, createPortal } from 'tl-react-bridge';
import type { TLDropMarker, TLRunningDrag } from 'tl-react-bridge';

// What a drop target (table, tree, Kanban board) shows while a drag hovers it and the server refuses
// a drop there: the verdict answered to a drop probe, and the hint carrying the reason next to the
// pointer.

/**
 * Where the hint on a refused drop target stands: at the pointer's `x`, beside the drag image, which
 * spans `height` pixels down from `y` (see {@link TLRunningDrag.image}).
 */
export interface HintAnchor {
  x: number;
  y: number;
  height: number;
}

/**
 * Why the target under the running drag refuses it: a tl-popover at the pointer, below the drag image
 * or, where the viewport has no room there, above it - not on it where either side has room, since the browser
 * draws the drag image over everything (a native tooltip is not shown while a drag runs). A hint that fits
 * neither side is shifted along the cross axis into the viewport and may then overlap the drag image. The target (table, tree, Kanban board)
 * moves it through `moveRef` on every dragover, so only the hint renders while the pointer moves, not
 * the target.
 */
export const DropHint: React.FC<{
  reason: string;
  start: HintAnchor;
  moveRef: React.MutableRefObject<((at: HintAnchor) => void) | null>;
}> = ({ reason, start, moveRef }) => {
  const [at, setAt] = React.useState(start);
  React.useEffect(() => {
    moveRef.current = (next) => setAt((p) => (p.x === next.x && p.y === next.y && p.height === next.height ? p : next));
    return () => { moveRef.current = null; };
  }, [moveRef]);
  const { setFloating, style } = usePopover({ open: true, anchor: at, placement: 'bottom-start' });
  return createPortal(
    <div ref={setFloating} className="tl-popover tl-drop-hint tl-type-body" role="status" style={style}>
      {reason}
    </div>,
    document.body);
};

/** The server's answer to a drop probe: whether a drop there would be accepted, and if not, why. */
export interface DropVerdict {
  accepted: boolean;
  /** Why the drop is refused, in the user's language. */
  reason?: string;
  /** What to draw for an accepted drop; absent where the target draws its own feedback. */
  marker?: TLDropMarker;
  /** Key of the item the marker is drawn at; absent for a marker of the control as a whole. */
  markerKey?: string;
}

/** Drag image extent for a drag of unknown origin: none. */
export const NO_DRAG_IMAGE: TLRunningDrag['image'] = { top: 0, bottom: 0 };
