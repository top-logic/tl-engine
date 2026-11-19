// The gear of the React table opens a popover of two pages (UI-Durchgang 2, #30234): first "Columns", the column
// selection the server holds, with the row "View" at its foot; behind it the page "View", a menu (#30102): density,
// stripes and column lines take effect at once, the reset closes the popover. A table without column selection opens
// the page "View" alone. Escape gives the focus back to the gear.

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
import MuiSkeletonAdapter from './adapters/MuiSkeletonAdapter';
import { childControl, mountAdapter, patchState, settle } from './adapters/wire-test-support';

// The test registry has no parts of its own: the table looks up the ones it draws with.
replacePart('Button', MuiButtonAdapter);
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);
replacePart('Skeleton', MuiSkeletonAdapter);

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
// The page "Columns" and the name of the gear opening it; the row leading from it to the page "View" is named VIEW.
const COLUMNS = 'js.table.columns';
const BACK = 'js.table.viewBack';
const RESET = 'js.table.viewReset';

const CMD_OPEN_COLUMNS = 'openColumns';
const CMD_CLOSE_COLUMNS = 'closeColumns';

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

/** The gear, whatever it is named: the trigger announcing the dialog. */
function queryGear(): HTMLElement | null {
  return document.querySelector<HTMLElement>('.tl-table__columns [aria-haspopup="dialog"]');
}

function gear(): HTMLElement {
  return queryGear()!;
}

/** Opens the popover with the gear and returns it. */
async function openPopover(): Promise<HTMLElement> {
  await userEvent.click(gear());
  await settle();
  return screen.getByRole('dialog');
}

/** The row "View" at the foot of the page "Columns". */
function viewRow(): HTMLElement {
  return screen.getByRole('button', { name: VIEW });
}

/** Opens the popover of a table offering the column selection and goes on to its page "View". */
async function openView(): Promise<HTMLElement> {
  await openPopover();
  await userEvent.click(viewRow());
  await settle();
  return screen.getByRole('dialog', { name: VIEW });
}

/** The names of the commands sent for the column selection, in their order. */
function columnCommands(sent: ReturnType<typeof mountTable>): string[] {
  return sent.mock.calls.map((call) => call[0] as string).filter((name) => name.endsWith('Columns'));
}

describe('the gear of the table', () => {
  it('is there for every table that offers the view, with or without column selection', async () => {
    mountTable({ columnSelect: false });
    await settle();
    expect(queryGear()).not.toBeNull();
    cleanup();
    mountTable({ columnSelect: true });
    await settle();
    expect(queryGear()).not.toBeNull();
  });

  it('is not there for a table that does not offer the view', async () => {
    mountTable({ viewOptions: false });
    await settle();
    expect(queryGear()).toBeNull();
  });

  it('is there for a table that keeps no view but offers the column selection, and not for one that offers neither', async () => {
    mountTable({ viewOptions: false, columnSelect: true });
    await settle();
    expect(queryGear()).not.toBeNull();
    cleanup();
    mountTable({ viewOptions: false, columnSelect: false });
    await settle();
    expect(queryGear()).toBeNull();
  });

  it('is named after the page it opens: "Columns" where the table offers the column selection, else "View"', async () => {
    mountTable({ columnSelect: true });
    await settle();
    expect(screen.getByRole('button', { name: COLUMNS })).toBe(gear());
    cleanup();
    mountTable({ columnSelect: false });
    await settle();
    expect(screen.getByRole('button', { name: VIEW })).toBe(gear());
  });

  it('opens a dialog and says so on the gear', async () => {
    mountTable();
    await settle();
    expect(gear().getAttribute('aria-haspopup')).toBe('dialog');
    expect(gear().getAttribute('aria-expanded')).toBe('false');
    const dialog = await openPopover();
    expect(dialog).toBeTruthy();
    expect(gear().getAttribute('aria-expanded')).toBe('true');
  });

  it('opens the dialog without sending anything where the table offers no column selection', async () => {
    const sent = mountTable({ columnSelect: false });
    await settle();
    await openPopover();
    expect(sent).not.toHaveBeenCalled();
  });

  it('asks the server for the column selection once when it opens where the table offers it', async () => {
    const sent = mountTable({ columnSelect: true });
    await settle();
    await openPopover();
    expect(sent).toHaveBeenCalledWith(CMD_OPEN_COLUMNS, {});
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS]);
  });
});

