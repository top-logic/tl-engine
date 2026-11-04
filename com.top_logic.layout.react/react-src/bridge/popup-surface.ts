/**
 * The shell of every surface that floats over the page at an anchor and closes again - a menu, a picker, the
 * overflow of a toolbar: one hook, so that the surfaces cannot drift apart in how they are placed, closed and
 * focused.
 *
 * <ul>
 * <li>Placed by {@link usePopover} at its anchor, its height held to the room on the side it stands on
 *     (`fitHeight` of usePopover): a surface too high for either side scrolls instead of covering its
 *     anchor.</li>
 * <li>Closed by a press outside ({@link useCloseOnOutsidePress}) and by Escape. Escape is bound in a keyboard scope
 *     of its own, registered when the surface opens and hence above the scopes around it: it closes the surface
 *     wherever the focus is - still on the trigger after a press, in a field of the surface - and inside a window
 *     it closes the surface, not the window.</li>
 * <li>The focus moves into the surface when it opens and back to where it was when it closes
 *     ({@link useFocusTrap}); if that place is gone and the focus has fallen to the body, the anchor takes it. A
 *     surface that is not modal (`trapFocus: false`) lets the focus leave, and gives it back only while it is
 *     still inside or has been dropped with the surface.</li>
 * <li>A surface that has no target for the focus when it opens - one placed only in the following frame, whose
 *     entries have no box yet - gets the focus moved to its target once more in the next frame.</li>
 * </ul>
 *
 * <p>Everything is bound to `open` only: a surface that stays mounted while closed (the toolbar overflow keeps its
 * entries live) holds no keyboard scope, no focus trap and no outside press until it opens.</p>
 *
 * <p>The surface itself - its element, role and content - is the caller's: it puts {@link PopupSurface.setRefs}
 * and {@link PopupSurface.style} on its root, a tl-popover portaled to the body, marked with
 * `anchoredOverlayProps`.</p>
 */
import React from 'react';
import { usePopover } from './popover';
import type { PopoverAnchor, PopoverPlacement } from './popover';
import { useCloseOnOutsidePress } from './outside-press';
import type { InsideRef } from './outside-press';
import { firstFocusable } from './focus-trap';
import { useFocusTrap, useStandaloneKeyboardScope } from './tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

/**
 * Where the focus goes when a surface opens: the first focusable element (`first`, a menu), the first form field
 * (`field`), the surface itself (`container`, a surface steering its focus itself), or the element a function picks
 * in the surface. Without a target, the surface itself.
 */
export type PopupInitialFocus = 'first' | 'field' | 'container' | ((surface: HTMLElement) => HTMLElement | null);

export interface PopupSurfaceOptions {
  /** Whether the surface is open. */
  open: boolean;

  /** Where the surface stands: at an element, or at a point of the viewport (a menu at the pointer). */
  anchor: PopoverAnchor | null;

  /** The side of the anchor the surface stands on, `bottom-start` if not given. */
  placement?: PopoverPlacement;

  /** Closes the surface; called on Escape and on a press outside. */
  onClose: () => void;

  /** Where the focus goes when the surface opens. */
  initialFocus: PopupInitialFocus;

  /**
   * Whether the focus is confined to the surface while it is open, `true` if not given. A surface that is not modal
   * (`false`) lets Tab and a press move the focus out, and closes on a press outside as every surface does.
   */
  trapFocus?: boolean;

  /**
   * Further elements a press may land in without closing the surface, besides the surface itself: the trigger that
   * toggles the surface on its click (see {@link usePopupTrigger}).
   */
  inside?: ReadonlyArray<InsideRef>;
}

export interface PopupSurface<T extends HTMLElement> {
  /** The root element of the surface, once rendered. */
  surfaceRef: React.RefObject<T | null>;

  /** The ref callback for the root element of the surface: sets {@link surfaceRef} and the element usePopover places. */
  setRefs: (element: T | null) => void;

  /** The position of the surface, for its root element. */
  style: React.CSSProperties;
}

