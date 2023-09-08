/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.scripting.tests;

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.demo.DemoSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;
import test.com.top_logic.layout.scripting.runtime.FailureMatchingTestWrapper;

/**
 * Test that JSPs get executed when scripting and errors on JSPs cause the test to fail.
 * <p>
 * <b>Note:</b> The failure messages for errors caused by JSPs changes depending on whether the JSP
 * is already compiled or had to be compiled for this test. (I don't know why.) These tests accepts
 * both error messages.
 * </p>
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestScriptingJspRendering {

	private static final Pattern EXPECTED_ERROR_FOR_ERROR_PAGE =
		Pattern.compile("(?ms).*Test of page throwing an error from a JSP.*");

	private static final Pattern EXPECTED_ERROR_FOR_FORM_JSP =
		Pattern.compile("(?ms).*Test of form throwing an error from a JSP.*");

	private static final Pattern EXPECTED_ERROR_FOR_FORM_COMPONENT =
		Pattern.compile("(?ms).*Intentionally thrown from form context creation.*");

	public static Test suite() {
		TestSuite suite = new TestSuite(TestScriptingJspRendering.class.getCanonicalName());
		suite.addTest(wrap(createTest("01_TestErrorPage"), EXPECTED_ERROR_FOR_ERROR_PAGE, false));
		suite.addTest(wrap(createTest("02_TestErrorFormJsp"), EXPECTED_ERROR_FOR_FORM_JSP, false));
		suite.addTest(wrap(createTest("03_TestErrorFormComponent"), EXPECTED_ERROR_FOR_FORM_COMPONENT, false));
		return DemoSetup.createDemoSetup(suite);
	}

	private static Test createTest(String testName) {
		return XmlScriptedTestUtil.createTest(TestScriptingJspRendering.class, testName);
	}

	private static Test wrap(Test test, Pattern expectedError, boolean error) {
		return new FailureMatchingTestWrapper(test, expectedError, error);
	}

}
