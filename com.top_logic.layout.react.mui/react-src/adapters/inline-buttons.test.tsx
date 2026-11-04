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
const LABEL_COLUMNS_ENTRY = 'js.table.viewColumns';
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

  it('opens the filter popover of a column from its funnel, named so while the filter is in force', async () => {
    const sent = mountTable(true);
    await settle();

    const funnel = screen.getByRole('button', { name: LABEL_FILTER_ACTIVE });
    expectMuiIconButton(funnel);
    // A dialog opener, no toggle: the name and the state of the column tell the filter in force,
    // not a pressed state.
    expect(funnel.hasAttribute('aria-pressed')).toBe(false);
    expect(funnel.closest('[role="columnheader"]')!.getAttribute('data-tl-state')).toContain('filtered');
    expect(funnel.querySelector('i.fa-filter')).not.toBeNull();
    // The trigger of a popover (#30102): it says so, and whether the popover is open.
    expect(funnel.getAttribute('aria-haspopup')).toBe('dialog');
    expect(funnel.getAttribute('aria-expanded')).toBe('false');
    await userEvent.click(funnel);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_FILTER, { column: 'name' });
    expect(funnel.getAttribute('aria-expanded')).toBe('true');
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
      { entries: [entry('a', true, false), entry('b', false, false), entry('c', true, false)] });
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
  const CMD_COLUMN_REORDER = 'columnReorder';
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
    mountAdapter(TLColumnSelect, { entries: ENTRIES });
    await settle();

    expect(document.getElementById(CONTROL_ID)!.classList).toContain('tl-reorder-list__host');
  });

  it('hides a shown column on a click anywhere in its row', async () => {
    const sent = mountAdapter(TLColumnSelect, { groupable: true, entries: ENTRIES });
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
    const sent = mountAdapter(TLColumnSelect, { groupable: true, entries: ENTRIES });
    await settle();

    await userEvent.click(screen.getByText('Spalte b'));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_VISIBLE, { column: 'b', visible: true });
  });

  it('sends only the command of a button that is pressed in the row, not the one of the row as well', async () => {
    const sent = mountAdapter(TLColumnSelect, { groupable: true, entries: ENTRIES });
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

    // The handle is no click either.
    await userEvent.click(row('a').querySelector('.tl-reorder-list__handle')!);
    await settle();
    expect(sent).toHaveBeenCalledTimes(3);
  });

  it('does not hide the last shown column on a click in its row', async () => {
    const sent = mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('b', false)] });
    await settle();

    await userEvent.click(screen.getByText('Spalte a'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('keeps the dragged row where it is while the pointer moves, and releases it on the drop', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { entries: [entry('a', true), entry('b', true), entry('c', true)] });
    await settle();
    const dataTransfer = { effectAllowed: '', dropEffect: '', setData() {} };
    const handleOf = (name: string) => row(name);
    const first = handleOf('a');
    const third = handleOf('c');
    third.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;

    fireEvent.dragStart(first, { dataTransfer });
    await new Promise((resolve) => setTimeout(resolve, 5));
    expect(first.getAttribute('data-tl-state')).toContain('dragging');
    fireEvent.dragOver(third, { dataTransfer, clientY: 15 });
    // Hovering rebuilds no row: the same elements stand in the list, and the dragged one is still the same.
    expect(handleOf('a')).toBe(first);
    expect(handleOf('c')).toBe(third);
    expect(third.getAttribute('data-tl-state')).toContain('drop-after');

    fireEvent.drop(third, { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_REORDER, { column: 'a', targetIndex: 2 });
    // After the drop no row stays dimmed as dragged, and no row shows an insertion mark.
    expect(document.querySelectorAll('.tl-reorder-list__item[data-tl-state~="dragging"]')).toHaveLength(0);
    expect(document.querySelectorAll('.tl-reorder-list__item[data-tl-state~="drop-after"]')).toHaveLength(0);
  });
});

