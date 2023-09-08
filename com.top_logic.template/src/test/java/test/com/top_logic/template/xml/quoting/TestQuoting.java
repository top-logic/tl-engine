/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.quoting;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests whether text is quoted correctly.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestQuoting extends TestCase {

	/**
	 * Test if the value written with the attribute access syntax is quoted when the output format
	 * is XML.
	 */
	public void testAttributeAccessSyntaxForXml() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter", "\"");

		String expected = "<foo bar=\"&quot;\"/>pre\"after";
		assertExpansion(getTemplate("TestAttributeAccessSyntaxForXml.xml"), parameterValues, expected);
	}

	/**
	 * Test if the value written with the attribute access syntax is quoted when the output format
	 * is plain text.
	 */
	public void testAttributeAccessSyntaxForText() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		parameterValues.put("Example_Parameter", "\"");

		String expected = "<foo bar=\"\"\"/>pre\"after";
		assertExpansion(getTemplate("TestAttributeAccessSyntaxForText.xml"), parameterValues, expected);
	}

	/**
	 * Test if an hard coded value is quoted when the output format is XML.
	 */
	public void testAttributeHardcodedForXml() {
		String expected = "<foo bar=\"&quot;\"/>pre\"after";
		assertExpansion(getTemplate("TestAttributeHardcodedForXml.xml"), expected);
	}

	/**
	 * Test if an hard coded value is quoted when the output format is plain text.
	 */
	public void testAttributeHardcodedForText() {
		String expected = "<foo bar=\"\"\"/>pre\"after";
		assertExpansion(getTemplate("TestAttributeHardcodedForText.xml"), expected);
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
		return ModuleTestSetup.setupModule(TestQuoting.class);
	}

}
