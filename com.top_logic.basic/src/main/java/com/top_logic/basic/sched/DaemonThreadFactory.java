/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.sched;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * {@link ThreadFactory} that switches threads created by a given implementation to
 * {@link Thread#isDaemon() daemon thread}s.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DaemonThreadFactory implements ThreadFactory {

	/**
	 * Creates a {@link ThreadFactory} that switches threads created by a given implementation to
	 * {@link Thread#isDaemon() daemon thread}s.
	 * 
	 * @param impl
	 *        The actual {@link ThreadFactory} creating the threads.
	 */
	public static ThreadFactory daemonThreadFactory(ThreadFactory impl) {
		return new DaemonThreadFactory(impl);
	}

	/**
	 * /** Creates a {@link ThreadFactory} that switches threads created by a
	 * {@link Executors#defaultThreadFactory() default ThreadFactory} {@link Thread#isDaemon()
	 * daemon thread}s.
	 * 
	 * @see Executors#defaultThreadFactory()
	 */
	public static ThreadFactory daemonThreadFactory() {
		return daemonThreadFactory(Executors.defaultThreadFactory());
	}

	private final ThreadFactory _impl;

	/**
	 * Creates a new {@link DaemonThreadFactory}.
	 * 
	 * @param impl
	 *        {@link ThreadFactory} which actually produces the result threads.
	 */
	private DaemonThreadFactory(ThreadFactory impl) {
		_impl = impl;
	}

	@Override
	public Thread newThread(Runnable r) {
		Thread thread = _impl.newThread(r);
		if (!thread.isDaemon()) {
			thread.setDaemon(true);
		}
		return thread;
	}

}

