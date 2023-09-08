/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.model.attribute;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;

/**
 * Regression test that an wrong configured attribute does not crash the system.
 */
public class TestInvalidNameAttribute extends TestCase {

	/**
	 * Test suite.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestInvalidNameAttribute.class.getCanonicalName());
		suite.addTest(createTest("ExpectError"));
		return ApplicationTestSetup.setupApplication(suite);
	}

	private static Test createTest(String testName) {
		return XmlScriptedTestUtil.createTest(TestInvalidNameAttribute.class, testName);
	}

}
