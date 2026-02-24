import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { ChildDescriptor } from 'tl-react-bridge';

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
    <div style={{ marginTop: '0.5em', display: 'flex', gap: '4px' }}>
      <TLChild descriptor={state.disabledButton as ChildDescriptor} />
      <TLChild descriptor={state.immutableButton as ChildDescriptor} />
      <TLChild descriptor={state.mandatoryButton as ChildDescriptor} />
    </div>
  );
};

export default TLFieldToggles;
