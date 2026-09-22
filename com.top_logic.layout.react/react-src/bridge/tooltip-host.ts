/**
 * Tooltip delegation for the whole document: a single pair of hover listeners resolves the tooltip
 * of whatever the pointer rests on and renders it through one {@link TooltipPopover} portal.
 *
 * <p>An element declares its tooltip with the {@link TOOLTIP_ATTR} attribute, whose value names
 * the mode:</p>
 * <ul>
 *   <li>{@code text:<text>} - the plain text following the prefix.</li>
 *   <li>{@code html:<markup>} - the markup following the prefix.</li>
 *   <li>{@code key:<key>} - rich content fetched from the server for the enclosing mounted
 *       control, which answers the key.</li>
 *   <li>{@code dynamic} - the declaring element resolves key or content itself, asked through a
 *       {@code tl-tooltip-resolve} DOM event that carries the hovered element (a table resolving
 *       the tooltip of the cell under the pointer).</li>
 *   <li>{@code content} - the element's own text, whitespace collapsed; an element without text
 *       has no tooltip.</li>
 * </ul>
 *
 * <p>Resolution walks up from the hovered element, so the closest declaration wins. On the way an
 * element may attach a condition with {@link TOOLTIP_WHEN_ATTR}; the closest one between the
 * hovered element and the declaring element - that element included - decides.
 * {@link WHEN_TRUNCATED} holds the tooltip back while the condition element's text is fully
 * readable and yields it once that text is clipped or hidden, so the tooltip says what the element
 * itself cannot show. A condition that is not met yields no tooltip at all: the walk stops at the
 * declaration that declined instead of falling through to an enclosing one. An unknown condition
 * imposes no restriction.</p>
 */
import React from 'react';
import { createRoot, Root } from 'react-dom/client';
import { isMountedControl, getApiBase } from './tl-react-bridge';
import { TooltipPopover, TooltipData } from './TooltipPopover';

const HOVER_DELAY_MS = 400;
const CLOSE_DELAY_MS_PASSIVE = 150;
const CLOSE_DELAY_MS_INTERACTIVE = 400;

/** Attribute with which an element declares its tooltip, see the mode list above. */
export const TOOLTIP_ATTR = 'data-tooltip';

/** Attribute with which an element restricts when the declared tooltip is shown. */
export const TOOLTIP_WHEN_ATTR = 'data-tooltip-when';

/**
 * {@link TOOLTIP_WHEN_ATTR} value showing the tooltip only while the element's text is not fully
 * readable, because it is clipped or hidden.
 */
export const WHEN_TRUNCATED = 'truncated';

/** {@link TOOLTIP_ATTR} value letting the declaring element resolve its tooltip on demand. */
const MODE_DYNAMIC = 'dynamic';

/** {@link TOOLTIP_ATTR} value taking the tooltip from the declaring element's own text. */
const MODE_CONTENT = 'content';

export interface TooltipResolveDetail {
  target: Element;
  resolved: { key: string } | { inline: TooltipData } | null;
}

type Spec =
  | { kind: 'text'; text: string; el: Element }
  | { kind: 'html'; html: string; el: Element }
  | { kind: 'key'; controlId: string; key: string; el: Element }
  | { kind: 'dynamic'; host: Element }
  | { kind: 'content'; el: Element };

interface Active {
  anchor: Element;
  data: TooltipData;
}

const _cache = new Map<string, Promise<TooltipData | null>>();
let _hostDiv: HTMLDivElement | null = null;
let _root: Root | null = null;
let _openTimer: number | null = null;
let _closeTimer: number | null = null;
let _active: Active | null = null;

export function initTooltipHost(): void {
  if (_hostDiv) return;
  _hostDiv = document.createElement('div');
  _hostDiv.id = 'tl-tooltip-host';
  document.body.appendChild(_hostDiv);
  _root = createRoot(_hostDiv);
  renderActive();

  document.addEventListener('pointerover', onPointerOver, true);
  document.addEventListener('pointerout', onPointerOut, true);
}

