// The chips and badges a control draws - the toggles of an option chip field, the chosen values of a
// dropdown, the named filters of a table, the files of a file chip field, the values with a color
// role of a choice field that is not editable, a text drawn as a pill, the counts of the sidebar -
// drawn by the MUI adapters of the parts Chip and Badge: one replacement of a part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, within, act } from '@testing-library/react';
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
import MuiDividerAdapter from './MuiDividerAdapter';
import MuiProgressBarAdapter from './MuiProgressBarAdapter';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';
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
// The lines of the sidebar, the bar of a dropdown loading its options, the spinner of a toggle.
replacePart('Divider', MuiDividerAdapter);
replacePart('ProgressBar', MuiProgressBarAdapter);
replacePart('Spinner', MuiSpinnerAdapter);

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
      rows: [{ id: 'r0', index: 0, cells: { name: childControl('c0', 'Zeile') } }],
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

  it('hands the focus to the next filter of the user once a deleted one is gone', async () => {
    const OTHER = { id: 'other', label: 'Andere', deletable: true };
    mountTable({ namedFilters: [DECLARED, OWN, OTHER] });
    await settle();

    await userEvent.click(deleteIcon(chip('Meine'), LABEL_DELETE_FILTER));
    patchState({ namedFilters: [DECLARED, OTHER] });
    await settle();

    expect(document.activeElement).toBe(chip('Andere'));
  });

  it('offers no deletion of a declared filter', async () => {
    mountTable({});
    await settle();

    expect(chip('Offen').querySelector('.MuiChip-deleteIcon')).toBeNull();
  });
});

