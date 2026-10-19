// The views of wave 5c (spec 2026-10-19 "planen und ziehen"): calendar, Kanban board, dashboard. The test registry has
// no parts of its own, so the parts the controls look up are the MUI ones, as in containers.test.tsx and
// media.test.tsx; what is tested here is the behaviour of the controls and of the shared helpers, not the drawing of the
// parts.
//
// First the shared helpers (spec §8): one interactive-target check, one category mapping, the readable text on a data
// colour, the linear roving focus - and the table, whose clicks must behave exactly as before the interactive-target
// check was merged (review focus 5).

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  React, replacePart, register, isInteractiveTarget, isInteractiveWithin, isOperableTarget, categoryOf, textOn,
} from 'tl-react-bridge';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLValueList from '../../../com.top_logic.layout.react/react-src/controls/TLValueList';
import { useRovingFocus } from '../../../com.top_logic.layout.react/react-src/controls/useRovingFocus';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiChipAdapter from './MuiChipAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

// The table looks up the toggle of its tree rows, the selection boxes of its rows, the box of its search, the chips of
// its named filters and its buttons. Without them it throws ("No component registered for the part …") instead of
// failing an expectation.
replacePart('Button', MuiButtonAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
  document.documentElement.style.removeProperty('--tl-text-contrast-light');
  document.documentElement.style.removeProperty('--tl-text-contrast-dark');
});

/** The text roles textOn chooses between. */
const TEXT_LIGHT = 'var(--tl-text-contrast-light)';
const TEXT_DARK = 'var(--tl-text-contrast-dark)';

describe('the interactive-target check', () => {
  /** The elements {@link element} added to the page, removed after each test. */
  const hosts: HTMLElement[] = [];

  afterEach(() => {
    hosts.splice(0).forEach(host => host.remove());
  });

  function element(html: string): HTMLElement {
    const host = document.createElement('div');
    host.innerHTML = html;
    document.body.appendChild(host);
    hosts.push(host);
    return host.firstElementChild as HTMLElement;
  }

  it('counts the element itself and its ancestors as interactive', () => {
    const button = element('<button type="button"><span>Ok</span></button>');
    const plain = element('<div><span>Text</span></div>');

    expect(isInteractiveTarget(button)).toBe(true);
    expect(isInteractiveTarget(button.querySelector('span'))).toBe(true);
    expect(isInteractiveTarget(plain)).toBe(false);
    expect(isInteractiveTarget(plain.querySelector('span'))).toBe(false);
    expect(isInteractiveTarget(null)).toBe(false);
  });

  it('does not count an option itself as interactive within it', () => {
    const option = element('<div role="option"><span>Karte</span></div>');

    expect(isInteractiveWithin(option.querySelector('span'), option)).toBe(false);
    expect(isInteractiveTarget(option.querySelector('span'))).toBe(true);
  });

  it('counts an interactive element inside an option within it', () => {
    const option = element('<div role="option"><a href="#x"><span>Link</span></a></div>');

    expect(isInteractiveWithin(option.querySelector('span'), option)).toBe(true);
  });

  it('does not count a disabled or read-only element as operable', () => {
    expect(isOperableTarget(element('<button type="button" disabled>Aus</button>'))).toBe(false);
    expect(isOperableTarget(element('<input readonly>'))).toBe(false);
    expect(isOperableTarget(element('<div role="button" aria-disabled="true">Aus</div>'))).toBe(false);
    expect(isOperableTarget(element('<input type="checkbox" aria-readonly="true">'))).toBe(false);
    expect(isOperableTarget(element('<button type="button">An</button>'))).toBe(true);
    expect(isOperableTarget(element('<span>Text</span>'))).toBe(false);
  });
});

// A cell holding what a click must leave alone or may read as a click on the row: a link, an editor, a disabled button,
// plain text.
register('PlanenTestCell', () => (
  <div>
    <span>Text</span>
    <a href="#cell">Link</a>
    <input aria-label="Editor" />
    <button type="button" disabled>Aus</button>
  </div>
));

