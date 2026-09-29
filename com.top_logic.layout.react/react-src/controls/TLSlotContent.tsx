import { React } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * Server-side contribution placeholder. Renders nothing visible: the contributed children are
 * rendered by the matched {@code <slot>} placeholder (via the server-side {@code SlotRegistry}),
 * not here. This component exists only so that the React tree has a mount-point representing
 * the contribution; its sole job on the client is to occupy a stable position in the parent's
 * children list.
 */
const TLSlotContent: React.FC<TLCellProps> = ({ controlId }) => {
  return <div id={controlId} className="tlSlotContent" style={{ display: 'none' }} />;
};

export default TLSlotContent;
