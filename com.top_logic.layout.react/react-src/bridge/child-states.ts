import { useCallback, useContext, useEffect, useMemo, useRef, useSyncExternalStore } from 'react';
import { acquireChildContext } from './TLChild';
import type { ChildDescriptor } from './TLChild';
import { getApiBase } from './page-context';
import { TLControlContext, unmount } from './tl-react-bridge';
import type { TLControlContextValue } from './tl-react-bridge';
import { enqueueCommand, type CommandResponse } from './command-channel';
import type { CommandOptions } from './command-channel';

/** The states of no children: one array, so that it is the same in every render. */
const NO_STATES: readonly Record<string, unknown>[] = [];

/** The contexts of no children: one array, so that it is the same in every render. */
const NO_CONTEXTS: readonly TLControlContextValue[] = [];

/**
 * Holds the stores of the given child controls while the calling component is mounted, whether or
 * not the children are rendered meanwhile.
 *
 * <p>A child's store follows the server's patches of that child as long as someone holds it. A
 * container that stops rendering its children for a while - a form showing a message instead of its
 * fields - holds them by this hook, so that a child rendered again shows its live state instead of
 * the state its descriptor carried when the container was last serialized. The stores are the ones
 * {@link TLChild} renders the children with: the stores of children already rendered are shared, the
 * others are created and subscribed.</p>
 *
 * @param controls the child controls, as the container's state holds them (child descriptors);
 *        {@code null} entries are skipped
 * @return the contexts of the children, in their order; the array keeps its identity until the set
 *         of children changes
 */
export function useChildContexts(controls: readonly unknown[]): readonly TLControlContextValue[] {
  const descriptors = (controls as (ChildDescriptor | null | undefined)[])
    .filter((d): d is ChildDescriptor => d != null && typeof d.controlId === 'string');
  const key = descriptors.map(d => d.controlId).join('\u0000');

  // Acquired as TLChild acquires a child's context, during the render, so that a child unmounted in
  // the same commit does not tear its store down before this hook holds it.
  const contexts = useMemo(
    () => descriptors.length === 0 ? NO_CONTEXTS : descriptors.map(d => acquireChildContext(d)),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [key]
  );

  useEffect(() => () => {
    for (const context of contexts) {
      unmount(context.controlId);
    }
  }, [contexts]);

  return contexts;
}

/**
 * The live states of the given child controls, in their order.
 *
 * <p>A container decides by it on what its children currently say about themselves - e.g. a
 * toolbar leaving out a menu whose entries are all {@code hidden} - without rendering them. The
 * states are the ones {@link TLChild} renders the children with: each child's store, which follows
 * the server's patches of that child. The hook holds the stores (and their SSE subscriptions) of
 * the children while it is mounted, see {@link useChildContexts}.</p>
 *
 * <p>The returned array keeps its identity until a child's state or the set of children changes.</p>
 *
 * @param controls the child controls, as the container's state holds them (child descriptors);
 *        {@code null} entries are skipped
 */
export function useChildStates(controls: readonly unknown[]): readonly Record<string, unknown>[] {
  const contexts = useChildContexts(controls);

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
 * @return Sends the given command with its arguments to the child of the given ID; the promise it
 *         returns resolves with the answer of the command, see {@link useTLCommand}.
 */
export function useChildCommand(): (
  childControlId: string,
  command: string,
  args?: Record<string, unknown>,
  options?: CommandOptions,
) => Promise<CommandResponse | undefined> {
  const ctx = useContext(TLControlContext);
  if (!ctx) {
    throw new Error('useChildCommand must be used inside a TLReact-mounted component.');
  }
  const windowName = ctx.windowName;
  return useCallback(
    (childControlId: string, command: string, args?: Record<string, unknown>, options?: CommandOptions) => {
      return enqueueCommand(getApiBase() + 'react-api/command', {
        controlId: childControlId,
        command,
        windowName,
        arguments: args ?? {},
      }, options);
    },
    [windowName]
  );
}
