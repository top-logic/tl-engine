/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.security;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Class creating tests test security in default search.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestSearchSecurity {

	/**
	 * All tests.
	 */
	public static Test suite() {
		String[] testCases = {
			"01_CreateUser",
			"02_CreateObjects",
			"03_TestSearchNoReadRight",
			"04_SetReadRight",
			"05_TestSearchReadRight",
			"06_DeleteObjects",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestSearchSecurity.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}

