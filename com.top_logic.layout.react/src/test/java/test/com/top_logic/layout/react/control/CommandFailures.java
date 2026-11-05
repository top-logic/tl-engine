/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import junit.framework.AssertionFailedError;

import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.util.error.TopLogicException;

/**
 * Test utilities for commands that report a failure by throwing a {@link TopLogicException}.
 *
 * @see CommandErrors
 */
public final class CommandFailures {

	private CommandFailures() {
		// Static utility.
	}

	/**
	 * Runs the given command and returns the {@link TopLogicException} it fails with.
	 *
	 * @param message
	 *        The message of the assertion failing if the command completes.
	 * @param command
	 *        The command to run.
	 * @return The exception the command threw.
	 * @throws AssertionFailedError
	 *         If the command completes without throwing a {@link TopLogicException}.
	 */
	public static TopLogicException failure(String message, Runnable command) {
		try {
			command.run();
		} catch (TopLogicException ex) {
			return ex;
		}
		throw new AssertionFailedError(message);
	}

	/**
	 * Runs the given command and returns the {@link TopLogicException} it fails with, if any.
	 *
	 * @param command
	 *        The command to run.
	 * @return The exception the command threw, <code>null</code> if it completed.
	 */
	public static TopLogicException outcome(Runnable command) {
		return outcome(TopLogicException.class, command);
	}

	/**
	 * Runs the given command and returns the exception of the given type it fails with, if any.
	 *
	 * @param type
	 *        The type of failure expected, e.g. {@link RuntimeException} for a command that may
	 *        fail internally as well.
	 * @param command
	 *        The command to run.
	 * @return The exception the command threw, <code>null</code> if it completed.
	 */
	public static <E extends RuntimeException> E outcome(Class<E> type, Runnable command) {
		try {
			command.run();
			return null;
		} catch (RuntimeException ex) {
			if (type.isInstance(ex)) {
				return type.cast(ex);
			}
			throw ex;
		}
	}

	/**
	 * Runs the given command and returns the {@link TopLogicException} it fails with.
	 *
	 * @see #failure(String, Runnable)
	 */
	public static TopLogicException failure(Runnable command) {
		return failure("The command must fail.", command);
	}
}
