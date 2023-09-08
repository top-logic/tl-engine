/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.textui.TestRunner;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.model.TemplateException;
import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.model.TemplateProblem.Type;
import com.top_logic.template.tree.ForeachStatement;
import com.top_logic.template.tree.FunctionCall;

/**
 * Test cases for model checks of various define statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestDefineCheck extends TestCase {

	public TestDefineCheck(String string) {
		super(string);
	}
	
	public static Test suite() {
		// The setup is needed for some of the error messages that are being thrown when something
		// is broken. If everything is green, the setup would not be needed.
		return ModuleTestSetup.setupModule(TestDefineCheck.class);
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/**
	 * Regression test for Ticket #7353.
	 */
	public void testUnaryExprSymbol() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse =
			"<%def (x as !#equals(\"\", \"\")) %><%= x %>";
		
		String theExpected = "false";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Regression test for Ticket #7353.
	 */
	public void testBinaryExprSymbol() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse =
			"<%def (x as \"A\" < \"B\") %><%= x %>";
		
		String theExpected = "true";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	/** 
	 * Test with a correct simple define statements. No exception should be thrown.
	 */
	public void testDefineStatement_simpleVariable() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%def (till as \"Till\") id=\"test\" %>\n" + 
		                 "<%= till %>";

		String theExpected = "\nTill";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with a correct simple define statements using a model reference. No exception should be
	 * thrown.
	 */
	public void testDefineStatement_ModelReference() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%def (till as $Person:TBE) id=\"test\" %>\n" + 
		                 "<%= till.vName %>";
		
		String theExpected = "\nTill";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	/**
	 * Test with two simple define statements using the same variable name. This should result in a
	 * {@link TemplateException}.
	 */
	public void testDefineStatement_DuplicateModelReference() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
        	String toParse = "<%def (person as $Person:TBE) %>\n" + 
        	                 "<%def (person as $Person:FSC) %>";
        	
        	ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch (TemplateException tex) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.REFERENCE_ALREADY_IN_SCOPE__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(1, ecb);
			assertEquals(32, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * Test with two simple define statements using the same variable name. This should result in a
	 * {@link TemplateException} with a {@link Type#MODEL_ERROR}.
	 */
	public void testDefineStatement_FunctionCallWrongType() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
        	String toParse = "<%def (person as $Person:TBE) %>\n" + 
        	                 "<%= #isEmpty (person) %>";
        	
        	ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch (TemplateException tex) {
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
			
			assertEquals(5, ecb);
			assertEquals(21, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * Test with a correct simple define statements assigning the result of a {@link FunctionCall}
	 * to a variable. No exception should be thrown.
	 */
	public void testDefineStatement_FunctionCall() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%def (till as #equals($Person:TBE.vName, $Person:FSC.vName)) id=\"test\" %>\n" + 
		                 "<%= till %>";
		
		String theExpected = "\nfalse";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	/**
	 * Test if a variable storing a structured value can be read at least once.
	 */
	public void testDefineStatementReadOnce() throws Throwable {
		String toParse = "<%def (tillVar as $Person:TBE) %>\n" + "<%= tillVar.vName %>";

		String theExpected = "\nTill";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/**
	 * Test if a variable storing a structured value can be read multiple times.
	 */
	public void testDefineStatementMultiRead() throws Throwable {
		String toParse = "<%def (tillVar as $Person:TBE) %>\n"
			+ "<%= tillVar.vName %>" + "<%= tillVar.vName %>" + "<%= tillVar.vName %>";

		String theExpected = "\nTillTillTill";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/**
	 * Test if a variable storing a structured value can be read multiple times.
	 */
	public void testDefineStatementMultiAttributeRead() throws Throwable {
		String toParse = "<%def (tillVar as $Person:TBE) %>\n" + "<%= tillVar.vName %> <%= tillVar.nName %>";

		String theExpected = "\nTill Bentz";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/**
	 * Test with a correct simple define statements. The variable is then used outside its scope.
	 * This should result in a {@link TemplateException}.
	 */
	public void testDefineStatement_OutsideScope() throws NoSuchAttributeException, UnknownTypeException {
		try {
    		String toParse = "<%foreach (p in $Group:Empl) %>" +
    		                 "  <%def (name as p.vName) %>" +
    				         "  <%= name %>" +
    				         "<%/ foreach %>\n" +
    		                 "<%= name %>";
    		ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertTrue("Expected an error.", !errors.isEmpty());
			TemplateProblem theError = errors.get(0);
			
			assertEquals(I18NConstants.REFERENCE_NOT_IN_SCOPE__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());
			int ecb = theError.getColumnBegin();
			int ece = theError.getColumnEnd();
			int erb = theError.getRowBegin();
			int ere = theError.getRowEnd();
			
			assertEquals(5, ecb);
			assertEquals(8, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);
		}
	}
	
	/**
	 * Test with two correct simple define statements. No exception should be thrown.
	 */
	public void testDefineStatement_MultipleScope() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%def (employers as $Group:Empl) id=\"test\" %>\n" +
		                 "<%foreach (p in employers) %>" +
		                 "  <%def (name as p.vName) %>" +
				         "  <%= name %>" +
				         "<%/ foreach %>\n";
		ModelUtils.doExpand(toParse);
	}
	

	/**
	 * Test with a correct simple define statements using a model reference. The variable is then 
	 * used in a correct {@link ForeachStatement}. No exception should be thrown.
	 */
	public void testDefineStatement_useVariableInforeach() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%def (employers as $Group:Empl) id=\"test\" %>\n" +
		                 "<Group><%" +
		                 "foreach (per in employers) id=\"feG\"%>\n" +
                         "  <Person Value=\"199\" Enabled=\"true\">\n" +
                         "    <Name><%= per.vName %></Name>\n" +
                         "    <Familyname><%= per.nName %></Familyname>\n" +
                         "  </Person>" +
                         "<%/ foreach %>\n" +
                         "</Group>";
		
		String theExpected = "\n<Group>\n" +
                             "  <Person Value=\"199\" Enabled=\"true\">\n" +
                             "    <Name>Till</Name>\n" +
                             "    <Familyname>Bentz</Familyname>\n" +
                             "  </Person>\n" +
                             "  <Person Value=\"199\" Enabled=\"true\">\n" +
                             "    <Name>Friedemann</Name>\n" +
                             "    <Familyname>Schneider</Familyname>\n" +
                             "  </Person>\n" +
                             "  <Person Value=\"199\" Enabled=\"true\">\n" +
                             "    <Name>Theo</Name>\n" +
                             "    <Familyname>Sattler</Familyname>\n" +
                             "  </Person>\n" +
                             "  <Person Value=\"199\" Enabled=\"true\">\n" +
                             "    <Name>Michael</Name>\n" +
                             "    <Familyname>Gänsler</Familyname>\n" +
                             "  </Person>\n" +
                             "  <Person Value=\"199\" Enabled=\"true\">\n" +
                             "    <Name>Frank</Name>\n" +
                             "    <Familyname>Mausz</Familyname>\n" +
                             "  </Person>\n" +
                             "</Group>";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	public void testDefineStatement_Scopes() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<% if (true) %>" +
			             "<% def (result as \"true\") %>" +
			             "<%= result %>" +
			             "<% else /%>" +
			             "<% def (result as \"true\") %>" +
			             "<%= result %>" +
			             "<%/ if %>\n";
		
		String theExpected = "true\n";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
}
