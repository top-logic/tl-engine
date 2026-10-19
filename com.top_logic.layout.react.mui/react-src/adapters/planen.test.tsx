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

  /** Whether any element of the page still carries a class of the old style sheet. */
  function legacyClasses() {
    return Array.from(document.querySelectorAll('*'))
      .flatMap(element => Array.from(element.classList))
      .filter(name => name.startsWith('tlDashboard'));
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

  /** Whether any element of the page still carries a class of the old style sheet. */
  function legacyClasses() {
    return Array.from(document.querySelectorAll('*'))
      .flatMap(element => Array.from(element.classList))
      .filter(name => name.startsWith('tlKanbanBoard'));
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
    expect(column.hasAttribute('data-tl-state')).toBe(false);

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
    expect(column.hasAttribute('data-tl-state')).toBe(false);
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

  it('marks a column a drop goes to the end of: an empty one, or any one without reordering', async () => {
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
