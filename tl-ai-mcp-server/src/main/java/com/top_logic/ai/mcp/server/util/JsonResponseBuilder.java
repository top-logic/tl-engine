/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.util;

import java.io.IOException;
import java.io.StringWriter;

import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.util.Resources;

/**
 * Utility class for building JSON responses in the MCP server.
 *
 * <p>
 * This class provides helper methods to reduce boilerplate when constructing JSON responses using
 * TopLogic's {@link JsonWriter}. It follows the wrapper pattern, augmenting {@link JsonWriter} with
 * common operations while preserving the streaming nature of JSON generation.
 * </p>
 *
 * <h3>Usage Pattern:</h3>
 *
 * <pre>
 * StringWriter buffer = new StringWriter();
 * try (JsonWriter json = JsonResponseBuilder.createWriter(buffer)) {
 * 	json.beginObject();
 * 	json.name("name").value(value);
 * 	JsonResponseBuilder.writeLabelAndDescription(json, resourceKey);
 * 	json.endObject();
 * }
 * return buffer.toString();
 * </pre>
 */
public final class JsonResponseBuilder {

	/** Standard indentation for formatted JSON output. */
	private static final String INDENT = "  ";

	/** MIME type for JSON content. */
	public static final String JSON_MIME_TYPE = "application/json";

	/** Private constructor for utility class. */
	private JsonResponseBuilder() {
		// Utility class - no instances
	}

	// ========== Writer Creation ==========

	/**
	 * Creates a configured {@code JsonWriter} with standard indentation.
	 *
	 * @param buffer
	 *        The {@code StringWriter} to write to.
	 * @return A {@code JsonWriter} ready for use.
	 */
	public static JsonWriter createWriter(StringWriter buffer) {
		JsonWriter json = new JsonWriter(buffer);
		json.setIndent(INDENT);
		return json;
	}

	// ========== Optional Field Writing ==========

	/**
	 * Writes a field only if the value is non-null and non-empty.
	 *
	 * @param json
	 *        The {@code JsonWriter} to write to.
	 * @param name
	 *        The field name.
	 * @param value
	 *        The value to write (can be null).
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeOptionalField(JsonWriter json, String name, String value) throws IOException {
		if (value != null && !value.isEmpty()) {
			json.name(name).value(value);
		}
	}

	// ========== Resource Key Conversion ==========

	/**
	 * Writes label and description fields from a resource key.
	 *
	 * <p>
	 * This is the most common pattern in the codebase: retrieve the label from the resource key,
	 * and the description from the tooltip sub-key. Both fields are optional - they're only
	 * written if non-null and non-empty.
	 * </p>
	 *
	 * @param json
	 *        The {@code JsonWriter} to write to.
	 * @param resourceKey
	 *        The resource key to retrieve label and description from.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeLabelAndDescription(JsonWriter json, ResKey resourceKey) throws IOException {
		Resources resources = Resources.getInstance();

		// Write label if available
		String label = resources.getString(resourceKey, null);
		writeOptionalField(json, "label", label);

		// Write description from tooltip sub-key if available
		ResKey tooltipKey = resourceKey.tooltip();
		String description = resources.getString(tooltipKey, null);
		writeOptionalField(json, "description", description);
	}

	// ========== Error Response Building ==========

	/**
	 * Creates a standardized error response JSON string.
	 *
	 * <p>
	 * All error responses should use this format for consistency:
	 * </p>
	 *
	 * <pre>
	 * {
	 *   "error": "Error message here"
	 * }
	 * </pre>
	 *
	 * @param errorMessage
	 *        The error message to include.
	 * @return JSON string containing the error.
	 */
	public static String buildErrorJson(String errorMessage) {
		try {
			StringWriter buffer = new StringWriter();
			try (JsonWriter json = createWriter(buffer)) {
				json.beginObject();
				json.name("error").value(errorMessage);
				json.endObject();
			}
			return buffer.toString();
		} catch (IOException ex) {
			// Fallback if JSON creation fails
			return buildFallbackErrorJson(errorMessage);
		}
	}

	/**
	 * Creates a minimal fallback error response when JSON generation fails.
	 *
	 * <p>
	 * This uses string escaping rather than JsonWriter as a last resort when even error response
	 * generation fails.
	 * </p>
	 *
	 * @param errorMessage
	 *        The error message to include.
	 * @return JSON string with escaped error message.
	 */
	private static String buildFallbackErrorJson(String errorMessage) {
		String escaped = errorMessage.replace("\"", "\\\"");
		return "{\"error\": \"" + escaped + "\"}";
	}
}
