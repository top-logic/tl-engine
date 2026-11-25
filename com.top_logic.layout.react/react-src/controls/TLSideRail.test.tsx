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
import { React, register, registerPart } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLSideRail, { ARG_DRAWER, ARG_NAME, ARG_WIDTH, CMD_SET_DRAWER, CMD_SET_WIDTH, CMD_SHOW_SECTION } from './TLSideRail';
import TLSideRailSections, { CMD_TOGGLE_SECTION } from './TLSideRailSections';
import TLDrawer from './TLDrawer';
import ButtonPart from './button/ButtonPart';
import BadgePart from './chip/BadgePart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const RAIL_ID = 'rail';
const BODY_ID = 'rail-body';
const DRAWER_ID = 'rail-drawer';

/** A command a control of the rail sent. */
type Sent = { controlId: string; command: string; args: Record<string, unknown> };

/** The content of a section: a button, so that the focus has somewhere to go. */
const Content: React.FC<TLCellProps> = ({ controlId }) => (
  <button type="button" id={controlId} className="content">{controlId}</button>
);

/** The observers the rail created, so that a test can say "the area was resized". */
const observers: FakeResizeObserver[] = [];

class FakeResizeObserver {
  constructor(private readonly callback: () => void) {
    observers.push(this);
  }
  /** The elements this observer watches. */
  readonly targets: Element[] = [];
  observe(target: Element): void {
    this.targets.push(target);
  }
  unobserve(): void {}
  disconnect(): void {
    observers.splice(observers.indexOf(this), 1);
  }
  fire(): void {
    this.callback();
  }
}

let sent: Sent[] = [];

/** The width of the area of the rail, in px (at a 16 px rem, 44 rem are 704 px). */
let areaWidth = 1100;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  registerPart('Badge', BadgePart);
  register('TLSideRail', TLSideRail);
  register('TLSideRailSections', TLSideRailSections);
  register('TLDrawer', TLDrawer);
  register('TestContent', Content);
});

afterEach(() => {
  act(() => unmount(RAIL_ID));
  findMountPoint(RAIL_ID)?.remove();
  vi.unstubAllGlobals();
  observers.length = 0;
  document.body.replaceChildren();
});

/** The sections as the server writes them; `content` only for the column. */
function sections(withContent: boolean, overrides: Record<string, Record<string, unknown>> = {}) {
  const base: Record<string, unknown>[] = [
    { name: 'tasks', label: 'Tasks', icon: 'css:fa-solid fa-list-check', expanded: true, hidden: false,
      badge: { content: '3' } },
    { name: 'sources', label: 'Sources', expanded: false, hidden: false },
    { name: 'secret', label: 'Secret', expanded: true, hidden: true },
  ];
  return base.map((section) => ({
    ...section,
    ...(overrides[section.name as string] ?? {}),
    ...(withContent ? { content: { controlId: `content-${section.name}`, module: 'TestContent', state: {} } } : {}),
  }));
}

function bodyControl() {
  return { controlId: BODY_ID, module: 'TLSideRailSections', state: { sections: sections(true) } };
}

/** The state of a rail with three sections, one of them hidden, as the server writes it. */
function railState(overrides: Record<string, unknown> = {}): Record<string, unknown> {
  return {
    side: 'left',
    strip: true,
    open: true,
    drawerBelow: 44,
    resizable: true,
    width: 280,
    sectionWidth: 0,
    sections: sections(false),
    body: bodyControl(),
    drawer: {
      controlId: DRAWER_ID,
      module: 'TLDrawer',
      state: { open: false, position: 'left', size: 'narrow', anchor: 'container', modal: true, child: bodyControl() },
    },
    ...overrides,
  };
}

/** Mounts the rail into an area of the given width, and records the commands its controls send. */
async function mountRail(overrides: Record<string, unknown> = {}, width = 1100): Promise<void> {
  sent = [];
  areaWidth = width;
  vi.stubGlobal('ResizeObserver', FakeResizeObserver);
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (typeof body.command === 'string') {
      sent.push({ controlId: body.controlId, command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, RAIL_ID);
  Object.defineProperty(element, 'clientWidth', { get: () => areaWidth });
  document.body.appendChild(element);
  await act(async () => mount(RAIL_ID, 'TLSideRail', railState(overrides), 'w', '/ctx'));
}

/** The area becomes this wide, and the observer says so. */
function resize(width: number): void {
  areaWidth = width;
  act(() => observers.forEach((observer) => observer.fire()));
}

/** The server changes the state of a control. */
function patch(controlId: string, values: Record<string, unknown>): void {
  act(() => updates.get(controlId)!(values));
}

function rail(): HTMLElement {
  return document.getElementById(RAIL_ID)!;
}

function stripItems(): HTMLButtonElement[] {
  return [...rail().querySelectorAll<HTMLButtonElement>('.tl-side-rail__strip button')];
}

function column(): HTMLElement | null {
  return rail().querySelector<HTMLElement>('.tl-side-rail__column');
}

function drawer(): HTMLElement | null {
  return document.getElementById(DRAWER_ID);
}

function key(element: Element, name: string): void {
  act(() => {
    element.dispatchEvent(new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true }));
  });
}

