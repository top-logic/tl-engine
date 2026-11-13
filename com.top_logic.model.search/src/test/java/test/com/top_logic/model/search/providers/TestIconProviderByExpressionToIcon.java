/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.providers;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataURI;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.search.providers.IconProviderByExpression;

/**
 * Test for {@link IconProviderByExpression#toIcon(Object)}.
 */
@SuppressWarnings("javadoc")
public class TestIconProviderByExpressionToIcon extends BasicTestCase {

	private static final byte[] DATA = { 1, 2, 3 };

	public void testImageBinary() {
		ThemeImage icon = IconProviderByExpression.toIcon(BinaryDataFactory.createBinaryData(DATA, "image/png"));
		assertNotNull(icon);
		assertEquals(BinaryDataURI.PREFIX + "image/png" + BinaryDataURI.BASE64_SEPARATOR + "AQID",
			icon.toEncodedForm());
	}

	public void testNoImageBinary() {
		assertNull(IconProviderByExpression.toIcon(BinaryDataFactory.createBinaryData(DATA, "application/pdf")));
	}

	public void testTooLargeBinary() {
		byte[] large = new byte[ThemeImage.MAX_DATA_IMAGE_SIZE + 1];
		assertNull(IconProviderByExpression.toIcon(BinaryDataFactory.createBinaryData(large, "image/png")));
	}

	public void testTextAndImage() {
		ThemeImage css = IconProviderByExpression.toIcon("css:fas fa-car-side");
		assertEquals("css:fas fa-car-side", css.toEncodedForm());
		assertSame(css, IconProviderByExpression.toIcon(css));
		assertNull(IconProviderByExpression.toIcon(null));
	}

	public void testDataUriText() {
		String uri = BinaryDataURI.PREFIX + "image/png" + BinaryDataURI.BASE64_SEPARATOR + "AQID";
		assertEquals(uri, IconProviderByExpression.toIcon(uri).toEncodedForm());
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestIconProviderByExpressionToIcon.class);
	}

}
