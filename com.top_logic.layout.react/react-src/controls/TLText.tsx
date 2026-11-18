import { React, useTLState, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps, TextStateJson, ColorRole } from 'tl-react-bridge';

/** The type class of a typographic role (design system, base.css): `tl-type-<role>`. */
function typeClass(variant: TextStateJson.Variant): string {
  return 'tl-type-' + variant;
}

/**
 * The data-tl-tone of each tone (text.css); the primary tone carries none and inherits the color of
 * its surroundings, the text on a filled surface reads the label on the brand.
 */
const TONE_ATTR: Record<string, string | undefined> = {
  primary: undefined,
  secondary: 'secondary',
  helper: 'helper',
  placeholder: 'placeholder',
  success: 'success',
  warning: 'warning',
  error: 'error',
  'on-color': 'on-brand',
};

/**
 * The pill role a text of the given tone takes when it is drawn as a pill without a role of its own:
 * the meanings keep their name, everything else is neutral.
 */
function roleOfTone(tone: string): ColorRole {
  switch (tone) {
    case 'success': case 'warning': case 'error': return tone;
    default: return 'neutral';
  }
}

/**
 * Read-only text (design system: tl-text). A `span` with the type class of its variant and its tone
 * as `data-tl-tone`; `overflow="ellipsis"` truncates it to one line (`tl-text--truncate`). Drawn as a
 * pill - `appearance="pill"` or a value with a color role - the text is the part `Chip` of that role
 * inside a plain `span`.
 *
 * The state is described by TextStateJson. Each typographic role is a role of the type scale of
 * the design system and is drawn with its type class, a text standing out included
 * (`body-strong`, `label-strong`).
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextStateJson>>();
  const Chip = usePart('Chip');
  const text = state.text ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = state.role || undefined;
  const colorRole = state.colorRole || undefined;
  const variant: TextStateJson.Variant = state.variant || 'body';
  const tone: string = state.tone || 'primary';
  const pill = state.appearance === 'pill';
  const tooltip = hasTooltip ? 'key:tooltip' : undefined;
  // A pill is drawn around content only: a value without a label - an empty channel, a value not
  // yet chosen - shows nothing rather than an empty tinted box.
  const pillRole = text === '' ? undefined : pill ? (colorRole as ColorRole | undefined) ?? roleOfTone(tone)
    : colorRole as ColorRole | undefined;

  if (pillRole) {
    // The pill is the part Chip; no tl-text around it. The element around it carries the id, the
    // attributes of the text and its CSS classes, and sets no type: the chip takes its own.
    return (
      <span id={controlId} className={rootClassName(state) || undefined} role={role} data-tooltip={tooltip}>
        <Chip colorRole={pillRole} label={text} />
      </span>
    );
  }

  return (
    <span
      id={controlId}
      className={rootClassName(
        state,
        'tl-text',
        typeClass(variant),
        state.overflow === 'ellipsis' && 'tl-text--truncate',
      )}
      data-tl-tone={TONE_ATTR[tone]}
      role={role}
      data-tooltip={tooltip}
    >{text}</span>
  );
};

export default TLText;
