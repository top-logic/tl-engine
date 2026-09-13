/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import com.top_logic.ai.chat.api.messages.MessageFeedbackDto;
import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.layout.WindowScope;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.message.TokenUsage;

/**
 * Session-scoped storage for chat state and processing management.
 *
 * <p>
 * This tracker stores session state associated with a {@link WindowScope}, making it
 * persist across UI navigation within the same session.
 * </p>
 */
public class TaskProgressTracker {

	/** Static storage keyed by WindowScope (weak references for automatic cleanup). */
	private static final Map<WindowScope, TaskProgressTracker> TRACKERS = new WeakHashMap<>();

	private final ChatSessionManager _sessionManager;

	{
		_sessionManager = new ChatSessionManager();
		_sessionManager.setTracker(this);
	}

	/**
	 * Active AI processing states keyed by task ID. Supports concurrent processing (e.g. when two
	 * users share a session) and eliminates the single-slot overwrite race where a second request
	 * could overwrite the first request's state before its background task starts.
	 */
	private final ConcurrentHashMap<String, AIProcessingState> _processingStates = new ConcurrentHashMap<>();

	/** User display name resolved from TLContext (set where context is available, used as fallback elsewhere). */
	private volatile String _userName = null;

	/** User's first name for the welcome screen greeting. */
	private volatile String _welcomeName = null;

	/** Configured prompt-improver agent name, or {@code null} when the feature is disabled. */
	private volatile String _promptImproverAgentName = null;

	/** Current prompt-improvement run, independent of the main chat processing states. */
	private final AtomicReference<PromptImprovementState> _promptImprovementState = new AtomicReference<>();

	/**
	 * Registry key ({@code windowName}) of the sub-session this tracker's chat UI was rendered in.
	 *
	 * <p>
	 * Rendered into the client as {@code data-window-id} and echoed back by the client so the
	 * {@code ChatServlet} can resolve the originating sub-session. See {@link ChatSessionRegistry}.
	 * </p>
	 */
	private volatile String _windowId = null;

	/** Session-scoped approval mode. Persists across messages/processing states. */
	private volatile ApprovalMode _approvalMode = ApprovalMode.MANUAL;

	/** Tools approved for the current session. */
	private final Set<String> _sessionApprovedTools = ConcurrentHashMap.newKeySet();

	/** Skills loaded via loadSkill() in this session. */
	private final Set<String> _loadedSkills = ConcurrentHashMap.newKeySet();

	/**
	 * Answer ratings of the active session, keyed by the zero-based turn index they belong to.
	 *
	 * <p>
	 * Session state like the approved tools above: it is written into the session metadata on
	 * persist and read back when the session is re-opened, and it is cleared when the window
	 * switches to another session.
	 * </p>
	 */
	private final Map<Integer, MessageFeedbackDto> _feedback = new ConcurrentHashMap<>();

	/** Effort level last chosen in the active session; {@code null} when none was. */
	private volatile String _effort;

	/** Cumulative token usage across all runs in this session. */
	private volatile TokenUsage _cumulativeUsage = TokenUsage.EMPTY;

	/**
	 * Returns the chat for this window. Same instance for add and render so messages persist.
	 */
	public Chat getChat() {
		return _sessionManager.getActiveChat();
	}

	/**
	 * Registers a processing state for the given task ID.
	 *
	 * @param taskId
	 *        The task ID (must not be null).
	 * @param state
	 *        The processing state to register.
	 */
	public void addProcessingState(String taskId, AIProcessingState state) {
		_processingStates.put(taskId, state);
	}

	/**
	 * Removes the processing state for the given task ID (called when processing finishes).
	 *
	 * @param taskId
	 *        The task ID to remove.
	 */
	public void removeProcessingState(String taskId) {
		if (taskId != null) {
			_processingStates.remove(taskId);
		}
	}

	/**
	 * Evicts completed (non-{@link AIProcessingState#isProcessing() processing}) states for the
	 * given chat session.
	 *
	 * <p>
	 * Called when a new run starts for a session so a prior run's dead state cannot linger and
	 * shadow the new run in {@link #getProcessingStateForSession(String)}. Only non-processing
	 * states are removed, so an in-flight run of the same session is never touched. Evicting at run
	 * start (rather than at run end) is safe: the completed run's final events have already been
	 * flushed to the client - the user only sends a follow-up message after seeing the previous run
	 * finish.
	 * </p>
	 *
	 * @param sessionId
	 *        The chat session id; {@code null} is a no-op.
	 */
	public void evictCompletedStatesForSession(String sessionId) {
		if (sessionId == null) {
			return;
		}
		_processingStates.values()
			.removeIf(state -> sessionId.equals(state.getSessionId()) && !state.isProcessing());
	}

