// The views of wave 5c (spec 2026-10-19 "planen und ziehen"): calendar, Kanban board, dashboard. The test registry has
// no parts of its own, so the parts the controls look up are the MUI ones, as in containers.test.tsx and
// media.test.tsx; what is tested here is the behaviour of the controls and of the shared helpers, not the drawing of the
// parts.
//
// First the shared helpers (spec §8): one interactive-target check, one category mapping, the readable text on a data
// colour, the linear roving focus - and the table, whose clicks must behave exactly as before the interactive-target
// check was merged (review focus 5).

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, createEvent, fireEvent, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {
  React, replacePart, register, isInteractiveTarget, isInteractiveWithin, isOperableTarget, categoryOf, textOn,
  TOOLTIP_ATTR, useTLState, readDragPayload, LONG_PRESS_EVENT, boundaryAt,
} from 'tl-react-bridge';
import { clearI18NCache } from '../../../com.top_logic.layout.react/react-src/bridge/i18n';
import TLTableView from '../../../com.top_logic.layout.react/react-src/controls/TLTableView';
import TLValueList from '../../../com.top_logic.layout.react/react-src/controls/TLValueList';
import TLDashboard from '../../../com.top_logic.layout.react/react-src/controls/TLDashboard';
import TLKanbanBoard from '../../../com.top_logic.layout.react/react-src/controls/TLKanbanBoard';
import TLCalendar from '../../../com.top_logic.layout.react/react-src/controls/TLCalendar';
import TLWindow from '../../../com.top_logic.layout.react/react-src/controls/TLWindow';
import BadgePart from '../../../com.top_logic.layout.react/react-src/controls/chip/BadgePart';
import { useRovingFocus } from '../../../com.top_logic.layout.react/react-src/controls/useRovingFocus';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiCheckboxAdapter from './MuiCheckboxAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiChipAdapter from './MuiChipAdapter';
import { CONTROL_ID, childControl, mountAdapter, patchState, settle } from './wire-test-support';

// The table looks up the toggle of its tree rows, the selection boxes of its rows, the box of its search, the chips of
// its named filters and its buttons. Without them it throws ("No component registered for the part …") instead of
// failing an expectation.
replacePart('Button', MuiButtonAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Checkbox', MuiCheckboxAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Chip', MuiChipAdapter);
// The counter of a Kanban column is the part Badge; its TopLogic drawing is the badge of the design system (tl-badge),
// which the board's test asks for.
replacePart('Badge', BadgePart);

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

  /** Items keyed by their names, in the given order. */
  function Keyed({ order }: { order: string[] }) {
    const ref = React.useRef<HTMLDivElement | null>(null);
    const roving = useRovingFocus(ref, ':scope > [role="option"]', order.length, -1);
    return (
      <div ref={ref} role="listbox" onKeyDown={roving.onKeyDown} onFocus={roving.onFocus}>
        {order.map((name, i) => (
          <div key={name} role="option" aria-selected={false} tabIndex={roving.tabIndexOf(i)}>{name}</div>
        ))}
      </div>
    );
  }

  it('keeps the tab stop on the focused item when React moves it to another position', () => {
    const { rerender } = render(<Keyed order={['A', 'B', 'C']} />);
    screen.getByText('A').focus();
    expect(screen.getByText('A').tabIndex).toBe(0);

    // React moves the node of A behind C and gives it the focus back, without a focus event.
    rerender(<Keyed order={['B', 'C', 'A']} />);

    expect(document.activeElement).toBe(screen.getByText('A'));
    expect(screen.getByText('A').tabIndex).toBe(0);
    expect(screen.getByText('B').tabIndex).toBe(-1);
    expect(screen.getByText('C').tabIndex).toBe(-1);
  });
});

