import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { TLPill } from './pill/TLPill';

/**
 * Simple read-only text control rendering a {@code <span>}.
 *
 * State:
 * - text: string - the text to display
 * - cssClass: string - optional additional CSS class appended to the default {@code tlText} class
 * - role: string - optional ARIA role (e.g. "alert" for a message announced when it appears)
 * - color: string - optional CSS color the value carries in the model; shown as a pill
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const text = (state.text as string) ?? '';
  const extra = (state.cssClass as string) ?? '';
  const hasTooltip = state.hasTooltip === true;
  const role = (state.role as string) || undefined;
  const color = (state.color as string) || undefined;
  const className = extra ? `tlText ${extra}` : 'tlText';

  return (
    <span
      id={controlId}
      className={className}
      role={role}
      data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
    >{color ? <TLPill color={color}>{text}</TLPill> : text}</span>
  );
};

export default TLText;
