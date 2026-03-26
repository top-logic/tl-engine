import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

const { useCallback } = React;

/** Built-in SVG icons keyed by name (used by CompositionTable detail/delete buttons). */
const ICONS: Record<string, React.FC> = {
  detail: () => (
    <svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="currentColor"
      strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="8" cy="8" r="6" />
      <line x1="8" y1="5.5" x2="8" y2="5.5" strokeWidth="2" />
      <line x1="8" y1="7.5" x2="8" y2="11" />
    </svg>
  ),
  delete: () => (
    <svg viewBox="0 0 16 16" width="16" height="16" fill="none" stroke="currentColor"
      strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      <line x1="2" y1="4" x2="14" y2="4" />
      <path d="M5.5 4V2.5h5V4" />
      <path d="M3.5 4v9.5a1 1 0 0 0 1 1h7a1 1 0 0 0 1-1V4" />
      <line x1="6.5" y1="7" x2="6.5" y2="11" />
      <line x1="9.5" y1="7" x2="9.5" y2="11" />
    </svg>
  ),
};

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
 * <p>Supports three icon modes:</p>
 * <ul>
 *   <li>{@code state.image} — ThemeImage encoded form (e.g. "css:fas fa-edit"), rendered
 *       alongside the label via {@link ThemeIcon}.</li>
 *   <li>{@code state.icon} — Built-in SVG icon name ("detail", "delete"), rendered as
 *       icon-only button with the label as accessible title.</li>
 *   <li>Neither — plain text button with label only.</li>
 * </ul>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, disabled }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedDisabled = disabled ?? state.disabled === true;
  const resolvedHidden = state.hidden === true;
  const icon = state.icon as string | undefined;
  const image = state.image as string | undefined;
  const hiddenStyle = resolvedHidden ? { display: 'none' as const } : undefined;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  // Built-in SVG icon (icon-only button, used by CompositionTable).
  const BuiltInIcon = icon ? ICONS[icon] : undefined;
  if (BuiltInIcon) {
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
        <BuiltInIcon />
      </button>
    );
  }

  // ThemeImage + label button.
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

  // Plain label-only button.
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
