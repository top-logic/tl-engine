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
 * <p>Supports three rendering modes based on state:</p>
 * <ul>
 *   <li>{@code image} + {@code iconOnly} — icon-only button (label as tooltip).</li>
 *   <li>{@code image} — icon + label side by side.</li>
 *   <li>Neither — plain text button.</li>
 * </ul>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, disabled }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedDisabled = disabled ?? state.disabled === true;
  const resolvedHidden = state.hidden === true;
  const image = state.image as string | undefined;
  const iconOnly = state.iconOnly === true;
  const hiddenStyle = resolvedHidden ? { display: 'none' as const } : undefined;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  // Icon-only: image visible, label as tooltip.
  if (image && iconOnly) {
    return (
      <button
        type="button"
        id={controlId}
        onClick={handleClick}
        disabled={resolvedDisabled}
        style={hiddenStyle}
        className="tlReactButton tlReactButton--icon"
        title={resolvedLabel}
        aria-label={resolvedLabel}
      >
        <ThemeIcon encoded={image} />
      </button>
    );
  }

  // Image + label.
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

  // Plain label-only.
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
