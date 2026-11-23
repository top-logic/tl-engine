// The gear of the React table opens a popover with the column selection the server holds and, where the table keeps
// its state, "Reset columns" at its foot (UI-Durchgang 2, #30234). How tables look - density, stripes, column lines -
// is not set there: the user menu sets it for every table of the page (#30233). The gear is there only where the
// popover has something to offer. The reset closes the popover; Escape gives the focus back to the gear.

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
import { CONTROL_ID, childControl, mountAdapter, patchState, settle, resetTableView } from './adapters/wire-test-support';

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
// The name of the gear and of the popover it opens.
const COLUMNS = 'js.table.columns';
const RESET = 'js.table.columnsReset';

const CMD_OPEN_COLUMNS = 'openColumns';
const CMD_CLOSE_COLUMNS = 'closeColumns';

/**
 * Mounts a table. The state key `viewOptions` says that the table keeps its state, so that its columns can be reset;
 * `columnSelect` says that it offers the column selection.
 */
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

/** The names of the commands sent for the column selection, in their order. */
function columnCommands(sent: ReturnType<typeof mountTable>): string[] {
  return sent.mock.calls.map((call) => call[0] as string).filter((name) => name.endsWith('Columns'));
}

/** The room the table keeps free at the end of its rows for the gear. */
function gearReserve(): string {
  return document.querySelector<HTMLElement>('.tl-table__canvas')!.style.paddingRight;
}