describe('the popover of a table that offers no column selection', () => {
  it('shows the page "View" alone: no way back, no row "View", and the focus on the density in force', async () => {
    const sent = mountTable({ columnSelect: false, viewDensity: 'normal' });
    await settle();
    const dialog = await openPopover();
    expect(screen.getByRole('dialog', { name: VIEW })).toBe(dialog);
    expect(dialog.querySelectorAll('.tl-table-view-options__page')).toHaveLength(1);
    expect(dialog.querySelector('.tl-table-view-options__back')).toBeNull();
    expect(dialog.querySelector('.tl-menu__item--nav')).toBeNull();
    expect(screen.getByRole('menu', { name: VIEW })).toBeTruthy();
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }));
    expect(sent).not.toHaveBeenCalledWith(CMD_OPEN_COLUMNS, expect.anything());
  });

  it('closes without a command for the column selection', async () => {
    const sent = mountTable({ columnSelect: false });
    await settle();
    await openPopover();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(sent).not.toHaveBeenCalledWith(CMD_CLOSE_COLUMNS, expect.anything());
  });
});

describe('the popover of a table that keeps no view', () => {
  function mountPlain() {
    return mountTable({ viewOptions: false, columnSelect: true });
  }

  it('has no page "View": the page "Columns" carries the reset at its foot, a plain button after a divider', async () => {
    mountPlain();
    await settle();
    const dialog = await openPopover();
    expect(screen.getByRole('dialog', { name: COLUMNS })).toBe(dialog);
    expect(dialog.querySelectorAll('.tl-table-view-options__page')).toHaveLength(1);
    expect(dialog.querySelector('[role="menu"]')).toBeNull();
    expect(dialog.querySelector('[role="menuitemradio"]')).toBeNull();
    expect(dialog.querySelector('.tl-menu__item--nav')).toBeNull();
    const reset = screen.getByRole('button', { name: RESET });
    expect(reset.hasAttribute('role')).toBe(false);
    expect(reset.previousElementSibling!.tagName).toBe('HR');
    expect(reset.parentElement!.lastElementChild).toBe(reset);
  });

  it('starts the focus on the reset while the selection has not arrived', async () => {
    mountPlain();
    await settle();
    await openPopover();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: RESET }));
  });

  it('resets and closes, telling the server that the column selection closes, the focus back on the gear', async () => {
    const sent = mountPlain();
    await settle();
    await openPopover();
    patchState({ columnSelection: childControl('cs', 'Spaltenwahl') });
    await settle();
    await userEvent.click(screen.getByRole('button', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });
});

describe('the entries of the view', () => {
  it('shows density, stripes and column lines, and the current values', async () => {
    mountTable({ viewDensity: 'normal', striped: true, columnLines: false });
    await settle();
    await openPopover();
    expect(screen.getByRole('group', { name: 'js.table.viewDensity' })).toBeTruthy();
    expect(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('false');
    expect(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('false');
    expect(screen.getByRole('menuitemcheckbox', { name: STRIPES }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }).getAttribute('aria-checked')).toBe('false');
  });

  it('reads a missing density as "as everywhere"', async () => {
    mountTable({});
    await settle();
    await openPopover();
    expect(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the density at once, with the other two values as they are', async () => {
    const sent = mountTable();
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    expect(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
  });

  it('sends the empty density for "as everywhere"', async () => {
    const sent = mountTable({ viewDensity: 'compact', striped: true });
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_DEFAULT }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the stripes at once', async () => {
    const sent = mountTable();
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: '', striped: true, columnLines: false });
  });

  it('sends the column lines at once, keeping what the table shows', async () => {
    const sent = mountTable({ viewDensity: 'normal', striped: true });
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }));
    await settle();
    expect(sent).toHaveBeenCalledWith('setViewOptions', { density: 'normal', striped: true, columnLines: true });
  });

  it('keeps a change that the server has not echoed yet when the next one is sent', async () => {
    const sent = mountTable();
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
    await userEvent.click(screen.getByRole('menuitemcheckbox', { name: STRIPES }));
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: true, columnLines: false });
  });

  it('follows the server when it echoes other values', async () => {
    mountTable();
    await settle();
    await openPopover();
    patchState({ viewDensity: 'compact', striped: true, columnLines: true });
    await settle();
    expect(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemcheckbox', { name: STRIPES }).getAttribute('aria-checked')).toBe('true');
    expect(screen.getByRole('menuitemcheckbox', { name: COLUMN_LINES }).getAttribute('aria-checked')).toBe('true');
  });
});

