import { React, useTLState, useTLCommand, useFill, FillBarrier, TLChild, rootClassName, ThemeIcon } from 'tl-react-bridge';
import type { TLCellProps, TabBarStateJson } from 'tl-react-bridge';
import Tabs from '@mui/material/Tabs';
import Tab from '@mui/material/Tab';
import type { SxProps, Theme } from '@mui/material/styles';

const { useCallback } = React;

/** Command a tab bar sends when another tab is chosen. */
const CMD_SELECT_TAB = 'selectTab';

/** Argument of {@link CMD_SELECT_TAB}: the ID of the chosen tab. */
const ARG_TAB_ID = 'tabId';

/** Size class of the design system for the icon of a tab. */
const ICON_CLASS = 'tl-icon-sm';

/** A tab as the server describes it, always with its ID and label. */
type TabInfo = Partial<TabBarStateJson.Tab> & Required<Pick<TabBarStateJson.Tab, 'id' | 'label'>>;

/**
 * The root: a column whose strip keeps its height while the content region takes the rest. It
 * carries the fill class of the bridge, which makes it span the height its container offers.
 */
const ROOT_STYLE: React.CSSProperties = { display: 'flex', flexDirection: 'column', minHeight: 0 };

/** The strip: a line below the tabs, as Material UI draws a tab bar above its content. */
const TABS_SX: SxProps<Theme> = { flexShrink: 0, borderBottom: 1, borderColor: 'divider' };

/** A tab with an icon beside its label rather than above it, at the height of a label-only tab. */
const TAB_SX: SxProps<Theme> = { minHeight: 48 };

/** The content region: bounded by the root, scrolling what does not fit. */
const PANEL_STYLE: React.CSSProperties = {
  display: 'flex',
  flexDirection: 'column',
  flex: '1 1 auto',
  minHeight: 0,
  overflow: 'auto',
};

/**
 * Renders the state of a TopLogic tab bar (module name `TLTabBar`) with the MUI `Tabs` and a `Tab`
 * per tab above the content of the selected tab.
 *
 * <p>Mapping from the control state to the MUI props:</p>
 * <ul>
 * <li>tabs → a `Tab` per tab: label → label, icon → `icon` at the start of the label, rendered by
 *     the bridge's {@link ThemeIcon};</li>
 * <li>activeTabId → value of the `Tabs`; an ID naming no tab selects none;</li>
 * <li>activeContent → the content region below the strip, rendered through {@link TLChild}. The
 *     server sends the content of the selected tab only, so the content of a tab is mounted when
 *     it is selected;</li>
 * <li>choosing another tab (a click, or the arrow keys, Home and End on the strip, which select
 *     the tab they reach as in TLTabBar) → the command `selectTab` with the argument `tabId`.
 *     Choosing the selected tab sends nothing, and so does a tab receiving the focus without such
 *     a gesture (a closing dialog giving the focus back to it);</li>
 * <li>a strip wider than the tab bar scrolls (`variant="scrollable"`), with scroll buttons while
 *     it does;</li>
 * <li>hidden → the tab bar is not rendered and does not fill; the configured CSS class →
 *     className of the root.</li>
 * </ul>
 *
 * <p>The fill contract is the one of TLTabBar: the root always fills its container (it carries the
 * fill class of {@link useFill}), so the strip stays pinned and only the content region scrolls.
 * The region ends the fill chain ({@link FillBarrier}): a filling control inside a tab resolves
 * its height against the region.</p>
 *
 * <p>The contract has no closable, disabled or hidden tabs; the adapter offers none.</p>
 */
const MuiTabBarAdapter: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState<Partial<TabBarStateJson>>();
  const sendCommand = useTLCommand();
  const hidden = state.hidden === true;
  const fillClass = useFill(!hidden);
  const tabs = (state.tabs ?? []) as TabInfo[];
  const activeTabId = state.activeTabId;

  const selectTab = useCallback((tabId: string) => {
    if (tabId !== activeTabId) {
      sendCommand(CMD_SELECT_TAB, { [ARG_TAB_ID]: tabId });
    }
  }, [sendCommand, activeTabId]);

  const handleChange = useCallback((_event: React.SyntheticEvent, tabId: string) => {
    selectTab(tabId);
  }, [selectTab]);

  // The keys moving through the strip select the tab they reach, as in TLTabBar. MUI moves the
  // focus there (its own handler on the strip runs after this one); a focus that arrives otherwise
  // (given back by a dialog closing, for instance) selects nothing.
  const handleKeyDown = useCallback((event: React.KeyboardEvent, index: number) => {
    const count = tabs.length;
    let target: number;
    if (event.key === 'ArrowRight') target = (index + 1) % count;
    else if (event.key === 'ArrowLeft') target = (index - 1 + count) % count;
    else if (event.key === 'Home') target = 0;
    else if (event.key === 'End') target = count - 1;
    else return;
    selectTab(tabs[target].id);
  }, [tabs, selectTab]);

  if (hidden) {
    return null;
  }

  const selected = tabs.some(tab => tab.id === activeTabId) ? activeTabId! : false;
  const tabElementId = (tabId: string) => `${controlId}-tab-${tabId}`;
  const panelId = `${controlId}-panel`;

  return (
    <div id={controlId} className={rootClassName(state, fillClass)} style={ROOT_STYLE}>
      <Tabs
        value={selected}
        onChange={handleChange}
        variant="scrollable"
        scrollButtons="auto"
        sx={TABS_SX}
      >
        {tabs.map((tab, index) => (
          <Tab
            key={tab.id}
            value={tab.id}
            id={tabElementId(tab.id)}
            aria-controls={panelId}
            label={tab.label}
            icon={tab.icon ? <ThemeIcon encoded={tab.icon} className={ICON_CLASS} /> : undefined}
            iconPosition="start"
            sx={TAB_SX}
            onKeyDown={(event: React.KeyboardEvent) => handleKeyDown(event, index)}
          />
        ))}
      </Tabs>
      <div
        id={panelId}
        role="tabpanel"
        aria-labelledby={selected === false ? undefined : tabElementId(selected)}
        style={PANEL_STYLE}
      >
        <FillBarrier>
          {!!state.activeContent && <TLChild control={state.activeContent} />}
        </FillBarrier>
      </div>
    </div>
  );
};

export default MuiTabBarAdapter;
