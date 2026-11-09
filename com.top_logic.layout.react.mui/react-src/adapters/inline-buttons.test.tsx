// The buttons a control draws inside itself - the header buttons of a panel, the column buttons of a
// table, the buttons of a value list, the close button of a drawer - drawn by the MUI adapter of the
// part Button: one replacement of the part reaches them all.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent, createEvent, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { register, replacePart } from 'tl-react-bridge';
import TLPanel from '../../../com.top_logic.layout.react/react-src/controls/TLPanel';
import TLDrawer from '../../../com.top_logic.layout.react/react-src/controls/TLDrawer';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLValueList from '../../../com.top_logic.layout.react/react-src/controls/TLValueList';
import TLColumnSelect from '../../../com.top_logic.layout.react/react-src/controls/TLColumnSelect';
import TLDrawerToggle from '../../../com.top_logic.layout.react/react-src/controls/TLDrawerToggle';
import TLToolbar from '../../../com.top_logic.layout.react/react-src/controls/TLToolbar';
import TLWindow from '../../../com.top_logic.layout.react/react-src/controls/TLWindow';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiMenuTriggerAdapter from './MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import { CONTROL_ID, childControl, mountAdapter, patchState, settle } from './wire-test-support';
import { clearI18NCache } from '../../../com.top_logic.layout.react/react-src/bridge/i18n';
import MuiChipAdapter from './MuiChipAdapter';
import MuiDividerAdapter from './MuiDividerAdapter';

replacePart('Button', MuiButtonAdapter);
// The gear of the table is the trigger of its view.
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
// The table looks up the toggle of its tree rows as well.
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
// The table looks up the selection boxes of its rows as well.
replacePart('Checkbox', MuiCheckboxAdapter);
// The table looks up the boxes of its search as well.
replacePart('TextInput', MuiTextInputAdapter);
// The table draws its named filters as chips.
replacePart('Chip', MuiChipAdapter);
// The toolbar draws the lines between its groups and between the sections of its menus.
replacePart('Divider', MuiDividerAdapter);

/** The labels of the buttons: the keys, as the test stub translates each key to itself. */
const LABEL_MINIMIZE = 'js.panel.minimize';
const LABEL_MAXIMIZE = 'js.panel.maximize';
const LABEL_POP_OUT = 'js.panel.popOut';
const LABEL_DRAWER_CLOSE = 'js.drawer.close';
const LABEL_VIEW = 'js.table.view';
// The entry is the label and the value "n of m" together: a pattern.
const LABEL_COLUMNS_ENTRY = /^js\.table\.viewColumns\b/;
const LABEL_FILTER = 'js.table.filter';
const LABEL_FILTER_ACTIVE = 'js.table.filterActive';
const LABEL_ADD = 'js.valueList.add';
const LABEL_REMOVE = 'js.valueList.remove';
const LABEL_GROUP_BY = 'js.table.groupBy';
const LABEL_UNGROUP = 'js.table.ungroup';
const LABEL_OPEN_DRAWER = 'js.sidebar.openDrawer';
const LABEL_COLUMN_HIDE = 'js.table.columnHide';
const LABEL_COLUMN_LOCK = 'js.table.columnLock';
const LABEL_COLUMN_SHOW_LOCK = 'js.table.columnShowLock';

/** The commands the controls send for their buttons. */
const CMD_TOGGLE_MINIMIZE = 'toggleMinimize';
const CMD_CLOSE = 'close';
const CMD_OPEN_COLUMNS = 'openColumns';
const CMD_OPEN_FILTER = 'openFilter';
const CMD_ADD_ELEMENT = 'addElement';
const CMD_REMOVE_ELEMENT = 'removeElement';

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

/**
 * Answers the given keys of the next translation requests with English texts instead of the keys themselves, so that
 * an announcement composed from them can be read.
 */
function englishTexts(texts: Record<string, string>) {
  const answer = globalThis.fetch;
  vi.stubGlobal('fetch', async (url: string, init?: RequestInit) => {
    const response = await answer(url, init);
    if (!/react-api\/i18n/.test(url)) {
      return response;
    }
    const data = await response.json() as Record<string, string>;
    Object.keys(data).filter(key => key in texts).forEach(key => { data[key] = texts[key]; });
    return new Response(JSON.stringify(data), { status: 200, headers: { 'Content-Type': 'application/json' } });
  });
}

/** Asserts that the given button is an icon button of Material UI and none of TopLogic. */
function expectMuiIconButton(button: HTMLElement) {
  expect(button.classList).toContain('MuiIconButton-root');
  expect(button.classList).not.toContain('tl-button');
}

describe('the header buttons of a panel as MUI icon buttons', () => {
  it('draws minimize, maximize and pop-out, each named by its label', async () => {
    mountAdapter(TLPanel, {
      title: 'Kunden', showMinimize: true, showMaximize: true, showPopOut: true, child: childControl('x', 'Inhalt'),
    });
    await settle();

    for (const label of [LABEL_MINIMIZE, LABEL_MAXIMIZE, LABEL_POP_OUT]) {
      const button = screen.getByRole('button', { name: label });
      expectMuiIconButton(button);
      // The element around the buttons places them; the buttons carry no class of their own.
      expect(button.parentElement!.classList).toContain('tl-titlebar__buttons');
    }
  });

  it('sends the command of the pressed button', async () => {
    const sent = mountAdapter(TLPanel, { title: 'Kunden', showMinimize: true, child: childControl('x', 'Inhalt') });
    await settle();

    await userEvent.click(screen.getByRole('button', { name: LABEL_MINIMIZE }));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_TOGGLE_MINIMIZE, expect.anything());
  });
});

describe('the close button of a drawer as MUI icon button', () => {
  it('closes the drawer', async () => {
    const sent = mountAdapter(TLDrawer, { open: true, title: 'Details', child: childControl('x', 'Inhalt') });
    await settle();

    const close = screen.getByRole('button', { name: LABEL_DRAWER_CLOSE });
    expectMuiIconButton(close);
    await userEvent.click(close);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_CLOSE, expect.anything());
  });
});

