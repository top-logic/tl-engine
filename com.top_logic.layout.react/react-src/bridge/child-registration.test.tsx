import React, { act } from 'react';
import { afterEach, beforeAll, describe, expect, it, vi } from 'vitest';

/**
 * The SSE listeners subscribed by the bridge, by control ID. Delivering an event to a control
 * calls its listeners like the SSE client does, an event for a control without listener is dropped.
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

import { createChildContext, isMountedControl, mount, unmount } from './tl-react-bridge';
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

function descriptor(controlId: string, state: Record<string, unknown>): Record<string, unknown> {
  return { controlId, module: 'TestChild', state };
}

/** A child showing its selection. */
const TestChild: React.FC<{ controlId: string; state: Record<string, unknown> }> = ({ controlId, state }) => (
  <span id={controlId} className="selection">{String(state.selection)}</span>
);

/**
 * A parent rendering its content child, at one of two places depending on its layout, so that a
 * change of the layout remounts the child.
 */
const TestParent: React.FC<{ controlId: string; state: Record<string, unknown> }> = ({ state }) => (
  <div>
    {state.content == null ? null
      : state.layout === 'b'
        ? <section><TLChild control={state.content} /></section>
        : <div><TLChild control={state.content} /></div>}
  </div>
);

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which
  // jsdom does not implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  register('TestChild', TestChild);
  register('TestParent', TestParent);
});

const mounted: string[] = [];

/** Mounts a {@link TestParent} as top-level control. */
function mountParent(state: Record<string, unknown>): string {
  const id = nextId('p');
  const element = document.createElement('div');
  element.id = id;
  document.body.appendChild(element);
  act(() => mount(id, 'TestParent', state, 'w', '/ctx'));
  mounted.push(id);
  return id;
}

function shownSelection(): string | null | undefined {
  return document.querySelector('.selection')?.textContent;
}

afterEach(async () => {
  for (const id of mounted.splice(0)) {
    act(() => unmount(id));
    document.getElementById(id)?.remove();
  }
  // Lets the DOM observer of the bridge see the removals while the test environment exists.
  await Promise.resolve();
});

describe('child descriptor arriving with an SSE update', () => {
  it('lets the child receive its patches of the same batch before it is rendered', () => {
    const parentId = nextId('p');
    const childId = nextId('c');
    createChildContext(parentId, {});

    const created = descriptor(childId, { selection: null });
    sse.deliver(parentId, { content: created });
    sse.deliver(childId, { selection: 'X' });

    // TLChild rendering the parent's new state.
    const child = createChildContext(childId, (created.state as Record<string, unknown>));
    expect(child.store.getSnapshot().selection).toBe('X');

    unmount(childId);
    unmount(parentId);
  });

  it('renders the child with the selection set right after it was created', () => {
    const parentId = mountParent({ content: null });
    const childId = nextId('c');

    // One poll answer: no render between the parent's patch and the child's patch.
    act(() => {
      sse.deliver(parentId, { content: descriptor(childId, { selection: null }) });
      sse.deliver(childId, { selection: 'X' });
    });

    expect(shownSelection()).toBe('X');
  });

  it('applies a newer descriptor over the patches the child received before', () => {
    const parentId = mountParent({ content: null });
    const childId = nextId('c');

    act(() => {
      sse.deliver(parentId, { content: descriptor(childId, { selection: null }) });
      sse.deliver(childId, { selection: 'X' });
    });
    expect(shownSelection()).toBe('X');

    act(() => sse.deliver(parentId, { content: descriptor(childId, { selection: 'Y' }) }));
    expect(shownSelection()).toBe('Y');
  });

  it('does not turn a remounted child back to the descriptor applied before', () => {
    const childId = nextId('c');
    const content = descriptor(childId, { selection: null });
    const parentId = mountParent({ content, layout: 'a' });

    act(() => sse.deliver(childId, { selection: 'X' }));
    expect(shownSelection()).toBe('X');

    // Remount at the other place, with the descriptor the parent already held.
    act(() => sse.deliver(parentId, { layout: 'b' }));
    expect(document.querySelector('section .selection')?.textContent).toBe('X');

    // The parent sending the same descriptor again.
    act(() => sse.deliver(parentId, { layout: 'a', content: descriptor(childId, { selection: null }) }));
    expect(shownSelection()).toBe('X');
  });

  it('tears an unclaimed child down with the control that described it', () => {
    const parentId = nextId('p');
    const childId = nextId('c');
    const nestedId = nextId('n');
    createChildContext(parentId, {});

    sse.deliver(parentId, {
      items: [descriptor(childId, { selection: null, content: descriptor(nestedId, {}) })],
    });
    expect(isMountedControl(childId)).toBe(true);
    expect(isMountedControl(nestedId)).toBe(true);
    expect(sse.listeners.has(childId)).toBe(true);

    unmount(parentId);
    expect(isMountedControl(childId)).toBe(false);
    expect(isMountedControl(nestedId)).toBe(false);
    expect(sse.listeners.has(childId)).toBe(false);
    expect(sse.listeners.has(nestedId)).toBe(false);
  });

  it('keeps a claimed child while its holder lives', () => {
    const parentId = nextId('p');
    const childId = nextId('c');
    createChildContext(parentId, {});

    const created = descriptor(childId, { selection: null });
    sse.deliver(parentId, { content: created });
    createChildContext(childId, created.state as Record<string, unknown>);

    unmount(parentId);
    expect(isMountedControl(childId)).toBe(true);
    expect(sse.listeners.has(childId)).toBe(true);

    unmount(childId);
    expect(isMountedControl(childId)).toBe(false);
    expect(sse.listeners.has(childId)).toBe(false);
  });
});
