/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.parser;

import java.io.InputStream;
import java.io.StringReader;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.SimpleTestFactory;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.equal.ConfigEquality;
import com.top_logic.basic.html.SafeHTML;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.model.search.expr.config.ExprPrinter;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.parser.ParseException;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;
import com.top_logic.model.search.expr.parser.SearchExpressionParserTokenManager;
import com.top_logic.model.search.expr.parser.Token;

/**
 * Test case for {@link SearchExpressionParser}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestSearchExpressionParser extends TestCase {

	/**
	 * Name of resource that contains the test descriptions.
	 */
	private static final String SPEC_RESOURCE = "SearchExpressionSpec.txt";

	private String[] _inputs;

	public TestSearchExpressionParser(String name, String[] inputs) {
		super(name);
		_inputs = inputs;
	}

	@Override
	protected void runTest() throws Throwable {
		int idx = 0;
		Expr expr = parse(_inputs[idx++]);
		String canonicalRepresentation = ExprPrinter.toString(expr);
		Expr canonicalExpr;
		try {
			canonicalExpr = parse(canonicalRepresentation);
		} catch (ParseException ex) {
			throw (AssertionFailedError) new AssertionFailedError(
				"Parsing canonical representation failed: " + canonicalRepresentation).initCause(ex);
		}
		assertTrue(
			"Canonical representation '" + canonicalRepresentation.toString()
				+ "' does not parse to same result: expected " + TypedConfiguration.toString(expr) + " but got "
				+ TypedConfiguration.toString(canonicalExpr),
			ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(expr, canonicalExpr));

		if (idx < _inputs.length) {
			assertEquals(_inputs[idx++].trim(), canonicalRepresentation);
		}
		if (idx < _inputs.length) {
			ConfigurationItem expected = TypedConfiguration.fromString(_inputs[idx++]);
			assertTrue(expr.toString(), ConfigEquality.INSTANCE_ALL_BUT_DERIVED.equals(expected, expr));
		}
	}

	private Expr parse(String src) throws ParseException {
		SearchExpressionParser parser = parser(src);
		Expr expr = parser.expr();
		Token lastToken = parser.getNextToken();
		if (lastToken.kind != SearchExpressionParserTokenManager.EOF) {
			fail("Trailing garbage in: " + src + ", consumed: " + ExprPrinter.toString(expr));
		}
		return expr;
	}

	private SearchExpressionParser parser(String src) {
		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(src));
		return parser;
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(TestSearchExpressionParser.class.getName());
		try {
			InputStream in = TestSearchExpressionParser.class.getResourceAsStream(SPEC_RESOURCE);
			String spec = StreamUtilities.readAllFromStream(in);
			String[] tests = spec.split("=====\\s*");
			for (String test : tests) {
				test = test.trim();
				if (test.isEmpty()) {
					continue;
				}
				int descrEnd = test.indexOf('\n');
				String name = test.substring(0, descrEnd + 1);
				String[] inputs = test.substring(descrEnd + 1).split("-----\\s*");
				suite.addTest(new TestSearchExpressionParser(name, inputs));
			}
		} catch (Throwable ex) {
			suite.addTest(SimpleTestFactory.newBrokenTest(TestSearchExpressionParser.class.getName(), ex));
		}
		return ModuleLicenceTestSetup.setupModule(ServiceTestSetup.createSetup(suite, SafeHTML.Module.INSTANCE));
	}

}
