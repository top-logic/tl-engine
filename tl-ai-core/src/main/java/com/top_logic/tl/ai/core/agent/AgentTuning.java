/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.TimeUnit;

/**
 * Central home for the agent-execution tuning constants that were previously scattered across
 * {@link AgentLoop}, {@link TransactionScope} and
 * {@link com.top_logic.tl.ai.core.tools.interaction.AskUserTool}.
 *
 * <p>
 * These are deliberate knobs, not derived values; keeping them in one place makes them easy to find
 * and tune, and removes the duplicated definitions (the backoff poll slice was defined identically
 * in both {@link AgentLoop} and {@link TransactionScope}). Values that merely happen to be equal but
 * mean different things are intentionally <em>not</em> merged.
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

	/** Base backoff delay in milliseconds for transient provider retries; doubles each attempt. */
	public static final long BASE_BACKOFF_MS = 1000L;

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

	/** Time to wait for a human response to an interaction request, in {@link TimeUnit#MINUTES}. */
	public static final long INTERACTION_TIMEOUT_MINUTES = 5L;
}
