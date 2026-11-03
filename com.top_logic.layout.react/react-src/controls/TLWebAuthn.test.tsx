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
import { register, registerPart } from 'tl-react-bridge';
import TLStack from './TLStack';
import TLWebAuthn from './TLWebAuthn';
import ButtonPart from './button/ButtonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

/** A command a control sent: its control and its name. */
type Sent = { controlId: string; command: string };

const ROOT_ID = 'root';

const BUTTON_ID = 'passkey';

/** Options of a login ceremony, as the server sends them. */
const OPTIONS = JSON.stringify({ publicKey: { challenge: 'AAAA', timeout: 300000, allowCredentials: [] } });

let sent: Sent[] = [];

let get: ReturnType<typeof vi.fn>;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  // The DOM observer of the bridge looks for mounts below a removed element with CSS.escape, which jsdom does not
  // implement. The control IDs of the tests need no escaping.
  if (typeof globalThis.CSS === 'undefined') {
    globalThis.CSS = { escape: (value: string) => value } as unknown as typeof CSS;
  }
  registerPart('Button', ButtonPart);
  register('TLStack', TLStack);
  register('TLWebAuthn', TLWebAuthn);
});

afterEach(async () => {
  act(() => unmount(ROOT_ID));
  document.getElementById(ROOT_ID)?.remove();
  vi.unstubAllGlobals();
  delete (navigator as unknown as Record<string, unknown>).credentials;
  await settle();
});

afterAll(async () => {
  document.body.replaceChildren();
  await settle();
});

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle(): Promise<void> {
  await act(async () => {
    for (let round = 0; round < 5; round++) {
      await new Promise<void>(resolve => setTimeout(resolve, 0));
    }
  });
}

/**
 * Mounts a column holding the passkey button with the given state, on a page that can run a ceremony, and records
 * the commands sent and the calls of the browser.
 */
function mountButton(state: Record<string, unknown>): void {
  sent = [];
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.command) {
      sent.push({ controlId: body.controlId, command: body.command });
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  vi.stubGlobal('isSecureContext', true);
  vi.stubGlobal('PublicKeyCredential', {});
  get = vi.fn(() => new Promise(() => {}));
  Object.defineProperty(navigator, 'credentials', { configurable: true, value: { get, create: vi.fn() } });

  const element = document.createElement('div');
  element.id = ROOT_ID;
  document.body.appendChild(element);
  const child = { controlId: BUTTON_ID, module: 'TLWebAuthn', state: { kind: 'get', label: 'Sign in', ...state } };
  act(() => mount(ROOT_ID, 'TLStack', { direction: 'column', children: [child] }, 'w', '/ctx'));
}

function commandsOfButton(): string[] {
  return sent.filter(entry => entry.controlId === BUTTON_ID).map(entry => entry.command);
}

describe('a hidden passkey button', () => {
  it('renders nothing, reports nothing and starts no ceremony', async () => {
    mountButton({ hidden: true, options: OPTIONS, autoStart: true });
    await settle();

    expect(document.getElementById(BUTTON_ID)).toBeNull();
    expect(document.getElementById(ROOT_ID)!.querySelector('button')).toBeNull();
    expect(commandsOfButton()).toEqual([]);
    expect(get).not.toHaveBeenCalled();
  });

  it('is rendered and reports the origin of the page once the server shows it', async () => {
    mountButton({ hidden: true, options: OPTIONS });
    await settle();

    act(() => sse.deliver(BUTTON_ID, { hidden: false }));
    await settle();

    expect(document.getElementById(BUTTON_ID)).not.toBeNull();
    expect(commandsOfButton()).toEqual(['origin']);
  });
});

describe('a shown passkey button', () => {
  it('is rendered and reports the origin of the page', async () => {
    mountButton({ options: OPTIONS });
    await settle();

    expect(document.getElementById(BUTTON_ID)).not.toBeNull();
    expect(commandsOfButton()).toEqual(['origin']);
  });
});
