/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.scripted.model.attribute;

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;
import test.com.top_logic.layout.scripting.runtime.FailureMatchingTestWrapper;

/**
 * Regression test that an wrong configured attribute does not crash the system.
 */
public class TestInvalidNameAttribute extends TestCase {

	private static final Pattern PATTERN_LOG_ERROR =
		Pattern.compile("(?s).*\\QCannot cast com.top_logic.basic.util.ResKey$LiteralKey to java.lang.String\\E.*");

	/**
	 * Test suite.
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite(TestInvalidNameAttribute.class.getCanonicalName());
		suite.addTest(wrapExpectProblem(createTest("ExpectError"), PATTERN_LOG_ERROR, false));
		return ApplicationTestSetup.setupApplication(suite);
	}

	private static Test createTest(String testName) {
		return XmlScriptedTestUtil.createTest(TestInvalidNameAttribute.class, testName);
	}

	private static Test wrapExpectProblem(Test test, Pattern expectedMessage, boolean error) {
		return new FailureMatchingTestWrapper(test, expectedMessage, error);
	}

}
