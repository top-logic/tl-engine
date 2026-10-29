import { useCallback, useContext, useEffect, useMemo, useRef, useSyncExternalStore } from 'react';
import { acquireChildContext } from './TLChild';
import type { ChildDescriptor } from './TLChild';
import { getApiBase, TLControlContext, unmount } from './tl-react-bridge';
import { enqueueCommand } from './command-channel';
import type { CommandOptions } from './command-channel';

/** The states of no children: one array, so that it is the same in every render. */
const NO_STATES: readonly Record<string, unknown>[] = [];

/**
 * The live states of the given child controls, in their order.
 *
 * <p>A container decides by it on what its children currently say about themselves - e.g. a
 * toolbar leaving out a menu whose entries are all {@code hidden} - without rendering them. The
 * states are the ones {@link TLChild} renders the children with: each child's store, which follows
 * the server's patches of that child. The hook holds the stores (and their SSE subscriptions) of
 * the children while it is mounted, whether or not the children are rendered meanwhile.</p>
 *
 * <p>The returned array keeps its identity until a child's state or the set of children changes.</p>
 *
 * @param controls the child controls, as the container's state holds them (child descriptors);
 *        {@code null} entries are skipped
 */
export function useChildStates(controls: readonly unknown[]): readonly Record<string, unknown>[] {
  const descriptors = (controls as (ChildDescriptor | null | undefined)[])
    .filter((d): d is ChildDescriptor => d != null && typeof d.controlId === 'string');
  const key = descriptors.map(d => d.controlId).join('\u0000');

  // Acquired as TLChild acquires a child's context: the stores of children already rendered are
  // shared, the others are created and subscribed.
  const contexts = useMemo(
    () => descriptors.map(d => acquireChildContext(d)),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [key]
  );

  useEffect(() => () => {
    for (const context of contexts) {
      unmount(context.controlId);
    }
  }, [contexts]);

  const snapshotRef = useRef<readonly Record<string, unknown>[]>(NO_STATES);

  const subscribe = useCallback((onChange: () => void) => {
    const releases = contexts.map(context => context.store.subscribeStore(onChange));
    return () => releases.forEach(release => release());
  }, [contexts]);

  const getSnapshot = useCallback(() => {
    const previous = snapshotRef.current;
    const current = contexts.map(context => context.store.getSnapshot());
    if (current.length === previous.length && current.every((state, i) => state === previous[i])) {
      return previous;
    }
    snapshotRef.current = current.length === 0 ? NO_STATES : current;
    return snapshotRef.current;
  }, [contexts]);

  return useSyncExternalStore(subscribe, getSnapshot);
}

/**
 * Sends commands to child controls on their behalf: to a child a container draws itself instead of
 * rendering it with {@link TLChild}, e.g. an action a component library draws as a button of its
 * own, pressed by sending {@code click} to the button control.
 *
 * <p>A child rendered with {@link TLChild} sends its commands itself (useTLCommand); this hook is for
 * the container that reads the child's state with {@link useChildStates} and draws it. The commands
 * go through the same channel as the child's own.</p>
 *
 * @return Sends the given command with its arguments to the child of the given ID.
 */
export function useChildCommand(): (
  childControlId: string,
  command: string,
  args?: Record<string, unknown>,
  options?: CommandOptions,
) => Promise<void> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useChildCommand must be used inside a TLReact-mounted component.');
  }
  const windowName = ctx.windowName;
  return useCallback(
    async (childControlId: string, command: string, args?: Record<string, unknown>, options?: CommandOptions) => {
      await enqueueCommand(getApiBase() + 'react-api/command', {
        controlId: childControlId,
        command,
        windowName,
        arguments: args ?? {},
      }, options);
    },
    [windowName]
  );
}
