/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.demo.layout.scripting.tests;

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.XmlScriptedTestUtil;
import test.com.top_logic.layout.scripting.runtime.FailureMatchingTestWrapper;

/**
 * Test whether tests fail when an error is logged.
 * 
 * @author <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TestTestsFailOnLoggedError {

	private static final Pattern PATTERN_LOG_ERROR = Pattern
			.compile("\\QAn error was logged during the test execution! Assertion failed: 1 error(s) were logged. Log messages:\n\tThis is a test error message used to check if the test framework fails properly on logged errors. As this message is on priority ERROR, it should cause the test to FAIL.\\E");

	private static final Pattern PATTERN_LOG_FATAL = Pattern
			.compile("\\QAn error was logged during the test execution! Assertion failed: 1 error(s) were logged. Log messages:\n\tThis is a test error message used to check if the test framework fails properly on logged errors. As this message is on priority FATAL it should cause the test to FAIL.\\E");

	private static final Pattern PATTERN_LOG_TWO_ERRORS = Pattern
			.compile("\\QAn error was logged during the test execution! Assertion failed: 2 error(s) were logged. Log messages:\n\tThis is a test error message used to check if the test framework fails properly on logged errors. This script logs two errors and both of them should be mentioned in the test-failed-messages. This is message ONE.\n\tThis is a test error message used to check if the test framework fails properly on logged errors. This script logs two errors and both of them should be mentioned in the test-failed-messages. This is message TWO.\\E");

	private static final Pattern PATTERN_LOG_INFO_AND_ERROR = Pattern
			.compile("\\QAn error was logged during the test execution! Assertion failed: 1 error(s) were logged. Log messages:\n\tThis is a test error message used to check if the test framework fails properly on logged errors. This script logs two messages: One on INFO and one on ERROR. Only the ERROR should be mentioned in the test-failed-messages. This is the ERROR.\\E");

	public static Test suite() {
		TestSuite suite = new TestSuite(TestTestsFailOnLoggedError.class.getCanonicalName());
		suite.addTest(createTest("01_LogDebugAndInfo"));
		suite.addTest(createTest("02_LogWarning"));
		suite.addTest(wrapExpectProblem(createTest("03_LogError"), PATTERN_LOG_ERROR, false));
		suite.addTest(wrapExpectProblem(createTest("04_LogFatal"), PATTERN_LOG_FATAL, false));
		suite.addTest(wrapExpectProblem(createTest("05_LogTwoErrors"), PATTERN_LOG_TWO_ERRORS, false));
		suite.addTest(wrapExpectProblem(createTest("06_LogInfoAndError"), PATTERN_LOG_INFO_AND_ERROR, false));
		return ApplicationTestSetup.setupApplication(suite);
	}

	private static Test createTest(String testName) {
		return XmlScriptedTestUtil.createTest(TestTestsFailOnLoggedError.class, testName);
	}

	private static Test wrapExpectProblem(Test test, Pattern expectedMessage, boolean error) {
		return new FailureMatchingTestWrapper(test, expectedMessage, error);
	}

}
