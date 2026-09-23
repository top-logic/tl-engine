/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;

/**
 * The typographic role a {@link ReactTextControl} is displayed in.
 *
 * <p>
 * A role says what the text is for, not which font it is drawn in: the size, the line height, the
 * family and the weight come from the design tokens of the active theme, so a theme restyles every
 * text of a role at once.
 * </p>
 */
public enum TextVariant implements ExternallyNamed {

	/** Running text, the size a page is read at (the default). */
	BODY("body"),

	/** The heading of a section within a page. */
	TITLE("title"),

	/** The heading a page is introduced with. */
	HEADLINE("headline"),

	/** The largest text on a page, for the one statement a landing page is built around. */
	DISPLAY("display"),

	/** The name of a value, shown above or beside what it names. */
	LABEL("label"),

	/** A remark accompanying another text: a hint, a date, an attribution. */
	CAPTION("caption");

	private final String _externalName;

	TextVariant(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
