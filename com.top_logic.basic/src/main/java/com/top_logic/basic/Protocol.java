/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic;

/**
 * Light-weight logging interface for service classes.
 * 
 * <p>
 * {@link Protocol} supports both, reporting detailed information about
 * non-fatal errors and feedback about the occurrence of (potentially multiple)
 * non-fatal errors to the caller.
 * </p>
 * 
 * <p>
 * This supersedes both, the common {@link Logger} API and the Java built-in
 * exception mechanism:
 * </p>
 * 
 * <ul>
 * <li>The {@link Logger} API however can report non-final errors, but cannot
 * inform the caller of a service class about the occurrence of such errors.</li>
 * <li>The Java exception mechanism can only report a single fatal error, which
 * immediately terminates the execution of a service. Multiple non-fatal
 * problems, which in sum result in a failure of a service method cannot easily
 * realized using exceptions.</li>
 * </ul>
 * 
 * <p>
 * Using the {@link Protocol} interface is especially useful for tooling
 * implementations that inform the the programmer of multiple errors e.g. in a
 * configuration file. Stopping with a (fatal) exception at the first
 * encountered problem results in inconvenient usage characteristics, because
 * the tool has to be restarted after fixing each single problem. At the other
 * hand, it is absolutely necessary that the tool implementation terminates
 * abnormally, if any error has occurred, to ensure that a tool chain can
 * interrupt.
 * </p>
 * 
 * @see SyserrProtocol An implementation for command line tools that typically
 *      use {@link System#out} and {@link System#err} for direct user feedback
 *      (required e.g. in Ant to support coloring error messages).
 * @see LogProtocol An adaption to a server-side logging API that can be used to
 *      reuse the same service classes from server applications.
 * @see Main How to report multiple problems from a command line tool but
 *      ensuring that the program terminates abnormally if any error has
 *      occurred.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Protocol extends Log {
	
	/**
	 * Reports a non-fatal error.
	 * 
	 * <p>
	 * Execution continues until a {@link #fatal(String, Throwable)} problem
	 * occurs, or the service using this {@link Protocol} finishes. One or more
	 * reported errors signals a final failure of the service.
	 * </p>
	 * 
	 * @param message
	 *        The error message to report.
	 */
	@Override
	void error(String message);

	/**
	 * Converts an exception that occurred within the service implementation into a non-fatal error.
	 * 
	 * <p>
	 * Execution continues until a {@link #fatal(String, Throwable)} problem occurs, or the service
	 * using this {@link Protocol} finishes. One or more reported errors signals a final failure of
	 * the service.
	 * </p>
	 * 
	 * @param message
	 *        The error message to report.
	 * @param ex
	 *        The problem that is converted into a non-fatal error.
	 */
	@Override
	void error(String message, Throwable ex);

	/**
	 * Immediately terminates the service by throwing an exception of the return
	 * type.
	 * 
	 * <p>
	 * This method never terminates normally, but the created
	 * {@link RuntimeException} is always thrown instead of being returned. The
	 * return type is only declared of exception type to allow using this method
	 * with the following pattern:
	 * </p>
	 * 
	 * <code>throw fatal("Problem description.")</code>
	 * 
	 * <p>
	 * This signals the Java compiler that the control flow cannot reach a point
	 * after the method call and prevents errors due to access to eventually
	 * uninitialized local variables.
	 * </p>
	 * 
	 * @param message
	 *        The error message to report.
	 */
	RuntimeException fatal(String message);

	/**
	 * Converts the given declared exception into an immediate termination of
	 * the service without the need to declare the type of the given exception
	 * to be thrown.
	 * 
	 * <p>
	 * This method never terminates normally, but the created
	 * {@link RuntimeException} is always thrown instead of being returned. The
	 * return type is only declared of exception type to allow using this method
	 * with the following pattern:
	 * </p>
	 * 
	 * <code>throw fatal("Problem description.", caughtException)</code>
	 * 
	 * <p>
	 * This signals the Java compiler that the control flow cannot reach a point
	 * after the method call and prevents errors due to access to eventually
	 * uninitialized local variables.
	 * </p>
	 * 
	 * @param message
	 *        The error message to report.
	 */
	RuntimeException fatal(String message, Throwable ex);
	
	/**
	 * Terminate the execution of the service, if any errors have occurred.
	 */
	void checkErrors();

	/**
	 * The declared exception that was passed to any of the methods
	 * {@link #error(String, Throwable)} or {@link #fatal(String, Throwable)}.
	 */
	@Override
	Throwable getFirstProblem();
	
}
