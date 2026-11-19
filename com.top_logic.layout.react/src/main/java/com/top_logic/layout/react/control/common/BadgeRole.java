/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.state.Badge;

/**
 * The color role a {@link BadgeDisplay badge} is drawn in.
 *
 * <p>
 * A role names what the color means, not which color it is: the color comes from the design tokens
 * of the active theme. The external names are those of {@link Badge.Role} in the state of a badge.
 * </p>
 */
public enum BadgeRole implements ExternallyNamed {

	/** The color of the brand (the default). */
	BRAND("brand"),

	/** Something failed. */
	ERROR("error"),

	/** Something calls for attention before it fails. */
	WARNING("warning"),

	/** Something went well. */
	SUCCESS("success"),

	/** Something to know. */
	INFO("info");

	private final String _externalName;

	BadgeRole(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
