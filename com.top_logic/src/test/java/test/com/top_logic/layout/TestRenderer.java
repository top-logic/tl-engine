/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import java.io.IOException;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.Renderer;

/**
 * Test for {@link Renderer} upgrades.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestRenderer extends TestCase {

	public void testUpgrade() throws IOException {
		Renderer<CharSequence> x = new Renderer<>() {
			@Override
			public void write(DisplayContext context, TagWriter out, CharSequence value) throws IOException {
				out.writeText(value);
			}
		};

		assertWrite("foobar", x, "foobar");
		assertWrite("foobar", x.generic(), "foobar");
		assertWrite("", x.generic(), new Object());
		try {
			assertWrite("", unsafeCast(x), new Object());
			fail("Crash expected.");
		} catch (ClassCastException ex) {
			// Expected.
		}
		assertSame(x, x.generic(String.class));
	}

	@SuppressWarnings("unchecked")
	private <T> Renderer<T> unsafeCast(Renderer<?> x) {
		return (Renderer<T>) x;
	}

	private <T> void assertWrite(String expected, Renderer<T> x, T value) throws IOException {
		TagWriter buffer = new TagWriter();
		x.write(null, buffer, value);
		assertEquals(expected, buffer.toString());
	}

}
