/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.junit;

import org.junit.Assume;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.JUnit38ClassRunner;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runner.notification.StoppedByUserException;

import junit.framework.TestCase;

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
 * fallback, so nothing else about the test class has to change. Filtering and ordering - and with
 * them surefire's {@code -Dtest=TestSomething#testNeedingAChromium} - keep working, because they
 * are inherited unchanged.
 * </p>
 */
public class AssumptionAwareRunner extends JUnit38ClassRunner {

	/**
	 * Creates a {@link AssumptionAwareRunner}.
	 *
	 * @param testClass
	 *        The {@link TestCase} class to run.
	 */
	public AssumptionAwareRunner(Class<?> testClass) {
		super(testClass);
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
