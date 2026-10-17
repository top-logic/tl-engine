import { React, useTLState, useTLCommand, useI18N, rootClassName, usePart, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

const I18N_KEYS = {
  'js.bottomBar.ariaLabel': 'Bottom navigation',
};

interface BottomBarItem {
  id: string;
  label: string;
  icon: string;
  badge?: string;
}

/**
 * Bottom navigation bar for small screens: a few pages with icon above label.
 *
 * State:
 * - items: { id, label, icon, badge? }[]  - icon as a theme image or a CSS class
 * - activeItemId: string                  - the page the user is on (aria-current="page")
 *
 * The badge of an item is the part `Badge`, over the corner of the icon; the element around it (tl-icon-badge__count)
 * places it, so that the badge keeps the position any implementation of the part gives its own root.
 */
const TLBottomBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);
  const Badge = usePart('Badge');

  const items = (state.items as BottomBarItem[]) ?? [];
  const activeItemId = state.activeItemId as string;

  const handleSelect = useCallback((itemId: string) => {
    if (itemId !== activeItemId) {
      sendCommand('selectItem', { itemId });
    }
  }, [sendCommand, activeItemId]);

  return (
    <nav id={controlId} className={rootClassName(state, 'tl-bottom-bar')} aria-label={i18n['js.bottomBar.ariaLabel']}>
      {items.map(item => {
        const isActive = item.id === activeItemId;
        const icon = <ThemeIcon encoded={item.icon} className="tl-icon-md" />;
        return (
          <button
            key={item.id}
            type="button"
            className="tl-bottom-bar__item"
            onClick={() => handleSelect(item.id)}
            aria-current={isActive ? 'page' : undefined}
          >
            <span className="tl-bottom-bar__icon">
              {item.badge ? (
                <span className="tl-icon-badge">
                  {icon}
                  <span className="tl-icon-badge__count">
                    <Badge content={item.badge} />
                  </span>
                </span>
              ) : icon}
            </span>
            <span className={'tl-bottom-bar__label ' + (isActive ? 'tl-type-label-strong' : 'tl-type-label')}>{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};

export default TLBottomBar;
