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
  + '[role="slider"], [role="menu"], [role="menuitem"], [role="menuitemcheckbox"], [role="menuitemradio"]';

/**
 * Whether the given target lies on an interactive element (see {@link INTERACTIVE_SELECTOR}) within
 * the given surface, the surface itself not counted: a gesture of the surface leaves such an element
 * alone.
 */
export function isInteractiveWithin(target: EventTarget | null, surface: Element): boolean {
  const hit = target instanceof Element ? target.closest(INTERACTIVE_SELECTOR) : null;
  return hit !== null && hit !== surface && surface.contains(hit);
}

/**
 * Whether the given target lies on an interactive element (see {@link INTERACTIVE_SELECTOR}), the
 * target itself counted. A gesture of the surrounding element must leave such a target alone:
 * neither steal its focus, nor suppress its default mouse handling (e.g. double-click word selection
 * in a text input), nor read the click as one on the surface around it.
 *
 * Unlike {@link isInteractiveWithin}, every interactive ancestor counts - also the surface itself,
 * where it carries an interactive role (an option, a button).
 */
export function isInteractiveTarget(target: EventTarget | null): boolean {
  return target instanceof Element && target.closest(INTERACTIVE_SELECTOR) !== null;
}

/**
 * Whether the given target lies on an interactive element the user can operate: the nearest
 * {@link INTERACTIVE_SELECTOR interactive} element around the target is neither disabled nor
 * read-only (`:disabled`, `[readonly]`, `aria-disabled="true"`, `aria-readonly="true"`).
 *
 * A click on an element that cannot be operated - a read-only checkbox displaying a value, for
 * one - does nothing for that element, so a surrounding element may read it as a click on itself,
 * e.g. a table row selecting itself.
 */
export function isOperableTarget(target: EventTarget | null): boolean {
  const interactive = target instanceof Element ? target.closest(INTERACTIVE_SELECTOR) : null;
  return interactive !== null && !interactive.matches(INOPERABLE_SELECTOR);
}

/** Interactive elements that currently accept no input. */
const INOPERABLE_SELECTOR =
  ':disabled, [readonly], [aria-disabled="true"], [aria-readonly="true"]';
