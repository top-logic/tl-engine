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
	 */
	public NavigationItem(String id, String label, String icon, Supplier<ReactControl> contentFactory) {
		super(id);
		_label = label;
		_icon = icon;
		_contentFactory = contentFactory;
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
		return map;
	}

}
