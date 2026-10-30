/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.List;
import java.util.Map;

/**
 * A sidebar item that contains a collapsible group of child items.
 */
public class GroupItem extends ContainerItem {

	/** Type discriminator for group items. */
	public static final String TYPE_GROUP = "group";

	/** JSON key for the expanded state. */
	static final String EXPANDED = "expanded";

	private final String _label;

	private final String _icon;

	private final boolean _initiallyExpanded;

	/**
	 * Creates a new {@link GroupItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The icon CSS class, or {@code null} for no icon.
	 * @param children
	 *        The child items within this group.
	 * @param initiallyExpanded
	 *        Whether the group is expanded by default.
	 */
	public GroupItem(String id, String label, String icon, List<SidebarItem> children, boolean initiallyExpanded) {
		super(id, children);
		_label = label;
		_icon = icon;
		_initiallyExpanded = initiallyExpanded;
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
	 * Whether the group is expanded by default.
	 */
	public boolean isInitiallyExpanded() {
		return _initiallyExpanded;
	}

	@Override
	public String getType() {
		return TYPE_GROUP;
	}

	@Override
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = super.toStateMap();
		map.put(LABEL, _label);
		if (_icon != null) {
			map.put(ICON, _icon);
		}
		map.put(EXPANDED, Boolean.valueOf(_initiallyExpanded));
		return map;
	}

}