describe('the sections and blocks of the column selection', () => {
  const CMD_COLUMN_VISIBLE = 'columnVisible';
  const CMD_COLUMN_LOCK = 'columnLock';
  const CMD_COLUMN_REORDER = 'columnReorder';
  const SECTION_LOCKED = 'js.table.columnsLocked';
  const SECTION_MOVABLE = 'js.table.columnsMovable';
  const SECTION_HIDDEN = 'js.table.columnsHidden';

  function entry(name: string, visible: boolean, locked = false) {
    return { name, label: 'Spalte ' + name, visible, locked, grouped: false };
  }

  function row(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item') as HTMLElement;
  }

  /** The headings and rows of the list in their order, as text. */
  function listed(): string[] {
    return [...document.querySelectorAll('.tl-reorder-list > li')].map(item => item.textContent ?? '');
  }

  const dataTransfer = { effectAllowed: '', dropEffect: '', setData() {} };

  /** A drag of one row onto another row, in the upper half of it. */
  async function drag(from: HTMLElement, onto: HTMLElement, side: 'before' | 'after') {
    onto.getBoundingClientRect = () => ({ top: 0, height: 20, left: 0, width: 100 }) as DOMRect;
    fireEvent.dragStart(from, { dataTransfer });
    await new Promise((resolve) => setTimeout(resolve, 5));
    // jsdom has no drag events with a position: the hook reads clientY from a plain event.
    const over = createEvent.dragOver(onto, { dataTransfer });
    Object.defineProperty(over, 'clientY', { value: side === 'before' ? 2 : 18 });
    fireEvent(onto, over);
  }

  it('lists the frozen, the movable and the hidden columns under their headings in one list', async () => {
    mountAdapter(TLColumnSelect,
      { entries: [entry('a', true, true), entry('b', true), entry('h', false), entry('c', true)] });
    await settle();

    // A hidden column keeps its place on the server; the list shows it apart, below the others.
    expect(listed()).toEqual([SECTION_LOCKED, 'Spalte a', SECTION_MOVABLE, 'Spalte b', 'Spalte c', SECTION_HIDDEN, 'Spalte h']);
    expect(document.querySelectorAll('.tl-reorder-list')).toHaveLength(1);
    for (const heading of document.querySelectorAll('.tl-reorder-list__section')) {
      expect(heading.getAttribute('role')).toBe('presentation');
    }
  });

  it('names no movable block where nothing is frozen, and no hidden block where nothing is hidden', async () => {
    mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('b', true)] });
    await settle();

    expect(listed()).toEqual(['Spalte a', 'Spalte b']);
  });

  it('draws a hidden column without handle, not draggable, with a plus instead of the closed eye', async () => {
    mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('h', false)] });
    await settle();

    const hidden = row('h');
    expect(hidden.querySelector('.tl-reorder-list__handle')).toBeNull();
    expect(hidden.getAttribute('draggable')).not.toBe('true');
    expect(hidden.getAttribute('data-tl-state')).toContain('off');
    const show = within(hidden).getByRole('button', { name: 'js.table.columnShow' });
    expect(show.querySelector('i.fa-plus')).not.toBeNull();
    expect(show.querySelector('i.fa-eye-slash')).toBeNull();
    expect(row('a').getAttribute('draggable')).toBe('true');
  });

  it('shows and freezes a hidden column with its lock, in one command', async () => {
    const sent = mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('h', false)] });
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
      { entries: [entry('a', true), entry('b', true), entry('h', false), entry('c', true), entry('d', true)] });
    await settle();

    // d before c: in the list of the server c stands behind the hidden h, at 3.
    await drag(row('d'), row('c'), 'before');
    fireEvent.drop(row('c'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_REORDER, { column: 'd', targetIndex: 3 });
  });

  it('does not accept a drop across the line between the frozen and the movable columns', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { entries: [entry('a', true, true), entry('b', true), entry('c', true)] });
    await settle();

    // A movable column over the frozen one: no insertion mark, no move.
    await drag(row('b'), row('a'), 'after');
    expect(row('a').getAttribute('data-tl-state') ?? '').not.toContain('drop-');
    fireEvent.drop(row('a'), { dataTransfer });
    await settle();
    expect(sent).not.toHaveBeenCalled();

    // A frozen column over a movable one.
    await drag(row('a'), row('b'), 'before');
    expect(row('b').getAttribute('data-tl-state') ?? '').not.toContain('drop-');
    fireEvent.drop(row('b'), { dataTransfer });
    await settle();
    expect(sent).not.toHaveBeenCalled();
  });

  it('takes a drop within the block', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { entries: [entry('a', true, true), entry('b', true, true), entry('c', true)] });
    await settle();

    await drag(row('a'), row('b'), 'after');
    expect(row('b').getAttribute('data-tl-state')).toContain('drop-after');
    fireEvent.drop(row('b'), { dataTransfer });
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_REORDER, { column: 'a', targetIndex: 1 });
  });

  it('moves by Alt + arrow past a hidden column, and not across the line between the blocks', async () => {
    const sent = mountAdapter(TLColumnSelect,
      { entries: [entry('a', true, true), entry('b', true), entry('h', false), entry('c', true)] });
    await settle();

    // Down from b: over c, which stands behind the hidden h.
    fireEvent.keyDown(row('b').querySelector('.tl-reorder-list__handle')!, { key: 'ArrowDown', altKey: true });
    await settle();
    expect(sent).toHaveBeenCalledWith(CMD_COLUMN_REORDER, { column: 'b', targetIndex: 3 });
    sent.mockClear();

    // Down from the last frozen column would enter the movable block; up from the first movable one would leave it.
    fireEvent.keyDown(row('a').querySelector('.tl-reorder-list__handle')!, { key: 'ArrowDown', altKey: true });
    fireEvent.keyDown(row('b').querySelector('.tl-reorder-list__handle')!, { key: 'ArrowUp', altKey: true });
    await settle();
    expect(sent).not.toHaveBeenCalled();
  });

  it('marks the last shown column: no pointer row, and the reason as tooltip on the row', async () => {
    mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('h', false)] });
    await settle();

    expect(row('a').getAttribute('data-tl-state')).toContain('last');
    expect(row('a').getAttribute('data-tooltip')).toBe('text:js.table.columnLastVisible');
    expect(row('h').getAttribute('data-tl-state') ?? '').not.toContain('last');
    expect(row('h').hasAttribute('data-tooltip')).toBe(false);
  });

  it('marks no column as the last one while two are shown', async () => {
    mountAdapter(TLColumnSelect, { entries: [entry('a', true), entry('b', true)] });
    await settle();

    expect(row('a').getAttribute('data-tl-state') ?? '').not.toContain('last');
    expect(row('a').hasAttribute('data-tooltip')).toBe(false);
  });
});

