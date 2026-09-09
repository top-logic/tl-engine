/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.routing;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Percent-encoding of route values as URL path segments.
 *
 * <p>
 * A route value is an arbitrary string - the identifier of an object, a business key - while a path
 * segment is delimited by {@code /} and shares the URL with the query and the fragment. The
 * encoding therefore keeps only the unreserved characters of RFC 3986 ({@code A-Z}, {@code a-z},
 * {@code 0-9}, {@code -}, {@code .}, {@code _}, {@code ~}) and writes every other character as the
 * percent-escapes of its UTF-8 bytes. A value containing {@code /} thus stays one segment, and
 * {@code ?}, {@code #} and {@code %} keep their meaning for the part of the URL they introduce.
 * </p>
 *
 * @see RoutePattern#produce(java.util.Map)
 * @see RoutePattern#match(String)
 */
public class RouteEncoding {

	private static final char ESCAPE = '%';

	private static final char[] HEX_DIGITS = "0123456789ABCDEF".toCharArray();

	/**
	 * Encodes the given value as a single URL path segment.
	 *
	 * @param value
	 *        The value to encode.
	 * @return The encoded segment.
	 */
	public static String encodeSegment(String value) {
		int length = value.length();
		StringBuilder result = new StringBuilder(length);
		for (int n = 0; n < length; n++) {
			char ch = value.charAt(n);
			if (isUnreserved(ch)) {
				result.append(ch);
			} else {
				// A character outside the basic multilingual plane is a surrogate pair, whose halves
				// have no encoding of their own: the pair is encoded as the single code point it
				// forms.
				int end = Character.isHighSurrogate(ch) && n + 1 < length
					&& Character.isLowSurrogate(value.charAt(n + 1)) ? n + 2 : n + 1;
				appendEscaped(result, value.substring(n, end));
				n = end - 1;
			}
		}
		return result.toString();
	}

	/**
	 * Decodes a URL path segment into the value it carries.
	 *
	 * <p>
	 * An escape that names no byte - a {@code %} not followed by two hexadecimal digits - stands for
	 * itself, so that a segment which was never encoded is still readable as a value.
	 * </p>
	 *
	 * @param segment
	 *        The segment to decode.
	 * @return The decoded value.
	 */
	public static String decodeSegment(String segment) {
		int escape = segment.indexOf(ESCAPE);
		if (escape < 0) {
			return segment;
		}

		int length = segment.length();
		StringBuilder result = new StringBuilder(length);
		result.append(segment, 0, escape);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		for (int n = escape; n < length; n++) {
			char ch = segment.charAt(n);
			int value = ch == ESCAPE && n + 2 < length ? hexValue(segment.charAt(n + 1), segment.charAt(n + 2)) : -1;
			if (value >= 0) {
				// Collected rather than decoded one by one: a code point above the ASCII range is a
				// run of escapes whose bytes only form a character together.
				bytes.write(value);
				n += 2;
			} else {
				flush(bytes, result);
				result.append(ch);
			}
		}
		flush(bytes, result);
		return result.toString();
	}

	private static void appendEscaped(StringBuilder result, String text) {
		for (byte b : text.getBytes(StandardCharsets.UTF_8)) {
			result.append(ESCAPE);
			result.append(HEX_DIGITS[(b >> 4) & 0x0F]);
			result.append(HEX_DIGITS[b & 0x0F]);
		}
	}

	private static void flush(ByteArrayOutputStream bytes, StringBuilder result) {
		if (bytes.size() > 0) {
			result.append(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
			bytes.reset();
		}
	}

	private static boolean isUnreserved(char ch) {
		return (ch >= 'A' && ch <= 'Z') || (ch >= 'a' && ch <= 'z') || (ch >= '0' && ch <= '9')
			|| ch == '-' || ch == '.' || ch == '_' || ch == '~';
	}

	private static int hexValue(char high, char low) {
		int highValue = hexDigit(high);
		int lowValue = hexDigit(low);
		if (highValue < 0 || lowValue < 0) {
			return -1;
		}
		return (highValue << 4) | lowValue;
	}

	private static int hexDigit(char ch) {
		if (ch >= '0' && ch <= '9') {
			return ch - '0';
		}
		if (ch >= 'A' && ch <= 'F') {
			return ch - 'A' + 10;
		}
		if (ch >= 'a' && ch <= 'f') {
			return ch - 'a' + 10;
		}
		return -1;
	}

}
