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
