import React, { useEffect, useMemo, useSyncExternalStore } from 'react';
import { getComponent } from './registry';
import type { ChildControlJson } from '../state/control-state';
import { adoptChildDescriptor, applyChildDescriptor, createChildContext, unmount, TLControlContext } from './tl-react-bridge';
import type { TLControlContextValue } from './tl-react-bridge';

/**
 * Descriptor for a server-defined child control, as sent in the parent's state: a
 * {@link ChildControlJson}, which always carries its ID, its component and its state object.
 */
export type ChildDescriptor = Omit<Partial<ChildControlJson>, 'state'>
  & Required<Pick<ChildControlJson, 'controlId' | 'module'>>
  & { state: Record<string, unknown> };

/**
 * Acquires the context of a child control for a holder other than {@link TLChild}, e.g. a container
 * reading the live state of a child it does not render (see useChildStates). Released by
 * {@code unmount(controlId)}.
 *
 * <p>The descriptor counts as applied to the child's store: the state it carries is the one the store
 * starts with, or older than the patches the store has received since. A TLChild rendering the child
 * later therefore does not turn the child back to the state of this descriptor.</p>
 */
export function acquireChildContext(descriptor: ChildDescriptor): TLControlContextValue {
  const context = createChildContext(descriptor.controlId, descriptor.state);
  adoptChildDescriptor(context, descriptor.state);
  return context;
}

/**
 * Generic component that renders a server-described child control.
 *
 * <p>Handles component lookup, child context creation, and context wrapping so that
 * composite React components never need to touch {@code createChildContext} or
 * {@code TLControlContext.Provider} directly.</p>
 *
 * <p>The {@code control} prop accepts the raw state value (typed as {@code unknown})
 * so that callers can write {@code <TLChild control={state.foo} />} without a cast.</p>
 */
const TLChild: React.FC<{ control: unknown }> = ({ control }) => {
  const descriptor = control as ChildDescriptor;
  const Component = getComponent(descriptor.module);

  const childCtx = useMemo(
    () => createChildContext(descriptor.controlId, descriptor.state),
    [descriptor.controlId]
  );

  // Clean up SSE subscription when this child is replaced (controlId changes) or
  // when the parent unmounts.  This must NOT fire when the child component merely
  // returns null (e.g. a hidden snackbar), because TLChild itself stays mounted.
  useEffect(() => {
    return () => unmount(descriptor.controlId);
  }, [descriptor.controlId]);

  // Subscribe to the child's ControlStateStore so that SSE patch events (e.g. editable
  // changing from false to true) trigger a re-render with the live state.
  const liveState = useSyncExternalStore(childCtx.store.subscribeStore, childCtx.store.getSnapshot);

  // When the parent re-serializes this child with the same controlId but new state (e.g. a
  // toolbar re-emitting its groups after a command's executability changed), the child's
  // store was created once and would otherwise keep its mount-time snapshot. Sync the
  // server-provided state into the store so an already-mounted child (e.g. an open dropdown
  // item) reflects the update instead of waiting for a remount. Keyed on the serialized
  // state so this only fires when the embedded state actually changes — controls that update
  // solely via their own SSE patches keep a constant descriptor.state and are unaffected.
  //
  // A descriptor is applied to a store once. A parent that renders the child at another place
  // (e.g. a list switching between its edit and its display layout) remounts it with the
  // descriptor it already held; the store survives the remount (see createChildContext) and has
  // received the child's own patches since, so applying that descriptor again would turn the
  // child back to the state it had when the parent last serialized it.
  //
  // A descriptor arriving with an SSE update of the parent is applied when it arrives (see
  // registerChildDescriptors in tl-react-bridge), before the child's own patches of the same
  // batch; it counts as applied here.
  const stateKey = JSON.stringify(descriptor.state);
  useEffect(() => {
    applyChildDescriptor(childCtx, descriptor.state, stateKey);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [childCtx, stateKey]);

  // Stamp the view-boundary source path onto this control's real root element (which the
  // component renders with id={controlId}). Done imperatively rather than via a wrapper so the
  // attribute lands on a boxed element — the picker's hover-highlight reads its bounding rect —
  // without disturbing flex/grid layout. writeAsChild only sends viewSource for view boundaries.
  useEffect(() => {
    if (!descriptor.viewSource) {
      return;
    }
    const el = document.getElementById(descriptor.controlId);
    if (el) {
      el.setAttribute('data-view-source', descriptor.viewSource);
    }
  }, [descriptor.controlId, descriptor.viewSource, liveState]);

  if (!Component) {
    return <span>[Component not registered: {descriptor.module}]</span>;
  }

  return (
    <TLControlContext.Provider value={childCtx}>
      <Component controlId={descriptor.controlId} state={liveState} />
    </TLControlContext.Provider>
  );
};

export default TLChild;
