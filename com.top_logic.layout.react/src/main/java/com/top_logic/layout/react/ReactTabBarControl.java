/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

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

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new SelectTabCommand());

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
	public ReactTabBarControl(Object model, List<TabDefinition> tabDefinitions, String initialActiveTabId) {
		super(model, REACT_MODULE, COMMANDS);
		_tabDefinitions = new ArrayList<>(tabDefinitions);
		_activeTabId = initialActiveTabId != null ? initialActiveTabId : tabDefinitions.get(0).getId();

		// Build the static tab list for the React component.
		List<Map<String, Object>> tabList = new ArrayList<>();
		for (TabDefinition tab : _tabDefinitions) {
			Map<String, Object> tabInfo = new HashMap<>();
			tabInfo.put("id", tab.getId());
			tabInfo.put("label", tab.getLabel());
			tabList.add(tabInfo);
		}
		getReactState().put(TABS, tabList);
		getReactState().put(ACTIVE_TAB_ID, _activeTabId);
		// activeContent is null until internalWrite creates it.
	}

	/**
	 * Creates a new {@link ReactTabBarControl} with the first tab active.
	 */
	public ReactTabBarControl(Object model, List<TabDefinition> tabDefinitions) {
		this(model, tabDefinitions, null);
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		FrameScope frameScope = getScope().getFrameScope();

		// Create the active tab's content (lazy init).
		ReactControl activeContent = getOrCreateContent(_activeTabId, frameScope);
		getReactState().put(ACTIVE_CONTENT, activeContent);

		super.internalWrite(context, out);

		// Register all cached content controls with the SSE queue.
		for (ReactControl cached : _contentCache.values()) {
			registerChildControl(cached);
		}
	}

	@Override
	protected void internalDetach() {
		// Unregister all cached content controls (and their nested children) before detaching.
		for (ReactControl cached : _contentCache.values()) {
			forEachChildControl(cached.getReactState(), this::unregisterChildControl);
			unregisterChildControl(cached);
		}
		_contentCache.clear();
		super.internalDetach();
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

		// Check if we have a FrameScope (i.e., we've been rendered).
		FrameScope frameScope = getFrameScopeOrNull();
		if (frameScope == null) {
			// Not yet rendered; just update state for deferred rendering.
			getReactState().put(ACTIVE_TAB_ID, _activeTabId);
			return;
		}

		ReactControl content = getOrCreateContent(tabId, frameScope);
		registerChildControl(content);
		forEachChildControl(content.getReactState(), this::registerChildControl);

		Map<String, Object> patch = new HashMap<>();
		patch.put(ACTIVE_TAB_ID, tabId);
		patch.put(ACTIVE_CONTENT, content);
		patchReactState(patch);
	}

	private ReactControl getOrCreateContent(String tabId, FrameScope frameScope) {
		ReactControl cached = _contentCache.get(tabId);
		if (cached != null) {
			return cached;
		}

		TabDefinition tabDef = findTab(tabId);
		ReactControl content = tabDef.getContentFactory().get();
		content.fetchID(frameScope);
		// Also assign IDs to any nested ReactControl children in the content's state.
		forEachChildControl(content.getReactState(), child -> child.fetchID(frameScope));
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

	private FrameScope getFrameScopeOrNull() {
		try {
			return getScope() != null ? getScope().getFrameScope() : null;
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * Command sent by the React client when a tab is clicked.
	 */
	public static class SelectTabCommand extends ControlCommand {

		static final String COMMAND = "selectTab";

		/** Creates a new {@link SelectTabCommand}. */
		public SelectTabCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_TAB_SELECTED;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactTabBarControl tabBar = (ReactTabBarControl) control;
			String tabId = (String) arguments.get("tabId");
			tabBar.selectTab(tabId);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
