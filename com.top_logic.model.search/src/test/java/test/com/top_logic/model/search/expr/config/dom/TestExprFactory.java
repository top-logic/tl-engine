/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.expr.config.dom;

import java.io.StringReader;

import junit.framework.TestCase;

import com.top_logic.model.search.expr.config.dom.Expr.StringLiteral;
import com.top_logic.model.search.expr.config.dom.Expr.TextContent;
import com.top_logic.model.search.expr.config.dom.ExprFactory;
import com.top_logic.model.search.expr.parser.SearchExpressionParser;

/**
 * Test case for {@link ExprFactory}
 */
public class TestExprFactory extends TestCase {

	private static final String BS = "\\";

	private ExprFactory _f;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		SearchExpressionParser parser = new SearchExpressionParser(new StringReader(""));
		_f = new ExprFactory(parser);
	}

	/**
	 * Test unicode escape sequences in strings.
	 * 
	 * <p>
	 * Note: According to the Java specs, the "u" escape can be repeated.
	 * </p>
	 */
	public void testUnicodeEscape() {
		StringLiteral literal = _f.stringLiteral("'hello\\u007Eworld 20\\uuu20ac'");
		assertEquals("hello~world 20\u20AC", literal.getValue());
	}

	/**
	 * Test case for removing '\' quoting characters from HTML text content.
	 */
	public void testHtmlQuote() {
		TextContent content = _f.textContent(BS + "{ quoted " + BS + BS + " backslash " + BS + BS + BS + "}" + BS);
		assertEquals("{ quoted " + BS + " backslash " + BS + "}" + BS, content.getValue());
	}

	/**
	 * Text block with trailing whitespace.
	 */
	public void testTextBlockTrailingWS() {
		assertTextLiteral(
			"A\n" +
			"	B\n" +
			"    C\n" +
			"D", 
			"	A     	\n" + 
			"		B	\n" + 
			"    	C  	\n" + 
			"    D	 	\n");
	}

	/**
	 * Text block that requires tab to space conversion while unindenting.
	 */
	public void testTextBlockIndented() {
		assertTextLiteral(
			"  A\n" +
			"	  B\n" +
			"      C\n" +
			"  D", 
			"	A\n" + 
			"		B\n" + 
			"    	C\n" + 
			"    D\n" + 
			"  ");
	}

	/**
	 * Text block with joined lines.
	 */
	public void testTextBlockLineJoining() {
		assertTextLiteral(
			"A\n" +
			"	B C\n" +
			"D", 
			"	A\n" + 
			"		B \\\n" + 
			"    	C\n" + 
			"    D\n");
	}
	
	/**
	 * Text block with CRLF.
	 */
	public void testTextBlockCRLF() {
		assertTextLiteral(
			"A\n" +
			"	B\n" +
			"C", 
			"	A\n" +
			"		B \r\n" + 
			"    C\r\n");
	}
	
	/**
	 * Text block with escape chars.
	 */
	public void testTextBlockEscape() {
		assertTextLiteral(
			"\t\b\n\r\f",
			"\\t\\b\\n\\r\\f");
	}
	
	/**
	 * Text block that starts with a blank line
	 */
	public void testTextBlockBlankStart() {
		assertTextLiteral(
			"A\n" +
			"	B\n" +
			"C", 
			"	        \n" + 
			"	A     	\n" + 
			"		B	\n" + 
			"    C	 	\n" + 
			"    ");
	}

	/**
	 * Text block with single line.
	 */
	public void testTextBlockSingleLine() {
		assertTextLiteral(
			"\t  \t",
			"\t  \t");
	}

	private void assertTextLiteral(String expected, String content) {
		StringLiteral literal = textLiteral(content);
		assertEquals(expected, literal.getValue());
	}

	private StringLiteral textLiteral(String string) {
		return _f.textLiteral("\"\"\"" + string + "\"\"\"");
	}
}
