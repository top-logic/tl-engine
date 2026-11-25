import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

/** The listeners of the state updates of the mounted controls, by control ID. */
const updates = new Map<string, (patch: Record<string, unknown>) => void>();

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (controlId: string, listener: (patch: Record<string, unknown>) => void) => updates.set(controlId, listener),
  unsubscribe: (controlId: string) => updates.delete(controlId),
}));

// The bridge first, as the page loads it.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { React, register, registerPart, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLDrawer from './TLDrawer';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const HOST_ID = 'host';
const DRAWER_ID = 'drawer';

/** The commands the drawer sent. */
let sent: string[] = [];

/** Content with a field and a button, so that the focus has somewhere to go. */
const Content: React.FC<TLCellProps> = ({ controlId }) => (
  <div id={controlId}>
    <input type="search" className="content-search" />
    <button type="button" className="content-button">Go</button>
  </div>
);

/** The drawer the host renders, set per test. */
let drawerControl: Record<string, unknown> = {};

/** A host rendering the drawer beside a button outside of it, the opener. */
const Host: React.FC<TLCellProps> = () => (
  <div>
    <button type="button" className="opener">Open</button>
    <TLChild control={drawerControl} />
  </div>
);

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  register('TLDrawer', TLDrawer);
  register('TestContent', Content);
  register('TestHost', Host);
});

afterEach(() => {
  act(() => unmount(HOST_ID));
  findMountPoint(HOST_ID)?.remove();
  vi.unstubAllGlobals();
  document.body.replaceChildren();
});

/** Mounts a container-anchored drawer into a host, closed. */
function mountDrawer(modal: boolean): void {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === DRAWER_ID) {
      sent.push(body.command);
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  drawerControl = {
    controlId: DRAWER_ID,
    module: 'TLDrawer',
    state: {
      open: false, position: 'left', size: 'narrow', anchor: 'container', modal,
      child: { controlId: 'content', module: 'TestContent', state: {} },
    },
  };
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, HOST_ID);
  document.body.appendChild(element);
  act(() => mount(HOST_ID, 'TestHost', {}, 'w', '/ctx'));
}

/** The server opens or closes the drawer. */
function setOpen(open: boolean): void {
  act(() => updates.get(DRAWER_ID)!({ open }));
}

function aside(): HTMLElement {
  return document.getElementById(DRAWER_ID)!;
}

function scrim(): HTMLElement | null {
  return document.querySelector<HTMLElement>('.tl-drawer__scrim');
}

function opener(): HTMLButtonElement {
  return document.querySelector<HTMLButtonElement>('.opener')!;
}

function key(element: Element, name: string): void {
  act(() => {
    element.dispatchEvent(new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true }));
  });
}

describe('a modal drawer', () => {
  it('puts a scrim before itself, is a dialog and takes the focus in', () => {
    mountDrawer(true);
    opener().focus();
    setOpen(true);

    expect(scrim()).not.toBeNull();
    expect(scrim()!.nextElementSibling).toBe(aside());
    expect(scrim()!.classList.contains('tl-drawer__scrim--viewport')).toBe(false);
    expect(aside().getAttribute('role')).toBe('dialog');
    expect(aside().getAttribute('aria-modal')).toBe('true');
    expect(aside().contains(document.activeElement)).toBe(true);
  });

  it('keeps the focus inside while it is open', () => {
    mountDrawer(true);
    setOpen(true);

    act(() => opener().focus());
    expect(aside().contains(document.activeElement)).toBe(true);
  });

  it('asks to close on a click on the scrim and on Escape', async () => {
    mountDrawer(true);
    setOpen(true);

    act(() => scrim()!.click());
    key(aside().querySelector('.content-button')!, 'Escape');
    await vi.waitFor(() => expect(sent).toEqual(['close', 'close']));
  });

  it('leaves Escape to a search field that still has text', async () => {
    mountDrawer(true);
    setOpen(true);
    const search = aside().querySelector<HTMLInputElement>('.content-search')!;
    search.value = 'abc';
    act(() => search.focus());

    key(search, 'Escape');
    search.value = '';
    key(search, 'Escape');
    await vi.waitFor(() => expect(sent).toEqual(['close']));
  });

  it('gives the focus back to the opener when it closes, and drops the scrim', () => {
    mountDrawer(true);
    opener().focus();
    setOpen(true);
    expect(document.activeElement).not.toBe(opener());

    setOpen(false);
    expect(scrim()).toBeNull();
    expect(document.activeElement).toBe(opener());
  });

  it('keeps the drawer element mounted when it opens', () => {
    mountDrawer(true);
    const closed = aside();
    setOpen(true);
    expect(aside()).toBe(closed);
  });
});

describe('a drawer that is not modal', () => {
  it('has no scrim, is no dialog and lets the focus leave', () => {
    mountDrawer(false);
    setOpen(true);

    expect(scrim()).toBeNull();
    expect(aside().getAttribute('role')).toBeNull();
    act(() => opener().focus());
    expect(document.activeElement).toBe(opener());
  });
});
