/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.api;

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
import java.util.regex.Pattern;

import com.top_logic.ai.chat.api.messages.ApprovalRequiredEvent;
import com.top_logic.ai.chat.api.messages.ApprovalResolvedEvent;
import com.top_logic.ai.chat.api.messages.CompactionStartedEvent;
import com.top_logic.ai.chat.api.messages.ContextCompositionEvent;
import com.top_logic.ai.chat.api.messages.ConnectedEvent;
import com.top_logic.ai.chat.api.messages.DelegationEventWrapper;
import com.top_logic.ai.chat.api.messages.HeartbeatEvent;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.InteractionResolvedEvent;
import com.top_logic.ai.chat.api.messages.LeadWakesExhaustedEvent;
import com.top_logic.ai.chat.api.messages.LeadWokenEvent;
import com.top_logic.ai.chat.api.messages.WokenSubagent;
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
import com.top_logic.ai.chat.api.messages.SubagentMessageDto;
import com.top_logic.ai.chat.api.messages.SubagentMessagesReadEvent;
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
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.WindowEntry;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.agent.subagent.SubagentInbox;
import com.top_logic.tl.ai.core.agent.subagent.SubagentNews;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.json.JsonEscape;

/**
 * Push-based implementation of {@link ChatEventHandler} that writes the chat's frames onto the
 * event stream the browser window already holds for the React layer.
 *
 * <p>
 * Receives agent events directly via {@link #onEvent(AgentEvent)}, buffers them in a
 * {@link ConcurrentLinkedQueue}, and flushes them every 50ms. User interaction events from
 * {@link UserInteractionStore} are still polled during the flush cycle because they originate
 * outside the agent event system.
 * </p>
 *
 * <p>
 * <b>One connection per page (#867).</b> A browser opens at most six connections to one host over
 * HTTP/1.1, for all its tabs together, and every page of the React layer holds one of them for its
 * event stream ({@code /react-api/events}). The chat used to open a second stream of its own: three
 * tabs then held all six, and no request of any tab reached the server any more - no click, no
 * navigation, no message. The frames of one flush therefore travel as one {@link PatchEvent} on the
 * window's {@link SSEUpdateQueue}, as <code>{"frames":[{"event":"text_delta","data":{...}}, ...]}</code>,
 * addressed to a listener id the browser chose when it attached ({@code subscribe} of the React
 * bridge). The id belongs to no control, so no control state is touched and the window's
 * interaction is not needed; the queue is thread-safe and keeps what it could not write until the
 * window's stream reconnects.
 * </p>
 *
 * <p>
 * The handler lives while its window is displayed: known to the registry, served by the same queue,
 * its page not unloaded. A tab that is closed or reloaded stops it, and with it the window's
 * presence in {@link ChatEventHandlerRegistry} - what waking a lead for subagent news and marking a
 * result seen go by.
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
 * <li>{@code subagent_started}, {@code subagent_running}, {@code subagent_completed}, {@code subagent_failed}, {@code subagent_cancelled} - Background subagents</li>
 * <li>{@code lead_woken}, {@code lead_wakes_exhausted} - A lead turn started for subagents' news, or not started any more</li>
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

	/** Prefix of every listener id, so a chat's id cannot name a control of the window. */
	static final String LISTENER_PREFIX = "tl-chat-stream:";

	/** What the browser may choose as its part of the listener id. */
	private static final Pattern TOKEN = Pattern.compile("[A-Za-z0-9_-]{1,64}");

	private final ReactWindowRegistry _registry;

	private final String _window;

	private final SSEUpdateQueue _queue;

	private final String _listener;

	/**
	 * The frames of the current flush cycle, each written by {@link #frame(String, String)}.
	 *
	 * <p>
	 * Written by the flush task only, and by {@link #start()} before that task is scheduled.
	 * </p>
	 */
	private final List<String> _frames = new ArrayList<>();

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
	 * Creates a {@link ChatSSEHandler} for one conversation of a browser window.
	 *
	 * @param tracker
	 *        Session-scoped task progress tracker.
	 * @param account
	 *        The signed-in account this stream was opened for; what makes this window one of the
	 *        account's when another of them answers a request. May be <code>null</code>.
	 * @param registry
	 *        The window registry of the session the window belongs to.
	 * @param window
	 *        The window's name, the {@code windowId} the chat sends with every request.
	 * @param queue
	 *        The window's event queue, as the registry hands it out now.
	 * @param token
	 *        The browser's part of the listener id, see {@link #isToken(String)}.
	 */
	public ChatSSEHandler(TaskProgressTracker tracker, Person account, ReactWindowRegistry registry,
			String window, SSEUpdateQueue queue, String token) {
		_tracker = tracker;
		_account = account;
		_registry = registry;
		_window = window;
		_queue = queue;
		_listener = listenerId(token);
	}

	/**
	 * Whether the browser's part of a listener id is acceptable: letters, digits, {@code -} and
	 * {@code _}, at most 64 of them.
	 */
	public static boolean isToken(String token) {
		return token != null && TOKEN.matcher(token).matches();
	}

	/**
	 * The listener id the browser subscribes to for the given token.
	 */
	static String listenerId(String token) {
		return LISTENER_PREFIX + token;
	}

	/**
	 * One frame as the patch carries it: an object of the frame's name and its payload.
	 */
	static String frame(String event, String data) {
		return "{\"event\":\"" + JsonEscape.escape(event) + "\",\"data\":" + data + "}";
	}

	/**
	 * The patch that carries the given frames, each written by {@link #frame(String, String)}.
	 */
	static String patch(List<String> frames) {
		return "{\"frames\":[" + String.join(",", frames) + "]}";
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
	 * Whether this handler writes to the given listener id.
	 */
	boolean writesTo(String listener) {
		return _listener.equals(listener);
	}

	/**
	 * Whether the window this handler writes to is still displayed: known to the registry, served
	 * by the same queue, and its page not unloaded.
	 */
	boolean isWindowShown() {
		WindowEntry window = _registry.getWindow(_window);
		return window != null && window.getQueue() == _queue && window.getUnloadedAt() == 0;
	}

	/**
	 * Starts sending this conversation's frames.
	 */
	public void start() {
		// Registering also drops the handler of an earlier attach of the same tracker.
		ChatEventHandlerRegistry.register(this);

		sendConnectedEvent();
		flushFrames();

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
		}
	}

	@Override
	public void onEvent(AgentEvent event) {
		if (_closed.get()) {
			// This handler was replaced by a newer attach of the window, or its window is gone.
			// Forward to the current active handler for the same tracker.
			ChatEventHandler current = ChatEventHandlerRegistry.find(_tracker);
			if (current != null && current != this) {
				current.onEvent(event);
			} else {
				Logger.warn("SSE handler closed, no replacement found - dropping event: "
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

	@Override
	public void sendApprovalMode(ApprovalMode mode) {
		enqueue("approval_mode", approvalModeFrame(mode));
	}

	/** The data of an {@code approval_mode} frame. */
	private static String approvalModeFrame(ApprovalMode mode) {
		return "{\"mode\":\"" + mode.name() + "\"}";
	}

	@Override
	public void sendWakesExhausted(int wakes) {
		enqueue("lead_wakes_exhausted", DtoMapper.toJson(LeadWakesExhaustedEvent.create().setWakes(wakes)));
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

		sendEvent("approval_mode", approvalModeFrame(_tracker.getApprovalMode()));
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

		// Heartbeat if no activity for 15s - into a stream that is read. A queue that still holds what
		// it could not write has no browser on it: the browser attaches again when it misses the
		// heartbeat, and a page that is gone would collect one more heartbeat every 15 s until its
		// session ends (Pietro #738).
		long now = System.currentTimeMillis();
		if (now - _lastActivityTime > HEARTBEAT_INTERVAL_MS && _queue.pendingEventCount() == 0) {
			sendHeartbeat();
		}

		flushFrames();
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
			sendEvent("run_completed", DtoMapper.toJson(withCost(RunCompletedEvent.create()
				.setRunId(e.runId())
				.setReason(e.reason().name())
				.setTotalUsage(mapTokenUsage(e.totalUsage())), e.totalCost())));
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
				.setParentToolCallId(e.parentToolCallId())
				.setQueued(e.queued())));
		} else if (event instanceof AgentEvent.SubagentRunning e) {
			sendEvent("subagent_running", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setParentToolCallId(e.parentToolCallId())));
		} else if (event instanceof AgentEvent.LeadWoken e) {
			LeadWokenEvent woken = LeadWokenEvent.create()
				.setFinished(e.finished())
				.setAsking(e.asking());
			for (SubagentNews news : e.news()) {
				woken.addSubagent(WokenSubagent.create()
					.setSubagentId(news.subagentId())
					.setAgentName(news.agentName())
					.setReason(news.reason().name())
					.setParentToolCallId(news.parentToolCallId())
					.setQuestion(news.question()));
			}
			sendEvent("lead_woken", DtoMapper.toJson(woken));
		} else if (event instanceof AgentEvent.SubagentCompleted e) {
			sendEvent("subagent_completed", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setDetail(e.result().finalText())
				.setTotalTokens(e.result().tokenUsage().totalTokens())
				.setDurationMs(e.result().durationMs())
				.setParentToolCallId(e.parentToolCallId())
				.setChildRunId(e.childRunId())
				.setStopReason(e.result().stopReason() == null ? "" : e.result().stopReason().name())));
		} else if (event instanceof AgentEvent.SubagentFailed e) {
			sendEvent("subagent_failed", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setDetail(e.errorSummary())
				.setParentToolCallId(e.parentToolCallId())
				.setChildRunId(e.childRunId())));
		} else if (event instanceof AgentEvent.SubagentCancelled e) {
			sendEvent("subagent_cancelled", DtoMapper.toJson(SubagentStatusEvent.create()
				.setSubagentId(e.subagentId())
				.setParentToolCallId(e.parentToolCallId())
				.setChildRunId(e.childRunId())));
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

	/**
	 * The run's cost on its completion frame, in the currency the chat shows costs in: the run
	 * reports US dollars, which are converted at today's rate ({@link CostConversion}).
	 */
	private static RunCompletedEvent withCost(RunCompletedEvent event, Money cost) {
		if (cost == null) {
			return event;
		}
		Money shown = CostConversion.shown(cost, null);
		return event.setTotalCost(shown.amount()).setCurrency(shown.currency());
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
			// A subagent's cost reaches its card and the panel's sum (#783).
			innerData = DtoMapper.toJson(withCost(RunCompletedEvent.create()
				.setRunId(e.runId()).setReason(e.reason().name())
				.setTotalUsage(mapTokenUsage(e.totalUsage())), e.totalCost()));
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
		} else if (child instanceof AgentEvent.UserMessageDelivered e) {
			// What the user wrote from the subagent's card has been read: the card says so.
			innerType = "user_message_read";
			SubagentMessagesReadEvent read = SubagentMessagesReadEvent.create();
			for (SubagentInbox.Message message : e.messages()) {
				read.addMessage(SubagentMessageDto.create().setId(message.id()).setText(message.text()));
			}
			innerData = DtoMapper.toJson(read);
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

		_frames.add(frame(eventType, data));
		_lastActivityTime = System.currentTimeMillis();
	}

	/**
	 * Writes what the cycle produced as one patch, and stops once the window is no longer shown.
	 */
	private void flushFrames() {
		if (_closed.get()) {
			return;
		}
		try {
			if (!_frames.isEmpty()) {
				String patch = patch(_frames);
				_frames.clear();
				deliver(patch);
			}
			if (!isWindowShown()) {
				stop();
			}
		} catch (Exception e) {
			Logger.error("Failed to write chat frames", e, ChatSSEHandler.class);
			stop();
		}
	}

	/**
	 * Puts one patch on the window's event stream, to this handler's listener id.
	 */
	void deliver(String patch) {
		_queue.enqueue(PatchEvent.create().setControlId(_listener).setPatch(patch));
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
	 * consumer instead of writing to a window. The tracker and the window are unused by the code
	 * paths exercised via {@link #onEventForTest(AgentEvent)}.
	 */
	static ChatSSEHandler recordingForTest(Consumer<String> eventNameSink) {
		return new ChatSSEHandler(null, null, null, null, null, "test") {
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
