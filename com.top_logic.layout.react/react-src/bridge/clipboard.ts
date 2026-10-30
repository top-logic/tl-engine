import React from 'react';

const { useCallback, useEffect, useRef, useState } = React;

/** How long a successful copy is shown, in milliseconds. */
export const COPIED_DISPLAY_MS = 1500;

/** What {@link useCopyToClipboard} returns. */
export interface CopyToClipboard {
  /** Whether the text was copied a moment ago, for {@link COPIED_DISPLAY_MS}. */
  copied: boolean;

  /**
   * Copies the text to the clipboard. Where the clipboard is unavailable or refuses the text, nothing
   * happens: no error surfaces and {@link copied} stays false, the text stays selectable.
   */
  copy: () => Promise<void>;
}

/**
 * Copies the given text to the clipboard on request and tells for a short while that it did, e.g.
 * for a button offering to copy a value (part `CopyButton`).
 *
 * @param text the text to copy
 */
export function useCopyToClipboard(text: string): CopyToClipboard {
  const [copied, setCopied] = useState(false);
  const timer = useRef<ReturnType<typeof setTimeout> | null>(null);
  const mounted = useRef(true);

  useEffect(() => {
    mounted.current = true;
    return () => {
      mounted.current = false;
      if (timer.current !== null) clearTimeout(timer.current);
    };
  }, []);

  const copy = useCallback(async () => {
    const clipboard = typeof navigator === 'undefined' ? undefined : navigator.clipboard;
    if (!clipboard || typeof clipboard.writeText !== 'function') return;
    try {
      await clipboard.writeText(text);
    } catch {
      // The clipboard may be blocked; the text stays selectable.
      return;
    }
    if (!mounted.current) return;
    setCopied(true);
    if (timer.current !== null) clearTimeout(timer.current);
    timer.current = setTimeout(() => {
      timer.current = null;
      if (mounted.current) setCopied(false);
    }, COPIED_DISPLAY_MS);
  }, [text]);

  return { copied, copy };
}
