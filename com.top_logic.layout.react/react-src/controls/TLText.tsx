import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps, TextStateJson } from 'tl-react-bridge';
import { roleAttr } from './pill/TLPill';

/**
 * The type class of each variant (design system, base.css). Caption and label share the label size:
 * the type scale has one size below the body.
 */
const TYPE_CLASS: Record<string, string> = {
  display: 'tl-type-display',
  headline: 'tl-type-heading-lg',
  title: 'tl-type-heading-sm',
  body: 'tl-type-body',
  label: 'tl-type-label',
  caption: 'tl-type-label',
};

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
function roleOfTone(tone: string): string {
  switch (tone) {
    case 'success': case 'warning': case 'error': return tone;
    default: return 'neutral';
  }
}

/**
 * Read-only text (design system: tl-text). A `span` with the type class of its variant and its tone
 * as `data-tl-tone`; `overflow="ellipsis"` truncates it to one line (`tl-text--truncate`). Drawn as a
 * pill - `appearance="pill"` or a value with a color role - the root is the `span.tl-pill` itself,
 * carrying the role as `data-tl-role`.
 *
 * The state is described by TextStateJson. A strong text is the strong type class given as the
 * element's CSS class (`tl-type-body-strong`).
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextStateJson>>();
  const text = state.text ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = state.role || undefined;
  const colorRole = state.colorRole || undefined;
  const variant: string = state.variant || 'body';
  const tone: string = state.tone || 'primary';
  const pill = state.appearance === 'pill';
  const tooltip = hasTooltip ? 'key:tooltip' : undefined;
  // A pill is drawn around content only: a value without a label - an empty channel, a value not
  // yet chosen - shows nothing rather than an empty tinted box.
  const pillRole = text === '' ? undefined : pill ? colorRole ?? roleOfTone(tone) : colorRole;

  if (pillRole) {
    // The pill is the root itself (3b §11, 07.10.): span.tl-pill with the role as data-tl-role and
    // the attributes of the text; no tl-text around it. The pill takes the label type class from
    // outside, as a button does (pill.contract.md).
    return (
      <span
        id={controlId}
        className={rootClassName(state, 'tl-pill', 'tl-type-label')}
        data-tl-role={roleAttr(pillRole)}
        role={role}
        data-tooltip={tooltip}
      ><span className="tl-pill__label">{text}</span></span>
    );
  }

  return (
    <span
      id={controlId}
      className={rootClassName(
        state,
        'tl-text',
        TYPE_CLASS[variant] ?? TYPE_CLASS.body,
        state.overflow === 'ellipsis' && 'tl-text--truncate',
      )}
      data-tl-tone={TONE_ATTR[tone]}
      role={role}
      data-tooltip={tooltip}
    >{text}</span>
  );
};

export default TLText;
