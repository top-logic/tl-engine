/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools.util;

import java.io.IOException;
import java.io.StringWriter;

import com.top_logic.basic.util.ResKey;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.model.TLObject;
import com.top_logic.util.Resources;

/**
 * Utility class for building JSON responses in internal agent tools.
 *
 * <p>
 * This class provides helper methods to reduce boilerplate when constructing JSON responses using
 * TopLogic's {@link JsonWriter}. It follows the wrapper pattern, augmenting JsonWriter with common
 * operations while preserving the streaming nature of JSON generation.
 * </p>
 *
 * <h3>Usage Pattern:</h3>
 *
 * <pre>
 * StringWriter buffer = new StringWriter();
 * try (JsonWriter json = JsonToolResponseBuilder.createWriter(buffer)) {
 *     json.beginObject();
 *     JsonToolResponseBuilder.writeField(json, "name", value);
 *     JsonToolResponseBuilder.writeLabelAndDescription(json, resourceKey);
 *     json.endObject();
 * }
 * return buffer.toString();
 * </pre>
 */
public final class JsonToolResponseBuilder {

	/** Standard indentation for formatted JSON output. */
	private static final String INDENT = "  ";

	/** Private constructor for utility class. */
	private JsonToolResponseBuilder() {
		// Utility class - no instances
	}

	// ========== Writer Creation ==========

	/**
	 * Creates a configured JsonWriter with standard indentation.
	 *
	 * @param buffer
	 *        The StringWriter to write to.
	 * @return A JsonWriter ready for use.
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
	 *        The JsonWriter to write to.
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

	/**
	 * Writes a field unconditionally.
	 *
	 * <p>
	 * This is a convenience wrapper that matches the style of
	 * {@link #writeOptionalField(JsonWriter, String, String)} for consistency in code that mixes
	 * optional and required fields.
	 * </p>
	 *
	 * @param json
	 *        The JsonWriter to write to.
	 * @param name
	 *        The field name.
	 * @param value
	 *        The value to write.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeField(JsonWriter json, String name, String value) throws IOException {
		json.name(name).value(value);
	}

	/**
	 * Writes a numeric field unconditionally.
	 *
	 * @param json
	 *        The JsonWriter to write to.
	 * @param name
	 *        The field name.
	 * @param value
	 *        The numeric value to write.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeField(JsonWriter json, String name, Number value) throws IOException {
		json.name(name).value(value);
	}

	/**
	 * Writes a boolean field unconditionally.
	 *
	 * @param json
	 *        The JsonWriter to write to.
	 * @param name
	 *        The field name.
	 * @param value
	 *        The boolean value to write.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeField(JsonWriter json, String name, boolean value) throws IOException {
		json.name(name).value(value);
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
	 *        The JsonWriter to write to.
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

	// ========== TLObject Reference Writing ==========

	/**
	 * Writes a TLObject reference as a structured object with type and TID.
	 *
	 * <p>
	 * Used to format references to other objects. Writes:
	 * </p>
	 *
	 * <pre>
	 * {
	 *   "type": "reference",
	 *   "tid": "...",
	 *   "refType": "..."  // if type is available
	 * }
	 * </pre>
	 *
	 * @param json
	 *        The JsonWriter to write to.
	 * @param tlObject
	 *        The TLObject to write as a reference.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeTLObjectReference(JsonWriter json, TLObject tlObject) throws IOException {
		json.beginObject();
		json.name("type").value("reference");
		json.name("tid").value(tlObject.tId().toString());
		if (tlObject.tType() != null) {
			json.name("refType").value(tlObject.tType().getName());
		}
		json.endObject();
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
