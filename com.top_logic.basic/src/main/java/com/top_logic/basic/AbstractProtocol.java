/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import com.top_logic.basic.thread.StackTrace;

/**
 * Common base class for {@link Protocol} implementations that ensures
 * conformance to the specification.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractProtocol extends ProtocolChain {

	private boolean hasError;

	private Throwable firstProblem;
	
	/**
	 * Creates a {@link AbstractProtocol}.
	 */
	public AbstractProtocol() {
		this(NoProtocol.INSTANCE);
	}

	/**
	 * Creates a {@link AbstractProtocol}.
	 * 
	 * @param chain
	 *        A {@link Protocol} that receives a all non-fatal events that this
	 *        instance receives.
	 * 
	 * @see #info(String)
	 * @see #info(String, int)
	 * @see #error(String)
	 * @see #error(String, Throwable)
	 * 
	 * @since TL_5_6_1
	 */
	public AbstractProtocol(Protocol chain) {
		super(chain);
	}

	@Override
	public final void localInfo(String message) {
		localInfo(message, INFO);
	}
	
	@Override
	public final void localError(String message) {
		localError(message, null);
	}

	/**
	 * Create an exception as stack trace for an error that does not have an {@link Exception} as
	 * cause.
	 * 
	 * @param message
	 *        The given error message.
	 * 
	 * @return An exception or <code>null</code>, if no stack trace should be produced.
	 * 
	 * @since 5.7.3
	 */
	protected Exception makeStackTrace(String message) {
		return new StackTrace(message);
	}
	
	@Override
	protected final void localError(String message, Throwable ex) {
		if (ex == null && firstProblem == null) {
			ex = makeStackTrace(message);
		}
		setError(ex);
		reportError(message, ex);
	}

	/**
	 * Callback for providing user feedback about the given problem description.
	 */
	protected abstract void reportError(String message, Throwable ex);

	@Override
	public final RuntimeException fatal(String message) {
		return fatal(message, null);
	}

	@Override
	public final RuntimeException fatal(String message, Throwable ex) {
		setError(ex);
		throw reportFatal(message, ex);
	}

	/**
	 * Callback for providing user feedback about the given problem description
	 * and creating an implementation defined runtime exception that is used to
	 * terminate the service.
	 */
	protected abstract RuntimeException reportFatal(String message, Throwable ex);

	@Override
	public final void checkErrors() {
		if (hasError) {
			throw createAbort();
		}
	}

	/**
	 * Callback for creating an implementation defined runtime exception that is used to
	 * terminate the service in case any errors have occurred.
	 * 
	 * <p>
	 * The implementation is not expected to throw the returned exception.
	 * </p>
	 */
	protected abstract RuntimeException createAbort();

	@Override
	public final boolean hasErrors() {
		return hasError;
	}
	
	/**
	 * Flags this {@link Protocol} with an error.
	 * 
	 * @see #hasErrors()
	 */
	protected void setError(Throwable ex) {
		hasError = true;
		if (firstProblem == null) {
			firstProblem = ex;
		}
	}
	
	@Override
	public final Throwable getFirstProblem() {
		return this.firstProblem;
	}

}
