/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.agents.background.BackgroundAgentExecutor;
import com.top_logic.ai.service.agents.background.BackgroundConfig;
import com.top_logic.ai.service.agents.background.ExecutionResult;
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.trigger.PostTurnTrigger;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.ai.service.approval.UserInteractionStore;
import com.top_logic.ai.service.approval.UserInteractionStoreBinding;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.TraceEntry;
import com.top_logic.ai.service.tools.task.TaskStore;
import com.top_logic.ai.service.tools.task.TaskStoreBinding;
import com.top_logic.basic.Logger;
import com.top_logic.model.TLObject;
import com.top_logic.basic.thread.ThreadContextManager;

import dev.langchain4j.agentic.UntypedAgent;
import dev.langchain4j.data.message.Content;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.memory.ChatMemory;

/**
 * Background processing logic for the chat component.
 *
 * <p>
 * Manages AI task execution, polling, and state synchronization between background threads
 * and the UI / SSE handlers. Extracted from {@link ChatControl} to separate processing
 * concerns from rendering.
 * </p>
 */
public class ChatProcessor {

	/** Polling interval for UI updates (milliseconds). */
	static final long POLL_INTERVAL_MS = 500;

	/** Funny placeholder descriptions for in-progress chat tasks. */
	public static final String[] CHAT_TASK_PLACEHOLDERS = {
		"Flabbergasting...",
		"Contemplating...",
		"Warming up neurons...",
		"Consulting the oracle...",
		"Assembling thoughts...",
		"Polishing the answer...",
		"Thinking really hard...",
		"Consulting ancient scrolls...",
		"Loading infinite wisdom...",
		"Brewing ideas...",
		"Reticulating splines...",
		"Herding cats...",
		"Convincing the AI...",
		"Spinning up...",
	};

	static String randomChatTaskPlaceholder() {
		return CHAT_TASK_PLACEHOLDERS[ThreadLocalRandom.current().nextInt(CHAT_TASK_PLACEHOLDERS.length)];
	}

	/** Background thread pool for AI operations. */
	private static final ExecutorService BACKGROUND_EXECUTOR =
		Executors.newCachedThreadPool(new ThreadFactory() {
			private final AtomicInteger _counter = new AtomicInteger();

			@Override
			public Thread newThread(Runnable r) {
				Thread thread = new Thread(r);
				thread.setName("ChatProcessor-AI-Worker-" + _counter.incrementAndGet());
				thread.setDaemon(true);
				return thread;
			}
		});

	private ChatProcessor() {
		// Static utility class
	}

	/**
	 * Submits a fire-and-forget task to the shared background executor.
	 *
	 * <p>
	 * Intended for lightweight background operations such as manual review invocations from the
	 * REST API.
	 * </p>
	 *
	 * @param task
	 *        The task to execute.
	 */
	public static void submitBackground(Runnable task) {
		BACKGROUND_EXECUTOR.submit(task);
	}