describe('the value list (spec §7: tl-value-list)', () => {
  /** The name of the handle: the key, as the test stub translates each key to itself. */
  const LABEL_MOVE = 'js.valueList.move';
  const CMD_MOVE_ELEMENT = 'moveElement';

  function values() {
    return [childControl('v1', 'Eins', true), childControl('v2', 'Zwei', true)];
  }

  /** The classes of the page that are not package classes: every class starting with `tl` is a `tl-…` class. */
  function legacyClasses() {
    return Array.from(document.querySelectorAll('*'))
      .flatMap(element => Array.from(element.classList))
      .filter(name => name.startsWith('tl') && !name.startsWith('tl-'));
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

describe('the dashboard (spec §6: tl-dashboard)', () => {
  const CMD_ACTIVATE = 'activate';
  const CMD_HIDE = 'hide';
  const CMD_REORDER = 'reorder';
  /** The name of the hide button: the key, as the test stub translates each key to itself. */
  const LABEL_HIDE = 'js.dashboard.hideTile';

  /** The style sheets a test added, removed after it. */
  const sheets: HTMLStyleElement[] = [];

  afterEach(() => {
    sheets.splice(0).forEach(sheet => sheet.remove());
    // The English texts of a test must not reach the next one; the dashboard is unmounted first, so that no hook of it
    // answers the cleared cache.
    cleanup();
    clearI18NCache();
  });

  /** Three tiles: one with an action, one without, one whose action is refused. */
  function tiles(order = ['t1', 't2', 't3']) {
    const all: Record<string, unknown> = {
      t1: {
        id: 't1', width: 'half', rowSpan: 1, control: childControl('c-t1', 'Projekte'),
        action: { label: 'Projekte öffnen', disabled: false, tooltip: 'Zu den Projekten' },
      },
      t2: { id: 't2', width: 'half', rowSpan: 1, control: childControl('c-t2', 'Notizen') },
      t3: {
        id: 't3', width: 'half', rowSpan: 1, control: childControl('c-t3', 'Berichte'),
        action: { label: 'Berichte öffnen', disabled: true },
      },
    };
    return order.map(id => all[id]);
  }

  function mountDashboard(editMode: boolean) {
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    return mountAdapter(TLDashboard, { children: tiles(), editMode });
  }

  function root() {
    return document.getElementById(CONTROL_ID)!;
  }

  function grid() {
    return root().querySelector<HTMLElement>(':scope > .tl-dashboard__grid')!;
  }

  function tileElements() {
    return Array.from(grid().querySelectorAll<HTMLElement>(':scope > .tl-dashboard__tile'));
  }

  /** The tile showing the control with the given ID. */
  function tileOf(controlId: string) {
    return document.getElementById(controlId)!.closest<HTMLElement>('.tl-dashboard__tile')!;
  }

  function sentOf(sent: ReturnType<typeof mountDashboard>, command: string) {
    return sent.mock.calls.filter(([name]) => name === command);
  }

  /** The classes of the page that are not package classes: every class starting with `tl` is a `tl-…` class. */
  function legacyClasses() {
    return Array.from(document.querySelectorAll('*'))
      .flatMap(element => Array.from(element.classList))
      .filter(name => name.startsWith('tl') && !name.startsWith('tl-'));
  }

  it('shows the tiles as cards with their content as the base of an overlay', async () => {
    mountDashboard(false);
    await settle();

    expect(root().classList).toContain('tl-dashboard');
    expect(root().classList).toContain('tl-inset');
    expect(root().hasAttribute('data-tl-state')).toBe(false);
    expect(grid().getAttribute('data-tl-axis')).toBe('horizontal');

    const shown = tileElements();
    expect(shown).toHaveLength(3);
    shown.forEach(tile => {
      expect(tile.tagName).toBe('DIV');
      ['tl-card', 'tl-dashboard__tile', 'tl-overlay', 'tl-overlay--fill'].forEach(name => expect(tile.classList).toContain(name));
      expect(tile.querySelector(':scope > .tl-overlay__base')).not.toBeNull();
      expect(tile.getAttribute('draggable')).not.toBe('true');
      expect(tile.hasAttribute('tabindex')).toBe(false);
      expect(tile.hasAttribute('role')).toBe(false);
      // No cover and no hide button outside of editing.
      expect(tile.querySelector('.tl-overlay__layer')).toBeNull();
    });
    expect(shown[0].querySelector(':scope > .tl-overlay__base #c-t1')).not.toBeNull();
    // The tooltip of the action is the one of the tile, which the pointer reaches.
    expect(shown[0].getAttribute(TOOLTIP_ATTR)).toBe('text:Zu den Projekten');
    expect(legacyClasses()).toEqual([]);
  });

  it('spaces the tiles by the token space-md, not by a fixed length', async () => {
    // jsdom inherits no custom property, so the token is declared where the dashboard reads it.
    const sheet = document.createElement('style');
    sheet.textContent = '.tl-dashboard { --tl-space-md: 20px; }';
    document.head.appendChild(sheet);
    sheets.push(sheet);
    mountDashboard(false);
    await settle();

    expect(grid().style.gap).toBe('20px');
  });

  it('names the action of a tile by a hidden button directly in the tile, which Enter activates', async () => {
    const sent = mountDashboard(false);
    await settle();

    const action = screen.getByRole('button', { name: 'Projekte öffnen' });
    expect(action.classList).toContain('tl-dashboard__action');
    expect(action.classList).toContain('tl-visually-hidden');
    // The ring of the tile is drawn for a focused button that is a direct child of the tile (base.css).
    expect(action.parentElement).toBe(tileOf('c-t1'));

    action.focus();
    await userEvent.keyboard('{Enter}');
    await settle();

    expect(sentOf(sent, CMD_ACTIVATE)).toEqual([[CMD_ACTIVATE, { tileId: 't1' }]]);
  });

  it('activates a tile by a click on its content', async () => {
    const sent = mountDashboard(false);
    await settle();

    await userEvent.click(screen.getByText('Projekte'));
    await settle();

    expect(sentOf(sent, CMD_ACTIVATE)).toEqual([[CMD_ACTIVATE, { tileId: 't1' }]]);
  });

  it('does not activate a tile whose action is refused', async () => {
    const sent = mountDashboard(false);
    await settle();

    const action = screen.getByRole('button', { name: 'Berichte öffnen' });
    expect((action as HTMLButtonElement).disabled).toBe(true);
    expect(action.parentElement).toBe(tileOf('c-t3'));

    fireEvent.click(screen.getByText('Berichte'));
    fireEvent.click(action);
    await settle();

    expect(sentOf(sent, CMD_ACTIVATE)).toEqual([]);
  });

  it('moves no tile by Alt + arrow outside of editing', async () => {
    const sent = mountDashboard(false);
    await settle();

    const second = tileOf('c-t2');
    fireEvent.keyDown(second, { key: 'ArrowRight', altKey: true });
    await settle();

    expect(sentOf(sent, CMD_REORDER)).toEqual([]);
  });

  it('marks the root while edited, makes each tile focusable and covers its content', async () => {
    mountDashboard(true);
    await settle();

    expect(root().getAttribute('data-tl-state')).toBe('edit');
    expect(grid().getAttribute('data-tl-axis')).toBe('horizontal');
    tileElements().forEach(tile => {
      expect(tile.tabIndex).toBe(0);
      expect(tile.getAttribute('role')).toBe('group');
      expect(tile.getAttribute('aria-label')).toBeTruthy();
      expect(tile.getAttribute('draggable')).toBe('true');
      expect(tile.getAttribute('aria-describedby')).toBeTruthy();
      const layers = Array.from(tile.querySelectorAll(':scope > .tl-overlay__layer'));
      expect(layers).toHaveLength(2);
      // The cover lies below the layer of the hide button: it comes first.
      expect(layers[0].classList).toContain('tl-overlay__layer--fill');
      expect(layers[0].querySelector(':scope > .tl-dashboard__cover')).not.toBeNull();
      expect(layers[1].classList).toContain('tl-overlay__layer--top-right');
    });
    // The action steps out of the tab order while the tiles are arranged.
    expect(screen.getByRole('button', { name: 'Projekte öffnen' }).tabIndex).toBe(-1);
    expect(legacyClasses()).toEqual([]);
  });

  it('hides a tile by the part Button in the action surface over the tile', async () => {
    const sent = mountDashboard(true);
    await settle();

    const hide = screen.getAllByRole('button', { name: LABEL_HIDE });
    expect(hide).toHaveLength(3);
    const second = hide[1];
    expect(second.classList).toContain('MuiIconButton-root');
    expect(second.parentElement!.classList).toContain('tl-overlay__action');
    expect(second.parentElement!.parentElement!.classList).toContain('tl-overlay__layer--top-right');
    expect(second.closest('.tl-dashboard__tile')).toBe(tileOf('c-t2'));

    await userEvent.click(second);
    await settle();

    expect(sentOf(sent, CMD_HIDE)).toEqual([[CMD_HIDE, { tileId: 't2' }]]);
    expect(sentOf(sent, CMD_ACTIVATE)).toEqual([]);
  });

  it('moves a tile one place on by Alt + arrow right, announces its position and keeps its focus', async () => {
    clearI18NCache();
    const sent = mountDashboard(true);
    englishTexts({ 'js.reorder.moved': '{0}, position {1} of {2}', 'js.dashboard.tile': 'Tile {0}' });
    await settle();

    tileOf('c-t2').focus();
    await userEvent.keyboard('{Alt>}{ArrowRight}{/Alt}');
    await settle();

    expect(sentOf(sent, CMD_REORDER)).toEqual([[CMD_REORDER, { order: ['t1', 't3', 't2'] }]]);
    expect(root().querySelector('[role="status"]')!.textContent).toBe('Tile 2, position 3 of 3');

    patchState({ children: tiles(['t1', 't3', 't2']) });
    await settle();

    expect(tileElements()[2]).toBe(tileOf('c-t2'));
    expect(document.activeElement).toBe(tileOf('c-t2'));
  });

  it('announces a tile with an action by the name of its action', async () => {
    clearI18NCache();
    const sent = mountDashboard(true);
    englishTexts({ 'js.reorder.moved': '{0}, position {1} of {2}', 'js.dashboard.tile': 'Tile {0}' });
    await settle();

    tileOf('c-t1').focus();
    await userEvent.keyboard('{Alt>}{ArrowRight}{/Alt}');
    await settle();

    expect(sentOf(sent, CMD_REORDER)).toEqual([[CMD_REORDER, { order: ['t2', 't1', 't3'] }]]);
    expect(root().querySelector('[role="status"]')!.textContent).toBe('Projekte öffnen, position 2 of 3');
  });

  it('moves no tile past the start of the dashboard', async () => {
    const sent = mountDashboard(true);
    await settle();

    tileOf('c-t1').focus();
    await userEvent.keyboard('{Alt>}{ArrowLeft}{/Alt}');
    await settle();

    expect(sentOf(sent, CMD_REORDER)).toEqual([]);
    expect(document.activeElement).toBe(tileOf('c-t1'));
  });
});

describe('the nearest boundary between the items of a vertical list', () => {
  /** An item laid out from the given top, 40 pixels high (jsdom lays nothing out). */
  function item(top: number): Element {
    const element = document.createElement('div');
    vi.spyOn(element, 'getBoundingClientRect').mockReturnValue(
      { left: 0, top, width: 100, height: 40, right: 100, bottom: top + 40, x: 0, y: top, toJSON: () => ({}) });
    return element;
  }

  it('lies before the first item whose middle is below the pointer, on an item or in a gap', () => {
    const items = [item(0), item(50), item(100)];

    expect(boundaryAt(5, items)).toEqual({ item: items[0], side: 'before' });
    // The lower half of the first item: the boundary before the second one.
    expect(boundaryAt(30, items)).toEqual({ item: items[1], side: 'before' });
    // The gap between the second and the third item.
    expect(boundaryAt(95, items)).toEqual({ item: items[2], side: 'before' });
  });

  it('lies after the last item below the middle of every item, and nowhere without items', () => {
    const items = [item(0), item(50)];

    expect(boundaryAt(80, items)).toEqual({ item: items[1], side: 'after' });
    expect(boundaryAt(500, items)).toEqual({ item: items[1], side: 'after' });
    expect(boundaryAt(5, [])).toBeNull();
  });
});

// A card of a Kanban board: its title, and a button of its own, which a click must be left to (review focus 1).
register('PlanenTestCard', () => {
  const state = useTLState() as { text?: string };
  return (
    <div>
      <span>{state.text}</span>
      <button type="button">{'Details ' + state.text}</button>
    </div>
  );
});

describe('the Kanban board (spec §5: tl-board)', () => {
  const CMD_SELECT_CARD = 'selectCard';
  const CMD_DROP = 'drop';
  const CMD_DROP_PROBE = 'dropProbe';
  /** The English text of the announcement of a moved card. */
  const MOVED = 'Moved to {0}, position {1} of {2}.';

  afterEach(() => {
    // A drag a test left running ends, so that it does not reach the next test.
    window.dispatchEvent(new Event('dragend'));
    // The English texts of a test must not reach the next one; the board is unmounted first, so that no hook of it
    // answers the cleared cache.
    cleanup();
    clearI18NCache();
  });

  /** The columns of a board, each named by its key and holding the cards with the given keys. */
  function columnsOf(columns: Record<string, string[]>) {
    return Object.entries(columns).map(([key, cards]) => ({
      key,
      label: 'Spalte ' + key,
      count: cards.length,
      cards: cards.map(card => ({
        key: card,
        content: { controlId: 'c-' + card, module: 'PlanenTestCard', state: { text: card } },
        draggable: true,
      })),
    }));
  }

  /** The state of a board whose cards are dragged and reordered, with the given columns. */
  function board(columns: Record<string, string[]>, extra: Record<string, unknown> = {}) {
    return {
      columns: columnsOf(columns),
      selected: [],
      multiSelect: false,
      dragEnabled: true,
      dragKind: 'ticket',
      dropAcceptsAny: false,
      dropAccepts: ['ticket'],
      reorder: true,
      dropVerdicts: {},
      ...extra,
    };
  }

  function root() {
    return document.getElementById(CONTROL_ID)!;
  }

  function columnElements() {
    return Array.from(root().querySelectorAll<HTMLElement>(':scope > section.tl-board__column'));
  }

  function listOf(column: HTMLElement) {
    return column.querySelector<HTMLElement>('.tl-board__cards')!;
  }

  /** The card showing the object with the given key. */
  function cardOf(key: string) {
    return screen.getByText(key, { selector: 'span' }).closest<HTMLElement>('[role="option"]')!;
  }

  function live() {
    return root().querySelector<HTMLElement>('.tl-board__live')!;
  }

  function sentOf(sent: ReturnType<typeof mountAdapter>, command: string) {
    return sent.mock.calls.filter(([name]) => name === command);
  }

  /** The classes of the page that are not package classes: every class starting with `tl` is a `tl-…` class. */
  function legacyClasses() {
    return Array.from(document.querySelectorAll('*'))
      .flatMap(element => Array.from(element.classList))
      .filter(name => name.startsWith('tl') && !name.startsWith('tl-'));
  }

  /** Focuses the card with the given key and presses Alt with the given arrow key on it. */
  async function altArrow(key: string, arrow: string) {
    cardOf(key).focus();
    await userEvent.keyboard('{Alt>}{' + arrow + '}{/Alt}');
    await settle();
  }

  /** A data transfer as the browser hands it to the events of one drag (jsdom has none). */
  function transfer() {
    const data = new Map<string, string>();
    const types: string[] = [];
    return {
      types,
      effectAllowed: 'all',
      dropEffect: 'none',
      setData(type: string, value: string) {
        data.set(type, value);
        if (!types.includes(type)) {
          types.push(type);
        }
      },
      getData(type: string) {
        return data.get(type) ?? '';
      },
      setDragImage() {},
    };
  }

  /** Lays the cards with the given keys out one below the other: 40 pixels high, 10 pixels apart. */
  function layOut(keys: string[]) {
    keys.forEach((key, i) => {
      const top = i * 50;
      vi.spyOn(cardOf(key), 'getBoundingClientRect').mockReturnValue(
        { left: 0, top, width: 200, height: 40, right: 200, bottom: top + 40, x: 0, y: top, toJSON: () => ({}) });
    });
  }

  /** Fires a drag event of the given type at the given element, the pointer at the given height. */
  function dragAt(type: 'dragOver' | 'drop', element: Element, clientY: number, data: ReturnType<typeof transfer>) {
    const event = createEvent[type](element, { dataTransfer: data });
    Object.defineProperty(event, 'clientX', { value: 10 });
    Object.defineProperty(event, 'clientY', { value: clientY });
    fireEvent(element, event);
  }

  /** Starts dragging the card with the given key and waits until the browser took its image. */
  async function startDrag(key: string) {
    const data = transfer();
    fireEvent.dragStart(cardOf(key), { dataTransfer: data });
    await new Promise(resolve => setTimeout(resolve, 0));
    await settle();
    return data;
  }

  it('shows each column as a section with a list box of cards and the count as a badge', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'], B: ['b1', 'b2'], C: [] }));
    await settle();

    expect(root().classList).toContain('tl-board');
    const sections = columnElements();
    expect(sections).toHaveLength(3);
    sections.forEach((section, i) => {
      const label = document.getElementById(section.getAttribute('aria-labelledby')!)!;
      expect(label.textContent).toBe('Spalte ' + ['A', 'B', 'C'][i]);
      expect(label.classList).toContain('tl-board__label');
      const header = section.querySelector(':scope > header.tl-board__header')!;
      expect(header.classList).toContain('tl-type-body-strong');
      expect(header.querySelector('.tl-badge')!.textContent).toBe(['3', '2', '0'][i]);

      const list = listOf(section);
      expect(list.tagName).toBe('DIV');
      expect(list.getAttribute('role')).toBe('listbox');
      expect(list.getAttribute('aria-labelledby')).toBe(label.id);
      expect(list.hasAttribute('aria-multiselectable')).toBe(false);
      const cards = Array.from(list.children) as HTMLElement[];
      expect(cards).toHaveLength([3, 2, 0][i]);
      cards.forEach(card => {
        expect(card.tagName).toBe('DIV');
        expect(card.classList).toContain('tl-item');
        expect(card.classList).toContain('tl-board__card');
        expect(card.getAttribute('role')).toBe('option');
        expect(card.getAttribute('aria-selected')).toBe('false');
        expect(card.getAttribute('draggable')).toBe('true');
      });
      // Exactly one tab stop in a column holding cards.
      expect(cards.filter(card => card.tabIndex === 0)).toHaveLength(cards.length > 0 ? 1 : 0);
    });
    const announcer = live();
    expect(announcer.classList).toContain('tl-visually-hidden');
    expect(announcer.getAttribute('role')).toBe('status');
    expect(legacyClasses()).toEqual([]);
  });

  it('marks the list boxes multi-selectable where several cards may be selected', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1'], B: [] }, { multiSelect: true }));
    await settle();

    expect(columnElements()).toHaveLength(2);
    columnElements().forEach(section => expect(listOf(section).getAttribute('aria-multiselectable')).toBe('true'));
  });

  it('moves the focus to the next card by arrow down, sending nothing', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'] }));
    await settle();

    cardOf('a1').focus();
    await userEvent.keyboard('{ArrowDown}');
    await settle();

    expect(document.activeElement).toBe(cardOf('a2'));
    expect(cardOf('a2').tabIndex).toBe(0);
    expect(cardOf('a1').tabIndex).toBe(-1);
    expect(sent).not.toHaveBeenCalled();
  });

  it('selects a card by a click, toggles it with Ctrl, extends the selection with Shift and selects it by Enter', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'] }, { multiSelect: true }));
    await settle();

    fireEvent.click(screen.getByText('a1', { selector: 'span' }));
    await settle();
    fireEvent.click(screen.getByText('a2', { selector: 'span' }), { ctrlKey: true });
    await settle();
    fireEvent.click(screen.getByText('a2', { selector: 'span' }), { shiftKey: true });
    await settle();
    cardOf('a1').focus();
    await userEvent.keyboard('{Enter}');
    await settle();
    cardOf('a2').dispatchEvent(new CustomEvent(LONG_PRESS_EVENT, { bubbles: true }));
    await settle();

    expect(sentOf(sent, CMD_SELECT_CARD)).toEqual([
      [CMD_SELECT_CARD, { card: 'a1', toggle: false, range: false }],
      [CMD_SELECT_CARD, { card: 'a2', toggle: true, range: false }],
      [CMD_SELECT_CARD, { card: 'a2', toggle: false, range: true }],
      [CMD_SELECT_CARD, { card: 'a1', toggle: false, range: false }],
      [CMD_SELECT_CARD, { card: 'a2', toggle: true, range: false }],
    ]);
  });

  it('shows a selected card with the check of an item and makes it the tab stop of its column', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'] }, { selected: ['a2'] }));
    await settle();

    expect(cardOf('a2').getAttribute('aria-selected')).toBe('true');
    const check = cardOf('a2').querySelector(':scope > span.tl-item__check');
    expect(check).not.toBeNull();
    expect(check!.getAttribute('aria-hidden')).toBe('true');
    expect(cardOf('a1').querySelector('.tl-item__check')).toBeNull();
    expect(cardOf('a2').tabIndex).toBe(0);
    expect(cardOf('a1').tabIndex).toBe(-1);
  });

  it('leaves a click on a button inside a card to the button (review focus 1)', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1'] }));
    await settle();

    await userEvent.click(screen.getByRole('button', { name: 'Details a1' }));
    await settle();

    expect(sentOf(sent, CMD_SELECT_CARD)).toEqual([]);
  });

  it('starts a drag on the card itself, an option, but none on a button inside it (review focus 1)', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'] }));
    await settle();

    const fromButton = transfer();
    const started = fireEvent.dragStart(screen.getByRole('button', { name: 'Details a1' }), { dataTransfer: fromButton });
    expect(started).toBe(false);
    expect(readDragPayload(fromButton as unknown as DataTransfer)).toBeNull();

    const fromCard = transfer();
    expect(fireEvent.dragStart(cardOf('a1'), { dataTransfer: fromCard })).toBe(true);
    expect(readDragPayload(fromCard as unknown as DataTransfer))
      .toEqual({ source: CONTROL_ID, keys: ['a1'], selection: false, kind: 'ticket' });
  });

  it('fades the dragged card until the drag ends', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'] }));
    await settle();

    await startDrag('a1');

    expect(cardOf('a1').getAttribute('data-tl-state')).toBe('dragging');
    expect(cardOf('a2').hasAttribute('data-tl-state')).toBe(false);

    fireEvent.dragEnd(cardOf('a1'));
    await settle();

    expect(cardOf('a1').hasAttribute('data-tl-state')).toBe(false);
  });

  it('moves a card one place down by Alt + arrow down with the drop of a drag, after the next card', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'], B: ['b1', 'b2'] }));
    await settle();

    await altArrow('a1', 'ArrowDown');

    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a1', selection: false, targetKey: 'a2', zone: 'lower' }],
    ]);
    // Until the new state arrives, the focus stays where it is.
    expect(document.activeElement).toBe(cardOf('a1'));
  });

  it('moves a card one place up by Alt + arrow up, before the previous card', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'] }));
    await settle();

    await altArrow('a3', 'ArrowUp');

    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a3', selection: false, targetKey: 'a2', zone: 'upper' }],
    ]);
  });

  it('moves no card past the ends of its column or the board', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'], B: ['b1'] }));
    await settle();

    await altArrow('a1', 'ArrowUp');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('a2', 'ArrowDown');
    expect(document.activeElement).toBe(cardOf('a2'));
    await altArrow('a1', 'ArrowLeft');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('b1', 'ArrowRight');
    expect(document.activeElement).toBe(cardOf('b1'));

    expect(sentOf(sent, CMD_DROP)).toEqual([]);
  });

  it('moves a card into the neighbour column at its own place by Alt + arrow right, or to its end', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'], B: ['b1', 'b2'] }));
    await settle();

    await altArrow('a1', 'ArrowRight');
    await altArrow('a3', 'ArrowRight');
    await altArrow('b2', 'ArrowLeft');

    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a1', selection: false, targetKey: 'b1', zone: 'upper' }],
      // B holds no third card: the end of the column.
      [CMD_DROP, { source: CONTROL_ID, keys: 'a3', selection: false, targetKey: 'B', zone: 'none' }],
      [CMD_DROP, { source: CONTROL_ID, keys: 'b2', selection: false, targetKey: 'a2', zone: 'upper' }],
    ]);
  });

  it('moves a card into an empty neighbour column', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'], B: [] }));
    await settle();

    await altArrow('a1', 'ArrowRight');

    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a1', selection: false, targetKey: 'B', zone: 'none' }],
    ]);
  });

  it('takes the selected cards of the column along with a selected card, and moves an unselected one alone', async () => {
    const sent = mountAdapter(TLKanbanBoard,
      board({ A: ['a1', 'a2', 'a3'], B: ['b1'] }, { multiSelect: true, selected: ['a1', 'a2', 'b1'] }));
    await settle();

    await altArrow('a1', 'ArrowDown');
    await altArrow('a3', 'ArrowUp');

    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a1,a2', selection: false, targetKey: 'a3', zone: 'lower' }],
      [CMD_DROP, { source: CONTROL_ID, keys: 'a3', selection: false, targetKey: 'a2', zone: 'upper' }],
    ]);
  });

  it('gives the moved card the focus and announces its new place once the new state arrives', async () => {
    clearI18NCache();
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'], B: ['b1', 'b2'] }));
    englishTexts({ 'js.kanbanBoard.moved': MOVED });
    await settle();

    await altArrow('a1', 'ArrowDown');
    patchState({ columns: columnsOf({ A: ['a2', 'a1', 'a3'], B: ['b1', 'b2'] }) });
    await settle();

    expect(document.activeElement).toBe(cardOf('a1'));
    expect(cardOf('a1').tabIndex).toBe(0);
    expect(live().textContent).toBe('Moved to Spalte A, position 2 of 3.');

    await altArrow('a1', 'ArrowRight');
    patchState({ columns: columnsOf({ A: ['a2', 'a3'], B: ['b1', 'a1', 'b2'] }) });
    await settle();

    expect(cardOf('a1').closest('section')).toBe(columnElements()[1]);
    expect(document.activeElement).toBe(cardOf('a1'));
    expect(cardOf('a1').tabIndex).toBe(0);
    expect(live().textContent).toBe('Moved to Spalte B, position 2 of 3.');
  });

  it('keeps the focus and announces nothing where the server refuses the move', async () => {
    clearI18NCache();
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'] }));
    englishTexts({ 'js.kanbanBoard.moved': MOVED });
    await settle();

    await altArrow('a1', 'ArrowDown');

    expect(document.activeElement).toBe(cardOf('a1'));
    expect(live().textContent).toBe('');
  });

  it('sends nothing by Alt + arrow on a board whose cards are not dragged, and keeps the focus (review focus 2)', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'], B: ['b1'] }, { dragEnabled: false }));
    await settle();

    await altArrow('a1', 'ArrowDown');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('a1', 'ArrowRight');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('a2', 'ArrowUp');
    expect(document.activeElement).toBe(cardOf('a2'));
    await altArrow('a2', 'ArrowLeft');
    expect(document.activeElement).toBe(cardOf('a2'));

    expect(sentOf(sent, CMD_DROP)).toEqual([]);
  });

  it('moves a card into the neighbour column, but not within its own one, without reordering (review focus 2)', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'], B: ['b1'] }, { reorder: false }));
    await settle();

    await altArrow('a1', 'ArrowDown');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('a2', 'ArrowUp');
    expect(document.activeElement).toBe(cardOf('a2'));
    expect(sentOf(sent, CMD_DROP)).toEqual([]);

    // What a pointer drop on the column sends: the column itself, no place beside a card.
    await altArrow('a1', 'ArrowRight');
    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a1', selection: false, targetKey: 'B', zone: 'none' }],
    ]);
  });

  it('sends nothing by Alt + arrow on a card that may not be dragged, and keeps the focus (review focus 2)', async () => {
    clearI18NCache();
    const state = board({ A: ['a1', 'a2'], B: ['b1'] });
    state.columns[0].cards[0].draggable = false;
    const sent = mountAdapter(TLKanbanBoard, state);
    englishTexts({ 'js.kanbanBoard.moved': MOVED });
    await settle();

    expect(cardOf('a1').getAttribute('draggable')).toBe('false');
    await altArrow('a1', 'ArrowDown');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('a1', 'ArrowRight');
    expect(document.activeElement).toBe(cardOf('a1'));

    expect(sentOf(sent, CMD_DROP)).toEqual([]);
    expect(live().textContent).toBe('');
  });

  it('sends nothing by Alt + arrow where the board does not accept the kind of its own cards (review focus 2)', async () => {
    clearI18NCache();
    const sent = mountAdapter(TLKanbanBoard,
      board({ A: ['a1', 'a2'], B: ['b1'] }, { reorder: false, dropAccepts: ['anderes'] }));
    englishTexts({ 'js.kanbanBoard.moved': MOVED });
    await settle();

    await altArrow('a1', 'ArrowRight');
    expect(document.activeElement).toBe(cardOf('a1'));
    await altArrow('b1', 'ArrowLeft');
    expect(document.activeElement).toBe(cardOf('b1'));

    expect(sentOf(sent, CMD_DROP)).toEqual([]);
    expect(live().textContent).toBe('');
  });

  it('moves cards without a kind only on a board accepting any drag', async () => {
    const sent = mountAdapter(TLKanbanBoard,
      board({ A: ['a1'], B: ['b1'] }, { reorder: false, dragKind: null, dropAccepts: [] }));
    await settle();

    await altArrow('a1', 'ArrowRight');
    expect(sentOf(sent, CMD_DROP)).toEqual([]);
    cleanup();

    const accepting = mountAdapter(TLKanbanBoard,
      board({ A: ['a1'], B: ['b1'] }, { reorder: false, dragKind: null, dropAccepts: [], dropAcceptsAny: true }));
    await settle();

    await altArrow('a1', 'ArrowRight');
    expect(sentOf(accepting, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'a1', selection: false, targetKey: 'B', zone: 'none' }],
    ]);
  });

  it('sends nothing by an arrow key without Alt (review focus 2)', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'], B: ['b1'] }));
    await settle();

    cardOf('a1').focus();
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(cardOf('a2'));
    await userEvent.keyboard('{ArrowUp}');
    expect(document.activeElement).toBe(cardOf('a1'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('marks the card boundary nearest to the pointer, also in a gap and below the last card', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2', 'a3'], B: ['b1'] }));
    await settle();
    layOut(['a1', 'a2', 'a3']);
    const column = columnElements()[0];
    const data = await startDrag('b1');

    // The upper half of a2.
    dragAt('dragOver', cardOf('a2'), 55, data);
    expect(cardOf('a2').getAttribute('data-tl-state')).toBe('drop-before');
    expect(column.getAttribute('data-tl-state')).toBe('drop-onto');

    // The lower half of a1: the boundary before a2, as before.
    dragAt('dragOver', cardOf('a1'), 30, data);
    expect(cardOf('a1').hasAttribute('data-tl-state')).toBe(false);
    expect(cardOf('a2').getAttribute('data-tl-state')).toBe('drop-before');

    // The gap between a2 and a3.
    dragAt('dragOver', listOf(column), 95, data);
    expect(cardOf('a2').hasAttribute('data-tl-state')).toBe(false);
    expect(cardOf('a3').getAttribute('data-tl-state')).toBe('drop-before');

    // Below the last card.
    dragAt('dragOver', listOf(column), 200, data);
    expect(cardOf('a3').getAttribute('data-tl-state')).toBe('drop-after');
    // The column is marked as the target in addition to the line (Hendrik 19.10.).
    expect(column.getAttribute('data-tl-state')).toBe('drop-onto');
  });

  it('drops at the boundary it marks', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'], B: ['b1'] }));
    await settle();
    layOut(['a1', 'a2']);
    const data = await startDrag('b1');

    dragAt('drop', cardOf('a2'), 55, data);
    await settle();

    expect(sentOf(sent, CMD_DROP)).toEqual([
      [CMD_DROP, { source: CONTROL_ID, keys: 'b1', selection: false, targetKey: 'a2', zone: 'upper' }],
    ]);
  });

  it('marks the column under the drag: an empty one, or any one without reordering, without a line', async () => {
    mountAdapter(TLKanbanBoard, board({ A: ['a1'], B: [] }));
    await settle();
    let data = await startDrag('a1');

    dragAt('dragOver', listOf(columnElements()[1]), 10, data);
    expect(columnElements()[1].getAttribute('data-tl-state')).toBe('drop-onto');
    fireEvent.dragEnd(cardOf('a1'));
    cleanup();

    mountAdapter(TLKanbanBoard, board({ A: ['a1', 'a2'], B: ['b1'] }, { reorder: false }));
    await settle();
    layOut(['a1', 'a2']);
    data = await startDrag('b1');

    dragAt('dragOver', cardOf('a1'), 5, data);
    expect(columnElements()[0].getAttribute('data-tl-state')).toBe('drop-onto');
    expect(cardOf('a1').hasAttribute('data-tl-state')).toBe(false);
  });

  it('marks a column refusing the drop and shows the reason in the drop hint', async () => {
    const sent = mountAdapter(TLKanbanBoard, board({ A: ['a1'], B: ['b1'] }));
    await settle();
    layOut(['a1']);
    const data = await startDrag('b1');

    dragAt('dragOver', cardOf('a1'), 5, data);
    await settle();

    const probes = sentOf(sent, CMD_DROP_PROBE);
    expect(probes).toHaveLength(1);
    const probe = probes[0][1];
    expect(probe).toMatchObject({ source: CONTROL_ID, keys: 'b1', selection: false, targetKey: 'a1', zone: 'upper' });

    patchState({ dropVerdicts: { [probe.probe as string]: { accepted: false, reason: 'Nicht in diese Spalte' } } });
    await settle();

    expect(columnElements()[0].getAttribute('data-tl-state')).toBe('drop-refused');
    expect(cardOf('a1').hasAttribute('data-tl-state')).toBe(false);
    const hint = document.querySelector<HTMLElement>('.tl-popover.tl-drop-hint')!;
    expect(hint).not.toBeNull();
    expect(hint.getAttribute('role')).toBe('status');
    expect(hint.textContent).toBe('Nicht in diese Spalte');
  });
});