describe('the density segments by keyboard', () => {
  it('moves to the next segment with the arrow, sends it and keeps the focus on it', async () => {
    const sent = mountTable();
    await settle();
    await openPopover();
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
    await openPopover();
    await userEvent.keyboard('{ArrowLeft}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'compact', striped: false, columnLines: false });
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: '', striped: false, columnLines: false });
  });

  it('keeps one tab stop for the three segments: the one in force, which follows the change', async () => {
    mountTable({ viewDensity: 'normal' });
    await settle();
    await openPopover();
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
    await openPopover();
    await userEvent.keyboard('{Escape}');
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('the page "View" behind the row "View" as a menu', () => {
  /** The roving stops in order: the density is one of them, whichever segment is in force. */
  const STOPS: string[] = [DENSITY_DEFAULT, STRIPES, COLUMN_LINES, RESET];

  function stop(name: string): HTMLElement {
    return screen.getByRole(
      name === STRIPES || name === COLUMN_LINES ? 'menuitemcheckbox' : name === RESET ? 'menuitem' : 'menuitemradio',
      { name });
  }

  function mountMenu(state: Record<string, unknown> = {}) {
    return mountTable({ columnSelect: true, ...state });
  }

  it('is a menu named "View", whose entries are menu items; the head with the way back stands outside of it', async () => {
    mountMenu();
    await settle();
    await openView();
    const menu = screen.getByRole('menu', { name: VIEW });
    expect(within(menu).getAllByRole('menuitemradio')).toHaveLength(3);
    expect(within(menu).getAllByRole('menuitemcheckbox')).toHaveLength(2);
    expect(within(menu).getAllByRole('menuitem')).toHaveLength(1);
    // The density is a group of its own, named by the quiet head.
    expect(within(menu).getByRole('group', { name: 'js.table.viewDensity' })).toBeTruthy();
    expect(menu.classList).toContain('tl-table-view-options__menu');
    // The menu is named by the text of the title alone; the way back is no part of the menu nor of its name.
    const back = screen.getByRole('button', { name: BACK });
    expect(menu.contains(back)).toBe(false);
    const name = document.getElementById(menu.getAttribute('aria-labelledby')!)!;
    expect(name.textContent).toBe(VIEW);
    expect(name.querySelector('button')).toBeNull();
    expect(name.closest('.tl-table-view-options__title')).toBe(back.closest('.tl-table-view-options__title'));
  });

  it('draws the rows as the spec has them: the whole row a menu item, the switch a drawing without input', async () => {
    mountMenu({ striped: true });
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

  it('starts on the way back, and Tab leads from it to the density in force', async () => {
    mountMenu({ viewDensity: 'compact' });
    await settle();
    await openView();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: BACK }));
    await userEvent.tab();
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
  });

  it('moves down over all entries, the three segments being one stop, and wraps around', async () => {
    mountMenu();
    await settle();
    await openView();
    stop(DENSITY_DEFAULT).focus();
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
    stop(DENSITY_DEFAULT).focus();
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
    stop(STRIPES).focus();
    await userEvent.keyboard('{End}');
    expect(document.activeElement).toBe(stop(RESET));
    await userEvent.keyboard('{Home}');
    expect(document.activeElement).toBe(stop(DENSITY_DEFAULT));
  });

  it('moves from the segment in force to the stripes, whichever segment that is', async () => {
    mountMenu({ viewDensity: 'compact' });
    await settle();
    await openView();
    // From the reset, the last entry, to the first: the density, on the segment in force.
    stop(RESET).focus();
    await userEvent.keyboard('{Home}');
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(stop(STRIPES));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_COMPACT }));
  });

  it('changes the density with Left and Right without moving the stop, and the arrows go on from there', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    stop(DENSITY_DEFAULT).focus();
    await userEvent.keyboard('{ArrowRight}');
    await settle();
    expect(sent).toHaveBeenLastCalledWith('setViewOptions', { density: 'normal', striped: false, columnLines: false });
    // The stop is still the density: the way down leads to the stripes, up around to the reset.
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(stop(STRIPES));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(stop(RESET));
  });

  it('leaves Left and Right on the other entries alone', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    sent.mockClear();
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

  it('resets, closes and tells the server that the column selection closes', async () => {
    const sent = mountMenu();
    await settle();
    await openView();
    await userEvent.click(stop(RESET));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });
});

