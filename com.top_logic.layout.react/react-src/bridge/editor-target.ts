/** The editors a node's or cell's content may hold: they answer arrows, Space and Enter themselves. */
const EDITOR_SELECTOR = 'input, textarea, select, [contenteditable]:not([contenteditable="false"])';

/**
 * Whether the target is an editor or sits inside one - an input, textarea, select or editable text.
 * A container with a virtual cursor (table, tree) leaves such a target its keys.
 */
export function isEditorTarget(target: EventTarget | null): boolean {
  return target instanceof Element && target.closest(EDITOR_SELECTOR) != null;
}
