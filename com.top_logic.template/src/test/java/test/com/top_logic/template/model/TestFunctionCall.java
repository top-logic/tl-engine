/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * Test cases for model checks of various function call statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestFunctionCall extends BasicTestCase {
	
	public TestFunctionCall(String string) {
		super(string);
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestFunctionCall.class);
//		return TLTestSetup.createTLTestSetup(new TestFunctionCall("testNestedFunctionCalls_InnerInnerNotExists"));
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}
	
	/** 
	 * A correct simple nested function call. No exception should be thrown.
	 */
	public void testNestedFunctionCalls() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%= #equals(#isEmpty($Group:Empl),#isEmpty($Group:Empl)) %>"; 

		String theExpected = "true";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/** 
	 * A correct function call. No exception should be thrown.
	 */
	public void testFunctionCalls_EmptyString() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%foreach (p in $Group:Empl) separator=\", \" %>" +
                         "<% if(!#equals(p.vName,\"\")) %><%= p.vName %><%/if%>" +
                         "<%/ foreach %>";
		
		String theExpected = "Till, Friedemann, Theo, Michael, Frank";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}

	/** 
	 * myFunction does not exist, so a {@link TemplateException} should be thrown.
	 */
	public void testNestedFunctionCalls_InnerNotExists() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
			String toParse = "<%= #exists(#myFunction($Group:Empl)) %>"; 

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
			
			assertEquals(13, ecb);
			assertEquals(36, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}

	/** 
	 * Reference used in FunctionCall does not exist, so a {@link TemplateException} should be thrown.
	 */
	public void testFunctionCalls_ReferenceNotExists() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
			String toParse = "<%= #exists($Group2:Empl) %>"; 
			
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
			
			assertEquals(13, ecb);
			assertEquals(-1, ece);
			assertEquals(1, erb);
			assertEquals(-1, ere);
		}
	}

	/** 
	 * myFunction does not exist, so a {@link TemplateException} should be thrown.
	 */
	public void testNestedFunctionCalls_InnerNotExists2() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%= #exists(#myFunction(#isEmpty($Group:Empl))) %>"; 
			
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
			
			assertEquals(13, ecb);
			assertEquals(46, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}

	/** 
	 * myFunction does not exist, so a {@link TemplateException} should be thrown.
	 */
	public void testNestedFunctionCalls_InnerInnerNotExists() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%= #exists(#isEmpty(#myFunction($Group:Empl))) %>"; 
			
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
			
			assertEquals(22, ecb);
			assertEquals(45, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}
}
