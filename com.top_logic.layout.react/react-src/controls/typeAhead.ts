import { React } from 'tl-react-bridge';

const { useCallback, useRef, useEffect } = React;

/** The time in milliseconds after which a pause in typing starts a new prefix. */
const TYPE_AHEAD_RESET_MS = 500;

/**
 * Whether the given key event types a character that type-ahead takes into its prefix: a single
 * printable character typed without a modifier that makes it a shortcut.
 *
 * A space is only part of a prefix already begun; on its own it keeps its meaning as a key.
 */
export function isTypeAheadKey(e: React.KeyboardEvent, typing: boolean): boolean {
  if (e.ctrlKey || e.metaKey || e.altKey) return false;
  if (e.key.length !== 1) return false;
  return e.key !== ' ' || typing;
}

/**
 * The index of the label the given prefix moves to, or -1 if no label starts with it.
 *
 * The search starts at the given index and wraps around the end of the list; the comparison
 * ignores case. A prefix of one character typed repeatedly searches from the label after the
 * current one for that character, so that pressing the same key cycles through the labels it
 * starts.
 *
 * @param labels The labels of the options, in the order of the list.
 * @param prefix The characters typed, lower case.
 * @param current The index of the active option, -1 for none.
 */
export function findTypeAheadMatch(labels: string[], prefix: string, current: number): number {
  const count = labels.length;
  if (count === 0 || prefix.length === 0) return -1;

  const repeated = prefix.split('').every((ch) => ch === prefix[0]);
  const search = repeated ? prefix[0] : prefix;
  // A repeated character moves on; a longer prefix may still be matched by the active option.
  const start = repeated ? current + 1 : Math.max(current, 0);

  for (let n = 0; n < count; n++) {
    const index = (((start + n) % count) + count) % count;
    if (labels[index].toLowerCase().startsWith(search)) {
      return index;
    }
  }
  return -1;
}

/**
 * Type-ahead in a list of options, as WAI-ARIA describes it for a listbox.
 *
 * The characters typed accumulate into a prefix that is forgotten after a pause of
 * TYPE_AHEAD_RESET_MS; every character typed moves to the first option whose label starts with
 * the prefix (see findTypeAheadMatch).
 *
 * @returns `type`, which takes a typed character into the prefix and returns the prefix, and
 *          `typing`, which tells whether a prefix is being typed.
 */
export function useTypeAhead(): { type: (ch: string) => string; typing: () => boolean } {
  const prefixRef = useRef('');
  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(
    () => () => {
      if (timerRef.current !== null) clearTimeout(timerRef.current);
    },
    []
  );

  const type = useCallback((ch: string) => {
    prefixRef.current += ch.toLowerCase();
    if (timerRef.current !== null) clearTimeout(timerRef.current);
    timerRef.current = setTimeout(() => {
      prefixRef.current = '';
      timerRef.current = null;
    }, TYPE_AHEAD_RESET_MS);
    return prefixRef.current;
  }, []);

  const typing = useCallback(() => prefixRef.current.length > 0, []);

  return { type, typing };
}