/** Waits until the given command was sent, and answers its arguments. */
async function sentCommand(command: string): Promise<Record<string, unknown>> {
  let found: Sent | undefined;
  await vi.waitFor(() => {
    found = sent.find((entry) => entry.command === command);
    expect(found).toBeDefined();
  });
  return found!.args;
}

describe('the strip', () => {
  it('shows an icon per section that is not hidden, the badge of the section on it', async () => {
    await mountRail();
    expect(stripItems().map((item) => item.getAttribute('aria-label') ?? item.title)).toHaveLength(2);
    expect(stripItems()[0].querySelector('.tl-badge')!.textContent).toBe('3');
    expect(stripItems()[1].querySelector('.tl-badge')).toBeNull();
  });

  it('marks the icon of a section shown in the open rail', async () => {
    await mountRail();
    expect(stripItems()[0].getAttribute('aria-pressed')).toBe('true');
    expect(stripItems()[1].getAttribute('aria-pressed')).toBe('false');
  });

  it('asks the server to show the section of a clicked icon', async () => {
    await mountRail();
    act(() => stripItems()[1].click());
    expect(await sentCommand(CMD_SHOW_SECTION)).toEqual({ [ARG_NAME]: 'sources' });
  });

  it('is left out when it is turned off', async () => {
    await mountRail({ strip: false });
    expect(stripItems()).toEqual([]);
  });
});

describe('the column', () => {
  it('shows the sections, a collapsed one with its content mounted and hidden', async () => {
    await mountRail();
    const headers = [...column()!.querySelectorAll<HTMLButtonElement>('.tl-side-rail__header')];
    expect(headers.map((header) => header.textContent!.trim())).toEqual(['Tasks 3', 'Sources']);
    expect(headers[1].getAttribute('aria-expanded')).toBe('false');
    expect(document.getElementById('content-sources')!.closest('[hidden]')).not.toBeNull();
    expect(document.getElementById('content-tasks')!.closest('[hidden]')).toBeNull();
    expect(document.getElementById('content-secret')).toBeNull();
  });

  it('asks the server to toggle a section on a click on its header', async () => {
    await mountRail();
    act(() => column()!.querySelectorAll<HTMLButtonElement>('.tl-side-rail__header')[1].click());
    const args = await sentCommand(CMD_TOGGLE_SECTION);
    expect(args).toEqual({ [ARG_NAME]: 'sources' });
    expect(sent.find((entry) => entry.command === CMD_TOGGLE_SECTION)!.controlId).toBe(BODY_ID);
  });

  it('stays mounted and hidden while the rail is closed', async () => {
    await mountRail({ open: false });
    expect(column()!.hidden).toBe(true);
    expect(document.getElementById('content-tasks')).not.toBeNull();
  });

  it('has the width of the rail, the width a shown section asks for taking precedence', async () => {
    await mountRail();
    expect(column()!.style.width).toBe('280px');
    patch(RAIL_ID, { sectionWidth: 480 });
    expect(column()!.style.width).toBe('480px');
  });

  it('reports the width the user moves its inner edge to', async () => {
    await mountRail();
    key(rail().querySelector('.tl-side-rail__handle')!, 'ArrowRight');
    expect(await sentCommand(CMD_SET_WIDTH)).toEqual({ [ARG_WIDTH]: 296 });
  });

  it('fills the area without a handle when it is not resizable, e.g. in a split panel', async () => {
    await mountRail({ resizable: false });
    expect(rail().querySelector('.tl-side-rail__handle')).toBeNull();
    expect(column()!.style.width).toBe('');
  });
});

