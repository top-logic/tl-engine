/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A sidebar item that holds further items.
 *
 * <p>
 * Lookups, routes and serialization of a {@link ReactSidebarControl} descend into every container:
 * an item within a container is as much part of the sidebar as one at the top.
 * </p>
 */
public abstract class ContainerItem extends SidebarItem {

	/** JSON key for the children list. */
	static final String CHILDREN = "children";

	private final List<SidebarItem> _children;

	/**
	 * Creates a new {@link ContainerItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param children
	 *        The items within this container.
	 */
	protected ContainerItem(String id, List<SidebarItem> children) {
		super(id);
		_children = new ArrayList<>(children);
	}

	/**
	 * The child items.
	 */
	public List<SidebarItem> getChildren() {
		return _children;
	}

	@Override
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = super.toStateMap();
		List<Map<String, Object>> childMaps = new ArrayList<>();
		for (SidebarItem child : _children) {
			childMaps.add(child.toStateMap());
		}
		map.put(CHILDREN, childMaps);
		return map;
	}

}
