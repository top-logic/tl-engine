/**
 * Global keyboard-gesture dispatcher.
 *
 * <p>A single {@code keydown} listener walks a stack of registered keyboard scopes
 * from the innermost (deepest in the React tree / most recently opened) outward; the
 * first active scope that binds the pressed gesture handles it. The individual
 * gesture-&gt;handler bindings are contributed by the controls themselves (buttons,
 * the table) via {@code useKeyboardBinding} in the React bridge.</p>
 *
 * <p>The outermost scope is the page scope ({@link pageScope}), created here before
 * any other scope exists and therefore walked last. It gives controls that sit
 * directly on the page — a page toolbar's buttons, the primary button of a login
 * page — a scope to bind their gestures in. Dialogs, windows and tables stack above
 * it with their own {@code KeyboardScopeProvider}: a modal window traps every
 * gesture, so page bindings stay dormant while it is open.</p>
 */

/** A gesture handler. Returning {@code false} declines so the dispatcher keeps looking. */
export type GestureHandler = () => boolean | void;

export interface KeyboardScope {
  /** Monotonic id; larger == created later == deeper/more-inner == higher priority. */
  readonly id: number;
  /** Whether this scope currently wants gestures (e.g. a focus check for tables). */
  isActive(): boolean;
  /** Canonical-gesture -&gt; stack of handlers (last registered wins within a scope). */
  readonly bindings: Map<string, GestureHandler[]>;
  /** When true, the scope traps all gestures: unbound keys do not fall through to scopes below. */
  readonly modal: boolean;
}

const _scopes: KeyboardScope[] = [];
let _nextScopeId = 1;
let _installed = false;

/** Allocates a new, not-yet-registered scope. Called during render so ids track tree depth. */
export function createScope(isActive: () => boolean, modal = false): KeyboardScope {
  return { id: _nextScopeId++, isActive, bindings: new Map(), modal };
}

/** Pushes a scope onto the dispatcher stack; returns an unregister function. */
export function registerScope(scope: KeyboardScope): () => void {
  _scopes.push(scope);
  return () => {
    const i = _scopes.indexOf(scope);
    if (i >= 0) {
      _scopes.splice(i, 1);
    }
  };
}

/**
 * The page scope: the outermost scope, holding the bindings of controls that are not
 * enclosed by a dialog, window or table scope.
 *
 * <p>Allocated while this module is evaluated — before any component renders and hence
 * before any other scope is created — so its id is the smallest and the dispatcher
 * walks it last. It is non-modal and always active. {@link initKeyboardDispatcher}
 * puts it on the stack.</p>
 */
const _pageScope: KeyboardScope = createScope(() => true);

/** The always-active outermost scope; see {@link _pageScope}. */
export function pageScope(): KeyboardScope {
  return _pageScope;
}

/** Adds a gesture binding to a scope; returns a remover. */
export function addBinding(scope: KeyboardScope, gesture: string, handler: GestureHandler): () => void {
  const canonical = normalizeGesture(gesture);
  let list = scope.bindings.get(canonical);
  if (!list) {
    list = [];
    scope.bindings.set(canonical, list);
  }
  list.push(handler);
  return () => {
    const arr = scope.bindings.get(canonical);
    if (!arr) {
      return;
    }
    const i = arr.lastIndexOf(handler);
    if (i >= 0) {
      arr.splice(i, 1);
    }
    if (arr.length === 0) {
      scope.bindings.delete(canonical);
    }
  };
}

// -- Gesture normalization ------------------------------------------------------

const KEY_ALIASES: Record<string, string> = {
  enter: 'Enter', return: 'Enter',
  escape: 'Escape', esc: 'Escape',
  space: 'Space', spacebar: 'Space', ' ': 'Space',
  up: 'ArrowUp', down: 'ArrowDown', left: 'ArrowLeft', right: 'ArrowRight',
  arrowup: 'ArrowUp', arrowdown: 'ArrowDown', arrowleft: 'ArrowLeft', arrowright: 'ArrowRight',
  home: 'Home', end: 'End', pageup: 'PageUp', pagedown: 'PageDown',
  delete: 'Delete', del: 'Delete', tab: 'Tab', backspace: 'Backspace',
};

function canonicalKeyName(raw: string): string {
  const alias = KEY_ALIASES[raw.toLowerCase()];
  if (alias) {
    return alias;
  }
  if (raw.length === 1) {
    return raw.toUpperCase();
  }
  return raw; // 'F2', 'Insert', ...
}

function withModifiers(key: string, ctrl: boolean, alt: boolean, shift: boolean, meta: boolean): string {
  return (ctrl ? 'Ctrl+' : '') + (alt ? 'Alt+' : '') + (shift ? 'Shift+' : '') + (meta ? 'Meta+' : '') + key;
}

