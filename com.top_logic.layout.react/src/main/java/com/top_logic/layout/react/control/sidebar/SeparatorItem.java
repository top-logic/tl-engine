/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.sidebar;

/**
 * A sidebar item that renders as a visual divider line.
 */
public class SeparatorItem extends SidebarItem {

	/** Type discriminator for separator items. */
	public static final String TYPE_SEPARATOR = "separator";

	/**
	 * Creates a new {@link SeparatorItem}.
	 *
	 * @param id
	 *        The unique identifier.
	 */
	public SeparatorItem(String id) {
		super(id);
	}

	@Override
	public String getType() {
		return TYPE_SEPARATOR;
	}

}
