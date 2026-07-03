/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.ai.chat.api.ConversationLogSerializer;
import com.top_logic.ai.chat.api.DtoMapper;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.Logger;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.memory.CompactionStrategy;
import com.top_logic.tl.ai.core.memory.ConversationMemory;
import com.top_logic.tl.ai.core.memory.SummarizingStrategy;
import com.top_logic.tl.ai.core.memory.TruncatingStrategy;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.provider.catalog.ModelInfo;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.ConversationLogReplay;
import com.top_logic.tl.ai.core.tools.DynamicToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolRegistry;
import com.top_logic.tl.ai.core.tools.task.TaskStore;

/**
 * Manages per-session in-memory state ({@link Chat} + {@link ConversationMemory}) with LRU caching.
 *
 * <p>
 * Each chat session gets its own {@link Chat} (message container) and {@link ConversationMemory}
 * (conversation context). When the user switches sessions, this manager persists the current
 * session, loads the target session from the database (or LRU cache), and rebuilds the AI's
 * conversation memory from the persisted messages.
 * </p>
 */
public class ChatSessionManager {

	/** Maximum number of sessions to keep in the in-memory LRU cache. */
	private static final int MAX_CACHED_SESSIONS = 5;

	/**
	 * Fallback context window budget for chat session memory, used only when no default model
	 * is configured. The budget is a placeholder until the first agent run: the agent runner
	 * re-aligns the memory with the effective per-agent context window (assignment override >
	 * model catalog) before each run.
	 */
	private static final int DEFAULT_CONTEXT_WINDOW_TOKENS = 128_000;

	/** Maximum output tokens for conversation summarization. */
	private static final int SUMMARIZATION_MAX_OUTPUT_TOKENS = 2048;

	private final Object _lock = new Object();

	/** Back-reference to the owning tracker for metadata serialization. */
	private volatile TaskProgressTracker _tracker;

	/** LRU cache of per-session state, bounded to {@link #MAX_CACHED_SESSIONS}. */
	private final Map<String, SessionState> _sessions = new LinkedHashMap<>(16, 0.75f, true) {
		@Override
		protected boolean removeEldestEntry(Map.Entry<String, SessionState> eldest) {
			return size() > MAX_CACHED_SESSIONS;
		}
	};

	/** The currently active session ID, or null if none. */
	private volatile String _activeSessionId;

	/**
	 * Per-session in-memory state: the chat messages and the AI's conversation memory.
	 */
	static class SessionState {
		final Chat _chat;

		ConversationMemory _conversationMemory;

		ConversationLog _conversationLog;

		TaskStore _taskStore;

		/**
		 * Session-scoped tool registry (typically a {@link DynamicToolCatalog}).
		 * Persists across agent runs so dynamically loaded tools survive between messages.
		 */
		ToolRegistry _toolRegistry;

		/** */
		SessionState(Chat chat, ConversationMemory conversationMemory) {
			_chat = chat;
			_conversationMemory = conversationMemory;
			_taskStore = new TaskStore();
		}
	}

	/**
	 * Sets the owning {@link TaskProgressTracker} for metadata persistence during session switches.
	 */
	void setTracker(TaskProgressTracker tracker) {
		_tracker = tracker;
	}

