// The buttons a control draws inside itself - the header buttons of a panel, the column buttons of a
// table, the buttons of a value list, the close button of a drawer - drawn by the MUI adapter of the
// part Button: one replacement of the part reaches them all.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLPanel from '../../../com.top_logic.layout.react/react-src/controls/TLPanel';
import TLDrawer from '../../../com.top_logic.layout.react/react-src/controls/TLDrawer';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLValueList from '../../../com.top_logic.layout.react/react-src/controls/TLValueList';
import TLColumnSelect from '../../../com.top_logic.layout.react/react-src/controls/TLColumnSelect';
import TLDrawerToggle from '../../../com.top_logic.layout.react/react-src/controls/TLDrawerToggle';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';
import MuiChipAdapter from './MuiChipAdapter';

replacePart('Button', MuiButtonAdapter);
// The table looks up the toggle of its tree rows as well.
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
// The table looks up the selection boxes of its rows as well.
replacePart('Checkbox', MuiCheckboxAdapter);
// The table looks up the boxes of its search as well.
replacePart('TextInput', MuiTextInputAdapter);
// The table draws its named filters as chips.
replacePart('Chip', MuiChipAdapter);

/** The labels of the buttons: the keys, as the test stub translates each key to itself. */
const LABEL_MINIMIZE = 'js.panel.minimize';
const LABEL_MAXIMIZE = 'js.panel.maximize';
const LABEL_POP_OUT = 'js.panel.popOut';
const LABEL_DRAWER_CLOSE = 'js.drawer.close';
const LABEL_COLUMNS = 'js.table.columns';
const LABEL_FILTER = 'js.table.filter';
const LABEL_FILTER_ACTIVE = 'js.table.filterActive';
const LABEL_ADD = 'js.valueList.add';
const LABEL_REMOVE = 'js.valueList.remove';
const LABEL_GROUP_BY = 'js.table.groupBy';
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
    });
  }

  it('opens the column selection from the gear', async () => {
    const sent = mountTable(false);
    await settle();

    const gear = screen.getByRole('button', { name: LABEL_COLUMNS });
    expectMuiIconButton(gear);
    await userEvent.click(gear);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_COLUMN_SELECT, expect.anything());
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

  it('offers no grouping in the dialog: grouping rows stays in the header menu (#29825)', async () => {
    mountAdapter(TLColumnSelect, { entries: [entry('a', true, false)] });
    await settle();

    expect(screen.queryByRole('button', { name: LABEL_GROUP_BY })).toBeNull();
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
