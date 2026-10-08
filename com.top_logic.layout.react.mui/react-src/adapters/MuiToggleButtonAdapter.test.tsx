// The wire contract of TLToggleButton, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import type { ToggleButtonStateJson } from 'tl-react-bridge';
import MuiToggleButtonAdapter from './MuiToggleButtonAdapter';
import { CONTROL_ID, mountAdapter, settle } from './wire-test-support';

/** The only command a toggle button sends; the server flips its state. */
const CMD_CLICK = 'click';

function mountToggle(state: Partial<ToggleButtonStateJson>) {
  return mountAdapter(MuiToggleButtonAdapter, state);
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLToggleButton as MUI ToggleButton inside a menu', () => {
  it('is a checkbox entry of the menu with the roving tabindex', () => {
    mountAdapter(MuiToggleButtonAdapter, { label: 'Fett', active: true }, { buttonDefaults: { appearance: 'menu-item' } });

    const item = screen.getByRole('menuitemcheckbox', { name: 'Fett' });
    expect(item.id).toBe(CONTROL_ID);
    expect(item.classList).toContain('MuiListItemButton-root');
    expect(item.getAttribute('aria-checked')).toBe('true');
    expect(item.getAttribute('tabindex')).toBe('-1');
  });

  it('sends click when the entry is chosen', async () => {
    const sent = mountAdapter(MuiToggleButtonAdapter, { label: 'Fett' }, { buttonDefaults: { appearance: 'menu-item' } });

    const item = screen.getByRole('menuitemcheckbox', { name: 'Fett' });
    expect(item.getAttribute('aria-checked')).toBe('false');
    await userEvent.click(item);
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLICK, {}]]);
  });
});

describe('TLToggleButton as MUI ToggleButton', () => {
  it('renders an MUI toggle button, pressed while active', () => {
    mountToggle({ label: 'Fett', active: true, cssClass: 'my-class' });

    const button = screen.getByRole('button', { name: 'Fett' });
    expect(button.id).toBe(CONTROL_ID);
    expect(button.classList).toContain('MuiToggleButton-root');
    expect(button.classList).toContain('Mui-selected');
    expect(button.classList).toContain('my-class');
    expect(button.getAttribute('aria-pressed')).toBe('true');
  });

  it('is not pressed while inactive', () => {
    mountToggle({ label: 'Fett' });

    const button = screen.getByRole('button', { name: 'Fett' });
    expect(button.getAttribute('aria-pressed')).toBe('false');
    expect(button.classList).not.toContain('Mui-selected');
  });

  it('sends exactly the command click when it is clicked', async () => {
    const sent = mountToggle({ label: 'Fett', active: true });

    await userEvent.click(screen.getByRole('button', { name: 'Fett' }));
    await settle();

    expect(sent.mock.calls).toEqual([[CMD_CLICK, {}]]);
  });

  it('renders nothing and sends nothing while hidden', async () => {
    const sent = mountToggle({ label: 'Fett', hidden: true });
    await settle();

    expect(screen.queryByRole('button')).toBeNull();
    expect(sent).not.toHaveBeenCalled();
  });
});