describe('the column buttons of a table as MUI icon buttons', () => {
  function mountTable(filterActive: boolean) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false, filterable: true, filterActive }],
      rows: [],
      totalRowCount: 0,
      columnSelect: true,
      viewOptions: true,
    });
  }

  it('opens the column selection from the view of the gear', async () => {
    const sent = mountTable(false);
    await settle();

    const gear = screen.getByRole('button', { name: LABEL_VIEW });
    expectMuiIconButton(gear);
    await userEvent.click(gear);
    await settle();
    await userEvent.click(screen.getByRole('menuitem', { name: LABEL_COLUMNS_ENTRY }));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_COLUMNS, expect.anything());
  });

  it('opens the filter of a column from its funnel, named so while the filter is in force', async () => {
    const sent = mountTable(true);
    await settle();

    const funnel = screen.getByRole('button', { name: LABEL_FILTER_ACTIVE });
    expectMuiIconButton(funnel);
    // A dialog opener, no toggle: the name and the state of the column tell the filter in force,
    // not a pressed state.
    expect(funnel.hasAttribute('aria-pressed')).toBe(false);
    expect(funnel.closest('[role="columnheader"]')!.getAttribute('data-tl-state')).toContain('filtered');
    expect(funnel.querySelector('i.fa-filter')).not.toBeNull();
    await userEvent.click(funnel);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_FILTER, { column: 'name' });
  });

  it('shows the funnel of a column without filter empty', async () => {
    mountTable(false);
    await settle();

    const funnel = screen.getByRole('button', { name: LABEL_FILTER });
    expect(funnel.closest('[role="columnheader"]')!.getAttribute('data-tl-state') ?? '').not.toContain('filtered');
    expect(funnel.querySelector('i.fa-filter')).not.toBeNull();
  });
});

describe('the gear of a table as the trigger of its view', () => {
  function mountTable() {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    // jsdom lays nothing out; the focus search skips elements without a client rect.
    vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows: [],
      totalRowCount: 0,
      columnSelect: true,
      viewOptions: true,
    });
  }

  function gear(): HTMLElement {
    return screen.getByRole('button', { name: LABEL_VIEW });
  }

  function view(): HTMLElement | null {
    return screen.queryByRole('dialog', { name: LABEL_VIEW });
  }

  afterEach(() => vi.restoreAllMocks());

  it('announces a dialog and whether it is open', async () => {
    mountTable();
    await settle();
    expect(gear().getAttribute('aria-haspopup')).toBe('dialog');
    expect(gear().getAttribute('aria-expanded')).toBe('false');
    await userEvent.click(gear());
    expect(gear().getAttribute('aria-expanded')).toBe('true');
  });

  it('closes the view on a second click and does not open it again', async () => {
    mountTable();
    await settle();
    await userEvent.click(gear());
    expect(view()).not.toBeNull();
    await userEvent.click(gear());
    expect(view()).toBeNull();
    expect(gear().getAttribute('aria-expanded')).toBe('false');
  });

  it('closes the view on Escape from the gear', async () => {
    mountTable();
    await settle();
    await userEvent.click(gear());
    fireEvent.keyDown(gear(), { key: 'Escape' });
    expect(view()).toBeNull();
  });

  it('gets the focus back when the view closes', async () => {
    mountTable();
    await settle();
    await userEvent.click(gear());
    expect(view()!.contains(document.activeElement)).toBe(true);
    await userEvent.keyboard('{Escape}');
    expect(view()).toBeNull();
    expect(document.activeElement).toBe(gear());
  });
});

describe('the menu group of a toolbar as popup surface', () => {
  /** A toolbar with one menu group of two entries. */
  const TOOLBAR = {
    groups: [{
      name: 'more',
      label: 'Bearbeiten',
      display: 'menu',
      items: [childControl('i1', 'Kopieren'), childControl('i2', 'Einfuegen')],
    }],
  };

  function trigger(): HTMLElement {
    return screen.getByRole('button', { name: 'Bearbeiten' });
  }

  function dropdown(): HTMLElement {
    return document.body.querySelector('.tl-menu[role="menu"]') as HTMLElement;
  }

  afterEach(() => vi.restoreAllMocks());

  it('closes on Escape from the trigger', async () => {
    mountAdapter(TLToolbar, TOOLBAR);
    await userEvent.click(trigger());
    expect(dropdown().hidden).toBe(false);
    // The trigger keeps the focus where it was on its press; the key reaches the page outside the menu.
    fireEvent.keyDown(trigger(), { key: 'Escape' });
    expect(dropdown().hidden).toBe(true);
    expect(trigger().getAttribute('aria-expanded')).toBe('false');
  });

  it('holds no keyboard scope while closed: Escape reaches the bindings around the toolbar', async () => {
    const onEscape = vi.fn();
    mountAdapter(TLToolbar, TOOLBAR, { outer: { gesture: 'ESCAPE', onGesture: onEscape } });
    await userEvent.keyboard('{Escape}');
    expect(onEscape).toHaveBeenCalledTimes(1);
    await userEvent.click(trigger());
    await userEvent.keyboard('{Escape}');
    expect(onEscape).toHaveBeenCalledTimes(1);
    expect(dropdown().hidden).toBe(true);
  });

  it('gives the focus to its trigger when it closes and the focus had no place to return to', async () => {
    mountAdapter(TLToolbar, TOOLBAR);
    // The trigger keeps the focus where it is on its press: here, nowhere.
    await userEvent.click(trigger());
    expect(dropdown().hidden).toBe(false);
    await userEvent.keyboard('{Escape}');
    await Promise.resolve();
    expect(document.activeElement).toBe(trigger());
  });

  it('holds its height to the room at its trigger, at most the height of the design system', async () => {
    mountAdapter(TLToolbar, TOOLBAR);
    await userEvent.click(trigger());
    await settle();
    expect(dropdown().style.maxHeight).toMatch(/^min\(\d+px, var\(--tl-size-popover-max\)\)$/);
  });

  it('closes only the menu on Escape inside a window', async () => {
    register('InlineButtonsToolbar', TLToolbar);
    const toolbar = { controlId: 'tb', module: 'InlineButtonsToolbar', state: TOOLBAR } as ReturnType<typeof childControl>;
    const sent = mountAdapter(TLWindow, { title: 'Fenster', child: toolbar, closable: true });
    await settle();
    await userEvent.click(trigger());
    expect(dropdown().hidden).toBe(false);
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(dropdown().hidden).toBe(true);
    expect(sent).not.toHaveBeenCalledWith(CMD_CLOSE, expect.anything());
    // Positive control: with the menu closed, the same key reaches the window.
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_CLOSE, {});
  });
});

