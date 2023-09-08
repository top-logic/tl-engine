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
import com.top_logic.template.model.TemplateProblem.Type;
import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;
import com.top_logic.template.parser.TokenMgrError;

/**
 * Test cases for syntax check of various assign statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestAssignParser extends BasicTestCase {
	
	public TestAssignParser(String aTest) {
		super(aTest);
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestAssignParser.class);
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * Test with correct assign statement. No exception should be thrown.
	 */
	public void testAssign() throws ParseException {
		String         toParse = "<%= $a %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	/** 
	 * Test with correct assign statement. No exception should be thrown.
	 */
	public void testAssign_digitLetter() throws ParseException {
		String         toParse = "<%= $1_a %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	/** 
	* Test with correct assign statement. No exception should be thrown.
	*/
	public void testAssign_letterDigit() throws ParseException {
		String         toParse = "<%= $a_1 %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	/** 
	 * Test with correct assign statement using a function call. No exception should be thrown.
	 */
	public void testAssignFunctionCall() throws ParseException {
		String         toParse = "<%= #myFunc($a) %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	/** 
	 * Test with correct assign statement using a function call. No exception should be thrown.
	 */
	public void testAssignOperators() throws ParseException {
		String         toParse = "<%= ($a >= $b) %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * Lexical Error: last Token is invalid, TokenMgrError (row 1, col 10) should be thrown.
	 */
	public void test_E_Assign() throws ParseException {
		try {
			String         toParse = "<%=  $a %e>";
			// error                           ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (TokenMgrError err) {
			TemplateProblem theProblem = new TemplateProblem(err);
			int rowBegin = theProblem.getRowBegin();
			int colBegin = theProblem.getColumnBegin();
			assertEquals(1, rowBegin);
			assertEquals(10, colBegin);
			return;
		}
	}
	
 	/** 
 	 * Test with an incorrect use of an opening tag. A {@link ParseException} with row 1, col 6 should be thrown.
 	 */
	public void testTemplateTags() {
		try {
			String         toParse = "<<<% <%= ($a >= $b) %>";
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(1, rowBegin);
			assertEquals(6, colBegin);
			return;
		}
	}

	/**
	 * Test with an end tag outside the syntax mode. Should trigger a {@link TokenMgrError} with row
	 * 1, col 26.
	 */
	public void testTemplateTags_LonelyEnd() throws ParseException {
		try {
			String         toParse = "abcd <%= ($a >= $b) %> %%> abcd";
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (TokenMgrError err) {
			TemplateProblem theError = new TemplateProblem(err);
			int rowBegin = theError.getRowBegin();
			int colBegin = theError.getColumnBegin();
			assertEquals(Type.TOK_MGR_ERROR, theError.getType());
			assertEquals(1, rowBegin);
			assertEquals(26, colBegin);
			return;
		}
	}

	public void testTemplateTags_correct() throws ParseException {
		String         toParse = "<<<<%= ($a >= $b) %>>>>>>>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
}
