// The header of the table arranges the columns across the freeze line (UI-Durchgang 2, Z10): a drop and
// Alt + arrow send one `columnMove` that sets the position and the freezing together, the target block is
// highlighted while a drop would change the freezing, and a step is announced.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup, createEvent, fireEvent, screen } from '@testing-library/react';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './adapters/wire-test-support';

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

const COLUMNS: Array<Record<string, unknown>> = [
  { name: 'a', label: 'A', width: 100, sortable: true, reorderable: true },
  { name: 'b', label: 'B', width: 100, sortable: true, reorderable: true },
  { name: 'c', label: 'C', width: 100, sortable: true, reorderable: true },
  { name: 'd', label: 'D', width: 100, sortable: true, reorderable: true },
  { name: 'act', label: '', width: 40, sortable: false, pinnedEnd: true, reorderable: false },
];

function mountTable(state: Record<string, unknown> = {}, columns = COLUMNS) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  const cells: Record<string, unknown> = {};
  for (const column of columns) {
    cells[column.name as string] = childControl(column.name + '0', column.name + '0');
  }
  return mountAdapter(TLTableView, {
    columns,
    rows: [{ id: 'r0', index: 0, cells }],
    totalRowCount: 1,
    ...state,
  });
}

function head(name: string): HTMLElement {
  return document.querySelector<HTMLElement>(`[role="columnheader"][data-col="${name}"]`)!;
}

function sortButton(name: string): HTMLElement {
  return head(name).querySelector<HTMLElement>('.tl-table__sort')!;
}

const dataTransfer = { effectAllowed: '', dropEffect: '', types: [] as string[], setData() {} };

/** Picks the heading of a column up and hovers the given half of another one; the drop is up to the test. */
async function dragOver(from: string, onto: string, side: 'before' | 'after') {
  const over = head(onto);
  over.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;
  fireEvent.dragStart(head(from), { dataTransfer });
  await new Promise((resolve) => setTimeout(resolve, 5));
  const event = createEvent.dragOver(over, { dataTransfer });
  Object.defineProperty(event, 'clientX', { value: side === 'before' ? 2 : 98 });
  fireEvent(over, event);
}

async function drag(from: string, onto: string, side: 'before' | 'after') {
  await dragOver(from, onto, side);
  fireEvent.drop(head(onto), { dataTransfer });
  await settle();
}

function targets(): string[] {
  return Array.from(document.querySelectorAll<HTMLElement>('[role="columnheader"][data-tl-state~="drop-target"]'))
    .map((cell) => cell.dataset.col!);
}

function altKey(element: HTMLElement, key: 'ArrowLeft' | 'ArrowRight') {
  fireEvent.keyDown(element, { key, altKey: true });
}

describe('dragging a heading across the freeze line', () => {
  it('freezes a movable column dropped behind the last frozen one', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    await drag('c', 'b', 'after');
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 2, locked: true, visible: true });
  });

  it('releases the first frozen column dropped in front of the first movable one', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    await drag('b', 'c', 'before');
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'b', targetIndex: 1, locked: false, visible: true });
  });

  it('keeps the freezing for a drop within its own block', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    await drag('a', 'b', 'after');
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'a', targetIndex: 1, locked: true, visible: true });
    await drag('d', 'c', 'before');
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'd', targetIndex: 2, locked: false, visible: true });
  });

  it('sends nothing for a drop at its own place in its own block', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    await drag('d', 'c', 'after');
    await drag('a', 'b', 'before');
    expect(sent).not.toHaveBeenCalled();
  });

  it('highlights the target block while the drop would change the freezing, and only then', async () => {
    mountTable({ frozenColumnCount: 2 });
    await settle();
    await dragOver('c', 'b', 'after');
    expect(targets()).toEqual(['a', 'b']);
    fireEvent.dragEnd(head('c'), { dataTransfer });
    expect(targets()).toEqual([]);

    await dragOver('b', 'c', 'before');
    expect(targets()).toEqual(['c', 'd']);
    fireEvent.dragEnd(head('b'), { dataTransfer });

    await dragOver('a', 'b', 'after');
    expect(targets()).toEqual([]);
  });

  it('highlights the select-all cell with the frozen block it starts, only when the drop would freeze', async () => {
    mountTable({ frozenColumnCount: 2, selectionMode: 'multi' });
    await settle();
    const selectAll = () => document.querySelector<HTMLElement>('.tl-table__select')!;
    await dragOver('c', 'b', 'after');
    expect(selectAll().dataset.tlState).toContain('drop-target');
    fireEvent.dragEnd(head('c'), { dataTransfer });
    expect(selectAll().dataset.tlState).not.toContain('drop-target');

    await dragOver('b', 'c', 'before');
    expect(selectAll().dataset.tlState).not.toContain('drop-target');
  });

  it('has no frozen block to drop into without a frozen column', async () => {
    mountTable({ frozenColumnCount: 0 });
    await settle();
    await dragOver('d', 'a', 'before');
    expect(targets()).toEqual([]);
  });

  it('offers neither a drag nor a step to a pinned column', async () => {
    const sent = mountTable({ frozenColumnCount: 0 });
    await settle();
    expect(head('act').getAttribute('draggable')).not.toBe('true');
    expect(head('a').getAttribute('draggable')).toBe('true');
    altKey(head('act'), 'ArrowLeft');
    await settle();
    expect(sent).not.toHaveBeenCalled();
  });

  describe('with a leading column outside the arrangement', () => {
    const DETAIL = [
      { name: 'detail', label: 'Detail', width: 40, sortable: false, reorderable: false },
      { name: 'x', label: 'X', width: 100, sortable: true, reorderable: true },
      { name: 'y', label: 'Y', width: 100, sortable: true, reorderable: true },
    ];

    it('offers it neither a drag nor a step', async () => {
      const sent = mountTable({ frozenColumnCount: 0 }, DETAIL);
      await settle();
      expect(head('detail').getAttribute('draggable')).not.toBe('true');
      expect(head('x').getAttribute('draggable')).toBe('true');
      altKey(head('detail'), 'ArrowRight');
      await settle();
      expect(sent).not.toHaveBeenCalled();
    });

    it('freezes the first movable column that steps back, behind the detail column', async () => {
      const sent = mountTable({ frozenColumnCount: 0 }, DETAIL);
      await settle();
      altKey(sortButton('x'), 'ArrowLeft');
      await settle();
      expect(sent).toHaveBeenCalledWith('columnMove', { column: 'x', targetIndex: 1, locked: true, visible: true });
    });

    it('takes no step onto it from a frozen column next to it', async () => {
      const sent = mountTable({ frozenColumnCount: 2 }, DETAIL);
      await settle();
      altKey(sortButton('x'), 'ArrowLeft');
      await settle();
      expect(sent).not.toHaveBeenCalled();
    });
  });
});

