/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.thread;

/**
 * A {@link Exception} that is not used for reporting an error, but for logging the current stack
 * trace.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class StackTrace extends Exception {

	/**
	 * The default message, if none is given in the constructor.
	 */
	public static final String DEFAULT_MESSAGE = "This stack trace does not indicate a problem or bug."
		+ " It's used only for documenting the source of an event or action.";

	/**
	 * Creates a {@link StackTrace} with the default {@link #DEFAULT_MESSAGE}.
	 */
	public StackTrace() {
		this(DEFAULT_MESSAGE);
	}

	/**
	 * Creates a {@link StackTrace} with the given message.
	 * 
	 * @param message
	 *        Is allowed to be null.
	 */
	public StackTrace(String message) {
		super(message);
	}

	/**
	 * Removes the given numbers of stack frames.
	 * <p>
	 * Removes the stack frames for the innermost / most recently invoked methods.
	 * </p>
	 * 
	 * @param framesToRemove
	 *        Is not allowed to be negative. If it is greater than the number of stack frames, the
	 *        stack trace will be an empty array.
	 * @return This object, for convenience.
	 */
	public StackTrace truncate(int framesToRemove) {
		StackTraceElement[] originalStackTrace = getStackTrace();
		if (originalStackTrace.length <= framesToRemove) {
			/* JVMs might return an empty array or only a part of the real stack trace. */
			setStackTrace(new StackTraceElement[0]);
			return this;
		}
		StackTraceElement[] strippedStackTrace = new StackTraceElement[originalStackTrace.length - framesToRemove];
		System.arraycopy(originalStackTrace, framesToRemove, strippedStackTrace, 0, strippedStackTrace.length);
		setStackTrace(strippedStackTrace);
		return this;
	}

}
