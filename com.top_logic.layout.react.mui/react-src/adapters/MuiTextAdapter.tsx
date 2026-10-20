import { React, useTLState, rootClassName, usePart, TOOLTIP_ATTR } from 'tl-react-bridge';
import type { TLCellProps, TextStateJson, ColorRole } from 'tl-react-bridge';
import Typography from '@mui/material/Typography';
import type { TypographyProps } from '@mui/material/Typography';
import type { SxProps, Theme } from '@mui/material/styles';

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
 * secondary text, a placeholder like disabled text. Text on a filled surface has no palette key, see {@link ON_COLOR_SX}.
 */
const COLORS: Record<TextStateJson.Tone, TypographyProps['color'] | undefined> = {
  primary: 'textPrimary',
  secondary: 'textSecondary',
  helper: 'textSecondary',
  placeholder: 'textDisabled',
  success: 'success',
  warning: 'warning',
  error: 'error',
  'on-color': undefined,
};

/**
 * The color of text on a filled surface: the role of the design system TLText draws it in
 * (`text-on-brand`). The surface is the application's, not one the text knows.
 */
const ON_COLOR_SX: SxProps<Theme> = { color: 'var(--tl-text-on-brand)' };

/**
 * The color role a text of the given tone is drawn in as a pill without a role of its own, as
 * TLText draws it: the meanings keep their name, everything else is neutral.
 */
function roleOfTone(tone: TextStateJson.Tone): ColorRole {
  switch (tone) {
    case 'success': case 'warning': case 'error': return tone;
    default: return 'neutral';
  }
}

/**
 * Renders the state of a TopLogic text (module name `TLText`) with the MUI `Typography`, or as the
 * part `Chip` where TLText draws a pill.
 *
 * <p>Mapping from the control state:</p>
 * <ul>
 * <li>text → the content of a `Typography` rendered as a `span`, as TLText renders one;</li>
 * <li>variant → the typography variant: body → `body2`, title → `h6`, headline → `h5`,
 *     display → `h4`, label → `subtitle2`, caption → `caption`;</li>
 * <li>tone → the `color` of the `Typography`: primary → `textPrimary`, secondary and helper →
 *     `textSecondary`, placeholder → `textDisabled`, success, warning and error → the palette color of that
 *     name, on-color → the role for text on a filled surface (`text-on-brand`, as in TLText);</li>
 * <li>overflow `ellipsis` → `noWrap`;</li>
 * <li>colorRole, or appearance `pill` → the part `Chip` of the role (for a pill without a role of
 *     its own, the role of its tone) inside a `span`, as TLText draws it; an empty text is never a
 *     pill;</li>
 * <li>role → the ARIA role; hasTooltip → the rich tooltip of the control;</li>
 * <li>hidden → nothing is rendered; the configured CSS class → className of the root.</li>
 * </ul>
 */
const MuiTextAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextStateJson>>();
  const Chip = usePart('Chip');
  const text = state.text ?? '';
  const tone = state.tone ?? 'primary';
  const colorRole = state.colorRole || undefined;
  const pill = state.appearance === 'pill';
  const pillRole = text === '' ? undefined : pill ? (colorRole as ColorRole | undefined) ?? roleOfTone(tone)
    : colorRole as ColorRole | undefined;
  const tooltip = state.hasTooltip === true ? { [TOOLTIP_ATTR]: RICH_TOOLTIP } : {};

  if (state.hidden === true) {
    return null;
  }

  if (pillRole !== undefined) {
    return (
      <span id={controlId} role={state.role || undefined} className={rootClassName(state)} {...tooltip}>
        <Chip colorRole={pillRole} label={text} />
      </span>
    );
  }

  return (
    <Typography id={controlId} component="span" variant={VARIANTS[state.variant ?? 'body']}
      color={COLORS[tone]} sx={tone === 'on-color' ? ON_COLOR_SX : undefined}
      noWrap={state.overflow === 'ellipsis'} role={state.role || undefined}
      className={rootClassName(state)} {...tooltip}>
      {text}
    </Typography>
  );
};

export default MuiTextAdapter;