describe('the reset of the view', () => {
  it('resets the view and closes, the focus back on the gear', async () => {
    const sent = mountTable({ viewDensity: 'compact', striped: true });
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('menuitem', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('false');
  });

  it('resets with the keyboard as well', async () => {
    const sent = mountTable();
    await settle();
    await openPopover();
    await userEvent.keyboard('{End}{Enter}');
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });
});

describe('the page "Columns", the first page of the popover', () => {
  /** The content the server holds for the page: the column selection, a child control of the table. */
  const SELECTION = childControl('cs', 'Spaltenwahl');
  /** A selection with a search: more than ten columns. */
  const SEARCHABLE = childControl('cs', 'Spaltenwahl', true);

  function mountColumns(state: Record<string, unknown> = {}) {
    return mountTable({ columnSelect: true, ...state });
  }

  /** The server's answer to the opening: the selection arrives. */
  async function arrive(selection: unknown = SELECTION): Promise<void> {
    patchState({ columnSelection: selection });
    await settle();
  }

  function page(): HTMLElement {
    return screen.getByRole('dialog', { name: COLUMNS });
  }

  it('opens first, in the popover of the gear, and shows no other dialog; the page "View" stays hidden', async () => {
    mountColumns();
    await settle();
    await openPopover();

    // One dialog, the popover itself, named after the page; the gear still says it is open.
    expect(screen.getAllByRole('dialog')).toHaveLength(1);
    expect(page().classList).toContain('tl-table-view-options');
    expect(gear().getAttribute('aria-expanded')).toBe('true');
    expect(document.querySelector('[aria-modal="true"]')).toBeNull();
    expect(screen.queryByRole('menu')).toBeNull();
    expect(screen.queryByRole('menuitemradio')).toBeNull();
  });

  it('shows the selection the server holds under its title, without a way back, the row "View" at its foot', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive();

    const dialog = page();
    const pages = dialog.querySelectorAll<HTMLElement>('.tl-table-view-options__page');
    expect(pages).toHaveLength(2);
    // The page "View" stays in the popover, hidden: only one of them shows at a time.
    const [columns, view] = Array.from(pages);
    expect(columns.hidden).toBe(false);
    expect(view.hidden).toBe(true);
    expect(columns.querySelector('.tl-table-view-options__title')!.textContent).toBe(COLUMNS);
    expect(columns.querySelector('.tl-table-view-options__back')).toBeNull();
    // The selection scrolls its own list: no second scroll area around it.
    expect(within(dialog).getByText('Spaltenwahl')).not.toBeNull();
    expect(dialog.querySelector('.tl-picker__scroll')).toBeNull();
    // At the foot, after a divider: the row leading to the page "View", after the selection.
    const row = viewRow();
    expect(columns.lastElementChild).toBe(row);
    expect(row.previousElementSibling!.tagName).toBe('HR');
    expect(document.getElementById('cs')!.compareDocumentPosition(row) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    // The reset belongs to the page "View".
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
    expect(screen.queryByRole('button', { name: RESET })).toBeNull();
  });

  it('draws the row "View" as a way to the next page: a plain button, the label and a hidden chevron at the end', async () => {
    mountColumns();
    await settle();
    await openPopover();
    const row = viewRow();
    expect(row.tagName).toBe('BUTTON');
    // The page is no menu: the row is no menu item.
    expect(row.hasAttribute('role')).toBe(false);
    expect(row.classList.contains('tl-menu__item')).toBe(true);
    expect(row.classList.contains('tl-menu__item--nav')).toBe(true);
    // The label and the chevron, in this order; no value.
    const [label, chevron] = Array.from(row.children);
    expect(label.className).toContain('tl-menu__label');
    expect(label.textContent).toBe(VIEW);
    expect(chevron.tagName).toBe('I');
    expect(chevron.classList.contains('tl-menu__chevron')).toBe(true);
    expect(chevron.classList.contains('tl-icon-sm')).toBe(true);
    expect(chevron.classList.contains('fa-chevron-right')).toBe(true);
    expect(chevron.getAttribute('aria-hidden')).toBe('true');
    expect(row.children.length).toBe(2);
    expect(row.querySelector('.tl-menu__value')).toBeNull();
    expect(row.textContent).not.toContain('›');
  });

  it('starts the focus on the row "View" while the selection has not arrived', async () => {
    mountColumns();
    await settle();
    await openPopover();
    expect(document.activeElement).toBe(viewRow());
  });

  it('moves the focus into the search of the selection when it arrives, and never again by itself', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive(SEARCHABLE);
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl' }));

    // The person goes back to the row; the server sends the selection anew: the focus stays.
    viewRow().focus();
    await arrive(childControl('cs', 'Spaltenwahl', true));
    expect(document.activeElement).toBe(viewRow());
  });

  it('keeps the focus on the row "View" when the selection arrives without a search', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive();
    expect(document.activeElement).toBe(viewRow());
  });

  it('leaves the focus where the person moved it before the selection arrived', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await userEvent.click(viewRow());
    await settle();
    const back = screen.getByRole('button', { name: BACK });
    expect(document.activeElement).toBe(back);
    await arrive(SEARCHABLE);
    expect(document.activeElement).toBe(back);
  });

  it('gives the focus to the row "View" when the selection goes away while it holds the focus', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive(SEARCHABLE);
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl' }));

    await arrive(null);
    expect(document.activeElement).toBe(viewRow());
    // The page stays: there is no other page to fall back to.
    expect(page()).toBeTruthy();
  });

  it('shows the page "View" behind the row, with the way back focused, and keeps the selection', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();

    await userEvent.click(viewRow());
    await settle();

    expect(screen.getByRole('dialog', { name: VIEW })).toBeTruthy();
    expect(screen.getByRole('menu', { name: VIEW })).toBeTruthy();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: BACK }));
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS]);
    // The page "Columns" stays in the popover, hidden, with its selection.
    const selection = document.getElementById('cs')!;
    expect(selection).not.toBeNull();
    expect((selection.closest('.tl-table-view-options__page') as HTMLElement).hidden).toBe(true);
  });

  it('goes back to the page "Columns" without asking the server again, the focus on the row "View"', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();
    await userEvent.click(viewRow());
    await settle();

    await userEvent.click(screen.getByRole('button', { name: BACK }));
    await settle();

    expect(page()).toBeTruthy();
    expect(screen.getByText('Spaltenwahl')).toBeTruthy();
    expect(document.activeElement).toBe(viewRow());
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS]);
  });

  it('closes on Escape from the page "Columns", tells the server once, and gives the focus back to the gear', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
    expect(document.activeElement).toBe(gear());
  });

  it('closes on Escape from the page "View", telling the server once', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();
    await userEvent.click(viewRow());
    await settle();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
    expect(document.activeElement).toBe(gear());
  });

  it('closes on a press outside, telling the server', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();

    fireEvent.pointerDown(document.body);
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
  });

  it('opens on the page "Columns" again after it was closed on the page "View"', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive();
    await userEvent.click(viewRow());
    await settle();
    await userEvent.keyboard('{Escape}');
    await settle();
    await arrive(null);

    await openPopover();

    expect(page()).toBeTruthy();
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('stays on the page "Columns" when the server releases the selection, showing none until a new one arrives', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive();

    await arrive(null);
    expect(page()).toBeTruthy();
    expect(screen.queryByText('Spaltenwahl')).toBeNull();
    expect(screen.queryByRole('menu')).toBeNull();

    await arrive(childControl('cs2', 'Spaltenwahl zwei'));
    expect(screen.getByText('Spaltenwahl zwei')).toBeTruthy();
  });

  it('shows no selection the server has released: open, close and open again within one round trip', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive(childControl('cs1', 'Spaltenwahl eins'));
    expect(screen.getByText('Spaltenwahl eins')).toBeTruthy();

    // Closed and opened again, before the server has answered the first of them: it still holds the first selection.
    await userEvent.keyboard('{Escape}');
    await openPopover();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS, CMD_OPEN_COLUMNS]);
    // The released selection is not drawn again, and the page waits for the new one.
    expect(screen.queryByText('Spaltenwahl eins')).toBeNull();
    expect(page()).toBeTruthy();

    // The server's answers: it releases the first, then builds the second.
    await arrive(null);
    expect(page()).toBeTruthy();
    await arrive(childControl('cs2', 'Spaltenwahl zwei'));
    expect(screen.getByText('Spaltenwahl zwei')).toBeTruthy();
  });

  it('follows the focus through the late answers of a closing before the arrival: first selection, none, second', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await userEvent.keyboard('{Escape}');
    await openPopover();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS, CMD_OPEN_COLUMNS]);

    // The answer to the first opening: the focus, still on the row, goes into its search.
    await arrive(childControl('cs1', 'Spaltenwahl eins', true));
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl eins' }));
    // The answer to the closing takes it away with the focus: the row takes the focus.
    await arrive(null);
    expect(document.activeElement).toBe(viewRow());
    // The answer to the second opening arrives as the first one did.
    await arrive(childControl('cs2', 'Spaltenwahl zwei', true));
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl zwei' }));
  });

  it('tells the server when the button goes away while the popover is open, on either page', async () => {
    let sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();
    sent.mockClear();
    cleanup();
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE_COLUMNS, {});

    sent = mountColumns();
    await settle();
    await openView();
    sent.mockClear();
    cleanup();
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE_COLUMNS, {});
  });

  it('tells the server nothing when the button goes away while the popover is closed', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await userEvent.keyboard('{Escape}');
    await settle();
    sent.mockClear();

    cleanup();
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('keeps the page open between the opening and the answer of the server', async () => {
    mountColumns();
    await settle();
    await openPopover();

    // The selection has not arrived: the title and the row are there, nothing else.
    expect(page().querySelector('.tl-table-view-options__title')!.textContent).toBe(COLUMNS);
    expect(viewRow()).toBeTruthy();
  });
});

