import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, ToggleButtonStateJson } from 'tl-react-bridge';
import ToggleButton from '@mui/material/ToggleButton';

const { useCallback } = React;

/** Command a toggle button sends when it is clicked; the server flips its state. */
const CMD_CLICK = 'click';

/**
 * Renders the state of a TopLogic toggle button (module name `TLToggleButton`) with the MUI
 * `ToggleButton`.
 *
 * <p>Mapping from the control state to the MUI props: label → children, active → `selected`
 * (rendered as `aria-pressed`), hidden → nothing is rendered, the configured CSS class → className
 * (through {@link rootClassName}). A click sends the click command; the server answers with the
 * flipped active state, so the MUI button stays controlled. MUI requires a `value`; it is the
 * control ID and carries no meaning.</p>
 *
 * <p>Not reproduced: the checkbox entry (`menuitemcheckbox`) TLToggleButton becomes inside a menu;
 * TLToggleButton reads the menu from a container context that 'tl-react-bridge' does not
 * export.</p>
 */
const MuiToggleButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ToggleButtonStateJson>>();
  const sendCommand = useTLCommand();

  const handleChange = useCallback(() => {
    sendCommand(CMD_CLICK);
  }, [sendCommand]);

  if (state.hidden === true) {
    return null;
  }

  return (
    <ToggleButton
      id={controlId}
      value={controlId}
      selected={state.active === true}
      onChange={handleChange}
      className={rootClassName(state)}
    >
      {state.label}
    </ToggleButton>
  );
};

export default MuiToggleButtonAdapter;
