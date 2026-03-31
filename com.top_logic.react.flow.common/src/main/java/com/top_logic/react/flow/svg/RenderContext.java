/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.svg;

/**
 * Context for rendering to SVG.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RenderContext {

	/**
	 * Measures the given text with the default font size.
	 *
	 * @see TextMetrics
	 */
	TextMetrics measure(String text);

	/**
	 * Measures the given text with specific font properties.
	 *
	 * @param text
	 *        The text to measure.
	 * @param fontFamily
	 *        The font family, or {@code null} to use the default.
	 * @param fontSize
	 *        The font size in pixels, or 0 to use the default.
	 * @return The text metrics.
	 */
	default TextMetrics measure(String text, String fontFamily, double fontSize) {
		return measure(text, fontFamily, fontSize, null);
	}

	/**
	 * Measures the given text with specific font properties including weight.
	 *
	 * @param text
	 *        The text to measure.
	 * @param fontFamily
	 *        The font family, or {@code null} to use the default.
	 * @param fontSize
	 *        The font size in pixels, or 0 to use the default.
	 * @param fontWeight
	 *        The font weight (e.g. "bold"), or {@code null} for normal.
	 * @return The text metrics.
	 */
	default TextMetrics measure(String text, String fontFamily, double fontSize, String fontWeight) {
		// Default: ignore font properties, use default. Subclasses should override.
		return measure(text);
	}

}
