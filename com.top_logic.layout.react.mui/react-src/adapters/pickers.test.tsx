// The pickers of wave 5a (spec 2026-10-17). This file starts with the shared keyboard of tab lists, useTabList, and
// the tab bar that runs on it. The tab bar draws icons through ThemeIcon, which needs no part.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { render, screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, useTabList, register } from 'tl-react-bridge';
import { PickerPopover } from '../../../com.top_logic.layout.react/react-src/controls/picker/PickerPopover';
import TLWindow from '../../../com.top_logic.layout.react/react-src/controls/TLWindow';
import TLTabBar from '../../../com.top_logic.layout.react/react-src/controls/TLTabBar';
import { childControl, mountAdapter, settle } from './wire-test-support';

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

  it('wraps Tab from the last element to the first', () => {
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
  });
});
