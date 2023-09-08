/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.parser.variable;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.TemplateXMLParser;
import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the {@link TemplateXMLParser}s header parsing functionality.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestVariablePathSyntax extends TestCase {

	/** Test the expansion of a template where a variable is read once. */
	public void testVariableRead() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter", "Example Value");

		String expected = "Example Value";
		assertExpansion(getTemplate("TestVariableRead.xml"), parameterValues, expected);
	}

	/** Test the expansion of a template where a variable is read more than once. */
	public void testVariableMultiRead() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter", "Example Value");

		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestVariableMultiRead.xml"), parameterValues, expected);
	}

	/**
	 * Test for reading an attribute of a structure with the colon syntax. This should fail as the
	 * part before the colon is a namespace and namespaces are not supported.
	 */
	public void testStructureColonRead() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureSingletonValues = new HashMap<>();
		structureSingletonValues.put("Example_Attribute", "Example Value");
		parameterValues.put("Example_Parameter", structureSingletonValues);

		Pattern expected = Pattern.compile(".*not supported.*");
		assertExpansionFailure(getTemplate("TestStructureColonRead.xml"), parameterValues, expected);
	}

	/** Test for reading an attribute of a structure with the dot syntax. */
	public void testStructureDotRead() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureSingletonValues = new HashMap<>();
		structureSingletonValues.put("Example_Attribute", "Example Value");
		parameterValues.put("Example_Parameter", structureSingletonValues);

		String expected = "Example Value";
		assertExpansion(getTemplate("TestStructureDotRead.xml"), parameterValues, expected);
	}

	/** Test for reading an attribute of a structure with the dot syntax multiple times. */
	public void testStructureDotMultiRead() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		HashMap<String, Object> structureSingletonValues = new HashMap<>();
		structureSingletonValues.put("Example_Attribute", "Example Value");
		parameterValues.put("Example_Parameter", structureSingletonValues);

		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestStructureDotMultiRead.xml"), parameterValues, expected);
	}

	/**
	 * Test the expansion of a template where a variable gets a value from a structure attribute and
	 * is read more than once.
	 */
	public void testVariableFromStructureMultiRead() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> structureExample = new HashMap<>();
		structureExample.put("Example_Attribute", "Example Value");
		parameters.put("Example_Parameter", structureExample);

		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestVariableFromStructureMultiRead.xml"), parameters, expected);
	}

	/**
	 * Test the expansion of a template where a variable gets a structure as value and an attribute
	 * of that structure is read with the colon syntax once.
	 */
	public void testVariableWithStructureColonRead() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> structureExample = new HashMap<>();
		structureExample.put("Example_Attribute", "Example Value");
		parameters.put("Example_Parameter", structureExample);

		Pattern expected = Pattern.compile(".*Example_Attribute.*");
		assertExpansionFailure(getTemplate("TestVariableWithStructureColonRead.xml"), parameters, expected);
	}

	/**
	 * Test the expansion of a template where a variable gets a structure as value and an attribute
	 * of that structure is read with the dot syntax once.
	 */
	public void testVariableWithStructureDotRead() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> structureExample = new HashMap<>();
		structureExample.put("Example_Attribute", "Example Value");
		parameters.put("Example_Parameter", structureExample);

		String expected = "Example Value";
		assertExpansion(getTemplate("TestVariableWithStructureDotRead.xml"), parameters, expected);
	}

	/**
	 * Test the expansion of a template where a variable gets a structure as value and an attribute
	 * of that structure is read with the dot syntax more than once.
	 */
	public void testVariableWithStructureDotMultiRead() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> structureExample = new HashMap<>();
		structureExample.put("Example_Attribute", "Example Value");
		parameters.put("Example_Parameter", structureExample);

		String expected = "Example Value" + "Example Value" + "Example Value";
		assertExpansion(getTemplate("TestVariableWithStructureDotMultiRead.xml"), parameters, expected);
	}

	/**
	 * Test the expansion of a template where a variable gets a structure as value and multiple
	 * attributes of that structure are read with the dot syntax.
	 */
	public void testVariableWithStructureDotMultiAttributeRead() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> structureExample = new HashMap<>();
		structureExample.put("Example_Attribute_A", "Example Value A");
		structureExample.put("Example_Attribute_B", "Example Value B");
		structureExample.put("Example_Attribute_C", "Example Value C");
		parameters.put("Example_Parameter", structureExample);

		String expected = "Example Value A" + "Example Value B" + "Example Value C";
		assertExpansion(getTemplate("TestVariableWithStructureDotMultiAttributeRead.xml"), parameters, expected);
	}

	/**
	 * Test the expansion of a template with deeply nested types and variables.
	 */
	public void testVariableDeeplyNestedReads() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> level5Structure = new HashMap<>();
		HashMap<String, Object> level4Structure = new HashMap<>();
		HashMap<String, Object> level3Structure = new HashMap<>();
		HashMap<String, Object> level2Structure = new HashMap<>();
		HashMap<String, Object> level1Structure = new HashMap<>();
		level1Structure.put("Left_Attribute", "Level 1 Left Value");
		level1Structure.put("Right_Attribute", "Level 1 Right Value");
		level2Structure.put("Level_1_Attribute", level1Structure);
		level2Structure.put("Integer_Attribute", 42);
		level3Structure.put("Level_2_Attribute", level2Structure);
		level3Structure.put("Float_Attribute", 3.14159);
		level4Structure.put("Level_3_Attribute", level3Structure);
		level4Structure.put("Boolean_Attribute", true);
		level5Structure.put("Level_4_Attribute", level4Structure);
		level5Structure.put("Xml_Attribute", "<fubar />");
		parameters.put("Level_5_Parameter", level5Structure);
		parameters.put("String_Parameter", "String Parameter Value");

		String expected = ""
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "Level 1 Right Value"
			+ "Level 1 Right Value"
			+ "42"
			+ "42"
			+ "3.14159"
			+ "3.14159"
			+ "true"
			+ "true"
			+ "<fubar />"
			+ "<fubar />"
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ ""
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "<fubar />"
			+ "<fubar />"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "true"
			+ "true"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "3.14159"
			+ "3.14159"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "42"
			+ "42"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "Level 1 Right Value"
			+ "Level 1 Right Value"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ ""
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "Level 1 Right Value"
			+ "Level 1 Right Value"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "42"
			+ "42"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "3.14159"
			+ "3.14159"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "true"
			+ "true"
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "<fubar />"
			+ "<fubar />"
			+ ""
			+ "Level 1 Left Value"
			+ "Level 1 Left Value"
			+ "Level 1 Right Value"
			+ "Level 1 Right Value"
			+ "42"
			+ "42"
			+ "3.14159"
			+ "3.14159"
			+ "true"
			+ "true"
			+ "<fubar />"
			+ "<fubar />"
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ "";
		assertExpansion(getTemplate("TestVariableDeeplyNestedReads.xml"), parameters, expected);
	}

	/**
	 * Test the expansion of a template with deeply nested types and variables in a for-loop.
	 */
	public void testVariableDeeplyNestedReadsInFor() {
		HashMap<String, Object> parameters = new HashMap<>();
		HashMap<String, Object> level5Structure_1 = new HashMap<>();
		HashMap<String, Object> level4Structure_1 = new HashMap<>();
		HashMap<String, Object> level3Structure_1 = new HashMap<>();
		HashMap<String, Object> level2Structure_1 = new HashMap<>();
		HashMap<String, Object> level1Structure_1 = new HashMap<>();
		level1Structure_1.put("Left_Attribute", "Level 1 Left Value 1");
		level1Structure_1.put("Right_Attribute", "Level 1 Right Value 1");
		level2Structure_1.put("Level_1_Attribute", level1Structure_1);
		level2Structure_1.put("Integer_Attribute", 42);
		level3Structure_1.put("Level_2_Attribute", level2Structure_1);
		level3Structure_1.put("Float_Attribute", 3.14159);
		level4Structure_1.put("Level_3_Attribute", level3Structure_1);
		level4Structure_1.put("Boolean_Attribute", true);
		level5Structure_1.put("Level_4_Attribute", level4Structure_1);
		level5Structure_1.put("Xml_Attribute", "<fubar />");
		HashMap<String, Object> level5Structure_2 = new HashMap<>();
		HashMap<String, Object> level4Structure_2 = new HashMap<>();
		HashMap<String, Object> level3Structure_2 = new HashMap<>();
		HashMap<String, Object> level2Structure_2 = new HashMap<>();
		HashMap<String, Object> level1Structure_2 = new HashMap<>();
		level1Structure_2.put("Left_Attribute", "Level 1 Left Value 2");
		level1Structure_2.put("Right_Attribute", "Level 1 Right Value 2");
		level2Structure_2.put("Level_1_Attribute", level1Structure_2);
		level2Structure_2.put("Integer_Attribute", 23);
		level3Structure_2.put("Level_2_Attribute", level2Structure_2);
		level3Structure_2.put("Float_Attribute", 1.618);
		level4Structure_2.put("Level_3_Attribute", level3Structure_2);
		level4Structure_2.put("Boolean_Attribute", false);
		level5Structure_2.put("Level_4_Attribute", level4Structure_2);
		level5Structure_2.put("Xml_Attribute", "<foo />");
		List<Object> level5ListOfStructures = Arrays.<Object> asList(level5Structure_1, level5Structure_2);
		parameters.put("Level_5_Parameter", level5ListOfStructures);
		parameters.put("String_Parameter", "String Parameter Value");

		String expected = ""
			+ "xxx Loop Start xxx"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Right Value 1"
			+ "Level 1 Right Value 1"
			+ "42"
			+ "42"
			+ "3.14159"
			+ "3.14159"
			+ "true"
			+ "true"
			+ "<fubar />"
			+ "<fubar />"
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ ""
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "<fubar />"
			+ "<fubar />"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "true"
			+ "true"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "3.14159"
			+ "3.14159"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "42"
			+ "42"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Right Value 1"
			+ "Level 1 Right Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ ""
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Right Value 1"
			+ "Level 1 Right Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "42"
			+ "42"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "3.14159"
			+ "3.14159"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "true"
			+ "true"
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "<fubar />"
			+ "<fubar />"
			+ ""
			+ "Level 1 Left Value 1"
			+ "Level 1 Left Value 1"
			+ "Level 1 Right Value 1"
			+ "Level 1 Right Value 1"
			+ "42"
			+ "42"
			+ "3.14159"
			+ "3.14159"
			+ "true"
			+ "true"
			+ "<fubar />"
			+ "<fubar />"
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ "xxx Loop End xxx"

			+ "xxx Loop Start xxx"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Right Value 2"
			+ "Level 1 Right Value 2"
			+ "23"
			+ "23"
			+ "1.618"
			+ "1.618"
			+ "false"
			+ "false"
			+ "<foo />"
			+ "<foo />"
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ ""
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "<foo />"
			+ "<foo />"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "false"
			+ "false"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "1.618"
			+ "1.618"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "23"
			+ "23"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Right Value 2"
			+ "Level 1 Right Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ ""
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Right Value 2"
			+ "Level 1 Right Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "23"
			+ "23"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "1.618"
			+ "1.618"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "false"
			+ "false"
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "<foo />"
			+ "<foo />"
			+ ""
			+ "Level 1 Left Value 2"
			+ "Level 1 Left Value 2"
			+ "Level 1 Right Value 2"
			+ "Level 1 Right Value 2"
			+ "23"
			+ "23"
			+ "1.618"
			+ "1.618"
			+ "false"
			+ "false"
			+ "<foo />"
			+ "<foo />"
			+ "String Parameter Value"
			+ "String Parameter Value"
			+ "xxx Loop End xxx"
			+ "";
		assertExpansion(getTemplate("TestVariableDeeplyNestedReadsInFor.xml"), parameters, expected);
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
		return ModuleTestSetup.setupModule(TestVariablePathSyntax.class);
	}

}
