/**
 * Focus trap for modal surfaces (dialogs/windows, popup menus).
 *
 * <p>While a trap is active and topmost, keyboard focus is confined to its container: a focus
 * landing outside is pulled back in. The hook {@code useFocusTrap} additionally moves focus into
 * the container when it opens and restores it to the previously focused element when it closes.
 * Combined with the keyboard dispatcher, this ensures gestures reach the intended surface (the
 * default focus is inside the dialog, not somewhere in the background) and that focus returns to
 * its origin (e.g. a table) after the surface closes.</p>
 */

interface FocusTrap {
  readonly id: number;
  getElement(): HTMLElement | null;
  /** Where to send focus when it escapes the trap; falls back to the first focusable / container. */
  getFocusTarget?(): HTMLElement | null;
}

const _traps: FocusTrap[] = [];
let _nextId = 1;
let _installed = false;

const FOCUSABLE = [
  'a[href]',
  'button:not([disabled])',
  'input:not([disabled]):not([type=hidden])',
  'select:not([disabled])',
  'textarea:not([disabled])',
  '[tabindex]:not([tabindex="-1"])',
].join(',');

const FIELD = 'input:not([disabled]):not([type=hidden]),select:not([disabled]),textarea:not([disabled])';

/** Returns the first visible focusable (or, when {@code fieldsOnly}, the first form field) in the container. */
export function firstFocusable(container: HTMLElement, fieldsOnly = false): HTMLElement | null {
  const candidates = container.querySelectorAll<HTMLElement>(fieldsOnly ? FIELD : FOCUSABLE);
  for (const el of candidates) {
    // getClientRects() is empty for display:none / detached elements but non-empty for
    // position:fixed ones (unlike offsetParent, which is null for fixed elements).
    if (el.getClientRects().length > 0 || el === document.activeElement) {
      return el;
    }
  }
  return null;
}

function topmostTrap(): FocusTrap | null {
  let top: FocusTrap | null = null;
  for (const trap of _traps) {
    if (trap.getElement() && (!top || trap.id > top.id)) {
      top = trap;
    }
  }
  return top;
}

function onFocusIn(e: FocusEvent): void {
  const top = topmostTrap();
  if (!top) {
    return;
  }
  const el = top.getElement();
  if (!el) {
    return;
  }
  const target = e.target as Node | null;
  if (target && el.contains(target)) {
    return;
  }
  // Focus escaped the topmost trap (e.g. Tab past the edge, or another control stealing focus);
  // pull it back to the trap's preferred target (its field/container), not an incidental button.
  const back = (top.getFocusTarget && top.getFocusTarget()) ?? firstFocusable(el) ?? el;
  if (back && back !== document.activeElement) {
    back.focus();
  }
}

/** Registers a focus trap; returns an unregister function. Higher id == more recently opened == topmost. */
export function pushTrap(
  getElement: () => HTMLElement | null,
  getFocusTarget?: () => HTMLElement | null
): () => void {
  const trap: FocusTrap = { id: _nextId++, getElement, getFocusTarget };
  _traps.push(trap);
  return () => {
    const i = _traps.indexOf(trap);
    if (i >= 0) {
      _traps.splice(i, 1);
    }
  };
}

/** Installs the single document-level focusin listener (idempotent). */
export function initFocusTrap(): void {
  if (_installed) {
    return;
  }
  _installed = true;
  document.addEventListener('focusin', onFocusIn, true);
}
