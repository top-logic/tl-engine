// The gear of the React table opens the popover "View" (UI-Durchgang 1, table): density, stripes and
// column lines take effect at once, the column selection and the reset are entries of it, and Escape
// gives the focus back to the gear.

import { describe, it, expect, vi, afterEach } from 'vitest';
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
import { childControl, mountAdapter, patchState, settle } from './adapters/wire-test-support';

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
// The entry "Columns · n of m" of the first page, and the page it opens.
const COLUMNS_ENTRY = 'js.table.viewColumns';
const COLUMNS = 'js.table.columns';
const BACK = 'js.table.viewBack';
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
    expect(screen.getByRole('button', { name: COLUMNS_ENTRY })).toBeTruthy();
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
  });

  it('sends the command opening the column selection', async () => {
    const sent = mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    await settle();
    expect(sent).toHaveBeenCalledWith('openColumns', {});
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
    expect(screen.queryByRole('button', { name: COLUMNS_ENTRY })).toBeNull();
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
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

describe('the page "Columns" of the view', () => {
  /** The content the server holds for the page: the column selection, a child control of the table. */
  const SELECTION = childControl('cs', 'Spaltenwahl');

  function mountColumns(state: Record<string, unknown> = {}) {
    return mountTable({ columnSelect: true, columnsShown: 3, columnsTotal: 5, ...state });
  }

  async function openColumnsPage(): Promise<void> {
    await userEvent.click(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    await settle();
    patchState({ columnSelection: SELECTION });
    await settle();
  }

  function page(): HTMLElement {
    return screen.getByRole('dialog', { name: COLUMNS });
  }

  it('is the entry of the first page, which is no dialog', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    // The text carries n and m; the arrow after it is a picture.
    const entry = screen.getByRole('button', { name: COLUMNS_ENTRY });
    expect(entry.querySelector('[aria-hidden="true"]')).not.toBeNull();
    expect(sent).not.toHaveBeenCalled();
  });

  it('opens in the popover of the view, asking the server for the selection, and shows no other dialog', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    await settle();

    expect(sent).toHaveBeenCalledWith('openColumns', {});
    // One dialog, the popover itself, named after the page; the gear still says it is open.
    expect(screen.getAllByRole('dialog')).toHaveLength(1);
    expect(page().classList).toContain('tl-table-view-options');
    expect(gear().getAttribute('aria-expanded')).toBe('true');
    expect(document.querySelector('[aria-modal="true"]')).toBeNull();
  });

  it('shows the selection the server holds, under a head with the way back, and hides the first page', async () => {
    mountColumns();
    await settle();
    await openView();
    await openColumnsPage();

    const dialog = page();
    const pages = dialog.querySelectorAll('.tl-table-view-options__page');
    expect(pages).toHaveLength(2);
    // The first page stays in the popover, hidden: only one of them shows at a time.
    expect((pages[0] as HTMLElement).hidden).toBe(true);
    expect((pages[1] as HTMLElement).hidden).toBe(false);
    const title = pages[1].querySelector('.tl-table-view-options__title')!;
    expect(title.textContent).toContain(COLUMNS);
    expect(title.querySelector('button.tl-table-view-options__back')).toBe(screen.getByRole('button', { name: BACK }));
    // The selection scrolls its own list: no second scroll area around it.
    expect(within(dialog).getByText('Spaltenwahl')).not.toBeNull();
    expect(dialog.querySelector('.tl-picker__scroll')).toBeNull();
    // Neither the density nor the reset belong to this page.
    expect(screen.queryByRole('radiogroup')).toBeNull();
    expect(screen.queryByRole('button', { name: RESET })).toBeNull();
  });

  it('puts the focus on the way back when the page opens', async () => {
    mountColumns();
    await settle();
    await openView();
    await openColumnsPage();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: BACK }));
  });

  it('goes back to the first page, tells the server, and focuses the entry "Columns"', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await openColumnsPage();

    await userEvent.click(screen.getByRole('button', { name: BACK }));
    await settle();

    expect(sent).toHaveBeenCalledWith('closeColumns', {});
    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
    expect(screen.queryByText('Spaltenwahl')).toBeNull();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
  });

  it('closes on Escape, tells the server that the page closes, and gives the focus back to the gear', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await openColumnsPage();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(sent).toHaveBeenCalledWith('closeColumns', {});
    expect(document.activeElement).toBe(gear());
  });

  it('closes on a press outside, telling the server', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await openColumnsPage();

    fireEvent.pointerDown(document.body);
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(sent).toHaveBeenCalledWith('closeColumns', {});
  });

  it('closes the first page without a command for the second', async () => {
    const sent = mountColumns();
    await settle();
    await openView();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(sent).not.toHaveBeenCalledWith('closeColumns', expect.anything());
  });

  it('opens on the first page again after it was closed on the second', async () => {
    mountColumns();
    await settle();
    await openView();
    await openColumnsPage();
    await userEvent.keyboard('{Escape}');
    await settle();
    patchState({ columnSelection: null });

    await openView();

    expect(screen.getByRole('radiogroup')).toBeTruthy();
    expect(screen.queryByText('Spaltenwahl')).toBeNull();
  });

  it('follows the server when it releases the selection: back on the first page', async () => {
    mountColumns();
    await settle();
    await openView();
    await openColumnsPage();

    patchState({ columnSelection: null });
    await settle();

    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
    expect(screen.getByRole('radiogroup')).toBeTruthy();
  });

  it('shows no selection the server has released: entry, back and entry again within one round trip', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    const first = childControl('cs1', 'Spaltenwahl eins');
    const second = childControl('cs2', 'Spaltenwahl zwei');
    await userEvent.click(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    patchState({ columnSelection: first });
    await settle();
    expect(screen.getByText('Spaltenwahl eins')).toBeTruthy();

    // Back and the entry again, before the server has answered the first of them: it still holds the first selection.
    await userEvent.click(screen.getByRole('button', { name: BACK }));
    await userEvent.click(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    await settle();
    expect(sent.mock.calls.map((call) => call[0]).filter((name) => name.endsWith('Columns')))
      .toEqual(['openColumns', 'closeColumns', 'openColumns']);
    // The released selection is not drawn again, and the page waits for the new one.
    expect(screen.queryByText('Spaltenwahl eins')).toBeNull();
    expect(screen.getByRole('button', { name: BACK })).toBeTruthy();

    // The server's answers: it releases the first, then builds the second. The release is no reason to go back.
    patchState({ columnSelection: null });
    await settle();
    expect(screen.getByRole('button', { name: BACK })).toBeTruthy();
    expect(screen.queryByRole('radiogroup')).toBeNull();
    patchState({ columnSelection: second });
    await settle();
    expect(screen.getByText('Spaltenwahl zwei')).toBeTruthy();
  });

  it('tells the server when the button goes away while the page is open', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await openColumnsPage();
    sent.mockClear();

    cleanup();
    await settle();

    expect(sent).toHaveBeenCalledWith('closeColumns', {});
  });

  it('tells the server nothing when the button goes away on the first page', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    sent.mockClear();

    cleanup();
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('keeps the page open between the click and the answer of the server', async () => {
    mountColumns();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('button', { name: COLUMNS_ENTRY }));
    await settle();

    // The selection has not arrived: the head is there, the popover has not gone back.
    expect(page()).toBeTruthy();
    expect(screen.getByRole('button', { name: BACK })).toBeTruthy();
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
});
