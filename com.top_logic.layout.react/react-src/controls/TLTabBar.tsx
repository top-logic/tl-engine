import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import TLChild from '../bridge/TLChild';

interface TabInfo {
  id: string;
  label: string;
}

const { useCallback } = React;

const TLTabBar: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const tabs = (state.tabs as TabInfo[]) ?? [];
  const activeTabId = state.activeTabId as string;

  const handleTabClick = useCallback((tabId: string) => {
    if (tabId !== activeTabId) {
      sendCommand('selectTab', { tabId });
    }
  }, [sendCommand, activeTabId]);

  return (
    <div className="tlTabBar">
      <div className="tlTabBar__tabs" role="tablist">
        {tabs.map(tab => (
          <button
            key={tab.id}
            role="tab"
            aria-selected={tab.id === activeTabId}
            className={'tlTabBar__tab' + (tab.id === activeTabId ? ' tlTabBar__tab--active' : '')}
            onClick={() => handleTabClick(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>
      <div className="tlTabBar__content" role="tabpanel">
        {state.activeContent && <TLChild control={state.activeContent} />}
      </div>
    </div>
  );
};

export default TLTabBar;
