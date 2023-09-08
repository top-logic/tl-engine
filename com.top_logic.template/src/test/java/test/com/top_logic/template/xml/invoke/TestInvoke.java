/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.invoke;

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
 * Tests for one template invoking another template.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestInvoke extends TestCase {

	/**
	 * Test the invocation of a template from within a template, where the invoked template has an
	 * empty model.
	 */
	public void testEmptyModel() {
		HashMap<String, Object> parameterValues = new HashMap<>();
		String expected = "PreFubarPost";
		assertExpansion(getTemplate("TestEmptyModelInvoke.xml"), parameterValues, expected);
	}

	/**
	 * Test the invocation of a template from within a template, where the invoked template has a
	 * model with one variable of every type.
	 */
	public void testParameterTypes() {
		HashMap<String, Object> parameterValues = new HashMap<>();

		String expected = ""
			+ "Value of String Parameter"
			+ "Some Text" + "<!-- Some Comment -->" + "More Text"
			+ "<child-node/>"
			+ "true"
			+ "42"
			+ "3.14159"
			+ "2012-12-31T23:59:59.999Z";
		assertExpansion(getTemplate("TestParameterTypesInvoke.xml"), parameterValues, expected);
	}

	/** Test for invoking a template with a list of Strings as parameter. */
	public void testStringListInvoke() {
		String expected = "one" + "two" + "three";
		assertExpansion(getTemplate("TestStringListInvoke.xml"), expected);
	}

	/** Test for invoking a template with a list of XMLs as parameter. */
	public void testXmlListInvoke() {
		String expected = ""
			+ "1"
			+ "<!-- 2 -->"
			+ "<foo>"
			+ "3"
			+ "</foo>"
			+ "<bar bla=\"blub\"/>"
			+ "<fubar>"
			+ "<!-- 4 -->"
			+ "</fubar>"

			+ ""
			+ ""

			+ "<foo><bar><fubar/></bar></foo>"

			+ "123"

			+ "<!-- Some Comment -->";
		assertExpansion(getTemplate("TestXmlListInvoke.xml"), expected);
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
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestInvoke.class,
			TemplateSourceFactory.Module.INSTANCE));
	}

}
