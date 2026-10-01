/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.nio.charset.StandardCharsets;

import com.top_logic.basic.StringServices;

/**
 * Formatting of the value of the HTTP {@link #HEADER} header (RFC 6266).
 *
 * <p>
 * The file name is given twice: as <code>filename*</code> parameter with the UTF-8 encoded name
 * according to RFC 5987, which current browsers use, and as plain <code>filename</code> parameter
 * with an ASCII approximation of the name for clients not supporting the encoded form.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ContentDisposition {

	/**
	 * Name of the HTTP header announcing how the content is presented.
	 */
	public static final String HEADER = "Content-Disposition";

	/**
	 * Disposition type for content displayed by the browser itself.
	 */
	public static final String INLINE = "inline";

	/**
	 * Disposition type for content the browser saves as file.
	 */
	public static final String ATTACHMENT = "attachment";

	private static final String FILENAME_PARAM = "filename";

	private static final String ENCODED_FILENAME_PARAM = "filename*";

	private static final String UTF_8_PREFIX = "UTF-8''";

	private static final char FALLBACK_CHAR = '_';

	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	private ContentDisposition() {
		// Utility class.
	}

	/**
	 * The value of the {@link #HEADER} header.
	 *
	 * @param dispositionType
	 *        The disposition type, {@link #INLINE} or {@link #ATTACHMENT}.
	 * @param fileName
	 *        The file name proposed to the browser, or <code>null</code> or empty for none.
	 * @return The header value, e.g.
	 *         <code>inline; filename="a_b.pdf"; filename*=UTF-8''a%C3%A4b.pdf</code>.
	 */
	public static String headerValue(String dispositionType, String fileName) {
		if (StringServices.isEmpty(fileName)) {
			return dispositionType;
		}
		return dispositionType
			+ "; " + FILENAME_PARAM + "=\"" + asciiFallback(fileName) + "\""
			+ "; " + ENCODED_FILENAME_PARAM + "=" + UTF_8_PREFIX + encodeRfc5987(fileName);
	}

	/**
	 * An ASCII approximation of the given file name usable inside a quoted string.
	 *
	 * <p>
	 * Each character outside printable ASCII, and each quote, backslash and percent sign, is
	 * replaced by an underscore.
	 * </p>
	 */
	public static String asciiFallback(String fileName) {
		StringBuilder result = new StringBuilder(fileName.length());
		for (int index = 0, length = fileName.length(); index < length;) {
			int codePoint = fileName.codePointAt(index);
			index += Character.charCount(codePoint);
			if (codePoint < 0x20 || codePoint > 0x7E || codePoint == '"' || codePoint == '\\' || codePoint == '%') {
				result.append(FALLBACK_CHAR);
			} else {
				result.append((char) codePoint);
			}
		}
		return result.toString();
	}

	/**
	 * Percent-encodes the UTF-8 bytes of the given value according to RFC 5987.
	 *
	 * <p>
	 * All bytes except the <code>attr-char</code> characters of RFC 5987 (letters, digits and
	 * <code>!#$&amp;+-.^_`|~</code>) are encoded as <code>%XX</code>.
	 * </p>
	 */
	public static String encodeRfc5987(String value) {
		byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
		StringBuilder result = new StringBuilder(bytes.length * 3);
		for (byte b : bytes) {
			int ch = b & 0xFF;
			if (isAttrChar(ch)) {
				result.append((char) ch);
			} else {
				result.append('%');
				result.append(HEX_DIGITS[ch >> 4]);
				result.append(HEX_DIGITS[ch & 0x0F]);
			}
		}
		return result.toString();
	}

	private static boolean isAttrChar(int ch) {
		if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) {
			return true;
		}
		switch (ch) {
			case '!':
			case '#':
			case '$':
			case '&':
			case '+':
			case '-':
			case '.':
			case '^':
			case '_':
			case '`':
			case '|':
			case '~':
				return true;
			default:
				return false;
		}
	}

}
