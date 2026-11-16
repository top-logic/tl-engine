import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

// Every component renders its root element with the control ID as id, and no other element of the
// page carries it: the mount point the server writes names the control in MOUNT_ATTRIBUTE only
// (ticket #30371).

/** The module names registered with register(), in the order of their registration. */
const registered = vi.hoisted(() => [] as string[]);

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

vi.mock('../bridge/registry', async (importOriginal) => {
  const original = await importOriginal<typeof import('../bridge/registry')>();
  return {
    ...original,
    register: (...args: Parameters<typeof original.register>) => {
      registered.push(args[0]);
      original.register(...args);
    },
  };
});

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { MOUNT_ATTRIBUTE, findMountPoint, mount, unmount } from '../bridge/tl-react-bridge';
import { register } from 'tl-react-bridge';
import '../controls-entry';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'root-id-probe';

/** A component that does not carry the control ID on its root element. */
const MODULE_WITHOUT_ID = 'TestWithoutId';

/** The components of the module, as the controls bundle registers them. */
const COMPONENTS = [...registered];

/** A child control, for a component that cannot be drawn without one. */
const CHILD = { controlId: 'root-id-child', module: 'TLText', state: { text: 'child' } };

/**
 * The state each of these components needs to be drawn, as the server always sends it. Every other
 * component draws with an empty state or draws nothing then: a closed menu, a toolbar without commands.
 */
const DRAWING_STATES: Record<string, Record<string, unknown>> = {
  TLAppShell: { content: CHILD, snackbar: { controlId: 'root-id-snackbar', module: 'TLSnackbar', state: {} } },
  TLCard: { child: CHILD },
  TLDialog: { open: true, child: CHILD },
  TLFormField: { field: CHILD },
  TLJobStatus: { status: 'running' },
  TLMaximizeRoot: { child: CHILD },
  TLMenu: { open: true, anchorX: 0, anchorY: 0, items: [] },
  TLOverlay: { base: CHILD },
  TLPanel: { child: CHILD },
  TLSnackbar: { visible: true, message: 'message' },
  TLTableView: { columns: [], rows: [], totalRowCount: 0, viewportStart: 0 },
  TLWindow: { child: CHILD },
};

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  register(MODULE_WITHOUT_ID, () => <div className="probe">no id</div>);
  // Components measuring their size; the test DOM has no layout.
  vi.stubGlobal('ResizeObserver', class {
    observe(): void {}
    unobserve(): void {}
    disconnect(): void {}
  });
  vi.stubGlobal('fetch', vi.fn(async () =>
    new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } })));
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  findMountPoint(CONTROL_ID)?.remove();
});

afterAll(() => {
  vi.unstubAllGlobals();
  document.body.replaceChildren();
});

/**
 * Mounts the given component as the root of a page with its minimal state and returns the
 * elements carrying the control ID, together with the mount point.
 */
function mountAsRoot(module: string): { mountPoint: HTMLElement; withId: Element[] } {
  const mountPoint = document.createElement('div');
  mountPoint.setAttribute(MOUNT_ATTRIBUTE, CONTROL_ID);
  document.body.appendChild(mountPoint);
  act(() => mount(CONTROL_ID, module, DRAWING_STATES[module] ?? {}, 'w', '/ctx'));
  return { mountPoint, withId: Array.from(document.querySelectorAll(`[id="${CONTROL_ID}"]`)) };
}

describe('root element of a component mounted as page root', () => {
  it('finds the registered components', () => {
    expect(COMPONENTS.length).toBeGreaterThan(50);
  });

  it.each(COMPONENTS)('%s carries the control ID, the mount point does not', (module) => {
    const { mountPoint, withId } = mountAsRoot(module);

    expect(mountPoint.id).toBe('');
    if (mountPoint.childElementCount > 0 || module in DRAWING_STATES) {
      expect(withId).toHaveLength(1);
      expect(withId[0]).not.toBe(mountPoint);
    } else {
      // Draws nothing in its empty state - or only outside the mount point, e.g. in a portal.
      expect(withId.length).toBeLessThanOrEqual(1);
    }
  });

  it('draws the components given a state to draw', () => {
    for (const module of Object.keys(DRAWING_STATES)) {
      expect(COMPONENTS).toContain(module);
    }
  });

  it('detects a component without the control ID on its root', () => {
    const { mountPoint, withId } = mountAsRoot(MODULE_WITHOUT_ID);

    expect(mountPoint.querySelector('.probe')).not.toBeNull();
    expect(withId).toHaveLength(0);
  });
});
