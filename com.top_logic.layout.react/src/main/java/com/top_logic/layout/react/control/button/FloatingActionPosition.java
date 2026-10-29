/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The corner of its frame a {@link ReactFloatingActionControl} floats over.
 *
 * <p>
 * Start and end follow the direction of the text: the end is the right corner in a left-to-right
 * text and the left corner in a right-to-left text.
 * </p>
 */
public enum FloatingActionPosition implements ExternallyNamed {

	/** The bottom corner at the end of the line, the default. */
	BOTTOM_END("bottom-end"),

	/** The bottom corner at the start of the line. */
	BOTTOM_START("bottom-start"),

	/** The top corner at the end of the line. */
	TOP_END("top-end"),

	/** The top corner at the start of the line. */
	TOP_START("top-start");

	private final String _externalName;

	FloatingActionPosition(String externalName) {
		_externalName = externalName;
	}

	/**
	 * The identifier transmitted to the {@code TLFloatingAction} React component.
	 */
	@Override
	public String getExternalName() {
		return _externalName;
	}
}
