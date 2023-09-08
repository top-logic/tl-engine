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
import com.top_logic.template.parser.TokenMgrError;

/**
 * Test cases for model checks of various assignment statements.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TestAssignmentCheck extends BasicTestCase {

	public TestAssignmentCheck(String string) {
		super(string);
	}
	
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestAssignmentCheck.class);
//		return TLTestSetup.createTLTestSetup(new TestAssignmentCheck("testEscapedAttributes"));
	}

	public static void main(String[] args) {
		TestRunner theRunner = new TestRunner();
		theRunner.doRun(suite());
	}

	
	/** 
	 * Test with 2 correct simple assignment statements. No exception should be thrown.
	 */
	public void testCheckReference() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%= $Person:TBE.vName id=\"test\" %>\n" + 
						 "<%= $Person:FSC.nName id=\"test2\" %>";

		String theExpected = "Till\n" +
				             "Schneider";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/** 
	 * Test with 2 correct simple assignment statements. No exception should be thrown.
	 */
	public void testCheckReference_lt() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%if (#exists($Person:TBE))%><<%= $Person:TBE.vName%>configdef><%/if%>";
//		String toParse = "<%= $Person:TBE.vName%><<%= $Person:TBE.vName%>configdef>";
		
		String theExpected = "<Tillconfigdef>";
		
		String theResult = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
	
	/**
	 * Test with 2 correct simple assignment statements using indentation and line breaks No
	 * exception should be thrown.
	 */
	public void testCheckAssignmentReference() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "Nutzer:\n" +
				         "  <%= $Person:TBE.nName id=\"1\"%>, <%= $Person:TBE.vName id=\"2\"%>\n" + 
						"Nutzer: <%= $Person:FSC.nName %>, <%= $Person:FSC.vName %>";
		
		String theExpected = "Nutzer:\n" +
                             "  Bentz, Till\n" +
                             "Nutzer: Schneider, Friedemann";

		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}

	/**
	 * Test with 2 correct assignment statements using existing functions. No exception should be
	 * thrown.
	 */
	public void testCheckFunctionEquals() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse = "<%= #equals($Person:TBE.vName, $Person:TBE.vName) %>\n" +
				         "<%= #equals($Person:TBE.vName, $Person:FSC.vName) %>";
		
		String theExpected = "true\n" +
				             "false";
		
		String theResult = ModelUtils.doExpand(toParse);

		assertEquals(theExpected, theResult);
	}
	
	/** 
	 * Test with 1 incorrect assignment statement using not existing functions. So an exception
	 * with 1:6 - 1:40 is expected.
	 */
	public void testCheckFunctionUnknown() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%= (#MyFunc ($Person:TBE, $Person:TBE )) %>";
			
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
			
			assertEquals(6, ecb);
			assertEquals(40, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}
	
	/**
	 * Test with 3 correct assignment statements using existing functions. The number of arguments
	 * is wrong, so an exception with 3 errors is expected:
	 * <ul>
	 * <li>1:5 - 1:30</li>
	 * <li>2:8 - 2:71</li>
	 * <li>3:5 - 3:13</li>
	 * </ul>
	 */
	public void testCheckFunctionArguments() throws NoSuchAttributeException, UnknownTypeException {
		try {
			String toParse = "<%= #equals($Person:TBE.vName) %>\n" +
					         " + <%= #equals($Person:TBE.vName, $Person:TBE.vName, $Person:TBE.vName) %>\n" +
					         "<%= #equals() %>";
			
			ModelUtils.doExpand(toParse);
			fail("Error expected.");
		}
		catch(TemplateException tex ) {
			List<TemplateProblem> errors = tex.getErrors();
			assertEquals(3, errors.size());
			TemplateProblem theError1 = errors.get(0);
			TemplateProblem theError2 = errors.get(1);
			TemplateProblem theError3 = errors.get(2);
			
			assertEquals(I18NConstants.FUNCTION_ARGUMENTS_SIZE__NAME_ROW_COL, theError1.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError1.getType());
			
			int ecb = theError1.getColumnBegin();
			int ece = theError1.getColumnEnd();
			int erb = theError1.getRowBegin();
			int ere = theError1.getRowEnd();
			
			assertEquals(5, ecb);
			assertEquals(30, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
			
			assertEquals(I18NConstants.FUNCTION_ARGUMENTS_SIZE__NAME_ROW_COL, theError2.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError2.getType());
			
			ecb = theError2.getColumnBegin();
			ece = theError2.getColumnEnd();
			erb = theError2.getRowBegin();
			ere = theError2.getRowEnd();

			assertEquals(8, ecb);
			assertEquals(71, ece);
			assertEquals(2, erb);
			assertEquals(2, ere);

			assertEquals(I18NConstants.FUNCTION_ARGUMENTS_SIZE__NAME_ROW_COL, theError3.getResKey().plain());
			assertEquals(Type.SYNTAX_ERROR, theError3.getType());
			
			ecb = theError3.getColumnBegin();
			ece = theError3.getColumnEnd();
			erb = theError3.getRowBegin();
			ere = theError3.getRowEnd();
			
			assertEquals(5, ecb);
			assertEquals(13, ece);
			assertEquals(3, erb);
			assertEquals(3, ere);
		}
	}
	
	/**
	 * Test with an incorrect use of an opening tag. A {@link Type#PARSE_ERROR} with row 1, col 6
	 * should be thrown.
	 */
	public void testTemplateTags() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
			String toParse     = "<<<% <%= $Person:TBE.vName %>";
			
			String theExpected = "";
			String theResult   = ModelUtils.doExpand(toParse);

			assertEquals(theExpected, theResult);
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
			
			assertEquals(6, ecb);
			assertEquals(6, ece);
			assertEquals(1, erb);
			assertEquals(1, ere);
		}
	}

	/**
	 * Test with an end tag outside the syntax mode. Should trigger a {@link TokenMgrError} with begin row
	 * 1, col 33; end row and col: -1 because they are not recorded for {@link Type#TOK_MGR_ERROR}s.
	 */
	public void testTemplateTags_LonelyEnd() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
			String         toParse = "abcd <%= $Person:TBE.vName %> %%> abcd";
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
			
			assertEquals(33, ecb);
			assertEquals(-1, ece);
			assertEquals(1, erb);
			assertEquals(-1, ere);
			return;
		}
	}

	/**
	 * Test with a single end tag outside the syntax mode. Should trigger a {@link TokenMgrError}
	 * with begin row 1, col 8; end row and col: -1 because they are not recorded for
	 * {@link Type#TOK_MGR_ERROR}s.
	 */
	public void testTemplateTags_SingleEnd() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		try {
			String         toParse = "abcd %%> abcd";
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
			assertEquals(1, erb);
			assertEquals(-1, ere);
			return;
		}
	}

	public void testTemplateTags_correct() throws NoSuchAttributeException, UnknownTypeException, TemplateException {
		String toParse     = "<<<<%= $Person:TBE.vName %>>>>>>>";
		
		String theExpected = "<<<Till>>>>>>";
		String theResult   = ModelUtils.doExpand(toParse);
		
		assertEquals(theExpected, theResult);
	}
}
