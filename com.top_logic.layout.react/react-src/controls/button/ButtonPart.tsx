import { React, ThemeIcon, buttonPartAttributes, buttonLinkAttributes } from 'tl-react-bridge';
import type { ButtonPartProps, PartRef } from 'tl-react-bridge';
import { buttonClassName } from './buttonClassName';

/**
 * The TopLogic drawing of the part `Button` ({@link ButtonPartProps}): a native button with the
 * classes of the design system ({@link buttonClassName}), or an entry of a menu (tl-menu__item)
 * for the appearance `menu-item`.
 *
 * <p>A link ({@link ButtonPartProps.href}) is an anchor with the same classes
 * ({@link buttonLinkAttributes}).</p>
 *
 * <p>A button showing its icon alone is a square icon button; drawn small, it carries a small
 * icon. Beside a label the icon is small. The label is drawn in the label element of the button or
 * of the menu entry.</p>
 */
const ButtonPart: React.FC<ButtonPartProps> = (props) => {
  const {
    id, ref, href, target: _target, label, image, display, appearance, danger, small, active, checked: _checked,
    disabled, tabIndex, tooltip: _tooltip, className, onClick, onMouseDown, ...attributes
  } = props;
  const iconOnly = display === 'icon-only';
  // A button showing no icon shows its label, also in icon-only mode.
  const showLabel = !iconOnly || !image;
  const smallIcon = small === true && iconOnly;
  // An entry of a menu is drawn by the menu, a button by the button.
  const block = appearance === 'menu-item' ? 'tl-menu' : 'tl-button';

  const classes = buttonClassName({
    appearance, danger, small: smallIcon, icon: iconOnly && !!image, current: active, extra: className,
  });
  const content = (
    <>
      {image && display !== 'label-only' && (
        <ThemeIcon encoded={image} className={block + '__icon ' + (showLabel || smallIcon ? 'tl-icon-sm' : 'tl-icon-md')} />
      )}
      {showLabel && <span className={block + '__label'}>{label}</span>}
    </>
  );

  if (href !== undefined) {
    return (
      <a
        id={id}
        ref={ref as PartRef<HTMLAnchorElement>}
        onClick={onClick}
        onMouseDown={onMouseDown}
        tabIndex={tabIndex}
        className={classes}
        {...buttonLinkAttributes(props)}
        {...buttonPartAttributes(props)}
        {...attributes}
      >
        {content}
      </a>
    );
  }

  return (
    <button
      type="button"
      id={id}
      ref={ref as PartRef<HTMLButtonElement>}
      onClick={onClick}
      onMouseDown={onMouseDown}
      disabled={disabled}
      tabIndex={tabIndex}
      className={classes}
      {...buttonPartAttributes(props)}
      {...attributes}
    >
      {content}
    </button>
  );
};

export default ButtonPart;
