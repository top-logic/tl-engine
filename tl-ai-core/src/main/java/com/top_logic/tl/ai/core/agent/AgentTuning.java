/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

/**
 * Central home for the agent-execution tuning constants that were previously scattered across
 * {@link AgentLoop} and {@link TransactionScope}.
 *
 * <p>
 * These are deliberate knobs, not derived values; keeping them in one place makes them easy to find
 * and tune, and removes the duplicated definitions (the backoff poll slice was defined identically
 * in both {@link AgentLoop} and {@link TransactionScope}). Values that merely happen to be equal but
 * mean different things are intentionally <em>not</em> merged.
 * </p>
 *
 * <p>
 * The deadlines a <em>human</em> has to answer an agent are deliberately not here: they belong to
 * the application rather than to the engine and are configured through
 * {@link com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService}.
 * </p>
 */
public final class AgentTuning {

	private AgentTuning() {
		// Constants only.
	}

	/** Default maximum characters of a single tool result before it is truncated. */
	public static final int MAX_TOOL_RESULT_CHARS = 20000;

	/** Maximum consecutive identical (or A/B alternating) tool calls before a call is blocked. */
	public static final int MAX_DUPLICATE_TOOL_CALLS = 2;

	/** Maximum reactive compaction retries on {@code context_window_exceeded} before giving up. */
	public static final int MAX_REACTIVE_COMPACT_RETRIES = 2;

	/**
	 * How often in a row a run asks the model to continue after a reply that held no text and called
	 * no tool, before it ends the run with {@link StopReason#NO_ANSWER}.
	 *
	 * <p>
	 * In a row: a reply that calls a tool is progress and starts the count again, so a long run can
	 * be helped more than once. The run's turn limit still bounds the whole.
	 * </p>
	 */
	public static final int MAX_EMPTY_REPLY_CONTINUATIONS = 1;

	/** Base backoff delay in milliseconds for transient provider retries; doubles each attempt. */
	public static final long BASE_BACKOFF_MS = 1000L;

	/** Longest single wait between two provider retries, unless the provider asks for a longer one. */
	public static final long MAX_BACKOFF_MS = 30_000L;

	/**
	 * How long, in seconds, a run waits in total for a model that answers "too many requests", when
	 * the run has no other model to go to.
	 *
	 * <p>
	 * Of the order of minutes, because what holds the model is usually another request of the same
	 * key that is still running - on a local model one request can take well over a minute (#678).
	 * An installation sets its own value in {@code LLMProviderService}'s
	 * {@code <failover rate-limit-wait-seconds="..."/>}.
	 * </p>
	 */
	public static final int DEFAULT_RATE_LIMIT_WAIT_SECONDS = 180;

	/**
	 * Sleep slice in milliseconds for backoff/retry waits, so cancellation is noticed promptly
	 * rather than after the full wait.
	 */
	public static final long BACKOFF_POLL_MS = 250L;

	/** Maximum attempts to acquire a UI lock before failing the agent write. */
	public static final int LOCK_MAX_RETRIES = 3;

	/** Delay in milliseconds between lock-acquisition attempts. */
	public static final long LOCK_RETRY_DELAY_MS = 10_000L;

	/** Duration in milliseconds a UI lock is held once acquired. */
	public static final long LOCK_DURATION_MS = 10 * 60 * 1000L;
}
