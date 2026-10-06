/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.servlet;

import java.nio.charset.StandardCharsets;

import jakarta.servlet.http.HttpServletResponse;

/**
 * The {@code Content-Disposition} header of a file sent to the browser (RFC 6266).
 *
 * <p>
 * The file name is given twice: as {@code filename*} in UTF-8 (RFC 5987), so that a name with
 * characters beyond ASCII arrives intact, and as a plain {@code filename} with such characters
 * replaced, for a client that does not read the former.
 * </p>
 */
public class ContentDisposition {

	/** Name of the header. */
	public static final String HEADER = "Content-Disposition";

	/**
	 * Marks the response as a file to save under the given name, rather than to display.
	 */
	public static void setAttachment(HttpServletResponse response, String name) {
		response.setHeader(HEADER, attachment(name));
	}

	/**
	 * Marks the response as a file to display in the browser, saved under the given name.
	 */
	public static void setInline(HttpServletResponse response, String name) {
		response.setHeader(HEADER, inline(name));
	}

	/**
	 * The header value of an attachment of the given name.
	 */
	public static String attachment(String name) {
		return value("attachment", name);
	}

	/**
	 * The header value of an inline file of the given name.
	 */
	public static String inline(String name) {
		return value("inline", name);
	}

	private static String value(String type, String name) {
		StringBuilder result = new StringBuilder(type).append("; filename=\"");
		for (int n = 0, length = name.length(); n < length; n++) {
			char ch = name.charAt(n);
			result.append(ch < 0x20 || ch > 0x7E || ch == '"' || ch == '\\' ? '_' : ch);
		}
		result.append("\"; filename*=UTF-8''");
		for (byte b : name.getBytes(StandardCharsets.UTF_8)) {
			int ch = b & 0xFF;
			if (isAttributeChar(ch)) {
				result.append((char) ch);
			} else {
				result.append('%').append(Character.toUpperCase(Character.forDigit(ch >> 4, 16)))
					.append(Character.toUpperCase(Character.forDigit(ch & 0xF, 16)));
			}
		}
		return result.toString();
	}

	/** Whether the given byte may stand unencoded in an RFC 5987 value. */
	private static boolean isAttributeChar(int ch) {
		return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '.' || ch == '-'
			|| ch == '_' || ch == '~';
	}

}
