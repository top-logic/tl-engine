/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;

import com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent;
import com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent;
import com.top_logic.ai.chat.api.messages.CompactionStartedEvent;
import com.top_logic.ai.chat.api.messages.ContextCompositionEvent;
import com.top_logic.ai.chat.api.messages.ConnectedEvent;
import com.top_logic.ai.chat.api.messages.DelegationEventWrapper;
import com.top_logic.ai.chat.api.messages.HeartbeatEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.ModelSwitchedEvent;
import com.top_logic.ai.chat.api.messages.RetryScheduledEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.api.messages.RunCompletedEvent;
import com.top_logic.ai.chat.api.messages.RunStartedEvent;
import com.top_logic.ai.chat.api.messages.TextDeltaEvent;
import com.top_logic.ai.chat.api.messages.ThinkingDeltaEvent;
import com.top_logic.ai.chat.api.messages.TokenUsageDto;
import com.top_logic.ai.chat.api.messages.ToolCompletedEvent;
import com.top_logic.ai.chat.api.messages.ToolDeniedEvent;
import com.top_logic.ai.chat.api.messages.ToolFailedEvent;
import com.top_logic.ai.chat.api.messages.ToolStartedEvent;
import com.top_logic.ai.chat.api.messages.TransactionCommittedEvent;
import com.top_logic.ai.chat.api.messages.SubagentStatusEvent;
import com.top_logic.ai.chat.api.messages.TaskUpdateEvent;
import com.top_logic.ai.chat.api.messages.TransactionRolledBackEvent;
import com.top_logic.ai.chat.api.messages.TurnCompletedEvent;
import com.top_logic.ai.chat.api.messages.TurnStartedEvent;
import com.top_logic.ai.chat.layout.Chat;
import com.top_logic.ai.chat.layout.ChatProcessor;
import com.top_logic.ai.chat.layout.PendingInteractionRegistry;
import com.top_logic.ai.chat.layout.TaskProgressTracker;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.json.JsonEscape;

/**
 * Push-based SSE implementation of {@link ChatEventHandler}.
 *
 * <p>
 * Receives agent events directly via {@link #onEvent(AgentEvent)}, buffers them in a
 * {@link ConcurrentLinkedQueue}, and flushes to the SSE stream every 50ms. User interaction
 * events from {@link UserInteractionStore} are still polled during the flush cycle because
 * they originate outside the agent event system.
 * </p>
 *
 * <p>
 * SSE event types emitted:
 * </p>
 * <ul>
 * <li>{@code connected} - Initial connection state</li>
 * <li>{@code run_started}, {@code run_completed} - Run lifecycle</li>
 * <li>{@code turn_started}, {@code turn_completed} - Turn lifecycle</li>
 * <li>{@code text_delta}, {@code thinking_delta} - LLM streaming</li>
 * <li>{@code tool_started}, {@code tool_completed}, {@code tool_failed}, {@code tool_denied}, {@code tool_progress} - Tool lifecycle</li>
 * <li>{@code approval_required}, {@code approval_resolved} - Approval flow</li>
 * <li>{@code lock_acquired}, {@code lock_failed} - Edit lock</li>
 * <li>{@code tx_began}, {@code tx_committed}, {@code tx_rollback} - Transactions</li>
 * <li>{@code compaction_started} - Context compaction</li>
 * <li>{@code provider_error} - Unrecoverable provider errors</li>
 * <li>{@code message_complete} - Final assistant message with HTML</li>
 * <li>{@code user_interaction} - Polled from UserInteractionStore</li>
 * <li>{@code heartbeat} - Keep-alive ping</li>
 * </ul>
 */
public class ChatSSEHandler implements ChatEventHandler {

	private static final long FLUSH_INTERVAL_MS = 50;
	private static final long HEARTBEAT_INTERVAL_MS = 15000;

	private static final AtomicInteger SSE_THREAD_COUNTER = new AtomicInteger();

