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
	 * Runs the given command and returns the {@link TopLogicException} it fails with.
	 *
	 * @see #failure(String, Runnable)
	 */
	public static TopLogicException failure(Runnable command) {
		return failure("The command must fail.", command);
	}
}
