// The wire contract of TLButton, checked against the customer's adapter.
//
// A replacement registered with replace('TLButton', ...) receives the state the server sends for a
// TopLogic button and must answer with the commands the server understands. This test mounts the
// adapter exactly as the bridge does -- inside a TLControlContext holding the control's state --
// and records the commands instead of sending them.

import React from 'react';
import { describe, it, expect, vi, afterEach } from 'vitest';
import { render, screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TLControlContext, KeyboardScopeProvider, useKeyboardBinding } from 'tl-react-bridge';
import type { ButtonStateJson } from 'tl-react-bridge';
import { BrandProvider } from '../example-lib';
import BrandButtonAdapter from './BrandButtonAdapter';

/** The only command a button sends: it was clicked (or its keyboard gesture was pressed). */
const CMD_CLICK = 'click';

/** A keyboard gesture in the notation of the server (see keyboard-dispatcher.ts). */
const GESTURE_SAVE = 'Ctrl+S';

const CONTROL_ID = 'c1';

/**
 * A binding of the same gesture outside the button's keyboard scope (the page, a dialog around a
 * table): it receives the gesture whenever the button declines it.
 */
function OuterBinding({ gesture, onGesture }: { gesture: string; onGesture: () => void }) {
  useKeyboardBinding(gesture, () => {
    onGesture();
    return true;
  });
  return null;
}

type ControlContext = NonNullable<React.ContextType<typeof TLControlContext>>;

/**
 * The context the bridge mounts a control in. Its store offers only what useTLState() reads: the
 * state as a fixed snapshot.
 */
function contextOf(snapshot: Record<string, unknown>): ControlContext {
  const store = {
    getSnapshot: () => snapshot,
    subscribeStore: () => () => {},
  };
  return { controlId: CONTROL_ID, windowName: 'w', store } as unknown as ControlContext;
}

/**
 * Mounts the adapter like the bridge does and returns the recorded commands.
 *
 * useTLCommand() posts through the bridge's command channel; the stub answers every request, and
 * the recorded request bodies are the commands the adapter sent.
 */
function mountButton(state: Partial<ButtonStateJson>, outer?: () => void) {
  const sent = vi.fn<(command: string) => void>();
  vi.stubGlobal('fetch', vi.fn(async (_url: string, init?: RequestInit) => {
    const body = JSON.parse(String(init?.body ?? '{}'));
    if (body.controlId === CONTROL_ID) {
      sent(body.command);
    }
    return new Response('{}', { status: 200, headers: { 'Content-Type': 'application/json' } });
  }));
  const snapshot = { ...state } as Record<string, unknown>;
  render(
    <>
      {outer && state.keyGesture && <OuterBinding gesture={state.keyGesture} onGesture={outer} />}
      <KeyboardScopeProvider>
        <TLControlContext.Provider value={contextOf(snapshot)}>
          <BrandProvider name="test" accent="var(--tl-surface-brand)" corners="pill">
            <BrandButtonAdapter controlId={CONTROL_ID} state={snapshot} />
          </BrandProvider>
        </TLControlContext.Provider>
      </KeyboardScopeProvider>
    </>,
  );
  return sent;
}

/** Waits until the command channel has handed all queued commands to fetch. */
async function settle() {
  for (let i = 0; i < 10; i++) {
    await Promise.resolve();
  }
  await new Promise(resolve => setTimeout(resolve, 0));
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLButton — Draht', () => {
  it('sends exactly the command click when it is clicked', async () => {
    const sent = mountButton({ label: 'Speichern' });

    await userEvent.click(screen.getByRole('button', { name: 'Speichern' }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLICK]]);
  });

  it('sends nothing while disabled and lets its keyboard gesture fall through', async () => {
    const outer = vi.fn();
    const sent = mountButton({ label: 'Speichern', disabled: true, keyGesture: GESTURE_SAVE }, outer);

    // The button's scope binds the gesture first; the adapter declines, so it reaches the outer one.
    document.body.dispatchEvent(
      new KeyboardEvent('keydown', { key: 's', ctrlKey: true, bubbles: true, cancelable: true }));
    expect(outer).toHaveBeenCalledTimes(1);

    await userEvent.click(screen.getByRole('button', { name: 'Speichern' }));
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});
