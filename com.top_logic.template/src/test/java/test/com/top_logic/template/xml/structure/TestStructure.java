/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.structure;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for defining and using structures. (User defined data types.)
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestStructure extends TestCase {

	/** Test for defining an empty structure. */
	public void testEmpty() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Structure_Empty", null);

		String expected = "fubar";
		assertExpansion(getTemplate("TestEmpty.xml"), parameterValues, expected);
	}

	/** Test for defining and accessing an user defined type with a single attribute. */
	public void testSingleton() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureSingletonValues = new HashMap<>();
		structureSingletonValues.put("Example_Attribute", "fubar");
		parameterValues.put("Example_Structure_Singleton", structureSingletonValues);

		String expected = "fubar";
		assertExpansion(getTemplate("TestSingleton.xml"), parameterValues, expected);
	}

	/** Test for defining and accessing an user defined type with multiple attributes. */
	public void testTriple() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureValues = new HashMap<>();
		structureValues.put("Attribute_A", "red");
		structureValues.put("Attribute_B", "green");
		structureValues.put("Attribute_C", "blue");
		parameterValues.put("Example_Structure_Triple", structureValues);

		String expected = "red" + "green" + "blue";
		assertExpansion(getTemplate("TestTriple.xml"), parameterValues, expected);
	}

	/** Test for accessing an user defined type in more than one parameter. */
	public void testMultiUse() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureA = new HashMap<>();
		structureA.put("Example_Attribute", "red");
		HashMap<String, Object> structureB = new HashMap<>();
		structureB.put("Example_Attribute", "green");
		HashMap<String, Object> structureC = new HashMap<>();
		structureC.put("Example_Attribute", "blue");
		parameterValues.put("Example_Structure_A", structureA);
		parameterValues.put("Example_Structure_B", structureB);
		parameterValues.put("Example_Structure_C", structureC);

		String expected = "red" + "green" + "blue";
		assertExpansion(getTemplate("TestMultiUse.xml"), parameterValues, expected);
	}

	/** Test for defining and accessing multiple user defined types. */
	public void testMultipleStructuredTypes() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureA = new HashMap<>();
		structureA.put("Example_Attribute_A", "red");
		HashMap<String, Object> structureB = new HashMap<>();
		structureB.put("Example_Attribute_B", "green");
		HashMap<String, Object> structureC = new HashMap<>();
		structureC.put("Example_Attribute_C", "blue");
		parameterValues.put("Example_Structure_A", structureA);
		parameterValues.put("Example_Structure_B", structureB);
		parameterValues.put("Example_Structure_C", structureC);

		String expected = "red" + "green" + "blue";
		assertExpansion(getTemplate("TestMultipleStructuredTypes.xml"), parameterValues, expected);
	}

	/** Test for defining and accessing a type that has a attribute of an other user defined type. */
	public void testNested() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> outerStructure = new HashMap<>();
		HashMap<String, Object> innerStructure = new HashMap<>();
		innerStructure.put("Attribute_Left", "Links");
		innerStructure.put("Attribute_Right", "Rechts");
		outerStructure.put("Attribute_Inner_Structure", innerStructure);
		outerStructure.put("Attribute_String", "Deutsch");
		parameters.put("Parameter_Outer_Structure", outerStructure);

		String expected = "Deutsch" + "Links" + "Rechts";
		assertExpansion(getTemplate("TestNested.xml"), parameters, expected);
	}

	/** Test for printing a structure set to null. */
	public void testOutputStructuredTypeNull() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Structure_Empty", null);

		String expected = "";
		assertExpansion(getTemplate("TestOutputStructuredTypeNull.xml"), parameterValues, expected);
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
		return ModuleTestSetup.setupModule(TestStructure.class);
	}

}
