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
 *       {@code tl-tooltip-resolve} DOM event that carries the hovered element (a container
 *       answering for whichever of its parts the pointer rests on).</li>
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
 *
 * <p>Text cut off by an ellipsis offers its full text without any declaration. The text in
 * question is the one the pointer rests on: the text of the closest element around the hovered spot
 * that has text, widened to the largest element around it that adds no text of its own - the box of
 * a chip, a cell, a button with its icon. When a single element carries all of that text and is cut
 * off - {@code text-overflow: ellipsis} with content wider than its box, or a {@code line-clamp}
 * with content taller than its box - the tooltip is that element's text, whitespace collapsed, and
 * stands at that element. A declaration inside that box or the box itself is closer and wins; one
 * enclosing the box gives way to the cut-off text. Where an element of the box sets a native
 * {@code title}, the browser's tooltip speaks and the cut-off text offers none.</p>
 *
 * <p>A pointer press closes the tooltip at once and drops one still waiting to open, since what
 * the press brings up - a menu, a dialog - must not sit under it. The element the tooltip of the
 * pressed spot stands at then offers none until the pointer has left it. A press inside an open
 * tooltip leaves it alone.</p>
 */
import React from 'react';
import { createRoot, Root } from 'react-dom/client';
import { isMountedControl, getApiBase } from './tl-react-bridge';
import { TooltipPopover, TooltipData } from './TooltipPopover';
import { wrapRoot } from './root-wrapper';

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

/**
 * Declares the given text as the tooltip of the element the result is spread onto.
 *
 * <p>Text that is empty or absent yields no attributes at all, so the result can be spread
 * unconditionally onto an element whose tooltip is optional.</p>
 */
export function tooltipProps(text?: string | null): Record<string, string> {
  return text ? { [TOOLTIP_ATTR]: `text:${text}` } : {};
}

/**
 * Declares an element's own text as its tooltip, offered only while that text is not fully
 * readable.
 *
 * <p>Text cut off by an ellipsis needs no declaration (see above). This one is for an element whose
 * content is cut at its edge without an ellipsis - a row of several values clipped by the box they
 * sit in - so that the tooltip says what the element cannot show.</p>
 */
export const TOOLTIP_WHEN_CLIPPED: Record<string, string> = {
  [TOOLTIP_ATTR]: MODE_CONTENT,
  [TOOLTIP_WHEN_ATTR]: WHEN_TRUNCATED,
};

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
  /** Number of this activation, distinct from every earlier one. */
  id: number;
  anchor: Element;
  data: TooltipData;
}

const _cache = new Map<string, Promise<TooltipData | null>>();
let _hostDiv: HTMLDivElement | null = null;
let _root: Root | null = null;
let _openTimer: number | null = null;
let _closeTimer: number | null = null;
let _active: Active | null = null;
let _activations = 0;

/** The tooltip anchor pressed last, whose tooltip is held back until the pointer leaves it. */
let _suppressed: Element | null = null;

/**
 * Watches the document while a tooltip is shown and closes it as soon as its anchor leaves the
 * document - an anchor removed under the resting pointer sends no pointerout.
 */
let _anchorObserver: MutationObserver | null = null;

export function initTooltipHost(): void {
  if (_hostDiv) return;
  _hostDiv = document.createElement('div');
  _hostDiv.id = 'tl-tooltip-host';
  document.body.appendChild(_hostDiv);
  _root = createRoot(_hostDiv);
  renderActive();

  document.addEventListener('pointerover', onPointerOver, true);
  document.addEventListener('pointerout', onPointerOut, true);
  document.addEventListener('pointerdown', onPointerDown, true);
}

function onPointerDown(e: PointerEvent): void {
  const target = e.target as Element | null;
  if (target && _hostDiv && _hostDiv.contains(target)) return;

  cancelOpen();
  cancelClose();
  _active = null;
  renderActive();

  const spec = target ? findSpec(target) : null;
  _suppressed = spec && target ? tooltipAnchor(spec, target) : null;
}

function onPointerOver(e: PointerEvent): void {
  const target = e.target as Element | null;
  if (!target) return;
  if (_suppressed && _suppressed.contains(target)) return;
  const spec = findSpec(target);
  if (!spec) return;

  const pending = resolveSpec(spec, target);
  if (!pending) {
    // A declaration that yields nothing - an element whose own text is empty, a host declining
    // to answer - leaves the pointer on an element without a tooltip, which
    // closes what is open. The pointer arriving here suppressed the close its pointerout would
    // otherwise have scheduled, so the close is scheduled here instead.
    cancelOpen();
    scheduleClose();
    return;
  }

  cancelClose();
  cancelOpen();

  scheduleOpen(tooltipAnchor(spec, target), pending);
}

function onPointerOut(e: PointerEvent): void {
  const related = e.relatedTarget as Element | null;
  if (_suppressed && !(related && _suppressed.contains(related))) _suppressed = null;
  if (related && _hostDiv && _hostDiv.contains(related)) return;
  if (related && findSpec(related)) return;

  cancelOpen();
  scheduleClose();
}

/**
 * The element the tooltip of the given declaration stands at: the declaring element, or for a
 * {@link MODE_DYNAMIC} host, which answers for each of its parts, the part under the pointer.
 */
function tooltipAnchor(spec: Spec, target: Element): Element {
  return spec.kind === 'dynamic' ? target : spec.el;
}

