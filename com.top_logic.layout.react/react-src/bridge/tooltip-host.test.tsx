// The tooltip of an element whose text is cut off by an ellipsis: without a tooltip of its own the element offers its
// full text, with one the full text stands as the first line above it - as long as the text is cut off and the tooltip
// does not name it already, and also when the tooltip is fetched or resolved on demand.

import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest';
import { act } from 'react';

/** The control the elements under test belong to, which answers the `key:` tooltips. */
const CONTROL_ID = 'c1';

vi.mock('./tl-react-bridge', async (importOriginal) => ({
  ...(await importOriginal<typeof import('./tl-react-bridge')>()),
  isMountedControl: (id: string) => id === CONTROL_ID,
  getApiBase: () => '/app/',
}));

import { HOVER_DELAY_MS, TOOLTIP_ATTR, initTooltipHost } from './tooltip-host';
import type { TooltipResolveDetail } from './tooltip-host';
import type { TooltipData } from './TooltipPopover';

declare global {
  // eslint-disable-next-line no-var
  var IS_REACT_ACT_ENVIRONMENT: boolean;
}

const LABEL = 'Critical';

const DESCRIPTION = 'Flag for emphasis, without further effect.';

/** A resize observer that never reports: jsdom lays nothing out. */
class NoResizeObserver {
  observe() {}
  unobserve() {}
  disconnect() {}
}

/** The answers of the server to the tooltip keys asked for, by key; a key without answer is refused. */
const answers = new Map<string, TooltipData>();

/** The control the elements under test are placed in. */
let control: HTMLElement;

beforeAll(() => {
  globalThis.IS_REACT_ACT_ENVIRONMENT = true;
  act(() => initTooltipHost());
});

afterAll(() => {
  document.body.replaceChildren();
});

beforeEach(() => {
  vi.useFakeTimers();
  vi.stubGlobal('ResizeObserver', NoResizeObserver);
  vi.stubGlobal('fetch', vi.fn(async (url: string) => {
    const key = new URL(url, 'http://localhost').searchParams.get('key') ?? '';
    const answer = answers.get(key);
    return answer
      ? new Response(JSON.stringify(answer), { status: 200, headers: { 'Content-Type': 'application/json' } })
      : new Response('', { status: 404 });
  }));
  // jsdom's viewport has no size: floating-ui would push every tooltip to the same edge.
  Object.defineProperty(document.documentElement, 'clientWidth', { configurable: true, value: 1000 });
  Object.defineProperty(document.documentElement, 'clientHeight', { configurable: true, value: 800 });
  control = document.createElement('div');
  control.id = CONTROL_ID;
  document.body.append(control);
});

