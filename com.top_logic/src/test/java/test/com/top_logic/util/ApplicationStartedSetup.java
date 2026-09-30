/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.NestableTestSetup;
import test.com.top_logic.basic.ReflectionUtils;

import com.top_logic.basic.col.MutableInteger;
import com.top_logic.util.ApplicationStartup;
import com.top_logic.util.sched.Scheduler;

/**
 * {@link NestableTestSetup} that runs its test in an application that has started, i.e. with a
 * completed {@link ApplicationStartup}.
 *
 * <p>
 * A test does not boot an application, so the {@link ApplicationStartup} does not complete, and
 * services waiting for it, e.g. the dispatch of the {@link Scheduler}, do not become active. A test
 * depending on such a service wraps itself into this setup. The setup completes the
 * {@link ApplicationStartup} and restores its former state afterwards.
 * </p>
 */
public class ApplicationStartedSetup extends NestableTestSetup {

	/** Name of the method of {@link ApplicationStartup} that resets it to not started. */
	private static final String METHOD_BEGIN = "begin";

	/** Name of the method of {@link ApplicationStartup} that completes it. */
	private static final String METHOD_COMPLETE = "complete";

	private static final MutableInteger SETUP_CNT = new MutableInteger();

	private boolean _wasStarted;

	/**
	 * Creates a {@link ApplicationStartedSetup}.
	 *
	 * @param test
	 *        The test to run in the started application.
	 */
	public ApplicationStartedSetup(Test test) {
		super(test, SETUP_CNT);
	}

	/**
	 * Creates a {@link ApplicationStartedSetup} for all tests of the given class.
	 */
	public static Test setup(Class<? extends Test> testClass) {
		return new ApplicationStartedSetup(new TestSuite(testClass));
	}

	@Override
	protected void doSetUp() throws Exception {
		_wasStarted = ApplicationStartup.getInstance().isStarted();
		completeStartup();
	}

	@Override
	protected void doTearDown() throws Exception {
		if (!_wasStarted) {
			resetStartup();
		}
	}

	/**
	 * Completes the {@link ApplicationStartup}, as the application does, when it has started.
	 *
	 * <p>
	 * Runs all actions waiting for the start of the application.
	 * </p>
	 */
	public static void completeStartup() {
		invoke(METHOD_COMPLETE);
	}

	/**
	 * Resets the {@link ApplicationStartup} to not started, as the application does, when it boots.
	 */
	public static void resetStartup() {
		invoke(METHOD_BEGIN);
	}

	private static void invoke(String method) {
		ReflectionUtils.executeMethod(ApplicationStartup.getInstance(), method, new Class<?>[0], new Object[0]);
	}

}
