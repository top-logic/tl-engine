/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import static com.top_logic.basic.StringServices.*;
import junit.framework.TestCase;


/**
 * This {@link TestCase} listens to the log if any error is logged. If so, the test is marked as failed.
 * More precisely, it checks for an logged error during the {@link #setUp()}, during {@link #tearDown()}
 * and of course during the {@link #runTest()} itself. It does that by overriding {@link #runBare()}
 * and makes that method final therefore.
 * <br/>
 * If not explicitly specified otherwise via the constructor, <b>logged warnings are treated as errors</b>, too.
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class LogListeningTestCase extends TestCase {
	
	private final boolean treatWarningsAsErrors;
	
	private AssertNoErrorLogListener logListener;

	/**
	 * Treats warnings as errors.
	 * 
	 * @see TestCase#TestCase()
	 */
	public LogListeningTestCase() {
		this.treatWarningsAsErrors = true;
	}
	
	/**
	 * @param treatWarningsAsErrors Shall logged warnings count as errors, too?
	 * 
	 * @see TestCase#TestCase()
	 */
	public LogListeningTestCase(boolean treatWarningsAsErrors) {
		this.treatWarningsAsErrors = treatWarningsAsErrors;
	}
	
	/**
	 * Treats warnings as errors.
	 * 
	 * @see TestCase#TestCase(String)
	 */
	public LogListeningTestCase(String name) {
		super(name);
		this.treatWarningsAsErrors = true;
	}
	
	/**
	 * @param treatWarningsAsErrors Shall logged warnings count as errors, too?
	 * 
	 * @see TestCase#TestCase(String)
	 */
	public LogListeningTestCase(String name, boolean treatWarningsAsErrors) {
		super(name);
		this.treatWarningsAsErrors = treatWarningsAsErrors;
	}
	
	@Override
	public final void runBare() throws Throwable {
		logListener = new AssertNoErrorLogListener(treatWarningsAsErrors);
		try {
			runBareWithLogListener();
		} finally {
			/* Prevent memory leak, as JUnit might hold references to this class until the VM shuts
			 * down. And all this time the LogListener would accumulate more logs. */
			AssertNoErrorLogListener tempLogListener = logListener;
			logListener = null;
			/* Nulling out the field before calling 'deactivate', as 'deactivate' is a more
			 * "complex" call and might fail. ("complex" in contrast to 'logListener = null', which
			 * is so simple it never fails.) */
			tempLogListener.deactivate();
		}
	}

	private void runBareWithLogListener() throws Exception, Throwable {
		String messagePrefix = getErrorLoggedMessage();
		messagePrefix = isEmpty(messagePrefix) ? "" : messagePrefix + " ";
		setUp();
		try {
			// Do this inside the try, to ensure the tearDown() is called.
			logListener.assertNoErrorLogged(messagePrefix + "An error was logged during the test setup!");
			// Just in case the setup deactivated it and forgot to reactive it.
			logListener.activate();

			runTest();
			logListener.assertNoErrorLogged(messagePrefix + "An error was logged during the test execution!");
			// Just in case the test deactivated it and forgot to reactive it.
			logListener.activate();
		}
		finally {
			tearDown();
		}
		// Do this outside the finally block. It makes only a difference if the test already failed.
		// And in that case, a failing / error logging tearDown might just be an aftereffect.
		// And if not, the tester will see that tearDown logs an error,
		// after he repaired the test and reran it.
		logListener.assertNoErrorLogged(messagePrefix + "An error was logged during the test teardown!");
	}

	/**
	 * The message that should be prepended, if an error was logged.
	 * <p>
	 * Is appended with a space, if not null. Is not used when {@link #assertNoErrorLogged(String)}
	 * and {@link #assertAnErrorWasLogged(String)} are called, as those have parameters for passing
	 * custom error messages.<br/>
	 * <em>It is unspecified, when this method is called.</em> It might for example be called before
	 * {@link #setUp()} is run and even if no error was logged.
	 * </p>
	 * 
	 * @return Is allowed to be null.
	 */
	protected String getErrorLoggedMessage() {
		return null;
	}

	/**
	 * Redirect to {@link AssertNoErrorLogListener#assertNoErrorLogged(String)}
	 * <p>
	 * You don't have to call this method, if you just want the test to fail on a logged error. The
	 * tests fails automatically when an error is logged. Call this method for example, if the test
	 * should asserts that the error is logged and only fail if the error is not logged.
	 * </p>
	 */
	protected void assertNoErrorLogged(String message) {
		logListener.assertNoErrorLogged(message + " An error was logged during the test execution!");
	}

	/**
	 * If an error was logged and this method was called, another call of this method will fail,
	 * unless a new error was logged. That means, this method will not succeed twice for the same
	 * log entry. <br/>
	 * Depending on the setting given in the constructor, warnings may count as errors or not.
	 */
	protected void assertAnErrorWasLogged(String message) {
		if (!wasErrorLogged()) {
			fail(message + " No error was logged!");
		}
	}

	private boolean wasErrorLogged() {
		return !logListener.getAndClearLogEntries().isEmpty();
	}

}
