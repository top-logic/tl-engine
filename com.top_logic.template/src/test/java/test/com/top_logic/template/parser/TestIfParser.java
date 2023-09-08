/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.parser;

import java.io.StringReader;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;
import com.top_logic.template.parser.TokenMgrError;

/**
 * Test cases for syntax check of various if statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestIfParser extends BasicTestCase {
	
	public TestIfParser(String aTest) {
		super(aTest);
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestIfParser.class);
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * Test with correct if-then-else statement. No exception should be thrown.
	 */
	public void testIfThenElse() throws ParseException {
		String         toParse = "<%if ($a == true) %>thenblock<%else/%>elseblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	/** 
	 * Test with correct if-(if-then-else)-else statement. No exception should be thrown.
	 */
	public void testIf_IfThenElse_Else() throws ParseException {
		String         toParse = "<%if ($a == true) %>\n" +
				                 "<%if ($b == false) %>thenblock<%else/%>elseblock<%/if%>\n" +
				                 "<%else/%>\n" +
				                 "elseblock" +
				                 "<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * Test with correct if-then-(if-then-else) statement. No exception should be thrown.
	 */
	public void testIfThen_IfThenElse() throws ParseException {
		String toParse = "<%if ($a == true) %>\n" +
		                 "thenblock" +
		                 "<%else/%>\n" +
		                 "<%if ($b == false) %>thenblock<%else/%>elseblock<%/if%>\n" +
		                 "<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * The inner if statement is not closed correctly.
	 */
	public void testIfThen_IfThenElse_E_() {
		try {
			String toParse = "<%if ($a == true) %>\n" +
			                 "<%if ($b == false) %>thenblock<%else/%><%else/%><%/if%>";
			// error                                                   ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(2, rowBegin);
			assertEquals(42, colBegin);
			return;
		}
	}
	
	/** 
	 * The second if keyword "ief" is wrong. So an exception with row 3, col 3 is expected.
	 */
	public void testIfThen__E_IfThenElse() {
		try {
			String toParse = "<%if ($a == true) %>\n" +
			                 "thenblock" +
			                 "<%else/%>\n" +
			                 "<%ief ($b == false) %>thenblock<%else/%>elseblock<%/if%>\n" +
			// error            ^
			                 "<%/if%>";
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(3, rowBegin);
			assertEquals(3, colBegin);
			return;
		}
	}

	/** 
	 * The second else keyword "eelse" is wrong. So an exception with row 3, col 33 is expected.
	 */
	public void testIfThen_IfThen_E_Else() {
		try {
			String toParse = "<%if ($a == true) %>\n" +
			                 "thenblock" +
			                 "<%else/%>\n" +
			                 "<%if ($b == false) %>thenblock<%eelse/%>elseblock<%/if%>\n" +
			// error                                          ^
			                 "<%/if%>";
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(3, rowBegin);
			assertEquals(33, colBegin);
			return;
		}
	}

	/** 
	 * The first else keyword "eelse" is wrong. So an exception with row 2, col 12 is expected.
	 */
	public void testIfThen_E__IfThen_Else() {
		try {
			String toParse = "<%if ($a == true) %>\n" +
			                 "thenblock" +
			                 "<%eelse/%>\n" +
			// error            ^
			                 "<%if ($b == false) %>thenblock<%else/%>elseblock<%/if%>\n" +
			                 "<%/if%>";
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(2, rowBegin);
			assertEquals(12, colBegin);
			return;
		}
	}

	/** 
	 * Test with correct if-then statement using the different operators. No exception should be thrown. 
	 */
	public void testIfThen_eq() throws ParseException {
		String         toParse = "<%if (a == true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	public void testIfThen_neq() throws ParseException {
		String         toParse = "<%if (a != true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	public void testIfThen_gt() throws ParseException {
		String         toParse = "<%if (a > true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	public void testIfThen_gte() throws ParseException {
		String         toParse = "<%if (a >= true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	public void testIfThen_lt() throws ParseException {
		String         toParse = "<%if (a < true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	public void testIfThen_lte() throws ParseException {
		String         toParse = "<%if (a <= true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	public void testIfThen_not() throws ParseException {
		String         toParse = "<%if (!true) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	public void testIfThen_and() throws ParseException {
		String         toParse = "<%if (a && b) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	public void testIfThen_or() throws ParseException {
		String         toParse = "<%if (a || b) %>thenblock<%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	/** 
	 * Test with correct if-then statement. No exception should be thrown. 
	 */
	public void testIf_IfThen() throws ParseException {
		String         toParse = "<%if (a == true) %><%if (b == true) %>thenblock<%/if%><%/if%>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * The second if keyword "ief" is wrong. So an exception with row 1, col 22 is expected. 
	 */
	public void testIf__E_IfThen() {
		try {
			String         toParse = "<%if (a == true) %><%ief (b == true) %>thenblock<%/if%><%/if%>";
			// error                                       ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(1, rowBegin);
			assertEquals(22, colBegin);
			return;
		}
	}

	/** 
	 * The keyword "ief" is wrong. So an exception with row 1, col 3 is expected. 
	 */
	public void test_E_IfThen() {
		try {
			String         toParse = "<%ief (a == true) %>thenblock<%/if%>";
			// error                    ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(1, rowBegin);
			assertEquals(3, colBegin);
			return;
		}
	}

	/** 
	 * The keyword "eelse" is wrong. So an exception with row 1, col 31 is expected. 
	 */
	public void testIfThen_E_Else() {
		try {
			String         toParse = "<%if (a == true) %>thenblock<%eelse /%>elseblock<%/if%>";
			// error                                                ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(1, rowBegin);
			assertEquals(31, colBegin);
			return;
		}
	}

	/** 
	 * The operator is is unknown. So an exception with row 1, col 9 is expected. 
	 */
	public void testIfThenElse_UnknownOperator() throws ParseException {
		try {
			String         toParse = "<%if (a ?= true) %>thenblock<%else /%>elseblock<%/if%>";
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (TokenMgrError err) {
			TemplateProblem theProblem = new TemplateProblem(err);
			int rowBegin = theProblem.getRowBegin();
			int colBegin = theProblem.getColumnBegin();
			
			assertEquals(err.getErrorCode(), 0 /*TokenMgrError.LEXICAL_ERROR*/);
			assertEquals(1, rowBegin);
			assertEquals(9, colBegin);
			return;
		}
	}
}
