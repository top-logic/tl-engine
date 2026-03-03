import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

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
}

interface CommandItemInfo extends SidebarItemBase {
  type: 'command';
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
      result.push({ id: item.id, type: 'nav', groupId: parentGroupId });
    } else if (item.type === 'command') {
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
  return <i className={'tlSidebar__icon ' + icon} aria-hidden="true" />;
};

const SidebarNavItem: React.FC<{
  item: NavItem;
  active: boolean;
  collapsed: boolean;
  onSelect: (id: string) => void;
  tabIndex: number;
  itemRef: (el: HTMLElement | null) => void;
  onFocus: (id: string) => void;
}> = ({ item, active, collapsed, onSelect, tabIndex, itemRef, onFocus }) => (
  <button
    className={'tlSidebar__item tlSidebar__navItem' + (active ? ' tlSidebar__navItem--active' : '')}
    onClick={() => onSelect(item.id)}
    title={collapsed ? item.label : undefined}
    tabIndex={tabIndex}
    ref={itemRef}
    onFocus={() => onFocus(item.id)}
  >
    {collapsed && item.badge ? (
      <span className="tlSidebar__iconWrap">
        <SidebarIcon icon={item.icon} />
        <span className="tlSidebar__badge tlSidebar__badge--collapsed">{item.badge}</span>
      </span>
    ) : (
      <SidebarIcon icon={item.icon} />
    )}
    {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
    {!collapsed && item.badge && <span className="tlSidebar__badge">{item.badge}</span>}
  </button>
);

const SidebarCommandItem: React.FC<{
  item: CommandItemInfo;
  collapsed: boolean;
  onExecute: (id: string) => void;
  tabIndex: number;
  itemRef: (el: HTMLElement | null) => void;
  onFocus: (id: string) => void;
}> = ({ item, collapsed, onExecute, tabIndex, itemRef, onFocus }) => (
  <button
    className="tlSidebar__item tlSidebar__commandItem"
    onClick={() => onExecute(item.id)}
    title={collapsed ? item.label : undefined}
    tabIndex={tabIndex}
    ref={itemRef}
    onFocus={() => onFocus(item.id)}
  >
    <SidebarIcon icon={item.icon} />
    {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
  </button>
);

const SidebarHeaderItem: React.FC<{
  item: HeaderItemInfo;
  collapsed: boolean;
}> = ({ item, collapsed }) => {
  if (collapsed && !item.icon) return null;
  return (
    <div className="tlSidebar__headerItem" title={collapsed ? item.label : undefined}>
      <SidebarIcon icon={item.icon} />
      {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
    </div>
  );
};

const SidebarSeparator: React.FC = () => (
  <hr className="tlSidebar__separator" />
);

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
}> = ({ item, expanded, activeItemId, collapsed, onSelect, onExecute, onToggleGroup,
       tabIndex, itemRef, onFocus, focusedId, setItemRef, onItemFocus }) => (
  <div className="tlSidebar__group">
    <button
      className="tlSidebar__item tlSidebar__groupHeader"
      onClick={() => onToggleGroup(item.id)}
      title={collapsed ? item.label : undefined}
      aria-expanded={expanded}
      tabIndex={tabIndex}
      ref={itemRef}
      onFocus={() => onFocus(item.id)}
    >
      <SidebarIcon icon={item.icon} />
      {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
      {!collapsed && (
        <svg className={'tlSidebar__chevron' + (expanded ? ' tlSidebar__chevron--open' : '')}
          viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
          <path d="M4 6l4 4 4-4" fill="none" stroke="currentColor" strokeWidth="2"
            strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )}
    </button>
    {expanded && !collapsed && (
      <div className="tlSidebar__groupChildren">
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
          />
        ))}
      </div>
    )}
  </div>
);

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
}> = ({ item, activeItemId, collapsed, onSelect, onExecute, onToggleGroup,
       focusedId, setItemRef, onItemFocus, groupStates }) => {
  switch (item.type) {
    case 'nav':
      return <SidebarNavItem item={item} active={item.id === activeItemId}
        collapsed={collapsed} onSelect={onSelect}
        tabIndex={focusedId === item.id ? 0 : -1}
        itemRef={setItemRef(item.id)} onFocus={onItemFocus} />;
    case 'command':
      return <SidebarCommandItem item={item} collapsed={collapsed} onExecute={onExecute}
        tabIndex={focusedId === item.id ? 0 : -1}
        itemRef={setItemRef(item.id)} onFocus={onItemFocus} />;
    case 'header':
      return <SidebarHeaderItem item={item} collapsed={collapsed} />;
    case 'separator':
      return <SidebarSeparator />;
    case 'group': {
      const expanded = groupStates ? (groupStates.get(item.id) ?? item.expanded) : item.expanded;
      return <SidebarGroup item={item} expanded={expanded} activeItemId={activeItemId} collapsed={collapsed}
        onSelect={onSelect} onExecute={onExecute} onToggleGroup={onToggleGroup}
        tabIndex={focusedId === item.id ? 0 : -1}
        itemRef={setItemRef(item.id)} onFocus={onItemFocus}
        focusedId={focusedId} setItemRef={setItemRef} onItemFocus={onItemFocus} />;
    }
    default:
      return null;
  }
};

