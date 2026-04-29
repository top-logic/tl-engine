/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Conversation memory with token tracking and compaction support.
 *
 * <p>Wraps a {@link Conversation}, adding hybrid token estimation (API baseline +
 * heuristic) and automatic compaction when the context window is approaching its limit.</p>
 *
 * <p><b>Thread safety:</b> Same contract as {@link Conversation} — single writer (agent loop),
 * concurrent readers (UI). All mutating methods must be called from the agent loop thread.</p>
 *
 * @see TokenEstimator
 * @see CompactionStrategy
 */
public class ConversationMemory {

	/** Default reserved tokens (output headroom + safety buffer). */
	public static final int DEFAULT_RESERVED_TOKENS = 33_000;

	private final Conversation _conversation;

	private final TokenEstimator _estimator;

	private final CompactionStrategy _strategy;

	private final int _contextWindowTokens;

	private final int _reservedTokens;

	private int _apiBaselineTokens;

	private int _apiBaselineMessageCount;

	/**
	 * @param conversation        the underlying conversation
	 * @param estimator           estimates token cost of messages
	 * @param strategy            compaction strategy
	 * @param contextWindowTokens total context window of the model
	 * @param reservedTokens      tokens reserved for output and safety buffer
	 */
	public ConversationMemory(Conversation conversation, TokenEstimator estimator,
			CompactionStrategy strategy, int contextWindowTokens, int reservedTokens) {
		_conversation = Objects.requireNonNull(conversation, "Conversation must not be null.");
		_estimator = Objects.requireNonNull(estimator, "Estimator must not be null.");
		_strategy = Objects.requireNonNull(strategy, "Strategy must not be null.");
		if (contextWindowTokens <= 0) {
			throw new IllegalArgumentException("contextWindowTokens must be positive.");
		}
		if (reservedTokens < 0) {
			throw new IllegalArgumentException("reservedTokens must not be negative.");
		}
		_contextWindowTokens = contextWindowTokens;
		_reservedTokens = reservedTokens;
	}

	/**
	 * Creates a {@link ConversationMemory} with a new {@link Conversation},
	 * {@link ByteBasedTokenEstimator}, and {@link #DEFAULT_RESERVED_TOKENS}.
	 */
	public static ConversationMemory withDefaults(CompactionStrategy strategy,
			int contextWindowTokens) {
		return new ConversationMemory(
			new Conversation(), new ByteBasedTokenEstimator(), strategy,
			contextWindowTokens, DEFAULT_RESERVED_TOKENS);
	}

	/** Appends a message to the conversation. */
	public void add(Message message) {
		_conversation.add(message);
	}

	/** All messages in the conversation. */
	public List<Message> messages() {
		return _conversation.messages();
	}

	/**
	 * Estimate the current token usage of the conversation.
	 *
	 * <p>Uses the API baseline if available, plus heuristic estimation for messages
	 * added since the baseline was set. Falls back to full heuristic estimation
	 * if no baseline exists.</p>
	 */
	public int estimateTokens() {
		List<Message> msgs = _conversation.messages();

		if (_apiBaselineTokens > 0 && _apiBaselineMessageCount <= msgs.size()) {
			List<Message> newMessages = msgs.subList(_apiBaselineMessageCount, msgs.size());
			return _apiBaselineTokens + _estimator.estimateTokens(newMessages);
		}

		return _estimator.estimateTokens(msgs);
	}

	/**
	 * Whether the conversation should be compacted.
	 *
	 * @return {@code true} if estimated tokens exceed the usable budget
	 */
	public boolean shouldCompact() {
		return estimateTokens() >= usableTokens();
	}

	/**
	 * Run compaction with "auto" trigger.
	 *
	 * @return the compaction result
	 */
	public CompactionResult compact() {
		return compact("auto");
	}

	/**
	 * Run compaction with the specified trigger label.
	 *
	 * @param trigger "auto" or "manual"
	 * @return the compaction result
	 */
	public CompactionResult compact(String trigger) {
		List<Message> messages = _conversation.messages();
		int targetTokens = usableTokens();

		CompactionResult result = _strategy.compact(messages, targetTokens, _estimator);

		_conversation.replaceRange(0, messages.size(), result.replacementMessages());

		// Baseline holds absolute message indices; after replaceRange they are stale.
		_apiBaselineTokens = 0;
		_apiBaselineMessageCount = 0;

		if (!trigger.equals(result.trigger())) {
			return new CompactionResult(result.replacementMessages(),
				result.messagesSummarized(), result.tokensBefore(),
				result.tokensAfter(), trigger);
		}
		return result;
	}

	/**
	 * Update the token baseline with actual usage from the last API response.
	 *
	 * <p>Call this after each LLM response to improve token estimation accuracy.
	 * The baseline records the actual input token count and the current message count,
	 * so subsequent messages can be estimated incrementally.</p>
	 *
	 * @param usage token usage from the provider response
	 */
	public void updateTokenBaseline(TokenUsage usage) {
		Objects.requireNonNull(usage, "Usage must not be null.");
		_apiBaselineTokens = usage.inputTokens();
		_apiBaselineMessageCount = _conversation.messages().size();
	}

	/** Context window capacity minus reserved tokens. */
	public int usableTokens() {
		return _contextWindowTokens - _reservedTokens;
	}

	/**
	 * Return messages fitting within the given token budget.
	 *
	 * <p>Always includes all {@link SystemMessage}s. Then includes messages from
	 * newest to oldest until the budget is exhausted. This ensures the LLM always
	 * sees the system prompt and the most recent context.</p>
	 *
	 * @param tokenBudget maximum tokens for the returned messages
	 * @return messages fitting within the budget, in conversation order
	 */
	public List<Message> getMessages(int tokenBudget) {
		List<Message> all = _conversation.messages();
		int fullEstimate = _estimator.estimateTokens(all);
		if (fullEstimate <= tokenBudget) {
			return all;
		}

		// Always include system messages
		List<Message> systemMessages = new ArrayList<>();
		List<Message> nonSystemMessages = new ArrayList<>();
		for (Message msg : all) {
			if (msg instanceof SystemMessage) {
				systemMessages.add(msg);
			} else {
				nonSystemMessages.add(msg);
			}
		}

		int systemTokens = _estimator.estimateTokens(systemMessages);
		int remainingBudget = tokenBudget - systemTokens;

		// Add non-system messages from newest, respecting budget
		List<Message> selected = new ArrayList<>();
		for (int i = nonSystemMessages.size() - 1; i >= 0; i--) {
			Message msg = nonSystemMessages.get(i);
			int msgTokens = _estimator.estimateTokens(msg);
			if (msgTokens <= remainingBudget) {
				selected.add(0, msg);
				remainingBudget -= msgTokens;
			} else {
				break;
			}
		}

		// Rebuild in conversation order: system messages at their original positions
		List<Message> result = new ArrayList<>();
		int sysIdx = 0;
		int selIdx = 0;
		for (Message msg : all) {
			if (msg instanceof SystemMessage && sysIdx < systemMessages.size()
					&& msg == systemMessages.get(sysIdx)) {
				result.add(msg);
				sysIdx++;
			} else if (selIdx < selected.size() && msg == selected.get(selIdx)) {
				result.add(msg);
				selIdx++;
			}
		}
		return result;
	}

	/** The underlying conversation. */
	public Conversation conversation() {
		return _conversation;
	}
}
