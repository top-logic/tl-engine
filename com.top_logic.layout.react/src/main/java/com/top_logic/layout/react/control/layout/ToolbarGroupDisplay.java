/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Display mode of a clique group within a {@link ReactToolbarControl}.
 */
public enum ToolbarGroupDisplay implements ExternallyNamed {

	/** Commands shown inline, side by side, with separators between groups. */
	INLINE("inline"),

	/** Commands collapsed into a dropdown menu. */
	MENU("menu");

	private final String _externalName;

	ToolbarGroupDisplay(String externalName) {
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
