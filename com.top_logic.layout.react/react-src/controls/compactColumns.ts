/**
 * The columns a table displays while the window is compact, and the translation between counts of
 * those and counts of all of its columns.
 *
 * In the compact display (a phone) a table leaves out the columns marked `hideWhenCompact`. The
 * server knows nothing of that: it keeps one arrangement for both displays and counts all of the
 * table's columns - in the target position of a moved column and in the number of frozen columns.
 * The client counts the columns it displays, so each count crossing the wire is translated here.
 */

/** What the translation needs to know of a column. */
export interface CompactColumn {
  /** Whether the table leaves the column out while the window is compact. */
  hideWhenCompact?: boolean;
}

/** The columns displayed: all of them, in the compact display without the ones marked `hideWhenCompact`. */
export function shownColumns<C extends CompactColumn>(all: C[], compact: boolean): C[] {
  return compact ? all.filter((column) => !column.hideWhenCompact) : all;
}

/**
 * How many of all columns the first `count` displayed columns reach: the count the server is told
 * when the user freezes the displayed columns up to the `count`-th one.
 */
export function fullCount<C>(all: C[], shown: C[], count: number): number {
  if (count <= 0 || shown.length === 0) {
    return 0;
  }
  return all.indexOf(shown[Math.min(count, shown.length) - 1]) + 1;
}

/**
 * How many displayed columns are among the first `count` of all columns: the frozen columns the
 * table displays for the count the server sent.
 */
export function shownCount<C>(all: C[], shown: C[], count: number): number {
  return shown.filter((column) => all.indexOf(column) < count).length;
}

/**
 * The position among all columns that a column moved to the displayed position `to` is sent with.
 *
 * The server takes the column out and inserts it at the given position. The moved column ends up
 * where the displayed column at `to` was, before it when moved back, after it when moved forth:
 * whatever columns are left out between them keep their place relative to that column.
 */
export function fullIndex<C>(all: C[], shown: C[], to: number): number {
  return all.indexOf(shown[to]);
}
