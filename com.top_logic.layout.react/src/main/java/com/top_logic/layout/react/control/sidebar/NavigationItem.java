/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.layout.react.ReactControl;

/**
 * A sidebar item that switches the content area to a lazily created {@link ReactControl}.
 */
public class NavigationItem extends SidebarItem {

	/** Type discriminator for navigation items. */
	public static final String TYPE_NAV = "nav";

	private final String _label;

	private final String _icon;

	private final Supplier<ReactControl> _contentFactory;

	private String _badge;

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
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory) {
		this(id, label, icon, contentFactory, null);
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
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory, String badge) {
		super(id);
		_label = label;
		_icon = icon;
		_contentFactory = contentFactory;
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
			map.put("badge", _badge);
		}
		return map;
	}

}
