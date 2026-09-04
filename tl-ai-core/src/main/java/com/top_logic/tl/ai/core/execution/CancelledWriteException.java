/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.execution;

import com.top_logic.tl.ai.core.agent.RunCancellationBinding;

/**
 * Signals that a caller stopped waiting for an {@link AgentExecutionStore} write because its agent
 * run was cancelled.
 *
 * <p>
 * This is the expected outcome of cancelling a run, not a failure: cancellation interrupts the
 * agent thread, and an interrupt ends the wait for the writer thread. The submitted mutation
 * itself is unaffected - it is already queued and still commits on the writer thread - so nothing
 * is lost; only the caller no longer observes the result. Handlers should therefore trace this at
 * INFO instead of reporting it as an error.
 * </p>
 *
 * <p>
 * An interrupt during a write with any <em>other</em> cause remains a genuine failure and is
 * reported as a plain {@link RuntimeException}; {@link RunCancellationBinding} is what tells the
 * two apart.
 * </p>
 */
public class CancelledWriteException extends RuntimeException {

	/**
	 * Creates a {@link CancelledWriteException}.
	 *
	 * @param message
	 *        The detail message.
	 * @param cause
	 *        The {@link InterruptedException} that ended the wait.
	 */
	public CancelledWriteException(String message, Throwable cause) {
		super(message, cause);
	}
}