describe('closing the view', () => {
  it('closes on Escape and gives the focus back to the gear', async () => {
    mountTable({ columnSelect: true });
    await settle();
    await openPopover();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes on a second press of the gear', async () => {
    mountTable();
    await settle();
    await openPopover();
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
    await openPopover();
    fireEvent.contextMenu(screen.getByRole('menuitemradio', { name: DENSITY_NORMAL }));
    await settle();
    // The page "View" holds a menu itself: no second one opens.
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

  it('offers the grouping by several columns: "group by also" a further column, "ungroup this column" a grouping one', async () => {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    const sent = mountAdapter(TLTableView, {
      columns: [
        { name: 'name', label: 'Name', width: 120, sortable: true, filterable: false, groupable: true },
        { name: 'region', label: 'Region', width: 120, sortable: true, filterable: false, groupable: true },
      ],
      rows: [],
      totalRowCount: 0,
      grouping: ['region'],
    });
    await settle();
    fireEvent.contextMenu(screen.getByRole('columnheader', { name: /Name/ }));
    await settle();
    expect(screen.queryByRole('menuitem', { name: 'js.table.groupBy' })).toBeNull();
    expect(screen.queryByRole('menuitem', { name: 'js.table.ungroupColumn' })).toBeNull();
    expect(screen.getByRole('menuitem', { name: 'js.table.ungroup' })).toBeTruthy();
    await userEvent.click(screen.getByRole('menuitem', { name: 'js.table.groupByAlso' }));
    await settle();
    expect(sent).toHaveBeenCalledWith('group', { columns: ['region', 'name'] });
    fireEvent.contextMenu(screen.getByRole('columnheader', { name: /Region/ }));
    await settle();
    expect(screen.queryByRole('menuitem', { name: 'js.table.groupByAlso' })).toBeNull();
    await userEvent.click(screen.getByRole('menuitem', { name: 'js.table.ungroupColumn' }));
    await settle();
    expect(sent).toHaveBeenCalledWith('group', { columns: [] });
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
