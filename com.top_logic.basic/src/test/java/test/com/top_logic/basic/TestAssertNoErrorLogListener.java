/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.Logger;
import com.top_logic.basic.NamedConstant;

/**
 * {@link TestCase} for: {@link AssertNoErrorLogListener}
 * 
 * @author     <a href="mailto:Jan.Stolzenburg@top-logic.com">Jan.Stolzenburg</a>
 */
@SuppressWarnings("static-access")
public class TestAssertNoErrorLogListener extends TestCase {

	private static final String TESTED_CLASS = AssertNoErrorLogListener.class.getSimpleName();
	
	private static final Object CALLER = new NamedConstant("caller(" + TestAssertNoErrorLogListener.class.getCanonicalName() + ")");
	
	private static final String STANDARD_LOG_MESSAGE =
		"This is a test message. It does not indicate an error or anything. It is used to test the class: " + TESTED_CLASS;
	
	public static Test suite () {
		return BasicTestSetup.createBasicTestSetup(new TestSuite (TestAssertNoErrorLogListener.class));
	}

	public void testAssertNoErrorLogged() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		Logger.error(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (Throwable expectedException) {
			// Correct
			return;
		}
		fail("The " + TESTED_CLASS + " did NOT throw an exception after an error was logged and it was asserted that NO error was logged.");
	}
	
	public void testThrowsAssertionFailedError() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		Logger.error(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError expectedException) {
			// Correct
		} catch (Throwable unexpectedException) {
			fail("The " + TESTED_CLASS
				+ " threw the wrong exception type after an error was logged and it was asserted that no error was logged. Expected: "
				+ AssertionFailedError.class + "; Actual: " + unexpectedException.getClass()
				+ "; Message of that exception: " + unexpectedException.getMessage());
		}
	}
	
	public void testInfoLogged() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		Logger.info(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError unexpectedException) {
			fail("The " + TESTED_CLASS + " threw an exception after an INFO was logged and it was asserted that no ERROR was logged.");
		}
	}
	
	public void testWarnLoggedAndCountsAsError() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		Logger.warn(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError expectedException) {
			// Correct
			return;
		}
		fail("The " + TESTED_CLASS + " did NOT throw an exception after a WARNING was logged and it was asserted that NO error was logged and WARNINGS should count AS ERRORS.");
	}
	
	public void testDefaultConstructorMeansWarningsCountAsErrors() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener();
		Logger.warn(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError expectedException) {
			// Correct
			return;
		}
		fail("The parameterless constructor of class " + TESTED_CLASS + " should mean: 'Treat warnings as errors, too.' But thats not the case!");
	}
	
	public void testWarnLoggedAndCountsNotAsError() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(false);
		Logger.warn(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError unexpectedException) {
			fail("The " + TESTED_CLASS + " threw an exception after a WARNING was logged and it was asserted that no ERROR was logged and WARNINGS should NOT count AS ERRORS.");
		}
	}
	
	public void testAssertNoErrorLoggedTwice1() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		logListener.assertNoErrorLogged("");
		Logger.error(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError expectedException) {
			// Correct
			return;
		}
		fail("The " + TESTED_CLASS + " did not throw an exception after an error was logged and it was asserted that no error was logged, when that was asserted successfully before.");
	}
	
	public void testAssertNoErrorLoggedTwice2() {
		AssertNoErrorLogListener logListener = new AssertNoErrorLogListener(true);
		Logger.error(STANDARD_LOG_MESSAGE, CALLER);
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError expectedException) {
			// Correct
		}
		try {
			logListener.assertNoErrorLogged("");
		} catch (AssertionFailedError unexpectedException) {
			fail("The " + TESTED_CLASS + " threw an exception when no error was logged and it was asserted that no error was logged, when that assertion failed before.");
		}
	}
	
}
