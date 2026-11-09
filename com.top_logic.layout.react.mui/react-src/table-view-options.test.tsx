// The gear of the React table opens the popover "View" (UI-Durchgang 1, table): density, stripes and
// column lines - the view of the person, the same for every table - take effect at once, the column selection is
// an entry of it, and Escape gives the focus back to the gear. Its first page is a menu (#30102): every entry is a
// menu item, a click on the whole row acts, the arrows, Home and End move over the entries. "Reset columns" lies on
// the page of the columns, or, where the table has none, as the last entry of the first page; it closes the popover.

import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { screen, cleanup, fireEvent, within, act } from '@testing-library/react';
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
import { CONTROL_ID, childControl, mountAdapter, patchState, settle, resetTableView } from './adapters/wire-test-support';

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
  // jsdom lays nothing out; the roving over the entries of a menu skips entries without a client rect.
  vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
});

afterEach(() => {
  cleanup();
  resetTableView();
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
// The entry "Columns" with its value "n of m" and a chevron on the first page, and the page it opens. The name of the entry is
// the label and the value together; a pattern, because the value is part of it.
const COLUMNS_ENTRY = /^js\.table\.viewColumns\b/;
const COLUMNS_VALUE = 'js.table.viewColumnsCount';
const COLUMNS = 'js.table.columns';
const BACK = 'js.table.viewBack';
const RESET = 'js.table.columnsReset';

/**
 * Mounts a table. The view of the person (density, stripes, column lines) comes from the store of the bridge, here
 * `view`; the state key `viewOptions` says that the table keeps its state, so that its columns can be reset.
 */
function mountTable(state: Record<string, unknown> = {}, view: Partial<TableViewPreferences> = {}) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  setTableViewPreferences({ density: '', striped: false, columnLines: false, ...view });
  return mountAdapter(TLTableView, {
    columns: [{ name: 'name', label: 'Name', width: 120, sortable: false, filterable: false }],
    rows: [],
    totalRowCount: 0,
    viewOptions: true,
    ...state,
  });
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
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
  it('is there for every table, with or without column selection', async () => {
    mountTable({ columnSelect: false });
    await settle();
    expect(gear()).toBeTruthy();
    cleanup();
    mountTable({ columnSelect: true });
    await settle();
    expect(gear()).toBeTruthy();
  });

  it('is there for a table that keeps no state and offers no column selection', async () => {
    mountTable({ viewOptions: false, columnSelect: false });
    await settle();
    expect(gear()).toBeTruthy();
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

describe('the entries of a table that keeps no state', () => {
  it('offers density and switches like every table, and the column selection but no reset', async () => {
    mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    await openView();
    expect(screen.getByRole('group', { name: 'js.table.viewDensity' })).toBeTruthy();
    expect(screen.getAllByRole('menuitemradio')).toHaveLength(3);
    expect(screen.getAllByRole('menuitemcheckbox')).toHaveLength(2);
    expect(screen.getByRole('menuitem', { name: COLUMNS_ENTRY })).toBeTruthy();
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
  });

  it('sends the command opening the column selection', async () => {
    const sent = mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
    await settle();
    expect(sent).toHaveBeenCalledWith('openColumns', {});
  });

  it('draws the entry as a way to the next page: the label, the value n of m, and a hidden chevron at the end', async () => {
    mountTable({ viewOptions: false, columnSelect: true, columnsShown: 32, columnsTotal: 58 });
    await settle();
    await openView();
    const entry = screen.getByRole('menuitem', { name: COLUMNS_ENTRY });
    expect(entry.classList.contains('tl-menu__item--nav')).toBe(true);
    // The label, the value and the chevron, in this order; the value is text, the chevron is no text.
    const [label, value, chevron] = Array.from(entry.children);
    expect(label.className).toContain('tl-menu__label');
    expect(label.textContent).toBe('js.table.viewColumns');
    expect(value.className).toContain('tl-menu__value');
    expect(value.textContent).toBe(COLUMNS_VALUE);
    expect(chevron.tagName).toBe('I');
    expect(chevron.classList.contains('tl-menu__chevron')).toBe(true);
    expect(chevron.classList.contains('tl-icon-sm')).toBe(true);
    expect(chevron.classList.contains('fa-chevron-right')).toBe(true);
    expect(chevron.getAttribute('aria-hidden')).toBe('true');
    expect(entry.children.length).toBe(3);
    // The accessible name is the label and the value, the chevron is no part of it.
    expect(entry.textContent).not.toContain('\u203A');
    expect(screen.getByRole('menuitem', { name: /^js\.table\.viewColumns js\.table\.viewColumnsCount$/ })).toBe(entry);
  });
});

describe('the entries of the view', () => {
  it('shows density, stripes and column lines, and the current values', async () => {
    mountTable({}, { density: 'normal', striped: true, columnLines: false });
    await settle();
    await openView();
    expect(screen.getByRole('group', { name: 'js.table.viewDensity' })).toBeTruthy();
    expect(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('false');
    expect(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('false');
    expect(screen.getByRole('menuitemcheckbox', { name: STRIPES }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }).getAttribute('aria-checked')).toBe('false');
  });

  it('reads a missing density as "as everywhere"', async () => {
    mountTable();
    await settle();
    await openView();
    expect(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the density at once, with the other two values as they are', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    expect(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the empty density for "as everywhere"', async () => {
    const sent = mountTable({}, { density: 'compact', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the stripes at once', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the column lines at once, keeping what the table shows', async () => {
    const sent = mountTable({}, { density: 'normal', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'normal', striped: true, columnLines: true });
  });

  it('keeps a change that was made a moment ago when the next one is sent', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: true, columnLines: false });
  });

  it('shows at once in the table what is chosen, without an answer of the server', async () => {
    mountTable();
    await settle();
    await openView();
    expect(root().hasAttribute('data-tl-striped')).toBe(false);
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: STRIPES }));
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
    expect(root().hasAttribute('data-tl-striped')).toBe(true);
    expect(root().getAttribute('data-tl-density')).toBe('compact');
  });

  it('stores the choice before it sends it', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    sent.mockImplementation(() => {
      expect(root().hasAttribute('data-tl-column-lines')).toBe(true);
    });
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }));
    await settle();
    expect(sent).toHaveBeenCalledTimes(1);
  });

  it('follows the view of the person when another table of the page changes it', async () => {
    mountTable();
    await settle();
    await openView();
    act(() => setTableViewPreferences({ density: 'compact', striped: true, columnLines: true }));
    await settle();
    expect(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemcheckbox', { name: STRIPES }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }).getAttribute('aria-checked')).toBe('true');
  });

  it('offers the three entries also where the table keeps no state', async () => {
    mountTable({ viewOptions: false });
    await settle();
    await openView();
    expect(screen.getAllByRole('menuitemradio')).toHaveLength(3);
    expect(screen.getAllByRole('menuitemcheckbox')).toHaveLength(2);
  });
});

describe('the density segments by keyboard', () => {
  it('moves to the next segment with the arrow, sends it and keeps the focus on it', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    // The popover starts on the segment in force; the other segments are no tab stop.
    const asEverywhere = screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT });
    expect(document.activeElement).toBe(asEverywhere);
    expect(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }).getAttribute('tabindex')).toBe('-1');
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'normal', striped: false, columnLines: false });
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }));
  });

  it('wraps around at both ends', async () => {
    const sent = mountTable();
    await settle();
    await openView();
    await userEvent.keyboard('{ArrowLeft}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: false });
  });

  it('keeps one tab stop for the three segments: the one in force, which follows the change', async () => {
    mountTable({}, { density: 'normal' });
    await settle();
    await openView();
    const stops = () => [DENSITY_DEFAULT, DENSITY_NORMAL, DENSITY_COMPACT]
      .map((name) => screen.getByRole('menuitemradio', { name }).getAttribute('tabindex'));
    expect(stops()).toEqual(['-1', '0', '-1']);
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(stops()).toEqual(['-1', '-1', '0']);
  });

  it('closes on Escape also from a segment', async () => {
    mountTable();
    await settle();
    await openView();
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('the first page as a menu', () => {
  /** The roving stops in order: the density is one of them, whichever segment is in force. */
  const STOPS: Array<string | RegExp> = [DENSITY_DEFAULT, STRIPES, COLUMN_LINES, COLUMNS_ENTRY];

  function stop(name: string | RegExp): HTMLElement {
    return screen.getByRole(
      name === STRIPES || name === COLUMN_LINES ? 'menuitemcheckbox' :
        name === COLUMNS_ENTRY || name === RESET ? 'menuitem' : 'menuitemradio',
      { name });
  }

  function mountMenu(state: Record<string, unknown> = {}, view: Partial<TableViewPreferences> = {}) {
    return mountTable({ columnSelect: true, columnsShown: 3, columnsTotal: 5, ...state }, view);
  }

  it('is a menu named "View", whose entries are menu items', async () => {
    mountMenu();
    await settle();
    await openView();
    const menu = screen.getByRole('menu', { name: VIEW });
    expect(within(menu).getAllByRole('menuitemradio')).toHaveLength(3);
    expect(within(menu).getAllByRole('menuitemcheckbox')).toHaveLength(2);
    // The entry of the columns is the only plain item: the reset lies on the page of the columns.
    expect(within(menu).getAllByRole('menuitem')).toHaveLength(1);
    // The density is a group of its own, named by the quiet head.
    expect(within(menu).getByRole('group', { name: 'js.table.viewDensity' })).toBeTruthy();
  });

  it('draws the rows as the spec has them: the whole row a menu item, the switch a drawing without input', async () => {
    mountMenu({}, { striped: true });
    await settle();
    await openView();
    const row = screen.getByRole('menuitemcheckbox', { name: STRIPES });
    expect(row.tagName).toBe('BUTTON');
    expect(row.classList).toContain('tl-menu__item--check');
    const mark = row.querySelector('.tl-checkbox.tl-checkbox--switch')!;
    expect(mark.getAttribute('aria-hidden')).toBe('true');
    // The label stands left of the drawn switch.
    expect(row.querySelector('.tl-menu__label')!.nextElementSibling).toBe(mark);
    expect(screen.getByRole('menu').querySelector('input')).toBeNull();
    expect(screen.getByRole('menu').querySelector('.tl-choice')).toBeNull();
  });

  it('toggles the stripes with a click on the row, not only on the switch or the label', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    const row = screen.getByRole('menuitemcheckbox', { name: STRIPES });

    // A click on the row itself, where there is neither switch nor label.
    await userEvent.click(row);
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
    expect(row.getAttribute('aria-checked')).toBe('true');

    // The drawn switch and the label are part of the row: a click on them is a click on the row.
    await userEvent.click(row.querySelector('.tl-checkbox')!);
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: false });
    await userEvent.click(row.querySelector('.tl-menu__label')!);
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('toggles a row with Space and Enter', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    stop(COLUMN_LINES).focus();
    await userEvent.keyboard(' ');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: true });
    await userEvent.keyboard('{Enter}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: false });
  });

  it('starts on the segment in force', async () => {
    mountMenu({}, { density: 'compact' });
    await settle();
    await openView();
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
  });

  it('starts on the segment in force also where the table keeps no state', async () => {
    mountMenu({ viewOptions: false });
    await settle();
    await openView();
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }));
  });

  it('moves down over all entries, the three segments being one stop, and wraps around', async () => {
    mountMenu();
    await settle();
    await openView();
    const visited: Element[] = [document.activeElement!];
    for (let i = 0; i < STOPS.length; i++) {
      await userEvent.keyboard('{ArrowDown}');
      visited.push(document.activeElement!);
    }
    expect(visited).toEqual([...STOPS, STOPS[0]].map(stop));
  });

  it('moves up over all entries and wraps around to the last one', async () => {
    mountMenu();
    await settle();
    await openView();
    const visited: Element[] = [document.activeElement!];
    for (let i = 0; i < STOPS.length; i++) {
      await userEvent.keyboard('{ArrowUp}');
      visited.push(document.activeElement!);
    }
    expect(visited).toEqual([STOPS[0], ...[...STOPS].reverse()].map(stop));
  });

  it('jumps with Home and End', async () => {
    mountMenu();
    await settle();
    await openView();
    await userEvent.keyboard('{End}');
    expect(document.activeElement).toBe(stop(COLUMNS_ENTRY));
    await userEvent.keyboard('{Home}');
    expect(document.activeElement).toBe(stop(DENSITY_DEFAULT));
  });

  it('moves from the segment in force to the stripes, whichever segment that is', async () => {
    mountMenu({}, { density: 'compact' });
    await settle();
    await openView();
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(stop(STRIPES));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
  });

  it('changes the density with Left and Right without moving the stop, and the arrows go on from there', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'normal', striped: false, columnLines: false });
    // The stop is still the density: the way down leads to the stripes, up to the entry of the columns.
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(stop(STRIPES));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(stop(COLUMNS_ENTRY));
  });

  it('leaves Left and Right on the other entries alone', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    stop(STRIPES).focus();
    await userEvent.keyboard('{ArrowRight}{ArrowLeft}');
    await settle();
    expect(sent).not.toHaveBeenCalled();
    expect(document.activeElement).toBe(stop(STRIPES));
  });

  it('keeps one tab stop, the entry last focused', async () => {
    mountMenu();
    await settle();
    await openView();
    stop(COLUMN_LINES).focus();
    await settle();
    const tabStops = Array.from(screen.getByRole('menu').querySelectorAll('[tabindex="0"]'));
    expect(tabStops).toEqual([stop(COLUMN_LINES)]);
  });
});

