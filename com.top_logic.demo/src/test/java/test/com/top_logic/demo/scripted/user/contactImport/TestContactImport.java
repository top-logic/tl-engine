/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.user.contactImport;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Tests importer of contact
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestContactImport {

	public static Test suite() {
		TestSuite testSuite =
			XmlScriptedTestUtil.suite(TestContactImport.class,
				"01_importExcel",
				"02_importCSV"
			);
		return DemoSetup.createDemoSetup(testSuite);
	}

}