describe('a fast series of keyboard moves in the column selection', () => {
  const CMD_COLUMN_REORDER = 'columnReorder';

  function entry(name: string, locked = false) {
    return { name, label: 'Spalte ' + name, visible: true, locked, grouped: false };
  }

  function handle(name: string): HTMLElement {
    return screen.getByText('Spalte ' + name).closest('.tl-reorder-list__item')!
      .querySelector<HTMLElement>('.tl-reorder-list__handle')!;
  }

  /** The order the server sends after a move: the entries of the given names, in this order. */
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

  it('sends every step, each one from where the server put the column after the step before', async () => {
    lockedA = false;
    const sent = mountAdapter(TLColumnSelect, { entries: order('a', 'b', 'c', 'd') });
    await settle();
    handle('d').focus();

    up('d', 3);
    await settle();
    expect(sent.mock.calls).toEqual([[CMD_COLUMN_REORDER, { column: 'd', targetIndex: 2 }]]);

    patchState({ entries: order('a', 'b', 'd', 'c') });
    await settle();
    expect(sent.mock.calls.slice(1)).toEqual([[CMD_COLUMN_REORDER, { column: 'd', targetIndex: 1 }]]);

    patchState({ entries: order('a', 'd', 'b', 'c') });
    await settle();
    expect(sent.mock.calls.slice(2)).toEqual([[CMD_COLUMN_REORDER, { column: 'd', targetIndex: 0 }]]);

    patchState({ entries: order('d', 'a', 'b', 'c') });
    await settle();
    expect(sent).toHaveBeenCalledTimes(3);
    expect(document.activeElement).toBe(handle('d'));
  });

  it('drops the steps still queued when the server does not put the column where it was sent', async () => {
    lockedA = false;
    const sent = mountAdapter(TLColumnSelect, { entries: order('a', 'b', 'c', 'd') });
    await settle();
    handle('d').focus();

    up('d', 3);
    await settle();
    expect(sent).toHaveBeenCalledTimes(1);

    // The server refused: a new order arrives, d where it was.
    patchState({ entries: order('a', 'b', 'c', 'd', 'e') });
    await settle();
    expect(sent).toHaveBeenCalledTimes(1);

    // The next step starts afresh from where d stands.
    up('d', 1);
    await settle();
    expect(sent).toHaveBeenLastCalledWith(CMD_COLUMN_REORDER, { column: 'd', targetIndex: 2 });
    expect(sent).toHaveBeenCalledTimes(2);
  });

  it('stops the queued steps at the line between the frozen and the movable columns', async () => {
    lockedA = true;
    const sent = mountAdapter(TLColumnSelect, { entries: order('a', 'b', 'c', 'd') });
    await settle();
    handle('d').focus();

    up('d', 3);
    await settle();
    patchState({ entries: order('a', 'b', 'd', 'c') });
    await settle();
    patchState({ entries: order('a', 'd', 'b', 'c') });
    await settle();

    // The third step would move d into the frozen block.
    expect(sent.mock.calls).toEqual([
      [CMD_COLUMN_REORDER, { column: 'd', targetIndex: 2 }],
      [CMD_COLUMN_REORDER, { column: 'd', targetIndex: 1 }],
    ]);
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
      { groupable: true, entries: [entry('a', true, false), entry('b', false, false), entry('c', true, true)] });
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
      { groupable: true, entries: [entry('a', true, false), entry('c', true, true)] });
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
    mountAdapter(TLColumnSelect, { groupable: false, entries: [entry('a', true, false)] });
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
