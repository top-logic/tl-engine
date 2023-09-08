/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import com.top_logic.basic.thread.ThreadContext;

/**
 * Ensures that the wrapped {@link Runnable} can use a {@link ThreadContext} during its
 * {@link Runnable#run()} method.
 * 
 * @see ThreadContext#inSystemContext(Class, ComputationEx2)
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SystemContextRunnable<T extends Runnable> extends RunnableProxy<T> {

	/** Creates a {@link SystemContextRunnable} with the given inner {@link Runnable}. */
	public SystemContextRunnable(T target) {
		super(target);
	}

	/**
	 * Installs a {@link ThreadContext} and executes the wrapped
	 * {@link Runnable}.
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public final void run() {
		ThreadContext.inSystemContext(SystemContextRunnable.class, new RunnableComputation(getInner()));
	}

}
