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
 * <li>{@code collapsed} - whether the desktop rail is collapsed (persisted user preference)</li>
 * <li>{@code drawerOpen} - whether the mobile off-canvas drawer is currently shown
 * (transient, always starts {@code false} on page load)</li>
 * <li>{@code activeContent} - the active item's content as a child control descriptor</li>
 * <li>{@code headerContent} - optional header slot child control descriptor (expanded mode)</li>
 * <li>{@code headerCollapsedContent} - optional header slot child control descriptor (collapsed
 * mode)</li>
 * <li>{@code footerContent} - optional footer slot child control descriptor (expanded mode)</li>
 * <li>{@code footerCollapsedContent} - optional footer slot child control descriptor (collapsed
 * mode)</li>
 * <li>{@code drawerToggleContribution} - optional hidden child control descriptor whose
 * {@code SlotContentControl} contributes a drawer-toggle button to {@code <slot
 * name="appbar-leading"/>} (set via {@link #setDrawerToggleContribution(ReactControl)})</li>
 * </ul>
 */
public class ReactSidebarControl extends ReactControl implements RoutingParticipant {

	private static final String REACT_MODULE = "TLSidebar";

	private static final String ITEMS = "items";

	private static final String ACTIVE_ITEM_ID = "activeItemId";

	private static final String COLLAPSED = "collapsed";

	private static final String DRAWER_OPEN = "drawerOpen";

	private static final String ACTIVE_CONTENT = "activeContent";

	private static final String HEADER_CONTENT = "headerContent";

	private static final String FOOTER_CONTENT = "footerContent";

	private static final String HEADER_COLLAPSED_CONTENT = "headerCollapsedContent";

	private static final String FOOTER_COLLAPSED_CONTENT = "footerCollapsedContent";

	private static final String DRAWER_TOGGLE_CONTRIBUTION = "drawerToggleContribution";

	/** The {@link ReactCommandHandler} that selects a navigation item. */
	public static final String SELECT_ITEM_COMMAND = "selectItem";

	/** The {@link ReactCommandHandler} that executes a sidebar command item. */
	public static final String EXECUTE_COMMAND_COMMAND = "executeCommand";

	/** The {@link ReactCommandHandler} that expands or collapses a sidebar group. */
	public static final String TOGGLE_GROUP_COMMAND = "toggleGroup";

	private final Consumer<Boolean> _onCollapseChanged;

	private final BiConsumer<String, Boolean> _onGroupToggled;

	private final Map<String, Boolean> _groupStates;

	private final List<SidebarItem> _items;

	private final ReactControl _headerContent;

	private final ReactControl _headerCollapsedContent;

	private final ReactControl _footerContent;

	private final ReactControl _footerCollapsedContent;

	private ReactControl _drawerToggleContribution;

	private final LinkedHashMap<String, ReactControl> _contentCache = new LinkedHashMap<>();

	private String _activeItemId;

	private boolean _collapsed;

	private boolean _drawerOpen;

	private final List<RouteChangeListener> _routeChangeListeners = new ArrayList<>();

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
		putState(DRAWER_OPEN, Boolean.valueOf(_drawerOpen));
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
	 * Installs a control that the sidebar carries as an invisible child so its slot-content
	 * contribution registers with the same lifecycle as the sidebar. Must be called before the
	 * sidebar is rendered.
	 *
	 * <p>
	 * Typical use: the {@code SidebarElement} creates a {@code SlotContentControl} containing a
	 * drawer-toggle button targeting {@code <slot name="appbar-leading"/>}; the sidebar then owns
	 * its attach/detach lifecycle so the contribution registers when the sidebar mounts.
	 * </p>
	 */
	public void setDrawerToggleContribution(ReactControl contribution) {
		_drawerToggleContribution = contribution;
		if (contribution != null) {
			putState(DRAWER_TOGGLE_CONTRIBUTION, contribution);
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
		if (_drawerToggleContribution != null) {
			_drawerToggleContribution.attach();
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
		if (_drawerToggleContribution != null) {
			_drawerToggleContribution.detach();
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
		if (_drawerToggleContribution != null) {
			_drawerToggleContribution.cleanupTree();
		}
	}

	/**
	 * Selects the navigation item with the given ID.
	 */
	public void selectItem(String itemId) {
		if (itemId.equals(_activeItemId)) {
			// No navigation, but on mobile the user just picked something from the open drawer -
			// fold the drawer away so the previously selected view stays visible.
			closeDrawerIfOpen();
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
		closeDrawerIfOpen();
		commitUpdate(tx);

		if (previousContent != null) {
			previousContent.detach();
		}
		if (isAttached()) {
			content.attach();
		}

		// After successful selection, notify route listeners.
		NavigationItem newItem = findNavItem(_activeItemId, _items);
		if (newItem != null && newItem.getRoute() != null) {
			RoutePattern pattern = RoutePattern.compile(newItem.getRoute(), newItem.getId());
			RouteSegment segment = new RouteSegment(pattern.produce(Map.of()));
			for (RouteChangeListener listener : new ArrayList<>(_routeChangeListeners)) {
				listener.onRouteChange(this, segment);
			}
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

	// -- RoutingParticipant --

	@Override
	public List<RoutePattern> declaredRoutes() {
		List<RoutePattern> routes = new ArrayList<>();
		collectRoutes(routes, _items);
		return routes;
	}

	private void collectRoutes(List<RoutePattern> routes, List<SidebarItem> items) {
		for (SidebarItem item : items) {
			if (item instanceof NavigationItem) {
				String route = ((NavigationItem) item).getRoute();
				if (route != null) {
					routes.add(RoutePattern.compile(route, item.getId()));
				}
			}
			if (item instanceof GroupItem) {
				collectRoutes(routes, ((GroupItem) item).getChildren());
			}
		}
	}

	@Override
	public void activateRoute(RouteMatch match) {
		selectItem(match.itemId());
	}

	@Override
	public RouteSegment activeRouteSegment() {
		NavigationItem activeItem = findNavItem(_activeItemId, _items);
		if (activeItem != null && activeItem.getRoute() != null) {
			RoutePattern pattern = RoutePattern.compile(activeItem.getRoute(), activeItem.getId());
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
	 * Handles navigation item selection from the client.
	 */
	@ReactCommandHandler(SELECT_ITEM_COMMAND)
	void handleSelectItem(SelectItemArguments args) {
		String itemId = args.getItemId();

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
	@ReactCommandHandler(EXECUTE_COMMAND_COMMAND)
	HandlerResult handleExecuteCommand(ReactContext context, ExecuteCommandArguments args) {
		String itemId = args.getItemId();
		CommandItem cmdItem = findCommandItem(itemId, _items);
		if (cmdItem != null) {
			HandlerResult result = cmdItem.getAction().execute(context);
			closeDrawerIfOpen();
			return result;
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Closes the mobile drawer if it is currently open; no-op otherwise.
	 *
	 * <p>
	 * Called after a nav-item selection or command-item execution: the user has indicated
	 * their next intent so the drawer should fold itself away. On desktop {@code _drawerOpen}
	 * is always {@code false}, making this a no-op there.
	 * </p>
	 */
	private void closeDrawerIfOpen() {
		if (_drawerOpen) {
			_drawerOpen = false;
			putState(DRAWER_OPEN, Boolean.valueOf(_drawerOpen));
		}
	}

	/**
	 * Toggles the desktop rail's collapsed state (persisted user preference).
	 *
	 * <p>
	 * Triggered by the user clicking the in-rail collapse button. On mobile this state is unused;
	 * the mobile drawer's visibility is driven by {@link #toggleDrawer()} instead.
	 * </p>
	 */
	public void toggleCollapse() {
		_collapsed = !_collapsed;
		if (_onCollapseChanged != null) {
			_onCollapseChanged.accept(Boolean.valueOf(_collapsed));
		}
		putState(COLLAPSED, Boolean.valueOf(_collapsed));
	}

	/**
	 * Handles collapse toggle from the client.
	 */
	@ReactCommandHandler(value = "toggleCollapse", technical = true)
	void handleToggleCollapse() {
		toggleCollapse();
	}

	/**
	 * Toggles the mobile drawer's open state (transient — not persisted).
	 *
	 * <p>
	 * Triggered by the hamburger button in the app bar or by a click on the drawer backdrop.
	 * Independent of {@link #toggleCollapse()}, so the desktop rail preference is preserved across
	 * mobile drawer interactions. Always defaults to {@code false} on a fresh page load.
	 * </p>
	 */
	public void toggleDrawer() {
		_drawerOpen = !_drawerOpen;
		putState(DRAWER_OPEN, Boolean.valueOf(_drawerOpen));
	}

	/**
	 * Handles mobile drawer toggle from the client.
	 */
	@ReactCommandHandler(value = "toggleDrawer", technical = true)
	void handleToggleDrawer() {
		toggleDrawer();
	}

	/**
	 * Handles group expand/collapse toggle from the client.
	 */
	@ReactCommandHandler(value = TOGGLE_GROUP_COMMAND, technical = true)
	void handleToggleGroup(ToggleGroupArguments args) {
		String itemId = args.getItemId();
		boolean expanded = args.isExpanded();
		_groupStates.put(itemId, Boolean.valueOf(expanded));
		if (_onGroupToggled != null) {
			_onGroupToggled.accept(itemId, Boolean.valueOf(expanded));
		}
	}

	/**
	 * Addresses the active navigation item's content by the item's stable ID (e.g.
	 * {@code item[administration]}), so content addresses encode which sidebar item they belong to.
	 */
	@Override
	public String agentChildSlot(ReactControl child) {
		if (child == getState(ACTIVE_CONTENT)) {
			return AgentControl.slotSegment("item", _activeItemId);
		}
		return null;
	}

}
