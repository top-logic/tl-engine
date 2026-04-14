/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.dirty.ChannelVetoException;
import com.top_logic.layout.react.dirty.DirtyChannel;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.json.JsonWriter;

/**
 * A {@link ReactControl} that renders a sidebar with navigation items, command buttons, collapsible
 * groups, and optional header/footer slots.
 *
 * <p>
 * Navigation item content is created lazily on first selection and cached for instant re-selection.
 * State changes (collapse toggle, group expansion) are reported via callbacks so the caller can
 * handle persistence.
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
 * <li>{@code headerContent} - optional header slot child control descriptor (expanded mode)</li>
 * <li>{@code headerCollapsedContent} - optional header slot child control descriptor (collapsed
 * mode)</li>
 * <li>{@code footerContent} - optional footer slot child control descriptor (expanded mode)</li>
 * <li>{@code footerCollapsedContent} - optional footer slot child control descriptor (collapsed
 * mode)</li>
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

	private static final String HEADER_COLLAPSED_CONTENT = "headerCollapsedContent";

	private static final String FOOTER_COLLAPSED_CONTENT = "footerCollapsedContent";

	private static final String ITEM_ID_ARG = "itemId";

	private static final String EXPANDED_ARG = "expanded";

	private final Consumer<Boolean> _onCollapseChanged;

	private final BiConsumer<String, Boolean> _onGroupToggled;

	private final Map<String, Boolean> _groupStates;

	private final List<SidebarItem> _items;

	private final ReactControl _headerContent;

	private final ReactControl _headerCollapsedContent;

	private final ReactControl _footerContent;

	private final ReactControl _footerCollapsedContent;

	private final LinkedHashMap<String, ReactControl> _contentCache = new LinkedHashMap<>();

	private String _activeItemId;

	private boolean _collapsed;

	/**
	 * Creates a new {@link ReactSidebarControl}.
	 *
	 * @param items
	 *        The navigation item list.
	 * @param initialActiveItemId
	 *        The initially active navigation item ID, or {@code null} to default to the first
	 *        navigation item.
	 * @param initialCollapsed
	 *        The initial collapsed state (pre-loaded by the caller).
	 * @param initialGroupStates
	 *        Pre-loaded group expansion state overrides. Keys are group IDs, values are expanded
	 *        flags. May be {@code null} or empty for defaults.
	 * @param onCollapseChanged
	 *        Called when the user toggles the sidebar collapse. Receives the new collapsed state.
	 *        May be {@code null} if no persistence is desired.
	 * @param onGroupToggled
	 *        Called when the user toggles a group's expansion. Receives ({@code groupId}, expanded).
	 *        May be {@code null} if no persistence is desired.
	 * @param headerContent
	 *        Optional header slot control shown when expanded, or {@code null}.
	 * @param headerCollapsedContent
	 *        Optional header slot control shown when collapsed, or {@code null}.
	 * @param footerContent
	 *        Optional footer slot control shown when expanded, or {@code null}.
	 * @param footerCollapsedContent
	 *        Optional footer slot control shown when collapsed, or {@code null}.
	 */
	public ReactSidebarControl(ReactContext context, List<SidebarItem> items, String initialActiveItemId,
			boolean initialCollapsed, Map<String, Boolean> initialGroupStates,
			Consumer<Boolean> onCollapseChanged, BiConsumer<String, Boolean> onGroupToggled,
			ReactControl headerContent, ReactControl headerCollapsedContent,
			ReactControl footerContent, ReactControl footerCollapsedContent) {
		super(context, null, REACT_MODULE);
		_items = new ArrayList<>(items);
		_collapsed = initialCollapsed;
		_onCollapseChanged = onCollapseChanged;
		_onGroupToggled = onGroupToggled;
		_headerContent = headerContent;
		_headerCollapsedContent = headerCollapsedContent;
		_footerContent = footerContent;
		_footerCollapsedContent = footerCollapsedContent;

		// Track group states in memory for pushItemsUpdate().
		Map<String, Boolean> groupStates =
			initialGroupStates != null ? initialGroupStates : Collections.emptyMap();
		_groupStates = new HashMap<>(groupStates);

		// Determine initial active item.
		_activeItemId = initialActiveItemId != null ? initialActiveItemId : findFirstNavItemId(items);

		// Build serialized item list, merging pre-loaded group states.
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (SidebarItem item : _items) {
			Map<String, Object> itemMap = item.toStateMap();
			if (item instanceof GroupItem) {
				Boolean persisted = groupStates.get(item.getId());
				if (persisted != null) {
					itemMap.put(GroupItem.EXPANDED, persisted);
				}
			}
			itemList.add(itemMap);
		}

		putState(ITEMS, itemList);
		putState(ACTIVE_ITEM_ID, _activeItemId);
		putState(COLLAPSED, Boolean.valueOf(_collapsed));
		if (_headerContent != null) {
			putState(HEADER_CONTENT, _headerContent);
		}
		if (_headerCollapsedContent != null) {
			putState(HEADER_COLLAPSED_CONTENT, _headerCollapsedContent);
		}
		if (_footerContent != null) {
			putState(FOOTER_CONTENT, _footerContent);
		}
		if (_footerCollapsedContent != null) {
			putState(FOOTER_COLLAPSED_CONTENT, _footerCollapsedContent);
		}
	}

	/**
	 * Convenience constructor without collapsed-mode slot alternatives and no callbacks.
	 */
	public ReactSidebarControl(ReactContext context, List<SidebarItem> items, String initialActiveItemId,
			boolean initialCollapsed, ReactControl headerContent, ReactControl footerContent) {
		this(context, items, initialActiveItemId, initialCollapsed, null, null, null,
			headerContent, null, footerContent, null);
	}

	@Override
	protected void writeAsChild(JsonWriter writer)
			throws IOException {
		if (getState(ACTIVE_CONTENT) == null && _activeItemId != null) {
			ReactControl activeContent = getOrCreateContent(_activeItemId);
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
		if (_headerContent != null) {
			_headerContent.attach();
		}
		if (_headerCollapsedContent != null) {
			_headerCollapsedContent.attach();
		}
		if (_footerContent != null) {
			_footerContent.attach();
		}
		if (_activeItemId != null) {
			ReactControl content = _contentCache.get(_activeItemId);
			if (content != null) {
				content.attach();
			}
		}
	}

	@Override
	protected void propagateDetach() {
		super.propagateDetach();
		if (_headerContent != null) {
			_headerContent.detach();
		}
		if (_headerCollapsedContent != null) {
			_headerCollapsedContent.detach();
		}
		if (_footerContent != null) {
			_footerContent.detach();
		}
		if (_activeItemId != null) {
			ReactControl content = _contentCache.get(_activeItemId);
			if (content != null) {
				content.detach();
			}
		}
	}

	@Override
	protected void cleanupChildren() {
		if (_activeItemId != null) {
			ReactControl active = _contentCache.get(_activeItemId);
			if (active != null) {
				active.detach();
			}
		}
		for (ReactControl cached : _contentCache.values()) {
			cached.cleanupTree();
		}
		_contentCache.clear();
		if (_headerContent != null) {
			_headerContent.cleanupTree();
		}
		if (_headerCollapsedContent != null) {
			_headerCollapsedContent.cleanupTree();
		}
		if (_footerContent != null) {
			_footerContent.cleanupTree();
		}
		if (_footerCollapsedContent != null) {
			_footerCollapsedContent.cleanupTree();
		}
	}

	/**
	 * Selects the navigation item with the given ID.
	 */
	public void selectItem(String itemId) {
		if (itemId.equals(_activeItemId)) {
			return;
		}
		ReactControl previousContent = _contentCache.get(_activeItemId);
		_activeItemId = itemId;

		if (!isSSEAttached()) {
			putStateSilent(ACTIVE_ITEM_ID, _activeItemId);
			return;
		}

		ReactControl content = getOrCreateContent(itemId);

		Object tx = beginUpdate();
		putState(ACTIVE_ITEM_ID, itemId);
		putState(ACTIVE_CONTENT, content);
		commitUpdate(tx);

		if (previousContent != null) {
			previousContent.detach();
		}
		if (isAttached()) {
			content.attach();
		}
	}

	/**
	 * Updates the badge on a navigation item.
	 *
	 * @param itemId
	 *        The ID of the navigation item to update.
	 * @param badge
	 *        The new badge text, or {@code null} to remove the badge.
	 */
	public void updateBadge(String itemId, String badge) {
		NavigationItem navItem = findNavItem(itemId, _items);
		if (navItem == null) {
			return;
		}
		navItem.setBadge(badge);
		pushItemsUpdate();
	}

	private void pushItemsUpdate() {
		List<Map<String, Object>> itemList = new ArrayList<>();
		for (SidebarItem item : _items) {
			Map<String, Object> itemMap = item.toStateMap();
			if (item instanceof GroupItem) {
				Boolean tracked = _groupStates.get(item.getId());
				if (tracked != null) {
					itemMap.put(GroupItem.EXPANDED, tracked);
				}
			}
			itemList.add(itemMap);
		}

		if (isSSEAttached()) {
			putState(ITEMS, itemList);
		} else {
			putStateSilent(ITEMS, itemList);
		}
	}

	/**
	 * Finds a sidebar item by ID, searching recursively into groups.
	 *
	 * @param itemId
	 *        The item ID to search for.
	 * @return The item, or {@code null} if not found.
	 */
	public SidebarItem findItem(String itemId) {
		return findItemRecursive(itemId, _items);
	}

	private static SidebarItem findItemRecursive(String itemId, List<SidebarItem> items) {
		for (SidebarItem item : items) {
			if (item.getId().equals(itemId)) {
				return item;
			}
			if (item instanceof GroupItem) {
				SidebarItem found = findItemRecursive(itemId, ((GroupItem) item).getChildren());
				if (found != null) {
					return found;
				}
			}
		}
		return null;
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

	// -- Commands --

	/**
	 * Handles navigation item selection from the client.
	 */
	@ReactCommand("selectItem")
	void handleSelectItem(Map<String, Object> arguments) {
		String itemId = (String) arguments.get(ITEM_ID_ARG);

		// Check for dirty forms in the current sidebar item before switching.
		NavigationItem currentItem = findNavItem(_activeItemId, _items);
		if (currentItem != null) {
			DirtyChannel dirtyChannel = currentItem.getDirtyChannel();
			if (dirtyChannel != null && dirtyChannel.hasDirtyHandlers()) {
				throw new ChannelVetoException(dirtyChannel.getDirtyHandlers(), () -> selectItem(itemId));
			}
		}

		selectItem(itemId);
	}

	/**
	 * Handles command item execution from the client.
	 */
	@ReactCommand("executeCommand")
	HandlerResult handleExecuteCommand(ReactContext context, Map<String, Object> arguments) {
		String itemId = (String) arguments.get(ITEM_ID_ARG);
		CommandItem cmdItem = findCommandItem(itemId, _items);
		if (cmdItem != null) {
			return cmdItem.getAction().execute(context);
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Handles collapse toggle from the client.
	 */
	@ReactCommand("toggleCollapse")
	void handleToggleCollapse() {
		_collapsed = !_collapsed;
		if (_onCollapseChanged != null) {
			_onCollapseChanged.accept(Boolean.valueOf(_collapsed));
		}
		putState(COLLAPSED, Boolean.valueOf(_collapsed));
	}

	/**
	 * Handles group expand/collapse toggle from the client.
	 */
	@ReactCommand("toggleGroup")
	void handleToggleGroup(Map<String, Object> arguments) {
		String itemId = (String) arguments.get(ITEM_ID_ARG);
		Object expandedObj = arguments.get(EXPANDED_ARG);
		boolean expanded = expandedObj instanceof Boolean && ((Boolean) expandedObj).booleanValue();
		_groupStates.put(itemId, Boolean.valueOf(expanded));
		if (_onGroupToggled != null) {
			_onGroupToggled.accept(itemId, Boolean.valueOf(expanded));
		}
	}

}
