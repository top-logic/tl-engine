/**
 * The computations behind the halftone grid of the login backdrop: how large a dot is, how the pointer and a click
 * ripple lift the dots near them, and which shape the grid is drawn in.
 *
 * Everything here is a pure function of its arguments (or reads the page where it says so); the component
 * `TLLoginBackdrop` owns the canvas, the events and the timing.
 */
import { tokenMs, tokenPx } from 'tl-react-bridge';

/** The shape one grid point is drawn in. */
export type HalftoneShape = 'dot' | 'plus' | 'square';

/** The shapes in the order the grid passes through them. */
export const SHAPES: HalftoneShape[] = ['dot', 'plus', 'square'];

/** How many clicks on the background it takes to change the shape. */
export const CLICKS_PER_SHAPE = 5;

/** The most ripples that run at the same time; a click beyond that drops the oldest. */
const MAX_RIPPLES = 8;

/** The dots below this radius (px) are not drawn: they would only blur. */
const MIN_RADIUS = 0.35;

/** The width (px) of the band a ripple lifts around its front. */
const RIPPLE_BAND = 20;

/** How far the front of a ripple travels, as a fraction of the reach, until the ripple has ended. */
const RIPPLE_TRAVEL = 0.55;

/**
 * The radius of the dot at the given distance from the centre of the grid: a third of the step at the centre, falling
 * to nothing at the half diagonal. A radius too small to see is zero.
 *
 * @param distance Distance of the grid point from the centre (px).
 * @param halfDiagonal Half the diagonal of the area, where the dots end (px).
 * @param step The grid step (px).
 */
export function dotRadius(distance: number, halfDiagonal: number, step: number): number {
  const d = distance / halfDiagonal;
  if (d >= 1) {
    return 0;
  }
  const r = 0.33 * step * Math.pow(1 - d, 1.3);
  return r < MIN_RADIUS ? 0 : r;
}

/**
 * How strongly the pointer lifts a dot at the given offset from it: one at the pointer, `e^-1` one radius away.
 *
 * @param dx Horizontal offset of the dot from the pointer (px).
 * @param dy Vertical offset of the dot from the pointer (px).
 * @param radius The radius of the highlight (px).
 */
export function glowFactor(dx: number, dy: number, radius: number): number {
  const d = Math.sqrt(dx * dx + dy * dy) / radius;
  return Math.exp(-d * d);
}

/** A click ripple: where it started (px, in the area of the canvas) and when (ms, the clock of the animation frames). */
export interface Ripple {
  x: number;
  y: number;
  start: number;
}

/**
 * How strongly a ripple lifts the point at the given position: a band around the front, which travels outward and
 * weakens with the age of the ripple. Zero before the start and from the end on.
 *
 * @param r The ripple.
 * @param px Horizontal position of the point (px).
 * @param py Vertical position of the point (px).
 * @param now The time now (ms).
 * @param durationMs How long a ripple runs (ms).
 * @param reach How far a ripple may reach (px), usually the diagonal of the area.
 */
export function rippleFactor(r: Ripple, px: number, py: number, now: number, durationMs: number, reach: number): number {
  const p = (now - r.start) / durationMs;
  if (!(p >= 0 && p < 1)) {
    return 0;
  }
  const front = p * reach * RIPPLE_TRAVEL;
  const off = (Math.hypot(px - r.x, py - r.y) - front) / RIPPLE_BAND;
  return Math.exp(-off * off) * (1 - p);
}

/**
 * The ripples still running, at most {@link MAX_RIPPLES}; the oldest of too many fall away.
 *
 * @param ripples The ripples, oldest first.
 * @param now The time now (ms).
 * @param durationMs How long a ripple runs (ms).
 */
export function activeRipples(ripples: Ripple[], now: number, durationMs: number): Ripple[] {
  const running = ripples.filter((r) => now - r.start < durationMs);
  return running.length > MAX_RIPPLES ? running.slice(running.length - MAX_RIPPLES) : running;
}

