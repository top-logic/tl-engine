import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { initKeyboardDispatcher } from './keyboard-dispatcher';
import { initFocusTrap } from './focus-trap';
import { initOutsidePress } from './outside-press';
import { KeyboardScopeProvider, useKeyboardBinding } from './tl-react-bridge';
import { usePopupSurface } from './popup-surface';
import type { PopupInitialFocus } from './popup-surface';
import { usePopupTrigger } from './popup-trigger';
import { fittedMaxHeight, popoverMiddleware } from './popover';
import { useRovingMenu } from './menu';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

// The document listeners the bridge entry installs when the page loads it.
beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  initKeyboardDispatcher();
  initFocusTrap();
  initOutsidePress();
});

let root: Root | null = null;
let host: HTMLElement | null = null;

function render(element: React.ReactElement): void {
  host = document.createElement('div');
  document.body.appendChild(host);
  root = createRoot(host);
  act(() => root!.render(element));
}

beforeEach(() => {
  // jsdom lays nothing out; the focus search skips elements without a client rect.
  vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([new DOMRect()] as unknown as DOMRectList);
});

afterEach(() => {
  act(() => root?.unmount());
  host?.remove();
  root = null;
  host = null;
  vi.restoreAllMocks();
  vi.unstubAllGlobals();
});

function button(name: string): HTMLButtonElement {
  const found = Array.from(document.querySelectorAll('button')).find(element => element.textContent === name);
  if (!found) {
    throw new Error('No button ' + name);
  }
  return found;
}

function surface(): HTMLElement | null {
  return document.querySelector<HTMLElement>('[data-surface]');
}

/** The press of a pointer on the element: what reaches the document before the click. */
function press(element: Element): void {
  act(() => {
    element.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true, cancelable: true }));
  });
}

/** A click that a pointer press carries (a click count of one). */
function clickOf(element: Element): void {
  act(() => {
    element.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, detail: 1 }));
  });
}

/** Press and click: a click of the pointer. */
function pointerClick(element: Element): void {
  press(element);
  clickOf(element);
}

/** A click without a press: the keyboard activation of a button. */
function keyboardClick(element: Element): void {
  act(() => {
    element.dispatchEvent(new MouseEvent('click', { bubbles: true, cancelable: true, detail: 0 }));
  });
}

function key(element: Element, name: string): KeyboardEvent {
  const event = new KeyboardEvent('keydown', { key: name, bubbles: true, cancelable: true });
  act(() => {
    element.dispatchEvent(event);
  });
  return event;
}

interface HostProps {
  /** Whether the surface stays mounted while closed, only hidden (the toolbar overflow). */
  mounted?: boolean;
  /** Whether the trigger is among the elements a press may land in without closing the surface. */
  inside?: boolean;
  trapFocus?: boolean;
  initialFocus?: PopupInitialFocus;
  onClose?: () => void;
}

/** A trigger and the surface it opens, wired as the menus and popovers of the bridge wire them. */
function Host({ mounted = false, inside = false, trapFocus, initialFocus = 'first', onClose }: HostProps) {
  const [open, setOpen] = React.useState(false);
  const triggerRef = React.useRef<HTMLButtonElement | null>(null);
  const close = () => {
    onClose?.();
    setOpen(false);
  };
  const trigger = usePopupTrigger(open, setOpen, 'menu', inside);
  const popup = usePopupSurface({
    open,
    anchor: triggerRef.current,
    onClose: close,
    initialFocus,
    trapFocus,
    inside: inside ? [triggerRef] : [],
  });
  return (
    <div>
      <button ref={triggerRef} type="button" aria-haspopup={trigger.haspopup} aria-expanded={trigger.expanded}
        onClick={trigger.onClick}>Trigger</button>
      <button type="button">Elsewhere</button>
      <span>Outside</span>
      {(open || mounted) && (
        <div ref={popup.setRefs} data-surface="" className="tl-popover" hidden={!open} style={open ? popup.style : undefined}>
          <button type="button">One</button>
          <button type="button">Two</button>
        </div>
      )}
    </div>
  );
}

/** A binding of Escape around the host: a window or the page, which must not see an Escape the surface takes. */
function Around({ onEscape, children }: { onEscape: () => void; children: React.ReactNode }) {
  return (
    <KeyboardScopeProvider modal>
      <Binding onEscape={onEscape} />
      {children}
    </KeyboardScopeProvider>
  );
}

