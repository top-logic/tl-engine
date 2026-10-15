/**
 * Where the focus goes when an item vanishes together with the button that removed it (WCAG 2.4.3):
 * a select chip, a named filter of the table. Without a hand-over the focus falls to the document
 * body, and the keyboard user starts over at the top of the page.
 */
import { useCallback, useEffect, useRef } from 'react';

/**
 * Notes a removal: the position of the removed item among the remaining targets' list as it stood,
 * and the element that triggered it (its remove button), which vanishes with the item.
 */
export type FocusAfterRemove = (index: number, removed: Element | null) => void;

/**
 * Hands the focus on once a removed item has left the page.
 *
 * <p>The removal is decided on the server, so the item vanishes with a later state, not with the
 * press. After each change of `trigger` the hook checks whether the noted element has left the
 * document; until then it keeps waiting (an unrelated state change may come first). Once it has,
 * the focus goes to the neighbour - the target now at the same position, else the one before -
 * and, without any target left, to the fallback. A focus the user has meanwhile moved to another
 * element stays where it is.</p>
 *
 * @param trigger The state the removal arrives with; the check runs whenever it changes.
 * @param targets The elements that can take the focus after a removal, in their order (the remove
 *        buttons of the remaining items), read after the removal has rendered.
 * @param fallback The element taking the focus when no target is left.
 * @returns The function noting a removal; call it in the remove button's handler.
 */
export function useFocusAfterRemove(
  trigger: unknown,
  targets: () => ArrayLike<HTMLElement> | null | undefined,
  fallback: () => HTMLElement | null | undefined,
): FocusAfterRemove {
  const pending = useRef<{ index: number; removed: Element | null } | null>(null);
  const targetsRef = useRef(targets);
  targetsRef.current = targets;
  const fallbackRef = useRef(fallback);
  fallbackRef.current = fallback;

  useEffect(() => {
    const removal = pending.current;
    if (!removal || removal.removed?.isConnected) {
      return;
    }
    pending.current = null;
    const active = document.activeElement;
    if (active && active !== document.body && active.isConnected) {
      return;
    }
    const remaining = targetsRef.current();
    if (remaining && remaining.length > 0) {
      remaining[Math.min(removal.index, remaining.length - 1)].focus();
    } else {
      fallbackRef.current()?.focus();
    }
  }, [trigger]);

  return useCallback<FocusAfterRemove>((index, removed) => {
    pending.current = { index, removed };
  }, []);
}
