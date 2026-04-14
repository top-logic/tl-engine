import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

/**
 * A region that wraps a child control and reports DOM contextmenu events
 * to the server.
 *
 * State:
 * - child: ChildDescriptor
 */
const TLContextMenuRegion: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const child = state.child;

  const handleContextMenu = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    sendCommand('openContextMenu', { x: e.clientX, y: e.clientY });
  }, [sendCommand]);

  return (
    <div id={controlId} className="tl-context-menu-region" onContextMenu={handleContextMenu}>
      {child && <TLChild control={child} />}
    </div>
  );
};

export default TLContextMenuRegion;
