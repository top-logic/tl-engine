import { React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED } from 'tl-react-bridge';
import type { TLCellProps, ButtonState } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';
import { useButtonDefaults, buttonClassName } from './button/ButtonDefaults';
import type { ButtonAppearance } from './button/ButtonDefaults';

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
  displayMode?: ButtonState.DisplayMode;
  /** Appearance; defaults to state.appearance, then the container's ButtonDefaults, then "secondary". */
  appearance?: ButtonAppearance;
  /** Destructive action; defaults to state.tone === "danger". */
  danger?: boolean;
}

/**
 * A button rendered via React that sends a command to the server.
 *
 * <p>Supports three display modes (via {@code state.displayMode} or the {@code displayMode}
 * prop):</p>
 * <ul>
 *   <li>{@code icon-only} — only the theme icon is shown, the label serves as tooltip.</li>
 *   <li>{@code icon-label} — theme icon and label side by side, the label serves as tooltip while
 *       it is clipped or hidden.</li>
 *   <li>{@code label-only} — plain text button, the label serves as tooltip while it is clipped or
 *       hidden.</li>
 * </ul>
 *
 * <p>An explicit tooltip from {@code state.tooltip} wins over all of this and is shown whatever
 * the button displays. The conditional label tooltip is declared with {@link WHEN_TRUNCATED}, so a
 * button whose label a compact toolbar drops keeps naming itself, while a button reading out its
 * label offers no tooltip that merely repeats it.</p>
 *
 * <p>The icon is supplied as a {@code ThemeImage} encoded form via {@code state.image} and
 * rendered through {@link ThemeIcon}. In {@code label-only} mode the icon is not rendered. A
 * collapsing toolbar hides the label of a button that carries an icon through its stylesheet.
 * The label always provides the accessible name via {@code aria-label} when the icon is present,
 * so hiding the label text keeps the button named for assistive technology.</p>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, image, disabled, displayMode, appearance, danger }) => {
  const state = useTLState<ButtonState>();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? state.label;
  const resolvedImage = image ?? state.image;
  const resolvedDisabled = disabled ?? state.disabled === true;
  // The button's command is the alternative currently in force (e.g. the active theme) or a
  // pressed toggle; marked visually and reported to assistive technology as pressed.
  const resolvedActive = state.active === true;
  const resolvedHidden = state.hidden === true;
  const tooltip = state.tooltip;
  const defaults = useButtonDefaults();
  const resolvedAppearance: ButtonAppearance = appearance
    ?? state.appearance
    ?? defaults.appearance ?? 'secondary';
  const resolvedDanger = danger ?? state.tone === 'danger';
  const resolvedMode = displayMode ?? state.displayMode ?? 'label-only';
  const small = state.size === 'small' && resolvedMode === 'icon-only';
  // Additional CSS classes declared on the command this button renders, e.g. to mark a
  // destructive action.
  const cssClasses = state.cssClasses;
  // When set, clicking navigates the browser directly (e.g. an external SSO redirect) instead of
  // dispatching a server command - this avoids depending on the asynchronous SSE round-trip.
  const navigateUrl = state.navigateUrl;
  // Whether that target gets a window of its own, for a destination the user comes back from while
  // this page keeps running (e.g. a re-authentication at an external provider). The window is opened
  // from the click handler, so it is a window the user asked for and not a blocked pop-up, and it is
  // a window of this page, which is what lets the page it shows close it when it is done.
  const navigateNewWindow = state.navigateNewWindow === true;

  const handleClick = useCallback(() => {
    if (navigateUrl) {
      if (navigateNewWindow) {
        // A window of this page: a page can only close a window that was opened from a page, so
        // the target closes itself once its work is done.
        window.open(navigateUrl, '_blank');
      } else {
        window.location.assign(navigateUrl);
      }
      return;
    }
    sendCommand(resolvedCommand);
  }, [sendCommand, resolvedCommand, navigateUrl, navigateNewWindow]);

  // Trigger this button when its declared keyboard gesture fires within the enclosing scope.
  // A hidden or disabled button declines (returns false) so the gesture falls through.
  const keyGesture = state.keyGesture;
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

  // An explicit tooltip is shown as it is. Otherwise the label serves as tooltip wherever the
  // button does not read it out: in icon-only mode always, and in the other modes whenever the
  // label is clipped or hidden - a toolbar drops the labels of its buttons by CSS once it runs out
  // of room, and the tooltip is what names such a button then.
  const tooltipText = tooltip ?? resolvedLabel;
  const tooltipProps: Record<string, string> = {};
  if (tooltipText) {
    tooltipProps[TOOLTIP_ATTR] = `text:${tooltipText}`;
    if (!tooltip && !iconOnly) {
      tooltipProps[TOOLTIP_WHEN_ATTR] = WHEN_TRUNCATED;
    }
  }

  // A hidden button renders nothing: its empty wrapper (e.g. a toolbar item) can then collapse,
  // so the visible buttons stay flush with the toolbar edge. The keyboard binding above stays
  // registered and declines while hidden.
  if (resolvedHidden) {
    return null;
  }

  return (
    <button
      type="button"
      id={controlId}
      onClick={handleClick}
      disabled={resolvedDisabled}
      className={rootClassName(state, buttonClassName({ appearance: resolvedAppearance, danger: resolvedDanger, small, extra: cssClasses }))}
      {...tooltipProps}
      aria-pressed={resolvedActive ? true : undefined}
      aria-label={resolvedImage || iconOnly ? resolvedLabel : undefined}
    >
      {resolvedImage && !(resolvedMode === 'label-only') && (
        <ThemeIcon encoded={resolvedImage} className={'tl-button__icon ' + (showLabel ? 'tl-icon-sm' : 'tl-icon-md')} />
      )}
      {showLabel && <span className="tl-button__label">{resolvedLabel}</span>}
    </button>
  );
};

export default TLButton;
