/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.defaults.structure;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the default value feature when combined with structures.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestDefaultsAndStructures extends TestCase {

	/**
	 * Test for default values for a structure of primitive values where the default values are
	 * defined in attributes at the attributes.
	 */
	public void testInAttributeAtAttributes() {
		String expected = ""
			+ "Left Value"
			+ "Right Value"
			+ "";
		assertExpansion(getTemplate("TestInAttributeAtAttributes.xml"), expected);
	}

	/**
	 * Test for default values for a structure of primitive values where the default values are
	 * defined in tags at the attributes.
	 */
	public void testInTagAtAttributes() {
		String expected = ""
			+ "Left Value"
			+ "Right Value"
			+ "";
		assertExpansion(getTemplate("TestInTagAtAttributes.xml"), expected);
	}

	/**
	 * Test for default values for a structure of primitive values where the default values are
	 * defined in attributes at the parameter declaration.
	 */
	public void testInAttributeAtParameter() {
		String expected = ""
			+ "Left Value"
			+ "Right Value"
			+ "";
		assertExpansion(getTemplate("TestInAttributeAtParameter.xml"), expected);
	}

	/**
	 * Test for default values for a structure of primitive values where the default values are
	 * defined in tags at the parameter declaration.
	 */
	public void testInTagAtParameter() {
		String expected = ""
			+ "Left Value"
			+ "Right Value"
			+ "";
		assertExpansion(getTemplate("TestInTagAtParameter.xml"), expected);
	}

	/**
	 * Test for default values for a structure of structures of primitive values where the default
	 * values are defined at the primitive attribute declaration.
	 */
	public void testNestedStructureAtPrimitiveAttribute() {
		String expected = ""
			+ "Left Inner Value"
			+ "Right Inner Value"
			+ "Left Inner Value"
			+ "Right Inner Value"
			+ "";
		assertExpansion(getTemplate("TestNestedStructureAtPrimitiveAttribute.xml"),
			expected);
	}

	/**
	 * Test for default values for a structure of structures of primitive values where the default
	 * values are defined at the structure attribute declaration.
	 */
	public void testNestedStructureAtStructureAttribute() {
		String expected = ""
			+ "Left Outer Left Inner Value"
			+ "Left Outer Right Inner Value"
			+ "Right Outer Left Inner Value"
			+ "Right Outer Right Inner Value"
			+ "";
		assertExpansion(getTemplate("TestNestedStructureAtStructureAttribute.xml"),
			expected);
	}

	/**
	 * Test for default values for a structure of structures of primitive values where the default
	 * values are defined at the parameter declaration.
	 */
	public void testNestedStructureAtParameter() {
		String expected = ""
			+ "Left Outer Left Inner Value"
			+ "Left Outer Right Inner Value"
			+ "Right Outer Left Inner Value"
			+ "Right Outer Right Inner Value"
			+ "";
		assertExpansion(getTemplate("TestNestedStructureAtParameter.xml"), expected);
	}

	/** Test for default values for complex structures. */
	public void testComplexStructures() {
		String expected = ""
			+ "Fubar"
			+ "Some different default value"
			+ "Another default value"
			+ "Yet another default value"

			+ "Snafu"
			+ "Some default value"
			+ "Bliblablub"
			+ "Yet another default value"
			+ "";
		assertExpansion(getTemplate("TestComplexStructures.xml"), expected);
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
		return ModuleTestSetup.setupModule(TestDefaultsAndStructures.class);
	}

}
