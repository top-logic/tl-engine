/**
 * The trigger of a floating surface (see usePopupSurface): what its click does and what it announces.
 *
 * <p>A click toggles the surface: it closes an open surface and opens a closed one. The press of a pointer reaches
 * the surface before the click, and for a trigger outside its surface that press is a press outside, which has
 * already closed it: the click that follows must not open it again, and asks pressClosedSurface (see outside-press)
 * whether the press of its gesture has closed a surface. A trigger that is among the elements a press may land in
 * without closing its surface (`inside`, the toolbar overflow) is never closed by its own press, and its click simply
 * toggles.</p>
 *
 * <p>The trigger announces the kind of surface it opens (`aria-haspopup`) and whether it is open
 * (`aria-expanded`). The result is the props of the part `MenuTrigger`; a trigger drawn by another part passes them as
 * those attributes.</p>
 */
import React from 'react';
import { pressClosedSurface } from './outside-press';

const { useCallback } = React;

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

/**
 * The click and the announcement of the trigger of a floating surface (see the module description).
 *
 * @param open Whether the surface is open.
 * @param setOpen Opens or closes the surface.
 * @param kind The kind of surface the trigger opens.
 * @param inside Whether the trigger is among the elements a press may land in without closing the surface (the
 *        `inside` of usePopupSurface), `false` if not given.
 */
export function usePopupTrigger(open: boolean, setOpen: (open: boolean) => void, kind: PopupKind, inside = false): PopupTrigger {
  const onClick = useCallback(() => {
    if (open) {
      setOpen(false);
    } else if (inside || !pressClosedSurface()) {
      setOpen(true);
    }
  }, [open, setOpen, inside]);
  return { onClick, expanded: open, haspopup: kind };
}
