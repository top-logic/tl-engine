/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.dom;

import java.io.StringReader;

import junit.framework.TestCase;

import com.top_logic.model.search.expr.config.dom.Expr.TextContent;
import com.top_logic.model.search.expr.config.dom.ExprFactory;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;

/**
 * Test case for {@link ExprFactory}
 */
public class TestExprFactory extends TestCase {

	private static final String BS = "\\";

	/**
	 * Test case for removing '\' quoting characters from HTML text content.
	 */
	public void testHtmlQuote() {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(""));

		TextContent content =
			new ExprFactory(parser).textContent(BS + "{ quoted " + BS + BS + " backslash " + BS + BS + BS + "}" + BS);
		assertEquals("{ quoted " + BS + " backslash " + BS + "}" + BS, content.getValue());
	}
}