/** The canonical gesture string for a keyboard event (e.g. "Ctrl+S", "Shift+ArrowDown"). */
export function eventToGesture(e: KeyboardEvent): string {
  return withModifiers(canonicalKeyName(e.key), e.ctrlKey, e.altKey, e.shiftKey, e.metaKey);
}

/** Normalizes a binding spec ("ENTER", "ESCAPE", "Ctrl+S", "Shift+ArrowDown") to canonical form. */
export function normalizeGesture(spec: string): string {
  const parts = spec.split('+').map(p => p.trim()).filter(Boolean);
  let ctrl = false, alt = false, shift = false, meta = false;
  let key = '';
  for (const p of parts) {
    switch (p.toLowerCase()) {
      case 'ctrl': case 'control': ctrl = true; break;
      case 'alt': case 'option': alt = true; break;
      case 'shift': shift = true; break;
      case 'meta': case 'cmd': case 'command': case 'win': meta = true; break;
      default: key = p;
    }
  }
  return withModifiers(canonicalKeyName(key), ctrl, alt, shift, meta);
}

// -- Focus / text-entry rules ---------------------------------------------------

const TEXT_INPUT_TYPES = new Set([
  'text', 'search', 'url', 'tel', 'email', 'password', 'number',
  'date', 'datetime-local', 'month', 'week', 'time', '',
]);

/** Whether the element is a caret-editing context (single- or multi-line). */
function isTextEntry(el: Element | null): boolean {
  if (!el) {
    return false;
  }
  const node = el as HTMLElement;
  if (node.isContentEditable) {
    return true;
  }
  if (node.tagName === 'TEXTAREA') {
    return true;
  }
  if (node.tagName === 'INPUT') {
    return TEXT_INPUT_TYPES.has((node as HTMLInputElement).type);
  }
  return false;
}

/** Whether the element is a multi-line editor where Enter must insert a newline. */
function isMultilineEntry(el: Element | null): boolean {
  if (!el) {
    return false;
  }
  const node = el as HTMLElement;
  return node.isContentEditable || node.tagName === 'TEXTAREA';
}

const NAV_KEYS = new Set(['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight', 'Home', 'End', 'PageUp', 'PageDown']);

// -- Dispatch -------------------------------------------------------------------

function isButtonLike(el: Element | null): boolean {
  if (!el) {
    return false;
  }
  const node = el as HTMLElement;
  return node.tagName === 'BUTTON' || node.getAttribute('role') === 'button';
}

function handleKeydown(e: KeyboardEvent): void {
  // The dispatcher is a FALLBACK: it runs in the bubble phase, so a focused widget's own
  // key handler (e.g. a menu/dropdown's arrow + Enter navigation) gets first dibs and, by
  // calling preventDefault(), tells the dispatcher to stand down.
  if (e.defaultPrevented) {
    return;
  }
  const active = document.activeElement;

  // Enter inside a multi-line editor inserts a newline; never treat it as a submit gesture.
  if (e.key === 'Enter' && isMultilineEntry(active)) {
    return;
  }
  // Enter on a focused button activates that button natively; don't also fire a scope default.
  if (e.key === 'Enter' && isButtonLike(active)) {
    return;
  }
  // Navigation keys / Space inside a caret editor move the caret or type; leave them alone.
  if ((NAV_KEYS.has(e.key) || e.key === ' ') && isTextEntry(active)) {
    return;
  }
  // Ctrl/Meta+A inside a caret editor selects that editor's text, whatever a scope binds it to
  // (a table binds it to "select every row").
  if ((e.ctrlKey || e.metaKey) && (e.key === 'a' || e.key === 'A') && isTextEntry(active)) {
    return;
  }

  const gesture = eventToGesture(e);
  const ordered = _scopes.slice().sort((a, b) => b.id - a.id);
  for (const scope of ordered) {
    if (!scope.isActive()) {
      continue;
    }
    const handlers = scope.bindings.get(gesture);
    if (handlers && handlers.length > 0) {
      const result = handlers[handlers.length - 1]();
      if (result !== false) {
        e.preventDefault();
        e.stopPropagation();
        return;
      }
    }
    if (scope.modal) {
      // A modal surface traps all gestures; do not let unbound keys reach the background.
      return;
    }
  }
}

/**
 * Installs the single document-level keydown listener and registers the page scope
 * (idempotent, so the page scope is registered exactly once).
 */
export function initKeyboardDispatcher(): void {
  if (_installed) {
    return;
  }
  _installed = true;
  registerScope(_pageScope);
  // Bubble phase: focused widgets handle their own keys first; we are the fallback.
  document.addEventListener('keydown', handleKeydown, false);
}
