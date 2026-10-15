/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.text;

/**
 * The extent of a text laid out in a text box, as computed by {@link TextMeasure}.
 *
 * <p>
 * All lengths are given in points (1/72 inch).
 * </p>
 *
 * @param width
 *        The width of the widest line, without trailing white space.
 * @param lines
 *        The number of lines after wrapping the text.
 * @param height
 *        The height of the laid out text, without the insets of the text box.
 * @param lineHeight
 *        The height of the highest line, the distance between two consecutive baselines at single
 *        line spacing.
 * @param fontAvailable
 *        Whether all fonts used by the text are installed. Text in a font that is not installed is
 *        measured in a replacement font.
 * @param wordBroken
 *        Whether a line ends within a word, because the word is wider than the text box.
 */
public record TextMetrics(double width, int lines, double height, double lineHeight, boolean fontAvailable,
		boolean wordBroken) {
	// Pure data.
}
