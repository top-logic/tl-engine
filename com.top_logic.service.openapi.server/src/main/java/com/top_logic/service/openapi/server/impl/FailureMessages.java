/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.error.TopLogicException;

/**
 * Builds the messages of failed API requests.
 *
 * <p>
 * The messages of {@link TopLogicException}s are resolved lazily and may access the objects they
 * were created with. Therefore, the messages must be built before the interaction context in which
 * the failure occurred ends.
 * </p>
 */
public final class FailureMessages {

	private FailureMessages() {
		// Utility class.
	}

	/**
	 * The messages of the given exception and all its causes, joined by ": ".
	 *
	 * <p>
	 * A message that occurs several times in the chain of causes is only reported once.
	 * </p>
	 */
	public static String messages(Throwable ex) {
		StringBuilder buffer = new StringBuilder();
		appendMessages(buffer, new HashSet<>(), ex);
		return buffer.toString();
	}

	/**
	 * The message of a failed script execution.
	 *
	 * <p>
	 * The message consists of the arguments of the request, the failed expression of the innermost
	 * {@link SearchExpression} evaluation failure together with its function arguments, and the
	 * messages of the causes of that failure. The variables of the evaluation context are not
	 * reported: Besides the request arguments, they mainly consist of the functions the script binds
	 * to variables, i.e. of the script of the API method itself.
	 * </p>
	 *
	 * <p>
	 * Must be called within the interaction context in which the script was executed.
	 * </p>
	 *
	 * @param arguments
	 *        The arguments passed to the script.
	 * @param ex
	 *        The failure of the script execution.
	 */
	public static String scriptFailure(Map<String, Object> arguments, Throwable ex) {
		StringBuilder buffer = new StringBuilder();
		buffer.append("arguments: ");
		appendArguments(buffer, arguments);

		TopLogicException evaluationFailure = innermostEvaluationFailure(ex);
		if (evaluationFailure == null) {
			appendMessages(buffer, new HashSet<>(), ex);
		} else {
			Object[] failureArguments = evaluationFailure.getErrorKey().arguments();
			appendMessage(buffer, "Evaluation of '" + failureArguments[0] + "' failed, arguments: "
				+ valueString(failureArguments[1]));
			appendMessages(buffer, new HashSet<>(), evaluationFailure.getCause());
		}
		return buffer.toString();
	}

	private static TopLogicException innermostEvaluationFailure(Throwable ex) {
		TopLogicException result = null;
		for (Throwable current = ex; current != null; current = current.getCause()) {
			if (isEvaluationFailure(current)) {
				result = (TopLogicException) current;
			}
		}
		return result;
	}

	private static boolean isEvaluationFailure(Throwable ex) {
		if (!(ex instanceof TopLogicException)) {
			return false;
		}
		ResKey errorKey = ((TopLogicException) ex).getErrorKey();
		return errorKey != null && errorKey.plain() == I18NConstants.ERROR_EVALUATION_FAILED__EXPR_ARGS_DEFS
			&& errorKey.arguments().length == 3;
	}

	private static void appendArguments(StringBuilder buffer, Map<String, Object> arguments) {
		buffer.append('{');
		boolean first = true;
		for (Entry<String, Object> argument : arguments.entrySet()) {
			if (first) {
				first = false;
			} else {
				buffer.append(", ");
			}
			buffer.append(argument.getKey()).append('=').append(valueString(argument.getValue()));
		}
		buffer.append('}');
	}

	private static String valueString(Object value) {
		if (value instanceof byte[]) {
			return "byte[" + ((byte[]) value).length + "]";
		}
		try {
			return String.valueOf(value);
		} catch (RuntimeException ex) {
			return "<" + value.getClass().getName() + ": toString() failed: " + ex.getMessage() + ">";
		}
	}

	private static void appendMessages(StringBuilder buffer, Set<String> seenMessages, Throwable ex) {
		for (Throwable current = ex; current != null; current = current.getCause()) {
			String message = getMessage(current);
			if (seenMessages.add(message)) {
				appendMessage(buffer, message);
			}
		}
	}

	private static String getMessage(Throwable ex) {
		String message = ex.getMessage();
		if (message == null) {
			message = ex.getClass().getName();
		}
		return message;
	}

	private static void appendMessage(StringBuilder buffer, String message) {
		int length = buffer.length();
		if (length > 0) {
			if (buffer.charAt(length - 1) == '.') {
				buffer.setLength(length - 1);
			}
			buffer.append(": ");
		}
		buffer.append(message);
	}

}
