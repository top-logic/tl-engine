import {
  React, useTLState, useTLCommand, TLChild, useI18N, useStandaloneKeyboardScope,
  FillBarrier, rootClassName, tooltipProps, ThemeIcon, usePart,
  useDisplayClass, useFocusTrap, pressClosedSurface, ICON_EXPAND,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { Menu, MenuItem, MenuHeader, MenuSeparator, menuItemClassName } from 'tl-react-bridge';

const { useCallback, useState, useEffect, useRef } = React;

const I18N_KEYS = {
  'js.sidebar.ariaLabel': 'Sidebar navigation',
  'js.sidebar.expand': 'Expand sidebar',
  'js.sidebar.collapse': 'Collapse sidebar',
};

interface SidebarItemBase {
  id: string;
  type: string;
  label?: string;
  icon?: string;
}

interface NavItem extends SidebarItemBase {
  type: 'nav';
  badge?: string;
  hidden?: boolean;
}

interface CommandItemInfo extends SidebarItemBase {
  type: 'command';
  /** Withheld from the rail and the flyout, and not reachable by keyboard. */
  hidden?: boolean;
  /** Shown greyed out, not activatable and skipped by keyboard navigation. */
  disabled?: boolean;
  /** Explaining text, the reason for the disabling where a rule gave one. */
  tooltip?: string;
}

interface HeaderItemInfo extends SidebarItemBase {
  type: 'header';
}

interface SeparatorItemInfo extends SidebarItemBase {
  type: 'separator';
}

interface GroupItemInfo extends SidebarItemBase {
  type: 'group';
  expanded: boolean;
  children: SidebarItemData[];
}

type SidebarItemData = NavItem | CommandItemInfo | HeaderItemInfo | SeparatorItemInfo | GroupItemInfo;

/* ---------- Keyboard helpers ---------- */

interface FocusableEntry {
  id: string;
  type: 'nav' | 'command' | 'group';
  groupId?: string;
}

function collectFocusable(
  items: SidebarItemData[],
  collapsed: boolean,
  groupStates: Map<string, boolean>,
  parentGroupId?: string
): FocusableEntry[] {
  const result: FocusableEntry[] = [];
  for (const item of items) {
    if (item.type === 'nav') {
      if ((item as NavItem).hidden) continue;
      result.push({ id: item.id, type: 'nav', groupId: parentGroupId });
    } else if (item.type === 'command') {
      const command = item as CommandItemInfo;
      // An item that is not offered, or offered out of reach, is nothing the roving tabindex
      // may come to rest on.
      if (command.hidden || command.disabled) continue;
      result.push({ id: item.id, type: 'command', groupId: parentGroupId });
    } else if (item.type === 'group') {
      result.push({ id: item.id, type: 'group' });
      const expanded = groupStates.get(item.id) ?? item.expanded;
      if (expanded && !collapsed) {
        result.push(...collectFocusable(item.children, collapsed, groupStates, item.id));
      }
    }
    // headers and separators are not focusable
  }
  return result;
}

/* ---------- Sub-components ---------- */

const SidebarIcon: React.FC<{ icon?: string }> = ({ icon }) => {
  if (!icon) return null;
  return <ThemeIcon encoded={icon} className="tl-menu__icon tl-icon-sm" />;
};

/** Whether the page the user is on lies in the given group, at any depth. */
function containsCurrent(item: GroupItemInfo, activeItemId: string): boolean {
  return item.children.some(child => (child.type === 'nav' && child.id === activeItemId)
    || (child.type === 'group' && containsCurrent(child as GroupItemInfo, activeItemId)));
}

const SidebarNavItem: React.FC<{
  item: NavItem;
  active: boolean;
  collapsed: boolean;
  onSelect: (id: string) => void;
  tabIndex: number;
  itemRef: (el: HTMLElement | null) => void;
  onFocus: (id: string) => void;
}> = ({ item, active, collapsed, onSelect, tabIndex, itemRef, onFocus }) => {
  const Badge = usePart('Badge');
  return (
    <button
      type="button"
      className={menuItemClassName(active, 'tl-sidebar__item')}
      aria-current={active ? 'page' : undefined}
      onClick={() => onSelect(item.id)}
      aria-label={collapsed ? item.label : undefined}
      {...tooltipProps(collapsed ? item.label : undefined)}
      tabIndex={tabIndex}
      ref={itemRef}
      onFocus={() => onFocus(item.id)}
    >
      {collapsed && item.badge ? (
        // The count over the corner of the icon: the element around the part places it (view-layer/parts.md).
        <span className="tl-icon-badge">
          <SidebarIcon icon={item.icon} />
          <span className="tl-icon-badge__count">
            <Badge content={item.badge} />
          </span>
        </span>
      ) : (
        <SidebarIcon icon={item.icon} />
      )}
      {!collapsed && <span className="tl-menu__label">{item.label}</span>}
      {!collapsed && item.badge && <Badge content={item.badge} />}
    </button>
  );
};

const SidebarCommandItem: React.FC<{
  item: CommandItemInfo;
  collapsed: boolean;
  onExecute: (id: string) => void;
  tabIndex: number;
  itemRef: (el: HTMLElement | null) => void;
  onFocus: (id: string) => void;
}> = ({ item, collapsed, onExecute, tabIndex, itemRef, onFocus }) => (
  <button
    type="button"
    className={menuItemClassName(false, 'tl-sidebar__item')}
    onClick={() => onExecute(item.id)}
    disabled={item.disabled}
    aria-label={collapsed ? item.label : undefined}
    {...tooltipProps(item.tooltip ?? (collapsed ? item.label : undefined))}
    tabIndex={tabIndex}
    ref={itemRef}
    onFocus={() => onFocus(item.id)}
  >
    <SidebarIcon icon={item.icon} />
    {!collapsed && <span className="tl-menu__label">{item.label}</span>}
  </button>
);

const SidebarHeaderItem: React.FC<{
  item: HeaderItemInfo;
  collapsed: boolean;
}> = ({ item, collapsed }) => {
  if (collapsed && !item.icon) return null;
  return (
    <div className="tl-menu__header tl-type-label" role="presentation" {...tooltipProps(collapsed ? item.label : undefined)}>
      <SidebarIcon icon={item.icon} />
      {!collapsed && item.label}
    </div>
  );
};

/* ---------- Group flyout (collapsed mode) ---------- */

/** The entries of a group in its flyout: the menu at the group's button, right of the collapsed rail. */
const SidebarGroupFlyout: React.FC<{
  item: GroupItemInfo;
  activeItemId: string;
  anchor: HTMLElement | null;
  onSelect: (id: string) => void;
  onExecute: (id: string) => void;
  onClose: () => void;
}> = ({ item, activeItemId, anchor, onSelect, onExecute, onClose }) => {
  const Badge = usePart('Badge');
  return (
    <Menu open anchor={anchor} placement="right-start" onClose={onClose}>
      <MenuHeader label={item.label ?? ''} />
      {item.children.map(child => {
        if (child.type === 'nav') {
          const nav = child as NavItem;
          if (nav.hidden) return null;
          return (
            <MenuItem key={nav.id} icon={nav.icon} label={nav.label ?? ''}
              current={nav.id === activeItemId ? 'page' : false}
              trailing={nav.badge ? <Badge content={nav.badge} /> : undefined}
              onSelect={() => { onSelect(nav.id); onClose(); }} />
          );
        }
        if (child.type === 'command') {
          const command = child as CommandItemInfo;
          if (command.hidden) return null;
          return (
            <MenuItem key={command.id} icon={command.icon} label={command.label ?? ''}
              disabled={command.disabled} tooltip={command.tooltip}
              onSelect={() => { onExecute(command.id); onClose(); }} />
          );
        }
        if (child.type === 'header') return <MenuHeader key={child.id} label={child.label ?? ''} />;
        if (child.type === 'separator') return <MenuSeparator key={child.id} />;
        return null;
      })}
    </Menu>
  );
};

/* ---------- Group ---------- */

const SidebarGroup: React.FC<{
  item: GroupItemInfo;
  expanded: boolean;
  activeItemId: string;
  collapsed: boolean;
  onSelect: (id: string) => void;
  onExecute: (id: string) => void;
  onToggleGroup: (id: string) => void;
  tabIndex: number;
  itemRef: (el: HTMLElement | null) => void;
  onFocus: (id: string) => void;
  focusedId: string;
  setItemRef: (id: string) => (el: HTMLElement | null) => void;
  onItemFocus: (id: string) => void;
  flyoutGroupId: string | null;
  onOpenFlyout: (id: string) => void;
  onCloseFlyout: () => void;
}> = ({ item, expanded, activeItemId, collapsed, onSelect, onExecute, onToggleGroup,
       tabIndex, itemRef, onFocus, focusedId, setItemRef, onItemFocus,
       flyoutGroupId, onOpenFlyout, onCloseFlyout }) => {

  const headerBtnRef = useRef<HTMLButtonElement>(null);

  const handleClick = useCallback(() => {
    if (collapsed) {
      if (flyoutGroupId === item.id) {
        onCloseFlyout();
      } else if (!pressClosedSurface()) {
        // The press of this click may just have closed the flyout from outside; it must not reopen it.
        onOpenFlyout(item.id);
      }
    } else {
      onToggleGroup(item.id);
    }
  }, [collapsed, flyoutGroupId, item.id, onToggleGroup, onOpenFlyout, onCloseFlyout]);

  // Combine refs: one for roving tabindex, one for anchor positioning.
  const combinedRef = useCallback((el: HTMLElement | null) => {
    (headerBtnRef as React.MutableRefObject<HTMLButtonElement | null>).current = el as HTMLButtonElement | null;
    itemRef(el);
  }, [itemRef]);

  const showFlyout = collapsed && flyoutGroupId === item.id;
  // Where the page the user is on hides in a closed group, the group's head shows it.
  const marked = containsCurrent(item, activeItemId) && (collapsed || !expanded);

  return (
    <div className="tl-sidebar__group">
      <button
        type="button"
        className={menuItemClassName(false, 'tl-sidebar__item')}
        data-tl-state={marked ? 'contains-current' : undefined}
        onClick={handleClick}
        aria-label={collapsed ? item.label : undefined}
        aria-haspopup={collapsed ? 'menu' : undefined}
        {...tooltipProps(collapsed ? item.label : undefined)}
        aria-expanded={collapsed ? showFlyout : expanded}
        tabIndex={tabIndex}
        ref={combinedRef}
        onFocus={() => onFocus(item.id)}
      >
        <SidebarIcon icon={item.icon} />
        {!collapsed && <span className="tl-menu__label">{item.label}</span>}
        {!collapsed && <ThemeIcon encoded={ICON_EXPAND} className="tl-sidebar__chevron tl-icon-sm" />}
      </button>
      {showFlyout && (
        <SidebarGroupFlyout item={item} activeItemId={activeItemId} anchor={headerBtnRef.current}
          onSelect={onSelect} onExecute={onExecute} onClose={onCloseFlyout} />
      )}
      {expanded && !collapsed && (
        <div className="tl-sidebar__children">
          {item.children.map(child => (
            <SidebarItemRenderer
              key={child.id}
              item={child}
              activeItemId={activeItemId}
              collapsed={collapsed}
              onSelect={onSelect}
              onExecute={onExecute}
              onToggleGroup={onToggleGroup}
              focusedId={focusedId}
              setItemRef={setItemRef}
              onItemFocus={onItemFocus}
              groupStates={null as any}
              flyoutGroupId={flyoutGroupId}
              onOpenFlyout={onOpenFlyout}
              onCloseFlyout={onCloseFlyout}
            />
          ))}
        </div>
      )}
    </div>
  );
};

const SidebarItemRenderer: React.FC<{
  item: SidebarItemData;
  activeItemId: string;
  collapsed: boolean;
  onSelect: (id: string) => void;
  onExecute: (id: string) => void;
  onToggleGroup: (id: string) => void;
  focusedId: string;
  setItemRef: (id: string) => (el: HTMLElement | null) => void;
  onItemFocus: (id: string) => void;
  groupStates: Map<string, boolean>;
  flyoutGroupId: string | null;
  onOpenFlyout: (id: string) => void;
  onCloseFlyout: () => void;
}> = ({ item, activeItemId, collapsed, onSelect, onExecute, onToggleGroup,
       focusedId, setItemRef, onItemFocus, groupStates,
       flyoutGroupId, onOpenFlyout, onCloseFlyout }) => {
  switch (item.type) {
    case 'nav':
      if ((item as NavItem).hidden) return null;
      return <SidebarNavItem item={item} active={item.id === activeItemId}
        collapsed={collapsed} onSelect={onSelect}
        tabIndex={focusedId === item.id ? 0 : -1}
        itemRef={setItemRef(item.id)} onFocus={onItemFocus} />;
    case 'command':
      if ((item as CommandItemInfo).hidden) return null;
      return <SidebarCommandItem item={item} collapsed={collapsed} onExecute={onExecute}
        tabIndex={focusedId === item.id ? 0 : -1}
        itemRef={setItemRef(item.id)} onFocus={onItemFocus} />;
    case 'header':
      return <SidebarHeaderItem item={item} collapsed={collapsed} />;
    case 'separator':
      return <MenuSeparator />;
    case 'group': {
      const expanded = groupStates ? (groupStates.get(item.id) ?? item.expanded) : item.expanded;
      return <SidebarGroup item={item} expanded={expanded} activeItemId={activeItemId} collapsed={collapsed}
        onSelect={onSelect} onExecute={onExecute} onToggleGroup={onToggleGroup}
        tabIndex={focusedId === item.id ? 0 : -1}
        itemRef={setItemRef(item.id)} onFocus={onItemFocus}
        focusedId={focusedId} setItemRef={setItemRef} onItemFocus={onItemFocus}
        flyoutGroupId={flyoutGroupId} onOpenFlyout={onOpenFlyout} onCloseFlyout={onCloseFlyout} />;
    }
    default:
      return null;
  }
};

/* ---------- Main component ---------- */

const TLSidebar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const DisclosureToggle = usePart('DisclosureToggle');
  const compact = useDisplayClass() === 'compact';

  const items = (state.items as SidebarItemData[]) ?? [];
  const activeItemId = state.activeItemId as string;
  const persistedCollapsed = state.collapsed as boolean;
  const drawerOpen = state.drawerOpen as boolean;

  // Compact, the navigation is a drawer showing labels whatever the rail preference; the preference stays for the
  // wide screen.
  const collapsed = compact ? false : persistedCollapsed;
  const drawerVisible = compact && drawerOpen;
  const drawerRef = useRef<HTMLElement | null>(null);

  // The drawer is modal: focus moves in and is kept there; closing - Escape, the scrim, choosing an entry - gives it
  // back to the hamburger it came from.
  useFocusTrap(drawerVisible, drawerRef, 'first');

  // Lift group expanded state into parent.
  const [groupStates, setGroupStates] = useState<Map<string, boolean>>(() => {
    const map = new Map<string, boolean>();
    const init = (list: SidebarItemData[]) => {
      for (const item of list) {
        if (item.type === 'group') {
          map.set(item.id, item.expanded);
          init(item.children);
        }
      }
    };
    init(items);
    return map;
  });

  const handleToggleGroup = useCallback((itemId: string) => {
    setGroupStates(prev => {
      const next = new Map(prev);
      const current = next.get(itemId) ?? false;
      next.set(itemId, !current);
      sendCommand('toggleGroup', { itemId, expanded: !current });
      return next;
    });
  }, [sendCommand]);

  const handleSelect = useCallback((itemId: string) => {
    if (itemId !== activeItemId) {
      sendCommand('selectItem', { itemId });
    }
  }, [sendCommand, activeItemId]);

  const handleExecute = useCallback((itemId: string) => {
    sendCommand('executeCommand', { itemId });
  }, [sendCommand]);

  const handleToggleCollapse = useCallback(() => {
    sendCommand('toggleCollapse', {});
  }, [sendCommand]);

  const handleToggleDrawer = useCallback(() => {
    sendCommand('toggleDrawer', {});
  }, [sendCommand]);

  useStandaloneKeyboardScope(drawerVisible, { ESCAPE: handleToggleDrawer });

  // Wide again with the drawer open: there is no drawer to show, and the next narrowing must not find it open
  // without anyone asking. The server keeps drawerOpen until told.
  useEffect(() => {
    if (!compact && drawerOpen) {
      handleToggleDrawer();
    }
  }, [compact, drawerOpen, handleToggleDrawer]);

  // Flyout state (collapsed-mode group popover).
  const [flyoutGroupId, setFlyoutGroupId] = useState<string | null>(null);

  const handleOpenFlyout = useCallback((id: string) => {
    setFlyoutGroupId(id);
  }, []);

  const handleCloseFlyout = useCallback(() => {
    setFlyoutGroupId(null);
  }, []);

  // Close flyout when sidebar expands.
  useEffect(() => {
    if (!collapsed) {
      setFlyoutGroupId(null);
    }
  }, [collapsed]);

  // Roving tabindex state.
  const [focusedId, setFocusedId] = useState<string>(() => {
    const focusable = collectFocusable(items, collapsed, groupStates);
    return focusable.length > 0 ? focusable[0].id : '';
  });
  const itemRefs = useRef<Map<string, HTMLElement>>(new Map());

  const setItemRef = useCallback((id: string) => (el: HTMLElement | null) => {
    if (el) {
      itemRefs.current.set(id, el);
    } else {
      itemRefs.current.delete(id);
    }
  }, []);

  const onItemFocus = useCallback((id: string) => {
    setFocusedId(id);
  }, []);

  // Move DOM focus when focusedId changes programmatically (keyboard navigation).
  const focusTrigger = useRef(0);
  const moveFocus = useCallback((id: string) => {
    setFocusedId(id);
    focusTrigger.current++;
  }, []);

  useEffect(() => {
    const el = itemRefs.current.get(focusedId);
    if (el && document.activeElement !== el) {
      el.focus();
    }
  }, [focusedId, focusTrigger.current]);

  // Keyboard handler on the items container.
  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    // Keys from the flyout bubble up the React tree from its portal; the menu handles them, the rail does not.
    if (!(e.currentTarget as HTMLElement).contains(e.target as Node)) return;

    const focusable = collectFocusable(items, collapsed, groupStates);
    if (focusable.length === 0) return;

    const idx = focusable.findIndex(f => f.id === focusedId);
    if (idx < 0) {
      // The item the roving tabindex rested on was withdrawn while the sidebar stood - a command
      // that reported itself unexecutable, say. Keyboard navigation resumes at the top instead of
      // stalling on an item that is no longer there.
      if (e.key === 'ArrowDown' || e.key === 'ArrowUp' || e.key === 'Home' || e.key === 'End') {
        e.preventDefault();
        moveFocus(focusable[0].id);
      }
      return;
    }

    const current = focusable[idx];

    switch (e.key) {
      case 'ArrowDown': {
        e.preventDefault();
        const next = (idx + 1) % focusable.length;
        moveFocus(focusable[next].id);
        break;
      }
      case 'ArrowUp': {
        e.preventDefault();
        const prev = (idx - 1 + focusable.length) % focusable.length;
        moveFocus(focusable[prev].id);
        break;
      }
      case 'Home': {
        e.preventDefault();
        moveFocus(focusable[0].id);
        break;
      }
      case 'End': {
        e.preventDefault();
        moveFocus(focusable[focusable.length - 1].id);
        break;
      }
      case 'Enter':
      case ' ': {
        e.preventDefault();
        if (current.type === 'nav') {
          handleSelect(current.id);
        } else if (current.type === 'command') {
          handleExecute(current.id);
        } else if (current.type === 'group') {
          if (collapsed) {
            // Toggle flyout in collapsed mode.
            if (flyoutGroupId === current.id) {
              handleCloseFlyout();
            } else {
              handleOpenFlyout(current.id);
            }
          } else {
            handleToggleGroup(current.id);
          }
        }
        break;
      }
      case 'ArrowRight': {
        if (current.type === 'group') {
          if (collapsed) {
            e.preventDefault();
            handleOpenFlyout(current.id);
          } else if (!(groupStates.get(current.id) ?? false)) {
            e.preventDefault();
            handleToggleGroup(current.id);
          }
        }
        break;
      }
      case 'ArrowLeft': {
        if (current.type === 'group' && !collapsed) {
          const expanded = groupStates.get(current.id) ?? false;
          if (expanded) {
            e.preventDefault();
            handleToggleGroup(current.id);
          }
        }
        break;
      }
    }
  }, [items, collapsed, groupStates, focusedId, flyoutGroupId, moveFocus,
      handleSelect, handleExecute, handleToggleGroup, handleOpenFlyout, handleCloseFlyout]);

  const collapseLabel = collapsed ? i18n['js.sidebar.expand'] : i18n['js.sidebar.collapse'];
  const navId = `${controlId}-nav`;

  const nav = (
    <nav id={navId} className="tl-sidebar__nav" aria-label={i18n['js.sidebar.ariaLabel']}>
      {collapsed ? (
        !!state.headerCollapsedContent && (
          <div className="tl-sidebar__header">
            <TLChild control={state.headerCollapsedContent} />
          </div>
        )
      ) : (
        !!state.headerContent && (
          <div className="tl-sidebar__header">
            <TLChild control={state.headerContent} />
          </div>
        )
      )}

      <div className="tl-sidebar__items" onKeyDown={handleKeyDown}>
        {items.map(item => (
          <SidebarItemRenderer
            key={item.id}
            item={item}
            activeItemId={activeItemId}
            collapsed={collapsed}
            onSelect={handleSelect}
            onExecute={handleExecute}
            onToggleGroup={handleToggleGroup}
            focusedId={focusedId}
            setItemRef={setItemRef}
            onItemFocus={onItemFocus}
            groupStates={groupStates}
            flyoutGroupId={flyoutGroupId}
            onOpenFlyout={handleOpenFlyout}
            onCloseFlyout={handleCloseFlyout}
          />
        ))}
      </div>

      {collapsed ? (
        !!state.footerCollapsedContent && (
          <div className="tl-sidebar__footer">
            <TLChild control={state.footerCollapsedContent} />
          </div>
        )
      ) : (
        !!state.footerContent && (
          <div className="tl-sidebar__footer">
            <TLChild control={state.footerContent} />
          </div>
        )
      )}

      {/* Collapsing is a preference of the wide screen; a drawer has nothing to collapse. */}
      {!compact && (
        <div className="tl-sidebar__collapse">
          <DisclosureToggle expanded={!collapsed} opens="end" label={collapseLabel} onClick={handleToggleCollapse}
            aria-controls={navId} />
        </div>
      )}
    </nav>
  );

  return (
    <div id={controlId} className={rootClassName(state, 'tl-sidebar')} data-tl-state={collapsed ? 'collapsed' : undefined}>
      {!!state.drawerToggleContribution && (
        <TLChild control={state.drawerToggleContribution} />
      )}
      {!compact && nav}
      {drawerVisible && (
        // The modal drawer of the design system (wave 4a): the scrim closes it, the drawer holds the navigation.
        <div className="tl-dialog" onClick={(e) => { if (e.target === e.currentTarget) handleToggleDrawer(); }}>
          <aside
            ref={drawerRef}
            className="tl-drawer tl-drawer--start tl-drawer--narrow"
            data-tl-state="open"
            role="dialog"
            aria-modal="true"
            aria-label={i18n['js.sidebar.ariaLabel']}
            tabIndex={-1}
          >
            {nav}
          </aside>
        </div>
      )}

      {/* The region stretches the active view to its own bounded height either way, which ends the fill chain here. */}
      <div className="tl-sidebar__content tl-region">
        <FillBarrier>
          {!!state.activeContent && <TLChild control={state.activeContent} />}
        </FillBarrier>
      </div>
    </div>
  );

};

export default TLSidebar;
