// The wire contract of TLTabBar, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { FILL_CLASS } from 'tl-react-bridge';
import type { TabBarStateJson } from 'tl-react-bridge';
import MuiTabBarAdapter from './MuiTabBarAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

/** Command choosing another tab. */
const CMD_SELECT_TAB = 'selectTab';

/** Argument of {@link CMD_SELECT_TAB}: the ID of the chosen tab. */
const ARG_TAB_ID = 'tabId';

/** An icon font image in its encoded form. */
const IMAGE_HOME = 'css:fas fa-home';

const TABS: TabBarStateJson.Tab[] = [
  { id: 'a', label: 'Allgemein', icon: IMAGE_HOME },
  { id: 'b', label: 'Details', icon: '' },
  { id: 'c', label: 'Verlauf', icon: '' },
];

function mountTabBar(state: Partial<TabBarStateJson>) {
  return mountAdapter(MuiTabBarAdapter, { tabs: TABS, ...state });
}

function selected(tabId: string) {
  return [CMD_SELECT_TAB, { [ARG_TAB_ID]: tabId }];
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLTabBar as MUI Tabs', () => {
  it('renders an MUI tab per tab, the selected one marked, above the content of the selected tab', () => {
    mountTabBar({ activeTabId: 'a', activeContent: childControl('content-a', 'Inhalt A'), cssClass: 'my-tabs' });

    const tabs = screen.getAllByRole('tab');
    expect(tabs.map(tab => tab.textContent)).toEqual(['Allgemein', 'Details', 'Verlauf']);
    expect(tabs[0].classList).toContain('MuiTab-root');
    expect(tabs[0].getAttribute('aria-selected')).toBe('true');
    expect(tabs[1].getAttribute('aria-selected')).toBe('false');
    expect(tabs[0].id).toBe(CONTROL_ID + '-tab-a');
    expect(tabs[0].querySelector('i.fa-home')).not.toBeNull();

    const panel = screen.getByRole('tabpanel');
    expect(panel.getAttribute('aria-labelledby')).toBe(CONTROL_ID + '-tab-a');
    expect(panel.querySelector('#content-a')?.textContent).toBe('Inhalt A');
    expect(tabs[0].getAttribute('aria-controls')).toBe(panel.id);
  });

  it('fills its container and carries the control ID and the configured class', () => {
    mountTabBar({ activeTabId: 'a', cssClass: 'my-tabs' });

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList).toContain(FILL_CLASS);
    expect(root.classList).toContain('my-tabs');
  });

  it('selects no tab for an ID naming none', () => {
    mountTabBar({ activeTabId: 'x' });

    expect(screen.getAllByRole('tab').every(tab => tab.getAttribute('aria-selected') === 'false')).toBe(true);
    expect(screen.getByRole('tabpanel').hasAttribute('aria-labelledby')).toBe(false);
  });

  it('sends selectTab with the ID of the tab clicked', async () => {
    const sent = mountTabBar({ activeTabId: 'a' });

    await userEvent.click(screen.getByRole('tab', { name: 'Details' }));
    await settle();

    expect(sent.mock.calls).toEqual([selected('b')]);
  });

  it('selects the tab the arrow key reaches', async () => {
    const sent = mountTabBar({ activeTabId: 'a' });

    screen.getByRole('tab', { name: 'Allgemein' }).focus();
    await userEvent.keyboard('{ArrowRight}');
    await settle();

    expect(sent.mock.calls).toEqual([selected('b')]);
  });

  it('selects the first and the last tab for Home and End', async () => {
    const sent = mountTabBar({ activeTabId: 'b' });

    screen.getByRole('tab', { name: 'Details' }).focus();
    await userEvent.keyboard('{End}');
    await userEvent.keyboard('{Home}');
    await settle();

    expect(sent.mock.calls).toEqual([selected('c'), selected('a')]);
  });

  it('sends nothing when a tab receives the focus without a gesture of the user', async () => {
    const sent = mountTabBar({ activeTabId: 'a' });

    // A dialog that closes gives the focus back to the tab clicked before it opened.
    screen.getByRole('tab', { name: 'Verlauf' }).focus();
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('sends nothing when the selected tab is clicked', async () => {
    const sent = mountTabBar({ activeTabId: 'a' });

    await userEvent.click(screen.getByRole('tab', { name: 'Allgemein' }));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('renders nothing while hidden', () => {
    mountTabBar({ activeTabId: 'a', hidden: true });

    expect(screen.queryByRole('tablist')).toBeNull();
    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
