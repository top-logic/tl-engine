import { React, useTLState, rootClassName } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { TLPill } from './pill/TLPill';

/**
 * Simple read-only text control rendering a {@code <span>}.
 *
 * State:
 * - text: string - the text to display
 * - overflow: "wrap" | "ellipsis" - how text longer than the available width is displayed; wrapped
 *   onto several lines, or truncated on a single one
 * - role: string - optional ARIA role (e.g. "alert" for a message announced when it appears)
 * - color: string - optional CSS color the value carries in the model; shown as a pill
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const text = (state.text as string) ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = (state.role as string) || undefined;
  const color = (state.color as string) || undefined;
  const className = rootClassName(state, 'tlText', state.overflow === 'ellipsis' && 'tlText--ellipsis');

  return (
    <span
      id={controlId}
      className={rootClassName(state, className)}
      role={role}
      data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
    >{color ? <TLPill color={color}>{text}</TLPill> : text}</span>
  );
};

export default TLText;
