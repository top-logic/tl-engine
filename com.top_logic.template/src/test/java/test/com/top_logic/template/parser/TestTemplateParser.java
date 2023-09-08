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

import com.top_logic.basic.StringServices;
import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;
import com.top_logic.template.parser.Token;
import com.top_logic.template.parser.TokenMgrError;

/**
 * General test cases for the {@link TemplateParser}.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestTemplateParser extends BasicTestCase {
	
	public TestTemplateParser(String aTest) {
		super(aTest);
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestTemplateParser.class);
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * Test correct tab handling with standard tab size of 8.
	 */
	public void testStandardTabSize_PE() {
		int tabSize = 0;
		String         toParse = "<%=\t\t d $ a %>";
		String replaceTabs = "";
		try {
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			
			tabSize     = theParser.getTabSize();
			replaceTabs = replaceTabs(toParse, tabSize);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException e) {
			Token currentToken = e.currentToken.next;
			int br = currentToken.beginLine; 
			int bc = currentToken.beginColumn;
			int er = currentToken.endLine;
			int ec = currentToken.endColumn;
			
			int errorIdx = replaceTabs.indexOf("$") + 1;
			assertEquals(br, 1);
			assertEquals(bc, errorIdx);
			assertEquals(er, 1);
			assertEquals(ec, errorIdx);
		}
	}
	
	/** 
	 * Test correct tab handling with tab sizes between 1 and 9.
	 */
	public void testCustomTabSize_PE() {
		String         toParse = "<%=\t\t d $ a %>";
		
		for ( int i = 1; i < 10; i++) {
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.setTabSize(i);
			try {
				theParser.Start();
				fail("Error expected.");
			}
			catch (ParseException e) {
				Token currentToken = e.currentToken.next;
				int br = currentToken.beginLine; 
				int bc = currentToken.beginColumn;
				int er = currentToken.endLine;
				int ec = currentToken.endColumn;

				String tabsExpanded = replaceTabs(toParse, i);
				int errorIdx = tabsExpanded.indexOf("$") + 1;
				assertEquals(br, 1);
				assertEquals(bc, errorIdx);
				assertEquals(er, 1);
				assertEquals(ec, errorIdx);
			}
			theParser.ReInit(theReader);
		}
	}
	
	/**
	 * replaces \t by spaces, interpreting \t so, that the next column mod(aTabsize) == 0 is used
	 */
	private String replaceTabs(String aString, int aTabsize) {

		String hlp = aString;
		int pos = hlp.indexOf("\t");
		while(pos>-1){
			String start = hlp.substring(0,pos);
			String end = pos<hlp.length()-1 ? hlp.substring(pos+1): "";
			int numOfSpaces = (aTabsize - (pos % aTabsize));
			String spaces = StringServices.getSpaces(numOfSpaces);
			hlp=start+spaces+end;
			pos = hlp.indexOf("\t");
		}
		return hlp;
	}

	/**
	 * Lexical Error: last Token is invalid, tab size is default (8), TokenMgrError (row 1, col 15)
	 * should be thrown.
	 */
	public void test_defaultTabSize_E_Assign() throws ParseException {
		try {
			String         toParse = "<%=\t  $a %e>";
			// error                             ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (TokenMgrError err) {
			TemplateProblem theProblem = new TemplateProblem(err);
			theProblem.getColumnBegin();
			int rowBegin = theProblem.getRowBegin();
			int colBegin = theProblem.getColumnBegin();
			assertEquals(1, rowBegin);
			assertEquals(15, colBegin);
			return;
		}
	}
	
	/**
	 * Lexical Error: last Token is invalid, tab size between 1 and 9, TokenMgrError (row 1, col 15)
	 * should be thrown.
	 */
	public void test_customTabSize_E_Assign() throws ParseException {
		String         toParse = "<%=\t  $a %e>";
		// error                             ^
		
		String         replaceTabs = "";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);

		for ( int i = 1; i < 10; i++) {
			String replacable = toParse;
			theParser.setTabSize(i);
			replaceTabs = replaceTabs(replacable, i);
			try {
				theParser.Start();
				fail("Error expected.");
			}
			catch (TokenMgrError err) {
				TemplateProblem theProblem = new TemplateProblem(err);
				theProblem.getColumnBegin();
				int rowBegin = theProblem.getRowBegin();
				int colBegin = theProblem.getColumnBegin();
				int errorIdx = replaceTabs.indexOf("e") + 1;
				assertEquals(rowBegin, 1);
				assertEquals(colBegin, errorIdx);
				return;
			}
			theParser.ReInit(theReader);
		}
	}
}
