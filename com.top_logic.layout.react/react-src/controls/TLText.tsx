import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { TLPill } from './pill/TLPill';

/** Typographic role written as a class; the role the server omits. */
const DEFAULT_VARIANT = 'body';

/** Color role written as a class; the role the server omits. */
const DEFAULT_TONE = 'primary';

/**
 * Custom property the tone class fills with the color of the role, read by the stylesheet for the
 * text color and handed to a pill that has no color of its own.
 */
const TONE_COLOR = 'var(--tlText-tone)';

/**
 * Simple read-only text control rendering a {@code <span>}.
 *
 * State:
 * - text: string - the text to display
 * - overflow: "wrap" | "ellipsis" - how text longer than the available width is displayed; wrapped
 *   onto several lines, or truncated on a single one
 * - variant: what the text is for ("body" | "title" | "headline" | "display" | "label" |
 *   "caption"), written as the class tlText--<variant>
 * - tone: what its color means ("primary" | "secondary" | "helper" | "accent" | "success" |
 *   "warning" | "error" | "on-color"), written as the class tlText--tone-<tone>
 * - appearance: "text" | "pill" - plain text, or a pill whether or not the value carries a color
 * - role: string - optional ARIA role (e.g. "alert" for a message announced when it appears)
 * - color: string - optional CSS color the value carries in the model; shown as a pill
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const text = (state.text as string) ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = (state.role as string) || undefined;
  const color = (state.color as string) || undefined;
  const variant = (state.variant as string) || DEFAULT_VARIANT;
  const tone = (state.tone as string) || DEFAULT_TONE;
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
  const pillColor = text === '' ? undefined : pill ? color ?? TONE_COLOR : color;

  return (
    <span
      id={controlId}
      className={className}
      role={role}
      data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
    >{pillColor ? <TLPill color={pillColor}>{text}</TLPill> : text}</span>
  );
};

export default TLText;
