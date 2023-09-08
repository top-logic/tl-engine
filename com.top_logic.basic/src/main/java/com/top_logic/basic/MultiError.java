/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * An exception representing multiple other exceptions. When it prints its stacktrace via
 * {@link #printStackTrace()}, {@link #printStackTrace(PrintStream)} or
 * {@link #printStackTrace(PrintWriter)}, it prints its own stacktrace first, and then the other
 * exceptions in the order they were given to the constructor.
 * 
 * Must not be created for an empty list of exceptions or a single exception!
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public final class MultiError extends RuntimeException {

	private final List<Throwable> errors;

	/**
	 * Convenience variant of: {@link #MultiError(String, List)}
	 */
	public MultiError(String message, Throwable... errors) {
		this(message, Arrays.asList(errors));
	}

	/**
	 * Creates a {@link MultiError} containing the given {@link Throwable}s. The {@link Throwable}s
	 * are printed in the order given here. Therefore, please provide them in the order they
	 * occurred.
	 * 
	 * @param message
	 *        Should explain why multiple exceptions are reported at once and not only the first one
	 *        when it occurred.
	 * 
	 * @param errors
	 *        Have to be in chronologic order. Must contain at least two exceptions.
	 * @throws NullPointerException if the message is <code>null</code>, errors is <code>null</code> or contains a <code>null</code>.
	 * @throws IllegalArgumentException if errors contains less then two errors.
	 */
	public MultiError(String message, List<? extends Throwable> errors) {
		super("Multiple (" + errors.size() + ") exceptions have occured! " + message + "\nMessages: "
			+ concatMessages(errors) + "\nFirst Exception: " + (firstError(errors) != null
				? firstError(errors).getClass().getName() : ""),
			firstError(errors));
		if (message == null) {
			throw new NullPointerException("The message must not be null!");
		}
		if (errors.contains(null)) {
			throw new NullPointerException("The list of error must not contain null!");
		}
		if (errors.isEmpty()) {
			throw new IllegalArgumentException("A MultiError cannot be constructed for an empty list of exceptions!");
		}
		if (errors.size() == 1) {
			throw new IllegalArgumentException("A MultiError cannot be constructed for a single exception! By the way, that single exception is: ");
		}
		this.errors = new ArrayList<>(errors);
	}

	private static Throwable firstError(List<? extends Throwable> errors) {
		return errors == null || errors.isEmpty() ? null : errors.get(0);
	}

	/** Returns a mutable copy of the errors that caused this {@link MultiError}. */
	public List<Throwable> getErrors() {
		return new ArrayList<>(errors);
	}

	/**
	 * Prints the exceptions in chronologic order.
	 * 
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
	 */
	@Override
	public void printStackTrace(PrintStream printStream) {
		printStream.println(ExceptionUtil.printThrowableToString(this));
	}

	/**
	 * Prints the exceptions in chronologic order.
	 * 
	 * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
	 */
	@Override
	public void printStackTrace(PrintWriter printWriter) {
		printWriter.println("MultiError Stacktrace: ");
		super.printStackTrace(printWriter);
		printWriter.println();
		printWriter.println();

		printWriter.println("Stacktraces of the exceptions, in chronologic order:");
		for (Throwable exception : errors) {
			printWriter.println();
			exception.printStackTrace(printWriter);
		}
	}

	private static CharSequence concatMessages(List<? extends Throwable> throwables) {
		StringBuilder message = new StringBuilder();
		for (Throwable throwable : throwables) {
			message.append("\n\t").append(throwable.getMessage());
		}
		return message;
	}
}
