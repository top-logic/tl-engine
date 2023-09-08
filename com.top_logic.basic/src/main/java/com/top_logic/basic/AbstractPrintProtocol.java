/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * {@link Protocol} implementation that forwards formatted info and error output
 * to {@link #out(String)} and {@link #err(String)} methods.
 * 
 * @see #out(String) The info output hook.
 * @see #err(String) The error output hook.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractPrintProtocol extends AbstractProtocol {

    /**
     * {@link #verbosity} level that suppresses all but errors.
     * 
     * Private, because one cannot log with level quiet.
     */
	public static final int QUIET = Integer.MIN_VALUE;
    
    /**
     * @see #getVerbosity()
     */
	private int verbosity = Protocol.INFO;

	/**
	 * Creates a {@link AbstractPrintProtocol}.
	 */
	public AbstractPrintProtocol() {
		super();
	}

	/**
	 * Creates a {@link AbstractPrintProtocol}.
	 * 
	 * @param chain
	 *        See {@link AbstractProtocol#AbstractProtocol(Protocol)}.
	 */
	public AbstractPrintProtocol(Protocol chain) {
		super(chain);
	}

	/**
	 * The current level of verbosity.
	 * 
	 * @return One of {@link #QUIET}, {@link Protocol#WARN}, {@link Protocol#INFO},
	 *         {@link Protocol#VERBOSE}, {@link Protocol#DEBUG}.
	 */
	public int getVerbosity() {
		return verbosity;
	}
	
	/**
	 * @see #getVerbosity()
	 */
	public void setVerbosity(int verbosity) {
		switch (verbosity) {
		case QUIET:
		case Protocol.WARN:
		case Protocol.INFO:
		case Protocol.VERBOSE:
		case Protocol.DEBUG: {
			this.verbosity = verbosity;
			break;
		}
		default: {
			throw new IllegalArgumentException("No such verbosity level: " + verbosity);
		}
		}
	}
	
	@Override
	protected Exception makeStackTrace(String message) {
		// Only create stack trace in debug mode.
		
		if (getVerbosity() == Protocol.DEBUG) {
			return super.makeStackTrace(message);
		}
		
		return null;
	}

	@Override
	protected void reportError(String message, Throwable ex) {
		err(format(message, ex));
	}
	
	@Override
	protected RuntimeException reportFatal(String message, Throwable ex) {
		err(format(message, ex));
		return createAbort(message, ex);
	}

	@Override
	protected RuntimeException createAbort() {
		return createAbort("Aborting due to previous errors", getFirstProblem());
	}

	/**
	 * Converts the given {@link Throwable} into a {@link RuntimeException} that can be thrown to
	 * terminate the activity without declaring the exception.
	 * 
	 * @param message
	 *        Description of the problem.
	 * @param cause
	 *        The original problem.
	 * @return The exception to be thrown.
	 */
	protected RuntimeException createAbort(String message, Throwable cause) {
		return new AbortExecutionException(message, cause);
	}
	
	@Override
	public void localInfo(String message, int verbosityLevel) {
		super.localInfo(message, verbosityLevel);
    	if (verbosityLevel <= this.verbosity) {
    		out(format(message, null));
    	}
	}

	/**
	 * Called for each formatted info message.
	 * 
	 * @param message The message to output.
	 */
	protected abstract void out(String message);

	/**
	 * Called for each formatted error message.
	 * 
	 * @param message The message to output.
	 */
	protected abstract void err(String message);

	private String format(String message, Throwable ex) {
		if (message != null) {
			if (ex != null) {
				if (message.endsWith(".")) {
					message = message.substring(0, message.length() - 1);
				}
				String innerMessage = ex.getMessage();
				if (innerMessage != null) {
					message = message + ": " + innerMessage;
				} else {
					message = message + ": " + ex.getClass().getName();
				}
				return format(message, ex.getCause());
			} else {
				if (message.endsWith(".")) {
					return message;
				} else {
					return message + ".";
				}
			}
		} else {
			if (ex != null) {
				message = ex.getMessage();
				if (message == null) {
					message = ex.getClass().getName();
				}
				return format(message, ex.getCause());
			} else {
				return message;
			}
		}
	}

}
