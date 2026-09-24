/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Diameter of a {@link ReactAvatarControl}: one of four sizes of the design system's scale
 * ({@code size-avatar-sm} to {@code size-avatar-xl}), independent of the density.
 */
public enum AvatarSize implements ExternallyNamed {

	/** 24 px: a compact avatar, e.g. in a table cell or a list row. */
	SMALL("sm"),

	/** 32 px: the standard avatar size (the default). */
	MEDIUM("md"),

	/** 48 px: an emphasized avatar, e.g. in the header of a detail view. */
	LARGE("lg"),

	/** 64 px: a portrait-sized avatar, e.g. on a profile page. */
	EXTRA_LARGE("xl");

	private final String _externalName;

	AvatarSize(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
