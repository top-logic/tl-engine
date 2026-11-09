// A column label is put into the announcement of a step literally: "$&" and the like are no replacement patterns.
// Own file: the translations are cached per page, and this test needs the templates, not the keys the other tests see.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup, fireEvent, screen } from '@testing-library/react';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import { childControl, mountAdapter, settle } from './adapters/wire-test-support';

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

const LABEL = "Cost $& $` $'";

const COLUMNS = [
  { name: 'a', label: 'A', width: 100, sortable: true, reorderable: true },
  { name: 'b', label: 'B', width: 100, sortable: true, reorderable: true },
  { name: 'c', label: LABEL, width: 100, sortable: true, reorderable: true },
];

describe('the announcement of a step', () => {
  it('names a column with a replacement pattern in its label literally, with the positions put in', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    const cells: Record<string, unknown> = {};
    for (const column of COLUMNS) {
      cells[column.name] = childControl(column.name + '0', column.name + '0');
    }
    mountAdapter(TLTableView, {
      columns: COLUMNS, rows: [{ id: 'r0', index: 0, cells }], totalRowCount: 1, frozenColumnCount: 2,
    });
    // The server answers the templates: the keys alone carry no placeholders.
    const inner = globalThis.fetch;
    vi.stubGlobal('fetch', async (url: string, init?: RequestInit) => {
      if (/react-api\/i18n/.test(url)) {
        return new Response(JSON.stringify({ 'js.table.columnMovedLocked': '{0}, frozen, position {1} of {2}' }),
          { status: 200, headers: { 'Content-Type': 'application/json' } });
      }
      return inner(url, init);
    });
    await settle();
    const sort = document.querySelector<HTMLElement>('[data-col="c"] .tl-table__sort')!;
    fireEvent.keyDown(sort, { key: 'ArrowLeft', altKey: true });
    await settle();
    expect(screen.getByRole('status').textContent).toBe(LABEL + ', frozen, position 3 of 3');
  });

  it('counts only the columns the column selection lists: no detail column in front, no pinned one at the end', async () => {
    // As in the column selection ("position 2 of 34"): a column outside the arrangement is no position.
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    const columns = [
      { name: 'detail', label: '', width: 40, reorderable: false },
      { name: 'a', label: 'A', width: 100, sortable: true, reorderable: true },
      { name: 'b', label: 'B', width: 100, sortable: true, reorderable: true },
      { name: 'act', label: 'Act', width: 40, pinnedEnd: true, reorderable: false },
    ];
    const cells: Record<string, unknown> = {};
    for (const column of columns) {
      cells[column.name] = childControl(column.name + '0', column.name + '0');
    }
    const sent = mountAdapter(TLTableView, {
      columns, rows: [{ id: 'r0', index: 0, cells }], totalRowCount: 1, frozenColumnCount: 0,
    });
    const inner = globalThis.fetch;
    vi.stubGlobal('fetch', async (url: string, init?: RequestInit) => {
      if (/react-api\/i18n/.test(url)) {
        return new Response(JSON.stringify({
          'js.table.columnMovedLocked': '{0}, frozen, position {1} of {2}',
          'js.reorder.moved': '{0}, position {1} of {2}',
        }), { status: 200, headers: { 'Content-Type': 'application/json' } });
      }
      return inner(url, init);
    });
    await settle();
    const sort = (name: string) => document.querySelector<HTMLElement>(`[data-col="${name}"] .tl-table__sort`)!;

    fireEvent.keyDown(sort('a'), { key: 'ArrowLeft', altKey: true });
    await settle();
    expect(screen.getByRole('status').textContent).toBe('A, frozen, position 1 of 2');

    fireEvent.keyDown(sort('b'), { key: 'ArrowLeft', altKey: true });
    await settle();
    expect(screen.getByRole('status').textContent).toBe('B, position 1 of 2');
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'b', targetIndex: 1, locked: false, visible: true });
  });
});
