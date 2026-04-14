/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.ai.chat.api.ChatSSEHandler;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.agents.background.BackgroundAgentConfig;
import com.top_logic.ai.service.agents.background.BackgroundAgentExecutor;
import com.top_logic.ai.service.agents.background.ExecutionResult;
import com.top_logic.ai.service.agents.background.context.ChatContext;
import com.top_logic.ai.service.agents.background.trigger.PostTurnTrigger;
import com.top_logic.ai.service.agents.background.trigger.PreTurnTrigger;
import com.top_logic.ai.service.approval.ApprovalMode;
import com.top_logic.ai.service.approval.UserInteractionStore;
import com.top_logic.ai.service.approval.UserInteractionStoreBinding;
import com.top_logic.ai.service.scripting.Converters;
import com.top_logic.ai.service.tools.TimelineAgentListener;
import com.top_logic.ai.service.tools.ToolCallTracker;
import com.top_logic.ai.service.tools.ToolCallTracking;
import com.top_logic.ai.service.tools.TraceEntry;
import com.top_logic.ai.service.tools.task.TaskStore;
import com.top_logic.ai.service.tools.task.TaskStoreBinding;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.ApprovalResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.event.CompositeAgentEventListener;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;

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

	/** */
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
	 * Executes user message through the AgentRegistry entry point agent.
	 * Sets trace so AgentCall and ToolCall events are visible in the UI timeline.
	 * Uses state from tracker; {@code taskId} must already be set on state.
	 */
	static void executeOrchestrator(TaskProgressTracker taskTracker, AIProcessingState state,
			ConversationMemory conversationMemory, String userMessage,
			List<AttachmentDto> attachments) throws Exception {
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

			AgentRegistry agentRegistry = AgentRegistry.getInstance();
			if (agentRegistry == null) {
				state.addPendingMessage(
					new ChatMessage("Agent", "AgentRegistry is not available. Please ensure the service is started."));
				taskTracker.completeTask(taskId);
				return;
			}

			// Execute pre-turn background agents synchronously with model-level timeout.
			// The timeout is enforced at the LLM HTTP client level (not via Future.cancel())
			// to avoid thread-pool poisoning from lingering interrupt flags.
			List<String> preTurnAgents = agentRegistry.getBackgroundAgents(
				PreTurnTrigger.class, BackgroundAgentConfig.Scope.CHAT);
			for (String preTurnAgent : preTurnAgents) {
				BackgroundAgentConfig bgConfig =
					(BackgroundAgentConfig) agentRegistry.getAgentConfig(preTurnAgent);
				PreTurnTrigger trigger = (PreTurnTrigger) bgConfig.getTrigger();
				long timeoutMs = trigger.getTimeout();
				PreTurnTrigger.FailPolicy failPolicy = trigger.getFailPolicy();

				Duration timeoutOverride = timeoutMs > 0 ? Duration.ofMillis(timeoutMs) : null;
				BackgroundAgentExecutor bgExecutor =
					new BackgroundAgentExecutor(agentRegistry, timeoutOverride);
				try {
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
					if (failPolicy == PreTurnTrigger.FailPolicy.CLOSED) {
						state.addPendingMessage(new ChatMessage("System",
							"Message blocked: safety check failed for agent '" + preTurnAgent + "'"));
						taskTracker.completeTask(taskId);
						return;
					}
					// OPEN: continue to Orchestrator
				}
			}

			// Build user message with content parts (text + attachments)
			List<ContentPart> contentParts = new ArrayList<>();
			contentParts.add(new TextContent(trimmed));
			if (hasAttachments) {
				for (AttachmentDto att : attachments) {
					contentParts.add(toContentPart(att));
				}
			}

			Conversation conversation = conversationMemory.conversation();
			conversation.add(new UserMessage(
				UUID.randomUUID().toString(), Instant.now(), contentParts));

			// Build ToolContext with real security, approval, and transaction scope
			ToolContext toolContext = buildToolContext(interactionStore, taskTracker);

			// Run the entry point agent via AgentLoop
			String entryPointAgent = agentRegistry.getEntryPointAgentName();
			TimelineAgentListener timelineListener = new TimelineAgentListener(tracker);

			// Look up SSE handler here (deferred from sendMessage) to avoid race condition:
			// the JS client reconnects SSE during session creation, and the new connection
			// may not be ready when /chat/send arrives. Retry briefly to allow it to connect.
			ChatSSEHandler sseHandler = ChatSSEHandler.findHandler(taskTracker);
			if (sseHandler == null) {
				for (int i = 0; i < 10 && sseHandler == null; i++) {
					Thread.sleep(100);
					sseHandler = ChatSSEHandler.findHandler(taskTracker);
				}
			}
			state.setSseHandler(sseHandler);

			AgentEventListener listener;
			if (sseHandler != null) {
				listener = new CompositeAgentEventListener(List.of(timelineListener, sseHandler));
			} else {
				Logger.warn("No SSE handler found for tracker — streaming will not reach the browser.", ChatProcessor.class);
				listener = timelineListener;
			}
			String result = ThreadContextManager.inSystemInteraction(ChatProcessor.class,
				() -> agentRegistry.runAgent(entryPointAgent, conversation, conversationMemory, toolContext, listener));
			state.setLastAgentResponse(result);

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
			return ChatTimelineEvent.toolCall(chatToolCall, tc.durationMs());
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

			// Sync timeline events FIRST so the task snapshot (below) includes all tool calls.
			// Previously this ran after the snapshot, causing completed tasks to be persisted
			// with an empty timeline when tool calls and completion arrived in the same poll cycle.
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

			// Check for new messages (agent response when task completes)
			List<ChatMessage> newMessages = state.consumePendingMessages();
			if (!newMessages.isEmpty()) {
				if (currentTaskId != null) {
					ChatTask task = taskTracker.getTask(currentTaskId);
					if (task != null) {
						task = task.toBuilder()
							.withTotalUsage(taskTracker.getCumulativeUsage())
							.build();
					}
					chat.addTaskSnapshot(task);
				}
				for (ChatMessage msg : newMessages) {
					chat.addMessage(msg);
				}
				hasUpdates = true;
			}

			// Check for completion
			if (!state.isProcessing()) {
				Throwable error = state.getError();
				if (error != null) {
					if (currentTaskId != null) {
						ChatTask task = taskTracker.getTask(currentTaskId);
						if (task != null) {
							task = task.toBuilder()
								.withTotalUsage(taskTracker.getCumulativeUsage())
								.build();
						}
						chat.addTaskSnapshot(task);
					}
					String errorMsg = "Error: " + formatErrorMessage(error);
					if (error instanceof InterruptedException) {
						errorMsg = "Processing was cancelled";
					}
					chat.addMessage(new ChatMessage("AI", errorMsg));
					hasUpdates = true;
				}
				taskTracker.removeProcessingState(currentTaskId);
			}
			return hasUpdates;
		}
	}

	/**
	 * Shared send logic: adds user message, starts task, submits background orchestrator.
	 * Real-time updates are delivered via SSE ({@link com.top_logic.ai.chat.api.ChatSSEHandler}).
	 *
	 * @param tracker
	 *        Task progress tracker for this session.
	 * @param userName
	 *        Display name of the user sending the message.
	 * @param userMessage
	 *        The message text.
	 * @param attachments
	 *        File attachments sent with the message; may be null or empty.
	 * @return The send result (task ID, or busy/error); never null.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, String userName, String userMessage,
			List<AttachmentDto> attachments) {
		if (isProcessing(tracker)) {
			tracker.getChat().addMessage(new ChatMessage("System", "Please wait for the current response to complete."));
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
		tracker.addProcessingState(taskId, state);

		// Capture session context so background task writes to the correct Chat,
		// even if the user switches sessions before the task completes.
		Chat capturedChat = tracker.getChat();
		String capturedSessionId = tracker.getActiveSessionId();
		state.setChat(capturedChat);
		state.setSessionId(capturedSessionId);

		// SSE handler lookup is deferred to the background task (executeOrchestrator)
		// to avoid a race condition: the JS client reconnects SSE during session switching,
		// and the new SSE connection may not be established yet when /chat/send arrives.

		ConversationMemory conversationMemory = tracker.getConversationMemory();
		// Pass state directly to background task so it is immune to tracker-level overwrites
		Future<?> future = BACKGROUND_EXECUTOR.submit(
			new AIBackgroundTask(tracker, state, conversationMemory, userMessage, attachments));
		state._backgroundTask = future;

		return SendResult.ok(taskId);
	}

	/**
	 * Returns whether an AI task is currently in progress for the given tracker.
	 */
	public static boolean isProcessing(TaskProgressTracker tracker) {
		return tracker.hasActiveProcessingState();
	}

	/**
	 * Returns the current task ID for the given tracker, or null if none.
	 */
	public static String getCurrentTaskId(TaskProgressTracker tracker) {
		AIProcessingState state = tracker.getAnyActiveProcessingState();
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
			PostTurnTrigger.class, BackgroundAgentConfig.Scope.CHAT);

		Chat capturedChat = taskTracker.getChat();
		BackgroundAgentExecutor bgExecutor = new BackgroundAgentExecutor(agentRegistry);

		for (String agentName : postTurnAgents) {
			if (!taskTracker.tryStartBackgroundAgent(agentName)) {
				continue; // debounce: already running
			}

			String name = agentName; // effectively final for lambda
			BACKGROUND_EXECUTOR.submit(() -> {
				try {
					ChatContext context = ChatContext.lastTurn(name,
						userMessage, agentResponse, completedTask, capturedChat);
					ExecutionResult execResult = bgExecutor.execute(name, context);
					if (execResult.isBlocked()) {
						// Post-turn block: add system message with block reason
						capturedChat.addMessage(new ChatMessage("System", execResult.blockReason()));
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
	 * Converts an {@link AttachmentDto} to a tl-ai-core {@link ContentPart}.
	 *
	 * <p>
	 * Images are converted to {@link ImageContent}, PDFs to {@link PdfContent}. Unsupported
	 * types produce a placeholder {@link TextContent}.
	 * </p>
	 */
	private static ContentPart toContentPart(AttachmentDto att) {
		String mimeType = att.getMimeType();
		String base64 = att.getBase64Data();
		if (mimeType != null && mimeType.startsWith("image/")) {
			return new ImageContent("data:" + mimeType + ";base64," + base64, mimeType);
		} else if ("application/pdf".equals(mimeType)) {
			return new PdfContent(base64, mimeType);
		}
		return new TextContent("[Unsupported attachment: " + att.getFileName() + "]");
	}

	/**
	 * Builds a {@link ToolContext} for interactive chat use.
	 *
	 * <p>
	 * Uses {@link TopLogicSecurityContext} for permission checks and system mode gating,
	 * a live {@link TransactionScope} for KB transactions, and an {@link ApprovalCallback}
	 * backed by the {@link UserInteractionStore} for human-in-the-loop approval.
	 * </p>
	 */
	private static ToolContext buildToolContext(UserInteractionStore interactionStore,
			TaskProgressTracker taskTracker) {
		ApprovalCallback approvalCallback;
		if (taskTracker.getApprovalMode() == ApprovalMode.AUTO_APPROVE) {
			approvalCallback = ApprovalCallback.AUTO_APPROVE;
		} else {
			approvalCallback = request -> {
				Logger.info("Approval requested for tool '" + request.toolName()
					+ "' (mode=" + request.mode() + ", approvalRequestId="
					+ request.approvalRequestId() + "). Blocking until user responds.",
					ChatProcessor.class);
				CompletableFuture<String> future =
					interactionStore.registerApproval(request.approvalRequestId());
				try {
					String response = future.get(
						request.timeout().toMillis(), TimeUnit.MILLISECONDS);
					Logger.info("Approval response for '" + request.toolName()
						+ "': " + response, ChatProcessor.class);
					return mapApprovalResponse(response);
				} catch (TimeoutException e) {
					Logger.warn("Approval timed out for tool '" + request.toolName() + "'.",
						ChatProcessor.class);
					return new ApprovalResult.TimedOut();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return new ApprovalResult.TimedOut();
				} catch (ExecutionException e) {
					Logger.error("Approval future failed for tool '" + request.toolName()
						+ "': " + e.getMessage(), e, ChatProcessor.class);
					return new ApprovalResult.TimedOut();
				}
			};
		}
		return new ToolContext(
			new TopLogicSecurityContext(), approvalCallback, new TransactionScope(),
			taskTracker.getSessionApprovedTools());
	}

	/**
	 * Maps the raw response string from the UI to an {@link ApprovalResult}.
	 */
	private static ApprovalResult mapApprovalResponse(String response) {
		if (response == null) {
			return new ApprovalResult.TimedOut();
		}
		return switch (response) {
			case "APPROVED" -> new ApprovalResult.Approved();
			case "APPROVED_FOR_SESSION" -> new ApprovalResult.ApprovedForSession();
			default -> {
				if (response.startsWith("REJECTED")) {
					String comment = response.length() > "REJECTED".length()
						? response.substring("REJECTED:".length()).trim()
						: "";
					yield new ApprovalResult.Rejected(comment);
				}
				yield new ApprovalResult.Rejected(response);
			}
		};
	}

	/** Returns a human-readable error description, never null. */
	static String formatErrorMessage(Throwable error) {
		String msg = error.getMessage();
		if (msg != null && !msg.isEmpty()) {
			return msg;
		}
		return error.getClass().getSimpleName();
	}

	/**
	 * Background task for AI processing with detailed progress tracking.
	 *
	 * <p>
	 * The {@link AIProcessingState} is captured at construction time (not read from the tracker at
	 * execution time) to eliminate the race window where a concurrent request could overwrite the
	 * tracker's state before this task starts.
	 * </p>
	 */
	static class AIBackgroundTask implements Runnable {
		private final TaskProgressTracker _taskTracker;
		private final AIProcessingState _state;
		private final ConversationMemory _conversationMemory;
		private final String _userMessage;
		private final List<AttachmentDto> _attachments;

		/** */
		AIBackgroundTask(TaskProgressTracker taskTracker, AIProcessingState state,
				ConversationMemory conversationMemory, String userMessage, List<AttachmentDto> attachments) {
			_taskTracker = taskTracker;
			_state = state;
			_conversationMemory = conversationMemory;
			_userMessage = userMessage;
			_attachments = attachments;
		}

		@Override
		public void run() {
			AIProcessingState state = _state;
			state.setAgentThread(Thread.currentThread());
			try {
				ThreadContextManager.inSystemInteraction(ChatProcessor.class, () -> {
					try {
						executeOrchestrator(_taskTracker, state, _conversationMemory, _userMessage, _attachments);
						state.completeSuccessfully();

						ChatSSEHandler handler = state.getSseHandler();
						if (handler != null) {
							String responseText = state.getLastAgentResponse();
							if (responseText != null && !responseText.isEmpty()) {
								String html = Converters.markdownToHtmlString(responseText);
								MessageCompleteEvent event = MessageCompleteEvent.create()
									.setContentHtml(html)
									.setContentText(responseText);
								handler.sendMessageComplete(event);
							}
						}

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
			} finally {
				state.setAgentThread(null);
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

		// SSE handler and thread tracking for push-based updates and cancellation
		private volatile ChatSSEHandler _sseHandler;
		private volatile Thread _agentThread;
		private volatile String _lastAgentResponse;

		// Lifecycle management
		volatile Future<?> _backgroundTask = null;

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

		/** Cancels the background task. */
		public void cancel() {
			synchronized (_lock) {
				if (_backgroundTask != null) {
					_backgroundTask.cancel(true);
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

		/** The SSE handler for push-based real-time updates; may be null. */
		public ChatSSEHandler getSseHandler() {
			return _sseHandler;
		}

		/**
		 * Sets the SSE handler.
		 *
		 * @param handler
		 *        The handler; may be null.
		 */
		public void setSseHandler(ChatSSEHandler handler) {
			_sseHandler = handler;
		}

		/** The thread running the agent; may be null when not executing. */
		public Thread getAgentThread() {
			return _agentThread;
		}

		/**
		 * Sets the agent thread reference for cancellation support.
		 *
		 * @param thread
		 *        The thread; null to clear.
		 */
		public void setAgentThread(Thread thread) {
			_agentThread = thread;
		}

		/** The last agent response text; may be null. */
		public String getLastAgentResponse() {
			return _lastAgentResponse;
		}

		/**
		 * Sets the last agent response text.
		 *
		 * @param response
		 *        The response text.
		 */
		public void setLastAgentResponse(String response) {
			_lastAgentResponse = response;
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
