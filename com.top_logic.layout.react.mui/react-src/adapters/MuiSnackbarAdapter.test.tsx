// The wire contract of TLSnackbar, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup, act, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { React, useTransientOverlayRects } from 'tl-react-bridge';
import type { SnackbarStateJson } from 'tl-react-bridge';
import MuiSnackbarAdapter from './MuiSnackbarAdapter';
import { CONTROL_ID, fakeDebounceTimers, mountAdapter, patchState, settle } from './wire-test-support';

/** Command dismissing the message shown. */
const CMD_DISMISS = 'dismiss';

/** Argument of {@link CMD_DISMISS}: the generation of the message dismissed. */
const ARG_GENERATION = 'generation';

/** The label of the close button: the key, as the test stub translates each key to itself. */
const LABEL_DISMISS = 'js.snackbar.dismiss';

/** The display time of the messages of the tests. */
const DURATION_MS = 3000;

function mountSnackbar(state: Partial<SnackbarStateJson>) {
  return mountAdapter(MuiSnackbarAdapter, {
    visible: true, message: 'Gespeichert', variant: 'success', duration: DURATION_MS, generation: 3, ...state,
  });
}

/** Shows the number of transient overlays reported on the page. */
function OverlayCount() {
  return <output data-testid="overlays">{useTransientOverlayRects().length}</output>;
}

/** The number of transient overlays reported on the page, as {@link OverlayCount} shows it. */
function overlayCount(): number {
  return Number(screen.getByTestId('overlays').textContent);
}

function dismissed(generation: number) {
  return [CMD_DISMISS, { [ARG_GENERATION]: generation }];
}

afterEach(() => {
  cleanup();
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe('TLSnackbar as MUI Snackbar', () => {
  it('shows its message in a filled MUI alert of the severity of its variant', () => {
    mountSnackbar({ cssClass: 'my-snackbar' });

    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList).toContain('MuiSnackbar-root');
    expect(root.classList).toContain('my-snackbar');
    const alert = screen.getByRole('status');
    expect(alert.textContent).toContain('Gespeichert');
    expect(alert.classList).toContain('MuiAlert-filled');
    expect(alert.classList).toContain('MuiAlert-colorSuccess');
  });

  it('shows the HTML content instead of the text', () => {
    mountSnackbar({ content: '<b>Fehler</b> in Zeile 3', variant: 'error' });

    const alert = screen.getByRole('status');
    expect(alert.querySelector('b')?.textContent).toBe('Fehler');
    expect(alert.classList).toContain('MuiAlert-colorError');
  });

  it('renders nothing while not visible', () => {
    mountSnackbar({ visible: false });

    expect(screen.queryByRole('status')).toBeNull();
  });

  it('sends dismiss with its generation once its display time is over', async () => {
    fakeDebounceTimers();
    const sent = mountSnackbar({});

    act(() => vi.advanceTimersByTime(DURATION_MS - 1));
    await settle();
    expect(sent).not.toHaveBeenCalled();

    act(() => vi.advanceTimersByTime(1));
    await settle();
    expect(sent.mock.calls).toEqual([dismissed(3)]);
  });

  it('times the next message of a series anew', async () => {
    fakeDebounceTimers();
    const sent = mountSnackbar({});

    act(() => vi.advanceTimersByTime(DURATION_MS - 1));
    patchState({ message: 'Zweite', generation: 4 });
    act(() => vi.advanceTimersByTime(DURATION_MS - 1));
    await settle();
    expect(sent).not.toHaveBeenCalled();
    expect(screen.getByRole('status').textContent).toContain('Zweite');

    act(() => vi.advanceTimersByTime(1));
    await settle();
    expect(sent.mock.calls).toEqual([dismissed(4)]);
  });

  it('keeps a message without display time until it is dismissed by its close button', async () => {
    fakeDebounceTimers();
    const sent = mountSnackbar({ duration: 0 });

    act(() => vi.advanceTimersByTime(60_000));
    await settle();
    expect(sent).not.toHaveBeenCalled();

    vi.useRealTimers();
    await userEvent.click(screen.getByRole('button', { name: LABEL_DISMISS }));
    await settle();
    expect(sent.mock.calls).toEqual([dismissed(3)]);
  });

  it('stays for a click beside it', async () => {
    const sent = mountSnackbar({ duration: 0 });

    await userEvent.click(document.body);
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('reports itself as a transient overlay while it shows a message', () => {
    render(<OverlayCount />);
    expect(overlayCount()).toBe(0);

    mountSnackbar({});
    expect(overlayCount()).toBe(1);

    patchState({ message: 'Zweite', generation: 4 });
    expect(overlayCount()).toBe(1);

    patchState({ visible: false });
    expect(overlayCount()).toBe(0);
  });
});