describe('the buttons of a value list as MUI icon buttons', () => {
  it('appends and removes values', async () => {
    const sent = mountAdapter(TLValueList, { elements: [childControl('v1', 'Eins', true)], editable: true });
    await settle();

    const add = screen.getByRole('button', { name: LABEL_ADD });
    const remove = screen.getByRole('button', { name: LABEL_REMOVE });
    expectMuiIconButton(add);
    expectMuiIconButton(remove);

    await userEvent.click(remove);
    await userEvent.click(add);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_REMOVE_ELEMENT, { index: 0 });
    expect(sent).toHaveBeenCalledWith(CMD_ADD_ELEMENT, expect.anything());
  });

  it('cannot be pressed while the field is disabled', async () => {
    mountAdapter(TLValueList, { elements: [childControl('v1', 'Eins', true)], editable: true, disabled: true });
    await settle();

    expect((screen.getByRole('button', { name: LABEL_ADD }) as HTMLButtonElement).disabled).toBe(true);
    expect((screen.getByRole('button', { name: LABEL_REMOVE }) as HTMLButtonElement).disabled).toBe(true);
  });
});

describe('the lock and the eye of the column selection as MUI icon buttons', () => {
  /** The commands the column selection sends for lock and eye. */
  const CMD_COLUMN_LOCK = 'columnLock';
  const CMD_COLUMN_VISIBLE = 'columnVisible';

  function entry(name: string, visible: boolean, locked: boolean) {
    return { name, label: 'Spalte ' + name, visible, locked };
  }

  it('draws lock and eye as icon buttons in the row, named by what a click does', async () => {
    // Two shown columns, so that the eye of the first one is not the last eye and stays enabled.
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, false), entry('b', false, false), entry('c', true, false)] });
    await settle();

    const hide = screen.getAllByRole('button', { name: LABEL_COLUMN_HIDE })[0];
    const showLock = screen.getByRole('button', { name: LABEL_COLUMN_SHOW_LOCK });
    for (const button of [hide, showLock]) {
      expectMuiIconButton(button);
      expect(button.closest('.tl-reorder-list__item')).not.toBeNull();
      expect(button.textContent).toBe('');
      // The row shows them on pointing or keyboard focus (layout contract: tl-reveal-on-hover).
      expect(button.classList.contains('tl-reveal-on-hover')).toBe(true);
    }
    const grip = document.querySelector('.tl-reorder-list__handle');
    expect(grip).not.toBeNull();
    expect(grip!.classList.contains('tl-reveal-on-hover')).toBe(true);

    await userEvent.click(hide);
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_VISIBLE, { column: 'a', visible: false });

    await userEvent.click(showLock);
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_LOCK, { column: 'b', locked: true });
  });

});

describe('the row of the column selection', () => {
  /** The commands the column selection sends. */
  const CMD_COLUMN_VISIBLE = 'columnVisible';
  const CMD_COLUMN_LOCK = 'columnLock';
  const CMD_COLUMN_MOVE = 'columnMove';
  const CMD_GROUP = 'group';

  function entry(name: string, visible: boolean, locked = false) {
    return { name, label: 'Spalte ' + name, visible, locked, grouped: false };
  }

  /** Three columns, the middle one hidden. */
  const ENTRIES = [entry('a', true), entry('b', false), entry('c', true)];

  function row(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item') as HTMLElement;
  }

  it('is the root that fills the popover: the host of the list', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: ENTRIES });
    await settle();

    expect(document.getElementById(CONTROL_ID)!.classList).toContain('tl-reorder-list__host');
  });

  it('hides a shown column on a click anywhere in its row', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, groupable: true, entries: ENTRIES });
    await settle();

    await userEvent.click(screen.getByText('Spalte a'));
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_VISIBLE, { column: 'a', visible: false });

    // The empty part of the row counts as well.
    await userEvent.click(row('c'));
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_VISIBLE, { column: 'c', visible: false });
  });

  it('shows a hidden column on a click in its row', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, groupable: true, entries: ENTRIES });
    await settle();

    await userEvent.click(screen.getByText('Spalte b'));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_VISIBLE, { column: 'b', visible: true });
  });

  it('sends only the command of a button that is pressed in the row, not the one of the row as well', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, groupable: true, entries: ENTRIES });
    await settle();

    await userEvent.click(within(row('a')).getByRole('button', { name: LABEL_GROUP_BY }));
    await settle();
    expect(sent).toHaveBeenCalledTimes(1);
    expect(sent).toHaveBeenLastCalledWith(CMD_GROUP, { column: 'a' });

    await userEvent.click(within(row('a')).getByRole('button', { name: LABEL_COLUMN_LOCK }));
    await settle();
    expect(sent).toHaveBeenCalledTimes(2);
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_LOCK, { column: 'a', locked: true });

    await userEvent.click(within(row('a')).getByRole('button', { name: LABEL_COLUMN_HIDE }));
    await settle();
    expect(sent).toHaveBeenCalledTimes(3);
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_VISIBLE, { column: 'a', visible: false });

    // The handle is no click either, also the one of a hidden column.
    await userEvent.click(row('a').querySelector('.tl-reorder-list__handle')!);
    await userEvent.click(row('b').querySelector('.tl-reorder-list__handle')!);
    await settle();
    expect(sent).toHaveBeenCalledTimes(3);
  });

  it('does not hide the last shown column on a click in its row', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('b', false)] });
    await settle();

    await userEvent.click(screen.getByText('Spalte a'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('keeps the dragged row where it is while the pointer moves, and releases it on the drop', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true), entry('b', true), entry('c', true)] });
    await settle();
    const dataTransfer = { effectAllowed: '', dropEffect: '', setData() {} };
    const first = row('a');
    const third = row('c');
    third.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;

    fireEvent.dragStart(first, { dataTransfer });
    await new Promise((resolve) => setTimeout(resolve, 5));
    expect(first.getAttribute('data-tl-state')).toContain('dragging');
    fireEvent.dragOver(third, { dataTransfer, clientY: 15 });
    // Hovering rebuilds no row: the same elements stand in the list, and the dragged one is still the same.
    expect(row('a')).toBe(first);
    expect(row('c')).toBe(third);
    expect(third.getAttribute('data-tl-state')).toContain('drop-after');

    fireEvent.drop(third, { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 2, locked: false, visible: true });
    // After the drop no row stays dimmed as dragged, no row shows an insertion mark, and no section stays the target.
    expect(document.querySelectorAll('[data-tl-state~="dragging"]')).toHaveLength(0);
    expect(document.querySelectorAll('[data-tl-state~="drop-after"]')).toHaveLength(0);
    expect(document.querySelectorAll('[data-tl-state~="drop-target"]')).toHaveLength(0);
  });
});

