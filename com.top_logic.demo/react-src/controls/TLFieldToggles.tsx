import {
  React,
  useTLState,
  getComponent,
  createChildContext,
  TLControlContext,
} from 'tl-react-bridge';

const { useMemo } = React;

interface ChildDescriptor {
  controlId: string;
  state: Record<string, unknown>;
}

/**
 * A composite React control that renders three {@code TLButton} sub-components
 * for toggling a form field's disabled, immutable, and mandatory properties.
 *
 * <p>Demonstrates React-level composition: each child {@code TLButton} is wrapped in its own
 * {@code TLControlContext.Provider} so that {@code useTLCommand()} inside each button routes
 * commands to the correct server-side {@code ReactButtonControl}.  The child control IDs and
 * initial states are passed from the server via this component's state.</p>
 */
const TLFieldToggles: React.FC = () => {
  const state = useTLState();
  const TLButton = getComponent('TLButton');

  if (!TLButton) {
    return React.createElement('span', null, '[TLButton not registered]');
  }

  const children = (state.children ?? []) as ChildDescriptor[];

  return (
    <div style={{ marginTop: '0.5em', display: 'flex', gap: '4px' }}>
      {children.map((child) => (
        <ChildButton
          key={child.controlId}
          childDescriptor={child}
          ButtonComponent={TLButton}
        />
      ))}
    </div>
  );
};

/**
 * Renders a single TLButton child wrapped in its own TLControlContext so that
 * commands are dispatched to the child's server-side ReactButtonControl.
 */
const ChildButton: React.FC<{
  childDescriptor: ChildDescriptor;
  ButtonComponent: React.ComponentType<any>;
}> = ({ childDescriptor, ButtonComponent }) => {
  const childCtx = useMemo(
    () => createChildContext(childDescriptor.controlId, childDescriptor.state),
    [childDescriptor.controlId]
  );

  return (
    <TLControlContext.Provider value={childCtx}>
      <ButtonComponent controlId={childDescriptor.controlId} state={childDescriptor.state} />
    </TLControlContext.Provider>
  );
};

export default TLFieldToggles;
