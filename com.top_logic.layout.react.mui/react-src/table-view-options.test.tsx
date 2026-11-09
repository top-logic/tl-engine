// The gear of the React table opens the popover "View" (UI-Durchgang 1, table): density, stripes and
// column lines take effect at once, the column selection and the reset are entries of it, and Escape
// gives the focus back to the gear.

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { screen, cleanup, fireEvent, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../com.top_logic.layout.react/react-src/controls/TLTableView';
import MuiButtonAdapter from './adapters/MuiButtonAdapter';
import MuiMenuTriggerAdapter from './adapters/MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './adapters/MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './adapters/MuiCheckboxAdapter';
import MuiTextInputAdapter from './adapters/MuiTextInputAdapter';
import MuiChipAdapter from './adapters/MuiChipAdapter';
import { mountAdapter, patchState, settle } from './adapters/wire-test-support';

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
  // jsdom lays nothing out; the roving over the entries of a menu skips entries without a client rect.
  vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
});

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** The texts are answered with their keys. */
const VIEW = 'js.table.view';
const DENSITY_DEFAULT = 'js.table.viewDensityDefault';
const DENSITY_NORMAL = 'js.table.viewDensityNormal';
const DENSITY_COMPACT = 'js.table.viewDensityCompact';
const STRIPES = 'js.table.viewStripes';
const COLUMN_LINES = 'js.table.viewColumnLines';
const CHOOSE_COLUMNS = 'js.table.viewChooseColumns';
const RESET = 'js.table.viewReset';

function mountTable(state: Record<string, unknown> = {}) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  return mountAdapter(TLTableView, {
    columns: [{ name: 'name', label: 'Name', width: 120, sortable: false, filterable: false }],
    rows: [],
    totalRowCount: 0,
    viewOptions: true,
    ...state,
  });
}

function gear(): HTMLElement {
  return screen.getByRole('button', { name: VIEW });
}

async function openView(): Promise<HTMLElement> {
  await userEvent.click(gear());
  await settle();
  return screen.getByRole('dialog', { name: VIEW });
}

describe('the gear of the table', () => {
  it('is there for every table that offers the view, with or without column selection', async () => {
    mountTable({ columnSelect: false });
    await settle();
    expect(gear()).toBeTruthy();
    cleanup();
    mountTable({ columnSelect: true });
    await settle();
    expect(gear()).toBeTruthy();
  });

  it('is not there for a table that does not offer the view', async () => {
    mountTable({ viewOptions: false });
    await settle();
    expect(screen.queryByRole('button', { name: VIEW })).toBeNull();
  });

  it('is there for a table that keeps no view but offers the column selection, and not for one that offers neither', async () => {
    mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    expect(gear()).toBeTruthy();
    cleanup();
    mountTable({ viewOptions: false, columnSelect: false });
    await settle();
    expect(screen.queryByRole('button', { name: VIEW })).toBeNull();
  });

  it('opens a dialog named "View" and says so on the gear', async () => {
    mountTable();
    await settle();
    expect(gear().getAttribute('aria-haspopup')).toBe('dialog');
    expect(gear().getAttribute('aria-expanded')).toBe('false');
    const dialog = await openView();
    expect(dialog).toBeTruthy();
    expect(gear().getAttribute('aria-expanded')).toBe('true');
  });

  it('opens the dialog without sending anything', async () => {
    const sent = mountTable({ columnSelect: true });
    await settle();
    await openView();
    expect(sent).not.toHaveBeenCalled();
  });
});

describe('the entries of a table that keeps no view', () => {
  it('offers no density and no switches, only the column selection and the reset', async () => {
    mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    await openView();
    expect(screen.queryByRole('radiogroup')).toBeNull();
    expect(screen.queryByRole('radio')).toBeNull();
    expect(screen.queryByRole('switch')).toBeNull();
    expect(screen.getByRole('button', { name: CHOOSE_COLUMNS })).toBeTruthy();
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
  });

  it('sends the command opening the column selection', async () => {
    const sent = mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: CHOOSE_COLUMNS }));
    await settle();
    expect(sent).toHaveBeenCalledWith('openColumnSelect', {});
  });
});