describe('the sections of the column selection as drop zones', () => {
  const CMD_COLUMN_LOCK = 'columnLock';
  const CMD_COLUMN_VISIBLE = 'columnVisible';
  const CMD_COLUMN_MOVE = 'columnMove';
  const SECTION_LOCKED = 'js.table.columnsLocked';
  const SECTION_MOVABLE = 'js.table.columnsMovable';
  const SECTION_HIDDEN = 'js.table.columnsHidden';
  const DROP_LOCKED = 'js.table.columnDropLocked';
  const DROP_MOVABLE = 'js.table.columnDropMovable';
  const DROP_HIDDEN = 'js.table.columnDropHidden';

  function entry(name: string, visible: boolean, locked = false) {
    return { name, label: 'Spalte ' + name, visible, locked, grouped: false };
  }

  function row(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item') as HTMLElement;
  }

  /** The heading of a section or the placeholder of an empty one, by its text. */
  function element(text: string): HTMLElement {
    return screen.getByText(text);
  }

  /** The headings, placeholders and rows of the list in their order, as text. */
  function listed(): string[] {
    return [...document.querySelectorAll('.tl-reorder-list > li')].map(item => item.textContent ?? '');
  }

  /** The texts of the elements marked as the target section. */
  function targets(): string[] {
    return [...document.querySelectorAll('.tl-reorder-list > li[data-tl-state~="drop-target"]')]
      .map(item => item.textContent ?? '');
  }

  /** Whether a row anywhere in the list shows an insertion mark. */
  function insertionMarks(): number {
    return document.querySelectorAll('[data-tl-state~="drop-before"], [data-tl-state~="drop-after"]').length;
  }

  const dataTransfer = { effectAllowed: '', dropEffect: '', setData() {} };

  /** Picks the given row up. */
  async function pick(from: HTMLElement) {
    fireEvent.dragStart(from, { dataTransfer });
    await new Promise((resolve) => setTimeout(resolve, 5));
  }

  /** A drag of one row onto another shown row, in the upper or the lower half of it. */
  async function drag(from: HTMLElement, onto: HTMLElement, side: 'before' | 'after') {
    onto.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;
    await pick(from);
    // jsdom has no drag events with a position: the hook reads clientY from a plain event.
    const over = createEvent.dragOver(onto, { dataTransfer });
    Object.defineProperty(over, 'clientY', { value: side === 'before' ? 2 : 18 });
    fireEvent(onto, over);
  }

  /** A drag of one row onto an element of a zone; whether the element left the drop refused (its default kept). */
  async function dragOnto(from: HTMLElement, onto: HTMLElement): Promise<boolean> {
    await pick(from);
    return fireEvent.dragOver(onto, { dataTransfer });
  }

  it('lists the frozen, the movable and the hidden columns under their headings in one list', async () => {
    mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true), entry('h', false), entry('c', true)] });
    await settle();

    // A hidden column keeps its place on the server; the list shows it apart, below the others.
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', SECTION_MOVABLE, 'Spalte b', 'Spalte c', SECTION_HIDDEN, 'Spalte h']);
    expect(document.querySelectorAll('.tl-reorder-list')).toHaveLength(1);
    for (const heading of document.querySelectorAll('.tl-reorder-list__section')) {
      expect(heading.getAttribute('role')).toBe('presentation');
    }
  });

  it('always lists all three sections: an empty one shows a quiet row to drop on', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('b', true)] });
    await settle();

    expect(listed()).toEqual([SECTION_LOCKED, DROP_LOCKED, SECTION_MOVABLE, 'Spalte a', 'Spalte b', SECTION_HIDDEN, DROP_HIDDEN]);
    for (const placeholder of [element(DROP_LOCKED), element(DROP_HIDDEN)]) {
      expect(placeholder.classList).toContain('tl-reorder-list__empty');
      expect(placeholder.getAttribute('data-tl-state')).toBeNull();
      // Like a heading, the quiet row is no item of the list: a screen reader counts the columns alone.
      expect(placeholder.getAttribute('role')).toBe('presentation');
    }
  });

  it('gives "Movable" its quiet row as well where all shown columns are frozen', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true, true), entry('h', false)] });
    await settle();

    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', SECTION_MOVABLE, DROP_MOVABLE, SECTION_HIDDEN, 'Spalte h']);
  });

  it('leaves the empty sections out while a search narrows the list', async () => {
    const names = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l'];
    mountAdapter(TLColumnSelect, { revision: 0, entries: names.map((name, i) => entry(name, true, i === 0)) });
    await settle();
    expect(listed()).toContain(DROP_HIDDEN);

    await userEvent.type(screen.getByRole('searchbox', { name: 'js.table.columnSearch' }), 'Spalte b');

    expect(listed()).toEqual([SECTION_MOVABLE, 'Spalte b']);
  });

  it('draws a hidden column with handle, draggable, with a plus instead of the closed eye', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('h', false)] });
    await settle();

    const hidden = row('h');
    expect(hidden.querySelector('.tl-reorder-list__handle')).not.toBeNull();
    expect(hidden.getAttribute('draggable')).toBe('true');
    expect(hidden.getAttribute('data-tl-state')).toContain('off');
    const show = within(hidden).getByRole('button', { name: 'js.table.columnShow' });
    expect(show.querySelector('i.fa-plus')).not.toBeNull();
    expect(show.querySelector('i.fa-eye-slash')).toBeNull();
    expect(row('a').getAttribute('draggable')).toBe('true');
  });

  it('shows and freezes a hidden column with its lock, in one command', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('h', false)] });
    await settle();

    await userEvent.click(within(row('h')).getByRole('button', { name: 'js.table.columnShowLock' }));
    await settle();

    expect(sent).toHaveBeenCalledTimes(1);
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_LOCK, { column: 'h', locked: true });
    // The row click shows it alone.
    await userEvent.click(screen.getByText('Spalte h'));
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_VISIBLE, { column: 'h', visible: true });
  });

  it('counts the position of a drop among the shown columns and names it in the whole list', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true), entry('b', true), entry('h', false), entry('c', true), entry('d', true)] });
    await settle();

    // d before c: in the list of the server c stands behind the hidden h, at 3.
    await drag(row('d'), row('c'), 'before');
    fireEvent.drop(row('c'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'd', targetIndex: 3, locked: false, visible: true });
  });

  it('freezes a movable column dropped among the frozen ones, at the dropped place', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('c', true), entry('d', true)] });
    await settle();

    // c behind a: an insertion mark there, and "Frozen" is the target, not "Movable".
    await drag(row('c'), row('a'), 'after');
    expect(row('a').getAttribute('data-tl-state')).toContain('drop-after');
    expect(targets()).toEqual([SECTION_LOCKED, 'Spalte a', 'Spalte b']);
    fireEvent.drop(row('a'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'c', targetIndex: 1, locked: true, visible: true });
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', 'Spalte c', 'Spalte b', SECTION_MOVABLE, 'Spalte d', SECTION_HIDDEN, DROP_HIDDEN]);
    expect(row('c').getAttribute('data-tl-state')).toContain('locked');
  });

  it('releases a frozen column dropped among the movable ones, at the dropped place', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('c', true), entry('d', true)] });
    await settle();

    await drag(row('a'), row('c'), 'before');
    expect(targets()).toEqual([SECTION_MOVABLE, 'Spalte c', 'Spalte d']);
    fireEvent.drop(row('c'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 1, locked: false, visible: true });
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte b', SECTION_MOVABLE, 'Spalte a', 'Spalte c', 'Spalte d', SECTION_HIDDEN, DROP_HIDDEN]);
  });

  it('takes the section of the row a drop at the line lands on, though the position stays the same', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('c', true), entry('d', true)] });
    await settle();

    // The first movable column behind the last frozen one: the same position, frozen now.
    await drag(row('c'), row('b'), 'after');
    fireEvent.drop(row('b'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'c', targetIndex: 2, locked: true, visible: true });
  });

  it('takes a drop within the block', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('c', true)] });
    await settle();

    await drag(row('a'), row('b'), 'after');
    expect(row('b').getAttribute('data-tl-state')).toContain('drop-after');
    fireEvent.drop(row('b'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 1, locked: true, visible: true });
  });

  it('sends nothing for a drop that leaves the column where it is, in its section', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true), entry('b', true), entry('c', true)] });
    await settle();

    // b before c: b stays where it is.
    await drag(row('b'), row('c'), 'before');
    fireEvent.drop(row('c'), { dataTransfer });
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('sends nothing for a drop that changes nothing shown, though a hidden column stands in between on the server',
    async () => {
      // On the server, the hidden h stands between a and b; the list shows a, b and below them h.
      const sent = mountAdapter(TLColumnSelect,
        { revision: 0, entries: [entry('a', true), entry('h', false), entry('b', true)] });
      await settle();

      // b behind a: where it is shown already; only the place of b relative to the hidden h would change.
      await drag(row('b'), row('a'), 'after');
      fireEvent.drop(row('a'), { dataTransfer });
      await settle();

      expect(sent).not.toHaveBeenCalled();
      expect(listed()).toEqual([SECTION_LOCKED, DROP_LOCKED, SECTION_MOVABLE, 'Spalte a', 'Spalte b', SECTION_HIDDEN,
        'Spalte h']);
    });

  it('counts a drop during a search in the whole list: next to a row the search leaves out', async () => {
    // Twelve shown columns behind a hidden one; the search finds a and c, b between them is left out.
    const names = ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'i', 'j', 'k', 'l', 'm'];
    const shown = names.map(name => ({ ...entry(name, true), label: (name === 'a' || name === 'c' ? 'Treffer ' : 'Spalte ')
      + name }));
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('h', false), ...shown] });
    await settle();
    await userEvent.type(screen.getByRole('searchbox', { name: 'js.table.columnSearch' }), 'Treffer');
    const found = (name: string) => screen.getByText('Treffer ' + name).closest('.tl-reorder-list__item') as HTMLElement;
    expect(listed()).toEqual([SECTION_MOVABLE, 'Treffer a', 'Treffer c']);

    // c behind a: in front of b, which the search does not show - position 1 among the shown columns, which is 2 in
    // the list of the server behind the hidden h.
    await drag(found('c'), found('a'), 'after');
    fireEvent.drop(found('a'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledTimes(1);
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'c', targetIndex: 2, locked: false, visible: true });
  });

  it('shows a hidden column dropped between two movable ones exactly there', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true), entry('b', true), entry('h', false), entry('c', true), entry('d', true)] });
    await settle();

    await drag(row('h'), row('d'), 'before');
    expect(row('d').getAttribute('data-tl-state')).toContain('drop-before');
    fireEvent.drop(row('d'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'h', targetIndex: 3, locked: false, visible: true });
    expect(listed()).toEqual([SECTION_LOCKED, DROP_LOCKED, SECTION_MOVABLE,
      'Spalte a', 'Spalte b', 'Spalte c', 'Spalte h', 'Spalte d', SECTION_HIDDEN, DROP_HIDDEN]);
  });

  it('hides and releases a frozen column dropped into "Hidden": no insertion mark, the section is the target', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('c', true), entry('h', false)] });
    await settle();

    const refused = await dragOnto(row('a'), row('h'));
    expect(refused).toBe(false);
    expect(insertionMarks()).toBe(0);
    expect(targets()).toEqual([SECTION_HIDDEN, 'Spalte h']);
    fireEvent.drop(row('h'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 0, locked: false, visible: false });
    // Released, it rests directly behind the block, in front of the hidden h.
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte b', SECTION_MOVABLE, 'Spalte c', SECTION_HIDDEN, 'Spalte a', 'Spalte h']);
    expect(row('a').getAttribute('data-tl-state')).toContain('off');
    expect(row('a').getAttribute('data-tl-state')).not.toContain('locked');
  });

  it('takes a drop on the heading of a section like one into the section', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true), entry('c', true), entry('h', false)] });
    await settle();

    // A hidden column on "Frozen": shown and frozen, at the start of the block.
    expect(await dragOnto(row('h'), element(SECTION_LOCKED))).toBe(false);
    expect(targets()).toEqual([SECTION_LOCKED, 'Spalte a']);
    fireEvent.drop(element(SECTION_LOCKED), { dataTransfer });
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_MOVE, { column: 'h', targetIndex: 0, locked: true, visible: true });

    // A frozen column on "Movable": released, at the start of the rest.
    await dragOnto(row('a'), element(SECTION_MOVABLE));
    fireEvent.drop(element(SECTION_MOVABLE), { dataTransfer });
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 1, locked: false, visible: true });

    // A movable column on "Hidden".
    await dragOnto(row('c'), element(SECTION_HIDDEN));
    fireEvent.drop(element(SECTION_HIDDEN), { dataTransfer });
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_MOVE, { column: 'c', targetIndex: 3, locked: false, visible: false });
  });

  it('refuses "Hidden" for the last shown column: no target, no drop', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('h', false)] });
    await settle();

    // The event keeps its default, so the browser shows that nothing would happen.
    expect(await dragOnto(row('a'), row('h'))).toBe(true);
    expect(targets()).toEqual([]);
    expect(fireEvent.dragOver(element(SECTION_HIDDEN), { dataTransfer })).toBe(true);
    expect(targets()).toEqual([]);
    fireEvent.drop(element(SECTION_HIDDEN), { dataTransfer });
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('shows "Frozen" without frozen columns with its quiet row, which takes a column', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true), entry('b', true), entry('h', false)] });
    await settle();

    expect(await dragOnto(row('b'), element(DROP_LOCKED))).toBe(false);
    expect(targets()).toEqual([SECTION_LOCKED, DROP_LOCKED]);
    fireEvent.drop(element(DROP_LOCKED), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'b', targetIndex: 0, locked: true, visible: true });
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte b', SECTION_MOVABLE, 'Spalte a', SECTION_HIDDEN, 'Spalte h']);
  });

  it('releases a column dropped on the quiet row of "Movable" at the start of the rest', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('h', false)] });
    await settle();

    await dragOnto(row('a'), element(DROP_MOVABLE));
    expect(targets()).toEqual([SECTION_MOVABLE, DROP_MOVABLE]);
    fireEvent.drop(element(DROP_MOVABLE), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 1, locked: false, visible: true });
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte b', SECTION_MOVABLE, 'Spalte a', SECTION_HIDDEN, 'Spalte h']);
  });

  it('marks the last shown column: no pointer row, and the reason as tooltip on the row', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('h', false)] });
    await settle();

    expect(row('a').getAttribute('data-tl-state')).toContain('last');
    expect(row('a').getAttribute('data-tooltip')).toBe('text:js.table.columnLastVisible');
    expect(row('h').getAttribute('data-tl-state') ?? '').not.toContain('last');
    expect(row('h').hasAttribute('data-tooltip')).toBe(false);
  });

  it('marks no column as the last one while two are shown', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('b', true)] });
    await settle();

    expect(row('a').getAttribute('data-tl-state') ?? '').not.toContain('last');
    expect(row('a').hasAttribute('data-tooltip')).toBe(false);
  });
});