describe('the column selection and the reset', () => {
  it('offers the column selection only where the table offers it', async () => {
    mountTable({ columnSelect: false });
    await settle();
    await openView();
    expect(screen.queryByRole('menuitem', { name: COLUMNS_ENTRY })).toBeNull();
  });

  it('keeps the reset as the last entry of the first page where there is no page of the columns', async () => {
    mountTable({ columnSelect: false, viewOptions: true });
    await settle();
    await openView();
    const reset = screen.getByRole('menuitem', { name: RESET });
    const items = Array.from(screen.getByRole('menu').querySelectorAll('[role^="menuitem"]'));
    expect(items[items.length - 1]).toBe(reset);
    await userEvent.keyboard('{End}');
    expect(document.activeElement).toBe(reset);
  });

  it('offers no reset where the table keeps no state', async () => {
    mountTable({ columnSelect: false, viewOptions: false });
    await settle();
    await openView();
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
    expect(screen.queryByRole('button', { name: RESET })).toBeNull();
  });

  it('does not offer the reset on the first page where the page of the columns holds it', async () => {
    mountTable({ columnSelect: true, viewOptions: true });
    await settle();
    await openView();
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
  });

  it('resets the columns and closes, the focus back on the gear', async () => {
    const sent = mountTable({ columnSelect: false }, { density: 'compact', striped: true });
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitem', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('false');
    // The view of the person is not part of the table's arrangement: the reset leaves it alone.
    expect(root().getAttribute('data-tl-density')).toBe('compact');
    expect(root().hasAttribute('data-tl-striped')).toBe(true);
  });

  it('resets with the keyboard as well', async () => {
    const sent = mountTable({ columnSelect: false });
    await settle();
    await openView();
    await userEvent.keyboard('{End}{Enter}');
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });
});

