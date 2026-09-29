import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Simple read-only text control rendering a {@code <span>}.
 *
 * State:
 * - text: string - the text to display
 * - cssClass: string - optional additional CSS class appended to the default {@code tlText} class
 */
const TLText: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const text = (state.text as string) ?? '';
  const extra = (state.cssClass as string) ?? '';
  const hasTooltip = state.hasTooltip === true;
  const className = extra ? `tlText ${extra}` : 'tlText';

  return (
    <span
      id={controlId}
      className={className}
      data-tooltip={hasTooltip ? 'key:tooltip' : undefined}
    >{text}</span>
  );
};

export default TLText;