describe('the rail in a narrow area', () => {
  it('stays a column at every width with drawer-below 0', async () => {
    await mountRail({ drawerBelow: 0 }, 320);
    expect(column()).not.toBeNull();
    expect(drawer()).toBeNull();
    expect(sent.find((entry) => entry.command === CMD_SET_DRAWER)).toBeUndefined();
  });

  it('keeps only its strip, renders the drawer in place of the column and tells the server', async () => {
    await mountRail({}, 560);
    expect(column()).toBeNull();
    expect(drawer()).not.toBeNull();
    expect(rail().getAttribute('data-tl-state')).toContain('drawer');
    expect(drawer()!.style.insetInlineStart).toMatch(/^\d+px$/);
    expect(await sentCommand(CMD_SET_DRAWER)).toEqual({ [ARG_DRAWER]: true });
  });

  it('opens the drawer beside the strip once the rail has shrunk to it', async () => {
    await mountRail({}, 560);
    expect(observers.some((observer) => observer.targets.includes(rail()))).toBe(true);
    let railRight = 320;
    rail().getBoundingClientRect = () => ({ left: 0, right: railRight }) as DOMRect;

    // Still as wide as the column, e.g. while the split panel has not yet shrunk its pane.
    resize(560);
    expect(drawer()!.style.insetInlineStart).toBe('320px');

    // The pane shrank to the strip: the rail's own observer reports it.
    railRight = 41;
    act(() => observers.filter((observer) => observer.targets.includes(rail())).forEach((observer) => observer.fire()));
    expect(drawer()!.style.insetInlineStart).toBe('41px');
  });

  it('opens the section of a strip icon in the modal drawer, the focus inside', async () => {
    await mountRail({ open: false }, 560);
    const icon = stripItems()[0];
    act(() => icon.focus());
    act(() => icon.click());
    expect(await sentCommand(CMD_SHOW_SECTION)).toEqual({ [ARG_NAME]: 'tasks' });

    // The server answers by opening the drawer.
    patch(RAIL_ID, { open: true });
    patch(DRAWER_ID, { open: true });
    expect(document.querySelector('.tl-drawer__scrim')).not.toBeNull();
    expect(drawer()!.getAttribute('aria-modal')).toBe('true');
    expect(drawer()!.contains(document.activeElement)).toBe(true);
  });

  it('closes on Escape and hands the focus back to the strip icon', async () => {
    await mountRail({ open: false }, 560);
    const icon = stripItems()[0];
    act(() => icon.focus());
    patch(RAIL_ID, { open: true });
    patch(DRAWER_ID, { open: true });

    key(document.activeElement!, 'Escape');
    await vi.waitFor(() => expect(sent.some((entry) => entry.controlId === DRAWER_ID && entry.command === 'close'))
      .toBe(true));

    // The server answers by closing the drawer.
    patch(DRAWER_ID, { open: false });
    patch(RAIL_ID, { open: false });
    expect(document.activeElement).toBe(stripItems()[0]);
  });

  it('gives the column back when the area grows wide again, and tells the server', async () => {
    await mountRail({}, 560);
    await sentCommand(CMD_SET_DRAWER);
    sent = [];

    resize(1100);
    expect(column()).not.toBeNull();
    expect(drawer()).toBeNull();
    expect(await sentCommand(CMD_SET_DRAWER)).toEqual({ [ARG_DRAWER]: false });
  });

  it('measures the area it stands in once a hidden frame around it is shown', async () => {
    // In a hidden frame the rail has no positioned ancestor, and its parent - e.g. the pane of a
    // split panel - has no width.
    await mountRail({}, 0);
    expect(column()).not.toBeNull();
    expect(drawer()).toBeNull();

    // The frame is shown: the rail now stands in a positioned area that is wide enough.
    const area = document.createElement('div');
    Object.defineProperty(area, 'clientWidth', { get: () => 1100 });
    document.body.appendChild(area);
    Object.defineProperty(rail(), 'offsetParent', { get: () => area });
    act(() => observers.filter((observer) => observer.targets.includes(rail())).forEach((observer) => observer.fire()));
    expect(column()).not.toBeNull();
    expect(drawer()).toBeNull();
    expect(observers.some((observer) => observer.targets.includes(area))).toBe(true);
    expect(sent.find((entry) => entry.command === CMD_SET_DRAWER)).toBeUndefined();
  });
});

describe('a section without header', () => {
  it('shows its content at once, named by its label, without a header to collapse it', async () => {
    await mountRail({
      body: {
        controlId: BODY_ID,
        module: 'TLSideRailSections',
        state: { sections: sections(true, { tasks: { headless: true, expanded: true } }) },
      },
    });
    const section = document.getElementById('content-tasks')!.closest('section')!;
    expect(section.getAttribute('aria-label')).toBe('Tasks');
    expect(section.querySelector('.tl-side-rail__header')).toBeNull();
    expect(document.getElementById('content-tasks')!.closest('[hidden]')).toBeNull();
  });
});

describe('the side', () => {
  it('is written as a modifier of the root', async () => {
    await mountRail({ side: 'right' });
    expect(rail().classList.contains('tl-side-rail--end')).toBe(true);
  });
});
