/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.model;

/**
 * Character formatting in a presentation description.
 *
 * <p>
 * A property that is <code>null</code> is inherited: a text run inherits from its paragraph, a
 * paragraph from its text, and a text from its shape, placeholder or table style.
 * </p>
 *
 * @param bold
 *        Whether the text is bold.
 * @param italic
 *        Whether the text is italic.
 * @param underline
 *        Whether the text is underlined.
 * @param caps
 *        Whether the text is shown in capital letters.
 * @param size
 *        The font size in points.
 * @param color
 *        The text color.
 * @param font
 *        The font family.
 * @param characterSpacing
 *        Additional space between characters in points, negative to condense the text.
 */
public record RunStyle(Boolean bold, Boolean italic, Boolean underline, Boolean caps, Double size, ColorSpec color,
		String font, Double characterSpacing) {

	/**
	 * A style inheriting all properties.
	 */
	public static final RunStyle INHERIT = new RunStyle(null, null, null, null, null, null, null, null);

	/**
	 * Whether all properties are inherited.
	 */
	public boolean isInherit() {
		return equals(INHERIT);
	}

}
