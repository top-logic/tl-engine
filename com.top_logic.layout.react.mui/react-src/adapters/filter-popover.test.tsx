// The filter of a table column as a popover at its funnel (#30102, E1-E6, E15): the table draws the
// popover, not modal, around the content the server puts into its state; the value list of a filter
// choosing among values is that content, each row a check mark taking effect at once.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { screen, cleanup, fireEvent, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLFilterList from '../../../com.top_logic.layout.react/react-src/controls/table/TLFilterList';
import TLFilterForm from '../../../com.top_logic.layout.react/react-src/controls/table/TLFilterForm';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiMenuTriggerAdapter from './MuiMenuTriggerAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import { childControl, mountAdapter, patchState, settle } from './wire-test-support';

replacePart('Button', MuiButtonAdapter);
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
// The table looks up the toggle of its tree rows as well.
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('TextInput', MuiTextInputAdapter);

/** The labels: the keys, as the test stub translates each key to itself. */
const LABEL_FILTER = 'js.table.filter';
const LABEL_ALL = 'js.table.filterAll';
const LABEL_INVERT = 'js.table.filterInvert';
const LABEL_CLEAR = 'js.table.clear';
const LABEL_SEARCH = 'js.table.filterSearch';
const LABEL_NO_MATCH = 'js.table.filterNoMatch';
const LABEL_APPLY = 'js.table.filterApply';

const CMD_OPEN_FILTER = 'openFilter';
const CMD_CLOSE_FILTER = 'closeFilter';

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

