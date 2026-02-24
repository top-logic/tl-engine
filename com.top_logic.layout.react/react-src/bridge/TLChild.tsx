import React, { useMemo } from 'react';
import { getComponent } from './registry';
import { createChildContext, TLControlContext } from './tl-react-bridge';

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

  if (!Component) {
    return <span>[Component not registered: {descriptor.module}]</span>;
  }

  return (
    <TLControlContext.Provider value={childCtx}>
      <Component controlId={descriptor.controlId} state={descriptor.state} />
    </TLControlContext.Provider>
  );
};

export default TLChild;
