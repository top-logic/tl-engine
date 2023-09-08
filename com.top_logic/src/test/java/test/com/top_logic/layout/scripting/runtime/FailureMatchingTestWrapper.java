/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import java.util.Enumeration;
import java.util.regex.Pattern;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.GenericTest;

/**
 * This class takes a {@link Test}, executes it and checks that the correct set of failure and error
 * messages is created. This is necessary for meta-tests: Tests that check if other tests fail at
 * the right spots.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class FailureMatchingTestWrapper extends GenericTest {

	private final Test _test;

	private final Pattern _expectedMessage;

	private final boolean _failure;

	/**
	 * Creates a new {@link FailureMatchingTestWrapper}.
	 */
	public FailureMatchingTestWrapper(Test test, Pattern expectedMessages, boolean error) {
		this(testName(test), test, expectedMessages, error);
	}

	private static String testName(Test test) {
		if (test instanceof TestCase) {
			return ((TestCase) test).getName();
		}
		return BasicTestCase.sanitizeForEclipse(test.toString());
	}

	/**
	 * Creates a new {@link FailureMatchingTestWrapper}.
	 */
	public FailureMatchingTestWrapper(String testName, Test test, Pattern expectedMessages, boolean error) {
		super(testName);
		if (test.countTestCases() != 1) {
			throw buildExceptionNotSingleTest();
		}
		_test = test;
		_expectedMessage = expectedMessages;
		_failure = !error;
	}

	private IllegalArgumentException buildExceptionNotSingleTest() {
		return new IllegalArgumentException("This class can only handle single test cases, but this test has "
			+ _test.countTestCases() + " test cases!");
	}

	@Override
	protected void executeTest() throws Throwable {
		TestResult result = new TestResult();
		_test.run(result);
		Enumeration<?> failures = result.failures();
		if (!failures.hasMoreElements()) {
			throw buildExceptionTestDidNotFail();
		}
		TestFailure failure = (TestFailure) failures.nextElement();
		String actualMessage = failure.exceptionMessage();
		if (!_expectedMessage.matcher(actualMessage).matches()) {
			throw buildExceptionWrongMessage(actualMessage);
		}
		if (failure.isFailure() != _failure) {
			throw buildExceptionWrongProblemType(failure);
		}
	}

	private AssertionFailedError buildExceptionTestDidNotFail() {
		return new AssertionFailedError("Expected the underlying test to fail, but it succeeded!"
			+ " Expected an error message matched by the following RegExp: " + _expectedMessage);
	}

	private AssertionFailedError buildExceptionWrongProblemType(TestFailure failure) {
		return new AssertionFailedError("Expected the underlying test to result in " + problemType(_failure)
			+ ", but it was an " + problemType(failure.isFailure()) + ".");
	}

	private AssertionFailedError buildExceptionWrongMessage(String actualMessage) {
		return new AssertionFailedError(
			"Expected the underlying test to fail with an error matched by the following RegExp: >>>"
				+ _expectedMessage + "<<< But it failed with: >>>" + actualMessage + "<<<");
	}

	private String problemType(boolean failure) {
		return failure ? "a failure" : "an error";
	}

}