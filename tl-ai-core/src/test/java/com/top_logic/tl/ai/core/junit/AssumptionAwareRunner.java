/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.junit;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Runner for JUnit-3 style {@link TestCase}s that reports a violated {@link Assume assumption} as a
 * <b>skipped</b> test instead of an error.
 *
 * <p>
 * JUnit 3 has no notion of a skipped test: {@code junit.framework.TestResult} knows only failures
 * and errors, and the default {@link JUnit38ClassRunner} therefore turns the
 * {@link AssumptionViolatedException} thrown by {@link Assume} into a test failure. The only way a
 * {@link TestCase} could opt out of running was to {@code return} early - which surefire counts as
 * a <em>passed</em> test, so a suite that ran nothing still reported green.
 * </p>
 *
 * <p>
 * This runner closes that gap. It runs the test class exactly like {@link JUnit38ClassRunner} does,
 * but passes the notifier through a filter that re-routes an {@link AssumptionViolatedException}
 * from {@link RunNotifier#fireTestFailure(Failure)} to
 * {@link RunNotifier#fireTestAssumptionFailed(Failure)}. Surefire reports the latter as skipped,
 * with the assumption's message as the reason. Use it on any {@link TestCase} whose methods are
 * conditional on an environment the build does not always provide:
 * </p>
 *
 * <pre>
 * &#64;RunWith(AssumptionAwareRunner.class)
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
 * {@code @RunWith} takes precedence over both the {@code suite()} method and the plain JUnit-3
 * fallback, so nothing else about the test class has to change. A test class that declares a static
 * {@code suite()} keeps it: this runner looks the method up the way {@link SuiteMethod} does and
 * runs what it returns, so a suite wrapped in a {@code TestSetup} decorator - the JUnit-3 way of
 * booting the module system around a test class - still gets its setup. Filtering and ordering -
 * and with them surefire's {@code -Dtest=TestSomething#testNeedingAChromium} - keep working,
 * because they are inherited unchanged.
 * </p>
 */
public class AssumptionAwareRunner extends JUnit38ClassRunner {

	/**
	 * Creates a {@link AssumptionAwareRunner}.
	 *
	 * @param testClass
	 *        The {@link TestCase} class to run.
	 */
	public AssumptionAwareRunner(Class<?> testClass) throws Throwable {
		super(testFrom(testClass));
	}

	/**
	 * What the given class asks to be run: the result of its static {@code suite()} method if it
	 * declares one, otherwise all of its test methods.
	 *
	 * <p>
	 * {@link JUnit38ClassRunner#JUnit38ClassRunner(Class)} always builds a plain {@link TestSuite}
	 * from the class and so silently ignores {@code suite()}. That is fine for a test class that
	 * has none, but it would drop the {@code TestSetup} decorator of a class that wraps its suite
	 * to boot the module system.
	 * </p>
	 */
	private static Test testFrom(Class<?> testClass) throws Throwable {
		Method suiteMethod;
		try {
			suiteMethod = testClass.getMethod("suite");
		} catch (NoSuchMethodException ex) {
			return new TestSuite(testClass.asSubclass(TestCase.class));
		}
		if (!Modifier.isStatic(suiteMethod.getModifiers())) {
			throw new IllegalArgumentException(testClass.getName() + ".suite() must be static");
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
