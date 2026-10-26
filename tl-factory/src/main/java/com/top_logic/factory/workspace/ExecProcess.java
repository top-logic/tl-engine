/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.workspace;

import java.io.IOException;
import java.time.Duration;

/**
 * A command running inside a workspace, started by
 * {@link WorkspaceProvider#startExec(String, ExecCommand, ExecOutput)}.
 *
 * <p>
 * The command reads its standard input from this handle: what is written to it reaches the
 * command after {@link #flush()}, and the command sees the end of its input after
 * {@link #closeStdin()}. Its output is delivered to the {@link ExecOutput} given when starting it,
 * from a thread of the provider, while the command runs.
 * </p>
 *
 * <p>
 * Writing is thread-safe. {@link #close()} releases the handle; a command that still runs then is
 * killed. Each handle must be closed.
 * </p>
 */
public interface ExecProcess extends AutoCloseable {

	/**
	 * Writes bytes to the standard input of the command.
	 *
	 * <p>
	 * The bytes may be buffered until {@link #flush()}.
	 * </p>
	 *
	 * @throws IOException
	 *         If the input of the command is closed, or the connection to the command is lost.
	 */
	void write(byte[] data) throws IOException;

	/**
	 * Writes text, encoded as UTF-8, to the standard input of the command.
	 *
	 * @see #write(byte[])
	 */
	void write(String text) throws IOException;

	/**
	 * Sends everything written so far to the command.
	 */
	void flush() throws IOException;

	/**
	 * Flushes and closes the standard input of the command, which then reads the end of its input.
	 *
	 * <p>
	 * Closing a closed input does nothing.
	 * </p>
	 */
	void closeStdin() throws IOException;

	/**
	 * Waits until the command has finished, at most for the given time.
	 *
	 * @param timeout
	 *        The maximum time to wait, {@link Duration#ZERO} to only check.
	 * @return Whether the command has finished.
	 */
	boolean waitFor(Duration timeout) throws InterruptedException;

	/**
	 * Whether the command is still running.
	 */
	boolean isAlive() throws InterruptedException;

	/**
	 * The exit code of the finished command.
	 *
	 * @throws IllegalStateException
	 *         If the command is still running.
	 */
	int exitCode();

	/**
	 * Terminates the command and waits until it has finished.
	 *
	 * <p>
	 * The command and the processes it started are asked to terminate; those that do not finish
	 * within the grace period of the provider are killed. Terminating a finished command does
	 * nothing.
	 * </p>
	 */
	void terminate() throws InterruptedException;

	/**
	 * Releases the handle. A command that still runs is killed without grace period.
	 */
	@Override
	void close();

}
