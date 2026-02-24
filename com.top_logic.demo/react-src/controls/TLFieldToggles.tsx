import { React, useTLState, getComponent } from 'tl-react-bridge';

/**
 * A composite React control that renders three {@code TLButton} sub-components
 * for toggling a form field's disabled, immutable, and mandatory properties.
 *
 * <p>Demonstrates how to compose existing registered components (here {@code TLButton})
 * inside a higher-level control.  Each button sends a different command
 * ({@code toggleDisabled}, {@code toggleImmutable}, {@code toggleMandatory}) to the
 * server-side {@code DemoFieldTogglesControl}, which dispatches them accordingly.</p>
 *
 * <p>Because all sub-components share the parent's {@code TLControlContext}, the
 * {@code useTLCommand()} hook inside each {@code TLButton} automatically routes
 * commands to the enclosing control.</p>
 */
const TLFieldToggles: React.FC = () => {
  const state = useTLState();
  const TLButton = getComponent('TLButton');

  if (!TLButton) {
    return React.createElement('span', null, '[TLButton not registered]');
  }

  const buttonStyle = (active: boolean): React.CSSProperties => ({
    fontWeight: active ? 'bold' : 'normal',
    backgroundColor: active ? '#e0e0e0' : '',
  });

  return (
    <div style={{ marginTop: '0.5em' }}>
      <span style={buttonStyle(state.disabled === true)}>
        <TLButton controlId="" state={state} command="toggleDisabled" label="Disabled" />
      </span>
      <span style={buttonStyle(state.immutable === true)}>
        <TLButton controlId="" state={state} command="toggleImmutable" label="Immutable" />
      </span>
      <span style={buttonStyle(state.mandatory === true)}>
        <TLButton controlId="" state={state} command="toggleMandatory" label="Mandatory" />
      </span>
    </div>
  );
};

export default TLFieldToggles;
