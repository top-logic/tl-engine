import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * Props accepted when TLButton is used as a sub-component inside a composite control.
 *
 * All props are optional — when omitted, the corresponding value is read from the
 * control state (for standalone usage via ReactButtonControl).
 */
export interface TLButtonProps {
  /** The command name to send on click.  Defaults to {@code "click"}. */
  command?: string;
  /** The button label.  Defaults to {@code state.label}. */
  label?: string;
  /** Whether the button is disabled.  Defaults to {@code state.disabled}. */
  disabled?: boolean;
}

/**
 * A button rendered via React that sends a command to the server.
 *
 * <p>When mounted standalone (via {@code ReactButtonControl}), it reads its label
 * and disabled state from the control state and sends the {@code "click"} command.</p>
 *
 * <p>When composed inside another React component, the parent passes {@code command},
 * {@code label}, and {@code disabled} as props to customise behaviour.</p>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, disabled }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedDisabled = disabled ?? state.disabled === true;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      className="tlReactButton"
    >
      {resolvedLabel}
    </button>
  );
};

export default TLButton;
