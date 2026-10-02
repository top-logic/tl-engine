import { React, useTLState, useTLCommand, TLChild, useFill, FillBarrier, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, TabBarStateJson } from 'tl-react-bridge';

/** A tab as the server describes it, always with its ID and label. */
type TabInfo = Partial<TabBarStateJson.Tab> & Required<Pick<TabBarStateJson.Tab, 'id' | 'label'>>;

const { useCallback, useRef } = React;

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
  const listRef = useRef<HTMLDivElement>(null);

  // The tab in the tab order: the selected one, or the first while none is selected, so that the
  // strip stays reachable.
  const focusableId = tabs.some(tab => tab.id === activeTabId) ? activeTabId : tabs[0]?.id;
  const tabElementId = (tabId: string) => `${controlId}-tab-${tabId}`;
  const panelId = `${controlId}-panel`;

  const handleTabClick = useCallback((tabId: string) => {
    if (tabId !== activeTabId) {
      sendCommand('selectTab', { tabId });
    }
  }, [sendCommand, activeTabId]);

  const focusTab = (index: number) => {
    const all = Array.from(listRef.current?.querySelectorAll<HTMLButtonElement>('[role="tab"]') ?? []);
    const el = all[(index + all.length) % all.length];
    el?.focus();
    const id = el?.dataset.tabId;
    if (id) handleTabClick(id);
  };

  const handleKeyDown = (e: React.KeyboardEvent, index: number) => {
    if (e.key === 'ArrowRight') focusTab(index + 1);
    else if (e.key === 'ArrowLeft') focusTab(index - 1);
    else if (e.key === 'Home') focusTab(0);
    else if (e.key === 'End') focusTab(tabs.length - 1);
    else return;
    e.preventDefault();
  };

  return (
    <div id={controlId} className={rootClassName(state, 'tl-tabs ' + fillClass)}>
      <div ref={listRef} className="tl-tabs__list" role="tablist">
        {tabs.map((tab, index) => {
          const selected = tab.id === activeTabId;
          return (
            <button
              key={tab.id}
              type="button"
              role="tab"
              id={tabElementId(tab.id)}
              data-tab-id={tab.id}
              aria-selected={selected}
              aria-controls={panelId}
              tabIndex={tab.id === focusableId ? 0 : -1}
              className="tl-tabs__tab tl-type-body"
              onClick={() => handleTabClick(tab.id)}
              onKeyDown={e => handleKeyDown(e, index)}
            >
              {tab.icon && <ThemeIcon encoded={tab.icon} className="tl-icon-sm" />}
              <span>{tab.label}</span>
            </button>
          );
        })}
      </div>
      <div
        id={panelId}
        className="tl-tabs__panel"
        role="tabpanel"
        aria-labelledby={activeTabId ? tabElementId(activeTabId) : undefined}
      >
        <FillBarrier>
          {!!state.activeContent && <TLChild control={state.activeContent} />}
        </FillBarrier>
      </div>
    </div>
  );
};

export default TLTabBar;
