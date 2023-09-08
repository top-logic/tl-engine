/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import junit.extensions.TestSetup;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestListener;
import junit.framework.TestResult;
import junit.framework.TestSuite;

/**
 * This {@link SetupSuite} will inform the {@link SetupSuite.SetupCallback} about set up
 * and tear down of the tests added to this {@link TestSuite}. in contrast to a
 * {@link TestSetup}, the {@link SetupSuite} therefore has the possibility to
 * adapt each test separately via the given {@link SetupSuite.SetupCallback}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class SetupSuite extends TestSuite {

	/**
	 * {@link SetupSuite.SetupCallback}s will be informed about set up and tear
	 * down of the tests added to the {@link SetupSuite} the
	 * {@link SetupSuite.SetupCallback} is delivered.
	 */
	public interface SetupCallback {

		/**
		 * This method will be called before the given test is started,
		 * especially if the given test is a {@link TestCase} this method will
		 * be called before {@link TestCase#setUp()} of the test is called.
		 * 
		 * @param test
		 *        the {@link Test} which will start as next
		 */
		void setUp(Test test);

		/**
		 * This method is called after the given test has ended, especially if
		 * the given test is a {@link TestCase}, this method will be called
		 * after {@link TestCase#tearDown()} of the test has been called.
		 * 
		 * @param test
		 *        the {@link Test} which was currently ended
		 */
		void tearDown(Test test);

	}

	final SetupCallback callback;

	public SetupSuite(Class clazz, SetupCallback callback) {
		super(clazz);
		this.callback = callback;
	}

	public SetupSuite(Class clazz, String name, SetupCallback callback) {
		super(clazz, name);
		this.callback = callback;
	}

	@Override
	public void run(final TestResult result) {
		TestListener listener = new TestListener() {

			@Override
			public void startTest(Test test) {
				try {
					SetupSuite.this.callback.setUp(test);
				} catch (Throwable t) {
					result.addError(test, t);
				}
			}

			@Override
			public void endTest(Test test) {
				try {
					SetupSuite.this.callback.tearDown(test);
				} catch (Throwable t) {
					result.addError(test, t);
				}
			}

			@Override
			public void addFailure(Test test, AssertionFailedError t) {
			}

			@Override
			public void addError(Test test, Throwable t) {
			}
		};

		result.addListener(listener);
		super.run(result);
		result.removeListener(listener);
	}
}
