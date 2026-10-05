import { React, useTLState, useTLCommand, TLChild, pressClosedSurface, rootClassName, useButtonDefaults } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { buttonClassName } from './button/buttonClassName';

const { useCallback, useRef } = React;

/**
 * A region that wraps a child control and asks the server to open a menu for it.
 *
 * <p>The gesture is chosen by the server. {@code click} makes the region a drop-down trigger: a
 * native button (tl-button, ghost unless its container says otherwise) whose menu hangs off the
 * button itself and which reports whether that menu is open ({@code aria-expanded}). Enter and
 * Space reach it as the button's native click. {@code contextmenu} leaves the region a
 * pass-through without a box of its own: the menu stands at the pointer for the right mouse
 * button, and below the focused element for the context menu key and Shift+F10.</p>
 *
 * State:
 * - child: ChildDescriptor
 * - trigger: "contextmenu" | "click"
 * - menuOpen: boolean, whether the menu this region opened is open
 */
const TLMenuRegion: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const buttonRef = useRef<HTMLButtonElement>(null);
  const regionRef = useRef<HTMLDivElement>(null);
  const defaults = useButtonDefaults();

  const child = state.child;
  const trigger = (state.trigger as string | undefined) ?? 'contextmenu';
  const isClick = trigger === 'click';
  const menuOpen = state.menuOpen === true;

  // A drop-down hangs off the trigger itself, not off the point that was clicked: the menu stays
  // with the trigger when the page scrolls or reflows, and the keyboard reaches the same menu as
  // the mouse.
  const openAtRegion = useCallback(() => sendCommand('openMenu', { anchorId: controlId }), [sendCommand, controlId]);

  const openAtTarget = useCallback((target: Element) => {
    const r = target.getBoundingClientRect();
    sendCommand('openMenu', { x: Math.round(r.left), y: Math.round(r.bottom) });
  }, [sendCommand]);

  const handleContextMenu = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    sendCommand('openMenu', { x: e.clientX, y: e.clientY });
  }, [sendCommand]);

  const handleClick = useCallback((e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    // The press of this click has closed the menu this region opens: leave it closed.
    if (pressClosedSurface()) {
      return;
    }
    openAtRegion();
  }, [openAtRegion]);

  const handleContextKey = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'ContextMenu' || (e.key === 'F10' && e.shiftKey)) {
      e.preventDefault();
      e.stopPropagation();
      openAtTarget(e.target as Element);
    }
  }, [openAtTarget]);

  return isClick
    ? (
      <button
        type="button"
        id={controlId}
        ref={buttonRef}
        data-tl-trigger="click"
        className={rootClassName(state, 'tl-menu-region', buttonClassName({ appearance: defaults.appearance ?? 'ghost' }))}
        aria-haspopup="menu"
        aria-expanded={menuOpen}
        onClick={handleClick}
      >
        {!!child && <TLChild control={child} />}
      </button>
    )
    : (
      <div
        id={controlId}
        ref={regionRef}
        data-tl-trigger="contextmenu"
        className={rootClassName(state, 'tl-menu-region')}
        onContextMenu={handleContextMenu}
        onKeyDown={handleContextKey}
      >
        {!!child && <TLChild control={child} />}
      </div>
    );
};

export default TLMenuRegion;
