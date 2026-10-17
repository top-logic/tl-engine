import { React, useTLState, useTLCommand, TLChild, useFill, FillBarrier, rootClassName, ThemeIcon, useTabList } from 'tl-react-bridge';
import type { TLCellProps, TabBarStateJson } from 'tl-react-bridge';

/** A tab as the server describes it, always with its ID and label. */
type TabInfo = Partial<TabBarStateJson.Tab> & Required<Pick<TabBarStateJson.Tab, 'id' | 'label'>>;

const { useCallback } = React;

/**
 * A tab strip above the content of the selected tab (tl-tabs).
 *
 * Always fills its container, so the strip stays pinned and only the tab content scrolls. The
 * content region is bounded by that and ends the fill chain: a filling control inside a tab
 * resolves its height against the region and scrolls internally.
 *
 * Keyboard: roving tabindex - only the selected tab is in the tab order. The arrow keys, Home and
 * End move the focus and select the tab reached in one step, through the same command a click
 * sends; a tab holding unsaved changes may refuse to be left, and then the selection stays where
 * it was.
 */
const TLTabBar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TabBarStateJson>>();
  const sendCommand = useTLCommand();
  const fillClass = useFill(true);
  const tabs = (state.tabs ?? []) as TabInfo[];
  const activeTabId = state.activeTabId;
  const ids = tabs.map(tab => tab.id);

  const select = useCallback((tabId: string) => {
    if (tabId !== activeTabId) {
      sendCommand('selectTab', { tabId });
    }
  }, [sendCommand, activeTabId]);

  // One panel serves all tabs: it belongs to the selected tab, or to the first while none is.
  const list = useTabList({ idPrefix: controlId, ids, selected: activeTabId ?? '', onSelect: select });
  const panelKey = ids.includes(activeTabId ?? '') ? activeTabId! : ids[0];
  const panel = panelKey === undefined ? undefined : list.panelProps(panelKey);

  return (
    <div id={controlId} className={rootClassName(state, 'tl-tabs ' + fillClass)}>
      <div className="tl-tabs__list" role="tablist" onKeyDown={list.onListKeyDown}>
        {tabs.map(tab => (
          <button
            key={tab.id}
            {...list.tabProps(tab.id, panel && { controls: panel.id })}
            className="tl-tabs__tab tl-type-body"
          >
            {tab.icon && <ThemeIcon encoded={tab.icon} className="tl-icon-sm" />}
            <span>{tab.label}</span>
          </button>
        ))}
      </div>
      <div
        {...panel}
        hidden={false}
        className="tl-tabs__panel"
        role="tabpanel"
      >
        <FillBarrier>
          {!!state.activeContent && <TLChild control={state.activeContent} />}
        </FillBarrier>
      </div>
    </div>
  );
};

export default TLTabBar;
