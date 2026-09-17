/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.binary;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import com.top_logic.basic.io.Content;

/**
 * Codec representing a {@link BinaryDataSource} as a <code>data</code> URI.
 *
 * <p>
 * An encoded value starts with {@link #PREFIX}, followed by the content type, followed by URI
 * parameters, followed by {@link #BASE64_SEPARATOR} and the base64-encoded contents:
 * </p>
 *
 * <pre>
 * data:image/svg+xml;name=My%20Bild.svg;base64,PHN2Zy8+
 * </pre>
 *
 * <p>
 * The only parameter understood is {@link #NAME_PARAMETER} transporting the
 * {@link BinaryDataSource#getName() file name}. It is present only for a source that has a name
 * other than {@link Content#NO_NAME}. Its value is percent-encoded in UTF-8, so that neither the
 * parameter separator, nor the separator to the contents, nor non-ASCII characters can break the
 * syntax. Other parameters are ignored by {@link #decode(String)}.
 * </p>
 *
 */
public class BinaryDataURI {

	/**
	 * Prefix of a data URI.
	 */
	public static final String PREFIX = "data:";

	/**
	 * Separator between the header of a data URI and its base64-encoded contents.
	 */
	public static final String BASE64_SEPARATOR = ";base64,";

	/**
	 * URI parameter transporting the {@link BinaryDataSource#getName() file name}, including its
	 * assignment character.
	 */
	public static final String NAME_PARAMETER = "name=";

	/**
	 * Separator between the content type and the parameters of a data URI, and between two
	 * parameters.
	 */
	private static final char PARAMETER_SEPARATOR = ';';

	/**
	 * Character introducing a percent-encoded byte in a parameter value.
	 */
	private static final char ESCAPE = '%';

	/**
	 * Whether the given value is a data URI, i.e. whether it starts with {@link #PREFIX}.
	 *
	 * <p>
	 * Leading whitespace is ignored, since a value read from XML may be indented.
	 * </p>
	 */
	public static boolean isDataURI(String value) {
		return value.stripLeading().startsWith(PREFIX);
	}

	/**
	 * Encodes the given contents as data URI.
	 *
	 * @param data
	 *        The contents to encode.
	 * @return A value that {@link #decode(String)} reads back.
	 * @throws IOException
	 *         If reading the contents fails.
	 */
	public static String encode(BinaryDataSource data) throws IOException {
		StringBuilder result = new StringBuilder(PREFIX);

		String contentType = data.getContentType();
		result.append(contentType == null ? BinaryDataSource.CONTENT_TYPE_OCTET_STREAM : contentType);

		String name = data.getName();
		if (name != null && !Content.NO_NAME.equals(name)) {
			result.append(PARAMETER_SEPARATOR);
			result.append(NAME_PARAMETER);
			appendEncoded(result, name);
		}

		result.append(BASE64_SEPARATOR);

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		data.deliverTo(buffer);
		result.append(Base64.getEncoder().encodeToString(buffer.toByteArray()));

		return result.toString();
	}

	/**
	 * Decodes a data URI produced by {@link #encode(BinaryDataSource)}.
	 *
	 * @param value
	 *        The value to decode, see {@link #isDataURI(String)}.
	 * @return The decoded contents, with the content type and name transported by the URI.
	 * @throws IllegalArgumentException
	 *         If the value is no well-formed data URI.
	 */
	public static BinaryData decode(String value) {
		String uri = value.stripLeading();
		if (!uri.startsWith(PREFIX)) {
			throw new IllegalArgumentException("Not a data URI: " + uri);
		}

		int contentsStart = uri.indexOf(BASE64_SEPARATOR);
		if (contentsStart < 0) {
			throw new IllegalArgumentException("Data URI without '" + BASE64_SEPARATOR + "' separator: " + uri);
		}

		String header = uri.substring(PREFIX.length(), contentsStart);
		String contents = uri.substring(contentsStart + BASE64_SEPARATOR.length());

		String contentType = BinaryDataSource.CONTENT_TYPE_OCTET_STREAM;
		String name = Content.NO_NAME;

		int start = 0;
		while (start <= header.length()) {
			int stop = header.indexOf(PARAMETER_SEPARATOR, start);
			if (stop < 0) {
				stop = header.length();
			}
			String part = header.substring(start, stop);
			if (start == 0) {
				if (!part.isEmpty()) {
					contentType = part;
				}
			} else if (part.startsWith(NAME_PARAMETER)) {
				name = decodeParameter(part.substring(NAME_PARAMETER.length()));
			}
			start = stop + 1;
		}

		// Note: The MIME decoder tolerates the line breaks that an XML formatter may insert.
		byte[] bytes = Base64.getMimeDecoder().decode(contents);
		return BinaryDataFactory.createBinaryData(bytes, contentType, name);
	}

	/**
	 * Appends the given parameter value percent-encoded in UTF-8.
	 */
	private static void appendEncoded(StringBuilder out, String value) {
		for (byte b : value.getBytes(StandardCharsets.UTF_8)) {
			if (isUnreserved(b)) {
				out.append((char) b);
			} else {
				out.append(ESCAPE);
				out.append(Character.toUpperCase(Character.forDigit((b >> 4) & 0x0F, 16)));
				out.append(Character.toUpperCase(Character.forDigit(b & 0x0F, 16)));
			}
		}
	}

	/**
	 * Whether the given byte is an unreserved URI character that needs no percent-encoding.
	 */
	private static boolean isUnreserved(byte b) {
		return (b >= 'a' && b <= 'z') || (b >= 'A' && b <= 'Z') || (b >= '0' && b <= '9')
			|| b == '-' || b == '.' || b == '_' || b == '~';
	}

	/**
	 * Reverse operation of {@link #appendEncoded(StringBuilder, String)}.
	 */
	private static String decodeParameter(String value) {
		byte[] raw = value.getBytes(StandardCharsets.UTF_8);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream(raw.length);
		for (int n = 0; n < raw.length; n++) {
			byte b = raw[n];
			if (b == ESCAPE) {
				if (n + 2 >= raw.length) {
					throw new IllegalArgumentException("Truncated escape sequence in data URI parameter: " + value);
				}
				int high = Character.digit(raw[n + 1], 16);
				int low = Character.digit(raw[n + 2], 16);
				if (high < 0 || low < 0) {
					throw new IllegalArgumentException("Invalid escape sequence in data URI parameter: " + value);
				}
				bytes.write((high << 4) + low);
				n += 2;
			} else {
				bytes.write(b);
			}
		}
		return new String(bytes.toByteArray(), StandardCharsets.UTF_8);
	}

}
