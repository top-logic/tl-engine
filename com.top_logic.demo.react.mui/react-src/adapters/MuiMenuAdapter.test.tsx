// The wire contract of TLMenu, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest';
import { screen, cleanup, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ANCHORED_OVERLAY_ATTR, pressClosedSurface } from 'tl-react-bridge';
import type { MenuStateJson } from 'tl-react-bridge';
import MuiMenuAdapter from './MuiMenuAdapter';
import { CONTROL_ID, mountAdapter, patchState, settle } from './wire-test-support';
import type { MountOptions } from './wire-test-support';

/** Command choosing an item. */
const CMD_SELECT_ITEM = 'selectItem';

/** Argument of {@link CMD_SELECT_ITEM}: the ID of the chosen item. */
const ARG_ITEM_ID = 'itemId';

/** Command closing the menu without a choice. */
const CMD_CLOSE = 'close';

/** The ID of the element the menu hangs off. */
const ANCHOR_ID = 'trigger';

/** An icon font image in its encoded form. */
const IMAGE_COPY = 'css:fas fa-copy';

type Entry = Partial<MenuStateJson.Entry>;

const ITEMS: Entry[] = [
  { type: 'header', label: 'Bearbeiten' },
  { type: 'item', id: 'copy', label: 'Kopieren', icon: IMAGE_COPY },
  { type: 'item', id: 'paste', label: 'Einfügen', disabled: true },
  { type: 'item', id: 'bold', label: 'Fett', active: true, cssClasses: 'cmd-bold' },
  { type: 'separator' },
  { type: 'item', id: 'delete', label: 'Löschen', tone: 'danger' },
];

/** Called for a click on the trigger that would open the menu (it was not closed by the press). */
let triggerOpens = vi.fn();

function mountMenu(state: Partial<MenuStateJson>, options?: MountOptions) {
  // The trigger the menu hangs off, opening the menu on a click as TLMenuRegion does: unless the
  // press of the same gesture has closed it.
  const anchor = document.createElement('button');
  anchor.id = ANCHOR_ID;
  anchor.addEventListener('click', () => {
    if (!pressClosedSurface()) {
      triggerOpens();
    }
  });
  document.body.appendChild(anchor);
  return mountAdapter(MuiMenuAdapter,
    { open: true, anchorId: ANCHOR_ID, items: ITEMS as MenuStateJson.Entry[], ...state }, options);
}

function chosen(itemId: string) {
  return [CMD_SELECT_ITEM, { [ARG_ITEM_ID]: itemId }];
}

beforeEach(() => {
  triggerOpens = vi.fn();
  // The focus trap focuses only an element that is rendered, which jsdom, laying out nothing,
  // denies.
  vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
});

