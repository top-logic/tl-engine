/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

import java.util.Map;

/**
 * A sidebar item that displays a static label and optional icon.
 */
public class HeaderItem extends SidebarItem {

	/** Type discriminator for header items. */
	public static final String TYPE_HEADER = "header";

	private final String _label;

	private final String _icon;

	/**
	 * Creates a new {@link HeaderItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The icon CSS class, or {@code null} for no icon.
	 */
	public HeaderItem(String id, String label, String icon) {
		super(id);
		_label = label;
		_icon = icon;
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

	@Override
	public String getType() {
		return TYPE_HEADER;
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
