/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.services.simpleajax;

import java.io.IOException;

import junit.framework.Test;
import junit.textui.TestRunner;

import com.top_logic.base.services.simpleajax.AJAXConstants;
import com.top_logic.base.services.simpleajax.JSSnipplet;

/**
 * Testcase for {@link JSSnipplet}.
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TestJSSnipplet extends ActionTestcase {

	/**
	 * Create a new TestJSSnipplet for given function.
	 */
	public TestJSSnipplet(String aName) {
		super(aName);
	}

	/**
	 * Test CTor.
	 */
	public void testCTor() throws Exception {
		JSSnipplet snip = new JSSnipplet("window.opener.close()");

		assertXMLOutput(snip, "<"
				+ AJAXConstants.AJAX_ACTION_ELEMENT + ' '
				+ JSSnipplet.XSI_NAMESPACE_DECL + ' '
				+ AJAXConstants.XSI_TYPE_ATTRIBUTE + "=\"JSSnipplet\">"
				+ "<ajax:code>window.opener.close()</ajax:code>"
				+ "</" + AJAXConstants.AJAX_ACTION_ELEMENT + '>');
	}

	/**
	 * Test method for
	 * {@link com.top_logic.base.services.simpleajax.JSSnipplet#createAlert(java.lang.String)}.
	 */
	public void testCreateAlert() throws Exception {
		JSSnipplet snip = JSSnipplet.createAlert("testCreateAlert");
		assertXMLOutput(snip, "<"
				+ AJAXConstants.AJAX_ACTION_ELEMENT + ' '
				+ JSSnipplet.XSI_NAMESPACE_DECL + ' '
				+ AJAXConstants.XSI_TYPE_ATTRIBUTE + "=\"JSSnipplet\">"
				+ "<ajax:code>alert(\"testCreateAlert\")</ajax:code>"
				+ "</" + AJAXConstants.AJAX_ACTION_ELEMENT + '>');
	}

	/**
	 * Test method for {@link com.top_logic.base.services.simpleajax.JSSnipplet#createPageReload()}.
	 */
	public void testCreatePageReload() throws Exception {
		JSSnipplet snip = JSSnipplet.createPageReload();
		assertXMLOutput(snip, "<"
				+ AJAXConstants.AJAX_ACTION_ELEMENT + ' '
				+ JSSnipplet.XSI_NAMESPACE_DECL + ' '
				+ AJAXConstants.XSI_TYPE_ATTRIBUTE + "=\"JSSnipplet\">"
				+ "<ajax:code>window.location.reload();</ajax:code>"
				+ "</" + AJAXConstants.AJAX_ACTION_ELEMENT + '>');
	}

	/**
	 * Return the suite of Tests to execute.
	 */
	public static Test suite() {
		return suite(TestJSSnipplet.class);
	}

	/**
	 * Main function for direct execution.
	 */
	public static void main(String[] args) throws IOException {

		TestRunner.run(suite());
	}

}