	/**
	 * Executes user message through the AgentRegistry entry point agent.
	 * Sets trace so AgentCall and ToolCall events are visible in the UI timeline.
	 * Uses state from tracker; {@code taskId} must already be set on state.
	 */
	static void executeOrchestrator(TaskProgressTracker taskTracker, AIProcessingState state,
			ChatMemory chatMemory, String userMessage, List<AttachmentDto> attachments) throws Exception {
		if (state == null) {
			return;
		}
		String taskId = state.getCurrentTaskId();
		if (taskId == null) {
			return;
		}

		ToolCallTracker tracker = new ToolCallTracker();
		ToolCallTracking.setTracker(tracker);
		state.setToolCallTracker(tracker);

		TaskStore taskStore = new TaskStore();
		TaskStoreBinding.setStore(taskStore);
		state.setTaskStore(taskStore);

		UserInteractionStore interactionStore = new UserInteractionStore(taskTracker.getApprovalMode());
		UserInteractionStoreBinding.setStore(interactionStore);
		state.setUserInteractionStore(interactionStore);

		try {
			Logger.info("ChatProcessor: Starting agent in thread: " + Thread.currentThread().getName(),
				ChatProcessor.class);

			boolean hasAttachments = attachments != null && !attachments.isEmpty();
			boolean hasText = userMessage != null && !userMessage.trim().isEmpty();
			if (!hasText && !hasAttachments) {
				state.addPendingMessage(new ChatMessage("Agent", "Please provide a message."));
				taskTracker.completeTask(taskId);
				return;
			}
			String trimmed = hasText ? userMessage.trim() : "[See attached files]";

			// Build attachment content list for multimodal injection
			ChatMemory effectiveMemory = chatMemory;
			java.util.function.UnaryOperator<dev.langchain4j.model.chat.ChatModel> modelDecorator = null;
			if (hasAttachments) {
				List<Content> attachmentContents = new ArrayList<>();
				for (AttachmentDto att : attachments) {
					attachmentContents.add(toContent(att));
				}
				// Wrap ChatMemory so multimodal content is persisted for future conversation turns
				effectiveMemory = new MultimodalChatMemoryWrapper(chatMemory, attachmentContents);
				// Wrap ChatModel so the FIRST LLM request includes multimodal content
				List<Content> modelContents = new ArrayList<>(attachmentContents);
				modelDecorator = model -> new MultimodalChatModelWrapper(model, modelContents);
			}

			AgentRegistry agentRegistry = AgentRegistry.getInstance();
			if (agentRegistry == null) {
				state.addPendingMessage(
					new ChatMessage("Agent", "AgentRegistry is not available. Please ensure the service is started."));
				taskTracker.completeTask(taskId);
				return;
			}

			// Execute pre-turn background agents (synchronous, can block)
			List<String> preTurnAgents = agentRegistry.getBackgroundAgents(
				PreTurnTrigger.class, BackgroundConfig.Scope.CHAT);
			for (String preTurnAgent : preTurnAgents) {
				try {
					BackgroundAgentExecutor bgExecutor = new BackgroundAgentExecutor(agentRegistry);
					ChatContext bgContext = ChatContext.preTurn(preTurnAgent, trimmed, taskTracker.getChat());
					ExecutionResult execResult = bgExecutor.execute(preTurnAgent, bgContext);
					if (execResult.isBlocked()) {
						state.addPendingMessage(new ChatMessage("System", execResult.blockReason()));
						taskTracker.completeTask(taskId);
						return;
					}
				} catch (Exception ex) {
					Logger.error("Pre-turn agent '" + preTurnAgent + "' failed: " + ex.getMessage(),
						ex, ChatProcessor.class);
					// fail-open: continue to Orchestrator
				}
			}

			String entryPointAgent = agentRegistry.getEntryPointAgentName();
			java.util.Map<String, ChatMemory> subAgentMemories = taskTracker.getSubAgentMemories();
			UntypedAgent agent = agentRegistry.buildUntypedAgent(
				entryPointAgent, effectiveMemory, modelDecorator, subAgentMemories);
			String result = ThreadContextManager.inSystemInteraction(ChatProcessor.class,
				() -> (String) agent.invoke(Map.of("message", trimmed)));

			state.addPendingMessage(new ChatMessage("Agent", result));
			taskTracker.completeTask(taskId);

			// Flush pending messages (including agent response) into Chat before persisting,
			// so the persisted data includes the agent's response.
			Chat chat = state.getChat();
			if (chat == null) {
				chat = taskTracker.getChat();
			}
			applyPollUpdates(state, chat, taskTracker);

			// Persist messages to the session this task was started for (not the currently active one)
			String sessionId = state.getSessionId();
			if (sessionId == null) {
				sessionId = taskTracker.getActiveSessionId();
			}
			if (sessionId != null) {
				persistMessagesForSession(sessionId, chat);
			}

			// Trigger post-turn background agents
			triggerPostTurnAgents(taskTracker, trimmed, result, taskTracker.getTask(taskId));

		} catch (Exception ex) {
			taskTracker.failTask(taskId, formatErrorMessage(ex));
			throw ex;
		} finally {
			ToolCallTracking.setTracker(null);
			state.setToolCallTracker(null);
			TaskStoreBinding.setStore(null);
			state.setTaskStore(null);
			UserInteractionStoreBinding.setStore(null);
			state.setUserInteractionStore(null);
		}
	}

