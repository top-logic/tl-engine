/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.basic.config.annotation.Label;

/**
 * The typographic role a {@link ReactTextControl} is displayed in.
 *
 * <p>
 * A role says what the text is for, not which font it is drawn in: the size, the line height, the
 * family and the weight come from the design tokens of the active theme, so a theme restyles every
 * text of a role at once.
 * </p>
 *
 * <p>
 * The roles are the type scale of the design system, each with tokens of its own for the font
 * family, size, weight, line height and letter spacing.
 * </p>
 */
public enum TextVariant implements ExternallyNamed {

	/** Running text, the size a page is read at (the default). */
	BODY("body"),

	/** The largest text on a page, for the one statement a page is built around, e.g. a key figure. */
	DISPLAY("display"),

	/** The heading a page is introduced with, the largest of the three heading levels. */
	@Label("Large heading")
	HEADING_LG("heading-lg"),

	/** The heading of a section within a page, the middle of the three heading levels. */
	@Label("Medium heading")
	HEADING_MD("heading-md"),

	/** The heading of a group within a section, the smallest of the three heading levels. */
	@Label("Small heading")
	HEADING_SM("heading-sm"),

	/** Running text that stands out from the text around it. */
	@Label("Strong body")
	BODY_STRONG("body-strong"),

	/** Small text: the name of a value, a hint, a date, an attribution. */
	LABEL("label"),

	/** Small text that stands out from the text around it. */
	@Label("Strong label")
	LABEL_STRONG("label-strong"),

	/** A small line above a heading, set with wide letter spacing. */
	OVERLINE("overline"),

	/** Program code or other text set in a fixed-width font. */
	CODE("code");

	private final String _externalName;

	TextVariant(String externalName) {
		_externalName = externalName;
	}

	@Override
	public String getExternalName() {
		return _externalName;
	}
}
