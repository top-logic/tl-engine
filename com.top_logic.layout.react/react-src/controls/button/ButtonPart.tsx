import { React, ThemeIcon, buttonPartAttributes } from 'tl-react-bridge';
import type { ButtonPartProps } from 'tl-react-bridge';
import { buttonClassName } from './buttonClassName';

/**
 * The TopLogic drawing of the part `Button` ({@link ButtonPartProps}): a native button with the
 * classes of the design system ({@link buttonClassName}), or an entry of a menu (tl-menu__item)
 * for the appearance `menu-item`.
 *
 * <p>A button showing its icon alone is a square icon button; drawn small, it carries a small
 * icon. Beside a label the icon is small. The label is drawn in the label element of the button or
 * of the menu entry.</p>
 */
const ButtonPart: React.FC<ButtonPartProps> = (props) => {
  const {
    id, ref, label, image, display, appearance, danger, small, active, checked: _checked, disabled,
    tooltip: _tooltip, className, onClick, onMouseDown, ...attributes
  } = props;
  const iconOnly = display === 'icon-only';
  // A button showing no icon shows its label, also in icon-only mode.
  const showLabel = !iconOnly || !image;
  const smallIcon = small === true && iconOnly;
  // An entry of a menu is drawn by the menu, a button by the button.
  const block = appearance === 'menu-item' ? 'tl-menu' : 'tl-button';

  return (
    <button
      type="button"
      id={id}
      ref={ref}
      onClick={onClick}
      onMouseDown={onMouseDown}
      disabled={disabled}
      className={buttonClassName({
        appearance, danger, small: smallIcon, icon: iconOnly && !!image, current: active, extra: className,
      })}
      {...buttonPartAttributes(props)}
      {...attributes}
    >
      {image && display !== 'label-only' && (
        <ThemeIcon encoded={image} className={block + '__icon ' + (showLabel || smallIcon ? 'tl-icon-sm' : 'tl-icon-md')} />
      )}
      {showLabel && <span className={block + '__label'}>{label}</span>}
    </button>
  );
};

export default ButtonPart;
