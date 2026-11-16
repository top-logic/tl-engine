/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.basic;

import java.util.Date;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.TLTestSetup;

import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataURI;
import com.top_logic.layout.basic.ThemeImage;

/**
 * Test for {@link ThemeImage#fromValue(Object)}.
 */
@SuppressWarnings("javadoc")
public class TestThemeImageFromValue extends BasicTestCase {

	private static final byte[] DATA = { 1, 2, 3 };

	private static final String PNG_URI = BinaryDataURI.PREFIX + "image/png" + BinaryDataURI.BASE64_SEPARATOR + "AQID";

	private static final String CSS_ICON = "css:fas fa-car-side";

	public void testThemeImage() {
		ThemeImage icon = ThemeImage.internalDecode(CSS_ICON);
		assertSame(icon, ThemeImage.fromValue(icon));
	}

	public void testText() {
		assertEquals(CSS_ICON, ThemeImage.fromValue(CSS_ICON).toEncodedForm());
		assertEquals(CSS_ICON, ThemeImage.fromValue(new StringBuilder("  " + CSS_ICON + " ")).toEncodedForm());
		assertEquals("/icons/some.png", ThemeImage.fromValue("/icons/some.png").toEncodedForm());
		assertEquals(PNG_URI, ThemeImage.fromValue(PNG_URI).toEncodedForm());
	}

	public void testEmptyText() {
		assertNull(ThemeImage.fromValue(""));
		assertNull(ThemeImage.fromValue("  "));
	}

	public void testImageBinary() {
		ThemeImage icon = ThemeImage.fromValue(BinaryDataFactory.createBinaryData(DATA, "image/png"));
		assertNotNull(icon);
		assertEquals(PNG_URI, icon.toEncodedForm());
	}

	public void testNoImageBinary() {
		assertNull(ThemeImage.fromValue(BinaryDataFactory.createBinaryData(DATA, "application/pdf")));
	}

	public void testTooLargeBinary() {
		byte[] large = new byte[ThemeImage.MAX_DATA_IMAGE_SIZE + 1];
		assertNull(ThemeImage.fromValue(BinaryDataFactory.createBinaryData(large, "image/png")));
	}

	public void testNoIcon() {
		assertNull(ThemeImage.fromValue(null));
		assertNull(ThemeImage.fromValue(42));
		assertNull(ThemeImage.fromValue(new Object()));
		assertNull(ThemeImage.fromValue(new Date()));
		assertNull(ThemeImage.fromValue(List.of(CSS_ICON)));
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestThemeImageFromValue.class));
	}

}
