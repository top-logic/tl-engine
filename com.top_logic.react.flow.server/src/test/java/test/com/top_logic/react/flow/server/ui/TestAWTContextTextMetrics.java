/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.react.flow.server.ui;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;

import junit.framework.TestCase;

import com.top_logic.react.flow.server.ui.AWTContext;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.TextMetrics;

/**
 * Test for the height and baseline of a text measured by {@link AWTContext}.
 */
public class TestAWTContextTextMetrics extends TestCase {

	private static final float SIZE_PT = 12f;

	private static final double SIZE_PX = 20.0;

	private static final double DELTA = 1e-9;

	/**
	 * Texts with and without letters with descenders have the same height and baseline.
	 */
	public void testHeightIndependentOfLetters() {
		AWTContext context = new AWTContext(SIZE_PT);

		TextMetrics noDescender = context.measure("April");
		TextMetrics descender = context.measure("Juni gypsy");

		assertEquals(noDescender.getHeight(), descender.getHeight(), DELTA);
		assertEquals(noDescender.getBaseLine(), descender.getBaseLine(), DELTA);
	}

	/**
	 * The height is the ascent plus the descent of the font, without its leading, and the baseline
	 * is the ascent of the font.
	 *
	 * <p>
	 * Measures in a font with leading, if one is installed, so that a height including the leading
	 * is detected.
	 * </p>
	 */
	public void testHeightFromLineMetrics() {
		AWTContext context = new AWTContext(SIZE_PT);
		float sizePt = (float) (SIZE_PX * AWTContext.PT_PER_PX);
		String family = familyWithLeading(sizePt);
		LineMetrics expected = lineMetrics(font(family, sizePt), "April");

		TextMetrics metrics = context.measure("April", family, SIZE_PX, null);

		assertEquals((expected.getAscent() + expected.getDescent()) * AWTContext.PX_PER_PT,
			metrics.getHeight(), DELTA);
		assertEquals(expected.getAscent() * AWTContext.PX_PER_PT, metrics.getBaseLine(), DELTA);
	}

	/**
	 * Texts with an explicit font size and weight have the same height and baseline, independent of
	 * their letters.
	 */
	public void testExplicitFontHeightIndependentOfLetters() {
		AWTContext context = new AWTContext(SIZE_PT);

		TextMetrics noDescender = context.measure("April", RenderContext.DEFAULT_FONT_FAMILY, SIZE_PX, "bold");
		TextMetrics descender = context.measure("Juni gypsy", RenderContext.DEFAULT_FONT_FAMILY, SIZE_PX, "bold");

		assertEquals(noDescender.getHeight(), descender.getHeight(), DELTA);
		assertEquals(noDescender.getBaseLine(), descender.getBaseLine(), DELTA);
		assertTrue("Larger font must be higher.",
			noDescender.getHeight() > context.measure("April").getHeight());
	}

	/**
	 * The first installed font family whose font has a leading, or
	 * {@link RenderContext#DEFAULT_FONT_FAMILY} if there is none.
	 */
	private static String familyWithLeading(float sizePt) {
		for (String family : GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()) {
			if (lineMetrics(font(family, sizePt), "April").getLeading() > 0) {
				return family;
			}
		}
		return RenderContext.DEFAULT_FONT_FAMILY;
	}

	private static Font font(String family, float sizePt) {
		return new Font(family, Font.PLAIN, 1).deriveFont(sizePt);
	}

	private static LineMetrics lineMetrics(Font font, String text) {
		return font.getLineMetrics(text, new FontRenderContext(null, true, true));
	}

}
