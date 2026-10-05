// The wire contract of TLAppBar, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import type { AppBarStateJson } from 'tl-react-bridge';
import MuiAppBarAdapter from './MuiAppBarAdapter';
import { CONTROL_ID, childControl, mountAdapter } from './wire-test-support';

function mountBar(state: Partial<AppBarStateJson>) {
  return mountAdapter(MuiAppBarAdapter, { title: 'Demo', ...state });
}

function root(): HTMLElement {
  return document.getElementById(CONTROL_ID)!;
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLAppBar as MUI AppBar', () => {
  it('renders a flat static MUI app bar of the neutral color with its title', () => {
    mountBar({ cssClass: 'my-bar' });

    const bar = root();
    expect(bar.classList).toContain('MuiAppBar-root');
    expect(bar.classList).toContain('MuiAppBar-positionStatic');
    expect(bar.classList).toContain('MuiAppBar-colorDefault');
    expect(bar.classList).toContain('MuiPaper-elevation0');
    expect(bar.classList).toContain('my-bar');
    expect(screen.getByRole('heading', { level: 1 }).textContent).toBe('Demo');
  });

  it('raises an elevated bar', () => {
    mountBar({ variant: 'elevated' });

    expect(root().classList).toContain('MuiPaper-elevation4');
  });

  it('renders leading, children, actions and trailing in their slots, in this order', () => {
    mountBar({
      leading: childControl('menu', 'Menü'),
      children: [childControl('search', 'Suche')],
      actions: childControl('toolbar', 'Befehle'),
      trailing: childControl('user', 'Benutzer'),
    });

    const toolbar = root().querySelector('.MuiToolbar-root')!;
    const slots = Array.from(toolbar.children).map(slot => slot.className.split(' ')[0]);
    expect(slots[0]).toBe('tlAppBar__leading');
    expect(slots[2]).toBe('tlAppBar__children');
    expect(slots[3]).toBe('tlAppBar__actions');
    expect(slots[4]).toBe('tlAppBar__trailing');
    expect(toolbar.querySelector('.tlAppBar__leading > #menu')).not.toBeNull();
    expect(toolbar.querySelector('.tlAppBar__children > #search')).not.toBeNull();
    expect(toolbar.querySelector('.tlAppBar__actions > #toolbar')).not.toBeNull();
    expect(toolbar.querySelector('.tlAppBar__trailing > #user')).not.toBeNull();
  });

  it('renders no slot for a part the state does not have', () => {
    mountBar({});

    const toolbar = root().querySelector('.MuiToolbar-root')!;
    expect(toolbar.children).toHaveLength(1);
  });

  it('renders nothing while hidden', () => {
    mountBar({ hidden: true });

    expect(document.getElementById(CONTROL_ID)).toBeNull();
  });
});