afterEach(() => {
  cleanup();
  document.getElementById(ANCHOR_ID)?.remove();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

describe('TLMenu as MUI Menu', () => {
  it('renders an MUI menu item per item, with header, separator and the marks of the items', () => {
    mountMenu({ cssClass: 'my-menu' });

    const menu = screen.getByRole('menu');
    expect(menu.id).toBe(CONTROL_ID);
    expect(menu.classList).toContain('my-menu');
    expect(menu.closest(`[${ANCHORED_OVERLAY_ATTR}]`)).not.toBeNull();

    const items = screen.getAllByRole('menuitem');
    expect(items.map(item => item.textContent)).toEqual(['Kopieren', 'Einfügen', 'Fett', 'Löschen']);
    expect(items[0].classList).toContain('MuiMenuItem-root');
    expect(items[0].querySelector('.MuiListItemIcon-root i.fa-copy')).not.toBeNull();
    expect(items[1].getAttribute('aria-disabled')).toBe('true');
    expect(items[2].getAttribute('aria-current')).toBe('true');
    expect(items[2].classList).toContain('Mui-selected');
    expect(items[2].classList).toContain('cmd-bold');

    expect(screen.getByText('Bearbeiten').classList).toContain('MuiListSubheader-root');
    expect(menu.querySelector('.MuiDivider-root')).not.toBeNull();
  });

  it('opens at a point of the viewport for a context menu', () => {
    mountAdapter(MuiMenuAdapter, { open: true, anchorX: 120, anchorY: 80, items: ITEMS });

    expect(screen.getAllByRole('menuitem')).toHaveLength(4);
  });

  it('is at most as high as the side of its anchor with more room, and scrolls', () => {
    vi.stubGlobal('innerHeight', 900);
    mountAdapter(MuiMenuAdapter, { open: true, anchorX: 120, anchorY: 462, items: ITEMS });

    // 438px below the point, 462px above it; the space the placement keeps free is taken off.
    const surface = screen.getByRole('menu').parentElement!;
    expect(surface.style.maxHeight).toBe('446px');
    expect(getComputedStyle(surface).overflow).toBe('auto');
  });

  it('is not shown while closed, or while its anchor is missing', () => {
    mountMenu({ open: false });
    expect(screen.queryByRole('menu')).toBeNull();
    cleanup();

    mountAdapter(MuiMenuAdapter, { open: true, anchorId: 'nowhere', items: ITEMS });
    expect(screen.queryByRole('menu')).toBeNull();
  });

  it('sends selectItem with the ID of the item chosen', async () => {
    const sent = mountMenu({});

    await userEvent.click(screen.getByRole('menuitem', { name: 'Löschen' }));
    await settle();

    expect(sent.mock.calls).toEqual([chosen('delete')]);
  });

  it('moves the focus to the first item and sends selectItem for it on Enter', async () => {
    const sent = mountMenu({});

    await waitFor(() => expect(document.activeElement).toBe(screen.getByRole('menuitem', { name: 'Kopieren' })));
    await userEvent.keyboard('{Enter}');
    await settle();

    expect(sent.mock.calls).toEqual([chosen('copy')]);
  });

  it('gives the focus back to the trigger when it closes', async () => {
    const outer = document.createElement('button');
    document.body.appendChild(outer);
    outer.focus();
    try {
      mountMenu({});
      await waitFor(() => expect(screen.getByRole('menu').contains(document.activeElement)).toBe(true));

      patchState({ open: false });

      await waitFor(() => expect(document.activeElement).toBe(outer));
    } finally {
      outer.remove();
    }
  });

  it('sends nothing for a disabled item', async () => {
    const sent = mountMenu({});

    // MUI turns off pointer events on a disabled item; the click is dispatched anyway.
    await userEvent.setup({ pointerEventsCheck: 0 }).click(screen.getByRole('menuitem', { name: 'Einfügen' }));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('sends close for Escape, which does not reach a binding around the menu (a window)', async () => {
    const outer = vi.fn();
    const sent = mountMenu({}, { outer: { gesture: 'ESCAPE', onGesture: outer } });

    await userEvent.keyboard('{Escape}');
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLOSE, {}]]);
    expect(outer).not.toHaveBeenCalled();
  });

  it('sends close for a press outside, which reaches the element below', async () => {
    const below = vi.fn();
    const outside = document.createElement('button');
    outside.addEventListener('click', below);
    document.body.appendChild(outside);
    try {
      const sent = mountMenu({});
      await screen.findByRole('menu');

      await userEvent.click(outside);
      await settle();

      expect(sent.mock.calls).toEqual([[CMD_CLOSE, {}]]);
      expect(below).toHaveBeenCalledTimes(1);
    } finally {
      outside.remove();
    }
  });

  it('closes on a press on its trigger, which then does not open it again', async () => {
    const sent = mountMenu({});
    await screen.findByRole('menu');

    await userEvent.click(document.getElementById(ANCHOR_ID)!);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLOSE, {}]]);
    expect(triggerOpens).not.toHaveBeenCalled();
  });

  it('sends nothing for a press inside the menu beside its items', async () => {
    const sent = mountMenu({});

    await userEvent.click(await screen.findByText('Bearbeiten'));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});
