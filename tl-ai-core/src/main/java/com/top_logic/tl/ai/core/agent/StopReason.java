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
	CONTEXT_OVERFLOW,

	/**
	 * The principal's token budget ran out while the run was working.
	 *
	 * <p>
	 * An orderly stop, not a failure: the run keeps everything it had answered up to the turn that
	 * spent the last of the budget, and the surface that started it says why the answer is
	 * incomplete. A run whose budget was already spent when it started does not reach the loop at
	 * all - it is refused where every run enters, with the same sentence.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.core.limits.TokenAllowance
	 */
	BUDGET_EXHAUSTED
}
