import { React } from 'tl-react-bridge';

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
 * Whether the event originates from an interactive element (input, button, link, editor,
 * dropdown). A gesture of the surrounding element must leave such clicks alone: neither steal the
 * element's focus, nor suppress its default mouse handling (e.g. double-click word selection in a
 * text input), nor read the click as one on the surface around it.
 */
export function isInteractiveTarget(event: React.SyntheticEvent): boolean {
  const target = event.target as Element | null;
  return !!target?.closest?.(INTERACTIVE_SELECTOR);
}
