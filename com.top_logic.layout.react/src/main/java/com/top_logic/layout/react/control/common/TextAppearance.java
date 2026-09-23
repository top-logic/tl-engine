/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The shape a {@link ReactTextControl} is drawn in.
 */
public enum TextAppearance implements ExternallyNamed {

	/**
	 * Plain text, drawn as a pill when the displayed value carries a color of its own (the
	 * default).
	 */
	TEXT("text"),

	/**
	 * A pill, whether or not the displayed value carries a color: a badge, a status, a tag.
	 *
	 * <p>
	 * The color of the pill is the one the value carries, and the color of the
	 * {@link TextTone tone} for a value that carries none.
	 * </p>
	 */
	PILL("pill");

	private final String _externalName;

	TextAppearance(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
