/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

/**
 * A question a background subagent asked the agent that started it, through {@code ask_lead}.
 *
 * <p>
 * The subagent's thread is parked on {@link #answer()} until the lead replies with
 * {@code reply_to_subagent}, the question times out, or the subagent is cancelled. {@link #isDelivered()}
 * says whether the lead has been shown the question - at the start of one of its turns, or by
 * {@code wait_for_subagent} - so that it is shown once.
 * </p>
 */
public final class LeadQuestion {

	private final String _question;

	private final Instant _askedAt = Instant.now();

	private final CompletableFuture<String> _answer = new CompletableFuture<>();

	private volatile boolean _delivered;

	/**
	 * Creates a question.
	 *
	 * @param question
	 *        What the subagent asks, as it wrote it.
	 */
	public LeadQuestion(String question) {
		_question = question;
	}

	/** What the subagent asks, as it wrote it. */
	public String question() {
		return _question;
	}

	/** When the subagent asked. */
	public Instant askedAt() {
		return _askedAt;
	}

	/** Completed with the lead's answer, or with a framework sentence when there is none. */
	public CompletableFuture<String> answer() {
		return _answer;
	}

	/** Whether the lead has been shown this question. */
	public boolean isDelivered() {
		return _delivered;
	}

	/** Marks the question as shown to the lead; returns whether it was not shown before. */
	public synchronized boolean markDelivered() {
		boolean first = !_delivered;
		_delivered = true;
		return first;
	}
}
