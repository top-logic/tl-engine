import type { ControlStateJson } from '../state/control-state';

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
const CSS_CLASS = 'cssClass' satisfies keyof ControlStateJson;

/**
 * The class name for the root element of a control.
 *
 * @param state The control state, which carries the configured CSS class.
 * @param classes The classes the component brings itself; falsy entries (a modifier that does not
 *        apply, an empty class from a hook) are left out.
 * @returns The given classes followed by the configured one, separated by spaces.
 */
export function rootClassName(
  state: Record<string, unknown> | Partial<ControlStateJson>,
  ...classes: (string | false | null | undefined)[]
): string {
  const configured = (state as Partial<ControlStateJson>)[CSS_CLASS];
  const all = typeof configured === 'string' ? [...classes, configured] : classes;
  return all.filter(Boolean).join(' ');
}

/**
 * The value of a `data-tl-state` attribute: the state words that apply, separated by spaces, or
 * `undefined` - which leaves the attribute out - when none does. The design system reads each word
 * with `[data-tl-state~="word"]`.
 */
export function stateAttr(...words: (string | false | null | undefined)[]): string | undefined {
  const all = words.filter(Boolean).join(' ');
  return all === '' ? undefined : all;
}

/** The attribute the design system package reads state words from. */
export const STATE_ATTR = 'data-tl-state';

/**
 * Adds a word to the `data-tl-state` of an element the bridge marks outside of React (a jump target,
 * the document body), keeping the words already there.
 *
 * Not for an element a component renders: React writes its `data-tl-state` (see {@link stateAttr})
 * and drops a word set by hand on the next render.
 */
export function addStateWord(element: Element, word: string): void {
  const words = (element.getAttribute(STATE_ATTR) ?? '').split(/\s+/).filter(Boolean);
  if (!words.includes(word)) {
    words.push(word);
  }
  element.setAttribute(STATE_ATTR, words.join(' '));
}

/**
 * Removes a word from the `data-tl-state` of an element, keeping the other words; the attribute goes
 * with the last word. The counterpart of {@link addStateWord}.
 */
export function removeStateWord(element: Element, word: string): void {
  const words = (element.getAttribute(STATE_ATTR) ?? '').split(/\s+/).filter((w) => w && w !== word);
  if (words.length > 0) {
    element.setAttribute(STATE_ATTR, words.join(' '));
  } else {
    element.removeAttribute(STATE_ATTR);
  }
}
