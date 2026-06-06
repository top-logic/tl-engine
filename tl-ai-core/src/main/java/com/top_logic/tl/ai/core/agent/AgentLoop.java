/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
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
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.interaction.LoadedSkillsBinding;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;
import com.top_logic.tl.ai.tool.api.ProgressReporter;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Core agent loop: calls the LLM, executes tool calls, feeds results back, repeats.
 *
 * <p>Blocks the calling thread for the entire run. Progress is reported via
 * {@link AgentEventListener} callbacks on the same thread.</p>
 *
 * <p>One iteration:</p>
 * <ol>
 *   <li>Build {@link ProviderRequest} from conversation + config</li>
 *   <li>Call {@link LLMProvider#stream} — streams via internal handler</li>
 *   <li>Assemble {@link AssistantMessage}, add to conversation</li>
 *   <li>Extract {@link ToolCall}s — if none, stop</li>
 *   <li>Security check via {@link SecurityContext#hasToolPermission}</li>
 *   <li>Partition into concurrent/serial batches, execute</li>
 *   <li>Truncate oversized results, add to conversation</li>
 *   <li>Loop</li>
 * </ol>
 *
 * @see AgentParams
 * @see AgentResult
 * @see AgentEventListener
 */
public class AgentLoop {

	// TODO: Memory/attachment injection between turns

	/** Default maximum characters per tool result before truncation. */
	public static final int DEFAULT_MAX_RESULT_CHARS = 20000;

	private final AgentParams _config;

	private final LLMProvider _provider;

	private final ToolRegistry _registry;

	private final int _maxResultChars;

	private final ConversationMemory _memory;

	private final Set<String> _sessionApprovedTools = ConcurrentHashMap.newKeySet();

	/** Tool call IDs denied in the current turn; cleared at the start of each turn's tool execution. */
	private final Set<String> _turnDeniedCallIds = ConcurrentHashMap.newKeySet();

	/**
	 * Tracks recent tool calls by signature ({@code name + "\0" + arguments}) to detect
	 * duplicate calls. Maps signature to consecutive repeat count. Reset when a different
	 * tool call is made.
	 */
	private final Map<String, Integer> _recentToolCallCounts = new LinkedHashMap<>();

	/** Maximum times the same tool+args combination may be called before being blocked. */
	private static final int MAX_DUPLICATE_TOOL_CALLS = 2;

	/**
	 * Creates a new agent loop with default result truncation.
	 *
	 * @param config   loop configuration
	 * @param provider LLM provider for streaming responses
	 * @param registry tool registry for tool lookup and execution
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry) {
		this(config, provider, registry, DEFAULT_MAX_RESULT_CHARS);
	}

	/**
	 * Creates a new agent loop with configurable result truncation.
	 *
	 * @param config         loop configuration
	 * @param provider       LLM provider for streaming responses
	 * @param registry       tool registry for tool lookup and execution
	 * @param maxResultChars maximum characters per tool result before truncation
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry,
			int maxResultChars) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_registry = Objects.requireNonNull(registry, "Registry must not be null.");
		if (maxResultChars <= 0) {
			throw new IllegalArgumentException("maxResultChars must be positive.");
		}
		_maxResultChars = maxResultChars;
		_memory = null;
	}

	/**
	 * Creates a new agent loop with memory-aware context management.
	 *
	 * @param config   loop configuration
	 * @param provider LLM provider for streaming responses
	 * @param registry tool registry for tool lookup and execution
	 * @param memory   conversation memory with token tracking and compaction
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry,
			ConversationMemory memory) {
		this(config, provider, registry, memory, DEFAULT_MAX_RESULT_CHARS);
	}

	/**
	 * Creates a new agent loop with memory-aware context management and configurable result
	 * truncation.
	 *
	 * @param config           loop configuration
	 * @param provider         LLM provider for streaming responses
	 * @param registry         tool registry for tool lookup and execution
	 * @param memory           conversation memory with token tracking and compaction
	 * @param maxResultChars   maximum characters per tool result before truncation
	 */
	public AgentLoop(AgentParams config, LLMProvider provider, ToolRegistry registry,
			ConversationMemory memory, int maxResultChars) {
		_config = Objects.requireNonNull(config, "Config must not be null.");
		_provider = Objects.requireNonNull(provider, "Provider must not be null.");
		_registry = Objects.requireNonNull(registry, "Registry must not be null.");
		_memory = Objects.requireNonNull(memory, "Memory must not be null.");
		if (maxResultChars <= 0) {
			throw new IllegalArgumentException("maxResultChars must be positive.");
		}
		_maxResultChars = maxResultChars;
	}

	private static Instant now() {
		return Instant.now();
	}

	private static void emit(AgentEventListener listener, AgentEvent event) {
		listener.onEvent(event);
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
	public AgentResult run(Conversation conversation, ToolContext context, AgentEventListener listener) {
		Objects.requireNonNull(conversation, "Conversation must not be null.");
		Objects.requireNonNull(context, "Context must not be null.");
		Objects.requireNonNull(listener, "Listener must not be null.");

		_sessionApprovedTools.addAll(context.sessionApprovedTools());

		listener = AgentEventListener.serializing(listener);
		String runId = UUID.randomUUID().toString();
		emit(listener, new AgentEvent.RunStarted(runId, 0, now(), _config.model()));

		TokenUsage totalUsage = TokenUsage.EMPTY;
		int maxTurns = _config.maxTurns();

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted() || context.cancellationToken().isCancelled()) {
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			emit(listener, new AgentEvent.TurnStarted(runId, turn, now()));

			AssistantMessage assistantMsg;
			try {
				assistantMsg = callProvider(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				emit(listener, new AgentEvent.ProviderError(runId, turn, now(), e.kind().name(), e.getMessage()));
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), TokenUsage.EMPTY, List.of()));
				StopReason stopReason = StopReason.ERROR;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			conversation.add(assistantMsg);
			totalUsage = totalUsage.add(assistantMsg.usage());

			if (!_config.responseValidator().test(assistantMsg)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.REJECTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			List<ToolCall> toolCalls = assistantMsg.content().stream()
				.filter(ToolCall.class::isInstance)
				.map(ToolCall.class::cast)
				.toList();

			if (toolCalls.isEmpty()) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.COMPLETE;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			if (!executeToolCalls(toolCalls, context, conversation, listener, runId, turn)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
		}

		StopReason stopReason = StopReason.MAX_TURNS;
		emit(listener, new AgentEvent.RunCompleted(runId, maxTurns, now(), stopReason, totalUsage));
		return new AgentResult(stopReason, maxTurns, totalUsage);
	}

	/**
	 * Run the agent loop with an empty (unrestricted) context.
	 *
	 * <p>Convenience method for tests and trusted internal use.</p>
	 */
	public AgentResult run(Conversation conversation, AgentEventListener listener) {
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
	public AgentResult run(ToolContext context, AgentEventListener listener) {
		if (_memory == null) {
			throw new IllegalStateException(
				"This AgentLoop was not created with a ConversationMemory. "
				+ "Use the constructor that accepts ConversationMemory, "
				+ "or call run(Conversation, ToolContext, AgentEventListener) instead.");
		}
		Objects.requireNonNull(context, "Context must not be null.");
		Objects.requireNonNull(listener, "Listener must not be null.");

		_sessionApprovedTools.addAll(context.sessionApprovedTools());

		listener = AgentEventListener.serializing(listener);
		String runId = UUID.randomUUID().toString();
		emit(listener, new AgentEvent.RunStarted(runId, 0, now(), _config.model()));

		return runWithMemory(_memory, context, listener, runId);
	}

	/** Maximum reactive compaction retries on context_window_exceeded before giving up. */
	private static final int MAX_REACTIVE_COMPACT_RETRIES = 2;

	/** Maximum automatic retries for transient provider errors (rate limit, overloaded). */
	private static final int MAX_TRANSIENT_RETRIES = 3;

	/** Base backoff delay in milliseconds for transient retries. Doubles each attempt. */
	private static final long BASE_BACKOFF_MS = 1000;

	private AgentResult runWithMemory(ConversationMemory memory, ToolContext context,
			AgentEventListener listener, String runId) {
		Conversation conversation = memory.conversation();
		TokenUsage totalUsage = TokenUsage.EMPTY;
		int maxTurns = _config.maxTurns();

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted() || context.cancellationToken().isCancelled()) {
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			emit(listener, new AgentEvent.TurnStarted(runId, turn, now()));

			AssistantMessage assistantMsg;
			try {
				assistantMsg = callProviderWithReactiveCompaction(
					conversation, context, memory, listener, runId, turn);
			} catch (ProviderException e) {
				emit(listener, new AgentEvent.ProviderError(runId, turn, now(), e.kind().name(), e.getMessage()));
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), TokenUsage.EMPTY, List.of()));
				StopReason stopReason = e.kind() == ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED
					? StopReason.CONTEXT_OVERFLOW
					: StopReason.ERROR;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			conversation.add(assistantMsg);
			totalUsage = totalUsage.add(assistantMsg.usage());
			memory.updateTokenBaseline(assistantMsg.usage());

			if (!_config.responseValidator().test(assistantMsg)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.REJECTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			List<ToolCall> toolCalls = assistantMsg.content().stream()
				.filter(ToolCall.class::isInstance)
				.map(ToolCall.class::cast)
				.toList();

			if (toolCalls.isEmpty()) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.COMPLETE;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			if (!executeToolCalls(toolCalls, context, conversation, listener, runId, turn)) {
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
			}

			if (memory.shouldCompact()) {
				int tokensBefore = memory.estimateTokens();
				emit(listener, new AgentEvent.CompactionStarted(runId, turn, now(), tokensBefore, "proactive"));
				CompactionResult compactionResult = memory.compact();
				emit(listener, new AgentEvent.CompactionCompleted(runId, turn, now(), compactionResult));
			}

			emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), assistantMsg.usage(), assistantMsg.content()));
		}

		StopReason stopReason = StopReason.MAX_TURNS;
		emit(listener, new AgentEvent.RunCompleted(runId, maxTurns, now(), stopReason, totalUsage));
		return new AgentResult(stopReason, maxTurns, totalUsage);
	}

	/**
	 * Call provider with reactive compaction: if the API returns
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#CONTEXT_WINDOW_EXCEEDED},
	 * compact and retry.
	 */
	private AssistantMessage callProviderWithReactiveCompaction(
			Conversation conversation, ToolContext context, ConversationMemory memory,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		for (int attempt = 0; attempt <= MAX_REACTIVE_COMPACT_RETRIES; attempt++) {
			try {
				return callProvider(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				if (e.kind() != ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED
						|| attempt == MAX_REACTIVE_COMPACT_RETRIES) {
					throw e;
				}
				int tokensBefore = memory.estimateTokens();
				emit(listener, new AgentEvent.CompactionStarted(runId, turn, now(), tokensBefore, "reactive"));
				CompactionResult result = memory.compact("reactive");
				emit(listener, new AgentEvent.CompactionCompleted(runId, turn, now(), result));
			}
		}
		throw new IllegalStateException("Unreachable");
	}

	/**
	 * @return {@code true} if all tool calls were executed, {@code false} if interrupted
	 */
	private boolean executeToolCalls(List<ToolCall> toolCalls, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		_turnDeniedCallIds.clear();

		List<List<ToolCall>> batches = partitionBatches(toolCalls, context);
		int messagesBefore = conversation.size();

		for (List<ToolCall> batch : batches) {
			if (Thread.interrupted() || context.cancellationToken().isCancelled()) {
				Thread.currentThread().interrupt();
				rollbackTransaction(context, listener, runId, turn, "Interrupted");
				return false;
			}

			if (batch.size() == 1) {
				executeSingle(batch.get(0), context, conversation, listener, runId, turn);
			} else {
				executeConcurrent(batch, context, conversation, listener, runId, turn);
			}
		}

		// End-of-turn: check for failures and commit or rollback
		boolean anyFailed = conversation.messages().subList(messagesBefore, conversation.size())
			.stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed);

		if (anyFailed) {
			rollbackTransaction(context, listener, runId, turn, "Tool execution failed");
		} else {
			commitTransaction(context, listener, runId, turn);
		}

		return true;
	}

	/**
	 * Ensure a transaction is active before executing a write tool.
	 * Begins a transaction on first write. Locking is handled by individual
	 * tools via {@link TransactionScope#acquireLock(String, com.top_logic.model.TLObject...)}.
	 *
	 * @param toolCall the tool call about to execute
	 * @param tool     the resolved tool (may be {@code null} for unknown tools)
	 * @param context  the current execution context
	 * @param listener receives transaction events
	 * @param runId    the current run ID
	 * @param turn     the current turn number
	 * @return always {@code null}; retained for interface consistency with callers
	 *         that check the return value
	 */
	private ToolResultMessage ensureTransaction(ToolCall toolCall, Tool tool, ToolContext context,
			AgentEventListener listener, String runId, int turn) {
		TransactionScope scope = context.transaction();
		if (scope.isNoOp() || scope.isActive()) {
			return null;
		}

		if (tool == null || tool.mode() == ToolMode.READ) {
			return null;
		}

		scope.begin();
		emit(listener, new AgentEvent.TransactionBegan(runId, turn, now()));
		return null;
	}

	/** */
	private void commitTransaction(ToolContext context, AgentEventListener listener, String runId, int turn) {
		TransactionScope scope = context.transaction();
		if (!scope.isActive()) {
			return;
		}
		try {
			scope.commit();
			Revision rev = scope.commitRevision();
			long revNum = rev != null ? rev.getCommitNumber() : -1;
			emit(listener, new AgentEvent.TransactionCommitted(runId, turn, now(), revNum));
		} catch (TransactionException e) {
			scope.rollback();
			emit(listener, new AgentEvent.TransactionRolledBack(runId, turn, now(), "Commit failed: " + e.getMessage()));
		}
	}

	/** */
	private void rollbackTransaction(ToolContext context, AgentEventListener listener, String runId, int turn,
			String reason) {
		TransactionScope scope = context.transaction();
		if (!scope.isActive()) {
			return;
		}
		scope.rollback();
		emit(listener, new AgentEvent.TransactionRolledBack(runId, turn, now(), reason));
	}

	/**
	 * Partition tool calls into batches: consecutive concurrency-safe tools are grouped
	 * together, non-safe tools get their own batch.
	 *
	 * <p>A tool is considered safe for concurrent execution when it reports itself as
	 * concurrency-safe AND will not require interactive approval (which would block
	 * the thread waiting for user input).</p>
	 */
	List<List<ToolCall>> partitionBatches(List<ToolCall> toolCalls, ToolContext context) {
		List<List<ToolCall>> batches = new ArrayList<>();
		List<ToolCall> currentBatch = null;
		boolean currentBatchConcurrent = false;

		for (ToolCall tc : toolCalls) {
			boolean safe = isConcurrencySafe(tc, context);

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

	boolean isConcurrencySafe(ToolCall tc, ToolContext context) {
		Tool tool = _registry.getTool(tc.name());
		if (tool == null) {
			return false;
		}
		if (tool.mode() != ToolMode.READ) {
			// Only READ tools share concurrent batches. USER_SCOPED / DATA_WRITE / GLOBAL
			// tools trigger ensureTransaction, which opens a KB transaction in the worker's
			// own (interaction-scoped) DBContext; that transaction is auto-rolled-back when
			// the worker's interaction unbinds, leaving the shared TransactionScope holding
			// a zombie TX that the agent's end-of-turn commit then fails on with
			// "Invalid nesting of transactions".
			return false;
		}
		if (!tool.isConcurrencySafe()) {
			return false;
		}
		// Session-approved tools don't need interactive approval.
		if (_sessionApprovedTools.contains(tool.name())) {
			return true;
		}
		// Check if the security context auto-approves this tool.
		PermissionDecision decision = context.security().checkApproval(tool, tc);
		return decision instanceof PermissionDecision.Allowed;
	}

	private void executeSingle(ToolCall toolCall, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		ToolResultMessage duplicateResult = checkDuplicateToolCall(toolCall);
		if (duplicateResult != null) {
			conversation.add(duplicateResult);
			reportResult(toolCall, duplicateResult, listener, runId, turn, _turnDeniedCallIds);
			return;
		}

		ToolResultMessage result = executeWithSecurityCheck(toolCall, context, listener, runId, turn);
		conversation.add(result);
		reportResult(toolCall, result, listener, runId, turn, _turnDeniedCallIds);
	}

	/**
	 * Checks if this tool call is a duplicate of a recent call with the same name and arguments.
	 * Returns a synthetic error result if the call has been made too many times, {@code null}
	 * otherwise.
	 *
	 * <p>Allows each unique tool+args combination up to {@link #MAX_DUPLICATE_TOOL_CALLS} times
	 * (to account for legitimate retries). Beyond that, the call is blocked with a message
	 * directing the LLM to use the result it already received.</p>
	 */
	private ToolResultMessage checkDuplicateToolCall(ToolCall toolCall) {
		String signature = toolCall.name() + "\0" + toolCall.arguments();
		int count = _recentToolCallCounts.merge(signature, 1, Integer::sum);

		if (count > MAX_DUPLICATE_TOOL_CALLS) {
			return errorResult(toolCall,
				"DUPLICATE CALL BLOCKED: You have already called '" + toolCall.name()
				+ "' with these exact arguments " + (count - 1) + " time(s). "
				+ "The result is already in your conversation. "
				+ "Use the result you already received instead of calling again. "
				+ "If you need different information, call the tool with DIFFERENT arguments.");
		}

		return null;
	}

	private void executeConcurrent(List<ToolCall> batch, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		// Capture thread-local bindings from the calling thread for propagation.
		ConversationLogListener logListener = ConversationLogListenerBinding.getListener();
		TaskStore taskStore = TaskStoreBinding.getStore();
		UserInteractionStore interactionStore = UserInteractionStoreBinding.getStore();
		Set<String> loadedSkills = LoadedSkillsBinding.getSkills();

		ExecutorService executor = Executors.newCachedThreadPool();
		try {
			List<CompletableFuture<ToolResultMessage>> futures = new ArrayList<>();

			for (ToolCall tc : batch) {
				futures.add(CompletableFuture.supplyAsync(
					() -> ThreadContextManager.inSystemInteraction(AgentLoop.class, () -> {
						try (
							ConversationLogListenerBinding.Binding b1 =
								ConversationLogListenerBinding.bind(logListener);
							TaskStoreBinding.Binding b2 =
								TaskStoreBinding.bind(taskStore);
							UserInteractionStoreBinding.Binding b3 =
								UserInteractionStoreBinding.bind(interactionStore);
							LoadedSkillsBinding.Binding b4 =
								LoadedSkillsBinding.bind(loadedSkills)) {
							return executeWithSecurityCheck(tc, context, listener, runId, turn);
						}
					}), executor));
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
				reportResult(batch.get(i), results.get(i), listener, runId, turn, _turnDeniedCallIds);
			}
		} finally {
			executor.shutdown();
		}
	}

	private ToolResultMessage executeWithSecurityCheck(ToolCall toolCall, ToolContext context,
			AgentEventListener listener, String runId, int turn) {

		SecurityContext security = context.security();

		// Step 1: Permission + mode gate
		Tool tool = _registry.getTool(toolCall.name());

		if (tool != null) {
			ToolPermissionFilter filter = new ToolPermissionFilter(security);
			if (!filter.test(tool)) {
				String reason = filter.denialReason(tool);
				emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), reason));
				_turnDeniedCallIds.add(toolCall.id());
				return errorResult(toolCall, reason);
			}
		} else {
			if (!security.hasToolPermission(toolCall.name())) {
				String reason = "Permission denied: you do not have access to tool '"
					+ toolCall.name() + "'.";
				emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), reason));
				_turnDeniedCallIds.add(toolCall.id());
				return errorResult(toolCall, reason);
			}
		}

		// Step 2: Approval (only for registered tools with metadata)
		if (tool != null) {
			// Approval check
			// Check session memory first — skip approval if already approved for session
			if (_sessionApprovedTools.contains(tool.name())) {
				// Auto-approved for this session: emit a resolved event so listeners
				// still see an approval signal on repeat invocations, even though no
				// user interaction happens.
				emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
					toolCall.id(), UUID.randomUUID().toString(),
					ApprovalOutcome.APPROVED_FOR_SESSION));
			} else {
				PermissionDecision decision = security.checkApproval(tool, toolCall);
				if (decision instanceof PermissionDecision.Denied denied) {
					String reason = denied.reason();
					emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), reason));
					_turnDeniedCallIds.add(toolCall.id());
					return errorResult(toolCall, reason);
				} else if (decision instanceof PermissionDecision.NeedsApproval needsApproval) {
					Map<String, Object> parsedArgs;
					try {
						parsedArgs = ToolCatalog.parseJson(toolCall.arguments());
					} catch (IOException e) {
						parsedArgs = Map.of();
					}
					String approvalRequestId = UUID.randomUUID().toString();
					ApprovalRequest request = new ApprovalRequest(
						approvalRequestId,
						tool.name(), toolCall.id(),
						parsedArgs, tool.mode(),
						needsApproval.displayReason(),
						ApprovalRequest.DEFAULT_TIMEOUT);
					emit(listener, new AgentEvent.ApprovalRequired(runId, turn, now(),
						toolCall.id(), approvalRequestId, toolCall.name(),
						Map.copyOf(request.arguments()), request.displayReason()));
					ApprovalResult approvalResult = context.approval().requestApproval(request);
					if (approvalResult instanceof ApprovalResult.Approved) {
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.APPROVED));
						// proceed to execution
					} else if (approvalResult instanceof ApprovalResult.ApprovedForSession) {
						_sessionApprovedTools.add(tool.name());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.APPROVED_FOR_SESSION));
						// proceed to execution
					} else if (approvalResult instanceof ApprovalResult.Rejected rejected) {
						String feedback = rejected.userComment().isBlank()
							? "User denied tool '" + toolCall.name() + "'."
							: "User denied tool '" + toolCall.name() + "': "
								+ rejected.userComment();
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.REJECTED));
						emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), feedback));
						_turnDeniedCallIds.add(toolCall.id());
						return errorResult(toolCall, feedback);
					} else if (approvalResult instanceof ApprovalResult.TimedOut) {
						String feedback = "Approval timed out for tool '" + toolCall.name() + "'.";
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.TIMED_OUT));
						emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), feedback));
						_turnDeniedCallIds.add(toolCall.id());
						return errorResult(toolCall, feedback);
					}
				}
				// PermissionDecision.Allowed: proceed to execution
			}
		}

		// All security and approval gates passed — signal that the call is about to run.
		emit(listener, new AgentEvent.ToolCallStarted(runId, turn, now(),
			toolCall.id(), toolCall.name(), toolCall.arguments()));

		// Ensure transaction is open before executing a write tool.
		// Done here (after all security gates) so denied tools never open a transaction.
		ToolResultMessage lockError = ensureTransaction(toolCall, tool, context, listener, runId, turn);
		if (lockError != null) {
			return lockError;
		}

		ProgressReporter progressReporter = message ->
			emit(listener, new AgentEvent.ToolProgress(runId, turn, now(),
				toolCall.id(), toolCall.name(), message));
		ToolContext execContext = context.withToolCallId(toolCall.id())
			.withProgressReporter(progressReporter);

		ToolResultMessage result = _registry.execute(toolCall, execContext);
		return truncateIfNeeded(result, listener, runId, turn);
	}

	private ToolResultMessage truncateIfNeeded(ToolResultMessage result,
			AgentEventListener listener, String runId, int turn) {
		if (!(result.state() instanceof ToolState.Completed completed)) {
			return result;
		}
		String text = completed.result();
		int limit = completed.truncationHint() > 0 ? completed.truncationHint() : _maxResultChars;
		if (text.length() <= limit) {
			return result;
		}

		int halfBudget = limit / 2;
		String head = text.substring(0, halfBudget);
		String tail = text.substring(text.length() - halfBudget);
		int droppedChars = text.length() - head.length() - tail.length();
		String truncated = head
			+ "\n\n[... " + droppedChars + " chars truncated ...]\n\n"
			+ tail;

		emit(listener, new AgentEvent.ToolCallResultTruncated(runId, turn, now(),
			result.toolCallId(), result.toolName(), text.length(), truncated.length()));

		return new ToolResultMessage(
			result.id(), result.timestamp(), result.toolCallId(), result.toolName(),
			new ToolState.Completed(truncated, completed.startTime(), completed.endTime(),
				false, completed.summary(), -1));
	}

	private static ToolResultMessage errorResult(ToolCall toolCall, String error) {
		Instant now = Instant.now();
		return new ToolResultMessage(
			UUID.randomUUID().toString(), now,
			toolCall.id(), toolCall.name(),
			new ToolState.Failed(error, now, now));
	}

	private static void reportResult(ToolCall toolCall, ToolResultMessage result,
			AgentEventListener listener, String runId, int turnNumber,
			Set<String> deniedCallIds) {
		if (deniedCallIds.contains(toolCall.id())) {
			return;
		}
		if (result.state() instanceof ToolState.Failed failed) {
			Duration d = Duration.between(failed.startTime(), failed.endTime());
			emit(listener, new AgentEvent.ToolCallFailed(
				runId, turnNumber, now(), toolCall.id(), toolCall.name(), failed.error(), d));
		} else if (result.state() instanceof ToolState.Completed completed) {
			Duration d = Duration.between(completed.startTime(), completed.endTime());
			emit(listener, new AgentEvent.ToolCallCompleted(
				runId, turnNumber, now(), toolCall.id(), toolCall.name(), completed.result(), d));
		}
	}

	/**
	 * Call the LLM provider with automatic retry for transient errors.
	 *
	 * <p>
	 * Retries on {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#RATE_LIMITED} and
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#OVERLOADED} with exponential
	 * backoff, respecting the {@code retry-after} header when present.
	 * </p>
	 */
	private AssistantMessage callProvider(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		for (int attempt = 0; attempt <= MAX_TRANSIENT_RETRIES; attempt++) {
			try {
				return callProviderOnce(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				boolean retryable = e.kind() == ProviderException.Kind.RATE_LIMITED
					|| e.kind() == ProviderException.Kind.OVERLOADED;
				if (!retryable || attempt == MAX_TRANSIENT_RETRIES) {
					throw e;
				}
				long waitMs = e.retryAfter() != null
					? e.retryAfter().toMillis()
					: BASE_BACKOFF_MS * (1L << attempt);
				// Cap at 30 seconds.
				waitMs = Math.min(waitMs, 30_000);

				emit(listener, new AgentEvent.RetryScheduled(
					runId, turn, now(), e.kind().name(), attempt + 1, waitMs));

				try {
					Thread.sleep(waitMs);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
					throw new ProviderException(ProviderException.Kind.UNKNOWN, 0, "Interrupted during retry", ie);
				}
			}
		}
		throw new IllegalStateException("Unreachable");
	}

	/**
	 * Call the LLM provider and collect the response.
	 *
	 * <p>Filters tool schemas dynamically based on the security context: tools the
	 * user lacks permission for, and STRUCTURAL tools in PRODUCTION mode, are excluded
	 * from the request so the LLM never sees capabilities it cannot use.</p>
	 */
	private AssistantMessage callProviderOnce(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		List<ToolSchema> filteredTools = _registry.toolSchemas(new ToolPermissionFilter(context.security()));

		if (Logger.isDebugEnabled(AgentLoop.class)) {
			List<String> toolNames = filteredTools.stream().map(ToolSchema::name).toList();
			Logger.debug("[AgentLoop " + runId + " turn " + turn + "] "
				+ filteredTools.size() + " tools: " + toolNames
				+ " | " + conversation.messages().size() + " messages",
				AgentLoop.class);
		}

		ProviderRequest request = new ProviderRequest(
			_config.model(),
			_config.systemPrompt(),
			conversation.messages(),
			filteredTools,
			_config.maxOutputTokens(),
			_config.modelOptions(),
			null,
			_config.toolChoice());

		ResponseCollector collector = new ResponseCollector(listener, runId, turn);
		_provider.stream(request, collector);
		return collector.toAssistantMessage();
	}

	/**
	 * Internal handler that bridges {@link StreamHandler} events to {@link AgentEventListener}
	 * callbacks and accumulates data to build an {@link AssistantMessage}.
	 */
	private static class ResponseCollector implements StreamHandler {

		private final AgentEventListener _listener;

		private final String _runId;

		private final int _turnNumber;

		private String _id;

		private String _finishReason;

		private TokenUsage _usage;

		private List<ContentPart> _content;

		/**
		 * Stream-level error received via
		 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.Error}, if any.
		 */
		private ProviderException _streamError;

		ResponseCollector(AgentEventListener listener, String runId, int turnNumber) {
			_listener = listener;
			_runId = runId;
			_turnNumber = turnNumber;
		}

		@Override
		public void onEvent(StreamEvent event) {
			if (event instanceof StreamEvent.TextDelta delta) {
				_listener.onEvent(new AgentEvent.TextDelta(_runId, _turnNumber, Instant.now(), delta.text()));
			} else if (event instanceof StreamEvent.ThinkingDelta delta) {
				_listener.onEvent(new AgentEvent.ThinkingDelta(_runId, _turnNumber, Instant.now(), delta.text()));
			} else if (event instanceof StreamEvent.MessageComplete complete) {
				_id = complete.id();
				_finishReason = complete.finishReason();
				_usage = complete.usage();
				_content = complete.content();
			} else if (event instanceof StreamEvent.Error error) {
				// Store for re-throw in toAssistantMessage(); onEvent cannot throw checked exceptions.
				_streamError = new ProviderException(error.kind(), 0, error.message());
			}
			// ToolCallStart, ToolCallDelta, and lifecycle *Start/*End events:
			// accumulated in MessageComplete or not needed here.
		}

		/**
		 * Build the {@link AssistantMessage} from the accumulated stream events.
		 *
		 * @throws ProviderException
		 *         if the stream delivered a
		 *         {@link com.top_logic.tl.ai.core.provider.StreamEvent.Error} event
		 * @throws IllegalStateException
		 *         if no {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete} was
		 *         received
		 */
		AssistantMessage toAssistantMessage() throws ProviderException {
			if (_streamError != null) {
				throw _streamError;
			}
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