	/**
	 * Converts a {@link TraceEntry} to a {@link ChatTimelineEvent}.
	 */
	static ChatTimelineEvent toChatTimelineEvent(TraceEntry entry, ToolCallTracker tracker) {
		if (entry instanceof TraceEntry.ToolCallTraceEntry tc) {
			String resultJson = tracker.getRawResultJsons().get(tc.resultRef());
			ChatToolCall chatToolCall = new ChatToolCall(tc.toolName(), tc.args(), resultJson);
			return ChatTimelineEvent.toolCall(chatToolCall);
		}
		if (entry instanceof TraceEntry.AgentCallTraceEntry ac) {
			return ChatTimelineEvent.agentCall(ac.fromAgent(), ac.toAgent(), ac.inputSummary(), ac.resultSummary());
		}
		if (entry instanceof TraceEntry.MutableAgentCallTraceEntry mac) {
			return ChatTimelineEvent.agentCallWithDynamicResult(mac.fromAgent(), mac.toAgent(), mac.inputSummary(),
				mac::resultSummary);
		}
		return null;
	}

	/**
	 * Applies poll updates for the given state to chat and {@code taskTracker}. Clears tracker and
	 * cancels polling when processing is done.
	 *
	 * <p>
	 * Used by the SSE handler, the servlet, and the scheduled polling runnable so updates
	 * continue regardless of navigation.
	 * </p>
	 */
	public static boolean applyPollUpdates(AIProcessingState state, Chat chat,
			TaskProgressTracker taskTracker) {
		if (state == null) {
			return false;
		}

		// Synchronize on the state to prevent concurrent calls (from SSE handler poll,
		// /chat/updates servlet, or overlapping SSE reconnects) from reading the same
		// {@code lastSeenToolCallCount} and adding duplicate timeline events.
		synchronized (state) {
			boolean hasUpdates = false;
			String currentTaskId = state.getCurrentTaskId();

			// Check for new messages (agent response when task completes)
			List<ChatMessage> newMessages = state.consumePendingMessages();
			if (!newMessages.isEmpty()) {
				if (currentTaskId != null) {
					ChatTask task = taskTracker.getTask(currentTaskId);
					chat.addTaskSnapshot(task);
				}
				for (ChatMessage msg : newMessages) {
					chat.addMessage(msg);
				}
				hasUpdates = true;
			}

			// Check for new timeline events (tool calls and agent calls) in real-time
			ToolCallTracker tracker = state.getToolCallTracker();
			if (currentTaskId != null && tracker != null) {
				List<TraceEntry> timeline = tracker.getTimeline();
				int currentCount = timeline.size();
				int lastSeen = state.getLastSeenToolCallCount();
				if (currentCount > lastSeen) {
					for (int i = lastSeen; i < currentCount; i++) {
						TraceEntry entry = timeline.get(i);
						ChatTimelineEvent chatEv = toChatTimelineEvent(entry, tracker);
						if (chatEv != null) {
							taskTracker.addTimelineEvent(currentTaskId, chatEv);
						}
					}
					state.setLastSeenToolCallCount(currentCount);
					hasUpdates = true;
				}
			}

			// Check for completion
			if (!state.isProcessing()) {
				if (state._pollingTask != null) {
					state._pollingTask.cancel(false);
					state._pollingTask = null;
				}
				Throwable error = state.getError();
				if (error != null) {
					if (currentTaskId != null) {
						ChatTask task = taskTracker.getTask(currentTaskId);
						chat.addTaskSnapshot(task);
					}
					String errorMsg = "Error: " + formatErrorMessage(error);
					if (error instanceof InterruptedException) {
						errorMsg = "Processing was cancelled";
					}
					chat.addMessage(new ChatMessage("AI", errorMsg));
					hasUpdates = true;
				}
				taskTracker.clearCurrentProcessingState();
			}
			return hasUpdates;
		}
	}

