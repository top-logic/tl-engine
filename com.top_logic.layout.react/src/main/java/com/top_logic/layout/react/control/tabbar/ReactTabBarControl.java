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
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;

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
public class ReactTabBarControl extends ReactControl {

	private static final String REACT_MODULE = "TLTabBar";

	private static final String TABS = "tabs";

	private static final String ACTIVE_TAB_ID = "activeTabId";

	private static final String ACTIVE_CONTENT = "activeContent";

	/** Tab info key for the tab identifier. */
	private static final String TAB_ID = "id";

	/** Tab info key for the tab display label. */
	private static final String TAB_LABEL = "label";

	/** Command argument key for the selected tab ID. */
	private static final String TAB_ID_ARG = "tabId";

	private final List<TabDefinition> _tabDefinitions;

	private final LinkedHashMap<String, ReactControl> _contentCache = new LinkedHashMap<>();

	private String _activeTabId;

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
		if (getReactState().get(ACTIVE_CONTENT) == null) {
			ReactControl activeContent = getOrCreateContent(_activeTabId);
			getReactState().put(ACTIVE_CONTENT, activeContent);
		}
		super.writeAsChild(writer);
	}

	@Override
	protected void cleanupChildren() {
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
		_activeTabId = tabId;

		if (!isSSEAttached()) {
			// Not yet rendered; just update state for deferred rendering.
			getReactState().put(ACTIVE_TAB_ID, _activeTabId);
			return;
		}

		ReactControl content = getOrCreateContent(tabId);

		Map<String, Object> patch = new HashMap<>();
		patch.put(ACTIVE_TAB_ID, tabId);
		patch.put(ACTIVE_CONTENT, content);
		patchReactState(patch);
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

	// -- Commands --

	/**
	 * Handles tab selection from the client.
	 */
	@ReactCommand("selectTab")
	void handleSelectTab(Map<String, Object> arguments) {
		String tabId = (String) arguments.get(TAB_ID_ARG);
		selectTab(tabId);
	}

}
