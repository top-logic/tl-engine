// What the React table puts on its DOM so that the design system's table styles take effect
// (UI-Durchgang 1, table): the density of the table, stripes, column lines, the alignment of a
// column, the group and even rows, the overline of the heading and the reveal of the filter funnel -
// and the density change that reaches the row height.

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { act, cleanup, screen, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart, setTableViewPreferences } from 'tl-react-bridge';
import type { TableViewPreferences } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiMenuTriggerAdapter from './adapters/MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle, resetTableView } from './adapters/wire-test-support';

// The test registry has no parts of its own: the table looks up the ones it draws with.
replacePart('Button', MuiButtonAdapter);
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);

class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

beforeEach(() => {
  resetTableView();
});

afterEach(() => {
  cleanup();
  resetTableView();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

const COLUMNS = [
  { name: 'name', label: 'Name', width: 120, sortable: true, filterable: true },
  { name: 'amount', label: 'Amount', width: 120, sortable: true, align: 'end' },
];

function row(index: number, extra: Record<string, unknown> = {}) {
  return {
    id: 'r' + index, index,
    cells: { name: childControl('n' + index, 'Name ' + index), amount: childControl('a' + index, String(index)) },
    ...extra,
  };
}

/**
 * Mounts a table. The view of the person (density, stripes, column lines) comes from the store of the bridge, here
 * `view`; `controlId` gives a second table of the page its own ID.
 */
function mountTable(state: Record<string, unknown> = {}, view: Partial<TableViewPreferences> = {}, controlId?: string) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  setTableViewPreferences({ density: '', striped: false, columnLines: false, ...view });
  return mountAdapter(TLTableView, {
    columns: COLUMNS,
    rows: [row(0), row(1), row(2)],
    totalRowCount: 3,
    ...state,
  }, { controlId });
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

function rowElement(index: number): HTMLElement {
  return root().querySelector<HTMLElement>(`[role="row"][aria-rowindex="${index + 2}"]`)!;
}

describe('the attributes of the table root', () => {
  it('carries the density of the person only when it names one', async () => {
    mountTable({}, { density: 'compact' });
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('compact');
  });

  it('carries the normal density when the person names it', async () => {
    mountTable({}, { density: 'normal' });
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('normal');
  });

  it('carries no density for the empty one', async () => {
    mountTable({}, { density: '' });
    await settle();
    expect(root().hasAttribute('data-tl-density')).toBe(false);
  });

  it('takes the view from the start values on the root element of the page, and from nothing else', async () => {
    const html = document.documentElement;
    html.setAttribute('data-tl-table-view', '');
    html.setAttribute('data-tl-table-density', 'compact');
    html.setAttribute('data-tl-table-striped', '');
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    // Not through mountTable(): it would set the store; the first read of it takes the root element.
    mountAdapter(TLTableView, { columns: COLUMNS, rows: [row(0)], totalRowCount: 1 });
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('compact');
    expect(root().hasAttribute('data-tl-striped')).toBe(true);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(false);
  });

  it('carries stripes and column lines as the store says, and none by default', async () => {
    mountTable();
    await settle();
    expect(root().hasAttribute('data-tl-striped')).toBe(false);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(false);

    act(() => setTableViewPreferences({ density: '', striped: true, columnLines: true }));
    expect(root().hasAttribute('data-tl-striped')).toBe(true);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(true);

    act(() => setTableViewPreferences({ density: '', striped: false, columnLines: false }));
    expect(root().hasAttribute('data-tl-striped')).toBe(false);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(false);
  });

  it('ignores the state keys the server no longer sends', async () => {
    mountTable({ viewDensity: 'compact', striped: true, columnLines: true });
    await settle();
    expect(root().hasAttribute('data-tl-density')).toBe(false);
    expect(root().hasAttribute('data-tl-striped')).toBe(false);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(false);
  });
});

describe('two tables of one page', () => {
  it('share the view: stripes chosen in the popover of the first show in the second without an answer of the server', async () => {
    mountTable({}, {}, 'c1');
    mountTable({}, {}, 'c2');
    await settle();
    const second = document.getElementById('c2')!;
    expect(second.hasAttribute('data-tl-striped')).toBe(false);

    const gears = screen.getAllByRole('button', { name: 'js.table.view' });
    expect(gears).toHaveLength(2);
    await userEvent.click(gears[0]);
    await settle();
    await userEvent.click(within(screen.getByRole('dialog')).getByRole('menuitemcheckbox', { name: 'js.table.viewStripes' }));
    await settle();

    expect(document.getElementById('c1')!.hasAttribute('data-tl-striped')).toBe(true);
    expect(second.hasAttribute('data-tl-striped')).toBe(true);
  });
});

describe('the columns and rows', () => {
  it('aligns the heading and the cells of an end column, and only those', async () => {
    mountTable({});
    await settle();

    const headers = root().querySelectorAll<HTMLElement>('[role="columnheader"]');
    expect(headers[0].hasAttribute('data-tl-align')).toBe(false);
    expect(headers[1].getAttribute('data-tl-align')).toBe('end');
    const cells = rowElement(0).querySelectorAll<HTMLElement>('[role="gridcell"]');
    expect(cells[0].hasAttribute('data-tl-align')).toBe(false);
    expect(cells[1].getAttribute('data-tl-align')).toBe('end');
  });

  it('keeps the order of title, sort mark and filter in an end column, as in a start column', async () => {
    mountTable({
      columns: [
        { ...COLUMNS[0], sortDirection: 'asc' },
        { ...COLUMNS[1], filterable: true, sortDirection: 'asc' },
      ],
    });
    await settle();

    const before = (a: Element, b: Element) => (a.compareDocumentPosition(b) & Node.DOCUMENT_POSITION_FOLLOWING) !== 0;
    const headers = Array.from(root().querySelectorAll<HTMLElement>('[role="columnheader"]'));
    expect(headers).toHaveLength(2);
    for (const header of headers) {
      const sort = header.querySelector('.tl-table__sort')!;
      const label = header.querySelector('.tl-table__label')!;
      const mark = header.querySelector('.tl-table__sort .tl-table__mark')!;
      const filter = header.querySelector('.tl-table__filter')!;
      // Title, sort mark, then the funnel - as it reads from the left, in a start column and in an end column alike;
      // only the content of an end column moves to the end of the cell (CSS).
      expect(before(label, mark)).toBe(true);
      expect(before(sort, filter)).toBe(true);
    }
  });

  it('marks the even rows by their index, not by their place in the DOM', async () => {
    // The window starts at row 3: rows 3 and 4 are the first two in the DOM.
    mountTable({ rows: [row(3), row(4), row(5)], totalRowCount: 10, scrollStart: 3 });
    await settle();

    expect(rowElement(3).getAttribute('data-tl-state') ?? '').not.toMatch(/\beven\b/);
    expect(rowElement(4).getAttribute('data-tl-state')).toMatch(/\beven\b/);
    expect(rowElement(5).getAttribute('data-tl-state') ?? '').not.toMatch(/\beven\b/);
  });

  it('marks a group row as a group, and keeps the other state words', async () => {
    mountTable({ rows: [row(0, { groupCount: 2 }), row(1)], totalRowCount: 2, treeMode: true });
    await settle();

    expect(rowElement(0).getAttribute('data-tl-state')).toMatch(/\bgroup\b/);
    expect(rowElement(0).getAttribute('data-tl-state')).toMatch(/\beven\b/);
    expect(rowElement(1).getAttribute('data-tl-state') ?? '').not.toMatch(/\bgroup\b/);
  });

  it('sets the heading label in the overline class', async () => {
    mountTable({});
    await settle();

    const labels = root().querySelectorAll('.tl-table__label');
    expect(labels).toHaveLength(2);
    labels.forEach(label => expect(label.classList).toContain('tl-type-overline'));
  });

  it('reveals the filter funnel on hover, as a direct child of its heading', async () => {
    mountTable({});
    await settle();

    const funnel = root().querySelector<HTMLElement>('.tl-table__filter')!;
    expect(funnel.classList).toContain('tl-reveal-on-hover');
    expect(funnel.parentElement!.getAttribute('role')).toBe('columnheader');
  });
});

describe('the header row of a group', () => {
  // Widths that tell the columns apart in a sum: 100 + 110 + 120 + 130 + 40.
  const WIDE = [
    { name: 'a', label: 'A', width: 100, sortable: true },
    { name: 'b', label: 'B', width: 110, sortable: true },
    { name: 'c', label: 'C', width: 120, sortable: true },
    { name: 'd', label: 'D', width: 130, sortable: true },
    { name: 'act', label: '', width: 40, sortable: false, pinnedEnd: true },
  ];

  function wideRow(index: number, extra: Record<string, unknown> = {}) {
    const cells: Record<string, unknown> = {};
    for (const column of WIDE) {
      cells[column.name] = childControl(column.name + index, column.name + index);
    }
    return { id: 'r' + index, index, cells, ...extra };
  }

  function mountWide(columns: Array<Record<string, unknown>>, state: Record<string, unknown> = {}) {
    mountTable({
      columns,
      rows: [wideRow(0, { groupCount: 2 }), wideRow(1)],
      totalRowCount: 2,
      treeMode: true,
      ...state,
    });
  }

  function cells(index: number): HTMLElement[] {
    return Array.from(rowElement(index).querySelectorAll<HTMLElement>('[role="gridcell"]'));
  }

  it('lets its value span all columns but the pinned one when no column aggregates', async () => {
    mountWide(WIDE);
    await settle();

    const group = cells(0);
    expect(group.map(cell => cell.dataset.col)).toEqual(['a', 'act']);
    expect(group[0].style.minWidth).toBe('460px');
    expect(group[0].getAttribute('aria-colspan')).toBe('4');
    expect(group[1].hasAttribute('aria-colspan')).toBe(false);
    expect(group[1].getAttribute('data-tl-state')).toMatch(/\bpinned\b/);
    expect(rowElement(0).getAttribute('data-tl-state')).toMatch(/\bgroup\b/);
  });

  it('stops in front of the first column that aggregates, which draws its cell', async () => {
    mountWide(WIDE.map(column => (column.name === 'c' ? { ...column, aggregate: true } : column)));
    await settle();

    expect(cells(0).map(cell => cell.dataset.col)).toEqual(['a', 'c', 'd', 'act']);
    expect(cells(0)[0].style.minWidth).toBe('210px');
    expect(cells(0)[0].getAttribute('aria-colspan')).toBe('2');
    expect(cells(0)[1].hasAttribute('aria-colspan')).toBe(false);
  });

  it('spans nothing when the second column aggregates', async () => {
    mountWide(WIDE.map(column => (column.name === 'b' ? { ...column, aggregate: true } : column)));
    await settle();

    expect(cells(0).map(cell => cell.dataset.col)).toEqual(['a', 'b', 'c', 'd', 'act']);
    expect(cells(0)[0].hasAttribute('aria-colspan')).toBe(false);
  });

  it('keeps the cells of a data row, one per column', async () => {
    mountWide(WIDE);
    await settle();

    expect(cells(1).map(cell => cell.dataset.col)).toEqual(['a', 'b', 'c', 'd', 'act']);
    expect(cells(1).some(cell => cell.hasAttribute('aria-colspan'))).toBe(false);
    expect(cells(1)[0].style.minWidth).toBe('100px');
  });

  it('ends with the frozen columns, so it sticks with them', async () => {
    mountWide(WIDE, { frozenColumnCount: 1 });
    await settle();
    expect(cells(0).map(cell => cell.dataset.col)).toEqual(['a', 'b', 'c', 'd', 'act']);
    expect(cells(0)[0].hasAttribute('aria-colspan')).toBe(false);
    expect(cells(0)[0].getAttribute('data-tl-state')).toMatch(/\bfrozen\b/);
    cleanup();

    mountWide(WIDE, { frozenColumnCount: 2 });
    await settle();
    expect(cells(0).map(cell => cell.dataset.col)).toEqual(['a', 'c', 'd', 'act']);
    expect(cells(0)[0].style.minWidth).toBe('210px');
  });
});

describe('the density change of the table (review focus 3)', () => {
  /** A page whose rows are 36 px high, and 32 px under a compact density at or above the element. */
  function stubRowToken() {
    const original = window.getComputedStyle.bind(window);
    vi.spyOn(window, 'getComputedStyle').mockImplementation((element: Element, pseudo?: string | null) => {
      const style = original(element, pseudo);
      return new Proxy(style, {
        get(target, property) {
          if (property === 'getPropertyValue') {
            return (name: string) => name === '--tl-size-row'
              ? (element.closest('[data-tl-density="compact"]') ? '32px' : '36px')
              : target.getPropertyValue(name);
          }
          const value = Reflect.get(target, property);
          return typeof value === 'function' ? value.bind(target) : value;
        },
      });
    });
  }

  it('places the rows with the new row height when the table switches its density', async () => {
    stubRowToken();
    mountTable({});
    await settle();
    expect(rowElement(1).style.top).toBe('36px');
    expect(rowElement(1).style.height).toBe('36px');

    act(() => setTableViewPreferences({ density: 'compact', striped: false, columnLines: false }));
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('compact');
    expect(rowElement(1).style.top).toBe('32px');
    expect(rowElement(1).style.height).toBe('32px');

    act(() => setTableViewPreferences({ density: '', striped: false, columnLines: false }));
    await settle();
    expect(rowElement(1).style.top).toBe('36px');
  });
});
