import type { TLRunningDrag } from 'tl-react-bridge';

/**
 * What a drop target shows while a drag hovers it and the server refuses a drop there: the
 * verdict answered to a drop probe, and the hint carrying the reason next to the pointer.
 */

/** The server's answer to a drop probe: whether a drop there would be accepted, and if not, why. */
export interface DropVerdict {
  accepted: boolean;
  /** Why the drop is refused, in the user's language. */
  reason?: string;
}

/** Distance in pixels between the hint on a refused drop target and the pointer or drag image. */
const DROP_HINT_GAP = 8;

/**
 * Places the hint on a refused drop target right of the pointer at viewport position (`x`, `y`)
 * and below the drag image, or on the other side where the viewport has no room for it there.
 *
 * @param image Vertical extent of the drag image relative to the pointer, see
 *        {@link TLRunningDrag.image}.
 */
export function placeDropHint(hint: HTMLElement, x: number, y: number, image: TLRunningDrag['image']): void {
  const width = hint.offsetWidth;
  const height = hint.offsetHeight;
  let left = x + DROP_HINT_GAP;
  if (left + width > window.innerWidth) {
    left = x - DROP_HINT_GAP - width;
  }
  let top = y + image.bottom + DROP_HINT_GAP;
  if (top + height > window.innerHeight) {
    top = y + image.top - DROP_HINT_GAP - height;
  }
  hint.style.left = Math.max(0, left) + 'px';
  hint.style.top = Math.max(0, top) + 'px';
}

/** Drag image extent for a drag of unknown origin: none. */
export const NO_DRAG_IMAGE: TLRunningDrag['image'] = { top: 0, bottom: 0 };