	/**
	 * Shared send logic: adds user message, starts task, submits background orchestrator, optionally
	 * schedules UI polling.
	 *
	 * @param tracker
	 *        Task progress tracker for this session.
	 * @param userName
	 *        Display name of the user sending the message.
	 * @param userMessage
	 *        The message text.
	 * @param attachments
	 *        File attachments sent with the message; may be null or empty.
	 * @param repaintRequester
	 *        Called when polling has updates (e.g. {@code requestRepaint}); may be null for API-only.
	 * @param uiExecutor
	 *        TopLogic UI executor for server-side polling; if null, no polling is scheduled
	 *        (client polls via SSE).
	 * @return The send result (task ID, or busy/error); never null.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, String userName, String userMessage,
			List<AttachmentDto> attachments,
			Runnable repaintRequester, ScheduledExecutorService uiExecutor) {
		if (isProcessing(tracker)) {
			tracker.getChat().addMessage(new ChatMessage("System", "Please wait for the current response to complete."));
			if (repaintRequester != null) {
				repaintRequester.run();
			}
			return SendResult.busy();
		}

		List<ChatMessage.AttachmentMetadata> attachmentMeta = Collections.emptyList();
		if (attachments != null && !attachments.isEmpty()) {
			attachmentMeta = new ArrayList<>();
			for (AttachmentDto att : attachments) {
				attachmentMeta.add(new ChatMessage.AttachmentMetadata(
					att.getFileName(), att.getMimeType(), att.getSize()));
			}
		}
		ChatMessage message = new ChatMessage(userName, userMessage, Instant.now(), attachmentMeta);
		tracker.getChat().addMessage(message);

		// Persist user message before AI processing starts
		if (tracker.getActiveSessionId() != null) {
			persistMessages(tracker, tracker.getActiveSessionId());
		}

		AIProcessingState state = new AIProcessingState();
		state.startProcessing();
		String taskId = tracker.startTask(randomChatTaskPlaceholder());
		state.setCurrentTaskId(taskId);
		tracker.setCurrentProcessingState(state);

		// Capture session context so background task writes to the correct Chat,
		// even if the user switches sessions before the task completes.
		Chat capturedChat = tracker.getChat();
		String capturedSessionId = tracker.getActiveSessionId();
		state.setChat(capturedChat);
		state.setSessionId(capturedSessionId);

		if (repaintRequester != null) {
			tracker.setRepaintRequester(repaintRequester);
		}

		ChatMemory chatMemory = tracker.getChatMemory();
		Future<?> future = BACKGROUND_EXECUTOR.submit(new AIBackgroundTask(tracker, chatMemory, userMessage, attachments));
		state._backgroundTask = future;

		if (uiExecutor != null) {
			try {
				ScheduledFuture<?> pollingTask = uiExecutor.scheduleWithFixedDelay(
					() -> {
						AIProcessingState s = tracker.getCurrentProcessingState();
						if (s == null) {
							return;
						}
						boolean hasUpdates = applyPollUpdates(s, s.getChat(), tracker);
						if (hasUpdates) {
							tracker.requestRepaintIfNeeded();
						}
					},
					POLL_INTERVAL_MS,
					POLL_INTERVAL_MS,
					TimeUnit.MILLISECONDS);
				state._pollingTask = pollingTask;
			} catch (Exception ex) {
				Logger.error("Failed to schedule UI polling: " + ex.getMessage(), ex, ChatProcessor.class);
				future.cancel(true);
				tracker.clearCurrentProcessingState();
				return SendResult.error();
			}
		}
		return SendResult.ok(taskId);
	}

	/**
	 * Returns whether an AI task is currently in progress for the given tracker.
	 */
	public static boolean isProcessing(TaskProgressTracker tracker) {
		AIProcessingState state = tracker.getCurrentProcessingState();
		return state != null && state.isProcessing();
	}

