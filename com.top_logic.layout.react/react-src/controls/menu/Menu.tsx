import { React, useCloseOnOutsidePress, useFocusTrap, createPortal, usePopover, useMergeRefs, ThemeIcon, anchoredOverlayProps } from 'tl-react-bridge';
import type { PopoverAnchor, PopoverOptions } from 'tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

const ITEM = '[role="menuitem"]:not([disabled]), [role="menuitemcheckbox"]:not([disabled])';

/**
 * Roving tabindex over the entries of a menu: exactly one entry is in the tab order, the one that
 * has focus; arrows cycle, Home/End jump, Escape closes; disabled entries are skipped. Shared by
 * Menu and the toolbar overflow.
 */
export function useRovingMenu(ref: React.RefObject<HTMLElement | null>, open: boolean, onClose: () => void): { onKeyDown: (e: React.KeyboardEvent) => void } {
  const items = useCallback(() => Array.from(ref.current?.querySelectorAll<HTMLElement>(ITEM) ?? []), [ref]);

  useEffect(() => {
    if (!open) return;
    items().forEach((el, i) => el.setAttribute('tabindex', i === 0 ? '0' : '-1'));
  }, [open, items]);

  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    const all = items();
    if (all.length === 0) return;
    const current = all.indexOf(document.activeElement as HTMLElement);
    let next = -1;
    if (e.key === 'ArrowDown') next = (current + 1) % all.length;
    else if (e.key === 'ArrowUp') next = (current - 1 + all.length) % all.length;
    else if (e.key === 'Home') next = 0;
    else if (e.key === 'End') next = all.length - 1;
    else if (e.key === 'Escape') { e.preventDefault(); onClose(); return; }
    if (next < 0) return;
    e.preventDefault();
    all.forEach((el, i) => el.setAttribute('tabindex', i === next ? '0' : '-1'));
    all[next].focus();
  }, [items, onClose]);

  return { onKeyDown };
}

export interface MenuProps {
  open: boolean;
  anchor: PopoverAnchor | null;
  onClose: () => void;
  placement?: PopoverOptions['placement'];
  id?: string;
  className?: string;
  children: React.ReactNode;
}

/**
 * The one menu: a tl-popover with entries, portaled to the body, placed by usePopover, closed on
 * Escape and on a press outside. Mounted only while open - a caller that needs its entries to stay
 * live (the toolbar overflow) keeps them mounted itself and uses useRovingMenu directly.
 */
export const Menu: React.FC<MenuProps> = ({ open, anchor, onClose, placement, id, className, children }) => {
  const ref = useRef<HTMLDivElement | null>(null);
  const { setFloating, style } = usePopover({ open, anchor, placement });
  const setRefs = useMergeRefs<HTMLDivElement>([ref, setFloating]);
  useCloseOnOutsidePress(open, [ref], onClose);
  useFocusTrap(open, ref, 'first');
  const { onKeyDown } = useRovingMenu(ref, open, onClose);

  if (!open) return null;
  return createPortal(
    <div
      id={id}
      ref={setRefs}
      className={'tl-popover tl-menu' + (className ? ' ' + className : '')}
      role="menu"
      style={style}
      onKeyDown={onKeyDown}
      {...anchoredOverlayProps}
    >
      {children}
    </div>,
    document.body,
  );
};

/**
 * The class list of a menu entry: block, typography - an entry in force (`current`, reported as
 * `aria-current`) reads strong -, passed-through classes. Shared by MenuItem and every button that
 * renders as an entry (ButtonDefaults `menu-item`), so the two cannot drift apart.
 */
export function menuItemClassName(current?: boolean, extra?: string): string {
  return ['tl-menu__item', current ? 'tl-type-body-strong' : 'tl-type-body', extra ?? ''].filter(Boolean).join(' ');
}

export const MenuItem: React.FC<{ id?: string; icon?: string; label: string; disabled?: boolean; current?: boolean; className?: string; onSelect: () => void }> =
  ({ id, icon, label, disabled, current, className, onSelect }) => (
    <button
      type="button"
      id={id}
      className={menuItemClassName(current, className)}
      role="menuitem"
      aria-current={current ? 'true' : undefined}
      disabled={disabled}
      tabIndex={-1}
      onClick={onSelect}
    >
      {icon && <ThemeIcon encoded={icon} className="tl-menu__icon tl-icon-sm" />}
      <span className="tl-menu__label">{label}</span>
    </button>
  );

export const MenuHeader: React.FC<{ label: string }> = ({ label }) => (
  <div className="tl-menu__header tl-type-label" role="presentation">{label}</div>
);

export const MenuSeparator: React.FC = () => <hr className="tl-divider" />;
