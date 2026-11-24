import { React, ThemeIcon, tooltipProps, useButtonDefaults } from 'tl-react-bridge';
import type { MenuTriggerPartProps } from 'tl-react-bridge';
import { buttonClassName } from './buttonClassName';

/** Icon after the label of a labeled menu trigger. */
const CHEVRON_ICON = 'css:fa-solid fa-chevron-down';

/**
 * The TopLogic drawing of the part `MenuTrigger` ({@link MenuTriggerPartProps}): a native button
 * with the classes of the design system ({@link buttonClassName}) in the appearance of the buttons
 * beside it.
 *
 * <p>An icon-only trigger is a square icon button named by its label, which is also its tooltip, unless the
 * trigger opens its surface on hover (`data-tl-trigger`): the surface then says more. Drawn small, it carries a
 * small icon. A labeled trigger shows its label followed by a small chevron. The button names the kind of surface
 * it opens in `aria-haspopup`.</p>
 */
const MenuTriggerPart: React.FC<MenuTriggerPartProps> = ({
  id, ref, appearance, expanded, haspopup = 'menu', label, icon, small, children, className, style, onClick, onMouseDown, ...attributes
}) => {
  const iconOnly = children === undefined && !!icon;
  // A trigger opening on hover shows its surface at once; a tooltip beside it would say the same twice.
  const hover = attributes['data-tl-trigger'] === 'hover';
  const defaults = useButtonDefaults();
  const smallIcon = iconOnly && (small === true || defaults.small === true);
  let content: React.ReactNode;
  if (children !== undefined) {
    content = children;
  } else if (iconOnly) {
    content = <ThemeIcon encoded={icon} className={'tl-button__icon ' + (smallIcon ? 'tl-icon-sm' : 'tl-icon-md')} />;
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
      className={buttonClassName({ appearance, small: smallIcon, icon: iconOnly, extra: className })}
      style={style}
      onClick={onClick}
      onMouseDown={onMouseDown}
      aria-expanded={expanded}
      aria-haspopup={haspopup}
      aria-label={iconOnly ? label : undefined}
      {...tooltipProps(iconOnly && !hover ? label : undefined)}
      {...attributes}
    >
      {content}
    </button>
  );
};

export default MenuTriggerPart;
