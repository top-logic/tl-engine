// Client-side scroll-to-anchor primitive, shared by TLScrollLink (a click) and the server-driven
// <scroll-to-anchor> action (a FunctionCall to window.TLReact.scrollToAnchor).

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
    // navigation is perceptible.
    node.classList.remove('tlAnchor--flash');
    void (node as HTMLElement).offsetWidth;
    node.classList.add('tlAnchor--flash');
  });
}
