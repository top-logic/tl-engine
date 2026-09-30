/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.io.binary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.io.binary.BinaryDataURI;

/**
 * Test case for {@link BinaryDataURI}.
 *
 */
@SuppressWarnings("javadoc")
public class TestBinaryDataURI extends TestCase {

	private static final byte[] CONTENTS = "<svg xmlns=\"http://www.w3.org/2000/svg\"/>".getBytes(StandardCharsets.UTF_8);

	public void testRoundTrip() throws IOException {
		String name = "Mein Bild; mit Ümläuten, 1.svg";
		BinaryData data = BinaryDataFactory.createBinaryData(CONTENTS, "image/svg+xml", name);

		String uri = BinaryDataURI.encode(data);
		assertTrue(uri, BinaryDataURI.isDataURI(uri));
		String header = BinaryDataURI.PREFIX + "image/svg+xml;" + BinaryDataURI.NAME_PARAMETER;
		assertTrue(uri, uri.startsWith(header));
		String encodedName = uri.substring(header.length(), uri.indexOf(BinaryDataURI.BASE64_SEPARATOR));
		assertEquals("Separator characters must be escaped: " + encodedName, -1, encodedName.indexOf(';'));
		assertEquals("Separator characters must be escaped: " + encodedName, -1, encodedName.indexOf(','));

		BinaryData decoded = BinaryDataURI.decode(uri);
		assertEquals("image/svg+xml", decoded.getContentType());
		assertEquals(name, decoded.getName());
		assertContents(CONTENTS, decoded);
	}

	public void testNoName() throws IOException {
		BinaryData data = BinaryDataFactory.createBinaryData(CONTENTS, "image/png");
		assertEquals(Content.NO_NAME, data.getName());

		String uri = BinaryDataURI.encode(data);
		assertEquals(BinaryDataURI.PREFIX + "image/png" + BinaryDataURI.BASE64_SEPARATOR,
			uri.substring(0, uri.indexOf(BinaryDataURI.BASE64_SEPARATOR) + BinaryDataURI.BASE64_SEPARATOR.length()));

		BinaryData decoded = BinaryDataURI.decode(uri);
		assertEquals("image/png", decoded.getContentType());
		assertEquals(Content.NO_NAME, decoded.getName());
		assertContents(CONTENTS, decoded);
	}

	public void testEmptyContentType() {
		BinaryData decoded = BinaryDataURI.decode("data:;base64,QUJD");
		assertEquals(BinaryDataSource.CONTENT_TYPE_OCTET_STREAM, decoded.getContentType());
		assertEquals(Content.NO_NAME, decoded.getName());
		assertContents("ABC".getBytes(StandardCharsets.UTF_8), decoded);
	}

	public void testUnknownParameterIgnored() {
		BinaryData decoded = BinaryDataURI.decode("data:text/plain;charset=utf-8;name=a.txt;base64,QUJD");
		assertEquals("text/plain", decoded.getContentType());
		assertEquals("a.txt", decoded.getName());
		assertContents("ABC".getBytes(StandardCharsets.UTF_8), decoded);
	}

	public void testLineBreaksInContents() {
		BinaryData decoded = BinaryDataURI.decode("data:text/plain;base64,QUJ\n    D");
		assertContents("ABC".getBytes(StandardCharsets.UTF_8), decoded);
	}

	public void testLeadingWhitespace() {
		String uri = "\n\tdata:text/plain;base64,QUJD";
		assertTrue(BinaryDataURI.isDataURI(uri));
		assertEquals("text/plain", BinaryDataURI.decode(uri).getContentType());
	}

	public void testNoDataURI() {
		assertFalse(BinaryDataURI.isDataURI("QUJD"));
	}

	public void testMissingContentsSeparator() {
		try {
			BinaryDataURI.decode("data:image/png,QUJD");
			fail("Value without '" + BinaryDataURI.BASE64_SEPARATOR + "' must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testInvalidEscape() {
		try {
			BinaryDataURI.decode("data:image/png;name=a%ZZb.png;base64,QUJD");
			fail("Invalid escape sequence must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testNotADataURI() {
		try {
			BinaryDataURI.decode("QUJD");
			fail("Value without prefix must be rejected.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private static void assertContents(byte[] expected, BinaryData actual) {
		byte[] bytes;
		try {
			bytes = StreamUtilities.readStreamContents(actual);
		} catch (IOException ex) {
			throw new AssertionError(ex);
		}
		assertEquals(new String(expected, StandardCharsets.UTF_8), new String(bytes, StandardCharsets.UTF_8));
	}

}
