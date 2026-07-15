/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.button;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Size of a {@link ReactButtonControl}, independent of its {@link ButtonAppearance appearance}.
 */
public enum ButtonSize implements ExternallyNamed {

	/** The standard button size (the default). */
	DEFAULT("default"),

	/** A compact button, e.g. for a secondary inline action. */
	SMALL("small"),

	/** An emphasized, larger button. */
	LARGE("large");

	private final String _externalName;

	ButtonSize(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
