// The part Divider drawn with Material UI: the MUI Divider.

import { describe, it, expect, afterEach } from 'vitest';
import { cleanup, render, screen } from '@testing-library/react';
import type { DividerPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiDividerAdapter from './MuiDividerAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

function draw(props: DividerPartProps) {
  render(<MuiRoot><MuiDividerAdapter {...props} /></MuiRoot>);
  return screen.getByRole('separator');
}

afterEach(() => {
  cleanup();
});

describe('the part Divider as MUI Divider', () => {
  it('draws a horizontal divider, with class and attributes', () => {
    const divider = draw({ className: 'x', 'data-testid': 'd' });

    expect(divider.tagName).toBe('HR');
    expect(divider.classList).toContain('MuiDivider-root');
    expect(divider.classList).not.toContain('MuiDivider-vertical');
    expect(divider.classList).not.toContain('tl-divider');
    expect(divider.classList).toContain('x');
    expect(divider.getAttribute('data-testid')).toBe('d');
    expect(divider.getAttribute('aria-orientation')).not.toBe('vertical');
  });

  it('draws a vertical divider as an item of the row stretching to its height', () => {
    const divider = draw({ orientation: 'vertical', className: 'x' });

    expect(divider.classList).toContain('MuiDivider-vertical');
    expect(divider.classList).toContain('MuiDivider-flexItem');
    expect(divider.getAttribute('aria-orientation')).toBe('vertical');
    expect(divider.classList).toContain('x');
  });
});
