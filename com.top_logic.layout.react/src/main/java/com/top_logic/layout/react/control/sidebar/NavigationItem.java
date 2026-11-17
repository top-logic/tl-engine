/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.react.dirty.DirtyChannel;

/**
 * A sidebar item that switches the content area to a lazily created {@link ReactControl}.
 */
public class NavigationItem extends SidebarItem {

	/** Type discriminator for navigation items. */
	public static final String TYPE_NAV = "nav";

	private final String _label;

	private final String _icon;

	private final Supplier<ReactControl> _contentFactory;

	private final DirtyChannel _dirtyChannel;

	private String _route;

	private boolean _hidden;

	private boolean _finalTitle;

	/**
	 * Creates a new {@link NavigationItem} without a badge.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The icon CSS class (e.g. "bi-speedometer2"), or {@code null} for no icon.
	 * @param contentFactory
	 *        Factory for lazily creating the content control.
	 */
	/**
	 * Creates a new {@link NavigationItem} without a badge or dirty channel.
	 */
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory) {
		this(id, label, icon, contentFactory, null, null);
	}

	/**
	 * Creates a new {@link NavigationItem} with a dirty channel but no badge.
	 */
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory,
			DirtyChannel dirtyChannel) {
		this(id, label, icon, contentFactory, dirtyChannel, null);
	}

	/**
	 * Creates a new {@link NavigationItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The icon CSS class (e.g. "bi-speedometer2"), or {@code null} for no icon.
	 * @param contentFactory
	 *        Factory for lazily creating the content control.
	 * @param badge
	 *        The badge of the item, or {@code null} for no badge.
	 */
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory,
			DirtyChannel dirtyChannel, BadgeDisplay badge) {
		super(id);
		_label = label;
		_icon = icon;
		_contentFactory = contentFactory;
		_dirtyChannel = dirtyChannel;
		setBadge(badge);
	}

	/**
	 * The display label.
	 */
	public String getLabel() {
		return _label;
	}

	/**
	 * The icon CSS class, or {@code null}.
	 */
	public String getIcon() {
		return _icon;
	}

	/**
	 * Factory for lazily creating the content control.
	 */
	public Supplier<ReactControl> getContentFactory() {
		return _contentFactory;
	}

	/**
	 * The dirty channel tracking unsaved changes in this item's content, or {@code null}.
	 */
	public DirtyChannel getDirtyChannel() {
		return _dirtyChannel;
	}

	/**
	 * The route pattern for this item, or {@code null} if this item is not route-forming.
	 */
	public String getRoute() {
		return _route;
	}

	/**
	 * Sets the route pattern for this item.
	 *
	 * @param route
	 *        The route pattern (e.g. "/property/:id"), or {@code null} for no route.
	 * @return This instance for fluent chaining.
	 */
	public NavigationItem withRoute(String route) {
		_route = route;
		return this;
	}

	/**
	 * Whether this item is hidden from the sidebar UI.
	 *
	 * <p>
	 * Hidden items are not displayed in the sidebar but can still be activated programmatically,
	 * e.g. via URL routing.
	 * </p>
	 */
	public boolean isHidden() {
		return _hidden;
	}

	/**
	 * Sets whether this item is hidden from the sidebar UI.
	 *
	 * @param hidden
	 *        {@code true} to hide this item from the sidebar.
	 * @return This instance for fluent chaining.
	 */
	public NavigationItem withHidden(boolean hidden) {
		_hidden = hidden;
		return this;
	}

	/**
	 * Whether the label of this item is the final title of the page while the item is selected.
	 *
	 * <p>
	 * Only meaningful for an item with a {@link #getRoute() route}: an item without one names no
	 * page.
	 * </p>
	 *
	 * @see com.top_logic.layout.react.routing.RoutingParticipant#titlesContent()
	 */
	public boolean isFinalTitle() {
		return _finalTitle;
	}

	/**
	 * Sets whether the label of this item is the final title of the page.
	 *
	 * @param finalTitle
	 *        {@code true} to keep the labels of the routes inside the content of this item out of
	 *        the title of the page.
	 * @return This instance for fluent chaining.
	 *
	 * @see #isFinalTitle()
	 */
	public NavigationItem withFinalTitle(boolean finalTitle) {
		_finalTitle = finalTitle;
		return this;
	}

	@Override
	public String getType() {
		return TYPE_NAV;
	}

	@Override
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = super.toStateMap();
		map.put(LABEL, _label);
		if (_icon != null) {
			map.put(ICON, _icon);
		}
		if (_hidden) {
			map.put(HIDDEN, Boolean.TRUE);
		}
		return map;
	}

}
