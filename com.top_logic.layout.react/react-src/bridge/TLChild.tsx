import React, { useEffect, useMemo, useSyncExternalStore } from 'react';
import { getComponent } from './registry';
import { createChildContext, destroyChildContext, TLControlContext } from './tl-react-bridge';

/**
 * Descriptor for a server-defined child control, as sent in the parent's state.
 */
export interface ChildDescriptor {
  controlId: string;
  module: string;
  state: Record<string, unknown>;
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

  // Clean up the child context's SSE subscription when this component unmounts.
  // This is the primary cleanup path for child contexts; the DOM MutationObserver
  // intentionally skips entries with root === null to avoid interfering with React's
  // internal element-type changes (e.g. <span> ↔ <input>).
  useEffect(() => {
    const id = descriptor.controlId;
    return () => destroyChildContext(id);
  }, [descriptor.controlId]);

  // Subscribe to the child's ControlStateStore so that SSE patch events (e.g. editable
  // changing from false to true) trigger a re-render with the live state.
  const liveState = useSyncExternalStore(childCtx.store.subscribeStore, childCtx.store.getSnapshot);

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
