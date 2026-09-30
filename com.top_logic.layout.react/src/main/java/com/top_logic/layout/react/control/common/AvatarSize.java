/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * Diameter of a {@link ReactAvatarControl}, chosen by where the avatar stands, independent of the
 * density.
 *
 * <p>
 * The external names are words: {@code small}, {@code medium}, {@code large} and {@code x-large}.
 * The CSS classes of the design system carry the abbreviations of its scale ({@code tl-avatar--sm},
 * {@code tl-avatar--lg}, {@code tl-avatar--xl}; the medium step has no modifier); the client maps
 * the one to the other.
 * </p>
 */
public enum AvatarSize implements ExternallyNamed {

	/** The rule: beside text, in lists and in comment headers - the first value, hence the default. */
	MEDIUM("medium"),

	/** An avatar in a table cell or a row. */
	SMALL("small"),

	/** An avatar in the header of a detail view. */
	LARGE("large"),

	/** A profile page: the picture is the content. */
	EXTRA_LARGE("x-large");

	private final String _externalName;

	AvatarSize(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
