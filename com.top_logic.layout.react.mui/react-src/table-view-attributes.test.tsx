// What the React table puts on its DOM so that the design system's table styles take effect
// (UI-Durchgang 1, table): the density of the table, stripes, column lines, the alignment of a
// column, the group and even rows, the overline of the heading and the reveal of the filter funnel -
// and the density change that reaches the row height.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import { CONTROL_ID, childControl, mountAdapter, patchState, settle } from './adapters/wire-test-support';

// The test registry has no parts of its own: the table looks up the ones it draws with.
replacePart('Button', MuiButtonAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);

class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
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

function mountTable(state: Record<string, unknown> = {}) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  return mountAdapter(TLTableView, {
    columns: COLUMNS,
    rows: [row(0), row(1), row(2)],
    totalRowCount: 3,
    ...state,
  });
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

function rowElement(index: number): HTMLElement {
  return root().querySelector<HTMLElement>(`[role="row"][aria-rowindex="${index + 2}"]`)!;
}

describe('the attributes of the table root', () => {
  it('carries the density of the table only when it names one', async () => {
    mountTable({ viewDensity: 'compact' });
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('compact');
  });

  it('carries the normal density when the table names it', async () => {
    mountTable({ viewDensity: 'normal' });
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('normal');
  });

  it('carries no density for the empty one and when the state has none', async () => {
    mountTable({ viewDensity: '' });
    await settle();
    expect(root().hasAttribute('data-tl-density')).toBe(false);
    cleanup();
    mountTable({});
    await settle();
    expect(root().hasAttribute('data-tl-density')).toBe(false);
  });

  it('carries stripes and column lines as the state says, and none by default', async () => {
    mountTable({});
    await settle();
    expect(root().hasAttribute('data-tl-striped')).toBe(false);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(false);

    patchState({ striped: true, columnLines: true });
    expect(root().hasAttribute('data-tl-striped')).toBe(true);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(true);

    patchState({ striped: false, columnLines: false });
    expect(root().hasAttribute('data-tl-striped')).toBe(false);
    expect(root().hasAttribute('data-tl-column-lines')).toBe(false);
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

    patchState({ viewDensity: 'compact' });
    await settle();
    expect(root().getAttribute('data-tl-density')).toBe('compact');
    expect(rowElement(1).style.top).toBe('32px');
    expect(rowElement(1).style.height).toBe('32px');

    patchState({ viewDensity: '' });
    await settle();
    expect(rowElement(1).style.top).toBe('36px');
  });
});
