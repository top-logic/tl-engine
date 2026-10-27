import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterAll, afterEach, beforeAll, describe, expect, it, vi } from 'vitest';
import { EDGE_END, EDGE_START, PAGE_FRACTION, pageScrollLeft, revealScrollLeft, stripOverflow } from './scroll-strip';
import { TabStrip } from './TabStrip';

// The labels as given: no server to translate them.
vi.mock('./i18n', () => ({ useI18N: (keys: Record<string, string>) => keys }));

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

describe('stripOverflow', () => {
  it('reports no hidden content when the content fits', () => {
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 300 }))
      .toEqual({ overflows: false, canScrollStart: false, canScrollEnd: false });
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 120 }))
      .toEqual({ overflows: false, canScrollStart: false, canScrollEnd: false });
  });

  it('reports hidden content in each direction separately', () => {
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ overflows: true, canScrollStart: false, canScrollEnd: true });
    expect(stripOverflow({ scrollLeft: 200, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ overflows: true, canScrollStart: true, canScrollEnd: true });
    expect(stripOverflow({ scrollLeft: 500, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ overflows: true, canScrollStart: true, canScrollEnd: false });
  });

  it('ignores sub-pixel remainders at the ends', () => {
    expect(stripOverflow({ scrollLeft: 0.3, clientWidth: 300, scrollWidth: 800.4 }))
      .toEqual({ overflows: true, canScrollStart: false, canScrollEnd: true });
    expect(stripOverflow({ scrollLeft: 499.8, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ overflows: true, canScrollStart: true, canScrollEnd: false });
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 300.4 }).overflows).toBe(false);
  });

  it('counts the space of shown buttons as available: content fitting without them does not overflow', () => {
    // Buttons shown (48px spare): 280px content fits the 300px the strip has without them.
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 252, scrollWidth: 280, spare: 48 }).overflows).toBe(false);
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 252, scrollWidth: 301, spare: 48 }).overflows).toBe(true);
  });
});

describe('revealScrollLeft', () => {
  const g = { scrollLeft: 200, clientWidth: 300, scrollWidth: 1000 };

  it('keeps the offset for an element already in view', () => {
    expect(revealScrollLeft(g, { start: 200, end: 300 })).toBe(200);
    expect(revealScrollLeft(g, { start: 400, end: 500 })).toBe(200);
  });

  it('moves a hidden element to the edge it was hidden behind', () => {
    expect(revealScrollLeft(g, { start: 50, end: 150 })).toBe(50);
    expect(revealScrollLeft(g, { start: 150, end: 250 })).toBe(150);
    expect(revealScrollLeft(g, { start: 600, end: 700 })).toBe(400);
    expect(revealScrollLeft(g, { start: 450, end: 550 })).toBe(250);
  });

  it('shows an element wider than the strip from its start', () => {
    expect(revealScrollLeft(g, { start: 300, end: 700 })).toBe(300);
  });

  it('stays inside the scroll range', () => {
    expect(revealScrollLeft(g, { start: 900, end: 1000 })).toBe(700);
    expect(revealScrollLeft({ scrollLeft: 0, clientWidth: 300, scrollWidth: 300 }, { start: 0, end: 100 })).toBe(0);
  });
});

describe('pageScrollLeft', () => {
  const g = { scrollLeft: 500, clientWidth: 400, scrollWidth: 1500 };

  it('moves by a share of the visible width', () => {
    expect(pageScrollLeft(g, EDGE_END)).toBe(500 + 400 * PAGE_FRACTION);
    expect(pageScrollLeft(g, EDGE_START)).toBe(500 - 400 * PAGE_FRACTION);
  });

  it('stops at the ends', () => {
    expect(pageScrollLeft({ ...g, scrollLeft: 50 }, EDGE_START)).toBe(0);
    expect(pageScrollLeft({ ...g, scrollLeft: 1000 }, EDGE_END)).toBe(1100);
  });
});

/**
 * A layout for jsdom, which has none: the strip is {@link stripWidth} wide; a scroll button not
 * hidden takes {@link BUTTON_WIDTH} of it, disabled or not; the tab list gets the rest. Each tab is
 * {@link TAB_WIDTH} wide, side by side from the start of the list.
 */
const STRIP_WIDTH = 250;
const BUTTON_WIDTH = 24;
const TAB_WIDTH = 100;

/** The current width of the strip; a test changes it to imitate a resize of the container. */
let stripWidth = STRIP_WIDTH;

/**
 * A resize observer for jsdom, which has none: delivers a resize of an element only when a test
 * calls {@link resized}, to every observer watching the element.
 */
class FakeResizeObserver {
  static readonly all = new Set<FakeResizeObserver>();

  private readonly _observed = new Set<Element>();

