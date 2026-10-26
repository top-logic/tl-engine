import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

/**
 * The SSE listeners subscribed by the bridge, by control ID: delivering an event to a control calls
 * its listeners like the SSE client does with a patch of the server.
 */
const sse = vi.hoisted(() => {
  const listeners = new Map<string, Set<(data: Record<string, unknown>) => void>>();
  return {
    listeners,
    deliver(controlId: string, data: Record<string, unknown>): void {
      for (const listener of listeners.get(controlId) ?? []) {
        listener(data);
      }
    },
  };
});

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: (controlId: string, listener: (data: Record<string, unknown>) => void) => {
    let set = sse.listeners.get(controlId);
    if (!set) {
      set = new Set();
      sse.listeners.set(controlId, set);
    }
    set.add(listener);
  },
  unsubscribe: (controlId: string, listener: (data: Record<string, unknown>) => void) => {
    const set = sse.listeners.get(controlId);
    set?.delete(listener);
    if (set?.size === 0) {
      sse.listeners.delete(controlId);
    }
  },
}));

// The bridge first, as the page loads it: the bridge entry reaches it through modules that the SSE client imports
// as well, and these must see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { React, register } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLSplitPanel from './TLSplitPanel';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command the split panel sent: its name and its arguments. */
type Sent = { command: string; args: Record<string, unknown> };

const SPLIT_ID = 'split';

/** The stand-in for the content of a pane. */
const PaneContent: React.FC<TLCellProps> = ({ controlId }) => <div id={controlId} />;

function content(controlId: string): Record<string, unknown> {
  return { controlId, module: 'SplitTestContent', state: {} };
}

/**
 * A pane in pixels, two panes in percent sharing the rest, and a collapsed pane at the end. Their widths in pixels
 * are given by {@link WIDTHS}.
 */
const CHILDREN = [
  { control: content('a'), size: 200, unit: 'px', minSize: 0, scrolling: 'auto', collapsed: false },
  { control: content('b'), size: 30, unit: '%', minSize: 0, scrolling: 'auto', collapsed: false },
  { control: content('c'), size: 70, unit: '%', minSize: 0, scrolling: 'auto', collapsed: false },
  { control: content('d'), size: 30, unit: 'px', minSize: 0, scrolling: 'auto', collapsed: true },
];

/** The widths the panes are laid out with (jsdom has no layout). */
const WIDTHS = [200, 240, 560, 30];

let sent: Sent[] = [];

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  // jsdom implements no pointer capture.
  const captured = new Set<number>();
  HTMLElement.prototype.setPointerCapture = (id: number) => { captured.add(id); };
  HTMLElement.prototype.releasePointerCapture = (id: number) => { captured.delete(id); };
  HTMLElement.prototype.hasPointerCapture = (id: number) => captured.has(id);
  register('SplitTestContent', PaneContent);
  register('TLSplitPanel', TLSplitPanel);
});

afterEach(async () => {
  act(() => unmount(SPLIT_ID));
  document.getElementById(SPLIT_ID)?.remove();
  vi.unstubAllGlobals();
  // Lets the DOM observer of the bridge see the removals while the test environment exists.
  await settle();
});

afterAll(async () => {
  // The bridge entry adds a tooltip host to the page. It goes while the test environment exists, so that the DOM
  // observer of the bridge does not see its removal by the teardown of the environment.
  document.body.replaceChildren();
  await settle();
});

/** Mounts the split panel, records the commands it sends, and gives its panes their {@link WIDTHS}. */
function mountSplit(): void {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === SPLIT_ID) {
      sent.push({ command: body.command, args: body.arguments });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const element = document.createElement('div');
  element.id = SPLIT_ID;
  document.body.appendChild(element);
  act(() => mount(SPLIT_ID, 'TLSplitPanel', { orientation: 'horizontal', resizable: true, children: CHILDREN }, 'w', '/ctx'));
  panes().forEach((pane, i) => {
    Object.defineProperty(pane, 'offsetWidth', { configurable: true, get: () => WIDTHS[i] });
  });
}

