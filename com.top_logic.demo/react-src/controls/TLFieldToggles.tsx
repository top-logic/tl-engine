import { React, useTLState, useTLCommand } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A composite React control that renders toggle buttons for form field properties.
 *
 * Demonstrates how to build a multi-button control that dispatches different commands
 * from a single React component.
 */
const TLFieldToggles: React.FC = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const toggleDisabled = useCallback(() => sendCommand('toggleDisabled'), [sendCommand]);
  const toggleImmutable = useCallback(() => sendCommand('toggleImmutable'), [sendCommand]);
  const toggleMandatory = useCallback(() => sendCommand('toggleMandatory'), [sendCommand]);

  const buttonStyle = (active: boolean): React.CSSProperties => ({
    padding: '4px 8px',
    marginRight: '4px',
    cursor: 'pointer',
    fontWeight: active ? 'bold' : 'normal',
    backgroundColor: active ? '#e0e0e0' : '',
  });

  return (
    <div style={{ marginTop: '0.5em' }}>
      <button type="button" onClick={toggleDisabled} style={buttonStyle(state.disabled === true)}>
        Disabled
      </button>
      <button type="button" onClick={toggleImmutable} style={buttonStyle(state.immutable === true)}>
        Immutable
      </button>
      <button type="button" onClick={toggleMandatory} style={buttonStyle(state.mandatory === true)}>
        Mandatory
      </button>
    </div>
  );
};

export default TLFieldToggles;
