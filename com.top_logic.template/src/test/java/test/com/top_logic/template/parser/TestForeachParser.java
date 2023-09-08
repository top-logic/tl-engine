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

import com.top_logic.template.parser.ParseException;
import com.top_logic.template.parser.TemplateParser;

/**
 * Test cases for syntax check of various foreach statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestForeachParser extends BasicTestCase {
	
	public TestForeachParser(String aTest) {
		super(aTest);
	}

	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestForeachParser.class);
//		return TLTestSetup.createTLTestSetup(new TestForeachParser(""));
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * Test with correct foreach statement. No exception should be thrown.
	 */
	public void testForeach() throws ParseException {
		String         toParse = "<%foreach (a in $b) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
				                 "<%= a %>" +
				                 "<%/ foreach %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

	
	/** 
	 * Test with correct foreach statement. No exception should be thrown.
	 */
	public void testForeach_WC() throws ParseException {
		String         toParse = "<%foreach (a in $b:*) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
				                 "<%= a %>" +
				                 "<%/ foreach %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * Test with correct nested foreach statement. No exception should be thrown.
	 */
	public void testNestedForeach() throws ParseException {
		String         toParse = "<%foreach (a in $b) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
		                         "<%= a %>" +
		                         " <% foreach (c in a.d) id=\"2\" %> \n" +
		                         "   <%= c %>" +
		                         " <%/foreach %>" +
		                         "<%/ foreach %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * Closing foreach tag is wrong. So an exception with row 2, col 13 is expected.
	 */
	public void test_E_Foreach() {
		try {
			String         toParse = "<%foreach (a in $b) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
					                 "<%= a %>" +
					                 "<%/ forreach %>";
			// error                      ^
			StringReader   theReader = new StringReader(toParse);
			TemplateParser theParser = new TemplateParser(theReader);
			theParser.Start();
			fail("Error expected.");
		}
		catch (ParseException pe) {
			int rowBegin = pe.currentToken.next.beginLine;
			int colBegin = pe.currentToken.next.beginColumn;
			assertEquals(2, rowBegin);
			assertEquals(13, colBegin);
			return;
		}
	}
	
}