afterEach(async () => {
  // A press closes the tooltip that is open.
  await act(async () => {
    document.body.dispatchEvent(new MouseEvent('pointerdown', { bubbles: true }));
  });
  control.remove();
  answers.clear();
  delete (document.documentElement as { clientWidth?: number }).clientWidth;
  delete (document.documentElement as { clientHeight?: number }).clientHeight;
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

/**
 * A label showing the given text, declaring the given tooltip (none: no declaration), cut off by its ellipsis or
 * fully readable.
 */
function label(text: string, tooltip: string | null, cutOff: boolean): HTMLElement {
  const element = document.createElement('span');
  element.style.textOverflow = 'ellipsis';
  element.style.overflow = 'hidden';
  element.style.whiteSpace = 'nowrap';
  if (tooltip != null) {
    element.setAttribute(TOOLTIP_ATTR, tooltip);
  }
  element.textContent = text;
  // jsdom lays nothing out: the content is as wide as the box, or wider when it is cut off.
  Object.defineProperty(element, 'clientWidth', { configurable: true, value: 40 });
  Object.defineProperty(element, 'scrollWidth', { configurable: true, value: cutOff ? 120 : 40 });
  control.append(element);
  return element;
}

/** A label resolving its tooltip on demand with the given answer, as a container answers for its parts. */
function dynamicLabel(cutOff: boolean, resolved: TooltipResolveDetail['resolved']): HTMLElement {
  const element = label(LABEL, 'dynamic', cutOff);
  element.addEventListener('tl-tooltip-resolve', event => {
    (event as CustomEvent<TooltipResolveDetail>).detail.resolved = resolved;
  });
  return element;
}

/** The pointer moves from outside onto the given element. */
function hover(element: Element) {
  element.dispatchEvent(new MouseEvent('pointerover', { bubbles: true }));
}

/** The pointer rests until the tooltip waiting for it has resolved its content and opened. */
async function rest() {
  await act(async () => {
    await vi.advanceTimersByTimeAsync(HOVER_DELAY_MS);
  });
  await act(async () => {
    await vi.advanceTimersByTimeAsync(0);
  });
}

/** The shown tooltip, or null when none is shown. */
function shownTooltip(): HTMLElement | null {
  return document.querySelector<HTMLElement>('[role="tooltip"]');
}

/** The shown tooltip, which must be there. */
function tooltip(): HTMLElement {
  const result = shownTooltip();
  expect(result, 'no tooltip shown').not.toBeNull();
  return result!;
}

/** The full text line of the shown tooltip, or null when it has none. */
function fullTextLine(): string | null {
  return tooltip().querySelector(':scope > .tl-tooltip__full-text')?.textContent ?? null;
}

/** The body of the shown tooltip. */
function body(): string | null {
  return tooltip().querySelector(':scope > .tl-tooltip__body')?.textContent ?? null;
}

describe('tooltip of a cut-off element', () => {
  it('a cut-off element with a tooltip of its own shows its full text above the tooltip', async () => {
    hover(label(LABEL, 'text:' + DESCRIPTION, true));
    await rest();

    const line = tooltip().querySelector(':scope > .tl-tooltip__full-text');
    expect(line?.textContent).toBe(LABEL);
    expect(line?.classList).toContain('tl-type-label-strong');
    // The full text is the first line, the tooltip of the element follows it.
    expect(tooltip().firstElementChild).toBe(line);
    expect(body()).toBe(DESCRIPTION);
  });

  it('an element that is not cut off shows only its own tooltip', async () => {
    hover(label(LABEL, 'text:' + DESCRIPTION, false));
    await rest();

    expect(fullTextLine()).toBeNull();
    expect(body()).toBe(DESCRIPTION);
  });

  it('a cut-off element without a tooltip of its own shows its full text as the tooltip', async () => {
    hover(label(LABEL, null, true));
    await rest();

    expect(fullTextLine()).toBeNull();
    expect(body()).toBe(LABEL);
  });

  it('an element that is neither cut off nor declares a tooltip shows none', async () => {
    hover(label(LABEL, null, false));
    await rest();

    expect(shownTooltip()).toBeNull();
  });

  it('a tooltip saying the full text shows it once', async () => {
    hover(label(LABEL, 'text:' + LABEL, true));
    await rest();

    expect(fullTextLine()).toBeNull();
    expect(body()).toBe(LABEL);
  });

  it('a tooltip whose caption is the full text shows it once', async () => {
    hover(dynamicLabel(true, { inline: { caption: LABEL, text: DESCRIPTION } }));
    await rest();

    expect(fullTextLine()).toBeNull();
    expect(tooltip().querySelector('.tl-tooltip__caption')?.textContent).toBe(LABEL);
    expect(body()).toBe(DESCRIPTION);
  });

  it('a tooltip fetched for a key gets the full text above it once it is resolved', async () => {
    answers.set('critical.tooltip', { text: DESCRIPTION });
    hover(label(LABEL, 'key:critical.tooltip', true));
    await rest();

    expect(fullTextLine()).toBe(LABEL);
    expect(body()).toBe(DESCRIPTION);
  });

  it('a tooltip resolved on demand gets the full text above it', async () => {
    hover(dynamicLabel(true, { inline: { html: '<p>' + DESCRIPTION + '</p>' } }));
    await rest();

    expect(fullTextLine()).toBe(LABEL);
    expect(body()).toBe(DESCRIPTION);
  });

  it('text for screen readers only is no part of the full text', async () => {
    const element = label(LABEL, 'text:' + DESCRIPTION, true);
    const hidden = document.createElement('span');
    hidden.className = 'tl-visually-hidden';
    hidden.textContent = 'required';
    element.append(hidden);
    hover(element);
    await rest();

    expect(fullTextLine()).toBe(LABEL);
    expect(body()).toBe(DESCRIPTION);
  });

  it('a cut-off element whose key resolves to nothing shows its full text', async () => {
    hover(label(LABEL, 'key:unknown.tooltip', true));
    await rest();

    expect(fullTextLine()).toBeNull();
    expect(body()).toBe(LABEL);
  });

  it('a cut-off element whose host declines to answer shows its full text', async () => {
    hover(dynamicLabel(true, null));
    await rest();

    expect(fullTextLine()).toBeNull();
    expect(body()).toBe(LABEL);
  });

  it('an element that is not cut off and whose tooltip resolves to nothing shows none', async () => {
    hover(label(LABEL, 'key:unknown.tooltip', false));
    await rest();
    expect(shownTooltip()).toBeNull();

    hover(dynamicLabel(false, null));
    await rest();
    expect(shownTooltip()).toBeNull();
  });
});
