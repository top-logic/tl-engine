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
