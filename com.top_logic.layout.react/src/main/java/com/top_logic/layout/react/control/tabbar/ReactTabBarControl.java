/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tabbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.AgentControl;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.DirtyChannel;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.react.routing.RoutingParticipant;

import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A {@link ReactControl} that renders a tab bar with lazily created content.
 *
 * <p>
 * Only the currently visible tab's content control is created on the server. Previously visited tabs
 * are cached so that re-selecting them is instant and preserves their state.
 * </p>
 *
 * <p>
 * The React component receives the following state:
 * </p>
 * <ul>
 * <li>{@code tabs} - list of {@code {id, label}} objects</li>
 * <li>{@code activeTabId} - the currently selected tab ID</li>
 * <li>{@code activeContent} - the active tab's content as a child control descriptor (or
 * {@code null})</li>
 * </ul>
 */
public class ReactTabBarControl extends ReactControl implements RoutingParticipant {

	private static final String REACT_MODULE = "TLTabBar";

	private static final String TABS = "tabs";

	private static final String ACTIVE_TAB_ID = "activeTabId";

	private static final String ACTIVE_CONTENT = "activeContent";

	/** Tab info key for the tab identifier. */
	private static final String TAB_ID = "id";

	/** Tab info key for the tab display label. */
	private static final String TAB_LABEL = "label";

	/** Tab info key for the tab CSS icon class. */
	private static final String TAB_ICON = "icon";

	/** Command argument key for the selected tab ID. */
	/** The {@link ReactCommandHandler} that activates a tab. */
	public static final String SELECT_TAB_COMMAND = "selectTab";

	private final List<TabDefinition> _tabDefinitions;

	private final LinkedHashMap<String, ReactControl> _contentCache = new LinkedHashMap<>();

	private String _activeTabId;

	private final List<RouteChangeListener> _routeChangeListeners = new ArrayList<>();

	/**
	 * Creates a new {@link ReactTabBarControl}.
	 *
	 * @param model
	 *        The server-side model object.
	 * @param tabDefinitions
	 *        The tab definitions. Must not be empty.
	 * @param initialActiveTabId
	 *        The initially active tab ID, or {@code null} to default to the first tab.
	 */
	public ReactTabBarControl(ReactContext context, Object model, List<TabDefinition> tabDefinitions, String initialActiveTabId) {
		super(context, model, REACT_MODULE);
		_tabDefinitions = new ArrayList<>(tabDefinitions);
		_activeTabId = initialActiveTabId != null ? initialActiveTabId : tabDefinitions.get(0).getId();

		// Build the static tab list for the React component.
		List<Map<String, Object>> tabList = new ArrayList<>();
		for (TabDefinition tab : _tabDefinitions) {
			Map<String, Object> tabInfo = new HashMap<>();
			tabInfo.put(TAB_ID, tab.getId());
			tabInfo.put(TAB_LABEL, tab.getLabel());
			if (tab.getIcon() != null) {
				tabInfo.put(TAB_ICON, tab.getIcon());
			}
			tabList.add(tabInfo);
		}
		putState(TABS, tabList);
		putState(ACTIVE_TAB_ID, _activeTabId);
		// activeContent is null until writeAsChild creates it.
	}

	/**
	 * Creates a new {@link ReactTabBarControl} with the first tab active.
	 */
	public ReactTabBarControl(ReactContext context, Object model, List<TabDefinition> tabDefinitions) {
		this(context, model, tabDefinitions, null);
	}

	@Override
	protected void writeAsChild(JsonWriter writer)
			throws IOException {
		if (getState(ACTIVE_CONTENT) == null) {
			ReactControl activeContent = getOrCreateContent(_activeTabId);
			putStateSilent(ACTIVE_CONTENT, activeContent);
			if (isAttached()) {
				activeContent.attach();
			}
		}
		super.writeAsChild(writer);
	}

	@Override
	protected void propagateAttach() {
		super.propagateAttach();
		if (_activeTabId != null) {
			ReactControl content = _contentCache.get(_activeTabId);
			if (content != null) {
				content.attach();
			}
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_activeTabId != null) {
			ReactControl content = _contentCache.get(_activeTabId);
			if (content != null) {
				content.detach();
			}
		}
	}