describe('the presets of a table (TLTableView) that do not fit in the row of the filter bar', () => {
  /** Eight presets, the sixth of the user's own. */
  const PRESETS = Array.from({ length: 8 }, (_, i) => ({ id: `p${i + 1}`, label: `Preset ${i + 1}`, deletable: i === 5 }));
  const CHIP_WIDTH = 100;
  const MORE_WIDTH = 80;
  const MORE_TEXT = 'js.table.morePresets';

  /** The width the filter bar has: what the test lays out, as jsdom lays out nothing. */
  let barWidth = 0;
  /** The callbacks of the resize observers the table has created. */
  let observers: Array<() => void> = [];

  class RecordingResizeObserver {
    constructor(callback: () => void) {
      observers.push(callback);
    }
    observe() {}
    unobserve() {}
    disconnect() {}
  }

  /** A bar with room for the given number of chips and the chip of the menu (and the pixel of rounding). */
  function roomFor(chips: number) {
    return chips * CHIP_WIDTH + MORE_WIDTH + 1;
  }

  function mountPresets(width: number, state: Record<string, unknown> = {}) {
    barWidth = width;
    observers = [];
    vi.stubGlobal('ResizeObserver', RecordingResizeObserver);
    vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
      let measured = 0;
      if (this.matches('.tl-table__filter-bar')) {
        measured = barWidth;
      } else if (this.matches('.tl-table__more')) {
        measured = MORE_WIDTH;
      } else if (this.hasAttribute('data-tl-preset')) {
        measured = CHIP_WIDTH;
      }
      return { x: 0, y: 0, top: 0, left: 0, right: measured, bottom: 0, width: measured, height: 0, toJSON() {} };
    });
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      rows: [{ id: 'r0', index: 0, cells: { name: childControl('c0', 'Zeile') } }],
      totalRowCount: 1,
      filterBar: true,
      namedFilters: PRESETS,
      ...state,
    });
  }

  /** The labels of the chips that stand in the row. */
  function rowLabels() {
    return Array.from(document.querySelectorAll('[data-tl-preset]')).map(element => element.textContent);
  }

  /** The chip of the menu. */
  function moreChip() {
    return document.querySelector<HTMLElement>('.tl-table__more');
  }

  /** The width of the bar changes: the resize observers report it. */
  function resizeTo(width: number) {
    barWidth = width;
    act(() => observers.forEach(callback => callback()));
  }

  /** Opens the menu of the presets and returns it. */
  async function openMenu() {
    await userEvent.click(moreChip()!);
    return screen.getByRole('menu');
  }

  /** The labels of the entries of the menu. */
  function entryLabels(menu: HTMLElement) {
    return within(menu).getAllByRole('menuitemradio').map(element => element.textContent);
  }

  it('shows the presets that fit and a chip "+ n more" for the rest, in declared order', async () => {
    mountPresets(roomFor(3));
    await settle();

    expect(rowLabels()).toEqual(['Preset 1', 'Preset 2', 'Preset 3']);
    expect(moreChip()!.textContent).toContain(MORE_TEXT);
    expect(moreChip()!.getAttribute('aria-haspopup')).toBe('menu');
    expect(moreChip()!.getAttribute('aria-expanded')).toBe('false');
    const menu = await openMenu();
    expect(moreChip()!.getAttribute('aria-expanded')).toBe('true');
    expect(entryLabels(menu)).toEqual(['Preset 4', 'Preset 5', 'Preset 6', 'Preset 7', 'Preset 8']);
  });

  it('shows no chip "+ n more" when all presets fit', async () => {
    mountPresets(8 * CHIP_WIDTH + 1);
    await settle();

    expect(rowLabels()).toHaveLength(8);
    expect(moreChip()).toBeNull();
  });

  it('keeps the active preset in the row: it takes the last place, the preset it displaces goes to the menu', async () => {
    mountPresets(roomFor(3), { activeNamedFilter: 'p7' });
    await settle();

    expect(rowLabels()).toEqual(['Preset 1', 'Preset 2', 'Preset 7']);
    expect(chip('Preset 7').getAttribute('aria-pressed')).toBe('true');
    const menu = await openMenu();
    expect(entryLabels(menu)).toEqual(['Preset 3', 'Preset 4', 'Preset 5', 'Preset 6', 'Preset 8']);
  });

  it('draws the entries of the menu as radio entries, none of them checked while the active preset stands in the row', async () => {
    mountPresets(roomFor(3), { activeNamedFilter: 'p7' });
    await settle();

    const entries = within(await openMenu()).getAllByRole('menuitemradio');
    expect(entries).toHaveLength(5);
    entries.forEach(entry => expect(entry.getAttribute('aria-checked')).toBe('false'));
  });

  it('applies a preset chosen in the menu, and closes the menu', async () => {
    const sent = mountPresets(roomFor(3));
    await settle();

    await userEvent.click(within(await openMenu()).getByRole('menuitemradio', { name: 'Preset 5' }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_APPLY_FILTER, { id: 'p5' }]]);
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('hands the focus to the preset chosen in the menu once it stands in the row', async () => {
    mountPresets(roomFor(3));
    await settle();

    await userEvent.click(within(await openMenu()).getByRole('menuitemradio', { name: 'Preset 5' }));
    patchState({ activeNamedFilter: 'p5' });
    await settle();

    expect(rowLabels()).toEqual(['Preset 1', 'Preset 2', 'Preset 5']);
    expect(document.activeElement).toBe(chip('Preset 5'));
  });

  it('deletes a preset of the user from the menu by its remove action', async () => {
    const sent = mountPresets(roomFor(3));
    await settle();

    const entry = within(await openMenu()).getByRole('group', { name: 'Preset 6' });
    await userEvent.click(within(entry).getByRole('button', { name: LABEL_DELETE_FILTER }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_DELETE_FILTER, { id: 'p6' }]]);
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('offers a remove action only for the presets of the user in the menu', async () => {
    mountPresets(roomFor(3));
    await settle();

    const menu = await openMenu();
    expect(within(menu).getAllByRole('button', { name: LABEL_DELETE_FILTER })).toHaveLength(1);
    expect(within(menu).getAllByRole('group')).toHaveLength(1);
  });

  it('deletes the focused preset of the user in the menu by the key Delete', async () => {
    const sent = mountPresets(roomFor(3));
    await settle();

    const entry = within(await openMenu()).getByRole('menuitemradio', { name: 'Preset 6' });
    expect(entry.getAttribute('aria-keyshortcuts')).toBe('Delete Backspace');
    entry.focus();
    await userEvent.keyboard('{Delete}');
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_DELETE_FILTER, { id: 'p6' }]]);
  });

  it('leaves the focus on the chip of the menu after a deletion from the menu, while presets are left in it', async () => {
    mountPresets(roomFor(3));
    await settle();

    const entry = within(await openMenu()).getByRole('group', { name: 'Preset 6' });
    await userEvent.click(within(entry).getByRole('button', { name: LABEL_DELETE_FILTER }));
    patchState({ namedFilters: PRESETS.filter(preset => preset.id !== 'p6') });
    await settle();

    expect(document.activeElement).toBe(moreChip());
  });

  it('hands the focus on after a deletion from the menu took the last preset out of it', async () => {
    const OWN_LAST = [...PRESETS.slice(0, 3), { id: 'p4', label: 'Preset 4', deletable: true }];
    mountPresets(roomFor(3), { namedFilters: OWN_LAST });
    await settle();

    const entry = within(await openMenu()).getByRole('group', { name: 'Preset 4' });
    await userEvent.click(within(entry).getByRole('button', { name: LABEL_DELETE_FILTER }));
    patchState({ namedFilters: PRESETS.slice(0, 3) });
    await settle();

    expect(moreChip()).toBeNull();
    expect(document.activeElement).not.toBe(document.body);
    expect(document.activeElement!.closest('.tl-table__filter-bar')).not.toBeNull();
  });

  it('cuts the row again when the width of the bar changes', async () => {
    mountPresets(roomFor(3));
    await settle();
    expect(rowLabels()).toHaveLength(3);

    resizeTo(roomFor(5));
    expect(rowLabels()).toEqual(['Preset 1', 'Preset 2', 'Preset 3', 'Preset 4', 'Preset 5']);
    expect(moreChip()).not.toBeNull();

    resizeTo(roomFor(1));
    expect(rowLabels()).toEqual(['Preset 1']);

    resizeTo(8 * CHIP_WIDTH + 1);
    expect(rowLabels()).toHaveLength(8);
    expect(moreChip()).toBeNull();
  });

  it('keeps the row it has when the bar is measured again without a change of width', async () => {
    mountPresets(roomFor(3), { activeNamedFilter: 'p8' });
    await settle();

    resizeTo(roomFor(3));
    resizeTo(roomFor(3));

    expect(rowLabels()).toEqual(['Preset 1', 'Preset 2', 'Preset 8']);
  });

  it('keeps the active preset in the row even with no room for a chip', async () => {
    mountPresets(10, { activeNamedFilter: 'p4' });
    await settle();

    expect(rowLabels()).toEqual(['Preset 4']);
    expect(await within(await openMenu()).findAllByRole('menuitemradio')).toHaveLength(7);
  });

  it('shows a preset added to the cut row, and cuts the row again', async () => {
    mountPresets(roomFor(3));
    await settle();

    patchState({ namedFilters: [...PRESETS, { id: 'p9', label: 'Preset 9', deletable: false }] });
    await settle();

    expect(rowLabels()).toEqual(['Preset 1', 'Preset 2', 'Preset 3']);
    expect(entryLabels(await openMenu())).toHaveLength(6);
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

  it('leads to a linked value on a press, a value with a color role and a text alike', async () => {
    const sent = mountValues([{ ...ALPHA, link: true }, { ...GAMMA, link: true }]);

    await userEvent.click(screen.getByRole('button', { name: 'Alpha' }));
    await userEvent.click(screen.getByRole('button', { name: 'Gamma' }));
    await settle();

    expect(screen.getByRole('button', { name: 'Alpha' }).classList).toContain('MuiLink-root');
    // The value of a color role is a link holding the chip of its role.
    const gamma = screen.getByRole('button', { name: 'Gamma' });
    expect(gamma.classList).toContain('MuiLink-root');
    expectMuiChip(gamma.querySelector('.MuiChip-root')!);
    expect(sent.mock.calls).toEqual([[CMD_GOTO, { [ARG_OPTION]: 'a' }], [CMD_GOTO, { [ARG_OPTION]: 'g' }]]);
  });
});

