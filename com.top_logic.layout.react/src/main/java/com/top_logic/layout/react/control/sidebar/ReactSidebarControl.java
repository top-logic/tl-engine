/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.SSEUpdateQueue;
import com.top_logic.layout.structure.PersonalizingExpandable;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A {@link ReactControl} that renders a sidebar with navigation items, command buttons, collapsible
 * groups, and optional header/footer slots.
 *
 * <p>
 * Navigation item content is created lazily on first selection and cached for instant re-selection.
 * The sidebar collapse state and group expansion states are persisted in
 * {@link PersonalConfiguration}.
 * </p>
 *
 * <p>
 * The React component {@code TLSidebar} receives the following state:
 * </p>
 * <ul>
 * <li>{@code items} - serialized item list</li>
 * <li>{@code activeItemId} - the currently selected navigation item ID</li>
 * <li>{@code collapsed} - whether the sidebar nav panel is collapsed</li>
 * <li>{@code activeContent} - the active item's content as a child control descriptor</li>
 * <li>{@code headerContent} - optional header slot child control descriptor</li>
 * <li>{@code footerContent} - optional footer slot child control descriptor</li>
 * </ul>
 */
public class ReactSidebarControl extends ReactControl {

	private static final String REACT_MODULE = "TLSidebar";

	private static final String ITEMS = "items";

	private static final String ACTIVE_ITEM_ID = "activeItemId";

	private static final String COLLAPSED = "collapsed";

	private static final String ACTIVE_CONTENT = "activeContent";

	private static final String HEADER_CONTENT = "headerContent";

	private static final String FOOTER_CONTENT = "footerContent";

	private static final String ITEM_ID_ARG = "itemId";

