/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import com.top_logic.basic.thread.ThreadContext;

/**
 * A {@link SystemContextThread} is a {@link Thread} which ensures that a
 * {@link ThreadContext} is present during its {@link Thread#run()} method.
 * 
 * <p>
 * The usage of a {@link SystemContextThread} is basically the same as of a
 * {@link Thread}, but subclasses has to override
 * {@link SystemContextThread#internalRun()} instead of
 * {@link SystemContextThread#run()}. Instead the {@link SystemContextThread}
 * can be build with some arbitrary {@link Runnable}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SystemContextThread extends Thread {

	public SystemContextThread() {
		super();
	}

	public SystemContextThread(Runnable target, String name) {
		super(target, name);
	}

	public SystemContextThread(Runnable target) {
		super(target);
	}

	public SystemContextThread(String name) {
		super(name);
	}

	public SystemContextThread(ThreadGroup group, Runnable target, String name, long stackSize) {
		super(group, target, name, stackSize);
	}

	public SystemContextThread(ThreadGroup group, Runnable target, String name) {
		super(group, target, name);
	}

	public SystemContextThread(ThreadGroup group, Runnable target) {
		super(group, target);
	}

	public SystemContextThread(ThreadGroup group, String name) {
		super(group, name);
	}

	@Override
	public final void run() {
		ThreadContext.inSystemContext(SystemContextThread.class, new Computation<Object>() {

			@Override
			public Object run() {
				internalRun();
				return null;
			}
		});
	}

	/**
	 * This method basically is called inside of
	 * {@link SystemContextThread#run()} and does the actual work of this
	 * {@link Thread}. It behaves like {@link Thread#run()} do in a
	 * &quot;normal&quot; {@link Thread}.
	 * 
	 * @see Thread#run()
	 */
	protected void internalRun() {
		// this side call is necessary to execute the Runnable of this Thread if
		// one was set.
		super.run();
	}

}
