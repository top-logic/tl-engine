import { React, useTLState, useTLCommand, rootClassName, menuItemProps, useButtonDefaults } from 'tl-react-bridge';
import type { TLCellProps, ToggleButtonStateJson } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';

const { useCallback } = React;

/**
 * Props accepted when TLToggleButton is used as a sub-component inside a composite control.
 *
 * All props are optional -- when omitted, the corresponding value is read from the
 * control state (for standalone usage via ReactToggleButtonControl).
 */
export interface TLToggleButtonProps {
  /** The command name to send on click.  Defaults to {@code "click"}. */
  command?: string;
  /** The button label.  Defaults to {@code state.label}. */
  label?: string;
  /** Whether the button is active.  Defaults to {@code state.active}. */
  active?: boolean;
  /** Whether the button is disabled.  Defaults to {@code false}. */
  disabled?: boolean;
}

/**
 * A toggle button rendered via React that sends a command to the server.
 *
 * <p>When mounted standalone (via {@code ReactToggleButtonControl}), it reads its label
 * and active state from the control state and sends the {@code "click"} command.</p>
 *
 * <p>When composed inside another React component, the parent passes {@code command},
 * {@code label}, {@code active}, and {@code disabled} as props to customise behaviour.</p>
 *
 * <p>Inside a menu ({@code ButtonDefaults.appearance} {@code menu-item}) it is a checkbox entry of
 * that menu ({@code menuitemcheckbox} with {@code aria-checked}). It shows no icon, so a compact
 * toolbar leaves it as it is.</p>
 */
const TLToggleButton: React.FC<TLCellProps & TLToggleButtonProps> = ({ controlId, command, label, active, disabled }) => {
  const state = useTLState<Partial<ToggleButtonStateJson>>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? state.label;
  const resolvedActive = active ?? state.active === true;
  const resolvedDisabled = disabled ?? false;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  const appearance = defaults.appearance ?? 'secondary';
  const asMenuItem = appearance === 'menu-item';

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      aria-pressed={!asMenuItem && resolvedActive ? true : undefined}
      aria-checked={asMenuItem ? resolvedActive : undefined}
      {...menuItemProps(defaults, resolvedActive)}
      className={rootClassName(state, buttonClassName({ appearance }))}
    >
      <span className={asMenuItem ? 'tl-menu__label' : 'tl-button__label'}>{resolvedLabel}</span>
    </button>
  );
};

export default TLToggleButton;
