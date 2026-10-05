// The wire contract of TLButton, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED } from 'tl-react-bridge';
import type { ButtonStateJson, ButtonDefaultsValue } from 'tl-react-bridge';
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

/** The defaults of a menu around a button: every button there is an entry. */
const IN_MENU: ButtonDefaultsValue = { appearance: 'menu-item' };

function mountInContainer(state: Partial<ButtonStateJson>, buttonDefaults: ButtonDefaultsValue) {
  return mountAdapter(MuiButtonAdapter, state, { buttonDefaults });
}

describe('TLButton as MUI Button inside a container', () => {
  it('takes the ghost look of a toolbar or an app bar as variant text', () => {
    mountInContainer({ label: 'Neu' }, { appearance: 'ghost' });

    expect(screen.getByRole('button', { name: 'Neu' }).classList).toContain('MuiButton-text');
  });

  it('takes the secondary look of the button bar of a window as variant outlined', () => {
    mountInContainer({ label: 'Abbrechen', appearance: 'default' }, { appearance: 'secondary' });

    expect(screen.getByRole('button', { name: 'Abbrechen' }).classList).toContain('MuiButton-outlined');
  });

  it('keeps the appearance its state names over the one of the container', () => {
    mountInContainer({ label: 'Speichern', appearance: 'primary' }, { appearance: 'ghost' });

    expect(screen.getByRole('button', { name: 'Speichern' }).classList).toContain('MuiButton-contained');
  });

  it('shows itself by its icon alone in a compact toolbar', () => {
    mountInContainer({ label: 'Bearbeiten', image: IMAGE_EDIT, displayMode: 'icon-label' }, { iconOnly: true });

    const button = screen.getByRole('button', { name: 'Bearbeiten' });
    expect(button.classList).toContain('MuiIconButton-root');
    expect(button.textContent).toBe('');
  });

  it('keeps its label in a compact toolbar when it shows no icon', () => {
    mountInContainer({ label: 'Bearbeiten', image: IMAGE_EDIT }, { iconOnly: true });

    expect(screen.getByRole('button', { name: 'Bearbeiten' }).classList).toContain('MuiButton-root');
  });

  it('is an entry of a menu: an MUI list item button with the role and the roving tabindex of the menu', () => {
    mountInContainer({ label: 'Löschen', image: IMAGE_EDIT, appearance: 'primary', tone: 'danger' }, IN_MENU);

    const item = screen.getByRole('menuitem', { name: 'Löschen' });
    expect(item.id).toBe(CONTROL_ID);
    expect(item.classList).toContain('MuiListItemButton-root');
    expect(item.getAttribute('tabindex')).toBe('-1');
    expect(item.querySelector('.MuiListItemIcon-root i.fa-edit')).not.toBeNull();
    expect(screen.queryByRole('button')).toBeNull();
  });

  it('marks the active entry of a menu as current, not as pressed', () => {
    mountInContainer({ label: 'Hell', active: true }, IN_MENU);

    const item = screen.getByRole('menuitem', { name: 'Hell' });
    expect(item.getAttribute('aria-current')).toBe('true');
    expect(item.hasAttribute('aria-pressed')).toBe(false);
    expect(item.classList).toContain('Mui-selected');
  });

  it('sends click when its menu entry is chosen, nothing while disabled', async () => {
    const sent = mountInContainer({ label: 'Löschen' }, IN_MENU);

    await userEvent.click(screen.getByRole('menuitem', { name: 'Löschen' }));
    await settle();
    expect(sent.mock.calls).toEqual([[CMD_CLICK, {}]]);
    cleanup();

    const sentDisabled = mountInContainer({ label: 'Löschen', disabled: true }, IN_MENU);
    const item = screen.getByRole('menuitem', { name: 'Löschen' });
    expect(item.hasAttribute('disabled')).toBe(true);
    await userEvent.setup({ pointerEventsCheck: 0 }).click(item);
    await settle();
    expect(sentDisabled).not.toHaveBeenCalled();
  });

  it('carries no role of a menu entry outside a menu', () => {
    mountButton({ label: 'Speichern' });

    expect(screen.queryByRole('menuitem')).toBeNull();
    expect(screen.getByRole('button', { name: 'Speichern' }).getAttribute('tabindex')).not.toBe('-1');
  });
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
