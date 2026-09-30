import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { TLPill } from './pill/TLPill';

/** Typographic role written as a class; the role the server omits. */
const DEFAULT_VARIANT = 'body';

/** Color role written as a class; the role the server omits. */
const DEFAULT_TONE = 'primary';

/**
 * The pill role a text of the given tone takes when it is drawn as a pill without a role of its own:
 * the meanings keep their name, the accent is the brand, everything else is neutral.
 */
function roleOfTone(tone: string): string {
  switch (tone) {
    case 'success': case 'warning': case 'error': return tone;
    case 'accent': return 'brand';
    default: return 'neutral';
  }
}

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
 * - appearance: "text" | "pill" - plain text, or a pill whether or not the value carries a role
 * - role: string - optional ARIA role (e.g. "alert" for a message announced when it appears)
 * - colorRole: string - optional color role the value carries in the model (neutral, brand, error,
 *   warning, success, info, category-1 … category-8); shown as a pill of that role
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const text = (state.text as string) ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = (state.role as string) || undefined;
  const colorRole = (state.colorRole as string) || undefined;
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
  const pillRole = text === '' ? undefined : pill ? colorRole ?? roleOfTone(tone) : colorRole;

  return (
    <span
      id={controlId}
      className={className}
      role={role}
      data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
    >{pillRole ? <TLPill role={pillRole}>{text}</TLPill> : text}</span>
  );
};

export default TLText;
