import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';

import {
  activeRipples,
  dotRadius,
  glowFactor,
  isBackgroundTarget,
  prefersReducedMotion,
  readHalftoneTheme,
  boundedStep,
  rippleFactor,
} from './halftone';
import type { Ripple } from './halftone';

describe('dotRadius', () => {
  it('is largest at the centre: a seventh of the step', () => {
    expect(dotRadius(0, 500, 16)).toBeCloseTo(2.24, 5);
  });

  it('is zero at and beyond the half diagonal', () => {
    expect(dotRadius(500, 500, 16)).toBe(0);
    expect(dotRadius(800, 500, 16)).toBe(0);
  });

  it('falls monotonically with the distance', () => {
    let previous = Infinity;
    for (let distance = 0; distance <= 500; distance += 10) {
      const r = dotRadius(distance, 500, 16);
      expect(r).toBeLessThanOrEqual(previous);
      previous = r;
    }
  });

  it('leaves out dots too small to see', () => {
    // (1 - 0.99)^0.6 * 2.24 is far below 0.35.
    expect(dotRadius(495, 500, 16)).toBe(0);
    expect(dotRadius(300, 500, 16)).toBeGreaterThanOrEqual(0.35);
  });
});

describe('glowFactor', () => {
  it('is one at the pointer and e^-1 one radius away', () => {
    expect(glowFactor(0, 0, 40)).toBe(1);
    expect(glowFactor(40, 0, 40)).toBeCloseTo(Math.exp(-1), 10);
    expect(glowFactor(24, 32, 40)).toBeCloseTo(Math.exp(-1), 10);
  });

  it('fades with the distance', () => {
    expect(glowFactor(80, 0, 40)).toBeLessThan(glowFactor(40, 0, 40));
  });
});

describe('rippleFactor', () => {
  const ripple: Ripple = { x: 100, y: 100, start: 1000 };
  const duration = 1000;
  const reach = 1000;

  it('is zero before the start and from the end on', () => {
    expect(rippleFactor(ripple, 100, 100, 999, duration, reach)).toBe(0);
    expect(rippleFactor(ripple, 100, 100, 2000, duration, reach)).toBe(0);
    expect(rippleFactor(ripple, 100, 100, 3000, duration, reach)).toBe(0);
  });

  it('peaks at the front, which travels outward', () => {
    // At p = 0.5 the front is 0.5 * 1000 * 0.55 = 275 px from the click.
    const onFront = rippleFactor(ripple, 100 + 275, 100, 1500, duration, reach);
    expect(onFront).toBeCloseTo(0.5, 10);
    expect(rippleFactor(ripple, 100 + 235, 100, 1500, duration, reach)).toBeLessThan(onFront);
    expect(rippleFactor(ripple, 100 + 315, 100, 1500, duration, reach)).toBeLessThan(onFront);
    expect(rippleFactor(ripple, 100, 100, 1500, duration, reach)).toBeLessThan(onFront);
  });

  it('weakens as the ripple ages', () => {
    const early = rippleFactor(ripple, 100 + 0.1 * reach * 0.55, 100, 1100, duration, reach);
    const late = rippleFactor(ripple, 100 + 0.9 * reach * 0.55, 100, 1900, duration, reach);
    expect(late).toBeLessThan(early);
  });
});

describe('activeRipples', () => {
  it('drops the ones that have ended', () => {
    const ripples: Ripple[] = [{ x: 0, y: 0, start: 0 }, { x: 0, y: 0, start: 900 }];
    expect(activeRipples(ripples, 1000, 1000)).toEqual([ripples[1]]);
  });

  it('keeps at most eight, the oldest falling first', () => {
    const ripples: Ripple[] = Array.from({ length: 10 }, (_, i) => ({ x: i, y: 0, start: 100 + i }));
    const kept = activeRipples(ripples, 200, 1000);
    expect(kept).toHaveLength(8);
    expect(kept[0]).toBe(ripples[2]);
    expect(kept[7]).toBe(ripples[9]);
  });
});

describe('isBackgroundTarget', () => {
  const root = document.createElement('div');
  const content = document.createElement('div');
  const card = document.createElement('section');
  const link = document.createElement('a');
  const canvas = document.createElement('canvas');
  card.appendChild(link);
  content.appendChild(card);
  root.append(canvas, content);

  it('is true for the root, the content itself and what lies outside the content', () => {
    expect(isBackgroundTarget(root, root, content)).toBe(true);
    expect(isBackgroundTarget(content, root, content)).toBe(true);
    expect(isBackgroundTarget(canvas, root, content)).toBe(true);
  });

  it('is false for anything inside the content: card, logo, link', () => {
    expect(isBackgroundTarget(card, root, content)).toBe(false);
    expect(isBackgroundTarget(link, root, content)).toBe(false);
  });

  it('is false for a target outside the root or none', () => {
    expect(isBackgroundTarget(document.body, root, content)).toBe(false);
    expect(isBackgroundTarget(null, root, content)).toBe(false);
  });
});

