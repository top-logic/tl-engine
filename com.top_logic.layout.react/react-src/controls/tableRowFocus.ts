import { operableSelector } from 'tl-react-bridge';

/**
 * Elements that accept text/edit focus inside an editable cell. Disabled and read-only controls are
 * excluded (see operableSelector): a row that is not edited still renders its boolean columns as
 * checkboxes displaying the value, marked `aria-readonly`, which must not count as "this row is
 * editable".
 */
export const EDITABLE_SELECTOR = operableSelector('input, textarea, select, [contenteditable="true"]');

/** Every keyboard-focusable element inside a row cell, including enabled action buttons/links. */
export const FOCUSABLE_SELECTOR = EDITABLE_SELECTOR + ', button:not([disabled]), a[href]';

/** The cell `<div>`s (carrying data-row/data-col) of the given row, in column order. */
export function rowCells(body: HTMLElement, rowId: string): HTMLElement[] {
  return Array.from(body.querySelectorAll<HTMLElement>('[data-row][data-col]'))
    .filter((c) => c.dataset.row === rowId);
}

/**
 * Whether the given element of the given cell can take the focus: neither it nor an element
 * between it and the cell is hidden. A control may keep an element it never shows - the
 * `<input type="file">` behind an upload button, say -; focusing it does nothing, so the focus
 * would go nowhere.
 */
function takesFocus(element: HTMLElement, cell: HTMLElement): boolean {
  if (element instanceof HTMLInputElement && element.type === 'hidden') {
    return false;
  }
  for (let e: HTMLElement | null = element; e && e !== cell; e = e.parentElement) {
    if (e.hidden || e.hasAttribute('inert')) {
      return false;
    }
    const style = getComputedStyle(e);
    if (style.display === 'none' || style.visibility === 'hidden') {
      return false;
    }
  }
  return true;
}

/**
 * The elements of the given cells matching the given selector that can take the focus, in
 * document order.
 */
export function focusablesIn(cells: HTMLElement[], selector: string): HTMLElement[] {
  return cells.flatMap((cell) => Array.from(cell.querySelectorAll<HTMLElement>(selector))
    .filter((element) => takesFocus(element, cell)));
}

/**
 * The first editable element of a row that can take the focus, `null` when the row has none (e.g.
 * a not-yet-selected row in a single-row-editing table).
 */
export function editableInRow(body: HTMLElement, rowId: string): HTMLElement | null {
  return focusablesIn(rowCells(body, rowId), EDITABLE_SELECTOR)[0] ?? null;
}

/**
 * Where the focus goes in a row that was entered: the editable element of the given column; without
 * a column, the first editable element - where Tab into the row from above lands -, or with `last`
 * the last focusable element, an action button included - where Shift+Tab into the row from below
 * lands. `null` when the row has no such element.
 *
 * A column whose control takes no caret (a dropdown, a checkbox) yields `null`: the focus must not
 * go to some other column, which would move it, and the horizontal scroll position with it, away
 * from the cell the user addressed.
 */
export function focusTargetInRow(
  body: HTMLElement, rowId: string, opts: { col?: string; last?: boolean } = {}
): HTMLElement | null {
  const cells = rowCells(body, rowId);
  if (opts.col) {
    const target = cells.filter((c) => c.dataset.col === opts.col);
    return focusablesIn(target, EDITABLE_SELECTOR)[0] ?? null;
  }
  if (opts.last) {
    const focusables = focusablesIn(cells, FOCUSABLE_SELECTOR);
    return focusables[focusables.length - 1] ?? null;
  }
  return focusablesIn(cells, EDITABLE_SELECTOR)[0] ?? null;
}
