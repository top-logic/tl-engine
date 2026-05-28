/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

/**
 * Context for rendering to SVG.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface RenderContext {

	/**
	 * Default {@code font-family} assumed when no per-text family is set.
	 *
	 * <p>
	 * Both the measurement (server- and client-side) and the SVG output must use this default
	 * consistently to ensure that rendered text fits its measured box.
	 * </p>
	 */
	String DEFAULT_FONT_FAMILY = "Arial";

	/**
	 * Default {@code font-size} (in CSS pixels) assumed when no per-text size is set.
	 *
	 * <p>
	 * Matches the default rule {@code text:not([font-size]):not([class]) { font-size: 14px }} from
	 * {@code tl-blocks.css}, which applies to both the block editor and flow diagrams in the
	 * interactive client.
	 * </p>
	 *
	 * @see #DEFAULT_FONT_FAMILY
	 */
	double DEFAULT_FONT_SIZE_PX = 14.0;

	/**
	 * Measures the given text using the context's default font.
	 *
	 * @see TextMetrics
	 */
	default TextMetrics measure(String text) {
		return measure(text, null, null, null);
	}

	/**
	 * Measures the given text using the given font properties.
	 *
	 * <p>
	 * A {@code null} value for any of {@code fontFamily}, {@code fontSize} or {@code fontWeight}
	 * means that the corresponding context default applies. The {@code fontSize} string follows
	 * SVG/CSS conventions, e.g. {@code "16px"}, {@code "12pt"} or a plain number (interpreted as
	 * SVG user units, i.e. pixels). The {@code fontWeight} string follows SVG/CSS conventions
	 * (e.g. {@code "bold"} or a numeric weight).
	 * </p>
	 *
	 * @see TextMetrics
	 */
	TextMetrics measure(String text, String fontFamily, String fontSize, String fontWeight);

}
