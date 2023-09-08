/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model;

import java.util.List;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.model.TemplateException;
import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.model.TemplateProblem.Type;

/**
 * Test cases for model checks of various foreach statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestForeachModel extends BasicTestCase {
	
	public TestForeachModel(String aTest) {
		super(aTest);
	}

	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestForeachModel.class);
//		return TLTestSetup.createTLTestSetup(new TestForeachModel("testCheckIf"));
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * Test with comments and a normal, valid assignment. No exception should be thrown.
	 */
	public void testComment() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%!-- Dies ist ein" +
				         "Kommentar, der nur in der" +
				         "Schablone steht --%>\n" +
				         "<%= $Person:TBE.vName %>";
		String theExpected = "\nTill";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/** 
	 * Test with correct foreach statement. No exception should be thrown.
	 */
	public void testForeachEscapedAttributes() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		//<%foreach (p in $Group:Empl) id="1" start="start\n" end="\ne"nd" separator=", " %>
		String toParse = "<%foreach (p in $Group:Empl) id=\"1\" start=\"start\n\" end=\"\ne\\\"nd\" separator=\", \" %>" +
		"<%= p.nName %>" +
		"<%/ foreach %>";
		
		String theExpected = "start\n" +
		"Bentz, Schneider, Sattler, Gänsler, Mausz" +
		"\ne\"nd";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	/** 
	 * Test with correct foreach statement. No exception should be thrown.
	 */
	public void testForeach() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%foreach (p in $Group:Empl) id=\"1\" start=\"start\n\" end=\"\nend\" separator=\", \" %>" +
		                 "<<%= p.nName %>>" +
		                 "<%/ foreach %>";
		
		String theExpected = "start\n" +
				             "<Bentz>, <Schneider>, <Sattler>, <Gänsler>, <Mausz>" +
				             "\nend";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/** 
	 * Test for list function. No exception should be thrown.
	 */
	public void testForeach_list_String() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%foreach (p in #stringList(\"test\", \"test1\")) separator=\", \" %>" +
		                 "<%= p %>" +
		                 "<%/ foreach %>";
		
		String theExpected = "test, test1";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	
	/** 
	 * Wrong Token, So an exception with row 2, col 8 is expected.
	 */
	public void testForeach_E_Syntax() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
    		String toParse = "<%foreach (p in $Group:Empl) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
    				         "<%= p %2>" +
    		// error                 ^
    				         "<%/ foreach %>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(Type.TOK_MGR_ERROR, theError.getType());
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(8, ecb);
			assertEquals(-1, ece);
			assertEquals(2, erb);
			assertEquals(-1, ere);
		}
	}
	
	/** 
	 * Test with correct if and foreach statement + function call that returns true. No exception should be thrown.
	 */
	public void testIf_FunctionCall_True_Foreach() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%if (#isEmpty($Group:Empl)) %>" +
				         "Group is empty!" +
				         "<%else/%>" +
				         "<%foreach (p in $Group:Empl) id=\"1\" start=\"start\n\" end=\"\nend\" separator=\", \n\" %>" +
				         "<%= p.vName %>" +
				         "<%/ foreach %>" +
				         "<%/if%>";
		
		String theExpected = "start\n" +
				             "Till, \n" +
				             "Friedemann, \n" +
				             "Theo, \n" +
				             "Michael, \n" +
				             "Frank\n" +
				             "end";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/** 
	 * Test with correct if and foreach statement + functioncall that returns true. No exception should be thrown.
	 */
	public void testIf_Negated_FunctionCall_True_Foreach() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%if (!#isEmpty($Group:Empl)) %>" +
		                 "<%foreach (p in $Group:Empl) id=\"1\" start=\"start\n\" end=\"\nend\" separator=\", \n\" %>" +
				         "<%= p.vName %>" +
				         "<%/ foreach %>" +
				         "<%else/%>" +
				         "Group is empty!" +
				         "<%/if%>";
		
		String theExpected = "start\n" +
                             "Till, \n" +
                             "Friedemann, \n" +
                             "Theo, \n" +
                             "Michael, \n" +
                             "Frank\n" +
                             "end";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	public void testForeachUnknownVariable() throws NoSuchAttributeException, UnknownTypeException {
		try {
    		String toParse = "<Group><% \n" +
    				         "foreach (per in $Group2:Empl) %>\n" +   // Group2 is unknown (2:17)
    		// error                           ^
    		                 "  <Person Value=\"199\" Enabled=\"true\">\n" +
    		                 "    <Name><%= per.vName %></Name>\n" +
    		  	             "    <Familyname><%= per.nName %></Familyname>\n" +
    		                 "  </Person>" +
    		                 "<%/ foreach %>\n" +
    		                 "</Group>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.REFERENCE_INVALID__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.MODEL_ERROR, theError.getType());
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(17, ecb);
			assertEquals(-1, ece);
			assertEquals(2, erb);
			assertEquals(-1, ere);
		}
	}

	/** 
	 * Test with correct nested foreach statement. No exception should be thrown.
	 */
	public void testNestedForeach() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Group><%" +
                         "foreach (per in $Group:Empl) id=\"feG\"%>\n" +
                         "  <Person Value=\"199\" Enabled=\"true\">\n" +
                         "    <Name><%= per.vName %></Name>\n" +
                         "    <Familyname><%= per.nName %></Familyname>\n" +
                         "    <Accounts>\n" +
                         "<%foreach (acc in per.accounts) %>" +
                         "      <acc name=\"<%= acc.name %>\">\n" +
                         "        <login><%= acc.login %></login>\n" +
                         "        <id><%= acc.id %></id>\n" +
                         "        <passwd><%= acc.passwd %></passwd>\n" +
                         "      </acc>\n" +
                         "<%/foreach%>" +
                         "    </Accounts>\n" +
                         "  </Person>" +
                         "<%/ foreach %>\n" +
                         "</Group>";

		String theExpected = "<Group>\n" +
				             "  <Person Value=\"199\" Enabled=\"true\">\n" +
				             "    <Name>Till</Name>\n" +
				             "    <Familyname>Bentz</Familyname>\n" +
				             "    <Accounts>\n" +
				             "      <acc name=\"acc0\">\n" +
				             "        <login>tbe</login>\n" +
				             "        <id>acc_tbe</id>\n" +
				             "        <passwd>0tbe123</passwd>\n" +
				             "      </acc>\n" +
				             "      <acc name=\"acc1\">\n" +
				             "        <login>tbe</login>\n" +
				             "        <id>acc_tbe</id>\n" +
				             "        <passwd>1tbe123</passwd>\n" +
				             "      </acc>\n" +
				             "    </Accounts>\n" +
				             "  </Person>\n" +
				             "  <Person Value=\"199\" Enabled=\"true\">\n" +
				             "    <Name>Friedemann</Name>\n" +
				             "    <Familyname>Schneider</Familyname>\n" +
				             "    <Accounts>\n" +
				             "      <acc name=\"acc0\">\n" +
				             "        <login>fsc</login>\n" +
				             "        <id>acc_fsc</id>\n" +
				             "        <passwd>0fsc123</passwd>\n" +
				             "      </acc>\n" +
				             "      <acc name=\"acc1\">\n" +
				             "        <login>fsc</login>\n" +
				             "        <id>acc_fsc</id>\n" +
				             "        <passwd>1fsc123</passwd>\n" +
				             "      </acc>\n" +
				             "    </Accounts>\n" +
				             "  </Person>\n" +
				             "  <Person Value=\"199\" Enabled=\"true\">\n" +
				             "    <Name>Theo</Name>\n" +
				             "    <Familyname>Sattler</Familyname>\n" +
				             "    <Accounts>\n" +
				             "      <acc name=\"acc0\">\n" +
				             "        <login>tsa</login>\n" +
				             "        <id>acc_tsa</id>\n" +
				             "        <passwd>0tsa123</passwd>\n" +
				             "      </acc>\n" +
				             "      <acc name=\"acc1\">\n" +
				             "        <login>tsa</login>\n" +
				             "        <id>acc_tsa</id>\n" +
				             "        <passwd>1tsa123</passwd>\n" +
				             "      </acc>\n" +
				             "    </Accounts>\n" +
				             "  </Person>\n" +
				             "  <Person Value=\"199\" Enabled=\"true\">\n" +
				             "    <Name>Michael</Name>\n" +
				             "    <Familyname>Gänsler</Familyname>\n" +
				             "    <Accounts>\n" +
				             "      <acc name=\"acc0\">\n" +
				             "        <login>mgä</login>\n" +
				             "        <id>acc_mgä</id>\n" +
				             "        <passwd>0mgä123</passwd>\n" +
				             "      </acc>\n" +
				             "      <acc name=\"acc1\">\n" +
				             "        <login>mgä</login>\n" +
				             "        <id>acc_mgä</id>\n" +
				             "        <passwd>1mgä123</passwd>\n" +
				             "      </acc>\n" +
				             "    </Accounts>\n" +
				             "  </Person>\n" +
				             "  <Person Value=\"199\" Enabled=\"true\">\n" +
				             "    <Name>Frank</Name>\n" +
				             "    <Familyname>Mausz</Familyname>\n" +
				             "    <Accounts>\n" +
				             "      <acc name=\"acc0\">\n" +
				             "        <login>fma</login>\n" +
				             "        <id>acc_fma</id>\n" +
				             "        <passwd>0fma123</passwd>\n" +
				             "      </acc>\n" +
				             "      <acc name=\"acc1\">\n" +
				             "        <login>fma</login>\n" +
				             "        <id>acc_fma</id>\n" +
				             "        <passwd>1fma123</passwd>\n" +
				             "      </acc>\n" +
				             "    </Accounts>\n" +
				             "  </Person>\n" +
				             "</Group>";
		
		String theResult = ModelUtils.doExpand(toParse);
		assertEquals(theExpected, theResult);
	}
	
	/** 
	 * Group2 is not known in the model. This leads to one error and two warnings.
	 */
	public void test_E_Foreach() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<Group><%" +
				             "foreach (per in Group2:Empl) %>\n" +                // ERROR: Group2 is unknown (1:26-1:31)
				             "  <Person Value=\"199\" Enabled=\"true\">\n" + 
				             "    <Name><%= per.vName %></Name>\n" +              // WARNING: per (Empl) is empty (3:15-3:21)
				             "    <Familyname><%= per.nName %></Familyname>\n" +  // WARNING: per (Empl) is empty (4:21-4:29)
				             "  </Person>" +
				             "<%/ foreach %>\n" +
				             "</Group>";
			ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors   = tex.getErrors();
			assertTrue("Expected an error.", !errors.isEmpty());
			TemplateProblem theError = errors.get(0);
			
			assertEquals(I18NConstants.REFERENCE_NOT_IN_SCOPE__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(26, ecb);
			assertEquals(31, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}
	
	/** 
	 * Duplicated "id" in the nested foreach. So an exception with row 2, col 32 is expected.
	 */
	public void testForeachDuplicatedID() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%foreach (per in $Group:Empl) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
			                 "<%= per %>" +
			                 " <%foreach (acc in per.accounts) id=\"1\" %> \n" +
			// error                                                ^
			                 "   <acc name=\"<%= acc.name %>\">" +
			                 " <%/foreach %>" +
			                 "<%/ foreach %>";
			ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.ID_EXISTS__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());
			
			int columnBegin = theError.getColumnBegin();
			int columnEnd   = theError.getColumnEnd();
			int rowBegin    = theError.getRowBegin();
			int rowEnd      = theError.getRowEnd();
			
			assertEquals(2, rowBegin);
			assertEquals(3, rowEnd);
			assertEquals(12, columnBegin);
			assertEquals(45, columnEnd);
		}
	}
	
	public void testCheckIf() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
    		String toParse = "<Staff>\n" +
    		                 "<% foreach (per in $Group:Empl) id=\"myId123\" \n" +
    		                 "%><% if (per.isManger <= true ) \n" +  // wrong operator (3:10), isManger is not an attribute of Person
    		                 "%>  <Executive>\n" +
    		                 "    <Familyname><%= per.nName %></Familyname>\n" +
    		                 "    <Name><%= per.vName %></Name>\n" +
    		                 "  </Executive>" +
    		                 "<% else /%>" +
    		                 "  <Employee>\n" +
    		                 "    <Familyname><%= per.nName %></Familyname>\n" +
    		                 "    <Name><%= per.vName %></Name>\n" +
    		                 "  </Employee>" +
    		                 "<%/ if %>\n" +
    		                 "<%/ foreach %>" +
    		                 "</Staff>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			
			TemplateProblem theError_0 = errors.get(0);
			
			assertEquals(I18NConstants.MISSING_ATTRIBUTE__NAME_ROW_COL, theError_0.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError_0.getType());

			int columnBegin = theError_0.getColumnBegin();
			int columnEnd   = theError_0.getColumnEnd();
			int rowBegin    = theError_0.getRowBegin();
			int rowEnd      = theError_0.getRowEnd();
			
			assertEquals(3, rowBegin);
			assertEquals(3, rowEnd);
			assertEquals(10, columnBegin);
			assertEquals(21, columnEnd);
			
		}
	}
}