describe('the clicks of a table row (review focus 5: as before the merge of the interactive-target check)', () => {
  const CMD_SELECT = 'select';

  function mountTable() {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLTableView, {
      columns: [{ name: 'name', label: 'Name', width: 120, sortable: false }],
      totalRowCount: 1,
      selectionMode: 'single',
      rows: [{
        id: 'r0', index: 0, selected: false,
        cells: { name: { controlId: 'c0', module: 'PlanenTestCell', state: {} } },
      }],
    });
  }

  function selects(sent: ReturnType<typeof mountTable>) {
    return sent.mock.calls.filter(([command]) => command === CMD_SELECT);
  }

  it('selects the row by a click on its text', async () => {
    const sent = mountTable();
    await settle();

    await userEvent.click(screen.getByText('Text'));
    await settle();

    expect(selects(sent)).toEqual([[CMD_SELECT, { rowIndex: 0, ctrlKey: false, shiftKey: false }]]);
  });

  it('does not select the row by a click on a link in a cell', async () => {
    const sent = mountTable();
    await settle();

    await userEvent.click(screen.getByText('Link'));
    await settle();

    expect(selects(sent)).toEqual([]);
  });

  it('does not select the row by a click into a cell editor, which keeps the focus', async () => {
    const sent = mountTable();
    await settle();

    const editor = screen.getByRole('textbox', { name: 'Editor' });
    await userEvent.click(editor);
    await settle();

    expect(selects(sent)).toEqual([]);
    expect(document.activeElement).toBe(editor);
  });

  it('selects the row by a click on a disabled button in it', async () => {
    const sent = mountTable();
    await settle();

    fireEvent.click(screen.getByText('Aus'));
    await settle();

    expect(selects(sent)).toEqual([[CMD_SELECT, { rowIndex: 0, ctrlKey: false, shiftKey: false }]]);
  });
});

describe('the category of a name', () => {
  it('maps a name as the avatar did', () => {
    // The values of avatarCategory before it became categoryOf.
    expect(categoryOf('Alice')).toBe(1);
    expect(categoryOf('Bob')).toBe(6);
    expect(categoryOf('')).toBe(1);
  });
});

describe('the readable text on a data colour', () => {
  function candidates() {
    document.documentElement.style.setProperty('--tl-text-contrast-light', '#ffffff');
    document.documentElement.style.setProperty('--tl-text-contrast-dark', '#1b1d21');
  }

  it('chooses the dark text on white and the light text on a dark blue', () => {
    candidates();

    expect(textOn('#ffffff')).toBe(TEXT_DARK);
    expect(textOn('#fff')).toBe(TEXT_DARK);
    expect(textOn('#003366')).toBe(TEXT_LIGHT);
  });

  it('reads rgb() and colour names', () => {
    candidates();

    expect(textOn('rgb(0, 0, 0)')).toBe(TEXT_LIGHT);
    expect(textOn('rgba(255, 255, 255, 0.5)')).toBe(TEXT_DARK);
    expect([TEXT_LIGHT, TEXT_DARK]).toContain(textOn('red'));
  });

  it('answers nothing for a value that is no colour', () => {
    candidates();

    expect(textOn('kein-farbwert')).toBeNull();
    expect(textOn('')).toBeNull();
  });

  it('answers nothing without the candidates of the design system', () => {
    expect(textOn('#ffffff')).toBeNull();
  });
});