describe('the keyboard across the sections of the column selection', () => {
  const CMD_COLUMN_MOVE = 'columnMove';
  const SECTION_LOCKED = 'js.table.columnsLocked';
  const SECTION_MOVABLE = 'js.table.columnsMovable';
  const SECTION_HIDDEN = 'js.table.columnsHidden';
  const DROP_LOCKED = 'js.table.columnDropLocked';
  const DROP_HIDDEN = 'js.table.columnDropHidden';
  const MOVED = 'js.reorder.moved';
  const MOVED_LOCKED = 'js.table.columnMovedLocked';
  const MOVED_MOVABLE = 'js.table.columnMovedMovable';
  const MOVED_HIDDEN = 'js.table.columnMovedHidden';

  function entry(name: string, visible: boolean, locked = false) {
    return { name, label: 'Spalte ' + name, visible, locked, grouped: false };
  }

  function handle(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item')!
      .querySelector<HTMLElement>('.tl-reorder-list__handle')!;
  }

  function listed(): string[] {
    return [...document.querySelectorAll('.tl-reorder-list > li')].map(item => item.textContent ?? '');
  }

  /** What the live region announced last. */
  function announced(): string {
    return document.querySelector('[role="status"]')!.textContent ?? '';
  }

  /** Alt + arrow on the handle of the given column, which has the focus. */
  function step(name: string, key: 'ArrowUp' | 'ArrowDown') {
    handle(name).focus();
    fireEvent.keyDown(handle(name), { key, altKey: true });
  }

  it('moves within a section past a hidden column, with the position among the shown columns', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true), entry('h', false), entry('c', true)] });
    await settle();

    // Down from b: over c, which stands behind the hidden h.
    step('b', 'ArrowDown');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'b', targetIndex: 3, locked: false, visible: true });
    expect(announced()).toBe(MOVED);
  });

  it('freezes the first movable column by Alt + up, at the end of "Frozen", and says so', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true), entry('c', true)] });
    await settle();

    step('b', 'ArrowUp');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'b', targetIndex: 1, locked: true, visible: true });
    expect(announced()).toBe(MOVED_LOCKED);
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', 'Spalte b', SECTION_MOVABLE, 'Spalte c', SECTION_HIDDEN, DROP_HIDDEN]);
    expect(document.activeElement).toBe(handle('b'));
  });

  it('releases the last frozen column by Alt + down, at the same position', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('c', true)] });
    await settle();

    step('b', 'ArrowDown');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'b', targetIndex: 1, locked: false, visible: true });
    expect(announced()).toBe(MOVED_MOVABLE);
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', SECTION_MOVABLE, 'Spalte b', 'Spalte c', SECTION_HIDDEN, DROP_HIDDEN]);
    expect(document.activeElement).toBe(handle('b'));
  });

  it('releases the last frozen column by Alt + down where "Movable" is empty', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('b', true, true), entry('h', false)] });
    await settle();

    step('b', 'ArrowDown');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'b', targetIndex: 1, locked: false, visible: true });
    expect(announced()).toBe(MOVED_MOVABLE);
  });

  it('hides the last movable column by Alt + down', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true), entry('b', true), entry('h', false)] });
    await settle();

    step('b', 'ArrowDown');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'b', targetIndex: 1, locked: false, visible: false });
    expect(announced()).toBe(MOVED_HIDDEN);
    expect(listed()).toEqual([SECTION_LOCKED, DROP_LOCKED, SECTION_MOVABLE, 'Spalte a', SECTION_HIDDEN, 'Spalte b', 'Spalte h']);
    expect(document.activeElement).toBe(handle('b'));
  });

  it('shows a hidden column by Alt + up at the end of "Movable"', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('h', false), entry('b', true), entry('i', false)] });
    await settle();

    step('i', 'ArrowUp');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'i', targetIndex: 3, locked: false, visible: true });
    expect(announced()).toBe(MOVED_MOVABLE);
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', SECTION_MOVABLE, 'Spalte b', 'Spalte i', SECTION_HIDDEN, 'Spalte h']);
  });

  it('does nothing and says nothing at the ends: down in "Hidden", up on the first frozen, down on the last shown', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a', true, true), entry('h', false), entry('i', false)] });
    await settle();

    step('h', 'ArrowDown');
    step('i', 'ArrowDown');
    step('a', 'ArrowUp');
    await settle();
    expect(sent).not.toHaveBeenCalled();
    expect(announced()).toBe('');
  });

  it('does not hide the last shown column by Alt + down', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true), entry('h', false)] });
    await settle();

    step('a', 'ArrowDown');
    await settle();

    expect(sent).not.toHaveBeenCalled();
    expect(announced()).toBe('');
  });

  describe('with the texts of the announcements', () => {
    afterEach(() => {
      // The English texts must not reach the next test; the list is unmounted first, so that no hook of it answers
      // the cleared cache.
      cleanup();
      clearI18NCache();
    });

    it('names the column, its position among the shown columns and their number', async () => {
      clearI18NCache();
      mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a', true, true), entry('b', true), entry('c', true),
        entry('d', true), entry('h', false)] });
      englishTexts({
        [MOVED]: '{0}, position {1} of {2}',
        [MOVED_LOCKED]: '{0}, frozen, position {1} of {2}',
        [MOVED_MOVABLE]: '{0}, movable, position {1} of {2}',
        [MOVED_HIDDEN]: '{0}, hidden',
      });
      await settle();

      // Within "Movable": c behind d, the last of the four shown columns; the hidden h does not count.
      step('c', 'ArrowDown');
      await settle();
      expect(announced()).toBe('Spalte c, position 4 of 4');

      // Across the line: b frozen behind a.
      step('b', 'ArrowUp');
      await settle();
      expect(announced()).toBe('Spalte b, frozen, position 2 of 4');

      // And back: b released at the same position.
      step('b', 'ArrowDown');
      await settle();
      expect(announced()).toBe('Spalte b, movable, position 2 of 4');

      // Into "Hidden": no position.
      step('c', 'ArrowDown');
      await settle();
      expect(announced()).toBe('Spalte c, hidden');
    });
  });
});