	private static final String EXPANDED_ARG = "expanded";

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		new SelectItemCommand(),
		new ExecuteCommandCommand(),
		new ToggleCollapseCommand(),
		new ToggleGroupCommand());

	private final String _personalizationKey;

	private final List<SidebarItem> _items;

	private final boolean _defaultCollapsed;

	private final ReactControl _headerContent;

	private final ReactControl _footerContent;

	private final LinkedHashMap<String, ReactControl> _contentCache = new LinkedHashMap<>();

	private String _activeItemId;

	private boolean _collapsed;

	/**
	 * Creates a new {@link ReactSidebarControl}.
	 *
	 * @param personalizationKey
	 *        Key prefix for {@link PersonalConfiguration} storage. The caller typically derives
	 *        this from the component name.
	 * @param items
	 *        The navigation item list.
	 * @param initialActiveItemId
	 *        The initially active navigation item ID, or {@code null} to default to the first
	 *        navigation item.
	 * @param defaultCollapsed
	 *        Default collapse state (before personal config override).
	 * @param headerContent
	 *        Optional header slot control, or {@code null}.
	 * @param footerContent
	 *        Optional footer slot control, or {@code null}.
	 */
	public ReactSidebarControl(String personalizationKey, List<SidebarItem> items, String initialActiveItemId,
			boolean defaultCollapsed, ReactControl headerContent, ReactControl footerContent) {
		super(null, REACT_MODULE, COMMANDS);
		_personalizationKey = personalizationKey;
		_items = new ArrayList<>(items);
		_defaultCollapsed = defaultCollapsed;
		_headerContent = headerContent;
		_footerContent = footerContent;

		// Load persisted collapse state.
		_collapsed = PersonalizingExpandable.loadCollapsed(personalizationKey + ".collapsed", defaultCollapsed);

		// Load persisted group states.
		Map<String, Boolean> persistedGroups = loadGroupStates();

		// Determine initial active item.
		_activeItemId = initialActiveItemId != null ? initialActiveItemId : findFirstNavItemId(items);

		// Build serialized item list, merging persisted group states.
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (SidebarItem item : _items) {
			Map<String, Object> itemMap = item.toStateMap();
			if (item instanceof GroupItem && persistedGroups != null) {
				Boolean persisted = persistedGroups.get(item.getId());
				if (persisted != null) {
					itemMap.put(GroupItem.EXPANDED, persisted);
				}
			}
			itemList.add(itemMap);
		}

		getReactState().put(ITEMS, itemList);
		getReactState().put(ACTIVE_ITEM_ID, _activeItemId);
		getReactState().put(COLLAPSED, Boolean.valueOf(_collapsed));
		if (_headerContent != null) {
			getReactState().put(HEADER_CONTENT, _headerContent);
		}
		if (_footerContent != null) {
			getReactState().put(FOOTER_CONTENT, _footerContent);
		}
		// activeContent is null until writeAsChild creates it.
	}

	@Override
	protected void writeAsChild(JsonWriter writer, FrameScope frameScope, SSEUpdateQueue queue)
			throws IOException {
		if (getReactState().get(ACTIVE_CONTENT) == null && _activeItemId != null) {
			ReactControl activeContent = getOrCreateContent(_activeItemId);
			getReactState().put(ACTIVE_CONTENT, activeContent);
		}
		super.writeAsChild(writer, frameScope, queue);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl cached : _contentCache.values()) {
			cached.cleanupTree();
		}
		_contentCache.clear();
		if (_headerContent != null) {
			_headerContent.cleanupTree();
		}
		if (_footerContent != null) {
			_footerContent.cleanupTree();
		}
	}

	/**
	 * Selects the navigation item with the given ID.
	 */
	public void selectItem(String itemId) {
		if (itemId.equals(_activeItemId)) {
			return;
		}
		_activeItemId = itemId;

		if (!isSSEAttached()) {
			getReactState().put(ACTIVE_ITEM_ID, _activeItemId);
			return;
		}

		ReactControl content = getOrCreateContent(itemId);

		Map<String, Object> patch = new HashMap<>();
		patch.put(ACTIVE_ITEM_ID, itemId);
		patch.put(ACTIVE_CONTENT, content);
		patchReactState(patch);
	}

	private ReactControl getOrCreateContent(String itemId) {
		ReactControl cached = _contentCache.get(itemId);
		if (cached != null) {
			return cached;
		}

		NavigationItem navItem = findNavItem(itemId, _items);
		ReactControl content = navItem.getContentFactory().get();
		_contentCache.put(itemId, content);
		return content;
	}

	private NavigationItem findNavItem(String itemId, List<SidebarItem> items) {
		for (SidebarItem item : items) {
			if (item instanceof NavigationItem && item.getId().equals(itemId)) {
				return (NavigationItem) item;
			}
			if (item instanceof GroupItem) {
				NavigationItem found = findNavItem(itemId, ((GroupItem) item).getChildren());
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private CommandItem findCommandItem(String itemId, List<SidebarItem> items) {
		for (SidebarItem item : items) {
			if (item instanceof CommandItem && item.getId().equals(itemId)) {
				return (CommandItem) item;
			}
			if (item instanceof GroupItem) {
				CommandItem found = findCommandItem(itemId, ((GroupItem) item).getChildren());
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	private static String findFirstNavItemId(List<SidebarItem> items) {
		for (SidebarItem item : items) {
			if (item instanceof NavigationItem) {
				return item.getId();
			}
			if (item instanceof GroupItem) {
				String found = findFirstNavItemId(((GroupItem) item).getChildren());
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Map<String, Boolean> loadGroupStates() {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return null;
		}
		Object value = pc.getJSONValue(_personalizationKey + ".groups");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<String, Boolean> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				if (entry.getValue() instanceof Boolean) {
					result.put(entry.getKey(), (Boolean) entry.getValue());
				}
			}
			return result;
		}
		return null;
	}

	private void saveGroupState(String groupId, boolean expanded) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}

		// Load current persisted states.
		Map<String, Boolean> states = loadGroupStates();
		if (states == null) {
			states = new HashMap<>();
		}

		// Find the group's default value.
		GroupItem group = findGroup(groupId, _items);
		if (group != null && expanded == group.isInitiallyExpanded()) {
			// Value matches default; remove from personalization.
			states.remove(groupId);
		} else {
			states.put(groupId, Boolean.valueOf(expanded));
		}

		// Store or clear.
		if (states.isEmpty()) {
			pc.setJSONValue(_personalizationKey + ".groups", null);
		} else {
			pc.setJSONValue(_personalizationKey + ".groups", states);
		}
	}

	private GroupItem findGroup(String groupId, List<SidebarItem> items) {
		for (SidebarItem item : items) {
			if (item instanceof GroupItem && item.getId().equals(groupId)) {
				return (GroupItem) item;
			}
		}
		return null;
	}

	/**
	 * Command sent by the React client when a navigation item is clicked.
	 */
	public static class SelectItemCommand extends ControlCommand {

		static final String COMMAND = "selectItem";

		/** Creates a new {@link SelectItemCommand}. */
		public SelectItemCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_SIDEBAR_ITEM_SELECTED;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSidebarControl sidebar = (ReactSidebarControl) control;
			String itemId = (String) arguments.get(ITEM_ID_ARG);
			sidebar.selectItem(itemId);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command sent by the React client when a command item is clicked.
	 */
	public static class ExecuteCommandCommand extends ControlCommand {

		static final String COMMAND = "executeCommand";

		/** Creates a new {@link ExecuteCommandCommand}. */
		public ExecuteCommandCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_SIDEBAR_COMMAND_EXECUTED;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSidebarControl sidebar = (ReactSidebarControl) control;
			String itemId = (String) arguments.get(ITEM_ID_ARG);
			CommandItem cmdItem = sidebar.findCommandItem(itemId, sidebar._items);
			if (cmdItem != null) {
				return cmdItem.getAction().execute(context);
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command sent by the React client when the collapse toggle is clicked.
	 */
	public static class ToggleCollapseCommand extends ControlCommand {

		static final String COMMAND = "toggleCollapse";

		/** Creates a new {@link ToggleCollapseCommand}. */
		public ToggleCollapseCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_SIDEBAR_COLLAPSE_TOGGLED;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSidebarControl sidebar = (ReactSidebarControl) control;
			sidebar._collapsed = !sidebar._collapsed;
			PersonalizingExpandable.saveCollapsed(
				sidebar._personalizationKey + ".collapsed", sidebar._collapsed, sidebar._defaultCollapsed);
			sidebar.patchReactState(Map.of(COLLAPSED, Boolean.valueOf(sidebar._collapsed)));
			return HandlerResult.DEFAULT_RESULT;
		}
	}

	/**
	 * Command sent by the React client when a group's expand/collapse is toggled.
	 */
	public static class ToggleGroupCommand extends ControlCommand {

		static final String COMMAND = "toggleGroup";

		/** Creates a new {@link ToggleGroupCommand}. */
		public ToggleGroupCommand() {
			super(COMMAND);
		}

		@Override
		public com.top_logic.basic.util.ResKey getI18NKey() {
			return I18NConstants.REACT_SIDEBAR_GROUP_TOGGLED;
		}

		@Override
		protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
			ReactSidebarControl sidebar = (ReactSidebarControl) control;
			String itemId = (String) arguments.get(ITEM_ID_ARG);
			Object expandedObj = arguments.get(EXPANDED_ARG);
			boolean expanded = expandedObj instanceof Boolean && ((Boolean) expandedObj).booleanValue();
			sidebar.saveGroupState(itemId, expanded);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
