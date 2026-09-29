import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

interface BottomBarItem {
  id: string;
  label: string;
  icon: string;
  badge?: string;
}

/**
 * Bottom navigation bar for mobile screens.
 *
 * State:
 * - items: { id, label, icon, badge? }[]
 * - activeItemId: string
 */
const TLBottomBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const items = (state.items as BottomBarItem[]) ?? [];
  const activeItemId = state.activeItemId as string;

  const handleSelect = useCallback((itemId: string) => {
    if (itemId !== activeItemId) {
      sendCommand('selectItem', { itemId });
    }
  }, [sendCommand, activeItemId]);

  return (
    <nav id={controlId} className="tlBottomBar" aria-label="Bottom navigation">
      {items.map(item => {
        const isActive = item.id === activeItemId;
        return (
          <button
            key={item.id}
            type="button"
            className={'tlBottomBar__item' + (isActive ? ' tlBottomBar__item--active' : '')}
            onClick={() => handleSelect(item.id)}
            aria-current={isActive ? 'page' : undefined}
          >
            <span className="tlBottomBar__iconWrap">
              <i className={'tlBottomBar__icon ' + item.icon} aria-hidden="true" />
              {item.badge && <span className="tlBottomBar__badge">{item.badge}</span>}
            </span>
            <span className="tlBottomBar__label">{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};

export default TLBottomBar;