describe('the predicted list of the column selection', () => {
  const CMD_COLUMN_MOVE = 'columnMove';

  function entry(name: string, visible = true, locked = false) {
    return { name, label: 'Spalte ' + name, visible, locked, grouped: false };
  }

  function row(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item') as HTMLElement;
  }

  function handle(name: string): HTMLElement {
    return row(name).querySelector<HTMLElement>('.tl-reorder-list__handle')!;
  }

  /** The labels of the rows in their order. */
  function rows(): string[] {
    return [...document.querySelectorAll('.tl-reorder-list__label')].map(label => label.textContent ?? '');
  }

  const dataTransfer = { effectAllowed: '', dropEffect: '', setData() {} };

  /** Drops the given row in front of another one. */
  async function dropBefore(from: string, onto: string) {
    const target = row(onto);
    target.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;
    fireEvent.dragStart(row(from), { dataTransfer });
    await new Promise((resolve) => setTimeout(resolve, 5));
    const over = createEvent.dragOver(target, { dataTransfer });
    Object.defineProperty(over, 'clientY', { value: 2 });
    fireEvent(target, over);
    fireEvent.drop(target, { dataTransfer });
  }

  it('shows the new order right after the drop, before the server answers', async () => {
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    await dropBefore('c', 'a');

    // No await: the list has not waited for anything.
    expect(rows()).toEqual(['Spalte c', 'Spalte a', 'Spalte b']);
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'c', targetIndex: 0, locked: false, visible: true });
    // A patch without a new revision is no answer to the move.
    patchState({ entries: [entry('a'), entry('b'), entry('c')] });
    await settle();
    expect(rows()).toEqual(['Spalte c', 'Spalte a', 'Spalte b']);
  });

  it('shows no prediction while the state carries no revision: nothing would end it', async () => {
    const sent = mountAdapter(TLColumnSelect, { entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    await dropBefore('c', 'a');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'c', targetIndex: 0, locked: false, visible: true });
    expect(rows()).toEqual(['Spalte a', 'Spalte b', 'Spalte c']);
    patchState({ entries: [entry('c'), entry('a'), entry('b')] });
    await settle();
    expect(rows()).toEqual(['Spalte c', 'Spalte a', 'Spalte b']);
  });

  it('shows the list of the server once the revision has caught up, also where it differs', async () => {
    mountAdapter(TLColumnSelect, { revision: 4, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    await dropBefore('c', 'a');
    await settle();
    expect(rows()).toEqual(['Spalte c', 'Spalte a', 'Spalte b']);

    // The server did not move c.
    patchState({ revision: 5, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();
    expect(rows()).toEqual(['Spalte a', 'Spalte b', 'Spalte c']);
  });

  it('keeps the prediction until every command sent is answered, the eye included', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    await dropBefore('c', 'a');
    await userEvent.click(within(row('b')).getByRole('button', { name: LABEL_COLUMN_HIDE }));
    await settle();

    // The answer to the move - here one the prediction does not show: the answer to the eye is still out, so the
    // predicted list stays.
    patchState({ revision: 1, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();
    expect(rows()).toEqual(['Spalte c', 'Spalte a', 'Spalte b']);
    expect(row('b').getAttribute('data-tl-state') ?? '').not.toContain('off');

    // The answer to the eye.
    patchState({ revision: 2, entries: [entry('c'), entry('a'), entry('b', false)] });
    await settle();
    expect(row('b').getAttribute('data-tl-state')).toContain('off');
    expect(rows()).toEqual(['Spalte c', 'Spalte a', 'Spalte b']);
  });

  it('gives the focus back to the handle where the answer differs and the focus fell to the page', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    handle('b').focus();
    fireEvent.keyDown(handle('b'), { key: 'ArrowUp', altKey: true });
    await settle();
    expect(rows()).toEqual(['Spalte b', 'Spalte a', 'Spalte c']);
    expect(document.activeElement).toBe(handle('b'));

    // The row moving back takes the focus away from the handle in the browser.
    handle('b').blur();
    patchState({ revision: 1, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();
    expect(rows()).toEqual(['Spalte a', 'Spalte b', 'Spalte c']);
    expect(document.activeElement).toBe(handle('b'));
  });

  it('leaves the focus where the user put it meanwhile', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    handle('b').focus();
    fireEvent.keyDown(handle('b'), { key: 'ArrowUp', altKey: true });
    await settle();
    handle('c').focus();
    patchState({ revision: 1, entries: [entry('a'), entry('b'), entry('c')] });
    await settle();

    expect(document.activeElement).toBe(handle('c'));
  });
});

describe('a list the server pushes while a column of the selection is dragged', () => {
  const CMD_COLUMN_MOVE = 'columnMove';
  const dataTransfer = { effectAllowed: '', dropEffect: '', setData() {} };

  function entry(name: string, visible = true) {
    return { name, label: 'Spalte ' + name, visible, locked: false, grouped: false };
  }

  function row(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item') as HTMLElement;
  }

  /** Picks the given row up. */
  async function pick(name: string) {
    fireEvent.dragStart(row(name), { dataTransfer });
    await new Promise((resolve) => setTimeout(resolve, 5));
  }

  it('drops the column picked up, at the place it is dropped in the new list', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: ['a', 'b', 'c', 'd', 'e'].map(name => entry(name)) });
    await settle();

    await pick('a');
    // Meanwhile a moved to the end, by the header of the table: b stands where a stood.
    patchState({ entries: ['b', 'c', 'd', 'e', 'a'].map(name => entry(name)) });
    await settle();
    const target = row('d');
    target.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;
    const over = createEvent.dragOver(target, { dataTransfer });
    Object.defineProperty(over, 'clientY', { value: 2 });
    fireEvent(target, over);
    fireEvent.drop(target, { dataTransfer });
    await settle();

    // a in front of d: position 2 of b, c, a, d, e.
    expect(sent).toHaveBeenCalledTimes(1);
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 2, locked: false, visible: true });
  });

  it('hides the column picked up, dropped into "Hidden"', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, entries: [entry('a'), entry('b'), entry('c'), entry('h', false)] });
    await settle();

    await pick('a');
    patchState({ entries: [entry('b'), entry('c'), entry('a'), entry('h', false)] });
    await settle();
    fireEvent.dragOver(row('h'), { dataTransfer });
    fireEvent.drop(row('h'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledTimes(1);
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_MOVE, { column: 'a', targetIndex: 2, locked: false, visible: false });
  });
});

