// Client-side scroll primitives: the scroll-to-anchor jump, shared by TLScrollLink (a click) and the
// server-driven <scroll-to-anchor> action (a FunctionCall to window.TLReact.scrollToAnchor), and the
// lookup of the box that scrolls an element.

import { addStateWord, removeStateWord } from './css';

/**
 * The `data-tl-state` word of a jump target while it flashes: the design system package draws
 * `[data-tl-anchor][data-tl-state~="flash"]` as a halo fading out (base.css).
 */
const FLASH = 'flash';

/** The keyframes of the flash in the design system package (base.css). */
const FLASH_ANIMATION = 'tl-anchor-flash';

/**
 * Ends the flash of the jump target this listener is registered on, once its own animation is over.
 *
 * Only the end counts, not a cancellation: restarting the flash on a repeated jump cancels the
 * running animation, and its cancellation arrives after the new one has started. A flash that never
 * ends (the target hidden meanwhile) keeps its word until the next jump to it restarts it.
 */
function endFlash(this: Element, event: Event): void {
  if (event.target !== this || (event as AnimationEvent).animationName !== FLASH_ANIMATION) {
    // Something inside the target animating, or another animation of the target itself.
    return;
  }
  this.removeEventListener('animationend', endFlash);
  removeStateWord(this, FLASH);
}

/**
 * Scrolls the element carrying the given `data-tl-anchor` value into view and briefly highlights it.
 *
 * Deferred to the next frame so it runs after any layout change that arrives in the same update
 * (e.g. a command that both mutates state and requests the scroll on the same response).
 */
export function scrollToAnchor(key: string): void {
  if (!key) {
    return;
  }
  requestAnimationFrame(() => {
    const node = document.querySelector('[data-tl-anchor="' + CSS.escape(key) + '"]');
    if (!node) {
      return;
    }
    node.scrollIntoView({ behavior: 'smooth', block: 'center' });
    // Restart the highlight even on repeat calls / when the target is already in view, so the
    // navigation is perceptible. The state goes when the animation ends; a listener of a jump that
    // is still flashing is the same function and is not added twice.
    removeStateWord(node, FLASH);
    void (node as HTMLElement).offsetWidth;
    addStateWord(node, FLASH);
    node.addEventListener('animationend', endFlash);
  });
}

/**
 * The nearest ancestor of the given element that scrolls its content vertically, null if none does.
 *
 * An ancestor counts while it is a scroll container (overflow `auto` or `scroll`) whose content is
 * taller than its box; a box that could scroll but currently has nothing to scroll is passed over.
 */
export function scrollingAncestor(element: HTMLElement): HTMLElement | null {
  for (let parent = element.parentElement; parent; parent = parent.parentElement) {
    const overflow = getComputedStyle(parent).overflowY;
    if ((overflow === 'auto' || overflow === 'scroll') && parent.scrollHeight > parent.clientHeight) {
      return parent;
    }
  }
  return null;
}