	@Override
	protected void cleanupChildren() {
		if (_activeTabId != null) {
			ReactControl active = _contentCache.get(_activeTabId);
			if (active != null) {
				active.detach();
			}
		}
		for (ReactControl cached : _contentCache.values()) {
			cached.cleanupTree();
		}
		_contentCache.clear();
	}

	/**
	 * Selects the tab with the given ID.
	 *
	 * <p>
	 * If the tab's content has not been created yet, it is lazily created and cached.
	 * </p>
	 *
	 * @param tabId
	 *        The ID of the tab to select.
	 */
	public void selectTab(String tabId) {
		if (tabId.equals(_activeTabId)) {
			return;
		}
		ReactControl previousContent = _contentCache.get(_activeTabId);
		_activeTabId = tabId;

		if (!isSSEAttached()) {
			// Not yet rendered; just update state for deferred rendering.
			putStateSilent(ACTIVE_TAB_ID, _activeTabId);
			return;
		}

		ReactControl content = getOrCreateContent(tabId);

		Object tx = beginUpdate();
		putState(ACTIVE_TAB_ID, tabId);
		putState(ACTIVE_CONTENT, content);
		commitUpdate(tx);

		if (previousContent != null) {
			previousContent.detach();
		}
		if (isAttached()) {
			content.attach();
		}

		// After successful selection, notify route listeners.
		TabDefinition newTab = findTab(tabId);
		if (newTab.getRoute() != null) {
			RoutePattern pattern = RoutePattern.compile(newTab.getRoute(), newTab.getId());
			RouteSegment segment = new RouteSegment(pattern.produce(Map.of()));
			for (RouteChangeListener listener : new ArrayList<>(_routeChangeListeners)) {
				listener.onRouteChange(this, segment);
			}
		}
	}

	private ReactControl getOrCreateContent(String tabId) {
		ReactControl cached = _contentCache.get(tabId);
		if (cached != null) {
			return cached;
		}

		TabDefinition tabDef = findTab(tabId);
		ReactControl content = tabDef.getContentFactory().get();
		_contentCache.put(tabId, content);
		return content;
	}

	private TabDefinition findTab(String tabId) {
		for (TabDefinition tab : _tabDefinitions) {
			if (tab.getId().equals(tabId)) {
				return tab;
			}
		}
		throw new IllegalArgumentException("Unknown tab ID: " + tabId);
	}

	// -- RoutingParticipant --

	@Override
	public List<RoutePattern> declaredRoutes() {
		List<RoutePattern> routes = new ArrayList<>();
		for (TabDefinition tab : _tabDefinitions) {
			String route = tab.getRoute();
			if (route != null) {
				routes.add(RoutePattern.compile(route, tab.getId()));
			}
		}
		return routes;
	}

	@Override
	public void activateRoute(RouteMatch match) {
		selectTab(match.itemId());
	}

	@Override
	public RouteSegment activeRouteSegment() {
		TabDefinition activeTab = findTab(_activeTabId);
		if (activeTab.getRoute() != null) {
			RoutePattern pattern = RoutePattern.compile(activeTab.getRoute(), activeTab.getId());
			return new RouteSegment(pattern.produce(Map.of()));
		}
		return null;
	}

	@Override
	public void addRouteChangeListener(RouteChangeListener listener) {
		_routeChangeListeners.add(listener);
	}

	@Override
	public void removeRouteChangeListener(RouteChangeListener listener) {
		_routeChangeListeners.remove(listener);
	}

	// -- Commands --

	/**
	 * Handles tab selection from the client.
	 */
	@ReactCommandHandler(SELECT_TAB_COMMAND)
	void handleSelectTab(SelectTabArguments args) {
		String tabId = args.getTabId();

		// Check for dirty forms in the current tab before switching.
		TabDefinition currentTab = findTab(_activeTabId);
		DirtyChannel dirtyChannel = currentTab.getDirtyChannel();
		if (dirtyChannel != null && dirtyChannel.hasDirtyHandlers()) {
			throw new ChannelVetoException(dirtyChannel.getDirtyHandlers(), () -> selectTab(tabId));
		}

		selectTab(tabId);
	}

	/**
	 * Addresses the active tab's content by the tab's stable ID (e.g. {@code tab[overview]}), so
	 * content addresses encode which tab they belong to.
	 */
	@Override
	public String agentChildSlot(ReactControl child) {
		if (child == getState(ACTIVE_CONTENT)) {
			return AgentControl.slotSegment("tab", _activeTabId);
		}
		return null;
	}

}
