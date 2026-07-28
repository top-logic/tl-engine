/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.TextMetrics;
import com.top_logic.graphic.blocks.svg.TextMetricsImpl;

/**
 * {@link RenderContext} for testing only.
 *
 * <p>
 * Reports the same advance width for every character, so that layout geometry in tests is
 * reproducible across machines regardless of which fonts are installed. That is the metric of a
 * fixed-pitch font, which is therefore what this context declares as its default font. Declaring it
 * keeps the written SVG faithful: a viewer renders the text at the width the layout reserved for
 * it, instead of falling back to its own proportional default font and drawing text roughly 40%
 * narrower than its box.
 * </p>
 */
final class TestingRenderContext implements RenderContext {

	/**
	 * Advance width of a single character, in CSS pixels.
	 */
	private static final double CHAR_WIDTH_PX = 12.0;

	/**
	 * Advance width of a fixed-pitch font, as a fraction of its font size.
	 *
	 * <p>
	 * The common fixed-pitch faces (Courier New, DejaVu Sans Mono, Liberation Mono) all advance
	 * 0.6em per character.
	 * </p>
	 */
	private static final double MONOSPACE_ADVANCE_EM = 0.6;

	@Override
	public String getDefaultFontFamily() {
		return "monospace";
	}

	@Override
	public double getDefaultFontSizePx() {
		return CHAR_WIDTH_PX / MONOSPACE_ADVANCE_EM;
	}

	@Override
	public TextMetrics measure(String text, String fontFamily, String fontSize, String fontWeight) {
		return new TextMetricsImpl(text.length() * CHAR_WIDTH_PX, 12, 10);
	}
}