	/**
	 * Returns the processing state for the given task ID, or null if not found.
	 *
	 * @param taskId
	 *        The task ID.
	 * @return The processing state, or null.
	 */
	public AIProcessingState getProcessingState(String taskId) {
		return _processingStates.get(taskId);
	}

	/**
	 * Returns any active processing state, or null if none. Used by SSE handlers, polling
	 * endpoints, and UI rendering that need to find "the" current state without knowing the task
	 * ID.
	 *
	 * @return An active processing state, or any remaining state, or null.
	 */
	public AIProcessingState getAnyActiveProcessingState() {
		for (AIProcessingState state : _processingStates.values()) {
			if (state.isProcessing()) {
				return state;
			}
		}
		// Fall back to any remaining state (completed but not yet cleaned up)
		var it = _processingStates.values().iterator();
		return it.hasNext() ? it.next() : null;
	}

	/**
	 * Returns the processing state of the run in the given chat session, or null if that session
	 * has no run.
	 *
	 * <p>
	 * Unlike {@link #getAnyActiveProcessingState()}, this never returns another session's run — so
	 * a background run's interactions are not surfaced into the foreground chat.
	 * </p>
	 *
	 * <p>
	 * When several states share the session id (e.g. a completed run whose state has not been
	 * evicted yet plus the live run of a follow-up message), the {@link AIProcessingState#isProcessing()
	 * processing} one is preferred. This mirrors {@link #getAnyActiveProcessingState()} and prevents a
	 * dead state (with a {@code null} {@code UserInteractionStore}) from shadowing the live run and
	 * swallowing its pending questions.
	 * </p>
	 *
	 * @param sessionId
	 *        The chat session id; null returns any active state (single-session fallback).
	 */
	public AIProcessingState getProcessingStateForSession(String sessionId) {
		if (sessionId == null) {
			return getAnyActiveProcessingState();
		}
		AIProcessingState anyMatch = null;
		for (AIProcessingState state : _processingStates.values()) {
			if (sessionId.equals(state.getSessionId())) {
				if (state.isProcessing()) {
					return state;
				}
				if (anyMatch == null) {
					anyMatch = state;
				}
			}
		}
		return anyMatch;
	}

