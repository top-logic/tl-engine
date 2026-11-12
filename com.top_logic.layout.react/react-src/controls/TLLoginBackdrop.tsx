import { React, useTLState, TLChild, useFillHost, FillProvider, rootClassName, useTokenSwitch } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import {
  activeRipples,
  dotRadius,
  drawShape,
  glowFactor,
  isBackgroundTarget,
  nextShape,
  prefersReducedMotion,
  readHalftoneTheme,
  rippleFactor,
} from './halftone';
import type { HalftoneShape, HalftoneTheme, Ripple } from './halftone';

/** The share of the way to the target the pointer highlight covers per animation frame. */
const GLOW_EASE = 0.15;

/** Below this distance from its target the highlight is taken to have arrived. */
const GLOW_EPSILON = 0.01;

/** Dots smaller than this radius (px), after highlight and ripple were added, are not drawn. */
const MIN_DRAWN_RADIUS = 0.3;

/** What the highlight and a ripple add to the radius of a dot at full strength (px). */
const GLOW_GROWTH = 0.8;
const RIPPLE_GROWTH = 2.2;

/** Whether two readings of the tokens are the same, so a switch that changes nothing the grid uses draws nothing. */
const SAME = (a: HalftoneTheme | null, b: HalftoneTheme | null): boolean =>
  a === b || (a !== null && b !== null
    && a.color === b.color && a.strong === b.strong && a.step === b.step && a.glow === b.glow
    && a.rippleMs === b.rippleMs && a.morphMs === b.morphMs && a.dark === b.dark);

/** What changes while the grid is shown, between two drawings; kept outside of React state, since it does not render. */
interface GridState {
  /** The pointer in the area of the root, or `null` while it is away. */
  pointer: { x: number; y: number } | null;
  /** How much the highlight shows, from 0 to 1: it follows the pointer's presence with a delay. */
  glow: number;
  ripples: Ripple[];
  /** Clicks on the background so far. */
  clicks: number;
  shape: HalftoneShape;
  /** The shape the grid changes from, while it crossfades. */
  previous: HalftoneShape;
  /** When the crossfade started; far in the past while there is none. */
  morphStart: number;
}

/**
 * The background of the login page: a halftone grid drawn on a canvas behind the content, which is centred in front.
 *
 * State:
 * - child: ChildDescriptor | null - the content in front of the grid (the login card and what belongs to it)
 * - logo: string | null - URL of the logo shown above the content; without it no logo is rendered
 * - logoDark: string | null - URL of the logo in dark mode; without it the logo is used in both modes
 * - logoAlt: string | null - the alternative text of the logo; empty, since the logo only decorates, when absent
 *
 * The dots are largest at the centre and fade out toward the corners. The colours, the grid step, the radius of the
 * highlight and the durations come from the design system tokens and are read again when the density, theme or mode
 * of the page switches. A mouse pointer lifts the dots near it, a click on the background sends a ripple through the
 * grid, and every fifth such click changes the shape of the dots with a crossfade. With reduced motion there is
 * neither highlight nor ripple, and the shape changes at once.
 *
 * The grid is drawn on occasion only - when mounted, resized, switched, or touched - and an animation frame runs only
 * while the highlight fades, a ripple travels or the shape crossfades; at rest the page does no work.
 *
 * Takes part in the fill contract as a container: the backdrop fills its container, so the page it frames is as high
 * as the window.
 */
