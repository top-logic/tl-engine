/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.util.AbstractBasicTestAll;

/**
 * Runner for JUnit-3 style tests that reports a violated {@link Assume assumption} as a
 * <b>skipped</b> test instead of an error.
 *
 * <p>
 * JUnit 3 has no notion of a skipped test: {@link junit.framework.TestResult} knows only failures
 * and errors, and the default {@link JUnit38ClassRunner} therefore turns the
 * {@link AssumptionViolatedException} thrown by {@link Assume} into a test failure.
 * </p>
 *
 * <p>
 * This runner runs the test class exactly like {@link JUnit38ClassRunner} does, but passes the
 * notifier through a filter that re-routes an {@link AssumptionViolatedException} from
 * {@link RunNotifier#fireTestFailure(Failure)} to {@link RunNotifier#fireTestAssumptionFailed(Failure)}.
 * Surefire reports the latter as skipped, with the assumption's message as the reason.
 * </p>
 *
 * <p>
 * {@link AbstractBasicTestAll} is annotated with this runner. Since {@link RunWith} is inherited,
 * every <code>TestAll</code> runs with it, and a test collected by the <code>TestAll</code> may
 * call {@link Assume} to declare a precondition the build does not always provide (a network, a
 * browser, an API key):
 * </p>
 *
 * <pre>
 * public class TestSomething extends TestCase {
 *
 *     public void testNeedingAChromium() {
 *         Assume.assumeTrue("no Chromium here", chromiumAvailable());
 *         ...
 *     }
 * }
 * </pre>
 *
 * <p>
 * A test class that declares a static <code>suite()</code> method is run with the {@link Test}
 * it returns, the way {@link SuiteMethod} does, so a suite wrapped in a setup decorator keeps its
 * setup.
 * </p>
 */
public class AssumptionAwareRunner extends JUnit38ClassRunner {

	private static final String SUITE_METHOD_NAME = "suite";

	/**
	 * Creates a {@link AssumptionAwareRunner}.
	 *
	 * @param testClass
	 *        The test class to run, either a class with a static <code>suite()</code> method or
	 *        a {@link TestCase}.
	 */
	public AssumptionAwareRunner(Class<?> testClass) throws Throwable {
		super(testFrom(testClass));
	}

	/**
	 * What the given class asks to be run: the result of its static <code>suite()</code> method
	 * if it declares one, otherwise all of its test methods.
	 */
	private static Test testFrom(Class<?> testClass) throws Throwable {
		Method suiteMethod;
		try {
			suiteMethod = testClass.getMethod(SUITE_METHOD_NAME);
		} catch (NoSuchMethodException ex) {
			return new TestSuite(testClass.asSubclass(TestCase.class));
		}
		if (!Modifier.isStatic(suiteMethod.getModifiers())) {
			throw new IllegalArgumentException(testClass.getName() + "." + SUITE_METHOD_NAME + "() must be static");
		}
		try {
			return (Test) suiteMethod.invoke(null);
		} catch (InvocationTargetException ex) {
			throw ex.getCause();
		}
	}

	@Override
	public void run(RunNotifier notifier) {
		super.run(new AssumptionReportingNotifier(notifier));
	}

	/**
	 * {@link RunNotifier} that forwards everything to another one, except a test failure caused by
	 * a violated assumption, which it reports as a skipped test.
	 */
	private static final class AssumptionReportingNotifier extends RunNotifier {

		private final RunNotifier _target;

		AssumptionReportingNotifier(RunNotifier target) {
			_target = target;
		}

		@Override
		public void fireTestFailure(Failure failure) {
			if (failure.getException() instanceof AssumptionViolatedException) {
				_target.fireTestAssumptionFailed(failure);
			} else {
				_target.fireTestFailure(failure);
			}
		}

		@Override
		public void fireTestStarted(Description description) throws StoppedByUserException {
			_target.fireTestStarted(description);
		}

		@Override
		public void fireTestFinished(Description description) {
			_target.fireTestFinished(description);
		}

		@Override
		public void fireTestAssumptionFailed(Failure failure) {
			_target.fireTestAssumptionFailed(failure);
		}

		@Override
		public void fireTestIgnored(Description description) {
			_target.fireTestIgnored(description);
		}

		@Override
		public void fireTestSuiteStarted(Description description) {
			_target.fireTestSuiteStarted(description);
		}

		@Override
		public void fireTestSuiteFinished(Description description) {
			_target.fireTestSuiteFinished(description);
		}

		@Override
		public void fireTestRunStarted(Description description) {
			_target.fireTestRunStarted(description);
		}

		@Override
		public void fireTestRunFinished(Result result) {
			_target.fireTestRunFinished(result);
		}

		@Override
		public void addListener(RunListener listener) {
			_target.addListener(listener);
		}

		@Override
		public void addFirstListener(RunListener listener) {
			_target.addFirstListener(listener);
		}

		@Override
		public void removeListener(RunListener listener) {
			_target.removeListener(listener);
		}

		@Override
		public void pleaseStop() {
			_target.pleaseStop();
		}
	}
}