describe('a fast series of keyboard moves in the column selection', () => {
  const CMD_COLUMN_MOVE = 'columnMove';

  function entry(name: string, locked = false) {
    return { name, label: 'Spalte ' + name, visible: true, locked, grouped: false };
  }

  function handle(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item')!
      .querySelector<HTMLElement>('.tl-reorder-list__handle')!;
  }

  /** The entries of the given names, in this order, a frozen where `lockedA`. */
  function order(...names: string[]) {
    return names.map(name => entry(name, name === 'a' && lockedA));
  }

  let lockedA = false;

  /** Alt + arrow up on the handle of the given column, `times` times before anything arrives from the server. */
  function up(name: string, times: number) {
    for (let n = 0; n < times; n++) {
      fireEvent.keyDown(handle(name), { key: 'ArrowUp', altKey: true });
    }
  }

  it('sends every step at once, each from where the predicted list puts the column', async () => {
    lockedA = false;
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: order('a', 'b', 'c', 'd') });
    await settle();
    handle('d').focus();

    up('d', 3);
    await settle();
    expect(sent.mock.calls).toEqual([
      [CMD_COLUMN_MOVE, { column: 'd', targetIndex: 2, locked: false, visible: true }],
      [CMD_COLUMN_MOVE, { column: 'd', targetIndex: 1, locked: false, visible: true }],
      [CMD_COLUMN_MOVE, { column: 'd', targetIndex: 0, locked: false, visible: true }],
    ]);
    expect(document.activeElement).toBe(handle('d'));

    // The answers come one by one; the list stays at the last step until all three have arrived.
    patchState({ revision: 1, entries: order('a', 'b', 'd', 'c') });
    await settle();
    expect([...document.querySelectorAll('.tl-reorder-list__label')].map(label => label.textContent))
      .toEqual(['Spalte d', 'Spalte a', 'Spalte b', 'Spalte c']);
    patchState({ revision: 3, entries: order('d', 'a', 'b', 'c') });
    await settle();
    expect(sent).toHaveBeenCalledTimes(3);
    expect(document.activeElement).toBe(handle('d'));
  });

  it('freezes the column with the step that crosses the line', async () => {
    lockedA = true;
    const sent = mountAdapter(TLColumnSelect, { revision: 0, entries: order('a', 'b', 'c', 'd') });
    await settle();
    handle('d').focus();

    up('d', 3);
    await settle();

    expect(sent.mock.calls).toEqual([
      [CMD_COLUMN_MOVE, { column: 'd', targetIndex: 2, locked: false, visible: true }],
      [CMD_COLUMN_MOVE, { column: 'd', targetIndex: 1, locked: false, visible: true }],
      [CMD_COLUMN_MOVE, { column: 'd', targetIndex: 1, locked: true, visible: true }],
    ]);
    expect(document.querySelector('[role="status"]')!.textContent).toBe('js.table.columnMovedLocked');
    expect(document.activeElement).toBe(handle('d'));
  });
});

