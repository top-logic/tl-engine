import { React, useTLState, useTLCommand, useKeyboardBinding, rootClassName, TOOLTIP_ATTR, TOOLTIP_WHEN_ATTR, WHEN_TRUNCATED, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, ButtonStateJson } from 'tl-react-bridge';
import { useButtonDefaults, buttonClassName, menuItemProps } from './button/ButtonDefaults';
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
  displayMode?: ButtonStateJson.DisplayMode;
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
 * compact toolbar tells its buttons through {@code ButtonDefaults.iconOnly} to present themselves
 * by their icon: a button that shows its icon then drops its label and becomes a square icon
 * button. The label always provides the accessible name via {@code aria-label} when the icon is
 * present, and the tooltip carries it, so the compact button stays named.</p>
 *
 * <p>Inside a menu ({@code ButtonDefaults.appearance} {@code menu-item}) the button is an entry
 * of that menu: it shows its label (with its icon, if it has one), takes the role
 * {@code menuitem} and joins the menu's roving tabindex. An active button - the alternative in
 * force - is marked there as the menu marks it: {@code aria-current} and strong text, instead of
 * {@code aria-pressed}.</p>
 */
const TLButton: React.FC<TLCellProps & TLButtonProps> = ({ controlId, command, label, image, disabled, displayMode, appearance, danger }) => {
  const state = useTLState<Partial<ButtonStateJson>>();
  const sendCommand = useTLCommand();

  const resolvedCommand = command ?? 'click';
  const resolvedLabel = label ?? state.label;
  const resolvedImage = image ?? state.image;
  const resolvedDisabled = disabled ?? state.disabled === true;
  // The button's command is the alternative currently in force (e.g. the active theme) or a
  // pressed toggle; marked visually and reported to assistive technology as pressed - in a menu as
  // the current entry.
  const resolvedActive = state.active === true;
  const resolvedHidden = state.hidden === true;
  const tooltip = state.tooltip;
  const defaults = useButtonDefaults();
  // The default appearance of the server is the one the container suggests.
  const serverAppearance = state.appearance === 'default' ? undefined : state.appearance;
  // Inside a menu the container wins over the server: every button there is an entry.
  const resolvedAppearance: ButtonAppearance = defaults.appearance === 'menu-item'
    ? 'menu-item'
    : (appearance ?? serverAppearance ?? defaults.appearance ?? 'secondary');
  const resolvedDanger = danger ?? state.tone === 'danger';
  const resolvedMode = displayMode ?? state.displayMode ?? 'label-only';
  // Only a button that shows its icon goes compact; inside a menu every button shows its label.
  const shows = resolvedMode !== 'label-only' && !!resolvedImage;
  const mode: ButtonStateJson.DisplayMode = defaults.iconOnly && shows
    ? 'icon-only'
    : resolvedAppearance === 'menu-item' && resolvedImage ? 'icon-label' : resolvedMode;
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

  const iconOnly = mode === 'icon-only';
  const icon = iconOnly && !!resolvedImage;
  const small = state.size === 'small' && iconOnly;
  // In icon-only mode without an image, the label glyph is the visible content.
  const showLabel = mode === 'label-only' || mode === 'icon-label'
    || (iconOnly && !resolvedImage);
  // Part classes: an entry of a menu is drawn by the menu, a button by the button.
  const asMenuItem = resolvedAppearance === 'menu-item';
  const part = asMenuItem ? 'tl-menu' : 'tl-button';

  // An explicit tooltip is shown as it is. Otherwise the label serves as tooltip wherever the
  // button does not read it out: in icon-only mode always - which is also what names the button of
  // a compact toolbar - and in the other modes whenever the label is clipped.
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
      className={rootClassName(state, buttonClassName({ appearance: resolvedAppearance, danger: resolvedDanger, small, icon, current: resolvedActive, extra: cssClasses }))}
      {...tooltipProps}
      {...menuItemProps(defaults)}
      aria-pressed={!asMenuItem && resolvedActive ? true : undefined}
      aria-current={asMenuItem && resolvedActive ? 'true' : undefined}
      aria-label={resolvedImage || iconOnly ? resolvedLabel : undefined}
    >
      {resolvedImage && mode !== 'label-only' && (
        <ThemeIcon encoded={resolvedImage} className={part + '__icon ' + (showLabel ? 'tl-icon-sm' : 'tl-icon-md')} />
      )}
      {showLabel && <span className={part + '__label'}>{resolvedLabel}</span>}
    </button>
  );
};

export default TLButton;
