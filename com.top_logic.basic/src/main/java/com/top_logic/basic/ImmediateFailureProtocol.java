/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;


/**
 * {@link Protocol} that immediately fails with an error instead of collecting messages and waiting
 * for {@link #checkErrors()} being called.
 * 
 * <p>
 * Implementations of this class can be used as singletons.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ImmediateFailureProtocol implements Protocol {

	@Override
	public final void info(String message) {
		// Ignore.
	}

	@Override
	public void info(String message, int verbosityLevel) {
		// Ignore.
	}

	@Override
	public final void error(String message) {
		error(message, null);
	}

	@Override
	public void error(String message, Throwable ex) {
		throw fatal(message, ex);
	}

	@Override
	public final RuntimeException fatal(String message) {
		return fatal(message, null);
	}

	@Override
	public RuntimeException fatal(String message, Throwable ex) {
		throw createFailure(message, ex);
	}

	/**
	 * Create the failure, that aborts the process.
	 * 
	 * @param message
	 *        The exception message.
	 * @param ex
	 *        The exception cause.
	 * @return The created exception instance.
	 */
	protected abstract RuntimeException createFailure(String message, Throwable ex);

	@Override
	public final boolean hasErrors() {
		return false;
	}

	@Override
	public final void checkErrors() {
		// Ignore.
	}

	@Override
	public final Throwable getFirstProblem() {
		return null;
	}
}