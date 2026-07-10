/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.util;

import java.io.IOException;
import java.util.Map;

import com.top_logic.tl.ai.core.tools.model.messages.ErrorResponse;
import com.top_logic.basic.Logger;

import de.haumacher.msgbuf.data.DataObject;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonWriter;

/**
 * Utility for serializing msgbuf data classes to JSON with error handling.
 *
 * <p>
 * This utility provides a centralized way to convert msgbuf data objects to JSON strings with
 * consistent error handling and logging. It ensures that serialization failures are gracefully
 * handled with fallback error responses.
 * </p>
 *
 * <h3>Usage Pattern:</h3>
 *
 * <pre>
 * public String myToolMethod(String param) {
 *     try {
 *         // Business logic returns a data class. The tool already runs inside a TopLogic
 *         // interaction established by the call boundary, so no per-tool context setup is needed.
 *         MyResponse result = MyResponse.create();
 *         // ... populate result ...
 *
 *         // Serialize once at the end
 *         return DataClassSerializer.toJson(result, MyTool.class, "myToolMethod()");
 *
 *     } catch (Exception ex) {
 *         // Exception handling at outermost level
 *         Logger.error("Error: " + ex.getMessage(), ex, MyTool.class);
 *         return DataClassSerializer.errorToJson("Operation failed: " + ex.getMessage(),
 *             MyTool.class, "myToolMethod()");
 *     }
 * }
 * </pre>
 */
public final class DataClassSerializer {

	/** Private constructor for utility class. */
	private DataClassSerializer() {
		// Utility class - no instances
	}

	/**
	 * Serializes a data object to JSON string with logging.
	 *
	 * <p>
	 * If serialization fails, this method automatically returns a fallback error JSON response
	 * instead of throwing an exception.
	 * </p>
	 *
	 * @param dataObject
	 *        The data object to serialize.
	 * @param logContext
	 *        Class for logging context (used to identify the source in logs).
	 * @param operationName
	 *        Operation name for logging (e.g., "getInstance()").
	 * @return JSON string representation of the data object.
	 */
	public static String toJson(DataObject dataObject, Class<?> logContext, String operationName) {
		StringW buffer = new StringW();
		try (JsonWriter json = new JsonWriter(buffer)) {
			dataObject.writeTo(json);
		} catch (IOException ex) {
			// Serialization failed - return error response
			String errorMsg = "Failed to serialize response: " + ex.getMessage();
			Logger.error(errorMsg, ex, logContext);
			return buildFallbackErrorJson(errorMsg);
		}

		String result = buffer.toString();
		return result;
	}

	/**
	 * Serializes a data object to JSON string with logging.
	 *
	 * <p>
	 * If serialization fails, this method automatically returns a fallback error JSON response
	 * instead of throwing an exception.
	 * </p>
	 *
	 * <p>
	 * Tool call tracking is handled automatically by the {@code TimelineAgentListener} and does not
	 * need to be performed here. The {@code args} parameter is retained for API compatibility.
	 * </p>
	 *
	 * @param dataObject
	 *        The data object to serialize.
	 * @param logContext
	 *        Class for logging context (used to identify the source in logs).
	 * @param operationName
	 *        Operation name for logging (e.g., "getInstance()"). Tool name is extracted from this.
	 * @param args
	 *        Optional map of tool arguments (retained for API compatibility).
	 * @return JSON string representation of the data object.
	 */
	public static String toJson(DataObject dataObject, Class<?> logContext, String operationName,
			Map<String, Object> args) {
		return toJson(dataObject, logContext, operationName);
	}

	/**
	 * Creates an error response and serializes it to JSON.
	 *
	 * <p>
	 * This is a convenience method for creating error responses from exception messages or
	 * validation failures.
	 * </p>
	 *
	 * @param errorMessage
	 *        The error message.
	 * @param logContext
	 *        Class for logging context.
	 * @param operationName
	 *        Operation name for logging.
	 * @return JSON error response string.
	 */
	public static String errorToJson(String errorMessage, Class<?> logContext, String operationName) {
		ErrorResponse error = ErrorResponse.create();
		error.setError(errorMessage);
		return toJson(error, logContext, operationName);
	}

	/**
	 * Creates an error response and serializes it to JSON.
	 *
	 * <p>
	 * This is a convenience method for creating error responses from exception messages or
	 * validation failures. The {@code args} parameter is retained for API compatibility; tool call
	 * tracking is handled automatically by the {@code TimelineAgentListener}.
	 * </p>
	 *
	 * @param errorMessage
	 *        The error message.
	 * @param logContext
	 *        Class for logging context.
	 * @param operationName
	 *        Operation name for logging. Tool name is extracted from this.
	 * @param args
	 *        Optional map of tool arguments (retained for API compatibility).
	 * @return JSON error response string.
	 */
	public static String errorToJson(String errorMessage, Class<?> logContext, String operationName,
			Map<String, Object> args) {
		ErrorResponse error = ErrorResponse.create();
		error.setError(errorMessage);
		return toJson(error, logContext, operationName, args);
	}

	/**
	 * Builds a minimal fallback error JSON when msgbuf serialization fails.
	 *
	 * <p>
	 * This method uses string escaping rather than msgbuf serialization as a last resort when even
	 * error response generation fails.
	 * </p>
	 *
	 * @param errorMessage
	 *        The error message to include.
	 * @return JSON string with escaped error message.
	 */
	private static String buildFallbackErrorJson(String errorMessage) {
		return "{\"error\":\"" + errorMessage.replace("\"", "\\\"") + "\"}";
	}
}
