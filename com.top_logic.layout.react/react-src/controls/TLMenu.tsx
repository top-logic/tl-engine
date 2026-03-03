import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

interface MenuItem {
  id: string;
  label: string;
  icon?: string;
  disabled?: boolean;
  type: 'item' | 'separator';
}

/**
 * A popup menu triggered by an anchor element.
 *
 * State:
 * - open: boolean
 * - anchorId: string
 * - items: MenuItem[]
 */
const TLMenu: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const open = state.open === true;
  const anchorId = state.anchorId as string;
  const items = (state.items as MenuItem[]) ?? [];

  const menuRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState<{ top: number; left: number }>({ top: 0, left: 0 });
  const [focusedIndex, setFocusedIndex] = useState(0);

  const focusableItems = items.filter(it => it.type === 'item' && !it.disabled);

  // Position relative to anchor.
  useEffect(() => {
    if (!open || !anchorId) return;
    const anchor = document.getElementById(anchorId);
    if (!anchor) return;
    const rect = anchor.getBoundingClientRect();
    const menuHeight = menuRef.current?.offsetHeight ?? 200;
    const menuWidth = menuRef.current?.offsetWidth ?? 200;

    let top = rect.bottom + 4;
    let left = rect.left;

    // Flip vertically if near bottom.
    if (top + menuHeight > window.innerHeight) {
      top = rect.top - menuHeight - 4;
    }
    // Flip horizontally if near right edge.
    if (left + menuWidth > window.innerWidth) {
      left = rect.right - menuWidth;
    }

    setPosition({ top, left });
    setFocusedIndex(0);
  }, [open, anchorId]);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleSelect = useCallback((itemId: string) => {
    sendCommand('selectItem', { itemId });
  }, [sendCommand]);

  // Close on outside click.
  useEffect(() => {
    if (!open) return;
    const handleMouseDown = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        handleClose();
      }
    };
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [open, handleClose]);

  // Keyboard navigation.
  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Escape') { handleClose(); return; }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setFocusedIndex(i => (i + 1) % focusableItems.length);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setFocusedIndex(i => (i - 1 + focusableItems.length) % focusableItems.length);
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      const item = focusableItems[focusedIndex];
      if (item) handleSelect(item.id);
    }
  }, [handleClose, handleSelect, focusableItems, focusedIndex]);

  // Focus menu on open.
  useEffect(() => {
    if (open && menuRef.current) {
      menuRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  return (
    <div
      id={controlId}
      className="tlMenu"
      role="menu"
      ref={menuRef}
      tabIndex={-1}
      style={{ position: 'fixed', top: position.top, left: position.left }}
      onKeyDown={handleKeyDown}
    >
      {items.map((item, index) => {
        if (item.type === 'separator') {
          return <hr key={index} className="tlMenu__separator" />;
        }
        const focusIdx = focusableItems.indexOf(item);
        const isFocused = focusIdx === focusedIndex;
        return (
          <button
            key={item.id}
            type="button"
            className={'tlMenu__item' + (isFocused ? ' tlMenu__item--focused' : '') +
              (item.disabled ? ' tlMenu__item--disabled' : '')}
            role="menuitem"
            disabled={item.disabled}
            tabIndex={isFocused ? 0 : -1}
            onClick={() => handleSelect(item.id)}
          >
            {item.icon && <i className={'tlMenu__icon ' + item.icon} aria-hidden="true" />}
            <span className="tlMenu__label">{item.label}</span>
          </button>
        );
      })}
    </div>
  );
};

export default TLMenu;
