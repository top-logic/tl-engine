import React from 'react';

const { useCallback, useEffect, useRef, useState } = React;

/** How long the outcome of a copy is shown, in milliseconds. */
export const COPIED_DISPLAY_MS = 1500;

/** The outcome of the last copy, shown for {@link COPIED_DISPLAY_MS}: none, success or failure. */
export type CopyOutcome = 'idle' | 'copied' | 'failed';

/** What {@link useCopyToClipboard} returns. */
export interface CopyToClipboard {
  /** Whether the text was copied a moment ago, for {@link COPIED_DISPLAY_MS}. */
  copied: boolean;

  /**
   * Whether copying failed a moment ago, for {@link COPIED_DISPLAY_MS}: the page may write neither
   * through the clipboard API nor through the copy command of the document. The text stays
   * selectable, so the user can copy it by hand.
   */
  failed: boolean;

  /**
   * Copies the text to the clipboard. Called from the press of a button: where the asynchronous
   * clipboard API is unavailable (a page that is no secure context), the text is copied at once
   * through the copy command of the document, which needs the activation by the user.
   *
   * @param origin the element pressed, e.g. the `currentTarget` of the click. The element holding
   *        the text for the copy command is placed beside it, inside a dialog trapping the focus.
   */
  copy: (origin?: Element | null) => Promise<void>;
}

/**
 * Copies the text through the copy command of the document: selects it in a hidden text area placed
 * beside the given element, runs `copy` and restores the focus and the selection of before.
 *
 * @return whether the document reports that it copied the text
 */
export function copyByCommand(text: string, origin?: Element | null): boolean {
  if (typeof document === 'undefined' || typeof document.execCommand !== 'function') return false;
  const doc = origin?.ownerDocument ?? document;
  const container = origin?.parentElement ?? doc.body;
  const focused = doc.activeElement instanceof HTMLElement ? doc.activeElement : null;
  const selection = doc.getSelection();
  const ranges: Range[] = [];
  if (selection) {
    for (let i = 0; i < selection.rangeCount; i++) ranges.push(selection.getRangeAt(i));
  }

  const area = doc.createElement('textarea');
  area.value = text;
  area.readOnly = true;
  area.setAttribute('aria-hidden', 'true');
  area.tabIndex = -1;
  // Off the screen, without a size of its own, so that the page does not move.
  area.style.position = 'fixed';
  area.style.top = '0';
  area.style.left = '-10000px';
  area.style.opacity = '0';
  container.appendChild(area);
  let done = false;
  try {
    area.focus({ preventScroll: true });
    area.select();
    area.setSelectionRange(0, text.length);
    done = doc.execCommand('copy');
  } catch {
    done = false;
  } finally {
    area.remove();
    focused?.focus({ preventScroll: true });
    if (selection) {
      selection.removeAllRanges();
      ranges.forEach(range => selection.addRange(range));
    }
  }
  return done;
}

/**
 * Copies the given text to the clipboard on request and tells for a short while whether it did, e.g.
 * for a button offering to copy a value (part `CopyButton`).
 *
 * <p>In a secure context the text is written through `navigator.clipboard`; where that API is
 * missing or refuses the text, through the copy command of the document (see
 * {@link copyByCommand}). If both fail, the hook reports {@link CopyToClipboard.failed}.</p>
 *
 * @param text the text to copy
 */
export function useCopyToClipboard(text: string): CopyToClipboard {
  const [outcome, setOutcome] = useState<CopyOutcome>('idle');
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const mounted = useRef(true);

  useEffect(() => {
    mounted.current = true;
    return () => {
      mounted.current = false;
      if (timer.current !== null) clearTimeout(timer.current);
    };
  }, []);

  const show = useCallback((shown: CopyOutcome) => {
    if (!mounted.current) return;
    setOutcome(shown);
    if (timer.current !== null) clearTimeout(timer.current);
    timer.current = setTimeout(() => {
      timer.current = null;
      if (mounted.current) setOutcome('idle');
    }, COPIED_DISPLAY_MS);
  }, []);

  const copy = useCallback(async (origin?: Element | null) => {
    const clipboard = typeof window !== 'undefined' && window.isSecureContext && typeof navigator !== 'undefined'
      ? navigator.clipboard
      : undefined;
    if (!clipboard || typeof clipboard.writeText !== 'function') {
      // Synchronously within the press: the copy command needs the activation by the user.
      show(copyByCommand(text, origin) ? 'copied' : 'failed');
      return;
    }
    try {
      await clipboard.writeText(text);
      show('copied');
    } catch {
      show(copyByCommand(text, origin) ? 'copied' : 'failed');
    }
  }, [text, show]);

  return { copied: outcome === 'copied', failed: outcome === 'failed', copy };
}