/**
 * The tooltip of the given element: the closest declaration along its ancestors, or the text the
 * element shows cut off by an ellipsis, whichever is closer.
 *
 * <p>The first {@link TOOLTIP_ATTR} met decides, unless it encloses the box of a cut-off text, see
 * {@link findCutOffText}. A {@link TOOLTIP_WHEN_ATTR} passed on the way - the closest one, which is
 * the one met first - restricts the declaration: when its condition does not hold, that declaration
 * declines and no enclosing one takes over.</p>
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
        const cutOff = start ? findCutOffText(start) : null;
        if (cutOff && cutOff.box !== el && el.contains(cutOff.box)) return cutOff.spec;
        if (condition && !conditionMet(condition.value, condition.el)) return null;
        return parsed;
      }
    }
    el = el.parentElement;
  }
  return start ? findCutOffText(start)?.spec ?? null : null;
}

/**
 * The text at the given hovered element that an ellipsis cuts off, with the box it is read in.
 *
 * <p>The box is the closest element around the hovered one that has text, widened as long as the
 * enclosing element adds no text of its own. Its text is cut off when one element in it holds all
 * of it - the box itself or an element on the chain down from the box through the only child with
 * text - and that element is clipped with an ellipsis. Only that chain is inspected, so the cost
 * stays with the depth of the box, not its size, and a computed style is read only for an element
 * that is clipped at all. Text in several elements of the box (a value next to a count) is no
 * single cut-off text and offers none.</p>
 */
function findCutOffText(hovered: Element): { box: Element; spec: Spec } | null {
  let box: Element | null = hovered;
  while (box && !hasText(box)) box = box.parentElement;
  if (!box) return null;
  for (let parent = box.parentElement; parent && !addsText(parent, box); parent = parent.parentElement) {
    box = parent;
  }

  for (let el: Element | null = hovered; el; el = el.parentElement) {
    if (el.hasAttribute('title')) return null;
    if (el === box) break;
  }

  for (let el: Element | null = box; el; el = onlyChildWithText(el)) {
    if (isCutOff(el)) return { box, spec: { kind: 'content', el } };
  }
  return null;
}

/** Whether the given element shows any non-blank text, stopping at the first text found. */
function hasText(el: Element): boolean {
  const walker = document.createTreeWalker(el, NodeFilter.SHOW_TEXT);
  for (let node = walker.nextNode(); node; node = walker.nextNode()) {
    if ((node.nodeValue ?? '').trim() !== '') return true;
  }
  return false;
}

/** Whether the given parent shows text beside the text of the given child. */
function addsText(parent: Element, child: Element): boolean {
  for (let node = parent.firstChild; node; node = node.nextSibling) {
    if (node === child) continue;
    if (node.nodeType === Node.TEXT_NODE) {
      if ((node.nodeValue ?? '').trim() !== '') return true;
    } else if (node instanceof Element && hasText(node)) {
      return true;
    }
  }
  return false;
}

/**
 * The child holding all text of the given element, or {@code null} when the element shows text
 * of its own or spreads it over several children.
 */
function onlyChildWithText(el: Element): Element | null {
  if (hasOwnText(el)) return null;
  let result: Element | null = null;
  for (let child = el.firstElementChild; child; child = child.nextElementSibling) {
    if (hasText(child)) {
      if (result) return null;
      result = child;
    }
  }
  return result;
}

/** Whether the given element cuts off its text with an ellipsis, on one line or several. */
function isCutOff(el: Element): boolean {
  const wide = el.scrollWidth > el.clientWidth;
  const tall = el.scrollHeight > el.clientHeight;
  if (!wide && !tall) return false;
  const style = getComputedStyle(el);
  if (wide && style.textOverflow === 'ellipsis') return true;
  const clamp = style.getPropertyValue('-webkit-line-clamp');
  return tall && clamp !== '' && clamp !== 'none';
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
    _active = { id: ++_activations, anchor, data };
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

function onDocumentMutated(): void {
  if (!_active || _active.anchor.isConnected) return;
  // A pending open targets another anchor and checks that one itself when it fires.
  cancelClose();
  _active = null;
  renderActive();
}

/** Observes the document exactly while a tooltip is shown. */
function updateAnchorObserver(): void {
  if (_active) {
    if (!_anchorObserver) {
      _anchorObserver = new MutationObserver(onDocumentMutated);
      _anchorObserver.observe(document.body, { childList: true, subtree: true });
    }
  } else if (_anchorObserver) {
    _anchorObserver.disconnect();
    _anchorObserver = null;
  }
}

/** Shows the tooltip of {@link _active}, or none when there is none. */
function renderActive(): void {
  updateAnchorObserver();
  if (!_root || !_hostDiv) return;
  if (!_active) { _root.render(null); return; }
  const { id, anchor, data } = _active;
  // Each activation mounts a popover of its own: the floating hook positions against the anchor
  // it was mounted with, so a tooltip taking over from one still open - the pointer moved on to
  // the next button before the first closed - must not reuse the first popover's instance.
  _root.render(wrapRoot(
    React.createElement(TooltipPopover, {
      key: id,
      anchor, data,
      portalRoot: _hostDiv,
      onClose: () => { _active = null; renderActive(); },
      onEnter: cancelClose,
      onLeave: scheduleClose,
    })
  ));
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
