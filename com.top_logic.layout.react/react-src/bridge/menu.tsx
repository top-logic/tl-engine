import React from 'react';
import { createPortal } from 'react-dom';
import { usePart } from './parts';
import { useCloseOnOutsidePress } from './outside-press';
import { useFocusTrap } from './tl-react-bridge';
import { anchoredOverlayProps } from './focus-trap';
import { usePopover } from './popover';
import type { PopoverAnchor, PopoverOptions } from './popover';
import { ThemeIcon } from './ThemeIcon';
import { tooltipProps } from './tooltip-host';

const { useCallback, useEffect, useRef } = React;

const ITEM = '[role="menuitem"]:not([disabled]), [role="menuitemcheckbox"]:not([disabled]), [role="menuitemradio"]:not([disabled])';

/**
 * Roving tabindex over the entries of a menu: exactly one entry is in the tab order, the one that
 * has focus; arrows cycle, Home/End jump, Escape closes; disabled and hidden entries are skipped. Shared by
 * Menu and the toolbar overflow.
 */
export function useRovingMenu(ref: React.RefObject<HTMLElement | null>, open: boolean, onClose: () => void): { onKeyDown: (e: React.KeyboardEvent) => void } {
  // Only rendered entries rove: an entry inside a hidden wrapper (the upload button renders
  // `<span hidden>` around its menuitem) cannot take focus, and as a target it would stall the arrows.
  const items = useCallback(
    () => Array.from(ref.current?.querySelectorAll<HTMLElement>(ITEM) ?? []).filter(el => el.getClientRects().length > 0),
    [ref],
  );

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
  // Both the own ref and the popover's go on the surface. A plain callback rather than floating-ui's
  // useMergeRefs: that one's return type comes from the React types of this module and fails to match
  // the JSX of a module with its own copy of @types/react.
  const setRefs = useCallback((el: HTMLDivElement | null) => {
    ref.current = el;
    setFloating(el);
  }, [setFloating]);
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
 * The class list of a menu entry: block, tone - an entry of a destructive command (`danger`) is
 * tl-menu__item--danger -, typography - an entry in force (`current`, reported as `aria-current`)
 * reads strong -, passed-through classes. Shared by MenuItem and every button that renders as an
 * entry (ButtonDefaults `menu-item`), so the two cannot drift apart.
 */
export function menuItemClassName(current?: boolean, extra?: string, danger?: boolean): string {
  return ['tl-menu__item', danger ? 'tl-menu__item--danger' : '', current ? 'tl-type-body-strong' : 'tl-type-body', extra ?? '']
    .filter(Boolean).join(' ');
}

/**
 * An entry of a menu. `current` marks the entry in force (`true`, reported as aria-current="true") or the page the
 * user is on (`'page'`, the flyout of a sidebar group). `trailing` stands after the label (the badge of a sidebar
 * page); `tooltip` explains a disabled entry.
 */
export const MenuItem: React.FC<{ id?: string; icon?: string; label: string; disabled?: boolean; current?: boolean | 'page';
  danger?: boolean; className?: string; trailing?: React.ReactNode; tooltip?: string; onSelect: () => void }> =
  ({ id, icon, label, disabled, current, danger, className, trailing, tooltip, onSelect }) => (
    <button
      type="button"
      id={id}
      className={menuItemClassName(!!current, className, danger)}
      role="menuitem"
      aria-current={current === 'page' ? 'page' : current ? 'true' : undefined}
      disabled={disabled}
      tabIndex={-1}
      onClick={onSelect}
      {...tooltipProps(tooltip)}
    >
      {icon && <ThemeIcon encoded={icon} className="tl-menu__icon tl-icon-sm" />}
      <span className="tl-menu__label">{label}</span>
      {trailing}
    </button>
  );

export const MenuHeader: React.FC<{ label: string }> = ({ label }) => (
  <div className="tl-menu__header tl-type-label" role="presentation">{label}</div>
);

/**
 * The class of the element around the divider between the sections of a menu, which gives it the
 * space around it.
 */
const SEPARATOR_CLASS = 'tl-menu__separator';

/** The line between two sections of a menu, drawn by the part `Divider`. */
export const MenuSeparator: React.FC = () => {
  const Divider = usePart('Divider');
  // No role of its own: the divider inside is the separator of the menu.
  return (
    <div className={SEPARATOR_CLASS} role="none">
      <Divider />
    </div>
  );
};
