import type { React } from 'tl-react-bridge';

/**
 * Whether the key asks for the context menu of the focused element: the ContextMenu key, or Shift+F10 where a
 * keyboard has none. One test for every place that opens a menu by keyboard, so they all answer the same keys.
 */
export function isContextMenuKey(e: React.KeyboardEvent): boolean {
  return e.key === 'ContextMenu' || (e.key === 'F10' && e.shiftKey);
}
