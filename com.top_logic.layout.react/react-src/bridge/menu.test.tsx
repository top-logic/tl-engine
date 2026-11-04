import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { Menu, MenuItem } from './menu';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** What the menu stands at: the trigger button or a point (a context menu). */
type AnchorKind = 'trigger' | 'point';

let container: HTMLDivElement;
let root: Root;
let closes: number;
let setOpen: (open: boolean) => void = () => {};
let originalGetClientRects: typeof Element.prototype.getClientRects;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // jsdom lays nothing out; the menu only roves over and focuses entries that are rendered.
  originalGetClientRects = Element.prototype.getClientRects;
  Element.prototype.getClientRects = function () {
    return [{}] as unknown as DOMRectList;
  };
});

afterAll(() => {
  Element.prototype.getClientRects = originalGetClientRects;
});

beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
  closes = 0;
});

afterEach(() => {
  act(() => root.unmount());
  container.remove();
});

/** A trigger button, a field the focus is in before opening, and the menu. */
const Harness: React.FC<{ anchorKind: AnchorKind; disabled: boolean }> = ({ anchorKind, disabled }) => {
  const [open, setOpenState] = React.useState(false);
  const [trigger, setTrigger] = React.useState<HTMLButtonElement | null>(null);
  setOpen = setOpenState;
  const close = () => {
    closes++;
    setOpenState(false);
  };
  return (
    <>
      <button type="button" id="trigger" ref={setTrigger}>Menu</button>
      <input id="origin" />
      <Menu open={open} anchor={anchorKind === 'trigger' ? trigger : { x: 10, y: 10 }} onClose={close}>
        <MenuItem id="first" label="First" disabled={disabled} onSelect={close} />
        <MenuItem id="second" label="Second" disabled={disabled} onSelect={close} />
      </Menu>
    </>
  );
};

function byId(id: string): HTMLElement {
  return document.getElementById(id)!;
}

/** Renders the harness, focuses the origin field, and opens the menu. */
function openMenu(anchorKind: AnchorKind, disabled = false): void {
  act(() => root.render(<Harness anchorKind={anchorKind} disabled={disabled} />));
  byId('origin').focus();
  act(() => setOpen(true));
}

function pressEscape(): void {
  const target = document.activeElement as HTMLElement;
  act(() => {
    target.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true }));
  });
}

function menuOpen(): boolean {
  return document.querySelector('[role="menu"]') !== null;
}

describe('Menu', () => {
  it('moves the focus into the menu when it opens', () => {
    openMenu('trigger');
    expect(document.activeElement).toBe(byId('first'));
  });

  it('closes with Escape when all entries are disabled', () => {
    openMenu('trigger', true);
    expect(menuOpen()).toBe(true);
    expect(document.querySelector('[role="menu"]')!.contains(document.activeElement)).toBe(true);
    pressEscape();
    expect(closes).toBe(1);
    expect(menuOpen()).toBe(false);
  });

  it('gives the focus to the trigger when closed with Escape', () => {
    openMenu('trigger');
    pressEscape();
    expect(menuOpen()).toBe(false);
    expect(document.activeElement).toBe(byId('trigger'));
  });

  it('gives the focus to the trigger when closed with Escape although all entries are disabled', () => {
    openMenu('trigger', true);
    pressEscape();
    expect(document.activeElement).toBe(byId('trigger'));
  });

  it('gives the focus back to where it was when an entry is chosen', () => {
    openMenu('trigger');
    act(() => byId('first').click());
    expect(menuOpen()).toBe(false);
    expect(document.activeElement).toBe(byId('origin'));
  });

  it('gives the focus back to where it was when closed from outside', () => {
    openMenu('trigger');
    act(() => setOpen(false));
    expect(document.activeElement).toBe(byId('origin'));
  });

  it('gives the focus back to where it was after a close without Escape following one with Escape', () => {
    openMenu('trigger');
    pressEscape();
    byId('origin').focus();
    act(() => setOpen(true));
    act(() => setOpen(false));
    expect(document.activeElement).toBe(byId('origin'));
  });

  it('gives the focus back to where it was when a menu at a point is closed with Escape', () => {
    openMenu('point');
    expect(document.activeElement).toBe(byId('first'));
    pressEscape();
    expect(menuOpen()).toBe(false);
    expect(document.activeElement).toBe(byId('origin'));
  });
});
