/**
 * The column list of the column selection and the change a `columnMove` makes to it.
 *
 * The server holds the list (ReactColumnSelectControl) and pushes it after every command. The column selection shows
 * the list it expects right after a drag or an Alt + arrow, until the server's answer arrives; {@link applyColumnMove}
 * computes that list exactly as the server's command does, so the answer replaces it without a jump.
 */

/** One column of the list, as the server pushes it. */
export interface ColumnEntry {
  name: string;
  label: string;
  visible: boolean;
  /** Whether the table freezes this column; the locked entries stand as a block at the start. */
  locked: boolean;
  /** Whether the rows are grouped by this column; at most one entry is. */
  grouped: boolean;
}

/** The arguments of the command `columnMove` (ColumnMoveArguments): a column, its target position and state. */
export interface ColumnMove {

  /** The name of the moved column. */
  column: string;

  /**
   * The position in the whole list (hidden columns included), counted without the moved column; ignored where the
   * column is hidden.
   */
  targetIndex: number;

  /** Whether the column ends up frozen; a hidden column is never frozen. */
  locked: boolean;

  /** Whether the column ends up shown. */
  visible: boolean;
}

/** The number of entries of the block of frozen columns at the start of the given list. */
export function lockedCount(entries: readonly ColumnEntry[]): number {
  let result = 0;
  while (result < entries.length && entries[result].locked) {
    result++;
  }
  return result;
}

/**
 * The list after the given move, as the server's command `columnMove` makes it.
 *
 * - An unknown column, and hiding the last shown one, change nothing: the given list itself is returned.
 * - Hiding keeps the column at its place; only a frozen one is released and moves directly behind the block.
 * - Showing puts the column at the target index of the list without it, with the given lock, taken to the nearest
 *   place of its block: a frozen column ends the block at the latest, a movable one starts the rest at the earliest.
 *
 * The given list is left unchanged.
 */
export function applyColumnMove(entries: readonly ColumnEntry[], move: ColumnMove): readonly ColumnEntry[] {
  const from = entries.findIndex((entry) => entry.name === move.column);
  if (from < 0) {
    return entries;
  }
  const entry = entries[from];
  const result = entries.slice();
  if (!move.visible) {
    if (entry.visible && entries.filter((other) => other.visible).length <= 1) {
      return entries;
    }
    if (entry.locked) {
      result.splice(from, 1);
      result.splice(lockedCount(result), 0, { ...entry, locked: false, visible: false });
    } else {
      result[from] = { ...entry, visible: false };
    }
    return result;
  }
  result.splice(from, 1);
  const block = lockedCount(result);
  const target = Math.min(Math.max(move.targetIndex, 0), result.length);
  result.splice(move.locked ? Math.min(target, block) : Math.max(target, block), 0,
    { ...entry, visible: true, locked: move.locked });
  return result;
}