describe('the gear of the table', () => {
  it('is there where the table offers the column selection, with or without the reset', async () => {
    mountTable({ columnSelect: true, viewOptions: false });
    await settle();
    expect(queryGear()).not.toBeNull();
    cleanup();
    mountTable({ columnSelect: true });
    await settle();
    expect(queryGear()).not.toBeNull();
  });

  it('is there where the table keeps its state and offers no column selection: the reset is all it opens', async () => {
    mountTable({ columnSelect: false, viewOptions: true });
    await settle();
    expect(queryGear()).not.toBeNull();
  });

  it('is not there for a table with neither: nothing to open, and no room kept for it', async () => {
    mountTable({ columnSelect: true });
    await settle();
    const reserved = gearReserve();
    expect(reserved).not.toBe('0px');
    cleanup();

    mountTable({ columnSelect: false, viewOptions: false });
    await settle();
    expect(queryGear()).toBeNull();
    expect(gearReserve()).toBe('0px');
  });

  it('is named "Columns", whatever the popover holds', async () => {
    mountTable({ columnSelect: true });
    await settle();
    expect(screen.getByRole('button', { name: COLUMNS })).toBe(gear());
    cleanup();
    mountTable({ columnSelect: false });
    await settle();
    expect(screen.getByRole('button', { name: COLUMNS })).toBe(gear());
  });

  it('opens a dialog named "Columns" and says so on the gear', async () => {
    mountTable({ columnSelect: true });
    await settle();
    expect(gear().getAttribute('aria-haspopup')).toBe('dialog');
    expect(gear().getAttribute('aria-expanded')).toBe('false');
    const dialog = await openPopover();
    expect(screen.getByRole('dialog', { name: COLUMNS })).toBe(dialog);
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

describe('the popover offers no view settings', () => {
  it('has neither density, nor stripes, nor column lines, nor a second page', async () => {
    mountTable({ columnSelect: true });
    await settle();
    const dialog = await openPopover();
    expect(screen.queryByRole('menu')).toBeNull();
    expect(screen.queryByRole('menuitemradio')).toBeNull();
    expect(screen.queryByRole('menuitemcheckbox')).toBeNull();
    expect(dialog.querySelectorAll('.tl-table-view-options__page')).toHaveLength(1);
    expect(dialog.querySelector('.tl-menu__item--nav')).toBeNull();
    expect(dialog.querySelector('.tl-table-view-options__back')).toBeNull();
  });
});

describe('the popover of a table that offers no column selection', () => {
  it('holds the reset of the columns alone, and takes the focus itself', async () => {
    const sent = mountTable({ columnSelect: false });
    await settle();
    const dialog = await openPopover();
    expect(within(dialog).getByRole('button', { name: RESET })).toBeTruthy();
    expect(dialog.querySelector('.tl-table-view-options__title')).toBeNull();
    expect(dialog.querySelector('hr')).toBeNull();
    expect(document.activeElement).toBe(dialog);
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

  it('resets the columns and closes, the focus back on the gear', async () => {
    const sent = mountTable({ columnSelect: false });
    await settle();
    await openPopover();
    await userEvent.click(screen.getByRole('button', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });

  it('resets with the keyboard as well', async () => {
    const sent = mountTable({ columnSelect: false });
    await settle();
    await openPopover();
    await userEvent.tab();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: RESET }));
    await userEvent.keyboard('{Enter}');
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(screen.queryByRole('dialog')).toBeNull();
  });
});

describe('the popover of a table that offers the column selection', () => {
  /** The content the server holds for the popover: the column selection, a child control of the table. */
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

  function popover(): HTMLElement {
    return screen.getByRole('dialog', { name: COLUMNS });
  }

  it('opens in a popover that is the one dialog; the gear still says it is open', async () => {
    mountColumns();
    await settle();
    await openPopover();

    expect(screen.getAllByRole('dialog')).toHaveLength(1);
    expect(popover().classList).toContain('tl-table-view-options');
    expect(gear().getAttribute('aria-expanded')).toBe('true');
    expect(document.querySelector('[aria-modal="true"]')).toBeNull();
  });

  it('shows the selection the server holds under its title, the reset at its foot after a divider', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive();

    const dialog = popover();
    const page = dialog.querySelector<HTMLElement>('.tl-table-view-options__page')!;
    expect(page.querySelector('.tl-table-view-options__title')!.textContent).toBe(COLUMNS);
    // The selection scrolls its own list: no second scroll area around it.
    expect(within(dialog).getByText('Spaltenwahl')).not.toBeNull();
    expect(dialog.querySelector('.tl-picker__scroll')).toBeNull();
    const reset = within(page).getByRole('button', { name: RESET });
    expect(page.lastElementChild).toBe(reset);
    expect(reset.previousElementSibling!.tagName).toBe('HR');
    expect(document.getElementById('cs')!.compareDocumentPosition(reset) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();
    // The popover is no menu: the reset is a plain button, no menu item.
    expect(reset.hasAttribute('role')).toBe(false);
    expect(reset.classList).toContain('tl-menu__item');
    expect(screen.queryByRole('menuitem', { name: RESET })).toBeNull();
  });

  it('gives the reset of the columns an icon, hidden from assistive technology', async () => {
    mountColumns();
    await settle();
    await openPopover();
    const icon = within(popover()).getByRole('button', { name: RESET }).querySelector('.tl-menu__icon')!;
    expect(icon.classList.contains('fa-arrow-rotate-left')).toBe(true);
    expect(icon.getAttribute('aria-hidden')).toBe('true');
  });

  it('offers no reset and no divider where the table keeps no state', async () => {
    mountColumns({ viewOptions: false });
    await settle();
    await openPopover();
    await arrive();
    expect(screen.queryByRole('button', { name: RESET })).toBeNull();
    expect(popover().querySelector('hr')).toBeNull();
  });

  it('resets the columns from the popover, tells the server that the selection closes, and closes', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();
    await userEvent.click(screen.getByRole('button', { name: RESET }));
    await settle();
    expect(sent).toHaveBeenCalledWith('resetView', {});
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(gear());
  });

  it('starts the focus on the popover itself while the selection has not arrived, not on the reset', async () => {
    mountColumns();
    await settle();
    const dialog = await openPopover();
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
    expect(document.activeElement).toBe(dialog);
  });

  it('moves the focus into the search of the selection when it arrives, and never again by itself', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive(SEARCHABLE);
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl' }));

    // The person goes to the reset; the server sends the selection anew: the focus stays.
    const reset = screen.getByRole('button', { name: RESET });
    reset.focus();
    await arrive(childControl('cs', 'Spaltenwahl', true));
    expect(document.activeElement).toBe(reset);
  });

  it('keeps the focus on the popover when the selection arrives without a search', async () => {
    mountColumns();
    await settle();
    const dialog = await openPopover();
    await arrive();
    expect(document.activeElement).toBe(dialog);
  });

  it('leaves the focus where the person moved it before the selection arrived', async () => {
    mountColumns();
    await settle();
    await openPopover();
    const reset = screen.getByRole('button', { name: RESET });
    reset.focus();
    await arrive(SEARCHABLE);
    expect(document.activeElement).toBe(reset);
  });

  it('gives the focus to the popover when the selection goes away while it holds the focus', async () => {
    mountColumns();
    await settle();
    const dialog = await openPopover();
    await arrive(SEARCHABLE);
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl' }));

    await arrive(null);
    expect(document.activeElement).toBe(dialog);
    // The popover stays: the server may send a new selection.
    expect(popover()).toBeTruthy();
  });

  it('closes on Escape, tells the server once, and gives the focus back to the gear', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(screen.queryByRole('dialog')).toBeNull();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS]);
    expect(document.activeElement).toBe(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('false');
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

  it('closes on a second press of the gear', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await userEvent.click(gear());
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('is operable with the keyboard alone: Enter on the gear opens it', async () => {
    mountColumns();
    await settle();
    gear().focus();
    await userEvent.keyboard('{Enter}');
    await settle();
    expect(popover()).toBeTruthy();
  });

  it('stays open when the server releases the selection, showing none until a new one arrives', async () => {
    mountColumns();
    await settle();
    await openPopover();
    await arrive();

    await arrive(null);
    expect(popover()).toBeTruthy();
    expect(screen.queryByText('Spaltenwahl')).toBeNull();

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
    // The released selection is not drawn again, and the popover waits for the new one.
    expect(screen.queryByText('Spaltenwahl eins')).toBeNull();
    expect(popover()).toBeTruthy();

    // The server's answers: it releases the first, then builds the second.
    await arrive(null);
    expect(popover()).toBeTruthy();
    await arrive(childControl('cs2', 'Spaltenwahl zwei'));
    expect(screen.getByText('Spaltenwahl zwei')).toBeTruthy();
  });

  it('follows the focus through the late answers of a closing before the arrival: first selection, none, second', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await userEvent.keyboard('{Escape}');
    const dialog = await openPopover();
    expect(columnCommands(sent)).toEqual([CMD_OPEN_COLUMNS, CMD_CLOSE_COLUMNS, CMD_OPEN_COLUMNS]);

    // The answer to the first opening: the focus, still on the popover, goes into its search.
    await arrive(childControl('cs1', 'Spaltenwahl eins', true));
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl eins' }));
    // The answer to the closing takes it away with the focus: the popover takes the focus.
    await arrive(null);
    expect(document.activeElement).toBe(dialog);
    // The answer to the second opening arrives as the first one did.
    await arrive(childControl('cs2', 'Spaltenwahl zwei', true));
    expect(document.activeElement).toBe(screen.getByRole('textbox', { name: 'Spaltenwahl zwei' }));
  });

  it('tells the server when the button goes away while the popover is open', async () => {
    const sent = mountColumns();
    await settle();
    await openPopover();
    await arrive();
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

  it('keeps the popover open between the opening and the answer of the server', async () => {
    mountColumns();
    await settle();
    await openPopover();

    // The selection has not arrived: the title and the reset are there, nothing else.
    expect(popover().querySelector('.tl-table-view-options__title')!.textContent).toBe(COLUMNS);
    expect(screen.getByRole('button', { name: RESET })).toBeTruthy();
  });
});

describe('the gear in a heading', () => {
  it('opens no column menu on a right-click inside the popover', async () => {
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
    fireEvent.contextMenu(screen.getByRole('button', { name: RESET }));
    await settle();
    expect(screen.queryByRole('menu')).toBeNull();
    // The heading itself still opens its menu.
    fireEvent.contextMenu(screen.getByRole('columnheader', { name: /Name/ }));
    await settle();
    expect(screen.getAllByRole('menu')).toHaveLength(1);
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
    patchState({ grouping: ['name'] });
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
