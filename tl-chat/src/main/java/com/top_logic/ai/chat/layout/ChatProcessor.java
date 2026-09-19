/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.CitationDto;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.util.TLContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.agent.environment.RunFactsBinding;
import com.top_logic.tl.ai.core.agent.environment.UserMemories;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryBinding;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.AgentTaskMirror;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.agent.event.CompositeAgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.interaction.ToolRegistryReloadFlag;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

/**
 * Background processing logic for the chat component.
 *
 * <p>
 * Manages AI task execution, polling, and state synchronization between background threads
 * and the UI / SSE handlers. Extracted from the classic {@code ChatControl} (which lives in
 * <code>tl-chat-classic</code> since #398) to separate processing concerns from rendering.
 * </p>
 */
public class ChatProcessor {

	/**
	 * Per-turn chat UI choices: which agent to run, which model to use, and which MCP servers to
	 * attach.
	 *
	 * @param agent
	 *        Agent name, or {@code null}/{@code blank} for the configured entry-point.
	 * @param model
	 *        Model override, or {@code null}/{@code blank} to keep the agent's assignment.
	 * @param mcpOverride
	 *        When {@code true}, {@code mcpServers} replaces the agent's configured MCP list.
	 * @param mcpServers
	 *        MCP server names used when {@code mcpOverride} is {@code true}; never {@code null}.
	 * @param effort
	 *        Thinking / reasoning effort chosen for this turn, or {@code null} to leave the model's
	 *        own setting alone.
	 */
	public record ChatRunOptions(String agent, String model, boolean mcpOverride, List<String> mcpServers,
			String effort) {

		/** No UI overrides. */
		public static final ChatRunOptions DEFAULT = new ChatRunOptions(null, null, false, List.of(), null);

		/**
		 * Canonicalizes null lists so callers never have to null-check {@link #mcpServers()}.
		 */
		public ChatRunOptions {
			mcpServers = mcpServers != null ? List.copyOf(mcpServers) : List.of();
		}

		/** Options without an effort level, for callers that do not offer the setting. */
		public ChatRunOptions(String agent, String model, boolean mcpOverride, List<String> mcpServers) {
			this(agent, model, mcpOverride, mcpServers, null);
		}
	}

	/**
	 * The agent a request that names none runs as.
	 *
	 * @see AgentFactory#defaultAgentFor(com.top_logic.tl.ai.core.security.RunAuthorization,
	 *      java.util.Collection)
	 */
	private static String defaultAgent(ToolContext context, TaskProgressTracker tracker,
			AgentFactory agentFactory) {
		String improver = tracker == null ? null : tracker.getPromptImproverAgentName();
		String resolved = AgentFactory.defaultAgentFor(context.security().runAuthorization(),
			improver == null || improver.isBlank() ? Set.of() : Set.of(improver));
		return resolved != null ? resolved : agentFactory.getEntryPointAgentName();
	}

	/**
	 * The {@code kind} of an SSE {@code provider_error} frame that reports a failure of this
	 * application rather than of the model's provider.
	 *
	 * <p>
	 * Every other value of that field is a {@link ProviderException.Kind}, and a client is entitled
	 * to read it as one: {@code RATE_LIMITED} means wait, {@code CONTEXT_WINDOW_EXCEEDED} means
	 * start a new chat. This value says the opposite - the model did nothing wrong and repeating
	 * the message will not help - and exists so that the two are distinguishable at all, which
	 * {@code UNKNOWN} for everything made impossible (issue #410). The chat UI matches on the
	 * literal, so it is part of the wire contract.
	 * </p>
	 */
	public static final String APPLICATION_ERROR = "APPLICATION_ERROR";

	/**
	 * The {@link ProviderException} behind a failed run, or <code>null</code> if the run did not
	 * fail in the provider.
	 *
	 * <p>
	 * Looked for one level down as well, because the loop's own wrapping puts it there; deeper than
	 * that a provider failure is no longer what the run died of.
	 * </p>
	 */
	static ProviderException providerFailure(Throwable ex) {
		if (ex instanceof ProviderException pe) {
			return pe;
		}
		return ex.getCause() instanceof ProviderException pe ? pe : null;
	}

