/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The size step of an icon, chosen by its role.
 *
 * <p>
 * The external names are words: {@code small}, {@code medium}, {@code large} and {@code glyph}.
 * The design system renders the steps with its four classes {@code tl-icon-sm}, {@code tl-icon-md},
 * {@code tl-icon-lg} and {@code tl-icon-glyph}; the client maps the one to the other.
 * </p>
 */
public enum IconSize implements ExternallyNamed {

	/** The icon stands beside text - the rule and the default. */
	SMALL("small"),

	/** The icon is the control itself, nothing stands beside it. */
	MEDIUM("medium"),

	/** The icon is a statement, not a control - a dialog head, a large status hint. */
	LARGE("large"),

	/** A direction sign inside a control - a chevron, an arrow, a sort indicator. */
	GLYPH("glyph");

	private final String _externalName;

	IconSize(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
