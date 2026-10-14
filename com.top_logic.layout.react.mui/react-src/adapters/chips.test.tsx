// The chips and badges a control draws - the toggles of an option chip field, the chosen values of a
// dropdown, the named filters of a table, the files of a file chip field, the values with a color
// role of a choice field that is not editable, a text drawn as a pill, the counts of the sidebar -
// drawn by the MUI adapters of the parts Chip and Badge: one replacement of a part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, within } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { CMD_VALUE_CHANGED, TOOLTIP_ATTR, fieldLabel, replacePart } from 'tl-react-bridge';
import TLOptionChips from '../../../com.top_logic.layout.react/react-src/controls/TLOptionChips';
import TLDropdownSelect from '../../../com.top_logic.layout.react/react-src/controls/TLDropdownSelect';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLFileChips from '../../../com.top_logic.layout.react/react-src/controls/TLFileChips';
import TLChoiceGroup from '../../../com.top_logic.layout.react/react-src/controls/TLChoiceGroup';
import TLText from '../../../com.top_logic.layout.react/react-src/controls/TLText';
import TLSidebar from '../../../com.top_logic.layout.react/react-src/controls/TLSidebar';
import TLBottomBar from '../../../com.top_logic.layout.react/react-src/controls/TLBottomBar';
import MuiChipAdapter from './MuiChipAdapter';
import MuiBadgeAdapter from './MuiBadgeAdapter';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiFieldValueAdapter from './MuiFieldValueAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import { CONTROL_ID, childControl, mountAdapter, patchState, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