/**
 * The shape after the given number of clicks on the background: every {@link CLICKS_PER_SHAPE}th click moves on to the
 * next of the {@link SHAPES}, and after the last the first comes again.
 */
export function nextShape(clicks: number): HalftoneShape {
  return SHAPES[Math.floor(clicks / CLICKS_PER_SHAPE) % SHAPES.length];
}

/**
 * Whether a click at the given target counts as one on the background: the root, the content itself, and everything
 * outside the content - not the card, the logo or a link inside it.
 *
 * @param target The target of the event.
 * @param root The root element of the backdrop.
 * @param content The element holding the logo and the content the backdrop frames.
 */
export function isBackgroundTarget(target: EventTarget | null, root: Element, content: Element): boolean {
  if (!(target instanceof Node) || !root.contains(target)) {
    return false;
  }
  return target === content || !content.contains(target);
}

/**
 * Fills one grid point in the current fill style of the context: a circle of the radius, a plus whose two bars are
 * `2.6 r` long and `max(0.7, 0.5 r)` thick, or a square with an edge of `1.8 r`.
 *
 * @param g The drawing context, its fill style set.
 * @param shape The shape.
 * @param x Horizontal centre (px).
 * @param y Vertical centre (px).
 * @param r The radius of the dot the shape stands for (px).
 */
export function drawShape(g: CanvasRenderingContext2D, shape: HalftoneShape, x: number, y: number, r: number): void {
  if (shape === 'dot') {
    g.beginPath();
    g.arc(x, y, r, 0, Math.PI * 2);
    g.fill();
  } else if (shape === 'plus') {
    const length = 2.6 * r;
    const thickness = Math.max(0.7, 0.5 * r);
    g.fillRect(x - length / 2, y - thickness / 2, length, thickness);
    g.fillRect(x - thickness / 2, y - length / 2, thickness, length);
  } else {
    const edge = 1.8 * r;
    g.fillRect(x - edge / 2, y - edge / 2, edge, edge);
  }
}

/** Whether the user asked for reduced motion: then there is no highlight, no ripple and no crossfade. */
export function prefersReducedMotion(): boolean {
  return typeof window.matchMedia === 'function' && window.matchMedia('(prefers-reduced-motion: reduce)').matches;
}

/** What the grid is drawn with, read from the tokens. */
export interface HalftoneTheme {
  /** The colour of the dots at rest (`surface-halftone`). */
  color: string;
  /** The colour a dot takes where the pointer or a ripple lifts it (`surface-halftone-strong`). */
  strong: string;
  /** The grid step (px). */
  step: number;
  /** The radius of the pointer highlight (px). */
  glow: number;
  /** How long a ripple runs (ms). */
  rippleMs: number;
  /** How long the crossfade between two shapes runs (ms). */
  morphMs: number;
  /** Whether the document root is in dark mode. */
  dark: boolean;
}

/**
 * Reads the halftone tokens at the given element.
 *
 * @param root Where the colours and the step are read; the durations are read at the document root.
 * @returns The theme, or `null` while the base colour is not defined (no design system loaded): nothing is drawn then.
 */
export function readHalftoneTheme(root: Element): HalftoneTheme | null {
  const style = getComputedStyle(root);
  const color = style.getPropertyValue('--tl-surface-halftone').trim();
  if (color === '') {
    return null;
  }
  return {
    color,
    strong: style.getPropertyValue('--tl-surface-halftone-strong').trim() || color,
    step: tokenPx(root, '--tl-size-halftone-step', 16),
    glow: tokenPx(root, '--tl-size-halftone-glow', 40),
    rippleMs: tokenMs('--tl-duration-ripple', 1800),
    morphMs: tokenMs('--tl-duration-morph', 700),
    dark: document.documentElement.getAttribute('data-tl-mode') === 'dark',
  };
}
