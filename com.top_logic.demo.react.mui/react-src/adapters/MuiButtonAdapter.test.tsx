// The wire contract of TLButton, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED } from 'tl-react-bridge';
import type { ButtonStateJson } from 'tl-react-bridge';
import MuiButtonAdapter from './MuiButtonAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

/** The only command a button sends: it was clicked (or its keyboard gesture was pressed). */
const CMD_CLICK = 'click';

/** A keyboard gesture in the notation of the server (see keyboard-dispatcher.ts). */
const GESTURE_SAVE = 'Ctrl+S';

/** An icon font image in its encoded form. */
const IMAGE_EDIT = 'css:fas fa-edit';

function mountButton(state: Partial<ButtonStateJson>, outer?: () => void) {
  return mountAdapter(MuiButtonAdapter, state,
    outer && state.keyGesture ? { outer: { gesture: state.keyGesture, onGesture: outer } } : {});
}

function pressCtrlS() {
  document.body.dispatchEvent(
    new KeyboardEvent('keydown', { key: 's', ctrlKey: true, bubbles: true, cancelable: true }));
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLButton as MUI Button', () => {
  it('renders an outlined MUI button carrying the control ID and the configured class', () => {
    mountButton({ label: 'Speichern', cssClass: 'my-class', cssClasses: 'cmd-class' });

    const button = screen.getByRole('button', { name: 'Speichern' });
    expect(button.id).toBe(CONTROL_ID);
    expect(button.classList).toContain('MuiButton-root');
    expect(button.classList).toContain('MuiButton-outlined');
    expect(button.classList).toContain('my-class');
    expect(button.classList).toContain('cmd-class');
    expect(button.getAttribute(TOOLTIP_ATTR)).toBe('text:Speichern');
    expect(button.getAttribute(TOOLTIP_WHEN_ATTR)).toBe(WHEN_TRUNCATED);
  });

  it('maps appearance and tone to variant and color', () => {
    mountButton({ label: 'Löschen', appearance: 'primary', tone: 'danger' });

    const button = screen.getByRole('button', { name: 'Löschen' });
    expect(button.classList).toContain('MuiButton-contained');
    expect(button.classList).toContain('MuiButton-colorError');
  });

  it('marks an active button as pressed', () => {
    mountButton({ label: 'Hell', active: true, appearance: 'ghost' });

    const button = screen.getByRole('button', { name: 'Hell' });
    expect(button.getAttribute('aria-pressed')).toBe('true');
    expect(button.classList).toContain('MuiButton-text');
  });

  it('renders an icon-only button as MUI IconButton named by its label', () => {
    mountButton({ label: 'Bearbeiten', image: IMAGE_EDIT, displayMode: 'icon-only' });

    const button = screen.getByRole('button', { name: 'Bearbeiten' });
    expect(button.id).toBe(CONTROL_ID);
    expect(button.classList).toContain('MuiIconButton-root');
    expect(button.querySelector('i.fa-edit')).not.toBeNull();
    expect(button.textContent).toBe('');
    // The label is always the tooltip of an icon-only button.
    expect(button.getAttribute(TOOLTIP_ATTR)).toBe('text:Bearbeiten');
    expect(button.hasAttribute(TOOLTIP_WHEN_ATTR)).toBe(false);
  });

  it('shows icon and label in display mode icon-label', () => {
    mountButton({ label: 'Bearbeiten', image: IMAGE_EDIT, displayMode: 'icon-label' });

    const button = screen.getByRole('button', { name: 'Bearbeiten' });
    expect(button.querySelector('.MuiButton-startIcon i.fa-edit')).not.toBeNull();
    expect(button.textContent).toBe('Bearbeiten');
  });

  it('renders nothing while hidden', () => {
    mountButton({ label: 'Speichern', hidden: true });

    expect(screen.queryByRole('button')).toBeNull();
  });

  it('sends exactly the command click when it is clicked', async () => {
    const sent = mountButton({ label: 'Speichern' });

    await userEvent.click(screen.getByRole('button', { name: 'Speichern' }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLICK, {}]]);
  });

  it('sends the command click when its keyboard gesture is pressed', async () => {
    const outer = vi.fn();
    const sent = mountButton({ label: 'Speichern', keyGesture: GESTURE_SAVE }, outer);

    pressCtrlS();
    await settle();

    expect(outer).not.toHaveBeenCalled();
    expect(sent.mock.calls).toEqual([[CMD_CLICK, {}]]);
  });

  it('sends nothing while disabled and lets its keyboard gesture fall through', async () => {
    const outer = vi.fn();
    const sent = mountButton({ label: 'Speichern', disabled: true, keyGesture: GESTURE_SAVE }, outer);

    pressCtrlS();
    expect(outer).toHaveBeenCalledTimes(1);

    const button = screen.getByRole('button', { name: 'Speichern' });
    expect(button).toHaveProperty('disabled', true);
    // MUI turns off pointer events on a disabled button; the click is dispatched anyway.
    await userEvent.setup({ pointerEventsCheck: 0 }).click(button);
    await settle();

    expect(sent).not.toHaveBeenCalled();
  });

  it('declines its keyboard gesture while hidden', async () => {
    const outer = vi.fn();
    const sent = mountButton({ label: 'Speichern', hidden: true, keyGesture: GESTURE_SAVE }, outer);

    pressCtrlS();
    await settle();

    expect(outer).toHaveBeenCalledTimes(1);
    expect(sent).not.toHaveBeenCalled();
  });

  it('navigates instead of sending a command when the server names a URL', async () => {
    const open = vi.fn();
    vi.stubGlobal('open', open);
    const sent = mountButton({ label: 'Anmelden', navigateUrl: '/sso', navigateNewWindow: true });

    await userEvent.click(screen.getByRole('button', { name: 'Anmelden' }));
    await settle();

    expect(open).toHaveBeenCalledWith('/sso', '_blank');
    expect(sent).not.toHaveBeenCalled();
  });
});
