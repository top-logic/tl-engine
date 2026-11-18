// The part Skeleton drawn with Material UI: the MUI Skeleton.

import { describe, it, expect, afterEach, vi } from 'vitest';
import { cleanup, render } from '@testing-library/react';
import type { SkeletonPartProps } from 'tl-react-bridge';
import { createMuiRoot, createPageThemes } from '../MuiRoot';
import MuiSkeletonAdapter from './MuiSkeletonAdapter';

/** The root wrapper the adapter is drawn below: the one of the MUI default theme. */
const MuiRoot = createMuiRoot(createPageThemes({}));

/** Draws the part and answers its outermost element. */
function draw(props: SkeletonPartProps): HTMLElement {
  const { container } = render(<MuiRoot><MuiSkeletonAdapter {...props} /></MuiRoot>);
  return container.querySelector('[aria-hidden="true"]') as HTMLElement;
}

/** Answers every media query as matching the given query only. */
function matchOnly(query: string): void {
  vi.stubGlobal('matchMedia', (q: string) => ({
    matches: q === query,
    media: q,
    onchange: null,
    addEventListener: () => {},
    removeEventListener: () => {},
    addListener: () => {},
    removeListener: () => {},
    dispatchEvent: () => false,
  }));
}

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe('the part Skeleton as MUI Skeleton', () => {
  it('draws a rectangle without a shape, hidden, with class, style and attributes', () => {
    const skeleton = draw({ className: 'x', style: { height: '4rem' }, 'data-testid': 's' });

    expect(skeleton.classList).toContain('MuiSkeleton-root');
    expect(skeleton.classList).toContain('MuiSkeleton-rectangular');
    expect(skeleton.classList).not.toContain('tl-skeleton');
    expect(skeleton.classList).toContain('x');
    expect(skeleton.style.height).toBe('4rem');
    expect(skeleton.getAttribute('data-testid')).toBe('s');
  });

  it('maps the shapes to the MUI variants', () => {
    expect(draw({ shape: 'rect' }).classList).toContain('MuiSkeleton-rectangular');
    cleanup();
    expect(draw({ shape: 'text' }).classList).toContain('MuiSkeleton-text');
    cleanup();
    expect(draw({ shape: 'circle' }).classList).toContain('MuiSkeleton-circular');
  });

  it('draws several lines of text in an element hidden from assistive technology', () => {
    const skeleton = draw({ shape: 'text', lines: 3, className: 'x' });

    expect(skeleton.classList).not.toContain('MuiSkeleton-root');
    expect(skeleton.classList).toContain('x');
    const lines = Array.from(skeleton.children) as HTMLElement[];
    expect(lines).toHaveLength(3);
    for (const line of lines) {
      expect(line.classList).toContain('MuiSkeleton-text');
    }
    expect(lines[0].style.width).toBe('');
    expect(lines[2].style.width).toBe('50%');
  });

  it('pulses', () => {
    expect(draw({}).classList).toContain('MuiSkeleton-pulse');
  });

  it('stands still for a viewer who asked for reduced motion', () => {
    matchOnly('(prefers-reduced-motion: reduce)');

    expect(draw({}).classList).not.toContain('MuiSkeleton-pulse');
  });
});
