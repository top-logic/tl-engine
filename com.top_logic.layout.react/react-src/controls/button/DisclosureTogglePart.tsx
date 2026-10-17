import { React, ThemeIcon, tooltipProps, ICON_EXPAND } from 'tl-react-bridge';
import type { DisclosureTogglePartProps } from 'tl-react-bridge';

/** The class of a toggle whose content opens toward the end of the line. */
const OPENS_END_CLASS = 'tl-toggle--end';

/**
 * The TopLogic drawing of the part `DisclosureToggle` ({@link DisclosureTogglePartProps}): the
 * toggle of the design system (tl-toggle), a square ghost button with a chevron that points to the
 * end of the line and turns down while `aria-expanded`. A toggle whose content opens toward the end
 * of the line turns its chevron back to the start instead (tl-toggle--end).
 */
const DisclosureTogglePart: React.FC<DisclosureTogglePartProps> = ({
  id, ref, expanded, label, opens, disabled, tabIndex, className, onClick, onMouseDown, ...attributes
}) => {
  const classes = ['tl-toggle', opens === 'end' ? OPENS_END_CLASS : '', className ?? ''].filter(Boolean).join(' ');
  return (
    <button
      type="button"
      id={id}
      ref={ref}
      className={classes}
      onClick={onClick}
      onMouseDown={onMouseDown}
      disabled={disabled}
      tabIndex={tabIndex}
      aria-expanded={expanded}
      aria-label={label}
      {...tooltipProps(label)}
      {...attributes}
    >
      <ThemeIcon encoded={ICON_EXPAND} className="tl-icon-sm" />
    </button>
  );
};

export default DisclosureTogglePart;
