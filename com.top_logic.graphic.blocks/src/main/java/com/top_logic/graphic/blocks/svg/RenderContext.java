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
	 * Declaration of {@link #DEFAULT_FONT_FAMILY} for a stylesheet, naming metric-compatible
	 * substitutes after it.
	 *
	 * <p>
	 * Naming a single family in a standalone SVG only expresses a preference: a viewer that does
	 * not have that family substitutes one of its own, and a substitute with different advance
	 * widths no longer fits the boxes the layout measured. Listing families that share the metrics
	 * of {@link #DEFAULT_FONT_FAMILY} keeps the geometry intact wherever the document is opened.
	 * {@code Liberation Sans} and {@code Arimo} are metric-compatible with {@code Arial} by
	 * design; the trailing generic family is a last resort only.
	 * </p>
	 */
	String DEFAULT_FONT_DECLARATION = "Arial, 'Liberation Sans', Arimo, Helvetica, sans-serif";

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
	 * The {@code font-family} this context assumes for text that has no explicit family set.
	 *
	 * <p>
	 * A standalone SVG document has no external stylesheet, so it must declare this family itself
	 * to render text in the font it was measured with. The diagram states it on its root element
	 * through {@link SvgWriter#setTextStyle(String, String, String)}, from where its text inherits
	 * it.
	 * </p>
	 *
	 * @see #DEFAULT_FONT_FAMILY
	 */
	default String getDefaultFontFamily() {
		return DEFAULT_FONT_FAMILY;
	}

	/**
	 * How {@link #getDefaultFontFamily()} is declared in a stylesheet.
	 *
	 * <p>
	 * Measurement resolves a single family, while a viewer resolves whatever the document asks
	 * for. Naming metric-compatible substitutes alongside the measured family keeps the two in
	 * agreement on a machine that does not have it.
	 * </p>
	 *
	 * @see #DEFAULT_FONT_DECLARATION
	 */
	default String getDefaultFontDeclaration() {
		return DEFAULT_FONT_DECLARATION;
	}

	/**
	 * The {@code font-size} in CSS pixels this context assumes for text that has no explicit size
	 * set.
	 *
	 * @see #getDefaultFontFamily()
	 * @see #DEFAULT_FONT_SIZE_PX
	 */
	default double getDefaultFontSizePx() {
		return DEFAULT_FONT_SIZE_PX;
	}

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
