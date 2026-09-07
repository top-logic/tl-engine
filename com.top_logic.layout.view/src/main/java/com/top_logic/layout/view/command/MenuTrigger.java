/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The gesture on a {@link MenuRegionControl} that opens its menu.
 */
public enum MenuTrigger implements ExternallyNamed {

	/**
	 * The menu opens on a {@code contextmenu} event - a right-click, or its touch and keyboard
	 * equivalents - and appears at the pointer.
	 */
	CONTEXT_MENU("contextmenu"),

	/**
	 * The menu opens on a click and appears anchored below the region, the way a drop-down does.
	 */
	CLICK("click");

	private final String _externalName;

	private MenuTrigger(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
