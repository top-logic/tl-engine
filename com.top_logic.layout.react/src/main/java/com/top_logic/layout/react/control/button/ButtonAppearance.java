/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Visual appearance of a {@link ReactButtonControl}, independent of its {@link ButtonSize size} and
 * {@link ButtonDisplayMode display mode}.
 */
public enum ButtonAppearance implements ExternallyNamed {

	/** The standard button appearance. */
	DEFAULT("default"),

	/** Renders as an inline text link rather than a button. */
	LINK("link");

	private final String _externalName;

	ButtonAppearance(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