/* ---------- Main component ---------- */

const TLSidebar: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const items = (state.items as SidebarItemData[]) ?? [];
  const activeItemId = state.activeItemId as string;
  const collapsed = state.collapsed as boolean;

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
    const focusable = collectFocusable(items, collapsed, groupStates);
    if (focusable.length === 0) return;

    const idx = focusable.findIndex(f => f.id === focusedId);
    if (idx < 0) return;

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
          handleToggleGroup(current.id);
        }
        break;
      }
      case 'ArrowRight': {
        if (current.type === 'group' && !collapsed) {
          const expanded = groupStates.get(current.id) ?? false;
          if (!expanded) {
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
  }, [items, collapsed, groupStates, focusedId, moveFocus, handleSelect, handleExecute, handleToggleGroup]);

  return (
    <div className={'tlSidebar' + (collapsed ? ' tlSidebar--collapsed' : '')}>
      <nav className="tlSidebar__nav" aria-label={i18n['js.sidebar.ariaLabel']}>
        {collapsed ? (
          state.headerCollapsedContent && (
            <div className="tlSidebar__headerSlot tlSidebar__headerSlot--collapsed">
              <TLChild control={state.headerCollapsedContent} />
            </div>
          )
        ) : (
          state.headerContent && (
            <div className="tlSidebar__headerSlot">
              <TLChild control={state.headerContent} />
            </div>
          )
        )}

        <div className="tlSidebar__items" onKeyDown={handleKeyDown}>
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
            />
          ))}
        </div>

        {collapsed ? (
          state.footerCollapsedContent && (
            <div className="tlSidebar__footerSlot tlSidebar__footerSlot--collapsed">
              <TLChild control={state.footerCollapsedContent} />
            </div>
          )
        ) : (
          state.footerContent && (
            <div className="tlSidebar__footerSlot">
              <TLChild control={state.footerContent} />
            </div>
          )
        )}

        <button className="tlSidebar__collapseBtn" onClick={handleToggleCollapse}
          title={collapsed ? i18n['js.sidebar.expand'] : i18n['js.sidebar.collapse']}>
          <svg viewBox="0 0 16 16" width="16" height="16" aria-hidden="true">
            <path d={collapsed ? 'M6 4l4 4-4 4' : 'M10 4l-4 4 4 4'}
              fill="none" stroke="currentColor" strokeWidth="2"
              strokeLinecap="round" strokeLinejoin="round" />
          </svg>
        </button>
      </nav>

      <div className="tlSidebar__content">
        {state.activeContent && <TLChild control={state.activeContent} />}
      </div>
    </div>
  );
};

export default TLSidebar;
