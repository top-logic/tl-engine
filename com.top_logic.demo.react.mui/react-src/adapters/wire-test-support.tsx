// Support of the wire tests of the adapters.
//
// A replacement registered with replace(...) receives the state the server sends for a TopLogic
// control and must answer with the commands the server understands. A wire test mounts an adapter
// as the bridge does -- inside a TLControlContext holding the control's state, below the
// root wrapper of this module -- and records the commands instead of sending them.

import React from 'react';
import { vi } from 'vitest';
import { act, render } from '@testing-library/react';
import { TLControlContext, KeyboardScopeProvider, FieldLabelContext, useKeyboardBinding } from 'tl-react-bridge';
import type { TLCellProps, FieldLabel } from 'tl-react-bridge';
import MuiRoot from '../MuiRoot';

/** The macrotask queue of Node, which the tests run in (no Node typings in this module). */
declare function setImmediate(callback: () => void): unknown;

/** The ID of the control under test. */
export const CONTROL_ID = 'c1';

/** A request of the bridge for translated texts, with the requested keys. */
const I18N_REQUEST = /react-api\/i18n\?keys=([^&]*)/;

/** A command the adapter sent: its name and its arguments. */
export type SentCommand = [command: string, args: Record<string, unknown>];

type ControlContext = NonNullable<React.ContextType<typeof TLControlContext>>;

/**
 * The context the bridge mounts a control in. Its store offers what useTLState() and
 * useTLFieldValue() use: the state, and the local patch a field applies when its value changes
 * (so that a field shows the value it sent, as it does in the bridge).
 */
/** Applies a patch to the state of the control mounted last, see {@link patchState}. */
let applyServerPatch: (patch: Record<string, unknown>) => void = () => {};

/**
 * Sends a state patch to the control mounted last, as the server does after a command (e.g. the
 * options a field asked for).
 */
export function patchState(patch: Record<string, unknown>) {
  act(() => applyServerPatch(patch));
}

function contextOf(initial: Record<string, unknown>): ControlContext {
  let snapshot = initial;
  const listeners = new Set<() => void>();
  const store = {
    getSnapshot: () => snapshot,
    subscribeStore: (listener: () => void) => {
      listeners.add(listener);
      return () => listeners.delete(listener);
    },
    applyPatch: (patch: Record<string, unknown>) => {
      snapshot = { ...snapshot, ...patch };
      listeners.forEach(listener => listener());
    },
  };
  applyServerPatch = store.applyPatch;
  return { controlId: CONTROL_ID, windowName: 'w', store } as unknown as ControlContext;
}

/**
 * A binding of a gesture outside the adapter's keyboard scope (the page, a dialog around a
 * table): it receives the gesture whenever the adapter declines it.
 */
function OuterBinding({ gesture, onGesture }: { gesture: string; onGesture: () => void }) {
  useKeyboardBinding(gesture, () => {
    onGesture();
    return true;
  });
  return null;
}

/** Options of {@link mountAdapter}. */
export interface MountOptions {
  /** A gesture bound outside the adapter's scope, with the callback it triggers. */
  outer?: { gesture: string; onGesture: () => void };

  /** The label association of a form field around the adapter. */
  fieldLabel?: FieldLabel;
}

/**
 * Mounts the adapter like the bridge does and returns the recorded commands.
 *
 * useTLCommand() posts through the bridge's command channel; the stub answers every request, and
 * the recorded request bodies are the commands the adapter sent.
 */
export function mountAdapter(
  Adapter: React.FC<TLCellProps>,
  state: Record<string, unknown>,
  options: MountOptions = {},
) {
  const sent = vi.fn<(...command: SentCommand) => void>();
  vi.stubGlobal('fetch', vi.fn(async (url: string, init?: RequestInit) => {
    const i18n = I18N_REQUEST.exec(url);
    if (i18n !== null) {
      // Each key is answered with itself, so a test finds a translated text by its key.
      const keys = decodeURIComponent(i18n[1]).split(',');
      const texts = Object.fromEntries(keys.map(key => [key, key]));
      return new Response(JSON.stringify(texts), { status: 200, headers: { 'Content-Type': 'application/json' } });
    }
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === CONTROL_ID) {
      sent(body.command, body.arguments);
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const snapshot = { ...state };
  const outer = options.outer;
  render(
    <MuiRoot>
      {outer && <OuterBinding gesture={outer.gesture} onGesture={outer.onGesture} />}
      <KeyboardScopeProvider>
        <FieldLabelContext.Provider value={options.fieldLabel ?? null}>
          <TLControlContext.Provider value={contextOf(snapshot)}>
            <Adapter controlId={CONTROL_ID} state={snapshot} />
          </TLControlContext.Provider>
        </FieldLabelContext.Provider>
      </KeyboardScopeProvider>
    </MuiRoot>,
  );
  return sent;
}

/**
 * Waits until the command channel has handed all queued commands to fetch.
 *
 * <p>Waits for macrotasks through `setImmediate`, which a test faking only `setTimeout` (to drive
 * a debounce) leaves real.</p>
 */
export async function settle() {
  for (let round = 0; round < 5; round++) {
    for (let i = 0; i < 10; i++) {
      await Promise.resolve();
    }
    await new Promise<void>(resolve => setImmediate(() => resolve()));
  }
}

/** Fakes the timers a field debounces its value with, leaving the ones {@link settle} waits for. */
export function fakeDebounceTimers() {
  vi.useFakeTimers({ toFake: ['setTimeout', 'clearTimeout'] });
}

/**
 * Lets the pointer devices of the page match `(pointer: fine)`, so that the MUI X pickers render
 * their desktop variant (jsdom implements no media queries).
 */
export function matchDesktopPointer() {
  vi.stubGlobal('matchMedia', (query: string) => ({
    matches: true,
    media: query,
    onchange: null,
    addEventListener() {},
    removeEventListener() {},
    addListener() {},
    removeListener() {},
    dispatchEvent: () => false,
  }));
}
