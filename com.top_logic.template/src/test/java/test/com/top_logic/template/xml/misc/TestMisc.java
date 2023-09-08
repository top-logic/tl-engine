/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.misc;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.template.xml.TemplateXMLParser;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the {@link TemplateXMLParser}s header parsing functionality that don't fit into any
 * other category.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestMisc extends TestCase {

	/**
	 * Test if the XSD validation allows a valid template-tag nested within other non-template-tags.
	 */
	public void testXsdNestedTemplateTag() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", "Value of String Parameter");

		String expected = "<foo>" + "<bar>" +
			"Value of String Parameter" + "</bar>" + "</foo>";
		assertExpansion(getTemplate("TestXsdNestedTemplateTag.xml"), parameterValues, expected);
	}

	/** Test the expansion of an empty model. */
	public void testEmptyModel() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		String expected = "Fubar";
		assertExpansion(getTemplate("TestEmptyModel.xml"), parameterValues, expected);
	}

	/** Test the expansion of a model where a parameter without default value is missing. */
	public void testMissingParameter() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_String.*");
		assertExpansionFailure(getTemplate("TestMissingParameter.xml"), parameterValues, expectedFailure);
	}

	/** Test the expansion of a model with a single parameter. */
	public void testSingleParameterModel() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", "Value of String Parameter");

		String expected = "Value of String Parameter";
		assertExpansion(getTemplate("TestSingleParameterModel.xml"), parameterValues, expected);
	}

	/** Test the expansion of a model with one variable of every type. */
	public void testParameterTypes() throws Throwable {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", " Value of String Parameter ");
		parameterValues.put("Example_Attribute_Type_Xml", "Some Text <!-- Some Comment --> More   Text <child-node />");
		parameterValues.put("Example_Attribute_Type_Boolean", true);
		parameterValues.put("Example_Attribute_Type_Integer", 42);
		parameterValues.put("Example_Attribute_Type_Float", 3.14159);
		String dateString = "2012-12-31T23:59:59.999Z";
		parameterValues.put("Example_Attribute_Type_Date",
			XmlDateTimeFormat.INSTANCE.parseObject(dateString));

		String expected = " Value of String Parameter " +
			"Some Text <!-- Some Comment --> More   Text <child-node />" +
			"true" +
			"42" +
			"3.14159" +
			dateString;
		assertExpansion(getTemplate("TestParameterTypes.xml"), parameterValues, expected);
	}

	/** Test the expansion of a model with one variable of every type all set to <code>null</code>. */
	public void testParameterTypesNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", null);
		parameterValues.put("Example_Attribute_Type_Xml", null);
		parameterValues.put("Example_Attribute_Type_Boolean", null);
		parameterValues.put("Example_Attribute_Type_Integer", null);
		parameterValues.put("Example_Attribute_Type_Float", null);
		parameterValues.put("Example_Attribute_Type_Date", null);

		String expected = "";
		assertExpansion(getTemplate("TestParameterTypes.xml"), parameterValues, expected);
	}

	/** Test the value ranges for the numeric parameter types. */
	public void testNumberValueRanges() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ Long.toString(Long.MAX_VALUE)
			+ Long.toString(Long.MIN_VALUE)
			+ Double.toString(Double.POSITIVE_INFINITY)
			+ Double.toString(Double.NEGATIVE_INFINITY)
			+ Double.toString(Double.POSITIVE_INFINITY)
			+ Double.toString(Double.NEGATIVE_INFINITY)
			+ Double.toString(Double.NaN)
			+ Double.toString(Double.MAX_VALUE)
			+ Double.toString(Double.MIN_VALUE)
			+ Double.toString(Double.MIN_NORMAL)
			+ "";
		assertExpansion(getTemplate("TestNumberValueRanges.xml"), parameterValues, expected);
	}

	/** Test simple lists. */
	public void testLists() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_List_String", Arrays.asList("Bli", "Bla", "Blub"));
		parameterValues.put("Example_List_Xml",
			Arrays.asList("<one>Eins</one>", "<two>Zwei</two>", "<many>Viele</many>"));
		parameterValues.put("Example_List_Boolean", Arrays.asList(true, false, true));
		parameterValues.put("Example_List_Integer", Arrays.asList(0, 1, 2));
		parameterValues.put("Example_List_Float", Arrays.asList(0.1, 0.2, 0.3));
		parameterValues.put("Example_List_Date",
			Arrays.asList(new Date(101, 0, 1), new Date(110, 9, 10), new Date(112, 11, 12)));

		String expected = ""
			+ "Bli" + "Bla" + "Blub"
			+ "<one>Eins</one>" + "<two>Zwei</two>"
			+ "<many>Viele</many>"
			+ "true" + "false" + "true"
			+ "0" + "1" + "2"
			+ "0.1" + "0.2" + "0.3"
			+ "2001-01-01" + "2010-10-10"
			+ "2012-12-12";
		assertExpansion(getTemplate("TestLists.xml"), parameterValues, expected);
	}

	private TemplateSource getTemplate(String filename) {
		return createTemplateSource(filename, getClass());
	}

	/**
	 * Create the {@link TestSuite} with the necessary setups for this {@link Test}.
	 */
	public static Test suite() {
		// The setup is needed for some of the error messages that are being thrown when something
		// is broken. If everything is green, the setup would not be needed.
		return ModuleTestSetup.setupModule(TestMisc.class);
	}

}
