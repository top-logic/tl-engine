import { React, useTLState, useTLCommand, useKeyboardBinding } from 'tl-react-bridge';
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
 * rendered through {@link ThemeIcon}. The icon element is present in the DOM whenever an image
 * is set, independent of the display mode; in {@code label-only} mode CSS hides it. This lets
 * responsive containers (e.g. the app bar at compact viewport widths) swap a labeled button to
 * its icon presentation with a pure CSS media query. The label always provides the accessible
 * name via {@code aria-label} when the icon is present, so hiding the label text keeps the
 * button named for assistive technology.</p>
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
  // Optional appearance modifier (e.g. "link" renders the button as an inline text link).
  const appearance = state.appearance as string | undefined;
  // Optional size modifier ("small" / "large"); absent means the default size.
  const size = state.size as string | undefined;
  // When set, clicking navigates the browser directly (e.g. an external SSO redirect) instead of
  // dispatching a server command - this avoids depending on the asynchronous SSE round-trip.
  const navigateUrl = state.navigateUrl as string | undefined;

  const handleClick = useCallback(() => {
    if (navigateUrl) {
      window.location.assign(navigateUrl);
      return;
    }
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand, navigateUrl]);

  // Trigger this button when its declared keyboard gesture fires within the enclosing scope.
  // A hidden or disabled button declines (returns false) so the gesture falls through.
  const keyGesture = state.keyGesture as string | undefined;
  useKeyboardBinding(keyGesture, () => {
    if (resolvedDisabled || resolvedHidden) {
      return false;
    }
    handleClick();
    return true;
  });

  const iconOnly = resolvedMode === 'icon-only';
  // In icon-only mode without an image, the label glyph is the visible content.
  const showLabel = resolvedMode === 'label-only' || resolvedMode === 'icon-label'
    || (iconOnly && !resolvedImage);

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
      className={'tlReactButton' + (iconOnly ? ' tlReactButton--iconOnly' : '')
        + (resolvedMode === 'label-only' ? ' tlReactButton--labelOnly' : '')
        + (appearance === 'link' ? ' tlReactButton--link' : '')
        + (appearance === 'primary' ? ' tlReactButton--primary' : '')
        + (size === 'small' ? ' tlReactButton--small' : '')
        + (size === 'large' ? ' tlReactButton--large' : '')}
      data-tooltip={tooltipAttr}
      aria-label={resolvedImage || iconOnly ? resolvedLabel : undefined}
    >
      {resolvedImage && (
        <ThemeIcon encoded={resolvedImage} className="tlReactButton__image" />
      )}
      {showLabel && <span className="tlReactButton__label">{resolvedLabel}</span>}
    </button>
  );
};

export default TLButton;