describe('the linear roving focus', () => {
  /** Lays the given element out at the given position (jsdom lays nothing out). */
  function place(element: HTMLElement, left: number, top: number) {
    vi.spyOn(element, 'getBoundingClientRect').mockReturnValue(
      { left, top, width: 50, height: 10, right: left + 50, bottom: top + 10, x: left, y: top, toJSON: () => ({}) });
  }

  function Items({ positions, linear }: { positions: [number, number][]; linear: boolean }) {
    const ref = React.useRef<HTMLDivElement | null>(null);
    const roving = useRovingFocus(ref, ':scope > [role="option"]', positions.length, -1, { linear });
    return (
      <div ref={ref} role="listbox" onKeyDown={roving.onKeyDown} onFocus={roving.onFocus}>
        {positions.map(([left, top], i) => (
          <div key={i} role="option" aria-selected={false} tabIndex={roving.tabIndexOf(i)}
            ref={element => { if (element) place(element, left, top); }}>
            {'Eintrag ' + i}
          </div>
        ))}
      </div>
    );
  }

  it('moves down from the first item to the second, standing one below the other', async () => {
    render(<Items positions={[[0, 0], [0, 20]]} linear />);

    screen.getByText('Eintrag 0').focus();
    await userEvent.keyboard('{ArrowDown}');

    expect(document.activeElement).toBe(screen.getByText('Eintrag 1'));
  });

  it('moves down and up in document order, whatever the layout', async () => {
    // The second item stands right of the first, the third below the first.
    render(<Items positions={[[0, 0], [100, 0], [0, 20]]} linear />);

    screen.getByText('Eintrag 0').focus();
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(screen.getByText('Eintrag 1'));

    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(screen.getByText('Eintrag 2'));

    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(screen.getByText('Eintrag 1'));
  });

  it('follows the layout without the option', async () => {
    render(<Items positions={[[0, 0], [100, 0], [0, 20]]} linear={false} />);

    screen.getByText('Eintrag 0').focus();
    await userEvent.keyboard('{ArrowDown}');

    expect(document.activeElement).toBe(screen.getByText('Eintrag 2'));
  });
});

describe('the value list (spec §7: tl-value-list)', () => {
  /** The name of the handle: the key, as the test stub translates each key to itself. */
  const LABEL_MOVE = 'js.valueList.move';
  const CMD_MOVE_ELEMENT = 'moveElement';

  function values() {
    return [childControl('v1', 'Eins', true), childControl('v2', 'Zwei', true)];
  }

  /** Whether any element of the page still carries a class of the old style sheet. */
  function legacyClasses() {
    return Array.from(document.querySelectorAll('*'))
      .flatMap(element => Array.from(element.classList))
      .filter(name => name.startsWith('tlValueList'));
  }

  it('reads as one text with the separator written out', async () => {
    mountAdapter(TLValueList, { elements: values(), editable: false, cssClass: 'kunde' });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.tagName).toBe('SPAN');
    expect(root.className).toBe('tl-value-list kunde');
    const separators = root.querySelectorAll(':scope > span.tl-select__separator');
    expect(separators).toHaveLength(1);
    expect(separators[0].textContent).toBe(', ');
    expect(root.querySelectorAll(':scope > span.tl-value-list__item')).toHaveLength(2);
    expect(legacyClasses()).toEqual([]);
  });

  it('marks the values on lines of their own as block', async () => {
    mountAdapter(TLValueList, { elements: values(), editable: false, layout: 'block' });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.className).toBe('tl-value-list tl-value-list--block');
    expect(root.querySelector(':scope > span.tl-select__separator')!.textContent).toBe(', ');
    expect(legacyClasses()).toEqual([]);
  });

  it('is a group of rows while edited, each ordered row with the handle as the part Button', async () => {
    mountAdapter(TLValueList, { elements: values(), editable: true, ordered: true });
    await settle();

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.tagName).toBe('DIV');
    expect(root.getAttribute('role')).toBe('group');
    expect(root.className).toBe('tl-value-list');
    expect(root.querySelectorAll(':scope > .tl-value-list__row')).toHaveLength(2);

    const handles = screen.getAllByRole('button', { name: LABEL_MOVE });
    expect(handles).toHaveLength(2);
    // The MUI part draws it: small, icon-only, no button of the control's own.
    handles.forEach(handle => {
      expect(handle.classList).toContain('MuiIconButton-root');
      expect(handle.closest('.tl-value-list__row')).not.toBeNull();
      expect(handle.getAttribute('aria-describedby')).toBeTruthy();
    });
    expect(legacyClasses()).toEqual([]);
  });

  it('moves a value down by Alt + arrow down on its handle', async () => {
    const sent = mountAdapter(TLValueList, { elements: values(), editable: true, ordered: true });
    await settle();

    const [first] = screen.getAllByRole('button', { name: LABEL_MOVE });
    first.focus();
    await userEvent.keyboard('{Alt>}{ArrowDown}{/Alt}');
    await settle();

    expect(sent).toHaveBeenCalledWith(CMD_MOVE_ELEMENT, { index: 0, targetIndex: 1 });
  });
});
