/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The size step of an icon, chosen by its role, not by a pixel value: the four classes
 * {@code tl-icon-sm}, {@code tl-icon-md}, {@code tl-icon-lg} and {@code tl-icon-glyph} of the design
 * system.
 */
public enum IconSize implements ExternallyNamed {

	/** 16 px: an icon beside a label - the rule (the default). */
	SMALL("sm"),

	/** 20 px: the icon is the control itself, nothing is written beside it. */
	MEDIUM("md"),

	/** 24 px: the icon is a statement, not a control - a dialog head, a large status hint. */
	LARGE("lg"),

	/** 12 px: a direction sign inside a control - a chevron, an arrow, a sort indicator. */
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
