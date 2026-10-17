// The pickers of wave 5a (spec 2026-10-17). This file starts with the shared keyboard of tab lists, useTabList, and
// the tab bar that runs on it. The tab bar draws icons through ThemeIcon, which needs no part.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { render, screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, useTabList, register, replacePart } from 'tl-react-bridge';
import { PickerPopover } from '../../../com.top_logic.layout.react/react-src/controls/picker/PickerPopover';
import TLWindow from '../../../com.top_logic.layout.react/react-src/controls/TLWindow';
import TLTabBar from '../../../com.top_logic.layout.react/react-src/controls/TLTabBar';
import TLColorInput from '../../../com.top_logic.layout.react/react-src/controls/TLColorInput';
import MuiButtonAdapter from './MuiButtonAdapter';
import MuiTextInputAdapter from './MuiTextInputAdapter';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';
import MuiFieldValueAdapter from './MuiFieldValueAdapter';
import MuiDividerAdapter from './MuiDividerAdapter';
import { childControl, mountAdapter, settle } from './wire-test-support';

// The test registry has no parts of its own: the parts the pickers look up are the MUI ones.
replacePart('Button', MuiButtonAdapter);
replacePart('TextInput', MuiTextInputAdapter);
replacePart('Spinner', MuiSpinnerAdapter);
replacePart('FieldValue', MuiFieldValueAdapter);
replacePart('Divider', MuiDividerAdapter);

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

const IDS = ['a', 'b', 'c'];

function Tabs({ selected, onSelect }: { selected: string; onSelect: (id: string) => void }) {
  const list = useTabList({ idPrefix: 'tt', ids: IDS, selected, onSelect });
  return (
    <div>
      <div role="tablist" onKeyDown={list.onListKeyDown}>
        {IDS.map(id => (
          <button key={id} {...list.tabProps(id)}>{id}</button>
        ))}
        <button type="button">extra</button>
      </div>
      {IDS.map(id => (
        <div key={id} {...list.panelProps(id)}>Panel {id}</div>
      ))}
    </div>
  );
}

function tab(name: string): HTMLElement {
  return screen.getByRole('tab', { name });
}

describe('useTabList', () => {
  it('ArrowRight on the last tab focuses and selects the first', () => {
    const onSelect = vi.fn();
    render(<Tabs selected="c" onSelect={onSelect} />);
    tab('c').focus();
    fireEvent.keyDown(tab('c'), { key: 'ArrowRight' });
    expect(document.activeElement).toBe(tab('a'));
    expect(onSelect).toHaveBeenCalledWith('a');
  });

  it('ArrowLeft on the first tab goes to the last', () => {
    const onSelect = vi.fn();
    render(<Tabs selected="a" onSelect={onSelect} />);
    tab('a').focus();
    fireEvent.keyDown(tab('a'), { key: 'ArrowLeft' });
    expect(document.activeElement).toBe(tab('c'));
    expect(onSelect).toHaveBeenCalledWith('c');
  });

  it('Home and End go to the first and the last tab', () => {
    const onSelect = vi.fn();
    render(<Tabs selected="b" onSelect={onSelect} />);
    tab('b').focus();
    fireEvent.keyDown(tab('b'), { key: 'End' });
    expect(document.activeElement).toBe(tab('c'));
    expect(onSelect).toHaveBeenLastCalledWith('c');
    fireEvent.keyDown(tab('c'), { key: 'Home' });
    expect(document.activeElement).toBe(tab('a'));
    expect(onSelect).toHaveBeenLastCalledWith('a');
  });

  it('puts only the selected tab into the tab order', () => {
    render(<Tabs selected="b" onSelect={() => {}} />);
    expect(tab('a').tabIndex).toBe(-1);
    expect(tab('b').tabIndex).toBe(0);
    expect(tab('c').tabIndex).toBe(-1);
    expect(tab('b').getAttribute('aria-selected')).toBe('true');
    expect(tab('a').getAttribute('aria-selected')).toBe('false');
  });

  it('puts the first tab into the tab order while none is selected', () => {
    render(<Tabs selected="" onSelect={() => {}} />);
    expect(tab('a').tabIndex).toBe(0);
    expect(tab('b').tabIndex).toBe(-1);
  });

  it('points aria-controls at the panel that points back with aria-labelledby', () => {
    render(<Tabs selected="a" onSelect={() => {}} />);
    for (const id of IDS) {
      const panel = document.getElementById(tab(id).getAttribute('aria-controls')!)!;
      expect(panel.getAttribute('role')).toBe('tabpanel');
      expect(panel.getAttribute('aria-labelledby')).toBe(tab(id).id);
    }
  });

  it('hides the panels of the tabs that are not selected', () => {
    render(<Tabs selected="b" onSelect={() => {}} />);
    expect(document.getElementById('tt-panel-a')!.hidden).toBe(true);
    expect(document.getElementById('tt-panel-b')!.hidden).toBe(false);
  });

  it('prevents a key it handles and leaves another alone', () => {
    render(<Tabs selected="a" onSelect={() => {}} />);
    tab('a').focus();
    expect(fireEvent.keyDown(tab('a'), { key: 'ArrowRight' })).toBe(false);
    expect(fireEvent.keyDown(tab('a'), { key: 'a' })).toBe(true);
  });

  it('ignores a key from a button in the list that is not a tab', () => {
    const onSelect = vi.fn();
    render(<Tabs selected="b" onSelect={onSelect} />);
    const extra = screen.getByRole('button', { name: 'extra' });
    extra.focus();
    expect(fireEvent.keyDown(extra, { key: 'ArrowRight' })).toBe(true);
    expect(document.activeElement).toBe(extra);
    expect(onSelect).not.toHaveBeenCalled();
  });

  it('selects on a click', () => {
    const onSelect = vi.fn();
    render(<Tabs selected="a" onSelect={onSelect} />);
    fireEvent.click(tab('b'));
    expect(onSelect).toHaveBeenCalledWith('b');
  });
});

