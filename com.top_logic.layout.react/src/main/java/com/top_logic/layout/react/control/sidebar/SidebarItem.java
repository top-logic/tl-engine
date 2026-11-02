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

	/** @see #getId() */
	static final String ID = "id";

	/** @see #getType() */
	static final String TYPE = "type";

	/** Item display label. */
	static final String LABEL = "label";

	/** Item CSS icon class. */
	static final String ICON = "icon";

	/** Whether the item is withheld from the sidebar UI. */
	static final String HIDDEN = "hidden";

	/** Whether the item is displayed, but cannot be activated. */
	static final String DISABLED = "disabled";

	/** Text explaining the item, shown when the pointer rests on it. */
	static final String TOOLTIP = "tooltip";

	private final String _id;

	private boolean _withheld;

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
		if (_withheld) {
			map.put(HIDDEN, Boolean.TRUE);
		}
		return map;
	}

	/**
	 * Whether this item is currently not drawn, e.g. because the current user may not see it for
	 * the object displayed.
	 *
	 * <p>
	 * A withheld group withholds everything in it. A withheld navigation item that is open stays
	 * open: only its entry is not drawn.
	 * </p>
	 *
	 * @see ReactSidebarControl#setItemWithheld(String, boolean)
	 */
	public boolean isWithheld() {
		return _withheld;
	}

	/**
	 * Sets whether this item is currently not drawn.
	 *
	 * @see #isWithheld()
	 */
	public void setWithheld(boolean withheld) {
		_withheld = withheld;
	}

}
