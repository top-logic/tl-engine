import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// The mount point of a control mounted as page root names the control in MOUNT_ATTRIBUTE; the control
// ID belongs to the root element of the component alone. Hiding and unmounting act on the mount point
// (ticket #30371).

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

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, discoverAndMount, findMountPoint, isMountedControl, mount, unmount } from './tl-react-bridge';
import { register } from 'tl-react-bridge';
import type { TLCellProps } from './types';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'c1';

/** The module of the probe component. */
const PROBE = 'TestMountProbe';

/** A component rendering its root element with the control ID, as every component does. */
function Probe({ controlId }: TLCellProps) {
  return <div id={controlId} className="probe" />;
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  register(PROBE, Probe);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Adds a mount point for the probe as the server writes it. */
function addMountPoint(): HTMLElement {
  const element = document.createElement('div');
  element.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  element.dataset.reactModule = PROBE;
  document.body.appendChild(element);
  return element;
}

/** The elements of the page carrying the control ID. */
function withControlId(): Element[] {
  return Array.from(document.querySelectorAll(`[id="${CONTROL_ID}"]`));
}

describe('mount point of a root control', () => {
  it('is found by the control it names', () => {
    const mountPoint = addMountPoint();

    expect(findMountPoint(CONTROL_ID)).toBe(mountPoint);
    expect(findMountPoint('other')).toBeNull();
  });

  it('leaves the control ID to the root element of the component', () => {
    const mountPoint = addMountPoint();
    act(() => mount(CONTROL_ID, PROBE, {}, 'w', '/ctx'));

    const elements = withControlId();
    expect(elements).toHaveLength(1);
    expect(elements[0]).not.toBe(mountPoint);
    expect(elements[0].parentElement).toBe(mountPoint);
    expect(elements[0].className).toBe('probe');
  });

  it('is mounted when discovered', () => {
    const mountPoint = addMountPoint();
    document.body.dataset.windowName = 'w';
    document.body.dataset.contextPath = '/ctx';
    try {
      act(() => discoverAndMount());
    } finally {
      delete document.body.dataset.windowName;
      delete document.body.dataset.contextPath;
    }

    expect(isMountedControl(CONTROL_ID)).toBe(true);
    expect(mountPoint.querySelector('.probe')?.id).toBe(CONTROL_ID);
  });

  it('is hidden while the control is hidden', () => {
    const mountPoint = addMountPoint();
    act(() => mount(CONTROL_ID, PROBE, { hidden: true }, 'w', '/ctx'));

    expect(mountPoint.style.display).toBe('none');

    act(() => sse.deliver(CONTROL_ID, { hidden: false }));
    expect(mountPoint.style.display).toBe('');

    act(() => sse.deliver(CONTROL_ID, { hidden: true }));
    expect(mountPoint.style.display).toBe('none');
    expect(document.getElementById(CONTROL_ID)!.style.display).toBe('');
  });

  it('is emptied when the control is unmounted', () => {
    const mountPoint = addMountPoint();
    act(() => mount(CONTROL_ID, PROBE, {}, 'w', '/ctx'));

    act(() => unmount(CONTROL_ID));

    expect(isMountedControl(CONTROL_ID)).toBe(false);
    expect(mountPoint.isConnected).toBe(true);
    expect(mountPoint.childElementCount).toBe(0);
    expect(withControlId()).toHaveLength(0);
  });

  it('unmounts the control when it leaves the page', async () => {
    const mountPoint = addMountPoint();
    act(() => mount(CONTROL_ID, PROBE, {}, 'w', '/ctx'));

    await act(async () => {
      mountPoint.remove();
      // The DOM observer reports the removal as a microtask.
      await Promise.resolve();
    });

    expect(isMountedControl(CONTROL_ID)).toBe(false);
  });
});