describe('the page "Columns" of the view', () => {
  /** The content the server holds for the page: the column selection, a child control of the table. */
  const SELECTION = childControl('cs', 'Spaltenwahl');

  function mountColumns(state: Record<string, unknown> = {}) {
    return mountTable({ columnSelect: true, columnsShown: 3, columnsTotal: 5, ...state });
  }

  async function openColumnsPage(): Promise<void> {
    await userEvent.click(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
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
    const entry = screen.getByRole('menuitem', { name: COLUMNS_ENTRY });
    expect(entry.querySelector('[aria-hidden="true"]')).not.toBeNull();
    expect(sent).not.toHaveBeenCalled();
  });

  it('opens in the popover of the view, asking the server for the selection, and shows no other dialog', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await userEvent.click(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
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
    // The density does not belong to this page; the reset lies below the selection, no menu item.
    expect(screen.queryByRole('menuitemradio')).toBeNull();
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
    const reset = within(dialog).getByRole('button', { name: RESET });
    expect(reset.getAttribute('role')).toBeNull();
    expect(reset.classList).toContain('tl-menu__item');
    expect(reset.compareDocumentPosition(within(dialog).getByText('Spaltenwahl')) & Node.DOCUMENT_POSITION_PRECEDING).toBeTruthy();
  });

  it('resets the columns from the page, sends the command and closes the popover', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    await openColumnsPage();

    await userEvent.click(screen.getByRole('button', { name: RESET }));
    await settle();

    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(sent).toHaveBeenCalledWith('closeColumns', {});
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });

  it('offers no reset on the page where the table keeps no state', async () => {
    mountColumns({ viewOptions: false });
    await settle();
    await openView();
    await openColumnsPage();
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
    expect(document.activeElement).toBe(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
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

    expect(screen.getByRole('menu')).toBeTruthy();
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
    expect(screen.getByRole('menu', { name: VIEW })).toBeTruthy();
  });

  it('shows no selection the server has released: entry, back and entry again within one round trip', async () => {
    const sent = mountColumns();
    await settle();
    await openView();
    const first = childControl('cs1', 'Spaltenwahl eins');
    const second = childControl('cs2', 'Spaltenwahl zwei');
    await userEvent.click(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
    patchState({ columnSelection: first });
    await settle();
    expect(screen.getByText('Spaltenwahl eins')).toBeTruthy();

    // Back and the entry again, before the server has answered the first of them: it still holds the first selection.
    await userEvent.click(screen.getByRole('button', { name: BACK }));
    await userEvent.click(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
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
    expect(screen.queryByRole('menuitemradio')).toBeNull();
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
    await userEvent.click(screen.getByRole('menuitem', { name: COLUMNS_ENTRY }));
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
    fireEvent.contextMenu(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }));
    await settle();
    // The first page of the view is itself a menu: no second one opens.
    expect(screen.getAllByRole('menu')).toHaveLength(1);
    // The heading itself still opens its menu.
    fireEvent.contextMenu(screen.getByRole('columnheader', { name: /Name/ }));
    await settle();
    expect(screen.getAllByRole('menu')).toHaveLength(2);
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