function panes(): HTMLElement[] {
  return [...document.querySelectorAll<HTMLElement>(`#${SPLIT_ID} > .tl-split__pane`)];
}

function handle(): HTMLElement {
  return document.querySelector<HTMLElement>(`#${SPLIT_ID} .tl-split__handle`)!;
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  for (let round = 0; round < 5; round++) {
    await new Promise<void>(resolve => setTimeout(resolve, 0));
  }
}

function pointer(type: string, clientX: number): Event {
  const event = new MouseEvent(type, { bubbles: true, cancelable: true, clientX, clientY: 0 });
  Object.defineProperty(event, 'pointerId', { value: 1 });
  return event;
}

function flexes(): string[] {
  return panes().map(pane => pane.style.flex);
}

describe('the sizes of a split panel', () => {
  it('keeps a pane in pixels at its size and lets the panes in percent share the rest', () => {
    mountSplit();
    expect(flexes()).toEqual(['0 1 200px', '30 1 0%', '70 1 0%', '0 0 auto']);
  });

  it('sends each open pane in its unit after a drag, and renders the panes as the sent sizes do', async () => {
    mountSplit();
    const separator = handle();
    act(() => {
      separator.dispatchEvent(pointer('pointerdown', 100));
      separator.dispatchEvent(pointer('pointermove', 150));
      separator.dispatchEvent(pointer('pointerup', 150));
    });
    await settle();

    expect(sent).toEqual([{
      command: 'updateSizes',
      args: { sizes: { a: 250, b: 25.33, c: 74.67 } },
    }]);
    expect(flexes()).toEqual(['0 1 250px', '25.33 1 0%', '74.67 1 0%', '0 0 auto']);

    // The server answers with the sizes it was sent.
    act(() => sse.deliver(SPLIT_ID, {
      children: CHILDREN.map(child => ({
        ...child,
        size: ({ a: 250, b: 25.33, c: 74.67 } as Record<string, number>)[(child.control as { controlId: string }).controlId]
          ?? child.size,
      })),
    }));
    expect(flexes()).toEqual(['0 1 250px', '25.33 1 0%', '74.67 1 0%', '0 0 auto']);
  });

  it('gives a pane in percent its weight back when the drag ends at the sizes the panes had', async () => {
    mountSplit();
    const separator = handle();
    act(() => {
      separator.dispatchEvent(pointer('pointerdown', 100));
      separator.dispatchEvent(pointer('pointermove', 150));
      separator.dispatchEvent(pointer('pointermove', 100));
      separator.dispatchEvent(pointer('pointerup', 100));
    });
    await settle();

    expect(sent).toEqual([{ command: 'updateSizes', args: { sizes: { a: 200, b: 30, c: 70 } } }]);
    // The server state renders as before, so the rendering leaves the elements as they are: none of them may be left
    // at the pixel size the drag wrote onto it.
    act(() => sse.deliver(SPLIT_ID, { children: CHILDREN.map(child => ({ ...child })) }));
    expect(flexes()).toEqual(['0 1 200px', '30 1 0%', '70 1 0%', '0 0 auto']);
  });

  it('sends each open pane in its unit after a key move, and renders the panes as the sent sizes do', async () => {
    mountSplit();
    const separator = handle();
    act(() => {
      separator.dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true, cancelable: true }));
      separator.dispatchEvent(new KeyboardEvent('keyup', { key: 'ArrowRight', bubbles: true, cancelable: true }));
    });
    await settle();

    expect(sent).toHaveLength(1);
    const sizes = sent[0].args.sizes as Record<string, number>;
    expect(Object.keys(sizes).sort()).toEqual(['a', 'b', 'c']);
    expect(sizes.a).toBeGreaterThan(200);
    expect(sizes.b + sizes.c).toBeCloseTo(100, 1);
    expect(flexes()).toEqual([`0 1 ${sizes.a}px`, `${sizes.b} 1 0%`, `${sizes.c} 1 0%`, '0 0 auto']);
  });
});
