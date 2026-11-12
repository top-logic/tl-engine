import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

vi.mock('../bridge/sse-client', async (importOriginal) => ({
  ...(await importOriginal<typeof import('../bridge/sse-client')>()),
  connect: () => {},
  subscribe: () => {},
  unsubscribe: () => {},
}));

// The bridge first, as the page loads it, so that the modules it imports see the mocked SSE client.
import { mount, unmount } from '../bridge/tl-react-bridge';
import { register } from 'tl-react-bridge';
import TLLoginBackdrop from './TLLoginBackdrop';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const CONTROL_ID = 'backdrop';

/** A resize observer for jsdom, which has none; it reports nothing. */
class FakeResizeObserver {
  observe(): void {}
  unobserve(): void {}
  disconnect(): void {}
}

/** The calls drawn on a fake 2d context, and the animation frames asked for and not yet run. */
let drawn: string[];
/** The fill style in force at each shape drawn. */
let fills: string[];
let frames: FrameRequestCallback[];
let style: HTMLStyleElement;

/** A 2d context that records what is drawn. */
function fakeContext() {
  const record = (name: string) => () => { drawn.push(name); };
  const shape = (name: string) => function (this: { fillStyle: string }) { drawn.push(name); fills.push(this.fillStyle); };
  return {
    setTransform: record('setTransform'), clearRect: record('clearRect'), beginPath: record('beginPath'),
    arc: shape('arc'), fill: record('fill'), fillRect: shape('fillRect'),
    globalAlpha: 1, fillStyle: '',
  };
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  register('TLLoginBackdrop', TLLoginBackdrop);
});

beforeEach(() => {
  drawn = [];
  fills = [];
  frames = [];
  vi.stubGlobal('ResizeObserver', FakeResizeObserver);
  vi.stubGlobal('matchMedia', () => ({ matches: false }));
  vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => frames.push(callback));
  vi.stubGlobal('cancelAnimationFrame', () => {});
  style = document.createElement('style');
  style.textContent = `:root { --tl-surface-halftone: #112233; --tl-surface-halftone-strong: #445566;
    --tl-size-halftone-step: 16px; --tl-size-halftone-glow: 40px; --tl-duration-ripple: 1800ms; --tl-duration-morph: 700ms; }
    :root[data-tl-mode="dark"] { --tl-surface-halftone: #eeeeee; --tl-surface-halftone-strong: #ffffff; }`;
  document.head.appendChild(style);
});

