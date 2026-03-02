/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base for items displayed in a {@link ReactSidebarControl}.
 *
 * <p>
 * Each item has a unique identifier and a type discriminator used by the React component to render
 * the appropriate UI element.
 * </p>
 */
public abstract class SidebarItem {

	/** JSON key for the item identifier. */
	static final String ID = "id";

	/** JSON key for the item type discriminator. */
	static final String TYPE = "type";

	/** JSON key for the item label. */
	static final String LABEL = "label";

	/** JSON key for the item icon. */
	static final String ICON = "icon";

	private final String _id;

	/**
	 * Creates a new {@link SidebarItem}.
	 *
	 * @param id
	 *        The unique identifier for this item.
	 */
	protected SidebarItem(String id) {
		_id = id;
	}

	/**
	 * The unique identifier for this item.
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The type discriminator string sent to the React component.
	 */
	public abstract String getType();

	/**
	 * Converts this item to a map suitable for JSON serialization.
	 */
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = new HashMap<>();
		map.put(ID, _id);
		map.put(TYPE, getType());
		return map;
	}

}
