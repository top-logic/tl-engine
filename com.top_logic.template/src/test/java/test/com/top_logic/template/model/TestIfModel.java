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
import com.top_logic.template.model.ExpansionModel;
import com.top_logic.template.model.TemplateException;
import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.model.TemplateProblem.Type;

/**
 * Test cases for model checks of various if statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestIfModel extends BasicTestCase {
	
	public TestIfModel(String aTest) {
		super(aTest);
	}

	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestIfModel.class);
//		return TLTestSetup.createTLTestSetup(new TestIfModel("testCheckIf_BooleanTest"));
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/**
	 * Test with correct if statement. The then case is <code>true</code> for the used
	 * {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_Then() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Staff>\n" +
		                 "<% if ($Person:TBE.isManager == false ) \n" +
		                 "%>  <Executive>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Executive>" +
		                 "<% else /%>" +
		                 "  <Employee>\n" +
		                 "    <Familyname><%= $Person:FSC.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:FSC.vName %></Name>\n" +
		                 "  </Employee>" +
		                 "<%/ if %>\n" +
		                 "</Staff>";
		
		String theExpected = "<Staff>\n" +
                             "  <Executive>\n" +
                             "    <Familyname>Bentz</Familyname>\n" +
                             "    <Name>Till</Name>\n" +
                             "  </Executive>\n" +
                             "</Staff>";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statement. The else case is <code>true</code> for the used
	 * {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_Else() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Staff>\n" +
		                 "<% if ($Person:TBE.isManager == true ) \n" +
		                 "%>  <Executive>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Executive>" +
		                 "<% else /%>" +
		                 "  <Employee>\n" +
		                 "    <Familyname><%= $Person:FSC.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:FSC.vName %></Name>\n" +
		                 "  </Employee>" +
		                 "<%/ if %>\n" +
		                 "</Staff>";
		
		String theExpected = "<Staff>\n" +
        		             "  <Employee>\n" +
        		             "    <Familyname>Schneider</Familyname>\n" +
        		             "    <Name>Friedemann</Name>\n" +
        		             "  </Employee>\n" +
        		             "</Staff>";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statement. The else case is <code>false</code> for the used
	 * {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_FunctionCall_Then_Else() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Staff>\n" +
		                 "<% if (#equals($Person:TBE.isManager, false)) \n" +
		                 "%>  <Executive>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Executive>" +
		                 "<% else /%>" +
		                 "  <Employee>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Employee>" +
		                 "<%/ if %>" +
		                 "</Staff>";
		
		String theExpected = "<Staff>\n" +
                             "  <Executive>\n" +
                             "    <Familyname>Bentz</Familyname>\n" +
                             "    <Name>Till</Name>\n" +
                             "  </Executive></Staff>";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statement without a then-statement. The else case is <code>false</code>
	 * for the used {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_NoThen_Else() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Staff>\n" +
		                 "<% if ($Person:TBE.isManager == false ) \n" +
		                 "%><% else /%>" +
		                 "  <Employee>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Employee>" +
		                 "<%/ if %>" +
		                 "</Staff>";
		
		String theExpected = "<Staff>\n" +
                             "</Staff>";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statement without a then-statement. The else case is <code>false</code>
	 * for the used {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_FunctionCall_Exists_True() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Staff>\n" +
		                 "<% if (#exists($Person:TBE)) %>" +
		                 "  <Employee>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Employee>" +
		                 "<%/ if %>" +
		                 "</Staff>";
		
		String theExpected = "<Staff>\n" +
                             "  <Employee>\n" +
                             "    <Familyname>Bentz</Familyname>\n" +
                             "    <Name>Till</Name>\n" +
                             "  </Employee>" +
                             "</Staff>";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statement without a then-statement. The else case is <code>false</code>
	 * for the used {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_FunctionCall_Exists_False() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<Staff>\n" +
		                 "<% if (#exists($Person:TBE2)) %>" +
		                 "  <Employee>\n" +
		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
		                 "  </Employee>" +
		                 "<%/ if %>" +
		                 "</Staff>";
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals("<Staff>\n</Staff>", theResult);
	}
	
	/**
	 * The function call is wrong: the equals function takes only two arguments. So an exception
	 * with 2:8 - 2:63 is expected.
	 */
	public void testCheckIf_FunctionCall_E() throws NoSuchAttributeException, UnknownTypeException {
		try {
    		String toParse = "<Staff>\n" +
    		                 "<% if (#equals($Person:TBE.vName, $Person:TBE.isManager, false)) \n" +
    		                 "%>" +
    		                 "  <Employee>\n" +
    		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
    		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
    		                 "  </Employee>" +
    		                 "<%/ if %>" +
    		                 "</Staff>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.FUNCTION_ARGUMENTS_SIZE__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());

			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(8, ecb);
			assertEquals(63, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * The function call is wrong: the exists function takes only one argument of type collection.
	 * So an exception with 2:8 - 2:37 is expected.
	 */
	public void testCheckIf_FunctionCall_E2() throws NoSuchAttributeException, UnknownTypeException {
		try {
    		String toParse = "<Staff>\n" +
    		                 "<% if (#isEmpty($Person:TBE.isManager)) \n" +
    		                 "%>  <Executive>\n" +
    		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
    		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
    		                 "  </Executive>" +
    		                 "<%/ if %>" +
    		                 "</Staff>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.FUNCTION_MATCHING_TYPE__NAME_ROW_COL_EXPECTED_ENCOUNTERED,
				theError.getResKey().plain());
			assertEquals(Type.MODEL_ERROR, theError.getType());
			
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(8, ecb);
			assertEquals(38, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * The function call is wrong: the function does not exist in the used {@link ExpansionModel}.
	 * So an exception with 2:8 - 2:37 is expected.
	 */
	public void testCheckIf_FunctionCall_E3() throws NoSuchAttributeException, UnknownTypeException {
		try {
    		String toParse = "<Staff>\n" +
    		                 "<% if (#unknown($Person:TBE.isManager)) \n" +
    		                 "%>  <Executive>\n" +
    		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
    		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
    		                 "  </Executive>" +
    		                 "<%/ if %>" +
    		                 "</Staff>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.FUNCTION_NOT_EXISTS__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.MODEL_ERROR, theError.getType());
			
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(8, ecb);
			assertEquals(38, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * The function call is wrong: the function does not exist in the used {@link ExpansionModel}.
	 * The difference to {@link #testCheckIf_FunctionCall_E3()} is the negation, which caused a NPE.
	 * So an exception with 2:10 - 2:40 is expected.
	 */
	public void testCheckIf_Negated_FunctionCall_E4() throws NoSuchAttributeException, UnknownTypeException {
		try {
    		String toParse = "<Staff>\n" +
    		                 "<% if (!(#unknown($Person:TBE.isManager))) \n" +
    		                 "%>  <Executive>\n" +
    		                 "    <Familyname><%= $Person:TBE.nName %></Familyname>\n" +
    		                 "    <Name><%= $Person:TBE.vName %></Name>\n" +
    		                 "  </Executive>" +
    		                 "<%/ if %>" +
    		                 "</Staff>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.FUNCTION_NOT_EXISTS__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.MODEL_ERROR, theError.getType());
			
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(10, ecb);
			assertEquals(40, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * Test with correct if statements. The first if test is <code>false</code>, the second one
	 * <code>true</code> for the used {@link ExpansionModel}. No exception should be thrown.
	 */
	public void testCheckIf_FunctionCall_Nested_1() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) && ($Person:TBE.isManager == true)) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#exists($Person:MGA) && ($Person:MGA.isManager == true)) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
				             "true";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statements and two function calls combined with AND. The first if test
	 * is <code>false</code>, the second one <code>true</code> for the used {@link ExpansionModel}.
	 * No exception should be thrown.
	 */
	public void testCheckIf_2_FunctionCall_Nested_AND() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) && (#equals($Person:TBE.isManager,true))) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#exists($Person:MGA) && (#equals($Person:MGA.isManager,true))) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
				             "true";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statements and two function calls combined with OR. For each if at least
	 * one of the test will evaluate to <code>true</code>. for the used {@link ExpansionModel}. No
	 * exception should be thrown.
	 */
	public void testCheckIf_2_FunctionCall_Nested_OR() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) || (#equals($Person:TBE.isManager,true))) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#exists($Person:MGA) || (#equals($Person:MGA.isManager,true))) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "true\n" +
				             "true";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statements and two function calls combined with OR. For each if at least
	 * one of the test will evaluate to <code>true</code>. for the used {@link ExpansionModel}. No
	 * exception should be thrown.
	 */
	public void testCheckIf_2_FunctionCall_Multi_Nested_OR_AND() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) || " +
				         "(#equals($Person:TBE.isManager,true)) && (#equals($Person:TBE.isManager,\"false\"))) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "true";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statements and two function calls combined with OR. For each if at least
	 * one of the test will evaluate to <code>true</code>. for the used {@link ExpansionModel}. No
	 * exception should be thrown.
	 */
	public void testCheckIf_2_FunctionCall_Multi_Nested_AND_AND() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) && " +
				         "(#equals($Person:TBE.isManager,true)) && (#equals($Person:TBE.isManager,false))) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with correct if statements and function calls combined with AND. No exception should be
	 * thrown.
	 */
	public void testCheckIf_2_FunctionCall_Multi_Nested_NOT_AND_AND() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) && (!#equals($Person:TBE.isManager,true)) && (#equals($Person:TBE.hasCar,true))) %>true<% else /%>false<%/ if %>\n" +
				         "<% if (#exists($Person:TSA) && (!#equals($Person:TSA.isManager,true)) && (#equals($Person:TSA.hasCar,true))) %>true<% else /%>false<%/ if %>\n" +
				         "<% if (#exists($Person:MGA) && (#equals($Person:MGA.isManager,true)) && (!#equals($Person:MGA.hasCar,false))) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
				             "true\n" +
				             "true";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	
	/**
	 * Test with correct if statements and function calls combined with AND. No exception should be
	 * thrown.
	 */
	public void testCheckIf_3_FunctionCall_Multi_Nested_NOT_AND_AND() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#exists($Person:TBE) && ((#equals($Person:TBE.vName ,\"Till\"))       || (#equals($Person:TBE.nName,\"Bentz\")))) %>true<% else /%>false<%/ if %>\n" +
				         "<% if (#exists($Person:TBE) && ((#equals($Person:TBE.vName ,\"Friedemann\")) || (#equals($Person:TBE.nName,\"Schneider\")))) %>true<% else /%>false<%/ if %>\n" +
				         "<% if (#exists($Person:FSC) && ((#equals($Person:TBE.vName ,\"Friedemann\")) || (#equals($Person:TBE.nName,\"Bentz\")))) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "true\n" +
				             "false\n" +
				             "true";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/** 
	 * The keyword for the 'else' case is wrong so an exception with 1:52 - 1:58 is expected.
	 */
	public void testCheckFunctionCallElsed() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
			String toParse = "<%if (#equals ($Person:TBE, $Person:FSC )) %>true<%elsed /%>false<%/if%>";

			ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(Type.PARSE_ERROR, theError.getType());
			
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(52, ecb);
			assertEquals(56, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}
	
	/**
	 * Correct if statement that resolves to false, so that an empty String is expected.
	 */
	public void testCheckFunctionCallIf() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%if (#equals ($Person:TBE, $Person:FSC )) %>true<%/if%>";
		
		String theExpected = "";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/**
	 * Test the difference between <code>true</code> and "true".
	 */
	public void testCheckIf_BooleanTest() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#equals($Person:TBE.isManager ,\"false\")) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#equals($Person:TBE.isManager ,false)) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#equals($Person:MGA.isManager ,true)) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#equals($Person:MGA.isManager ,\"true\")) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
		                     "true\n" +
		                     "true\n" +
		                     "false";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test the difference between <code>true</code> and "true".
	 */
	public void testCheckIf_IsEmpty() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (#isEmpty($Group:Empl)) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (!#isEmpty($Group:Empl)) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (#isEmpty($Group:Empty)) %>true<% else /%>false<%/ if %>\n" +
		                 "<% if (!#isEmpty($Group:Empty)) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
		                     "true\n" +
		                     "true\n" +
		                     "false";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	/**
	 * Test the different operators &&, ||, !=, !, <, <=, >, >=.
	 */
	public void testCheckOperator_and() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = 
		"<% if (true && true) %>true<% else /%>false<%/ if %>\n" +
		"<% if (true && false) %>true<% else /%>false<%/ if %>\n" +
		"<% if (false && false) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "true\n" +
		"false\n" +
		"false";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	public void testCheckOperator_or() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = 
			"<% if (true || true) %>true<% else /%>false<%/ if %>\n" +
			"<% if (true || false) %>true<% else /%>false<%/ if %>\n" +
			"<% if (false || false) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "true\n" +
		"true\n" +
		"false";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	public void testCheckOperator_neq() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = 
			"<% if (true != true) %>true<% else /%>false<%/ if %>\n" +
			"<% if (true != false) %>true<% else /%>false<%/ if %>\n" +
			"<% if (false != false) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
		"true\n" +
		"false";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	public void testCheckOperator_not() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = 
			"<% if (!true) %>true<% else /%>false<%/ if %>\n" +
			"<% if (!false) %>true<% else /%>false<%/ if %>";
		
		String theExpected = "false\n" +
		"true";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
//	public void testCheckOperator_lt() throws NoSuchAttributeException, UnknownTypeException {
//		try {String toParse = 
//			"<% if (2 < 2) %>true<% else /%>false<%/ if %>\n" +
//			"<% if (1 < 2) %>true<% else /%>false<%/ if %>";
//		
//		String theExpected = "false\n" +
//		"true";
//		
//		String theResult = ModelUtils.doExpand(toParse);
//		
//		assertEquals(theExpected, theResult);
//		}
//		catch (TemplateException ex) {
//			ex.getMessage();
//		}
//	}
//	
//	public void testCheckOperator_lte() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
//		String toParse = 
//			"<% if (!true) %>true<% else /%>false<%/ if %>\n" +
//			"<% if (!false) %>true<% else /%>false<%/ if %>";
//		
//		String theExpected = "false\n" +
//		"true";
//		
//		String theResult = ModelUtils.doExpand(toParse);
//		
//		assertEquals(theExpected, theResult);
//	}
//	
//	public void testCheckOperator_gt() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
//		String toParse = 
//			"<% if (!true) %>true<% else /%>false<%/ if %>\n" +
//			"<% if (!false) %>true<% else /%>false<%/ if %>";
//		
//		String theExpected = "false\n" +
//		"true";
//		
//		String theResult = ModelUtils.doExpand(toParse);
//		
//		assertEquals(theExpected, theResult);
//	}
//	
//	public void testCheckOperator_gte() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
//		String toParse = 
//			"<% if (!true) %>true<% else /%>false<%/ if %>\n" +
//			"<% if (!false) %>true<% else /%>false<%/ if %>";
//		
//		String theExpected = "false\n" +
//		"true";
//		
//		String theResult = ModelUtils.doExpand(toParse);
//		
//		assertEquals(theExpected, theResult);
//	}
}
