import React, { act } from 'react';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';

/**
 * The SSE listeners subscribed by the bridge, by control ID. Delivering an event to a control
 * calls its listeners like the SSE client does.
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

vi.mock('./sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('./sse-client')>()),
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

// The texts are the fallbacks; no server answers in the test.
vi.mock('./i18n', async (importOriginal) => ({
  ...(await importOriginal<typeof import('./i18n')>()),
  useI18N: (keys: Record<string, string>) => keys,
}));

import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from './tl-react-bridge';
import { register } from './registry';
import TLChild from './TLChild';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** Fresh control IDs, so that the module-global mount registry of one test does not leak into another. */
let serial = 0;
function nextId(prefix: string): string {
  return prefix + (++serial);
}

/** A child showing its text, throwing while it renders as long as its state says so. */
const TestItem: React.FC<{ controlId: string; state: Record<string, unknown> }> = ({ controlId, state }) => {
  if (state.fail === true) {
    throw new Error('Item failure.');
  }
  return <span id={controlId} className="item">{String(state.text)}</span>;
};

/** A parent rendering its children side by side. */
const TestRow: React.FC<{ controlId: string; state: Record<string, unknown> }> = ({ controlId, state }) => (
  <div id={controlId} className="row">
    {(state.children as unknown[]).map((child, index) => <TLChild key={index} control={child} />)}
  </div>
);

function item(controlId: string, text: string, fail: boolean): Record<string, unknown> {
  return { controlId, module: 'TestItem', state: { text, fail } };
}

const mounted: string[] = [];

/** Mounts a {@link TestRow} with the given children as top-level control. */
function mountRow(children: Record<string, unknown>[]): HTMLElement {
  const id = nextId('row');
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, id);
  document.body.appendChild(element);
  act(() => mount(id, 'TestRow', { children }, 'w', '/ctx'));
  mounted.push(id);
  return document.getElementById(id)!;
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which
  // jsdom does not implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TestItem', TestItem);
  register('TestRow', TestRow);
});

let consoleError: ReturnType<typeof vi.spyOn>;

beforeEach(() => {
  // React and the boundary report the failure; the reports are checked, not printed.
  consoleError = vi.spyOn(console, 'error').mockImplementation(() => {});
});

afterEach(async () => {
  for (const id of mounted.splice(0)) {
    act(() => unmount(id));
    findMountPoint(id)?.remove();
  }
  consoleError.mockRestore();
  // Lets the DOM observer of the bridge see the removals while the test environment exists.
  await Promise.resolve();
});

describe('a child control failing to render', () => {
  it('is replaced by a placeholder while its siblings stay rendered', () => {
    const first = nextId('a');
    const broken = nextId('b');
    const last = nextId('c');
    const row = mountRow([item(first, 'First', false), item(broken, 'Broken', true), item(last, 'Last', false)]);

    const children = Array.from(row.children);
    expect(children.map(child => child.id)).toEqual([first, broken, last]);
    expect(children[0].textContent).toBe('First');
    expect(children[2].textContent).toBe('Last');

    const placeholder = children[1];
    expect(placeholder.className).toBe('tl-alert');
    expect(placeholder.getAttribute('data-tl-role')).toBe('error');
    expect(placeholder.getAttribute('role')).toBe('alert');
    expect(placeholder.textContent).toBe('This part of the view could not be displayed.');
  });

  it('reports the error to the console', () => {
    const broken = nextId('b');
    mountRow([item(broken, 'Broken', true)]);

    const reported = consoleError.mock.calls.some((args: unknown[]) =>
      String(args[0]).includes(broken) && args[1] instanceof Error && (args[1] as Error).message === 'Item failure.');
    expect(reported).toBe(true);
  });

  it('is rendered again when its state changes', () => {
    const broken = nextId('b');
    const row = mountRow([item(broken, 'Repaired', true)]);
    expect(row.querySelector('.tl-alert')).not.toBeNull();

    act(() => sse.deliver(broken, { fail: false }));

    expect(row.querySelector('.tl-alert')).toBeNull();
    expect(document.getElementById(broken)?.textContent).toBe('Repaired');
  });
});

describe('a child control rendering without failure', () => {
  it('is rendered as it is, without an element of the boundary', () => {
    const first = nextId('a');
    const second = nextId('b');
    const row = mountRow([item(first, 'First', false), item(second, 'Second', false)]);

    expect(row.innerHTML).toBe(
      '<span id="' + first + '" class="item">First</span>'
      + '<span id="' + second + '" class="item">Second</span>');
    expect(consoleError).not.toHaveBeenCalled();
  });
});
