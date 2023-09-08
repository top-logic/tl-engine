/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.mandatory;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the mandatory flag on parameters and parameter attributes.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestMandatory extends TestCase {

	/**
	 * Test the expansion of a model with a mandatory parameter of type 'string'.
	 */
	public void testString() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", "foo");

		String expected = "foo";
		assertExpansion(getTemplate("TestString.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'string' gets the empty
	 * string as value.
	 */
	public void testStringEmpty() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", "");

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_String.*mandatory.*");
		assertExpansionFailure(getTemplate("TestString.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'string' gets
	 * <code>null</code> as value.
	 */
	public void testStringNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_String", null);

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_String.*mandatory.*");
		assertExpansionFailure(getTemplate("TestString.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model with a mandatory parameter of type 'xml'.
	 */
	public void testXml() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Xml", "<foo />");

		String expected = "<foo />";
		assertExpansion(getTemplate("TestXml.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'xml' gets an empty xml
	 * snippet as value.
	 */
	public void testXmlEmpty() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Xml", "");

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_Xml.*mandatory.*");
		assertExpansionFailure(getTemplate("TestXml.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'xml' gets
	 * <code>null</code> as value.
	 */
	public void testXmlNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Xml", null);

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_Xml.*mandatory.*");
		assertExpansionFailure(getTemplate("TestXml.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model with a mandatory parameter of type 'boolean'.
	 */
	public void testBoolean() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Boolean", true);

		String expected = "true";
		assertExpansion(getTemplate("TestBoolean.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'boolean' gets
	 * <code>null</code> as value.
	 */
	public void testBooleanNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Boolean", null);

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_Boolean.*mandatory.*");
		assertExpansionFailure(getTemplate("TestBoolean.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model with a mandatory parameter of type 'integer'.
	 */
	public void testInteger() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Integer", 42);

		String expected = "42";
		assertExpansion(getTemplate("TestInteger.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'integer' gets
	 * <code>null</code> as value.
	 */
	public void testIntegerNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Integer", null);

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_Integer.*mandatory.*");
		assertExpansionFailure(getTemplate("TestInteger.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model with a mandatory parameter of type 'float'.
	 */
	public void testFloat() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Float", 3.14159);

		String expected = "3.14159";
		assertExpansion(getTemplate("TestFloat.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'float' gets
	 * <code>null</code> as value.
	 */
	public void testFloatNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Float", null);

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_Float.*mandatory.*");
		assertExpansionFailure(getTemplate("TestFloat.xml"), parameterValues, expectedFailure);
	}

	/**
	 * Test the expansion of a model with a mandatory parameter of type 'date'.
	 */
	public void testDate() throws Throwable {
		HashMap<String, Object> parameterValues = new HashMap<>();
		String dateString = "2012-12-31T23:59:59.999Z";
		parameterValues.put("Example_Attribute_Type_Date",
			XmlDateTimeFormat.INSTANCE.parseObject(dateString));

		String expected = dateString;
		assertExpansion(getTemplate("TestDate.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a model where a mandatory parameter of type 'date' gets
	 * <code>null</code> as value.
	 */
	public void testDateNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Attribute_Type_Date", null);

		Pattern expectedFailure = Pattern.compile(".*Example_Attribute_Type_Date.*mandatory.*");
		assertExpansionFailure(getTemplate("TestDate.xml"), parameterValues, expectedFailure);
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
		return ModuleTestSetup.setupModule(TestMandatory.class);
	}

}
