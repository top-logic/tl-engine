import { getApiBase, isMountedControl } from './tl-react-bridge';

/** What a click in pick mode is resolved to (the external names of the server's PickKind). */
export type PickKind = 'view' | 'control';

/** Data shape of the msgbuf-generated PickEvent. */
export interface PickEventData {
  token: string;
  targetWindowId: string;
  kind: PickKind;
}

const HIGHLIGHT_ID = 'tl-pick-highlight';

const ACTIVE_CLASS = 'tlPick--active';

/** JSON field names of the pick result, mirroring the server's ElementPicker constants. */
const TOKEN_FIELD = 'token';
const KIND_FIELD = 'kind';
const PATH_FIELD = 'path';
const CONTROL_ID_FIELD = 'controlId';

/** The element under the pointer that a click would report, with what identifies it. */
interface PickTarget {
  element: HTMLElement;
  /** The value reported for the pick's kind: a view source path, or a control id. */
  value: string;
}

let _active = false;
let _token: string | null = null;
let _kind: PickKind = 'view';
let _highlight: HTMLDivElement | null = null;

/** Walks up from the clicked element to the nearest view source. */
function nearestViewSource(start: EventTarget | null): PickTarget | null {
  let el = start as Element | null;
  while (el) {
    if (el instanceof HTMLElement && el.dataset.viewSource) {
      return { element: el, value: el.dataset.viewSource };
    }
    el = el.parentElement;
  }
  return null;
}

/** Walks up from the clicked element to the innermost control mounted in this window. */
function nearestControl(start: EventTarget | null): PickTarget | null {
  let el = start as Element | null;
  while (el) {
    if (el instanceof HTMLElement && el.id && isMountedControl(el.id)) {
      return { element: el, value: el.id };
    }
    el = el.parentElement;
  }
  return null;
}

function nearestTarget(start: EventTarget | null): PickTarget | null {
  return _kind === 'control' ? nearestControl(start) : nearestViewSource(start);
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
  const target = nearestTarget(e.target);
  if (target) {
    moveHighlight(target.element);
  } else if (_highlight) {
    _highlight.style.display = 'none';
  }
}

function onClick(e: MouseEvent): void {
  e.preventDefault();
  e.stopPropagation();
  const target = nearestTarget(e.target);
  const token = _token;
  const kind = _kind;
  stop();
  if (target && token) {
    const body: Record<string, string> = { [TOKEN_FIELD]: token, [KIND_FIELD]: kind };
    body[kind === 'control' ? CONTROL_ID_FIELD : PATH_FIELD] = target.value;
    void fetch(getApiBase() + 'react-api/pick', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    }).catch(err => console.error('[TLReact] pick failed:', err));
  }
}

function onKeyDown(e: KeyboardEvent): void {
  if (e.key === 'Escape') {
    e.preventDefault();
    e.stopPropagation();
    stop();
  }
}

function start(token: string, kind: PickKind): void {
  if (_active) stop();
  _active = true;
  _token = token;
  _kind = kind;

  _highlight = document.createElement('div');
  _highlight.id = HIGHLIGHT_ID;
  _highlight.style.display = 'none';
  document.body.appendChild(_highlight);

  document.body.classList.add(ACTIVE_CLASS);
  document.addEventListener('pointermove', onPointerMove, true);
  document.addEventListener('click', onClick, true);
  document.addEventListener('keydown', onKeyDown, true);
}

function stop(): void {
  _active = false;
  _token = null;
  document.body.classList.remove(ACTIVE_CLASS);
  document.removeEventListener('pointermove', onPointerMove, true);
  document.removeEventListener('click', onClick, true);
  document.removeEventListener('keydown', onKeyDown, true);
  if (_highlight) {
    _highlight.remove();
    _highlight = null;
  }
}

/** Handles a PickEvent from SSE: enters pick mode in this window. */
export function handlePick(event: PickEventData): void {
  const myWindow = document.body.dataset.windowName ?? '';
  if (event.targetWindowId && event.targetWindowId !== myWindow) {
    return;
  }
  start(event.token, event.kind === 'control' ? 'control' : 'view');
}

/** Idempotent init hook (no global listeners until a PickEvent arrives). */
export function initElementPicker(): void {
  // Listeners are attached only while picking; nothing to install at startup.
}