replacePart('Chip', MuiChipAdapter);
replacePart('Badge', MuiBadgeAdapter);
// The read-only values without a color role, the buttons, toggles and search boxes of the controls.
replacePart('FieldValue', MuiFieldValueAdapter);
replacePart('Button', MuiButtonAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('TextInput', MuiTextInputAdapter);

/** Argument of {@link CMD_VALUE_CHANGED}: the values of the chosen options. */
const ARG_VALUE = 'value';

/** The command following the link of a displayed value, and its argument. */
const CMD_GOTO = 'goto';
const ARG_OPTION = 'option';

/** The ID of the form field around a control. */
const FIELD_ID = 'f1';

/** The names of the means removing a value: the keys, as the test stub translates each key to itself. */
const LABEL_REMOVE_VALUE = 'js.dropdownSelect.removeChip';
const LABEL_DELETE_FILTER = 'js.table.deleteFilter';
const LABEL_REMOVE_FILE = 'js.fileChips.remove';
const LABEL_ADD_FILE = 'js.fileChips.add';

/** The commands of the table for its named filters. */
const CMD_APPLY_FILTER = 'applyNamedFilter';
const CMD_CLEAR_FILTER = 'clearFilter';
const CMD_DELETE_FILTER = 'deleteNamedFilter';

/** The command of the file chips removing a file, and its argument. */
const CMD_REMOVE_FILE = 'removeChip';
const ARG_KEY = 'key';

const ALPHA = { value: 'a', label: 'Alpha' };
const BETA = { value: 'b', label: 'Beta', colorRole: 'success' };
const GAMMA = { value: 'g', label: 'Gamma', colorRole: 'error', image: 'css:fa-solid fa-star' };
const OPTIONS = [ALPHA, BETA];

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

/** The command changing the value of a choice field to the options of the given values. */
function changedTo(...values: string[]) {
  return [CMD_VALUE_CHANGED, { [ARG_VALUE]: values }];
}

/** The MUI chip that is a button of the given name. */
function chip(name: string) {
  const element = screen.getByRole('button', { name });
  expectMuiChip(element);
  return element;
}

/** Asserts that the given element is an MUI chip and not a chip or pill of TopLogic. */
function expectMuiChip(element: Element) {
  expect(element.classList).toContain('MuiChip-root');
  expect(element.classList).toContain('MuiChip-sizeSmall');
  expect(element.classList).not.toContain('tl-chip');
  expect(element.classList).not.toContain('tl-pill');
}

/** The delete icon of the given MUI chip, which shows the given name as its tooltip. */
function deleteIcon(element: Element, name: string) {
  expect(element.getAttribute('aria-keyshortcuts')).toBe('Backspace Delete');
  const icon = element.querySelector('.MuiChip-deleteIcon')!;
  expect(icon.getAttribute(TOOLTIP_ATTR)).toBe('text:' + name);
  expect(icon.getAttribute('aria-hidden')).toBe('true');
  return icon;
}

describe('the toggles of an option chip field (TLOptionChips) as MUI chips', () => {
  function mountChips(state: Record<string, unknown>, options?: MountOptions) {
    return mountAdapter(TLOptionChips, { options: OPTIONS, display: 'chips', ...state }, options);
  }

  it('renders a chip per option, a chosen one filled and pressed, in a labelled group', () => {
    mountChips({ value: [ALPHA] }, { fieldLabel: fieldLabel(FIELD_ID, CONTROL_ID, vi.fn()) });

    const group = screen.getByRole('group');
    expect(group.id).toBe(CONTROL_ID);
    expect(group.getAttribute('aria-labelledby')).toBe(FIELD_ID + '-label');
    expect(chip('Alpha').getAttribute('aria-pressed')).toBe('true');
    expect(chip('Alpha').classList).toContain('MuiChip-filled');
    expect(chip('Alpha').classList).toContain('MuiChip-colorPrimary');
    expect(chip('Beta').getAttribute('aria-pressed')).toBe('false');
    expect(chip('Beta').classList).toContain('MuiChip-outlined');
    expect(chip('Beta').classList).toContain('MuiChip-colorSuccess');
  });

  it('draws the image of an option as the icon of its chip', () => {
    mountChips({ value: [], options: [GAMMA] });

    expect(chip('Gamma').querySelector('.MuiChip-icon.fa-star')).not.toBeNull();
  });

  it('toggles the options of a multi-valued field, also before the server answers', async () => {
    const sent = mountChips({ value: [ALPHA], multiSelect: true });

    await userEvent.click(chip('Beta'));
    await userEvent.click(chip('Alpha'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a', 'b'), changedTo('b')]);
  });

  it('chooses one option of a single-valued field and clears it on a second press', async () => {
    const sent = mountChips({ value: [ALPHA] });

    await userEvent.click(chip('Beta'));
    await userEvent.click(chip('Beta'));
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('b'), changedTo()]);
  });

  it('keeps the option of a mandatory single-valued field on a second press', async () => {
    const sent = mountChips({ value: [ALPHA], mandatory: true });

    await userEvent.click(chip('Alpha'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('is pressed from the keyboard', async () => {
    const sent = mountChips({ value: [], multiSelect: true });

    await userEvent.tab();
    expect(document.activeElement).toBe(chip('Alpha'));
    await userEvent.keyboard(' ');
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a')]);
  });

  it('draws every chip in the color of the state of the value', () => {
    mountChips({ value: [ALPHA], hasError: true });

    expect(chip('Alpha').classList).toContain('MuiChip-colorError');
    expect(chip('Beta').classList).toContain('MuiChip-colorError');
  });

  it('shows the chosen values while read-only, without toggles', () => {
    mountChips({ value: [BETA], editable: false });

    expect(screen.queryByRole('group')).toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
    const values = document.getElementById(CONTROL_ID)!;
    expect(values.textContent).toBe('Beta');
    expectMuiChip(values.querySelector('.MuiChip-root')!);
  });

  it('sends nothing while disabled', async () => {
    const sent = mountChips({ value: [], editable: false, disabled: true });

    await userEvent.setup({ pointerEventsCheck: 0 }).click(chip('Alpha'));
    await settle();

    expect(chip('Alpha').classList).toContain('Mui-disabled');
    expect(sent).not.toHaveBeenCalled();
  });
});

describe('the chosen values of a dropdown (TLDropdownSelect) as MUI chips', () => {
  function mountDropdown(state: Record<string, unknown>) {
    return mountAdapter(TLDropdownSelect, {
      options: [ALPHA, BETA, GAMMA], optionsLoaded: true, multiSelect: true, ...state,
    });
  }

  /** The chips of the chosen values. */
  function valueChips() {
    return Array.from(document.getElementById(CONTROL_ID)!.querySelectorAll('.MuiChip-root'));
  }

  it('draws a chip per chosen value, with the color and the icon of its option', async () => {
    mountDropdown({ value: [ALPHA, GAMMA] });
    await settle();

    const chips = valueChips();
    expect(chips.map(element => element.querySelector('.MuiChip-label')!.textContent)).toEqual(['Alpha', 'Gamma']);
    chips.forEach(expectMuiChip);
    expect(chips[0].classList).toContain('MuiChip-colorDefault');
    expect(chips[1].classList).toContain('MuiChip-colorError');
    expect(chips[1].querySelector('.MuiChip-icon.fa-star')).not.toBeNull();
  });

  it('removes a value by the delete icon of its chip, without opening the list', async () => {
    const sent = mountDropdown({ value: [ALPHA, BETA] });
    await settle();

    const icon = deleteIcon(valueChips()[0], LABEL_REMOVE_VALUE);
    await userEvent.click(icon);
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('b')]);
    expect(screen.queryByRole('listbox')).toBeNull();
  });

  it('removes a value by Backspace on its focused chip', async () => {
    const sent = mountDropdown({ value: [ALPHA, BETA] });
    await settle();

    (valueChips()[1] as HTMLElement).focus();
    await userEvent.keyboard('{Backspace}');
    await settle();

    expect(sent.mock.calls).toEqual([changedTo('a')]);
  });

  it('focuses the next chip after a value is removed', async () => {
    mountDropdown({ value: [ALPHA, BETA] });
    await settle();

    await userEvent.click(deleteIcon(valueChips()[0], LABEL_REMOVE_VALUE));
    patchState({ value: [BETA] });
    await settle();

    expect(document.activeElement).toBe(valueChips()[0]);
  });

  it('offers no removal of a value while disabled', async () => {
    mountDropdown({ value: [ALPHA, BETA], disabled: true });
    await settle();

    expect(valueChips()).toHaveLength(2);
    valueChips().forEach(element => expect(element.querySelector('.MuiChip-deleteIcon')).toBeNull());
  });

  it('draws a single value without a color role as text, not as a chip', async () => {
    mountDropdown({ value: [ALPHA], multiSelect: false });
    await settle();

    expect(valueChips()).toHaveLength(0);
    expect(document.getElementById(CONTROL_ID)!.textContent).toContain('Alpha');
  });
});

describe('the named filters of a table (TLTableView) as MUI chips', () => {
  const OWN = { id: 'own', label: 'Meine', deletable: true };
  const DECLARED = { id: 'open', label: 'Offen', deletable: false };

  function mountTable(state: Record<string, unknown>) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows: [{ id: 'r0', index: 0, selected: false, cells: { name: childControl('c0', 'Zeile') } }],
      totalRowCount: 1,
      filterBar: true,
      namedFilters: [OWN, DECLARED],
      ...state,
    });
  }

  it('draws each filter as a toggle chip, pressed while it is applied', async () => {
    mountTable({ activeNamedFilter: 'open' });
    await settle();

    expect(chip('Meine').getAttribute('aria-pressed')).toBe('false');
    expect(chip('Meine').classList).toContain('MuiChip-outlined');
    expect(chip('Offen').getAttribute('aria-pressed')).toBe('true');
    expect(chip('Offen').classList).toContain('MuiChip-filled');
  });

  it('applies a filter, and clears the applied one on a second press', async () => {
    const sent = mountTable({ activeNamedFilter: 'open' });
    await settle();

    await userEvent.click(chip('Meine'));
    await userEvent.click(chip('Offen'));
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_APPLY_FILTER, { id: 'own' });
    expect(sent).toHaveBeenCalledWith(CMD_CLEAR_FILTER, {});
  });

  it('deletes a filter of the user by the delete icon, without applying it', async () => {
    const sent = mountTable({});
    await settle();

    const icon = deleteIcon(chip('Meine'), LABEL_DELETE_FILTER);
    await userEvent.click(icon);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_DELETE_FILTER, { id: 'own' }]]);
  });

  it('offers no deletion of a declared filter', async () => {
    mountTable({});
    await settle();

    expect(chip('Offen').querySelector('.MuiChip-deleteIcon')).toBeNull();
  });
});

