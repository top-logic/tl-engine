/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;

/**
 * Core agent loop: calls the LLM, executes tool calls, feeds results back, repeats.
 *
 * <p>Blocks the calling thread for the entire run. Progress is reported via
 * {@link AgentListener} callbacks on the same thread.</p>
 *
 * <p>One iteration:</p>
 * <ol>
 *   <li>Build {@link ProviderRequest} from conversation + config</li>
 *   <li>Call {@link LLMProvider#stream} — streams via internal handler</li>
 *   <li>Assemble {@link AssistantMessage}, add to conversation</li>
 *   <li>Extract {@link ToolCall}s — if none, stop</li>
 *   <li>Execute each via {@link ToolExecutor}, add results to conversation</li>
 *   <li>Loop</li>
 * </ol>
 *
 * @see AgentConfig
 * @see AgentResult
 * @see AgentListener
 */
public class AgentLoop {

	// TODO: Retry with backoff for RATE_LIMITED / OVERLOADED (callProvider is the seam)
	// TODO: Context compaction when CONTEXT_WINDOW_EXCEEDED (callProvider is the seam)
	// TODO: Parallel tool execution (partition read-only vs write)
	// TODO: Permission check before tool execution
	// TODO: Memory/attachment injection between turns
	// TODO: Stop hooks (post-response validation)

	private final AgentConfig _config;

	private final LLMProvider _provider;

	private final ToolExecutor _toolExecutor;

	/**
	 * Creates a new agent loop.
	 *
	 * @param config       loop configuration
	 * @param provider     LLM provider for streaming responses
	 * @param toolExecutor executes tool calls requested by the LLM
	 */
	public AgentLoop(AgentConfig config, LLMProvider provider, ToolExecutor toolExecutor) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_toolExecutor = Objects.requireNonNull(toolExecutor, "Tool executor must not be null.");
	}

	/**
	 * Run the agent loop.
	 *
	 * <p>Blocks until the LLM completes without tool calls, the turn limit is reached,
	 * an unrecoverable error occurs, or the thread is interrupted.</p>
	 *
	 * @param conversation the conversation to operate on (messages are added during the run)
	 * @param listener     receives progress events
	 * @return the result describing why the loop stopped
	 */
	public AgentResult run(Conversation conversation, AgentListener listener) {
		Objects.requireNonNull(conversation, "Conversation must not be null.");
		Objects.requireNonNull(listener, "Listener must not be null.");

		TokenUsage totalUsage = TokenUsage.EMPTY;
		int maxTurns = _config.maxTurns();

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted()) {
				return new AgentResult(StopReason.ABORTED, turn - 1, totalUsage);
			}

			listener.onTurnStart(turn);

			AssistantMessage assistantMsg;
			try {
				assistantMsg = callProvider(conversation, listener);
			} catch (ProviderException e) {
				listener.onError(e);
				listener.onTurnComplete(turn, TokenUsage.EMPTY);
				return new AgentResult(StopReason.ERROR, turn - 1, totalUsage);
			}

			conversation.add(assistantMsg);
			totalUsage = totalUsage.add(assistantMsg.usage());

			List<ToolCall> toolCalls = assistantMsg.content().stream()
				.filter(ToolCall.class::isInstance)
				.map(ToolCall.class::cast)
				.toList();

			if (toolCalls.isEmpty()) {
				listener.onTurnComplete(turn, assistantMsg.usage());
				return new AgentResult(StopReason.COMPLETE, turn, totalUsage);
			}

			for (ToolCall toolCall : toolCalls) {
				if (Thread.interrupted()) {
					listener.onTurnComplete(turn, assistantMsg.usage());
					return new AgentResult(StopReason.ABORTED, turn, totalUsage);
				}
				listener.onToolCallStart(toolCall);
				ToolResultMessage resultMsg = _toolExecutor.execute(toolCall, ToolContext.EMPTY);
				conversation.add(resultMsg);

				if (resultMsg.state() instanceof ToolState.Failed failed) {
					listener.onToolError(toolCall, failed.error());
				} else {
					listener.onToolCallComplete(toolCall, resultMsg);
				}
			}

			listener.onTurnComplete(turn, assistantMsg.usage());
			// TODO: check token budget, trigger compaction if approaching limit
		}

		return new AgentResult(StopReason.MAX_TURNS, maxTurns, totalUsage);
	}

	/**
	 * Call the LLM provider and collect the response.
	 *
	 * <p>This is the seam for retry logic (rate limiting, context overflow, backoff).
	 * Currently calls once and throws on error. Recovery strategies will wrap
	 * this method's internals with retry/compact/fallback logic.</p>
	 */
	private AssistantMessage callProvider(Conversation conversation, AgentListener listener)
			throws ProviderException {
		ProviderRequest request = new ProviderRequest(
			_config.model(),
			_config.systemPrompt(),
			conversation.messages(),
			_config.tools(),
			_config.maxOutputTokens(),
			_config.modelOptions());

		ResponseCollector collector = new ResponseCollector(listener);
		_provider.stream(request, collector);
		return collector.toAssistantMessage();
	}

	/**
	 * Internal handler that bridges {@link StreamHandler} events to {@link AgentListener}
	 * callbacks and accumulates data to build an {@link AssistantMessage}.
	 */
	private static class ResponseCollector implements StreamHandler {

		private final AgentListener _listener;

		private String _id;

		private String _finishReason;

		private TokenUsage _usage;

		private List<ContentPart> _content;

		ResponseCollector(AgentListener listener) {
			_listener = listener;
		}

		@Override
		public void onEvent(StreamEvent event) {
			switch (event) {
				case StreamEvent.TextDelta delta -> {
					_listener.onTextDelta(delta.text());
				}
				case StreamEvent.ThinkingDelta delta -> {
					_listener.onThinkingDelta(delta.text());
				}
				case StreamEvent.ToolCallStart start -> {
					// Accumulated in MessageComplete
				}
				case StreamEvent.ToolCallDelta delta -> {
					// Accumulated in MessageComplete
				}
				case StreamEvent.MessageComplete complete -> {
					_id = complete.id();
					_finishReason = complete.finishReason();
					_usage = complete.usage();
					_content = complete.content();
				}
			}
		}

		/**
		 * Build the {@link AssistantMessage} from the accumulated stream events.
		 *
		 * @throws IllegalStateException if no {@link StreamEvent.MessageComplete} was received
		 */
		AssistantMessage toAssistantMessage() {
			if (_content == null) {
				throw new IllegalStateException("No MessageComplete event received.");
			}
			return new AssistantMessage(
				_id != null ? _id : UUID.randomUUID().toString(),
				Instant.now(),
				_content,
				_usage,
				_finishReason != null ? _finishReason : "unknown");
		}
	}
}