	private static final ScheduledExecutorService SSE_EXECUTOR =
		Executors.newScheduledThreadPool(
			Math.max(4, Runtime.getRuntime().availableProcessors()),
			r -> {
				Thread t = new Thread(r);
				t.setName("ChatSSE-Worker-" + SSE_THREAD_COUNTER.incrementAndGet());
				t.setDaemon(true);
				return t;
			});

	private final TaskProgressTracker _tracker;

	/** The signed-in account this stream was opened for; may be <code>null</code>. */
	private final Person _account;

	private final AsyncContext _asyncContext;
	private final PrintWriter _writer;
	private final AtomicBoolean _closed = new AtomicBoolean(false);

	/** Thread-safe event buffer between agent thread and SSE flush thread. */
	/**
	 * Everything still to be written, in the order it was produced.
	 *
	 * <p>
	 * Holds two kinds: an {@link AgentEvent}, rendered at flush time because it reads state that is
	 * only settled then, and a {@link Frame} that is already rendered. One queue rather than two,
	 * because the ordering between the kinds is the point: a message-complete written straight to
	 * the stream overtook the text deltas still waiting here, and the browser dropped the tail of
	 * every answer.
	 * </p>
	 */
	private final ConcurrentLinkedQueue<Object> _eventQueue = new ConcurrentLinkedQueue<>();

	/** An SSE frame whose payload is already known, queued to keep its place. */
	private record Frame(String type, String data) {
		// Pure data.
	}

	/** Tracks already-emitted user interaction request IDs to avoid duplicates. */
	private final Set<String> _emittedInteractionIds = new HashSet<>();

	private long _lastActivityTime = System.currentTimeMillis();
	private ScheduledFuture<?> _flushTask;

	/**
	 * Creates a new SSE handler.
	 *
	 * @param tracker
	 *        Session-scoped task progress tracker.
	 * @param asyncContext
	 *        Async context from the servlet.
	 * @param writer
	 *        Response writer for SSE output.
	 */
	public ChatSSEHandler(TaskProgressTracker tracker, AsyncContext asyncContext, PrintWriter writer) {
		this(tracker, null, asyncContext, writer);
	}

	/**
	 * Creates a {@link ChatSSEHandler} for the window of a known account.
	 *
	 * @param account
	 *        The signed-in account this stream was opened for; what makes this window one of the
	 *        account's when another of them answers a request. May be <code>null</code>.
	 */
	public ChatSSEHandler(TaskProgressTracker tracker, Person account, AsyncContext asyncContext,
			PrintWriter writer) {
		_tracker = tracker;
		_account = account;
		_asyncContext = asyncContext;
		_writer = writer;
	}

	@Override
	public TaskProgressTracker getTracker() {
		return _tracker;
	}

	@Override
	public Person getAccount() {
		return _account;
	}

	@Override
	public boolean isActive() {
		return !_closed.get();
	}

	/**
	 * Starts the SSE event stream.
	 */
	public void start() {
		// Registering also drops stale handlers of the same tracker (e.g. on SSE reconnection).
		ChatEventHandlerRegistry.register(this);

		_asyncContext.addListener(new AsyncListener() {
			@Override
			public void onComplete(AsyncEvent event) {
				stop();
			}

			@Override
			public void onTimeout(AsyncEvent event) {
				stop();
			}

			@Override
			public void onError(AsyncEvent event) {
				stop();
			}

			@Override
			public void onStartAsync(AsyncEvent event) {
				// Nothing
			}
		});

		sendProxyBustPadding();
		sendConnectedEvent();

		_flushTask = SSE_EXECUTOR.scheduleWithFixedDelay(
			this::flushAndPoll,
			FLUSH_INTERVAL_MS,
			FLUSH_INTERVAL_MS,
			TimeUnit.MILLISECONDS);
	}

