// The foundation of wave 5d (spec 2026-10-20 "Fundament", §3 to §5): what the bridge marks on the page for the design
// system package to draw - the filling control, the jump target flashing, the element picker, the drag shield, the touch
// drag, the tooltip - and that nothing of it carries an old class any more. The test registry has no parts of its own,
// as in planen.test.tsx; nothing here renders a part.
//
// Review focus 1: the tooltip stands at the element the pointer moved on to, not at the one it came from.
// Review focus 5: Escape in the element picker inside an open dialog ends only the picker, and the hint refusing a drop
// lies above the image of a touch drag - both sit on layer-drag, so the hint has to follow the image in the DOM.
// Review focus 4: the density chosen in the user menu reaches the page without a reload - the page script's
// `tlTheme.density(value)` sets `data-tl-density` on <html>, and what the controls read through useTokenPx follows.

import { describe, it, expect, vi, afterEach, beforeEach, beforeAll } from 'vitest';
import { act, cleanup, fireEvent, render, renderHook, screen, waitFor } from '@testing-library/react';
import { React, useFill, useTokenPx, KeyboardScopeProvider, EscapeToClose, startPointerDrag, TOOLTIP_ATTR, Menu, MenuItem, ButtonDefaults, useButtonDefaults, usePart, registerPart } from 'tl-react-bridge';
import { scrollToAnchor } from '../../../com.top_logic.layout.react/react-src/bridge/scroll';
import { handlePick } from '../../../com.top_logic.layout.react/react-src/bridge/element-picker';
import { initKeyboardDispatcher } from '../../../com.top_logic.layout.react/react-src/bridge/keyboard-dispatcher';
import { installTouchDrag } from '../../../com.top_logic.layout.react/react-src/bridge/touch-drag';
import ButtonPart from '../../../com.top_logic.layout.react/react-src/controls/button/ButtonPart';
import { DropHint } from '../../../com.top_logic.layout.react/react-src/controls/DropHint';
import type { HintAnchor } from '../../../com.top_logic.layout.react/react-src/controls/DropHint';
import { HOVER_DELAY_MS } from '../../../com.top_logic.layout.react/react-src/bridge/tooltip-host';
import type { TooltipResolveDetail } from '../../../com.top_logic.layout.react/react-src/bridge/tooltip-host';
import type { TooltipData } from '../../../com.top_logic.layout.react/react-src/bridge/TooltipPopover';

/**
 * The element the bridge shows its tooltips in, created when the bridge loads. The cleanup after each test empties the
 * page, the tooltip tests put it back.
 */
const tooltipHost = document.getElementById('tl-tooltip-host');

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

