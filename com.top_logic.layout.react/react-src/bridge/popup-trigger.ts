/**
 * The trigger of a floating surface (see usePopupSurface): what its click does and what it announces.
 *
 * <p>A click toggles the surface: it closes an open surface and opens a closed one. The press of a pointer reaches
 * the surface before the click, and for a trigger outside its surface that press is a press outside, which has
 * already closed it: the click that follows must not open it again. The trigger therefore remembers the gesture in
 * which its surface closed (currentGesture of outside-press) and keeps the surface closed on a click of that same
 * gesture. A press that closed another surface - the filter of one column open, the funnel of the next one pressed -
 * does not stop the click: the one gesture closes the other surface and opens this one. A trigger that is among the
 * elements a press may land in without closing its surface (`inside` of usePopupSurface, the toolbar overflow) is
 * never closed by its own press, and its click simply toggles.</p>
 *
 * <p>The trigger announces the kind of surface it opens (`aria-haspopup`) and whether it is open
 * (`aria-expanded`). The result is the props of the part `MenuTrigger`; a trigger drawn by another part passes them as
 * those attributes.</p>
 */
import React from 'react';
import { currentGesture } from './outside-press';

const { useCallback, useLayoutEffect, useRef } = React;

/** The kind of surface a trigger opens: a menu of commands, or a dialog-like surface of other content (a popover). */
export type PopupKind = 'menu' | 'dialog';

export interface PopupTrigger {
  /** The click of the trigger: toggles the surface, see the module description. */
  onClick: () => void;

  /** Whether the surface is open, for `aria-expanded`. */
  expanded: boolean;

  /** The kind of surface the trigger opens, for `aria-haspopup`. */
  haspopup: PopupKind;
}

/** No gesture: the surface has not closed yet. Gestures are counted from zero upwards. */
const NO_GESTURE = -1;

/**
 * The click and the announcement of the trigger of a floating surface (see the module description).
 *
 * @param open Whether the surface is open.
 * @param setOpen Opens or closes the surface.
 * @param kind The kind of surface the trigger opens.
 */
export function usePopupTrigger(open: boolean, setOpen: (open: boolean) => void, kind: PopupKind): PopupTrigger {
  // The gesture in which the surface last went from open to closed. Taken when the closed state is committed: the
  // press that closes the surface sets its state, which is rendered before the click of the same gesture arrives.
  const closedIn = useRef(NO_GESTURE);
  const wasOpen = useRef(open);
  useLayoutEffect(() => {
    if (wasOpen.current && !open) {
      closedIn.current = currentGesture();
    }
    wasOpen.current = open;
  }, [open]);

  const onClick = useCallback(() => {
    if (open) {
      setOpen(false);
    } else if (closedIn.current !== currentGesture()) {
      setOpen(true);
    }
  }, [open, setOpen]);
  return { onClick, expanded: open, haspopup: kind };
}
