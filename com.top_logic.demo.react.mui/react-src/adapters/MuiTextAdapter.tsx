import { React, useTLState, rootClassName, TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TLCellProps, TextStateJson } from 'tl-react-bridge';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import type { TypographyProps } from '@mui/material/Typography';
import { roleColor } from './choice';

/** The value of {@link TOOLTIP_ATTR} fetching the rich tooltip the server holds for the control. */
const RICH_TOOLTIP = 'key:tooltip';

/**
 * The MUI typography variant of each typographic role. Running text is `body2`, the size of the
 * text in the MUI inputs and of the values a form displays.
 */
const VARIANTS: Record<TextStateJson.Variant, TypographyProps['variant']> = {
  body: 'body2',
  title: 'h6',
  headline: 'h5',
  display: 'h4',
  label: 'subtitle2',
  caption: 'caption',
};

/**
 * The color of the `Typography` for each color role: a palette key of its `color` prop (a dotted
 * palette path such as `error.main` produces no color there). A helper text is drawn like
 * secondary text; text on a filled surface states no color and takes the one of that surface.
 */
const COLORS: Record<TextStateJson.Tone, TypographyProps['color'] | undefined> = {
  primary: 'textPrimary',
  secondary: 'textSecondary',
  helper: 'textSecondary',
  accent: 'primary',
  success: 'success',
  warning: 'warning',
  error: 'error',
  'on-color': undefined,
};

/**
 * The color role a text of the given tone is drawn in as a pill without a role of its own, as
 * TLText draws it: the meanings keep their name, the accent is the brand, everything else is
 * neutral.
 */
function roleOfTone(tone: TextStateJson.Tone): string {
  switch (tone) {
    case 'success': case 'warning': case 'error': return tone;
    case 'accent': return 'brand';
    default: return 'neutral';
  }
}

/**
 * Renders the state of a TopLogic text (module name `TLText`) with the MUI `Typography`, or as a
 * small MUI `Chip` where TLText draws a pill.
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>text → the content of a `Typography` rendered as a `span`, as TLText renders one;</li>
 * <li>variant → the typography variant: body → `body2`, title → `h6`, headline → `h5`,
 *     display → `h4`, label → `subtitle2`, caption → `caption`;</li>
 * <li>tone → the `color` of the `Typography`: primary → `textPrimary`, secondary and helper →
 *     `textSecondary`, accent → `primary`, success, warning and error → the palette color of that
 *     name, on-color → none, the color of the surface the text stands on;</li>
 * <li>overflow `ellipsis` → `noWrap`;</li>
 * <li>colorRole, or appearance `pill` → a small `Chip` of the color of the role (for a pill
 *     without a role of its own, the role of its tone); an empty text is never a pill;</li>
 * <li>role → the ARIA role; hasTooltip → the rich tooltip of the control;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the root.</li>
 * </ul>
 *
 * <p>The color roles without an MUI counterpart (neutral, the categories) are drawn as the
 * default chip.</p>
 */
const MuiTextAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextStateJson>>();
  const text = state.text ?? '';
  const tone = state.tone ?? 'primary';
  const colorRole = state.colorRole || undefined;
  const pill = state.appearance === 'pill';
  const pillRole = text === '' ? undefined : pill ? colorRole ?? roleOfTone(tone) : colorRole;
  const tooltip = state.hasTooltip === true ? { [TOOLTIP_ATTR]: RICH_TOOLTIP } : {};

  if (state.hidden === true) {
    return null;
  }

  if (pillRole !== undefined) {
    return (
      <Chip id={controlId} component="span" size="small" color={roleColor(pillRole)} label={text}
        role={state.role || undefined} className={rootClassName(state)} {...tooltip} />
    );
  }

  return (
    <Typography id={controlId} component="span" variant={VARIANTS[state.variant ?? 'body']}
      color={COLORS[tone]} noWrap={state.overflow === 'ellipsis'} role={state.role || undefined}
      className={rootClassName(state)} {...tooltip}>
      {text}
    </Typography>
  );
};

export default MuiTextAdapter;
