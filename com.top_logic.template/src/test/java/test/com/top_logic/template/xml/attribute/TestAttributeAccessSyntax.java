/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.attribute;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Tests for the attribute access syntax.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestAttributeAccessSyntax extends TestCase {

	/**
	 * Test if the attribute access syntax is working correctly.
	 */
	public void testSyntax() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter_A", "Alpha");
		parameterValues.put("Example_Parameter_B", "Beta");
		parameterValues.put("Example_Parameter_C", "Gamma");

		String expected = ""
			+ "<fubar><foo bar=\"Alpha\"/></fubar>"
			+ "<foo bar=\"\"/>"
			+ "<foo bar=\"$\"/>"
			+ "<foo bar=\"$\"/>"
			+ "<foo bar=\"$$\"/>"
			+ "<foo bar=\"$$\"/>"
			+ "<foo bar=\"$$$\"/>"
			+ "<foo bar=\"$$$\"/>"
			+ "<foo bar=\" $ \"/>"
			+ "<foo bar=\" $ \"/>"
			+ "<foo bar=\" $$ \"/>"
			+ "<foo bar=\" $$ \"/>"
			+ "<foo bar=\" $$$ \"/>"
			+ "<foo bar=\" $$$ \"/>"
			+ "<foo bar=\"$ $\"/>"
			+ "<foo bar=\"$ $ $\"/>"
			+ "<foo bar=\" $ $ \"/>"
			+ "<foo bar=\" $ $ $ \"/>"
			+ "<foo bar=\"$ $\"/>"
			+ "<foo bar=\"$ $ $\"/>"
			+ "<foo bar=\" $ $ \"/>"
			+ "<foo bar=\" $ $ $ \"/>"
			+ "<foo bar=\"  $  \"/>"
			+ "<foo bar=\"  $  \"/>"
			+ "<foo bar=\"   $   \"/>"
			+ "<foo bar=\"   $   \"/>"
			+ "<foo bar=\"  $  $  $  \"/>"
			+ "<foo bar=\"   $   $   $   \"/>"
			+ "<foo bar=\"Alpha\"/>"
			+ "<foo bar=\"AlphaBeta\"/>"
			+ "<foo bar=\"AlphaBetaGamma\"/>"
			+ "<foo bar=\" Alpha \"/>"
			+ "<foo bar=\" AlphaBeta \"/>"
			+ "<foo bar=\" AlphaBetaGamma \"/>"
			+ "<foo bar=\"Alpha Beta\"/>"
			+ "<foo bar=\"Alpha Beta Gamma\"/>"
			+ "<foo bar=\" Alpha Beta \"/>"
			+ "<foo bar=\" Alpha Beta Gamma \"/>"
			+ "<foo bar=\"  Alpha  \"/>"
			+ "<foo bar=\"   Alpha   \"/>"
			+ "<foo bar=\"  Alpha  Beta  Gamma  \"/>"
			+ "<foo bar=\"   Alpha   Beta   Gamma   \"/>"
			+ "<foo bar=\"${$Example_Parameter_A}\"/>"
			+ "<foo bar=\" ${$Example_Parameter_A} \"/>"
			+ "<foo bar=\"$${$Example_Parameter_A}\"/>"
			+ "<foo bar=\" $${$Example_Parameter_A} \"/>"
			+ "<foo bar=\"$Alpha\"/>"
			+ "<foo bar=\"$$Alpha\"/>"
			+ "<foo bar=\"Alpha$\"/>"
			+ "<foo bar=\"Alpha$\"/>"
			+ "<foo bar=\"$Alpha$Beta$Gamma\"/>"
			+ "";
		assertExpansion(getTemplate("TestSyntax.xml"), parameterValues, expected);
	}

	/**
	 * Test if the parameters can be accessed within xml attributes that are within the template.
	 */
	public void testInTemplate() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter", "Example_Value");

		String expected = "<foo bar=\"Example_Value\"/>";
		assertExpansion(getTemplate("TestInTemplate.xml"), parameterValues, expected);
	}

	/**
	 * Test if the parameters can be accessed within xml attributes that are within default value
	 * definitions.
	 */
	public void testInDefaultValueXml() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter_A", "Example_Value");

		String expected = "<foo bar=\"Example_Value\"/>";
		assertExpansion(getTemplate("TestInDefaultValueXml.xml"), parameterValues, expected);
	}

	/**
	 * Test if the parameters can be accessed within the 'default' attribute.
	 */
	public void testInDefaultValue() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter_A", "Example_Value");

		String expected = "Example_Value";
		assertExpansion(getTemplate("TestInDefaultValueAttribute.xml"), parameterValues, expected);
	}

	/**
	 * Test if the parameters can be accessed within the parameter value attribute of an invoke
	 * statement.
	 */
	public void testInInvokeParameterValue() {
		String expected = "Example Value";
		assertExpansion(getTemplate("TestInInvokeParameterValue.xml"), expected);
	}

	/**
	 * Test all parameter types within the parameter value attribute of an invoke statement.
	 */
	public void testParameterTypesInInvoke() {
		String expected = ""
			+ "Example Value"
			+ "<foo><bar fubar=\"snafu\"/></foo>"
			+ "true"
			+ "42"
			+ "3.14159"
			+ "2012-12-31T23:59:59.999Z";
		assertExpansion(getTemplate("TestParameterTypesInInvoke.xml"), expected);
	}

	/**
	 * Test string lists within the parameter value attribute of an invoke statement.
	 */
	public void testStringListsInInvoke() {
		String expected = ""
			+ "Delta Goodrem"
			+ "Amy Studt"
			+ "Natasha Bedingfield";
		assertExpansion(getTemplate("TestStringListsInInvoke.xml"), expected);
	}

	/**
	 * Test if the parameters can be accessed within XML namespace declarations.
	 */
	public void testInNamespaceDeclaration() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter", "Example_Namespace");

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
			+ "<foo xmlns:example=\"Example_Namespace\"/>";
		assertExpansion(getTemplate("TestInNamespaceDeclaration.xml"), parameterValues, expected);
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
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestAttributeAccessSyntax.class,
			TemplateSourceFactory.Module.INSTANCE));
	}

}
