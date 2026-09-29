/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.dirty.DirtyChannel;

/**
 * A sidebar item that switches the content area to a lazily created {@link ReactControl}.
 */
public class NavigationItem extends SidebarItem {

	/** Type discriminator for navigation items. */
	public static final String TYPE_NAV = "nav";

	/** @see #setBadge(String) */
	private static final String BADGE = "badge";

	/** @see #withHidden(boolean) */
	private static final String HIDDEN = "hidden";

	private final String _label;

	private final String _icon;

	private final Supplier<ReactControl> _contentFactory;

	private final DirtyChannel _dirtyChannel;

	private String _badge;

	private String _route;

	private boolean _hidden;

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
	 *        Optional badge text (e.g. "3"), or {@code null} for no badge.
	 */
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory,
			DirtyChannel dirtyChannel, String badge) {
		super(id);
		_label = label;
		_icon = icon;
		_contentFactory = contentFactory;
		_dirtyChannel = dirtyChannel;
		_badge = badge;
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
	 * Optional badge text (e.g. "3"), or {@code null} for no badge.
	 */
	public String getBadge() {
		return _badge;
	}

	/**
	 * Sets the badge text.
	 *
	 * @param badge
	 *        Badge text (e.g. "5"), or {@code null} to remove the badge.
	 */
	public void setBadge(String badge) {
		_badge = badge;
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
		if (_badge != null) {
			map.put(BADGE, _badge);
		}
		if (_hidden) {
			map.put(HIDDEN, Boolean.TRUE);
		}
		return map;
	}

}
