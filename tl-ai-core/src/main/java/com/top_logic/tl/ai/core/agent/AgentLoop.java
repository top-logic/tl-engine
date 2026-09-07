/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.agent.approval.ApprovalRequest;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.event.DiagnosticLoggingListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.binding.BindingSnapshot;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.InteractionRecorder;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.SystemMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.security.PreCommitGuard;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.tool.api.CancellationToken;
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

	/** Default maximum characters per tool result before truncation. */
	public static final int DEFAULT_MAX_RESULT_CHARS = AgentTuning.MAX_TOOL_RESULT_CHARS;

	private final AgentParams _config;

	private final LLMProvider _provider;

	private final ToolRegistry _registry;

	private final int _maxResultChars;

	private final ConversationMemory _memory;

	private final Set<String> _sessionApprovedTools = ConcurrentHashMap.newKeySet();

	/** Tool call IDs denied in the current turn; cleared at the start of each turn's tool execution. */
	private final Set<String> _turnDeniedCallIds = ConcurrentHashMap.newKeySet();

	/**
	 * Tool call IDs of the current turn whose
	 * {@link com.top_logic.tl.ai.core.message.ToolState.Failed} result is synthetic — the tool
	 * never executed (permission denied, approval rejected, duplicate blocked, unknown tool). These
	 * failures must not trigger the end-of-turn rollback: they cannot have corrupted any data, and
	 * rolling back would silently discard writes of tools that succeeded in the same turn.
	 */
	private final Set<String> _turnBlockedCallIds = ConcurrentHashMap.newKeySet();

	/**
	 * Signature ({@code name + "\0" + arguments}) of the most recent tool call, for
	 * consecutive-duplicate detection. Reset whenever a different tool call is made.
	 */
	private String _lastToolCallSignature;

	/** Consecutive repeats of {@link #_lastToolCallSignature}. */
	private int _lastToolCallRepeats;

	/**
	 * Signature of the tool call before {@link #_lastToolCallSignature}, for period-2
	 * oscillation detection (e.g. a model stuck alternating A, B, A, B, ... — each call
	 * differs from its immediate predecessor, so {@link #_lastToolCallRepeats} alone never
	 * catches it).
	 */
	private String _secondLastToolCallSignature;

	/** Consecutive A/B alternations matching {@link #_secondLastToolCallSignature}. */
	private int _alternationRepeats;

	/** Maximum consecutive times the same tool+args combination may be called before being blocked. */
	private static final int MAX_DUPLICATE_TOOL_CALLS = AgentTuning.MAX_DUPLICATE_TOOL_CALLS;

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
	 * Appends any completed-but-undelivered subagent results to the conversation.
	 *
	 * <p>Called at the start of every turn, i.e. after the previous turn's tool results were
	 * appended and before the next provider request is built.</p>
	 */
	private void injectBetweenTurns(Conversation conversation) {
		injectSubagentResultsForTest(conversation);
	}

	/**
	 * Injects results of any completed-but-undelivered subagents (from the thread-bound
	 * registry) into the conversation as system messages, so the next provider call sees them.
	 * Realizes the long-standing "memory/attachment injection between turns" intent.
	 *
	 * <p>Test-visible entry point for the subagent part of {@link #injectBetweenTurns}.</p>
	 */
	static void injectSubagentResultsForTest(Conversation conversation) {
		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			return;
		}
		for (SubagentJob job : registry.takeUndeliveredResults()) {
			SubagentResult r = job.result();
			String text = "Subagent " + job.targetAgentName() + " (" + job.subagentId()
				+ ") finished with status " + job.status() + ":\n" + r.finalText();
			conversation.add(new SystemMessage(
				UUID.randomUUID().toString(), Instant.now(), text));
		}
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

		ToolContext runContext = securedContext(context);

		_sessionApprovedTools.addAll(context.sessionApprovedTools());

		AgentEventListener serialized =
			AgentEventListener.serializing(new DiagnosticLoggingListener(listener));
		String runId = UUID.randomUUID().toString();
		emit(serialized, new AgentEvent.RunStarted(runId, 0, now(), _config.model()));

		try {
			return runLoop(conversation, runContext, serialized, runId);
		} catch (RuntimeException e) {
			// Guarantee a terminal event so event consumers (e.g. SSE clients) are
			// never left waiting for a run that already died.
			Logger.error("Agent run " + runId + " failed unexpectedly: " + e.getMessage(), e, AgentLoop.class);
			emit(serialized, new AgentEvent.RunCompleted(runId, 0, now(), StopReason.ERROR, TokenUsage.EMPTY));
			throw e;
		}
	}

	/**
	 * The context this run executes with: the caller's, bound to this loop's model.
	 *
	 * <p>
	 * What the run may do is decided by the context the caller built, which names the acting
	 * principal. A child run inherits it unchanged - see {@link ToolContext#forDelegation} and
	 * {@link ToolContext#forDetachedSubagent} - so no agent definition can widen its own reach.
	 * </p>
	 */
	private ToolContext securedContext(ToolContext context) {
		return context.withModel(_config.model());
	}

	private AgentResult runLoop(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId) {
		TokenUsage totalUsage = TokenUsage.EMPTY;
		int maxTurns = _config.maxTurns();

		for (int turn = 1; turn <= maxTurns; turn++) {
			if (Thread.interrupted() || context.cancellationToken().isCancelled()) {
				StopReason stopReason = StopReason.ABORTED;
				emit(listener, new AgentEvent.RunCompleted(runId, turn - 1, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn - 1, totalUsage);
			}

			emit(listener, new AgentEvent.TurnStarted(runId, turn, now()));

			injectBetweenTurns(conversation);

			AssistantMessage assistantMsg;
			try {
				assistantMsg = callProvider(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				emit(listener, new AgentEvent.ProviderError(runId, turn, now(), e.kind().name(), e.getMessage()));
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), TokenUsage.EMPTY, List.of()));
				StopReason stopReason = stopReasonFor(e, context);
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
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

		ToolContext runContext = securedContext(context);

		_sessionApprovedTools.addAll(context.sessionApprovedTools());

		AgentEventListener serialized =
			AgentEventListener.serializing(new DiagnosticLoggingListener(listener));
		String runId = UUID.randomUUID().toString();
		emit(serialized, new AgentEvent.RunStarted(runId, 0, now(), _config.model()));

		try {
			return runWithMemory(_memory, runContext, serialized, runId);
		} catch (RuntimeException e) {
			// Guarantee a terminal event so event consumers (e.g. SSE clients) are
			// never left waiting for a run that already died.
			Logger.error("Agent run " + runId + " failed unexpectedly: " + e.getMessage(), e, AgentLoop.class);
			emit(serialized, new AgentEvent.RunCompleted(runId, 0, now(), StopReason.ERROR, TokenUsage.EMPTY));
			throw e;
		}
	}

	/**
	 * Classify a provider failure: a run cancelled by the user is {@link StopReason#ABORTED}
	 * regardless of the provider error that surfaced it, context overflow is
	 * {@link StopReason#CONTEXT_OVERFLOW}, everything else is {@link StopReason#ERROR}.
	 */
	private static StopReason stopReasonFor(ProviderException e, ToolContext context) {
		if (context.cancellationToken().isCancelled() || Thread.currentThread().isInterrupted()) {
			return StopReason.ABORTED;
		}
		return e.kind() == ProviderException.Kind.CONTEXT_WINDOW_EXCEEDED
			? StopReason.CONTEXT_OVERFLOW
			: StopReason.ERROR;
	}

	/** Maximum reactive compaction retries on context_window_exceeded before giving up. */
	private static final int MAX_REACTIVE_COMPACT_RETRIES = AgentTuning.MAX_REACTIVE_COMPACT_RETRIES;

	/** Base backoff delay in milliseconds for transient retries. Doubles each attempt. */
	private static final long BASE_BACKOFF_MS = AgentTuning.BASE_BACKOFF_MS;

	/** Sleep slice for backoff waits, so cancellation is noticed promptly. */
	private static final long BACKOFF_POLL_MS = AgentTuning.BACKOFF_POLL_MS;

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

			injectBetweenTurns(conversation);

			AssistantMessage assistantMsg;
			try {
				assistantMsg = callProviderWithReactiveCompaction(
					conversation, context, memory, listener, runId, turn);
			} catch (ProviderException e) {
				emit(listener, new AgentEvent.ProviderError(runId, turn, now(), e.kind().name(), e.getMessage()));
				emit(listener, new AgentEvent.TurnCompleted(runId, turn, now(), TokenUsage.EMPTY, List.of()));
				StopReason stopReason = stopReasonFor(e, context);
				emit(listener, new AgentEvent.RunCompleted(runId, turn, now(), stopReason, totalUsage));
				return new AgentResult(stopReason, turn, totalUsage);
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

			// TODO: This check runs once per turn, after every tool result from
			// executeToolCalls is already appended. A turn with many tool calls can push
			// the estimate well past the usable budget before this fires (observed:
			// tokensBefore of 9041/10868 against a 7000-token window). Not a hard failure —
			// callProviderWithReactiveCompaction still catches an actual provider
			// CONTEXT_WINDOW_EXCEEDED — but tightening this to check after each tool
			// result (inside executeToolCalls' batch loop) would bound the overshoot
			// to one tool result instead of one full turn.
			if (memory.shouldCompact()) {
				int tokensBefore = memory.estimateTokens();
				emit(listener, new AgentEvent.CompactionStarted(runId, turn, now(), tokensBefore, "proactive"));
				// Pass the same label used above, so CompactionResult.trigger() actually
				// matches what CompactionStarted already reported — memory.compact()'s
				// "auto" default is never read by anything that persists or displays it.
				CompactionResult compactionResult = memory.compact("proactive");
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
		_turnBlockedCallIds.clear();

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

		// End-of-turn: commit unless a tool actually executed and failed. Synthetic
		// failures (denied, duplicate-blocked, unknown tool) never ran, so they cannot
		// have corrupted data — rolling back for them would discard unrelated writes
		// that succeeded in the same turn. Failures marked ToolState.Failed.synthetic()
		// (e.g. unresolvable arguments) are equally synthetic but only known once
		// ToolCatalog has already produced the result, so they are recognized here
		// instead of via _turnBlockedCallIds.
		boolean anyExecutionFailed = conversation.messages().subList(messagesBefore, conversation.size())
			.stream()
			.filter(ToolResultMessage.class::isInstance)
			.map(ToolResultMessage.class::cast)
			.filter(tr -> !_turnBlockedCallIds.contains(tr.toolCallId()))
			.anyMatch(tr -> tr.state() instanceof ToolState.Failed failed && !failed.synthetic());

		if (anyExecutionFailed) {
			rollbackTurn(context, conversation, listener, runId, turn, "Tool execution failed",
				"Because a tool call in this turn failed, ALL data changes made by tools in this turn"
					+ " were rolled back and NOT persisted — including changes reported as successful."
					+ " Re-apply any changes that are still needed.");
		} else {
			commitTransaction(context, conversation, listener, runId, turn);
		}

		return true;
	}

	/**
	 * Rolls the whole turn back and corrects the picture the model was left with.
	 *
	 * <p>
	 * The one rollback route for "everything this turn wrote is gone": a failed tool call, a
	 * {@link com.top_logic.tl.ai.core.security.PreCommitGuard} violation and a commit that itself
	 * failed all go through here, so all-or-nothing means the same thing in every case and the model
	 * is told in the same shape. Leaving the commit-failure path out was what let an agent report a
	 * created object, by the identifier its tool had returned, that no commit ever persisted.
	 * </p>
	 *
	 * @param reason
	 *        The reason for the
	 *        {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.TransactionRolledBack} event.
	 * @param notice
	 *        What to tell the model, without the {@code [Framework notice]} prefix. Only added when
	 *        a transaction was actually active - with nothing rolled back there is nothing to
	 *        correct.
	 */
	private void rollbackTurn(ToolContext context, Conversation conversation, AgentEventListener listener,
			String runId, int turn, String reason, String notice) {
		boolean hadTransaction = context.transaction().isActive();
		rollbackTransaction(context, listener, runId, turn, reason);
		if (hadTransaction) {
			// The model was already told (via the individual tool results) that some
			// tools succeeded. Correct that picture: their writes are gone now.
			conversation.add(new UserMessage(
				UUID.randomUUID().toString(), now(),
				List.of(new TextContent("[Framework notice] " + notice))));
		}
	}

	/**
	 * Ensure a transaction is active before executing a write tool.
	 * Begins a transaction on first write. Locking is handled by individual
	 * tools via {@link TransactionScope#acquireLock(String, com.top_logic.model.TLObject...)}.
	 *
	 * @param tool     the resolved tool (may be {@code null} for unknown tools)
	 * @param context  the current execution context
	 * @param listener receives transaction events
	 * @param runId    the current run ID
	 * @param turn     the current turn number
	 */
	private void ensureTransaction(Tool tool, ToolContext context,
			AgentEventListener listener, String runId, int turn) {
		TransactionScope scope = context.transaction();
		if (scope.isNoOp() || scope.isActive()) {
			return;
		}

		if (tool == null || tool.mode() == ToolMode.READ) {
			return;
		}

		scope.begin();
		emit(listener, new AgentEvent.TransactionBegan(runId, turn, now()));
	}

	/** */
	private void commitTransaction(ToolContext context, Conversation conversation,
			AgentEventListener listener, String runId, int turn) {
		TransactionScope scope = context.transaction();
		if (!scope.isActive()) {
			// No write tool ran this turn: nothing to commit and nothing to guard.
			return;
		}
		if (!preCommitGuardPasses(context, conversation, listener, runId, turn)) {
			return;
		}
		try {
			scope.commit();
			Revision rev = scope.commitRevision();
			long revNum = rev != null ? rev.getCommitNumber() : -1;
			emit(listener, new AgentEvent.TransactionCommitted(runId, turn, now(), revNum));
		} catch (TransactionException e) {
			// Nothing else logs a failed commit: TransactionScope wraps the engine's exception
			// without logging it and the rollback event is only traced at DEBUG, so without this a
			// deployment that hits it has no way to find out why.
			Logger.error("Agent run " + runId + " turn " + turn
				+ ": the transaction of the turn could not be committed.", e, AgentLoop.class);
			String reason = rollbackReason(e);
			rollbackTurn(context, conversation, listener, runId, turn, reason,
				"The data changes of this turn could NOT be saved and were rolled back: "
					+ asSentence(reason)
					+ " This includes every change a tool of this turn reported as successful -"
					+ " those results are void, and any identifier they named does not exist."
					+ " Tell the user that the change was not saved, and do not claim otherwise."
					+ " The cause is a database failure, not a mistake of yours, so do not retry"
					+ " the identical change.");
		}
	}

	/**
	 * The last gate before the turn's changes are persisted: may the run's principal make every
	 * change this turn accumulated?
	 *
	 * <p>
	 * The layer that catches tools which performed no check of their own - remote MCP tools,
	 * TL-Script tools authored in the running application, and any Java tool whose author forgot. On a
	 * violation the whole turn is rolled back through {@link #rollbackTurn}, the same route a failed
	 * tool call takes; each violation is audited by the guard itself.
	 * </p>
	 *
	 * <p>
	 * Costs nothing for a run that enforces no model access: the guard is asked whether it enforces
	 * <em>before</em> the change set is read from the transaction.
	 * </p>
	 *
	 * @return Whether the commit may proceed.
	 */
	private boolean preCommitGuardPasses(ToolContext context, Conversation conversation,
			AgentEventListener listener, String runId, int turn) {
		SecurityContext security = context.security();
		PreCommitGuard guard = PreCommitGuard.forRun();
		if (!guard.enforces()) {
			return true;
		}
		List<PreCommitGuard.Violation> violations = guard.check(security, context.transaction().changes());
		if (violations.isEmpty()) {
			return true;
		}
		rollbackTurn(context, conversation, listener, runId, turn, "Pre-commit security check failed",
			"The data changes of this turn were rolled back and NOT persisted, because your account is"
				+ " not permitted to make all of them: " + PreCommitGuard.describe(violations)
				+ ". The rollback is all-or-nothing, so the permitted changes of this turn are gone as"
				+ " well. Do not retry the same change; ask the user for help or continue without it.");
		return false;
	}

	/**
	 * The reason to report for a rollback caused by a failed commit.
	 *
	 * <p>
	 * Only the reason: the event says a rollback happened, and the chat prefixes it once more with
	 * "Rolled back: ". Adding {@link TransactionScope#COMMIT_FAILED_PREFIX} here as well is what
	 * produced the "Rolled back: Commit failed: Commit failed: ..." a real session reported.
	 * </p>
	 */
	private static String rollbackReason(TransactionException failure) {
		String message = failure.getMessage();
		if (message != null && message.startsWith(TransactionScope.COMMIT_FAILED_PREFIX)) {
			return message.substring(TransactionScope.COMMIT_FAILED_PREFIX.length());
		}
		return message;
	}

	/**
	 * The reason as a sentence, so the notice built around it does not run two sentences together.
	 *
	 * <p>
	 * A commit failure reason is whatever the engine produced: {@code "Database operation failed."}
	 * ends in a full stop, a nested rollback's reason may not.
	 * </p>
	 */
	private static String asSentence(String reason) {
		if (StringServices.isEmpty(reason)) {
			return "The database reported no reason.";
		}
		char last = reason.charAt(reason.length() - 1);
		return last == '.' || last == '!' || last == '?' ? reason : reason + ".";
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
	 * Checks if this tool call is a consecutive duplicate of the previous call, or part of a
	 * two-call oscillation (A, B, A, B, ...), with the same name and arguments each time.
	 * Returns a synthetic error result if either pattern has repeated too many times,
	 * {@code null} otherwise.
	 *
	 * <p>Allows up to {@link #MAX_DUPLICATE_TOOL_CALLS} consecutive identical calls, and up to
	 * {@link #MAX_DUPLICATE_TOOL_CALLS} full A/B cycles (to account for legitimate retries and
	 * genuine compare-two-things back-and-forth). Beyond that, the call is blocked with a
	 * message directing the LLM to use the result it already received. Both counters reset as
	 * soon as a call breaks the pattern, so legitimate later re-reads (e.g. verify after an
	 * edit, or a third distinct comparison) stay possible.</p>
	 *
	 * <p>Only called from the loop thread — duplicate detection for concurrent batches
	 * happens before the batch is dispatched to worker threads.</p>
	 */
	private ToolResultMessage checkDuplicateToolCall(ToolCall toolCall) {
		String signature = toolCall.name() + "\0" + toolCall.arguments();
		if (signature.equals(_lastToolCallSignature)) {
			_lastToolCallRepeats++;
		} else {
			_alternationRepeats = signature.equals(_secondLastToolCallSignature) ? _alternationRepeats + 1 : 1;
			_secondLastToolCallSignature = _lastToolCallSignature;
			_lastToolCallSignature = signature;
			_lastToolCallRepeats = 1;
		}

		if (_lastToolCallRepeats > MAX_DUPLICATE_TOOL_CALLS) {
			_turnBlockedCallIds.add(toolCall.id());
			return errorResult(toolCall,
				"DUPLICATE CALL BLOCKED: You have already called '" + toolCall.name()
				+ "' with these exact arguments " + (_lastToolCallRepeats - 1) + " time(s) in a row. "
				+ "The result is already in your conversation. "
				+ "Use the result you already received instead of calling again. "
				+ "If you need different information, call the tool with DIFFERENT arguments.");
		}
		if (_alternationRepeats > MAX_DUPLICATE_TOOL_CALLS) {
			_turnBlockedCallIds.add(toolCall.id());
			return errorResult(toolCall,
				"DUPLICATE CALL BLOCKED: You are alternating between '" + toolCall.name()
				+ "' and a previous call without making progress. "
				+ "Both results are already in your conversation. "
				+ "Use the results you already received instead of calling again. "
				+ "If you need different information, call the tool with DIFFERENT arguments.");
		}

		return null;
	}

	/**
	 * Shared worker pool for concurrent READ-tool batches. Cached daemon threads are reused
	 * across batches and runs, and reaped after idling — avoids creating a fresh pool per batch.
	 */
	private static final ExecutorService TOOL_WORKERS = Executors.newCachedThreadPool(runnable -> {
		Thread thread = new Thread(runnable, "agent-tool-worker");
		thread.setDaemon(true);
		return thread;
	});

	private void executeConcurrent(List<ToolCall> batch, ToolContext context,
			Conversation conversation, AgentEventListener listener, String runId, int turn) {

		// Capture thread-local bindings and the caller's sub-session from the calling thread:
		// worker threads do not inherit thread-locals, so each must re-establish them.
		BindingSnapshot bindings = BindingSnapshot.capture();
		SubSessionContext userSession = ThreadContextManager.getSubSession();

		List<Future<ToolResultMessage>> futures = new ArrayList<>();

		for (ToolCall tc : batch) {
			// Duplicate detection runs on the loop thread, in call order, before dispatch —
			// same behaviour as for serial calls.
			ToolResultMessage duplicateResult = checkDuplicateToolCall(tc);
			if (duplicateResult != null) {
				futures.add(CompletableFuture.completedFuture(duplicateResult));
				continue;
			}

			ComputationEx2<ToolResultMessage, RuntimeException, RuntimeException> job = () -> {
				try (BindingSnapshot.Established established = bindings.establish()) {
					return executeWithSecurityCheck(tc, context, listener, runId, turn);
				}
			};
			// Propagate the caller's sub-session (e.g. the chat user's) onto the worker thread
			// so parallel tools run in the same context as sequential ones; fall back to a
			// system interaction only when the caller had no bound context.
			//
			// Submitted directly to the ExecutorService (not via CompletableFuture.supplyAsync)
			// so the returned Future is backed by FutureTask: its cancel(true) actually
			// interrupts the worker thread if the job is running. CompletableFuture.cancel()
			// never interrupts the underlying task regardless of mayInterruptIfRunning.
			futures.add(TOOL_WORKERS.submit(
				() -> userSession != null
					? ThreadContextManager.inContext(userSession, job)
					: ThreadContextManager.inSystemInteraction(AgentLoop.class, job)));
		}

		List<ToolResultMessage> results = collectBatchResults(batch, futures, context);

		for (int i = 0; i < batch.size(); i++) {
			conversation.add(results.get(i));
			reportResult(batch.get(i), results.get(i), listener, runId, turn, _turnDeniedCallIds);
		}
	}

	/**
	 * Await batch results while staying responsive to interrupts and cancellation.
	 *
	 * <p>{@link CompletableFuture#join()} waits uninterruptibly, which would make the run
	 * un-cancellable while a batch is in flight. Uses timed polling instead; once cancelled
	 * or interrupted, remaining futures are cancelled and reported as failed, which triggers
	 * the end-of-turn rollback. Polls every not-yet-done future once per tick instead of
	 * exhausting one future's timeout before moving to the next, so cancellation is noticed
	 * within one poll slice regardless of the batch size.</p>
	 */
	private static List<ToolResultMessage> collectBatchResults(List<ToolCall> batch,
			List<Future<ToolResultMessage>> futures, ToolContext context) {
		ToolResultMessage[] results = new ToolResultMessage[futures.size()];
		boolean abort = false;
		int remaining = futures.size();
		while (remaining > 0) {
			if (!abort && (context.cancellationToken().isCancelled() || Thread.interrupted())) {
				abort = true;
			}
			for (int i = 0; i < futures.size(); i++) {
				if (results[i] != null) {
					continue;
				}
				Future<ToolResultMessage> future = futures.get(i);
				if (abort) {
					future.cancel(true);
					results[i] = errorResult(batch.get(i), "Cancelled.");
					remaining--;
					continue;
				}
				try {
					results[i] = future.get(200, TimeUnit.MILLISECONDS);
					remaining--;
				} catch (TimeoutException e) {
					// Still running — loop and re-check cancellation.
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					abort = true;
					future.cancel(true);
					results[i] = errorResult(batch.get(i), "Cancelled.");
					remaining--;
				} catch (CancellationException e) {
					results[i] = errorResult(batch.get(i), "Cancelled.");
					remaining--;
				} catch (ExecutionException e) {
					Throwable cause = e.getCause() != null ? e.getCause() : e;
					results[i] = errorResult(batch.get(i),
						"Concurrent execution failed: " + cause.getMessage());
					remaining--;
				}
			}
		}
		return Arrays.asList(results);
	}

	/**
	 * Marks a tool call as denied: its failure result is synthetic (the tool never ran), so it is
	 * excluded from failure events (a
	 * {@link com.top_logic.tl.ai.core.agent.event.AgentEvent.ToolCallDenied} was already emitted)
	 * and from the end-of-turn rollback decision.
	 */
	private void markDenied(String toolCallId) {
		_turnDeniedCallIds.add(toolCallId);
		_turnBlockedCallIds.add(toolCallId);
	}

	private ToolResultMessage executeWithSecurityCheck(ToolCall toolCall, ToolContext context,
			AgentEventListener listener, String runId, int turn) {

		if (context.cancellationToken().isCancelled()) {
			// A concurrent batch's worker may not start running this job until well after
			// the loop thread already gave up on it (thread-pool queueing, or another job in
			// the same batch is slow). Bail out before emitting any lifecycle event or
			// touching the tool. This covers jobs that haven't started yet; jobs already
			// mid-execution rely on future.cancel(true) interrupting the worker thread (see
			// collectBatchResults) plus the tool itself cooperatively checking this token.
			return errorResult(toolCall, "Cancelled.");
		}

		SecurityContext security = context.security();

		// Step 1: Permission + mode gate
		Tool tool = _registry.getTool(toolCall.name());

		if (tool != null) {
			ToolPermissionFilter filter = new ToolPermissionFilter(security);
			if (!filter.test(tool)) {
				String reason = filter.denialReason(tool);
				emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), reason));
				markDenied(toolCall.id());
				return errorResult(toolCall, reason);
			}
		}
		// A name the registry does not resolve is deliberately not run through the permission
		// check: it belongs to no tool group, so the check would report a hallucinated or
		// misspelled name as a permission problem and swallow the "Available:" hint the model
		// needs to correct itself. Such a name cannot execute either way - the registry refuses
		// it below with a message that says so and lists what this user does have.

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
					markDenied(toolCall.id());
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
						toolCall.arguments(), request.displayReason()));
					// Durable record of the pending approval, carrying the tool call's actual
					// argument JSON (not the possibly-empty parsed copy shown above).
					String interactionId = InteractionRecorder.begin(
						context.executionHandle().get(),
						AgentExecutionStore.InteractionType.APPROVAL, approvalRequestId,
						tool.name(), needsApproval.displayReason(), toolCall.arguments());
					ApprovalResult approvalResult = context.approval().requestApproval(request);
					if (approvalResult instanceof ApprovalResult.Approved) {
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.RESOLVED, ApprovalOutcome.APPROVED.name());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.APPROVED));
						// proceed to execution
					} else if (approvalResult instanceof ApprovalResult.ApprovedForSession) {
						_sessionApprovedTools.add(tool.name());
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.RESOLVED,
							ApprovalOutcome.APPROVED_FOR_SESSION.name());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.APPROVED_FOR_SESSION));
						// proceed to execution
					} else if (approvalResult instanceof ApprovalResult.Rejected rejected) {
						String feedback = rejected.userComment().isBlank()
							? "User denied tool '" + toolCall.name() + "'."
							: "User denied tool '" + toolCall.name() + "': "
								+ rejected.userComment();
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.RESOLVED,
							rejected.userComment().isBlank() ? ApprovalOutcome.REJECTED.name()
								: ApprovalOutcome.REJECTED.name() + ": " + rejected.userComment());
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.REJECTED));
						emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), feedback));
						markDenied(toolCall.id());
						return errorResult(toolCall, feedback);
					} else if (approvalResult instanceof ApprovalResult.TimedOut) {
						String feedback = "Approval timed out for tool '" + toolCall.name() + "'.";
						InteractionRecorder.resolve(interactionId,
							AgentExecutionStore.InteractionState.TIMED_OUT, null);
						emit(listener, new AgentEvent.ApprovalResolved(runId, turn, now(),
							toolCall.id(), approvalRequestId, ApprovalOutcome.TIMED_OUT));
						emit(listener, new AgentEvent.ToolCallDenied(runId, turn, now(), toolCall.id(), toolCall.name(), feedback));
						markDenied(toolCall.id());
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
		ensureTransaction(tool, context, listener, runId, turn);

		ProgressReporter progressReporter = message ->
			emit(listener, new AgentEvent.ToolProgress(runId, turn, now(),
				toolCall.id(), toolCall.name(), message));
		ToolContext execContext = context.withToolCallId(toolCall.id())
			.withProgressReporter(progressReporter);

		if (tool == null) {
			// Unknown tool: the registry fails the call without executing anything, so the
			// failure is synthetic and must not trigger the end-of-turn rollback.
			_turnBlockedCallIds.add(toolCall.id());
		}

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

		String truncated = truncateToBudget(text, limit);

		emit(listener, new AgentEvent.ToolCallResultTruncated(runId, turn, now(),
			result.toolCallId(), result.toolName(), text.length(), truncated.length()));

		return new ToolResultMessage(
			result.id(), result.timestamp(), result.toolCallId(), result.toolName(),
			new ToolState.Completed(truncated, completed.startTime(), completed.endTime(),
				false, completed.summary(), -1, truncateTextParts(completed.content(), limit)));
	}

	/**
	 * Applies the same character budget to the text parts of a multimodal tool result.
	 *
	 * <p>Images and documents pass through untouched: a character budget does not describe them,
	 * and dropping them would silently cost the model the very content the tool was called for.</p>
	 */
	private static List<ContentPart> truncateTextParts(List<ContentPart> content, int limit) {
		if (content.isEmpty()) {
			return content;
		}
		List<ContentPart> result = new ArrayList<>(content.size());
		for (ContentPart part : content) {
			if (part instanceof TextContent text && text.text().length() > limit) {
				result.add(new TextContent(truncateToBudget(text.text(), limit)));
			} else {
				result.add(part);
			}
		}
		return result;
	}

	/** Keeps the first and last half of the budget, reporting how much was dropped in between. */
	private static String truncateToBudget(String text, int limit) {
		int halfBudget = limit / 2;
		String head = text.substring(0, halfBudget);
		String tail = text.substring(text.length() - halfBudget);
		int droppedChars = text.length() - head.length() - tail.length();
		return head + "\n\n[... " + droppedChars + " chars truncated ...]\n\n" + tail;
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
	 * Retries up to {@link AgentParams#maxRetries()} times on
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#RATE_LIMITED} and
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind#OVERLOADED} with exponential
	 * backoff, respecting the {@code retry-after} header when present. The backoff sleeps in
	 * small slices so a user cancellation ends the wait promptly.
	 * </p>
	 */
	private AssistantMessage callProvider(Conversation conversation, ToolContext context,
			AgentEventListener listener, String runId, int turn) throws ProviderException {
		int maxRetries = Math.max(0, _config.maxRetries());
		for (int attempt = 0; attempt <= maxRetries; attempt++) {
			try {
				return callProviderOnce(conversation, context, listener, runId, turn);
			} catch (ProviderException e) {
				boolean retryable = e.kind() == ProviderException.Kind.RATE_LIMITED
					|| e.kind() == ProviderException.Kind.OVERLOADED;
				if (!retryable || attempt == maxRetries) {
					throw e;
				}
				long waitMs = e.retryAfter() != null
					? e.retryAfter().toMillis()
					: BASE_BACKOFF_MS * (1L << attempt);
				// Cap at 30 seconds.
				waitMs = Math.min(waitMs, 30_000);

				emit(listener, new AgentEvent.RetryScheduled(
					runId, turn, now(), e.kind().name(), attempt + 1, waitMs));

				backoff(waitMs, context, e);
			}
		}
		throw new IllegalStateException("Unreachable");
	}

	/**
	 * Sleep for the given backoff duration in {@link #BACKOFF_POLL_MS} slices, re-checking
	 * cancellation between slices. When the run is cancelled or the thread interrupted,
	 * gives up retrying by rethrowing the pending transient error — the run loop then maps
	 * it to {@link StopReason#ABORTED} via {@link #stopReasonFor}.
	 */
	private static void backoff(long waitMs, ToolContext context, ProviderException pending)
			throws ProviderException {
		if (context.cancellationToken().sleepCancellably(waitMs, BACKOFF_POLL_MS)) {
			throw pending;
		}
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
			Logger.debug("run=" + runId + " turn=" + turn + " PROVIDER_REQUEST"
				+ " tools=" + filteredTools.size() + " messages=" + conversation.messages().size()
				+ " toolNames=" + toolNames,
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
			_config.toolChoice(),
			Duration.ofSeconds(_config.timeoutSeconds()));

		ResponseCollector collector = new ResponseCollector(listener, runId, turn, context.cancellationToken());
		long startNanos = System.nanoTime();
		try {
			_provider.stream(request, collector);
		} finally {
			if (Logger.isDebugEnabled(AgentLoop.class)) {
				// Provider call latency is operational signal that is not persisted (the KB stores
				// only the run's token totals, not per-turn timing). Logged even on failure so a
				// slow-then-erroring provider is visible. Correlated by runId/turn.
				Logger.debug("run=" + runId + " turn=" + turn + " PROVIDER_MS ms="
					+ (System.nanoTime() - startNanos) / 1_000_000L, AgentLoop.class);
			}
		}
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

		private final CancellationToken _cancellation;

		private String _id;

		private String _finishReason;

		private TokenUsage _usage;

		private List<ContentPart> _content;

		/**
		 * Stream-level error received via
		 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.Error}, if any.
		 */
		private ProviderException _streamError;

		ResponseCollector(AgentEventListener listener, String runId, int turnNumber,
				CancellationToken cancellation) {
			_listener = listener;
			_runId = runId;
			_turnNumber = turnNumber;
			_cancellation = cancellation;
		}

		@Override
		public CancellationToken cancellation() {
			return _cancellation;
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
