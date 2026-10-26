import React, { act } from 'react';
import { createRoot } from 'react-dom/client';
import type { Root } from 'react-dom/client';
import { afterAll, afterEach, beforeAll, describe, expect, it } from 'vitest';
import { EDGE_END, EDGE_START, PAGE_FRACTION, pageScrollLeft, revealScrollLeft, stripOverflow } from './scroll-strip';
import { TabStrip } from './TabStrip';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

describe('stripOverflow', () => {
  it('reports no hidden content when the content fits', () => {
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 300 }))
      .toEqual({ canScrollStart: false, canScrollEnd: false });
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 120 }))
      .toEqual({ canScrollStart: false, canScrollEnd: false });
  });

  it('reports hidden content in each direction separately', () => {
    expect(stripOverflow({ scrollLeft: 0, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ canScrollStart: false, canScrollEnd: true });
    expect(stripOverflow({ scrollLeft: 200, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ canScrollStart: true, canScrollEnd: true });
    expect(stripOverflow({ scrollLeft: 500, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ canScrollStart: true, canScrollEnd: false });
  });

  it('ignores sub-pixel remainders at the ends', () => {
    expect(stripOverflow({ scrollLeft: 0.3, clientWidth: 300, scrollWidth: 800.4 }))
      .toEqual({ canScrollStart: false, canScrollEnd: true });
    expect(stripOverflow({ scrollLeft: 499.8, clientWidth: 300, scrollWidth: 800 }))
      .toEqual({ canScrollStart: true, canScrollEnd: false });
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
 * A layout for jsdom, which has none: the tab list is {@link LIST_WIDTH} wide, each tab
 * {@link TAB_WIDTH}, side by side from the start of the list.
 */
const LIST_WIDTH = 250;
const TAB_WIDTH = 100;

const scrollOffsets = new WeakMap<Element, number>();
const isList = (e: Element) => e.getAttribute('role') === 'tablist';
const tabsOf = (list: Element) => list.querySelectorAll('[role="tab"]').length;

const saved: [string, PropertyDescriptor | undefined][] = [];
function stub(name: string, descriptor: PropertyDescriptor): void {
  saved.push([name, Object.getOwnPropertyDescriptor(Element.prototype, name)]);
  Object.defineProperty(Element.prototype, name, { configurable: true, ...descriptor });
}

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  stub('clientWidth', { get(this: Element) { return isList(this) ? LIST_WIDTH : 0; } });
  stub('scrollWidth', { get(this: Element) { return isList(this) ? tabsOf(this) * TAB_WIDTH : 0; } });
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
        width = LIST_WIDTH;
      } else if (this.getAttribute('role') === 'tab') {
        const list = this.parentElement!;
        left = Array.from(list.children).indexOf(this) * TAB_WIDTH - list.scrollLeft;
        width = TAB_WIDTH;
      }
      return { left, right: left + width, top: 0, bottom: 0, width, height: 0, x: left, y: 0, toJSON: () => ({}) };
    },
  });
});

afterAll(() => {
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

  const list = () => container.querySelector('[role="tablist"]')!;
  const scrollButton = (edge: string) => container.querySelector<HTMLButtonElement>(`[data-tl-edge="${edge}"]`)!;
  const shown = () => [EDGE_START, EDGE_END].filter(edge => !scrollButton(edge).hidden);

  afterEach(() => {
    act(() => root.unmount());
    container.remove();
  });

  function mount(): void {
    container = document.createElement('div');
    document.body.appendChild(container);
    root = createRoot(container);
  }

  it('shows no scroll button while the tabs fit', () => {
    mount();
    render(2, 't1');
    expect(shown()).toEqual([]);
    expect(list().scrollLeft).toBe(0);
  });

  it('keeps the selected tab in view by scrolling the list only', () => {
    mount();
    render(6, 't0');
    expect(list().scrollLeft).toBe(0);
    expect(shown()).toEqual([EDGE_END]);

    // Tab 4 (400..500) is hidden behind the end: it moves to the end edge.
    render(6, 't4');
    expect(list().scrollLeft).toBe(500 - LIST_WIDTH);
    expect(shown()).toEqual([EDGE_START, EDGE_END]);

    // Tab 3 (300..400) is in view (250..500): nothing moves.
    render(6, 't3');
    expect(list().scrollLeft).toBe(500 - LIST_WIDTH);

    render(6, 't5');
    expect(list().scrollLeft).toBe(600 - LIST_WIDTH);
    expect(shown()).toEqual([EDGE_START]);
  });

  it('scrolls a page towards the edge of the button pressed', () => {
    mount();
    render(6, 't5');
    act(() => scrollButton(EDGE_START).click());
    expect(list().scrollLeft).toBe(600 - LIST_WIDTH - LIST_WIDTH * PAGE_FRACTION);
    act(() => scrollButton(EDGE_START).click());
    expect(list().scrollLeft).toBe(0);
    expect(shown()).toEqual([EDGE_END]);
  });
});
