import { React, useTLState, useTLCommand, rootClassName, usePart } from 'tl-react-bridge';
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
 *
 * The badge of an item is the part `Badge`, over the corner of the icon.
 */
const TLBottomBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const Badge = usePart('Badge');

  const items = (state.items as BottomBarItem[]) ?? [];
  const activeItemId = state.activeItemId as string;

  const handleSelect = useCallback((itemId: string) => {
    if (itemId !== activeItemId) {
      sendCommand('selectItem', { itemId });
    }
  }, [sendCommand, activeItemId]);

  return (
    <nav id={controlId} className={rootClassName(state, 'tlBottomBar')} aria-label="Bottom navigation">
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
              {item.badge && <Badge content={item.badge} className="tlBottomBar__badge" />}
            </span>
            <span className="tlBottomBar__label">{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};

export default TLBottomBar;
