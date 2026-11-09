import { useSyncExternalStore } from 'react';

// The table view of the person (density, zebra stripes, column lines), as the page holds it. The server
// writes the stored values as attributes of the element marked with MARK_ATTR when it renders the page; the
// store takes them from there when first read and hands changes to every table of the page at once.

/** The row density of a table: the person's choice, or '' for the one the theme gives. */
export type TableDensity = '' | 'normal' | 'compact';

/** The table view of the person. */
export interface TableViewPreferences {

  /** The row density, '' for the density of the theme. */
  density: TableDensity;

  /** Whether the rows alternate in background. */
  striped: boolean;

  /** Whether lines separate the columns. */
  columnLines: boolean;
}

/** The attribute marking the element the server writes the start values on. */
const MARK_ATTR = 'data-tl-table-view';

/** The values the person has not chosen. */
const FACTORY: TableViewPreferences = { density: '', striped: false, columnLines: false };

/** The values of the page, or null while the DOM has not been read. */
let current: TableViewPreferences | null = null;

const listeners = new Set<() => void>();

function readDom(): TableViewPreferences {
  if (typeof document === 'undefined') {
    return FACTORY;
  }
  const element = document.querySelector('[' + MARK_ATTR + ']');
  if (!element) {
    return FACTORY;
  }
  const density = element.getAttribute('data-tl-table-density');
  return {
    density: density === 'normal' || density === 'compact' ? density : '',
    striped: element.hasAttribute('data-tl-table-striped'),
    columnLines: element.hasAttribute('data-tl-table-column-lines'),
  };
}

function getSnapshot(): TableViewPreferences {
  if (current === null) {
    current = readDom();
  }
  return current;
}

function subscribe(listener: () => void): () => void {
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
  };
}

/** The table view of the person; the component renders again when it changes. */
export function useTableViewPreferences(): TableViewPreferences {
  return useSyncExternalStore(subscribe, getSnapshot, () => FACTORY);
}

/** Sets the table view of the page and tells every component using it. */
export function setTableViewPreferences(next: TableViewPreferences): void {
  current = { density: next.density, striped: next.striped, columnLines: next.columnLines };
  listeners.forEach((listener) => listener());
}

/** Forgets the values, so that the next read takes the DOM again. For tests only. */
export function resetTableViewPreferencesForTest(): void {
  current = null;
  listeners.forEach((listener) => listener());
}