afterEach(() => {
  // Ends a pick mode a failing test left running.
  fireEvent.keyDown(document.body, { key: 'Escape' });
  cleanup();
  document.body.replaceChildren();
  document.body.removeAttribute('data-tl-state');
  document.documentElement.removeAttribute('data-tl-density');
  document.documentElement.style.removeProperty('--tl-size-row');
  vi.useRealTimers();
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

/** The words of the element's `data-tl-state`. */
function stateWords(element: Element): string[] {
  return (element.getAttribute('data-tl-state') ?? '').split(/\s+/).filter(Boolean);
}

describe('fill', () => {
  it('fill marks with tl-fill', () => {
    function Filling() {
      return <div data-testid="filling" className={useFill(true)} />;
    }
    render(<Filling />);

    expect(screen.getByTestId('filling').className).toBe('tl-fill');
  });
});

describe('anchor', () => {
  /** A jump target carrying the given state words, as an `<anchor>` around a control renders it. */
  function anchor(key: string, state?: string): HTMLElement {
    const node = document.createElement('div');
    node.setAttribute('data-tl-anchor', key);
    if (state) {
      node.setAttribute('data-tl-state', state);
    }
    node.scrollIntoView = vi.fn();
    document.body.append(node);
    return node;
  }

  /** The end of an animation with the given name (jsdom has no AnimationEvent). */
  function animationEnd(animationName: string): Event {
    const event = new Event('animationend', { bubbles: true });
    Object.defineProperty(event, 'animationName', { value: animationName });
    return event;
  }

  it('anchor flash sets and clears data-tl-state flash', () => {
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => { callback(0); return 0; });
    const node = anchor('a1', 'selected');
    const child = document.createElement('span');
    node.append(child);

    scrollToAnchor('a1');

    expect(node.scrollIntoView).toHaveBeenCalled();
    expect(stateWords(node)).toEqual(['selected', 'flash']);
    expect(node.className).toBe('');

    // An animation of something inside the target ends: the flash goes on.
    child.dispatchEvent(animationEnd('tl-anchor-flash'));
    expect(stateWords(node)).toEqual(['selected', 'flash']);

    // The flash of the target itself ends: the state goes, the other words stay.
    node.dispatchEvent(animationEnd('tl-anchor-flash'));
    expect(stateWords(node)).toEqual(['selected']);
  });

  it('anchor flash is not ended by another animation of the target', () => {
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => { callback(0); return 0; });
    const node = anchor('a3');

    scrollToAnchor('a3');
    node.dispatchEvent(animationEnd('tl-tooltip-in'));
    expect(stateWords(node)).toEqual(['flash']);

    node.dispatchEvent(animationEnd('tl-anchor-flash'));
    expect(node.hasAttribute('data-tl-state')).toBe(false);
  });

  it('anchor flash starts again on a repeated jump and is cleared once', () => {
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => { callback(0); return 0; });
    const node = anchor('a2');

    scrollToAnchor('a2');
    scrollToAnchor('a2');
    expect(stateWords(node)).toEqual(['flash']);

    node.dispatchEvent(animationEnd('tl-anchor-flash'));
    expect(node.hasAttribute('data-tl-state')).toBe(false);
  });
});