	/**
	 * Stops the SSE handler and releases resources.
	 */
	public void stop() {
		if (_closed.compareAndSet(false, true)) {
			ChatEventHandlerRegistry.unregister(this);
			if (_flushTask != null) {
				_flushTask.cancel(false);
			}
			try {
				_asyncContext.complete();
			} catch (Exception ignored) {
				// Already completed or invalid state
			}
		}
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (_closed.get()) {
			// This handler's SSE connection is gone (browser reconnected).
			// Forward to the current active handler for the same tracker.
			ChatEventHandler current = ChatEventHandlerRegistry.find(_tracker);
			if (current != null && current != this) {
				current.onEvent(event);
			} else {
				Logger.warn("SSE handler closed, no replacement found — dropping event: "
					+ event.getClass().getSimpleName(), ChatSSEHandler.class);
			}
			return;
		}
		_eventQueue.add(event);
	}

	/**
	 * Queues a piece of the answer with its position, rendered right away: unlike most events it
	 * reads no state that settles only at flush time.
	 *
	 * <p>
	 * The frame names the chat this window is showing, which is the run's: the run streams only to
	 * windows showing its chat.
	 * </p>
	 */
	@Override
	public void onTextDelta(AgentEvent.TextDelta delta, long end) {
		String sessionId = _tracker == null ? null : _tracker.getActiveSessionId();
		enqueue("text_delta", DtoMapper.toJson(TextDeltaEvent.create()
			.setText(delta.text())
			.setEnd(end)
			.setSessionId(sessionId == null ? "" : sessionId)));
	}

	/**
	 * Queues the final answer. Called from {@link ChatProcessor} after the run completes, not via
	 * {@link #onEvent(AgentEvent)}.
	 *
	 * <p>
	 * Queued rather than written straight out: it carries the authoritative text, and a client that
	 * applies it must not receive it before the deltas it supersedes.
	 * </p>
	 */
	@Override
	public void sendMessageComplete(MessageCompleteEvent event) {
		enqueue("message_complete", DtoMapper.toJson(event));
	}

	/** Queues a provider error event for the client. */
	@Override
	public void sendProviderError(ProviderErrorEvent event) {
		enqueue("provider_error", DtoMapper.toJson(event));
	}

	/** Queues the news that another window of this account answered a request this one may show. */
	@Override
	public void sendInteractionResolved(InteractionResolvedEvent event) {
		enqueue("interaction_resolved", DtoMapper.toJson(event));
	}

	/**
	 * Adds an already-rendered frame to the queue, or hands it to the handler that replaced this
	 * one when the browser has reconnected in the meantime.
	 */
	private void enqueue(String type, String data) {
		if (_closed.get()) {
			ChatEventHandler current = ChatEventHandlerRegistry.find(_tracker);
			if (current instanceof ChatSSEHandler handler && handler != this) {
				handler.enqueue(type, data);
			} else {
				Logger.warn("SSE handler closed, no replacement found - dropping " + type,
					ChatSSEHandler.class);
			}
			return;
		}
		_eventQueue.add(new Frame(type, data));
	}

	/**
	 * Sends an SSE comment block large enough to flush corporate proxy buffers.
	 *
	 * <p>
	 * Many corporate proxies (Zscaler, BlueCoat, F5) buffer the response until they've
	 * accumulated enough data (typically 4-8 KB). This padding forces the proxy to flush,
	 * ensuring SSE events reach the browser immediately.
	 * </p>
	 */
	private void sendProxyBustPadding() {
		if (_closed.get()) {
			return;
		}
		try {
			synchronized (_writer) {
				// SSE comments (lines starting with ':') are ignored by EventSource.
				// Send 4 KB of padding to push through proxy buffers.
				StringBuilder padding = new StringBuilder(4096);
				padding.append(": proxy-bust-padding\n");
				while (padding.length() < 4096) {
					padding.append(": " + " ".repeat(253) + "\n");
				}
				padding.append('\n');
				_writer.write(padding.toString());
				_writer.flush();
			}
		} catch (Exception e) {
			Logger.error("Failed to send proxy-bust padding", e, ChatSSEHandler.class);
		}
	}

