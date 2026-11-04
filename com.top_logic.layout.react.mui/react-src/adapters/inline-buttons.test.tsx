// The buttons a control draws inside itself - the header buttons of a panel, the column buttons of a
// table, the buttons of a value list, the close button of a drawer - drawn by the MUI adapter of the
// part Button: one replacement of the part reaches them all.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
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
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';
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
const LABEL_CHOOSE_COLUMNS = 'js.table.viewChooseColumns';
const LABEL_FILTER = 'js.table.filter';
const LABEL_FILTER_ACTIVE = 'js.table.filterActive';
const LABEL_ADD = 'js.valueList.add';
const LABEL_REMOVE = 'js.valueList.remove';
const LABEL_GROUP_BY = 'js.table.groupBy';
const LABEL_UNGROUP = 'js.table.ungroup';
const LABEL_OPEN_DRAWER = 'js.sidebar.openDrawer';
const LABEL_COLUMN_HIDE = 'js.table.columnHide';
const LABEL_COLUMN_SHOW_LOCK = 'js.table.columnShowLock';

/** The commands the controls send for their buttons. */
const CMD_TOGGLE_MINIMIZE = 'toggleMinimize';
const CMD_CLOSE = 'close';
const CMD_OPEN_COLUMN_SELECT = 'openColumnSelect';
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
    await userEvent.click(screen.getByRole('button', { name: LABEL_CHOOSE_COLUMNS }));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_COLUMN_SELECT, expect.anything());
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
