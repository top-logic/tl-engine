import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

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
  /** Whether the button is disabled.  Defaults to {@code state.disabled}. */
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
const TLToggleButton: React.FC<TLCellProps & TLToggleButtonProps> = ({ command, label, active, disabled }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedActive = active ?? state.active === true;
  const resolvedDisabled = disabled ?? state.disabled === true;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  const activeStyle: React.CSSProperties = {
    padding: '4px 8px',
    marginRight: '4px',
    cursor: 'pointer',
    fontWeight: 'bold',
    backgroundColor: '#2563eb',
    borderColor: '#2563eb',
    color: '#ffffff',
    border: '1px solid #2563eb',
    borderRadius: '3px',
  };

  const inactiveStyle: React.CSSProperties = {
    padding: '4px 8px',
    marginRight: '4px',
    cursor: 'pointer',
    fontWeight: 'normal',
    backgroundColor: '#e5e7eb',
    borderColor: '#d1d5db',
    color: '#374151',
    border: '1px solid #d1d5db',
    borderRadius: '3px',
  };

  return (
    <button
      type="button"
      onClick={handleClick}
      disabled={resolvedDisabled}
      className={'tlReactButton' + (resolvedActive ? ' tlReactButtonActive' : '')}
      style={resolvedActive ? activeStyle : inactiveStyle}
    >
      {resolvedLabel}
    </button>
  );
};

export default TLToggleButton;
