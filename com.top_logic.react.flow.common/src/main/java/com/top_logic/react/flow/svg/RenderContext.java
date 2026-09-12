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
	 * Default {@code font-family} assumed when no per-text family is set.
	 *
	 * <p>
	 * Both the measurement and the SVG output must use this default consistently to ensure that
	 * rendered text fits its measured box.
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
	 * @see #DEFAULT_FONT_FAMILY
	 */
	double DEFAULT_FONT_SIZE_PX = 14.0;

	/**
	 * The {@code font-family} this context assumes for text that has no explicit family set.
	 *
	 * <p>
	 * A standalone SVG document has no external stylesheet, so it must declare this family itself
	 * to render text in the font it was measured with. The diagram states it on its root element
	 * through {@link SvgWriter#setTextStyle(String, double, String)}, from where its text inherits
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

	/**
	 * The current rendering zoom factor.
	 *
	 * <p>
	 * Conceptually the multiplier from logical units to screen pixels along the active axis. In
	 * Gantt content this is the {@code pixelsPerUnit} of the time axis; in non-zoomed contexts the
	 * value is {@code 1.0}.
	 * </p>
	 *
	 * <p>
	 * Level-of-detail boxes consult this value to decide which content variant to render.
	 * </p>
	 */
	default double getZoom() {
		return 1.0;
	}

	/**
	 * Returns a context that delegates all text measurement to this context but reports the given
	 * zoom factor from {@link #getZoom()}.
	 */
	default RenderContext withZoom(double zoom) {
		RenderContext self = this;
		return new RenderContext() {
			@Override
			public String getDefaultFontFamily() {
				return self.getDefaultFontFamily();
			}

			@Override
			public String getDefaultFontDeclaration() {
				return self.getDefaultFontDeclaration();
			}

			@Override
			public double getDefaultFontSizePx() {
				return self.getDefaultFontSizePx();
			}

			@Override
			public TextMetrics measure(String text) {
				return self.measure(text);
			}

			@Override
			public TextMetrics measure(String text, String fontFamily, double fontSize) {
				return self.measure(text, fontFamily, fontSize);
			}

			@Override
			public TextMetrics measure(String text, String fontFamily, double fontSize, String fontWeight) {
				return self.measure(text, fontFamily, fontSize, fontWeight);
			}

			@Override
			public double getZoom() {
				return zoom;
			}
		};
	}

}
