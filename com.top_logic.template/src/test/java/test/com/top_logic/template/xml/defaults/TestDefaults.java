/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.defaults;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.sql.Date;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the default value feature.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestDefaults extends TestCase {

	/**
	 * Test if default values are used if no parameter value is given.
	 */
	public void testNoValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "Value of String Parameter"
			+ ""
			+ ""
			+ "Foo"
			+ "<!-- foo -->"
			+ "<foo/>"
			+ "<foo/><bar/><fubar/>"
			+ "1<!-- 2 --><foo>3</foo><bar bla=\"blub\"/><fubar><!-- 4 --></fubar>"
			+ "true"
			+ "false"
			+ "42"
			+ "3.14159"
			+ "2012-12-31T23:59:59.999Z";
		assertExpansion(getTemplate("TestDefaults.xml"), parameterValues, expected);
	}

	/**
	 * Test if default values are not used if the parameter value is given.
	 */
	public void testWithValue() throws ParseException {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String_Default", "GaGa");
		parameterValues.put("Example_Attribute_Type_XML_Default_Empty_1", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Empty_2", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Text", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Comment", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Tag", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Three_Tags", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Complex", "<bla />");
		parameterValues.put("Example_Attribute_Type_Boolean_Default_True", false);
		parameterValues.put("Example_Attribute_Type_Boolean_Default_False", true);
		parameterValues.put("Example_Attribute_Type_Integer_Default", 3);
		parameterValues.put("Example_Attribute_Type_Float_Default", -7);
		String dateString = "2001-01-01T01:01:01.001Z";
		parameterValues.put("Example_Attribute_Type_Date_Default",
			XmlDateTimeFormat.INSTANCE.parseObject(dateString));

		String expected = ""
			+ "GaGa"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "false"
			+ "true"
			+ "3"
			+ "-7.0"
			+ "2001-01-01T01:01:01.001Z";
		assertExpansion(getTemplate("TestDefaults.xml"), parameterValues, expected);
	}

	/**
	 * Test if default values are not used if null is given as value.
	 */
	public void testWithNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String_Default", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Empty_1", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Empty_2", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Text", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Comment", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Tag", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Three_Tags", null);
		parameterValues.put("Example_Attribute_Type_XML_Default_Complex", null);
		parameterValues.put("Example_Attribute_Type_Boolean_Default_True", null);
		parameterValues.put("Example_Attribute_Type_Boolean_Default_False", null);
		parameterValues.put("Example_Attribute_Type_Integer_Default", null);
		parameterValues.put("Example_Attribute_Type_Float_Default", null);
		parameterValues.put("Example_Attribute_Type_Date_Default", null);

		String expected = "";
		assertExpansion(getTemplate("TestDefaults.xml"), parameterValues, expected);
	}

	/**
	 * Test if default values are used if the default value is null and no parameter value is given.
	 */
	public void testNullNoValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "";
		assertExpansion(getTemplate("TestNull.xml"), parameterValues, expected);
	}

	/**
	 * Test if default values are not used if the default value is null and some parameter value is
	 * given.
	 */
	public void testNullWithValue() throws ParseException {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String_Default", "GaGa");
		parameterValues.put("Example_Attribute_Type_XML_Default_Empty", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Text", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Comment", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Tag", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Three_Tags", "<bla />");
		parameterValues.put("Example_Attribute_Type_XML_Default_Complex", "<bla />");
		parameterValues.put("Example_Attribute_Type_Boolean_Default_True", false);
		parameterValues.put("Example_Attribute_Type_Boolean_Default_False", true);
		parameterValues.put("Example_Attribute_Type_Integer_Default", 3);
		parameterValues.put("Example_Attribute_Type_Float_Default", -7);
		String dateString = "2001-01-01T01:01:01.001Z";
		parameterValues.put("Example_Attribute_Type_Date_Default",
			XmlDateTimeFormat.INSTANCE.parseObject(dateString));

		String expected = ""
			+ "GaGa"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "<bla />"
			+ "false"
			+ "true"
			+ "3"
			+ "-7.0"
			+ "2001-01-01T01:01:01.001Z";
		assertExpansion(getTemplate("TestNull.xml"), parameterValues, expected);
	}

	/** Test simple lists with default values, when the values are not given. */
	public void testListNoValues() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "Delta Goodrem" + "Amy Studt"
			+ "<alpha>A</alpha>" + "<beta>B</beta>"
			+ "<gamma>C</gamma>" + "<delta>D</delta>"
			+ "false" + "true"
			+ "2" + "3" + "5"
			+ "-0.0" + "-1.1" + "-2.2"
			+ "2011-11-11T00:00:00.000Z" + "2012-12-12T00:00:00.000Z";
		assertExpansion(getTemplate("TestList.xml"), parameterValues, expected);
	}

	/** Test simple lists with default values, when the values are given. */
	public void testListWithValues() {
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
			+ "<one>Eins</one>" + "<two>Zwei</two>" + "<many>Viele</many>"
			+ "true" + "false" + "true"
			+ "0" + "1" + "2"
			+ "0.1" + "0.2" + "0.3"
			+ "2001-01-01" + "2010-10-10" + "2012-12-12";
		assertExpansion(getTemplate("TestList.xml"), parameterValues, expected);
	}

	/** Test simple lists with default values, when the given values are empty lists. */
	public void testListWithEmptyListValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_List_String", null);
		parameterValues.put("Example_List_Xml", null);
		parameterValues.put("Example_List_Boolean", null);
		parameterValues.put("Example_List_Integer", null);
		parameterValues.put("Example_List_Float", null);
		parameterValues.put("Example_List_Date", null);

		String expected = "";
		assertExpansion(getTemplate("TestList.xml"), parameterValues, expected);
	}

	/**
	 * Test simple lists with default values, when the default values are empty lists and no values
	 * are given.
	 */
	public void testListEmptyValueNoValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "";
		assertExpansion(getTemplate("TestListEmptyValue.xml"), parameterValues, expected);
	}

	/**
	 * Test simple lists with default values, when the default values are empty lists and the values
	 * are given and not empty lists.
	 */
	public void testListEmptyValueWithValue() {
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
			+ "<one>Eins</one>" + "<two>Zwei</two>" + "<many>Viele</many>"
			+ "true" + "false" + "true"
			+ "0" + "1" + "2"
			+ "0.1" + "0.2" + "0.3"
			+ "2001-01-01" + "2010-10-10" + "2012-12-12";
		assertExpansion(getTemplate("TestListEmptyValue.xml"), parameterValues, expected);
	}

	/**
	 * Test simple lists with default values, when the default values and the given values are empty
	 * lists.
	 */
	public void testListEmptyValueWithEmptyListValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_List_String", null);
		parameterValues.put("Example_List_Xml", null);
		parameterValues.put("Example_List_Boolean", null);
		parameterValues.put("Example_List_Integer", null);
		parameterValues.put("Example_List_Float", null);
		parameterValues.put("Example_List_Date", null);

		String expected = "";
		assertExpansion(getTemplate("TestListEmptyValue.xml"), parameterValues, expected);
	}

	/**
	 * Test if the template syntax can be used within default value definitions.
	 */
	public void testTemplateExpression() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "1"
			+ "212"
			+ "2123212"
			+ "2123212"
			+ "121221232122123212"
			+ "<tag>121221232122123212</tag>";
		assertExpansion(getTemplate("TestTemplateExpression.xml"), parameterValues, expected);
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
		return ModuleTestSetup.setupModule(TestDefaults.class);
	}

}
