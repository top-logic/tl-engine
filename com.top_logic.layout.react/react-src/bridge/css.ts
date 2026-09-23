// The class name of a control's root element: what the component itself needs to look like its
// kind, plus the class the server configured for this one control.
//
// Every control carries that configured class under the same state key, so the composition is done
// here once instead of in each component - a component only lists the classes it brings itself.

/**
 * State key under which a control carries the CSS class configured for it.
 *
 * Written by the server side of every control, so a component reads it through
 * {@link rootClassName} rather than by name.
 */
const CSS_CLASS = 'cssClass';

/**
 * The class name for the root element of a control.
 *
 * @param state The control state, which carries the configured CSS class.
 * @param classes The classes the component brings itself; falsy entries (a modifier that does not
 *        apply, an empty class from a hook) are left out.
 * @returns The given classes followed by the configured one, separated by spaces.
 */
export function rootClassName(
  state: Record<string, unknown>,
  ...classes: (string | false | null | undefined)[]
): string {
  const configured = state[CSS_CLASS];
  const all = typeof configured === 'string' ? [...classes, configured] : classes;
  return all.filter(Boolean).join(' ');
}
