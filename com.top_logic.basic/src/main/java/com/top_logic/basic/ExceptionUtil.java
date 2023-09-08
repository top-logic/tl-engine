/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Methods and classes useful when working with {@link Throwable}s.
 * 
 * <p>
 * They are collected here even if they have their own class and file to easily find them.
 * It's not called ThrowableUtil as "Throwable" is a rarely used term.
 * It's called "exception handling" after all, not "throwable handling".
 * </p>
 * 
 * @author     <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class ExceptionUtil {

	/**
	 * Convenience variant of {@link Throwable#printStackTrace(PrintWriter)} that returns a string
	 * instead of writing to a {@link PrintWriter}.
	 */
	public static String printThrowableToString(Throwable throwable) {
		Writer writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	/**
	 * @see MultiError#MultiError(String, List)
	 */
	public static MultiError createMultiError(String message, Throwable... throwables) {
		return new MultiError(message, throwables);
	}

	/**
	 * @see MultiError#MultiError(String, List)
	 */
	public static MultiError createMultiError(String message, List<? extends Throwable> throwables) {
		return new MultiError(message, throwables);
	}

	/**
	 * Creates a {@link RuntimeException} for the given message and {@link Throwable}s.
	 * <p>
	 * If the {@link List} is <code>null</code> or empty, a {@link RuntimeException} without cause
	 * is returned. <br/>
	 * If the {@link List} contains exactly one element, a {@link RuntimeException} with that single
	 * {@link Throwable} as cause is created. <br/>
	 * If the {@link List} contains multiple {@link Throwable}s, a {@link MultiError} is created.
	 * </p>
	 * 
	 * @param message
	 *        Must not be <code>null</code>.
	 * @param throwables
	 *        Must not contain <code>null</code>. Is allowed to be <code>null</code>.
	 */
	public static RuntimeException createException(String message, List<? extends Throwable> throwables) {
		if (throwables == null || throwables.isEmpty()) {
			return new RuntimeException(message);
		}
		if (throwables.size() == 1) {
			Throwable cause = throwables.get(0);
			return new RuntimeException(createErrorMessage(message, cause), cause);
		}
		return new MultiError(message, throwables);
	}

	/**
	 * Message for {@link #createException(String, List)}
	 */
	public static String createErrorMessage(String message, Throwable cause) {
		if (cause == null) {
			return message;
		}
		return message + " (" + cause.getClass().getName() +
			(cause.getMessage() == null ? "" : ": " + cause.getMessage()) + ")";
	}

	/**
	 * If the {@link Throwable} has a {@link Throwable#getCause() cause} with its own message,
	 * and that cause-message is not contained in the throwable message, it is appended to it.
	 * That "full message" makes analyzing errors easier,
	 * as you don't have to debug into a problem just to get the error message of the cause.
	 * 
	 * @param throwable Is allowed to be <code>null</code>.
	 * @return Never <code>null</code>
	 */
	public static String getFullMessage(Throwable throwable) {
		if (throwable == null) {
			return "null";
		}
		if (throwable.getCause() != null && !containsAllInfos(throwable, throwable.getCause())) {
			return getMessageWithClass(throwable) + " Caused by: " + getFullMessage(throwable.getCause());
		}
		return getMessageWithClass(throwable);
	}

	/**
	 * Does the message of the {@link Throwable} contain all the information in the given cause?
	 * <p>
	 * "All information" means: The {@link Throwable#getMessage() message} and the
	 * {@link Class#getSimpleName() (simple) class name}.
	 * </p>
	 */
	private static boolean containsAllInfos(Throwable throwable, Throwable cause) {
		if (cause == null) {
			return true;
		}
		return messageContainsCauseMessage(throwable, cause)
			&& messageContainsCauseClass(throwable, cause);
	}

	private static boolean messageContainsCauseMessage(Throwable throwable, Throwable cause) {
		return nonNull(throwable.getMessage()).contains(nonNull(cause.getMessage()));
	}

	private static boolean messageContainsCauseClass(Throwable throwable, Throwable cause) {
		return nonNull(throwable.getMessage()).contains(cause.getClass().getSimpleName());
	}

	private static String getMessageWithClass(Throwable throwable) {
		if (throwable == null) {
			return "null";
		}
		if (messageContainsClass(throwable)) {
			return throwable.getMessage();
		}
		return throwable.getClass().getName() + ": " + nonNull(throwable.getMessage());
	}

	private static boolean messageContainsClass(Throwable throwable) {
		if (throwable == null) {
			return false;
		}
		return getMessage(throwable).contains(throwable.getClass().getSimpleName());
	}

	/**
	 * Returns the {@link Throwable#getMessage() message} of the {@link Throwable} nullsafe: If
	 * the throwable or its message is <code>null</code>, the empty string is returned.
	 * 
	 * @return Never <code>null</code>
	 */
	public static String getMessage(Throwable throwable) {
		return throwable == null ? "" : nonNull(throwable.getMessage());
	}

	/**
	 * Creates a List from the cause chain.
	 * <p>
	 * Starts with the given {@link Throwable}.
	 * </p>
	 * <p>
	 * If a {@link Throwable} has itself as cause (which should not happen), the chain is ending
	 * with that {@link Throwable}.
	 * </p>
	 * 
	 * @param throwable
	 *        Is allowed to be null, resulting in an empty list.
	 * @return Never null. Never contains null. Always mutable. Always expand-/shrink-able.
	 */
	public static List<Throwable> getCauseChain(Throwable throwable) {
		List<Throwable> result = new ArrayList<>();
		Throwable cause = throwable;
		while (cause != null) {
			result.add(cause);
			if (cause == cause.getCause()) {
				// Prevent infinite loop
				break;
			}
			cause = cause.getCause();
		}
		return result;
	}

	/**
	 * Re-throws the given {@link Throwable} wrapped into a {@link RuntimeException} with the given
	 * message and the message of the cause.
	 * 
	 * @param message
	 *        Is not allowed to be null.
	 * @param cause
	 *        Is not allowed to be null.
	 * @return Never returns.
	 * @throws RuntimeException
	 *         Always.
	 */
	public static RuntimeException rethrow(String message, Throwable cause) {
		checkRethrowArguments(message, cause);
		throw new RuntimeException(message + " Cause: " + cause.getMessage(), cause);
	}

	private static void checkRethrowArguments(String message, Throwable cause) {
		if ((cause == null) && (message == null)) {
			throw new RuntimeException("META ERROR: Missing message and cause when calling 'rethrow'.");
		}
		if (cause == null) {
			throw new RuntimeException("META ERROR: Missing cause when calling 'rethrow'."
				+ " Original problem: " + message);
		}
		if (message == null) {
			throw new RuntimeException("META ERROR: Missing message when calling 'rethrow'."
				+ " Original problem" + cause.getMessage(), cause);
		}
	}

	/**
	 * Runs the given {@link Supplier}.
	 * <p>
	 * When a {@link Throwable} is thrown, the {@link Throwable} is logged and the given
	 * {@link Function} is called with the {@link Throwable}. The {@link Function} has to compute
	 * the error result, which is then returned. When the Function throws something, too, it is not
	 * caught.
	 * </p>
	 * 
	 * @param unsafeCode
	 *        Is not allowed to be null.
	 * @param errorResult
	 *        Is not allowed to be null.
	 * @param caller
	 *        The class for which the error is logged.
	 */
	public static <T> T runAndLogErrors(Supplier<? extends T> unsafeCode,
			Function<? super Throwable, ? extends T> errorResult, Class<?> caller) {
		try {
			return unsafeCode.get();
		} catch (RuntimeException | Error exception) {
			if (exception instanceof ThreadDeath) {
				/* Never suppress ThreadDeath, as that make it impossible to stop this thread from
				 * within the JVM. */
				throw exception;
			}
			logError(exception.getMessage(), exception, caller);
			return errorResult.apply(exception);
		}
	}

	/**
	 * Runs the given {@link Supplier}.
	 * <p>
	 * When a {@link Throwable} is thrown, the {@link Throwable} is logged and the given
	 * {@link Function} is called with the {@link Throwable}. The {@link Function} has to compute
	 * the error result, which is then returned. When the {@link Function} throws something, too, it
	 * is not caught.
	 * </p>
	 * 
	 * @param unsafeCode
	 *        Is not allowed to be null.
	 * @param caller
	 *        The class for which the error is logged.
	 */
	public static void runAndLogErrors(Runnable unsafeCode, Class<?> caller) {
		try {
			unsafeCode.run();
		} catch (RuntimeException | Error exception) {
			if (exception instanceof ThreadDeath) {
				/* Never suppress ThreadDeath, as that make it impossible to stop this thread from
				 * within the JVM. */
				throw exception;
			}
			logError(exception.getMessage(), exception, caller);
		}
	}

	private static void logError(String message, Throwable exception, Class<?> caller) {
		Logger.error(message, exception, caller);
	}

}
