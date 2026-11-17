/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.graphic.flow.server.ui;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.util.Arrays;

import test.com.top_logic.basic.LogListeningTestCase;

import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.flow.server.ui.AWTContext;
import com.top_logic.graphic.flow.server.ui.FontFamilyResolver;

/**
 * Test for {@link FontFamilyResolver} and its use in {@link AWTContext}.
 *
 * <p>
 * The installed font families are given explicitly, so that the results do not depend on the
 * fonts of the machine.
 * </p>
 */
public class TestFontFamilyResolver extends LogListeningTestCase {

	private static final String ARIAL = "Arial";

	private static final String LIBERATION_SANS = "Liberation Sans";

	private static final String DEJAVU_SANS = "DejaVu Sans";

	private static final float SIZE_PT = 12f;

	private static final String TEXT = "iiiiWWWW";

	private static final double DELTA = 1e-9;

	/**
	 * Without {@value #ARIAL}, the default measures with its metric-compatible substitute, which
	 * the SVG declares first.
	 */
	public void testDefaultResolvesToSubstitute() {
		AWTContext context = context(LIBERATION_SANS, DEJAVU_SANS);

		assertEquals(LIBERATION_SANS, context.getDefaultFontFamily());
		assertEquals("'Liberation Sans', Arial, Arimo, Helvetica, sans-serif",
			context.getDefaultFontDeclaration());
		assertEquals(width(LIBERATION_SANS), context.measure(TEXT).getWidth(), DELTA);
	}

	/**
	 * With {@value #ARIAL} installed, the default declaration is kept as it is.
	 */
	public void testDefaultResolvesToPreferred() {
		AWTContext context = context(ARIAL);

		assertEquals(ARIAL, context.getDefaultFontFamily());
		assertEquals(RenderContext.DEFAULT_FONT_DECLARATION, context.getDefaultFontDeclaration());
	}

	/**
	 * Without any family of the default list, the logical sans-serif font of Java is used and a
	 * warning is logged once.
	 */
	public void testDefaultResolvesToGenericWithWarning() {
		FontFamilyResolver resolver = resolver(DEJAVU_SANS);
		AWTContext context = new AWTContext(SIZE_PT, resolver);

		double width = context.measure(TEXT).getWidth();
		assertAnErrorWasLogged("Missing default font families must be reported.");

		assertEquals(Font.SANS_SERIF, resolver.getDefault().getAWTFamily());
		assertEquals(width(Font.SANS_SERIF), width, DELTA);
		assertEquals(FontFamilyResolver.GENERIC_SANS_SERIF, context.getDefaultFontFamily());
		assertTrue(context.getDefaultFontDeclaration().startsWith(FontFamilyResolver.GENERIC_SANS_SERIF + ", Arial"));

		// No further warning.
		context.measure(TEXT);
		new AWTContext(SIZE_PT, resolver).measure(TEXT);
	}

	/**
	 * The family list of a text is resolved to its first installed family.
	 */
	public void testTextFamilyList() {
		FontFamilyResolver resolver = resolver(LIBERATION_SANS, DEJAVU_SANS);

		assertEquals(DEJAVU_SANS, resolver.resolve("Missing, 'DejaVu Sans'").getAWTFamily());
		assertEquals(DEJAVU_SANS, resolver.resolve("Missing, \"DejaVu Sans\", 'Liberation Sans'").getAWTFamily());
		assertEquals("'DejaVu Sans', Missing, 'Liberation Sans'",
			resolver.resolve("Missing, \"DejaVu Sans\", 'Liberation Sans'").getDeclaration());
	}

	/**
	 * Family names match independent of their case, the installed spelling is used for measuring.
	 */
	public void testCaseInsensitive() {
		FontFamilyResolver resolver = resolver(DEJAVU_SANS);

		assertEquals(DEJAVU_SANS, resolver.resolve("dejavu sans").getAWTFamily());
	}

	/**
	 * A list without installed family falls back to the default, a generic name to the logical
	 * font of Java.
	 */
	public void testFallbacks() {
		AWTContext context = context(LIBERATION_SANS);

		assertEquals(width(LIBERATION_SANS), context.measure(TEXT, "Missing, 'Also Missing'", null, null).getWidth(), DELTA);
		assertEquals(width(Font.MONOSPACED), context.measure(TEXT, "Missing, monospace", null, null).getWidth(), DELTA);
		assertEquals(width(Font.SERIF), context.measure(TEXT, "SERIF", null, null).getWidth(), DELTA);
		assertFalse(width(Font.MONOSPACED) == width(Font.SANS_SERIF));
	}

	private static AWTContext context(String... installedFamilies) {
		return new AWTContext(SIZE_PT, resolver(installedFamilies));
	}

	private static FontFamilyResolver resolver(String... installedFamilies) {
		return new FontFamilyResolver(Arrays.asList(installedFamilies));
	}

	private static double width(String awtFamily) {
		Font font = new Font(awtFamily, Font.PLAIN, 1).deriveFont(SIZE_PT);
		return font.getStringBounds(TEXT, new FontRenderContext(null, true, true)).getWidth()
			* AWTContext.PX_PER_PT;
	}

}
