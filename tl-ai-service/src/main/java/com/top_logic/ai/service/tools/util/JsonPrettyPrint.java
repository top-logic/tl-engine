/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.util;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.json.JSON;

/**
 * Utility for pretty-printing JSON for display (e.g. in chat tool results).
 * Outputs one property per line for readability.
 */
public final class JsonPrettyPrint {

	private static final String INDENT = "  ";

	private JsonPrettyPrint() {
		// utility
	}

	/**
	 * Pretty-prints a JSON string with indentation and one property per line.
	 *
	 * @param rawJson
	 *        Raw (possibly minified) JSON string; may be null or empty.
	 * @return Pretty-printed JSON, or the original string if parsing fails or input is null/empty.
	 */
	public static String prettyPrint(String rawJson) {
		if (rawJson == null || rawJson.isEmpty()) {
			return rawJson;
		}
		String trimmed = rawJson.trim();
		if (trimmed.isEmpty()) {
			return rawJson;
		}
		try {
			Object parsed = JSON.fromString(rawJson);
			StringBuilder out = new StringBuilder();
			appendValue(out, parsed, 0);
			return out.toString();
		} catch (Exception e) {
			return rawJson;
		}
	}

	private static void appendValue(StringBuilder out, Object value, int depth) {
		if (value == null) {
			out.append("null");
		} else if (value instanceof String) {
			appendEscaped(out, (String) value);
		} else if (value instanceof Number || value instanceof Boolean) {
			out.append(value);
		} else if (value instanceof List<?>) {
			List<?> list = (List<?>) value;
			out.append("[\n");
			String pad = INDENT.repeat(depth + 1);
			for (int i = 0; i < list.size(); i++) {
				out.append(pad);
				appendValue(out, list.get(i), depth + 1);
				if (i < list.size() - 1) {
					out.append(",");
				}
				out.append("\n");
			}
			out.append(INDENT.repeat(depth)).append("]");
		} else if (value instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) value;
			out.append("{\n");
			String pad = INDENT.repeat(depth + 1);
			int i = 0;
			for (Map.Entry<?, ?> e : map.entrySet()) {
				out.append(pad);
				appendEscaped(out, String.valueOf(e.getKey()));
				out.append(": ");
				appendValue(out, e.getValue(), depth + 1);
				if (++i < map.size()) {
					out.append(",");
				}
				out.append("\n");
			}
			out.append(INDENT.repeat(depth)).append("}");
		} else {
			appendEscaped(out, String.valueOf(value));
		}
	}

	private static void appendEscaped(StringBuilder out, String s) {
		out.append('"');
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			switch (c) {
				case '"':
					out.append("\\\"");
					break;
				case '\\':
					out.append("\\\\");
					break;
				case '\n':
					out.append("\\n");
					break;
				case '\r':
					out.append("\\r");
					break;
				case '\t':
					out.append("\\t");
					break;
				default:
					if (c < ' ') {
						out.append(String.format("\\u%04x", (int) c));
					} else {
						out.append(c);
					}
					break;
			}
		}
		out.append('"');
	}
}
