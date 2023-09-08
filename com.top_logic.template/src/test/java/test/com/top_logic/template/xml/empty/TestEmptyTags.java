/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.empty;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.template.xml.source.TemplateSource;

/**
 * Tests if the parser behaves correctly when certain tags are empty.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestEmptyTags extends TestCase {

	/** Test the expansion of a template xml file where the body tag appears but is empty. */
	public void testBodyTag() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		String expected = "";
		assertExpansion(getTemplate("TestBodyTag.xml"), parameterValues, expected);
	}

	/** Test the expansion of a template xml file where the types tag appears but is empty. */
	public void testTypesTag() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		String expected = "";
		assertExpansion(getTemplate("TestTypesTag.xml"), parameterValues, expected);
	}

	/** Test the expansion of a template xml file where the parameters tag appears but is empty. */
	public void testParametersTag() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		String expected = "";
		assertExpansion(getTemplate("TestParametersTag.xml"), parameterValues, expected);
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
		return ModuleTestSetup.setupModule(TestEmptyTags.class);
	}

}
