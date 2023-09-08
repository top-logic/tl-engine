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
 * Test for the security of persistent search expressions.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestPersistentSearchSecurity {

	public static Test suite() {
		String[] testCases = {
			"01_CreateUsers",
			"02_AssignRoleProfiles",
			"03_SaveSearch",
			"04_SaveAndPublishSearch",
			"05_ChangeAndSaveSearch",
			"06_PublishSearchAfterwards",
			"07_UnpublishSearch",
			"08_RevertRoleProfiles",
			"09_DeleteUsers",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestPersistentSearchSecurity.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}