	private void sendConnectedEvent() {
		Chat chat = _tracker.getChat();
		int messageCount = chat.getMessages().size();

		String currentTaskId = ChatProcessor.getCurrentTaskId(_tracker);
		boolean processing = false;
		ChatProcessor.AIProcessingState procState = _tracker.getAnyActiveProcessingState();
		if (procState != null && procState.isProcessing()) {
			String procSessionId = procState.getSessionId();
			String activeSessionId = _tracker.getActiveSessionId();
			if (procSessionId == null || procSessionId.equals(activeSessionId)) {
				processing = true;
			}
		}

		try {
			ConnectedEvent event = ConnectedEvent.create()
				.setMessageSeq(messageCount)
				.setTimelineSeq(0)
				.setProcessing(processing)
				.setTaskId(currentTaskId != null ? currentTaskId : "");
			sendEvent("connected", DtoMapper.toJson(event));
		} catch (Exception e) {
			Logger.error("Failed to send connected event", e, ChatSSEHandler.class);
		}

		String mode = _tracker.getApprovalMode().name();
		sendEvent("approval_mode", "{\"mode\":\"" + mode + "\"}");
	}

	/**
	 * Drains the event queue, emits SSE frames, polls for user interactions, and sends
	 * heartbeats when idle.
	 */
	private void flushAndPoll() {
		if (_closed.get()) {
			return;
		}

		try {
			// Drain everything produced since the last flush, in the order it was produced.
			Object queued;
			while ((queued = _eventQueue.poll()) != null) {
				if (queued instanceof Frame frame) {
					sendEvent(frame.type(), frame.data());
				} else {
					emitAgentEvent((AgentEvent) queued);
				}
			}

			// Poll for user interactions (from AskUserTool, not part of AgentEvent)
			pollUserInteractions();
		} catch (Exception e) {
			Logger.error("Error in SSE flush", e, ChatSSEHandler.class);
		}

		// Heartbeat if no activity for 15s
		long now = System.currentTimeMillis();
		if (now - _lastActivityTime > HEARTBEAT_INTERVAL_MS) {
			sendHeartbeat();
		}
	}

