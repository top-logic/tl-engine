// The wire contract of TLProgress, checked against the MUI adapter.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { screen, cleanup } from '@testing-library/react';
import type { ProgressStateJson } from 'tl-react-bridge';
import MuiProgressAdapter from './MuiProgressAdapter';
import { CONTROL_ID, mountAdapter } from './wire-test-support';

function mountProgress(state: Partial<ProgressStateJson>) {
  return mountAdapter(MuiProgressAdapter, state);
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('TLProgress as MUI LinearProgress', () => {
  it('renders a determinate MUI bar of the fraction with its label beside it', () => {
    mountProgress({ fraction: 0.75, label: '3 / 4', cssClass: 'my-progress' });

    const bar = screen.getByRole('progressbar');
    expect(bar.classList).toContain('MuiLinearProgress-determinate');
    expect(bar.getAttribute('aria-valuenow')).toBe('75');
    expect(bar.getAttribute('aria-valuetext')).toBe('3 / 4');
    const root = document.getElementById(CONTROL_ID)!;
    expect(root.classList).toContain('my-progress');
    expect(root.textContent).toBe('3 / 4');
  });

  it('renders the indeterminate bar without a fraction, busy and without a value', () => {
    mountProgress({ fraction: null as unknown as number });

    const bar = screen.getByRole('progressbar');
    expect(bar.classList).toContain('MuiLinearProgress-indeterminate');
    expect(bar.hasAttribute('aria-valuenow')).toBe(false);
    expect(bar.getAttribute('aria-busy')).toBe('true');
    expect(document.getElementById(CONTROL_ID)!.textContent).toBe('');
  });

  it('renders nothing while hidden', () => {
    mountProgress({ fraction: 0.5, hidden: true });

    expect(screen.queryByRole('progressbar')).toBeNull();
  });
});
