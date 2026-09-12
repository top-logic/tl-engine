import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useRef } = React;

/**
 * A region that wraps a child control and asks the server to open a menu for it.
 *
 * <p>The gesture is chosen by the server: {@code contextmenu} places the menu at the pointer,
 * {@code click} anchors it below the region so it reads as a drop-down. Both report the viewport
 * coordinates the menu is to appear at, so the server side is the same either way.</p>
 *
 * State:
 * - child: ChildDescriptor
 * - trigger: "contextmenu" | "click"
 */
const TLMenuRegion: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const regionRef = useRef<HTMLDivElement>(null);

  const child = state.child;
  const trigger = (state.trigger as string | undefined) ?? 'contextmenu';
  const isClick = trigger === 'click';

  const handleContextMenu = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    sendCommand('openMenu', { x: e.clientX, y: e.clientY });
  }, [sendCommand]);

  // A drop-down hangs off the region itself, not off the point that was clicked, so that repeated
  // openings place the menu identically however the region was hit - and so that the keyboard
  // reaches the same menu as the mouse.
  const openBelowRegion = useCallback(() => {
    const rect = regionRef.current?.getBoundingClientRect();
    sendCommand('openMenu', {
      x: Math.round(rect ? rect.left : 0),
      y: Math.round(rect ? rect.bottom : 0),
    });
  }, [sendCommand]);

  const handleClick = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    openBelowRegion();
  }, [openBelowRegion]);

  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      openBelowRegion();
    }
  }, [openBelowRegion]);

  return (
    <div
      id={controlId}
      className={'tlMenuRegion' + (isClick ? ' tlMenuRegion--click' : '')}
      ref={regionRef}
      onContextMenu={isClick ? undefined : handleContextMenu}
      onClick={isClick ? handleClick : undefined}
      role={isClick ? 'button' : undefined}
      tabIndex={isClick ? 0 : undefined}
      aria-haspopup={isClick ? 'menu' : undefined}
      onKeyDown={isClick ? handleKeyDown : undefined}
    >
      {child && <TLChild control={child} />}
    </div>
  );
};

export default TLMenuRegion;
