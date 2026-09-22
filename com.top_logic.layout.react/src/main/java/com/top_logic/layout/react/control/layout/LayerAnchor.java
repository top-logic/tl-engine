/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Where a layer of a {@link ReactOverlayControl} sits over the content it covers.
 */
public enum LayerAnchor implements ExternallyNamed {

	/** The layer covers the whole content (the default). */
	FILL("fill"),

	/** The layer sits in the top left corner. */
	TOP_LEFT("top-left"),

	/** The layer sits centered at the top edge. */
	TOP("top"),

	/** The layer sits in the top right corner. */
	TOP_RIGHT("top-right"),

	/** The layer sits centered at the left edge. */
	LEFT("left"),

	/** The layer sits in the middle. */
	CENTER("center"),

	/** The layer sits centered at the right edge. */
	RIGHT("right"),

	/** The layer sits in the bottom left corner. */
	BOTTOM_LEFT("bottom-left"),

	/** The layer sits centered at the bottom edge. */
	BOTTOM("bottom"),

	/** The layer sits in the bottom right corner. */
	BOTTOM_RIGHT("bottom-right");

	private final String _externalName;

	LayerAnchor(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
