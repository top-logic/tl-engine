/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Diameter of a {@link ReactAvatarControl}.
 */
public enum AvatarSize implements ExternallyNamed {

	/** The standard avatar size (the default). */
	DEFAULT("default"),

	/** A compact avatar, e.g. in a table cell or a list row. */
	SMALL("small"),

	/** An emphasized avatar, e.g. in the header of a detail view. */
	LARGE("large"),

	/** A portrait-sized avatar, e.g. on a profile page. */
	X_LARGE("x-large");

	private final String _externalName;

	AvatarSize(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
