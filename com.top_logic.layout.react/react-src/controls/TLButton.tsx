import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

const { useCallback } = React;

/**
 * Props accepted when TLButton is used as a sub-component inside a composite control.
 */
export interface TLButtonProps {
  /** The command name to send on click.  Defaults to "click". */
  command?: string;
  /** The button label.  Defaults to state.label. */
  label?: string;
  /** Encoded theme image for the button icon.  Defaults to state.image. */
  image?: string;
  /** Whether the button is disabled.  Defaults to state.disabled. */
  disabled?: boolean;
  /** Display mode.  Defaults to state.displayMode or "label-only". */
  displayMode?: 'icon-only' | 'icon-label' | 'label-only';
}

/**
 * A button rendered via React that sends a command to the server.
 *
 * <p>Supports three display modes (via {@code state.displayMode} or the {@code displayMode}
 * prop):</p>
 * <ul>
 *   <li>{@code icon-only} — only the theme icon is shown, the label serves as tooltip.</li>
 *   <li>{@code icon-label} — theme icon and label side by side.</li>
 *   <li>{@code label-only} — plain text button.</li>
 * </ul>
 *
 * <p>The icon is supplied as a {@code ThemeImage} encoded form via {@code state.image} and
 * rendered through {@link ThemeIcon}.</p>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, image, disabled, displayMode }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? (state.label as string);
  const resolvedImage = image ?? (state.image as string | undefined);
  const resolvedDisabled = disabled ?? state.disabled === true;
  const resolvedMode = displayMode ?? (state.displayMode as string | undefined) ?? 'label-only';
  const resolvedHidden = state.hidden === true;
  const tooltip = state.tooltip as string | undefined;
  const hiddenStyle = resolvedHidden ? { display: 'none' as const } : undefined;

  const handleClick = useCallback(() => {
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand]);

  const iconOnly = resolvedMode === 'icon-only';
  const showIcon = resolvedMode === 'icon-only' || resolvedMode === 'icon-label';
  const showLabel = resolvedMode === 'label-only' || resolvedMode === 'icon-label';

  // Prefer an explicit tooltip; on icon-only buttons fall back to the label so it stays
  // discoverable when the visible button carries no text.
  const tooltipText = tooltip ?? (iconOnly ? resolvedLabel : undefined);
  const tooltipAttr = tooltipText ? `text:${tooltipText}` : undefined;

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      style={hiddenStyle}
      className={'tlReactButton' + (iconOnly ? ' tlReactButton--iconOnly' : '')}
      data-tooltip={tooltipAttr}
      aria-label={iconOnly ? resolvedLabel : undefined}
    >
      {showIcon && resolvedImage && (
        <ThemeIcon encoded={resolvedImage} className="tlReactButton__image" />
      )}
      {showLabel && <span className="tlReactButton__label">{resolvedLabel}</span>}
    </button>
  );
};

export default TLButton;
