/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;

import com.top_logic.basic.col.Maybe;

/**
 * {@link TestSetup} that enhances the messages written to {@link System#out} and {@link System#err}
 * .
 * 
 * <p>
 * For each test the messages written by the test are catched and modified such that the name of the
 * test is also contained. E.g. if the {@link TestCase test} "testFoo" in class
 * "test.com.top_logic.Bar" writes message <blockquote><code>did something</code></blockquote> the
 * message is modified to <blockquote>
 * <code>'Start test.com.top_logic.Bar#testFoo'did something'End test.com.top_logic.Bar#testFoo'</code>
 * </blockquote>
 * </p>
 * 
 * <p>
 * Only one {@link LoggingTestSetup} may run at a time, and it must be the outermost
 * {@link TestSetup}. While it runs, it is available through {@link #getDecorator()}.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class LoggingTestSetup extends TestSetup implements TestListener, TestSetupDecorator {

	/**
	 * The currently running {@link LoggingTestSetup}, or {@code null} if none is running.
	 * 
	 * <p>
	 * Set in {@link #setUp()} when {@link System#out} and {@link System#err} are redirected, cleared
	 * in {@link #tearDown()} after they are restored.
	 * </p>
	 */
	private static LoggingTestSetup active;

	private final ByteArrayOutputStream _actualOut = new ByteArrayOutputStream();

	private final PrintStream _out = new PrintStream(_actualOut, true);

	private final ByteArrayOutputStream _actualErr = new ByteArrayOutputStream();

	private final PrintStream _err = new PrintStream(_actualErr, true);

	private PrintStream _origOut;

	private PrintStream _origErr;

	@Override
	public void addError(Test test, Throwable t) {
		// Don't care about errors or failures
	}

	@Override
	public void addFailure(Test test, AssertionFailedError t) {
		// Don't care about errors or failures
	}

	@Override
	public void endTest(Test test) {
		flushContent(test, "Run");
	}

	private void flushContent(Test test, String phase) {
		copy(_actualOut, _origOut, test, phase);
		copy(_actualErr, _origErr, test, phase);
	}

	private void copy(ByteArrayOutputStream in, PrintStream out, Test test, String phase) {
		if (in.size() == 0) {
			return;
		}
		String testName = getTestName(test);

		out.print('\'');
		out.print("Start: ");
		out.print(phase);
		out.print(": ");
		out.print(testName);
		out.print('\'');
		out.println();
		flush(in, out);
		out.print('\'');
		out.print("End: ");
		out.print(phase);
		out.print(": ");
		out.print(testName);
		out.print('\'');
		out.println();
	}

	private void flush(ByteArrayOutputStream actual, PrintStream orig) {
		try {
			if (orig != null) {
				actual.writeTo(orig);
			}
		} catch (IOException ex) {
			throw new RuntimeException("Unable to copy written content '" + actual.toString()
				+ "' to orginal stream.", ex);
		}
		actual.reset();
	}

	private String getTestName(Test test) {
		if (test instanceof TestCase) {
			StringBuilder b = new StringBuilder(test.getClass().getName());
			b.append('#');
			b.append(((TestCase) test).getName());
			return b.toString();
		}
		if (test instanceof NamedTestDecorator) {
			// name also contains the class
			return ((NamedTestDecorator) test).getName();
		}
		return test.getClass().getName();
	}

	@Override
	public void startTest(Test test) {
		flushStreams();
	}

	private void flushStreams() {
		flush(_actualErr, _origErr);
		flush(_actualOut, _origOut);
	}

	/**
	 * Returns a {@link TestSetupDecorator} that executes the decorated actions by modifying the
	 * messages written in {@link TestSetupDecorator.SetupAction#setUpDecorated()} and
	 * {@link TestSetupDecorator.SetupAction#tearDownDecorated()} in a similar manor as
	 * {@link LoggingTestSetup}.
	 * 
	 * @return The currently running {@link LoggingTestSetup}, or {@link Maybe#none()} if none is
	 *         running.
	 */
	public static Maybe<TestSetupDecorator> getDecorator() {
		LoggingTestSetup current = active;
		if (current == null) {
			return Maybe.none();
		}
		return Maybe.<TestSetupDecorator> some(current);
	}

	@Override
	public void setup(SetupAction innerSetup) throws Exception {
		DecoratedTestSetup test = innerSetup.decoratedTest();
		flushStreams();
		innerSetup.setUpDecorated();
		flushContent(test, "Setup");
	}

	@Override
	public void tearDown(SetupAction innerSetup) throws Exception {
		DecoratedTestSetup test = innerSetup.decoratedTest();
		flushStreams();
		innerSetup.tearDownDecorated();
		flushContent(test, "Teardown");
	}

	/**
	 * Creates a {@link LoggingTestSetup} decorating the given test.
	 * 
	 * <p>
	 * Each call creates a separate instance. The instance becomes available through
	 * {@link #getDecorator()} only while it runs.
	 * </p>
	 */
	public static LoggingTestSetup newLoggingTestSetup(Test test) {
		return new LoggingTestSetup(test);
	}

	private LoggingTestSetup(Test test) {
		super(test);
	}

	@Override
	protected void setUp() throws Exception {
		_origOut = System.out;
		System.setOut(_out);
		_origErr = System.err;
		System.setErr(_err);
		active = this;
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			flushStreams();
		} finally {
			System.setOut(_origOut);
			System.setErr(_origErr);
			active = null;
		}
		super.tearDown();
	}

	@Override
	public void run(TestResult result) {
		result.addListener(this);
		super.run(result);
		result.removeListener(this);
	}

}
