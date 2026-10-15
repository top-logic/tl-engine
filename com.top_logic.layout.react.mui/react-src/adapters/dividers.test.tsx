// The lines a control draws between its content - between the groups of a toolbar, between the
// sections of a toolbar menu and of a menu, between the items of the sidebar - drawn by the MUI
// adapter of the part Divider: one replacement of the part reaches all of them.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { replacePart } from 'tl-react-bridge';
import TLToolbar from '../../../com.top_logic.layout.react/react-src/controls/TLToolbar';
import TLMenu from '../../../com.top_logic.layout.react/react-src/controls/TLMenu';
import TLSidebar from '../../../com.top_logic.layout.react/react-src/controls/TLSidebar';
import MuiDividerAdapter from './MuiDividerAdapter';
import MuiMenuTriggerAdapter from './MuiMenuTriggerAdapter';
import MuiDisclosureToggleAdapter from './MuiDisclosureToggleAdapter';
import MuiBadgeAdapter from './MuiBadgeAdapter';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';
import { childControl, mountAdapter, settle } from './wire-test-support';

replacePart('Divider', MuiDividerAdapter);
// The toolbar draws the triggers of its menus, the sidebar its toggle and its counts.
replacePart('MenuTrigger', MuiMenuTriggerAdapter);
replacePart('DisclosureToggle', MuiDisclosureToggleAdapter);
replacePart('Spinner', MuiSpinnerAdapter);
replacePart('Badge', MuiBadgeAdapter);

/** The name of the trigger of the overflow menu: the key, as the test stub translates each key to itself. */
const LABEL_OVERFLOW = 'js.toolbar.overflow';

/** The class the toolbar finds the divider between two groups by, to read its width. */
const TOOLBAR_SEPARATOR_CLASS = 'tlToolbar__separator';

/** The width of a button of a toolbar, as the stubbed layout reports it. */
const UNIT_WIDTH = 100;

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
});

/** Asserts that the given element is a divider of Material UI and not the one of TopLogic. */
function expectMuiDivider(element: Element) {
  expect(element.classList).toContain('MuiDivider-root');
  expect(element.classList).not.toContain('tl-divider');
}

/** A toolbar of two inline groups of one button each. */
function twoGroups(overflow?: string) {
  return {
    groups: [
      { name: 'edit', display: 'inline', items: [childControl('i1', 'Kopieren')] },
      { name: 'view', display: 'inline', items: [childControl('i2', 'Ansicht')] },
    ],
    overflow,
  };
}

/**
 * Lays the toolbar out as a browser would: each button has the width {@link UNIT_WIDTH}, the
 * divider between the groups the given width, the toolbar the given width; jsdom lays nothing out.
 */
function layOut(separatorWidth: number, toolbarWidth: number) {
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  vi.spyOn(HTMLElement.prototype, 'getBoundingClientRect').mockImplementation(function (this: HTMLElement) {
    const width = this.hasAttribute('data-tlunit') ? UNIT_WIDTH
      : this.classList.contains(TOOLBAR_SEPARATOR_CLASS) ? separatorWidth
      : this.getAttribute('role') === 'toolbar' ? toolbarWidth
      : 0;
    return { x: 0, y: 0, top: 0, left: 0, right: width, bottom: 0, width, height: 0, toJSON: () => ({}) } as DOMRect;
  });
}