const TLLoginBackdrop: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const [fillClass, fillHost] = useFillHost(true);
  const rootRef = React.useRef<HTMLDivElement>(null);
  const contentRef = React.useRef<HTMLDivElement>(null);
  const canvasRef = React.useRef<HTMLCanvasElement>(null);
  const grid = React.useRef<GridState>({
    pointer: null,
    glow: 0,
    ripples: [],
    clicks: 0,
    shape: 'dot',
    previous: 'dot',
    morphStart: -Infinity,
  });

  const theme = useTokenSwitch<HalftoneTheme | null>(
    () => (rootRef.current ? readHalftoneTheme(rootRef.current) : null), null, SAME);
  const dark = useTokenSwitch(() => document.documentElement.getAttribute('data-tl-mode') === 'dark', false);

  React.useEffect(() => {
    const root = rootRef.current;
    const content = contentRef.current;
    const canvas = canvasRef.current;
    const g = canvas?.getContext('2d');
    if (!root || !content || !canvas || !g || !theme) {
      return undefined;
    }
    const { color, strong, step, glow, rippleMs, morphMs } = theme;
    const s = grid.current;
    let frame = 0;
    let width = 0;
    let height = 0;

    /** Sizes the canvas to the root, in device pixels. */
    const resize = () => {
      width = root.clientWidth;
      height = root.clientHeight;
      const ratio = window.devicePixelRatio || 1;
      canvas.width = Math.round(width * ratio);
      canvas.height = Math.round(height * ratio);
      g.setTransform(ratio, 0, 0, ratio, 0, 0);
    };

    /** One dot: in the base colour, and over it in the strong one as much as the lift `k` says. */
    const paint = (shape: HalftoneShape, x: number, y: number, r: number, k: number) => {
      if (r < MIN_DRAWN_RADIUS) {
        return;
      }
      g.globalAlpha = 1;
      g.fillStyle = color;
      drawShape(g, shape, x, y, r);
      if (k > 0.01) {
        g.globalAlpha = Math.min(1, k);
        g.fillStyle = strong;
        drawShape(g, shape, x, y, r);
      }
    };

    const draw = (now: number) => {
      g.clearRect(0, 0, width, height);
      if (width === 0 || height === 0) {
        return;
      }
      const cx = width / 2;
      const cy = height / 2;
      const halfDiagonal = Math.hypot(width, height) / 2;
      const reach = halfDiagonal * 2;
      const morph = morphMs > 0 ? Math.max(0, Math.min(1, (now - s.morphStart) / morphMs)) : 1;
      const lifted = s.pointer !== null && s.glow > GLOW_EPSILON;
      for (let y = cy % step; y < height; y += step) {
        for (let x = cx % step; x < width; x += step) {
          let r = dotRadius(Math.hypot(x - cx, y - cy), halfDiagonal, step);
          let k = 0;
          if (lifted) {
            const h = glowFactor(x - s.pointer!.x, y - s.pointer!.y, glow) * s.glow;
            r += GLOW_GROWTH * h;
            k += h;
          }
          for (const ripple of s.ripples) {
            const w = rippleFactor(ripple, x, y, now, rippleMs, reach);
            r += RIPPLE_GROWTH * w;
            k += w;
          }
          if (morph < 1) {
            paint(s.previous, x, y, r * (1 - morph), k);
            paint(s.shape, x, y, r * morph, k);
          } else {
            paint(s.shape, x, y, r, k);
          }
        }
      }
      g.globalAlpha = 1;
    };

    /** One animation frame: advances what runs, draws, and asks for the next frame only while something still runs. */
    const tick = (now: number) => {
      frame = 0;
      const target = s.pointer !== null ? 1 : 0;
      s.glow += (target - s.glow) * GLOW_EASE;
      if (Math.abs(target - s.glow) <= GLOW_EPSILON) {
        s.glow = target;
      }
      s.ripples = activeRipples(s.ripples, now, rippleMs);
      draw(now);
      if (s.glow !== target || s.ripples.length > 0 || now - s.morphStart < morphMs) {
        frame = requestAnimationFrame(tick);
      }
    };

    /** Draws soon, once for all the occasions until the next frame. */
    const schedule = () => {
      if (frame === 0) {
        frame = requestAnimationFrame(tick);
      }
    };

    const onPointerMove = (e: PointerEvent) => {
      if (e.pointerType !== 'mouse' || prefersReducedMotion()) {
        return;
      }
      const box = root.getBoundingClientRect();
      s.pointer = { x: e.clientX - box.left, y: e.clientY - box.top };
      schedule();
    };

    const onPointerLeave = (e: PointerEvent) => {
      if (e.pointerType !== 'mouse' || s.pointer === null) {
        return;
      }
      s.pointer = null;
      schedule();
    };

    const onClick = (e: MouseEvent) => {
      if (!isBackgroundTarget(e.target, root, content)) {
        return;
      }
      const reduced = prefersReducedMotion();
      const now = performance.now();
      s.clicks++;
      let changed = false;
      if (!reduced) {
        const box = root.getBoundingClientRect();
        s.ripples = [...s.ripples, { x: e.clientX - box.left, y: e.clientY - box.top, start: now }];
        changed = true;
      }
      const shape = nextShape(s.clicks);
      if (shape !== s.shape) {
        s.previous = s.shape;
        s.shape = shape;
        s.morphStart = reduced ? -Infinity : now;
        changed = true;
      }
      if (changed) {
        schedule();
      }
    };

    resize();
    draw(performance.now());
    const observer = typeof ResizeObserver === 'function'
      ? new ResizeObserver(() => { resize(); draw(performance.now()); })
      : null;
    observer?.observe(root);
    root.addEventListener('pointermove', onPointerMove);
    root.addEventListener('pointerleave', onPointerLeave);
    root.addEventListener('click', onClick);
    // A change of shape or ripple that was running when the tokens switched goes on.
    if (s.ripples.length > 0 || s.glow > 0) {
      schedule();
    }
    return () => {
      observer?.disconnect();
      root.removeEventListener('pointermove', onPointerMove);
      root.removeEventListener('pointerleave', onPointerLeave);
      root.removeEventListener('click', onClick);
      if (frame !== 0) {
        cancelAnimationFrame(frame);
      }
    };
  }, [theme]);

  const logo = state.logo as string | null | undefined;
  const logoDark = state.logoDark as string | null | undefined;
  const logoAlt = state.logoAlt as string | null | undefined;

  return (
    <FillProvider host={fillHost}>
      <div id={controlId} ref={rootRef} className={rootClassName(state, 'tl-login-backdrop', fillClass)}>
        <canvas ref={canvasRef} className="tl-login-backdrop__canvas" aria-hidden="true" />
        <div ref={contentRef} className="tl-login-backdrop__content">
          {!!logo && (
            <div className="tl-login-backdrop__brand">
              <img className="tl-login-backdrop__logo" src={dark && logoDark ? logoDark : logo} alt={logoAlt ?? ''} />
            </div>
          )}
          {!!state.child && <TLChild control={state.child} />}
        </div>
      </div>
    </FillProvider>
  );
};

export default TLLoginBackdrop;