describe('element picker', () => {
  /** Enters pick mode in this window, choosing views. */
  function pick() {
    handlePick({ token: 'token-1', targetWindowId: '', kind: 'view' });
  }

  it('picker: highlight has class tl-pick-highlight, body carries data-tl-state picking while active', () => {
    const view = document.createElement('div');
    view.dataset.viewSource = 'app/main.view.xml';
    document.body.append(view);

    pick();
    expect(stateWords(document.body)).toContain('picking');
    expect(document.body.classList).not.toContain('tlPick--active');

    view.dispatchEvent(new MouseEvent('pointermove', { bubbles: true }));
    const highlight = document.querySelector<HTMLElement>('.tl-pick-highlight');
    expect(highlight).not.toBeNull();
    expect(highlight!.id).toBe('');
    expect(highlight!.style.display).toBe('block');

    fireEvent.keyDown(document.body, { key: 'Escape' });
    expect(stateWords(document.body)).not.toContain('picking');
    expect(document.querySelector('.tl-pick-highlight')).toBeNull();
  });

  it('picker: Escape inside an open dialog stops only the picker', () => {
    initKeyboardDispatcher();
    const onClose = vi.fn();
    render(
      <KeyboardScopeProvider modal>
        <EscapeToClose onClose={onClose} />
        <div role="dialog">
          <button type="button">OK</button>
        </div>
      </KeyboardScopeProvider>,
    );
    const button = screen.getByRole('button');
    button.focus();

    // The Escape of the picker goes through the keyboard dispatcher, not through a listener of its own.
    const listen = vi.spyOn(document, 'addEventListener');
    pick();
    expect(listen.mock.calls.map(call => call[0])).not.toContain('keydown');
    expect(stateWords(document.body)).toContain('picking');

    fireEvent.keyDown(button, { key: 'Escape' });
    expect(stateWords(document.body)).not.toContain('picking');
    expect(onClose).not.toHaveBeenCalled();
    expect(screen.getByRole('dialog')).not.toBeNull();

    // The picker is gone, its binding with it: the next Escape is the dialog's again.
    fireEvent.keyDown(button, { key: 'Escape' });
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});

describe('pointer drag', () => {
  /** A pointer event of the pointer 1 at the given point (jsdom has no PointerEvent). */
  function pointer(type: string, x: number, y: number): MouseEvent {
    const event = new MouseEvent(type, { bubbles: true, clientX: x, clientY: y });
    Object.defineProperty(event, 'pointerId', { value: 1 });
    return event;
  }

  it('pointer drag shield has class tl-drag-shield', () => {
    const handle = document.createElement('div');
    handle.setPointerCapture = vi.fn();
    handle.hasPointerCapture = () => true;
    handle.releasePointerCapture = vi.fn();
    document.body.append(handle);

    startPointerDrag(pointer('pointerdown', 0, 0) as PointerEvent, { captureOn: handle, onMove: () => {} });
    handle.dispatchEvent(pointer('pointermove', 10, 0));

    const shield = document.querySelector('.tl-drag-shield');
    expect(shield).not.toBeNull();
    expect(document.querySelector('.tlDragShield')).toBeNull();

    handle.dispatchEvent(pointer('pointerup', 10, 0));
    expect(document.querySelector('.tl-drag-shield')).toBeNull();
  });
});

describe('touch drag', () => {
  /** The DataTransfer of a drag played by a finger (jsdom has none). */
  class TestDataTransfer {
    private readonly data = new Map<string, string>();
    dropEffect = 'none';
    effectAllowed = 'all';
    get types() { return Array.from(this.data.keys()); }
    setData(type: string, value: string) { this.data.set(type, value); }
    getData(type: string) { return this.data.get(type) ?? ''; }
    clearData() { this.data.clear(); }
  }

  /** A drag event carrying a DataTransfer (jsdom has no DragEvent). */
  class TestDragEvent extends MouseEvent {
    readonly dataTransfer: DataTransfer | null;
    constructor(type: string, init: DragEventInit = {}) {
      super(type, init);
      this.dataTransfer = init.dataTransfer ?? null;
    }
  }

  /** What lies under the finger. */
  let underFinger: Element | null = null;

  /** Plays touches on the document, with the drag events the touch drag dispatches. */
  function installFinger() {
    vi.useFakeTimers();
    vi.stubGlobal('DataTransfer', TestDataTransfer);
    vi.stubGlobal('DragEvent', TestDragEvent);
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    Object.defineProperty(document, 'elementFromPoint', { configurable: true, value: () => underFinger });
    installTouchDrag();
  }

  afterEach(() => {
    underFinger = null;
    delete (document as { elementFromPoint?: unknown }).elementFromPoint;
  });

  /** Dispatches a touch of finger 1 at the given point to the given element. */
  function touch(type: string, target: Element, x: number, y: number) {
    const finger = { identifier: 1, clientX: x, clientY: y, target };
    const event = new Event(type, { bubbles: true, cancelable: true });
    Object.defineProperty(event, 'touches', { value: type === 'touchend' ? [] : [finger] });
    Object.defineProperty(event, 'changedTouches', { value: [finger] });
    act(() => {
      target.dispatchEvent(event);
    });
  }

  /** A draggable element the control marks as selected, appended to the given parent. */
  function source(parent: Element = document.body): HTMLElement {
    const element = document.createElement('div');
    element.id = 'card-1';
    element.draggable = true;
    element.setAttribute('data-tl-state', 'selected');
    element.textContent = 'Card';
    parent.append(element);
    return element;
  }

  it('touch drag: armed gets data-tl-armed, data-tl-state stays untouched, image has class tl-drag-image', () => {
    installFinger();
    const card = source();
    underFinger = card;

    touch('touchstart', card, 10, 10);
    act(() => { vi.advanceTimersByTime(450); });
    expect(card.hasAttribute('data-tl-armed')).toBe(true);
    expect(card.getAttribute('data-tl-state')).toBe('selected');
    expect(card.hasAttribute('data-touch-drag')).toBe(false);

    touch('touchmove', card, 40, 10);
    expect(card.hasAttribute('data-tl-armed')).toBe(false);
    expect(card.getAttribute('data-tl-state')).toBe('selected');
    expect(card.hasAttribute('data-touch-drag')).toBe(false);

    const image = document.querySelector<HTMLElement>('.tl-drag-image');
    expect(image).not.toBeNull();
    expect(image!.hasAttribute('data-tl-armed')).toBe(false);
    expect(image!.hasAttribute('data-touch-drag')).toBe(false);
    expect(image!.id).toBe('');
    expect(document.querySelector('.tlTouchDragImage')).toBeNull();

    touch('touchend', card, 40, 10);
    expect(document.querySelector('.tl-drag-image')).toBeNull();
  });

  it('touch drag: an element picked up and released unmoved loses data-tl-armed', () => {
    installFinger();
    const card = source();
    underFinger = card;

    touch('touchstart', card, 10, 10);
    act(() => { vi.advanceTimersByTime(450); });
    expect(card.hasAttribute('data-tl-armed')).toBe(true);

    touch('touchend', card, 10, 10);
    expect(card.hasAttribute('data-tl-armed')).toBe(false);
    expect(card.getAttribute('data-tl-state')).toBe('selected');
  });

  it('drop hint lies after the touch drag image in the DOM', () => {
    installFinger();

    /** A drop target refusing every drop, with the hint saying why, as table, tree and board show it. */
    function RefusingTarget() {
      const [refused, setRefused] = React.useState(false);
      const moveRef = React.useRef<((at: HintAnchor) => void) | null>(null);
      return (
        <div data-testid="target" onDragOver={() => setRefused(true)}>
          {refused && <DropHint reason="Not here" start={{ x: 40, y: 10, height: 20 }} moveRef={moveRef} />}
        </div>
      );
    }
    render(<RefusingTarget />);
    const card = source();
    underFinger = card;

    touch('touchstart', card, 10, 10);
    act(() => { vi.advanceTimersByTime(450); });
    underFinger = screen.getByTestId('target');
    touch('touchmove', card, 40, 10);

    const image = document.querySelector('.tl-drag-image');
    const hint = document.querySelector('.tl-popover.tl-drop-hint');
    expect(image).not.toBeNull();
    expect(hint).not.toBeNull();
    expect(image!.compareDocumentPosition(hint!) & Node.DOCUMENT_POSITION_FOLLOWING).toBeTruthy();

    touch('touchend', card, 40, 10);
  });
});

describe('tooltip', () => {
  /** A box at the given place, as `getBoundingClientRect` reports it. */
  function box(left: number, top: number, width: number, height: number): DOMRect {
    return {
      left, top, width, height, x: left, y: top, right: left + width, bottom: top + height, toJSON() {},
    } as DOMRect;
  }

  /** A column heading declaring the given tooltip, laid out 80 pixels wide from the given left edge. */
  function heading(text: string, left: number): HTMLElement {
    const element = document.createElement('span');
    element.setAttribute(TOOLTIP_ATTR, 'text:' + text);
    element.textContent = text;
    element.getBoundingClientRect = () => box(left, 100, 80, 20);
    document.body.append(element);
    return element;
  }

  /** The pointer moves from the given element (none: from outside the page) onto the given one. */
  function movePointer(from: Element | null, to: Element) {
    from?.dispatchEvent(new MouseEvent('pointerout', { bubbles: true, relatedTarget: to }));
    to.dispatchEvent(new MouseEvent('pointerover', { bubbles: true, relatedTarget: from }));
  }

  /** The pointer rests until the tooltip waiting for it opens and floating-ui has placed it. */
  async function rest() {
    await act(async () => {
      await vi.advanceTimersByTimeAsync(HOVER_DELAY_MS);
    });
    await act(async () => {
      await vi.advanceTimersByTimeAsync(0);
    });
  }

  /**
   * The horizontal place floating-ui gave the shown tooltip: the left edge of its box. The tooltip has no size in jsdom,
   * so a tooltip centred above its anchor stands at the anchor's centre.
   */
  function tooltipLeft(): number {
    const transform = screen.getByRole('tooltip').style.transform;
    const match = /translate\((-?[\d.]+)px,/.exec(transform);
    expect(match, 'transform ' + transform).not.toBeNull();
    return Number(match![1]);
  }

  beforeEach(() => {
    vi.useFakeTimers();
    vi.stubGlobal('ResizeObserver', NoResizeObserver);
    // jsdom lays nothing out, its viewport has no size: floating-ui would push every tooltip to the same edge.
    Object.defineProperty(document.documentElement, 'clientWidth', { configurable: true, value: 1000 });
    Object.defineProperty(document.documentElement, 'clientHeight', { configurable: true, value: 800 });
    document.body.append(tooltipHost!);
  });

  afterEach(async () => {
    // A press closes the tooltip that is open.
    await act(async () => {
      document.body.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true }));
    });
    delete (document.documentElement as { clientWidth?: number }).clientWidth;
    delete (document.documentElement as { clientHeight?: number }).clientHeight;
  });

  /** An element answering for its tooltip on demand with the given one, as a container answers for its parts. */
  function withTooltip(data: TooltipData): HTMLElement {
    const element = document.createElement('span');
    element.setAttribute(TOOLTIP_ATTR, 'dynamic');
    element.textContent = 'Price';
    element.getBoundingClientRect = () => box(0, 100, 80, 20);
    element.addEventListener('tl-tooltip-resolve', event => {
      (event as CustomEvent<TooltipResolveDetail>).detail.resolved = { inline: data };
    });
    document.body.append(element);
    return element;
  }

  it('tooltip is the block tl-tooltip with caption, body and arrow', async () => {
    movePointer(null, withTooltip({ caption: 'Price', text: 'Gross, in euros' }));
    await rest();

    const tooltip = screen.getByRole('tooltip');
    expect(tooltip.classList).toContain('tl-tooltip');
    expect(tooltip.querySelector(':scope > .tl-tooltip__caption.tl-type-label-strong')?.textContent).toBe('Price');
    expect(tooltip.querySelector(':scope > .tl-tooltip__body.tl-type-label')?.textContent).toBe('Gross, in euros');
    expect(tooltip.querySelector(':scope > .tl-tooltip__arrow')).not.toBeNull();
    // Passive: the pointer goes through it, by the rule of the block, not by a style of its own.
    expect(stateWords(tooltip)).toEqual([]);
    expect(tooltip.style.pointerEvents).toBe('');
    expect(tooltipHost!.querySelector('[class*="tl-tooltip-"]')).toBeNull();
  });

  it('tooltip without caption has no caption row', async () => {
    movePointer(null, withTooltip({ text: 'Gross, in euros' }));
    await rest();

    const tooltip = screen.getByRole('tooltip');
    expect(tooltip.querySelector('.tl-tooltip__caption')).toBeNull();
    expect(tooltip.querySelector('.tl-tooltip__body')?.textContent).toBe('Gross, in euros');
  });

  it('interactive tooltip carries data-tl-state interactive, not a modifier class', async () => {
    movePointer(null, withTooltip({ html: '<a href="#help">Help</a>', interactive: true }));
    await rest();

    const tooltip = screen.getByRole('tooltip');
    expect(stateWords(tooltip)).toEqual(['interactive']);
    expect(tooltip.className).toBe('tl-tooltip');
    expect(tooltip.querySelector(':scope > .tl-tooltip__body.tl-type-label > a')?.textContent).toBe('Help');
  });

  it('tooltip follows the new anchor when the pointer moves to the next element while open', async () => {
    const first = heading('Name', 0);
    const second = heading('Price', 200);

    movePointer(null, first);
    await rest();
    expect(screen.getByRole('tooltip').textContent).toBe('Name');
    expect(tooltipLeft()).toBe(40);

    // From heading to heading, the tooltip still open.
    movePointer(first, second);
    await rest();
    expect(screen.getByRole('tooltip').textContent).toBe('Price');
    expect(tooltipLeft()).toBe(240);
  });
});

