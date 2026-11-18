import { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest';
import { React } from 'tl-react-bridge';
import type { SkeletonPartProps } from 'tl-react-bridge';
import SkeletonPart from './SkeletonPart';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

let container: HTMLDivElement;
let root: Root;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
});

afterAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = false;
});

beforeEach(() => {
  container = document.createElement('div');
  document.body.appendChild(container);
  root = createRoot(container);
});

afterEach(() => {
  act(() => root.unmount());
  container.remove();
});

/** Draws the part with the given props and returns its outermost element. */
function render(props: SkeletonPartProps): HTMLElement {
  act(() => root.render(<SkeletonPart {...props} />));
  return container.firstElementChild as HTMLElement;
}

describe('SkeletonPart', () => {
  it('draws a rectangle without a shape', () => {
    const skeleton = render({});

    expect(skeleton.className).toBe('tl-skeleton');
    expect(skeleton.getAttribute('aria-hidden')).toBe('true');
  });

  it('draws a rectangle for the shape rect', () => {
    expect(render({ shape: 'rect' }).className).toBe('tl-skeleton');
  });

  it('draws one line of text for the shape text', () => {
    const skeleton = render({ shape: 'text' });

    expect(skeleton.className).toBe('tl-skeleton tl-skeleton--text');
    expect(skeleton.getAttribute('aria-hidden')).toBe('true');
    expect(skeleton.children).toHaveLength(0);
  });

  it('draws a circle for the shape circle', () => {
    const skeleton = render({ shape: 'circle' });

    expect(skeleton.className).toBe('tl-skeleton tl-skeleton--circle');
    expect(skeleton.getAttribute('aria-hidden')).toBe('true');
  });

  it('draws several lines of text in an element hidden from assistive technology', () => {
    const skeleton = render({ shape: 'text', lines: 3, className: 'site', style: { width: '10rem' }, 'data-test': 'x' });

    expect(skeleton.tagName).toBe('DIV');
    expect(skeleton.getAttribute('aria-hidden')).toBe('true');
    expect(skeleton.className).toBe('site');
    expect(skeleton.style.width).toBe('10rem');
    expect(skeleton.getAttribute('data-test')).toBe('x');
    const lines = Array.from(skeleton.children);
    expect(lines).toHaveLength(3);
    for (const line of lines) {
      expect(line.className).toBe('tl-skeleton tl-skeleton--text');
    }
  });

  it('draws one line for a count of lines below one', () => {
    expect(render({ shape: 'text', lines: 0 }).className).toBe('tl-skeleton tl-skeleton--text');
  });

  it('ignores the count of lines for other shapes', () => {
    const skeleton = render({ shape: 'circle', lines: 3 });

    expect(skeleton.className).toBe('tl-skeleton tl-skeleton--circle');
    expect(skeleton.children).toHaveLength(0);
  });

  it('puts class, style and attributes on a single element and keeps it hidden', () => {
    const skeleton = render({ className: 'site', style: { height: '4rem' }, 'data-test': 'y', 'aria-hidden': false });

    expect(skeleton.className).toBe('tl-skeleton site');
    expect(skeleton.style.height).toBe('4rem');
    expect(skeleton.getAttribute('data-test')).toBe('y');
    expect(skeleton.getAttribute('aria-hidden')).toBe('true');
  });
});
