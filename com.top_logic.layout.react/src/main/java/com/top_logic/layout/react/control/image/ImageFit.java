/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.image;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How a picture fills the box of a {@link ReactImageControl} whose proportions differ from its own.
 */
public enum ImageFit implements ExternallyNamed {

	/** The picture fills the box and is cropped on the axis that does not fit (the default). */
	COVER("cover"),

	/** The picture is shown as a whole, leaving free space on the axis that does not fit. */
	CONTAIN("contain");

	private final String _externalName;

	ImageFit(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
