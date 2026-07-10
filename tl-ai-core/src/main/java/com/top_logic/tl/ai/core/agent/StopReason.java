/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Why the agent loop stopped.
 *
 * @see AgentResult
 */
public enum StopReason {

	/** LLM finished without requesting tools. */
	COMPLETE,

	/** Hit the configured maximum turns limit. */
	MAX_TURNS,

	/** Unrecoverable provider error. */
	ERROR,

	/** Loop was interrupted (cancellation). */
	ABORTED,

	/** Response validator rejected the LLM's response. */
	REJECTED,

	/** Context window exceeded after compaction retries exhausted. */
	CONTEXT_OVERFLOW
}