describe('TLTabBar', () => {
  const STATE = {
    tabs: [{ id: 't1', label: 'Eins' }, { id: 't2', label: 'Zwei' }],
    activeTabId: 't1',
  };

  it('sends selectTab with the next id on ArrowRight', async () => {
    const sent = mountAdapter(TLTabBar, STATE);
    await settle();
    tab('Eins').focus();
    fireEvent.keyDown(tab('Eins'), { key: 'ArrowRight' });
    await settle();
    expect(sent).toHaveBeenCalledWith('selectTab', { tabId: 't2' });
    expect(document.activeElement).toBe(tab('Zwei'));
  });

  it('sends nothing when the tab reached is the selected one', async () => {
    const sent = mountAdapter(TLTabBar, { ...STATE, activeTabId: 't2' });
    await settle();
    tab('Zwei').focus();
    fireEvent.keyDown(tab('Zwei'), { key: 'End' });
    await settle();
    expect(sent).not.toHaveBeenCalledWith('selectTab', expect.anything());
  });

  it('points every tab at the one panel', async () => {
    mountAdapter(TLTabBar, STATE);
    await settle();
    const panel = screen.getByRole('tabpanel');
    expect(tab('Eins').getAttribute('aria-controls')).toBe(panel.id);
    expect(tab('Zwei').getAttribute('aria-controls')).toBe(panel.id);
    expect(panel.getAttribute('aria-labelledby')).toBe(tab('Eins').id);
  });
});