  constructor(private readonly _callback: ResizeObserverCallback) {
    FakeResizeObserver.all.add(this);
  }

  observe(element: Element): void { this._observed.add(element); }

  unobserve(element: Element): void { this._observed.delete(element); }

  disconnect(): void { this._observed.clear(); }

  deliver(element: Element): void {
    if (this._observed.has(element)) {
      this._callback([{ target: element } as ResizeObserverEntry], this as unknown as ResizeObserver);
    }
  }
}

/** Reports a size change of the given element to the resize observers watching it. */
function resized(element: Element): void {
  act(() => {
    for (const observer of FakeResizeObserver.all) {
      observer.deliver(element);
    }
  });
}

const scrollOffsets = new WeakMap<Element, number>();
const isList = (e: Element) => e.getAttribute('role') === 'tablist';
const isScrollButton = (e: Element) => e.hasAttribute('data-tl-edge');
const tabsOf = (list: Element) => list.querySelectorAll('[role="tab"]').length;

/** The width a scroll button takes: none while hidden. */
function buttonWidth(button: Element | null): number {
  return button && !(button as HTMLElement).hidden ? BUTTON_WIDTH : 0;
}

/** The width of the given tab list: the strip minus its shown buttons. */
function listWidthOf(list: Element): number {
  let width = stripWidth;
  for (const sibling of Array.from(list.parentElement!.children)) {
    if (sibling !== list) {
      width -= buttonWidth(sibling);
    }
  }
  return width;
}

const saved: [string, PropertyDescriptor | undefined][] = [];
function stub(name: string, descriptor: PropertyDescriptor): void {
  saved.push([name, Object.getOwnPropertyDescriptor(Element.prototype, name)]);
  Object.defineProperty(Element.prototype, name, { configurable: true, ...descriptor });
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  globalThis.ResizeObserver = FakeResizeObserver as unknown as typeof ResizeObserver;
  stub('clientWidth', { get(this: Element) { return isList(this) ? listWidthOf(this) : 0; } });
  stub('scrollWidth', {
    get(this: Element) { return isList(this) ? Math.max(tabsOf(this) * TAB_WIDTH, listWidthOf(this)) : 0; },
  });
  stub('scrollLeft', {
    get(this: Element) { return scrollOffsets.get(this) ?? 0; },
    set(this: Element, value: number) {
      scrollOffsets.set(this, value);
      this.dispatchEvent(new Event('scroll'));
    },
  });
  stub('scrollTo', {
    value(this: Element, options: ScrollToOptions) { this.scrollLeft = options.left ?? 0; },
  });
  stub('getBoundingClientRect', {
    value(this: Element) {
      let left = 0;
      let width = 0;
      if (isList(this)) {
        left = buttonWidth(this.previousElementSibling);
        width = listWidthOf(this);
      } else if (isScrollButton(this)) {
        width = buttonWidth(this);
      } else if (this.getAttribute('role') === 'tab') {
        const list = this.parentElement!;
        left = buttonWidth(list.previousElementSibling) + Array.from(list.children).indexOf(this) * TAB_WIDTH - list.scrollLeft;
        width = TAB_WIDTH;
      }
      return { left, right: left + width, top: 0, bottom: 0, width, height: 0, x: left, y: 0, toJSON: () => ({}) };
    },
  });
});

afterAll(() => {
  delete (globalThis as { ResizeObserver?: unknown }).ResizeObserver;
  for (const [name, descriptor] of saved.reverse()) {
    if (descriptor) {
      Object.defineProperty(Element.prototype, name, descriptor);
    } else {
      delete (Element.prototype as unknown as Record<string, unknown>)[name];
    }
  }
});