describe('density', () => {
  /** The row height of each density, as tokens.css gives it; jsdom does not cascade the package's rules. */
  const ROW = { normal: '36px', compact: '28px' };

  /** What the page script's `density` function does: sets the attribute at once (the tokens follow by cascade). */
  function setDensity(density: keyof typeof ROW) {
    document.documentElement.style.setProperty('--tl-size-row', ROW[density]);
    document.documentElement.setAttribute('data-tl-density', density);
  }

  it('a length read via useTokenPx changes with data-tl-density, without a reload', async () => {
    setDensity('normal');
    const ref = { current: document.documentElement };
    const { result } = renderHook(() => useTokenPx(ref, '--tl-size-row', 40));
    expect(result.current).toBe(36);

    act(() => setDensity('compact'));
    await waitFor(() => expect(result.current).toBe(28));
    expect(document.documentElement.getAttribute('data-tl-density')).toBe('compact');

    act(() => setDensity('normal'));
    await waitFor(() => expect(result.current).toBe(36));
  });

  it('a hook mounted after the switch reads the chosen density', () => {
    setDensity('compact');
    const ref = { current: document.documentElement };
    const { result } = renderHook(() => useTokenPx(ref, '--tl-size-row', 40));

    expect(result.current).toBe(28);
  });
});

