/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

/**
 * The class {@link RunnableComputation} dispatches its {@link #run()} method to
 * a runnable. This class can be used if a {@link Thread} is present but a
 * {@link Computation} is needed.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RunnableComputation implements Computation<Void> {

	private final Runnable runnable;

	/**
	 * Creates a {@link RunnableComputation}.
	 *
	 * @param runnable
	 *        The {@link Runnable} to run in the {@link Computation}'s {@link #run()} method.
	 */
	public RunnableComputation(Runnable runnable) {
		super();
		this.runnable = runnable;
	}

	/**
	 * Dispatches to its {@link Runnable}; constantly return <code>null</code>
	 * 
	 * @see com.top_logic.basic.util.Computation#run()
	 */
	@Override
	public Void run() {
		runnable.run();
		return null;
	}

}