describe('PickerPopover', () => {
  // jsdom lays nothing out; the focus search skips elements without a client rect.
  beforeEach(() => {
    vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
  });

  function Host({ onClose, initialFocus = 'first' }: { onClose: () => void; initialFocus?: 'first' | ((root: HTMLElement) => HTMLElement | null) }) {
    const anchor = React.useRef<HTMLButtonElement | null>(null);
    const [open, setOpen] = React.useState(true);
    return (
      <div>
        <button ref={anchor} type="button">Anker</button>
        <button type="button" onClick={() => setOpen(o => !o)}>Umschalter</button>
        <span>Aussen</span>
        {open && (
          <PickerPopover anchorRef={anchor} label="Farbe" initialFocus={initialFocus} onClose={onClose}>
            <button type="button">Eins</button>
            <button type="button">Zwei</button>
            <button type="button">Drei</button>
          </PickerPopover>
        )}
      </div>
    );
  }

  it('renders a named dialog under the body with the focus on the first content', () => {
    const { container } = render(<Host onClose={() => {}} />);
    const dialog = screen.getByRole('dialog', { name: 'Farbe' });
    expect(dialog.parentElement).toBe(document.body);
    expect(container.contains(dialog)).toBe(false);
    expect(dialog.classList.contains('tl-popover')).toBe(true);
    expect(dialog.classList.contains('tl-picker')).toBe(true);
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Eins' }));
  });

  it('lets a function as initialFocus win', () => {
    render(<Host onClose={() => {}} initialFocus={root => root.querySelectorAll<HTMLElement>('button')[1]} />);
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Zwei' }));
  });

  it('pulls the focus back into the popup when it leaves past the last element', () => {
    render(<Host onClose={() => {}} />);
    screen.getByRole('button', { name: 'Drei' }).focus();
    // The trap listens to focusin: Tab past the edge reaches the next element of the page (jsdom's Tab goes to the body
    // instead and fires no focusin), and the trap pulls the focus back to its first element.
    screen.getByRole('button', { name: 'Umschalter' }).focus();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Eins' }));
  });

  it('calls onClose once on Escape', async () => {
    const onClose = vi.fn();
    render(<Host onClose={onClose} />);
    await userEvent.keyboard('{Escape}');
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('calls onClose on a press outside, not on a press inside', () => {
    const onClose = vi.fn();
    render(<Host onClose={onClose} />);
    fireEvent.pointerDown(screen.getByRole('button', { name: 'Zwei' }));
    expect(onClose).not.toHaveBeenCalled();
    fireEvent.pointerDown(screen.getByText('Aussen'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('returns the focus to the anchor focused before the mount', () => {
    function Late() {
      const anchor = React.useRef<HTMLButtonElement | null>(null);
      const [open, setOpen] = React.useState(false);
      return (
        <div>
          <button ref={anchor} type="button" onClick={() => setOpen(true)}>Anker</button>
          {open && (
            <PickerPopover anchorRef={anchor} label="Farbe" initialFocus="first" onClose={() => setOpen(false)}>
              <button type="button">Eins</button>
            </PickerPopover>
          )}
        </div>
      );
    }
    render(<Late />);
    const anchor = screen.getByRole('button', { name: 'Anker' });
    anchor.focus();
    fireEvent.click(anchor);
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Eins' }));
    fireEvent.keyDown(document.activeElement!, { key: 'Escape' });
    expect(screen.queryByRole('dialog')).toBeNull();
    expect(document.activeElement).toBe(anchor);
  });

  it('gives the focus to the anchor when it was not focused before the mount', async () => {
    function Unfocused() {
      const anchor = React.useRef<HTMLButtonElement | null>(null);
      const [open, setOpen] = React.useState(false);
      return (
        <div>
          <button ref={anchor} type="button">Anker</button>
          <button type="button" onClick={() => setOpen(true)}>Oeffnen</button>
          {open && (
            <PickerPopover anchorRef={anchor} label="Farbe" initialFocus="first" onClose={() => setOpen(false)}>
              <button type="button">Eins</button>
            </PickerPopover>
          )}
        </div>
      );
    }
    render(<Unfocused />);
    // A click that does not focus the opener: the focus is on the body when the picker mounts.
    fireEvent.click(screen.getByRole('button', { name: 'Oeffnen' }));
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Eins' }));
    fireEvent.keyDown(document.activeElement!, { key: 'Escape' });
    expect(screen.queryByRole('dialog')).toBeNull();
    await Promise.resolve();
    expect(document.activeElement).toBe(screen.getByRole('button', { name: 'Anker' }));
  });

  it('closes only the picker on Escape inside a window', async () => {
    const onClose = vi.fn();
    const Inner: React.FC = () => {
      const anchor = React.useRef<HTMLButtonElement | null>(null);
      const [open, setOpen] = React.useState(false);
      return (
        <div>
          <button ref={anchor} type="button" onClick={() => setOpen(true)}>Waehlen</button>
          {open && (
            <PickerPopover anchorRef={anchor} label="Farbe" initialFocus="first" onClose={() => { onClose(); setOpen(false); }}>
              <button type="button">Eins</button>
            </PickerPopover>
          )}
        </div>
      );
    };
    register('PickersInnerPicker', Inner);
    const inner = { controlId: 'ip', module: 'PickersInnerPicker', state: {} } as ReturnType<typeof childControl>;
    const sent = mountAdapter(TLWindow, { title: 'Fenster', child: inner, closable: true });
    await settle();
    await userEvent.click(screen.getByRole('button', { name: 'Waehlen' }));
    await settle();
    expect(screen.getByRole('dialog', { name: 'Farbe' })).toBeTruthy();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(sent).not.toHaveBeenCalledWith('close', expect.anything());
    // Positive control: with the picker gone, the same key reaches the window.
    expect(screen.queryByRole('dialog', { name: 'Farbe' })).toBeNull();
    await userEvent.keyboard('{Escape}');
    await settle();
    expect(sent).toHaveBeenCalledWith('close', {});
  });
});

describe('TLColorInput', () => {
  // jsdom lays nothing out; the focus search skips elements without a client rect.
  beforeEach(() => {
    vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
  });

  const PALETTE = ['#e53935', '#fb8c00', '#fdd835', null];
  const STATE = {
    value: '#fb8c00',
    editable: true,
    palette: PALETTE,
    paletteColumns: 4,
    defaultPalette: PALETTE,
    canReset: true,
  };

  function field(): HTMLElement {
    return document.querySelector('button.tl-swatch')!;
  }

  async function open(state: Record<string, unknown> = STATE) {
    const sent = mountAdapter(TLColorInput, state);
    await settle();
    await userEvent.click(field());
    await settle();
    return sent;
  }

  async function openMixer(state: Record<string, unknown> = STATE) {
    const sent = await open(state);
    fireEvent.keyDown(screen.getByRole('tab', { name: 'js.colorInput.paletteTab' }), { key: 'ArrowRight' });
    await settle();
    return sent;
  }

  function neu(): HTMLElement {
    return screen.getByRole('img', { name: 'js.colorInput.new' });
  }

  /** The channels of a color the way jsdom reports an inline background. */
  function rgbOf(el: HTMLElement): number[] {
    return /rgb\((\d+), (\d+), (\d+)\)/.exec(el.style.backgroundColor)!.slice(1).map(Number);
  }

  function slider(name: string): HTMLElement {
    return screen.getByRole('slider', { name });
  }

  function press(el: HTMLElement, key: string, times = 1) {
    for (let i = 0; i < times; i++) fireEvent.keyDown(el, { key });
  }

  it('draws the field as a color swatch button, empty without a value', async () => {
    mountAdapter(TLColorInput, STATE);
    await settle();
    const full = field();
    expect(full.classList.contains('tl-swatch--color')).toBe(true);
    expect(full.getAttribute('data-tl-state')).toBeNull();
    cleanup();
    mountAdapter(TLColorInput, { ...STATE, value: null });
    await settle();
    expect(field().getAttribute('data-tl-state')).toBe('empty');
    expect(screen.getByRole('button', { name: /js\.colorInput\.noColor/ })).toBe(field());
  });

  it('carries the field state as attributes', async () => {
    mountAdapter(TLColorInput, { ...STATE, hasError: true, mandatory: true });
    await settle();
    expect(field().getAttribute('aria-invalid')).toBe('true');
    expect(field().getAttribute('aria-required')).toBe('true');
  });

  it('shows a read-only value as a span without a button (review focus 5)', async () => {
    mountAdapter(TLColorInput, { ...STATE, editable: false });
    await settle();
    expect(document.querySelector('button')).toBeNull();
    const swatch = document.querySelector('span.tl-swatch')!;
    expect(swatch.classList.contains('tl-swatch--color')).toBe(true);
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('draws a disabled field as a disabled button that opens nothing (review focus 5)', async () => {
    mountAdapter(TLColorInput, { ...STATE, editable: false, disabled: true });
    await settle();
    expect(field().hasAttribute('disabled')).toBe(true);
    await userEvent.click(field());
    await settle();
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('opens a dialog with the focus on the selected tab, and the arrow switches to the mixer', async () => {
    await open();
    expect(screen.getByRole('dialog', { name: 'js.colorInput.chooseColor' })).toBeTruthy();
    const palette = screen.getByRole('tab', { name: 'js.colorInput.paletteTab' });
    expect(document.activeElement).toBe(palette);
    fireEvent.keyDown(palette, { key: 'ArrowRight' });
    await settle();
    const mixer = screen.getByRole('tab', { name: 'js.colorInput.mixerTab' });
    expect(mixer.getAttribute('aria-selected')).toBe('true');
    expect(document.activeElement).toBe(mixer);
    expect(slider('js.colorInput.hue')).toBeTruthy();
  });

  it('has one tab stop in the palette, moves by arrow, takes a color by Space and confirms by Enter', async () => {
    const sent = await open();
    const options = screen.getAllByRole('option');
    expect(options).toHaveLength(3);
    expect(options.filter(o => o.tabIndex === 0)).toEqual([options[1]]);
    options[1].focus();
    fireEvent.keyDown(options[1], { key: 'ArrowRight' });
    expect(document.activeElement).toBe(options[2]);
    fireEvent.keyDown(options[2], { key: ' ' });
    expect(rgbOf(neu())).toEqual([253, 216, 53]);
    expect(options[2].getAttribute('aria-selected')).toBe('true');
    expect(sent).not.toHaveBeenCalledWith('valueChanged', expect.anything());
    fireEvent.keyDown(options[2], { key: 'Enter' });
    await settle();
    expect(sent).toHaveBeenCalledWith('valueChanged', { value: '#fdd835' });
    expect(screen.queryByRole('dialog')).toBeNull();
    await Promise.resolve();
    expect(document.activeElement).toBe(field());
  });

  it('has no tab stop and no error for a palette without a filled cell (review focus 3)', async () => {
    await open({ ...STATE, value: null, palette: [null, null, null] });
    expect(screen.queryAllByRole('option')).toHaveLength(0);
    const list = screen.getByRole('listbox', { name: 'js.colorInput.colors' });
    expect(list.querySelector('[tabindex="0"]')).toBeNull();
  });

  it('gives the mixer field a slider role whose arrows change the saturation by 1 %', async () => {
    await openMixer({ ...STATE, value: '#cc6666' });
    const sb = slider('js.colorInput.saturationBrightness');
    expect(sb.getAttribute('aria-valuetext')).toContain('js.colorInput.saturationBrightnessValue');
    expect(sb.getAttribute('aria-valuenow')).toBe('50');
    sb.focus();
    press(sb, 'ArrowRight');
    expect(sb.getAttribute('aria-valuenow')).toBe('51');
    press(sb, 'ArrowLeft', 2);
    expect(sb.getAttribute('aria-valuenow')).toBe('49');
  });

  it('keeps the hue over black, so that lifting the brightness is not red (review focus 4)', async () => {
    await openMixer({ ...STATE, value: '#000000' });
    const hue = slider('js.colorInput.hue');
    hue.focus();
    press(hue, 'PageDown', 12);
    expect(hue.getAttribute('aria-valuenow')).toBe('120');
    const sb = slider('js.colorInput.saturationBrightness');
    sb.focus();
    press(sb, 'ArrowRight', 20);
    press(sb, 'PageUp');
    const [r, g, b] = rgbOf(neu());
    expect(g).toBeGreaterThan(r);
    expect(g).toBeGreaterThan(b);
    expect(hue.getAttribute('aria-valuenow')).toBe('120');
  });

  it('stays at 0 and at 100 at the edge of the field (review focus 4)', async () => {
    await openMixer({ ...STATE, value: '#000000' });
    const sb = slider('js.colorInput.saturationBrightness');
    sb.focus();
    press(sb, 'ArrowDown', 3);
    press(sb, 'ArrowLeft', 3);
    expect(rgbOf(neu())).toEqual([0, 0, 0]);
    expect(sb.getAttribute('aria-valuenow')).toBe('0');
    cleanup();
    await openMixer({ ...STATE, value: '#ff0000' });
    const red = slider('js.colorInput.saturationBrightness');
    red.focus();
    press(red, 'ArrowUp', 3);
    press(red, 'ArrowRight', 3);
    expect(rgbOf(neu())).toEqual([255, 0, 0]);
    expect(red.getAttribute('aria-valuenow')).toBe('100');
  });

  it('confirms by Enter in the hex field when the value is valid (U1)', async () => {
    const sent = await open();
    const hex = screen.getByRole('textbox', { name: 'js.colorInput.hex' });
    await userEvent.clear(hex);
    await userEvent.type(hex, '#43a047{Enter}');
    await settle();
    // The field shows the color in capitals once it is valid; Enter confirms it like OK, which also files it
    // into the first free cell of the palette.
    expect(sent).toHaveBeenCalledWith('valueChanged', { value: '#43A047' });
    expect(sent).toHaveBeenCalledWith('paletteChanged', { palette: ['#e53935', '#fb8c00', '#fdd835', '#43A047'] });
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('does not confirm by Enter in the hex field while the value is invalid (U1)', async () => {
    const sent = await open();
    const hex = screen.getByRole('textbox', { name: 'js.colorInput.hex' });
    await userEvent.clear(hex);
    await userEvent.type(hex, '#43a{Enter}');
    await settle();
    expect(sent).not.toHaveBeenCalledWith('valueChanged', expect.anything());
    expect(screen.getByRole('dialog')).toBeTruthy();
  });
});
