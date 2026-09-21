/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The end at which a {@link ReactToolbarControl} gives way when its commands need more width than
 * the toolbar is given.
 *
 * <p>
 * Collapsing happens in two steps: buttons carrying an icon drop their label, and commands that
 * still do not fit move into an overflow menu. Both take effect from the end named here, so the
 * commands at the opposite end stay reachable longest.
 * </p>
 */
public enum ToolbarOverflow implements ExternallyNamed {

	/** The toolbar keeps every command laid out side by side, whatever width it is given. */
	NONE("none"),

	/**
	 * The toolbar collapses from its trailing end and shows the overflow menu there, keeping the
	 * leading commands visible.
	 */
	TRAILING("trailing"),

	/**
	 * The toolbar collapses from its leading end and shows the overflow menu there, keeping the
	 * trailing commands visible.
	 */
	LEADING("leading");

	private final String _externalName;

	ToolbarOverflow(String externalName) {
		_externalName = externalName;
	}

	/**
	 * The identifier transmitted to the {@code TLToolbar} React component.
	 */
	@Override
	public String getExternalName() {
		return _externalName;
	}
}
