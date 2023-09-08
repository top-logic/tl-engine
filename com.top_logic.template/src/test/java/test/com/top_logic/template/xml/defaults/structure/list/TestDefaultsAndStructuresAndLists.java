/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.defaults.structure.list;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the default value feature when combined with structures and lists.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestDefaultsAndStructuresAndLists extends TestCase {

	/**
	 * Test for default values for a structure of lists of primitive values where the default values
	 * are defined in attributes at the list attributes.
	 */
	public void testStructureOfListInAttributeAtListAttribute() {
		String expected = ""
			+ "First Left Value"
			+ "Second Left Value"
			+ "Third Left Value"
			+ "First Right Value"
			+ "Second Right Value"
			+ "Third Right Value"
			+ "";
		assertExpansion(getTemplate("TestStructureOfListInAttributeAtListAttribute.xml"),
			expected);
	}

	/**
	 * Test for default values for a structure of lists of primitive values where the default values
	 * are defined in tags at the list attributes.
	 */
	public void testStructureOfListInTagAtListAttribute() {
		String expected = ""
			+ "First Left Value"
			+ "Second Left Value"
			+ "Third Left Value"
			+ "First Right Value"
			+ "Second Right Value"
			+ "Third Right Value"
			+ "";
		assertExpansion(getTemplate("TestStructureOfListInTagAtListAttribute.xml"), expected);
	}

	/**
	 * Test for default values for a structure of lists of primitive values where the default values
	 * are defined in attributes at the parameter declaration.
	 */
	public void testStructureOfListInAttributeAtParameter() {
		String expected = ""
			+ "First Left Value"
			+ "Second Left Value"
			+ "Third Left Value"
			+ "First Right Value"
			+ "Second Right Value"
			+ "Third Right Value"
			+ "";
		assertExpansion(getTemplate("TestStructureOfListInAttributeAtParameter.xml"), expected);
	}

	/**
	 * Test for default values for a structure of lists of primitive values where the default values
	 * are defined in tags at the parameter declaration.
	 */
	public void testStructureOfListInTagAtParameter() {
		String expected = ""
			+ "First Left Value"
			+ "Second Left Value"
			+ "Third Left Value"
			+ "First Right Value"
			+ "Second Right Value"
			+ "Third Right Value"
			+ "";
		assertExpansion(getTemplate("TestStructureOfListInTagAtParameter.xml"), expected);
	}

	/**
	 * Test for default values for a list of structures of primitive values where the default values
	 * are defined at the primitive attribute declaration.
	 */
	public void testListOfStructureAtPrimitiveAttribute() {
		String expected = ""
			+ "Left Value"
			+ "Right Value"
			+ "Second Left Value"
			+ "Right Value"
			+ "Left Value"
			+ "Third Right Value"
			+ "";
		assertExpansion(getTemplate("TestListOfStructureAtPrimitiveAttribute.xml"), expected);
	}

	/**
	 * Test for default values for a list of structures of primitive values where the default values
	 * are defined at the parameter declaration.
	 */
	public void testListOfStructureAtParameter() {
		String expected = ""
			+ "First Left Value"
			+ "First Right Value"
			+ "Second Left Value"
			+ "Second Right Value"
			+ "Third Left Value"
			+ "Third Right Value"
			+ "";
		assertExpansion(getTemplate("TestListOfStructureAtParameter.xml"), expected);
	}

	/**
	 * Test calling a template with a generic argument list.
	 */
	public void testGenericArguments() {
		String expected = ""
			+ "staticParam1"
			+ "arg1"
			+ "staticParam2"
			+ "5"
			+ "foo"
			+ "bar"
			+ "another"
			+ "value"
			+ "";
		Map<String, Object> arguments = new LinkedHashMap<>();
		arguments.put("staticParam1", "arg1");
		arguments.put("foo", "bar");
		arguments.put("another", "value");
		assertExpansion(getTemplate("TestGenericArguments.xml"), arguments, expected);
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
		return ModuleTestSetup.setupModule(TestDefaultsAndStructuresAndLists.class);
	}

}
