/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Why the agent loop stopped.
 *
 * @see AgentResult
 */
public enum StopReason {

	/** LLM finished without requesting tools, and its reply is an answer. */
	COMPLETE,

	/**
	 * Hit the configured maximum turns limit.
	 *
	 * <p>
	 * The last turn of such a run offers no tool: the model is told that the limit is reached and
	 * answers with what it has, so the run ends with an answer rather than with a call nobody runs.
	 * </p>
	 */
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
	 * The principal's token or money budget ran out while the run was working.
	 *
	 * <p>
	 * An orderly stop, not a failure: the run keeps everything it had answered up to the turn that
	 * spent the last of the budget, and the surface that started it says why the answer is
	 * incomplete. A run whose budget was already spent when it started does not reach the loop at
	 * all - it is refused where every run enters, with the same sentence.
	 * </p>
	 *
	 * @see com.top_logic.tl.ai.core.limits.BudgetAllowance
	 */
	BUDGET_EXHAUSTED,

	/**
	 * The model ended its turn without saying anything: its last reply held no text and called no
	 * tool, and asking it to continue did not change that (#677).
	 *
	 * <p>
	 * A reply of that shape - typically nothing but a thinking block - used to end the run as
	 * {@link #COMPLETE}, and the chat showed a finished turn with no answer in it. The loop now asks
	 * the model once to go on; a run that still gets nothing ends with this reason, which the
	 * surface that started it tells the user.
	 * </p>
	 *
	 * @see AgentTuning#MAX_EMPTY_REPLY_CONTINUATIONS
	 */
	NO_ANSWER,

	/**
	 * The model's last reply was cut off at the output limit while it was writing its answer
	 * (#677).
	 *
	 * <p>
	 * The text it wrote is kept, but it is not the whole answer, and a reply that ends there must
	 * not read as one the model chose to end. The loop does not ask for the rest by itself: the
	 * rest would be a second answer, and only the last one of a run is the one a surface shows.
	 * </p>
	 */
	OUTPUT_LIMIT;

	/**
	 * Whether the run stopped in order at one of its limits - turns, budget, output -
	 * rather than because it was done or failed.
	 *
	 * <p>
	 * Such a run has an answer, but maybe not the whole one; the chat says "limit reached" for it
	 * (#786).
	 * </p>
	 */
	public boolean isLimit() {
		return this == MAX_TURNS || this == BUDGET_EXHAUSTED || this == OUTPUT_LIMIT;
	}
}