	/**
	 * Whose failure ended the run, as the {@code kind} of the SSE frame that reports it.
	 *
	 * @see #APPLICATION_ERROR
	 */
	static String errorKind(Throwable ex) {
		ProviderException provider = providerFailure(ex);
		return provider != null ? provider.kind().name() : APPLICATION_ERROR;
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
	 * Executes user message through the AgentFactory entry point agent.
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

		ConversationLog convLog = state.getConversationLog();
		if (convLog == null) {
			convLog = new ConversationLog();
			state.setConversationLog(convLog);
		}
		ConversationLogListener logListener = new ConversationLogListener(convLog);
		state.setLogListener(logListener);

		TaskStore taskStore = taskTracker.getActiveTaskStore();

		UserInteractionStore interactionStore = new UserInteractionStore();
		state.setUserInteractionStore(interactionStore);

		// Ordered, because the order is what the next run restores the tool catalog in: a catalog
		// restored in another order sends the same tools as different bytes (Gitea #346).
		Set<String> loadedSkills = Collections.synchronizedSet(new LinkedHashSet<>());
		loadedSkills.addAll(taskTracker.getLoadedSkills());

		// One token per run, shared by the tool context, the transaction scope and the run's
		// thread-local environment. Created here rather than in buildToolContext so that the
		// environment below already binds it: everything the run does afterwards - including the
		// recording writes after the loop returned - can then tell an interrupt caused by
		// /chat/cancel apart from an interrupt with any other cause.
		CancellationToken cancellationToken = new CancellationToken();

		// The turn this run answers, for the per-turn context block: the framework states it beside
		// the page's facts, and nothing between here and AgentFactory.buildConfig would carry it.
		Turn currentTurn = convLog.currentTurn();
		int turnNumber = currentTurn == null ? 0 : currentTurn.turnNumber();

		try (AgentEnvironment env = AgentEnvironment.open(logListener, taskStore, interactionStore,
				loadedSkills, cancellationToken);
				RunFactsBinding.Binding factsBinding =
					RunFactsBinding.bind(state.getFacts(), turnNumber);
				UserMemoryBinding.Binding memoryBinding =
					UserMemoryBinding.bind(state.getMemories())) {

			// What this run asks its user belongs to the account, not to the window it was started
			// in: any window of theirs - a second tab, a reloaded page, a new login - may answer it,
			// and the answer has to reach this store wherever the run is parked. Registered inside
			// the scope whose finally drops it again, so a run that dies leaves nothing behind. See
			// PendingInteractionRegistry.
			PendingInteractionRegistry.register(taskId, TLContext.currentUser(), taskTracker, state);

			Logger.info("ChatProcessor: Starting agent in thread: " + Thread.currentThread().getName(),
				ChatProcessor.class);

			boolean hasAttachments = attachments != null && !attachments.isEmpty();
			boolean hasText = userMessage != null && !userMessage.trim().isEmpty();
			if (!hasText && !hasAttachments) {
				state.addPendingMessage(new ChatMessage("Agent", "Please provide a message."));
				return;
			}
			String trimmed = hasText ? userMessage.trim() : "[See attached files]";

			AgentFactory agentFactory = AgentFactory.getInstance();

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
			ToolContext toolContext = buildToolContext(interactionStore, taskTracker, cancellationToken);
			// Expose the run's cancellation token so /chat/cancel can fire cooperative-cancellation
			// listeners in addition to interrupting the agent thread.
			state.setCancellationToken(cancellationToken);

			// Run the agent chosen in the chat UI, or - when the request names none - the one this
			// account may run. Resolved exactly as /chat/capabilities reports it and as the pre-check
			// of /chat/send asks about it: a client that is told one entry point and runs another is
			// refused for an agent nobody chose. Falls back to the configured entry point when this
			// account may run nothing, so that the refusal below is the ordinary one.
			ChatRunOptions options = state.getRunOptions();
			if (options == null) {
				options = ChatRunOptions.DEFAULT;
			}
			String entryPointAgent = options.agent() != null && !options.agent().isBlank()
				? options.agent().trim()
				: defaultAgent(toolContext, taskTracker, agentFactory);
			List<String> mcpOverride = options.mcpOverride() ? options.mcpServers() : null;
			AgentFactory.RunOverrides runOverrides =
				new AgentFactory.RunOverrides(options.model(), mcpOverride, options.effort());

			// Look up SSE handler here (deferred from sendMessage) to avoid race condition:
			// the JS client reconnects SSE during session creation, and the new connection
			// may not be ready when /chat/send arrives. Retry briefly to allow it to connect.
			ChatEventHandler sseHandler = ChatEventHandlerRegistry.find(taskTracker);
			if (sseHandler == null) {
				for (int i = 0; i < 10 && sseHandler == null; i++) {
					Thread.sleep(100);
					sseHandler = ChatEventHandlerRegistry.find(taskTracker);
				}
			}
			state.setSseHandler(sseHandler);

			// SSE handler still receives raw AgentEvents for text_delta streaming.
			// ConversationLogListener handles structured recording via ConversationLog.
			// The handler is resolved per event, NOT captured once: the browser reopens the
			// SSE stream on session switches (replacing the handler), and a handler captured
			// at run start would go stale — the UI would stop updating for the rest of the run.
			if (sseHandler == null) {
				Logger.warn("No SSE handler found for tracker — streaming may not reach the browser.",
					ChatProcessor.class);
			}
			String runSessionId = state.getSessionId();
			AgentEventListener sseForwarder = event -> {
				// Stream only while this run's session is the one being viewed: with
				// concurrent runs in different sessions, a background run's events would
				// otherwise render into the wrong chat. Its progress is recovered from the
				// live log when the user switches back (reattach path).
				if (runSessionId != null && !runSessionId.equals(taskTracker.getActiveSessionId())) {
					return;
				}
				ChatEventHandler currentHandler = ChatEventHandlerRegistry.find(taskTracker);
				if (currentHandler != null) {
					currentHandler.onEvent(event);
				}
			};
			AgentEventListener listener =
				new CompositeAgentEventListener(List.of(logListener, sseForwarder));
			// Allow sub-agent approval events to bubble up to the SSE handler
			logListener.setParentListener(listener);
			taskStore.setListener(listener);
			SubagentJobRegistry subagentRegistry = taskTracker.getActiveSubagentRegistry();
			ToolRegistry sessionRegistry = taskTracker.getActiveToolRegistry();

			// Record the run against the session captured at send time (not the currently
			// active one), so a completed run is never written under the wrong session.
			AgentSession agentSession = resolveAgentSession(state.getSessionId(), taskTracker.getSessionOwner());
			// The agent's plan is in-memory state of the run; mirroring it onto AgentTask rows is
			// what makes it survive a reload and lets a model-bound view show it while the run is
			// still going.
			try (SubagentRegistryBinding.Binding saBind = SubagentRegistryBinding.bind(subagentRegistry);
					AgentTaskMirror taskMirror =
						AgentTaskMirror.attach(taskStore, agentSession, toolContext.executionHandle())) {
				AgentRunner.RecordedRun runResult = AgentRunner.run(entryPointAgent, conversation,
					conversationMemory, toolContext, listener, sessionRegistry, convLog, agentSession, null,
					runOverrides);
				if (ToolRegistryReloadFlag.consume()) {
					// A tool modified a service that affects the tool catalog (e.g. FrameworkWiringTool
					// or ScriptToolAuthoringTool). Discard the cached registry so the next turn
					// rebuilds it from the updated service configuration.
					taskTracker.setActiveToolRegistry(null);
				} else {
					taskTracker.setActiveToolRegistry(runResult.registry());
				}
				// Replaced rather than merged: the run's set is authoritative, and a skill the
				// catalog evicted has to disappear from the session too, or the next run restores
				// tools that are no longer loaded.
				taskTracker.loadLoadedSkills(loadedSkills);
				String result = runResult.response();
				state.setLastAgentResponse(result);
				state.setLastCitations(lastCitations(convLog));

				state.addPendingMessage(new ChatMessage("Agent", result));

				// Flush pending messages (including agent response) into Chat before persisting,
				// so the persisted data includes the agent's response.
				Chat chat = state.getChat();
				if (chat == null) {
					chat = taskTracker.getChat();
				}
				applyPollUpdates(state, chat, taskTracker);

				// Persist conversation log, task store, and metadata in a single write
				String sessionId = state.getSessionId();
				if (sessionId == null) {
					sessionId = taskTracker.getActiveSessionId();
				}
				if (sessionId != null) {
					persistMessages(taskTracker, sessionId);
				}
			}

		} finally {
			PendingInteractionRegistry.unregister(taskId);
			state.setLogListener(null);
			taskStore.setListener(null);
			state.setUserInteractionStore(null);
		}
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
		// /chat/updates servlet, or overlapping SSE reconnects) from double-processing.
		synchronized (state) {
			boolean hasUpdates = false;
			String currentTaskId = state.getCurrentTaskId();

			// Check for new messages (agent response when task completes)
			List<ChatMessage> newMessages = state.consumePendingMessages();
			if (!newMessages.isEmpty()) {
				for (ChatMessage msg : newMessages) {
					chat.addMessage(msg);
				}
				hasUpdates = true;
			}

			// Check for completion
			if (!state.isProcessing()) {
				Throwable error = state.getError();
				if (error != null) {
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
	 * Real-time updates are delivered to the chat UI registered for the tracker (see
	 * {@link com.top_logic.ai.chat.api.ChatEventHandlerRegistry}); in this module that is the SSE
	 * handler.
	 *
	 * @param tracker
	 *        Task progress tracker for this session.
	 * @param userSession
	 *        The user's application-window sub-session the background agent runs in, so its tools
	 *        operate on the user's UI. Must not be null.
	 * @param userName
	 *        Display name of the user sending the message.
	 * @param userMessage
	 *        The message text.
	 * @param attachments
	 *        File attachments sent with the message; may be null or empty.
	 * @return The send result (task ID, or busy/error); never null.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, TLSubSessionContext userSession,
			String userName, String userMessage, List<AttachmentDto> attachments) {
		return sendMessage(tracker, userSession, userName, userMessage, attachments, ChatRunOptions.DEFAULT);
	}

	/**
	 * Sends a user message with optional agent / model / MCP overrides from the chat UI.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, TLSubSessionContext userSession,
			String userName, String userMessage, List<AttachmentDto> attachments, ChatRunOptions options) {
		return sendMessage(tracker, userSession, userName, userMessage, attachments, options, List.of());
	}

	/**
	 * Sends a user message, together with what the page states about the situation it is asked in.
	 *
	 * <p>
	 * The facts were computed on the server, from the chat element's {@code <context>} configuration
	 * and this page's channels. They are the seam between the application and the model: the harness
	 * renders the stable ones into the system prompt, and the message text stays exactly what the
	 * user wrote - in the bubble, in the log and in the request.
	 * </p>
	 *
	 * @param facts
	 *        What the page states; never {@code null}, may be empty.
	 */
	public static SendResult sendMessage(TaskProgressTracker tracker, TLSubSessionContext userSession,
			String userName, String userMessage, List<AttachmentDto> attachments, ChatRunOptions options,
			List<EnvironmentFact> facts) {
		// Busy is per chat session: a run in another session must not block this one.
		if (tracker.hasActiveProcessingStateFor(tracker.getActiveSessionId())) {
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

		// Evict any dead state left over from a prior run of this session so it cannot shadow the
		// new run in getProcessingStateForSession (which would swallow the new run's ask-user
		// questions). Prior runs' final events have already been flushed by this point.
		tracker.evictCompletedStatesForSession(tracker.getActiveSessionId());

		AIProcessingState state = new AIProcessingState();
		state.startProcessing();
		String taskId = tracker.generateTaskId();
		state.setCurrentTaskId(taskId);
		tracker.addProcessingState(taskId, state);

		// Capture session context so background task writes to the correct Chat,
		// even if the user switches sessions before the task completes.
		Chat capturedChat = tracker.getChat();
		String capturedSessionId = tracker.getActiveSessionId();
		state.setChat(capturedChat);
		state.setSessionId(capturedSessionId);
		state.setRunOptions(options != null ? options : ChatRunOptions.DEFAULT);
		state.setFacts(facts);
		// Read here, on the request thread and in the acting account's context, and only once per
		// conversation: they go into the environment block, which has to stay byte-identical
		// across the turns of one conversation. See ChatSessionManager.getActiveMemories.
		state.setMemories(tracker.getSessionManager().getActiveMemories(tracker.getSessionOwner()));

		// Create ConversationLog Turn before submitting background task
		String messageText = userMessage != null ? userMessage.trim() : "";
		ConversationLog convLog = tracker.getSessionManager().getOrCreateConversationLog();
		Turn turn = convLog.startTurn();
		turn.setUserParts(List.of(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), messageText)));
		state.setConversationLog(convLog);

		// SSE handler lookup is deferred to the background task (executeOrchestrator)
		// to avoid a race condition: the JS client reconnects SSE during session switching,
		// and the new SSE connection may not be established yet when /chat/send arrives.

		ConversationMemory conversationMemory = tracker.getConversationMemory();
		// Pass state directly to background task so it is immune to tracker-level overwrites
		Future<?> future = BACKGROUND_EXECUTOR.submit(
			new AIBackgroundTask(tracker, state, conversationMemory, userMessage, attachments, userSession));
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
	 * Resolves the {@link AgentSession} a run is recorded against, or <code>null</code> if it
	 * cannot be established as this person's (recording is then skipped for the run).
	 *
	 * <p>
	 * This is where a turn is written into a transcript, so the owner is asked again here rather
	 * than trusted from the session id the run carries: a run must never append to a conversation
	 * that is not the acting user's.
	 * </p>
	 */
	public static AgentSession resolveAgentSession(String sessionId, Person owner) {
		if (sessionId == null) {
			Logger.info("Run will not be recorded: no active session id at send time.",
				ChatProcessor.class);
			return null;
		}
		try {
			TLObject session = ChatHistoryService.findOwnedSessionById(sessionId, owner);
			if (session instanceof AgentSession agentSession) {
				return agentSession;
			}
			Logger.warn("Run will not be recorded: session " + sessionId
				+ (session == null ? " not found for this user." : " is not an AgentSession."),
				ChatProcessor.class);
			return null;
		} catch (RuntimeException ex) {
			Logger.warn("Failed to resolve agent session " + sessionId + ": " + ex.getMessage(),
				ChatProcessor.class);
			return null;
		}
	}

	/**
	 * Persists the current chat state to the active session.
	 */
	static void persistMessages(TaskProgressTracker tracker, String sessionId) {
		try {
			tracker.getSessionManager().persistActiveSession();
		} catch (Exception ex) {
			Logger.error("SessionID: " + sessionId + "; Failed to persist chat messages: " + ex.getMessage(), ex,
				ChatProcessor.class);
		}
	}

	/**
	 * Converts an {@link AttachmentDto} to a tl-ai-core {@link ContentPart}.
	 *
	 * <p>
	 * Images are converted to {@link ImageContent}, PDFs to {@link PdfContent}. Unsupported types
	 * produce a placeholder {@link TextContent} - and an image type outside
	 * {@link ImageContent#ACCEPTED_MEDIA_TYPES} is exactly that: being an image is not enough to be
	 * sendable.
	 * </p>
	 *
	 * <p>
	 * Package-visible for testing.
	 * </p>
	 */
	static ContentPart toContentPart(AttachmentDto att) {
		String mimeType = ImageContent.normalizeMediaType(att.getMimeType());
		String base64 = att.getBase64Data();
		if (mimeType.startsWith("image/")) {
			if (!ImageContent.isAccepted(mimeType)) {
				// The upload check in ChatServlet refuses this type already, so reaching here means a
				// caller that bypassed it. It is still answered the way every other unsendable
				// attachment is - a marker the model can explain to the user - rather than by building
				// a payload only the provider can reject.
				return new TextContent("[Unsupported attachment: " + att.getFileName() + " - "
					+ mimeType + " images are not accepted. Accepted image types are "
					+ ImageContent.acceptedMediaTypes() + ".]");
			}
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
			TaskProgressTracker taskTracker, CancellationToken cancellationToken) {
		ApprovalCallback approvalCallback;
		if (taskTracker.getApprovalMode() == ApprovalMode.AUTO_APPROVE) {
			approvalCallback = ApprovalCallback.AUTO_APPROVE;
		} else {
			approvalCallback = request -> {
				Logger.info("Approval requested for tool '" + request.toolName()
					+ "' (mode=" + request.mode() + ", approvalRequestId="
					+ request.approvalRequestId() + "). Blocking until user responds.",
					ChatProcessor.class);
				CompletableFuture<String> future = interactionStore.registerApproval(request);
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
				} finally {
					// Nobody waits on this future any more. Answering removes the registration
					// already, this drops it on every other exit so that a timed-out approval
					// does not linger as a phantom in getPendingApprovals().
					interactionStore.discardApproval(request.approvalRequestId());
				}
			};
		}
		// The one token of the run is shared between context and transaction scope so lock-retry
		// waits also end promptly when the user cancels the run.
		// The chat user is the run's principal: their roles decide which tools the agent may use, and
		// their model access rights which objects those tools may touch. An interactive run always
		// has a human who can answer an approval prompt, so it is not a background run.
		// ChatSessionFilter has already rejected an unauthenticated caller on /chat/*, so there is a
		// user here; forRun refuses if there is not, rather than running unchecked.
		SecurityContext security = TopLogicSecurityContext.forRun(TLContext.currentUser(), false);
		return new ToolContext(
			security, approvalCallback, new TransactionScope(cancellationToken),
			taskTracker.getSessionApprovedTools(), cancellationToken);
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

		private final SubSessionContext _userSession;

		/** */
		AIBackgroundTask(TaskProgressTracker taskTracker, AIProcessingState state,
				ConversationMemory conversationMemory, String userMessage, List<AttachmentDto> attachments,
				TLSubSessionContext userSession) {
			_taskTracker = taskTracker;
			_state = state;
			_conversationMemory = conversationMemory;
			_userMessage = userMessage;
			_attachments = attachments;
			// The originating application-window sub-session (resolved by ChatServlet from the
			// client's window id), so the agent's tools act on the user's UI - not the servlet
			// thread's throwaway sub-session.
			_userSession = userSession;
		}

		@Override
		public void run() {
			AIProcessingState state = _state;
			state.setAgentThread(Thread.currentThread());
			try {
				ThreadContextManager.inContext(_userSession, () -> {
					try {
						executeOrchestrator(_taskTracker, state, _conversationMemory, _userMessage, _attachments);
						state.completeSuccessfully();

						ChatEventHandler handler = state.getSseHandler();
						if (handler != null) {
							String responseText = state.getLastAgentResponse();
							if (responseText != null && !responseText.isEmpty()) {
								String html = Converters.markdownToHtmlString(responseText);
								MessageCompleteEvent event = MessageCompleteEvent.create()
									.setContentHtml(html)
									.setContentText(responseText);
								for (Citation citation : state.getLastCitations()) {
									event.addCitation(toCitationDto(citation));
								}
								handler.sendMessageComplete(event);
							}
						}

						return null;
					} catch (InterruptedException ex) {
						Thread.currentThread().interrupt();
						state.completeWithError(ex);
						return null;
					} catch (Throwable ex) {
						Logger.error("AI processing failed: " + ex.getMessage(), ex, ChatProcessor.class);
						state.completeWithError(ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
						notifyError(state, ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
						return null;
					}
				});
			} catch (Throwable ex) {
				Logger.error("Failed to run background task: " + ex.getMessage(), ex, ChatProcessor.class);
				state.completeWithError(ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
				notifyError(state, ex instanceof Exception ? (Exception) ex : new RuntimeException(ex));
			} finally {
				state.setAgentThread(null);
			}
		}

		/**
		 * Tells the browser and the chat history that the run died.
		 *
		 * <p>
		 * The kind says <em>whose</em> failure it was, and a reader is told a different thing for
		 * each: a provider kind means the model or its service refused, and sending again may well
		 * work; {@link ChatProcessor#APPLICATION_ERROR} means this application broke while
		 * answering, and sending the same message again will break it again. Reporting everything
		 * as an unknown provider kind is what told a user "the model reported an error, you can
		 * send again" after their own application had refused a write for a missing mandatory
		 * attribute (issue #410), and left no way for anything downstream to tell the two apart.
		 * </p>
		 */
		private void notifyError(AIProcessingState state, Throwable ex) {
			ProviderException provider = providerFailure(ex);
			String kind = errorKind(ex);
			String message = provider != null ? provider.getMessage() : ex.getMessage();
			// Send via SSE if handler is available
			ChatEventHandler handler = state.getSseHandler();
			if (handler != null) {
				try {
					ProviderErrorEvent event = ProviderErrorEvent.create()
						.setKind(kind)
						.setErrorMessage(message != null ? message : ex.getClass().getSimpleName());
					handler.sendProviderError(event);
				} catch (Exception sseEx) {
					Logger.warn("Failed to send error via SSE: " + sseEx.getMessage(), ChatProcessor.class);
				}
			}

			// Always add a chat message as fallback so the error is visible on poll
			Chat chat = state.getChat();
			if (chat == null) {
				chat = _taskTracker.getChat();
			}
			String displayMessage = message != null ? message : ex.getClass().getSimpleName();
			chat.addMessage(new ChatMessage("System", "Error: " + displayMessage));
		}
	}

	/**
	 * The citations the validator kept for the turn, in marker order.
	 *
	 * <p>
	 * Collected over every {@link Part.TextPart} of the turn rather than the last one alone: a turn
	 * that wrote text, called a tool and wrote more text carries markers in more than one part, and
	 * the answer the chat renders is all of them.
	 * </p>
	 */
	private static List<Citation> lastCitations(ConversationLog log) {
		Turn turn = lastTurn(log);
		if (turn == null) {
			return List.of();
		}
		Map<Integer, Citation> byIndex = new TreeMap<>();
		for (Part part : turn.assistantParts()) {
			if (part instanceof Part.TextPart text) {
				for (Citation citation : text.citations()) {
					byIndex.putIfAbsent(Integer.valueOf(citation.index()), citation);
				}
			}
		}
		return List.copyOf(byIndex.values());
	}

	private static Turn lastTurn(ConversationLog log) {
		if (log == null) {
			return null;
		}
		Turn open = log.currentTurn();
		if (open != null) {
			return open;
		}
		List<Turn> turns = log.turns();
		return turns.isEmpty() ? null : turns.get(turns.size() - 1);
	}

	private static CitationDto toCitationDto(Citation citation) {
		return CitationDto.create()
			.setIndex(citation.index())
			.setToolName(citation.toolName())
			.setToolCallId(citation.toolCallId())
			.setKind(citation.kind())
			.setKindLabel(citation.kindLabel())
			.setId(citation.id())
			.setLabel(citation.label())
			.setUrl(citation.url())
			.setPassage(citation.passage())
			.setExcerpt(citation.excerpt());
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

		// ConversationLog-based recording
		private volatile ConversationLog _conversationLog = null;
		private volatile ConversationLogListener _logListener = null;

		// Session context: captured at creation, never changes.
		// Ensures background task writes to the correct Chat even if the user switches sessions.
		private volatile String _sessionId = null;
		private volatile Chat _chat = null;

		// User interaction store for HITL flow
		private volatile UserInteractionStore _userInteractionStore = null;

		// Result accumulation
		private final List<ChatMessage> _pendingMessages = new ArrayList<>();

		// SSE handler and thread tracking for push-based updates and cancellation
		private volatile ChatEventHandler _sseHandler;
		private volatile Thread _agentThread;
		private volatile CancellationToken _cancellationToken;
		private volatile String _lastAgentResponse;
		private volatile List<Citation> _lastCitations = List.of();

		private volatile ChatRunOptions _runOptions = ChatRunOptions.DEFAULT;

		private volatile List<EnvironmentFact> _facts = List.of();

		/**
		 * The memories the conversation was opened with, captured at send time; never
		 * {@code null}.
		 */
		private volatile List<UserMemories.Entry> _memories = List.of();

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

		/** The conversation log for structured event recording; may be null. */
		public ConversationLog getConversationLog() {
			return _conversationLog;
		}

		/**
		 * Sets the conversation log.
		 *
		 * @param log
		 *        The conversation log; may be null.
		 */
		public void setConversationLog(ConversationLog log) {
			_conversationLog = log;
		}

		/** The conversation log listener; may be null when not processing. */
		public ConversationLogListener getLogListener() {
			return _logListener;
		}

		/**
		 * Sets the conversation log listener.
		 *
		 * @param listener
		 *        The listener; may be null to clear.
		 */
		public void setLogListener(ConversationLogListener listener) {
			_logListener = listener;
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
		public ChatEventHandler getSseHandler() {
			return _sseHandler;
		}

		/**
		 * Sets the SSE handler.
		 *
		 * @param handler
		 *        The handler; may be null.
		 */
		public void setSseHandler(ChatEventHandler handler) {
			_sseHandler = handler;
		}

		/** Per-turn agent / model / MCP choices captured at send time. */
		public ChatRunOptions getRunOptions() {
			return _runOptions;
		}

		/** Captures UI run options before the background task starts. */
		public void setRunOptions(ChatRunOptions options) {
			_runOptions = options != null ? options : ChatRunOptions.DEFAULT;
		}

		/** What the page stated about the situation, captured at send time; never {@code null}. */
		public List<EnvironmentFact> getFacts() {
			return _facts;
		}

		/** Captures the page's facts before the background task starts. */
		public void setFacts(List<EnvironmentFact> facts) {
			_facts = facts == null ? List.of() : List.copyOf(facts);
		}

		/**
		 * The memories this conversation was opened with, captured at send time; never
		 * {@code null}.
		 */
		public List<UserMemories.Entry> getMemories() {
			return _memories;
		}

		/** Captures the conversation's memories before the background task starts. */
		public void setMemories(List<UserMemories.Entry> memories) {
			_memories = memories == null ? List.of() : List.copyOf(memories);
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

		/** The run's cancellation token, used to signal cooperative cancellation; may be null. */
		public CancellationToken getCancellationToken() {
			return _cancellationToken;
		}

		/**
		 * Sets the run's cancellation token so a cancel request can fire cooperative-cancellation
		 * listeners, not just interrupt the thread.
		 *
		 * @param token
		 *        The token; null to clear.
		 */
		public void setCancellationToken(CancellationToken token) {
			_cancellationToken = token;
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

		/** Citations the validator kept for {@link #getLastAgentResponse()}; empty when none. */
		public List<Citation> getLastCitations() {
			return _lastCitations;
		}

		/**
		 * Stores the citations that belong with the last agent response.
		 *
		 * @param citations
		 *        kept citations; {@code null} becomes empty
		 */
		public void setLastCitations(List<Citation> citations) {
			_lastCitations = citations == null ? List.of() : List.copyOf(citations);
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
