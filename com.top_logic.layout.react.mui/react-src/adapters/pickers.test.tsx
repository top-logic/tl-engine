// The pickers of wave 5a (spec 2026-10-17). This file starts with the shared keyboard of tab lists, useTabList, and
// the tab bar that runs on it. The tab bar draws icons through ThemeIcon, which needs no part.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup, fireEvent } from '@testing-library/react';
import { React, useTabList } from 'tl-react-bridge';
import TLTabBar from '../../../com.top_logic.layout.react/react-src/controls/TLTabBar';
import { mountAdapter, settle } from './wire-test-support';

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
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
