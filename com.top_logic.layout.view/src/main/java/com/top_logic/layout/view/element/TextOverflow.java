/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * How a {@link TextElement} handles text that is longer than the available width.
 */
public enum TextOverflow implements ExternallyNamed {

	/** Wrap onto multiple lines (the default). */
	WRAP("wrap"),

	/** Keep a single line and truncate the overflow with an ellipsis. */
	ELLIPSIS("ellipsis");

	private final String _externalName;

	TextOverflow(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
