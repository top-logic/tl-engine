import { React, useTLState, useTLCommand, TLChild, useFill, FillBarrier } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { ThemeIcon } from './icon/ThemeIcon';

interface TabInfo {
  id: string;
  label: string;
  icon?: string;
}

const { useCallback } = React;

/**
 * A tab strip above the content of the selected tab.
 *
 * Always fills its container, so the strip stays pinned and only the tab content scrolls. The
 * content region is bounded by that and ends the fill chain: a filling control inside a tab
 * resolves its height against the region and scrolls internally.
 */
const TLTabBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);
  const tabs = (state.tabs as TabInfo[]) ?? [];
  const activeTabId = state.activeTabId as string;

  const handleTabClick = useCallback((tabId: string) => {
    if (tabId !== activeTabId) {
      sendCommand('selectTab', { tabId });
    }
  }, [sendCommand, activeTabId]);

  return (
    <div id={controlId} className={'tlReactTabBar ' + fillClass}>
      <div className="tlReactTabBar__tabs" role="tablist">
        {tabs.map(tab => (
          <button
            key={tab.id}
            role="tab"
            aria-selected={tab.id === activeTabId}
            className={'tlReactTabBar__tab' + (tab.id === activeTabId ? ' tlReactTabBar__tab--active' : '')}
            onClick={() => handleTabClick(tab.id)}
          >
            {tab.icon && <ThemeIcon encoded={tab.icon} className="tlReactTabBar__tabIcon" />}
            {tab.label}
          </button>
        ))}
      </div>
      <div className="tlReactTabBar__content" role="tabpanel">
        <FillBarrier>
          {state.activeContent && <TLChild control={state.activeContent} />}
        </FillBarrier>
      </div>
    </div>
  );
};

export default TLTabBar;