describe('the files of a file chip field (TLFileChips) as MUI chips', () => {
  const REPORT = { key: 'k1', name: 'report.pdf', size: 2048, hasData: true };
  const PENDING = { key: 'k2', name: 'scan.png', size: null, hasData: false };

  function mountFiles(state: Record<string, unknown>) {
    return mountAdapter(TLFileChips, { chips: [REPORT, PENDING], ...state });
  }

  /** The chip showing the given file name. */
  function fileChip(name: string) {
    const element = screen.getByText(name).closest('.MuiChip-root')!;
    expectMuiChip(element);
    return element as HTMLElement;
  }

  it('draws a chip per file with its icon and its size', async () => {
    mountFiles({ editable: false });
    await settle();

    expect(fileChip('report.pdf').querySelector('.MuiChip-icon.fa-file')).not.toBeNull();
    expect(fileChip('report.pdf').textContent).toBe('report.pdf2.0 KB');
    expect(fileChip('scan.png').textContent).toBe('scan.png');
  });

  it('downloads a file with data on a press; a file without data is no button', async () => {
    mountFiles({ editable: false });
    await settle();

    expect(fileChip('report.pdf').getAttribute('role')).toBe('button');
    expect(fileChip('scan.png').getAttribute('role')).toBeNull();
    vi.spyOn(console, 'error').mockImplementation(() => {});
    await userEvent.click(fileChip('report.pdf'));
    await settle();

    const fetched = vi.mocked(fetch).mock.calls.map(([url]) => String(url));
    expect(fetched.some(url => url.includes('react-api/data') && url.endsWith('&key=k1'))).toBe(true);
  });

  it('removes a file by the delete icon of its chip while editable, and adds files by an MUI button', async () => {
    const sent = mountFiles({ editable: true });
    await settle();

    const icon = deleteIcon(fileChip('scan.png'), LABEL_REMOVE_FILE);
    await userEvent.click(icon);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_REMOVE_FILE, { [ARG_KEY]: 'k2' }]]);
    const add = screen.getByRole('button', { name: LABEL_ADD_FILE });
    expect(add.classList).toContain('MuiButton-root');
    expect(add.classList).toContain('tlFileChips__add');
  });

  it('offers neither removing nor adding files while not editable', async () => {
    mountFiles({ editable: false });
    await settle();

    expect(document.querySelector('.MuiChip-deleteIcon')).toBeNull();
    expect(screen.queryByRole('button', { name: LABEL_ADD_FILE })).toBeNull();
  });
});

