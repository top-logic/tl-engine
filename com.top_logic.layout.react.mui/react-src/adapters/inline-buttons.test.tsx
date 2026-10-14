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
import MuiButtonAdapter from './MuiButtonAdapter';
import { childControl, mountAdapter, settle } from './wire-test-support';

replacePart('Button', MuiButtonAdapter);

/** The labels of the buttons: the keys, as the test stub translates each key to itself. */
const LABEL_MINIMIZE = 'js.panel.minimize';
const LABEL_MAXIMIZE = 'js.panel.maximize';
const LABEL_POP_OUT = 'js.panel.popOut';
const LABEL_DRAWER_CLOSE = 'js.drawer.close';
const LABEL_COLUMNS = 'js.table.columns';
const LABEL_FILTER = 'js.table.filter';
const LABEL_ADD = 'js.valueList.add';
const LABEL_REMOVE = 'js.valueList.remove';

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
      // The panel positions its buttons by its own class.
      expect(button.classList).toContain('tlPanel__actionButton');
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

  it('opens the filter of a column from its funnel, filled while the filter is in force', async () => {
    const sent = mountTable(true);
    await settle();

    const funnel = screen.getByRole('button', { name: LABEL_FILTER });
    expectMuiIconButton(funnel);
    // A dialog opener, no toggle: the filled funnel tells the filter in force, not a pressed state.
    expect(funnel.hasAttribute('aria-pressed')).toBe(false);
    expect(funnel.classList).toContain('tlTableView__filterButton--active');
    expect(funnel.querySelector('i.bi-funnel-fill')).not.toBeNull();
    await userEvent.click(funnel);
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_OPEN_FILTER, { column: 'name' });
  });

  it('shows the funnel of a column without filter empty', async () => {
    mountTable(false);
    await settle();

    const funnel = screen.getByRole('button', { name: LABEL_FILTER });
    expect(funnel.classList).not.toContain('tlTableView__filterButton--active');
    expect(funnel.querySelector('i.bi-funnel')).not.toBeNull();
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