describe('a text drawn as a pill (TLText) as MUI chip', () => {
  it('draws the text as a chip of its color role inside the span of the control', () => {
    mountAdapter(TLText, { text: 'Erledigt', colorRole: 'success' });

    const text = document.getElementById(CONTROL_ID)!;
    expect(text.tagName).toBe('SPAN');
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

  /**
   * Asserts that the element with the given class around the badge places it over the corner of
   * the icon: MUI positions the root of its badge itself, and its rules win against the
   * stylesheets of TopLogic, so a class on the root could not move it there.
   */
  function expectPlacedByCorner(badge: Element, cornerClass: string) {
    expect(getComputedStyle(badge).position).toBe('relative');
    expect(badge.classList).not.toContain(cornerClass);
    expect(badge.parentElement!.classList).toContain(cornerClass);
  }

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
    expect(badge.classList).not.toContain('tl-sidebar__item');
    expect(badge.parentElement!.classList).toContain('tl-sidebar__item');
    expect(badge.querySelector('.MuiBadge-colorPrimary')).not.toBeNull();
  });

  it('draws the count over the icon of a collapsed sidebar', async () => {
    mountAdapter(TLSidebar, { items: [INBOX], activeItemId: 'inbox', collapsed: true, drawerOpen: false });
    await settle();

    expect(badges()).toEqual(['3']);
    expectPlacedByCorner(document.querySelector('.MuiBadge-root')!, 'tl-icon-badge__count');
  });

  it('draws the count of an item of the bottom bar, and none for an item without', () => {
    mountAdapter(TLBottomBar, { items: [INBOX, ARCHIVE], activeItemId: 'inbox' });

    expect(badges()).toEqual(['3']);
    expectPlacedByCorner(document.querySelector('.MuiBadge-root')!, 'tl-icon-badge__count');
  });
});
