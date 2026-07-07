/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.visit;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import junit.framework.TestCase;

import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.visit.ToString;

/**
 * Test case for {@link ToString}.
 */
@SuppressWarnings("javadoc")
public class TestToString extends TestCase {

	/**
	 * An embedded expression in an HTML literal must be wrapped in "{...}", so that it is
	 * distinguishable from literal HTML content (Ticket #29367).
	 */
	public void testEmbeddedExpressionInHtml() {
		assertEquals("{{{{$description}}}}", html(var("description")).toString());
	}

	/**
	 * An embedded string literal must be wrapped in "{...}" as well.
	 */
	public void testEmbeddedLiteralInHtml() {
		assertEquals("{{{{\"txt\"}}}}", html(literal("txt")).toString());
	}

	/**
	 * A literal (start) tag is HTML-native and must not be wrapped, while an embedded
	 * expression must be.
	 */
	public void testTagAndEmbeddedExpression() {
		SearchExpression expr = html(
			tag("b", false),
			var("x"));
		assertEquals("{{{<b>{$x}}}}", expr.toString());
	}

}
