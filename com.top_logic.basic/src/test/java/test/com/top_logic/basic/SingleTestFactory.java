/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * {@link TestFactory} that uses a selection of test method from a {@link Test} to create test.
 * 
 * <p>
 * This class is useful in debugging test cases, if only a single (failing) test case from a test
 * class should be executed.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SingleTestFactory implements TestFactory {
	
	private final Pattern _testPattern;

	/**
	 * Creates a {@link SingleTestFactory}.
	 * 
	 * @param testName
	 *        The name of the test case to execute.
	 */
	public SingleTestFactory(String testName) {
		this(Pattern.compile(Pattern.quote(testName)));
	}

	/**
	 * Creates a {@link SingleTestFactory}.
	 * 
	 * @param testPattern
	 *        The pattern matching names of the test cases to execute.
	 */
	public SingleTestFactory(Pattern testPattern) {
		this._testPattern = testPattern;
	}

	@Override
	public Test createSuite(Class<? extends TestCase> testCase, String suiteName) {
		try {
			return create(testCase, suiteName);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	private Test create(Class<? extends Test> testCase, String suiteName) {
		Method[] allMethods = testCase.getMethods();
		Test test = null;
		for (Method m : allMethods) {
			String methodName = m.getName();
			if (_testPattern.matcher(methodName).matches()) {
				Test t = TestSuite.createTest(testCase, methodName);
				if (test == null) {
					test = t;
				} else if (test instanceof TestSuite) {
					((TestSuite) test).addTest(t);
				} else {
					TestSuite suite = new TestSuite(suiteName);
					suite.addTest(test);
					suite.addTest(t);
					test = suite;
				}
			}
		}
		if (test == null) {
			return SimpleTestFactory.newBrokenTest(true, "No test matching pattern: " + _testPattern.pattern(),
				"No such test");
		}
		return test;
	}

}
