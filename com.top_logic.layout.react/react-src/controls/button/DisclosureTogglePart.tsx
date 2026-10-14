import { React, ThemeIcon, tooltipProps, usePart } from 'tl-react-bridge';
import type { DisclosureTogglePartProps } from 'tl-react-bridge';

/** The chevron of the toggle, pointing to the end of the line; the design system turns it. */
const CHEVRON_ICON = 'css:fa-solid fa-chevron-right';

/** The class of a toggle whose content opens toward the end of the line. */
const OPENS_END_CLASS = 'tlDisclosureToggle--end';

/**
 * The TopLogic drawing of the part `DisclosureToggle` ({@link DisclosureTogglePartProps}): the
 * toggle of the design system (tl-toggle), a square ghost button with a chevron that points to the
 * end of the line and turns down while `aria-expanded`. A toggle whose content opens toward the end
 * of the line turns its chevron back to the start instead (tlDisclosureToggle--end).
 *
 * <p>While the content is being loaded, the toggle is marked `aria-busy` and shows a small part
 * `Spinner` in place of the chevron.</p>
 */
const DisclosureTogglePart: React.FC<DisclosureTogglePartProps> = ({
  id, ref, expanded, label, opens, loading, disabled, tabIndex, className, onClick, ...attributes
}) => {
  const Spinner = usePart('Spinner');
  const classes = ['tl-toggle', opens === 'end' ? OPENS_END_CLASS : '', className ?? ''].filter(Boolean).join(' ');
  return (
    <button
      type="button"
      id={id}
      ref={ref}
      className={classes}
      onClick={onClick}
      disabled={disabled}
      tabIndex={tabIndex}
      aria-expanded={expanded}
      aria-busy={loading ? true : undefined}
      aria-label={label}
      {...tooltipProps(label)}
      {...attributes}
    >
      {loading
        ? <Spinner size="small" />
        : <ThemeIcon encoded={CHEVRON_ICON} className="tl-icon-sm" />}
    </button>
  );
};

export default DisclosureTogglePart;
