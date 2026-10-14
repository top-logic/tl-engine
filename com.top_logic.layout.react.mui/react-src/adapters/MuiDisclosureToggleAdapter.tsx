import { React, tooltipProps, usePart } from 'tl-react-bridge';
import type { DisclosureTogglePartProps, DisclosureDirection } from 'tl-react-bridge';
import IconButton from '@mui/material/IconButton';
import SvgIcon from '@mui/material/SvgIcon';
import type { SxProps, Theme } from '@mui/material/styles';

/** The Material icon `ChevronRight`: points to the end of the line while the content is hidden. */
const CHEVRON_PATH = 'M10 6 8.59 7.41 13.17 12l-4.58 4.59L10 18l6-6z';

/** The angle the chevron is turned by while the content is shown, by direction of the content. */
const EXPANDED_ROTATION: Record<DisclosureDirection, string> = {
  below: 'rotate(90deg)',
  end: 'rotate(180deg)',
};

/** The chevron of the toggle, turned by the given transform. */
function chevronSx(transform: string | undefined): SxProps<Theme> {
  return {
    transform,
    transition: theme => theme.transitions.create('transform', { duration: theme.transitions.duration.shortest }),
  };
}

/**
 * Draws the part `DisclosureToggle` ({@link DisclosureTogglePartProps}) with Material UI: a small
 * `IconButton` with the chevron of the Material icons, which points to the end of the line while
 * the content is hidden and turns toward the content while it is shown - down for content opening
 * below, back to the start for content opening toward the end of the line.
 *
 * <p>The button is named by the label, which is also its tooltip, and carries `aria-expanded`.
 * While the content is being loaded, it is marked `aria-busy` and shows a small part `Spinner` in
 * place of the chevron.</p>
 */
const MuiDisclosureToggleAdapter: React.FC<DisclosureTogglePartProps> = ({
  id, ref, expanded, label, opens, loading, disabled, tabIndex, className, onClick, ...attributes
}) => {
  const Spinner = usePart('Spinner');
  return (
    <IconButton
      id={id}
      ref={ref}
      size="small"
      className={className}
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
        : (
          <SvgIcon fontSize="small" sx={chevronSx(expanded ? EXPANDED_ROTATION[opens ?? 'below'] : undefined)}>
            <path d={CHEVRON_PATH} />
          </SvgIcon>
        )}
    </IconButton>
  );
};

export default MuiDisclosureToggleAdapter;