function onPointerOver(e: PointerEvent): void {
  const target = e.target as Element | null;
  if (!target) return;
  const spec = findSpec(target);
  if (!spec) return;

  const pending = resolveSpec(spec, target);
  if (!pending) {
    // A declaration that yields nothing - an empty cell of a table resolving its tooltips
    // dynamically, a host declining - leaves the pointer on an element without a tooltip, which
    // closes what is open. The pointer arriving here suppressed the close its pointerout would
    // otherwise have scheduled, so the close is scheduled here instead.
    cancelOpen();
    scheduleClose();
    return;
  }

  cancelClose();
  cancelOpen();

  const anchor = spec.kind === 'dynamic' ? target : spec.el;
  scheduleOpen(anchor, pending);
}

function onPointerOut(e: PointerEvent): void {
  const related = e.relatedTarget as Element | null;
  if (related && _hostDiv && _hostDiv.contains(related)) return;
  if (related && findSpec(related)) return;

  cancelOpen();
  scheduleClose();
}

/**
 * The tooltip declared for the given element, looked up along its ancestors.
 *
 * <p>The first {@link TOOLTIP_ATTR} met decides. A {@link TOOLTIP_WHEN_ATTR} passed on the way -
 * the closest one, which is the one met first - restricts it: when its condition does not hold,
 * that declaration declines and no enclosing one takes over.</p>
 */
function findSpec(start: Element | null): Spec | null {
  let el: Element | null = start;
  let condition: { value: string; el: Element } | null = null;
  while (el) {
    if (!condition) {
      const when = el.getAttribute?.(TOOLTIP_WHEN_ATTR);
      if (when != null) condition = { value: when, el };
    }
    const raw = el.getAttribute?.(TOOLTIP_ATTR);
    if (raw != null) {
      const parsed = parseAttr(raw, el);
      if (parsed) {
        if (condition && !conditionMet(condition.value, condition.el)) return null;
        return parsed;
      }
    }
    el = el.parentElement;
  }
  return null;
}

const _unknownConditions = new Set<string>();

function conditionMet(condition: string, el: Element): boolean {
  if (condition === WHEN_TRUNCATED) return isTruncated(el);
  if (!_unknownConditions.has(condition)) {
    _unknownConditions.add(condition);
    console.warn(`Unknown ${TOOLTIP_WHEN_ATTR} value "${condition}", tooltip shown unconditionally.`);
  }
  return true;
}

/**
 * Whether the text of the given element is not fully readable.
 *
 * <p>Text is unreadable when it does not fit its box - the element's own or that of a descendant,
 * which is where a clipped label sits - or when the element showing it has no layout box at all,
 * as a label a compact layout sets to {@code display: none} has. Only descendants are checked for
 * that, since the element itself is under the pointer and therefore laid out.</p>
 */
function isTruncated(el: Element): boolean {
  if (isClipped(el)) return true;
  const descendants = el.querySelectorAll('*');
  for (let n = 0; n < descendants.length; n++) {
    const descendant = descendants[n];
    if (isClipped(descendant)) return true;
    if (hasOwnText(descendant) && descendant.getClientRects().length === 0) return true;
  }
  return false;
}

/** Whether the element's content is wider than the box showing it. */
function isClipped(el: Element): boolean {
  return el.scrollWidth > el.clientWidth;
}

/** Whether the element shows text of its own, as opposed to only the text of nested elements. */
function hasOwnText(el: Element): boolean {
  for (let child = el.firstChild; child; child = child.nextSibling) {
    if (child.nodeType === Node.TEXT_NODE && (child.nodeValue ?? '').trim() !== '') return true;
  }
  return false;
}

function parseAttr(raw: string, el: Element): Spec | null {
  if (raw === MODE_DYNAMIC) return { kind: 'dynamic', host: el };
  if (raw === MODE_CONTENT) return { kind: 'content', el };
  const idx = raw.indexOf(':');
  if (idx < 0) return null;
  const prefix = raw.substring(0, idx);
  const payload = raw.substring(idx + 1);
  switch (prefix) {
    case 'text': return { kind: 'text', text: payload, el };
    case 'html': return { kind: 'html', html: payload, el };
    case 'key': {
      const controlId = resolveControlId(el);
      if (!controlId) return null;
      return { kind: 'key', controlId, key: payload, el };
    }
    default:
      return null;
  }
}