describe('the group toggle of the column selection', () => {
  /** The command the column selection sends for the group toggle. */
  const CMD_GROUP = 'group';

  function entry(name: string, visible: boolean, grouped: boolean) {
    return { name, label: 'Spalte ' + name, visible, locked: false, grouped };
  }

  it('offers grouping on every row, also a hidden one, and moves the grouping to the chosen column', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, groupable: true, entries: [entry('a', true, false), entry('b', false, false), entry('c', true, true)] });
    await settle();

    const groupBy = screen.getAllByRole('button', { name: LABEL_GROUP_BY });
    expect(groupBy).toHaveLength(2);
    for (const button of groupBy) {
      expect(button.getAttribute('aria-pressed')).toBe('false');
      expect(button.classList.contains('tl-reveal-on-hover')).toBe(true);
    }

    // The toggle of the hidden column b.
    await userEvent.click(groupBy[1]);
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_GROUP, { column: 'b' });
  });

  it('keeps the toggle of the grouped row in sight, pressed, and removes the grouping', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { revision: 0, groupable: true, entries: [entry('a', true, false), entry('c', true, true)] });
    await settle();

    const ungroup = screen.getByRole('button', { name: LABEL_UNGROUP });
    expect(ungroup.getAttribute('aria-pressed')).toBe('true');
    expect(ungroup.classList.contains('tl-reveal-on-hover')).toBe(false);
    expect(ungroup.closest('.tl-reorder-list__item')!.getAttribute('data-tl-state')).toContain('grouped');

    await userEvent.click(ungroup);
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_GROUP, { column: '' });
  });

  it('offers no grouping for a tree', async () => {
    mountAdapter(TLColumnSelect, { revision: 0, groupable: false, entries: [entry('a', true, false)] });
    await settle();

    expect(screen.queryByRole('button', { name: LABEL_GROUP_BY })).toBeNull();
    expect(screen.queryByRole('button', { name: LABEL_UNGROUP })).toBeNull();
  });
});

describe('the button opening the drawer of the sidebar as MUI icon button', () => {
  /** The command the button sends to the sidebar. */
  const CMD_TOGGLE = 'toggle';

  it('is a button on a compact screen that reports whether the drawer is open, and toggles the drawer', async () => {
    const sent = mountAdapter(TLDrawerToggle, { open: false, controls: 'nav' }, { displayClass: 'compact' });
    await settle();

    const button = screen.getByRole('button', { name: LABEL_OPEN_DRAWER });
    expectMuiIconButton(button);
    expect(button.id).toBe(CONTROL_ID);
    expect(button.getAttribute('aria-expanded')).toBe('false');

    await userEvent.click(button);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_TOGGLE, {});
  });

  it('renders nothing on a wide screen', async () => {
    mountAdapter(TLDrawerToggle, { open: false });
    await settle();

    expect(screen.queryByRole('button', { name: LABEL_OPEN_DRAWER })).toBeNull();
  });
});