	/**
	 * Returns the current task ID for the given tracker, or null if none.
	 */
	public static String getCurrentTaskId(TaskProgressTracker tracker) {
		AIProcessingState state = tracker.getCurrentProcessingState();
		return state != null ? state.getCurrentTaskId() : null;
	}

	/**
	 * Persists the current chat state (messages + task snapshots) to the active session.
	 */
	static void persistMessages(TaskProgressTracker tracker, String sessionId) {
		try {
			tracker.getSessionManager().persistActiveSession();
		} catch (Exception ex) {
			Logger.error("Failed to persist chat messages: " + ex.getMessage(), ex, ChatProcessor.class);
		}
	}

	/**
	 * Persists the given chat's entries to a specific session by ID.
	 * Used by the background task to persist to the correct session even if the user has
	 * switched sessions.
	 */
	static void persistMessagesForSession(String sessionId, Chat chat) {
		try {
			ThreadContextManager.inSystemInteraction(ChatProcessor.class, () -> {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session != null) {
					String json = DtoMapper.serializeEntries(chat.getEntries());
					ChatHistoryService.saveMessages(session, json);
					ChatHistoryService.touchLastUpdated(session);
				}
				return null;
			});
		} catch (Exception ex) {
			Logger.error("Failed to persist chat messages for session " + sessionId + ": " + ex.getMessage(),
				ex, ChatProcessor.class);
		}
	}

	/**
	 * Triggers all post-turn background agents to evaluate the completed agent turn.
	 *
	 * <p>
	 * Uses {@link TaskProgressTracker#tryStartBackgroundAgent(String)} for per-agent debounce.
	 * Each agent runs on {@link #BACKGROUND_EXECUTOR} as a fire-and-forget task.
	 * </p>
	 */
	static void triggerPostTurnAgents(TaskProgressTracker taskTracker, String userMessage,
			String agentResponse, ChatTask completedTask) {
		AgentRegistry agentRegistry = AgentRegistry.getInstance();
		if (agentRegistry == null) {
			return;
		}

		List<String> postTurnAgents = agentRegistry.getBackgroundAgents(
			PostTurnTrigger.class, BackgroundConfig.Scope.CHAT);

		for (String agentName : postTurnAgents) {
			if (!taskTracker.tryStartBackgroundAgent(agentName)) {
				continue; // debounce: already running
			}

			String name = agentName; // effectively final for lambda
			BACKGROUND_EXECUTOR.submit(() -> {
				try {
					BackgroundAgentExecutor executor = new BackgroundAgentExecutor(agentRegistry);
					ChatContext context = ChatContext.lastTurn(name,
						userMessage, agentResponse, completedTask, taskTracker.getChat());
					ExecutionResult execResult = executor.execute(name, context);
					if (execResult.isBlocked()) {
						// Post-turn block: add system message with block reason
						Chat chat = taskTracker.getChat();
						chat.addMessage(new ChatMessage("System", execResult.blockReason()));
					}
				} catch (Exception ex) {
					Logger.error("Background agent '" + name + "' failed: " + ex.getMessage(),
						ex, ChatProcessor.class);
				} finally {
					taskTracker.finishBackgroundAgent(name);
				}
			});
		}
	}

	/**
	 * Converts an {@link AttachmentDto} to a LangChain4j {@link Content} object.
	 *
	 * <p>
	 * Images are converted to {@link ImageContent}, PDFs to {@link PdfFileContent}. Unsupported
	 * types produce a placeholder {@link TextContent}.
	 * </p>
	 */
	/** Returns a human-readable error description, never null. */
	static String formatErrorMessage(Throwable error) {
		String msg = error.getMessage();
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}
		return error.getClass().getSimpleName();
	}

	private static Content toContent(AttachmentDto att) {
		String mimeType = att.getMimeType();
		String base64 = att.getBase64Data();
		if (mimeType != null && mimeType.startsWith("image/")) {
			return new ImageContent(base64, mimeType);
		} else if ("application/pdf".equals(mimeType)) {
			return new PdfFileContent(base64, mimeType);
		}
		return new TextContent("[Unsupported attachment: " + att.getFileName() + "]");
	}

	/**
	 * Background task for AI processing with detailed progress tracking.
	 */
	static class AIBackgroundTask implements Runnable {
		private final TaskProgressTracker _taskTracker;
		private final ChatMemory _chatMemory;
		private final String _userMessage;
		private final List<AttachmentDto> _attachments;

		AIBackgroundTask(TaskProgressTracker taskTracker, ChatMemory chatMemory,
				String userMessage, List<AttachmentDto> attachments) {
			_taskTracker = taskTracker;
			_chatMemory = chatMemory;
			_userMessage = userMessage;
			_attachments = attachments;
		}

		@Override
		public void run() {
			AIProcessingState state = _taskTracker.getCurrentProcessingState();
			if (state == null) {
				return;
			}
			try {
				ThreadContextManager.inSystemInteraction(ChatProcessor.class, () -> {
					try {
						executeOrchestrator(_taskTracker, state, _chatMemory, _userMessage, _attachments);
						state.completeSuccessfully();
						return null;
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						state.completeWithError(ex);
						return null;
					} catch (Exception ex) {
						Logger.error("AI processing failed: " + ex.getMessage(), ex, ChatProcessor.class);
						state.completeWithError(ex);
						return null;
					}
				});
			} catch (Exception ex) {
				Logger.error("Failed to run background task: " + ex.getMessage(), ex, ChatProcessor.class);
				state.completeWithError(ex);
			}
		}
	}

	/**
	 * Thread-safe container for AI processing state shared between background and UI threads.
	 */
	public static class AIProcessingState {
		private final Object _lock = new Object();

		// Processing status
		private volatile boolean _processing = false;
		private volatile boolean _completed = false;
		private volatile Throwable _error = null;

		// Current task ID for progress tracking
		private volatile String _currentTaskId = null;

		// Tool call tracking for real-time updates
		private volatile ToolCallTracker _toolCallTracker = null;
		private volatile TaskStore _taskStore = null;
		private final AtomicInteger _lastSeenToolCallCount = new AtomicInteger(0);

		// Session context: captured at creation, never changes.
		// Ensures background task writes to the correct Chat even if the user switches sessions.
		private volatile String _sessionId = null;
		private volatile Chat _chat = null;

		// User interaction store for HITL flow
		private volatile UserInteractionStore _userInteractionStore = null;

		// Result accumulation
		private final List<ChatMessage> _pendingMessages = new ArrayList<>();

		// Lifecycle management
		volatile Future<?> _backgroundTask = null;
		volatile ScheduledFuture<?> _pollingTask = null;

		/** Whether AI is currently processing (not yet completed). */
		public boolean isProcessing() {
			synchronized (_lock) {
				return _processing && !_completed;
			}
		}

		/** Starts a new AI processing session. */
		public void startProcessing() {
			synchronized (_lock) {
				_processing = true;
				_completed = false;
				_error = null;
				_pendingMessages.clear();
			}
		}

		/**
		 * Adds a pending message to the queue.
		 *
		 * @param message
		 *        The message to add.
		 */
		public void addPendingMessage(ChatMessage message) {
			synchronized (_lock) {
				_pendingMessages.add(message);
			}
		}

		/**
		 * Consumes all pending messages and clears the queue.
		 *
		 * @return The list of pending messages.
		 */
		public List<ChatMessage> consumePendingMessages() {
			synchronized (_lock) {
				List<ChatMessage> result = new ArrayList<>(_pendingMessages);
				_pendingMessages.clear();
				return result;
			}
		}

		/** Marks processing as successfully completed. */
		public void completeSuccessfully() {
			synchronized (_lock) {
				_processing = false;
				_completed = true;
			}
		}

		/**
		 * Marks processing as completed with an error.
		 *
		 * @param error
		 *        The error that occurred.
		 */
		public void completeWithError(Throwable error) {
			synchronized (_lock) {
				_processing = false;
				_completed = true;
				_error = error;
			}
		}

		/** Cancels the background task and polling task. */
		public void cancel() {
			synchronized (_lock) {
				if (_backgroundTask != null) {
					_backgroundTask.cancel(true);
				}
				if (_pollingTask != null) {
					_pollingTask.cancel(false);
				}
			}
		}

		/** The error if processing failed; null otherwise. */
		public Throwable getError() {
			return _error;
		}

		/**
		 * Sets the current task ID.
		 *
		 * @param taskId
		 *        The task ID.
		 */
		public void setCurrentTaskId(String taskId) {
			_currentTaskId = taskId;
		}

		/** The current task ID. */
		public String getCurrentTaskId() {
			return _currentTaskId;
		}

		/**
		 * Sets the tool call tracker for monitoring real-time tool call events.
		 *
		 * @param tracker
		 *        The tool call tracker; may be null.
		 */
		public void setToolCallTracker(ToolCallTracker tracker) {
			_toolCallTracker = tracker;
			if (tracker != null) {
				// Reset only when starting a new processing round (non-null tracker).
				// Do NOT reset when clearing (null) to avoid a race where the SSE poller
				// reads the tracker reference before it becomes null but reads the count
				// after it was reset to 0, causing all events to be re-added as duplicates.
				_lastSeenToolCallCount.set(0);
			}
		}

		/** The tool call tracker; may be null. */
		public ToolCallTracker getToolCallTracker() {
			return _toolCallTracker;
		}

		/**
		 * Sets the task store for agent task tracking.
		 *
		 * @param store
		 *        The task store; may be {@code null}.
		 */
		public void setTaskStore(TaskStore store) {
			_taskStore = store;
		}

		/** The task store; may be {@code null}. */
		public TaskStore getTaskStore() {
			return _taskStore;
		}

		/** The number of tool calls last seen by the polling mechanism. */
		public int getLastSeenToolCallCount() {
			return _lastSeenToolCallCount.get();
		}

		/**
		 * Updates the last seen tool call count.
		 *
		 * @param count
		 *        The new count.
		 */
		public void setLastSeenToolCallCount(int count) {
			_lastSeenToolCallCount.set(count);
		}

		/** The session ID this processing state belongs to; set once at creation. */
		public String getSessionId() {
			return _sessionId;
		}

		/** Sets the session ID (called once in {@link ChatProcessor#sendMessage}). */
		public void setSessionId(String sessionId) {
			_sessionId = sessionId;
		}

		/** The Chat this processing state writes to; set once at creation. */
		public Chat getChat() {
			return _chat;
		}

		/** Sets the captured Chat reference (called once in {@link ChatProcessor#sendMessage}). */
		public void setChat(Chat chat) {
			_chat = chat;
		}

		/** The user interaction store for HITL flow; may be null when not processing. */
		public UserInteractionStore getUserInteractionStore() {
			return _userInteractionStore;
		}

		/**
		 * Sets the user interaction store.
		 *
		 * @param store
		 *        The store; may be null to clear.
		 */
		public void setUserInteractionStore(UserInteractionStore store) {
			_userInteractionStore = store;
		}
	}

	/** Result of a send attempt: success ({@code taskId}), busy (already processing), or error. */
	public static final class SendResult {
		private final String _taskId;
		private final boolean _busy;

		private SendResult(String taskId, boolean busy) {
			_taskId = taskId;
			_busy = busy;
		}

		/**
		 * Creates a successful send result with the given task ID.
		 *
		 * @param taskId
		 *        The task ID.
		 */
		public static SendResult ok(String taskId) {
			return new SendResult(taskId, false);
		}

		/** Creates a busy send result (AI is already processing). */
		public static SendResult busy() {
			return new SendResult(null, true);
		}

		/** Creates an error send result (send failed). */
		public static SendResult error() {
			return new SendResult(null, false);
		}

		/** The task ID if successful; null otherwise. */
		public String getTaskId() {
			return _taskId;
		}

		/** Whether the send was rejected because AI is already processing. */
		public boolean isBusy() {
			return _busy;
		}
	}
}