describe('menu', () => {
  beforeAll(() => registerPart('Button', ButtonPart));

  /** An anchor for the menu; jsdom lays nothing out, the popover only needs an element. */
  function anchorElement(): HTMLElement {
    const anchor = document.createElement('button');
    document.body.appendChild(anchor);
    return anchor;
  }

  it('the menu of the bridge moves focus with the arrows and closes on Escape', () => {
    // jsdom has no layout; the menu roves over the entries that have a box.
    vi.spyOn(HTMLElement.prototype, 'getClientRects').mockReturnValue([{}] as unknown as DOMRectList);
    const onClose = vi.fn();
    render(
      <Menu open anchor={anchorElement()} onClose={onClose}>
        <MenuItem label="Cut" onSelect={() => {}} />
        <MenuItem label="Copy" onSelect={() => {}} />
        <MenuItem label="Paste" onSelect={() => {}} />
      </Menu>,
    );
    const menu = screen.getByRole('menu');
    const entries = screen.getAllByRole('menuitem');
    expect(entries).toHaveLength(3);
    expect(entries.map(e => e.getAttribute('tabindex'))).toEqual(['0', '-1', '-1']);

    entries[0].focus();
    fireEvent.keyDown(entries[0], { key: 'ArrowDown' });
    expect(document.activeElement).toBe(entries[1]);
    fireEvent.keyDown(entries[1], { key: 'End' });
    expect(document.activeElement).toBe(entries[2]);
    fireEvent.keyDown(entries[2], { key: 'ArrowDown' });
    expect(document.activeElement).toBe(entries[0]);
    expect(menu.contains(document.activeElement)).toBe(true);

    fireEvent.keyDown(entries[0], { key: 'Escape' });
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  /** A toggle entry as `TLToggleButton` draws it: the part `Button` with a pressed state. */
  const Entry: React.FC<{ label: string; checked: boolean }> = ({ label, checked }) => {
    const Button = usePart('Button');
    const defaults = useButtonDefaults();
    return <Button label={label} display="label-only" appearance="menu-item" checked={checked} radio={defaults.radio} onClick={() => {}} />;
  };

  it('under ButtonDefaults radio, the entries are menuitemradio and exactly one is checked', () => {
    vi.spyOn(HTMLElement.prototype, 'getClientRects').mockReturnValue([{}] as unknown as DOMRectList);
    render(
      <Menu open anchor={anchorElement()} onClose={() => {}}>
        <ButtonDefaults appearance="menu-item" radio>
          <Entry label="Paragraph" checked={false} />
          <Entry label="Heading 1" checked />
          <Entry label="Heading 2" checked={false} />
        </ButtonDefaults>
      </Menu>,
    );
    const radios = screen.getAllByRole('menuitemradio');
    expect(radios).toHaveLength(3);
    expect(radios.filter(r => r.getAttribute('aria-checked') === 'true').map(r => r.textContent)).toEqual(['Heading 1']);
    expect(radios.filter(r => r.getAttribute('aria-checked') === 'false')).toHaveLength(2);
    // The checked entry is marked visibly: it reads strong; the others do not.
    expect(radios.map(r => r.classList.contains('tl-type-body-strong'))).toEqual([false, true, false]);
    // The roving finds the radio entries.
    expect(radios.map(r => r.getAttribute('tabindex'))).toEqual(['0', '-1', '-1']);
    fireEvent.keyDown(radios[0], { key: 'ArrowDown' });
    expect(document.activeElement).toBe(radios[1]);
  });

  it('without radio the toggle entries stay checkbox entries', () => {
    render(
      <ButtonDefaults appearance="menu-item">
        <Entry label="Bold" checked />
      </ButtonDefaults>,
    );
    expect(screen.getByRole('menuitemcheckbox').getAttribute('aria-checked')).toBe('true');
    expect(screen.queryByRole('menuitemradio')).toBeNull();
  });
});
