/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
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
import com.top_logic.tl.ai.core.tool.Tool;
import com.top_logic.tl.ai.core.tool.ToolMode;
import com.top_logic.tl.ai.core.tool.ToolRegistry;

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
 *   <li>Security check via {@link SecurityContext#canUseTool}</li>
 *   <li>Partition into concurrent/serial batches, execute</li>
 *   <li>Truncate oversized results, add to conversation</li>
 *   <li>Loop</li>
 * </ol>
 *
 * @see AgentConfig
 * @see AgentResult
 * @see AgentListener
 */
public class AgentLoop {

	// TODO: Retry with backoff for RATE_LIMITED / OVERLOADED (callProvider is the seam)
	// TODO: Memory/attachment injection between turns
	// TODO: Stop hooks (post-response validation)
	// TODO: Audit logging of tool executions — PRD FR-4 requires logging to an audit trail.
	//   Timing data (start/end Instant) is already tracked in ToolState. Needs a listener
	//   or dedicated AuditLogger that persists: who, which tool, arguments, result, duration.

	/** Default maximum characters per tool result before truncation. */
	public static final int DEFAULT_MAX_RESULT_CHARS = 4000;

	private final AgentConfig _config;

	private final LLMProvider _provider;

	private final ToolExecutor _toolExecutor;

	private final int _maxResultChars;

	private final ConversationMemory _memory;

	/**
	 * Creates a new agent loop with default result truncation.
	 *
	 * @param config       loop configuration
	 * @param provider     LLM provider for streaming responses
	 * @param toolExecutor executes tool calls requested by the LLM
	 */
	public AgentLoop(AgentConfig config, LLMProvider provider, ToolExecutor toolExecutor) {
		this(config, provider, toolExecutor, DEFAULT_MAX_RESULT_CHARS);
	}

	/**
	 * Creates a new agent loop with configurable result truncation.
	 *
	 * @param config         loop configuration
	 * @param provider       LLM provider for streaming responses
	 * @param toolExecutor   executes tool calls requested by the LLM
	 * @param maxResultChars maximum characters per tool result before truncation
	 */
	public AgentLoop(AgentConfig config, LLMProvider provider, ToolExecutor toolExecutor,
			int maxResultChars) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_toolExecutor = Objects.requireNonNull(toolExecutor, "Tool executor must not be null.");
		if (maxResultChars <= 0) {
			throw new IllegalArgumentException("maxResultChars must be positive.");
		}
		_maxResultChars = maxResultChars;
		_memory = null;
	}

	/**
	 * Creates a new agent loop with memory-aware context management.
	 *
	 * @param config       loop configuration
	 * @param provider     LLM provider for streaming responses
	 * @param toolExecutor executes tool calls requested by the LLM
	 * @param memory       conversation memory with token tracking and compaction
	 */
	public AgentLoop(AgentConfig config, LLMProvider provider, ToolExecutor toolExecutor,
			ConversationMemory memory) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_toolExecutor = Objects.requireNonNull(toolExecutor, "Tool executor must not be null.");
		_memory = Objects.requireNonNull(memory, "Memory must not be null.");
		_maxResultChars = DEFAULT_MAX_RESULT_CHARS;
	}

	/**
	 * Run the agent loop.
	 *
	 * <p>Blocks until the LLM completes without tool calls, the turn limit is reached,
	 * an unrecoverable error occurs, or the thread is interrupted.</p>
	 *
	 * @param conversation the conversation to operate on (messages are added during the run)
	 * @param context      execution context for the current user (security, transaction)
	 * @param listener     receives progress events
	 * @return the result describing why the loop stopped
	 */
	public AgentResult run(Conversation conversation, ToolContext context, AgentListener listener) {
		Objects.requireNonNull(conversation, "Conversation must not be null.");
		Objects.requireNonNull(context, "Context must not be null.");
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

			if (!executeToolCalls(toolCalls, context, conversation, listener)) {
				listener.onTurnComplete(turn, assistantMsg.usage());
				return new AgentResult(StopReason.ABORTED, turn, totalUsage);
			}

			listener.onTurnComplete(turn, assistantMsg.usage());
		}

		return new AgentResult(StopReason.MAX_TURNS, maxTurns, totalUsage);
	}

	/**
	 * Run the agent loop with an empty (unrestricted) context.
	 *
	 * <p>Convenience method for tests and trusted internal use.</p>
	 */
	public AgentResult run(Conversation conversation, AgentListener listener) {
		return run(conversation, ToolContext.EMPTY, listener);
	}

	/**
	 * Run the agent loop using the configured {@link ConversationMemory}.
	 *
	 * <p>Uses the memory's conversation for message history. After each LLM response,
	 * updates the token baseline. At the end of each turn, checks whether compaction
	 * is needed and triggers it automatically.</p>
	 *
	 * @param context  execution context for the current user
	 * @param listener receives progress events
	 * @return the result describing why the loop stopped
	 * @throws IllegalStateException if this loop was not created with a ConversationMemory
	 */
	public AgentResult run(ToolContext context, AgentListener listener) {
		if (_memory == null) {
			throw new IllegalStateException(
				"This AgentLoop was not created with a ConversationMemory. "
				+ "Use the constructor that accepts ConversationMemory, "
				+ "or call run(Conversation, ToolContext, AgentListener) instead.");
		}
		return runWithMemory(_memory, context, listener);
	}

	/** Maximum reactive compaction retries on context_window_exceeded before giving up. */
	private static final int MAX_REACTIVE_COMPACT_RETRIES = 2;

	private AgentResult runWithMemory(ConversationMemory memory, ToolContext context,
			AgentListener listener) {
		Conversation conversation = memory.conversation();
		TokenUsage totalUsage = TokenUsage.EMPTY;
		int maxTurns = _config.maxTurns();

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted()) {
				return new AgentResult(StopReason.ABORTED, turn - 1, totalUsage);
			}

			listener.onTurnStart(turn);

			AssistantMessage assistantMsg;
			try {
				assistantMsg = callProviderWithReactiveCompaction(
					conversation, memory, listener);
			} catch (ProviderException e) {
				listener.onError(e);
				listener.onTurnComplete(turn, TokenUsage.EMPTY);
				return new AgentResult(StopReason.ERROR, turn - 1, totalUsage);
			}

			conversation.add(assistantMsg);
			totalUsage = totalUsage.add(assistantMsg.usage());
			memory.updateTokenBaseline(assistantMsg.usage());

			List<ToolCall> toolCalls = assistantMsg.content().stream()
				.filter(ToolCall.class::isInstance)
				.map(ToolCall.class::cast)
				.toList();

			if (toolCalls.isEmpty()) {
				listener.onTurnComplete(turn, assistantMsg.usage());
				return new AgentResult(StopReason.COMPLETE, turn, totalUsage);
			}

			if (!executeToolCalls(toolCalls, context, conversation, listener)) {
				listener.onTurnComplete(turn, assistantMsg.usage());
				return new AgentResult(StopReason.ABORTED, turn, totalUsage);
			}

			if (memory.shouldCompact()) {
				int tokensBefore = memory.estimateTokens();
				listener.onCompactionStart(tokensBefore);
				CompactionResult compactionResult = memory.compact();
				listener.onCompactionComplete(compactionResult);
			}

			listener.onTurnComplete(turn, assistantMsg.usage());
		}

		return new AgentResult(StopReason.MAX_TURNS, maxTurns, totalUsage);
	}

	/**
	 * Call provider with reactive compaction: if the API returns
	 * {@link ProviderException.Kind#CONTEXT_WINDOW_EXCEEDED}, compact and retry.
	 */
	private AssistantMessage callProviderWithReactiveCompaction(
			Conversation conversation, ConversationMemory memory,
			AgentListener listener) throws ProviderException {
		for (int attempt = 0; attempt <= MAX_REACTIVE_COMPACT_RETRIES; attempt++) {
			try {
				return callProvider(conversation, listener);
			} catch (ProviderException e) {
				if (e.kind() != ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED
						|| attempt == MAX_REACTIVE_COMPACT_RETRIES) {
					throw e;
				}
				int tokensBefore = memory.estimateTokens();
				listener.onCompactionStart(tokensBefore);
				CompactionResult result = memory.compact("reactive");
				listener.onCompactionComplete(result);
			}
		}
		throw new IllegalStateException("Unreachable");
	}

	/**
	 * @return {@code true} if all tool calls were executed, {@code false} if interrupted
	 */
	private boolean executeToolCalls(List<ToolCall> toolCalls, ToolContext context,
			Conversation conversation, AgentListener listener) {

		List<List<ToolCall>> batches = partitionBatches(toolCalls);

		for (List<ToolCall> batch : batches) {
			if (Thread.interrupted()) {
				Thread.currentThread().interrupt();
				return false;
			}
			if (batch.size() == 1) {
				executeSingle(batch.get(0), context, conversation, listener);
			} else {
				executeConcurrent(batch, context, conversation, listener);
			}
		}
		return true;
	}

	/**
	 * Partition tool calls into batches: consecutive concurrency-safe tools are grouped
	 * together, non-safe tools get their own batch.
	 */
	private List<List<ToolCall>> partitionBatches(List<ToolCall> toolCalls) {
		List<List<ToolCall>> batches = new ArrayList<>();
		List<ToolCall> currentBatch = null;
		boolean currentBatchConcurrent = false;

		for (ToolCall tc : toolCalls) {
			boolean safe = isConcurrencySafe(tc);

			if (currentBatch == null) {
				currentBatch = new ArrayList<>();
				currentBatch.add(tc);
				currentBatchConcurrent = safe;
			} else if (safe && currentBatchConcurrent) {
				currentBatch.add(tc);
			} else {
				batches.add(currentBatch);
				currentBatch = new ArrayList<>();
				currentBatch.add(tc);
				currentBatchConcurrent = safe;
			}
		}
		if (currentBatch != null) {
			batches.add(currentBatch);
		}
		return batches;
	}

	private boolean isConcurrencySafe(ToolCall tc) {
		if (_toolExecutor instanceof ToolRegistry registry) {
			Tool tool = registry.getTool(tc.name());
			return tool != null && tool.isConcurrencySafe();
		}
		return false;
	}

	private void executeSingle(ToolCall toolCall, ToolContext context,
			Conversation conversation, AgentListener listener) {

		ToolResultMessage result = executeWithSecurityCheck(toolCall, context, listener);
		conversation.add(result);
		reportResult(toolCall, result, listener);
	}

	private void executeConcurrent(List<ToolCall> batch, ToolContext context,
			Conversation conversation, AgentListener listener) {

		ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
		try {
			List<CompletableFuture<ToolResultMessage>> futures = new ArrayList<>();

			for (ToolCall tc : batch) {
				listener.onToolCallStart(tc);
				futures.add(CompletableFuture.supplyAsync(
					() -> executeWithSecurityCheck(tc, context, null), executor));
			}

			List<ToolResultMessage> results = new ArrayList<>();
			for (CompletableFuture<ToolResultMessage> future : futures) {
				try {
					results.add(future.join());
				} catch (Exception e) {
					results.add(errorResult(batch.get(results.size()), "Concurrent execution failed: " + e.getMessage()));
				}
			}

			for (int i = 0; i < batch.size(); i++) {
				conversation.add(results.get(i));
				reportResult(batch.get(i), results.get(i), listener);
			}
		} finally {
			executor.close();
		}
	}

	private ToolResultMessage executeWithSecurityCheck(ToolCall toolCall, ToolContext context,
			AgentListener listener) {

		if (listener != null) {
			listener.onToolCallStart(toolCall);
		}

		// Step 1: User-level access check (future per-user security)
		SecurityContext security = context.security();
		if (_toolExecutor instanceof ToolRegistry registry) {
			Tool tool = registry.getTool(toolCall.name());
			if (tool != null) {
				if (!security.canUseTool(tool)) {
					String reason = "Permission denied: you do not have access to tool '" + toolCall.name() + "'.";
					if (listener != null) {
						listener.onPermissionDenied(toolCall, reason);
					}
					return errorResult(toolCall, reason);
				}

				// Step 2: System mode gate
				if (tool.mode() == ToolMode.GLOBAL
						&& security.executionMode() == ExecutionMode.PRODUCTION) {
					String reason = "Tool '" + toolCall.name() + "' cannot execute: it modifies shared "
						+ "application state, which requires maintenance or development mode. "
						+ "The system is currently in production mode.";
					if (listener != null) {
						listener.onPermissionDenied(toolCall, reason);
					}
					return errorResult(toolCall, reason);
				}

				// Step 3: Approval check
				PermissionDecision decision = security.checkApproval(tool, toolCall);
				switch (decision) {
					case PermissionDecision.Denied denied -> {
						String reason = denied.reason();
						if (listener != null) {
							listener.onPermissionDenied(toolCall, reason);
						}
						return errorResult(toolCall, reason);
					}
					case PermissionDecision.NeedsApproval needsApproval -> {
						ApprovalRequest request = new ApprovalRequest(
							tool.name(), toolCall.id(),
							Map.of(), tool.mode(),
							needsApproval.displayReason());
						if (listener != null) {
							listener.onApprovalRequired(toolCall, request);
						}
						ApprovalResult approvalResult = context.approval().requestApproval(request);
						switch (approvalResult) {
							case ApprovalResult.Approved approved -> {
								// proceed to execution
							}
							case ApprovalResult.Rejected rejected -> {
								String feedback = rejected.userComment().isBlank()
									? "User denied tool '" + toolCall.name() + "'."
									: "User denied tool '" + toolCall.name() + "': "
										+ rejected.userComment();
								return errorResult(toolCall, feedback);
							}
						}
					}
					case PermissionDecision.Allowed allowed -> {
						// proceed to execution
					}
				}
			}
		}

		ToolResultMessage result = _toolExecutor.execute(toolCall, context);
		return truncateIfNeeded(result);
	}

	private ToolResultMessage truncateIfNeeded(ToolResultMessage result) {
		if (!(result.state() instanceof ToolState.Completed completed)) {
			return result;
		}
		String text = completed.result();
		if (text.length() <= _maxResultChars) {
			return result;
		}

		String truncated = text.substring(0, _maxResultChars)
			+ "\n\n[Result truncated: " + text.length() + " chars total, showing first " + _maxResultChars + "]";

		return new ToolResultMessage(
			result.id(), result.timestamp(), result.toolCallId(), result.toolName(),
			new ToolState.Completed(truncated, completed.startTime(), completed.endTime(), false));
	}

	private static ToolResultMessage errorResult(ToolCall toolCall, String error) {
		Instant now = Instant.now();
		return new ToolResultMessage(
			UUID.randomUUID().toString(), now,
			toolCall.id(), toolCall.name(),
			new ToolState.Failed(error, now, now));
	}

	private static void reportResult(ToolCall toolCall, ToolResultMessage result,
			AgentListener listener) {
		if (result.state() instanceof ToolState.Failed failed) {
			listener.onToolError(toolCall, failed.error());
		} else {
			listener.onToolCallComplete(toolCall, result);
		}
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
