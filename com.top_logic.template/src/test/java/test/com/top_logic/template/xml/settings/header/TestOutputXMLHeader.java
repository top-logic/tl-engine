/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.settings.header;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests for the header setting for outputting a simple xml header.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestOutputXMLHeader extends TestCase {

	/**
	 * Tests if an xml header is printed if the output format is text and the default value for
	 * "output xml header" is used.
	 */
	public void testTextDefault() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "";
		assertExpansion(getTemplate("TestTextDefault.xml"), parameterValues, expected);
	}

	/**
	 * Tests if an xml header is printed if the output format is text and "output xml header" set to
	 * <code>false</code>.
	 */
	public void testTextExplicitFalse() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "";
		assertExpansion(getTemplate("TestTextExplicitFalse.xml"), parameterValues, expected);
	}

	/**
	 * Tests if an xml header is printed if the output format is text and "output xml header" set to
	 * <code>true</code>.
	 */
	public void testTextExplicitTrue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
		assertExpansion(getTemplate("TestTextExplicitTrue.xml"), parameterValues, expected);
	}

	/**
	 * Tests if an xml header is printed if the output format is xml and the default value for
	 * "output xml header" is used.
	 */
	public void testXmlDefault() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
		assertExpansion(getTemplate("TestXmlDefault.xml"), parameterValues, expected);
	}

	/**
	 * Tests if an xml header is printed if the output format is xml and "output xml header" set to
	 * <code>false</code>.
	 */
	public void testXmlExplicitFalse() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "";
		assertExpansion(getTemplate("TestXmlExplicitFalse.xml"), parameterValues, expected);
	}

	/**
	 * Tests if an xml header is printed if the output format is xml and "output xml header" set to
	 * <code>true</code>.
	 */
	public void testXmlExplicitTrue() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n";
		assertExpansion(getTemplate("TestXmlExplicitTrue.xml"), parameterValues, expected);
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
		return ModuleTestSetup.setupModule(TestOutputXMLHeader.class);
	}

}
