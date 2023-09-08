/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.format;

import java.awt.Color;
import java.text.ParseException;

import junit.framework.TestCase;

import com.top_logic.layout.form.format.ColorFormat;

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
