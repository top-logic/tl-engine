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
 * Test cases for syntax check of various define statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestDefineParser extends BasicTestCase {
	
	public TestDefineParser(String aTest) {
		super(aTest);
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestDefineParser.class);
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * Test with simple single define statement. No exception should be thrown.
	 */
	public void testDefine() throws ParseException {
		String         toParse = "<%def (name as var) %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/** 
	 * Assign a function call to a variable. No exception should be thrown.
	 */
	public void testDefineUsage() throws ParseException {
		String         toParse = "<%def (name as #FunctionCall(a1, a2)) %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}
	
	/**
	 * Test with a syntactically correct simple define statements. Even though the variable is then
	 * used outside its scope it should not produce a {@link ParseException}.
	 */
	public void testDefineStatement_OutsideScope() throws ParseException {
		String toParse = "<%foreach (p in $Group:Empl) %>" +
		                 "  <%def (name as p.vName) %>" +
				         "  <%= name %>" +
				         "<%/ foreach %>" +
		                 "<%= name %>";
		StringReader   theReader = new StringReader(toParse);
		TemplateParser theParser = new TemplateParser(theReader);
		theParser.Start();
	}

}