function resolveControlId(trigger: Element): string | null {
  let el: Element | null = trigger;
  while (el) {
    const id = (el as HTMLElement).id;
    if (id && isMountedControl(id)) return id;
    el = el.parentElement;
  }
  return null;
}

function resolveSpec(spec: Spec, target: Element): Promise<TooltipData | null> | null {
  switch (spec.kind) {
    case 'text': return Promise.resolve({ text: spec.text });
    case 'html': return Promise.resolve({ html: spec.html });
    case 'content': {
      // The rendered text, read as one line: the tooltip repeats what the element says, not how
      // its markup happens to lay it out.
      const text = (spec.el.textContent ?? '').replace(/\s+/g, ' ').trim();
      return text ? Promise.resolve({ text }) : null;
    }
    case 'key': {
      const cacheKey = spec.controlId + '\u0000' + spec.key;
      let p = _cache.get(cacheKey);
      if (!p) {
        p = fetchTooltip(spec.controlId, spec.key);
        _cache.set(cacheKey, p);
      }
      return p;
    }
    case 'dynamic': {
      const detail: TooltipResolveDetail = { target, resolved: null };
      spec.host.dispatchEvent(new CustomEvent('tl-tooltip-resolve', { detail, bubbles: false }));
      const r = detail.resolved;
      if (!r) return null;
      if ('inline' in r) return Promise.resolve(r.inline);
      const controlId = resolveControlId(spec.host);
      if (!controlId) return null;
      const cacheKey = controlId + '\u0000' + r.key;
      let p = _cache.get(cacheKey);
      if (!p) {
        p = fetchTooltip(controlId, r.key);
        _cache.set(cacheKey, p);
      }
      return p;
    }
  }
}

function scheduleOpen(anchor: Element, pending: Promise<TooltipData | null>): void {
  _openTimer = window.setTimeout(async () => {
    _openTimer = null;
    let data: TooltipData | null = null;
    try {
      data = await pending;
    } catch (problem) {
      console.warn('Tooltip resolution failed.', problem);
    }
    // A resolution arriving empty or failing, and an anchor gone from the document, leave the
    // pointer on an element without a tooltip: whatever is open goes with it.
    if (!data || !document.contains(anchor)) {
      _active = null;
      renderActive();
      return;
    }
    _active = { anchor, data };
    renderActive();
  }, HOVER_DELAY_MS);
}

function scheduleClose(): void {
  const delay = _active?.data.interactive ? CLOSE_DELAY_MS_INTERACTIVE : CLOSE_DELAY_MS_PASSIVE;
  _closeTimer = window.setTimeout(() => {
    _closeTimer = null;
    _active = null;
    renderActive();
  }, delay);
}

function cancelOpen(): void {
  if (_openTimer != null) { window.clearTimeout(_openTimer); _openTimer = null; }
}

function cancelClose(): void {
  if (_closeTimer != null) { window.clearTimeout(_closeTimer); _closeTimer = null; }
}

function renderActive(): void {
  if (!_root || !_hostDiv) return;
  if (!_active) { _root.render(null); return; }
  const { anchor, data } = _active;
  _root.render(
    React.createElement(TooltipPopover, {
      anchor, data,
      portalRoot: _hostDiv,
      onClose: () => { _active = null; renderActive(); },
      onEnter: cancelClose,
      onLeave: scheduleClose,
    })
  );
}

async function fetchTooltip(controlId: string, key: string): Promise<TooltipData | null> {
  const windowName = (document.body.dataset.windowName as string) ?? '';
  const url = getApiBase() + `react-api/tooltip?controlId=${encodeURIComponent(controlId)}`
    + `&key=${encodeURIComponent(key)}`
    + `&windowName=${encodeURIComponent(windowName)}`;
  const resp = await fetch(url, { credentials: 'same-origin' });
  if (!resp.ok) return null;
  return (await resp.json()) as TooltipData;
}
