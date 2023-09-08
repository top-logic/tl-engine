/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Abstract implementation of {@link Log} delegating all methods to a different {@link Log}
 * implementation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class LogAdaptor implements Log {

	@Override
	public void error(String message) {
		impl().error(message);
	}

	@Override
	public void error(String message, Throwable ex) {
		impl().error(message, ex);
	}

	@Override
	public Throwable getFirstProblem() {
		return impl().getFirstProblem();
	}

	@Override
	public boolean hasErrors() {
		return impl().hasErrors();
	}

	@Override
	public void info(String message) {
		impl().info(message);
	}

	@Override
	public void info(String message, int verbosityLevel) {
		impl().info(message, verbosityLevel);
	}

	/**
	 * Provides access to the actual {@link Log} implementation.
	 */
	protected abstract Log impl();

}