	private void emitAgentEvent(AgentEvent event) {
		if (event instanceof AgentEvent.DelegatedEvent delegated) {
			emitDelegatedEvent(delegated);
			return;
		}
		if (event instanceof AgentEvent.RunStarted e) {
			sendEvent("run_started", DtoMapper.toJson(RunStartedEvent.create()
				.setRunId(e.runId())
				.setModel(e.model())
				.setUsedTokens(usedTokens())
				.setContextWindow(contextWindow())));
		} else if (event instanceof AgentEvent.RunCompleted e) {
			sendEvent("run_completed", DtoMapper.toJson(RunCompletedEvent.create()
				.setRunId(e.runId())
				.setReason(e.reason().name())
				.setTotalUsage(mapTokenUsage(e.totalUsage()))));
		} else if (event instanceof AgentEvent.TurnStarted e) {
			sendEvent("turn_started", DtoMapper.toJson(TurnStartedEvent.create()
				.setTurnNumber(e.turnNumber())
				.setUsedTokens(usedTokens())
				.setContextWindow(contextWindow())));
		} else if (event instanceof AgentEvent.TurnCompleted e) {
			sendEvent("turn_completed", DtoMapper.toJson(TurnCompletedEvent.create()
				.setTurnNumber(e.turnNumber())
				.setTurnUsage(mapTokenUsage(e.turnUsage()))
				.setContextWindow(contextWindow())));
		} else if (event instanceof AgentEvent.ContextComposition e) {
			sendEvent("context_composition", DtoMapper.toJson(ContextCompositionEvent.create()
				.setSystemPromptTokens(e.systemPromptTokens())
				.setToolDefinitionTokens(e.toolDefinitionTokens())
				.setMessageTokens(e.messageTokens())
				.setToolResultTokens(e.toolResultTokens())
				.setToolCount(e.toolCount())
				.setLoadedSkills(e.loadedSkills())));
		} else if (event instanceof AgentEvent.TextDelta e) {
			sendEvent("text_delta", DtoMapper.toJson(TextDeltaEvent.create()
				.setText(e.text())));
		} else if (event instanceof AgentEvent.ThinkingDelta e) {
			sendEvent("thinking_delta", DtoMapper.toJson(ThinkingDeltaEvent.create()
				.setText(e.text())));
		} else if (event instanceof AgentEvent.ToolCallStarted e) {
			sendEvent("tool_started", DtoMapper.toJson(ToolStartedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setArgsJson(e.argsJson())));
		} else if (event instanceof AgentEvent.ToolCallCompleted e) {
			sendEvent("tool_completed", DtoMapper.toJson(ToolCompletedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setResult(e.result())
				.setDurationMs(e.duration().toMillis())));
		} else if (event instanceof AgentEvent.ToolCallFailed e) {
			sendEvent("tool_failed", DtoMapper.toJson(ToolFailedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setErrorMessage(e.errorMessage())
				.setDurationMs(e.duration().toMillis())));
		} else if (event instanceof AgentEvent.ToolCallDenied e) {
			sendEvent("tool_denied", DtoMapper.toJson(ToolDeniedEvent.create()
				.setToolCallId(e.toolCallId())
				.setToolName(e.toolName())
				.setReason(e.reason())));
		} else if (event instanceof AgentEvent.ToolCallResultTruncated) {
			// Not surfaced to UI
		} else if (event instanceof AgentEvent.ToolProgress e) {
			sendEvent("tool_progress", "{\"toolCallId\":\"" + JsonEscape.escape(e.toolCallId())
				+ "\",\"toolName\":\"" + JsonEscape.escape(e.toolName())
				+ "\",\"message\":\"" + JsonEscape.escape(e.message()) + "\"}");
		} else if (event instanceof AgentEvent.ApprovalRequired e) {
			Logger.info("SSE: Sending approval_required for tool '" + e.toolName()
				+ "' (approvalRequestId=" + e.approvalRequestId() + ")", ChatSSEHandler.class);
			sendEvent("approval_required", DtoMapper.toJson(ApprovalRequiredEvent.create()
				.setToolCallId(e.toolCallId())
				.setApprovalRequestId(e.approvalRequestId())
				.setToolName(e.toolName())
				.setArgsJson(e.argsJson())
				.setDisplayReason(e.displayReason())
				.setToolMode(e.toolMode() == null ? "" : e.toolMode().name())
				.setRemainingMs(remainingMs(e))));
		} else if (event instanceof AgentEvent.ApprovalResolved e) {
			sendEvent("approval_resolved", DtoMapper.toJson(ApprovalResolvedEvent.create()
				.setToolCallId(e.toolCallId())
				.setApprovalRequestId(e.approvalRequestId())
				.setOutcome(e.outcome().name())));
		} else if (event instanceof AgentEvent.TransactionBegan) {
			sendEvent("tx_began", "{}");
		} else if (event instanceof AgentEvent.TransactionCommitted e) {
			sendEvent("tx_committed", DtoMapper.toJson(TransactionCommittedEvent.create()
				.setRevisionNumber(e.revisionNumber())));
		} else if (event instanceof AgentEvent.TransactionRolledBack e) {
			sendEvent("tx_rollback", DtoMapper.toJson(TransactionRolledBackEvent.create()
				.setReason(e.reason())));
		} else if (event instanceof AgentEvent.CompactionStarted e) {
			sendEvent("compaction_started", DtoMapper.toJson(CompactionStartedEvent.create()
				.setTokensBefore(e.tokensBefore())
				.setTrigger(e.trigger())
				.setContextWindow(contextWindow())));
		} else if (event instanceof AgentEvent.CompactionCompleted) {
			// Not surfaced to UI
		} else if (event instanceof AgentEvent.TaskUpdated e) {
			sendEvent("task_update", DtoMapper.toJson(TaskUpdateEvent.create()
				.setTasksJson(e.tasksJson())));
		} else if (event instanceof AgentEvent.SubagentStarted e) {
			sendEvent("subagent_started", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setTargetAgentName(e.targetAgentName())
				.setDetail(e.taskBrief())
				.setParentToolCallId(e.parentToolCallId())));
		} else if (event instanceof AgentEvent.SubagentCompleted e) {
			sendEvent("subagent_completed", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setDetail(e.result().finalText())
				.setTotalTokens(e.result().tokenUsage().totalTokens())
				.setDurationMs(e.result().durationMs())));
		} else if (event instanceof AgentEvent.SubagentFailed e) {
			sendEvent("subagent_failed", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setDetail(e.errorSummary())));
		} else if (event instanceof AgentEvent.SubagentCancelled e) {
			sendEvent("subagent_cancelled", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())));
		} else if (event instanceof AgentEvent.ProviderError e) {
			sendEvent("provider_error", DtoMapper.toJson(ProviderErrorEvent.create()
				.setKind(e.kind())
				.setErrorMessage(e.message())));
		} else if (event instanceof AgentEvent.ModelSwitched e) {
			sendEvent("model_switched", DtoMapper.toJson(ModelSwitchedEvent.create()
				.setFromModel(e.fromModel())
				.setToModel(e.toModel())
				.setReason(e.reason())));
		} else if (event instanceof AgentEvent.RetryScheduled e) {
			// The run waits for a busy model, possibly for minutes (#678). Without this the chat
			// looks frozen for exactly as long.
			sendEvent("retry_scheduled", DtoMapper.toJson(RetryScheduledEvent.create()
				.setKind(e.kind())
				.setAttempt(e.attempt())
				.setWaitMs(e.waitMs())));
		}
	}

	private void emitDelegatedEvent(AgentEvent.DelegatedEvent delegated) {
		AgentEvent child = delegated.childEvent();
		String innerType;
		String innerData;

		if (child instanceof AgentEvent.TextDelta e) {
			innerType = "text_delta";
			innerData = DtoMapper.toJson(TextDeltaEvent.create().setText(e.text()));
		} else if (child instanceof AgentEvent.ThinkingDelta e) {
			innerType = "thinking_delta";
			innerData = DtoMapper.toJson(ThinkingDeltaEvent.create().setText(e.text()));
		} else if (child instanceof AgentEvent.ToolCallStarted e) {
			innerType = "tool_started";
			innerData = DtoMapper.toJson(ToolStartedEvent.create()
				.setToolCallId(e.toolCallId()).setToolName(e.toolName()).setArgsJson(e.argsJson()));
		} else if (child instanceof AgentEvent.ToolCallCompleted e) {
			innerType = "tool_completed";
			innerData = DtoMapper.toJson(ToolCompletedEvent.create()
				.setToolCallId(e.toolCallId()).setToolName(e.toolName())
				.setResult(e.result()).setDurationMs(e.duration().toMillis()));
		} else if (child instanceof AgentEvent.ToolCallFailed e) {
			innerType = "tool_failed";
			innerData = DtoMapper.toJson(ToolFailedEvent.create()
				.setToolCallId(e.toolCallId()).setToolName(e.toolName())
				.setErrorMessage(e.errorMessage()).setDurationMs(e.duration().toMillis()));
		} else if (child instanceof AgentEvent.ToolProgress e) {
			innerType = "tool_progress";
			innerData = "{\"toolCallId\":\"" + JsonEscape.escape(e.toolCallId())
				+ "\",\"toolName\":\"" + JsonEscape.escape(e.toolName())
				+ "\",\"message\":\"" + JsonEscape.escape(e.message()) + "\"}";
		} else if (child instanceof AgentEvent.ApprovalRequired e) {
			innerType = "approval_required";
			innerData = DtoMapper.toJson(ApprovalRequiredEvent.create()
				.setToolCallId(e.toolCallId()).setApprovalRequestId(e.approvalRequestId())
				.setToolName(e.toolName()).setArgsJson(e.argsJson())
				.setDisplayReason(e.displayReason())
				.setToolMode(e.toolMode() == null ? "" : e.toolMode().name())
				.setRemainingMs(remainingMs(e)));
		} else if (child instanceof AgentEvent.ApprovalResolved e) {
			innerType = "approval_resolved";
			innerData = DtoMapper.toJson(ApprovalResolvedEvent.create()
				.setToolCallId(e.toolCallId()).setApprovalRequestId(e.approvalRequestId())
				.setOutcome(e.outcome().name()));
		} else if (child instanceof AgentEvent.RunStarted e) {
			innerType = "run_started";
			innerData = DtoMapper.toJson(RunStartedEvent.create()
				.setRunId(e.runId()).setModel(e.model())
				.setUsedTokens(usedTokens()).setContextWindow(contextWindow()));
		} else if (child instanceof AgentEvent.RunCompleted e) {
			innerType = "run_completed";
			innerData = DtoMapper.toJson(RunCompletedEvent.create()
				.setRunId(e.runId()).setReason(e.reason().name())
				.setTotalUsage(mapTokenUsage(e.totalUsage())));
		} else if (child instanceof AgentEvent.TurnStarted e) {
			innerType = "turn_started";
			innerData = DtoMapper.toJson(TurnStartedEvent.create()
				.setTurnNumber(e.turnNumber())
				.setUsedTokens(usedTokens()).setContextWindow(contextWindow()));
		} else if (child instanceof AgentEvent.TurnCompleted e) {
			innerType = "turn_completed";
			innerData = DtoMapper.toJson(TurnCompletedEvent.create()
				.setTurnNumber(e.turnNumber()).setTurnUsage(mapTokenUsage(e.turnUsage()))
				.setContextWindow(contextWindow()));
		} else if (child instanceof AgentEvent.ToolCallDenied e) {
			innerType = "tool_denied";
			innerData = DtoMapper.toJson(ToolDeniedEvent.create()
				.setToolCallId(e.toolCallId()).setToolName(e.toolName())
				.setReason(e.reason()));
		} else if (child instanceof AgentEvent.ProviderError e) {
			innerType = "provider_error";
			innerData = DtoMapper.toJson(ProviderErrorEvent.create()
				.setKind(e.kind()).setErrorMessage(e.message()));
		} else if (child instanceof AgentEvent.ModelSwitched e) {
			innerType = "model_switched";
			innerData = DtoMapper.toJson(ModelSwitchedEvent.create()
				.setFromModel(e.fromModel()).setToModel(e.toModel()).setReason(e.reason()));
		} else {
			return;
		}

		sendEvent("delegation_event", DtoMapper.toJson(DelegationEventWrapper.create()
			.setSubagentId(delegated.subagentId())
			.setDelegationToolCallId(delegated.delegationToolCallId())
			.setEventType(innerType)
			.setEventData(innerData)));
	}

	/**
	 * Milliseconds left of an approval's deadline at the moment the frame is written.
	 *
	 * <p>
	 * Events are queued and flushed in batches, and this frame may also be a re-delivery, so the
	 * remaining time is measured against the event's own timestamp rather than assumed to be the
	 * full timeout.
	 * </p>
	 */
	private static long remainingMs(AgentEvent.ApprovalRequired event) {
		return DtoMapper.remainingMs(event.timestamp().toEpochMilli(), event.timeout());
	}

	private void pollUserInteractions() {
		// Only the run whose session is being viewed may surface its questions here; a background
		// run's ask-user prompts must not appear in the foreground chat. On switching back, the new
		// SSE handler re-emits this session's still-pending interactions (its emitted-id set is
		// fresh), so nothing is lost.
		String sessionId = _tracker.getActiveSessionId();
		ChatProcessor.AIProcessingState procState = _tracker.getProcessingStateForSession(sessionId);
		if (procState == null) {
			// A run another window of this account started in this conversation: this window streams
			// it too (issue #544), and a question it asks is as much this window's to answer as an
			// approval is (#393).
			PendingInteractionRegistry.PendingRun run =
				PendingInteractionRegistry.findBySession(_account, sessionId);
			procState = run == null ? null : run.state();
		}
		List<UserInteractionStore> stores = new ArrayList<>();
		UserInteractionStore runStore = procState == null ? null : procState.getUserInteractionStore();
		if (runStore != null) {
			stores.add(runStore);
		}
		// The background subagents of this conversation ask through their own stores, which outlive
		// the turn that started them: a question asked after that turn ended is found only here.
		stores.addAll(PendingInteractionRegistry.subagentStores(_account, sessionId));
		for (UserInteractionStore interactionStore : stores) {
			emitPending(interactionStore);
		}
	}

	private void emitPending(UserInteractionStore interactionStore) {
		if (!interactionStore.hasPending()) {
			return;
		}
		for (UserInteractionRequest req : interactionStore.getPendingRequests()) {
			if (!_emittedInteractionIds.contains(req.getId())) {
				Logger.info("Emitting user interaction event (id: " + req.getId()
					+ ", type: " + req.getType() + ", header: " + req.getHeader() + ")",
					ChatSSEHandler.class);
				try {
					sendEvent("user_interaction", DtoMapper.toJson(
						DtoMapper.toUserInteractionEvent(req)));
					_emittedInteractionIds.add(req.getId());
				} catch (Exception e) {
					Logger.error("Failed to emit user interaction event (id: " + req.getId()
						+ ", type: " + req.getType() + ")", e, ChatSSEHandler.class);
				}
			}
		}
	}

	private void sendHeartbeat() {
		try {
			HeartbeatEvent event = HeartbeatEvent.create()
				.setTs(System.currentTimeMillis());
			sendEvent("heartbeat", DtoMapper.toJson(event));
		} catch (Exception e) {
			Logger.error("Failed to send heartbeat", e, ChatSSEHandler.class);
		}
	}

	void sendEvent(String eventType, String data) {
		if (_closed.get()) {
			return;
		}

		try {
			synchronized (_writer) {
				_writer.write("event: " + eventType + "\n");
				_writer.write("data: " + data + "\n\n");
				_writer.flush();
			}

			if (_writer.checkError()) {
				stop();
				return;
			}

			_lastActivityTime = System.currentTimeMillis();
		} catch (Exception e) {
			Logger.error("Failed to write SSE event", e, ChatSSEHandler.class);
			stop();
		}
	}

	private int usedTokens() {
		com.top_logic.tl.ai.core.memory.ConversationMemory memory = _tracker.getConversationMemory();
		return memory == null ? 0 : memory.estimateTokens();
	}

	private int contextWindow() {
		com.top_logic.tl.ai.core.memory.ConversationMemory memory = _tracker.getConversationMemory();
		return memory == null ? 0 : memory.contextWindowTokens();
	}

	private static TokenUsageDto mapTokenUsage(TokenUsage usage) {
		if (usage == null) {
			return null;
		}
		return TokenUsageDto.create()
			.setInputTokens(usage.inputTokens())
			.setOutputTokens(usage.outputTokens())
			.setThinkingTokens(usage.thinkingTokens())
			.setCacheReadTokens(usage.cacheReadTokens())
			.setCacheWriteTokens(usage.cacheWriteTokens());
	}

	// ---- Test seams (package-visible) ----

	/**
	 * Creates a handler whose {@link #sendEvent(String, String)} forwards event names to the given
	 * consumer instead of writing to a real HTTP connection. The tracker and asyncContext are
	 * unused by the code paths exercised via {@link #onEventForTest(AgentEvent)}.
	 */
	static ChatSSEHandler recordingForTest(Consumer<String> eventNameSink) {
		return new ChatSSEHandler(null, null, new PrintWriter(new StringWriter())) {
			@Override
			void sendEvent(String eventType, String data) {
				eventNameSink.accept(eventType);
			}
		};
	}

	/** Submits an event directly to {@link #emitAgentEvent(AgentEvent)} for testing. */
	void onEventForTest(AgentEvent event) {
		emitAgentEvent(event);
	}

	/** No-op flush seam for test compatibility; real flush goes through the scheduled task. */
	void flushForTest() {
		// nothing to flush when using the recording factory
	}
}
