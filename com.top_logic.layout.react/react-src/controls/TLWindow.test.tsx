import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { React, register } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLWindow from './TLWindow';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const WINDOW_ID = 'window';

/** The stand-in for the content of the window. */
const WindowContent: React.FC<TLCellProps> = ({ controlId }) => <div id={controlId} />;

const CONTENT = { controlId: 'content', module: 'WindowTestContent', state: {} };

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('WindowTestContent', WindowContent);
  register('TLWindow', TLWindow);
  // jsdom implements no pointer capture.
  const captured = new Set<number>();
  HTMLElement.prototype.setPointerCapture = (id: number) => { captured.add(id); };
  HTMLElement.prototype.releasePointerCapture = (id: number) => { captured.delete(id); };
  HTMLElement.prototype.hasPointerCapture = (id: number) => captured.has(id);
});

afterEach(async () => {
  act(() => unmount(WINDOW_ID));
  document.getElementById(WINDOW_ID)?.remove();
  vi.unstubAllGlobals();
  await settle();
});

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so that the DOM
  // observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await settle();
});

/** Waits until the DOM observer of the bridge has seen the changes. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise<void>(resolve => setTimeout(resolve, 0));
  }
}

/** A resize observer reporting a size change only when the test says so (jsdom has no layout). */
class FakeResizeObserver {
  static readonly all = new Set<FakeResizeObserver>();

  private readonly _observed = new Set<Element>();

  constructor(private readonly _callback: ResizeObserverCallback) {
    FakeResizeObserver.all.add(this);
  }

  observe(element: Element): void { this._observed.add(element); }

  unobserve(element: Element): void { this._observed.delete(element); }

  disconnect(): void {
    this._observed.clear();
    FakeResizeObserver.all.delete(this);
  }

  deliver(element: Element): void {
    if (this._observed.has(element)) {
      this._callback([{ target: element } as ResizeObserverEntry], this as unknown as ResizeObserver);
    }
  }
}

/** The height the browser window has in jsdom. */
const VIEWPORT_HEIGHT = 768;

/** The margin a window keeps towards the edges of the browser window, before the stylesheet is measured. */
const MARGIN = 24;

/** Lays the surface out at the given height, as its content makes it, and reports the change. */
function laidOut(height: number): void {
  const el = surface();
  el.getBoundingClientRect = () => ({ x: 0, y: 0, left: 0, top: 0, right: 500, bottom: height, width: 500, height,
    toJSON: () => ({}) }) as DOMRect;
  act(() => {
    for (const observer of FakeResizeObserver.all) {
      observer.deliver(el);
    }
  });
}

function pointer(type: string, clientX: number, clientY: number): Event {
  const event = new MouseEvent(type, { bubbles: true, cancelable: true, clientX, clientY });
  Object.defineProperty(event, 'pointerId', { value: 1 });
  return event;
}

/** Mounts a window with the given state, answering every command it sends. */
function mountWindow(state: Record<string, unknown>): void {
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
  vi.stubGlobal('ResizeObserver', FakeResizeObserver);
  vi.stubGlobal('innerHeight', VIEWPORT_HEIGHT);
  const element = document.createElement('div');
  element.id = WINDOW_ID;
  document.body.appendChild(element);
  act(() => mount(WINDOW_ID, 'TLWindow', { title: 'Window', resizable: true, child: CONTENT, ...state }, 'w', '/ctx'));
}

/** The surface of the mounted window. */
function surface(): HTMLElement {
  return document.querySelector<HTMLElement>('.tl-dialog__surface')!;
}

describe('the configured size of a window', () => {
  it('uses a size in pixels as it is', () => {
    mountWindow({ width: '640px', height: '600px' });
    expect(surface().style.width).toBe('640px');
    expect(surface().style.height).toBe('600px');
  });

  it('relates a size in percent to the browser window', () => {
    mountWindow({ width: '80%', height: '75%' });
    expect(surface().style.width).toBe('calc(var(--tl-size-viewport-width) * 0.8)');
    expect(surface().style.height).toBe('calc(var(--tl-size-viewport-height) * 0.75)');
  });

  it('leaves the height to the content without a configured height', () => {
    mountWindow({ width: '500px' });
    expect(surface().style.height).toBe('');
  });
});

describe('the height of a window without a configured height', () => {
  it('grows with its content', () => {
    mountWindow({ width: '500px' });
    laidOut(300);
    expect(surface().style.minHeight).toBe('300px');
    laidOut(500);
    expect(surface().style.minHeight).toBe('500px');
  });

  it('does not shrink when its content does', () => {
    mountWindow({ width: '500px' });
    laidOut(500);
    laidOut(200);
    expect(surface().style.minHeight).toBe('500px');
  });

  it('never grows higher than the browser window', () => {
    mountWindow({ width: '500px' });
    laidOut(2000);
    expect(surface().style.minHeight).toBe((VIEWPORT_HEIGHT - 2 * MARGIN) + 'px');
  });

  it('starts at the height of its content when opened again', async () => {
    mountWindow({ width: '500px' });
    laidOut(500);
    act(() => unmount(WINDOW_ID));
    document.getElementById(WINDOW_ID)?.remove();
    // The DOM observer of the bridge sees the removal before the window opens again.
    await settle();

    mountWindow({ width: '500px' });
    expect(surface().style.minHeight).toBe('');
    laidOut(200);
    expect(surface().style.minHeight).toBe('200px');
  });

  it('keeps no minimum with a configured height', () => {
    mountWindow({ width: '500px', height: '600px' });
    laidOut(300);
    expect(surface().style.minHeight).toBe('');
    expect(surface().style.height).toBe('600px');
  });

  it('keeps no minimum of its own with a remembered size', () => {
    mountWindow({ width: '500px', customWidth: 400, customHeight: 250 });
    laidOut(500);
    expect(surface().style.minHeight).toBe('250px');
  });

  it('keeps no minimum once the user dragged its size', () => {
    mountWindow({ width: '500px' });
    laidOut(500);
    const handle = document.querySelector<HTMLElement>('.tl-dialog__resize[data-tl-edge="s"]')!;
    act(() => {
      handle.dispatchEvent(pointer('pointerdown', 0, 500));
      handle.dispatchEvent(pointer('pointermove', 0, 400));
      handle.dispatchEvent(pointer('pointerup', 0, 400));
    });
    laidOut(300);
    expect(surface().style.minHeight).toBe('');
    // Dragged at the centered window, which resizes symmetrically.
    expect(surface().style.height).toBe('300px');
  });
});
