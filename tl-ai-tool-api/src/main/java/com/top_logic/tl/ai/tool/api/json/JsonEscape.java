/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api.json;

/**
 * Shared JSON string escaping for building inline JSON values by hand.
 */
public class JsonEscape {

	/**
	 * Escapes the given string for use as a JSON string value (without surrounding quotes).
	 *
	 * <p>
	 * Besides the standard {@code " \ \n \r \t} escapes, all other control characters below
	 * {@code U+0020} are emitted as {@code \\uXXXX} escapes. A {@code null} input yields the empty
	 * string.
	 * </p>
	 */
	public static String escape(String s) {
		if (s == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		for (char c : s.toCharArray()) {
			switch (c) {
				case '"':
					sb.append("\\\"");
					break;
				case '\\':
					sb.append("\\\\");
					break;
				case '\n':
					sb.append("\\n");
					break;
				case '\r':
					sb.append("\\r");
					break;
				case '\t':
					sb.append("\\t");
					break;
				default:
					if (c < 32) {
						sb.append(String.format("\\u%04x", (int) c));
					} else {
						sb.append(c);
					}
			}
		}
		return sb.toString();
	}
}
