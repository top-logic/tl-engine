/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.ComputationEx;

/**
 * {@link ThreadContextDecorator} is a {@link TestSetupDecorator} which sets the
 * {@link ThreadContext} for its decorated setup.
 * 
 * @see ThreadContextSetup
 * @see RearrangableThreadContextSetup
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ThreadContextDecorator implements TestSetupDecorator {
	
	/**
	 * Singleton {@link ThreadContextDecorator} instance.
	 */
	public static final ThreadContextDecorator INSTANCE = new ThreadContextDecorator();

	private ThreadContextDecorator() {
		// Singleton constructor.
	}

	@Override
	public void setup(final SetupAction innerSetup) throws Exception {
		ThreadContext.inSystemContext(ThreadContextDecorator.class, new ComputationEx<Void, Exception>() {
			@Override
			public Void run() throws Exception {
				innerSetup.setUpDecorated();
				return null;
			}
		});
	}

	@Override
	public void tearDown(final SetupAction innerSetup) throws Exception {
		ThreadContext.inSystemContext(ThreadContextDecorator.class, new ComputationEx<Void, Exception>() {
			@Override
			public Void run() throws Exception {
				innerSetup.tearDownDecorated();
				return null;
			}
		});
	}

}
