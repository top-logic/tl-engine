import { React, useTLState, useTLCommand, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, ToggleButtonState } from 'tl-react-bridge';
import { buttonClassName, useButtonDefaults } from './button/ButtonDefaults';

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
 */
const TLToggleButton: React.FC<TLCellProps & TLToggleButtonProps> = ({ controlId, command, label, active, disabled }) => {
  const state = useTLState<ToggleButtonState>();
  const sendCommand = useTLCommand();
  const defaults = useButtonDefaults();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? state.label;
  const resolvedActive = active ?? state.active === true;
  const resolvedDisabled = disabled ?? false;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      aria-pressed={resolvedActive ? true : undefined}
      className={rootClassName(state, buttonClassName({ appearance: defaults.appearance ?? 'secondary' }))}
    >
      <span className="tl-button__label">{resolvedLabel}</span>
    </button>
  );
};

export default TLToggleButton;
