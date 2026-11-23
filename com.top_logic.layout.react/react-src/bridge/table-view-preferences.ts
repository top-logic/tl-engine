import { useSyncExternalStore } from 'react';

// The table view of the person (zebra stripes, column lines), as the page holds it. The server writes the
// stored values as attributes of the element marked with MARK_ATTR when it renders the page, and the user menu
// changes them there while the page is open (a script of the server sets or removes the attribute). The store
// reads them from that element and follows it, so that every table of the page changes at once.

/** The table view of the person. */
export interface TableViewPreferences {

  /** Whether the rows alternate in background. */
  striped: boolean;

  /** Whether lines separate the columns. */
  columnLines: boolean;
}

/** The attribute marking the element the server writes the values on. */
const MARK_ATTR = 'data-tl-table-view';

const STRIPED_ATTR = 'data-tl-table-striped';

const COLUMN_LINES_ATTR = 'data-tl-table-column-lines';

/** The values the person has not chosen. */
const FACTORY: TableViewPreferences = { striped: false, columnLines: false };

/** The values of the page, or null while the DOM has not been read. */
let current: TableViewPreferences | null = null;

const listeners = new Set<() => void>();

/** Watches the marked element while a component uses the values; null otherwise. */
let observer: MutationObserver | null = null;

function readDom(): TableViewPreferences {
  if (typeof document === 'undefined') {
    return FACTORY;
  }
  const element = document.querySelector('[' + MARK_ATTR + ']');
  if (!element) {
    return FACTORY;
  }
  return {
    striped: element.hasAttribute(STRIPED_ATTR),
    columnLines: element.hasAttribute(COLUMN_LINES_ATTR),
  };
}

function getSnapshot(): TableViewPreferences {
  if (current === null) {
    current = readDom();
  }
  return current;
}

function subscribe(listener: () => void): () => void {
  if (listeners.size === 0) {
    observe();
  }
  listeners.add(listener);
  return () => {
    listeners.delete(listener);
    if (listeners.size === 0) {
      // Nobody follows the element any more: the next table reads it afresh, not a cache that went stale.
      observer?.disconnect();
      observer = null;
      current = null;
    }
  };
}

function observe(): void {
  const element = typeof document === 'undefined' ? null : document.querySelector('[' + MARK_ATTR + ']');
  if (!element || typeof MutationObserver === 'undefined') {
    return;
  }
  observer = new MutationObserver(() => {
    current = readDom();
    listeners.forEach((listener) => listener());
  });
  observer.observe(element, { attributes: true, attributeFilter: [STRIPED_ATTR, COLUMN_LINES_ATTR] });
}

/** The table view of the person; the component renders again when it changes. */
export function useTableViewPreferences(): TableViewPreferences {
  return useSyncExternalStore(subscribe, getSnapshot, () => FACTORY);
}

/** Forgets the values, so that the next read takes the DOM again. For tests only. */
export function resetTableViewPreferencesForTest(): void {
  current = null;
  listeners.forEach((listener) => listener());
}
