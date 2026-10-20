// The foundation of wave 5d (spec 2026-10-20 "Fundament", §3 and §4): what the bridge marks on the page for the design
// system package to draw - the filling control, the jump target flashing, the element picker, the drag shield, the touch
// drag - and that nothing of it carries an old class any more. The test registry has no parts of its own, as in
// planen.test.tsx; nothing here renders a part.
//
// Review focus 5: Escape in the element picker inside an open dialog ends only the picker, and the hint refusing a drop
// lies above the image of a touch drag - both sit on layer-drag, so the hint has to follow the image in the DOM.

import { describe, it, expect, vi, afterEach } from 'vitest';
import { act, cleanup, fireEvent, render, screen } from '@testing-library/react';
import { React, useFill, KeyboardScopeProvider, EscapeToClose, startPointerDrag } from 'tl-react-bridge';
import { scrollToAnchor } from '../../../com.top_logic.layout.react/react-src/bridge/scroll';
import { handlePick } from '../../../com.top_logic.layout.react/react-src/bridge/element-picker';
import { initKeyboardDispatcher } from '../../../com.top_logic.layout.react/react-src/bridge/keyboard-dispatcher';
import { installTouchDrag } from '../../../com.top_logic.layout.react/react-src/bridge/touch-drag';
import { DropHint } from '../../../com.top_logic.layout.react/react-src/controls/DropHint';
import type { HintAnchor } from '../../../com.top_logic.layout.react/react-src/controls/DropHint';

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
    child.dispatchEvent(new Event('animationend', { bubbles: true }));
    expect(stateWords(node)).toEqual(['selected', 'flash']);

    // The flash of the target itself ends: the state goes, the other words stay.
    node.dispatchEvent(new Event('animationend', { bubbles: true }));
    expect(stateWords(node)).toEqual(['selected']);
  });

  it('anchor flash starts again on a repeated jump and is cleared once', () => {
    vi.stubGlobal('requestAnimationFrame', (callback: FrameRequestCallback) => { callback(0); return 0; });
    const node = anchor('a2');

    scrollToAnchor('a2');
    scrollToAnchor('a2');
    expect(stateWords(node)).toEqual(['flash']);

    node.dispatchEvent(new Event('animationend'));
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
