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

/** The key closing a menu. */
const KEY_ESCAPE = 'Escape';

/**
 * Escape closing a menu, with the focus going to the menu's trigger afterwards, as for a WAI-ARIA
 * menu button.
 *
 * <p>{@code onKeyDown} closes the menu on Escape and answers whether it handled the key; it goes on
 * the menu surface, or is called first by the key handler of the menu. The returned
 * {@code restoreTo} is the restore target for the focus trap of the menu (option {@code restoreTo}
 * of useFocusTrap): after the menu was closed with Escape it answers the element of
 * {@code trigger}. After every other close (an entry chosen, a press outside, the trigger clicked)
 * and for a menu without trigger element (one opened at a point) it answers {@code null}, and the
 * focus returns to the element that had it when the menu opened.</p>
 *
 * @param trigger
 *        The element that opens the menu, read when the menu closes; absent or answering
 *        {@code null} for a menu without one.
 */
export function useEscapeClose(open: boolean, onClose: () => void, trigger?: () => HTMLElement | null):
    { onKeyDown: (e: React.KeyboardEvent) => boolean; restoreTo: () => HTMLElement | null } {
  // Whether the menu was closed with Escape since it opened.
  const escapedRef = useRef(false);
  const triggerRef = useRef(trigger);
  triggerRef.current = trigger;

  useEffect(() => {
    if (open) escapedRef.current = false;
  }, [open]);

  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key !== KEY_ESCAPE) return false;
    e.preventDefault();
    escapedRef.current = true;
    onClose();
    return true;
  }, [onClose]);

  const restoreTo = useCallback(
    () => escapedRef.current ? triggerRef.current?.() ?? null : null,
    [],
  );

  return { onKeyDown, restoreTo };
}

/**
 * Roving tabindex over the entries of a menu: exactly one entry is in the tab order, the one that
 * has focus; arrows cycle, Home/End jump; disabled and hidden entries are skipped. Escape closes -
 * also a menu whose entries are all disabled - and {@code restoreTo} then answers the trigger, see
 * {@link useEscapeClose}. Shared by Menu and the toolbar overflow.
 *
 * @param trigger
 *        The element that opens the menu, read when the menu closes; absent or answering
 *        {@code null} for a menu without one.
 */
export function useRovingMenu(ref: React.RefObject<HTMLElement | null>, open: boolean, onClose: () => void,
  trigger?: () => HTMLElement | null): { onKeyDown: (e: React.KeyboardEvent) => void; restoreTo: () => HTMLElement | null } {
  // Only rendered entries rove: an entry inside a hidden wrapper (the upload button renders
  // `<span hidden>` around its menuitem) cannot take focus, and as a target it would stall the arrows.
  const items = useCallback(
    () => Array.from(ref.current?.querySelectorAll<HTMLElement>(ITEM) ?? []).filter(el => el.getClientRects().length > 0),
    [ref],
  );
  const escape = useEscapeClose(open, onClose, trigger);
  const handleEscape = escape.onKeyDown;

  useEffect(() => {
    if (!open) return;
    items().forEach((el, i) => el.setAttribute('tabindex', i === 0 ? '0' : '-1'));
  }, [open, items]);

  const onKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (handleEscape(e)) return;
    const all = items();
    if (all.length === 0) return;
    const current = all.indexOf(document.activeElement as HTMLElement);
    let next = -1;
    if (e.key === 'ArrowDown') next = (current + 1) % all.length;
    else if (e.key === 'ArrowUp') next = (current - 1 + all.length) % all.length;
    else if (e.key === 'Home') next = 0;
    else if (e.key === 'End') next = all.length - 1;
    if (next < 0) return;
    e.preventDefault();
    all.forEach((el, i) => el.setAttribute('tabindex', i === next ? '0' : '-1'));
    all[next].focus();
  }, [items, handleEscape]);

  return { onKeyDown, restoreTo: escape.restoreTo };
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
 * Escape and on a press outside. Closed with Escape, it gives the focus to its anchor when that is an
 * element (the button that opened it); otherwise, and after every other close, the focus returns to
 * where it was when the menu opened. Mounted only while open - a caller that needs its entries to stay
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
  const anchorElement = anchor instanceof HTMLElement ? anchor : null;
  const trigger = useCallback(() => anchorElement, [anchorElement]);
  const { onKeyDown, restoreTo } = useRovingMenu(ref, open, onClose, trigger);
  useFocusTrap(open, ref, 'first', { restoreTo });

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
