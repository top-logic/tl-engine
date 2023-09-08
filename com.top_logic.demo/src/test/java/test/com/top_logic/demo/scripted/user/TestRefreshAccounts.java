/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.user;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test for refreshing account list.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestRefreshAccounts {

	@SuppressWarnings("javadoc")
	public static Test suite() {
		TestSuite testSuite =
			XmlScriptedTestUtil.suite(TestRefreshAccounts.class,
				"01_testRootUser",
				"02_setupNonRootUser",
				"03_testNonRootUser",
				"04_teardownNonRootUser"
			);
		return DemoSetup.createDemoSetup(testSuite);
	}

}
