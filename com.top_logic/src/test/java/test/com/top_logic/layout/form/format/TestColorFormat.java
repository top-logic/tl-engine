/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.format;

import java.awt.Color;
import java.text.ParseException;
import java.text.ParsePosition;

import junit.framework.TestCase;

import com.top_logic.layout.form.format.ColorFormat;
import com.top_logic.util.error.TopLogicException;

/**
 * Test case for {@link ColorFormat}
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestColorFormat extends TestCase {

	public void testParse() throws ParseException {
		assertParse(0x000000, 0x00, "transparent");
		assertParse(0x112233, 0xFF, "#123");
		assertParse(0x112233, 0x44, "#1234");
		assertParse(0x123456, 0xFF, "#123456");
		assertParse(0x123456, 0x78, "#12345678");
		assertParse(0x89ABCD, 0xEF, "#89ABCDEF");
		assertParse(0x89ABCD, 0xEF, "#89abcdef");
	}

	public void testParseFunction() throws ParseException {
		assertParse(0x010203, 0xFF, "rgb(1,2,3)");
		assertParse(0xFF80FF, 0xFF, "rgb(255, 128, 255)");
		assertParse(0x000000, 0xFF, "rgb(0, 0, 0)");
		assertParse(0x010203, 0x00, "rgba(1, 2, 3, 0)");
		assertParse(0x010203, 0x80, "rgba(1, 2, 3, 0.5)");
		assertParse(0x010203, 0xFF, "rgba(1, 2, 3, 1)");
		assertParse(0x010203, 0xFF, "rgba(1, 2, 3, 1.0)");
		assertParse(0x010203, 0x80, "RGBA( 1 ,2,3, .5 )");
		assertParse(0x010203, 0xFF, "Rgb(1,2,3) ");
		assertParse(0x010203, 0x80, "rgb(1, 2, 3, 0.5)");
		assertParse(0x010203, 0xFF, "rgba(1, 2, 3)");
		assertParse(0xFF8000, 0xFF, "rgb(100%, 50%, 0%)");
		assertParse(0x010203, 0x80, "rgba(1, 2, 3, 50%)");
	}

	public void testParseFunctionInvalid() {
		assertInvalid("rgba(1,2)");
		assertInvalid("rgb(1,2,3,4,5)");
		assertInvalid("rgb(256,0,0)");
		assertInvalid("rgb(1000,0,0)");
		assertInvalid("rgb(-1,0,0)");
		assertInvalid("rgb(1.5,0,0)");
		assertInvalid("rgb(101%,0,0)");
		assertInvalid("rgba(1,2,3,2)");
		assertInvalid("rgba(1,2,3,101%)");
		assertInvalid("rgba(1,2,3,)");
		assertInvalid("rgb(1,2,3)x");
		assertInvalid("rgb(1 2 3)");
		assertInvalid("rgb(a,b,c)");
		assertInvalid("rgb(");
		assertInvalid("rgb");
		assertInvalid("rgbx(1,2,3)");
	}

	public void testParseColorOrNull() {
		assertEquals(new Color(0x01, 0x02, 0x03, 0x80), ColorFormat.parseColorOrNull("rgba(1, 2, 3, 0.5)"));
		assertEquals(new Color(0x12, 0x34, 0x56), ColorFormat.parseColorOrNull("#123456"));
		assertEquals(new Color(0, 0, 0, 0), ColorFormat.parseColorOrNull("transparent"));
		assertEquals(Color.RED, ColorFormat.parseColorOrNull("red"));

		assertNull(ColorFormat.parseColorOrNull(null));
		assertNull(ColorFormat.parseColorOrNull(""));
		assertNull(ColorFormat.parseColorOrNull("none"));
		assertNull(ColorFormat.parseColorOrNull("url(#g)"));
		assertNull(ColorFormat.parseColorOrNull("currentColor"));
		assertNull(ColorFormat.parseColorOrNull("var(--color)"));
		assertNull(ColorFormat.parseColorOrNull("#12345"));
		assertNull(ColorFormat.parseColorOrNull("#123456 x"));
		assertNull(ColorFormat.parseColorOrNull("rgb(256,0,0)"));
	}

	public void testFormat() {
		assertFormat(0x000000, 0x00, "transparent");
		assertFormat(0x123456, 0xFF, "#123456");
		assertFormat(0x123456, 0x78, "#12345678");
		assertFormat(0x89ABCD, 0xEF, "#89ABCDEF");

		assertFormat("#FFFFFF", Color.WHITE);
		assertFormat("#FF0000", Color.RED);
		assertFormat("#00FF00", Color.GREEN);
		assertFormat("#0000FF", Color.BLUE);
	}

	private void assertParse(int rgb, int a, String colorSpec) throws ParseException {
		Color color = ColorFormat.parseColor(colorSpec);
		assertEquals(a, color.getAlpha());
		assertEquals(rgba(rgb, a), color.getRGB());
	}

	private void assertInvalid(String colorSpec) {
		ParsePosition pos = new ParsePosition(0);
		assertNull(colorSpec, ColorFormat.INSTANCE.parseObject(colorSpec, pos));
		assertTrue(colorSpec, pos.getErrorIndex() >= 0);
		assertNull(colorSpec, ColorFormat.parseColorOrNull(colorSpec));
		try {
			ColorFormat.parseColor(colorSpec);
			fail("Expected error for: " + colorSpec);
		} catch (TopLogicException ex) {
			// Expected.
		}
	}

	private int rgba(int rgb, int a) {
		return (a << 24) | rgb;
	}

	private void assertFormat(int rgb, int a, String expected) {
		assertFormat(expected, new Color(rgba(rgb, a), true));
	}

	private void assertFormat(String expected, Color colorValue) {
		assertEquals(expected, ColorFormat.formatColor(colorValue));
	}
}
