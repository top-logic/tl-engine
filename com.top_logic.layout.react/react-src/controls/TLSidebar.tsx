import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useState } = React;

interface SidebarItemBase {
  id: string;
  type: string;
  label?: string;
  icon?: string;
}

interface NavItem extends SidebarItemBase {
  type: 'nav';
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
}> = ({ item, active, collapsed, onSelect }) => (
  <button
    className={'tlSidebar__navItem' + (active ? ' tlSidebar__navItem--active' : '')}
    onClick={() => onSelect(item.id)}
    title={collapsed ? item.label : undefined}
  >
    <SidebarIcon icon={item.icon} />
    {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
  </button>
);

const SidebarCommandItem: React.FC<{
  item: CommandItemInfo;
  collapsed: boolean;
  onExecute: (id: string) => void;
}> = ({ item, collapsed, onExecute }) => (
  <button
    className="tlSidebar__commandItem"
    onClick={() => onExecute(item.id)}
    title={collapsed ? item.label : undefined}
  >
    <SidebarIcon icon={item.icon} />
    {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
  </button>
);

const SidebarHeaderItem: React.FC<{
  item: HeaderItemInfo;
  collapsed: boolean;
}> = ({ item, collapsed }) => (
  <div className="tlSidebar__headerItem" title={collapsed ? item.label : undefined}>
    <SidebarIcon icon={item.icon} />
    {!collapsed && <span className="tlSidebar__label">{item.label}</span>}
  </div>
);

const SidebarSeparator: React.FC = () => (
  <hr className="tlSidebar__separator" />
);

const SidebarGroup: React.FC<{
  item: GroupItemInfo;
  activeItemId: string;
  collapsed: boolean;
  onSelect: (id: string) => void;
  onExecute: (id: string) => void;
  onToggleGroup: (id: string, expanded: boolean) => void;
}> = ({ item, activeItemId, collapsed, onSelect, onExecute, onToggleGroup }) => {
  const [expanded, setExpanded] = useState(item.expanded);

  const handleToggle = useCallback(() => {
    const next = !expanded;
    setExpanded(next);
    onToggleGroup(item.id, next);
  }, [expanded, item.id, onToggleGroup]);

  return (
    <div className="tlSidebar__group">
      <button
        className="tlSidebar__groupHeader"
        onClick={handleToggle}
        title={collapsed ? item.label : undefined}
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
  onToggleGroup: (id: string, expanded: boolean) => void;
}> = ({ item, activeItemId, collapsed, onSelect, onExecute, onToggleGroup }) => {
  switch (item.type) {
    case 'nav':
      return <SidebarNavItem item={item} active={item.id === activeItemId}
        collapsed={collapsed} onSelect={onSelect} />;
    case 'command':
      return <SidebarCommandItem item={item} collapsed={collapsed} onExecute={onExecute} />;
    case 'header':
      return <SidebarHeaderItem item={item} collapsed={collapsed} />;
    case 'separator':
      return <SidebarSeparator />;
    case 'group':
      return <SidebarGroup item={item} activeItemId={activeItemId} collapsed={collapsed}
        onSelect={onSelect} onExecute={onExecute} onToggleGroup={onToggleGroup} />;
    default:
      return null;
  }
};

/* ---------- Main component ---------- */

const TLSidebar: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const items = (state.items as SidebarItemData[]) ?? [];
  const activeItemId = state.activeItemId as string;
  const collapsed = state.collapsed as boolean;

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

  const handleToggleGroup = useCallback((itemId: string, expanded: boolean) => {
    sendCommand('toggleGroup', { itemId, expanded });
  }, [sendCommand]);

  return (
    <div className={'tlSidebar' + (collapsed ? ' tlSidebar--collapsed' : '')}>
      <nav className="tlSidebar__nav">
        {!collapsed && state.headerContent && (
          <div className="tlSidebar__headerSlot">
            <TLChild control={state.headerContent} />
          </div>
        )}

        <div className="tlSidebar__items">
          {items.map(item => (
            <SidebarItemRenderer
              key={item.id}
              item={item}
              activeItemId={activeItemId}
              collapsed={collapsed}
              onSelect={handleSelect}
              onExecute={handleExecute}
              onToggleGroup={handleToggleGroup}
            />
          ))}
        </div>

        {!collapsed && state.footerContent && (
          <div className="tlSidebar__footerSlot">
            <TLChild control={state.footerContent} />
          </div>
        )}

        <button className="tlSidebar__collapseBtn" onClick={handleToggleCollapse}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}>
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
