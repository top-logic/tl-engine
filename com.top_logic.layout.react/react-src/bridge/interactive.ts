/**
 * Elements that handle a click themselves: the native form controls, and the controls that carry
 * their role through ARIA instead of an element name — a dropdown, for one, is a `div` with
 * `role="combobox"`, so leaving those out makes a click on it look like a click on the plain text
 * around it.
 */
export const INTERACTIVE_SELECTOR =
  'input, textarea, select, button, a, [contenteditable="true"], '
  + '[role="combobox"], [role="listbox"], [role="option"], [role="button"], [role="link"], '
  + '[role="checkbox"], [role="radio"], [role="switch"], [role="textbox"], [role="spinbutton"], '
  + '[role="slider"], [role="menu"], [role="menuitem"]';

/**
 * Whether the given target lies on an interactive element (see {@link INTERACTIVE_SELECTOR}) within
 * the given surface, the surface itself not counted: a gesture of the surface leaves such an element
 * alone.
 */
export function isInteractiveWithin(target: EventTarget | null, surface: Element): boolean {
  const hit = target instanceof Element ? target.closest(INTERACTIVE_SELECTOR) : null;
  return hit !== null && hit !== surface && surface.contains(hit);
}