describe('the dividers of a toolbar (TLToolbar) as MUI dividers', () => {
  it('draws a vertical MUI divider between two groups, found by the class of the toolbar', () => {
    mountAdapter(TLToolbar, twoGroups());

    const dividers = screen.getAllByRole('separator');
    expect(dividers).toHaveLength(1);
    expectMuiDivider(dividers[0]);
    expect(dividers[0].classList).toContain('MuiDivider-vertical');
    expect(dividers[0].classList).toContain('MuiDivider-flexItem');
    expect(dividers[0].classList).toContain(TOOLBAR_SEPARATOR_CLASS);
    expect(dividers[0].getAttribute('aria-orientation')).toBe('vertical');
    expect(dividers[0].previousElementSibling!.textContent).toBe('Kopieren');
    expect(dividers[0].nextElementSibling!.textContent).toBe('Ansicht');
  });

  it('draws no divider for a single group', () => {
    mountAdapter(TLToolbar, { groups: [twoGroups().groups[0]] });

    expect(screen.queryByRole('separator')).toBeNull();
  });

  it('counts the width of the MUI divider when it decides what fits', async () => {
    // Both buttons fit beside each other (200), but not with the divider between them (250).
    layOut(50, 240);
    mountAdapter(TLToolbar, twoGroups('trailing'));
    await settle();

    expect(screen.getByRole('button', { name: LABEL_OVERFLOW })).toBeDefined();
    expect(screen.queryByRole('separator')).toBeNull();
  });

  it('collapses nothing while buttons and divider fit', async () => {
    layOut(50, 260);
    mountAdapter(TLToolbar, twoGroups('trailing'));
    await settle();

    expect(screen.queryByRole('button', { name: LABEL_OVERFLOW })).toBeNull();
    expectMuiDivider(screen.getByRole('separator'));
  });

  it('draws a horizontal MUI divider between the sections of a menu group', async () => {
    mountAdapter(TLToolbar, {
      groups: [{
        name: 'more', display: 'menu', label: 'Mehr', items: [childControl('i1', 'Kopieren')],
        subGroups: [{ name: 'danger', display: 'inline', items: [childControl('i2', 'Löschen')] }],
      }],
    });
    await userEvent.click(screen.getByRole('button', { name: 'Mehr' }));

    const menu = document.body.querySelector('.tl-menu[role="menu"]')!;
    const divider = menu.querySelector('hr')!;
    expectMuiDivider(divider);
    expect(divider.classList).not.toContain('MuiDivider-vertical');
    // The menu gives the space around the divider through the element around it.
    const separator = divider.parentElement!;
    expect(divider.classList).not.toContain('tlMenu__separator');
    expect(separator.classList).toContain('tlMenu__separator');
    expect(separator.parentElement).toBe(menu);
    expect(separator.previousElementSibling!.textContent).toBe('Kopieren');
    expect(separator.nextElementSibling!.textContent).toBe('Löschen');
  });
});

describe('the dividers of a menu (TLMenu) as MUI dividers', () => {
  it('draws a separator entry as a horizontal MUI divider between the entries', () => {
    mountAdapter(TLMenu, {
      open: true, anchorX: 10, anchorY: 10,
      items: [
        { type: 'item', id: 'a', label: 'Öffnen' },
        { type: 'separator', id: 's', label: '' },
        { type: 'item', id: 'b', label: 'Löschen' },
      ],
    });

    const menu = screen.getByRole('menu');
    const divider = menu.querySelector('hr')!;
    expectMuiDivider(divider);
    expect(divider.parentElement!.classList).toContain('tlMenu__separator');
    expect(Array.from(menu.children).map(child => child.tagName)).toEqual(['BUTTON', 'DIV', 'BUTTON']);
  });
});

describe('the dividers of the sidebar (TLSidebar) as MUI dividers', () => {
  const INBOX = { id: 'inbox', type: 'nav', label: 'Eingang', icon: 'css:fa-solid fa-inbox' };
  const ARCHIVE = { id: 'archive', type: 'nav', label: 'Archiv', icon: 'css:fa-solid fa-box' };
  const SEPARATOR = { id: 'sep', type: 'separator' };

  it('draws a separator item as a horizontal MUI divider with the space of the sidebar', async () => {
    mountAdapter(TLSidebar, { items: [INBOX, SEPARATOR, ARCHIVE], activeItemId: 'inbox', collapsed: false, drawerOpen: false });
    await settle();

    const dividers = document.querySelectorAll('hr');
    expect(dividers).toHaveLength(1);
    expectMuiDivider(dividers[0]);
    expect(dividers[0].classList).not.toContain('tlSidebar__separator');
    expect(dividers[0].parentElement!.classList).toContain('tlSidebar__separator');
  });

  it('draws no divider for a sidebar without separator items', async () => {
    mountAdapter(TLSidebar, { items: [INBOX, ARCHIVE], activeItemId: 'inbox', collapsed: false, drawerOpen: false });
    await settle();

    expect(document.querySelector('hr')).toBeNull();
  });
});