function Binding({ onEscape }: { onEscape: () => void }) {
  useKeyboardBinding('ESCAPE', () => {
    onEscape();
    return true;
  });
  return null;
}

describe('usePopupSurface', () => {
  it('closes on Escape while the focus is still on the trigger', () => {
    const onClose = vi.fn();
    render(<Host trapFocus={false} onClose={onClose} />);
    keyboardClick(button('Trigger'));
    expect(surface()).not.toBeNull();
    // Not modal: the focus may go back to the trigger, and Escape there still reaches the surface.
    act(() => button('Trigger').focus());
    expect(document.activeElement).toBe(button('Trigger'));
    key(button('Trigger'), 'Escape');
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(surface()).toBeNull();
  });

  it('closes on Escape from outside the surface also with a focus trap', () => {
    const onClose = vi.fn();
    render(<Host onClose={onClose} />);
    keyboardClick(button('Trigger'));
    expect(surface()!.contains(document.activeElement)).toBe(true);
    // The key of a gesture that started before the focus moved: its target is the trigger.
    key(button('Trigger'), 'Escape');
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('closes on a press outside, not on a press inside', () => {
    const onClose = vi.fn();
    render(<Host onClose={onClose} />);
    keyboardClick(button('Trigger'));
    press(button('One'));
    expect(onClose).not.toHaveBeenCalled();
    press(document.querySelector('span')!);
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('takes Escape before a modal scope around it, and leaves it to that scope once closed', () => {
    const onClose = vi.fn();
    const onEscape = vi.fn();
    render(<Around onEscape={onEscape}><Host onClose={onClose} /></Around>);
    keyboardClick(button('Trigger'));
    key(document.activeElement!, 'Escape');
    expect(onClose).toHaveBeenCalledTimes(1);
    expect(onEscape).not.toHaveBeenCalled();
    key(document.activeElement ?? document.body, 'Escape');
    expect(onEscape).toHaveBeenCalledTimes(1);
  });

  it('holds neither a keyboard scope nor a focus trap while closed, though mounted', () => {
    const onClose = vi.fn();
    const onEscape = vi.fn();
    render(<Around onEscape={onEscape}><Host mounted onClose={onClose} /></Around>);
    expect(surface()!.hidden).toBe(true);
    act(() => button('Elsewhere').focus());
    expect(document.activeElement).toBe(button('Elsewhere'));
    key(button('Elsewhere'), 'Escape');
    expect(onClose).not.toHaveBeenCalled();
    expect(onEscape).toHaveBeenCalledTimes(1);
  });

  it('opens a mounted surface with the focus on its first entry and gives it back on closing', () => {
    render(<Host mounted />);
    act(() => button('Elsewhere').focus());
    keyboardClick(button('Trigger'));
    expect(surface()!.hidden).toBe(false);
    expect(document.activeElement).toBe(button('One'));
    key(button('One'), 'Escape');
    expect(surface()!.hidden).toBe(true);
    expect(document.activeElement).toBe(button('Elsewhere'));
  });

  it('confines the focus by default', () => {
    render(<Host />);
    keyboardClick(button('Trigger'));
    act(() => button('Elsewhere').focus());
    expect(document.activeElement).toBe(button('One'));
  });

  it('without trap, lets the focus leave and leaves it where the user put it on closing', () => {
    render(<Host trapFocus={false} />);
    act(() => button('Trigger').focus());
    keyboardClick(button('Trigger'));
    expect(document.activeElement).toBe(button('One'));
    act(() => button('Elsewhere').focus());
    expect(document.activeElement).toBe(button('Elsewhere'));
    press(button('Elsewhere'));
    expect(surface()).toBeNull();
    expect(document.activeElement).toBe(button('Elsewhere'));
  });

  it('gives the focus to its anchor when it falls to the body on closing', async () => {
    render(<Host />);
    // A click that does not focus the trigger (Safari, a trigger keeping the focus where it is).
    keyboardClick(button('Trigger'));
    expect(document.activeElement).toBe(button('One'));
    key(button('One'), 'Escape');
    expect(surface()).toBeNull();
    await act(async () => {
      await Promise.resolve();
    });
    expect(document.activeElement).toBe(button('Trigger'));
  });

  it('gives the focus to its anchor when it stays in a mounted surface that closes', async () => {
    render(<Host mounted />);
    // Nothing focused before: the trap has nowhere to give the focus back to.
    keyboardClick(button('Trigger'));
    expect(document.activeElement).toBe(button('One'));
    key(button('One'), 'Escape');
    expect(surface()!.hidden).toBe(true);
    await act(async () => {
      await Promise.resolve();
    });
    expect(document.activeElement).toBe(button('Trigger'));
  });

  it('moves the focus to the first entry one frame later when nothing is laid out yet at opening', () => {
    // A guard: a surface placed only in the following frame (a menu at the pointer) has no entry with a box when
    // it opens, and the trap falls back to the surface itself.
    const frames: FrameRequestCallback[] = [];
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => frames.push(callback));
    vi.stubGlobal('cancelAnimationFrame', () => {});
    const rects = vi.spyOn(Element.prototype, 'getClientRects').mockReturnValue([] as unknown as DOMRectList);
    render(<Host />);
    keyboardClick(button('Trigger'));
    expect(document.activeElement).toBe(surface());
    rects.mockReturnValue([new DOMRect()] as unknown as DOMRectList);
    act(() => frames.splice(0).forEach(frame => frame(0)));
    expect(document.activeElement).toBe(button('One'));
  });

  it('leaves the focus alone a frame later when the trap has found its target', () => {
    const frames: FrameRequestCallback[] = [];
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => frames.push(callback));
    vi.stubGlobal('cancelAnimationFrame', () => {});
    render(<Host />);
    keyboardClick(button('Trigger'));
    act(() => button('Two').focus());
    act(() => frames.splice(0).forEach(frame => frame(0)));
    expect(document.activeElement).toBe(button('Two'));
  });
});

describe('the height of a surface', () => {
  const VIEWPORT_WIDTH = 800;
  const VIEWPORT_HEIGHT = 600;
  /** The gap between anchor and surface: --tl-space-xs, not set in jsdom, falls back to 8. */
  const GAP = 8;
  const ANCHOR_TOP = 300;
  const ANCHOR_BOTTOM = 320;
  /** The height the surface's content asks for: more than the room on either side of the anchor. */
  const CONTENT_HEIGHT = 500;

  /** The number of pixels in the max height of the surface, its content height if it has none. */
  function heightOf(element: HTMLElement): number {
    const cap = /^min\((\d+)px/.exec(element.style.maxHeight);
    return cap ? Math.min(CONTENT_HEIGHT, Number(cap[1])) : CONTENT_HEIGHT;
  }

  beforeEach(() => {
    const html = document.documentElement;
    vi.spyOn(html, 'clientWidth', 'get').mockReturnValue(VIEWPORT_WIDTH);
    vi.spyOn(html, 'clientHeight', 'get').mockReturnValue(VIEWPORT_HEIGHT);
    vi.spyOn(Element.prototype, 'getBoundingClientRect').mockImplementation(function (this: Element) {
      if (this.textContent === 'Trigger' && this.tagName === 'BUTTON') {
        return new DOMRect(10, ANCHOR_TOP, 100, ANCHOR_BOTTOM - ANCHOR_TOP);
      }
      return new DOMRect(0, 0, 0, 0);
    });
    vi.spyOn(HTMLElement.prototype, 'offsetWidth', 'get').mockImplementation(function (this: HTMLElement) {
      return this.hasAttribute('data-surface') ? 200 : 0;
    });
    vi.spyOn(HTMLElement.prototype, 'offsetHeight', 'get').mockImplementation(function (this: HTMLElement) {
      return this.hasAttribute('data-surface') ? heightOf(this) : 0;
    });
  });

  async function positioned() {
    for (let i = 0; i < 5; i++) {
      await act(async () => {
        await Promise.resolve();
      });
    }
  }

  /** The top edge of the surface, from the translation floating-ui places it with. */
  function topOf(element: HTMLElement): number {
    const translate = /translate\(([-\d.]+)px, ([-\d.]+)px\)/.exec(element.style.transform);
    if (!translate) {
      throw new Error('Not placed: ' + element.style.cssText);
    }
    return Number(translate[2]);
  }

  it('is at most the room on the side it stands on, so it scrolls instead of covering its anchor', async () => {
    render(<Host />);
    keyboardClick(button('Trigger'));
    await positioned();
    const element = surface()!;
    // More room above (300 - gap - margin) than below (600 - 320 - gap - margin): the surface stands above.
    const roomAbove = ANCHOR_TOP - GAP - GAP;
    expect(element.style.maxHeight).toBe(`min(${roomAbove}px, var(--tl-size-popover-max))`);
    expect(topOf(element) + heightOf(element)).toBeLessThanOrEqual(ANCHOR_TOP - GAP);
  });
});

describe('popoverMiddleware', () => {
  it('fits the height between flip and shift, and only when asked', () => {
    // After shift, which moves the surface along the side axis too, size would take the whole viewport as room.
    expect(popoverMiddleware(8, true).map(middleware => middleware.name)).toEqual(['offset', 'flip', 'size', 'shift']);
    expect(popoverMiddleware(8).map(middleware => middleware.name)).toEqual(['offset', 'flip', 'shift']);
  });

  it('caps the height at the room, in whole pixels and never below zero, and at the size of the design system', () => {
    expect(fittedMaxHeight(284.7)).toBe('min(284px, var(--tl-size-popover-max))');
    expect(fittedMaxHeight(-12)).toBe('min(0px, var(--tl-size-popover-max))');
  });
});

describe('usePopupTrigger', () => {
  it('announces the kind of its surface and whether it is open', () => {
    render(<Host />);
    const trigger = button('Trigger');
    expect(trigger.getAttribute('aria-haspopup')).toBe('menu');
    expect(trigger.getAttribute('aria-expanded')).toBe('false');
    pointerClick(trigger);
    expect(trigger.getAttribute('aria-expanded')).toBe('true');
  });

  it('closes on a second click, whose press has closed the surface, and does not open it again', () => {
    render(<Host />);
    pointerClick(button('Trigger'));
    expect(surface()).not.toBeNull();
    pointerClick(button('Trigger'));
    expect(surface()).toBeNull();
    expect(button('Trigger').getAttribute('aria-expanded')).toBe('false');
  });

  it('opens after a press that closed nothing', () => {
    render(<Host />);
    pointerClick(button('Trigger'));
    pointerClick(button('Trigger'));
    pointerClick(button('Trigger'));
    expect(surface()).not.toBeNull();
  });

  it('toggles on its click when it is inside its surface: its press closes nothing', () => {
    const onClose = vi.fn();
    render(<Host inside mounted onClose={onClose} />);
    pointerClick(button('Trigger'));
    expect(surface()!.hidden).toBe(false);
    press(button('Trigger'));
    expect(onClose).not.toHaveBeenCalled();
    clickOf(button('Trigger'));
    expect(surface()!.hidden).toBe(true);
  });

  it('toggles on a click without press (the keyboard)', () => {
    render(<Host trapFocus={false} />);
    keyboardClick(button('Trigger'));
    expect(surface()).not.toBeNull();
    keyboardClick(button('Trigger'));
    expect(surface()).toBeNull();
  });
});

describe('useRovingMenu', () => {
  function RovingHost({ onArrow }: { onArrow?: (event: React.KeyboardEvent) => void }) {
    const ref = React.useRef<HTMLDivElement | null>(null);
    const { onKeyDown } = useRovingMenu(ref, true);
    return (
      <div ref={ref} role="menu" onKeyDown={onKeyDown}>
        <button type="button" role="menuitem" onKeyDown={onArrow}>One</button>
        <button type="button" role="menuitem">Two</button>
      </div>
    );
  }

  it('moves on with the arrows', () => {
    render(<RovingHost />);
    act(() => button('One').focus());
    const event = key(button('One'), 'ArrowDown');
    expect(document.activeElement).toBe(button('Two'));
    expect(event.defaultPrevented).toBe(true);
  });

  it('leaves Escape to the surface', () => {
    render(<RovingHost />);
    act(() => button('One').focus());
    const event = key(button('One'), 'Escape');
    expect(event.defaultPrevented).toBe(false);
  });

  it('leaves a key alone that an entry has handled', () => {
    render(<RovingHost onArrow={event => event.preventDefault()} />);
    act(() => button('One').focus());
    key(button('One'), 'ArrowDown');
    expect(document.activeElement).toBe(button('One'));
  });
});
