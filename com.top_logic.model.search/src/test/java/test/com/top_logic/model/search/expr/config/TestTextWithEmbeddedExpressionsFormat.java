/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config;

import junit.framework.Test;

import test.com.top_logic.model.search.expr.AbstractSearchExpressionTest;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.TextWithEmbeddedExpressionsFormat;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * Test case for {@link TextWithEmbeddedExpressionsFormat}
 */
@SuppressWarnings("javadoc")
public class TestTextWithEmbeddedExpressionsFormat extends AbstractSearchExpressionTest {

	public void testParse() throws ConfigurationException {
		String textWithEmbeddedExpressions = "Value is {$model + 13}.";

		Expr expr = TextWithEmbeddedExpressionsFormat.INSTANCE.getValue("text", textWithEmbeddedExpressions);
		
		SearchExpression executor = build(expr);
		assertEquals("Value is 55.", eval(executor, 42));
		assertEquals("Value is 20.", eval(executor, 7));

		assertEquals(textWithEmbeddedExpressions, TextWithEmbeddedExpressionsFormat.INSTANCE.getSpecification(expr));
	}

	public static Test suite() {
		return suite(TestTextWithEmbeddedExpressionsFormat.class, SafeHTML.Module.INSTANCE);
	}
}
