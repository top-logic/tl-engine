/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;

import de.haumacher.msgbuf.data.DataObject;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.tools.util.DataClassSerializer;

/**
 * Centralizes the try / catch / log / serialize skeleton repeated by nearly every
 * {@code @ToolMethod}.
 *
 * <p>
 * Tool methods run their work, serialize the resulting {@link DataObject} response to JSON via
 * {@link DataClassSerializer}, and on any exception log it and return a serialized error response.
 * {@link #invoke} performs exactly that, preserving the established output and logging conventions:
 * </p>
 *
 * <ul>
 * <li>Success: {@code DataClassSerializer.toJson(result, toolClass, operationName, args)}.</li>
 * <li>Failure: {@code Logger.error(operationName + " error: " + ex.getMessage(), ex, toolClass)}
 * followed by {@code DataClassSerializer.errorToJson(errorPrefix + ex.getMessage(), toolClass,
 * operationName, args)}.</li>
 * </ul>
 */
public final class ToolResults {

	private ToolResults() {
		// Static utility.
	}

	/**
	 * A supplier of a tool response that may throw a checked exception.
	 */
	@FunctionalInterface
	public interface ThrowingSupplier<T> {
		/**
		 * Computes the tool response.
		 */
		T get() throws Exception;
	}

	/**
	 * Runs {@code work}, serializes its response, and turns any thrown exception into a logged,
	 * serialized error response.
	 *
	 * @param toolClass
	 *        Logging context and serialization source class.
	 * @param operationName
	 *        Operation name for logging, e.g. {@code "listModules()"}.
	 * @param args
	 *        Tool arguments (retained for API compatibility; see {@link DataClassSerializer}).
	 * @param errorPrefix
	 *        Prefix prepended to {@code ex.getMessage()} in the error response, e.g.
	 *        {@code "Error listing modules: "}.
	 * @param work
	 *        The tool logic producing the response object.
	 * @return The serialized success or error response.
	 */
	public static <T extends DataObject> String invoke(Class<?> toolClass, String operationName,
			Map<String, Object> args, String errorPrefix, ThrowingSupplier<T> work) {
		try {
			T result = work.get();
			return DataClassSerializer.toJson(result, toolClass, operationName, args);
		} catch (Exception ex) {
			Logger.error(operationName + " error: " + ex.getMessage(), ex, toolClass);
			return DataClassSerializer.errorToJson(errorPrefix + ex.getMessage(), toolClass,
				operationName, args);
		}
	}
}
