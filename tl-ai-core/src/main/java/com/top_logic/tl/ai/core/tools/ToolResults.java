/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools;

import java.util.Map;
import java.util.function.UnaryOperator;

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
 * <li>Failure: one log line, at {@code error} with the stack trace for a fault of this
 * application and at {@code info} without one for a refusal (an
 * {@link IllegalArgumentException}), followed by
 * {@code DataClassSerializer.errorToJson(errorPrefix + ex.getMessage(), toolClass, operationName,
 * args)}.</li>
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
		return invoke(toolClass, operationName, args, errorPrefix, UnaryOperator.identity(), work);
	}

	/**
	 * Runs {@code work} like {@link #invoke(Class, String, Map, String, ThrowingSupplier)} does, and
	 * puts {@code framing} around the serialized response.
	 *
	 * <p>
	 * Around the <b>success</b> response and only around it. The framing a caller passes says something
	 * about what the tool disclosed - that it is business data somebody else wrote, say. An error
	 * response discloses nothing: it is this application's own sentence about a call that failed, and
	 * framing it as content from elsewhere would be untrue. A framing that throws is treated like a
	 * failure of the work itself, so a broken framing cannot hand the model an unframed payload.
	 * </p>
	 *
	 * <p>
	 * <b>A tool that frames here declares {@link com.top_logic.tl.ai.tool.api.AnswerMarking#TOOL}.</b>
	 * The framework marks a {@code @ToolMethod} answer by default, and it marks the whole answer,
	 * because at the reflective seam a disclosed record and a sentence about a failed call are both
	 * just a string. Drawing the line between them - and naming a better {@code source} than the tool's
	 * name - is what this overload is for, and a tool that does it says so on its annotation.
	 * </p>
	 *
	 * @param framing
	 *        Applied to the serialized success response.
	 * @see #invoke(Class, String, Map, String, ThrowingSupplier)
	 */
	public static <T extends DataObject> String invoke(Class<?> toolClass, String operationName,
			Map<String, Object> args, String errorPrefix, UnaryOperator<String> framing,
			ThrowingSupplier<T> work) {
		try {
			T result = work.get();
			return framing.apply(DataClassSerializer.toJson(result, toolClass, operationName, args));
		} catch (Exception ex) {
			logFailure(toolClass, operationName, ex);
			return DataClassSerializer.errorToJson(errorPrefix + ex.getMessage(), toolClass,
				operationName, args);
		}
	}

	/**
	 * Writes the operational log line for a call that did not produce a response.
	 *
	 * <p>
	 * Two different things end up here and they are not equally interesting to an operator. An
	 * {@link IllegalArgumentException} is how these tools refuse: the model named a module, a type or
	 * an identifier this application does not have, and the sentence it carries is the answer to that.
	 * Nothing is wrong with the installation, so it is logged at info and without a stack trace - the
	 * trace would only point back at the line that composed the refusal. Every other exception is a
	 * fault of this application and keeps {@code Logger.error} with its trace, which is what an
	 * operator is watching the log for (Gitea #387).
	 * </p>
	 */
	private static void logFailure(Class<?> toolClass, String operationName, Exception ex) {
		if (ex instanceof IllegalArgumentException) {
			Logger.info(operationName + " refused: " + ex.getMessage(), toolClass);
		} else {
			Logger.error(operationName + " error: " + ex.getMessage(), ex, toolClass);
		}
	}
}
