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
 */

import { useEffect, useRef } from 'react';

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
      onCloseRef.current();
    };
    document.addEventListener('pointerdown', handlePointerDown, { capture: true });
    return () => document.removeEventListener('pointerdown', handlePointerDown, { capture: true });
  }, [active]);
}