beforeEach(() => {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  // jsdom lays nothing out; the focus search and the roving of a menu skip elements without a box.
  vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

/** One value of a value list, as the server sends it. */
interface Entry { key: string; label: string; count?: number; checked: boolean }

function entries(labels: string[], unchecked: string[] = [], counts: number[] = []): Entry[] {
  return labels.map((label, index) => ({
    key: String(index), label, count: counts[index] ?? index + 1, checked: !unchecked.includes(label),
  }));
}

const STATUS = ['Offen', 'In Arbeit', 'Erledigt', 'Abgelehnt'];

function row(name: string): HTMLElement {
  return screen.getByRole('menuitemcheckbox', { name: new RegExp('^' + name) });
}

describe('the value list of a column filter', () => {
  function mountList(state: Record<string, unknown>) {
    return mountAdapter(TLFilterList, { label: 'Status', searchable: false, active: false, ...state });
  }

  it('draws each value as a check row of a menu named by the column, with its count', async () => {
    mountList({ entries: entries(STATUS, ['Erledigt'], [4, 12, 230, 0]), active: true });
    await settle();

    const menu = screen.getByRole('menu', { name: 'Status' });
    expect(menu.classList).toContain('tl-picker__scroll');
    const offen = row('Offen');
    expect(offen.tagName).toBe('BUTTON');
    expect(offen.classList).toContain('tl-menu__item');
    expect(offen.classList).toContain('tl-menu__item--check');
    expect(offen.getAttribute('aria-checked')).toBe('true');
    expect(offen.querySelector('span.tl-checkbox[aria-hidden="true"]')).not.toBeNull();
    expect(offen.querySelector('input')).toBeNull();
    expect(offen.querySelector('.tl-filter-list__count')!.textContent).toBe('4');
    expect(row('Erledigt').getAttribute('aria-checked')).toBe('false');
    // A value without matches is quiet, but stays a row that clicks.
    expect(row('Abgelehnt').getAttribute('data-tl-state')).toBe('empty');
    expect(row('Abgelehnt').hasAttribute('disabled')).toBe(false);
    expect(row('Offen').hasAttribute('data-tl-state')).toBe(false);
  });

  it('shows no count where the table does not count', async () => {
    mountList({ entries: [{ key: '0', label: 'Offen', checked: true }] });
    await settle();
    expect(row('Offen').querySelector('.tl-filter-list__count')).toBeNull();
    expect(row('Offen').hasAttribute('data-tl-state')).toBe(false);
  });

  it('toggles a value on a click anywhere in its row', async () => {
    const sent = mountList({ entries: entries(STATUS) });
    await settle();

    await userEvent.click(row('Erledigt').querySelector('.tl-menu__label')!);
    await settle();

    expect(sent).toHaveBeenCalledWith('toggle', { key: '2' });
  });

  it('heads the values with "All": mixed while some are checked, a click checks all', async () => {
    const sent = mountList({ entries: entries(STATUS, ['Erledigt']), active: true });
    await settle();

    const all = row(LABEL_ALL);
    expect(all.classList).toContain('tl-filter-list__all');
    expect(all.getAttribute('aria-checked')).toBe('mixed');
    await userEvent.click(all);
    await settle();
    expect(sent).toHaveBeenCalledWith('all', { checked: true });
  });

  it('unchecks all from "All" when all are checked, and checks all again when none is', async () => {
    const sent = mountList({ entries: entries(STATUS) });
    await settle();
    expect(row(LABEL_ALL).getAttribute('aria-checked')).toBe('true');
    await userEvent.click(row(LABEL_ALL));
    await settle();
    expect(sent).toHaveBeenLastCalledWith('all', { checked: false });

    patchState({ entries: entries(STATUS, STATUS), active: true });
    expect(row(LABEL_ALL).getAttribute('aria-checked')).toBe('false');
    await userEvent.click(row(LABEL_ALL));
    await settle();
    expect(sent).toHaveBeenLastCalledWith('all', { checked: true });
  });

  it('offers "Remove filter" only while a filter is in force (E6), and "Invert selection" always (E15)', async () => {
    const sent = mountList({ entries: entries(STATUS) });
    await settle();

    const clear = screen.getByRole('button', { name: LABEL_CLEAR });
    const invert = screen.getByRole('button', { name: LABEL_INVERT });
    expect(clear.closest('.tl-filter-list__footer')).not.toBeNull();
    expect(invert.closest('.tl-filter-list__footer')).toBe(clear.closest('.tl-filter-list__footer'));
    expect((clear as HTMLButtonElement).disabled).toBe(true);
    expect((invert as HTMLButtonElement).disabled).toBe(false);

    await userEvent.click(invert);
    await settle();
    expect(sent).toHaveBeenCalledWith('invert', {});

    patchState({ entries: entries(STATUS, ['Offen']), active: true });
    expect((clear as HTMLButtonElement).disabled).toBe(false);
    await userEvent.click(clear);
    await settle();
    expect(sent).toHaveBeenCalledWith('clear', {});
  });

  it('keeps the focus in the list when "Remove filter" is disabled by its own click', async () => {
    mountList({ entries: entries(STATUS, ['Offen']), active: true });
    await settle();

    await userEvent.click(screen.getByRole('button', { name: LABEL_CLEAR }));
    patchState({ entries: entries(STATUS), active: false });

    expect(screen.getByRole('menu').contains(document.activeElement)).toBe(true);
  });

  it('has a search from eight values on that narrows the list, without "All" while it narrows (E3)', async () => {
    const many = ['Alpha', 'Beta', 'Gamma', 'Delta', 'Epsilon', 'Zeta', 'Eta', 'Theta'];
    mountList({ entries: entries(many), searchable: true });
    await settle();

    const search = screen.getByRole('searchbox', { name: LABEL_SEARCH });
    expect(screen.getAllByRole('menuitemcheckbox')).toHaveLength(9);
    // The search stands above the menu, not among its rows.
    expect(screen.getByRole('menu').contains(search)).toBe(false);

    await userEvent.type(search, 'eta');
    expect(screen.getAllByRole('menuitemcheckbox').map(item => item.textContent)).toEqual(
      ['Beta2', 'Zeta6', 'Eta7', 'Theta8']);
    expect(screen.queryByRole('menuitemcheckbox', { name: new RegExp('^' + LABEL_ALL) })).toBeNull();

    await userEvent.clear(search);
    await userEvent.type(search, 'xyz');
    expect(screen.queryByRole('menu')).toBeNull();
    expect(screen.getByText(LABEL_NO_MATCH).classList).toContain('tl-status');
  });

  it('has no search below eight values', async () => {
    mountList({ entries: entries(STATUS), searchable: false });
    await settle();
    expect(screen.queryByRole('searchbox')).toBeNull();
  });

  it('moves the focus between the rows with the arrows, Home and End, one row in the tab order', async () => {
    mountList({ entries: entries(STATUS) });
    await settle();

    const rows = screen.getAllByRole('menuitemcheckbox');
    expect(rows.filter(item => item.tabIndex === 0)).toHaveLength(1);
    rows[0].focus();
    fireEvent.keyDown(rows[0], { key: 'ArrowDown' });
    expect(document.activeElement).toBe(rows[1]);
    fireEvent.keyDown(rows[1], { key: 'End' });
    expect(document.activeElement).toBe(rows[rows.length - 1]);
    fireEvent.keyDown(rows[rows.length - 1], { key: 'Home' });
    expect(document.activeElement).toBe(rows[0]);
    fireEvent.keyDown(rows[0], { key: 'ArrowUp' });
    expect(document.activeElement).toBe(rows[rows.length - 1]);
    expect(screen.getAllByRole('menuitemcheckbox').filter(item => item.tabIndex === 0)).toEqual([rows[rows.length - 1]]);
  });
});

describe('the form of a column filter', () => {
  it('shows the form, and "Remove filter" only while a filter is in force', async () => {
    const sent = mountAdapter(TLFilterForm, { form: childControl('f1', 'Muster', true), active: false, applies: false });
    await settle();

    expect(screen.getByRole('textbox', { name: 'Muster' })).not.toBeNull();
    const clear = screen.getByRole('button', { name: LABEL_CLEAR }) as HTMLButtonElement;
    expect(clear.disabled).toBe(true);
    expect(screen.queryByRole('button', { name: LABEL_APPLY })).toBeNull();

    patchState({ active: true });
    expect(clear.disabled).toBe(false);
    await userEvent.click(clear);
    await settle();
    expect(sent).toHaveBeenCalledWith('clear', {});
  });

  it('offers "Apply" for the filter UI of an application (E5)', async () => {
    const sent = mountAdapter(TLFilterForm, { form: childControl('f1', 'Abteilung', true), active: false, applies: true });
    await settle();

    await userEvent.click(screen.getByRole('button', { name: LABEL_APPLY }));
    await settle();
    expect(sent).toHaveBeenCalledWith('apply', {});
  });

  it('puts the focus into the new form when the old one goes with the removed filter', async () => {
    mountAdapter(TLFilterForm, { form: childControl('f1', 'Muster', true), active: true, applies: false });
    await settle();

    await userEvent.click(screen.getByRole('button', { name: LABEL_CLEAR }));
    patchState({ form: childControl('f2', 'Muster neu', true), active: false });

    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Muster neu' }));
  });
});

describe('the funnel of a column and its filter popover', () => {
  const COLUMNS = [
    { name: 'status', label: 'Status', width: 120, sortable: false, filterable: true },
    { name: 'title', label: 'Titel', width: 120, sortable: false, filterable: true },
  ];

  const ROWS = [
    { id: 'r1', index: 0, cells: {} },
    { id: 'r2', index: 1, cells: {} },
    { id: 'r3', index: 2, cells: {} },
  ];

  function mountTable(filterPopover: unknown = null) {
    return mountAdapter(TLTableView, {
      columns: COLUMNS,
      rows: ROWS,
      totalRowCount: ROWS.length,
      viewportStart: 0,
      filterPopover,
    });
  }

  function funnels(): HTMLElement[] {
    return screen.getAllByRole('button', { name: LABEL_FILTER });
  }

  function popover(): HTMLElement | null {
    return screen.queryByRole('dialog');
  }

  const OPEN_STATUS = { column: 'status', content: childControl('fp', 'Inhalt', true) };

  it('announces a dialog at the funnel and asks the server for the filter on a click', async () => {
    const sent = mountTable();
    await settle();

    const funnel = funnels()[0];
    expect(funnel.getAttribute('aria-haspopup')).toBe('dialog');
    expect(funnel.getAttribute('aria-expanded')).toBe('false');
    await userEvent.click(funnel);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_FILTER, { column: 'status' });
    expect(funnel.getAttribute('aria-expanded')).toBe('true');
  });

  it('shows the content of the server in a popover that is not modal, named after the column', async () => {
    mountTable(OPEN_STATUS);
    await settle();

    const dialog = popover()!;
    expect(dialog.getAttribute('aria-label')).toBe(LABEL_FILTER + ' Status');
    expect(dialog.classList).toContain('tl-popover');
    expect(dialog.classList).toContain('tl-picker');
    expect(dialog.classList).toContain('tl-filter-list');
    expect(dialog.hasAttribute('aria-modal')).toBe(false);
    expect(document.querySelector('[aria-modal="true"]')).toBeNull();
    expect(within(dialog).getByText('Inhalt')).not.toBeNull();
    expect(funnels()[0].getAttribute('aria-expanded')).toBe('true');
    // The focus starts in the content: the search, the first row or the first field.
    expect(document.activeElement).toBe(within(dialog).getByRole('textbox', { name: 'Inhalt' }));
  });

  it('closes on a press outside and tells the server, the filter staying', async () => {
    const sent = mountTable(OPEN_STATUS);
    await settle();
    expect(popover()).not.toBeNull();

    fireEvent.pointerDown(document.body);
    await settle();

    expect(popover()).toBeNull();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE_FILTER, {});
    expect(funnels()[0].getAttribute('aria-expanded')).toBe('false');
  });

  it('lets the focus leave without closing (not modal)', async () => {
    mountTable(OPEN_STATUS);
    await settle();

    funnels()[1].focus();
    expect(document.activeElement).toBe(funnels()[1]);
    expect(popover()).not.toBeNull();
  });

  it('closes on Escape and gives the focus back to the funnel', async () => {
    const sent = mountTable(OPEN_STATUS);
    await settle();
    expect(popover()!.contains(document.activeElement)).toBe(true);

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(popover()).toBeNull();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE_FILTER, {});
    expect(document.activeElement).toBe(funnels()[0]);
  });

  it('closes on a second click of its funnel and does not open again', async () => {
    const sent = mountTable(OPEN_STATUS);
    await settle();

    await userEvent.click(funnels()[0]);
    await settle();

    expect(popover()).toBeNull();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE_FILTER, {});
    expect(sent).not.toHaveBeenCalledWith(CMD_OPEN_FILTER, expect.anything());
  });

  it('opens the filter of another column in the one click that closes the open one', async () => {
    const sent = mountTable(OPEN_STATUS);
    await settle();

    await userEvent.click(funnels()[1]);
    await settle();

    expect(sent.mock.calls.map(([command]) => command).filter(command => command.endsWith('Filter')))
      .toEqual([CMD_CLOSE_FILTER, CMD_OPEN_FILTER]);
    expect(sent).toHaveBeenLastCalledWith(CMD_OPEN_FILTER, { column: 'title' });
  });

  it('stays open with the focus in place while its own filter changes the rows', async () => {
    mountTable(OPEN_STATUS);
    await settle();
    const dialog = popover()!;
    const field = within(dialog).getByRole('textbox', { name: 'Inhalt' });
    expect(document.activeElement).toBe(field);

    patchState({ rows: ROWS.slice(0, 1), totalRowCount: 1, columns: [{ ...COLUMNS[0], filterActive: true }, COLUMNS[1]] });
    await settle();

    expect(popover()).toBe(dialog);
    expect(document.activeElement).toBe(field);
    // The anchor is the funnel of the column, now named for the filter in force, still marked open.
    expect(screen.getByRole('button', { name: 'js.table.filterActive' }).getAttribute('aria-expanded')).toBe('true');
  });

  it('closes when the server drops the popover', async () => {
    mountTable(OPEN_STATUS);
    await settle();

    patchState({ filterPopover: null });
    await settle();

    expect(popover()).toBeNull();
  });
});
