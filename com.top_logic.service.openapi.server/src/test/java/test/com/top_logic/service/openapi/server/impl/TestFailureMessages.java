/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.service.openapi.server.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.model.search.expr.I18NConstants;
import com.top_logic.service.openapi.server.impl.FailureMessages;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for {@link FailureMessages}.
 */
@SuppressWarnings("javadoc")
public class TestFailureMessages extends TestCase {

	public void testScriptFailureReportsInnermostEvaluationWithoutVariables() {
		Map<String, Object> arguments = new LinkedHashMap<>();
		arguments.put("uuid", "5324fb1f");
		arguments.put("count", 3.0);

		Throwable failure = new RuntimeException("outer",
			evaluationFailure("outerExpr", "(outerArg)", "outerVariables",
				evaluationFailure("innerExpr", "(innerArg)", "innerVariables",
					new IllegalStateException("Root cause."))));

		String message = FailureMessages.scriptFailure(arguments, failure);

		assertEquals(
			"arguments: {uuid=5324fb1f, count=3.0}: Evaluation of 'innerExpr' failed, arguments: (innerArg): Root cause.",
			message);
	}

	public void testScriptFailureWithoutEvaluationFailure() {
		Throwable failure = new RuntimeException("Failure.", new IllegalStateException("Cause."));

		String message = FailureMessages.scriptFailure(Collections.emptyMap(), failure);

		assertEquals("arguments: {}: Failure: Cause.", message);
	}

	public void testScriptFailureReportsSizeOfBinaryArguments() {
		Map<String, Object> arguments = new LinkedHashMap<>();
		arguments.put("data", new byte[] { 1, 2, 3 });

		String message = FailureMessages.scriptFailure(arguments, new RuntimeException("Failure."));

		assertEquals("arguments: {data=byte[3]}: Failure.", message);
	}

	public void testMessagesSkipsDuplicates() {
		Throwable failure = new RuntimeException("Same.", new IllegalStateException("Same."));

		assertEquals("Same.", FailureMessages.messages(failure));
	}

	private static TopLogicException evaluationFailure(Object expr, Object args, Object variables,
			Throwable cause) {
		return new TopLogicException(I18NConstants.ERROR_EVALUATION_FAILED__EXPR_ARGS_DEFS.fill(expr, args, variables),
			cause);
	}

}
