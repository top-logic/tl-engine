import { React, useTLState, useTLCommand, useButtonDefaults, menuItemProps, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, ToggleButtonStateJson } from 'tl-react-bridge';
import ListItemText from '@mui/material/ListItemText';
import ListItemButton from '@mui/material/ListItemButton';
import ToggleButton from '@mui/material/ToggleButton';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback } = React;

/** Command a toggle button sends when it is clicked; the server flips its state. */
const CMD_CLICK = 'click';

/** An entry of a menu spans the menu. */
const MENU_ITEM_SX: SxProps<Theme> = { width: '100%' };

/**
 * Renders the state of a TopLogic toggle button (module name `TLToggleButton`) with the MUI
 * `ToggleButton`, or with the MUI `ListItemButton` inside a menu.
 *
 * <p>Mapping from the control state to the MUI props: label → children, active → `selected`
 * (rendered as `aria-pressed`), hidden → nothing is rendered, the configured CSS class → className
 * (through {@link rootClassName}). A click sends the click command; the server answers with the
 * flipped active state, so the MUI button stays controlled. MUI requires a `value`; it is the
 * control ID and carries no meaning.</p>
 *
 * <p>Inside a menu (the container's appearance `menu-item`, see {@link useButtonDefaults}) it is a
 * checkbox entry of that menu, as TLToggleButton is: a `ListItemButton` with the role
 * `menuitemcheckbox`, `aria-checked` and the roving tabindex of a menu entry
 * ({@link menuItemProps}).</p>
 *
 * <p>Not reproduced: the other appearances a container suggests (ghost in a toolbar); the MUI
 * `ToggleButton` has no variants.</p>
 */
const MuiToggleButtonAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<ToggleButtonStateJson>>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();

  const handleChange = useCallback(() => {
    sendCommand(CMD_CLICK);
  }, [sendCommand]);

  if (state.hidden === true) {
    return null;
  }

  const active = state.active === true;

  if (defaults.appearance === 'menu-item') {
    return (
      <ListItemButton
        id={controlId}
        component="button"
        type="button"
        onClick={handleChange}
        aria-checked={active}
        className={rootClassName(state)}
        sx={MENU_ITEM_SX}
        {...menuItemProps(defaults, active)}
      >
        <ListItemText>{state.label}</ListItemText>
      </ListItemButton>
    );
  }

  return (
    <ToggleButton
      id={controlId}
      value={controlId}
      selected={active}
      onChange={handleChange}
      className={rootClassName(state)}
    >
      {state.label}
    </ToggleButton>
  );
};

export default MuiToggleButtonAdapter;
