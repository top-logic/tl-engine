/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.ai.chat.layout.ChatProcessor.AIProcessingState;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
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
		_cumulativeUsage = TokenUsage.EMPTY;
	}

	/**
	 * Generates a unique task ID for tracking a processing request.
	 *
	 * @return A unique task ID.
	 */
	public String generateTaskId() {
		return UUID.randomUUID().toString();
	}
}