// The calendar (spec §3): one block tl-calendar for all its views. The toolbar is a tl-toolbar with the view choice as a
// tl-segmented; the events of a grid are one list box with one tab stop, painted in their category or in the colour of
// the server with the text readable on it; the days are buttons named by their full date, today marked alike in every
// view; the field of a new event discards it by Escape through the dispatcher, also inside a window.
describe('the calendar (spec §3: tl-calendar)', () => {
  const CMD_SELECT_EVENT = 'selectEvent';
  const CMD_GOTO = 'goto';
  const CMD_SWITCH_GRANULARITY = 'switchGranularity';
  const CMD_MOVE_EVENT = 'moveEvent';
  const CMD_CREATE_SLOT = 'createSlot';
  const MS_DAY = 86400000;

  afterEach(() => {
    // The texts of a test must not reach the next one; the calendar is unmounted first, so that no hook of it answers
    // the cleared cache.
    cleanup();
    clearI18NCache();
  });

  /** Monday 19 October 2026 plus the given days, at the given time of the day (local time, as the calendar reads it). */
  function at(day: number, hour = 0, minute = 0) {
    return new Date(2026, 9, 19 + day, hour, minute).getTime();
  }

  /** An event as the server describes it, its title its ID. */
  function event(id: string, start: number, end: number, extra: Record<string, unknown> = {}) {
    return { id, start, end, allDay: false, title: id, movable: true, resizable: true, selected: false, ...extra };
  }

  /** The state of a calendar in the given view, today Wednesday 21 October 2026, at the week of 19 October. */
  function calendar(granularity: string, events: unknown[], extra: Record<string, unknown> = {}) {
    return {
      granularity, title: 'Zeitraum', rangeStart: at(0), rangeEnd: at(7), anchor: at(2), now: at(2, 12),
      firstDayOfWeek: 1, dayStartHour: 8, dayEndHour: 18, nonWorkingDays: [0, 6], locale: 'de-DE', editable: true,
      events, ...extra,
    };
  }

  /** The events of a week: two on Monday, one on Tuesday, one on Wednesday, one all day on Friday. */
  const WEEK = [
    event('daily', at(0, 9), at(0, 10), { title: 'Daily', category: 'Team' }),
    event('review', at(0, 13), at(0, 14, 30), { title: 'Review', category: null }),
    event('planning', at(1, 10), at(1, 11, 30), { title: 'Planung', category: '' }),
    event('workshop', at(2, 9), at(2, 12), { title: 'Workshop' }),
    event('holiday', at(4), at(5), { title: 'Urlaub', allDay: true, category: 'Frei' }),
  ];

  function root() {
    return document.getElementById(CONTROL_ID)!;
  }

  function sentOf(sent: ReturnType<typeof mountAdapter>, command: string) {
    return sent.mock.calls.filter(([name]) => name === command);
  }

  /** The option of the event with the given title. */
  function optionOf(title: string) {
    return Array.from(document.querySelectorAll<HTMLElement>('[role="option"]'))
      .find(option => option.getAttribute('aria-label')!.includes(title))!;
  }

  /** The list box of the time grid. */
  function timedList() {
    return root().querySelector<HTMLElement>('.tl-calendar__time-grid .tl-calendar__columns > .tl-calendar__events')!;
  }

  /** The tile of the month showing the day of the given name. */
  function cellOf(name: string) {
    return screen.getByRole('button', { name }).closest<HTMLElement>('.tl-calendar__day')!;
  }

  /** The classes that are not package classes (`tl-…`), and inline styles in the page that set a custom property instead of data. */
  function legacy() {
    const elements = Array.from(document.querySelectorAll('*'));
    return [
      ...elements.flatMap(element => Array.from(element.classList)).filter(name => name.startsWith('tl') && !name.startsWith('tl-')),
      ...Array.from(document.body.querySelectorAll('*')).map(element => element.getAttribute('style') ?? '').filter(style => /(^|;)\s*--/.test(style)),
    ];
  }

  /** A data transfer as the browser hands it to the events of one drag (jsdom has none). */
  function transfer() {
    const data = new Map<string, string>();
    return {
      types: [] as string[],
      effectAllowed: 'all',
      dropEffect: 'none',
      setData(type: string, value: string) {
        data.set(type, value);
        this.types.push(type);
      },
      getData(type: string) {
        return data.get(type) ?? '';
      },
      setDragImage() {},
    };
  }

  /** The commands the control with the given ID sent, also one embedded in the control under test. */
  function commandsOf(controlId: string) {
    return vi.mocked(globalThis.fetch).mock.calls
      .map(([, init]) => JSON.parse(String((init as RequestInit | undefined)?.body ?? '{}')))
      .filter(body => body.controlId === controlId)
      .map(body => [body.command, body.arguments]);
  }

  it('is the block tl-calendar with a toolbar holding the view choice as a segmented radio group', async () => {
    const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK, { cssClass: 'kalender-der-anwendung' }));
    await settle();

    expect(Array.from(root().classList)).toEqual(['tl-calendar', 'kalender-der-anwendung']);
    const toolbar = root().querySelector<HTMLElement>(':scope > .tl-toolbar')!;
    expect(toolbar.tagName).toBe('DIV');
    expect(toolbar.classList).toContain('tl-calendar__toolbar');
    expect(toolbar.getAttribute('role')).toBe('toolbar');
    expect(toolbar.getAttribute('aria-label')).toBe('js.calendar.toolbar');
    expect(toolbar.querySelectorAll(':scope > .tl-toolbar__group button')).toHaveLength(3);
    const title = toolbar.querySelector(':scope > .tl-calendar__title')!;
    expect(title.classList).toContain('tl-type-heading-sm');
    expect(title.textContent).toBe('Zeitraum');

    const views = toolbar.querySelector<HTMLElement>(':scope > div.tl-segmented')!;
    expect(views.getAttribute('role')).toBe('radiogroup');
    expect(views.getAttribute('aria-label')).toBe('js.calendar.view');
    const segments = Array.from(views.querySelectorAll<HTMLElement>(':scope > button.tl-segmented__segment'));
    expect(segments.map(segment => segment.getAttribute('role'))).toEqual(['radio', 'radio', 'radio', 'radio', 'radio']);
    expect(segments.map(segment => segment.getAttribute('aria-checked'))).toEqual(['false', 'false', 'true', 'false', 'false']);
    expect(segments.map(segment => segment.tabIndex)).toEqual([-1, -1, 0, -1, -1]);
    segments.forEach(segment => {
      expect(segment.classList).toContain('tl-type-label');
      expect(segment.querySelector(':scope > .tl-segmented__label')).not.toBeNull();
    });

    fireEvent.click(screen.getByRole('radio', { name: 'js.calendar.month' }));
    await settle();

    expect(sentOf(sent, CMD_SWITCH_GRANULARITY)).toEqual([[CMD_SWITCH_GRANULARITY, { granularity: 'MONTH' }]]);
    expect(legacy()).toEqual([]);
  });

  it('switches the view by the arrows in the view choice, as a radio group does', async () => {
    const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
    await settle();

    screen.getByRole('radio', { name: 'js.calendar.week' }).focus();
    await userEvent.keyboard('{ArrowRight}');
    await settle();

    expect(document.activeElement).toBe(screen.getByRole('radio', { name: 'js.calendar.month' }));
    expect(sentOf(sent, CMD_SWITCH_GRANULARITY)).toEqual([[CMD_SWITCH_GRANULARITY, { granularity: 'MONTH' }]]);
  });

  it('lists the events of the time grid in one list box with one tab stop, the arrows following the time', async () => {
    const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
    await settle();

    const list = timedList();
    expect(list.getAttribute('role')).toBe('listbox');
    expect(list.getAttribute('aria-label')).toBe('js.calendar.events');
    const options = Array.from(list.querySelectorAll<HTMLElement>(':scope > .tl-calendar__column > [role="option"]'));
    expect(options.map(option => option.getAttribute('aria-label')!.split(', ')[1]))
      .toEqual(['Daily', 'Review', 'Planung', 'Workshop']);
    expect(options.map(option => option.tabIndex)).toEqual([0, -1, -1, -1]);
    options.forEach(option => {
      expect(option.tagName).toBe('DIV');
      expect(option.classList).toContain('tl-calendar__event');
      expect(option.classList).toContain('tl-type-label');
      expect(option.getAttribute('aria-selected')).toBe('false');
      expect(option.querySelector(':scope > .tl-calendar__event-time')!.classList).toContain('tl-type-label');
      expect(option.querySelector(':scope > .tl-calendar__event-title')!.classList).toContain('tl-type-label-strong');
    });
    // The name tells the time and the title.
    expect(optionOf('Daily').getAttribute('aria-label')).toMatch(/^9:00.*10:00, Daily$/);
    expect(optionOf('Daily').getAttribute('data-tl-role')).toBe('category-' + categoryOf('Team'));

    optionOf('Daily').focus();
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(optionOf('Review'));
    await userEvent.keyboard('{ArrowDown}');
    expect(document.activeElement).toBe(optionOf('Planung'));
    expect(optionOf('Planung').tabIndex).toBe(0);
    await userEvent.keyboard('{Enter}');
    await settle();

    expect(sentOf(sent, CMD_SELECT_EVENT)).toEqual([[CMD_SELECT_EVENT, { eventId: 'planning' }]]);
  });

  it('lists the events of the whole day in a list box of their own, named by their title and all day', async () => {
    mountAdapter(TLCalendar, calendar('WEEK', WEEK));
    await settle();

    const list = root().querySelector<HTMLElement>('.tl-calendar__all-day > .tl-calendar__events')!;
    expect(list.getAttribute('role')).toBe('listbox');
    expect(list.getAttribute('aria-label')).toBe('js.calendar.events, js.calendar.allDay');
    const options = Array.from(list.querySelectorAll<HTMLElement>(':scope > .tl-calendar__all-day-cell > [role="option"]'));
    expect(options).toHaveLength(1);
    expect(options[0].getAttribute('aria-label')).toBe('Urlaub, js.calendar.allDay');
    expect(options[0].classList).toContain('tl-calendar__event');
    expect(options[0].tabIndex).toBe(0);
    const label = root().querySelector('.tl-calendar__all-day > .tl-calendar__all-day-label')!;
    expect(label.classList).toContain('tl-calendar__gutter');
    expect(label.classList).toContain('tl-type-label');
  });

  it('marks the selected event and makes it the tab stop of its list', async () => {
    mountAdapter(TLCalendar, calendar('WEEK', WEEK.map(e => e.id === 'planning' ? { ...e, selected: true } : e)));
    await settle();

    expect(optionOf('Planung').getAttribute('aria-selected')).toBe('true');
    expect(optionOf('Planung').tabIndex).toBe(0);
    expect(optionOf('Daily').getAttribute('aria-selected')).toBe('false');
    expect(optionOf('Daily').tabIndex).toBe(-1);
  });

  it('paints an event without category and colour as the brand, a server colour inline with readable text, an unreadable colour by its category (review focus 3)', async () => {
    document.documentElement.style.setProperty('--tl-text-contrast-light', '#ffffff');
    document.documentElement.style.setProperty('--tl-text-contrast-dark', '#000000');
    mountAdapter(TLCalendar, calendar('WEEK', [
      event('none', at(0, 9), at(0, 10), { title: 'Ohne', category: null, color: null }),
      event('empty', at(0, 11), at(0, 12), { title: 'Leer', category: '' }),
      event('blue', at(1, 9), at(1, 10), { title: 'Blau', category: 'Team', color: 'rgb(0, 51, 102)' }),
      event('broken', at(2, 9), at(2, 10), { title: 'Kaputt', category: 'Kunde', color: 'kein-farbwert' }),
      event('broken-plain', at(3, 9), at(3, 10), { title: 'Schlicht', color: 'kein-farbwert' }),
    ]));
    await settle();

    for (const title of ['Ohne', 'Leer', 'Schlicht']) {
      const option = optionOf(title);
      expect(option.hasAttribute('data-tl-role')).toBe(false);
      expect(option.style.background).toBe('');
      expect(option.style.color).toBe('');
    }
    const blue = optionOf('Blau');
    expect(blue.style.background).toBe('rgb(0, 51, 102)');
    expect(blue.style.color).toBe('var(--tl-text-contrast-light)');
    expect(blue.hasAttribute('data-tl-role')).toBe(false);
    const broken = optionOf('Kaputt');
    expect(broken.getAttribute('data-tl-role')).toBe('category-' + categoryOf('Kunde'));
    expect(broken.style.background).toBe('');
    expect(broken.style.color).toBe('');
  });

  it('makes the day heads buttons named by the full date, today marked, non-working days in their state', async () => {
    const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
    await settle();

    const heads = Array.from(root().querySelectorAll<HTMLElement>('.tl-calendar__head > .tl-calendar__day-head'));
    expect(heads).toHaveLength(7);
    heads.forEach(head => expect(head.tagName).toBe('BUTTON'));
    expect(heads[0].getAttribute('aria-label')).toBe('Montag, 19. Oktober 2026');
    expect(heads.map(head => head.getAttribute('aria-current'))).toEqual([null, null, 'date', null, null, null, null]);
    expect(heads.map(head => head.getAttribute('data-tl-state')))
      .toEqual([null, null, null, null, null, 'nonworking', 'nonworking']);
    expect(heads[0].querySelector(':scope > .tl-calendar__weekday')!.classList).toContain('tl-type-label');
    const number = heads[0].querySelector(':scope > .tl-calendar__day-number')!;
    expect(number.classList).toContain('tl-type-heading-sm');
    expect(number.textContent).toBe('19');

    // The columns: non-working in their state, the working time drawn on working days only, before the hour lines.
    const columns = Array.from(timedList().querySelectorAll<HTMLElement>(':scope > .tl-calendar__column'));
    expect(columns.map(column => column.getAttribute('data-tl-state')))
      .toEqual([null, null, null, null, null, 'nonworking', 'nonworking']);
    columns.slice(0, 5).forEach(column => {
      expect(column.firstElementChild!.classList).toContain('tl-calendar__work');
      expect(column.children[1].classList).toContain('tl-calendar__hour-line');
    });
    columns.slice(5).forEach(column => expect(column.querySelector('.tl-calendar__work')).toBeNull());

    fireEvent.click(heads[1]);
    await settle();

    expect(sentOf(sent, CMD_GOTO)).toEqual([[CMD_GOTO, { date: at(1), granularity: 'DAY' }]]);
  });

  describe('in the month view', () => {
    /** Thursday 1 October 2026 plus the given days, at the given time of the day. */
    function oct(day: number, hour = 0, minute = 0) {
      return new Date(2026, 9, day, hour, minute).getTime();
    }

    const MONTH = [
      event('fair', oct(1), oct(3), { title: 'Messe', allDay: true, category: 'Messe' }),
      event('daily', oct(6, 9), oct(6, 10), { title: 'Daily', category: 'Team' }),
      event('call', oct(7, 15), oct(7, 15, 30), { title: 'Telefonat', color: 'rgb(0, 51, 102)' }),
    ];

    function month() {
      return calendar('MONTH', MONTH, {
        rangeStart: new Date(2026, 8, 28).getTime(), rangeEnd: new Date(2026, 10, 9).getTime(), anchor: oct(15),
      });
    }

    it('makes the day numbers buttons and the events a list box: all-day ones filled, timed ones as chips', async () => {
      document.documentElement.style.setProperty('--tl-text-contrast-light', '#ffffff');
      document.documentElement.style.setProperty('--tl-text-contrast-dark', '#000000');
      const sent = mountAdapter(TLCalendar, month());
      await settle();

      const list = root().querySelector<HTMLElement>('.tl-calendar__month-body > .tl-calendar__events')!;
      expect(list.getAttribute('role')).toBe('listbox');
      expect(list.getAttribute('aria-label')).toBe('js.calendar.events');
      expect(list.querySelectorAll(':scope > .tl-calendar__week')).toHaveLength(6);

      const first = screen.getByRole('button', { name: 'Donnerstag, 1. Oktober 2026' });
      expect(first.classList).toContain('tl-calendar__day-number');
      expect(first.classList).toContain('tl-type-heading-sm');
      expect(cellOf('Montag, 28. September 2026').getAttribute('data-tl-state')).toBe('other');
      expect(cellOf('Samstag, 3. Oktober 2026').getAttribute('data-tl-state')).toBe('nonworking');
      expect(cellOf('Donnerstag, 1. Oktober 2026').hasAttribute('data-tl-state')).toBe(false);
      expect(cellOf('Mittwoch, 21. Oktober 2026').getAttribute('aria-current')).toBe('date');
      expect(cellOf('Donnerstag, 22. Oktober 2026').hasAttribute('aria-current')).toBe(false);

      const fair = optionOf('Messe');
      expect(fair.classList).toContain('tl-calendar__event');
      expect(fair.parentElement!.classList).toContain('tl-calendar__overlay');
      expect(fair.getAttribute('aria-label')).toBe('Messe, js.calendar.allDay');
      expect(fair.getAttribute('data-tl-role')).toBe('category-' + categoryOf('Messe'));

      const daily = optionOf('Daily');
      expect(daily.classList).toContain('tl-calendar__chip');
      expect(daily.classList).toContain('tl-type-label');
      expect(daily.classList).not.toContain('tl-calendar__event');
      expect(daily.getAttribute('aria-label')).toMatch(/^9:00.*10:00, Daily$/);
      const dot = daily.querySelector<HTMLElement>(':scope > .tl-calendar__chip-dot')!;
      expect(dot.getAttribute('data-tl-role')).toBe('category-' + categoryOf('Team'));
      expect(daily.querySelector(':scope > .tl-calendar__event-time')).not.toBeNull();
      expect(daily.querySelector(':scope > .tl-calendar__event-title')).not.toBeNull();

      // A server colour paints the dot; the chip itself stays unfilled.
      const call = optionOf('Telefonat');
      expect(call.querySelector<HTMLElement>(':scope > .tl-calendar__chip-dot')!.style.background).toBe('rgb(0, 51, 102)');
      expect(call.style.background).toBe('');
      expect(call.style.color).toBe('');

      const options = Array.from(list.querySelectorAll<HTMLElement>('[role="option"]'));
      expect(options.filter(option => option.tabIndex === 0)).toHaveLength(1);

      fireEvent.click(first);
      await settle();
      expect(sentOf(sent, CMD_GOTO)).toEqual([[CMD_GOTO, { date: oct(1), granularity: 'DAY' }]]);
      expect(legacy()).toEqual([]);
    });

    it('marks the day an event is dragged over and moves the event there on the drop', async () => {
      const sent = mountAdapter(TLCalendar, month());
      await settle();

      const data = transfer();
      fireEvent.dragStart(optionOf('Daily'), { dataTransfer: data });
      await settle();
      expect(optionOf('Daily').getAttribute('data-tl-state')).toBe('dragging');

      const target = cellOf('Donnerstag, 8. Oktober 2026');
      fireEvent.dragOver(target, { dataTransfer: data });
      await settle();
      expect(target.getAttribute('data-tl-state')).toBe('drop-onto');
      expect(cellOf('Freitag, 9. Oktober 2026').hasAttribute('data-tl-state')).toBe(false);

      fireEvent.drop(target, { dataTransfer: data });
      fireEvent.dragEnd(optionOf('Daily'));
      await settle();

      expect(sentOf(sent, CMD_MOVE_EVENT)).toEqual([[CMD_MOVE_EVENT, { eventId: 'daily', start: oct(8, 9), end: oct(8, 10) }]]);
      expect(target.hasAttribute('data-tl-state')).toBe(false);
      expect(optionOf('Daily').hasAttribute('data-tl-state')).toBe(false);
    });
  });

  describe('in the year view', () => {
    function year() {
      return calendar('YEAR', [
        event('today', at(2, 9), at(2, 10), { title: 'Heute' }),
        event('early', new Date(2026, 9, 6, 9).getTime(), new Date(2026, 9, 6, 10).getTime(), { title: 'Früh' }),
      ], { rangeStart: new Date(2026, 0, 1).getTime(), rangeEnd: new Date(2027, 0, 1).getTime() });
    }

    /** The days of the mini month of the given name. */
    function daysOf(name: string) {
      const grid = root().querySelector<HTMLElement>(`.tl-calendar__mini-grid[aria-label="${name}"]`)!;
      return Array.from(grid.querySelectorAll<HTMLElement>(':scope > .tl-calendar__mini-day'));
    }

    it('shows each month as a grid of day buttons with one tab stop, today and the days with events marked', async () => {
      const sent = mountAdapter(TLCalendar, year());
      await settle();

      expect(root().querySelectorAll('.tl-calendar__year > .tl-calendar__mini')).toHaveLength(12);
      const title = screen.getByRole('button', { name: 'Oktober 2026' });
      expect(title.classList).toContain('tl-calendar__mini-title');
      expect(title.classList).toContain('tl-type-label-strong');
      expect(title.textContent).toBe('Oktober');

      const october = daysOf('Oktober 2026');
      expect(october).toHaveLength(42);
      october.forEach(day => expect(day.tagName).toBe('BUTTON'));
      expect(october.filter(day => day.tabIndex === 0).map(day => day.getAttribute('aria-label')))
        .toEqual(['Mittwoch, 21. Oktober 2026']);
      const today = october.find(day => day.getAttribute('aria-label') === 'Mittwoch, 21. Oktober 2026')!;
      expect(today.getAttribute('aria-current')).toBe('date');
      expect(today.getAttribute('data-tl-state')).toBe('event');
      expect(today.classList).toContain('tl-type-label-strong');
      const plain = october.find(day => day.getAttribute('aria-label') === 'Dienstag, 20. Oktober 2026')!;
      expect(plain.hasAttribute('data-tl-state')).toBe(false);
      expect(plain.classList).toContain('tl-type-label');
      expect(october[0].getAttribute('aria-label')).toBe('Montag, 28. September 2026');
      expect(october[0].getAttribute('data-tl-state')).toBe('other');
      const saturday = october.find(day => day.getAttribute('aria-label') === 'Samstag, 3. Oktober 2026')!;
      expect(saturday.getAttribute('data-tl-state')).toBe('nonworking');

      // A month without today has its first day as the tab stop.
      expect(daysOf('September 2026').filter(day => day.tabIndex === 0).map(day => day.getAttribute('aria-label')))
        .toEqual(['Dienstag, 1. September 2026']);

      fireEvent.click(title);
      await settle();
      expect(sentOf(sent, CMD_GOTO)).toEqual([[CMD_GOTO, { date: new Date(2026, 9, 1).getTime(), granularity: 'MONTH' }]]);
      expect(legacy()).toEqual([]);
    });

    it('moves to the next day by arrow right and opens it by Enter', async () => {
      const sent = mountAdapter(TLCalendar, year());
      await settle();

      const october = daysOf('Oktober 2026');
      october.find(day => day.getAttribute('aria-label') === 'Mittwoch, 21. Oktober 2026')!.focus();
      await userEvent.keyboard('{ArrowRight}');

      const next = daysOf('Oktober 2026').find(day => day.getAttribute('aria-label') === 'Donnerstag, 22. Oktober 2026')!;
      expect(document.activeElement).toBe(next);
      expect(next.tabIndex).toBe(0);

      await userEvent.keyboard('{Enter}');
      await settle();
      expect(sentOf(sent, CMD_GOTO)).toEqual([[CMD_GOTO, { date: new Date(2026, 9, 22).getTime(), granularity: 'DAY' }]]);
    });
  });

  describe('creating an event', () => {
    /** The cell of the row of whole days showing the given day of the week. */
    function allDayCell(day: number) {
      return document.querySelectorAll<HTMLElement>('.tl-calendar__all-day .tl-calendar__all-day-cell')[day];
    }

    it('focuses the field of the new event and creates it by Enter with the title entered', async () => {
      const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
      await settle();

      await userEvent.click(allDayCell(0));
      await settle();

      const field = document.activeElement as HTMLInputElement;
      expect(field.tagName).toBe('INPUT');
      expect(field.classList).toContain('tl-field');
      expect(field.classList).toContain('tl-calendar__create');
      expect(field.getAttribute('aria-label')).toBe('js.calendar.newEventTitle');
      const stub = field.parentElement!;
      expect(stub.classList).toContain('tl-calendar__event');
      expect(stub.getAttribute('data-tl-state')).toBe('preview');

      await userEvent.keyboard('Neu{Enter}');
      await settle();

      expect(sentOf(sent, CMD_CREATE_SLOT))
        .toEqual([[CMD_CREATE_SLOT, { start: at(0), end: at(0) + MS_DAY, allDay: true, title: 'Neu' }]]);
    });

    it('keeps the new event and the focus in its field on a press on the event beside the field', async () => {
      const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
      await settle();

      await userEvent.click(allDayCell(0));
      await settle();
      await userEvent.keyboard('Neu');
      const field = document.activeElement as HTMLInputElement;

      await userEvent.click(field.parentElement!);
      await settle();

      expect(document.querySelector('.tl-calendar__create')).toBe(field);
      expect(document.activeElement).toBe(field);
      expect(field.value).toBe('Neu');
      expect(sentOf(sent, CMD_CREATE_SLOT)).toEqual([]);
    });

    it('creates the new event with the title entered on a press elsewhere, and discards it without a title', async () => {
      const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
      await settle();

      await userEvent.click(allDayCell(0));
      await settle();
      await userEvent.keyboard('Neu');
      await userEvent.click(allDayCell(3));
      await settle();

      expect(sentOf(sent, CMD_CREATE_SLOT))
        .toEqual([[CMD_CREATE_SLOT, { start: at(0), end: at(0) + MS_DAY, allDay: true, title: 'Neu' }]]);

      // The press has opened a new event at the other day; left without a title, it is gone.
      expect((document.activeElement as HTMLElement).classList).toContain('tl-calendar__create');
      await userEvent.click(allDayCell(5));
      await settle();
      expect(sentOf(sent, CMD_CREATE_SLOT)).toHaveLength(1);
    });

    it('creates the new event with the title entered when a slot is pulled open in the grid of hours', async () => {
      const sent = mountAdapter(TLCalendar, calendar('WEEK', WEEK));
      await settle();

      await userEvent.click(allDayCell(0));
      await settle();
      await userEvent.keyboard('Neu');
      await userEvent.pointer({ keys: '[MouseLeft>]', target: document.querySelectorAll<HTMLElement>('.tl-calendar__column')[2] });
      await settle();

      expect(sentOf(sent, CMD_CREATE_SLOT))
        .toEqual([[CMD_CREATE_SLOT, { start: at(0), end: at(0) + MS_DAY, allDay: true, title: 'Neu' }]]);
    });

    it('discards only the new event by Escape inside a window, which stays open (review focus 4)', async () => {
      register('PlanenTestCalendar', TLCalendar);
      const child = { controlId: 'cal', module: 'PlanenTestCalendar', state: calendar('WEEK', WEEK) };
      const sent = mountAdapter(TLWindow, { title: 'Fenster', child, closable: true });
      await settle();

      await userEvent.click(allDayCell(0));
      await settle();
      expect((document.activeElement as HTMLElement).classList).toContain('tl-calendar__create');

      await userEvent.keyboard('Neu{Escape}');
      await settle();

      expect(document.querySelector('.tl-calendar__create')).toBeNull();
      expect(commandsOf('cal').filter(([command]) => command === CMD_CREATE_SLOT)).toEqual([]);
      expect(sent).not.toHaveBeenCalledWith('close', expect.anything());
      expect(screen.getByRole('dialog', { name: 'Fenster' })).toBeTruthy();

      // Positive control: with the new event gone, the same key reaches the window.
      await userEvent.keyboard('{Escape}');
      await settle();
      expect(sent).toHaveBeenCalledWith('close', {});
    });
  });
});