	/**
	 * Returns whether an AI task is currently in progress for the given chat session.
	 *
	 * <p>
	 * Runs are per-session: a run in another session does not block this one.
	 * </p>
	 */
	public boolean hasActiveProcessingStateFor(String sessionId) {
		if (sessionId == null) {
			return hasActiveProcessingState();
		}
		for (AIProcessingState state : _processingStates.values()) {
			if (state.isProcessing() && sessionId.equals(state.getSessionId())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns whether any AI task is currently in progress for this tracker.
	 */
	public boolean hasActiveProcessingState() {
		for (AIProcessingState state : _processingStates.values()) {
			if (state.isProcessing()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the user display name stored on this tracker, or null if not yet set.
	 *
	 * <p>
	 * Set from contexts where {@code TLContext} is available (e.g. during component creation). Used
	 * as fallback in contexts where {@code TLContext} is not available (e.g. {@code ChatServlet}).
	 * </p>
	 */
	public String getUserName() {
		return _userName;
	}

	/**
	 * Stores the user display name on this tracker for cross-context resolution.
	 *
	 * @param userName
	 *        The display name of the current user.
	 */
	public void setUserName(String userName) {
		_userName = userName;
	}

	/**
	 * Returns the welcome name (first name) for the welcome screen greeting, or null if not set.
	 */
	public String getWelcomeName() {
		return _welcomeName;
	}

	/**
	 * Stores the welcome name (first name) for the welcome screen greeting.
	 *
	 * @param welcomeName
	 *        The first name to display in the welcome greeting.
	 */
	public void setWelcomeName(String welcomeName) {
		_welcomeName = welcomeName;
	}

	/**
	 * Returns the administrator-configured prompt-improver agent name, or {@code null} when prompt
	 * improvement is disabled for this chat.
	 */
	public String getPromptImproverAgentName() {
		return _promptImproverAgentName;
	}

	/**
	 * Stores the administrator-configured prompt-improver agent name.
	 *
	 * @param agentName
	 *        The configured agent name, or {@code null} to disable prompt improvement.
	 */
	public void setPromptImproverAgentName(String agentName) {
		_promptImproverAgentName = agentName;
	}

	/**
	 * Returns the current prompt-improvement run, or {@code null} if none is active.
	 */
	public PromptImprovementState getPromptImprovementState() {
		return _promptImprovementState.get();
	}

	/**
	 * Installs a prompt-improvement run and returns the previously installed run, if any.
	 */
	public PromptImprovementState replacePromptImprovementState(PromptImprovementState state) {
		return _promptImprovementState.getAndSet(state);
	}

	/**
	 * Clears the given prompt-improvement run only if it is still current.
	 *
	 * @return Whether the run was current and has been cleared.
	 */
	public boolean clearPromptImprovementState(PromptImprovementState state) {
		return _promptImprovementState.compareAndSet(state, null);
	}

	/**
	 * Cancels the current prompt-improvement run when its request ID matches.
	 *
	 * @param requestId
	 *        The request ID supplied by the client.
	 * @return Whether a matching run was found and cancelled.
	 */
	public boolean cancelPromptImprovement(String requestId) {
		PromptImprovementState state = _promptImprovementState.get();
		if (state == null || requestId == null || !requestId.equals(state.getRequestId())) {
			return false;
		}
		state.getCancellationToken().cancel();
		return true;
	}

	/**
	 * Returns the window id ({@code windowName}) of the sub-session this tracker belongs to, or
	 * {@code null} if the chat UI has not been rendered yet.
	 *
	 * @see ChatSessionRegistry#windowId(com.top_logic.base.context.TLSessionContext,
	 *      com.top_logic.base.context.TLSubSessionContext)
	 */
	public String getWindowId() {
		return _windowId;
	}

	/**
	 * Stores the window id of the sub-session this tracker belongs to (set while rendering the chat
	 * component).
	 *
	 * @param windowId
	 *        The sub-session registry key.
	 */
	public void setWindowId(String windowId) {
		_windowId = windowId;
	}

	/** Returns the session-scoped approval mode. */
	public ApprovalMode getApprovalMode() {
		return _approvalMode;
	}

	/** Sets the session-scoped approval mode. */
	public void setApprovalMode(ApprovalMode mode) {
		_approvalMode = mode;
	}

	/**
	 * Returns the active session ID for persistence, or null if no session is selected.
	 */
	public String getActiveSessionId() {
		return _sessionManager.getActiveSessionId();
	}

	/**
	 * Sets the active session ID for persistence. Triggers session switching via
	 * {@link ChatSessionManager}.
	 *
	 * @param sessionId
	 *        The session ID; null to clear.
	 */
	public void setActiveSessionId(String sessionId) {
		_sessionManager.switchSession(sessionId);
	}

	/**
	 * Returns the active session's {@link TaskStore}.
	 */
	public TaskStore getActiveTaskStore() {
		return _sessionManager.getActiveTaskStore();
	}

	/**
	 * Returns the active session's {@link SubagentJobRegistry}, creating one on first use.
	 * Returns {@code null} if no session is active.
	 */
	public SubagentJobRegistry getActiveSubagentRegistry() {
		return _sessionManager.getActiveSubagentRegistry();
	}

	/**
	 * Returns the session-scoped tool registry, or {@code null} if not yet created.
	 */
	public ToolRegistry getActiveToolRegistry() {
		return _sessionManager.getActiveToolRegistry();
	}

	/**
	 * Sets the session-scoped tool registry.
	 */
	public void setActiveToolRegistry(ToolRegistry registry) {
		_sessionManager.setActiveToolRegistry(registry);
	}

	/**
	 * Returns the session manager for this tracker.
	 */
	public ChatSessionManager getSessionManager() {
		return _sessionManager;
	}

	/**
	 * Returns the conversation memory for this session, creating it on first access. The memory
	 * uses a {@link com.top_logic.tl.ai.core.memory.TruncatingStrategy} to keep conversation
	 * context within the configured token budget.
	 */
	public ConversationMemory getConversationMemory() {
		return _sessionManager.getActiveConversationMemory();
	}

	/**
	 * Gets or creates a {@link TaskProgressTracker} from the given {@link WindowScope}.
	 *
	 * @param windowScope
	 *        The window scope to get/store the tracker.
	 * @return The tracker instance, never null.
	 */
	public static TaskProgressTracker getInstance(WindowScope windowScope) {
		synchronized (TRACKERS) {
			TaskProgressTracker tracker = TRACKERS.get(windowScope);
			if (tracker == null) {
				tracker = new TaskProgressTracker();
				TRACKERS.put(windowScope, tracker);
			}
			return tracker;
		}
	}

	/** Adds a tool to the session-approved set. */
	public void addSessionApprovedTool(String toolName) {
		_sessionApprovedTools.add(toolName);
	}

	/** Returns the set of session-approved tool names. */
	public Set<String> getSessionApprovedTools() {
		return Set.copyOf(_sessionApprovedTools);
	}

	/** Loads session-approved tools (e.g., from persisted metadata). */
	public void loadSessionApprovedTools(Set<String> tools) {
		_sessionApprovedTools.clear();
		_sessionApprovedTools.addAll(tools);
	}

	/** Adds a skill ID to the loaded set. */
	public void addLoadedSkill(String skillId) {
		_loadedSkills.add(skillId);
	}

	/** Returns the set of loaded skill IDs. */
	public Set<String> getLoadedSkills() {
		return Set.copyOf(_loadedSkills);
	}

	/** Loads skill IDs from persisted metadata. */
	public void loadLoadedSkills(Set<String> skills) {
		_loadedSkills.clear();
		_loadedSkills.addAll(skills);
	}

	/** Adds token usage from a completed run. */
	public void addTokenUsage(TokenUsage usage) {
		_cumulativeUsage = _cumulativeUsage.add(usage);
	}

	/** Returns cumulative token usage. */
	public TokenUsage getCumulativeUsage() {
		return _cumulativeUsage;
	}

	/** Resets session-specific state (approvals, loaded skills, and tokens). */
	public void resetSessionState() {
		_sessionApprovedTools.clear();
		_loadedSkills.clear();
		_feedback.clear();
		_effort = null;
		_cumulativeUsage = TokenUsage.EMPTY;
	}

	/**
	 * Records, changes or withdraws the rating of one answer.
	 *
	 * @param turnIndex
	 *        zero-based index of the turn whose answer was rated
	 * @param rating
	 *        {@code "up"} or {@code "down"}; anything else withdraws an earlier rating
	 * @param note
	 *        free text the user added, or {@code null}
	 */
	public void setFeedback(int turnIndex, String rating, String note) {
		if (!"up".equals(rating) && !"down".equals(rating)) {
			_feedback.remove(Integer.valueOf(turnIndex));
			return;
		}
		_feedback.put(Integer.valueOf(turnIndex), MessageFeedbackDto.create()
			.setTurnIndex(turnIndex)
			.setRating(rating)
			.setNote(note == null ? "" : note)
			.setRecordedAt(DateTimeFormatter.ISO_INSTANT.format(Instant.now())));
	}

	/** Every rating of the active session, in turn order. */
	public List<MessageFeedbackDto> getFeedback() {
		List<MessageFeedbackDto> all = new ArrayList<>(_feedback.values());
		all.sort(Comparator.comparingInt(MessageFeedbackDto::getTurnIndex));
		return all;
	}

	/** Replaces the active session's ratings, e.g. when the session is loaded. */
	public void loadFeedback(List<MessageFeedbackDto> feedback) {
		_feedback.clear();
		if (feedback == null) {
			return;
		}
		for (MessageFeedbackDto entry : feedback) {
			_feedback.put(Integer.valueOf(entry.getTurnIndex()), entry);
		}
	}

	/**
	 * Drops the ratings of turns that no longer exist.
	 *
	 * @param turnCount
	 *        how many turns the session has left; ratings at or after this index are removed
	 */
	public void dropFeedbackFrom(int turnCount) {
		_feedback.keySet().removeIf(index -> index.intValue() >= turnCount);
	}

	/** Effort level last chosen in the active session, or {@code null}. */
	public String getEffort() {
		return _effort;
	}

	/** Remembers the effort level chosen for the active session. */
	public void setEffort(String effort) {
		_effort = effort == null || effort.isBlank() ? null : effort.trim();
	}

	/**
	 * Generates a unique task ID for tracking a processing request.
	 *
	 * @return A unique task ID.
	 */
	public String generateTaskId() {
		return UUID.randomUUID().toString();
	}

	/**
	 * State of one prompt-improvement request.
	 */
	public static final class PromptImprovementState {

		private final String _requestId;

		private final CancellationToken _cancellationToken;

		/**
		 * Creates a prompt-improvement state.
		 *
		 * @param requestId
		 *        Correlation ID of the browser request.
		 * @param cancellationToken
		 *        Token that cooperatively cancels this run.
		 */
		public PromptImprovementState(String requestId, CancellationToken cancellationToken) {
			_requestId = requestId;
			_cancellationToken = cancellationToken;
		}

		/** Returns the correlation ID of this request. */
		public String getRequestId() {
			return _requestId;
		}

		/** Returns the cancellation token of this run. */
		public CancellationToken getCancellationToken() {
			return _cancellationToken;
		}
	}
}