describe('moving a heading with Alt + arrow', () => {
  it('freezes the first movable column that steps back, and says so', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    sortButton('c').focus();
    altKey(sortButton('c'), 'ArrowLeft');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 2, locked: true, visible: true });
    expect(screen.getByRole('status').textContent).toBe('js.table.columnMovedLocked');
    expect(document.activeElement).toBe(sortButton('c'));
  });

  it('releases the last frozen column that steps forward, and says so', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    altKey(sortButton('b'), 'ArrowRight');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'b', targetIndex: 1, locked: false, visible: true });
    expect(screen.getByRole('status').textContent).toBe('js.table.columnMovedMovable');
  });

  it('freezes the first column of a table without a frozen column', async () => {
    const sent = mountTable({ frozenColumnCount: 0 });
    await settle();
    altKey(sortButton('a'), 'ArrowLeft');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'a', targetIndex: 0, locked: true, visible: true });
  });

  it('keeps the freezing for a step within the block', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    altKey(sortButton('c'), 'ArrowRight');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 3, locked: false, visible: true });
    altKey(sortButton('b'), 'ArrowLeft');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'b', targetIndex: 0, locked: true, visible: true });
  });

  it('releases the last frozen column in front of a pinned one, without a place to go', async () => {
    const sent = mountTable({ frozenColumnCount: 4 });
    await settle();
    altKey(sortButton('d'), 'ArrowRight');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'd', targetIndex: 3, locked: false, visible: true });
  });

  it('releases the only frozen column at its own place', async () => {
    const sent = mountTable({ frozenColumnCount: 1 });
    await settle();
    altKey(sortButton('a'), 'ArrowRight');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'a', targetIndex: 0, locked: false, visible: true });
  });

  it('takes no step onto a pinned column', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    altKey(sortButton('d'), 'ArrowRight');
    await settle();
    expect(sent).not.toHaveBeenCalled();
  });

  it('leaves the name of the sort button as the heading says it', async () => {
    mountTable({ frozenColumnCount: 2 });
    await settle();
    expect(sortButton('c').hasAttribute('aria-label')).toBe(false);
    expect(sortButton('c').getAttribute('aria-describedby')).toBeTruthy();
  });

  it('moves a column whose only tab stop is the width handle', async () => {
    const columns = COLUMNS.map((c) => (c.name === 'c' ? { ...c, sortable: false } : c));
    const sent = mountTable({ frozenColumnCount: 2 }, columns);
    await settle();
    const handle = head('c').querySelector<HTMLElement>('[role="separator"]')!;
    altKey(handle, 'ArrowLeft');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 2, locked: true, visible: true });
    expect(handle.getAttribute('aria-describedby')).toBeTruthy();
    expect(handle.getAttribute('aria-label')).toBeTruthy();
  });

  it('still opens the heading menu with Shift + F10 and moves with Alt + arrow beside it', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    fireEvent.keyDown(sortButton('c'), { key: 'F10', shiftKey: true });
    await settle();
    expect(document.querySelector('[role="menu"]')).not.toBeNull();
    expect(sent).not.toHaveBeenCalled();
    fireEvent.keyDown(document.activeElement ?? document.body, { key: 'Escape' });
    altKey(sortButton('c'), 'ArrowRight');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 3, locked: false, visible: true });
  });

  it('does the same in a number column, whose heading stands in the order of every column: the filter button follows the sort button', async () => {
    const columns = COLUMNS.map((c) => (c.name === 'c' ? { ...c, align: 'end', filterable: true } : c));
    const sent = mountTable({ frozenColumnCount: 2 }, columns);
    await settle();
    const buttons = Array.from(head('c').querySelectorAll('button'));
    expect(buttons[0]).toBe(sortButton('c'));
    expect(buttons.length).toBeGreaterThan(1);
    altKey(buttons[1] as HTMLElement, 'ArrowLeft');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 2, locked: true, visible: true });
    altKey(sortButton('c'), 'ArrowRight');
    await settle();
    expect(sent).toHaveBeenCalledWith('columnMove', { column: 'c', targetIndex: 3, locked: false, visible: true });
  });

  it('sends no columnReorder any more', async () => {
    const sent = mountTable({ frozenColumnCount: 2 });
    await settle();
    await drag('d', 'a', 'before');
    altKey(sortButton('d'), 'ArrowLeft');
    await settle();
    expect(sent.mock.calls.some((call) => call[0] === 'columnReorder')).toBe(false);
    expect(sent).toHaveBeenCalled();
  });
});