afterEach(() => {
  act(() => unmount(CONTROL_ID));
  document.getElementById(CONTROL_ID)?.remove();
  style.remove();
  document.documentElement.removeAttribute('data-tl-mode');
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

afterAll(() => {
  document.body.replaceChildren();
});

/** Mounts a backdrop with the given state and answers the element it is drawn into. */
function mountBackdrop(state: Record<string, unknown>): HTMLElement {
  const element = document.createElement('div');
  element.id = CONTROL_ID;
  document.body.appendChild(element);
  act(() => mount(CONTROL_ID, 'TLLoginBackdrop', state, 'w', '/ctx'));
  return document.getElementById(CONTROL_ID)!;
}

/** Lets the root of the backdrop be 320 by 240 px large and the canvas draw on a fake context. */
function sizeAndContext() {
  vi.spyOn(HTMLElement.prototype, 'clientWidth', 'get').mockReturnValue(320);
  vi.spyOn(HTMLElement.prototype, 'clientHeight', 'get').mockReturnValue(240);
  vi.spyOn(HTMLCanvasElement.prototype, 'getContext').mockReturnValue(fakeContext() as unknown as CanvasRenderingContext2D);
}

describe('the structure', () => {
  it('draws a canvas hidden from assistive technology behind the content', () => {
    const root = mountBackdrop({});
    expect(root.querySelector('.tl-login-backdrop')).not.toBeNull();
    const canvas = root.querySelector('canvas.tl-login-backdrop__canvas')!;
    expect(canvas.getAttribute('aria-hidden')).toBe('true');
    expect(root.querySelector('.tl-login-backdrop__content')).not.toBeNull();
  });

  it('renders no brand without a logo', () => {
    const root = mountBackdrop({ logo: null, logoDark: '/dark.svg' });
    expect(root.querySelector('.tl-login-backdrop__brand')).toBeNull();
    expect(root.querySelector('img')).toBeNull();
  });

  it('puts the logo in the brand, with the alternative text, and none when there is none', () => {
    const root = mountBackdrop({ logo: '/light.svg', logoAlt: 'Acme' });
    const img = root.querySelector('.tl-login-backdrop__brand > img.tl-login-backdrop__logo')!;
    expect(img.getAttribute('src')).toBe('/light.svg');
    expect(img.getAttribute('alt')).toBe('Acme');
  });

  it('leaves the alternative text empty without one', () => {
    const root = mountBackdrop({ logo: '/light.svg' });
    expect(root.querySelector('img')!.getAttribute('alt')).toBe('');
  });

  it('follows the mode with the one logo, falling back to the light one', async () => {
    const root = mountBackdrop({ logo: '/light.svg', logoDark: '/dark.svg' });
    expect(root.querySelectorAll('img')).toHaveLength(1);
    expect(root.querySelector('img')!.getAttribute('src')).toBe('/light.svg');
    await act(async () => document.documentElement.setAttribute('data-tl-mode', 'dark'));
    expect(root.querySelector('img')!.getAttribute('src')).toBe('/dark.svg');
    await act(async () => document.documentElement.setAttribute('data-tl-mode', 'light'));
    expect(root.querySelector('img')!.getAttribute('src')).toBe('/light.svg');
  });

  it('uses the light logo in dark mode when there is no dark one', () => {
    document.documentElement.setAttribute('data-tl-mode', 'dark');
    const root = mountBackdrop({ logo: '/light.svg' });
    expect(root.querySelector('img')!.getAttribute('src')).toBe('/light.svg');
  });
});

describe('the grid', () => {
  it('draws nothing and does not throw where the canvas has no 2d context', () => {
    // jsdom has none: getContext answers nothing.
    const root = mountBackdrop({});
    const canvas = root.querySelector('canvas')!;
    expect(canvas.getContext('2d')).toBeNull();
    act(() => { root.querySelector('.tl-login-backdrop')!.dispatchEvent(new MouseEvent('click', { bubbles: true })); });
    expect(frames).toHaveLength(0);
  });

  it('draws nothing without the halftone tokens', () => {
    style.textContent = '';
    sizeAndContext();
    mountBackdrop({});
    expect(drawn).toEqual([]);
  });

  it('draws the grid once when mounted, and asks for no animation frame at rest', () => {
    sizeAndContext();
    mountBackdrop({});
    expect(drawn).toContain('arc');
    expect(frames).toHaveLength(0);
  });

  it('sends a ripple through the grid on a click on the background, and stops when it has ended', () => {
    sizeAndContext();
    const root = mountBackdrop({});
    const backdrop = root.querySelector('.tl-login-backdrop')!;
    act(() => { backdrop.dispatchEvent(new MouseEvent('click', { bubbles: true, clientX: 10, clientY: 10 })); });
    expect(frames).toHaveLength(1);
    const now = performance.now();
    drawn.length = 0;
    act(() => { frames.shift()!(now + 100); });
    expect(drawn).toContain('arc');
    expect(frames).toHaveLength(1);
    act(() => { frames.shift()!(now + 5000); });
    expect(frames).toHaveLength(0);
  });

  it('ignores a click inside the content', () => {
    sizeAndContext();
    const root = mountBackdrop({ logo: '/light.svg' });
    act(() => { root.querySelector('img')!.dispatchEvent(new MouseEvent('click', { bubbles: true })); });
    expect(frames).toHaveLength(0);
  });

  it('makes no ripple and no frame with reduced motion', () => {
    vi.stubGlobal('matchMedia', () => ({ matches: true }));
    sizeAndContext();
    const root = mountBackdrop({});
    act(() => { root.querySelector('.tl-login-backdrop')!.dispatchEvent(new MouseEvent('click', { bubbles: true })); });
    // Only the shape could change, and not on the first click.
    const pointer = new Event('pointermove', { bubbles: true }) as Event & { pointerType: string };
    Object.assign(pointer, { pointerType: 'mouse', clientX: 5, clientY: 5 });
    act(() => { root.querySelector('.tl-login-backdrop')!.dispatchEvent(pointer); });
    expect(frames).toHaveLength(0);
  });

  it('changes the shape with the fifth click on the background, at once with reduced motion', () => {
    vi.stubGlobal('matchMedia', () => ({ matches: true }));
    sizeAndContext();
    const root = mountBackdrop({});
    const backdrop = root.querySelector('.tl-login-backdrop')!;
    expect(drawn).not.toContain('fillRect');
    for (let i = 0; i < 4; i++) {
      act(() => { backdrop.dispatchEvent(new MouseEvent('click', { bubbles: true })); });
    }
    expect(frames).toHaveLength(0);
    act(() => { backdrop.dispatchEvent(new MouseEvent('click', { bubbles: true })); });
    expect(frames).toHaveLength(1);
    drawn.length = 0;
    act(() => { frames.shift()!(performance.now()); });
    expect(drawn).toContain('fillRect');
    expect(drawn).not.toContain('arc');
    expect(frames).toHaveLength(0);
  });

  /** Runs the pending animation frames, one after the other 16 ms apart, until none is asked for; answers how many ran. */
  function runToRest(): number {
    let count = 0;
    let now = performance.now();
    while (frames.length > 0 && count < 500) {
      now += 16;
      act(() => { frames.shift()!(now); });
      count++;
    }
    return count;
  }

  /** Dispatches a pointer event of the given type and kind of pointer at the element. */
  function pointer(target: Element, type: string, pointerType = 'mouse') {
    const event = new Event(type, { bubbles: false });
    Object.assign(event, { pointerType, clientX: 5, clientY: 5 });
    act(() => { target.dispatchEvent(event); });
  }

  it('lets the highlight fade out where the pointer left, instead of ending it at once', () => {
    sizeAndContext();
    const backdrop = mountBackdrop({}).querySelector('.tl-login-backdrop')!;
    pointer(backdrop, 'pointermove');
    expect(runToRest()).toBeGreaterThan(1);
    // At rest with the pointer inside, the highlight is at full strength and no frame is asked for.
    expect(frames).toHaveLength(0);
    pointer(backdrop, 'pointerleave');
    expect(frames).toHaveLength(1);
    fills.length = 0;
    act(() => { const f = frames.shift()!; f(performance.now() + 16); });
    // The next frame still draws the highlight in the strong colour, over the base dots.
    expect(fills).toContain('#445566');
    expect(frames).toHaveLength(1);
    expect(runToRest()).toBeGreaterThan(5);
    expect(frames).toHaveLength(0);
    // A second leave changes nothing.
    pointer(backdrop, 'pointerleave');
    expect(frames).toHaveLength(0);
  });

  it('goes on with a crossfade that was running when the tokens switched', async () => {
    // The ripples are short, so that only the crossfade is still running when the tokens switch.
    style.textContent = style.textContent!.replace('--tl-duration-ripple: 1800ms', '--tl-duration-ripple: 100ms');
    sizeAndContext();
    const clock = vi.spyOn(performance, 'now').mockReturnValue(1000);
    const backdrop = mountBackdrop({}).querySelector('.tl-login-backdrop')!;
    for (let i = 0; i < 5; i++) {
      act(() => { backdrop.dispatchEvent(new MouseEvent('click', { bubbles: true })); });
    }
    // A frame after the ripples have ended clears them away; the crossfade goes on.
    act(() => { frames.shift()!(1150); });
    expect(frames).toHaveLength(1);
    frames.length = 0;
    clock.mockReturnValue(1300);
    await act(async () => document.documentElement.setAttribute('data-tl-mode', 'dark'));
    expect(frames).toHaveLength(1);
  });

  it('highlights for a mouse only', () => {
    sizeAndContext();
    const root = mountBackdrop({});
    const backdrop = root.querySelector('.tl-login-backdrop')!;
    const move = (pointerType: string) => {
      const event = new Event('pointermove', { bubbles: true });
      Object.assign(event, { pointerType, clientX: 5, clientY: 5 });
      act(() => { backdrop.dispatchEvent(event); });
    };
    move('touch');
    expect(frames).toHaveLength(0);
    move('mouse');
    expect(frames).toHaveLength(1);
  });
});