describe('prefersReducedMotion', () => {
  afterEach(() => vi.unstubAllGlobals());

  it('is false where the browser cannot tell', () => {
    vi.stubGlobal('matchMedia', undefined);
    expect(prefersReducedMotion()).toBe(false);
  });

  it('follows the media query', () => {
    vi.stubGlobal('matchMedia', (query: string) => ({ matches: query.includes('reduce') }));
    expect(prefersReducedMotion()).toBe(true);
    vi.stubGlobal('matchMedia', () => ({ matches: false }));
    expect(prefersReducedMotion()).toBe(false);
  });
});

describe('readHalftoneTheme', () => {
  let style: HTMLStyleElement;
  let root: HTMLElement;

  beforeEach(() => {
    root = document.createElement('div');
    document.body.appendChild(root);
    style = document.createElement('style');
    document.head.appendChild(style);
  });

  afterEach(() => {
    style.remove();
    root.remove();
    document.documentElement.removeAttribute('data-tl-mode');
  });

  const define = (css: string) => { style.textContent = css; };

  it('reads colours, lengths and durations of the tokens', () => {
    define(`:root {
      --tl-surface-halftone: #112233; --tl-surface-halftone-strong: #445566;
      --tl-size-halftone-step: 1rem; --tl-size-halftone-glow: 2.5rem;
      --tl-duration-ripple: 1.8s;
    }`);
    const theme = readHalftoneTheme(root)!;
    expect(theme.color).toBe('#112233');
    expect(theme.strong).toBe('#445566');
    expect(theme.step).toBe(16);
    expect(theme.glow).toBe(40);
    expect(theme.rippleMs).toBe(1800);
  });

  it('is null without the base colour', () => {
    define(':root { --tl-surface-halftone-strong: #445566; }');
    expect(readHalftoneTheme(root)).toBeNull();
  });

  it('follows the mode of the document root', () => {
    define(`
      :root { --tl-surface-halftone: #111111; --tl-surface-halftone-strong: #222222; }
      :root[data-tl-mode="dark"] { --tl-surface-halftone: #eeeeee; --tl-surface-halftone-strong: #ffffff; }`);
    expect(readHalftoneTheme(root)!.color).toBe('#111111');
    document.documentElement.setAttribute('data-tl-mode', 'dark');
    const dark = readHalftoneTheme(root)!;
    expect(dark.color).toBe('#eeeeee');
    expect(dark.strong).toBe('#ffffff');
  });

  it('falls back to 16 px for a step that is too small to draw or not a length', () => {
    for (const bad of ['0px', '2px', '3.9px', '-8px', 'auto']) {
      define(`:root { --tl-surface-halftone: #112233; --tl-size-halftone-step: ${bad}; }`);
      expect(readHalftoneTheme(root)!.step).toBe(16);
    }
    define(':root { --tl-surface-halftone: #112233; --tl-size-halftone-step: 4px; }');
    expect(readHalftoneTheme(root)!.step).toBe(4);
  });

  it('is null for a colour the browser does not take', () => {
    define(':root { --tl-surface-halftone: not-a-colour; --tl-surface-halftone-strong: #445566; }');
    vi.stubGlobal('CSS', { supports: (_property: string, value: string) => value !== 'not-a-colour' });
    expect(readHalftoneTheme(root)).toBeNull();
    define(':root { --tl-surface-halftone: #112233; --tl-surface-halftone-strong: not-a-colour; }');
    expect(readHalftoneTheme(root)).toBeNull();
    define(':root { --tl-surface-halftone: #112233; --tl-surface-halftone-strong: #445566; }');
    expect(readHalftoneTheme(root)).not.toBeNull();
    vi.unstubAllGlobals();
  });

  it('takes the colours as they are where CSS.supports is missing', () => {
    vi.stubGlobal('CSS', undefined);
    define(':root { --tl-surface-halftone: #112233; }');
    expect(readHalftoneTheme(root)!.color).toBe('#112233');
    vi.unstubAllGlobals();
  });
});

describe('boundedStep', () => {
  it('keeps a usable step', () => {
    expect(boundedStep(4)).toBe(4);
    expect(boundedStep(16)).toBe(16);
    expect(boundedStep(24.5)).toBe(24.5);
  });

  it('falls back to 16 for a step below 4, or one that is not a finite number', () => {
    for (const bad of [3.99, 1, 0, -5, NaN, Infinity, -Infinity]) {
      expect(boundedStep(bad)).toBe(16);
    }
  });
});
