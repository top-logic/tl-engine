// The wire contract of TLTabBar, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { TabBarStateJson } from 'tl-react-bridge';
import { replacePart } from 'tl-react-bridge';
import MuiTabBarAdapter from './MuiTabBarAdapter';
import MuiBadgeAdapter from './MuiBadgeAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

replacePart('Badge', MuiBadgeAdapter);

/** Command choosing another tab. */
const CMD_SELECT_TAB = 'selectTab';

/** Argument of {@link CMD_SELECT_TAB}: the ID of the chosen tab. */
const ARG_TAB_ID = 'tabId';

/** An icon font image in its encoded form. */
const IMAGE_HOME = 'css:fas fa-home';

const TABS: TabBarStateJson.Tab[] = [
  { id: 'a', label: 'Allgemein', icon: IMAGE_HOME, hidden: false },
  { id: 'b', label: 'Details', icon: '', hidden: false },
  { id: 'c', label: 'Verlauf', icon: '', hidden: false },
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
    expect(root.classList).toContain('tl-fill');
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

/**
 * The horizontally scrolling element of the strip, given the extent jsdom does not compute: its
 * content `scrollWidth` wide in `clientWidth` of visible width, scrolled from 0.
 */
function scrollerOf(scrollWidth: number, clientWidth: number): HTMLElement {
  const scroller = screen.getByRole('tablist').parentElement!;
  expect(scroller.classList).toContain('MuiTabs-scroller');
  let scrollLeft = 0;
  Object.defineProperty(scroller, 'scrollWidth', { configurable: true, get: () => scrollWidth });
  Object.defineProperty(scroller, 'clientWidth', { configurable: true, get: () => clientWidth });
  Object.defineProperty(scroller, 'scrollLeft', {
    configurable: true,
    get: () => scrollLeft,
    set: (value: number) => { scrollLeft = value; },
  });
  return scroller;
}

describe('TLTabBar as MUI Tabs under the mouse wheel', () => {
  it('scrolls an overflowing strip sideways and takes the event, without selecting a tab', async () => {
    const sent = mountTabBar({ activeTabId: 'a' });
    const scroller = scrollerOf(800, 300);

    expect(fireEvent.wheel(scroller, { deltaY: 120 })).toBe(false);
    expect(scroller.scrollLeft).toBe(120);

    // At the end the strip keeps taking the event, so that nothing around it starts to scroll.
    expect(fireEvent.wheel(scroller, { deltaY: 1000 })).toBe(false);
    expect(scroller.scrollLeft).toBe(500);
    expect(fireEvent.wheel(scroller, { deltaY: 40 })).toBe(false);
    expect(scroller.scrollLeft).toBe(500);
    await settle();
    expect(sent).not.toHaveBeenCalled();
  });

  it('leaves the wheel to the browser while the tabs fit', () => {
    mountTabBar({ activeTabId: 'a' });
    const scroller = scrollerOf(300, 300);

    expect(fireEvent.wheel(scroller, { deltaY: 120 })).toBe(true);
    expect(scroller.scrollLeft).toBe(0);
  });
});

describe('TLTabBar with a badge as MUI Tabs', () => {
  it('draws the count after the label of a tab, and reads a badge of null as none', () => {
    mountTabBar({
      activeTabId: 'a',
      tabs: [
        { ...TABS[0], badge: { content: '5', role: 'error' } } as TabBarStateJson.Tab,
        { ...TABS[1], badge: null } as unknown as TabBarStateJson.Tab,
      ],
    });

    const counted = screen.getByRole('tab', { name: /^Allgemein\s*5$/ });
    expect(counted.querySelector('.MuiBadge-colorError')).not.toBeNull();
    const plain = screen.getByRole('tab', { name: 'Details' });
    expect(plain.querySelector('.MuiBadge-root')).toBeNull();
  });
});
