import { React, INTERACTIVE_SELECTOR } from 'tl-react-bridge';

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

/**
 * Whether the event originates from an interactive element the user can operate: the nearest
 * {@link INTERACTIVE_SELECTOR interactive} element around the target is neither disabled nor
 * read-only (`:disabled`, `[readonly]`, `aria-disabled="true"`, `aria-readonly="true"`).
 *
 * A click on an element that cannot be operated - a read-only checkbox displaying a value, for
 * one - does nothing for that element, so a surrounding element may read it as a click on itself,
 * e.g. a table row selecting itself.
 */
export function isOperableTarget(event: React.SyntheticEvent): boolean {
  const target = event.target as Element | null;
  const interactive = target?.closest?.(INTERACTIVE_SELECTOR);
  return !!interactive && !interactive.matches(INOPERABLE_SELECTOR);
}

/** Interactive elements that currently accept no input. */
const INOPERABLE_SELECTOR =
  ':disabled, [readonly], [aria-disabled="true"], [aria-readonly="true"]';
