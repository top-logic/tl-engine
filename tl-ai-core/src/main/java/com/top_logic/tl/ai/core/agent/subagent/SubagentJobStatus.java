/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

/**
 * Lifecycle state of an asynchronous subagent job.
 */
public enum SubagentJobStatus {

	/** Registered but not yet started (waiting for a concurrency slot). */
	QUEUED,

	/** Running on a background thread. */
	RUNNING,

	/** Finished successfully; {@link SubagentJob#result()} holds the envelope. */
	DONE,

	/** Finished with an error; the envelope carries an error summary. */
	FAILED,

	/** Cooperatively cancelled before completion. */
	CANCELLED;

	/** Whether this is a terminal state (no further transitions). */
	public boolean isTerminal() {
		return this == DONE || this == FAILED || this == CANCELLED;
	}
}
