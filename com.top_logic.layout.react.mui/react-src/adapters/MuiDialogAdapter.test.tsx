// The wire contract of TLDialog, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { DialogStateJson } from 'tl-react-bridge';
import MuiDialogAdapter from './MuiDialogAdapter';
import { CONTROL_ID, childControl, mountAdapter, settle } from './wire-test-support';

/** Command dismissing the dialog. */
const CMD_CLOSE = 'close';

function mountDialog(state: Partial<DialogStateJson>) {
  return mountAdapter(MuiDialogAdapter, { open: true, child: childControl('window', 'Fenster'), ...state });
}

function backdrop(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

function pressEscape() {
  document.body.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true }));
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLDialog as MUI Backdrop', () => {
  it('renders its content on an MUI backdrop carrying the control ID and the configured class', () => {
    mountDialog({ cssClass: 'my-dialog' });

    expect(backdrop().classList).toContain('MuiBackdrop-root');
    expect(backdrop().classList).toContain('my-dialog');
    expect(backdrop().querySelector('#window')?.textContent).toBe('Fenster');
  });

  it('renders nothing while not open', () => {
    mountDialog({ open: false });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
    expect(document.getElementById('window')).toBeNull();
  });

  it('sends close for a click on the backdrop beside its content, and for Escape', async () => {
    const sent = mountDialog({});

    await userEvent.click(backdrop());
    pressEscape();
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLOSE, {}], [CMD_CLOSE, {}]]);
  });

  it('sends nothing for a click on its content', async () => {
    const sent = mountDialog({});

    await userEvent.click(document.getElementById('window')!);
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('stays for a click on the backdrop when told so, but not for Escape', async () => {
    const sent = mountDialog({ closeOnBackdrop: false });

    await userEvent.click(backdrop());
    pressEscape();
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLOSE, {}]]);
  });

  it('sends nothing while not closable', async () => {
    const sent = mountDialog({ closable: false });

    await userEvent.click(backdrop());
    pressEscape();
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });
});