	/**
	 * Returns the active session's {@link Chat}, or a default empty chat if no session is active.
	 */
	public Chat getActiveChat() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				return state._chat;
			}
			// No active session — return a transient chat (will be replaced on session activation)
			return new Chat();
		}
	}

	/**
	 * Returns the active session's {@link ConversationMemory}, or creates a transient one if no
	 * session is active.
	 */
	public ConversationMemory getActiveConversationMemory() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._conversationMemory == null) {
					state._conversationMemory = createDefaultMemory();
				}
				return state._conversationMemory;
			}
			return createDefaultMemory();
		}
	}

	/**
	 * Returns the active session's {@link ConversationLog}, creating one if it does not exist.
	 * Returns a transient log if no session is active.
	 */
	public ConversationLog getOrCreateConversationLog() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._conversationLog == null) {
					state._conversationLog = new ConversationLog();
				}
				return state._conversationLog;
			}
			return new ConversationLog();
		}
	}

	/**
	 * Returns the active session's {@link TaskStore}. Returns a transient empty store if no
	 * session is active.
	 */
	public TaskStore getActiveTaskStore() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				if (state._taskStore == null) {
					state._taskStore = new TaskStore();
				}
				return state._taskStore;
			}
			return new TaskStore();
		}
	}

	/**
	 * Returns the active session's {@link ToolRegistry}, or {@code null} if none has been set.
	 */
	public ToolRegistry getActiveToolRegistry() {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			return state != null ? state._toolRegistry : null;
		}
	}

	/**
	 * Sets the active session's {@link ToolRegistry}.
	 */
	public void setActiveToolRegistry(ToolRegistry registry) {
		synchronized (_lock) {
			SessionState state = _activeSessionId != null ? _sessions.get(_activeSessionId) : null;
			if (state != null) {
				state._toolRegistry = registry;
			}
		}
	}

	/**
	 * Returns the active session ID, or null if none.
	 */
	public String getActiveSessionId() {
		return _activeSessionId;
	}

	/**
	 * Switches the active session. Persists the current session first, then loads the target session
	 * from the database (or LRU cache) and rebuilds the AI's conversation memory.
	 *
	 * @param sessionId
	 *        The session ID to switch to.
	 */
	public void switchSession(String sessionId) {
		if (sessionId == null) {
			return;
		}
		synchronized (_lock) {
			if (sessionId.equals(_activeSessionId)) {
				return;
			}

			// Persist current session before switching
			persistActiveSessionInternal();

			// Reset tracker state immediately so it is never retained from the previous session,
			// regardless of whether the new session is found in the LRU cache or loaded from DB.
			if (_tracker != null) {
				_tracker.resetSessionState();
			}

			// Check LRU cache first
			SessionState cached = _sessions.get(sessionId);
			if (cached != null) {
				_activeSessionId = sessionId;
				return;
			}

			// Load from database
			Chat chat = new Chat();
			ConversationMemory memory = createDefaultMemory();
			ConversationLog convLog = new ConversationLog();
			TaskStore loadedTaskStore = new TaskStore();

			try {
				TLObject session = ChatHistoryService.findSessionById(sessionId);
				if (session != null) {
					String json = ChatHistoryService.getMessagesJson(session);
					if (json != null && !json.isEmpty()) {
						if (json.trim().startsWith("{")) {
							convLog = ConversationLogSerializer.fromJson(json);
							ConversationLogReplay.rebuildMemory(convLog, memory.conversation());
						} else {
							Logger.warn("Session " + sessionId + " has legacy chat format. V2-only load ignores legacy data.",
								ChatSessionManager.class);
						}
					}

					if (_tracker != null) {
						String metadataJson = ChatHistoryService.getMetadataJson(session);
						DtoMapper.loadMetadata(metadataJson, _tracker);
						loadedTaskStore = DtoMapper.loadTaskStore(metadataJson);
					}
				}
			} catch (Exception ex) {
				Logger.error("Failed to load session " + sessionId + ": " + ex.getMessage(), ex,
					ChatSessionManager.class);
			}

			SessionState newState = new SessionState(chat, memory);
			newState._conversationLog = convLog;
			newState._taskStore = loadedTaskStore;
			_sessions.put(sessionId, newState);
			_activeSessionId = sessionId;
		}
	}

	/**
	 * Persists the active session's full state (messages) to the database.
	 */
	public void persistActiveSession() {
		synchronized (_lock) {
			persistActiveSessionInternal();
		}
	}

	private void persistActiveSessionInternal() {
		String sessionId = _activeSessionId;
		if (sessionId == null) {
			return;
		}
		SessionState state = _sessions.get(sessionId);
		if (state == null) {
			return;
		}
		try {
			TLObject session = ChatHistoryService.findSessionById(sessionId);
			if (session != null) {
				ConversationLog conversationLog = state._conversationLog;
				if (conversationLog == null) {
					conversationLog = new ConversationLog();
					state._conversationLog = conversationLog;
				}
				String json = ConversationLogSerializer.toJson(conversationLog);
				String metadataJson = _tracker != null ? DtoMapper.serializeMetadata(_tracker) : null;
				ChatHistoryService.saveMessagesAndMetadata(session, json, metadataJson);
			}
		} catch (Exception ex) {
			Logger.error("Failed to persist session " + sessionId + ": " + ex.getMessage(), ex,
				ChatSessionManager.class);
		}
	}

	/**
	 * Creates a default {@link ConversationMemory} with summarization strategy.
	 *
	 * <p>
	 * Uses the system's default model from {@link LLMService} for summarization.
	 * Falls back to truncation if no default model is configured or if the registry is not
	 * available. The initial context window is a placeholder; the effective per-agent window
	 * is applied by the agent runner before each run.
	 * </p>
	 */
	private static ConversationMemory createDefaultMemory() {
		CompactionStrategy strategy = createCompactionStrategy();
		int contextWindow = resolveContextWindow();
		return ConversationMemory.withDefaults(strategy, contextWindow);
	}

	private static int resolveContextWindow() {
		LLMService registry = LLMService.getInstance();
		if (registry == null) {
			return DEFAULT_CONTEXT_WINDOW_TOKENS;
		}
		String defaultModel = registry.getDefaultModel();
		if (defaultModel == null || defaultModel.isBlank()) {
			return DEFAULT_CONTEXT_WINDOW_TOKENS;
		}
		ModelInfo info = registry.getModel(defaultModel);
		if (info == null) {
			return DEFAULT_CONTEXT_WINDOW_TOKENS;
		}
		return info.limits().contextWindow();
	}

	private static CompactionStrategy createCompactionStrategy() {
		LLMService registry = LLMService.getInstance();
		if (registry == null) {
			Logger.warn("LLMService not available, falling back to truncation.", ChatSessionManager.class);
			return new TruncatingStrategy();
		}
		String defaultModel = registry.getDefaultModel();
		if (defaultModel == null || defaultModel.isBlank()) {
			Logger.warn("No default model configured, falling back to truncation.", ChatSessionManager.class);
			return new TruncatingStrategy();
		}
		LLMProvider provider;
		try {
			provider = registry.buildProvider(defaultModel);
		} catch (IllegalStateException ex) {
			Logger.warn("Default model '" + defaultModel + "' not found in any provider, falling back to truncation.",
				ChatSessionManager.class);
			return new TruncatingStrategy();
		}
		return new SummarizingStrategy(provider, defaultModel, SUMMARIZATION_MAX_OUTPUT_TOKENS);
	}
}
