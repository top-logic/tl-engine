/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.ai.chat.api.ChatEventHandler;
import com.top_logic.ai.chat.api.ChatEventHandlerRegistry;
import com.top_logic.ai.chat.api.messages.AttachmentDto;
import com.top_logic.ai.chat.api.messages.CitationDto;
import com.top_logic.ai.chat.api.messages.MessageCompleteEvent;
import com.top_logic.ai.chat.api.messages.ProviderErrorEvent;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.approval.ApprovalResult;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentFact;
import com.top_logic.tl.ai.core.agent.environment.RunFactsBinding;
import com.top_logic.tl.ai.core.agent.environment.UserMemories;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryBinding;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.event.CompositeAgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentNews;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.AgentTaskMirror;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.limits.I18NConstants;
import com.top_logic.tl.ai.core.limits.RunLimitExceededException;
import com.top_logic.tl.ai.core.limits.RunLimits;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.Citation;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.interaction.ToolRegistryReloadFlag;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Background processing logic for the chat component.
 *
 * <p>
 * Manages AI task execution, polling, and state synchronization between background threads
 * and the UI / SSE handlers. Extracted from the classic {@code ChatControl} (removed by #618) to
 * separate processing concerns from rendering.
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
	 * What a lead turn started without a user message runs with: the window, the choices and the
	 * page's facts of the user's last message in that window.
	 *
	 * @param userSession
	 *        The application-window sub-session the turn's tools act in. Held weakly: a closed
	 *        window is not kept alive for a turn nobody would see.
	 * @param options
	 *        Agent, model and MCP choices.
	 * @param facts
	 *        What the page stated.
	 */
	record LastSend(WeakReference<TLSubSessionContext> userSession, ChatRunOptions options,
			List<EnvironmentFact> facts) {
		// Plain data.
	}

	/**
	 * The agent a request that names none runs as.
	 *
	 * @see AgentFactory#defaultAgentFor(com.top_logic.tl.ai.core.security.RunAuthorization)
	 */
	private static String defaultAgent(ToolContext context, AgentFactory agentFactory) {
		String resolved = AgentFactory.defaultAgentFor(context.security().runAuthorization());
		return resolved != null ? resolved : agentFactory.getEntryPointAgentName();
	}

	/**
	 * The {@code kind} of an SSE {@code provider_error} frame that reports a failure of this
	 * application rather than of the model's provider.
	 *
	 * <p>
	 * Every other value of that field is a
	 * {@link com.top_logic.tl.ai.core.provider.ProviderException.Kind}, and a client is entitled to
	 * read it as one: {@code RATE_LIMITED} means wait, {@code CONTEXT_WINDOW_EXCEEDED} means start
	 * a new chat. This value says the opposite - the model did nothing wrong and repeating the
	 * message will not help - and exists so that the two are distinguishable at all, which
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

			// This run belongs to the account, not to the window it was started in: any window of
			// theirs - a second tab, a reloaded page, a new login - may answer what it asks and may
			// stop it, and both have to reach this state wherever the run is parked. Registered
			// inside the scope whose finally drops it again, so a run that dies leaves nothing
			// behind. See PendingInteractionRegistry.
			PendingInteractionRegistry.register(taskId, TLContext.currentUser(), taskTracker, state);

			Logger.info("ChatProcessor: Starting agent in thread: " + Thread.currentThread().getName(),
				ChatProcessor.class);

			AgentFactory agentFactory = AgentFactory.getInstance();
			Conversation conversation = conversationMemory.conversation();

			if (!state.isWake()) {
				boolean hasAttachments = attachments != null && !attachments.isEmpty();
				boolean hasText = userMessage != null && !userMessage.trim().isEmpty();
				if (!hasText && !hasAttachments) {
					state.addPendingMessage(new ChatMessage("Agent", "Please provide a message."));
					return;
				}
				String trimmed = hasText ? userMessage.trim() : "[See attached files]";

				// Build user message with content parts (text + attachments)
				List<ContentPart> contentParts = new ArrayList<>();
				contentParts.add(new TextContent(trimmed));
				if (hasAttachments) {
					for (AttachmentDto att : attachments) {
						contentParts.add(toContentPart(att));
					}
				}

				conversation.add(new UserMessage(
					UUID.randomUUID().toString(), Instant.now(), contentParts));
			}

			// Build ToolContext with real security, approval, and transaction scope
			ToolContext toolContext =
				buildToolContext(interactionStore, taskTracker, cancellationToken, state.getSessionId());
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
				: defaultAgent(toolContext, agentFactory);
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

			// SSE handlers still receive raw AgentEvents for text_delta streaming.
			// ConversationLogListener handles structured recording via ConversationLog, and comes
			// first: the forwarder reads from it where each delta ends.
			if (sseHandler == null) {
				Logger.warn("No SSE handler found for tracker — streaming may not reach the browser.",
					ChatProcessor.class);
			}
			AgentEventListener sseForwarder =
				streamForwarder(taskTracker, taskTracker.getSessionOwner(), state.getSessionId(), logListener);
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
				if (state.isWake() && !openWithNews(convLog, conversation, toolContext, listener)) {
					return;
				}
				String result;
				try {
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
					result = withStopNotice(runResult.response(), runResult.result(), toolContext);
				} catch (RunLimitExceededException ex) {
					// A ceiling of this installation, not a failure: the account's budget was already
					// spent when the run wanted to start, or the last free slot went to another request
					// between the pre-check and here. Said as the agent's own answer, because that is
					// where the user is looking - notifyError would report it as a broken application.
					Logger.info("Chat run not started: " + ex.getMessage(), ChatProcessor.class);
					result = Resources.getInstance().getString(ex.reason());
				}
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
	 * Opens a lead turn that was started without a user message with the subagents' news, and tells
	 * the windows why the turn started.
	 *
	 * @return Whether there was news. When there was none - a turn of the user's read it in between -
	 *         the empty turn is taken out of the log again and nothing runs.
	 */
	private static boolean openWithNews(ConversationLog convLog, Conversation conversation,
			ToolContext toolContext, AgentEventListener listener) {
		List<SubagentNews> news = AgentLoop.injectSubagentNews(conversation, toolContext);
		if (news.isEmpty()) {
			int index = convLog.turns().indexOf(convLog.currentTurn());
			if (index >= 0) {
				convLog.removeFrom(index);
			}
			return false;
		}
		int asking = (int) news.stream().filter(item -> item.reason() == SubagentNews.Reason.ASKS).count();
		listener.onEvent(new AgentEvent.LeadWoken("", 0, Instant.now(), news.size() - asking, asking, news));
		return true;
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
		AIProcessingState state;
		String taskId;
		// Taken with the lead turn a subagent's news starts, so that the two cannot both find the
		// chat idle and start a turn each.
		synchronized (tracker.sendLock()) {
			// Busy is per chat session: a run in another session must not block this one.
			if (tracker.hasActiveProcessingStateFor(tracker.getActiveSessionId())) {
				tracker.getChat().addMessage(new ChatMessage("System", "Please wait for the current response to complete."));
				return SendResult.busy();
			}

			String noRoom = noRoomReason(tracker);
			if (noRoom != null) {
				return SendResult.rejected(noRoom);
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

			state = new AIProcessingState();
			state.startProcessing();
			taskId = tracker.generateTaskId();
			state.setCurrentTaskId(taskId);
			// Before it is registered: that is what the other side of the lock asks for.
			state.setSessionId(tracker.getActiveSessionId());
			tracker.addProcessingState(taskId, state);
		}

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
		Future<?> future;
		try {
			future = ChatRunExecutor.submit(
				new AIBackgroundTask(tracker, state, conversationMemory, userMessage, attachments, userSession));
		} catch (ChatRunExecutor.NoRoom ex) {
			// The pre-check above said there was room and another request took it in between. Rare,
			// and the only path on which the user's message is already in the conversation - so it is
			// said in the conversation, where they will see it under their own message.
			Logger.info("Chat run refused for '" + userName + "': " + ex.getMessage(), ChatProcessor.class);
			String reason = Resources.getInstance().getString(I18NConstants.TOO_MANY_RUNS_IN_PROGRESS);
			state.completeSuccessfully();
			tracker.removeProcessingState(taskId);
			capturedChat.addMessage(new ChatMessage("System", reason));
			return SendResult.rejected(reason);
		}
		state._backgroundTask = future;

		tracker.setLastSend(new LastSend(new WeakReference<>(userSession), state.getRunOptions(), state.getFacts()));
		SubagentWaker waker = tracker.getSessionManager().getSubagentWaker(capturedSessionId);
		if (waker != null) {
			waker.userSent();
		}

		return SendResult.ok(taskId);
	}

	/**
	 * Starts a turn of the lead in the given chat that opens with its subagents' news rather than with
	 * a message of the user.
	 *
	 * <p>
	 * The turn runs as the user's last message in this window did: in that window, with the agent,
	 * model and facts it was sent with. It is recorded as a turn without user text, and the windows
	 * draw it with a line saying that subagents reported back.
	 * </p>
	 *
	 * @return Whether the turn was started; <code>false</code> when this window shows another chat,
	 *         no window shows it, a turn of it runs already, or the installation has no room.
	 */
	static boolean sendWake(TaskProgressTracker tracker, String sessionId) {
		LastSend last = tracker.getLastSend();
		if (last == null || sessionId == null || !sessionId.equals(tracker.getActiveSessionId())) {
			return false;
		}
		TLSubSessionContext userSession = last.userSession().get();
		if (userSession == null) {
			return false;
		}
		if (ChatEventHandlerRegistry.find(tracker) == null) {
			// Nobody would see the turn, and the window it would act in is gone or reconnecting.
			return false;
		}
		AIProcessingState state;
		String taskId;
		synchronized (tracker.sendLock()) {
			if (tracker.hasActiveProcessingStateFor(sessionId) || noRoomReason(tracker) != null) {
				return false;
			}
			tracker.evictCompletedStatesForSession(sessionId);
			state = new AIProcessingState();
			state.setWake(true);
			state.startProcessing();
			taskId = tracker.generateTaskId();
			state.setCurrentTaskId(taskId);
			state.setSessionId(sessionId);
			tracker.addProcessingState(taskId, state);
		}
		state.setChat(tracker.getChat());
		state.setRunOptions(last.options());
		state.setFacts(last.facts());
		state.setMemories(tracker.getSessionManager().getActiveMemories(tracker.getSessionOwner()));

		ConversationLog convLog = tracker.getSessionManager().getOrCreateConversationLog();
		convLog.startTurn().setUserParts(List.of());
		state.setConversationLog(convLog);

		try {
			state._backgroundTask = ChatRunExecutor.submit(new AIBackgroundTask(tracker, state,
				tracker.getConversationMemory(), null, null, userSession));
		} catch (ChatRunExecutor.NoRoom ex) {
			int index = convLog.turns().indexOf(convLog.currentTurn());
			if (index >= 0) {
				convLog.removeFrom(index);
			}
			state.completeSuccessfully();
			tracker.removeProcessingState(taskId);
			return false;
		}
		Logger.info("Started a lead turn for subagent news in chat " + sessionId + ".", ChatProcessor.class);
		return true;
	}

	/**
	 * How the {@link SubagentWaker} of the given chat in the given window starts a turn and tells the
	 * user.
	 */
	static SubagentWaker.Host wakeHost(TaskProgressTracker tracker, Person owner, String sessionId) {
		return new SubagentWaker.Host() {
			@Override
			public boolean leadRuns() {
				return tracker.hasActiveProcessingStateFor(sessionId);
			}

			@Override
			public boolean wake(SubagentJobRegistry.News news) {
				return sendWake(tracker, sessionId);
			}

			@Override
			public void wakesExhausted(int wakes) {
				for (ChatEventHandler handler : ChatEventHandlerRegistry.findForSession(owner, sessionId)) {
					try {
						handler.sendWakesExhausted(wakes);
					} catch (RuntimeException ex) {
						Logger.info("Could not tell a window that subagent news waits: " + ex.getMessage(),
							ChatProcessor.class);
					}
				}
			}
		};
	}

	/**
	 * Why this installation has no room for another run of this account right now, in the user's
	 * language, or <code>null</code> if it has.
	 *
	 * <p>
	 * Asked before anything of the request has been written down, so that a send with no room costs
	 * the user a sentence and nothing else - no message in a transcript with no answer under it, no
	 * turn removed by a regenerate that then does not happen.
	 * </p>
	 *
	 * <p>
	 * <b>Usually <code>null</code>.</b> An installation that configured no ceiling has room for
	 * every send, and one that configured a ceiling queues behind it; this answers a reason only
	 * where the {@code <capacity>} section asked for a refusal - a bounded chat queue, or a
	 * per-account cap - or where the account has spent a budget of the {@code <budget>} section.
	 * </p>
	 *
	 * <p>
	 * <b>Not the gate.</b> {@code AgentFactory} refuses the run whatever this says, and the pool
	 * refuses the submit; this exists so that the ordinary case reads as information rather than as
	 * a turn that failed. Between this answer and the run, another request can take the last place.
	 * </p>
	 */
	public static String noRoomReason(TaskProgressTracker tracker) {
		if (!ChatRunExecutor.hasRoom()) {
			Logger.info("Chat run refused: " + ChatRunExecutor.noRoomDescription(), ChatProcessor.class);
			return Resources.getInstance().getString(I18NConstants.TOO_MANY_RUNS_IN_PROGRESS);
		}
		ResKey capRefusal = RunLimits.concurrencyRefusal(tracker.getSessionOwner());
		if (capRefusal == null) {
			// A budget that is spent refuses the run where it starts too, but there no answer is
			// written under which the chat could show the sentence (#722).
			capRefusal = RunLimits.budgetRefusal(tracker.getSessionOwner());
		}
		return capRefusal == null ? null : Resources.getInstance().getString(capRefusal);
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
	 * The security context comes from the installation's
	 * {@link com.top_logic.tl.ai.core.security.SecurityContextFactory} - by default the one backed by
	 * TopLogic's roles and model access rights. A live {@link TransactionScope} carries the KB
	 * transactions, and the {@link ApprovalCallback} is backed by the {@link UserInteractionStore} for
	 * human-in-the-loop approval.
	 * </p>
	 *
	 * <p>
	 * A tracker that declares itself {@link TaskProgressTracker#isUnattended() unattended} - the
	 * scheduler's {@code AgentTask}, which has no browser window - is answered with the two things
	 * that follow from having no human: a {@link SecurityContext#isBackgroundRun() background} tool
	 * ceiling, and a {@link ToolContext#unattended() context} in which {@code askUser} fails instead
	 * of parking the run for the length of the interaction timeout. Such a run is also the one the
	 * factory is told about as {@link RunEntryPoint#SCHEDULED} rather than {@link RunEntryPoint#CHAT}:
	 * nobody typed its message.
	 * </p>
	 *
	 * <p>
	 * Package-visible rather than private so a test can ask what a tracker's runs are given; it has
	 * one production caller, {@link #executeOrchestrator}.
	 * </p>
	 */
	static ToolContext buildToolContext(UserInteractionStore interactionStore,
			TaskProgressTracker taskTracker, CancellationToken cancellationToken, String sessionId) {
		ApprovalCallback approvalCallback;
		if (taskTracker.getApprovalMode() == ApprovalMode.AUTO_APPROVE) {
			approvalCallback = ApprovalCallback.AUTO_APPROVE;
		} else {
			approvalCallback = request -> {
				Logger.info("Approval requested for tool '" + request.toolName()
					+ "' (mode=" + request.mode() + ", approvalRequestId="
					+ request.approvalRequestId() + "). Blocking until user responds.",
					ChatProcessor.class);
				// The store bound on the asking thread, not the one this run was started with: a
				// background subagent inherits this callback, and it asks through its job's own store,
				// which outlives this run's turn. On the run's own thread both are the same.
				UserInteractionStore bound = UserInteractionStoreBinding.getStore();
				UserInteractionStore store = bound != null ? bound : interactionStore;
				CompletableFuture<String> future = store.registerApproval(request);
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
					store.discardApproval(request.approvalRequestId());
				}
			};
		}
		// The one token of the run is shared between context and transaction scope so lock-retry
		// waits also end promptly when the user cancels the run.
		// The chat user is the run's principal: their roles decide which tools the agent may use, and
		// their model access rights which objects those tools may touch. An interactive run always
		// has a human who can answer an approval prompt, so it is not a background run; a scheduled
		// one says so on its tracker and is bounded by the policy's <background-allowlist> instead.
		// ChatSessionFilter has already rejected an unauthenticated caller on /chat/*, so there is a
		// user here; the factory refuses if there is not, rather than running unchecked.
		// Which context that is, is the installation's decision (Gitea #381): the default answers
		// approval by the tool's mode, an application that configured its own factory answers it from
		// the call.
		boolean unattended = taskTracker.isUnattended();
		RunEntryPoint entryPoint = unattended ? RunEntryPoint.SCHEDULED : RunEntryPoint.CHAT;
		SecurityContext security = RunSecurity.contextFor(
			new RunPrincipal(TLContext.currentUser(), entryPoint, unattended, sessionId));
		ToolContext context = new ToolContext(
			security, approvalCallback, new TransactionScope(cancellationToken),
			taskTracker.getSessionApprovedTools(), cancellationToken);
		return unattended ? context.unattended() : context;
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
	 * The listener that shows a run in every window showing its conversation.
	 *
	 * <p>
	 * A conversation can be open in more than one window of its account, and each of them shows the
	 * turn while it is being written - not only the window the message was sent from (issue #544).
	 * A window showing another chat is not addressed: with concurrent runs in different sessions, a
	 * background run's events would otherwise render into the wrong chat. Its progress is recovered
	 * from the live log when the user switches back.
	 * </p>
	 *
	 * <p>
	 * The windows are looked up per event, never captured: the browser reopens its stream on a
	 * session switch, and another window may open the conversation at any point of the run.
	 * </p>
	 *
	 * @param startedIn
	 *        The tracker of the window the run was started in; the run's tokens are counted there.
	 * @param owner
	 *        The account the run belongs to.
	 * @param sessionId
	 *        The chat the run writes into; <code>null</code> for a run that has none, which is shown
	 *        in the window it was started in alone.
	 * @param log
	 *        The run's log listener. It must see every event before this listener does: it is what
	 *        says where in the run's text a delta ends.
	 */
	public static AgentEventListener streamForwarder(TaskProgressTracker startedIn, Person owner,
			String sessionId, ConversationLogListener log) {
		return event -> {
			if (event instanceof AgentEvent.RunCompleted completed) {
				// Counted here, once, against the window the run belongs to - not by each window
				// that shows it.
				startedIn.addTokenUsage(completed.totalUsage());
			} else if (event instanceof AgentEvent.SubagentCompleted
				|| event instanceof AgentEvent.SubagentFailed
				|| event instanceof AgentEvent.SubagentCancelled) {
				subagentEndRecorded(startedIn, owner, sessionId, log.log());
			}
			for (ChatEventHandler handler : ChatEventHandlerRegistry.findStreamTargets(startedIn, owner,
				sessionId)) {
				if (event instanceof AgentEvent.TextDelta delta) {
					handler.onTextDelta(delta, log.streamedChars());
				} else {
					handler.onEvent(event);
				}
			}
		};
	}

	/**
	 * Has every window that holds a copy of the conversation read it back, because a subagent's end
	 * was written into its recorded transcript.
	 *
	 * <p>
	 * The end is recorded in the log of the run that started the subagent, and the copies other
	 * windows loaded still show it at work - so does a copy this window loaded anew since. Each reads
	 * the conversation back with its next request; the end is in the database by then (see
	 * {@link ConversationLogListener}). A window that holds the very log the end was recorded in is
	 * up to date already, and a window running a turn in the conversation keeps its copy: the turn is
	 * being written into it.
	 * </p>
	 */
	private static void subagentEndRecorded(TaskProgressTracker startedIn, Person owner, String sessionId,
			ConversationLog recordedIn) {
		if (sessionId == null) {
			return;
		}
		Set<TaskProgressTracker> windows = new LinkedHashSet<>();
		windows.add(startedIn);
		for (ChatEventHandler handler : ChatEventHandlerRegistry.findOthers(owner, null)) {
			if (handler.getTracker() != null) {
				windows.add(handler.getTracker());
			}
		}
		for (TaskProgressTracker window : windows) {
			if (!window.hasActiveProcessingStateFor(sessionId)) {
				window.getSessionManager().markChangedElsewhere(sessionId, recordedIn);
			}
		}
		// A window shows the chat as the subagent ends: the user sees the end there, so the chat
		// list must not report it as news.
		if (!ChatEventHandlerRegistry.findForSession(owner, sessionId).isEmpty()) {
			ChatHistoryService.markSeen(ChatHistoryService.findOwnedSessionById(sessionId, owner));
		}
	}

	/**
	 * Every chat UI that has to be told how this run ended, each with its copy of the conversation
	 * brought up to date.
	 *
	 * <p>
	 * The answer and the error that replaces it are addressed to the <em>conversation</em>, not to
	 * the tab that sent the message. That tab is often gone by the time the run finishes - the user
	 * closed it, reloaded the page, or signed in again somewhere else - while another window of the
	 * same account has the conversation open and is showing it as still running. Sending only to the
	 * handler captured at run start left that window spinning for ever and wrote one line into the
	 * log: "SSE handler closed, no replacement found - dropping message_complete" (issue #512).
	 * </p>
	 */
	private static List<ChatEventHandler> completionTargets(AIProcessingState state,
			TaskProgressTracker tracker) {
		List<ChatEventHandler> targets = ChatEventHandlerRegistry.findForSession(tracker.getSessionOwner(),
			state.getSessionId(), state.getSseHandler());
		// The turn is recorded now, in the database and in the cache of the run's own window. A
		// window that only watched the run still holds the conversation as it was when it opened
		// it; it reads it back with the next request it makes, the one that shows the finished
		// turn (issue #544).
		for (ChatEventHandler target : targets) {
			TaskProgressTracker window = target.getTracker();
			if (window != null && window != tracker) {
				window.getSessionManager().markChangedElsewhere(state.getSessionId());
			}
		}
		return targets;
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

						String responseText = state.getLastAgentResponse();
						if (responseText != null && !responseText.isEmpty()) {
							String html = Converters.markdownToHtmlString(responseText);
							MessageCompleteEvent event = MessageCompleteEvent.create()
								.setContentHtml(html)
								.setContentText(responseText);
							for (Citation citation : state.getLastCitations()) {
								event.addCitation(toCitationDto(citation));
							}
							for (ChatEventHandler handler : completionTargets(state, _taskTracker)) {
								try {
									handler.sendMessageComplete(event);
								} catch (RuntimeException ex) {
									Logger.info("Could not deliver the answer of run "
										+ state.getCurrentTaskId() + " to a window: " + ex.getMessage(),
										ChatProcessor.class);
								}
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
				// News that arrived too late for this turn to read waits for the next one.
				SubagentWaker waker = _taskTracker.getSessionManager().getSubagentWaker(state.getSessionId());
				if (waker != null) {
					waker.leadRunEnded();
				}
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
			// To whoever is looking at this chat, for the reason completionTargets gives: a run
			// that died after its window closed leaves every other window spinning on a turn that
			// has ended, exactly as a completed one does.
			ProviderErrorEvent event = ProviderErrorEvent.create()
				.setKind(kind)
				.setErrorMessage(message != null ? message : ex.getClass().getSimpleName());
			for (ChatEventHandler handler : completionTargets(state, _taskTracker)) {
				try {
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
	 * The answer, with a line saying why it stops there when the run did not end with a whole one:
	 * the account's budget ran out mid-run, the model stopped without an answer, its answer was
	 * cut off at the output limit (#677), or the run reached its limit of turns or tool calls and
	 * answered with what it had.
	 *
	 * <p>
	 * The run itself is orderly - it kept every turn it finished - but an answer that simply ends is
	 * indistinguishable from one the model chose to end, and a turn with no answer at all reads as
	 * finished. The notice is part of the answer text rather than a separate system message so that
	 * it is persisted with it and is still there when the conversation is reopened.
	 * </p>
	 */
	private static String withStopNotice(String response, AgentResult result, ToolContext context) {
		if (result == null) {
			return response;
		}
		ResKey notice = switch (result.stopReason()) {
			case BUDGET_EXHAUSTED -> RunLimits.budgetStopNotice(context.security());
			case NO_ANSWER -> com.top_logic.tl.ai.core.agent.I18NConstants.RUN_STOPPED_NO_ANSWER;
			case OUTPUT_LIMIT -> com.top_logic.tl.ai.core.agent.I18NConstants.RUN_STOPPED_OUTPUT_LIMIT;
			case MAX_TURNS -> com.top_logic.tl.ai.core.agent.I18NConstants.RUN_STOPPED_TURN_LIMIT;
			case MAX_TOOL_CALLS -> com.top_logic.tl.ai.core.agent.I18NConstants.RUN_STOPPED_TOOL_LIMIT;
			default -> null;
		};
		if (notice == null) {
			return response;
		}
		String text = Resources.getInstance().getString(notice);
		return response == null || response.isBlank() ? text : response + "\n\n" + text;
	}

	/**
	 * The citations the validator kept for the turn, in marker order.
	 *
	 * <p>
	 * Collected over every {@link com.top_logic.tl.ai.core.session.Part.TextPart} of the turn
	 * rather than the last one alone: a turn that wrote text, called a tool and wrote more text
	 * carries markers in more than one part, and the answer the chat renders is all of them.
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
			.setExcerpt(citation.excerpt())
			.setArchiveUrl(citation.archiveUrl())
			.setArchivedAt(citation.archivedAt());
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

		/** Whether the run opens with subagents' news rather than with a message of the user. */
		private volatile boolean _wake;

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

		/** Whether the run opens with subagents' news rather than with a message of the user. */
		public boolean isWake() {
			return _wake;
		}

		/** @see #isWake() */
		void setWake(boolean wake) {
			_wake = wake;
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

	/**
	 * Result of a send attempt: success ({@code taskId}), busy (already processing), rejected (no
	 * room right now, with a reason), or error.
	 */
	public static final class SendResult {
		private final String _taskId;

		private final boolean _busy;

		private final String _reason;

		private SendResult(String taskId, boolean busy, String reason) {
			_taskId = taskId;
			_busy = busy;
			_reason = reason;
		}

		/**
		 * Creates a successful send result with the given task ID.
		 *
		 * @param taskId
		 *        The task ID.
		 */
		public static SendResult ok(String taskId) {
			return new SendResult(taskId, false, null);
		}

		/** Creates a busy send result (AI is already processing). */
		public static SendResult busy() {
			return new SendResult(null, true, null);
		}

		/**
		 * Creates a send result for a request this installation has no room for right now.
		 *
		 * <p>
		 * Not an error and not "busy": nothing is wrong with the request and this chat session is
		 * not running anything - there is simply a ceiling, and trying again shortly can work.
		 * </p>
		 *
		 * @param reason
		 *        The sentence the client shows, in the user's language.
		 */
		public static SendResult rejected(String reason) {
			return new SendResult(null, false, reason);
		}

		/** The task ID if successful; null otherwise. */
		public String getTaskId() {
			return _taskId;
		}

		/** Whether the send was rejected because AI is already processing. */
		public boolean isBusy() {
			return _busy;
		}

		/** Whether a ceiling of this installation refused the send for now. */
		public boolean isRejected() {
			return _reason != null;
		}

		/**
		 * Why the send was refused, in the user's language, or <code>null</code> if it was not.
		 *
		 * @see #rejected(String)
		 */
		public String getReason() {
			return _reason;
		}
	}
}
