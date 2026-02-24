import { React, useTLState, TLChild } from 'tl-react-bridge';

/**
 * A composite React control that renders three toggle buttons for a form field's
 * disabled, immutable, and mandatory properties.
 *
 * <p>Each child button is a named slot in the server state. The generic {@code TLChild}
 * component handles all context wiring and component lookup.</p>
 */
const TLFieldToggles: React.FC = () => {
  const state = useTLState();

  return (
    <div className="tlFieldToggles">
      <TLChild control={state.disabledButton} />
      <TLChild control={state.immutableButton} />
      <TLChild control={state.mandatoryButton} />
    </div>
  );
};

export default TLFieldToggles;
