/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.Charset;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.util.resource.UnicodeEscapeCharsetEncoder;

/**
 * Test case for {@link UnicodeEscapeCharsetEncoder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestUnicodeEscapeCharsetEncoder extends TestCase {

	/**
	 * Test encoding characters that are all within the base {@link Charset}.
	 */
	public void testEncodeAscii() throws IOException {
		assertEquals("Hello world!", encode("Hello world!", "ISO-8859-1"));
	}
	
	/**
	 * Test encoding characters that are all within the base {@link Charset}.
	 */
	public void testEncodeLatin() throws IOException {
		assertEquals("ÄÖÜ äöü ß áéíóú àèìòù âêîôû", encode("ÄÖÜ äöü ß áéíóú àèìòù âêîôû", "ISO-8859-1"));
	}
	
	
	/**
	 * Test encoding characters that are not all within the base {@link Charset}.
	 */
	public void testQuote() throws IOException {
		// 20AC is the Unicode for the Euro character, which is not part of ISO-8859-1.
		
		// Euro is escaped in Latin 1
		assertEquals("Hello\\u20ACworld!", encode("Hello\u20ACworld!", "ISO-8859-1"));
		
		// Euro is not escaped in Unicode
		assertEquals("Hello\u20ACworld!", encode("Hello\u20ACworld!", "UTF-8"));
		
		// Backslash is escaped even if it is within the base charset.
		assertEquals("Hello\\\\u20ACworld!", encode("Hello\\u20ACworld!", "ISO-8859-1"));
		assertEquals("\\\\u20ACHello\\\\u20ACworld!\\\\u20AC", encode("\\u20ACHello\\u20ACworld!\\u20AC", "ISO-8859-1"));
		assertEquals("\\\\\\\\Hello\\\\\\\\world!\\\\\\\\", encode("\\\\Hello\\\\world!\\\\", "ISO-8859-1"));
	}
	
	/**
	 * Test encoding with empty input and at start/end and in sequences with only special characters.
	 */
	public void testQuoteBorderCases() throws IOException {
		assertEquals("", encode("", "ISO-8859-1"));
		assertEquals("\\\\", encode("\\", "ISO-8859-1"));
		assertEquals("\\\\A", encode("\\A", "ISO-8859-1"));
		assertEquals("A\\\\", encode("A\\", "ISO-8859-1"));
		assertEquals("\\\\A\\\\", encode("\\A\\", "ISO-8859-1"));
		assertEquals("\\\\\\\\\\\\\\\\\\\\", encode("\\\\\\\\\\", "ISO-8859-1"));
		
		assertEquals("\\u20AC", encode("\u20AC", "ISO-8859-1"));
		assertEquals("\\u20ACA", encode("\u20ACA", "ISO-8859-1"));
		assertEquals("A\\u20AC", encode("A\u20AC", "ISO-8859-1"));
		assertEquals("\\u20ACA\\u20AC", encode("\u20ACA\u20AC", "ISO-8859-1"));
		assertEquals("\\u20AC\\u20AC\\u20AC\\u20AC\\u20AC", encode("\u20AC\u20AC\u20AC\u20AC\u20AC", "ISO-8859-1"));
	}
	

	private String encode(String input, String charsetName) throws IOException, UnsupportedEncodingException {
		ByteArrayOutputStream buffer;
		Writer out = new OutputStreamWriter(buffer = new ByteArrayOutputStream(), new UnicodeEscapeCharsetEncoder(Charset.forName(charsetName), true));
		
		out.write(input);
		
		out.flush();
		String result = new String(buffer.toByteArray(), charsetName);
		return result;
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return new TestSuite(TestUnicodeEscapeCharsetEncoder.class);
	}
	
}
