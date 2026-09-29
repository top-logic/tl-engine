import { getApiBase } from './tl-react-bridge';

/** Data shape of the msgbuf-generated ViewPickEvent. */
export interface ViewPickEventData {
  token: string;
  targetWindowId: string;
}

const HIGHLIGHT_ID = 'tl-view-pick-highlight';

let _active = false;
let _token: string | null = null;
let _highlight: HTMLDivElement | null = null;

function nearestViewSource(start: EventTarget | null): HTMLElement | null {
  let el = start as Element | null;
  while (el) {
    if (el instanceof HTMLElement && el.dataset.viewSource) {
      return el;
    }
    el = el.parentElement;
  }
  return null;
}

function moveHighlight(el: HTMLElement): void {
  if (!_highlight) return;
  const r = el.getBoundingClientRect();
  _highlight.style.display = 'block';
  _highlight.style.left = `${r.left}px`;
  _highlight.style.top = `${r.top}px`;
  _highlight.style.width = `${r.width}px`;
  _highlight.style.height = `${r.height}px`;
}

function onPointerMove(e: PointerEvent): void {
  const el = nearestViewSource(e.target);
  if (el) {
    moveHighlight(el);
  } else if (_highlight) {
    _highlight.style.display = 'none';
  }
}

function onClick(e: MouseEvent): void {
  e.preventDefault();
  e.stopPropagation();
  const el = nearestViewSource(e.target);
  const token = _token;
  stop();
  if (el && token) {
    const path = el.dataset.viewSource!;
    void fetch(getApiBase() + 'react-api/view-pick', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ token, path }),
    }).catch(err => console.error('[TLReact] view-pick failed:', err));
  }
}

function onKeyDown(e: KeyboardEvent): void {
  if (e.key === 'Escape') {
    e.preventDefault();
    e.stopPropagation();
    stop();
  }
}

function start(token: string): void {
  if (_active) stop();
  _active = true;
  _token = token;

  _highlight = document.createElement('div');
  _highlight.id = HIGHLIGHT_ID;
  _highlight.style.display = 'none';
  document.body.appendChild(_highlight);

  document.body.classList.add('tlViewPick--active');
  document.addEventListener('pointermove', onPointerMove, true);
  document.addEventListener('click', onClick, true);
  document.addEventListener('keydown', onKeyDown, true);
}

function stop(): void {
  _active = false;
  _token = null;
  document.body.classList.remove('tlViewPick--active');
  document.removeEventListener('pointermove', onPointerMove, true);
  document.removeEventListener('click', onClick, true);
  document.removeEventListener('keydown', onKeyDown, true);
  if (_highlight) {
    _highlight.remove();
    _highlight = null;
  }
}

/** Handles a ViewPickEvent from SSE: enters pick mode in this window. */
export function handleViewPick(event: ViewPickEventData): void {
  const myWindow = document.body.dataset.windowName ?? '';
  if (event.targetWindowId && event.targetWindowId !== myWindow) {
    return;
  }
  start(event.token);
}

/** Idempotent init hook (no global listeners until a ViewPickEvent arrives). */
export function initViewPicker(): void {
  // Listeners are attached only while picking; nothing to install at startup.
}
