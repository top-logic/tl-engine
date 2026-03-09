/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api.messages;

import com.top_logic.ai.chat.layout.ReviewNotification;

/**
 * Helper that serializes a {@link ReviewNotification} to JSON for the {@code review_alert} SSE
 * event type.
 *
 * <p>
 * Uses manual JSON construction to avoid a dependency on Jackson.
 * </p>
 */
public class ReviewAlertEvent {

	/**
	 * Serializes the given notification to a JSON string.
	 *
	 * @param notification
	 *        The review notification to serialize.
	 * @return A JSON object with {@code title}, {@code category}, {@code severity}, and
	 *         {@code timestamp} fields.
	 */
	public static String toJson(ReviewNotification notification) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append("\"title\":\"").append(escapeJson(notification.title())).append('"');
		sb.append(",\"category\":\"").append(escapeJson(notification.category())).append('"');
		sb.append(",\"severity\":\"").append(escapeJson(notification.severity())).append('"');
		String ts = notification.timestamp() != null ? notification.timestamp().toString() : "";
		sb.append(",\"timestamp\":\"").append(escapeJson(ts)).append('"');
		sb.append('}');
		return sb.toString();
	}

	/**
	 * Escapes JSON special characters in a string value.
	 */
	private static String escapeJson(String s) {
		if (s == null) {
			return "";
		}
		return s.replace("\\", "\\\\")
			.replace("\"", "\\\"")
			.replace("\n", "\\n")
			.replace("\r", "\\r")
			.replace("\t", "\\t");
	}
}
