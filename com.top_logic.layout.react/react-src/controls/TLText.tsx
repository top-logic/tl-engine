import { React, useTLState, rootClassName, usePart } from 'tl-react-bridge';
import type { TLCellProps, TextStateJson, ChipTone } from 'tl-react-bridge';

/** Typographic role written as a class; the role the server omits. */
const DEFAULT_VARIANT = 'body';

/** Color role written as a class; the role the server omits. */
const DEFAULT_TONE = 'primary';

/**
 * The pill role a text of the given tone takes when it is drawn as a pill without a role of its own:
 * the meanings keep their name, the accent is the brand, everything else is neutral.
 */
function roleOfTone(tone: string): ChipTone {
  switch (tone) {
    case 'success': case 'warning': case 'error': return tone;
    case 'accent': return 'brand';
    default: return 'neutral';
  }
}

/**
 * Simple read-only text control rendering a {@code <span>}.
 *
 * The state is described by TextStateJson. The variant and the tone are written as the classes
 * tlText--<variant> and tlText--tone-<tone>. A text the model gives a color role, or one drawn as a
 * pill, is the part `Chip` of that role inside the span.
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TextStateJson>>();
  const Chip = usePart('Chip');
  const text = state.text ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = state.role || undefined;
  const colorRole = state.colorRole || undefined;
  const variant = state.variant || DEFAULT_VARIANT;
  const tone = state.tone || DEFAULT_TONE;
  const pill = state.appearance === 'pill';
  const className = rootClassName(
    state,
    'tlText',
    'tlText--' + variant,
    'tlText--tone-' + tone,
    pill && 'tlText--pill',
    state.overflow === 'ellipsis' && 'tlText--ellipsis',
  );
  // A pill is drawn around content only: a value without a label - an empty channel, a value not
  // yet chosen - shows nothing rather than an empty tinted box.
  const pillRole = text === '' ? undefined : pill ? (colorRole as ChipTone | undefined) ?? roleOfTone(tone)
    : colorRole as ChipTone | undefined;

  return (
    <span
      id={controlId}
      className={className}
      role={role}
      data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
    >{pillRole ? <Chip tone={pillRole} label={text} /> : text}</span>
  );
};

export default TLText;