describe('TabStrip', () => {
  let container: HTMLDivElement;
  let root: Root;

  function mount(): void {
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  }

  function render(count: number, selected: string): void {
    act(() => {
      root.render(
        <TabStrip selected={selected} onKeyDown={() => {}}>
          {Array.from({ length: count }, (_, i) => (
            <button key={i} type="button" role="tab" aria-selected={'t' + i === selected}>{'Tab ' + i}</button>
          ))}
        </TabStrip>,
      );
    });
  }

  const list = () => container.querySelector<HTMLElement>('[role="tablist"]')!;
  const scrollButton = (edge: string) => container.querySelector<HTMLButtonElement>(`[data-tl-edge="${edge}"]`)!;
  /** The buttons that are there (not hidden). */
  const present = () => [EDGE_START, EDGE_END].filter(edge => !scrollButton(edge).hidden);
  /** The buttons that are there and can be pressed (drawn visible). */
  const enabled = () => present().filter(edge => !scrollButton(edge).disabled);

  /** Whether the selected tab lies completely inside the visible part of the list. */
  function selectedFullyVisible(): boolean {
    const tab = list().querySelector('[aria-selected="true"]')!.getBoundingClientRect();
    const box = list().getBoundingClientRect();
    return tab.left >= box.left && tab.right <= box.right;
  }

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
    FakeResizeObserver.all.clear();
    stripWidth = STRIP_WIDTH;
  });

  it('shows no scroll button while the tabs fit', () => {
    mount();
    render(2, 't1');
    expect(present()).toEqual([]);
    expect(list().clientWidth).toBe(STRIP_WIDTH);
    expect(list().scrollLeft).toBe(0);
  });

  it('keeps both buttons while overflowing, the one at an edge disabled', () => {
    mount();
    render(6, 't0');
    expect(present()).toEqual([EDGE_START, EDGE_END]);
    expect(enabled()).toEqual([EDGE_END]);
    expect(list().clientWidth).toBe(STRIP_WIDTH - 2 * BUTTON_WIDTH);
  });

  it('reveals the last tab completely when the overflow starts with it selected', () => {
    mount();
    render(6, 't5');
    expect(present()).toEqual([EDGE_START, EDGE_END]);
    expect(selectedFullyVisible()).toBe(true);
    expect(list().scrollLeft).toBe(600 - (STRIP_WIDTH - 2 * BUTTON_WIDTH));
    expect(enabled()).toEqual([EDGE_START]);
  });

  it('keeps the selected tab in view by scrolling the list only', () => {
    mount();
    render(6, 't0');
    const visible = STRIP_WIDTH - 2 * BUTTON_WIDTH;
    expect(list().scrollLeft).toBe(0);

    // Tab 4 (400..500) is hidden behind the end: it moves to the end edge.
    render(6, 't4');
    expect(list().scrollLeft).toBe(500 - visible);
    expect(selectedFullyVisible()).toBe(true);
    expect(enabled()).toEqual([EDGE_START, EDGE_END]);

    // Tab 3 (300..400) is in view: nothing moves.
    render(6, 't3');
    expect(list().scrollLeft).toBe(500 - visible);

    render(6, 't5');
    expect(list().scrollLeft).toBe(600 - visible);
    expect(selectedFullyVisible()).toBe(true);
    expect(enabled()).toEqual([EDGE_START]);
  });

  it('pages to both ends without changing the width of the list', () => {
    mount();
    render(6, 't0');
    const visible = STRIP_WIDTH - 2 * BUTTON_WIDTH;
    const max = 600 - visible;

    act(() => scrollButton(EDGE_END).click());
    expect(list().scrollLeft).toBeCloseTo(visible * PAGE_FRACTION);
    expect(enabled()).toEqual([EDGE_START, EDGE_END]);
    act(() => scrollButton(EDGE_END).click());
    act(() => scrollButton(EDGE_END).click());
    expect(list().scrollLeft).toBe(max);
    expect(enabled()).toEqual([EDGE_START]);
    expect(list().clientWidth).toBe(visible);

    act(() => scrollButton(EDGE_START).click());
    expect(list().scrollLeft).toBeCloseTo(max - visible * PAGE_FRACTION);
    act(() => scrollButton(EDGE_START).click());
    act(() => scrollButton(EDGE_START).click());
    expect(list().scrollLeft).toBe(0);
    expect(enabled()).toEqual([EDGE_END]);
    expect(list().clientWidth).toBe(visible);
  });

  it('does not toggle the buttons back and forth around the point where the tabs just fit', () => {
    mount();
    stripWidth = 320;
    render(3, 't0');
    expect(present()).toEqual([]);

    // 300px of tabs in 290px: overflow; the buttons narrow the list, which keeps it overflowing.
    stripWidth = 290;
    resized(list());
    expect(present()).toEqual([EDGE_START, EDGE_END]);
    resized(list());
    expect(present()).toEqual([EDGE_START, EDGE_END]);

    // 310px: the tabs fit once the buttons give back their space, and keep fitting after.
    stripWidth = 310;
    resized(list());
    expect(present()).toEqual([]);
    resized(list());
    expect(present()).toEqual([]);
    expect(list().scrollLeft).toBe(0);
  });

  it('reveals the selected tab again when the container resizes', () => {
    mount();
    render(6, 't0');
    act(() => { list().scrollLeft = 150; });
    stripWidth = 230;
    resized(list());
    expect(list().scrollLeft).toBe(0);
    expect(enabled()).toEqual([EDGE_END]);
  });

  it('reveals the selected tab again when a tab changes its size', () => {
    mount();
    render(6, 't1');
    act(() => { list().scrollLeft = 300; });
    resized(list().children[3]);
    expect(list().scrollLeft).toBe(TAB_WIDTH);
  });
});
