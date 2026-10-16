import { React, ThemeIcon, tooltipProps } from 'tl-react-bridge';
import type { MenuTriggerPartProps } from 'tl-react-bridge';
import { buttonClassName } from './buttonClassName';

/** Icon after the label of a labeled menu trigger. */
const CHEVRON_ICON = 'css:fa-solid fa-chevron-down';

/**
 * The TopLogic drawing of the part `MenuTrigger` ({@link MenuTriggerPartProps}): a native button
 * with the classes of the design system ({@link buttonClassName}) in the appearance of the buttons
 * beside it.
 *
 * <p>An icon-only trigger is a square icon button named by its label, which is also its tooltip. A
 * labeled trigger shows its label followed by a small chevron.</p>
 */
const MenuTriggerPart: React.FC<MenuTriggerPartProps> = ({
  id, ref, appearance, expanded, label, icon, children, className, style, onClick, onMouseDown, ...attributes
}) => {
  const iconOnly = children === undefined && !!icon;
  let content: React.ReactNode;
  if (children !== undefined) {
    content = children;
  } else if (iconOnly) {
    content = <ThemeIcon encoded={icon} className="tl-button__icon tl-icon-md" />;
  } else {
    content = (
      <>
        <span className="tl-button__label">{label}</span>
        <ThemeIcon encoded={CHEVRON_ICON} className="tl-button__icon tl-icon-sm" />
      </>
    );
  }

  return (
    <button
      type="button"
      id={id}
      ref={ref}
      className={buttonClassName({ appearance, icon: iconOnly, extra: className })}
      style={style}
      onClick={onClick}
      onMouseDown={onMouseDown}
      aria-expanded={expanded}
      aria-haspopup="menu"
      aria-label={iconOnly ? label : undefined}
      {...tooltipProps(iconOnly ? label : undefined)}
      {...attributes}
    >
      {content}
    </button>
  );
};

export default MenuTriggerPart;
