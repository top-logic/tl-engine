/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Arrays;

import junit.framework.Test;

import com.top_logic.basic.util.ResKey;

/**
 * Tests for the interaction of the {@code throw()} value argument with the {@code catch} function of
 * {@code try()}.
 *
 * @see com.top_logic.model.search.expr.Throw
 * @see com.top_logic.model.search.expr.Try
 */
@SuppressWarnings("javadoc")
public class TestTryCatch extends AbstractSearchExpressionTest {

	public void testCatchReceivesThrowValue() throws Exception {
		Object result = eval("try(throw('Error', 'Detail', 'payload'), catch: msg -> value -> $value)");

		assertEquals("payload", result);
	}

	public void testCatchValueNullWhenThrowHasNoValue() throws Exception {
		Object result = eval("try(throw('Error'), catch: msg -> value -> $value)");

		assertNull(result);
	}

	public void testCatchValueNullForNonThrowException() throws Exception {
		// A list with more than one element makes singleElement() fail at runtime with an exception
		// that does not originate from throw(). Passing the list as an argument avoids constant
		// folding at compile time.
		Object result = eval("l -> try($l.singleElement(), catch: msg -> value -> $value)",
			Arrays.asList("a", "b"));

		assertNull(result);
	}

	public void testSingleArgumentCatchStillReceivesMessage() throws Exception {
		Object result = eval("try(throw('Boom'), catch: msg -> $msg != null)");

		assertEquals(Boolean.TRUE, result);
	}

	public void testCatchReceivesResKeyOfThrow() throws Exception {
		Object result = eval("try(throw(#\"test.try.catch.message\"), catch: msg -> $msg)");

		assertEquals(ResKey.forTest("test.try.catch.message"), result);
	}

	public void testCatchReceivesLiteralResKeyOfThrownText() throws Exception {
		Object result = eval("try(throw('Boom'), catch: msg -> $msg)");

		assertEquals(ResKey.text("Boom"), result);
	}

	public void testCatchReceivesResKeyOfFunctionError() throws Exception {
		// singleElement() fails with a TopLogicException for a list with more than one element.
		Object result = eval("l -> try($l.singleElement(), catch: msg -> $msg)", Arrays.asList("a", "b"));

		assertInstanceof(result, ResKey.class);
	}

	public void testCatchReceivesResKeyOfPlainError() throws Exception {
		// An invalid pattern makes regex() fail with a PatternSyntaxException, which carries no
		// message key and is reported with the evaluation failure key. Passing the pattern as an
		// argument avoids constant folding at compile time.
		Object result = eval("p -> try(regex($p), catch: msg -> $msg)", "(");

		assertInstanceof(result, ResKey.class);
	}

	public void testCaughtPlainErrorCanBeLocalized() throws Exception {
		// The message bound to the catch variable must not refer to the evaluation context the
		// variable is bound in.
		Object result = eval("p -> try(regex($p), catch: msg -> $msg.localize())", "(");

		assertInstanceof(result, String.class);
		assertTrue("Message does not name the failed expression: " + result,
			((String) result).contains("regex"));
	}

	public void testCaughtPlainErrorCanBeConvertedToString() throws Exception {
		Object result = eval("p -> try(regex($p), catch: msg -> toString($msg))", "(");

		assertInstanceof(result, String.class);
	}

	public static Test suite() {
		return suite(TestTryCatch.class);
	}

}
