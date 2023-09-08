/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.template.xml.invoke.structure;

import static test.com.top_logic.template.TemplateXMLTestUtil.*;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.template.xml.source.TemplateSource;
import com.top_logic.template.xml.source.TemplateSourceFactory;

/**
 * Tests for one template invoking another template when one of the parameters is a structure.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class TestInvokeAndStructure extends TestCase {

	/** Test for invoking a template with an empty structure as parameter. */
	public void testEmpty() {
		String expected = "pre" + "fubar" + "post";
		assertExpansion(getTemplate("TestEmptyInvoke.xml"), expected);
	}

	/** Test for invoking a template with a triple structure as parameter. */
	public void testTriple() {
		String expected = "pre" + "red" + "green" + "blue" + "post";
		assertExpansion(getTemplate("TestTripleInvoke.xml"), expected);
	}

	/** Test for invoking a template with the value of an attribute given in value tags. */
	public void testTripleInvokeValueTag() {
		String expected = "pre" + "red" + "green" + "blue" + "post";
		assertExpansion(getTemplate("TestTripleInvokeValueTag.xml"), expected);
	}

	/** Test for invoking a template with a nested structure as parameter. */
	public void testNested() {
		String expected = "pre" + "HonneyPlays" + "Dewtroid" + "Lucious Lisa" + "post";
		assertExpansion(getTemplate("TestNestedInvoke.xml"), expected);
	}

	/** Test for invoking a template with a list of structures of strings as parameter. */
	public void testListString() {
		String expected = "one" + "alpha" + "two" + "beta" + "three" + "gamma";
		assertExpansion(getTemplate("TestListStringInvoke.xml"), expected);
	}

	/** Test for invoking a template with a structure containing a string list as parameter. */
	public void testStringList() {
		String expected = "one" + "two" + "three" + "numbers";
		assertExpansion(getTemplate("TestStringListInvoke.xml"), expected);
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
		return ModuleTestSetup.setupModule(ServiceTestSetup.createSetup(TestInvokeAndStructure.class,
			TemplateSourceFactory.Module.INSTANCE));
	}

}
