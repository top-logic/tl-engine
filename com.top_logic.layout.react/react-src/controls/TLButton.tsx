import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

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
 * <p>When mounted standalone (via {@code ReactButtonControl}), it reads its label,
 * disabled/hidden state, and image from the control state.</p>
 *
 * <p>When {@code state.image} is set (a ThemeImage encoded form like "css:fas fa-edit"
 * or "/icons/foo.png"), the button renders the icon alongside the label. Without an
 * image, a plain text button is rendered.</p>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, disabled }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedDisabled = disabled ?? state.disabled === true;
  const resolvedHidden = state.hidden === true;
  const image = state.image as string | undefined;
  const hiddenStyle = resolvedHidden ? { display: 'none' as const } : undefined;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  if (image) {
    return (
      <button
        type="button"
        id={controlId}
        onClick={handleClick}
        disabled={resolvedDisabled}
        style={hiddenStyle}
        className="tlReactButton"
      >
        <ThemeIcon encoded={image} className="tlReactButton__image" />
        <span className="tlReactButton__label">{resolvedLabel}</span>
      </button>
    );
  }

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      style={hiddenStyle}
      className="tlReactButton"
    >
      {resolvedLabel}
    </button>
  );
};

export default TLButton;
