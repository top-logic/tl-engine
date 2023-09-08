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
 * Class creating tests that tests the rights of user.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestUserRights {

	/**
	 * All tests.
	 */
	public static Test suite() {
		String[] testCases = {
			"01_CreateUserWithoutAnyPrivileges",
			"02_NoAdministrationAccess",
			"FF_DeleteUserWithoutAnyPrivileges",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestUserRights.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}

