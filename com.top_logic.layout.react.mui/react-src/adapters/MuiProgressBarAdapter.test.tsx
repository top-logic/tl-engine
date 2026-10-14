// The part ProgressBar drawn with Material UI: the MUI LinearProgress with a label beside it.

import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import type { ProgressBarPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiProgressBarAdapter from './MuiProgressBarAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

/** The ID of the drawn progress bar. */
const ID = 'p1';

function draw(props: ProgressBarPartProps) {
  render(<MuiRoot><MuiProgressBarAdapter id={ID} {...props} /></MuiRoot>);
  return document.getElementById(ID)!;
}

afterEach(() => {
  cleanup();
});

describe('the part ProgressBar as MUI LinearProgress', () => {
  it('draws a determinate bar of the share with its label beside it, class and attributes on the root', () => {
    const root = draw({ value: 0.75, label: '3 / 4', className: 'x', 'data-testid': 'progress' });

    const bar = screen.getByRole('progressbar');
    expect(bar.classList).toContain('MuiLinearProgress-determinate');
    expect(bar.getAttribute('aria-valuenow')).toBe('75');
    expect(bar.getAttribute('aria-valuetext')).toBe('3 / 4');
    expect(bar.hasAttribute('aria-busy')).toBe(false);
    expect(root.classList).toContain('x');
    expect(root.getAttribute('data-testid')).toBe('progress');
    expect(root.textContent).toBe('3 / 4');
  });

  it('draws the indeterminate bar without a value, busy and without a label', () => {
    const root = draw({});

    const bar = screen.getByRole('progressbar');
    expect(bar.classList).toContain('MuiLinearProgress-indeterminate');
    expect(bar.hasAttribute('aria-valuenow')).toBe(false);
    expect(bar.getAttribute('aria-busy')).toBe('true');
    expect(root.textContent).toBe('');
  });

  it('draws an empty and a full bar at the ends of the share', () => {
    draw({ value: 0 });
    expect(screen.getByRole('progressbar').getAttribute('aria-valuenow')).toBe('0');
    cleanup();

    draw({ value: 1 });
    expect(screen.getByRole('progressbar').getAttribute('aria-valuenow')).toBe('100');
  });
});