describe('the chosen values of a choice field that is not editable', () => {
  function mountValues(value: unknown[]) {
    return mountAdapter(TLChoiceGroup, { options: [], value, editable: false });
  }

  it('draws a value with a color role as an MUI chip, any other as MUI text, separated by a comma', async () => {
    mountValues([ALPHA, { ...ALPHA, value: 'a2', label: 'Delta' }, GAMMA]);

    const values = document.getElementById(CONTROL_ID)!;
    expect(values.classList).toContain('tl-select__values');
    expect(values.textContent).toBe('Alpha, DeltaGamma');
    expectMuiChip(within(values).getByText('Gamma').closest('.MuiChip-root')!);
    expect(within(values).getByText('Alpha').closest('.MuiTypography-root')).not.toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('leads to a linked value on a press, a chip and a text alike', async () => {
    const sent = mountValues([{ ...ALPHA, link: true }, { ...GAMMA, link: true }]);

    await userEvent.click(screen.getByRole('button', { name: 'Alpha' }));
    await userEvent.click(chip('Gamma'));
    await settle();

    expect(screen.getByRole('button', { name: 'Alpha' }).classList).toContain('MuiLink-root');
    expect(sent.mock.calls).toEqual([[CMD_GOTO, { [ARG_OPTION]: 'a' }], [CMD_GOTO, { [ARG_OPTION]: 'g' }]]);
  });
});

describe('a text drawn as a pill (TLText) as MUI chip', () => {
  it('draws the text as a chip of its color role inside the span of the control', () => {
    mountAdapter(TLText, { text: 'Erledigt', colorRole: 'success' });

    const text = document.getElementById(CONTROL_ID)!;
    expect(text.classList).toContain('tlText');
    expectMuiChip(text.querySelector('.MuiChip-root')!);
    expect(text.querySelector('.MuiChip-colorSuccess')!.textContent).toBe('Erledigt');
  });

  it('draws a text without a color role as text', () => {
    mountAdapter(TLText, { text: 'Erledigt' });

    expect(document.querySelector('.MuiChip-root')).toBeNull();
  });
});

describe('the counts of the navigation as MUI badges', () => {
  const INBOX = { id: 'inbox', type: 'nav', label: 'Eingang', icon: 'css:fa-solid fa-inbox', badge: '3' };
  const ARCHIVE = { id: 'archive', type: 'nav', label: 'Archiv', icon: 'css:fa-solid fa-box' };

  /** The MUI badges of the page with their content. */
  function badges() {
    return Array.from(document.querySelectorAll('.MuiBadge-root')).map(badge => {
      expect(badge.classList).not.toContain('tl-badge');
      return badge.querySelector('.MuiBadge-badge')!.textContent;
    });
  }

  it('draws the count of a sidebar item as a badge beside its label', async () => {
    mountAdapter(TLSidebar, { items: [INBOX, ARCHIVE], activeItemId: 'archive', collapsed: false, drawerOpen: false });
    await settle();

    expect(badges()).toEqual(['3']);
    const badge = document.querySelector('.MuiBadge-root')!;
    expect(badge.classList).toContain('tlSidebar__badge');
    expect(badge.querySelector('.MuiBadge-colorPrimary')).not.toBeNull();
  });

  it('draws the count over the icon of a collapsed sidebar', async () => {
    mountAdapter(TLSidebar, { items: [INBOX], activeItemId: 'inbox', collapsed: true, drawerOpen: false });
    await settle();

    expect(badges()).toEqual(['3']);
    expect(document.querySelector('.MuiBadge-root')!.classList).toContain('tlSidebar__badge--collapsed');
  });

  it('draws the count of an item of the bottom bar, and none for an item without', () => {
    mountAdapter(TLBottomBar, { items: [INBOX, ARCHIVE], activeItemId: 'inbox' });

    expect(badges()).toEqual(['3']);
    expect(document.querySelector('.MuiBadge-root')!.classList).toContain('tlBottomBar__badge');
  });
});
