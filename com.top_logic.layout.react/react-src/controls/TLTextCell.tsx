import { React, useTLState } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Simple read-only text cell for table display.
 *
 * State:
 * - text: string - the text to display
 */
const TLTextCell: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const text = (state.text as string) ?? '';

  return <span className="tlTextCell">{text}</span>;
};

export default TLTextCell;