describe('the entries of the view', () => {
  it('shows density, stripes and column lines, and the current values', async () => {
    mountTable({ viewDensity: 'normal', striped: true, columnLines: false });
    await settle();
    await openView();
    expect(screen.getByRole('radiogroup')).toBeTruthy();
    expect(screen.getByRole('radio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('false');
    expect(screen.getByRole('radio', { name: DENSITY_NORMAL }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('radio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('false');
    expect((screen.getByRole('switch', { name: STRIPES }) as HTMLInputElement).checked).toBe(true);
    expect((screen.getByRole('switch', { name: COLUMN_LINES }) as HTMLInputElement).checked).toBe(false);
  });

  it('reads a missing density as "as everywhere"', async () => {
    mountTable({});
    await settle();
    await openView();
    expect(screen.getByRole('radio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the density at once, with the other two values as they are', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('radio', { name: DENSITY_COMPACT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    expect(screen.getByRole('radio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the empty density for "as everywhere"', async () => {
    const sent = mountTable({ viewDensity: 'compact', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('radio', { name: DENSITY_DEFAULT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the stripes at once', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('switch', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the column lines at once, keeping what the table shows', async () => {
    const sent = mountTable({ viewDensity: 'normal', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('switch', { name: COLUMN_LINES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'normal', striped: true, columnLines: true });
  });

  it('keeps a change that the server has not echoed yet when the next one is sent', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('radio', { name: DENSITY_COMPACT }));
    await userEvent.click(screen.getByRole('switch', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: true, columnLines: false });
  });

  it('follows the server when it echoes other values', async () => {
    mountTable();
    await settle();
    await openView();
    patchState({ viewDensity: 'compact', striped: true, columnLines: true });
    await settle();
    expect(screen.getByRole('radio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
    expect((screen.getByRole('switch', { name: STRIPES }) as HTMLInputElement).checked).toBe(true);
    expect((screen.getByRole('switch', { name: COLUMN_LINES }) as HTMLInputElement).checked).toBe(true);
  });
});

describe('the density segments by keyboard', () => {
  it('moves to the next segment with the arrow, sends it and keeps the focus on it', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    // The popover starts on the segment in force; the other segments are no tab stop.
    const asEverywhere = screen.getByRole('radio', { name: DENSITY_DEFAULT });
    expect(document.activeElement).toBe(asEverywhere);
    expect(screen.getByRole('radio', { name: DENSITY_NORMAL }).getAttribute('tabindex')).toBe('-1');
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'normal', striped: false, columnLines: false });
    expect(document.activeElement).toBe(screen.getByRole('radio', { name: DENSITY_NORMAL }));
  });

  it('wraps around at both ends', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.keyboard('{ArrowLeft}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    await userEvent.keyboard('{ArrowDown}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: false });
  });

  it('closes on Escape also from a segment', async () => {
    mountTable();
    await settle();
    await openView();
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('the column selection and the reset', () => {
  it('offers the column selection only where the table offers it', async () => {
    mountTable({ columnSelect: false });
    await settle();
    await openView();
    expect(screen.queryByRole('button', { name: CHOOSE_COLUMNS })).toBeNull();
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
  });

  it('opens the column selection and closes the view', async () => {
    const sent = mountTable({ columnSelect: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: CHOOSE_COLUMNS }));
    await settle();
    expect(sent).toHaveBeenCalledWith('openColumnSelect', {});
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('resets the view and stays open', async () => {
    const sent = mountTable({ viewDensity: 'compact', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
  });
});

describe('closing the view', () => {
  it('closes on Escape and gives the focus back to the gear', async () => {
    mountTable({ columnSelect: true });
    await settle();
    await openView();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes on a second press of the gear', async () => {
    mountTable();
    await settle();
    await openView();
    await userEvent.click(gear());
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('is operable with the keyboard alone: Enter on the gear opens it', async () => {
    mountTable();
    await settle();
    gear().focus();
    await userEvent.keyboard('{Enter}');
    await settle();
    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
  });
});

describe('the gear in a heading', () => {
  it('opens no column menu on a right-click inside the view popover', async () => {
    // A table ending in a pinned column carries the gear in that heading; the popover is portaled out of it.
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    mountAdapter(TLTableView, {
      columns: [
        { name: 'name', label: 'Name', width: 120, sortable: false, filterable: false },
        { name: 'pin', label: 'Pin', width: 60, sortable: false, filterable: false, pinnedEnd: true },
      ],
      rows: [],
      totalRowCount: 0,
      viewOptions: true,
    });
    await settle();
    await openView();
    fireEvent.contextMenu(screen.getByRole('radio', { name: DENSITY_NORMAL }));
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    // The heading itself still opens its menu.
    fireEvent.contextMenu(screen.getByRole('columnheader', { name: /Name/ }));
    await settle();
    expect(screen.getByRole('menu')).toBeTruthy();
  });

  it('opens no column menu on a right-click or Shift+F10 on the gear, even where the heading offers entries', async () => {
    // The pinned heading is groupable: its own menu has an entry, so only the gear's being no part of it keeps it shut.
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    mountAdapter(TLTableView, {
      columns: [
        { name: 'name', label: 'Name', width: 120, sortable: false, filterable: false },
        { name: 'pin', label: 'Pin', width: 60, sortable: false, filterable: false, pinnedEnd: true, groupable: true },
      ],
      rows: [],
      totalRowCount: 0,
      viewOptions: true,
    });
    await settle();
    fireEvent.contextMenu(gear());
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    gear().focus();
    await userEvent.keyboard('{Shift>}{F10}{/Shift}');
    await settle();
    expect(screen.queryByRole('menuitem', { name: 'js.table.groupBy' })).toBeNull();
    // The heading itself still opens its menu.
    fireEvent.contextMenu(screen.getByRole('columnheader', { name: /Pin/ }));
    await settle();
    expect(screen.getByRole('menuitem', { name: 'js.table.groupBy' })).toBeTruthy();
  });
});

describe('the column menu by keyboard', () => {
  // The first entry of the menu of an unfrozen column.
  const FIRST_ENTRY = 'js.table.freezeUpTo';

  function mountColumn(column: Record<string, unknown>) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false, filterable: false, ...column }],
      rows: [],
      totalRowCount: 0,
    });
  }

  /** The resizer of the first column, not the splitter of the frozen columns. */
  function resizer(): HTMLElement {
    return screen.getByRole('separator', { name: /columnWidth/ });
  }

  async function openAndClose(focused: HTMLElement, key: Parameters<typeof userEvent.keyboard>[0]) {
    focused.focus();
    await userEvent.keyboard(key);
    await settle();
    // The menu hangs off the heading cell, its first entry has the focus.
    const entry = screen.getByRole('menuitem', { name: FIRST_ENTRY });
    expect(document.activeElement).toBe(entry);
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    expect(document.activeElement).toBe(focused);
  }

  it('opens from the sort button with Shift+F10 and returns the focus on Escape', async () => {
    mountColumn({ sortable: true });
    await settle();
    await openAndClose(screen.getByRole('button', { name: /Name/ }), '{Shift>}{F10}{/Shift}');
  });

  it('opens from the sort button with the ContextMenu key', async () => {
    mountColumn({ sortable: true });
    await settle();
    await openAndClose(screen.getByRole('button', { name: /Name/ }), '{ContextMenu}');
  });

  it('opens from the funnel', async () => {
    mountColumn({ filterable: true });
    await settle();
    await openAndClose(screen.getByRole('button', { name: 'js.table.filter' }), '{ContextMenu}');
  });

  it('opens from the resizer, the only focus of a column without sort and filter', async () => {
    mountColumn({});
    await settle();
    await openAndClose(resizer(), '{Shift>}{F10}{/Shift}');
  });

  it('is anchored at the heading cell: its rectangle is what the menu is placed by', async () => {
    mountColumn({});
    await settle();
    const cell = screen.getByRole('columnheader', { name: /Name/ });
    const rect = vi.spyOn(cell, 'getBoundingClientRect').mockReturnValue(new DOMRect(40, 10, 120, 30));
    resizer().focus();
    // Nothing but the placement of the menu asks the cell for its rectangle after this; a point anchor never does.
    rect.mockClear();
    await userEvent.keyboard('{ContextMenu}');
    await settle();
    expect(screen.getByRole('menu')).toBeTruthy();
    expect(rect).toHaveBeenCalled();
  });

  it('keeps the sort on Enter: the command is sent and no menu opens', async () => {
    const sent = mountColumn({ sortable: true });
    await settle();
    screen.getByRole('button', { name: /Name/ }).focus();
    await userEvent.keyboard('{Enter}');
    await settle();
    expect(sent).toHaveBeenCalledWith('sort', { column: 'name', direction: 'asc', mode: 'replace' });
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('keeps the arrows of the resizer: the width changes and no menu opens', async () => {
    mountColumn({});
    await settle();
    const handle = resizer();
    const before = Number(handle.getAttribute('aria-valuenow'));
    handle.focus();
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(Number(resizer().getAttribute('aria-valuenow'))).toBeGreaterThan(before);
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('opens no menu for a heading that offers no entry, by right-click or by keyboard', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    mountAdapter(TLTableView, {
      columns: [
        { name: 'name', label: 'Name', width: 120, sortable: false, filterable: false },
        { name: 'pin', label: 'Pin', width: 60, sortable: true, filterable: false, pinnedEnd: true },
      ],
      rows: [],
      totalRowCount: 0,
    });
    await settle();
    const pin = screen.getByRole('columnheader', { name: /Pin/ });
    fireEvent.contextMenu(pin);
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    within(pin).getByRole('button', { name: /Pin/ }).focus();
    await userEvent.keyboard('{ContextMenu}');
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('keeps no request of a heading without entries: a later freezing or grouping opens no menu by itself', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    mountAdapter(TLTableView, {
      columns: [
        { name: 'name', label: 'Name', width: 120, sortable: false, filterable: false, groupable: true },
        { name: 'pin', label: 'Pin', width: 60, sortable: true, filterable: false, pinnedEnd: true },
      ],
      rows: [],
      totalRowCount: 0,
    });
    await settle();
    const pin = screen.getByRole('columnheader', { name: /Pin/ });
    fireEvent.contextMenu(pin);
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    // The column page freezes a column: the pinned heading would now offer "unfreeze all".
    patchState({ frozenColumnCount: 1 });
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();

    patchState({ frozenColumnCount: 0 });
    await settle();
    within(pin).getByRole('button', { name: /Pin/ }).focus();
    await userEvent.keyboard('{ContextMenu}');
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    // Grouping by another column: the pinned heading would now offer "ungroup".
    patchState({ grouping: 'name' });
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('opens the menu of a groupable pinned heading, which offers "group by", by right-click and by keyboard', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    mountAdapter(TLTableView, {
      columns: [
        { name: 'name', label: 'Name', width: 120, sortable: false, filterable: false },
        { name: 'pin', label: 'Pin', width: 60, sortable: true, filterable: false, pinnedEnd: true, groupable: true },
      ],
      rows: [],
      totalRowCount: 0,
    });
    await settle();
    const pin = screen.getByRole('columnheader', { name: /Pin/ });
    fireEvent.contextMenu(pin);
    await settle();
    expect(screen.getByRole('menuitem', { name: 'js.table.groupBy' })).toBeTruthy();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    within(pin).getByRole('button', { name: /Pin/ }).focus();
    await userEvent.keyboard('{ContextMenu}');
    await settle();
    expect(screen.getByRole('menuitem', { name: 'js.table.groupBy' })).toBeTruthy();
  });

  it('leaves other keys alone', async () => {
    mountColumn({});
    await settle();
    resizer().focus();
    await userEvent.keyboard('{ArrowRight}{F10}');
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
  });
});
