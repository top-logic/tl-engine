/**
 * Closing a transient surface - a menu, a dropdown, a flyout, a popup - on a press that lands
 * outside of it.
 *
 * <p>The press is taken from the document's {@code pointerdown} in the capture phase, so that
 * every press reaches the surface regardless of what the element under the pointer makes of it.
 * A drag handle - a splitter, a window title bar, a resize grip - suppresses the default of its
 * own press to keep the browser from starting a selection or a native drag, and with it the
 * compatibility {@code mousedown} the browser would otherwise dispatch; a control that treats the
 * press as its own may also stop it from propagating. Either would leave an open menu standing
 * over a panel that is already being resized.</p>
 *
 * <p>A surface names the elements the press may land in without closing it: its own element, and
 * the trigger it hangs off where that trigger toggles it - the toggle then decides on the
 * following click, instead of the press closing the surface and the click reopening it.</p>
 *
 * <p>A trigger that opens a surface on the click following the press cannot see the surface it
 * opens - a menu placed by the server at viewport coordinates, a swatch popup whose open state
 * lives elsewhere - and so cannot tell whether the press of that very gesture has closed it. It
 * asks {@link pressClosedSurface} instead: an answer of {@code true} means the gesture has already
 * spent itself on closing a surface, and the click keeps its hands off, leaving the surface
 * closed.</p>
 */

import { useEffect, useRef } from 'react';

let _installed = false;

let _pressClosedSurface = false;

const onPressStart = () => {
  _pressClosedSurface = false;
};

/**
 * A click that no press carries - the keyboard activation of a button, a programmatic click - is a
 * gesture of its own, and the trigger it reaches opens its surface. Such a click is marked by a
 * {@code detail} of zero, the click count of the press it does not have.
 */
const onClick = (event: MouseEvent) => {
  if (event.detail === 0) {
    _pressClosedSurface = false;
  }
};

/**
 * Installs the document listeners that mark the bounds of a gesture.
 *
 * <p>Called once when the bridge loads, so that the press listener is the first
 * {@code pointerdown} listener on the document in the capture phase: every surface's own listener
 * is registered later, on the same target and in the same phase, and therefore runs after it. A
 * press thus starts with a clean slate that the surfaces closing on it then mark.</p>
 */
export function initOutsidePress(): void {
  if (_installed) {
    return;
  }
  _installed = true;
  document.addEventListener('pointerdown', onPressStart, { capture: true });
  document.addEventListener('click', onClick, { capture: true });
}

/**
 * Whether the press of the gesture under way has closed a surface.
 *
 * <p>Answers the click that follows the press: a trigger that opens a surface reads it to keep the
 * click from reopening what the press of the same gesture closed.</p>
 */
export function pressClosedSurface(): boolean {
  return _pressClosedSurface;
}

/**
 * An element the press may land in, held by the ref of the component that renders it.
 *
 * <p>A ref that currently holds no element names no area: a press is outside of it.</p>
 */
export type InsideRef = { readonly current: HTMLElement | null };

/**
 * Calls {@link onClose} for every press outside all of the {@link inside} elements, while
 * {@code active}.
 *
 * <p>Both the elements and the callback are read at the time of the press, so a caller may pass
 * freshly built arrays and closures.</p>
 *
 * @param active
 *        Whether the surface is open, i.e. whether a press can close it.
 * @param inside
 *        The elements a press does not close the surface from. An empty list makes every press
 *        close it.
 * @param onClose
 *        Closes the surface.
 */
export function useCloseOnOutsidePress(
  active: boolean,
  inside: ReadonlyArray<InsideRef>,
  onClose: () => void
): void {
  const insideRef = useRef(inside);
  insideRef.current = inside;
  const onCloseRef = useRef(onClose);
  onCloseRef.current = onClose;

  useEffect(() => {
    if (!active) {
      return;
    }
    const handlePointerDown = (event: PointerEvent) => {
      const target = event.target as Node | null;
      if (target != null && insideRef.current.some(ref => ref.current?.contains(target))) {
        return;
      }
      _pressClosedSurface = true;
      onCloseRef.current();
    };
    document.addEventListener('pointerdown', handlePointerDown, { capture: true });
    return () => document.removeEventListener('pointerdown', handlePointerDown, { capture: true });
  }, [active]);
}
