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
 * <p>A surface may open a sub-surface of its own - a select popup, a menu, a date picker - that is
 * portaled to the document body and is therefore no descendant of the surface's element. Such a
 * sub-surface marks its root as an anchored overlay ({@link ANCHORED_OVERLAY_ATTR}), as it does for
 * the focus trap of the surface. A press inside an anchored overlay counts as outside only for the
 * surfaces stacked above the overlay: the surfaces active while an overlay stands are ordered by
 * the time they became active, and the overlay stands at the position of the most recently
 * activated surface the press lands in; a surface rendered open together with the surface it sits
 * in counts as activated after it. An overlay that is no surface of this hook - a popup a
 * component library closes on its own - stands above every surface, like the topmost one. A press
 * in a select popup opened inside a popover thus keeps both open, a press in a menu below an open
 * popover closes the popover, and a press beside both closes both.</p>
 *
 * <p>A trigger that opens a surface on the click following the press cannot see the surface it
 * opens - a menu placed by the server at viewport coordinates, a swatch popup whose open state
 * lives elsewhere - and so cannot tell whether the press of that very gesture has closed it. It
 * asks {@link pressClosedSurface} instead: an answer of {@code true} means the gesture has already
 * spent itself on closing a surface, and the click keeps its hands off, leaving the surface
 * closed.</p>
 */

import { useEffect, useRef } from 'react';
import { ANCHORED_OVERLAY_ATTR } from './focus-trap';

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

/** An active surface: the order of its activation and the elements a press may land in. */
interface Surface {
  /** Higher is more recently activated, i.e. stacked higher. */
  readonly id: number;
  readonly contains: (target: Node) => boolean;
}

/** The surfaces currently active, in no particular order. */
const _surfaces: Surface[] = [];

let _nextSurfaceId = 1;

/**
 * Whether a press on {@code target}, which lies outside of {@code surface}, lands in an anchored
 * overlay that stands above {@code surface}.
 */
function inOverlayAbove(surface: Surface, target: Node): boolean {
  const element = target instanceof Element ? target : target.parentElement;
  if (element == null || element.closest(`[${ANCHORED_OVERLAY_ATTR}]`) == null) {
    return false;
  }
  let owner: Surface | null = null;
  for (const other of _surfaces) {
    if (other.contains(target) && (owner == null || other.id > owner.id)) {
      owner = other;
    }
  }
  return owner == null || owner.id > surface.id;
}

/**
 * An element the press may land in, held by the ref of the component that renders it.
 *
 * <p>A ref that currently holds no element names no area: a press is outside of it.</p>
 */
export type InsideRef = { readonly current: HTMLElement | null };

/**
 * Calls {@link onClose} for every press outside all of the {@link inside} elements, while
 * {@code active}. A press in an anchored overlay standing above the surface is no press outside
 * (see the module description).
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
  // The stacking position is taken when the surface renders active, not when its effect runs: a
  // parent renders before its children, but runs its effects after theirs, and a sub-surface
  // mounted open together with its surface must stand above it.
  const stackRef = useRef(0);
  if (!active) {
    stackRef.current = 0;
  } else if (stackRef.current === 0) {
    stackRef.current = _nextSurfaceId++;
  }

  useEffect(() => {
    if (!active) {
      return;
    }
    const surface: Surface = {
      id: stackRef.current,
      contains: target => insideRef.current.some(ref => ref.current?.contains(target) === true),
    };
    _surfaces.push(surface);
    const handlePointerDown = (event: PointerEvent) => {
      const target = event.target as Node | null;
      if (target != null && (surface.contains(target) || inOverlayAbove(surface, target))) {
        return;
      }
      _pressClosedSurface = true;
      onCloseRef.current();
    };
    document.addEventListener('pointerdown', handlePointerDown, { capture: true });
    return () => {
      document.removeEventListener('pointerdown', handlePointerDown, { capture: true });
      const index = _surfaces.indexOf(surface);
      if (index >= 0) {
        _surfaces.splice(index, 1);
      }
    };
  }, [active]);
}
