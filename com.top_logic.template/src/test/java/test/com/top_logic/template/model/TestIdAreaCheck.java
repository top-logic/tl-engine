/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.model;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.textui.TestRunner;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.template.I18NConstants;
import com.top_logic.template.model.CheckResult;
import com.top_logic.template.model.TemplateArea;
import com.top_logic.template.model.TemplateException;
import com.top_logic.template.model.TemplateProblem;
import com.top_logic.template.model.TemplateProblem.Type;

/**
 * Test cases for areas defined by ids in a given template
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestIdAreaCheck extends BasicTestCase {

	public TestIdAreaCheck(String string) {
		super(string);
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestIdAreaCheck.class);
//		return TLTestSetup.createTLTestSetup(new TestAssignmentCheck("testCheckFunctionUnknown"));
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}

	/**
	 * Test with 3 correct simple assignment statements using indentation and line breaks and ids. No
	 * exception should be thrown.
	 */
	public void testIdArea1() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "Nutzer:\n" +
				         "  <%= $Person:TBE.nName id=\"1\"%>, <%= $Person:TBE.vName id=\"2\"%>\n" + 
				         "<%foreach (p in $Group:Empl) id=\"3\" %>\n" +
		                 "<%= p.nName %>\n" +
		                 "<%/ foreach %>\n";
		
		CheckResult theCheckResult = ModelUtils.doCheck(toParse);
		
		Map<String, TemplateArea> theTemplateAreas = theCheckResult.getTemplateAreas();
		
		assertTrue(theTemplateAreas.containsKey("1"));
		assertTrue(theTemplateAreas.containsKey("2"));
		assertTrue(theTemplateAreas.containsKey("3"));
		
		TemplateArea templateArea1 = theTemplateAreas.get("1");
		TemplateArea templateArea2 = theTemplateAreas.get("2");
		TemplateArea templateArea3 = theTemplateAreas.get("3");
		
		int ecb = templateArea1.getColumnBegin();
		int ece = templateArea1.getColumnEnd();
		int erb = templateArea1.getRowBegin();
		int ere = templateArea1.getRowEnd();
		
		assertEquals(3, ecb);
		assertEquals(32, ece);
		assertEquals(2, erb);
		assertEquals(2, ere);
		
		ecb = templateArea2.getColumnBegin();  
		ece = templateArea2.getColumnEnd();    
		erb = templateArea2.getRowBegin();     
        ere = templateArea2.getRowEnd();
        
        assertEquals(35, ecb);
		assertEquals(64, ece);
		assertEquals(2, erb);
		assertEquals(2, ere);

        ecb = templateArea3.getColumnBegin();  
        ece = templateArea3.getColumnEnd();    
        erb = templateArea3.getRowBegin();     
        ere = templateArea3.getRowEnd();
        
        assertEquals(1, ecb);
		assertEquals(14, ece);
		assertEquals(3, erb);
		assertEquals(5, ere);
	}

	/**
	 * Test with 2 correct simple assignment statements using '<', '>' and ids. No
	 * exception should be thrown.
	 */
	public void testIdArea_lt() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "Nutzer: <<%= $Person:TBE.nName id=\"1\"%>>, <<%= $Person:TBE.vName id=\"2\"%>>"; 
		
		String theExpected = "Nutzer: <Bentz>, <Till>";
		
		CheckResult theCheckResult = ModelUtils.doCheck(toParse);
		String      theResult      = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);

		Map<String, TemplateArea> theTemplateAreas = theCheckResult.getTemplateAreas();
		
		assertTrue(theTemplateAreas.containsKey("1"));
		assertTrue(theTemplateAreas.containsKey("2"));
		
		TemplateArea templateArea1 = theTemplateAreas.get("1");
		TemplateArea templateArea2 = theTemplateAreas.get("2");
		
		int ecb = templateArea1.getColumnBegin();
		int ece = templateArea1.getColumnEnd();
		int erb = templateArea1.getRowBegin();
		int ere = templateArea1.getRowEnd();
		
		assertEquals(9, ecb);
		assertEquals(39, ece);
		assertEquals(1, erb);
		assertEquals(1, ere);
		
		ecb = templateArea2.getColumnBegin();  
		ece = templateArea2.getColumnEnd();    
		erb = templateArea2.getRowBegin();     
		ere = templateArea2.getRowEnd();
		
		assertEquals(43, ecb);
		assertEquals(73, ece);
		assertEquals(1, erb);
		assertEquals(1, ere);
	}
	
	/** 
	 * The attribute "ide" is wrong. So an exception with row 2, col 32 is expected.
	 */
	public void testForeachWrongAttributes() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%foreach (a in $Group:Empl) id=\"1\" start=\"start\" end=\"end\" separator=\", \" %>\n" +
			                 "<%= a %>\n" +
			                 " <% foreach (c in a.accounts) ide=\"1\" %> \n" +
			// error                                        ^
			                 "   <%= c %>\n" +
			                 " <%/foreach %>\n" +
			                 "<%/ foreach %>\n";
			ModelUtils.doCheck(toParse);
			fail("Error expected.");
		} catch (TemplateException tex) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.INVALID_ATTRIBUTES__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());
			assertEquals("ide", theError.getName());
			assertEquals(2, theError.getColumnBegin());
			assertEquals(14, theError.getColumnEnd());
			assertEquals(3, theError.getRowBegin());
			assertEquals(5, theError.getRowEnd());
		}
	}
	
	/**
	 * Special foreach attributes may not occur on arbitrary statements.
	 */
	public void testForeachAttributesOnWrongStatements() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%def (x as \"A\") start=\"start\" end=\"end\" separator=\", \" %>";
			ModelUtils.doCheck(toParse);
			fail("Error expected.");
		} catch (TemplateException tex) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(1, errors.size());
			TemplateProblem theError = CollectionUtil.getSingleValueFromCollection(errors);
			
			assertEquals(I18NConstants.INVALID_ATTRIBUTES__NAME_ROW_COL, theError.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError.getType());
			assertEquals("end, separator, start", theError.getName());
			assertEquals(1, theError.getColumnBegin());
			assertEquals(58, theError.getColumnEnd());
			assertEquals(1, theError.getRowBegin());
			assertEquals(1, theError.getRowEnd());
		}
	}
}