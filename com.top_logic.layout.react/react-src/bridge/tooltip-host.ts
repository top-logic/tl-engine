import React from 'react';
import { createRoot, Root } from 'react-dom/client';
import { isMountedControl, getApiBase } from './tl-react-bridge';
import { TooltipPopover, TooltipData } from './TooltipPopover';

const HOVER_DELAY_MS = 400;
const CLOSE_DELAY_MS = 150;

export interface TooltipResolveDetail {
  target: Element;
  resolved: { key: string } | { inline: TooltipData } | null;
}

type Spec =
  | { kind: 'text'; text: string; el: Element }
  | { kind: 'html'; html: string; el: Element }
  | { kind: 'key'; controlId: string; key: string; el: Element }
  | { kind: 'dynamic'; host: Element };

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

  cancelClose();
  cancelOpen();

  const pending = resolveSpec(spec, target);
  if (!pending) return;

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

function findSpec(start: Element | null): Spec | null {
  let el: Element | null = start;
  while (el) {
    const raw = el.getAttribute?.('data-tooltip');
    if (raw != null) {
      const parsed = parseAttr(raw, el);
      if (parsed) return parsed;
    }
    el = el.parentElement;
  }
  return null;
}

function parseAttr(raw: string, el: Element): Spec | null {
  if (raw === 'dynamic') return { kind: 'dynamic', host: el };
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
    const data = await pending;
    if (!data) return;
    if (!document.contains(anchor)) return;
    _active = { anchor, data };
    renderActive();
  }, HOVER_DELAY_MS);
}

function scheduleClose(): void {
  _closeTimer = window.setTimeout(() => {
    _closeTimer = null;
    _active = null;
    renderActive();
  }, CLOSE_DELAY_MS);
}

function cancelOpen(): void {
  if (_openTimer != null) { window.clearTimeout(_openTimer); _openTimer = null; }
}

function cancelClose(): void {
  if (_closeTimer != null) { window.clearTimeout(_closeTimer); _closeTimer = null; }
}

function renderActive(): void {
  if (!_root) return;
  if (!_active) { _root.render(null); return; }
  const { anchor, data } = _active;
  _root.render(
    React.createElement(TooltipPopover, {
      anchor, data,
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
