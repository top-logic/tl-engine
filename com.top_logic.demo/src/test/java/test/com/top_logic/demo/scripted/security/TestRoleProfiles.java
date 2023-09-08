/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.security;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Test around the role profiles.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestRoleProfiles extends BasicTestCase {

	/**
	 * All tests.
	 */
	public static Test suite() {
		String[] testCases = {
			"01_ImportRoleProfiles",
		};
		TestSuite theTestSuite = XmlScriptedTestUtil.suite(TestRoleProfiles.class, testCases);
		return DemoSetup.createDemoSetup(theTestSuite);
	}

}

