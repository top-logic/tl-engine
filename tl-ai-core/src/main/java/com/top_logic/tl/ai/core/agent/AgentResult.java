/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.Objects;

import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * The result of an {@link AgentLoop#run} invocation.
 *
 * @param stopReason   why the loop stopped
 * @param turnsUsed    number of LLM call iterations completed
 * @param totalUsage   cumulative token usage across all turns
 * @param errorKind    the {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind} that
 *                     ended the run, as its name; {@code null} unless a provider error did
 * @param errorMessage what the provider said about it; {@code null} unless a provider error ended
 *                     the run
 *
 * @see AgentLoop
 */
public record AgentResult(StopReason stopReason, int turnsUsed, TokenUsage totalUsage,
		String errorKind, String errorMessage) {

	/** */
	public AgentResult {
		Objects.requireNonNull(stopReason, "Stop reason must not be null.");
		Objects.requireNonNull(totalUsage, "Total usage must not be null.");
		if (turnsUsed < 0) {
			throw new IllegalArgumentException("Turns used must not be negative.");
		}
	}

	/**
	 * The result of a run that ended without a provider error - either because it succeeded, or
	 * because it stopped for a reason the provider had no part in (rejected, aborted, out of
	 * turns).
	 */
	public AgentResult(StopReason stopReason, int turnsUsed, TokenUsage totalUsage) {
		this(stopReason, turnsUsed, totalUsage, null, null);
	}

	/** Whether the run reached its answer - anything else is a failure to report, not a reply. */
	public boolean isComplete() {
		return stopReason == StopReason.COMPLETE;
	}
}
