/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr;

import java.util.Arrays;

import junit.framework.Test;

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

	public static Test suite() {
		return suite(TestTryCatch.class);
	}

}