/** The target of the focus in the surface that the given initial focus names; `null` if there is none. */
function focusTarget(surface: HTMLElement, initialFocus: PopupInitialFocus): HTMLElement | null {
  if (typeof initialFocus === 'function') {
    return initialFocus(surface);
  }
  if (initialFocus === 'container') {
    return surface;
  }
  return firstFocusable(surface, initialFocus === 'field');
}

/**
 * Places, closes and focuses a floating surface (see the module description).
 *
 * @param options The surface, its anchor and how it takes the focus.
 * @returns The ref callback and the style for the root element of the surface, and a ref holding that element.
 */
export function usePopupSurface<T extends HTMLElement = HTMLDivElement>(options: PopupSurfaceOptions): PopupSurface<T> {
  const { open, anchor, placement, onClose, initialFocus, trapFocus = true, inside = [] } = options;
  const surfaceRef = useRef<T | null>(null);
  const { setFloating, style } = usePopover({ open, anchor, placement, fitHeight: true });
  // A plain callback rather than floating-ui's useMergeRefs: that one's return type comes from the React types of
  // this module and fails to match the JSX of a module with its own copy of @types/react.
  const setRefs = useCallback((element: T | null) => {
    surfaceRef.current = element;
    setFloating(element);
  }, [setFloating]);

  useCloseOnOutsidePress(open, [surfaceRef, ...inside], onClose);
  useStandaloneKeyboardScope(open, {
    ESCAPE: () => {
      onClose();
      return true;
    },
  });
  useFocusTrap(open, surfaceRef, initialFocus, trapFocus ? {} : { trap: false, restore: 'from-inside' });
  useFocusRetry(open, surfaceRef, initialFocus);
  useFocusToAnchor(open, anchor, surfaceRef);

  return { surfaceRef, setRefs, style };
}

/**
 * Moves the focus to the target of the surface once more in the frame after it opened, if the focus trap found none
 * and focused the surface itself: the entries of a surface placed only in that frame (a menu at the pointer) have
 * no box when it opens, and the focus search skips them.
 */
function useFocusRetry(open: boolean, surfaceRef: React.RefObject<HTMLElement | null>, initialFocus: PopupInitialFocus): void {
  const initialFocusRef = useRef(initialFocus);
  initialFocusRef.current = initialFocus;
  useEffect(() => {
    if (!open || initialFocusRef.current === 'container') {
      return undefined;
    }
    const frame = requestAnimationFrame(() => {
      const surface = surfaceRef.current;
      // Only the fallback of the trap is replaced: a focus on an entry, or put elsewhere meanwhile, stays.
      if (surface === null || document.activeElement !== surface) {
        return;
      }
      const target = focusTarget(surface, initialFocusRef.current);
      if (target !== null && target !== surface) {
        target.focus();
      }
    });
    return () => cancelAnimationFrame(frame);
  }, [open, surfaceRef]);
}

/**
 * Gives the focus to the anchor element when the surface closes and the focus has no place: the element the focus
 * trap gives it back to may be gone, or there was none - a trigger that keeps the focus where it is on its press, a
 * browser that does not focus a button on a click. The focus then has fallen to the body, or, for a surface that stays
 * mounted while closed, stands on in the hidden surface. Checked after the trap has given the focus back, and only then.
 */
function useFocusToAnchor(open: boolean, anchor: PopoverAnchor | null, surfaceRef: React.RefObject<HTMLElement | null>): void {
  // The anchor while the surface is open: a caller may drop it together with closing the surface.
  const anchorElement = useRef<HTMLElement | null>(null);
  if (open) {
    anchorElement.current = anchor instanceof HTMLElement ? anchor : null;
  }
  useEffect(() => {
    if (!open) {
      return undefined;
    }
    return () => {
      const element = anchorElement.current;
      if (element === null) {
        return;
      }
      queueMicrotask(() => {
        const focused = document.activeElement;
        const lost = focused === null || focused === document.body || surfaceRef.current?.contains(focused) === true;
        if (element.isConnected && lost) {
          element.focus();
        }
      });
    };
  }, [open, surfaceRef]);
}
