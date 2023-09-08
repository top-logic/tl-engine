/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link Protocol} implementation that reports errors and informations also to an inner
 * {@link Protocol}. All other methods (i.e. the terminating methods) must be implemented.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ProtocolChain extends ProtocolAdaptor {

	/**
	 * Creates a new {@link ProtocolChain}.
	 * 
	 * @param chain
	 *        {@link Protocol} to delegate the non terminating methods to.
	 */
	public ProtocolChain(Protocol chain) {
		super(chain);
	}

	/**
	 * Checks whether this protocol has errors (not the chain).
	 * 
	 * @see com.top_logic.basic.ProtocolAdaptor#checkErrors()
	 */
	@Override
	public abstract void checkErrors();

	/**
	 * Returns the first problem of this protocol (not that of the chain).
	 * 
	 * @see com.top_logic.basic.ProtocolAdaptor#getFirstProblem()
	 */
	@Override
	public abstract Throwable getFirstProblem();

	/**
	 * Reports whether this protocol (not the chain) has errors.
	 * 
	 * @see com.top_logic.basic.ProtocolAdaptor#hasErrors()
	 */
	@Override
	public abstract boolean hasErrors();

	@Override
	public abstract RuntimeException fatal(String message);

	@Override
	public abstract RuntimeException fatal(String message, Throwable ex);

	@Override
	public final void info(String message) {
		localInfo(message);
		super.info(message);
	}

	/**
	 * Implementation local version of {@link #info(String)}.
	 * 
	 * @param message
	 *        see {@link #info(String)}.
	 */
	protected void localInfo(String message) {
		// no special local Implementation
	}

	@Override
	public final void info(String message, int verbosityLevel) {
		localInfo(message, verbosityLevel);
		super.info(message, verbosityLevel);
	}

	/**
	 * Implementation local version of {@link #info(String, int)}.
	 * 
	 * @param message
	 *        see {@link #info(String, int)}.
	 * @param verbosityLevel
	 *        see {@link #info(String, int)}
	 */
	protected void localInfo(String message, int verbosityLevel) {
		// no special local Implementation
	}

	@Override
	public final void error(String message) {
		localError(message);
		super.error(message);
	}

	/**
	 * Implementation local version of {@link #error(String)}.
	 * 
	 * @param message
	 *        see {@link #error(String)}
	 */
	protected void localError(String message) {
		// no special local Implementation
	}

	@Override
	public final void error(String message, Throwable ex) {
		localError(message, ex);
		super.error(message, ex);
	}

	/**
	 * Implementation local version of {@link #error(String, Throwable)}.
	 * 
	 * @param message
	 *        see {@link #error(String, Throwable)}
	 * @param ex
	 *        see {@link #error(String, Throwable)}
	 */
	protected void localError(String message, Throwable ex) {
		// no special local Implementation
	}
}


