/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.mcp.server.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for validating and extracting arguments from MCP tool calls.
 *
 * <p>
 * This class provides helper methods to extract and validate arguments passed to MCP tools
 * from the generic argument map structure used by the MCP protocol.
 * </p>
 */
public final class ToolArgumentUtil {

	private ToolArgumentUtil() {
		// Utility class - prevent instantiation
	}

	/**
	 * Requires a non-empty string argument from the arguments map.
	 *
	 * @param arguments
	 *        The arguments map from the tool call.
	 * @param key
	 *        The argument key to look up.
	 * @param fieldLabel
	 *        Human-readable label for error messages.
	 * @return The trimmed string value.
	 * @throws ToolInputException
	 *         If the argument is missing or empty.
	 */
	public static String requireNonEmptyString(
			Map<String, Object> arguments,
			String key,
			String fieldLabel) throws ToolInputException {

		if (arguments == null) {
			throw new ToolInputException(fieldLabel + " is required");
		}

		Object value = arguments.get(key);
		if (value == null) {
			throw new ToolInputException(fieldLabel + " is required");
		}

		String text = value.toString().trim();
		if (text.isEmpty()) {
			throw new ToolInputException(fieldLabel + " cannot be empty");
		}

		return text;
	}

	/**
	 * Exception thrown when tool input validation fails.
	 */
	public static final class ToolInputException extends Exception {
		/**
		 * Creates a new exception with the given message.
		 *
		 * @param message
		 *        The error message.
		 */
		public ToolInputException(String message) {
			super(message);
		}
	}

	/**
	 * Extracts an optional string argument from the arguments map.
	 *
	 * @param arguments
	 *        The arguments map.
	 * @param key
	 *        The argument key.
	 * @param defaultValue
	 *        The default value if the argument is not present.
	 * @return The string value, or the default if not present.
	 */
	public static String getOptionalString(Map<String, Object> arguments, String key, String defaultValue) {
		Object value = arguments.get(key);
		if (value == null) {
			return defaultValue;
		}
		String text = value.toString().trim();
		return text.isEmpty() ? defaultValue : text;
	}

	/**
	 * Extracts a boolean argument from the arguments map.
	 *
	 * @param arguments
	 *        The arguments map.
	 * @param key
	 *        The argument key.
	 * @param defaultValue
	 *        The default value if the argument is not present.
	 * @return The boolean value.
	 */
	public static boolean getBooleanArgument(Map<String, Object> arguments, String key, boolean defaultValue) {
		Object value = arguments.get(key);
		return value instanceof Boolean ? (Boolean) value : defaultValue;
	}

	/**
	 * Extracts an optional list of strings from the arguments map.
	 *
	 * @param arguments
	 *        The arguments map.
	 * @param key
	 *        The argument key.
	 * @return List of strings, or {@code null} if not present.
	 * @throws IllegalArgumentException
	 *         If the value is not an array or contains non-strings.
	 */
	public static List<String> getOptionalStringList(Map<String, Object> arguments, String key) {
		Object value = arguments.get(key);
		if (value == null) {
			return null; // "nicht gesetzt"
		}
		if (!(value instanceof Iterable<?>)) {
			throw new IllegalArgumentException("Expected an array for '" + key + "'.");
		}
		List<String> result = new ArrayList<>();
		for (Object item : (Iterable<?>) value) {
			if (!(item instanceof String)) {
				throw new IllegalArgumentException("Expected all entries of '" + key + "' to be strings.");
			}
			result.add((String) item);
		}
		return result;
	}

	/**
	 * Extracts an optional list of objects (maps) from the arguments.
	 *
	 * @param arguments
	 *        The arguments map.
	 * @param key
	 *        The argument key.
	 * @return List of maps, or empty list if not present.
	 * @throws IllegalArgumentException
	 *         If the value is not an array or contains non-maps.
	 */
	public static List<Map<String, Object>> getOptionalObjectList(Map<String, Object> arguments, String key) {
		Object value = arguments.get(key);
		if (value == null) {
			return java.util.Collections.emptyList();
		}
		if (!(value instanceof Iterable<?>)) {
			throw new IllegalArgumentException("Expected an array for '" + key + "'.");
		}
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object item : (Iterable<?>) value) {
			if (!(item instanceof Map)) {
				throw new IllegalArgumentException("Expected all entries of '" + key + "' to be objects.");
			}
			@SuppressWarnings("unchecked")
			Map<String, Object> map = (Map<String, Object>) item;
			result.add(map);
		}
		return result;
	}
}
