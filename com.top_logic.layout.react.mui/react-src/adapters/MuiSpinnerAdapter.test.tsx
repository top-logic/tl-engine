// The part Spinner drawn with Material UI: the indeterminate MUI CircularProgress.

import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import type { SpinnerPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiSpinnerAdapter from './MuiSpinnerAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: SpinnerPartProps) {
  const { container } = render(<MuiRoot><MuiSpinnerAdapter {...props} /></MuiRoot>);
  return container.querySelector<HTMLElement>('.MuiCircularProgress-root')!;
}

afterEach(() => {
  cleanup();
});

describe('the part Spinner as MUI CircularProgress', () => {
  it('draws a medium spinner in the primary color, named by its label', () => {
    const spinner = draw({ label: 'Laden…', className: 'x', 'data-testid': 's' });

    expect(screen.getByRole('progressbar', { name: 'Laden…' })).toBe(spinner);
    expect(spinner.classList).toContain('MuiCircularProgress-indeterminate');
    expect(spinner.classList).toContain('MuiCircularProgress-colorPrimary');
    expect(spinner.classList).toContain('x');
    expect(spinner.getAttribute('data-testid')).toBe('s');
    expect(spinner.style.width).toBe('24px');
    expect(spinner.hasAttribute('aria-hidden')).toBe(false);
  });

  it('draws a small spinner without a label as decoration in the color of the text', () => {
    const spinner = draw({ size: 'small' });

    expect(spinner.getAttribute('aria-hidden')).toBe('true');
    expect(spinner.hasAttribute('aria-label')).toBe(false);
    expect(spinner.classList).not.toContain('MuiCircularProgress-colorPrimary');
    expect(spinner.style.width).toBe('16px');
    expect(screen.queryByRole('progressbar')).toBeNull();
  });
});
