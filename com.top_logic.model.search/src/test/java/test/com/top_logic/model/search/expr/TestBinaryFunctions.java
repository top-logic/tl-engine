/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import junit.framework.Test;

/**
 * Tests for the TL-Script functions accessing the properties of a binary value.
 *
 * @see com.top_logic.model.search.expr.config.operations.BinaryFunctions
 */
@SuppressWarnings("javadoc")
public class TestBinaryFunctions extends AbstractSearchExpressionTest {

	public void testContentType() throws Exception {
		Object result =
			eval("binaryContentType(base64Decode('SGVsbG8=', name: 'greeting.txt', contentType: 'text/plain'))");

		assertEquals("text/plain", result);
	}

	public void testContentTypeChained() throws Exception {
		Object result =
			eval("base64Decode('SGVsbG8=', name: 'logo.png', contentType: 'image/png').binaryContentType()");

		assertEquals("image/png", result);
	}

	public void testSize() throws Exception {
		Object result = eval("binarySize(base64Decode('SGVsbG8=', name: 'greeting.txt', contentType: 'text/plain'))");

		// TL-Script normalizes all numbers to floating point values.
		assertEquals(Double.valueOf(5), result);
	}

	public void testConstructedBinary() throws Exception {
		Object result = eval("binaryContentType(binary(name: 'icon.svg', data: '<svg/>',"
			+ " contentType: 'image/svg+xml'))");

		// The content type of a constructed text value carries the character encoding as parameter.
		assertTrue("Unexpected content type: " + result, ((String) result).startsWith("image/svg+xml"));
	}

	public void testDistinguishesValues() throws Exception {
		Object image = eval("base64Decode('SGVsbG8=', name: 'a.png', contentType: 'image/png').binaryContentType()");
		Object document =
			eval("base64Decode('SGVsbG8sIFdvcmxkIQ==', name: 'a.pdf', contentType: 'application/pdf')"
				+ ".binaryContentType()");

		assertEquals("image/png", image);
		assertEquals("application/pdf", document);

		Object imageSize = eval("base64Decode('SGVsbG8=', name: 'a.png', contentType: 'image/png').binarySize()");
		Object documentSize =
			eval("base64Decode('SGVsbG8sIFdvcmxkIQ==', name: 'a.pdf', contentType: 'application/pdf').binarySize()");

		assertEquals(Double.valueOf(5), imageSize);
		assertEquals(Double.valueOf(13), documentSize);
	}

	public void testNoValue() throws Exception {
		assertNull(eval("binaryContentType(null)"));
		assertNull(eval("binarySize(null)"));
	}

	public static Test suite() {
		return suite(TestBinaryFunctions.class);
	}

}
